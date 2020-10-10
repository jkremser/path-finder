package cz.muni.fi.mobileIDS.src;

import cz.muni.fi.mobileIDS.src.forms.ChooseTimeTablesForm;
import cz.muni.fi.mobileIDS.src.forms.TimeTableForm;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author Jiri Kremser
 * 
 * Midlet used for viewing the time tables
 */
public class TimeTablesMidlet extends MIDlet implements CommandListener {

    private TimeTableForm timeTableForm;
    private ChooseTimeTablesForm chooseTimeTablesForm;
    private Alert alert;
    private static final LineUtils LU = LineUtils.getInstance();

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        switchDisplayable(null, getChooseTimeTablesForm());
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * getter for timeTableForm
     * @return Instance of TimeTableForm
     */
    public TimeTableForm getTimeTableForm() {
        if (timeTableForm == null) {
            timeTableForm = new TimeTableForm(this);
        }
        return timeTableForm;
    }

    /**
     * Returns an initiliazed instance of chooseTimeTablesForm component.
     * @return the initialized component instance
     */
    public ChooseTimeTablesForm getChooseTimeTablesForm() {
        if (chooseTimeTablesForm == null) {
            chooseTimeTablesForm = new ChooseTimeTablesForm(this);
        }
        return chooseTimeTablesForm;
    }

    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        Display display = getDisplay();
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == chooseTimeTablesForm) {
            if (command == getChooseTimeTablesForm().getExitCommand()) {
                exitMIDlet();
            } else if (command == getChooseTimeTablesForm().getShowCommand()) {
                if (!showTimeTable()) {
                    getAlert("Info", "Zadejte prosim korektni hodnoty", AlertType.INFO);
                    switchDisplayable(getAlert(), getChooseTimeTablesForm());
                }
            }
        } else if (displayable == timeTableForm) {
            if (command == getTimeTableForm().getBackCommand()) {
                System.gc();
                switchDisplayable(null, getChooseTimeTablesForm());
            } else if (command == getTimeTableForm().getExitCommand()) {
                exitMIDlet();
            }
        }
    }

    /**
     * Shows time table if everything is allright
     * @return true if everything is allright
     */
    public boolean showTimeTable() {
        int stationId = getChooseTimeTablesForm().getStationChoiceGroup().getSelectedIndex();
        int directionId = getChooseTimeTablesForm().getDirectionChoiceGroup().getSelectedIndex();
        char direction = (directionId == 0) ? 'A' : 'B';
        Station[] stations = getChooseTimeTablesForm().getStations();

        if ((getChooseTimeTablesForm().getLineTextField().size() > 0) &&
          (stationId != -1) &&
          (stationId < stations.length) &&
          (directionId != -1) &&
          (directionId < 2)) {
            int id = 0;
            try {
                id = Integer.parseInt(getChooseTimeTablesForm().getLineTextField().getString());
            } catch (NumberFormatException nfe) {
                return false;
            }
            String day = CommonUtils.getDay(getChooseTimeTablesForm().getDayDateField().getDate());
            new DeparturesThread(id, direction, stations[stationId], day, getTimeTableForm()).start();
            return true;

        } else {
            return false;
        }
    }

    private final class DeparturesThread extends Thread {

        private int line;
        private char direction;
        private int stationUid;
        private String stationName;
        private TimeTableForm target;
        private String day;

        public DeparturesThread(int line, char direction, Station station, String day, TimeTableForm target) {
            this.line = line;
            this.direction = direction;
            this.stationUid = station.getUid();
            this.stationName = station.getName();
            this.target = target;
            this.day = day;
        }

        public void run() {
            short[] departures = LU.getDepartures((short) line, direction, (short) stationUid, day);
            if (departures != null) {
                CommonUtils.sort(departures);
            }

            target.show(line, stationName, departures);
        }
    }

    /**
     * Returns an initiliazed instance of alert component.
     * @return the initialized component instance
     */
    public Alert getAlert() {
        if (alert == null) {
            alert = new Alert("", "", null, null);
            alert.setTimeout(1800);
        }
        return alert;
    }

    /**
     * Returns an initiliazed instance of alert component.
     * @param title Title of the alert
     * @param message Message of the alert
     * @param type Type of the alert
     * @return the initialized component instance
     */        
    public Alert getAlert(String title, String message, AlertType type) {
        getAlert().setTitle(title);
        getAlert().setString(message);
        getAlert().setType(type);
        return getAlert();
    }
}

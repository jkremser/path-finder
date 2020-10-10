package cz.muni.fi.mobileIDS.src;

import cz.muni.fi.mobileIDS.src.forms.DetailsForm;
import cz.muni.fi.mobileIDS.src.forms.ResultForm;
import cz.muni.fi.mobileIDS.src.forms.SearchForm;
import cz.muni.fi.mobileIDS.src.forms.WaitForm;
import cz.muni.fi.mobileIDS.src.threads.StationCompletionThread;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

/**
 *
 * @author Jiri Kremser
 * 
 * Midlet used for searching the links
 */
public class SearchMidlet extends MIDlet implements CommandListener {

    private static final String RECORD_STORE_PATH = "settings";
    private static final int LAST_STATIONS_ID = 1;
    private boolean midletPaused = false;
    private SearchForm searchForm;
    private ResultForm resultForm;
    private DetailsForm detailsForm;
    private WaitForm waitForm;
    private Alert alert;
    private Station from;
    private Station to;
    private RecordStore settings;
    private SearchThread searchThread;
    private volatile boolean settingsLoaded = false;

    /**
     * Constructor.
     */
    public SearchMidlet() {
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {
        switchDisplayable(null, getSearchForm());
        new LoadSettingsThread().start();
        while (!settingsLoaded) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        new StationCompletionThread(getSearchForm().getFromTextField().getString(), getSearchForm().getFromChoiceGroup()).complete(); //common method, because of the locking
        new StationCompletionThread(getSearchForm().getToTextField().getString(), getSearchForm().getToChoiceGroup()).start();
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {

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
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == detailsForm) {
            if (command == getDetailsForm().getBackCommand()) {
                switchDisplayable(null, getResultForm());
            } else if (command == getDetailsForm().getExitCommand()) {
                exitMIDlet();
            }
        } else if (displayable == resultForm) {
            if (command == getResultForm().getBackCommand()) {
                getSearchForm().setFromOk(false);
                getSearchForm().setToOk(false);
                System.gc();
                searchThread = null;
                switchDisplayable(null, getSearchForm());
            } else if (command == getResultForm().getDetailsCommand()) {
                getDetailsForm().showDetails(getResultForm().getLines(), getResultForm().getStations(), getResultForm().getJourneyTime());
            }
        } else if (displayable == searchForm) {
            if (command == getSearchForm().getExitCommand()) {
                exitMIDlet();
            } else if (command == getSearchForm().getSearchCommand()) {

                Calendar c = Calendar.getInstance();
                c.setTime(getSearchForm().getWhenDateField().getDate());

                //Validation
                String fromLabel = getSearchForm().getFromTextField().getString();
                String toLabel = getSearchForm().getToTextField().getString();
                getSearchForm().setFromOk(StationUtils.validateStation(fromLabel)); // is there just 1 proper statio which matches?
                getSearchForm().setToOk(StationUtils.validateStation(toLabel));

                if (!getSearchForm().isFromOk() || !getSearchForm().isToOk()) { // validation
                    String message = "";
                    if (!getSearchForm().isFromOk()) {
                        message += "Zastavka \"" + fromLabel + "\" neexistuje.\n\n";
                    }
                    if (!getSearchForm().isToOk()) {
                        message += "Zastavka \"" + toLabel + "\" neexistuje.\n\n";
                    }
                    getAlert("Spatne zadane hodnoty", message, AlertType.WARNING);
                    switchDisplayable(getAlert(), getSearchForm());
                    return;
                }

                from = StationUtils.getStation(fromLabel);
                to = StationUtils.getStation(toLabel);
                //main task
//                Searcher searcher = null;
//                Step step = null;
                try {
                    Date date = getSearchForm().getWhenDateField().getDate();
                    int timeInMin = CommonUtils.getTimeInMin(date);
                    String day = CommonUtils.getDay(date);
                    getSearchThread(timeInMin, day, from.getId(), to.getId()).start();
//                    searcher = new Searcher(timeInMin, day, from.getId(), to.getId());
//                    step = searcher.search();
//                    getResultForm().showResults(1, step);
                    switchDisplayable(null, getWaitForm());
                    getWaitForm().start();
                } catch (OutOfMemoryError oome) {
                    setSearchThread(null);
                    System.gc();
                    getAlert("Nedostatek pameti", "Aplikace bude ukoncena", AlertType.ERROR);
                    switchDisplayable(getAlert(), getSearchForm());
                    destroyApp(true);
                }
            }
        }
    }

    /**
     * Returns an initiliazed instance of searchForm component.
     * @return the initialized component instance
     */
    public SearchForm getSearchForm() {
        if (searchForm == null) {
            searchForm = new SearchForm(this);
        }
        return searchForm;
    }

    /**
     * Returns an initiliazed instance of resultForm component.
     * @return the initialized component instance
     */
    public ResultForm getResultForm() {
        if (resultForm == null) {
            resultForm = new ResultForm(this);
        }
        return resultForm;
    }

    /**
     * Returns an initiliazed instance of detailsForm component.
     * @return the initialized component instance
     */
    public DetailsForm getDetailsForm() {
        if (detailsForm == null) {
            detailsForm = new DetailsForm(this);
        }
        return detailsForm;
    }

    /**
     * Returns an initiliazed instance of waitForm component.
     * @return the initialized component instance
     */
    public WaitForm getWaitForm() {
        if (waitForm == null) {
            waitForm = new WaitForm(this, getSearchForm().getWidth());
        }
        return waitForm;
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

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
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
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            startMIDlet();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
        saveDetails();
    }

    /**
     * Load settings from RMS.
     */
    private class LoadSettingsThread extends Thread {

        public void run() {
            try {
                settings = RecordStore.openRecordStore(SearchMidlet.RECORD_STORE_PATH, true); // create if doesn't exist
                if (settings.getNumRecords() != 0) {
                    byte[] data = settings.getRecord(SearchMidlet.LAST_STATIONS_ID);
                    DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
                    getSearchForm().getFromTextField().setString(is.readUTF());
                    getSearchForm().getToTextField().setString(is.readUTF());
                    is.close();
                    is = null;
                    data = null;
                    settingsLoaded = true;
                }
            } catch (IOException ex) {
                getAlert("RMS problem", "Nelze otevrit uloziste zaznamu", AlertType.WARNING);
                switchDisplayable(getAlert(), getSearchForm());
            } catch (RecordStoreException ex) {
                getAlert("RMS problem", "Nelze pristupovat do uloziste zaznamu", AlertType.WARNING);
                switchDisplayable(getAlert(), getSearchForm());
            }
        }
    }

    /**
     * Save settings to RMS.
     */
    public void saveDetails() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        try {
            os.writeUTF(getSearchForm().getFromTextField().getString());
            os.writeUTF(getSearchForm().getToTextField().getString());
            os.close();
            byte[] data = baos.toByteArray();

            if (settings != null && settings.getSizeAvailable() > data.length) {
                if (settings.getNumRecords() == 0) {
                    settings.addRecord(data, 0, data.length);
                } else {
                    settings.setRecord(SearchMidlet.LAST_STATIONS_ID, data, 0, data.length);
                }
            }
        } catch (IOException ex) {
                getAlert("RMS problem", "Nelze otevrit uloziste zaznamu", AlertType.WARNING);
                switchDisplayable(getAlert(), getSearchForm());
        } catch (RecordStoreFullException ex) {
                getAlert("RMS problem", "Uloziste zaznamu je plne", AlertType.WARNING);
                switchDisplayable(getAlert(), getSearchForm());
        } catch (RecordStoreException ex) {
                getAlert("RMS problem", "Nelze pristupovat do uloziste zaznamu", AlertType.WARNING);
                switchDisplayable(getAlert(), getSearchForm());
        }
    }

    /**
     * Returns instance of SearchThread
     * @param min Minutes of day
     * @param day Day
     * @param fromId ID of start station
     * @param toId ID of end station
     * @return Instance of SearchThread
     */
    SearchMidlet.SearchThread getSearchThread(int min, String day, int fromId, int toId) {
        if (searchThread == null) {
            searchThread = new SearchMidlet.SearchThread(new Searcher(min, day, fromId, toId));
        }
        return searchThread;
    }

    /**
     * Returns instance of SearchThread
     */
    SearchMidlet.SearchThread getSearchThread() {
        return this.searchThread;
    }

    /**
     * Setter for searchThread
     * @param thread Instance of SearchThread
     */
    void setSearchThread(SearchMidlet.SearchThread thread) {
        this.searchThread = thread;
    }

    /**
     * Getter for fromStation
     * @return Start station
     */
    public Station getFrom() {
        return from;
    }

    /**
     * Getter for toStation
     * @return End station
     */
    public Station getTo() {
        return to;
    }

    /**
     * Getter for searchThread.done
     * @return true if search is done
     */
    public boolean isSearchDone() {
        return getSearchThread().isDone();
    }

    private class SearchThread extends Thread {

        private Searchable searcher;
        private boolean done;
        private Step step;
        private long time;

        public SearchThread(Searchable searcher) {
            this.searcher = searcher;
        }

        public void run() {
            long start = System.currentTimeMillis();
            step = searcher.search();
            time = System.currentTimeMillis() - start;
            done = true;
            getResultForm().showResults(time, step);
        }

        public boolean isDone() {
            return done;
        }

        public Step getStep() {
            return step;
        }

        public void setStep(Step step) {
            this.step = step;
        }

        public long getTime() {
            return time;
        }
    }
}

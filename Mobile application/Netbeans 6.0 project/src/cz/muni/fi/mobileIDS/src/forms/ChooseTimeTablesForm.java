package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import java.util.Calendar;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Jiri Kremser
 * 
 * Form used for choosing particular time table
 */
public class ChooseTimeTablesForm extends Form {

    private Command exitCommand;
    private TimeTablesMidlet sourceMidlet;
    private Command showCommand;
    private TextField lineTextField;
    private ChoiceGroup stationChoiceGroup;
    private ChoiceGroup directionChoiceGroup;
    private DateField dayDateField;
    private Station[] stations;

    /**
     * Constructor
     * 
     * @param sourceMidlet
     */
    public ChooseTimeTablesForm(TimeTablesMidlet sourceMidlet) {
        super("Vyber jizdniho radu");
        super.addCommand(getShowCommand());
        super.addCommand(getExitCommand());
        super.setCommandListener((CommandListener) sourceMidlet);
        super.append(getLineTextField());
        super.append(getStationChoiceGroup());
        super.append(getDirectionChoiceGroup());
        super.append(getDayDateField());
        super.setItemStateListener(new ChooseTimeTablesFormListener(this));
        this.sourceMidlet = sourceMidlet;
    }

    /**
     * getter for exitCommand
     * 
     * @return Command
     */
    public Command getExitCommand() {
        if (exitCommand == null) {
            exitCommand = new Command("Ukoncit", Command.SCREEN, 1);
        }
        return exitCommand;
    }

    /**
     * getter for showCommand
     * 
     * @return Command
     */    
    public Command getShowCommand() {
        if (showCommand == null) {
            showCommand = new Command("Zobrazit", 7, 0);
        }
        return showCommand;
    }

    /**
     * getter for dayDateField
     * 
     * @return DateField
     */    
    public DateField getDayDateField() {
        if (dayDateField == null) {
            dayDateField = new DateField("Den", DateField.DATE);
            Calendar calendar = Calendar.getInstance();
            dayDateField.setDate(calendar.getTime());
        }
        return dayDateField;
    }

    /**
     * getter for lineTextField
     * 
     * @return TextField
     */    
    public TextField getLineTextField() {
        if (lineTextField == null) {
            lineTextField = new TextField("Linka", "", 32, TextField.DECIMAL | TextField.NON_PREDICTIVE);
        }
        return lineTextField;
    }

    /**
     * getter for stationChoiceGroup
     * 
     * @return ChoiceGroup
     */    
    public ChoiceGroup getStationChoiceGroup() {
        if (stationChoiceGroup == null) {
            stationChoiceGroup = new ChoiceGroup("Zastavky", Choice.POPUP);
            stationChoiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            stationChoiceGroup.setPreferredSize(-1, -1);
        }
        return stationChoiceGroup;
    }

    /**
     * getter for directionChoiceGroup
     * 
     * @return ChoiceGroup
     */    
    public ChoiceGroup getDirectionChoiceGroup() {
        if (directionChoiceGroup == null) {
            directionChoiceGroup = new ChoiceGroup("Smer", Choice.POPUP);
            directionChoiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            directionChoiceGroup.setPreferredSize(-1, -1);
        }
        return directionChoiceGroup;
    }
    
    /**
     * getter for stations
     * 
     * @return Station[]
     */    
    public Station[] getStations() {
        return stations;
    }

    /**
     * setter for stations
     * 
     * @param stations
     */    
    public void setStations(Station[] stations) {
        this.stations = stations;
    }
}

package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import java.util.Calendar;
import java.util.TimeZone;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Jiri Kremser
 * 
 * Form used for typing in the search parameters
 */
public class SearchForm extends Form {

    private Command exitCommand;
    private Command searchCommand;
    private TextField fromTextField;
    private Spacer spacer;
    private TextField toTextField;
    private Spacer spacer1;
    private DateField whenDateField;
    private ChoiceGroup fromChoiceGroup;
    private ChoiceGroup toChoiceGroup;
    private Spacer spacer2;
    private boolean fromOk = false;
    private boolean toOk = false;
    

    /**
     * Constructor
     * 
     * @param sourceMidlet
     */
    public SearchForm(CommandListener sourceMidlet) {
        super("Vyhledavani linek");
        super.append(getFromTextField());
        super.append(getFromChoiceGroup());
        super.append(getSpacer());
        super.append(getToTextField());        
        super.append(getToChoiceGroup());
        super.append(getSpacer1());        
        super.append(getWhenDateField());
        super.append(getSpacer2());
        super.addCommand(getSearchCommand());        
        super.addCommand(getExitCommand());
        super.setCommandListener(sourceMidlet);
        super.setItemStateListener(new SearchFormListener(this));
    }

    
    /**
     * getter for fromChoiceGroup
     * 
     * @return ChoiceGroup
     */
    public ChoiceGroup getFromChoiceGroup() {
        if (fromChoiceGroup == null) {
            fromChoiceGroup = new ChoiceGroup("Zastavky", Choice.POPUP);
            fromChoiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            fromChoiceGroup.setPreferredSize(-1, -1);
        }
        return fromChoiceGroup;
    }

    /**
     * getter for fromTextField
     * 
     * @return TextField
     */
    public TextField getFromTextField() {
        if (fromTextField == null) {
            fromTextField = new TextField("Odkud", "", 32, TextField.INITIAL_CAPS_SENTENCE|TextField.NON_PREDICTIVE);
        }
        return fromTextField;
    }

    /**
     * getter for searchCommand
     * 
     * @return Command
     */    
    public Command getSearchCommand() {
        if (searchCommand == null) {
            searchCommand = new Command("Hledat", 7, 0);
        }
        return searchCommand;
    }
        
    /**
     * getter for exitCommand
     * 
     * @return Command
     */    
    public Command getExitCommand() {
        if (exitCommand == null) {
            exitCommand = new Command("Ukoncit", Command.SCREEN, 2);
        }
        return exitCommand;
    }
    
    /**
     * getter for spacer
     * 
     * @return Spacer
     */    
    public Spacer getSpacer() {
        if (spacer == null) {
            spacer = new Spacer(4, 4);
            spacer.setPreferredSize(4, 4);
            spacer.setLayout(ImageItem.LAYOUT_DEFAULT);
        }
        return spacer;
    }

    /**
     * getter for spacer1
     * 
     * @return Spacer
     */    
    public Spacer getSpacer1() { // pryc z getteru, dat to dovnitr
        if (spacer1 == null) {
            spacer1 = new Spacer(4, 4);
            spacer1.setPreferredSize(4, 4);
        }
        return spacer1;
    }
    
    /**
     * getter for spacer2
     * 
     * @return Spacer
     */        
    public Spacer getSpacer2() {
        if (spacer2 == null) {
            spacer2 = new Spacer(4, 4);
            spacer2.setPreferredSize(4, 4);
        }
        return spacer2;
    }
    
    /**
     * getter for toChoiceGroup
     * 
     * @return ChoiceGroup
     */        
    public ChoiceGroup getToChoiceGroup() {
        if (toChoiceGroup == null) {
            toChoiceGroup = new ChoiceGroup("Zastavky", Choice.POPUP);
            toChoiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            toChoiceGroup.setPreferredSize(-1, -1);
        }
        return toChoiceGroup;
    }

    /**
     * getter for toTextField
     * 
     * @return TextField
     */        
    public TextField getToTextField() {
        if (toTextField == null) {
            toTextField = new TextField("Kam", "", 32, TextField.INITIAL_CAPS_SENTENCE|TextField.NON_PREDICTIVE);
        }
        return toTextField;
    }

    /**
     * getter for whenDateField
     * 
     * @return DateField
     */        
    public DateField getWhenDateField() {
        if (whenDateField == null) {
            whenDateField = new DateField("Kdy", DateField.DATE_TIME,TimeZone.getDefault());
            Calendar calendar = Calendar.getInstance();
            whenDateField.setDate(calendar.getTime());
        }
        return whenDateField;
    }

    /**
     * getter for fromOk
     * 
     * @return boolean
     */
    public boolean isFromOk() {
        return fromOk;
    }

    /**
     * setter for fromOk
     * 
     * @param fromOk
     */
    public void setFromOk(boolean fromOk) {
        this.fromOk = fromOk;
    }

    /**
     * getter for toOk
     * 
     * @return boolean
     */    
    public boolean isToOk() {
        return toOk;
    }

    /**
     * setter for toOk
     * 
     * @param toOk
     */
    public void setToOk(boolean toOk) {
        this.toOk = toOk;
    }
    
}
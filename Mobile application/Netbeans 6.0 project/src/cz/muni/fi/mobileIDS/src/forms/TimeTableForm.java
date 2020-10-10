package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Jiri Kremser
 * 
 * Form used for showing timetables
 */
public class TimeTableForm extends Form {

    private Command exitCommand;
    private Command backCommand;
    private TimeTablesMidlet sourceMidlet;

/**
 * Constructor
 * 
 * @param sourceMidlet
 */    
    public TimeTableForm(TimeTablesMidlet sourceMidlet) {
        super("Jizdni rad");
        super.addCommand(getExitCommand());
        super.addCommand(getBackCommand());
        super.setCommandListener((CommandListener) sourceMidlet);
        this.sourceMidlet = sourceMidlet;
    }

    /**
     * shows the timetable
     */    
    public void show(int line, String station, short[] departures) {
        deleteAll();
        StringItem head = new StringItem("", "lin. " + line + "  zast. " + ((station.length() > 10)? station.substring(0,10) : station) + "\n");
//        head.setLayout(Item.LAYOUT_NEWLINE_AFTER|Item.LAYOUT_2);
        head.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        append(head);

        if (departures == null) {
            StringItem message = new StringItem("", "Zadne spoje");
            message.setLayout(Item.LAYOUT_VCENTER|Item.LAYOUT_2);
            message.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
            append(message);
            sourceMidlet.switchDisplayable(null, this);
            return;
        }
        
        StringBuffer[] lines = new StringBuffer[24];
        int hourId = 0;
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new StringBuffer();
            lines[i].append((i > 9) ? "" + i : "0" + i).append("|");
        }

        for (int i = 0; i < departures.length; i++) {
            int hoursNum = departures[i] / 60;
            int minutesNum = departures[i] - hoursNum * 60;
            String minutes = (minutesNum > 9) ? "" + minutesNum : "0" + minutesNum;
            while (hoursNum > hourId) {
                hourId++;
            }            
            lines[hourId].append(minutes).append(" ");
        }
        for (int i = 0; i < lines.length; i++) {
            StringItem item = new StringItem("", lines[i].toString()+"\n");
//            item.setLayout(Item.LAYOUT_NEWLINE_AFTER|Item.LAYOUT_2);
            item.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));        
            append(item);
        }
        sourceMidlet.switchDisplayable(null, this);
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
     * getter for backCommand
     * 
     * @return Command
     */        
    public Command getBackCommand() {
        if (backCommand == null) {
            backCommand = new Command("Zpet", 7, 0);
        }
        return backCommand;
    }
}

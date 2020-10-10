package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Jiri Kremser
 * 
 * Form used for showing details of the link
 */
public class DetailsForm extends Form {

    private Command backCommand;
    
    private Command exitCommand;
    private SearchMidlet sourceMidlet;

    /**
     * Constructor
     * 
     * @param source
     */    
    public DetailsForm(SearchMidlet source) {
        super("Detaily spoje");
        this.sourceMidlet = source;
        super.addCommand(getExitCommand());
        super.addCommand(getBackCommand());
        super.setCommandListener((CommandListener) sourceMidlet);
    }

    /**
     * getter for backCommand
     * 
     * @return Command
     */    
    public Command getBackCommand() {
        if (backCommand == null) {
            backCommand = new Command("Zpet", Command.BACK, 0);
        }
        return backCommand;
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
     * shows details about the link
     * 
     * @param lines
     * @param stations
     * @param journeyTime
     */    
    public void showDetails(Hashtable lines, Hashtable stations, int journeyTime) {
        deleteAll();
        Image tram = null;
        Image trol = null;
        Image bus = null;
        Image train = null;
        
        try {
            tram = Image.createImage(CommonUtils.IMAGE_TRAM);
            trol = Image.createImage(CommonUtils.IMAGE_TROLLEY_BUS);
            bus = Image.createImage(CommonUtils.IMAGE_BUS);
            train = Image.createImage(CommonUtils.IMAGE_TRAIN);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        StringItem item =  new StringItem("", "Linky\n");
        item.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD ,Font.SIZE_SMALL));
// some devices don't support        
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);

        append(item);
        Enumeration e = lines.keys();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            int typ = ((Integer) lines.get(name)).intValue();
            ImageItem ii = null;
            switch (typ) {
                case CommonUtils.ID_BUS1:
                    ii = new ImageItem(name, bus, ImageItem.LAYOUT_2|ImageItem.LAYOUT_LEFT|ImageItem.LAYOUT_NEWLINE_AFTER, "autobus");
                    break;
                case CommonUtils.ID_TRAIN1: case CommonUtils.ID_TRAIN2: 
                    ii = new ImageItem(name, train, ImageItem.LAYOUT_2|ImageItem.LAYOUT_LEFT|ImageItem.LAYOUT_NEWLINE_AFTER, "vlak");
                    break;
                case CommonUtils.ID_TRAM:
                    ii = new ImageItem(name, tram, ImageItem.LAYOUT_2|ImageItem.LAYOUT_LEFT|ImageItem.LAYOUT_NEWLINE_AFTER, "tramvaj");
                    break;
                case CommonUtils.ID_TROLLEY_BUS:
                    ii = new ImageItem(name, trol, ImageItem.LAYOUT_2|ImageItem.LAYOUT_LEFT|ImageItem.LAYOUT_NEWLINE_AFTER, "trolejbus");
                    break;                    
            }
            append(ii);
        }
        item = new StringItem(" "," \n");
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);
        item = new StringItem("Zona  Zastavka\n", "");
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        item.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD ,Font.SIZE_SMALL));
        append(item);
        
        int zoneCount = 0;
        Vector zones = new Vector(5);
        e = stations.keys();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            int zone = ((Integer) stations.get(name)).intValue();
            if (!zones.contains(new Integer(zone))) {
                zoneCount++;
                zones.addElement(new Integer(zone));
            }
            item = new StringItem(String.valueOf(zone+"   "), name+"\n");
//            item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
            item.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN ,Font.SIZE_SMALL));
            append(item);
        }
        item = new StringItem(" "," \n");
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);
        item = new StringItem("Celkova doba  ", String.valueOf(journeyTime) + " minut"+((journeyTime<5)?((journeyTime<2)?"a":"y"):""));
        item.setFont(Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN ,Font.SIZE_SMALL));
        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);
        item = new StringItem(" "," \n");
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);
        
        String ticket = DetailsForm.getSuitableTicket(zoneCount, journeyTime, lines.size());        
        item = new StringItem("Doporucena jizdenka\n", "");
//        item.setFont(Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN ,Font.SIZE_SMALL));
        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);
        item = new StringItem("", ticket+"\n");
        item.setFont(Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN ,Font.SIZE_SMALL));
//        item.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
        append(item);                
        
        sourceMidlet.switchDisplayable(null, this);
    }
    public static String getSuitableTicket(int zoneCount, int journeyTime, int lineCount) {
        if (journeyTime <= 10 && zoneCount <= 2 && lineCount == 1) {
            return "2 zony, 10min, neprestupni\ncena 8Kc/4Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 2) {
            return "2 zony, 40min, prestupni\ncena 13Kc/6Kc";
        } else if  (journeyTime <= 90 && zoneCount <= 3) {
            return "3 zony, 90min, prestupni\ncena 19Kc/9Kc";
        } else if  (journeyTime <= 90 && zoneCount <= 4) {
            return "4 zony, 90min, prestupni\ncena 24Kc/12Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 5) {
            return "5 zon, 120min, prestupni\ncena 29Kc/14Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 6) {
            return "6 zon, 120min, prestupni\ncena 34Kc/17Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 2) {
            return "7 zon, 150min, prestupni\ncena 39Kc/19Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 2) {
            return "8 zon, 150min, prestupni\ncena 44Kc/22Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 2) {
            return "9 zon, 180min, prestupni\ncena 49Kc/24Kc";
        } else if  (journeyTime <= 40 && zoneCount <= 2) {
            return "10 zon, 180min, prestupni\ncena 54Kc/27Kc";
        } else {
            return "2 zony, 40min, prestupni\ncena 59kc/29Kc";
        }
    }
    
}

package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import java.util.Hashtable;
import java.util.Stack;
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
 * Form used for results of the searching
 */
public class ResultForm extends Form {

    private Command detailsCommand;
    private Command backCommand;
    private StringItem header;
    private String headStation;
    private String headArrival;
    private String headDeparture;
    private String headLine;
    private int stationWidth;
    private int arrivalWidth;
    private int departureWidth;
    private SearchMidlet sourceMidlet;
    private Hashtable lines;
    private Hashtable stations;
    private int journeyTime;

    /**
     * Constructor
     * 
     * @param sourceMidlet
     */
    public ResultForm(SearchMidlet sourceMidlet) {
        super("Spoje");
        super.addCommand(getBackCommand());
        super.setCommandListener((CommandListener) sourceMidlet);
        this.sourceMidlet = sourceMidlet;
    }

    /**
     * shows header of the form
     */
    public void getHeader() {
        if (header == null) {
            int width = getWidth();
            Font mainFont = Font.getDefaultFont();
            Font headerFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
            int spaceWidth = mainFont.stringWidth(" ");

            headStation = "Zastavka   ";
            headArrival = "Prij.";
            headDeparture = "Odj. ";
            headLine = "Lin.";

            StringBuffer headTitle = new StringBuffer("Zastavka         Prij.      Odj.       Lin.");
            StringBuffer spaces = new StringBuffer("     ");

            while (headerFont.stringWidth(headTitle.toString()) > width && spaces.length() > 0) {
                headTitle.delete(0, headTitle.length());
                headTitle.append(headStation).append(spaces).append(headArrival).append(spaces);
                headTitle.append(headDeparture).append(spaces).append(headLine);
                spaces.deleteCharAt(0);
            }
            spaces.append(" ");

            while (headerFont.stringWidth(headTitle.toString()) + spaceWidth > width && headStation.length() > 4) {
                headTitle.delete(0, headTitle.length());
                headStation = headStation.substring(0, headStation.length() - 1);
                headTitle.append(headStation).append(spaces).append(headArrival).append(spaces);
                headTitle.append(headDeparture).append(spaces).append(headLine);
            }
            if (headStation.length() < 8) {
                headStation += ".";
            }
            headTitle.delete(0, headTitle.length());
            headTitle.append(headStation).append(spaces).append(headArrival).append(spaces);
            headTitle.append(headDeparture).append(spaces).append(headLine);

            if (headerFont.stringWidth(headTitle.toString()) + spaceWidth > width) {
                headArrival = "Pri.";
                headLine = "L.";
            }

            headTitle.delete(0, headTitle.length());
            headTitle.append(headStation).append(spaces).append(headArrival).append(spaces);
            headTitle.append(headDeparture).append(spaces).append(headLine);

            stationWidth = headerFont.stringWidth(headStation + spaces.toString());
            arrivalWidth = headerFont.stringWidth(headArrival + spaces.toString());
            departureWidth = headerFont.stringWidth(headDeparture + spaces.toString());

            header = new StringItem("", headTitle.toString());
            header.setFont(headerFont);
        }
        append(header);
    }

    /**
     * getter for detailsCommand
     * 
     * @return Command
     */        
    public Command getDetailsCommand() {
        if (detailsCommand == null) {
            detailsCommand = new Command("Detaily", 7, 0);
        }
        return detailsCommand;
    }

    /**
     * getter for backCommand
     * 
     * @return Command
     */        
    public Command getBackCommand() {
        if (backCommand == null) {
            backCommand = new Command("Zpet", Command.BACK, 1);
        }
        return backCommand;
    }

    /**
     * getter for sourceMidlet
     * 
     * @return SearchMidlet
     */        
    public SearchMidlet getSourceMidlet() {
        return sourceMidlet;
    }

    /**
     * shows the results
     * 
     * @param time
     * @param step
     */            
    public void showResults(long time, Step step) {
        deleteAll();
        //check null.. nothing found case..        
        if (step == null) {
            StringItem message = new StringItem("", "Spoj nebyl nalezen");
            message.setLayout(Item.LAYOUT_VCENTER | Item.LAYOUT_2);
            message.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
            append(message);
            sourceMidlet.switchDisplayable(null, this);
            return;
        }
        addCommand(getDetailsCommand());
        getHeader();

        SearchMidlet source = getSourceMidlet();

        //compatibility stuff
        int width = getWidth();
//        width = 129;
        
        //time stuff
        int startTime = CommonUtils.getTimeInMin(source.getSearchForm().getWhenDateField().getDate());

        Stack reverseSteps = new Stack();
        int lastLineId = -1;
        int lastDelay = -1;
        lines = new Hashtable(5);
        stations = new Hashtable(5);

        LineUtils lineUti = LineUtils.getInstance();


        while (step.getPrevious() != null) {
            if (lastLineId != step.getLineId()) {
                int lastLineIdFoo = step.getLineId();
                int lastDelayFoo = step.getDelay();
                step.setLineId((short) lastLineId);
                lastLineId = lastLineIdFoo;
                step.setDelay((short) lastDelay);
                lastDelay = lastDelayFoo;
                if (step.getLineId() != -1) {
                    lines.put(LineUtils.getLineName(step.getLineId()), new Integer(lineUti.getLineType(step.getLineId())));
                }
                stations.put(StationUtils.getStationById(step.getStationId()).getName(), new Integer(StationUtils.getStationById(step.getStationId()).getZone()));

                reverseSteps.push(step);
            } else {
                lastDelay = step.getDelay();
            }
            step = step.getPrevious();
        }
        step = new Step((short) source.getFrom().getId(), (short) lastLineId, (short) 0, (short) lastDelay, null);
        lines.put(LineUtils.getLineName(step.getLineId()), new Integer(lineUti.getLineType(step.getLineId())));
        stations.put(StationUtils.getStationById(step.getStationId()).getName(), new Integer(StationUtils.getStationById(step.getStationId()).getZone()));

        reverseSteps.push(step);
        journeyTime = 0;
        int shift = 0;
        boolean firstLoop = true;
        while (!reverseSteps.empty()) {
            step = (Step) reverseSteps.pop();
            if (!firstLoop) {
                if (step.getDelay() != -1) {
                    journeyTime += step.getDelay();
                }
                journeyTime += step.getArrivalTime();
            }
            firstLoop = false;
            step.setArrivalTime((short) (step.getArrivalTime() + shift));

            shift = (step.getDelay() == -1) ? 0 : step.getDelay() + step.getArrivalTime();
            //step
            append(step.toStringItem(startTime, headLine, stationWidth, arrivalWidth, departureWidth, width));
        }
        if (width < 130) {
            StringItem message = new StringItem("", "\nOmluvte snizenou kvalitu zobrazeni zapricinenou malou sirkou vaseho displeje ("+ width+"px).\n");
            message.setLayout(Item.LAYOUT_VCENTER | Item.LAYOUT_2);
            message.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
            append(message);
        }

//        append(new StringItem("", "\n\nVypocet trval " + time + " ms."));
        source.switchDisplayable(null, this);
    }

    /**
     * getter for journeyTime
     * 
     * @return int
     */            
    public int getJourneyTime() {
        return journeyTime;
    }
    
    /**
     * getter for lines
     * 
     * @return Hashtable
     */            
    public Hashtable getLines() {
        return lines;
    }

    /**
     * getter for stations
     * 
     * @return Hashtable
     */                
    public Hashtable getStations() {
        return stations;
    }
}

package cz.muni.fi.mobileIDS.src;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Jiri Kremser
 * 
 * Class which represents a connection between two station in search process
 */
public class Step {

    private short stationId;
    private short lineId;
    private short arrivalTime;
    private short delay;
    private Step previous;

    /**
     * Constructor
     * @param stationId ID of current station
     * @param lineId ID of current line
     * @param arrivalTime Time of a journey
     * @param delay Time of waiting to a new line
     * @param previous Previous step
     */
    public Step(short stationId, short lineId, short arrivalTime, short delay, Step previous) {
        this.stationId = stationId;
        this.lineId = lineId;
        this.delay = delay;
        this.previous = previous;
        this.arrivalTime = arrivalTime;
    }

    /**
     * getter for delay
     * @return Time of waiting to a new line
     */
    public short getDelay() {
        return delay;
    }

    /**
     * setter for delay
     * @param delay Time of waiting to a new line
     */    
    public void setDelay(short delay) {
        this.delay = delay;
    }

    /**
     * getter for lineId
     * @return ID of the line
     */    
    public short getLineId() {
        return lineId;
    }

    /**
     * setter for lineId
     * @param lineId ID of the line
     */    
    public void setLineId(short lineId) {
        this.lineId = lineId;
    }

    /**
     * getter for stationId
     * @return ID of the station
     */    
    public short getStationId() {
        return stationId;
    }
    
    /**
     * setter for stationId
     * @param parentId ID of the station
     */    
    public void setStationId(short parentId) {
        this.stationId = parentId;
    }

    /**
     * getter for previous step
     * @return Previous stem
     */    
    public Step getPrevious() {
        return previous;
    }

    /**
     * setter for previous step
     * @param previous Previous stem
     */    
    public void setPrevious(Step previous) {
        this.previous = previous;
    }

    /**
     * getter for arrival time
     * @return Time of a journey
     */    
    public short getArrivalTime() {
        return arrivalTime;
    }

    /**
     * setter for arrival time
     * @param arrivalTime Time of a journey
     */    
    public void setArrivalTime(short arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
        
    /**
     * Returns the StringItem with information of the step
     * @return StringItem with information of the step
     */
    public StringItem toStringItem(int startTime, String headLine, int stationWidth, int arrivalWidth, int departureWidth, int width) {

        Font mainFont = Font.getDefaultFont();
        int spaceWidth = mainFont.stringWidth(" ");
        
        String station = StationUtils.getStationById(getStationId()).getName();
        String arrival = (getPrevious() == null) ? "" : CommonUtils.getTimeFromMin(startTime + getArrivalTime());
        String departure = (getDelay() == -1) ? "" : CommonUtils.getTimeFromMin(startTime + getArrivalTime() + getDelay());
        String line = (getLineId() != -1) ? LineUtils.getLineName(getLineId()) : "  ";
        if (!"L.".equals(headLine)) {
            if (line.length() == 1) {
                line = "   " + line;
            } else if (line.length() == 2) {
                line = "  " + line;
            } else if (line.length() == 3) {
                line = " " + line;
            }
        }


        //reduce            
        while (mainFont.stringWidth(station) + spaceWidth > stationWidth) {
            station = station.substring(0, station.length() - 1);
        }
        //enlarge
        while (mainFont.stringWidth(station) + 2*spaceWidth < stationWidth) {
            station += " ";
        }

        //enlarge
        while (mainFont.stringWidth(arrival) < arrivalWidth) {
            arrival += " ";
        }
        //enlarge
        if (!"L.".equals(headLine)) {
            while (mainFont.stringWidth(departure) + spaceWidth < departureWidth) {
                departure += " ";
            }
        } else {
            departure += " ";
        }

        String wholeLine = station + arrival + " " + departure + line+"\n";

        StringItem si = new StringItem("", wholeLine);
        si.setPreferredSize(width, 15);
        si.setLayout(Item.LAYOUT_VCENTER);
        return si;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Step other = (Step) obj;
        if (this.stationId != other.stationId) {
            return false;
        }
        if (this.lineId != other.lineId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.stationId;
        hash = 53 * hash + this.lineId;
        return hash;
    }
    
    
}

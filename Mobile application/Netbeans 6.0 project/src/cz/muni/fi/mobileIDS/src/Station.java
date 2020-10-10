package cz.muni.fi.mobileIDS.src;

/**
 *
 * @author Jiri Kremser
 * 
 * Class which represents a stations
 */
public final class Station {
    
    private String name;
    private short id; // index in the array
    private short uid;// id in the system
    private final short[] lines;
    private short length = Short.MAX_VALUE;
    private short lineId;
    private char lineDirection;
    private short zone;

    /**
     * Constructor
     * @param id ID of the station
     * @param uid UID of the station
     * @param name Name of the station
     * @param lines Array of lines which belongs to the station
     * @param zone Zone of the station
     */
    public Station(short id, short uid, String name, short[] lines, short zone) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.lines = lines; //shallow
        this.zone = zone;
    }

    /**
     * getter for id
     * @return ID of the station
     */
    public short getId() {
        return id;
    }

    /**
     * getter for lines
     * @return Array of lines which belongs to the station
     */    
    public short[] getLines() {
        return lines;
    }

    /**
     * getter for name
     * @return Name of the station
     */    
    public String getName() {
        return name;
    }

    /**
     * getter for length
     * @return Number of minutes from start station
     */    
    public short getLength() {
        return length;
    }

    /**
     * setter for length
     * @param length Number of minutes from start station
     */        
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * getter for lineId
     * @return ID of the line which went to this stations as first
     */        
    public short getLineId() {
        return lineId;
    }

    /**
     * setter for lineId
     * @param lineId ID of the line which went to this stations as first
     */            
    public void setLineId(short lineId) {
        this.lineId = lineId;
    }

    /**
     * getter for lineDirection
     * @return Direction of the line which went to this stations as first
     */        
    public char getLineDirection() {
        return lineDirection;
    }

    /**
     * setter for lineDirection
     * @param lineDirection Direction of the line which went to this stations as first
     */        
    public void setLineDirection(char lineDirection) {
        this.lineDirection = lineDirection;
    }
    
    /**
     * getter for zone
     * @return Zone of the station
     */
    public short getZone() {
        return zone;
    }

    /**
     * setter for zone
     * @param zone Zone of the station
     */    
    public void setZone(short zone) {
        this.zone = zone;
    }
    
    /**
     * evaluate the station with proper length
     * @param parrent Parrent station
     * @param line Line which arrived at this station
     */    
    public void evalLength(Station parrent, Line line) {
        int alt = line.getNextDepartureInMin() + parrent.getLength() + line.getDelays()[line.getCurrentStationId()-1];        
        if (alt < getLength()) {
            setLength((short)alt);
        }
    }
    
    public String toString() {
        return getLength() + "zastavka " + getName() + " ohodnocena dobou " + getLength() +" min";
    }

    /**
     * getter for uis
     * @return UID of the station
     */    
    public short getUid() {
        return uid;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Station other = (Station) obj;
        if (this.uid != other.uid) {
            return false;
        }
        if (this.lineId != other.lineId) {
            return false;
        }
        if (this.lineDirection != other.lineDirection) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.uid;
        hash = 43 * hash + this.lineId;
        hash = 43 * hash + this.lineDirection;
        return hash;
    }


    /**
     * clone method (MIDP 2.0 doest't support clonable objects)
     * @return Copy of the station
     */
    public Station clone() {
        return new Station(id, uid, name, lines, zone);
    }    
}

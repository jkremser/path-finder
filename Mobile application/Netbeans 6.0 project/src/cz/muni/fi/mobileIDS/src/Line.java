package cz.muni.fi.mobileIDS.src;

/**
 *
 * @author Jiri Kremser
 * 
 * Class which represents a lines
 */
public final class Line {
    
    private short id;
    private final short[] stations;
    private final short[] delays;
    private short journeyTime;
    private short nextDepartureInMin;
    private byte currentStationId;
    private char direction;
    
    /**
     * Constructor
     * 
     * @param id ID of the line
     * @param stations Array of stations which belongs to the line
     * @param  delays Array of delays among stations
     */
    public Line(short id, short[] stations, short[] delays) {
        this.id = id;
        this.stations = stations;
        this.delays = delays;
        this.journeyTime = 0;
    }

    /**
     * getter for delays
     * 
     * @return Array of delays among stations
     */
    public short[] getDelays() {
        return delays;
    }

    /**
     * getter for id
     * 
     * @return ID of the line
     */    
    public short getId() {
        return id;
    }

    /**
     * getter for stations
     * 
     * @return Array of stations which belongs to the line
     */    
    public short[] getStations() {
        return stations;
    }

    /**
     * getter for nextDepartureInMin
     * 
     * @return Next departure of the line in minutes
     */    
    public short getNextDepartureInMin() {
        return nextDepartureInMin;
    }

    /**
     * setter for nextDepartureInMin
     * 
     * @param nextDepartureInMin Next departure of the line in minutes
     */    
    public void setNextDepartureInMin(short nextDepartureInMin) {
        this.nextDepartureInMin = nextDepartureInMin;
    }

    /**
     * getter for direction
     * 
     * @return Direction of the line ('A'/'B')
     */    
    public char getDirection() {
        return direction;
    }

    /**
     * setter for direction
     * 
     * @param direction Direction of the line ('A'/'B')
     */    
    public void setDirection(char direction) {
        this.direction = direction;
    }
       
    /**
     * getter for currentStationId
     * 
     * @return Index in stations array of the current station 
     */
    public byte getCurrentStationId() {
        return currentStationId;
    }

    /**
     * setter for currentStationId
     * 
     * @param currentStationId Index in stations array of the current station 
     */    
    public void setCurrentStationId(byte currentStationId) {
        this.currentStationId = currentStationId;
    }

    /**
     * getter for journeyTime
     * 
     * @return Journey time in minutes
     */    
    public short getJourneyTime() {
        return journeyTime;
    }

    /**
     * setter for journeyTime
     * 
     * @param journeyTime Journey time in minutes
     */    
    public void setJourneyTime(short journeyTime) {
        this.journeyTime = journeyTime;
    }

    /**
     * equals method
     * 
     * @param obj Object to compare
     */    
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Line other = (Line) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        return true;
    }

    /**
     * hashCode method
     */        
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.id;
        hash = 67 * hash + this.direction;
        return hash;
    }
    
    /**
     * toString method
     */            
    public String toString() {
        return nextDepartureInMin + "  id="+id+""+direction+", curStation="+stations[currentStationId];
    }
}
package cz.muni.fi.mobileIDS.src;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 *
 * @author Jiri Kremser
 * 
 * Utils and constants for working with line data
 */
public final class LineUtils {

    //max number of stations which belong to 1 line
    private static final int MAX_STATIONS_ON_LINE_COUNT = 55;
    //max number of records in the time table (departures per day)
    private static final int MAX_ROUTES_COUNT = 65;
    //max number of lines
    private static final String LOCAL_PREFIX = "../" + CommonUtils.RESOURCE_PREFIX_PATH;
    //max number of lines
    private static final String LINES_PATH = LOCAL_PREFIX + "lines";    
    //prefix of day to the resources
    private String day;
    //days
    static final String SUNDAY = "SU/";
    static final String MONDAY = "MO/";
    static final String TUESDAY = "TU/";
    static final String WEDNESDAY = "WE/";
    static final String THURSDAY = "TH/";
    static final String FRIDAY = "FR/";
    static final String SATURDAY = "SA/";
    public static final String[] DAYS = {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY};
    //separator between route and departures
    private static final byte SEPARATOR1 = (byte) 0x81;
    //separator between different routes
    private static final byte SEPARATOR2 = (byte) 0xFE;
    private static final Line NULL_LINE = new Line((short) -1, null, null);
    private static final int MAX_DEPARTURES_ON_LINE = 300;
    //singleton instance
    private static final LineUtils INSTANCE = new LineUtils();
    private static Hashtable lineNames;

    /**
     * Factory method
     * 
     * @return Singleton instance of the class
     */
    public static LineUtils getInstance() {
        return LineUtils.INSTANCE;
    }

    private LineUtils() {
        day = FRIDAY;
        lineNames = new Hashtable(25);
        InputStreamReader reader = new InputStreamReader(LineUtils.class.getResourceAsStream(LINES_PATH));
        char[] buffer = new char[32];
        StringBuffer sb = new StringBuffer(1000);
        int charCount;
        try {
            while ((charCount = reader.read(buffer, 0, buffer.length)) > -1) { // EOF=-1
                sb.append(buffer, 0, charCount);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String linesString = new String(sb);
        int lineTerminator = 0;
        int offset = 0;
        while ((lineTerminator = linesString.indexOf('\n', offset)) != -1) {
            lineNames.put(new Integer(Integer.parseInt(linesString.substring(offset,offset+3))), linesString.substring(offset+3,lineTerminator));
            offset = lineTerminator + 1;                
        }
    }

    /**
     * returns type of the line
     * 
     * @param id ID of the line
     * @return Type of the line
     */
    public short getLineType(short id) {
        StringBuffer strBuf = new StringBuffer(16);
        strBuf.append(LOCAL_PREFIX).append(FRIDAY).append(LineUtils.idToStringBuffer(id)).append('A');
        InputStream reader = getClass().getResourceAsStream(strBuf.toString());
        strBuf = null;
        if (reader == null) {
            return -1;
        }

        byte type;
        try {
            if ((type = (byte) reader.read()) > -1) {
                return type;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return -1; //else
    }

    /**
     * translates ID to XXX form
     * 
     * @return ID in XXX form
     */
    private static StringBuffer idToStringBuffer(short id) {
        StringBuffer sid = new StringBuffer(3);
        if (id < 100) {
            sid.append('0');
        }
        if (id < 10) {
            sid.append('0');
        }
        return sid.append(String.valueOf(id));
    }

    private static byte[] getDocument(InputStream reader) {

        byte[] buffer = new byte[32];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int charCount;
        try {
            while ((charCount = reader.read(buffer, 0, buffer.length)) > -1) { // EOF=-1
                baos.write(buffer, 0, charCount);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return baos.toByteArray();
    }

    private static String getNextDay(String day) {
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equals(day)) {
                if (i == DAYS.length - 1) {
                    return DAYS[0]; //Sunday
                } else {
                    return DAYS[i + 1];
                }
            }
        }
        return null;
    }

    /**
     * returns the line
     * 
     * @param id ID of the line
     * @param stationId ID of the station
     * @param time Time
     * @param direction Direction of the line
     * @return Instance of Line
     */
    public Line getLine(short id, short stationId, int time, char direction) {
        return getLine(id, stationId, time, direction, day);
    }

    private Line getLine(short id, short stationId, int time, char direction, String day) {
        return getLine(id, stationId, time, direction, day, 0, false);
    }

    private Line getLine(short id, short stationId, int time, char direction, String day, int shift, boolean nextDay) {

        StringBuffer strBuf = new StringBuffer(16);
        strBuf.append(LOCAL_PREFIX).append(day).append(LineUtils.idToStringBuffer(id)).append(direction);
        InputStream reader = getClass().getResourceAsStream(strBuf.toString());
        strBuf = null;
        if (reader == null) {
            return NULL_LINE;
        }

        byte[] doc = getDocument(reader);
        //1st part of the file (stations and delays between stations)        
        short[] stationsLob = new short[LineUtils.MAX_STATIONS_ON_LINE_COUNT];
        short[] delaysLob = new short[LineUtils.MAX_STATIONS_ON_LINE_COUNT - 1];
        short[][] allStations = new short[LineUtils.MAX_ROUTES_COUNT][];
        short[][] allDelays = new short[LineUtils.MAX_ROUTES_COUNT][];

        byte c;
        int counter = 1;

        short globalMinDeparture = Short.MAX_VALUE;
        int globalMinIndex = 0;
        int routeIndex = 0;
        int curStationId = 0;
        boolean isStationPresent = false;

        while (doc[counter] != -1) {

            int i = 0;
            int j = 0;
            boolean searchForDepartures = false;
            while ((c = doc[counter++]) != LineUtils.SEPARATOR1) {
                stationsLob[i] = (short) (c << 7 | doc[counter++]);
                if (stationsLob[i++] == stationId) {
                    searchForDepartures = true;
                }
                if ((c = doc[counter++]) != LineUtils.SEPARATOR1) {
                    delaysLob[j++] = c;
                } else {
                    break;
                }
            }
            short[] stations = new short[i];
            short[] delays = new short[j];

            System.arraycopy(stationsLob, 0, stations, 0, i); //trim
            System.arraycopy(delaysLob, 0, delays, 0, j); //trim            

            allStations[routeIndex] = stations;
            allDelays[routeIndex] = delays;

            //departures of the route
            short depTime = 0;

            if (searchForDepartures) {
                isStationPresent = true;
                int l;
                short delay = 0;
                for (l = 0; l < stations.length - 1; l++) { // time from end station to the station //TODO posledni prvek
                    if (stations[l] != stationId) {
                        delay += delays[l];
                    } else {
                        break;
                    }
                }
                while (true) {
                    if ((c = doc[counter++]) == LineUtils.SEPARATOR2) {
                        counter--;
                        break;
                    }
                    byte d;
                    if ((d = doc[counter++]) == LineUtils.SEPARATOR2) {
                        counter--;
                        break;
                    }

                    depTime = (short) (c << 7 | d);
                    if (depTime + delay >= time) {
                        if (globalMinDeparture > depTime + delay) {
                            globalMinDeparture = (short) (depTime + delay);
                            globalMinIndex = routeIndex;
                            curStationId = l;
                        }
                        break; //OK
                    }
                }
            }
            if (doc[counter] == -1) {
                break;
            }
            //skip departures
            while (doc[counter] != LineUtils.SEPARATOR2) {
                counter++;
            }
            routeIndex++;
            counter++; //skip separator2
        }

        try {
            reader.close();
        } catch (IOException ex) {
        //TODO
        }
        //        sb = null;
        Line line = null;
        if (globalMinDeparture != Short.MAX_VALUE) {
            line = new Line(id, allStations[globalMinIndex], allDelays[globalMinIndex]); //naco
            line.setCurrentStationId((byte) curStationId);
            line.setNextDepartureInMin((short) ((globalMinDeparture - time) + shift));
            line.setDirection(direction);
        } else {
            if (isStationPresent && !nextDay) {
                line = getLine(id, stationId, 0, direction, getNextDay(day), 24 * 60 - time, true); // its before midnight
            } else {
                return NULL_LINE;
            }
        }
        reader = null;
        allDelays = null;
        allStations = null;
        delaysLob = null;
        stationsLob = null;
        doc = null;
        return line;
    }

    /**
     * returns the array with the stations for the line
     * 
     * @param id ID of the line
     * @param day Day
     * @return Array with the stations for the line
     */
    public Station[] getStations(short id, String day) {
        StringBuffer strBuf = new StringBuffer(16);
        strBuf.append(LOCAL_PREFIX).append(day).append(LineUtils.idToStringBuffer(id)).append('A');
        InputStream reader = getClass().getResourceAsStream(strBuf.toString());
        strBuf = null;
        if (reader == null) {
            return null;
        }

        byte[] buffer = new byte[32];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int charCount;
        try {
            while ((charCount = reader.read(buffer, 0, buffer.length)) > -1) { // EOF=-1
                baos.write(buffer, 0, charCount);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte[] doc = baos.toByteArray();

        short[] stationsLob = new short[LineUtils.MAX_STATIONS_ON_LINE_COUNT];

        byte c;
        int counter = 1;
        Station[] stations = null;
        int i = 0;
        while ((c = doc[counter++]) != SEPARATOR1) {
            stationsLob[i++] = (short) (c << 7 | doc[counter++]);
            if (doc[counter++] == SEPARATOR1) {
                break;
            }
        }

        short[] idStations = new short[i];

        System.arraycopy(stationsLob, 0, idStations, 0, i); //truncate
        stationsLob = null;
        stations = new Station[i];
        for (int j = 0; j < idStations.length; j++) {
            stations[j] = StationUtils.getStationByUid(idStations[j]);
        }
        idStations = null;

        return stations;
    }

    /**
     * returns the array with the departures of the line at the station
     * 
     * @param line ID of the line
     * @param direction Direction of the line
     * @param station ID of the station
     * @param day Day
     * @return Array with the departures of the line at the station
     */
    public short[] getDepartures(short line, char direction, short station, String day) {
        StringBuffer strBuf = new StringBuffer(16);
        strBuf.append(LOCAL_PREFIX).append(day).append(LineUtils.idToStringBuffer(line)).append(direction);
        InputStream reader = getClass().getResourceAsStream(strBuf.toString());
        strBuf = null;
        if (reader == null) {
            return null;
        }
        byte[] doc = getDocument(reader);
        short[] departuresLob = new short[LineUtils.MAX_DEPARTURES_ON_LINE];
        int counter = 1;
        int depInd = 0;
        while (doc[counter] != -1) {

            short delay = 0;
            boolean searchForDepartures = false;
            byte c, d;
            while (true) {
                if (((c = doc[counter++]) == SEPARATOR1) || ((d = doc[counter++]) == SEPARATOR1)) {
                    break;
                }
                if ((c << 7 | d) != station) {
                    if (doc[counter] == SEPARATOR1) {
                        counter++;
                        break;
                    }
                    delay += doc[counter++];
                } else {
                    searchForDepartures = true;
                    break;
                }
            }

            //departures of the route
            if (searchForDepartures) {
                //skip the rest of the stations
                while (doc[counter] != LineUtils.SEPARATOR1) {
                    counter++;
                }
                counter++;
                while (true) {
                    if (((c = doc[counter++]) == SEPARATOR2) || ((d = doc[counter++]) == SEPARATOR2)) {
                        counter--;
                        break;
                    }
                    departuresLob[depInd++] = (short) ((c << 7 | d) + delay);
                }
            } else {
                //skip departures
                while (doc[counter] != LineUtils.SEPARATOR2) {
                    counter++;
                }
            }

            counter++; //skip separator2
        }

        try {
            reader.close();
        } catch (IOException ex) {
        //TODO
        }
        short[] departures = new short[depInd];
        System.arraycopy(departuresLob, 0, departures, 0, depInd);
        departuresLob = null;

        reader = null;
        doc = null;
        return departures;
    }

    /**
     * returns name of the line
     * @return Name of the line
     */    
    public static String getLineName(int id) {
        if (lineNames.containsKey(new Integer(id))) {
            return (String) lineNames.get(new Integer(id));
        } else return String.valueOf(id);
    }

    /**
     * getter for day
     * @return Day
     */
    public String getDay() {
        return day;
    }

    /**
     * setter for day
     * @param day Day
     */
    public void setDay(String day) {
        this.day = day;
    }
}

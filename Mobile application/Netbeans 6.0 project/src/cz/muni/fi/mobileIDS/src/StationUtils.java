package cz.muni.fi.mobileIDS.src;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Jiri Kremser
 * 
 * Utils and constants for working with station data
 */
public final class StationUtils {
    
    
    //max number of stations
    private static final int MAX_STATION_COUNT = 2500;
    //max number of suitable stations (stations which starts with needed string)
    private static final int MAX_SUITABLE_STATION_COUNT = 100;
    //max number of lines on 1 station
    private static final int MAX_LINES_ON_STATION_COUNT = 40;
    //path to the resource with stations
    private static final String STATIONS_RESOURCE_PATH = "../_data/stations";
    //separator between 2 different stations
    private static final char STRING_SEPARATOR = '\n';
    //separator between name of the station and it's lines
    private static final char STATIONS_SEPARATOR = '@';
    private static Station[] stations;
    
    //singleton instance (runs constructor and fills the station array when class load)
    private static final StationUtils INSTANCE = new StationUtils();

//    public static StationUtils getInstance() {
//        return StationUtils.INSTANCE;
//    }
    
    private StationUtils() {
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(StationUtils.STATIONS_RESOURCE_PATH));
        char[] buffer = new char[32];
        StringBuffer sb = new StringBuffer(StationUtils.MAX_STATION_COUNT * 10);
        int charCount;
        try {
            while ((charCount = reader.read(buffer, 0, buffer.length)) > -1) { // EOF=-1
                sb.append(buffer, 0, charCount);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String stationsString = new String(sb);
        short[] linesOnStationBlob = new short[StationUtils.MAX_LINES_ON_STATION_COUNT];
        short[] linesOnStation = null;
        Station[] stationsBlob = new Station[StationUtils.MAX_STATION_COUNT];

        int offsetStation = -1;
        int offsetString = -1;
        int stationTerminatorIndex;
        int stringTerminatorIndex;
        int i = 0;
        while ((stationTerminatorIndex = stationsString.indexOf(StationUtils.STRING_SEPARATOR, offsetStation + 1)) != -1) {
            //save id
            String id = stationsString.substring(offsetStation + 1, offsetStation + 5);
            //save the zone of the station
            String stationZone = stationsString.substring(offsetStation + 6, offsetStation + 9);
            //save the name of the station
            stringTerminatorIndex = stationsString.indexOf(StationUtils.STATIONS_SEPARATOR, offsetString + 10);
            String stationName = stationsString.substring(offsetStation + 10, stringTerminatorIndex);

            int counter = 0;
            int j = 0;
            while (stationsString.substring(stringTerminatorIndex + 1 + j, stringTerminatorIndex + 2 + j).charAt(0) != StationUtils.STRING_SEPARATOR) {
                linesOnStationBlob[counter] = Short.parseShort(stationsString.substring(stringTerminatorIndex + 1 + j, stringTerminatorIndex + 4 + j));
                j += 4;
                counter++;
            }

            linesOnStation = new short[counter];
            System.arraycopy(linesOnStationBlob, 0, linesOnStation, 0, counter); //trim
            stationsBlob[i] = new Station((short)i,Short.parseShort(id), stationName, linesOnStation, Short.parseShort(stationZone));

            i++;
            offsetStation = stationTerminatorIndex;
            offsetString = stringTerminatorIndex + j + 1;
        }

        stations = new Station[i];
        System.arraycopy(stationsBlob, 0, stations, 0, i); //trim

        try {
            reader.close();
        } catch (IOException ex) {
        //TODO
        }
        //memory leaks
        reader = null;
        buffer = null;
        sb = null;
        stationsString = null;
        linesOnStationBlob = null;
        linesOnStation = null;
        stationsBlob = null;

    }

    /**
     * Returns array with ids of lines at this station
     * @param idStation ID of the station
     * @return Array with ids of lines at this station
     */
    public static final short[] getLines(short idStation) {
        return stations[idStation].getLines();
    }

    /**
     * Returns suitable names of stations
     * @param param start of the name of a station
     * @return Suitable names of stations
     */    
    public static final String[] getCompletion(String param) {
        String[] suitableStations = new String[StationUtils.MAX_SUITABLE_STATION_COUNT];
        int index = 0;
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].getName().toLowerCase().startsWith(param.toLowerCase())) {
                suitableStations[index] = stations[i].getName(); //match => add
                index++;
                if (index >= StationUtils.MAX_SUITABLE_STATION_COUNT) {
                    break;
                }
            }
        }

        String[] suitableStationsTrim = new String[index];
        System.arraycopy(suitableStations, 0, suitableStationsTrim, 0, index); //trim
        suitableStations = null;        
        return suitableStationsTrim;
    }

    /**
     * getter for station by name
     * @param param Name of the station
     * @return Station
     */
    public static final Station getStation(String param) {
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].getName().toLowerCase().startsWith(param.toLowerCase())) {
                return stations[i];
            }
        }
        return null;
    }

    /**
     * Returns true if some stations starts with the string
     * 
     * @param station Name of a station
     * @return true if some stations starts with the string
     */
    public static final boolean validateStation(String station) {
        String[] suitableStations = getCompletion(station);
        if (suitableStations.length == 1) { //just 1
            return true;
        } else if (suitableStations.length > 1) {
            for (int i = 0; i < suitableStations.length; i++) {
                if (station.equalsIgnoreCase(suitableStations[i])); {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Getter for station by UID
     * @param uid UID of the station
     * @return Station
     */
    public static final Station getStationByUid(short uid) {
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].getUid() == uid) return stations[i];
        }
        return null;// if not found
    }
    
    /**
     * Getter for station by ID
     * @param id ID of the station
     * @return Station
     */    
    public static final Station getStationById(short id) {
        return stations[id];// if not found
    }
}
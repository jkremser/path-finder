/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author freon
 */
public class ServicesConverter {

    private static final String FILE_NAME = "stations";
    private static final String FILE_ENCODING = "windows-1250";
    private static final int ID_SERVICE_LENGTH = 9;
    private static final int BEG_ID_STATION = 5;
    private static final int END_ID_STATION = 10;
    private static final int BEG_ID_DEPARTURE = 21;
    private static final int END_ID_DEPARTURE = 26;
    private static final int BEG_ID_PUBLIC = 27;
    private static final int PUBLIC_FLAG = 'A';
    public static final int TRAM = 1;
    public static final int TROLEY_BUS = 2;
    public static final int BUS = 3;
    public static final int SHIP = 4;
    public static final int TRAIN = 5;
    private File sourceFile;
    private File targetFile;
    
    private List<String> route;
    private Map<String, Map<String, Map<List<String>, SortedSet<String>>>> services;
    private Map<String, Integer> serviceTypes;
    private static Map<String, Integer> lineTypes = new HashMap<String, Integer>();    
    private Map<String, Map<String,Integer>> linesAtStation;
    private StationsConverter stc;

    public ServicesConverter(String sourceFile, String targerDirectory, StationsConverter sc) {
        this.sourceFile = new File(sourceFile);
        this.targetFile = new File(targerDirectory + File.separator + FILE_NAME);
        this.stc = sc;
        this.linesAtStation = new HashMap<String, Map<String,Integer>>(StationsConverter.STATION_NUMBER);
        this.serviceTypes = new HashMap<String, Integer>();
        this.services = new HashMap<String, Map<String, Map<List<String>, SortedSet<String>>>>(530);
    }

    public void convert() throws IOException {
        int serviceIdEndIndex = ID_SERVICE_LENGTH + 1;
        int serviceTypeBeginIndex = ID_SERVICE_LENGTH + 8;
        try {
            BufferedReader buff = null;
            buff = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), FILE_ENCODING));

            String serviceId = null; //#001010701            
            String lineId = null; //L107
 
            String line = buff.readLine();
            boolean eof = false;
            boolean firstLoop = true;
            while (!eof) {
                if (line != null && line.charAt(0) != '#') {
                    line = buff.readLine();
                }
                if (line == null) {
                    eof = true;
                } else {
                    if (line.charAt(0) == '#') { //new service
                        serviceId = line.substring(1, serviceIdEndIndex);
                        
                        serviceTypes.put(serviceId, Integer.parseInt(line.substring(serviceTypeBeginIndex, serviceTypeBeginIndex + 1)));
                        line = buff.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.charAt(0) == 'L') {
                            lineId = line.substring(1, 4);
                        }
                        boolean departureLoaded = false;
                        String departure = null;
                        int lastMinutes = 0;

                        while (!eof) {
                            if (!firstLoop){
                                line = buff.readLine();
                            }
                            firstLoop = false;
                            if (line == null) {
                                eof = true;
                            } else {
                                if (line.charAt(0) == ' ' && line.charAt(BEG_ID_PUBLIC) != PUBLIC_FLAG) continue; // non public ride
                                if (line.charAt(0) == ' ') { //public ride between 2 stations
                                    if (!departureLoaded) {
                                        departure = line.substring(BEG_ID_DEPARTURE, END_ID_DEPARTURE);
                                        route = new ArrayList<String>(20);
                                        route.add("START");
                                        lastMinutes = Integer.valueOf(line.substring(BEG_ID_DEPARTURE + 3, END_ID_DEPARTURE));
                                    }

                                    int minutes = Integer.valueOf(line.substring(BEG_ID_DEPARTURE + 3, END_ID_DEPARTURE));

                                    if (departureLoaded) {
                                        route.add(String.valueOf((minutes - lastMinutes + 60) % 60));
                                    }

                                    String stationId = line.substring(BEG_ID_STATION, END_ID_STATION);
                                    stationId = stc.getId(stationId);
                                    route.add(stationId);
                                    if (!"600".equals(lineId)) {
                                        if (!linesAtStation.containsKey(stationId)) {
                                            linesAtStation.put(stationId, new HashMap<String,Integer>());
                                        }
                                        if (!linesAtStation.get(stationId).containsKey(lineId)) {
                                            linesAtStation.get(stationId).put(lineId,0);
                                        }
                                        int temp = linesAtStation.get(stationId).get(lineId);
                                        linesAtStation.get(stationId).put(lineId,temp+1);
                                    }
                                    
                                    lastMinutes = minutes;
                                    if (!departureLoaded) {
                                        departureLoaded = true;
                                    }
                                } else {
                                    if (departureLoaded) {
                                        if (!services.containsKey(serviceId)) {
                                            services.put(serviceId, new HashMap<String, Map<List<String>, SortedSet<String>>>(200));
                                        }
                                        if (!services.get(serviceId).containsKey(lineId)) {
                                            services.get(serviceId).put(lineId, new HashMap<List<String>, SortedSet<String>>(15));
                                        }
                                        if (!services.get(serviceId).get(lineId).containsKey(route)) {
                                            services.get(serviceId).get(lineId).put(route, new TreeSet<String>());
                                        }
                                        services.get(serviceId).get(lineId).get(route).add(departure);
                                        lineTypes.put(lineId, serviceTypes.get(serviceId));

                                        if (line.charAt(0) == 'L') {
                                            lineId = line.substring(1, 4);
                                        }
                                    }
                                    departureLoaded = false; // new line
                                }
                                if (line.charAt(0) == '#') {
                                    break; // new service
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServicesConverter.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Soubor " + sourceFile.getAbsolutePath() + " neexistuje.");
        } catch (IOException ex) {
            System.err.println("Soubor " + sourceFile.getAbsolutePath() + " se nepodařilo otevřít.");
            Logger.getLogger(ServicesConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

   }

    public int getServiceType(String id) {
        return serviceTypes.get(id);
    }
    
    public static int getLineType(String id) {
        return lineTypes.get(id);
    }
    
    public Map<String, Map<String, Map<List<String>, SortedSet<String>>>> getServices() {
        return services;
    }
    
    public Map<String,Integer> getLinesAtStation(String station) {
        return linesAtStation.get(station);
    }
}

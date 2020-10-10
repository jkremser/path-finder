/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package conversion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author freon
 */
public class DaysConverter {

    private static final String FILE_ENCODING = "windows-1250";
    private static final byte SEPARATOR1 = (byte) 129;
    private static final byte SEPARATOR2 = (byte) 254;
    private File sourceFile;
    private File targetDir;
    private Map<String, Map<String, Map<List<String>, SortedSet<String>>>> services;
    private Map<String, Map<List<String>, SortedSet<String>>> lines;
    private Map<String, Integer> lineTypes;
    private StationsConverter stc;
    private ServicesConverter sec;
    
    private static Map<String,byte[]> linesData = new HashMap<String,byte[]>();

    public enum Days {

        SU, MO, TU, WE, TH, FR, SA
    }
    private Days day = null;
    private boolean lock = Boolean.TRUE;
    private boolean fileInput;

    public DaysConverter(String sourceFile, String targerDirectory, StationsConverter stc, ServicesConverter sec, Days day, boolean fileInput) {
        this.sourceFile = new File(sourceFile);
        this.targetDir = new File(targerDirectory + File.separator + day.toString());
        this.stc = stc;
        this.sec = sec;
        this.day = day;
        this.services = sec.getServices();
        this.lines = new HashMap<String, Map<List<String>, SortedSet<String>>>(200);
        this.lineTypes = new HashMap<String, Integer>(200);
        this.fileInput = fileInput;
    }

    public void load() {
        try {
            BufferedReader buff = null;
            buff = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), FILE_ENCODING));

            String serviceId = null; //#001010701            
            String line = null;
            while ((line = buff.readLine()) != null) {
                if (line.charAt(0) != ' ') {
                    continue;
                }
                serviceId = line.substring(1, 10);

                Map<String, Map<List<String>, SortedSet<String>>> foo = services.get(serviceId);
                for (String lineName : foo.keySet()) {
                    if (lines.containsKey(lineName)) {
                        for (List<String> route : foo.get(lineName).keySet()) {
                            if (lines.get(lineName).containsKey(route)) {
                                lines.get(lineName).get(route).addAll(foo.get(lineName).get(route));
                            } else {
                                lines.get(lineName).put(route, foo.get(lineName).get(route));
                            }
                        }
                    } else {
                        lines.put(lineName, services.get(serviceId).get(lineName));
                        lineTypes.put(lineName, sec.getServiceType(serviceId)); // 1 line - just 1 service type (L001 == salina) 4ever true
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
        lock = Boolean.FALSE;
    }

    public void filter(int value) {
        if (lock) {
            return;
        }
        Map<String, Map<List<String>, SortedSet<String>>> tempLines = new HashMap<String, Map<List<String>, SortedSet<String>>>(200);
        tempLines.putAll(lines);
        lines = new HashMap<String, Map<List<String>, SortedSet<String>>>(200);
        for (String lineName : tempLines.keySet()) {
            for (List<String> route : tempLines.get(lineName).keySet()) {
                if ((tempLines.get(lineName).get(route).size() > value) //filter application
                        || ((lineName.charAt(1) == '9') && (lineName.charAt(0) == '0')) //rozjezd 09X
                        || (lineTypes.get(lineName) == ServicesConverter.TRAIN)) {
                    //add filtered record (deep copy)
                    if (!lines.containsKey(lineName)) {
                        lines.put(lineName, new HashMap<List<String>, SortedSet<String>>(10));
                    }
                    if (!lines.get(lineName).containsKey(route)) {
                        lines.get(lineName).put(route, tempLines.get(lineName).get(route));
                    }
                }
            }
        }
    }

    public void recognizeDirections() {
        if (lock) {
            return;
        }
        Map<String, Map<List<String>, SortedSet<String>>> tempLines = new HashMap<String, Map<List<String>, SortedSet<String>>>(200);
        tempLines.putAll(lines);
        lines = new HashMap<String, Map<List<String>, SortedSet<String>>>(200);
        for (String lineName : tempLines.keySet()) {
            Set<List<String>> routesKeys = tempLines.get(lineName).keySet();
            final Map<List<String>, SortedSet<String>> line = tempLines.get(lineName);

            List<String> directionList = null; // route with max number of departures            
            directionList = removeDelays(Collections.max(routesKeys, new Comparator<List<String>>() {

                public int compare(List<String> o1, List<String> o2) {
                    int ret = line.get(o1).size() - line.get(o2).size();
                    if (ret == 0) {
                        ret = o1.size() - o2.size();
                        if (ret == 0) {
                            ret = o1.hashCode() - o2.hashCode();
                        }
                    }
                    return -ret;
                }
            }));

            for (List<String> route : tempLines.get(lineName).keySet()) {
                boolean even = true;
                boolean start = true;
                char directionMark = 'A';
                int ind = -1;
                for (String item : route) {
                    if (start) {
                        start = false;
                        continue;
                    }
                    if (even) {
                        if (ind != -1) {
                            int ind2 = directionList.indexOf(item);
                            if (ind < ind2) { //1st direction
                                directionMark = 'A';
                                break;
                            } else if (ind2 != -1) { //2nd direction
                                directionMark = 'B';
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            ind = directionList.indexOf(item);
                        }
                    }
                    even = !even;
                }
                if (!lines.containsKey(lineName + directionMark)) {
                    lines.put(lineName + directionMark, new HashMap<List<String>, SortedSet<String>>(10));
                }
                if (!lines.get(lineName + directionMark).containsKey(route)) {
                    lines.get(lineName + directionMark).put(route, tempLines.get(lineName).get(route));
                }
            }
        }
    }

    private static List<String> removeDelays(List<String> route) {
        List<String> tempRoute = new ArrayList<String>(route.size() / 2);
        boolean start = true;
        boolean even = true;
        for (String item : route) {
            if (start) {
                start = false;
                continue; //skip 1st member (string "START" terminator)
            }
            if (even) {
                tempRoute.add(item);
            }
            even = !even;
        }
        return tempRoute;
    }

    public void generate() {
        if (lock) {
            return;
        }
        BufferedOutputStream out1 = null;
        ByteArrayOutputStream out2 = null;
        
        if (fileInput) {
            if (!targetDir.exists()) {
                targetDir.mkdir();
            }
        }


        for (String line : lines.keySet()) {
            try {
                if (fileInput) {File targetFile = new File(targetDir.getAbsolutePath() + File.separator + line);
                    if (!targetFile.exists()) {
                        targetFile.createNewFile();
                    }
                    out1 = new BufferedOutputStream(new FileOutputStream(targetFile.getAbsolutePath()), 256);
                }
                
                out2 = new ByteArrayOutputStream(1024*2);

                if (fileInput) out1.write(lineTypes.get(line.substring(0, line.length() - 1)).byteValue()); // remove direction mark
                out2.write(lineTypes.get(line.substring(0, line.length() - 1)).byteValue()); // remove direction mark

//                //move more frequent routes to top (performance)
                final Map<List<String>, SortedSet<String>> routes = lines.get(line);
                SortedSet<List<String>> keySet = new TreeSet<List<String>>(new Comparator<List<String>>() {

                    public int compare(List<String> o1, List<String> o2) {
                        int ret = routes.get(o1).size() - routes.get(o2).size();
                        if (ret == 0) {
                            ret = o1.size() - o2.size();
                            if (ret == 0) {
                                ret = o1.hashCode() - o2.hashCode();
                            }
                        }
                        return -ret;
                    }
                });
                keySet.addAll(routes.keySet());
              


                for (List<String> key : keySet) {
                    boolean start = true;
                    boolean even = true;
                 
                    for (String item : key) { //station or delay between 2 neighbor-stations
                        if (start) {
                            start = false;
                            continue; //skip 1st member (string "START" terminator)
                        }
                        if (even) {
                            if (fileInput) out1.write(encodeStation(item));
                            out2.write(encodeStation(item));
                            even = !even;
                        } else {
                            if (fileInput) out1.write((byte) Integer.parseInt(item));
                            out2.write((byte) Integer.parseInt(item));
                            even = !even;
                        }
                    }
                    if (fileInput) out1.write(DaysConverter.SEPARATOR1);
                    out2.write(DaysConverter.SEPARATOR1);
                    
                    Set<String> departures = routes.get(key);
                    for (String departure : departures) {
                        if (fileInput) out1.write(encodeDeparture(departure));
                        out2.write(encodeDeparture(departure));
                    }
                    if (fileInput) out1.write(DaysConverter.SEPARATOR2);
                    out2.write(DaysConverter.SEPARATOR2);
                }
                if (fileInput) out1.write((byte) -1); //myEOF
                out2.write((byte) -1); //myEOF
                linesData.put(Packager.PATH_TO_DATA+day.toString()+File.separator+line, out2.toByteArray());

            } catch (FileNotFoundException ex) {
                System.err.println("Adresář " + targetDir.getAbsolutePath() + " neexistuje.");
                Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                System.err.println("Adresář " + targetDir.getAbsolutePath() + " se nepodařilo otevřít.");
                Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (fileInput) out1.flush();
                    if (fileInput) out1.close();
                    out2.flush();
                    out2.close();
                } catch (IOException ex) {
                    Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static Map<String, byte[]> getLinesData() {
        return linesData;
    }        

    private byte[] encodeStation(String code) {
        int intCode = Integer.parseInt(code);
        return new byte[]{(byte) (intCode / 128), (byte) (intCode - (intCode / 128) * 128)};
    }

    private byte[] encodeDeparture(String code) {
        int hours = Integer.parseInt(code.substring(0, 2));
        int min = Integer.parseInt(code.substring(3, 5));
        int intCode = hours * 60 + min;
        return new byte[]{(byte) (intCode / 128), (byte) (intCode - (intCode / 128) * 128)};
    }

    public void setServices(Map<String, Map<String, Map<List<String>, SortedSet<String>>>> services) {
        this.services = services;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author freon
 */
public class StationsConverter {

    private static final String FILE_NAME = "stations";
    private static final String FILE_ENCODING = "windows-1250";
    private static final int ID_STATION_LENGTH = 5;
    private static final int ID_ZONE_LENGTH = 3;
    public static final int STATION_NUMBER = 2430;
    private static final char SEPARATOR = '@';
    private File sourceFile;
    private File targetFile;
    private List<List<String>> stations;
    private Map<String, String> keys;
    private boolean locked = Boolean.TRUE;
    private ServicesConverter sec;
    private int filter;
    private static byte[] stationsData;
    private static boolean fileInput;

    public StationsConverter(String sourceFile, String targerDirectory, boolean fileInput) {
        this.sourceFile = new File(sourceFile);
        this.targetFile = new File(targerDirectory + File.separator + FILE_NAME);
        stations = new LinkedList<List<String>>();
        keys = new HashMap<String, String>(STATION_NUMBER);
        filter = 25; //minimalni pocet prejezdu saliny, aby bylo k zastavce poznamenano, ze u ni dana linka zastavuje (vozovny)
        this.fileInput = fileInput;
    }

    public void load() {
        int zoneBeginIndex = ID_STATION_LENGTH + 1;
        int zoneEndIndex = ID_STATION_LENGTH + 1 + ID_ZONE_LENGTH;
        int nameBeginIndex = ID_STATION_LENGTH + 1 + ID_ZONE_LENGTH + 2;
        int index = 0;

        try {
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), FILE_ENCODING));
            boolean eof = false;
            while (!eof) {
                String line = in.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    if (line.charAt(0) == ' ') { // line containing info abou sub-station (sloupek)
                        continue;
                    }
                    List<String> station = new ArrayList<String>(3);
                    String id = line.substring(0, ID_STATION_LENGTH);
                    station.add(id); // id
                    station.add(line.substring(zoneBeginIndex, zoneEndIndex)); // zone
                    String name = line.substring(nameBeginIndex, line.indexOf('\'', nameBeginIndex)); // name     
                    String ret = Normalizer.normalize(name, Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    station.add(ret);
                    stations.add(station);
                    keys.put(id, String.valueOf(index++));
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Soubor " + sourceFile.getAbsolutePath() + " se nepodařilo otevřít.");
            Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("Soubor " + sourceFile.getAbsolutePath() + " se nepodařilo otevřít.");
            Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        locked = Boolean.FALSE;
    }

    public void generate() {
        if (locked || sec == null) {
            return;
        }
        BufferedWriter out1 = null;
        ByteArrayOutputStream out2 = null;
        try {
            if (fileInput) {
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                out1 = new BufferedWriter(new FileWriter(targetFile.getAbsolutePath()), 1024 * 32);
            }
            out2 = new ByteArrayOutputStream(1024 * 512);
            for (List<String> stationInfo : stations) {
                String id = getId(stationInfo.get(0));
                if (sec.getLinesAtStation(id) == null || sec.getLinesAtStation(id).size() == 0) {
                    continue;
                }
                if (fileInput) {
                    out1.write(id.length() == 4 ? id : id.length() == 3 ? "0" + id : id.length() == 2 ? "00" + id : "000" + id);
                }
                out2.write((id.length() == 4 ? id : id.length() == 3 ? "0" + id : id.length() == 2 ? "00" + id : "000" + id).getBytes());
                if (fileInput) {
                    out1.write(SEPARATOR);
                }
                out2.write(SEPARATOR);
                if (fileInput) {
                    out1.write(stationInfo.get(1));
                } //zone
                out2.write(stationInfo.get(1).getBytes()); //zone
                if (fileInput) {
                    out1.write(SEPARATOR);
                }
                out2.write(SEPARATOR);
                if (fileInput) {
                    out1.write(stationInfo.get(2));
                } //name
                out2.write(stationInfo.get(2).getBytes()); //name
                if (sec.getLinesAtStation(id) != null) {
                    if (fileInput) {
                        out1.write(SEPARATOR);
                    }
                    out2.write(SEPARATOR);
                    for (String line : sec.getLinesAtStation(id).keySet()) {

                        if (1 == ServicesConverter.getLineType(line) &&
                                sec.getLinesAtStation(id).get(line) < filter &&
                                sec.getLinesAtStation(id).keySet().size() > 1) {

                        } else {
                            if (fileInput) {
                                out1.write(line);
                            }
                            out2.write(line.getBytes());
                            if (fileInput) {
                                out1.write(SEPARATOR);
                            }
                            out2.write(SEPARATOR);
                        }
                    }
                }
                if (fileInput) {
                    out1.write('\n');
                }
                out2.write('\n');
            }
            stationsData = out2.toByteArray();

        } catch (FileNotFoundException ex) {
            System.err.println("Soubor " + targetFile.getAbsolutePath() + " se nepodařilo otevřít.");
            Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("Soubor " + targetFile.getAbsolutePath() + " se nepodařilo otevřít.");
            Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fileInput) {
                    out1.flush();
                }
                out2.close();
                if (fileInput) {
                    out1.flush();
                }
                out2.close();
            } catch (IOException ex) {
                Logger.getLogger(StationsConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static byte[] getStationsData() {
        return stationsData;
    }

    public void setSec(ServicesConverter sec) {
        this.sec = sec;
    }

    public String getId(String id) {
        return keys.get(id);
    }
}

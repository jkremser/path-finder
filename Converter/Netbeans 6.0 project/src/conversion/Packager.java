/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package conversion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author freon
 */
public class Packager {

    public static final String PATH_TO_PROPERTIES = "src/converter/resources/Midlet.properties";
    public static final String PROPERTY_VERSION = "Midlet.vetsion";
    private Map<String, Integer> sizes = new HashMap();
    private Map<String, byte[]> jarContents = new HashMap();
    private String jarFileName;
    private String target;
    public static final String PATH_TO_DATA = "cz/muni/fi/mobileIDS/_data/";
    public static final String ARCHIVE_NAME = "MobileIDS.jar";
    public static final String DESCRIPTOR_NAME = "MobileIDS.jad";
    private static final String DESCRIPTOR_SIZE = "MIDlet-Jar-Size: ";
    private static final String DESCRIPTOR_VERSION = "MIDlet-Version: ";
    private static final String DESCRIPTOR =
            "MIDlet-1: Vyhledavac,/cz/muni/fi/mobileIDS/_images/logo.png,cz.muni.fi.mobileIDS.src.SearchMidlet\n" +
            "MIDlet-2: Jizdni rady,/cz/muni/fi/mobileIDS/_images/ttables.png,cz.muni.fi.mobileIDS.src.TimeTablesMidlet\n" +
            "MIDlet-Jar-URL: http://www.fi.muni.cz/~xkremser/MobileIDS.jar\n" +
            "MIDlet-Name: MobileIDS\n" +
            "MIDlet-Vendor: Jiri Kremser\n" +
            "MIDlet-Description: Aplikace slouzici k vyhledavani dopravnich spoju v Jihomoravskem kraji\n" +
            "MicroEdition-Configuration: CLDC-1.1\n" +
            "MicroEdition-Profile: MIDP-2.0\n";

    public Packager(String jarFileName, String target) {
        this.jarFileName = jarFileName;
        this.target = target;
        init();
    }
    
    public static boolean exist(String fileName) {
        return (new File(fileName).exists());
    }

    private void init() {
        try {
            ZipFile zf = new ZipFile(jarFileName);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.getName().startsWith(PATH_TO_DATA)) {
                    continue;
                }

                sizes.put(ze.getName(), new Integer((int) ze.getSize()));
            }
            zf.close();


            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().startsWith(PATH_TO_DATA)) {
                    continue;
                }
                int size = (int) ze.getSize();
                // -1 means unknown size. 
                if (size == -1) {
                    size = ((Integer) sizes.get(ze.getName())).intValue();
                }
                byte[] b = new byte[(int) size];
                int rb = 0;
                int chunk = 0;
                while (((int) size - rb) > 0) {
                    chunk = zis.read(b, rb, (int) size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                // add to internal resource hashtable
                jarContents.put(ze.getName(), b);
            }
            jarContents.putAll(DaysConverter.getLinesData());
            jarContents.put(PATH_TO_DATA + "stations", StationsConverter.getStationsData());
        } catch (NullPointerException e) {            
        } catch (FileNotFoundException e) {
            System.err.println("Soubor " + jarFileName + " neexistuje.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Soubor " + jarFileName + " neexistuje.");
            e.printStackTrace();
        }
    }

    public void zip() {
        try {
            // Create the ZIP file
            String outFilename = target + File.separator + ARCHIVE_NAME;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

            // Compress the files
            for (String name : jarContents.keySet()) {
                if (jarContents.get(name) != null) {
                    // Add ZIP entry to output stream.
                    out.putNextEntry(new ZipEntry(name));
                    // Transfer bytes from the file to the ZIP file
                    out.write(jarContents.get(name));
                }
                // Complete the entry
                out.closeEntry();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
        }
        short[] a = new short[1];
        Arrays.sort(a);
    }

    public void createJadFile() {
        long size = new File(target + File.separator + ARCHIVE_NAME).length();
        String version = getIncrementedVersion();
        File descriptor = new File(target + File.separator + DESCRIPTOR_NAME);
        BufferedOutputStream out = null;
        try {
            descriptor.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(descriptor), 256);
            out.write(DESCRIPTOR.getBytes());
            out.write((DESCRIPTOR_VERSION + version + "\n").getBytes());
            out.write((DESCRIPTOR_SIZE + size + "\n").getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                System.err.println("Adresář " + target + " se nepodařilo otevřít.");
                Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getIncrementedVersion() {
        Properties properties = new Properties();
        String incrementedVersion = "";
        try {
            properties.load(new FileInputStream(PATH_TO_PROPERTIES));

            String version = properties.getProperty(PROPERTY_VERSION);

            int intVersion = 0;
            try {
                intVersion = Integer.parseInt(version.substring(version.indexOf('.') + 1, version.length()));
            } catch (NumberFormatException e) {

            }
            intVersion++;
            incrementedVersion = version.substring(0, version.indexOf('.') + 1) + (intVersion<10?"0"+intVersion:intVersion);
            properties.put(PROPERTY_VERSION, incrementedVersion);
            properties.store(new FileOutputStream(PATH_TO_PROPERTIES), "incremented version");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Packager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return incrementedVersion;
    }
}

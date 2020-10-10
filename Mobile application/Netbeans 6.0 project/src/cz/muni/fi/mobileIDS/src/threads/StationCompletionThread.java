package cz.muni.fi.mobileIDS.src.threads;

import cz.muni.fi.mobileIDS.src.*;
import javax.microedition.lcdui.ChoiceGroup;

/**
 *
 * @author Jiri Kremser
 * 
 * Thread which complete the name of the station
 */
public final class StationCompletionThread extends Thread {

    private String param;
    private ChoiceGroup where;
    private static volatile int count = 0;
    private int id;

    /**
     * Constructor
     * 
     * @param param
     * @param where
     */
    public StationCompletionThread(String param, ChoiceGroup where) {
        this.id = ++count;
        this.param = param;
        this.where = where;
    }

    /**
     * Completion inside
     */
    public void run() {
        try {    //only last completion will be performed (fast writing x many threads => memory)
            sleep(350);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if (count != id) {
            return;
        }
        where.deleteAll();
        String[] stations = StationUtils.getCompletion(param);
        for (int i = 0; i < stations.length; i++) {
            // fill choice group with proper stations
            where.append(stations[i], null);
        }
        stations = null;
    }
    
    
    /**
     * Completion inside (without locking)
     */    
    public void complete() {
        where.deleteAll();
        String[] stations = StationUtils.getCompletion(param);
        for (int i = 0; i < stations.length; i++) {
            // fill choice group with proper stations
            where.append(stations[i], null);
        }
    }
}

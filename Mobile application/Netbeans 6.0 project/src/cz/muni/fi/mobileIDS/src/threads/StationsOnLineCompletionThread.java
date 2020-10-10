package cz.muni.fi.mobileIDS.src.threads;

import cz.muni.fi.mobileIDS.src.*;
import cz.muni.fi.mobileIDS.src.forms.ChooseTimeTablesForm;

/**
 *
 * @author Jiri Kremser
 * 
 * Thread which complete proper stations of the line
 */
public final class StationsOnLineCompletionThread extends Thread {

    private int param;
    private ChooseTimeTablesForm source;
    static int count = 0;
    private int id;
    private static final LineUtils LU = LineUtils.getInstance();

    /**
     * Constructor
     * 
     * @param uidStation
     * @param source
     */
    public StationsOnLineCompletionThread(int uidStation, ChooseTimeTablesForm source) {
        this.id = ++count;
        this.param = uidStation;
        this.source = source;
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
        if (count != id) return;
        source.getStationChoiceGroup().deleteAll();
        source.getDirectionChoiceGroup().deleteAll();
        String day = CommonUtils.getDay(source.getDayDateField().getDate());
        Station[] stations = LU.getStations((short)param,day);
        if (stations == null) return;
        for (int i = 0; i < stations.length; i++) {
            // fill choice group with proper stations
            source.getStationChoiceGroup().append(stations[i].getName(), null);
        }
        source.getDirectionChoiceGroup().append(stations[0].getName() + " -> " + stations[stations.length-1].getName(), null);
        source.getDirectionChoiceGroup().append(stations[stations.length-1].getName() + " -> " + stations[0].getName(), null);
        source.setStations(stations);
    }
}

package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.*;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Jiri Kremser
 * 
 * Form used for showing time progress when searching
 */
public class WaitForm extends Form {

    private Gauge waitGauge;
    private StringItem infoStringItem;
    private int gaugeWidth;
    private SearchMidlet sourceMidlet;

    /**
     * Constructor
     * 
     * @param sourceMidlet
     * @param gaugeWidth
     */
    public WaitForm(SearchMidlet sourceMidlet, int gaugeWidth) {
        super("Probiha vyhledavani");
        super.append(new Spacer(30, 60));
        super.append(getWaitGauge());
        super.append(getInfoStringItem());
        this.gaugeWidth = gaugeWidth;
        this.sourceMidlet = sourceMidlet;
    }

    /**
     * getter for waitGauge
     * 
     * @return Gauge
     */
    public Gauge getWaitGauge() {
        if (waitGauge == null) {
            waitGauge = new Gauge("", false, 20, 0);
            waitGauge.setPreferredSize(gaugeWidth, 15);
            waitGauge.setLayout(Item.LAYOUT_2|Item.LAYOUT_VCENTER|Item.LAYOUT_CENTER);
        }
        return waitGauge;
    }

    /**
     * getter for infoStringItem
     * 
     * @return StringItem
     */    
    public StringItem getInfoStringItem() {
        if (infoStringItem == null) {
            infoStringItem = new StringItem("", "");
            infoStringItem.setLayout(Item.LAYOUT_2|Item.LAYOUT_BOTTOM);
        }
        return infoStringItem;
    }    

    /**
     * setter for value of the gauge
     * 
     * @param value
     */    
    public void setWaitGauge(int value) {
        getWaitGauge().setValue(value);
    }

    /**
     * execution
     */    
    public void start() {
        sourceMidlet.switchDisplayable(null, this);
        //progressBar
        new Thread() {
                    public void run() {
                        int i = 0;
                        while (!sourceMidlet.isSearchDone()) {
                            try {
                                setWaitGauge(i++ % 20);
//                                getInfoStringItem().setText("Volná paměť: " + Runtime.getRuntime().freeMemory());
                                Thread.currentThread().sleep(50);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }.start();
    }
}

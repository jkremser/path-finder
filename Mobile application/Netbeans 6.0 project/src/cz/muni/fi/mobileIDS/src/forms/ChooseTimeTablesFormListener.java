package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.threads.StationsOnLineCompletionThread;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Jiri Kremser
 * 
 * ItemStateListener for ChooseTimeTablesForm
 */
class ChooseTimeTablesFormListener implements ItemStateListener {

    private ChooseTimeTablesForm source;

    /**
     * Constructor
     * 
     * @param ChooseTimeTablesForm source
     */
    public ChooseTimeTablesFormListener(ChooseTimeTablesForm source) { //there is no other option than this tight couple
        this.source = source;
    }

    /**
     * Invoked, when ChooseTimeTablesForm's items get changed
     * 
     * @param item
     */
    public void itemStateChanged(Item item) {
        if ((item instanceof DateField) || (item instanceof TextField)) {
            int id = 0;
            try {
                id = Integer.parseInt(source.getLineTextField().getString());
            } catch (NumberFormatException nfe) {
                id = 0;
            }
            StationsOnLineCompletionThread st = new StationsOnLineCompletionThread(id, source);
            st.start();
            st = null;
        }
    }
}

package cz.muni.fi.mobileIDS.src.forms;

import cz.muni.fi.mobileIDS.src.threads.StationCompletionThread;
import cz.muni.fi.mobileIDS.src.*;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Jiri Kremser
 * 
 * ItemStateListener for SearchForm
 */
public class SearchFormListener implements ItemStateListener {

    private SearchForm source;
    
    /**
     * Constructor
     * 
     * @param source
     */
    public SearchFormListener(SearchForm source) { //there is no other option than this tight couple
        this.source = source;
    }

    /**
     * Invoked, when SearchForm's items get changed
     * 
     * @param item
     */    
    public void itemStateChanged(Item item) {
        if (item instanceof TextField) { // user is typing name of a station
            TextField textItem = (TextField) item;
            if (textItem.getLabel().equals(source.getFromTextField().getLabel())) {// FROM
                new StationCompletionThread(source.getFromTextField().getString(), source.getFromChoiceGroup()).start();
            } else if (textItem.getLabel().equals(source.getToTextField().getLabel())) {// TO
                new StationCompletionThread(source.getToTextField().getString(), source.getToChoiceGroup()).start();
            }
        } else if (item instanceof ChoiceGroup) {
            ChoiceGroup choiceGroup = (ChoiceGroup) item;
            if (choiceGroup.hashCode() == source.getFromChoiceGroup().hashCode()) { //enought strong for this app
                choiceGroup = source.getFromChoiceGroup();
                source.getFromTextField().setString(choiceGroup.getString(choiceGroup.getSelectedIndex())); // fill in the field FROM
                source.setFromOk(true);
            } else if (choiceGroup.hashCode() == source.getToChoiceGroup().hashCode()) {
                choiceGroup = source.getToChoiceGroup();
                source.getToTextField().setString(choiceGroup.getString(choiceGroup.getSelectedIndex()));
                source.setToOk(true);
            }
        }
    }
}

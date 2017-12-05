/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.metadaten;

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.DisplayCase;
import org.goobi.api.display.Item;
import org.goobi.api.display.Modes;
import org.goobi.api.display.enums.BindState;
import org.kitodo.data.database.beans.Process;

import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */

public class MetadatumImpl implements Metadatum {
    private Metadata md;
    private int identifier;
    private Prefs myPrefs;
    private Process myProcess;
    private HashMap<String, DisplayCase> myValues = new HashMap<>();
    private List<SelectItem> items;
    private List<String> selectedItems;

    /**
     * Allgemeiner Konstruktor().
     */
    public MetadatumImpl(Metadata m, int inID, Prefs inPrefs, Process inProcess) {
        this.md = m;
        this.identifier = inID;
        this.myPrefs = inPrefs;
        this.myProcess = inProcess;
        for (BindState state : BindState.values()) {
            this.myValues.put(state.getTitle(),
                    new DisplayCase(this.myProcess, state.getTitle(), this.md.getType().getName()));
        }
    }

    @Override
    public ArrayList<Item> getWert() {
        String value = this.md.getValue();
        if (value != null) {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getValue().equals(value)) {
                    i.setIsSelected(true);
                } else {
                    i.setIsSelected(false);
                }
            }
        }
        return this.myValues.get(Modes.getBindState().getTitle()).getItemList();
    }

    @Override
    public void setWert(String inWert) {
        this.md.setValue(inWert.trim());
    }

    @Override
    public String getTyp() {
        String label = this.md.getType()
                .getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        if (label == null) {
            label = this.md.getType().getName();
        }
        return label;
    }

    @Override
    public void setTyp(String inTyp) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(inTyp);
        this.md.setType(mdt);
    }

    /*
     * Getter und Setter
     */

    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public Metadata getMd() {
        return this.md;
    }

    @Override
    public void setMd(Metadata md) {
        this.md = md;
    }

    /**
     * new functions for use of display configuration whithin xml files.
     */

    @Override
    public String getOutputType() {
        return this.myValues.get(Modes.getBindState().getTitle()).getDisplayType().getTitle();
    }

    @Override
    public List<SelectItem> getItems() {
        this.items = new ArrayList<>();
        this.selectedItems = new ArrayList<>();
        for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
            this.items.add(new SelectItem(i.getLabel()));
            if (i.getIsSelected()) {
                this.selectedItems.add(i.getLabel());
            }
        }
        return this.items;
    }

    @Override
    public void setItems(List<SelectItem> items) {
        for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
            i.setIsSelected(false);
        }
        StringBuilder val = new StringBuilder();
        for (SelectItem sel : items) {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getLabel().equals(sel.getValue())) {
                    i.setIsSelected(true);
                    val.append(i.getValue());
                }
            }
        }
        setWert(val.toString());
    }

    @Override
    public List<String> getSelectedItems() {
        this.selectedItems = new ArrayList<>();
        String values = this.md.getValue();
        if (values != null && values.length() != 0) {
            while (!values.isEmpty()) {
                int semicolon = values.indexOf(";");
                if (semicolon != -1) {
                    String value = values.substring(0, semicolon);
                    for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                        if (i.getValue().equals(value)) {
                            this.selectedItems.add(i.getLabel());
                        }
                    }
                    int length = values.length();
                    values = values.substring(semicolon + 1, length);
                } else {
                    for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                        if (i.getValue().equals(values)) {
                            this.selectedItems.add(i.getLabel());
                        }
                    }
                    values = "";
                }
            }
        } else {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getIsSelected()) {
                    values = values + ";" + i.getValue();
                    this.selectedItems.add(i.getLabel());
                }
            }
            if (values != null) {
                setWert(values);
            }
        }
        return this.selectedItems;
    }

    @Override
    public void setSelectedItems(List<String> selectedItems) {

        StringBuilder val = new StringBuilder();
        for (String sel : selectedItems) {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getLabel().equals(sel)) {
                    val.append(i.getValue());
                    val.append(";");
                }
            }
        }

        setWert(val.toString());
    }

    @Override
    public String getSelectedItem() {
        String value = this.md.getValue();
        if (value != null && value.length() != 0) {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getValue().equals(value)) {
                    return i.getLabel();
                }
            }
        } else {
            for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
                if (i.getIsSelected()) {
                    value = i.getValue();
                    setWert(value);
                    return i.getLabel();
                }
            }
        }
        return "";
    }

    @Override
    public void setSelectedItem(String selectedItem) {

        for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
            if (i.getLabel().equals(selectedItem)) {
                setWert(i.getValue());
            }
        }
    }

    @Override
    public void setValue(String value) {
        setWert(value);
    }

    @Override
    public String getValue() {
        return this.md.getValue();
    }

}

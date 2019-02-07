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

package org.kitodo.production.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.metadata.display.DisplayCase;
import org.kitodo.production.metadata.display.Item;
import org.kitodo.production.metadata.display.Modes;
import org.kitodo.production.metadata.display.enums.BindState;
import org.kitodo.production.services.ServiceManager;

/**
 * Metadata implementation.
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */

public class MetadataImpl implements Metadata {

    private LegacyMetadataHelper md;
    private int identifier;
    private LegacyPrefsHelper myPrefs;
    private HashMap<String, DisplayCase> myValues = new HashMap<>();
    private List<SelectItem> items;
    private List<String> selectedItems;

    /**
     * Allgemeiner Konstruktor().
     */
    public MetadataImpl(LegacyMetadataHelper m, int inID, LegacyPrefsHelper inPrefs, Process inProcess) {
        this.md = m;
        this.identifier = inID;
        this.myPrefs = inPrefs;
        for (BindState state : BindState.values()) {
            this.myValues.put(state.getTitle(),
                    new DisplayCase(inProcess, state.getTitle(), this.md.getMetadataType().getName()));
        }
    }

    @Override
    public String getTyp() {
        String label = this.md.getMetadataType().getLanguage(ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage());
        if (label == null) {
            label = this.md.getMetadataType().getName();
        }
        return label;
    }

    @Override
    public void setTyp(String inTyp) {
        LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName(inTyp);
        this.md.setType(mdt);
    }

    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public LegacyMetadataHelper getMd() {
        return this.md;
    }

    @Override
    public void setMd(LegacyMetadataHelper md) {
        this.md = md;
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
        setValue(val.toString());
    }

    @Override
    public List<String> getSelectedItems() {
        this.selectedItems = new ArrayList<>();
        String values = this.md.getValue();
        if (values != null && values.length() != 0) {
            while (!values.isEmpty()) {
                int semicolon = values.indexOf(';');
                if (semicolon != -1) {
                    String value = values.substring(0, semicolon);
                    addItemsToSelectedItems(value);
                    int length = values.length();
                    values = values.substring(semicolon + 1, length);
                } else {
                    addItemsToSelectedItems(values);
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
                setValue(values);
            }
        }
        return this.selectedItems;
    }

    private void addItemsToSelectedItems(String value) {
        for (Item i : this.myValues.get(Modes.getBindState().getTitle()).getItemList()) {
            if (i.getValue().equals(value)) {
                this.selectedItems.add(i.getLabel());
            }
        }
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

        setValue(val.toString());
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
                    setValue(i.getValue());
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
                setValue(i.getValue());
            }
        }
    }

    @Override
    public void setValue(String value) {
        this.md.setStringValue(value.trim());
    }

    @Override
    public String getValue() {
        return this.md.getValue();
    }

}

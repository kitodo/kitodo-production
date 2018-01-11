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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;
import org.kitodo.api.ugh.Metadata;

class MockMetadatum implements Metadatum {

    private String value;

    public MockMetadatum() {
    }

    public MockMetadatum(String value) {
        this.value = value;
    }

    public int getIdentifier() {
        return 0;
    }

    public List<SelectItem> getItems() {
        return null;
    }

    public Metadata getMd() {
        return null;
    }

    public String getOutputType() {
        return null;
    }

    public String getSelectedItem() {
        return null;
    }

    public List<String> getSelectedItems() {
        return null;
    }

    public String getTyp() {
        return null;
    }

    public String getValue() {
        return value;
    }

    public ArrayList<Item> getWert() {
        return null;
    }

    public void setIdentifier(int identifier) {
    }

    public void setItems(List<SelectItem> items) {
    }

    public void setMd(Metadata md) {
    }

    public void setSelectedItem(String selectedItem) {
    }

    public void setSelectedItems(List<String> selectedItems) {
    }

    public void setTyp(String inTyp) {
    }

    public void setValue(String value) {
    }

    public void setWert(String inWert) {
        value = inWert;
    }

}

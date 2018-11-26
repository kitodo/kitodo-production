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

package org.kitodo.metadata.pagination;

import de.sub.goobi.metadaten.Metadata;

import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.api.ugh.MetadataInterface;

class MockMetadata implements Metadata {

    private String value;

    public MockMetadata() {
    }

    public MockMetadata(String value) {
        this.value = value;
    }

    public int getIdentifier() {
        return 0;
    }

    public List<SelectItem> getItems() {
        return null;
    }

    public MetadataInterface getMd() {
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

    public void setIdentifier(int identifier) {
    }

    public void setItems(List<SelectItem> items) {
    }

    public void setMd(MetadataInterface md) {
    }

    public void setSelectedItem(String selectedItem) {
    }

    public void setSelectedItems(List<String> selectedItems) {
    }

    public void setTyp(String inTyp) {
    }

    public void setValue(String value) {
        this.value = value;
    }
}

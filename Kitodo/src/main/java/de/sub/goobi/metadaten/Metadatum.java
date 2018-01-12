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
import org.kitodo.api.ugh.MetadataInterface;

public interface Metadatum {

    ArrayList<Item> getWert();

    void setWert(String inWert);

    String getTyp();

    void setTyp(String inTyp);

    int getIdentifier();

    void setIdentifier(int identifier);

    MetadataInterface getMd();

    void setMd(MetadataInterface md);

    /**
     * new functions for use of display configuration whithin xml files.
     *
     */
    String getOutputType();

    List<SelectItem> getItems();

    void setItems(List<SelectItem> items);

    List<String> getSelectedItems();

    void setSelectedItems(List<String> selectedItems);

    String getSelectedItem();

    void setSelectedItem(String selectedItem);

    void setValue(String value);

    String getValue();
}

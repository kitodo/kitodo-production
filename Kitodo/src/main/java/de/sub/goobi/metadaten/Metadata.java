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

import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.api.ugh.MetadataInterface;

public interface Metadata {

    /**
     * Get type.
     * 
     * @return type as String
     */
    String getTyp();

    /**
     * Set type.
     * 
     * @param inTyp
     *            as String
     */
    void setTyp(String inTyp);

    /**
     * Get identifier.
     * 
     * @return identifier
     */
    int getIdentifier();

    /**
     * Set identifier.
     * 
     * @param identifier
     *            as int
     */
    void setIdentifier(int identifier);

    /**
     * Get metadata.
     * 
     * @return metadata
     */
    MetadataInterface getMd();

    /**
     * Set metadata.
     * 
     * @param md
     *            as MetadataInterface object
     */
    void setMd(MetadataInterface md);

    /**
     * New function for use of display configuration within xml files. This one gets
     * output type.
     * 
     * @return output type as string
     */
    String getOutputType();

    /**
     * New function for use of display configuration within xml files. This one gets
     * items.
     *
     * @return items as List of SelectItem objects
     */
    List<SelectItem> getItems();

    /**
     * New function for use of display configuration within xml files. This one sets
     * list of items.
     *
     * @param items as List of SelectItem objects
     */
    void setItems(List<SelectItem> items);

    /**
     * New function for use of display configuration within xml files. This one gets
     * list of selected items.
     *
     * @return selected item as List of SelectItem objects
     */
    List<String> getSelectedItems();

    /**
     * New function for use of display configuration within xml files. This one sets
     * list of selected items.
     *
     * @param selectedItems as List of String objects
     */
    void setSelectedItems(List<String> selectedItems);

    /**
     * New function for use of display configuration within xml files. This one gets
     * selected item.
     *
     * @return selected item as String
     */
    String getSelectedItem();

    /**
     * New function for use of display configuration within xml files. This one sets
     * selected item.
     *
     * @param selectedItem as String
     */
    void setSelectedItem(String selectedItem);

    /**
     * New function for use of display configuration within xml files. This one sets
     * value.
     *
     * @param value as String
     */
    void setValue(String value);

    /**
     * New function for use of display configuration within xml files. This one gets
     * value.
     *
     * @return value as String
     */
    String getValue();
}

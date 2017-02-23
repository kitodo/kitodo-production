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

package org.goobi.api.display;

public class Item {
    private String myLabel;
    private String myValue;
    private Boolean isSelected;

    /**
     * Creates a new item with given params
     *
     * @param label label of the item
     * @param value value of the item
     * @param selected indicates whether an item is preselected or not
     */

    public Item(String label, String value, Boolean selected ){
        setLabel(label);
        setValue(value);
        setIsSelected(selected);

    }

    /**
     *
     * @param myLabel sets label for the item
     */

    public void setLabel(String myLabel) {
        this.myLabel = myLabel;
    }

    /**
     *
     * @return label of the item
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     *
     * @param myValue sets value for the item
     */

    public void setValue(String myValue) {
        this.myValue = myValue;
    }

    /**
     *
     * @return value of the item
     */
    public String getValue() {
        return myValue;
    }

    /**
     *
     * @param isSelected sets Boolean that indicates whether item is preselected or not
     */

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    /**
     *
     * @return Boolean: is preselected or not
     */
    public Boolean getIsSelected() {
        return isSelected;
    }

}

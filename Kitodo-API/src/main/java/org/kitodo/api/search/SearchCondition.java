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

package org.kitodo.api.search;

public class SearchCondition<S, V> {

    /** The field to search in. */
    private String fieldName;

    /** If the field should contain the value or not. */
    private boolean include;

    /** The value to search for. */
    private V value;

    /**
     * Gets the fieldName.
     * 
     * @return The fieldName.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the fieldName.
     * 
     * @param fieldName
     *            The fieldName.
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the include.
     * 
     * @return The include.
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * Sets the include.
     * 
     * @param include
     *            The include.
     */
    public void setInclude(boolean include) {
        this.include = include;
    }

    /**
     * Gets the value.
     * 
     * @return The value.
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(V value) {
        this.value = value;
    }
}

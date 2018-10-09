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

package org.kitodo.config.beans;

import java.util.ArrayList;
import java.util.List;

public class Parameter<T> {

    private String key;
    private T defaultValue;
    private List<T> possibleValues;

    /**
     * Public constructor with given key.
     * 
     * @param key
     *            of parameter
     */
    public Parameter(String key) {
        this.key = key;
        this.defaultValue = null;
        this.possibleValues = new ArrayList<>();
    }

    /**
     * Public constructor with given key and default value.
     * 
     * @param key
     *            of parameter
     * @param defaultValue
     *            of parameter
     */
    public Parameter(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.possibleValues = new ArrayList<>();
    }

    /**
     * Public constructor with given key, default value.
     * 
     * @param key
     *            of parameter
     * @param defaultValue
     *            of parameter
     * @param possibleValues
     *            of parameter
     */
    public Parameter(String key, T defaultValue, List<T> possibleValues) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.possibleValues = possibleValues;
    }

    /**
     * Get key.
     *
     * @return value of key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set key.
     *
     * @param key
     *            as java.lang.String
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get default value.
     *
     * @return value of defaultValue
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set default value.
     *
     * @param defaultValue
     *            as T
     */
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get possible values.
     *
     * @return value of possible values
     */
    public List<T> getPossibleValues() {
        return possibleValues;
    }

    /**
     * Set possible values.
     *
     * @param possibleValues
     *            as java.util.List&lt;T&gt;
     */
    public void setPossibleValues(List<T> possibleValues) {
        this.possibleValues = possibleValues;
    }
}

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

package org.kitodo.production.metadata.copier;

import java.lang.reflect.Field;

import org.kitodo.exceptions.MetadataException;

/**
 * A VariableSelector provides methods to retrieve variables used in the source
 * code.
 */
public class VariableSelector extends DataSelector {

    /**
     * Holds the name of the variable to resolve.
     */
    private final String qualifier;

    /**
     * If the selector passed to the constructor references a variable that is
     * part of an object which is held in a variable itself, then this
     * VariableSelector only handles the resolving of the first variable, and
     * the field subselector holds another VariableSelector to resolve the
     * remaining variable. Otherwise, this field is null.
     */
    private final VariableSelector subselector;

    /**
     * Creates a new VariableSelector.
     *
     * @param selector
     *            String identifying a variable
     */
    public VariableSelector(String selector) {
        if (selector.startsWith(VARIABLE_REFERENCE)) {
            selector = selector.substring(1);
        }
        String[] a = selector.split("\\.", 2);
        if (a.length == 2) {
            this.qualifier = a[0];
            this.subselector = new VariableSelector(a[1]);
        } else {
            this.qualifier = selector;
            this.subselector = null;
        }
    }

    /**
     * Returns the value of the variable named by the path used to construct the
     * variable selector. Returns null if the variable isn’t available.
     *
     * @param data
     *            object to inspect
     * @return value of the variable, or null if not found
     * @see org.kitodo.production.metadata.copier.DataSelector#findIn(CopierData)
     */
    @Override
    public String findIn(CopierData data) {
        return findIn((Object) data);
    }

    /**
     * Returns the value of the variable named by the path used to construct the
     * variable selector. Returns null if the variable isn’t available.
     *
     * @param classInstance
     *            object to inspect
     * @return value of the variable, or null if not found
     */
    private String findIn(Object classInstance) {
        try {
            Field classesFieldReference = classInstance.getClass().getDeclaredField(qualifier);
            classesFieldReference.setAccessible(true);
            Object fieldValue = classesFieldReference.get(classInstance);
            if (subselector == null) {
                return String.valueOf(fieldValue);
            } else {
                return fieldValue != null ? subselector.findIn(fieldValue) : null;
            }
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    /**
     * Returns a string that textually represents this LocalMetadataSelector.
     *
     * @return a string representation of this LocalMetadataSelector
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (subselector == null) {
            return VARIABLE_REFERENCE + qualifier;
        } else {
            return VARIABLE_REFERENCE + qualifier + '.' + subselector.toString().substring(1);
        }
    }
}

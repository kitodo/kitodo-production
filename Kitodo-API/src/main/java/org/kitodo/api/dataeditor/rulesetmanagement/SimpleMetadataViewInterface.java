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

package org.kitodo.api.dataeditor.rulesetmanagement;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Provides an interface for the meta-data key view service. The meta-data key
 * view service provides a filtered view on meta-data keys.
 */
public interface SimpleMetadataViewInterface extends MetadataViewInterface {
    /**
     * Maps a boolean value to a meta-data value.
     *
     * @param value
     *            boolean input value
     * @return value to save as a meta-data entry. If absent, delete the
     *         meta-data entry.
     */
    default Optional<String> convertBoolean(boolean value) {
        return value ? Optional.of(getSelectItems().entrySet().iterator().next().getKey()) : Optional.empty();
    }

    /**
     * Returns the default value for the input type boolean.
     *
     * @return the default value
     */
    default boolean getBooleanDefaultValue() {
        return !getDefaultItems().isEmpty();
    }

    /**
     * Specifies the value for the entry that appears first when a field is
     * added.
     *
     * @return the default value
     */
    Collection<String> getDefaultItems();

    /**
     * Returns the default value for all input types except boolean and multiple
     * selection.
     *
     * @return the default value
     */
    default String getDefaultValue() {
        return getDefaultItems().iterator().next();
    }

    /**
     * Specifies how the input item should be displayed.
     *
     * @return how the input item should be displayed
     */
    InputType getInputType();

    /**
     * Returns the possible values if the meta-data key is a list of values.
     *
     * @return the possible values
     */
    Map<String, String> getSelectItems();

    /**
     * Returns {@code false}. A simple meta-data key is not complex.
     *
     * @return always false
     */
    @Override
    default boolean isComplex() {
        return false;
    }

    /**
     * Returns whether values under this key can be edited in this view.
     * 
     * @return whether values can be edited
     */
    boolean isEditable();

    /**
     * Returns whether the value corresponds to the value range. The value range
     * can be determined in various ways. Integers or dates must parse, it may
     * be that the value must be in a list or is checked against a regular
     * expression. The application can then still decide whether to allow the
     * value to be saved or not.
     *
     * @param value
     *            value to be tested
     * @return whether the value corresponds to the value range
     */
    boolean isValid(String value);

}

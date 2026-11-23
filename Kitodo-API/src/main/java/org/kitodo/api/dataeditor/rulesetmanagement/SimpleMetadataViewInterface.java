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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.MetadataEntry;

/**
 * Provides an interface for the metadata key view service. The metadata key
 * view service provides a filtered view on metadata keys.
 */
public interface SimpleMetadataViewInterface extends MetadataViewInterface {
    /**
     * Maps a boolean value to a metadata value.
     *
     * @param value
     *            boolean input value
     * @return value to save as a metadata entry. If absent, delete the
     *         metadata entry.
     */
    default Optional<String> convertBoolean(boolean value) {
        if (value) {
            return getSelectItems(Collections.emptyList()).keySet().stream().filter(StringUtils::isNotEmpty).findAny();
        } else {
            return Optional.empty();
        }
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
        Collection<String> defaultItems = getDefaultItems();
        return defaultItems.isEmpty() ? "" : defaultItems.iterator().next();
    }

    /**
     * Specifies how the input item should be displayed.
     *
     * @return how the input item should be displayed
     */
    InputType getInputType();
    
    /**
     * Returns the minimum number of digits for integer types.
     *
     * @return the minimum number of digits
     */
    int getMinDigits();

    /**
     * Returns the possible values if the metadata key is a list of values. For
     * the maps of metadata entries, the function should only read the map keys,
     * and should set the map value to {@link Boolean#TRUE} for those metadata
     * entries that do have an influence on the showing select items, to let the
     * caller know that it must update the select items in case this metadata
     * entry changes.
     *
     * @param metadata
     *            metadata entries. Conditional select items may depend on their
     *            values. For nested keys, order of arguments is top-down, i.e.
     *            first grand-grandparent, then grandparent, then parent, last
     *            sibling.
     * @return the possible values
     */
    Map<String, String> getSelectItems(List<Map<MetadataEntry, Boolean>> metadata);

    /**
     * Returns {@code false}. A simple metadata key is not complex.
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
     * Returns whether values under this key can be filtered in this view.
     *
     * @return whether values can be filtered
     */
    boolean isFilterable();

    /**
     * Returns whether the value corresponds to the value range. The value range
     * can be determined in various ways. Integers or dates must parse, it may
     * be that the value must be in a list or is checked against a regular
     * expression. The application can then still decide whether to allow the
     * value to be saved or not.
     *
     * @param value
     *            value to be tested
     * @param metadata
     *            metadata entries. The available options for conditional select
     *            items depend on their values. For nested keys, order of
     *            arguments is top-down, i.e. from grand-grandparent to sibling.
     * @return whether the value corresponds to the value range
     */
    boolean isValid(String value, List<Map<MetadataEntry, Boolean>> metadata);

}

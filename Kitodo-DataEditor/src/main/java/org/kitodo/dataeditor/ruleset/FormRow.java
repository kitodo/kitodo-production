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

package org.kitodo.dataeditor.ruleset;

import java.util.Collection;
import java.util.Optional;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;

/**
 * Return type for list entries consisting of a meta-data key and a collection
 * of values. Instances of this class are returned by the meta-data acquisition
 * mask builder and each represent a line of the meta-data input mask.
 *
 * @param <T>
 *            type of meta-data objects
 */
class FormRow<T> implements MetadataViewWithValuesInterface<T> {
    /**
     * A possible view on the key. None, if it is hidden.
     */
    private Optional<MetadataViewInterface> optionalKeyView;

    /**
     * The values. This is always one at most, except for multiple selections.
     */
    private Collection<T> values;

    public FormRow(Optional<MetadataViewInterface> optionalKeyView, Collection<T> values) {
        this.optionalKeyView = optionalKeyView;
        this.values = values;
    }

    /**
     * Returns a view of the key of the meta-data entry. This can not be even,
     * if this is hidden and there is a value.
     *
     * @return the key view
     */
    @Override
    public Optional<MetadataViewInterface> getMetadata() {
        return optionalKeyView;
    }

    /**
     * Returns the values, if any.
     *
     * @return the values
     */
    @Override
    public Collection<T> getValues() {
        return values;
    }

}

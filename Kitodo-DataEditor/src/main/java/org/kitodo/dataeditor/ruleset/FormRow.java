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

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;

/**
 * Return type for list entries consisting of a metadata key and a collection
 * of values. Instances of this class are returned by the metadata acquisition
 * mask builder and each represent a line of the metadata input mask.
 */
class FormRow implements MetadataViewWithValuesInterface {
    /**
     * A possible view on the key. None, if it is hidden.
     */
    private Optional<MetadataViewInterface> optionalKeyView;

    /**
     * The values. This is always one at most, except for multiple selections.
     */
    private Collection<Metadata> values;

    FormRow(Optional<MetadataViewInterface> optionalKeyView, Collection<Metadata> values) {
        this.optionalKeyView = optionalKeyView;
        this.values = values;
    }

    /**
     * Returns a view of the key of the metadata entry. This cannot be even,
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
    public Collection<Metadata> getValues() {
        return values;
    }

}

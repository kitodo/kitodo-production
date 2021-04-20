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
import java.util.List;

import org.kitodo.api.Metadata;

/**
 * Provides an interface for the complex-type view service. The complex-type
 * view service provides a filtered view of a complex metadata type.
 */
public interface ComplexMetadataViewInterface extends MetadataViewInterface {
    /**
     * Returns the metadata keys that the user can still add to the form. The
     * list contains only metadata keys that are still allowed at this point,
     * minus those that are already displayed anyway but have no value yet.
     *
     * @param entered
     *            metadata objects that have already been entered, along with
     *            their key
     * @param additionallySelected
     *            metadata keys that the user has already selected
     * @return the metadata keys that the user can add
     */
    Collection<MetadataViewInterface> getAddableMetadata(Collection<Metadata> entered,
            Collection<String> additionallySelected);

    /**
     * Returns the metadata keys that are allowed on this complex metadata view.
     * This function exists for internal use to check whether a metadata record
     * is generally allowed in some place.
     *
     * @return the metadata keys that are allowed here
     */
    Collection<MetadataViewInterface> getAllowedMetadata();

    /**
     * Returns the metadata keys to display. The list consists of the keys to be
     * displayed, keys that are displayed because there are values for them, and
     * keys that the user has already manually added. The list is created and
     * then sorted according to the given sorting rules.
     *
     * @param entered
     *            metadata objects that have already been entered, along with
     *            their key
     * @param additionallySelected
     *            metadata keys that the user has already selected
     * @return The sorted list of keys with the value objects. If keys are
     *         already filled with values, the values are returned here. If a
     *         key is a multiple-selection, values are grouped below it.
     */
    List<MetadataViewWithValuesInterface> getSortedVisibleMetadata(Collection<Metadata> entered,
            Collection<String> additionallySelected);

    /**
     * Returns {@code true}. A complex metadata key is complex.
     *
     * @return always true
     */
    @Override
    default boolean isComplex() {
        return true;
    }
}

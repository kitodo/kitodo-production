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
import java.util.Map;

/**
 * Provides an interface for the complex-type view service. The complex-type
 * view service provides a filtered view of a complex meta-data type.
 */
public interface ComplexMetadataViewInterface extends MetadataViewInterface {
    /**
     * Returns the meta-data keys that the user can still add to the form. The
     * list contains only meta-data keys that are still allowed at this point,
     * minus those that are already displayed anyway but have no value yet.
     *
     * @param <T>
     *            the type of meta-data objects
     * @param entered
     *            meta-data objects that have already been entered, along with
     *            their key
     * @param additionallySelected
     *            meta-data keys that the user has already selected
     * @return the meta-data keys that the user can add
     */
    <T> Collection<MetadataViewInterface> getAddableMetadata(Map<T, String> entered,
            Collection<String> additionallySelected);

    /**
     * Returns the meta-data keys to display. The list consists of the keys to
     * be displayed, keys that are displayed because there are values for them,
     * and keys that the user has already manually added. The list is created
     * and then sorted according to the given sorting rules.
     *
     * @param <T>
     *            the type of meta-data objects
     * @param entered
     *            meta-data objects that have already been entered, along with
     *            their key
     * @param additionallySelected
     *            meta-data keys that the user has already selected
     * @return The sorted list of keys with the value objects. If keys are
     *         already filled with values, the values are returned here. If a
     *         key is a multiple-selection, values are grouped below it.
     */
    <T> List<MetadataViewWithValuesInterface<T>> getSortedVisibleMetadata(Map<T, String> entered,
            Collection<String> additionallySelected);

    /**
     * Returns {@code true}. A complex meta-data key is complex.
     *
     * @return always true
     */
    @Override
    default boolean isComplex() {
        return true;
    }
}

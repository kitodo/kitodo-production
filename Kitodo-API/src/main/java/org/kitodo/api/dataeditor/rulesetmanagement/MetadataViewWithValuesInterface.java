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
import java.util.Optional;

import org.kitodo.api.Metadata;

/**
 * Return value of the function
 * {@code ComplexMetadataViewInterface.getSortedVisibleMetadatas()}. The
 * function returns a sorted list consisting of objects of this type. It always
 * contains the key and the list of values if values have already been defined
 * for this key. If the key is a multiple choice, the values are grouped, hence
 * the list, otherwise the length of the list is always only one. Effectively,
 * the return type of
 * {@link ComplexMetadataViewInterface#getSortedVisibleMetadatas(Collection, Collection)}
 * is {@code List<Pair<MetadataViewInterface, Collection<T>>>}. Since the key
 * must be repeatable if there is more than one value but the key is not a
 * multiple choice, a map would be an inappropriate choice at this point.
 */
public interface MetadataViewWithValuesInterface {
    /**
     * Returns the key to be displayed at this point. The interface can return
     * entries where the key is {@code null} with values if there were values
     * for excluded keys.
     *
     * @return the key
     */
    Optional<MetadataViewInterface> getMetadata();

    /**
     * Returns the values for the key. The list is empty if there were none for
     * this key in the passed values, otherwise the object is in it. If it is a
     * key that represents a multiple choice, multiple values for that key are
     * grouped in the list. Otherwise the list always has the length 1 (or 0).
     *
     * @return the values for the key
     */
    Collection<Metadata> getValues();
}

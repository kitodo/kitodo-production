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

package org.kitodo.api.ugh;

import java.util.List;
import java.util.Map;

/**
 * Interface of a metadata group type.
 */
public interface MetadataGroupTypeInterface {

    /**
     * Adds a metadata type to this metadata group type.
     *
     * @param metadataType
     *            metadata type to add
     */
    void addMetadataType(MetadataTypeInterface metadataType);

    /**
     * Returns all display labels of this metadata group type.
     *
     * @return a map of display labels of this metadata group type
     */
    Map<String, String> getAllLanguages();

    /**
     * Returns the display label of this metadata group type in a given
     * language.
     *
     * @param language
     *            language of the display label to return
     * @return the display label in the given language
     */
    String getLanguage(String language);

    /**
     * Returns the member metadata type list of this metadata group.
     *
     * @return the member metadata type list
     */
    List<MetadataTypeInterface> getMetadataTypeList();

    /**
     * Returns the internal name (that is, the ID) of this metadata group.
     *
     * @return the ID of this
     */
    String getName();

    /**
     * Sets all display labels for this metadata group type.
     *
     * @param allLanguages
     *            the map of display labels of this metadata group type
     */
    void setAllLanguages(Map<String, String> allLanguages);

    /**
     * Sets the internal name (that is, the ID) of this metadata group.
     *
     * @param name
     *            the ID of this
     */
    void setName(String name);

    /**
     * Sets the minimum required/maximum allowed quantity for this metadata
     * group type.
     *
     * @param quantityRestriction
     *            one of "1m", "1o", "+", or "*"
     * @return {@code false}, if the string argument is not one of these four
     *         string; {@code true} otherwise. The return value is never used.
     */
    boolean setNum(String quantityRestriction);
}

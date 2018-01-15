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
 * Interface of a meta-data group type.
 */
public interface MetadataGroupTypeInterface {

    /**
     * Adds a meta-data type to this meta-data group type.
     *
     * @param metadataType
     *            meta-data type to add
     */
    void addMetadataType(MetadataTypeInterface metadataType);

    /**
     * Returns all display labels of this meta-data group type.
     *
     * @return a map of display labels of this meta-data group type
     */
    Map<String, String> getAllLanguages();

    /**
     * Returns the display label of this meta-data group type in a given
     * language.
     *
     * @param language
     *            language of the display label to return
     * @return the display label in the given language
     */
    String getLanguage(String language);

    /**
     * Returns the member meta-data type list of this meta-data group.
     *
     * @return the member meta-data type list
     */
    List<MetadataTypeInterface> getMetadataTypeList();

    /**
     * Returns the internal name (that is, the ID) of this meta-data group.
     *
     * @return the ID of this
     */
    String getName();

    /**
     * Sets all display labels for this meta-data group type.
     *
     * @param allLanguages
     *            the map of display labels of this meta-data group type
     */
    void setAllLanguages(Map<String, String> allLanguages);

    /**
     * Sets the internal name (that is, the ID) of this meta-data group.
     *
     * @param name
     *            the ID of this
     */
    void setName(String name);

    /**
     * Sets the minimum required/maximum allowed quantity for this meta-data
     * group type.
     *
     * @param quantityRestriction
     *            one of "1m", "1o", "+", or "*"
     * @return {@code false}, if the string argument is not one of these four
     *         string; {@code true} otherwise. The return value is never used.
     */
    boolean setNum(String quantityRestriction);
}

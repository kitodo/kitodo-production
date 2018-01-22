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

import java.util.HashMap;
import java.util.Map;

/**
 * When using, storing, writing or reading meta-data, groups or classes of
 * special meta-data objects can be formed, which have something in common. They
 * are all of the same kind. Meta-data of the same kind can be stored using the
 * same MetadataType object. Each MetadataType object can be identified easily
 * by using its internal name.
 *
 * <p>
 * Besides the internal name, a MetadataType object contains information about
 * it occurrences; some meta-data may occur just once, other may occur many
 * times.
 *
 * <p>
 * E.g., for all titles of a document, there can be a separate MetadataType
 * element, which contains information about this class of meta-data elements.
 * Information which they share are information about their occurrences; each
 * structure entity can only have a single title.
 *
 * <p>
 * MetadataType objects can occur in two different ways:
 * <ul>
 * <li>globally
 * <li>locally
 * </ul>
 *
 * <b>Global</b> {@code MetadataType} objects can be retrieved from the
 * {@code Prefs} object by giving the internal name. Some of the information of
 * a MetadataType object depends on the context in which it is used. Context
 * means it depends on the {@code DocStructType} object, in which a MetadataType
 * object is used. When adding a {@code MetadataType} object to a
 * {@code DocStructType} object, an internal copy is created and stored with the
 * {@code DocStructType} object. This copy is called <b>local</b> and may store
 * information about its occurrences in this special {@code DocStructType}
 * object. The {@code DocStructType} class contains methods to retrieve local
 * {@code MetadataType} objects from global ones.
 *
 * <p>
 * {@code MetadataType} objects are used to create new {@code MetadataType}
 * objects. They are the only parameter in the constructor of the
 * {@code MetadataType} object.
 */
public interface MetadataTypeInterface {

    /**
     * Returns all display labels of this meta-data type.
     *
     * @return a map of display labels of this meta-data type
     */
    Map<String, String> getAllLanguages();

    /**
     * Returns whether the meta-data type is a person type or not.
     */
    boolean isPerson();

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
     * Returns the internal name (that is, the ID) of this meta-data group.
     *
     * @return the ID of this
     */
    String getName();

    /**
     * Returns the display label of this meta-data group type in a given
     * language.
     *
     * @param language
     *            language of the display label to return
     * @return the display label in the given language
     */
    default String getNameByLanguage(String language) {
        return getLanguage(language);
    }

    /**
     * Gets the minimum required/maximum allowed quantity for this meta-data
     * group type.
     *
     * @return one of "1m", "1o", "+", or "*"
     */
    String getNum();

    /**
     * Sets all display labels for this meta-data type.
     *
     * @param labels
     *            the map of display labels of this meta-data type
     */
    void setAllLanguages(HashMap<String, String> labels);

    /**
     * Sets whether the meta-data type is an identifier type.
     *
     * @param identifier
     *            whether the meta-data type is an identifier type
     */
    void setIdentifier(boolean identifier);

    /**
     * Sets whether the meta-data type is a person type.
     *
     * @param person
     *            whether the meta-data type is a person type
     */
    void setPerson(boolean person);

    /**
     * Sets the internal name (that is, the ID) of this meta-data group.
     *
     * @param name
     *            the ID of this
     */
    void setName(String name);

    /**
     * Sets the minimum required/maximum allowed quantity for this meta-data
     * type.
     *
     * @param quantityRestriction
     *            one of "1m", "1o", "+", or "*"
     */
    void setNum(String quantityRestriction);
}

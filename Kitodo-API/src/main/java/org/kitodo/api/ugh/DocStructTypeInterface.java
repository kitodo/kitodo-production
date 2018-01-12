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

/**
 * A {@code DocStructType} object defines a kind of class to which a structure
 * entitiy (represented by a DocStruct object) belongs. All DocStruct objects
 * belonging to a similar class have something in common (like possible
 * children, special kind of metadata which can be available for a class, a
 * naming etc.). These things are stored in a DocStructType object.
 */
public interface DocStructTypeInterface {

    /**
     * Returns a list containing the names of all doc struct types which are
     * allowed as children.
     *
     * @return all doc struct types which are allowed as children. Note:
     *         {@code List<String>}, not {@code List<DocStructTypeInterface>}!
     */
    List<String> getAllAllowedDocStructTypes();

    /**
     * Retrieves all metadata type objects for this doc struct type instance.
     *
     *
     * @return all metadata type objects for this doc struct type.
     *         {@code Iterable<>} would be sufficient.
     */
    List<MetadataTypeInterface> getAllMetadataTypes();

    /**
     * Returns the name of the anchor structure, if any, or {@code null}
     * otherwise. Anchors are a special type of document structure, which group
     * other structure entities together, but have no own content. E.g. a
     * periodical as such can be an anchor. The periodical itself is a virtual
     * structure entity without any own content, but groups all years of
     * appearance together. Years may be anchors again for volumes, etc.
     *
     * @return String, which is {@code null} if it cannot be used as an anchor
     */
    String getAnchorClass();

    /**
     * Returns the internal name (i.e. ID) of this doc struct type.
     *
     * @return the name of this doc struct type
     */
    String getName();

    /**
     * Returns the display label for this doc struct type in the given language.
     * Returns {@code null} if no translation is available.
     *
     * @param language
     *            language code
     * @return name the display label for this doc struct type
     */
    String getNameByLanguage(String language);

    /**
     * Returns the minimal required or maximum allowed quantity for a metadata
     * object for the given metadata type for this doc struct type. metadata
     * types are compared using the internal name.
     *
     * @param metadataType
     *            metadata type can be a global type
     * @return String representing the allowed quantity, one of "1o", "1m", "*",
     *         or "+".
     */
    String getNumberOfMetadataType(MetadataTypeInterface metadataType);

}

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

import java.util.Collection;
import java.util.List;

/**
 * A meta-data group object represents a single meta-data group element. Each
 * meta-data group element has at least one meta-data element. The type of a
 * meta-data group element is stored as a {@code MetadataGroupType} object.
 *
 * <p>
 * Metadata group are a list of Metadata.
 */
public interface MetadataGroupInterface {

    /**
     * Adds a meta-data entry to this meta-data group.
     *
     * @param metadata
     *            meta-data entry to add
     */
    void addMetadata(MetadataInterface metadata);

    /**
     * Adds a meta-data entry of a person type to this meta-data group.
     *
     * @param person
     *            person entry to add
     */
    void addPerson(PersonInterface person);

    /**
     * Returns all meta-data entries of a given type.
     *
     * @param type
     *            type of meta-data entries to return
     * @return all meta-data entries of that type
     */
    List<MetadataInterface> getMetadataByType(String type);

    /**
     * Returns all meta-data entries.
     *
     * @return all meta-data entries
     */
    List<MetadataInterface> getMetadataList();

    /**
     * Returns all person-type meta-data entries of a given type.
     *
     * @param type
     *            type of meta-data entries to return
     * @return all meta-data entries of that type
     */
    Iterable<PersonInterface> getPersonByType(String type);

    /**
     * Returns all person-type meta-data entries.
     *
     * @return all person-type meta-data entries
     */
    Collection<PersonInterface> getPersonList();

    /**
     * Returns the meta-data group type of this meta-data group.
     *
     * @return the meta-data group type
     */
    MetadataGroupTypeInterface getMetadataGroupType();
}

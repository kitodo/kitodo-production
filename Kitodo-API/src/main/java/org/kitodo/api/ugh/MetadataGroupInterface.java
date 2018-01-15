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
 * A metadata group object represents a single metadata group element. Each
 * metadata group element has at least one metadata element. The type of a
 * metadata group element is stored as a {@code} MetadataGroupType} object.
 *
 * <p>
 * Metadata group are a list of Metadata.
 */
public interface MetadataGroupInterface {

    /**
     * Adds a meta-data entry to this metadata group.
     * 
     * @param metadata
     *            metadata entry to add
     */
    void addMetadata(MetadataInterface metadata);

    /**
     * Adds a meta-data entry of a person type to this metadata group.
     * 
     * @param metadata
     *            person entry to add
     */
    void addPerson(PersonInterface person);

    /**
     * Returns all metadata entries of a given type.
     * 
     * @param type
     *            type of metadata entries to return
     * @return all metadata entries of that type
     */
    List<MetadataInterface> getMetadataByType(String type);

    /**
     * Returns all metadata entries.
     * 
     * @return all metadata entries
     */
    public List<MetadataInterface> getMetadataList();

    /**
     * Returns all person-type metadata entries of a given type.
     * 
     * @param type
     *            type of metadata entries to return
     * @return all metadata entries of that type
     */
    public Iterable<PersonInterface> getPersonByType(String type);

    /**
     * Returns all person-type metadata entries.
     * 
     * @return all person-type metadata entries
     */
    public Collection<PersonInterface> getPersonList();

    /**
     * Returns the metadata group type of this metadata group.
     * 
     * @return the metadata group type
     */
    public MetadataGroupTypeInterface getType();
}

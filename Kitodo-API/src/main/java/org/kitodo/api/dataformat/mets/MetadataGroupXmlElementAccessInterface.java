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

package org.kitodo.api.dataformat.mets;

import java.util.Collection;

/**
 * Interface for a service that handles access to the
 * {@code <kitodo:metadataGroup>} element.
 *
 * <p>
 * A meta-data group is a collection of meta-data that is associated with each
 * other, shared, and repeated together. An example of a meta-data group may be
 * the description of a person involved in a work of art. The person has several
 * characteristics that all relate to the same person, for example, first name,
 * surname, or date of birth and death. There may have been several people
 * involved in a work of art. Nevertheless, it is not the same to say a book has
 * the author Charles Dickens and the illustrator Felix “Flix” Görmann, as to
 * say a book has a writer and an illustrator, it also has the first names
 * Chales, Flix and Felix and has the last names Dickens and Görmann. That’s
 * what meta-data groups are for.
 *
 * <p>
 * Meta-data is stored as a collection of key-value pairs. Access to the key is
 * possible via the parent interface, this interface grants access to the value,
 * which is meta-data by itself. Meta-data groups can be nested.
 */
public interface MetadataGroupXmlElementAccessInterface extends MetadataAccessInterface {
    /**
     * Returns the modifiable collection of meta-data contained in this
     * meta-data group. Adding, modifying and deleting meta-data in the group is
     * possible via the corresponding methods of the collection interface.
     *
     * @return the collection of meta-data in this group
     */
    Collection<MetadataAccessInterface> getMetadata();
}

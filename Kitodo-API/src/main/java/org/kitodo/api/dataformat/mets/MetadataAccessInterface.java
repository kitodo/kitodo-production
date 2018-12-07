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

/**
 * Abstract super-interface for the services that handle access to the
 * {@code <kitodo:metadata>} and {@code <kitodo:metadataGroup>} elements.
 *
 * <p>
 * Meta-data is stored in Production as a collection of key-value pairs. This
 * super interface grants access to the key while providing access to the value
 * through various subinterfaces, the use of which depends on the nature of the
 * meta-data, as the value must be accessed in different ways accordingly.
 *
 * <p>
 * This interface can be understood as abstract. Its function is to be able to
 * keep all objects that implement one of its subinterfaces in a common
 * collection. Also, functions that are the same in all sub-interfaces have been
 * summarized in this super interface.
 */
public interface MetadataAccessInterface {
    /**
     * Specifies the location of the meta-data entry in the METS file. METS
     * allows the storage of meta-data in five different areas with different
     * semantics. See {@link MdSec} for details.
     *
     * @return the location of the meta-data entry
     */
    MdSec getDomain();

    /**
     * Returns the type of the meta-data entry. The type is used to describe the
     * meta-data entry, i.e. whether the value of the entry is about the title,
     * the author or a summary of an intellectual work. Even if the type is
     * stored as simple text it is worth using a controlled vocabulary for this.
     *
     * @return the type of the meta-data entry
     */
    String getType();

    /**
     * Specifies the location of the meta-data entry in the METS file. METS
     * allows the storage of meta-data in five different areas with different
     * semantics. See {@link MdSec} for details. Note that you cannot set the
     * location for a meta-data entry that is a member of a meta-data group
     * differently from the location of the meta-data group.
     *
     * @param domain
     *            location to set for the meta-data entry
     */
    void setDomain(MdSec domain);

    /**
     * Sets the type of the meta-data entry. The type is used to describe the
     * meta-data entry, i.e. whether the value of the entry is about the title,
     * the author or a summary of an intellectual work. Even if the type is
     * stored as simple text it is worth using a controlled vocabulary for this.
     *
     * @param type
     *            the type of the meta-data entry
     */
    void setType(String type);
}

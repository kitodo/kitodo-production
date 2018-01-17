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

/**
 * A meta-data object represents a single meta-data entry. Each meta-data entry
 * has at least a value and a type. The type of the a meta-data element is
 * stored as a meta-data type object.
 *
 * <p>
 * Metadata can be any kind of data, which can be attached to a structure
 * element. The most common meta-data, which is available for almost any
 * structure element, is a title.
 */
public interface MetadataInterface {
    /**
     * Returns the document structure instance, to which this meta-data object
     * belongs. This is extremely helpful, if only the meta-data instance is
     * stored in a list; the reference to the associated document structure
     * instance is always kept.
     *
     * @return the document structure instance
     */
    DocStructInterface getDocStruct();

    /**
     * Returns the type of the meta-data instance. The MetadataType object which
     * is returned, may have the same name, but be a different object than the
     * MetadataType object from another MetadataType.
     *
     * @return the type of the meta-data instance
     */
    MetadataTypeInterface getMetadataType();

    /**
     * Returns the value of the meta-data object. Is always a string value all
     * types are converted to unicode strings and must be converted by the user.
     *
     * @return the value of the meta-data object
     */
    String getValue();

    /**
     * Sets the document structure entity to which this object belongs to.
     *
     * @param docStruct
     *            document structure entity to which this object belongs
     */
    void setDocStruct(DocStructInterface docStruct);

    /**
     * Sets the MetadataType for this instance.
     *
     * @param metadataType
     *            type to set
     * @return always {@code true}. The result is never used.
     */
    boolean setType(MetadataTypeInterface metadataType);

    /**
     * Sets the meta-data value.
     *
     * @param value
     *            value to set
     *
     * @return always {@code true}. The result is never used.
     */
    boolean setStringValue(String value);
}

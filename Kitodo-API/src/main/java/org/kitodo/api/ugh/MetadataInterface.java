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
 * A metadata object represents a single metadata entry. Each metadata entry has
 * at least a value and a type. The type of the a metadata element is stored as
 * a metadata type object.
 *
 * <p>
 * Metadata can be any kind of data, which can be attached to a structure
 * element. The most common metadata, which is available for almost any
 * structure element, is a title.
 */
public interface MetadataInterface {
    /**
     * Returns the doc struct instance, to which this metadata object belongs.
     * This is extremely helpful, if only the metadata instance is stored in a
     * list; the reference to the associated doc struct instance is always kept.
     *
     * @return the doc struct instance
     */
    public DocStructInterface getDocStruct();

    /**
     * Returns the type of the metadata instance. The MetadataType object which
     * is returned, may have the same name, but be a different object than the
     * MetadataType object from another MetadataType.
     *
     * @return the type of the metadata instance
     */
    public MetadataTypeInterface getMetadataType();

    /**
     * Returns the value of the metadata object. Is always a string value all
     * types are converted to unicode strings and must be converted by the user.
     *
     * @return the value of the metadata object
     */
    public String getValue();

    /**
     * Sets the document structure entity to which this object belongs to.
     *
     * @param docStruct
     *            document structure entity to which this object belongs
     */
    public void setDocStruct(DocStructInterface docStruct);

    /**
     * Sets the MetadataType for this instance.
     *
     * @param metadataType
     *            type to set
     * @return always {@code true}. The result is never used.
     */
    public boolean setType(MetadataTypeInterface metadataType);

    /**
     * Sets the metadata value.
     * 
     * @param value
     *            value to set
     *
     * @return always {@code true}. The result is never used.
     */
    public boolean setStringValue(String value);
}

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
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;

/**
 * A DigitalDocument represents a digital version of a work. This representation
 * contains the following information:
 * </p>
 *
 * <ul>
 * <li>metadata</li>
 * <li>structure of a work</li>
 * <li>content</li>
 * </ul>
 *
 * <p>
 * Those three different objects can be linked to each other in ways forming a
 * very complex object. The underlying document model tries to reduce the
 * complexity by defining some rules:
 * </p>
 *
 * <ul>
 * <li>every <code>DigitalDocument</code> has two kind of structures:
 *
 * <ul>
 * <li>logical structure: this structure represents the logical view. The
 * logical view is normally represented by chapters, paragraphs etc.</li>
 *
 * <li>physical structure: The physical structure represents the physical
 * representation of a work. For a book the physical binding and the pages can
 * be regarded a part of the physical structure.</li>
 *
 * Each structure has a single top structure element. These structure elements
 * are represented by <code>DocStruct</code> objects and may have children.
 *
 * </ul>
 * <li>metadata to this digital document is stored in structure entities</li>
 * <li>the content is represented by content files</li>
 * <li>ContentFiles can be linked to structure entities</li>
 * </ul>
 */
public interface DigitalDocumentInterface {

    /**
     * Add all content files to the digital document according to the
     * pathimagefiles metadata. The pages in the physical DocStruct must already
     * exist!
     */
    void addAllContentFiles();

    /**
     * Creates a doc struct with the given doc struct type for the Digital
     * Document.
     *
     * @param docStructType
     *            doc struct type for the new doc struct
     * @return the new doc struct
     * @throws TypeNotAllowedForParentException
     *             if this doc struct type is not allowed for the parent
     */
    DocStructInterface createDocStruct(DocStructTypeInterface docStructType) throws TypeNotAllowedForParentException;

    /**
     * Returns the file set.
     *
     * @return the file set
     */
    FileSetInterface getFileSet();

    /**
     * Returns the logical doc struct.
     *
     * @return the logical doc struct
     */
    DocStructInterface getLogicalDocStruct();

    /**
     * Returns the physical doc struct
     *
     * @return the physical doc struct
     */
    DocStructInterface getPhysicalDocStruct();

    /**
     * Overrides the content files of DigitalDocument with new names for images.
     *
     * @param images
     *            a list of sorted image names
     */
    void overrideContentFiles(List<String> images);

    /**
     * Sets the logical doc struct.
     *
     * @param docStruct
     *            the new doc struct
     * @return always {@code true}. The result is never used.
     */
    boolean setLogicalDocStruct(DocStructInterface docStruct);

    /**
     * Sets the physical doc struct.
     *
     * @param docStruct
     *            the new doc struct @return always {@code true}. The result is
     *            never used.
     */
    boolean setPhysicalDocStruct(DocStructInterface docStruct);

}

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
import java.util.List;

/**
 * Interface for a service that handles access to the {@code <mets:div>}
 * element.
 *
 * <p>
 * A div is a node of an outline tree, which divides the description of the
 * digitized work according to the principle of a table of contents. Each div
 * refers to an ordered list of one or more views (METS idiom: areas) that mimic
 * the section of the digitized work described by this node when consumed in the
 * given order. Each div can be assigned an amount of meta-data describing the
 * described section of the digitized work in abstracted form.
 */
public interface DivXmlElementAccessInterface {
    /**
     * Returns services to access the views (METS idiom: areas) that mimic the
     * section of the digitized work associated with this div. Since this area
     * is currently not fully developed, it can currently be assumed that the
     * number of areas is always 1.
     *
     * @return the areas of this div
     */
    List<AreaXmlElementAccessInterface> getAreas();

    /**
     * Returns services for access to the outline level that is a child of this
     * outline level.
     *
     * @return services for access to the children of this div
     */
    List<DivXmlElementAccessInterface> getChildren();

    /**
     * Returns the human-readable name of this outline element.
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns all meta-data that describe this outline level.
     *
     * @return the meta-data
     */
    Collection<MetadataAccessInterface> getMetadata();

    /**
     * Returns a variant of the human-readable name of this outline element,
     * which has been normalized so that it can be properly sorted by primitive
     * sorting algorithms.
     *
     * @return the orderlabel
     */
    String getOrderlabel();

    /**
     * Returns a type name for this item that allows you to infer the nature of
     * the conceptual unit of the item being represented. Examples: book,
     * chapter, page, …
     *
     * @return the type
     */
    String getType();

    /**
     * Sets the human-readable name of this outline element.
     *
     * @param label
     *            label to set
     */
    void setLabel(String label);

    /**
     * Sets the variant of the human-readable name of this outline element,
     * which has been normalized so that it can be properly sorted by primitive
     * sorting algorithms.
     *
     * @param orderlabel
     *            order label to set
     */
    void setOrderlabel(String orderlabel);

    /**
     * Sets a type name for this item that allows you to infer the nature of the
     * conceptual unit of the item being represented. Examples: book, chapter,
     * page, …
     *
     * @param type
     *            type to set
     */
    void setType(String type);
}

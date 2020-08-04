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

package org.kitodo.api.dataformat;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;

/**
 * The tree-like outline included structural element for digital representation.
 * This structuring included structural element can be subdivided into arbitrary
 * finely granular {@link #subincludedStructuralElements}. It can be described
 * by {@link #metadata}.
 */
public class IncludedStructuralElement implements Division<IncludedStructuralElement> {
    /**
     * The label for this included structural element. The label is displayed in
     * the graphical representation of the included structural element tree for
     * this level.
     */
    private String label;

    /**
     * Specifies the link if there is one.
     */
    private LinkedMetsResource link;

    /**
     * The metadata for this included structural element. This included
     * structural element level can be described with any metadata.
     */
    private Collection<Metadata> metadata = new HashSet<>();

    /**
     * Order of IncludedStructuralElements in relation to Views assigned to the parent.
     */
    private int order;

    /**
     * The order label of this included structural element, used to store the
     * machine-readable value if the label contains a human-readable value that
     * can be mapped to a machine-readable value.
     */
    private String orderlabel;

    /**
     * The sub-included structural elements of this included structural element,
     * which form the included structural element tree. The order of the
     * sub-included structural elements described by the order of the
     * {@code <div>} elements in the {@code <structMap TYPE="LOGICAL">} in the
     * METS file.
     */
    private List<IncludedStructuralElement> subincludedStructuralElements = new LinkedList<>();

    /**
     * The type of included structural element, for example, book, chapter,
     * page. Although the data type of this variable is a string, it is
     * recommended to use a controlled vocabulary. If the generated METS files
     * are to be used with the DFG Viewer, the list of possible included
     * structural element types is defined.
     *
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    private String type;

    /**
     * The views on {@link MediaUnit}s that this included structural element
     * level comprises.
     */
    private final LinkedList<View> views;

    /**
     * Creates a new included structural element.
     */
    public IncludedStructuralElement() {
        views = new LinkedList<>();
    }

    /**
     * Creates a new subclass of included structural element from an existing
     * included structural element.
     *
     * @param source
     *            included structural element that serves as data source
     */
    protected IncludedStructuralElement(IncludedStructuralElement source) {
        label = source.label;
        link = source.link;
        metadata = source.metadata;
        order = source.order;
        orderlabel = source.orderlabel;
        subincludedStructuralElements = source.subincludedStructuralElements;
        type = source.type;
        views = source.views;
    }

    /**
     * Returns the sub-included structural elements associated with this
     * included structural element.
     *
     * @return the sub-included structural elements
     */
    @Override
    public List<IncludedStructuralElement> getChildren() {
        return subincludedStructuralElements;
    }

    /**
     * Returns the label of this included structural element.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this included structural element.
     *
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the link of this included structural element.
     *
     * @return the link
     */
    public LinkedMetsResource getLink() {
        return link;
    }

    /**
     * Sets the link of this included structural element.
     *
     * @param link
     *            link to set
     */
    public void setLink(LinkedMetsResource link) {
        this.link = link;
    }

    /**
     * Returns the metadata on this included structural element.
     *
     * @return the metadata
     */
    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Get order.
     *
     * @return value of order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Set order.
     *
     * @param order as int
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the order label of this included structural element.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Sets the order label of this included structural element.
     *
     * @param orderlabel
     *            order label to set
     */
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Returns the type of this included structural element.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this included structural element.
     *
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the views associated with this included structural element.
     *
     * @return the views
     */
    public LinkedList<View> getViews() {
        return views;
    }

    @Override
    public String toString() {
        return type + " \"" + label + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IncludedStructuralElement that = (IncludedStructuralElement) o;
        return order == that.order
                && Objects.equals(label, that.label)
                && Objects.equals(link, that.link)
                && Objects.equals(metadata, that.metadata)
                && Objects.equals(orderlabel, that.orderlabel)
                && Objects.equals(subincludedStructuralElements, that.subincludedStructuralElements)
                && Objects.equals(type, that.type)
                && Objects.equals(views, that.views);
    }
}

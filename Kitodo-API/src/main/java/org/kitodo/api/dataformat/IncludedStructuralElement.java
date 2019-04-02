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

import org.kitodo.api.Metadata;

/**
 * The tree-like outline included structural element for digital representation.
 * This structuring included structural element can be subdivided into arbitrary
 * finely granular {@link #substructuralElements}. It can be described by
 * {@link #metadata}.
 */
public class IncludedStructuralElement extends StructuralElement {
    /**
     * The meta-data for this included structural element. This included
     * structural element level can be described with any meta-data.
     */
    private Collection<Metadata> metadata = new HashSet<>();

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
    private List<StructuralElement> substructuralElements = new LinkedList<>();

    /**
     * The views on {@link MediaUnit}s that this included structural element
     * level comprises. The list ensures the enforcement of the order of the
     * media units which is encoded by the media units’ {@code order} property.
     * To reorder this list, you must change the order property of the media
     * units.
     */
    private final Collection<View> views;

    /**
     * Creates a new included structural element.
     */
    public IncludedStructuralElement() {
        views = new SortedList<>(view -> view.getMediaUnit().getOrder());
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
        metadata = source.metadata;
        orderlabel = source.orderlabel;
        substructuralElements = source.substructuralElements;
        type = source.type;
        views = source.views;
    }

    /**
     * Returns the sub-included structural elements associated with this
     * included structural element.
     *
     * @return the sub-included structural elements
     */
    public List<StructuralElement> getChildren() {
        return substructuralElements;
    }

    /**
     * Returns the meta-data on this included structural element.
     *
     * @return the meta-data
     */
    public Collection<Metadata> getMetadata() {
        return metadata;
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
     * Returns the views associated with this included structural element.
     *
     * @return the views
     */
    public Collection<View> getViews() {
        return views;
    }
}

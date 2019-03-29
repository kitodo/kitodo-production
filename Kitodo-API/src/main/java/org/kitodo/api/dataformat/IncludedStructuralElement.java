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
 * The tree-like outline structural element for digital representation. This
 * structuring structural element can be subdivided into arbitrary finely
 * granular {@link #substructures}. It can be described by {@link #metadata}.
 */
public class IncludedStructuralElement extends StructuralElement {
    /**
     * The meta-data for this structural element. This structural element level
     * can be described with any meta-data.
     */
    private Collection<Metadata> metadata = new HashSet<>();

    /**
     * The order label of this structural element, used to store the
     * machine-readable value if the label contains a human-readable value that
     * can be mapped to a machine-readable value.
     */
    private String orderlabel;

    /**
     * The substructural elements of this included structural element, which
     * form the structural element tree. The order of the substructural elements
     * described by the order of the {@code <div>} elements in the
     * {@code <structMap TYPE="LOGICAL">} in the METS file.
     */
    private List<StructuralElement> substructures = new LinkedList<>();

    /**
     * The views on {@link MediaUnit}s that this structural element level
     * comprises. The list ensures the enforcement of the order of the media
     * units which is encoded by the media units’ {@code order} property. To
     * reorder this list, you must change the order property of the media units.
     */
    private final Collection<View> views;

    /**
     * Creates a new structural element.
     */
    public IncludedStructuralElement() {
        views = new SortedList<>(view -> view.getMediaUnit().getOrder());
    }

    /**
     * Creates a new subclass of structural element from an existing structural
     * element.
     *
     * @param includedStructuralElement
     *            structural element that serves as data source
     */
    protected IncludedStructuralElement(IncludedStructuralElement includedStructuralElement) {
        label = includedStructuralElement.label;
        metadata = includedStructuralElement.metadata;
        orderlabel = includedStructuralElement.orderlabel;
        substructures = includedStructuralElement.substructures;
        type = includedStructuralElement.type;
        views = includedStructuralElement.views;
    }

    /**
     * Returns the substructural elements associated with this structural
     * element.
     *
     * @return the substructural elements
     */
    public List<StructuralElement> getChildren() {
        return substructures;
    }

    /**
     * Returns the label of this structural element.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this structural element.
     *
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean isLinked() {
        return false;
    }

    /**
     * Returns the meta-data on this structural element.
     *
     * @return the meta-data
     */
    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Returns the order label of this structural element.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Sets the order label of this structural element.
     *
     * @param orderlabel
     *            order label to set
     */
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the views associated with this structural element.
     *
     * @return the views
     */
    public Collection<View> getViews() {
        return views;
    }

    @Override
    public String toString() {
        return type + " \"" + label + "\"";
    }
}

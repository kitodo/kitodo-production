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
 * The tree-like outline structure for digital representation. This structuring
 * structure can be subdivided into arbitrary finely granular
 * {@link #substructures}. It can be described by {@link #metadata}.
 */
public class Structure {
    /**
     * The label for this structure. The label is displayed in the graphical
     * representation of the structure tree for this level.
     */
    private String label;

    /**
     * The meta-data for this structure. This structure level can be described
     * with any meta-data.
     */
    private Collection<Metadata> metadata = new HashSet<>();

    /**
     * The order label of this structure. This is needed very rarely. It is not
     * displayed, and unlike the name suggests, it does not specify the order of
     * this substructure along with other substructures within its parent
     * structure, but the order is determined by the order of references from
     * the parent tree to each substructure. The order label may be used to
     * store the machine-readable value if the label contains a human-readable
     * value that can be mapped to a machine-readable value. An example of this
     * are calendar dates. For example, a label of “the fifteenth year of the
     * reign of Tiberius Caesar” could be stored as “{@code -0006}”.
     */
    private String orderlabel;

    /**
     * The substructures of this structure, which form the structure tree. The
     * order of the substructures described by the order of the {@code <div>}
     * elements in the {@code <structMap TYPE="LOGICAL">} in the METS file.
     */
    private List<Structure> substructures = new LinkedList<>();

    /**
     * The type of structure, for example, book, chapter, page. Although the
     * data type of this variable is a string, it is recommended to use a
     * controlled vocabulary. If the generated METS files are to be used with
     * the DFG Viewer, the list of possible structure types is defined.
     * 
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    private String type;

    /**
     * The views on media units that this structure level comprises. Currently,
     * only {@link AreaXmlElementAccess}s on media units as a whole are possible
     * with Production, but here it has already been built for the future, that
     * also {@code View}s on parts of {@link FileXmlElementAccess}s are to be
     * made possible. The list of {@code View}s is aware of the order of the
     * {@code MediaUnit}s encoded by the {@code MediaUnit}’s {@code order}
     * property. Although this list implements the {@link List} interface, it
     * always preserves the order as dictated by the {@code order} property of
     * the {@code MediaUnit}s. Therefore, to reorder this list, you must change
     * the {@code order} property of the {@code MediaUnit}s instead. It is not
     * possible to code several sequences that are in conflict with each other.
     */
    private final List<View> views = new SortedList<View>(view -> view.getMediaUnit().getOrder());

    /**
     * Returns the substructures associated with this structure.
     * 
     * @return the substructures
     */
    public List<Structure> getChildren() {
        return substructures;
    }

    /**
     * Returns the label of this structure.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this structure.
     * 
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the meta-data on this structure.
     * 
     * @return the meta-data
     */
    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Returns the order label of this structure.
     * 
     * @return the order label
     */
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Sets the order label of this structure.
     * 
     * @param orderlabel
     *            order label to set
     */
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Returns the type of this structure.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this structure.
     * 
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the views associated with this structure.
     * 
     * @return the views
     */
    public List<View> getViews() {
        return views;
    }
}

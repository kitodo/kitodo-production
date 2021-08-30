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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.api.Metadata;

/**
 * A tree-shaped description of the content of the digital representation of a
 * digital medium. This can be subdivided into finer granular children. It can
 * be described with metadata.
 *
 * @param <T>
 *            There are two versions of it, an {@link LogicalDivision}
 *            and a {@link PhysicalDivision}.
 */
public abstract class Division<T extends Division<T>> {
    /**
     * The children of this division, which form the structure tree.
     */
    private List<T> children = new LinkedList<>();

    private List<URI> contentIds = new ArrayList<>(0);

    /**
     * The label for this divison.
     */
    private String label;

    /**
     * The metadata for this division.
     */
    private Collection<Metadata> metadata = new HashSet<>();

    /**
     * Sequence number. The sequence number must be unique across all divisions.
     *
     * <p>
     * <i>For logical structures</i> meaningless, only used internally.
     *
     * <p>
     * <i>For physical divisions</i>, the display or playback order of several media
     * units referenced from one logical division is determined by
     * this attribute, not by the order of the references.
     */
    private int order;

    /**
     * The order label of this division.
     *
     * <p>
     * <i>For logical structures</i>, used to store <b>the machine-readable
     * value</b> if the <i>label</i> contains a human-readable value that can be
     * mapped to a machine-readable value.
     *
     * <p>
     * <i>For physical divisions</i>, <b>a human readable label</b> for the
     * <i>order</i> of this physical division. This need not be directly related to the
     * order number. Examples of order labels could be “I, II, III, IV, V, - ,
     * 1, 2, 3”, meanwhile the order would be “1, 2, 3, 4, 5, 6, 7, 8, 9”.
     */
    private String orderlabel;

    /**
     * The type of the division. Although the data type of this variable is a
     * string, it is recommended to use a controlled vocabulary. If the
     * generated METS files are to be used with the DFG Viewer, the list of
     * supported logical structure types is defined.
     *
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    private String type;

    /**
     * Creates a new division.
     */
    protected Division() {
    }

    /**
     * Creates a new division from an existing division. This is used by a
     * subclass to make a division an instance of the subclass, so the shallow
     * copies of {@code children} and {@code metadata} are intentional.
     *
     * @param source
     *            division that serves as data source
     */
    protected Division(Division<T> source) {
        children = source.children;
        label = source.label;
        metadata = source.metadata;
        order = source.order;
        orderlabel = source.orderlabel;
        type = source.type;
    }

    /**
     * Returns all children of this division as a flat list. The list isn’t
     * backed by the division, which means that insertions and deletions in the
     * list would not change the division. Therefore a list that cannot be
     * modified is returned.
     *
     * @return the children
     */
    public List<T> getAllChildren() {
        return Collections
                .unmodifiableList(children.stream().flatMap(Workpiece::treeStream).collect(Collectors.toList()));
    }

    /**
     * Returns the children of this division.
     *
     * @return the children
     */
    public List<T> getChildren() {
        return children;
    }

    /**
     * Returns the content IDs list of this division.
     *
     * @return the children
     */
    public List<URI> getContentIds() {
        return contentIds;
    }

    /**
     * Returns the label of this logical division.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this logical division.
     *
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the metadata on this structure.
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
     * @param order
     *            as int
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the order label for this physical division.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Sets the order label for this physical division.
     *
     * @param orderlabel
     *            order label to set
     */
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Returns the type of this division.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this division.
     *
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object compared) {
        if (this == compared) {
            return true;
        }
        if (compared == null) {
            return false;
        }
        if (!(compared instanceof Division)) {
            return false;
        }
        Division<?> other = (Division<?>) compared;
        return Objects.equals(children, other.children) && Objects.equals(label, other.label)
                && Objects.equals(metadata, other.metadata)
                && order == other.order && Objects.equals(orderlabel, other.orderlabel)
                && Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + order;
        result = prime * result + ((orderlabel == null) ? 0 : orderlabel.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
}

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

/**
 * A common interface for {@code Structure}s and {@code LinkedStructure}s.
 *
 * @see IncludedStructuralElement
 */
public abstract class StructuralElement {
    /**
     * The label of the linked structural element. The label is saved in the linked file.
     */
    protected String label;

    /**
     * The type of structural element, for example, book, chapter, page. Although the
     * data type of this variable is a string, it is recommended to use a
     * controlled vocabulary. If the generated METS files are to be used with
     * the DFG Viewer, the list of possible structural element types is defined. The type
     * is saved in the linked file.
     *
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    protected String type;

    /**
     * Returns the label of this structural element.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the type of this structural element.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns whether this structural element is a link to a structural element in another
     * workpiece.
     *
     * @return whether this structural element is a link
     */
    public abstract boolean isLinked();

    /**
     * Sets the label of this structural element.
     *
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the type of this structural element.
     *
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}

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
 * A common superclass for included and linked structural elements. If you get
 * this class as a result type, use instance-of-checks to find out which of the
 * two possible implementations, {@link IncludedStructuralElement} or
 * {@link LinkedStructuralElement}, it is.
 */
public abstract class StructuralElement {
    /**
     * The label for this structural element. The label is displayed in the
     * graphical representation of the structural element tree for this level.
     */
    protected String label;

    /**
     * The type of structural element, for example, book, chapter, page.
     * Although the data type of this variable is a string, it is recommended to
     * use a controlled vocabulary. If the generated METS files are to be used
     * with the DFG Viewer, the list of possible included structural element
     * types is defined.
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
     * Sets the label of this structural element.
     *
     * @param label
     *            label to set
     */
    public void setLabel(String label) {
        this.label = label;
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
     * Sets the type of this structural element.
     *
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " \"" + label + "\"";
    }
}

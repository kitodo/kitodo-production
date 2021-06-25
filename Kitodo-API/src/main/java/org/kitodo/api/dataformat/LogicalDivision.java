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

import java.util.LinkedList;
import java.util.Objects;

import org.kitodo.api.dataformat.mets.LinkedMetsResource;

/**
 * A tree-shaped description of the logical division of the digital
 * representation of a digital medium. The logical division can be
 * imagined as a table of contents and is used to display the table of contents
 * in the viewer. It uses {@link View}s to refer to elements of the
 * {@link PhysicalDivision} of the digital medium, or can {@link #link} to other
 * processes.
 */
public class LogicalDivision extends Division<LogicalDivision> {
    /**
     * Specifies the link if there is one.
     */
    private LinkedMetsResource link;

    /**
     * The views on {@link PhysicalDivision}s that this logical division
     * level comprises.
     */
    private final LinkedList<View> views;

    /**
     * Creates a new logical division.
     */
    public LogicalDivision() {
        views = new LinkedList<>();
    }

    /**
     * Creates a new subclass of logical division from an existing
     * logical division.
     *
     * @param source
     *            logical division that serves as data source
     */
    protected LogicalDivision(LogicalDivision source) {
        super(source);
        link = source.link;
        views = source.views;
    }

    /**
     * Returns the link of this logical division.
     *
     * @return the link
     */
    public LinkedMetsResource getLink() {
        return link;
    }

    /**
     * Sets the link of this logical division.
     *
     * @param link
     *            link to set
     */
    public void setLink(LinkedMetsResource link) {
        this.link = link;
    }

    /**
     * Returns the views associated with this logical division.
     *
     * @return the views
     */
    public LinkedList<View> getViews() {
        return views;
    }

    @Override
    public String toString() {
        return getType() + " \"" + getLabel() + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof LogicalDivision)) {
            return false;
        }
        LogicalDivision other = (LogicalDivision) o;
        return Objects.equals(link, other.link)
                && Objects.equals(views, other.views);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((link == null) ? 0 : link.hashCode());
        result = prime * result + ((views == null) ? 0 : views.hashCode());
        return result;
    }
}

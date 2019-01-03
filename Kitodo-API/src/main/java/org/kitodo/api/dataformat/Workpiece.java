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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.kitodo.api.Metadata;

/**
 * The administrative structure of the product of an element that passes through
 * a Production workflow.
 * 
 * <p>
 * A {@code Workpiece} has two essential characteristics: {@link MediaUnit}s and
 * an outline {@link Structure}. {@code MediaUnit}s are the types of every
 * single digital medium on a conceptual level, such as the individual pages of
 * a book. Each {@code MediaUnit} can be in different {@link MediaVariant}s (for
 * example, in different resolutions or file formats). Each {@code MediaVariant}
 * of a {@code MediaUnit} resides in a place described by an URI.
 * 
 * <p>
 * The {@code Structure} is a tree structure that can be finely subdivided, e.g.
 * a book, in which the chapters, in it individual elements such as tables or
 * figures. Each outline level points to the {@code MediaUnit}s that belong to
 * it via {@link View}s. Currently, a {@code View} always contains exactly one
 * {@code MediaUnit} unit, here a simple expandability is provided, so that in a
 * future version excerpts from {@code MediaUnit}s can be described. Each
 * outline level can be described with any {@link Metadata}.
 */
public class Workpiece {
    /**
     * The time this file was first created.
     */
    private GregorianCalendar creationDate = new GregorianCalendar();

    /**
     * The processing history.
     */
    private List<ProcessingNote> editHistory = new ArrayList<>();

    /**
     * The identifier of the workpiece.
     */
    private String id;

    /**
     * The media units that belong to this workpiece. The order of this
     * collection is meaningful, but only describes the order in which the media
     * units are displayed on the workstation of the compiler.
     */
    private List<MediaUnit> mediaUnits = new LinkedList<>();

    /**
     * The logical structure.
     */
    private Structure structure = new Structure();

    /**
     * Returns the creation date of the workpiece.
     * 
     * @return the creation date
     */
    public GregorianCalendar getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the edit history. The head of each METS file has space for
     * processing notes of the individual editors. In this way, the processing
     * process of the digital representation can be understood.
     * 
     * @return the edit history
     */
    public List<ProcessingNote> getEditHistory() {
        return editHistory;
    }

    /**
     * Returns the ID of the workpiece.
     * 
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the media units of this workpiece.
     * 
     * @return the media units
     */
    public List<MediaUnit> getMediaUnits() {
        return mediaUnits;
    }

    /**
     * Returns the root element of the structure.
     * 
     * @return root element of the structure
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Sets the creation date of the workpiece.
     * 
     * @param creationDate
     *            creation date to set
     */
    public void setCreationDate(GregorianCalendar creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the ID of the workpiece.
     * 
     * @param id
     *            ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the structure of the workpiece.
     * 
     * @param structure
     *            structure to set
     */
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Workpiece other = (Workpiece) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}

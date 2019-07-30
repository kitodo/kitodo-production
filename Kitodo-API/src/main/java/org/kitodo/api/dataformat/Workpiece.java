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
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The administrative structure of the product of an element that passes through
 * a Production workflow.
 */
public class Workpiece {

    // TODO: we probably need a way to configure MediaUnit types to be considered for renumbering/pagination!
    private static final String PAGE = "page";

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
     * The media unit that belongs to this workpiece. The media unit can have
     * children, such as a bound book that can have pages.
     */
    private MediaUnit mediaUnit = new MediaUnit();

    /**
     * The logical included structural element.
     */
    private List<IncludedStructuralElement> rootElements = new ArrayList<>(
            Arrays.asList(new IncludedStructuralElement()));

    /**
     * Returns the creation date of the workpiece.
     *
     * @return the creation date
     */
    public GregorianCalendar getCreationDate() {
        return creationDate;
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
     * Returns the edit history.
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
     * Sets the ID of the workpiece.
     *
     * @param id
     *            ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the media unit of this workpiece.
     *
     * @return the media units
     */
    public MediaUnit getMediaUnit() {
        return mediaUnit;
    }

    /**
     * Returns the media units of this workpiece.
     *
     * @return the media units
     * @deprecated Use {@code getMediaUnit().getChildren()}.
     */
    @Deprecated
    public List<MediaUnit> getMediaUnits() {
        return mediaUnit.getChildren();
    }

    /**
     * Returns the first root element of the workpiece.
     *
     * @return the first root element of the workpiece
     * @deprecated Use {@code getRootElements().get(0)}.
     */
    @Deprecated
    public IncludedStructuralElement getRootElement() {
        return rootElements.get(0);
    }

    /**
     * Returns the root elements of the workpiece.
     *
     * @return the root elements of the workpiece
     */
    public List<IncludedStructuralElement> getRootElements() {
        return rootElements;
    }

    /**
     * Sets the media unit of the workpiece.
     *
     * @param mediaUnit
     *            media unit to set
     */
    public void setMediaUnit(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    /**
     * Sets the first included structural element of the workpiece.
     *
     * @param rootElement
     *            included structural element to set
     * @deprecated Use {@code getRootElements().set(0, rootElement)}.
     */
    @Deprecated
    public void setRootElement(IncludedStructuralElement rootElement) {
        rootElements.set(0, rootElement);
    }

    @Override
    public String toString() {
        return id + ", " + rootElements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((id == null) ? 0 : id.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Workpiece) {
            Workpiece other = (Workpiece) obj;

            if (Objects.isNull(id)) {
                return Objects.isNull(other.id);
            } else {
                return id.equals(other.id);
            }
        }
        return false;
    }

    /**
     * Recursively search for all media units with type "page".
     *
     * @return list of all media units with type "page", sorted by their "ORDER" attribute.
     */
    public List<MediaUnit> getAllMediaUnits() {
        List<MediaUnit> mediaUnits = new LinkedList<>(mediaUnit.getChildren());
        for (MediaUnit mediaUnit : mediaUnit.getChildren()) {
            if (Objects.nonNull(mediaUnit)) {
                mediaUnits = getAllMediaUnitsRecursive(mediaUnit, mediaUnits);
            }
        }
        mediaUnits.sort(Comparator.comparing(MediaUnit::getOrder));
        return mediaUnits.stream().filter(m -> m.getType().equals(PAGE)).collect(Collectors.toList());
    }

    private List<MediaUnit> getAllMediaUnitsRecursive(MediaUnit parent, List<MediaUnit> mediaUnits) {
        List<MediaUnit> allMediaUnits = mediaUnits;
        for (MediaUnit mediaUnit : parent.getChildren()) {
            if (Objects.nonNull(mediaUnit)) {
                allMediaUnits.add(mediaUnit);
                if (!mediaUnit.getChildren().isEmpty()) {
                    allMediaUnits = getAllMediaUnitsRecursive(mediaUnit, mediaUnits);
                }
            }
        }
        return allMediaUnits;
    }
}

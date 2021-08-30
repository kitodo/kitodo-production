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
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The administrative structure of the product of an element that passes through
 * a Production workflow.
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
     * The media unit that belongs to this workpiece. The media unit can have
     * children, such as a bound book that can have pages.
     */
    private MediaUnit mediaUnit = new MediaUnit();

    /**
     * The logical logical division.
     */
    private LogicalDivision logicalStructure = new LogicalDivision();

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
     * Returns the root element of the logical division.
     *
     * @return root element of the logical division
     */
    public LogicalDivision getLogicalStructure() {
        return logicalStructure;
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
     * Sets the logical division of the workpiece.
     *
     * @param logicalStructure
     *            logical division to set
     */
    public void setLogicalStructure(LogicalDivision logicalStructure) {
        this.logicalStructure = logicalStructure;
    }

    @Override
    public String toString() {
        return id + ", " + logicalStructure;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((id == null) ? 0 : id.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Workpiece workpiece = (Workpiece) o;
        return Objects.equals(creationDate, workpiece.creationDate)
                && Objects.equals(editHistory, workpiece.editHistory)
                && Objects.equals(id, workpiece.id)
                && Objects.equals(mediaUnit, workpiece.mediaUnit)
                && Objects.equals(logicalStructure, workpiece.logicalStructure);
    }

    /**
     * Returns all logical divisions of the logical structure of the
     * workpiece as a flat list. The list isn’t backed by the included
     * structural elements, which means that insertions and deletions in the
     * list would not change the logical divisions. Therefore a list
     * that cannot be modified is returned.
     *
     * @return all logical divisions as an unmodifiable list
     */
    public List<LogicalDivision> getAllLogicalDivisions() {
        return Collections.unmodifiableList(treeStream(logicalStructure).collect(Collectors.toList()));
    }

    /**
     * Returns all child media units of the media unit of the workpiece with
     * type "page" sorted by their {@code order} as a flat list. The root media
     * unit is not contained. The list isn’t backed by the media units, which
     * means that insertions and deletions in the list would not change the
     * media units. Therefore a list that cannot be modified is returned.
     *
     * @return all media units with type "page", sorted by their {@code order}
     */
    public List<MediaUnit> getAllMediaUnitChildrenFilteredByTypePageAndSorted() {
        List<MediaUnit> mediaUnits = mediaUnit.getChildren().stream().flatMap(Workpiece::treeStream)
                .filter(mediaUnitToCheck -> Objects.equals(mediaUnitToCheck.getType(), MediaUnit.TYPE_PAGE))
                .sorted(Comparator.comparing(MediaUnit::getOrder)).collect(Collectors.toList());
        return Collections.unmodifiableList(mediaUnits);
    }

    /**
     * Returns all media units of the media unit of the workpiece as a flat
     * list. The list isn’t backed by the media units, which means that
     * insertions and deletions in the list would not change the media units.
     * Therefore a list that cannot be modified is returned.
     *
     * @return all media units as an unmodifiable list
     */
    public List<MediaUnit> getAllMediaUnits() {
        return Collections.unmodifiableList(treeStream(mediaUnit).collect(Collectors.toList()));
    }

    /**
     * Generates a stream of nodes from structure tree.
     *
     * @param tree
     *            starting node
     * @return all nodes as stream
     */
    @SuppressWarnings("unchecked")
    public static <T extends Division<T>> Stream<T> treeStream(Division<T> tree) {
        return Stream.concat(Stream.of((T) tree), tree.getChildren().stream().flatMap(Workpiece::treeStream));
    }
}

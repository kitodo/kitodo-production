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

package org.kitodo.production.forms.dataeditor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;

public class MediaPartialForm implements Serializable {
    
    private final DataEditorForm dataEditor;

    private Pair<PhysicalDivision, LogicalDivision> mediaSelection;
    private Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision;
    private String title;
    private Long begin;

    private Long duration;

    private Long extent;

    private String type;


    MediaPartialForm(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    public boolean isEdit() {
        return Objects.nonNull(mediaPartialDivision);
    }

    /**
     * Clean the media view division.
     */
    public void clean() {
        mediaPartialDivision = null;
        title = "";
        begin = null;
        extent = null;
    }

    /**
     * Save the media view.
     */
    public void save() {
        if (Objects.nonNull(mediaSelection)) {

            if (isEdit()) {
                mediaPartialDivision.getKey().setLabel(getTitle());
                mediaPartialDivision.getValue().setBegin(getBegin());
                mediaPartialDivision.getValue().setExtent(getExtent());
            } else {
                LogicalDivision logicalDivision = new LogicalDivision();
                logicalDivision.setType(getType());
                logicalDivision.setLabel(getTitle());
                PhysicalDivision physicalDivision = new PhysicalDivision();
                physicalDivision.getMediaFiles().putAll(mediaSelection.getKey().getMediaFiles());
                physicalDivision.setType(PhysicalDivision.TYPE_TRACK);

                MediaPartialView mediaPartialView = new MediaPartialView(getBegin(), getExtent());
                physicalDivision.setMediaPartialView(mediaPartialView);
                mediaPartialView.setPhysicalDivision(physicalDivision);
                logicalDivision.getViews().add(mediaPartialView);

                physicalDivision.getLogicalDivisions().add(logicalDivision);

                LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = MetadataEditor.getAncestorsOfPhysicalDivision(
                        mediaSelection.getKey(), dataEditor.getWorkpiece().getPhysicalStructure());

                ancestorsOfPhysicalDivision.getLast().getChildren().add(physicalDivision);

                mediaSelection.getValue().getChildren().add(logicalDivision);

            }

            // sorting reverse
            Collections.sort(mediaSelection.getValue().getChildren(), getLogicalDivisionComparator().reversed());

            ListIterator<LogicalDivision> iterator = mediaSelection.getValue().getChildren().listIterator();
            LogicalDivision previousLogicalDivision = null;
            while (iterator.hasNext()) {
                LogicalDivision logicalDivision = iterator.next();
                if (Objects.nonNull(previousLogicalDivision)) {
                    if (previousLogicalDivision.getViews().getFirst().getPhysicalDivision().hasMediaPartialView()) {
                        logicalDivision.getViews().getFirst().getPhysicalDivision().getMediaPartialView().setExtent(
                                previousLogicalDivision.getViews().getFirst().getPhysicalDivision()
                                        .getMediaPartialView().getBegin());
                    }
                } else {
                    MediaPartialView mediaPartialView = logicalDivision.getViews().getFirst().getPhysicalDivision()
                            .getMediaPartialView();
                    mediaPartialView.setExtent(
                            getFormattedTime(duration - getMilliseconds(mediaPartialView.getBegin())));
                }
                previousLogicalDivision = logicalDivision;
            }

            Collections.sort(mediaSelection.getValue().getChildren(), getLogicalDivisionComparator());




        }

        try {
            dataEditor.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }

    private static Long getMilliseconds(String formattedTime) {
        String[] timeParts = formattedTime.split(".");
        String[] time = timeParts[0].split(":");
        return Long.valueOf(
                Integer.valueOf(time[0]) * 60 * 60 + Integer.valueOf(time[1]) * 60 + Integer.valueOf(time[2]));
    }

    private static String getFormattedTime(Long milliseconds) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60, TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60);
    }

    private static Comparator<LogicalDivision> getLogicalDivisionComparator() {
        return (logicalDivision1, logicalDivision2) -> {
            View view1 = logicalDivision1.getViews().getFirst();
            View view2 = logicalDivision2.getViews().getFirst();
            if (Objects.nonNull(view1) && Objects.nonNull(view2)) {
                PhysicalDivision physicalDivision1 = view1.getPhysicalDivision();
                PhysicalDivision physicalDivision2 = view2.getPhysicalDivision();
                if (physicalDivision1.hasMediaPartialView() && physicalDivision2.hasMediaPartialView()) {
                    return physicalDivision1.getMediaPartialView().getBegin()
                            .compareTo(physicalDivision2.getMediaPartialView().getBegin());
                }
            }
            return Integer.compare(logicalDivision1.getOrder(), logicalDivision2.getOrder());
        };
    }

    public void setMediaSelection(Pair<PhysicalDivision, LogicalDivision> mediaSelection) {
        this.mediaSelection = mediaSelection;
    }

    public void setMediaPartialDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision) {
        this.mediaPartialDivision = mediaPartialDivision;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getBegin() {
        return begin;
    }

    public Long setBegin(Long begin) {
        this.begin = begin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getExtent() {
        return extent;
    }

    public Long setExtent(Long extent) {
        this.extent = extent;
    }

    public Long getDuration() {
        return duration;
    }

    public Long setDuration(Long duration) {
        this.duration = duration;
    }
}

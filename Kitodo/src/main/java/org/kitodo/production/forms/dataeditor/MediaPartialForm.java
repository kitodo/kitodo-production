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
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;

public class MediaPartialForm implements Serializable {
    
    private final DataEditorForm dataEditor;

    private Pair<PhysicalDivision, LogicalDivision> mediaSelection;
    private Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision;
    private String title;
    private String begin;

    private String extent;

    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

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
        begin = "";
        extent = "";
    }

    /**
     * Save the media view.
     */
    public void save() {
        if (isEdit()) {
            mediaPartialDivision.getKey().setLabel(getTitle());
            mediaPartialDivision.getValue().setBegin(getBegin());
            mediaPartialDivision.getValue().setExtent(getExtent());
        } else {
            if (Objects.nonNull(mediaSelection)) {
                LogicalDivision logicalDivision = new LogicalDivision();
                logicalDivision.setType(getType());
                logicalDivision.setLabel(getTitle());
                PhysicalDivision physicalDivision = new PhysicalDivision();
                physicalDivision.getMediaFiles().putAll(mediaSelection.getKey().getMediaFiles());
                physicalDivision.setType(PhysicalDivision.TYPE_TRACK);

                MediaPartialView mediaPartialView = new MediaPartialView(getBegin(),getExtent());
                physicalDivision.setMediaPartialView(mediaPartialView);
                mediaPartialView.setPhysicalDivision(physicalDivision);
                logicalDivision.getViews().add(mediaPartialView);

                physicalDivision.getLogicalDivisions().add(logicalDivision);

                LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = MetadataEditor.getAncestorsOfPhysicalDivision(
                        mediaSelection.getKey(), dataEditor.getWorkpiece().getPhysicalStructure());

                ancestorsOfPhysicalDivision.getLast().getChildren().add(physicalDivision);
                mediaSelection.getValue().getChildren().add(logicalDivision);
            }
        }

        try {
            dataEditor.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }
    }

    public void setMediaSelection(Pair<PhysicalDivision, LogicalDivision> mediaSelection) {
        this.mediaSelection = mediaSelection;
    }

    public void setMediaPartialDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision) {
        this.mediaPartialDivision = mediaPartialDivision;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }
}

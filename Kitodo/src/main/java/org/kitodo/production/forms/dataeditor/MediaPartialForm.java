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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.kitodo.production.forms.dataeditor.MediaPartialViewsPanel.generateExtentAndSortMediaPartials;
import static org.kitodo.production.forms.dataeditor.MediaPartialViewsPanel.getMillisecondsOfFormattedTime;

public class MediaPartialForm implements Serializable {

    private final DataEditorForm dataEditor;

    private Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision;
    private String title;
    private String begin;
    private String extent;
    private String type;
    private String validationError;

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
        validationError = "";
    }

    public boolean valid() {
        validationError = "";
        if (Objects.isNull(getMediaSelection())) {
            validationError = "Please select media";
            return false;
        }
        validationError = dataEditor.getGalleryPanel().getMediaPartialViewsPanel().validateDuration();
        if (Objects.nonNull(validationError)) {
            return false;
        }
        if (StringUtils.isEmpty(getBegin())) {
            validationError = "Begin is empty";
            return false;
        }
        if (!Pattern.compile(MediaPartialViewsPanel.FORMATTED_TIME_REGEX).matcher(getBegin()).matches()) {
            validationError = "Begin has wrong format";
            return false;
        }
        if (getMillisecondsOfFormattedTime(getBegin()) >= getMillisecondsOfFormattedTime(getDuration())) {
            validationError = "Begin musst be lower than duration";
            return false;
        }
        boolean exists = getMediaSelection().getValue().getChildren().stream().anyMatch(
                logicalDivision -> logicalDivision.getViews().getFirst().getPhysicalDivision().getMediaPartialView()
                        .getBegin().equals(getBegin()));
        if (exists) {
            validationError = "Begin already exists";
            return false;
        }
        return true;
    }

    public String getValidationError() {
        return validationError;
    }

    public boolean hasValidationError() {
        return StringUtils.isNotEmpty(validationError);
    }

    /**
     * Save the media view.
     */
    public void save() {
        if (!valid()) {
            return;
        }

        if (isEdit()) {
            mediaPartialDivision.getKey().setLabel(getTitle());
            mediaPartialDivision.getValue().setBegin(getBegin());
        } else {
            LogicalDivision logicalDivision = new LogicalDivision();
            logicalDivision.setType(getType());
            logicalDivision.setLabel(getTitle());
            PhysicalDivision physicalDivision = new PhysicalDivision();
            physicalDivision.getMediaFiles().putAll(getMediaSelection().getKey().getMediaFiles());
            physicalDivision.setType(PhysicalDivision.TYPE_TRACK);

            MediaPartialView mediaPartialView = new MediaPartialView(getBegin(), getExtent());
            physicalDivision.setMediaPartialView(mediaPartialView);
            mediaPartialView.setPhysicalDivision(physicalDivision);
            logicalDivision.getViews().add(mediaPartialView);

            physicalDivision.getLogicalDivisions().add(logicalDivision);

            LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = MetadataEditor.getAncestorsOfPhysicalDivision(
                    getMediaSelection().getKey(), dataEditor.getWorkpiece().getPhysicalStructure());

            ancestorsOfPhysicalDivision.getLast().getChildren().add(physicalDivision);

            getMediaSelection().getValue().getChildren().add(logicalDivision);

        }

        generateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                getMillisecondsOfFormattedTime(getDuration()));

        try {
            dataEditor.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }
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

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
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

    private String getDuration() {
        return dataEditor.getGalleryPanel().getMediaPartialViewsPanel().getDuration();
    }
    
    private Pair<PhysicalDivision, LogicalDivision> getMediaSelection() {
        return dataEditor.getGalleryPanel().getMediaPartialViewsPanel().getMediaSelection();
    }
}

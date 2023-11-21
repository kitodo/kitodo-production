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

import static org.kitodo.production.forms.dataeditor.MediaPartialsPanel.convertFormattedTimeToMilliseconds;
import static org.kitodo.production.forms.dataeditor.MediaPartialsPanel.generateExtentAndSortMediaPartials;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;

public class MediaPartialForm implements Serializable {

    private final DataEditorForm dataEditorForm;
    private Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision;
    private String title;
    private String begin;
    private String type;
    private String validationError;

    MediaPartialForm(DataEditorForm dataEditorForm) {
        this.dataEditorForm = dataEditorForm;
    }

    /**
     * Check if form is in edit mode.
     *
     * @return True if form is in edit mode.
     */
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
        validationError = "";
        Ajax.update("mediaPartialForm");
    }

    /**
     * Get the validation error message.
     *
     * @return The validation error message.
     */
    public String getValidationError() {
        return Helper.getTranslation(validationError);
    }

    /**
     * Check if form has a validation error.
     *
     * @return True if validation error is not empty.
     */
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

            MediaPartialView mediaPartialView = new MediaPartialView(getBegin());
            physicalDivision.setMediaPartialView(mediaPartialView);
            mediaPartialView.setPhysicalDivision(physicalDivision);
            logicalDivision.getViews().add(mediaPartialView);

            physicalDivision.getLogicalDivisions().add(logicalDivision);

            getAncestorsOfPhysicalDivision().getLast().getChildren().add(physicalDivision);

            getMediaSelection().getValue().getChildren().add(logicalDivision);

        }

        generateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                convertFormattedTimeToMilliseconds(getMediaDuration()));

        try {
            dataEditorForm.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }

        Ajax.update(MediaPartialsPanel.UPDATE_CLIENT_IDENTIFIERS);
        PrimeFaces.current().executeScript("PF('addMediaPartialDialog').hide();");
    }

    protected LinkedList<PhysicalDivision> getAncestorsOfPhysicalDivision() {
        return MetadataEditor.getAncestorsOfPhysicalDivision(getMediaSelection().getKey(),
                dataEditorForm.getWorkpiece().getPhysicalStructure());
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

    protected String getMediaDuration() {
        return dataEditorForm.getGalleryPanel().getMediaPartialsPanel().getMediaDuration();
    }

    protected Pair<PhysicalDivision, LogicalDivision> getMediaSelection() {
        return dataEditorForm.getGalleryPanel().getMediaPartialsPanel().getMediaSelection();
    }

    protected boolean valid() {
        validationError = "";
        if (Objects.isNull(getMediaSelection())) {
            validationError = "mediaPartialFormNoMedium";
            return false;
        }
        validationError = dataEditorForm.getGalleryPanel().getMediaPartialsPanel().validateMediaDuration();
        if (Objects.nonNull(validationError)) {
            return false;
        }
        if (StringUtils.isEmpty(getBegin())) {
            validationError = "mediaPartialFormStartEmpty";
            return false;
        }
        if (!Pattern.compile(MediaPartialsPanel.FORMATTED_TIME_REGEX).matcher(getBegin()).matches()) {
            validationError = "mediaPartialFormStartWrongTimeFormat";
            return false;
        }
        if (convertFormattedTimeToMilliseconds(getBegin()) >= convertFormattedTimeToMilliseconds(getMediaDuration())) {
            validationError = "mediaPartialFormStartLessThanMediaDuration";
            return false;
        }
        if (!isEdit() || (isEdit() && !mediaPartialDivision.getValue().getBegin().equals(getBegin()))) {
            boolean exists = getMediaSelection().getValue().getChildren().stream().anyMatch(
                    logicalDivision -> logicalDivision.getViews().getFirst().getPhysicalDivision().getMediaPartialView()
                            .getBegin().equals(getBegin()));
            if (exists) {
                validationError = "mediaPartialFormStartExists";
                return false;
            }
        }
        return true;
    }

}

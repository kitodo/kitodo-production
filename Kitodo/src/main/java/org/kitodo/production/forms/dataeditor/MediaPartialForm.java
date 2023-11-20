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

    private final DataEditorForm dataEditor;

    private Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision;
    private String title;
    private String begin;
    private String type;
    private String validationErrorMessage;

    MediaPartialForm(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
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
        validationErrorMessage = "";
        Ajax.update("mediaPartialForm");
    }

    /**
     * Get the validation error message.
     *
     * @return The validation error message.
     */
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    /**
     * Check if form has a validation error message.
     *
     * @return True if validation error is not empty.
     */
    public boolean hasValidationErrorMessage() {
        return StringUtils.isNotEmpty(validationErrorMessage);
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

            LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = MetadataEditor.getAncestorsOfPhysicalDivision(
                    getMediaSelection().getKey(), dataEditor.getWorkpiece().getPhysicalStructure());

            ancestorsOfPhysicalDivision.getLast().getChildren().add(physicalDivision);

            getMediaSelection().getValue().getChildren().add(logicalDivision);

        }

        generateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                convertFormattedTimeToMilliseconds(getMediaDuration()));

        try {
            dataEditor.refreshStructurePanel();
        } catch (UnknownTreeNodeDataException e) {
            Helper.setErrorMessage(e.getMessage());
        }

        Ajax.update("structureTreeForm", "imagePreviewForm:mediaDetailMediaPartialsContainer",
                    "imagePreviewForm:thumbnailStripe");
        PrimeFaces.current().executeScript("PF('addMediaPartialDialog').hide();");
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

    private String getMediaDuration() {
        return dataEditor.getGalleryPanel().getMediaPartialsPanel().getMediaDuration();
    }
    
    private Pair<PhysicalDivision, LogicalDivision> getMediaSelection() {
        return dataEditor.getGalleryPanel().getMediaPartialsPanel().getMediaSelection();
    }

    private boolean valid() {
        validationErrorMessage = "";
        if (Objects.isNull(getMediaSelection())) {
            validationErrorMessage = Helper.getTranslation("mediaPartialFormNoMedium");
            return false;
        }
        validationErrorMessage = dataEditor.getGalleryPanel().getMediaPartialsPanel().validateDuration();
        if (Objects.nonNull(validationErrorMessage)) {
            return false;
        }
        if (StringUtils.isEmpty(getBegin())) {
            validationErrorMessage = Helper.getTranslation("mediaPartialFormStartEmpty");
            return false;
        }
        if (!Pattern.compile(MediaPartialsPanel.FORMATTED_TIME_REGEX).matcher(getBegin()).matches()) {
            validationErrorMessage = Helper.getTranslation("mediaPartialFormStartWrongTimeFormat");
            return false;
        }
        if (convertFormattedTimeToMilliseconds(getBegin()) >= convertFormattedTimeToMilliseconds(getMediaDuration())) {
            validationErrorMessage = Helper.getTranslation("mediaPartialFormStartLessThanMediaDuration");
            return false;
        }
        if (!isEdit() || (isEdit() && !mediaPartialDivision.getValue().getBegin().equals(getBegin()))) {
            boolean exists = getMediaSelection().getValue().getChildren().stream().anyMatch(
                    logicalDivision -> logicalDivision.getViews().getFirst().getPhysicalDivision().getMediaPartialView()
                            .getBegin().equals(getBegin()));
            if (exists) {
                validationErrorMessage = Helper.getTranslation("mediaPartialFormStartExists");
                return false;
            }
        }
        return true;
    }

}

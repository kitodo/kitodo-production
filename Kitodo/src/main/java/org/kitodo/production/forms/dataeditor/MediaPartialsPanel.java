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

import static org.kitodo.production.helper.metadata.MediaPartialHelper.calculateExtentAndSortMediaPartials;
import static org.kitodo.production.helper.metadata.MediaPartialHelper.convertFormattedTimeToMilliseconds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartial;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.MediaPartialHelper;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.utils.MediaUtil;
import org.omnifaces.util.Ajax;

public class MediaPartialsPanel implements Serializable {

    public static final String REQUEST_PARAMETER_MEDIA_DURATION = "mediaDuration";
    public static final String[] UPDATE_CLIENT_IDENTIFIERS = {"structureTreeForm",
                                                              "imagePreviewForm:mediaDetailMediaPartialsContainer",
                                                              "imagePreviewForm:thumbnailStripe"};
    private final MediaPartialForm mediaPartialForm;
    private final DataEditorForm dataEditor;
    private String mediaDuration;
    private Pair<PhysicalDivision, LogicalDivision> mediaSelection;

    MediaPartialsPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
        mediaPartialForm = new MediaPartialForm(dataEditor);
    }

    /**
     * Get the media partial divisions.
     *
     * @return The media partial divisions
     */
    public Map<LogicalDivision, MediaPartial> getMediaPartialDivisions() {
        mediaSelection = dataEditor.getGalleryPanel().getLastSelection();
        Map<LogicalDivision, MediaPartial> mediaPartialDivisions = new LinkedHashMap<>();
        if (Objects.nonNull(mediaSelection)) {
            MediaPartialHelper.addMediaPartialDivisions(mediaPartialDivisions,
                    mediaSelection.getKey().getLogicalDivisions(),
                    mediaSelection.getLeft().getMediaFiles());
        }
        return mediaPartialDivisions;
    }

    /**
     * Validate the duration of the media.
     *
     * @return The error if media duration is not valid.
     */
    public String validateMediaDuration() {
        String error = null;
        if (StringUtils.isEmpty(getMediaDuration())) {
            error = "mediaPartialFormMediaDurationEmpty";
        } else if (!MediaPartialHelper.FORMATTED_TIME_PATTERN.matcher(getMediaDuration()).matches()) {
            error = "mediaPartialFormMediaDurationWrongTimeFormat";
        }
        return error;
    }

    /**
     * Delete media partial division from structure panel.
     *
     * @param mediaPartialDivision to delete
     */
    public void deleteMediaPartialDivision(Map.Entry<LogicalDivision, MediaPartial> mediaPartialDivision) {
        String error = validateMediaDuration();
        if (Objects.nonNull(error)) {
            Helper.setErrorMessage(Helper.getTranslation(error));
            return;
        }

        LogicalDivision logicalDivision = mediaPartialDivision.getKey();
        if (dataEditor.getStructurePanel()
                .deletePhysicalDivision(logicalDivision.getViews().getFirst().getPhysicalDivision())) {
            logicalDivision.getViews().remove();
            dataEditor.getStructurePanel().deleteLogicalDivision(logicalDivision);
            calculateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                    convertFormattedTimeToMilliseconds(getMediaDuration()));
        }

        Ajax.update(UPDATE_CLIENT_IDENTIFIERS);
    }

    /**
     * Edit media partial division form.
     *
     * @param mediaPartialDivision the media partial division
     */
    public void editMediaPartialDivision(Map.Entry<LogicalDivision, MediaPartial> mediaPartialDivision) {
        mediaPartialForm.clean();
        mediaPartialForm.setMediaPartialDivision(mediaPartialDivision);
        mediaPartialForm.setTitle(mediaPartialDivision.getKey().getLabel());
        mediaPartialForm.setBegin(mediaPartialDivision.getValue().getBegin());
    }

    /**
     * Check if media partials panel can be rendered.
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return !getMediaPartialChildDivisionsOfSelection().isEmpty();
    }

    /**
     * Retrieve the child divisions with use 'mediaPartial' from the current selection.
     *
     * @return The child divisions as selected items list
     */
    public List<SelectItem> getMediaPartialChildDivisionsOfSelection() {
        List<SelectItem> mediaPartialDivisions = new ArrayList<>();
        Pair<PhysicalDivision, LogicalDivision> lastSelection = dataEditor.getGalleryPanel().getLastSelection();
        if (Objects.nonNull(lastSelection) && MediaUtil.isAudioOrVideo(
                dataEditor.getGalleryPanel().getGalleryMediaContent(lastSelection.getKey()).getMediaViewMimeType())) {
            mediaPartialDivisions.addAll(DataEditorService.getSortedAllowedSubstructuralElements(
                    dataEditor.getRulesetManagement()
                    .getStructuralElementView(lastSelection.getRight().getType(), dataEditor.getAcquisitionStage(),
                            dataEditor.getPriorityList()), dataEditor.getProcess().getRuleset()));
            Collection<String> mediaPartialDivisionIds = dataEditor.getRulesetManagement()
                    .getFunctionalDivisions(FunctionalDivision.MEDIA_PARTIAL);
            mediaPartialDivisions = mediaPartialDivisions.stream()
                    .filter(selectItem -> selectItem.getValue() instanceof String)
                    .filter(selectItem -> mediaPartialDivisionIds.contains((String) selectItem.getValue()))
                    .collect(Collectors.toList());
        }
        return mediaPartialDivisions;
    }


    /**
     * Get the MediaPartialForm.
     *
     * @return The MediaPartialForm
     */
    public MediaPartialForm getMediaPartialForm() {
        return mediaPartialForm;
    }

    /**
     * Get the media selection.
     *
     * @return The media selection
     */
    public Pair<PhysicalDivision, LogicalDivision> getMediaSelection() {
        return mediaSelection;
    }

    /**
     * Set members of panel by request parameter.
     */
    public void setMembersByRequestParameter() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (params.containsKey(REQUEST_PARAMETER_MEDIA_DURATION)) {
            mediaDuration = params.get(REQUEST_PARAMETER_MEDIA_DURATION);
        }
    }

    /**
     * Get the media duration.
     *
     * @return The media duration
     */
    public String getMediaDuration() {
        return mediaDuration;
    }
}

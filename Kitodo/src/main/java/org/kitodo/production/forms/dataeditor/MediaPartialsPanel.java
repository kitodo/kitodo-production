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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.utils.MediaUtil;
import org.omnifaces.util.Ajax;

public class MediaPartialsPanel implements Serializable {

    public static final String FORMATTED_TIME_REGEX = "([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d";
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
     * Get the media view divisions.
     *
     * @return The media view divisions
     */
    public Map<LogicalDivision, MediaPartialView> getMediaPartialViewDivisions() {
        mediaSelection = dataEditor.getGalleryPanel().getLastSelection();
        Map<LogicalDivision, MediaPartialView> mediaPartialViewDivisions = new LinkedHashMap<>();
        if (Objects.nonNull(mediaSelection)) {
            getMediaPartialViewDivisions(mediaPartialViewDivisions, mediaSelection.getKey().getLogicalDivisions(),
                    mediaSelection.getLeft().getMediaFiles());
        }
        return mediaPartialViewDivisions;
    }

    private static void getMediaPartialViewDivisions(Map<LogicalDivision, MediaPartialView> mediaViewDivisions,
            List<LogicalDivision> logicalDivisions, Map<MediaVariant, URI> mediaFiles) {
        for (LogicalDivision logicalDivision : logicalDivisions) {
            for (View view : logicalDivision.getViews()) {
                if (PhysicalDivision.TYPE_TRACK.equals(
                        view.getPhysicalDivision().getType()) && view.getPhysicalDivision()
                        .hasMediaPartialView() && view.getPhysicalDivision().getMediaFiles().equals(mediaFiles)) {
                    mediaViewDivisions.put(logicalDivision, view.getPhysicalDivision().getMediaPartialView());
                }
            }
            getMediaPartialViewDivisions(mediaViewDivisions, logicalDivision.getChildren(), mediaFiles);
        }
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
        } else if (!Pattern.compile(MediaPartialsPanel.FORMATTED_TIME_REGEX).matcher(getMediaDuration()).matches()) {
            error = "mediaPartialFormMediaDurationWrongTimeFormat";
        }
        return error;
    }

    /**
     * Delete media partial division from structure panel.
     *
     * @param mediaPartialDivision to delete
     */
    public void deleteMediaPartialDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaPartialDivision) {
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
            generateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                    convertFormattedTimeToMilliseconds(getMediaDuration()));
        }

        Ajax.update(UPDATE_CLIENT_IDENTIFIERS);
    }

    /**
     * Edit media view division form.
     *
     * @param mediaViewDivision the media view division
     */
    public void editMediaViewDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaViewDivision) {
        mediaPartialForm.clean();
        mediaPartialForm.setMediaPartialDivision(mediaViewDivision);
        mediaPartialForm.setTitle(mediaViewDivision.getKey().getLabel());
        mediaPartialForm.setBegin(mediaViewDivision.getValue().getBegin());
    }

    /**
     * Check if media partials panel can be rendered.
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return !getMediaPartialDivisions().isEmpty();;
    }

    /**
     * Get the child divisions of use "mediaPartial" of the current selection.
     *
     * @return The divisions as selected items list
     */
    public List<SelectItem> getMediaPartialDivisions() {
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
                    .filter(selectItem -> mediaPartialDivisionIds.contains(selectItem.getValue()))
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
     * Generate the extent field of every media partial and sort media partials by begin.
     *
     * @param logicalDivisions
     *         The logical divisions of media partials.
     * @param mediaDuration
     *         The media duration.
     */
    public static void generateExtentAndSortMediaPartials(List<LogicalDivision> logicalDivisions, Long mediaDuration) {
        // sorting reverse to set extent starting from the last entry
        logicalDivisions.sort(getLogicalDivisionComparator().reversed());

        generateExtentForMediaPartials(logicalDivisions, mediaDuration);

        logicalDivisions.sort(getLogicalDivisionComparator());
    }

    private static void generateExtentForMediaPartials(List<LogicalDivision> logicalDivisions, Long mediaDuration) {
        ListIterator<LogicalDivision> iterator = logicalDivisions.listIterator();
        LogicalDivision previousLogicalDivision = null;
        while (iterator.hasNext()) {
            LogicalDivision logicalDivision = iterator.next();
            MediaPartialView mediaPartialView = logicalDivision.getViews().getFirst().getPhysicalDivision()
                    .getMediaPartialView();
            if (Objects.nonNull(previousLogicalDivision)) {
                PhysicalDivision previousPhysicalDivision = previousLogicalDivision.getViews().getFirst()
                        .getPhysicalDivision();
                if (previousPhysicalDivision.hasMediaPartialView()) {
                    mediaPartialView.setExtent(convertMillisecondsToFormattedTime(convertFormattedTimeToMilliseconds(
                            previousPhysicalDivision.getMediaPartialView()
                                    .getBegin()) - convertFormattedTimeToMilliseconds(
                            mediaPartialView.getBegin())));
                }
            } else {
                mediaPartialView.setExtent(convertMillisecondsToFormattedTime(
                        mediaDuration - convertFormattedTimeToMilliseconds(mediaPartialView.getBegin())));
            }
            previousLogicalDivision = logicalDivision;
        }
    }

    /**
     * Convert formatted time to milliseconds.
     *
     * @param formattedTime
     *         The formatted time in form of {@value #FORMATTED_TIME_REGEX}
     * @return The milliseconds
     */
    public static Long convertFormattedTimeToMilliseconds(String formattedTime) {
        if (formattedTime.contains(".")) {
            formattedTime = formattedTime.split(".")[0];
        }
        String[] time = formattedTime.split(":");
        return Long.valueOf(
                Integer.valueOf(time[0]) * 3600 + Integer.valueOf(time[1]) * 60 + Integer.valueOf(time[2])) * 1000;
    }

    public static String convertMillisecondsToFormattedTime(Long milliseconds) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60, TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60);
    }

    private static Comparator<LogicalDivision> getLogicalDivisionComparator() {
        return (logicalDivisionA, logicalDivisionB) -> {
            View viewA = logicalDivisionA.getViews().getFirst();
            View viewB = logicalDivisionB.getViews().getFirst();
            if (Objects.nonNull(viewA) && Objects.nonNull(viewB)) {
                PhysicalDivision physicalDivisionA = viewA.getPhysicalDivision();
                PhysicalDivision physicalDivisionB = viewB.getPhysicalDivision();
                if (physicalDivisionA.hasMediaPartialView() && physicalDivisionB.hasMediaPartialView()) {
                    return physicalDivisionA.getMediaPartialView().getBegin()
                            .compareTo(physicalDivisionB.getMediaPartialView().getBegin());
                }
            }
            return Integer.compare(logicalDivisionA.getOrder(), logicalDivisionB.getOrder());
        };
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

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



public class MediaPartialViewsPanel implements Serializable {

    public static final String FORMATTED_TIME_REGEX = "(([0-1][0-9])|([2][0-3])):([0-5][0-9]):([0-5][0-9])";
    public static final String REQUEST_PARAMETER_DURATION = "duration";
    private MediaPartialForm mediaPartialForm;
    private DataEditorForm dataEditor;
    private String duration;
    private Pair<PhysicalDivision, LogicalDivision> mediaSelection;

    MediaPartialViewsPanel(DataEditorForm dataEditor) {
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

    public String validateDuration() {
        String errorMessage = null;
        if (StringUtils.isEmpty(getDuration())) {
            errorMessage = "Duration is empty";
        } else if (!Pattern.compile(MediaPartialViewsPanel.FORMATTED_TIME_REGEX).matcher(getDuration()).matches()) {
            errorMessage = "Duration has wrong format";
        }
        return errorMessage;
    }

    /**
     * Delete media view division from structure panel.
     *
     * @param mediaViewDivision to delete
     */
    public void deleteMediaViewDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaViewDivision) {
        String errorMessage = validateDuration();
        if (Objects.nonNull(errorMessage)) {
            Helper.setErrorMessage(errorMessage);
            return;
        }

        LogicalDivision logicalDivision = mediaViewDivision.getKey();
        if (dataEditor.getStructurePanel()
                .deletePhysicalDivision(logicalDivision.getViews().getFirst().getPhysicalDivision())) {
            logicalDivision.getViews().remove();
            dataEditor.getStructurePanel().deleteLogicalDivision(logicalDivision);
            generateExtentAndSortMediaPartials(getMediaSelection().getValue().getChildren(),
                    getMillisecondsOfFormattedTime(getDuration()));
        }
    }

    /**
     * Edit media view division form.
     *
     * @param mediaViewDivision the media view division
     */
    public void editMediaViewDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaViewDivision) {
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
        return getMediaPartialDivisions().size() > 0;
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

    public Pair<PhysicalDivision, LogicalDivision> getMediaSelection() {
        return mediaSelection;
    }

    /**
     * Set members of panel by request parameter.
     */
    public void setMembersByRequestParameter() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (params.containsKey(REQUEST_PARAMETER_DURATION)) {
            duration = params.get(REQUEST_PARAMETER_DURATION);
        }
    }

    public static void generateExtentAndSortMediaPartials(List<LogicalDivision> logicalDivisions, Long duration) {
        // sorting reverse to set extent starting from the last entry
        Collections.sort(logicalDivisions, getLogicalDivisionComparator().reversed());

        generateExtentForMediaPartials(logicalDivisions, duration);

        Collections.sort(logicalDivisions, getLogicalDivisionComparator());
    }

    private static void generateExtentForMediaPartials(List<LogicalDivision> logicalDivisions, Long duration) {
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
                    mediaPartialView.setExtent(getFormattedTimeOfMilliseconds(getMillisecondsOfFormattedTime(
                            previousPhysicalDivision.getMediaPartialView().getBegin()) - getMillisecondsOfFormattedTime(
                            mediaPartialView.getBegin())));
                }
            } else {
                mediaPartialView.setExtent(getFormattedTimeOfMilliseconds(
                        duration - getMillisecondsOfFormattedTime(mediaPartialView.getBegin())));
            }
            previousLogicalDivision = logicalDivision;
        }
    }

    public static Long getMillisecondsOfFormattedTime(String formattedTime) {
        if (formattedTime.contains(".")) {
            formattedTime = formattedTime.split(".")[0];
        }
        String[] time = formattedTime.split(":");
        return Long.valueOf(
                Integer.valueOf(time[0]) * 3600 + Integer.valueOf(time[1]) * 60 + Integer.valueOf(time[2])) * 1000;
    }

    private static String getFormattedTimeOfMilliseconds(Long milliseconds) {
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

    public String getDuration() {
        return duration;
    }
}

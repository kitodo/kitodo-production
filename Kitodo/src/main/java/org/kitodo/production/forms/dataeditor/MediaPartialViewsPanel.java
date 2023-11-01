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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.utils.MediaUtil;



public class MediaPartialViewsPanel implements Serializable {

    private MediaPartialViewForm mediaPartialViewForm;
    private DataEditorForm dataEditor;

    MediaPartialViewsPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
        mediaPartialViewForm = new MediaPartialViewForm(dataEditor);
    }

    /**
     * Get the media view divisions.
     *
     * @return The media view divisions
     */
    public Map<LogicalDivision, MediaPartialView> getMediaPartialViewDivisions() {
        Pair<PhysicalDivision, LogicalDivision> lastSelection = dataEditor.getGalleryPanel().getLastSelection();
        Map<LogicalDivision, MediaPartialView> mediaPartialViewDivisions = new LinkedHashMap<>();
        if (Objects.nonNull(lastSelection)) {
            mediaPartialViewForm.setMediaSelection(lastSelection);
            getMediaPartialViewDivisions(mediaPartialViewDivisions, lastSelection.getKey().getLogicalDivisions(),
                    lastSelection.getLeft().getMediaFiles());

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
     * Delete media view division from structure panel.
     *
     * @param mediaViewDivision to delete
     */
    public void deleteMediaViewDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaViewDivision) {
        LogicalDivision logicalDivision = mediaViewDivision.getKey();
        if (dataEditor.getStructurePanel()
                .deletePhysicalDivision(logicalDivision.getViews().getFirst().getPhysicalDivision())) {
            logicalDivision.getViews().remove();
            dataEditor.getStructurePanel().deleteLogicalDivision(logicalDivision);
        }
    }

    /**
     * Edit media view division form.
     *
     * @param mediaViewDivision the media view division
     */
    public void editMediaViewDivision(Map.Entry<LogicalDivision, MediaPartialView> mediaViewDivision) {
        mediaPartialViewForm.setMediaViewDivision(mediaViewDivision);
        mediaPartialViewForm.setTitle(mediaViewDivision.getKey().getLabel());
        mediaPartialViewForm.setBegin(mediaViewDivision.getValue().getBegin());
    }

    /**
     * Check if media partials panel can be rendered.
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return getMediaPartialDivisions().size() > 0;
    }

    public List<SelectItem> getMediaPartialDivisions() {
        List<SelectItem> mediaPartialDivisions = new ArrayList<>();
        Pair<PhysicalDivision, LogicalDivision> lastSelection = dataEditor.getGalleryPanel().getLastSelection();
        if (Objects.nonNull(lastSelection) && MediaUtil.isAudioOrVideo(
                dataEditor.getGalleryPanel().getGalleryMediaContent(lastSelection.getKey()).getMediaViewMimeType())) {
            mediaPartialDivisions.addAll(DataEditorService.getAllowedSubstructuralElementsAsSortedListOfSelectItems(
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
     * Get the MediaPartialViewForm.
     *
     * @return The MediaPartialViewForm
     */
    public MediaPartialViewForm getMediaPartialViewForm() {
        return mediaPartialViewForm;
    }
}

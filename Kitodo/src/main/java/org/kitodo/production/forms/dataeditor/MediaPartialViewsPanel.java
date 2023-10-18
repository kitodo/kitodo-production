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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

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
        mediaPartialViewForm.setMediaSelection(lastSelection);
        Map<LogicalDivision, MediaPartialView> mediaViewDivisions = new LinkedHashMap<>();
        List<LogicalDivision> logicalDivisions = lastSelection.getKey().getLogicalDivisions();
        for (LogicalDivision logicalDivision : logicalDivisions) {
            getMediaPartialViewDivisions(mediaViewDivisions, logicalDivision.getChildren());
        }
        return mediaViewDivisions;
    }

    private static void getMediaPartialViewDivisions(Map<LogicalDivision, MediaPartialView> mediaViewDivisions,
            List<LogicalDivision> logicalDivisions) {
        for (LogicalDivision logicalDivision : logicalDivisions) {
            for (View view : logicalDivision.getViews()) {
                if (PhysicalDivision.TYPE_TRACK.equals(
                        view.getPhysicalDivision().getType()) && view instanceof MediaPartialView) {
                    mediaViewDivisions.put(logicalDivision, (MediaPartialView) view);
                }
            }
            getMediaPartialViewDivisions(mediaViewDivisions, logicalDivision.getChildren());
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

    public boolean isEnabled() {
        return getMediaPartialDivisions().size() > 0;
    }

    public Collection<String> getMediaPartialDivisions() {
        return dataEditor.getRulesetManagement().getFunctionalDivisions(FunctionalDivision.MEDIA_PARTIAL);
    }

    public MediaPartialViewForm getMediaPartialViewForm() {
        return mediaPartialViewForm;
    }
}

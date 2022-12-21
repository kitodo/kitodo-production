package org.kitodo.production.forms.dataeditor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

public class MediaViewsPanel implements Serializable {

    private final MediaViewForm mediaViewForm;
    private DataEditorForm dataEditor;

    MediaViewsPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
        this.mediaViewForm = new MediaViewForm(dataEditor);
    }

    public Map<LogicalDivision, MediaView> getMediaViewDivisions() {
        Pair<PhysicalDivision, LogicalDivision> lastSelection = dataEditor.getGalleryPanel().getLastSelection();
        mediaViewForm.setMediaSelection(lastSelection);
        Map<LogicalDivision, MediaView> mediaViewDivisions = new LinkedHashMap<>();
        List<LogicalDivision> logicalDivisions = lastSelection.getKey().getLogicalDivisions();
        for (LogicalDivision logicalDivision : logicalDivisions) {
            getMediaViewDivisions(mediaViewDivisions, logicalDivision.getChildren());
        }
        return mediaViewDivisions;
    }

    public void deleteMediaViewDivision(Map.Entry<LogicalDivision, MediaView> mediaViewDivision) {
        LogicalDivision logicalDivision = mediaViewDivision.getKey();
        if (dataEditor.getStructurePanel()
                .deletePhysicalDivision(logicalDivision.getViews().getFirst().getPhysicalDivision())) {
            logicalDivision.getViews().remove();
            dataEditor.getStructurePanel().deleteLogicalDivision(logicalDivision);
        }
    }

    public void editMediaViewDivision(Map.Entry<LogicalDivision, MediaView> mediaViewDivision) {
        mediaViewForm.setMediaViewDivision(mediaViewDivision);
        mediaViewForm.setTitle(mediaViewDivision.getKey().getLabel());
        mediaViewForm.setBegin(mediaViewDivision.getValue().getBegin());
    }

    private static void getMediaViewDivisions(Map<LogicalDivision, MediaView> mediaViewDivisions,
            List<LogicalDivision> logicalDivisions) {
        for (LogicalDivision logicalDivision : logicalDivisions) {
            for (View view : logicalDivision.getViews()) {
                if (PhysicalDivision.TYPE_TRACK.equals(
                        view.getPhysicalDivision().getType()) && view instanceof MediaView) {
                    mediaViewDivisions.put(logicalDivision, (MediaView) view);
                }
            }
            getMediaViewDivisions(mediaViewDivisions, logicalDivision.getChildren());
        }
    }

    public MediaViewForm getMediaViewForm() {
        return mediaViewForm;
    }
}

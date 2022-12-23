package org.kitodo.production.forms.dataeditor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;

public class MediaViewForm implements Serializable {
    
    private final DataEditorForm dataEditor;

    private Pair<PhysicalDivision, LogicalDivision> mediaSelection;
    private Map.Entry<LogicalDivision, MediaView> mediaViewDivision;
    private String title;
    private String begin;

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

    MediaViewForm(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    public boolean isEdit() {
        return Objects.nonNull(mediaViewDivision);
    }

    public void clean() {
        mediaViewDivision = null;
        title = "";
        begin = "";
    }

    public void save() {
        if (isEdit()) {
            mediaViewDivision.getKey().setLabel(getTitle());
            mediaViewDivision.getValue().setBegin(getBegin());
        } else {
            if (Objects.nonNull(mediaSelection)) {
                MediaView mediaView = new MediaView(getBegin());
                LogicalDivision logicalDivision = new LogicalDivision();
                logicalDivision.setType(getType());
                logicalDivision.setLabel(getTitle());
                PhysicalDivision physicalDivision = new PhysicalDivision();
                physicalDivision.getMediaFiles().putAll(mediaSelection.getKey().getMediaFiles());
                physicalDivision.setType(PhysicalDivision.TYPE_TRACK);
                physicalDivision.addMediaView(mediaView);
                mediaView.setPhysicalDivision(physicalDivision);
                logicalDivision.getViews().add(mediaView);

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

    public void setMediaViewDivision(Map.Entry<LogicalDivision, MediaView> mediaViewDivision) {
        this.mediaViewDivision = mediaViewDivision;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

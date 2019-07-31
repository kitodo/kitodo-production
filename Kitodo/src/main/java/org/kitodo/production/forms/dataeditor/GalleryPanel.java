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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.model.Subfolder;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Backing bean for the gallery panel of the metadata editor.
 */
public class GalleryPanel {
    private static final Logger logger = LogManager.getLogger(GalleryPanel.class);

    private static final Pattern DRAG_STRIPE_IMAGE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList:(\\d+):structuredPagePanel");

    private static final Pattern DROP_STRIPE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList");

    private static final Pattern UNSTRUCTURED_MEDIA = Pattern
            .compile("imagePreviewForm:unstructuredMediaList:(\\d+):unstructuredMediaPanel");

    private static final Pattern UNSTRUCTURED_MEDIA_AREA = Pattern
            .compile("imagePreviewForm:unstructuredMediaList");

    private final DataEditorForm dataEditor;
    private GalleryViewMode galleryViewMode = GalleryViewMode.LIST;
    private List<GalleryMediaContent> medias = Collections.emptyList();

    private MediaVariant mediaViewVariant;
    private Map<String, GalleryMediaContent> previewImageResolver = new HashMap<>();
    private MediaVariant previewVariant;
    private List<GalleryMediaContent> selectedMedia = new LinkedList<>();
    private GalleryStripe selectedStripe;
    private String selectionType = "";
    private GalleryMediaContent lastSelection;

    private List<GalleryStripe> stripes;

    private Subfolder previewFolder;

    GalleryPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    void clear() {
        selectedMedia = new LinkedList<>();
        lastSelection = null;
    }

    String getAcquisitionStage() {
        return dataEditor.getAcquisitionStage();
    }

    /**
     * Get galleryViewMode.
     *
     * @return value of galleryViewMode
     */
    public String getGalleryViewMode() {
        return galleryViewMode.toString().toLowerCase();
    }

    /**
     * Get lastSelection.
     *
     * @return value of lastSelection
     */
    public GalleryMediaContent getLastSelection() {
        return lastSelection;
    }

    /**
     * Get the list of image file paths for the current process.
     *
     * @return List of fullsize PNG images
     */
    public List<GalleryMediaContent> getMedias() {
        return medias;
    }

    String getMediaViewMimeType() {
        return mediaViewVariant.getMimeType();
    }

    /**
     * Returns the media content of the preview media. This is the method that
     * is called when the web browser wants to retrieve the media file itself.
     *
     * @return a Primefaces object that handles the output of media data
     */
    public StreamedContent getPreviewData() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE) {
            String id = context.getExternalContext().getRequestParameterMap().get("id");
            GalleryMediaContent mediaContent = previewImageResolver.get(id);
            if (Objects.nonNull(mediaContent)) {
                logger.trace("Serving image request {}", id);
                return mediaContent.getPreviewData();
            }
            logger.debug("Cannot serve image request, id = {}", id);
        }
        return new DefaultStreamedContent();
    }

    String getPreviewMimeType() {
        return previewVariant.getMimeType();
    }

    List<LanguageRange> getPriorityList() {
        return dataEditor.getPriorityList();
    }

    RulesetManagementInterface getRuleset() {
        return dataEditor.getRuleset();
    }

    /**
     * Get selectedImage.
     *
     * @return value of selectedImage
     */
    public List<GalleryMediaContent> getSelectedMedia() {
        return selectedMedia;
    }

    public GalleryStripe getSelectedStripe() {
        return selectedStripe;
    }

    /**
     * Set selectionType sent as request parameter.
     */
    public void setSelectionType() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectionType = params.get("selectionType");
    }

    /**
     * Get list of all logical structure elements for this process.
     *
     * @return List of logical elements
     */
    public List<GalleryStripe> getStripes() {
        return stripes;
    }

    /**
     * Handle event of page being dragged and dropped.
     *
     * @param event
     *            JSF drag'n'drop event description object
     */
    public void onPageDrop(DragDropEvent event) {
        Matcher dragStripeImageMatcher = DRAG_STRIPE_IMAGE.matcher(event.getDragId());
        Matcher dragUnstructuredMediaMatcher = UNSTRUCTURED_MEDIA.matcher(event.getDragId());
        Matcher dropStripeMatcher = DROP_STRIPE.matcher(event.getDropId());
        Matcher dropUnstructuredMediaMatcher = UNSTRUCTURED_MEDIA_AREA.matcher(event.getDropId());
        if ((dragUnstructuredMediaMatcher.matches() || dragStripeImageMatcher.matches())
                && (dropStripeMatcher.matches() || dropUnstructuredMediaMatcher.matches())) {
            int fromStripeIndex;
            int toStripeIndex;
            GalleryStripe fromStripe;
            if (dropStripeMatcher.matches()) {
                toStripeIndex = Integer.parseInt(dropStripeMatcher.group(1));
            } else if (dropUnstructuredMediaMatcher.matches()) {
                // First (0) stripe represents logical root element (unstructured media)
                toStripeIndex = 0;
            } else {
                return;
            }
            if (dragStripeImageMatcher.matches()) {
                fromStripeIndex = Integer.parseInt(dragStripeImageMatcher.group(1));
                if (fromStripeIndex == toStripeIndex) {
                    return;
                }
                fromStripe = stripes.get(fromStripeIndex);
            } else if (dragUnstructuredMediaMatcher.matches()) {
                // First (0) stripe represents logical root element (e.g. "Unstructured Media")
                fromStripe = stripes.get(0);
            } else {
                return;
            }

            GalleryStripe toStripe = stripes.get(toStripeIndex);

            // move views
            List<View> viewsToBeMoved = new ArrayList<>();
            for (GalleryMediaContent mediaContent : selectedMedia) {
                viewsToBeMoved.add(mediaContent.getView());
            }
            // TODO: rework GalleryPanel to allow dropping page thumbnails between other thumbnails!
            dataEditor.getStructurePanel().moveViews(fromStripe.getStructure(), toStripe.getStructure(), viewsToBeMoved);

            // update stripes
            fromStripe.getMedias().clear();
            for (View fromStripeView : fromStripe.getStructure().getViews()) {
                fromStripe.getMedias().add(createGalleryMediaContent(fromStripeView));
            }
            toStripe.getMedias().clear();
            for (View toStripeView : toStripe.getStructure().getViews()) {
                toStripe.getMedias().add(createGalleryMediaContent(toStripeView));
            }
            dataEditor.getStructurePanel().show();
            return;
        }
        logger.debug("Unsupported drag'n'drop event from {} to {}", event.getDragId(), event.getDropId());
    }

    /**
     * Set galleryViewMode.
     *
     * @param galleryViewMode
     *            as java.lang.String
     */
    public void setGalleryViewMode(String galleryViewMode) {
        this.galleryViewMode = GalleryViewMode.valueOf(galleryViewMode.toUpperCase());
    }

    /**
     * Set selectedMedia.
     *
     * @param selectedMedia
     *            as org.kitodo.production.forms.dataeditor.GalleryMediaContent
     */
    public void setSelectedMedia(List<GalleryMediaContent> selectedMedia) {
        this.selectedMedia = selectedMedia;
    }

    /**
     * Update the selected TreeNode in the physical structure tree.
     */
    public void updateStructure(GalleryMediaContent galleryMediaContent) {
        dataEditor.getStructurePanel().updateNodeSelection(galleryMediaContent);
    }

    /**
     * Sets the strip on which the user has just called the update function.
     *
     * @param selectedStripe
     *            selected stripe to set
     */
    public void setSelectedStripe(GalleryStripe selectedStripe) {
        this.selectedStripe = selectedStripe;
    }

    void updateSelection(MediaUnit mediaUnit) {
        if (mediaUnit.getMediaFiles().size() > 0) {

            // Update structured view
            if (this.galleryViewMode.equals(GalleryViewMode.LIST)) {
                for (GalleryStripe galleryStripe : getStripes()) {
                    for (GalleryMediaContent galleryMediaContent : galleryStripe.getMedias()) {
                        if (Objects.equals(mediaUnit, galleryMediaContent.getView().getMediaUnit())) {
                            selectedMedia = new LinkedList<>(Arrays.asList(galleryMediaContent));
                            lastSelection = galleryMediaContent;
                            setSelectedStripeForMediaUnit(mediaUnit);
                            break;
                        }
                    }
                }
            }
            // Update unstructured view
            else {
                for (GalleryMediaContent galleryMediaContent : getMedias()) {
                    if (Objects.equals(mediaUnit, galleryMediaContent.getView().getMediaUnit())) {
                        selectedMedia = new LinkedList<>(Arrays.asList(galleryMediaContent));
                        lastSelection = galleryMediaContent;
                        break;
                    }
                }
            }
        }
    }

    void show() {
        Process process = dataEditor.getProcess();
        Project project = process.getProject();
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getAllMediaUnits();

        Folder previewSettings = project.getPreview();
        previewVariant = Objects.nonNull(previewSettings) ? getMediaVariant(previewSettings, mediaUnits) : null;
        Folder mediaViewSettings = project.getMediaView();
        mediaViewVariant = Objects.nonNull(mediaViewSettings) ? getMediaVariant(mediaViewSettings, mediaUnits) : null;

        medias = new ArrayList<>(mediaUnits.size());
        stripes = new ArrayList<>();
        previewImageResolver = new HashMap<>();

        previewFolder = new Subfolder(process, project.getPreview());
        for (MediaUnit mediaUnit : mediaUnits) {
            View wholeMediaUnitView = new View();
            wholeMediaUnitView.setMediaUnit(mediaUnit);
            GalleryMediaContent mediaContent = createGalleryMediaContent(wholeMediaUnitView);
            medias.add(mediaContent);
            if (mediaContent.isShowingInPreview()) {
                previewImageResolver.put(mediaContent.getId(), mediaContent);
            }
        }

        addStripesRecursive(dataEditor.getWorkpiece().getRootElement());
        int imagesInStructuredView = stripes.parallelStream().mapToInt(stripe -> stripe.getMedias().size()).sum();
        if (imagesInStructuredView > 200) {
            logger.warn("Number of images in structured view: {}", imagesInStructuredView);
        }
    }

    void updateStripes() {
        stripes = new ArrayList<>();
        addStripesRecursive(dataEditor.getWorkpiece().getRootElement());
    }

    private static MediaVariant getMediaVariant(Folder folderSettings, List<MediaUnit> mediaUnits) {
        String use = folderSettings.getFileGroup();
        Optional<MediaVariant> maybeMediaVariant = mediaUnits.parallelStream().map(MediaUnit::getMediaFiles)
                .flatMap(mediaFiles -> mediaFiles.entrySet().parallelStream()).map(Entry::getKey)
                .filter(mediaVariant -> use.equals(mediaVariant.getUse())).findAny();
        if (maybeMediaVariant.isPresent()) {
            return maybeMediaVariant.get();
        } else {
            MediaVariant mediaVariant = new MediaVariant();
            mediaVariant.setMimeType(folderSettings.getMimeType());
            mediaVariant.setUse(use);
            return mediaVariant;
        }
    }

    private void addStripesRecursive(IncludedStructuralElement structure) {
        GalleryStripe galleryStripe = new GalleryStripe(this, structure);
        for (View view : structure.getViews()) {
            for (GalleryMediaContent galleryMediaContent : medias) {
                if (Objects.equals(view.getMediaUnit(), galleryMediaContent.getView().getMediaUnit())) {
                    galleryStripe.getMedias().add(galleryMediaContent);
                    if (galleryMediaContent.isShowingInPreview()) {
                        previewImageResolver.put(galleryMediaContent.getId(), galleryMediaContent);
                    }
                    break;
                }
            }
        }
        stripes.add(galleryStripe);
        for (IncludedStructuralElement child : structure.getChildren()) {
            if (Objects.isNull(child.getLink())) {
                addStripesRecursive(child);
            }
        }
    }

    private GalleryMediaContent createGalleryMediaContent(View view) {
        MediaUnit mediaUnit = view.getMediaUnit();
        URI previewUri = mediaUnit.getMediaFiles().get(previewVariant);
        URI mediaViewUri = mediaUnit.getMediaFiles().get(mediaViewVariant);
        String canonical = Objects.nonNull(previewUri) ? previewFolder.getCanonical(previewUri) : null;
        return new GalleryMediaContent(this, view, canonical, mediaUnit.getOrder(),
                mediaUnit.getOrderlabel(), previewUri, mediaViewUri);
    }

    /**
     * Return the GalleryStripe instance representing the logical structure element to which the Media represented
     * by the given GalleryMediaContent instance is assigned. Return null, if Media is not assigned to any logical
     * structure element.
     *
     * @param galleryMediaContent
     *          Media
     * @return GalleryStripe representing the logical structure element to which the Media is assigned
     */
    GalleryStripe getLogicalStructureOfMedia(GalleryMediaContent galleryMediaContent) {
        for (GalleryStripe galleryStripe : stripes) {
            for (GalleryMediaContent mediaContent : galleryStripe.getMedias()) {
                if (galleryMediaContent.getId().equals(mediaContent.getId())) {
                    return galleryStripe;
                }
            }
        }
        return null;
    }

    GalleryMediaContent getGalleryMediaContent(View view) {
        for (GalleryMediaContent galleryMediaContent : this.medias) {
            if (galleryMediaContent.getView().getMediaUnit().equals(view.getMediaUnit())) {
                return galleryMediaContent;
            }
        }
        return null;
    }

    private List<GalleryMediaContent> getGalleryMediaContentRange(GalleryMediaContent first, GalleryMediaContent last) {
        List<GalleryMediaContent> galleryMediaContents = new LinkedList<>();
        for (GalleryMediaContent galleryMediaContent : getLogicalStructureOfMedia(first).getMedias()) {
            if (Integer.parseInt(galleryMediaContent.getOrder()) >= Integer.parseInt(first.getOrder())
                    && Integer.parseInt(galleryMediaContent.getOrder()) <= Integer.parseInt(last.getOrder())) {
                galleryMediaContents.add(galleryMediaContent);
            }
        }
        return galleryMediaContents;
    }

    /**
     * Check whether the passed GalleryMediaContent is selected.
     * @param galleryMediaContent the GalleryMediaContent to be checked
     * @return Boolean whether passed GalleryMediaContent is selected
     */
    public boolean isSelected(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(galleryMediaContent) && Objects.nonNull(selectedMedia)) {
            for (GalleryMediaContent galleryMediaContentIterator : selectedMedia) {
                if (galleryMediaContentIterator.getOrder().equals(galleryMediaContent.getOrder())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Update selection based on the passed GalleryMediaContent and selectionType.
     *
     * @param currentSelection the GalleryMediaContent that was clicked
     */
    public void select(GalleryMediaContent currentSelection) {
        switch (selectionType) {
            case "multi":
                multiSelect(currentSelection);
                break;
            case "range":
                rangeSelect(currentSelection);
                break;
            default:
                defaultSelect(currentSelection);
                break;
        }

        selectionType = "default";
        lastSelection = currentSelection;
        updateStructure(currentSelection);
    }

    private void defaultSelect(GalleryMediaContent currentSelection) {
        if (Objects.isNull(currentSelection)) {
            return;
        }

        lastSelection = currentSelection;
        selectedMedia.clear();
        selectedMedia.add(currentSelection);
    }

    private void rangeSelect(GalleryMediaContent currentSelection) {
        if (Objects.isNull(currentSelection)) {
            return;
        }

        if (!Objects.nonNull(lastSelection) || !getLogicalStructureOfMedia(currentSelection).getMedias().contains(lastSelection)) {
            lastSelection = currentSelection;
        } else { // TODO nach Drag&Drop enthalten Stripes neue Objekte. Alte Instanz von lastSelection kann nicht gefunden werden.
        }

        if (Integer.parseInt(currentSelection.getOrder()) > Integer.parseInt(lastSelection.getOrder())) {
            selectedMedia = getGalleryMediaContentRange(lastSelection, currentSelection);
        } else {
            selectedMedia = getGalleryMediaContentRange(currentSelection, lastSelection);
        }
    }

    private void multiSelect(GalleryMediaContent currentSelection) {
        if (Objects.isNull(currentSelection)) {
            return;
        }

        if (!Objects.nonNull(lastSelection)) {
            lastSelection = currentSelection;
        }

        removeMediaOfOtherStripes(currentSelection, selectedMedia);

        if (selectedMedia.contains(currentSelection)) {
            if (selectedMedia.size() > 1) {
                selectedMedia.remove(currentSelection);
            }
        } else {
            selectedMedia.add(currentSelection);
        }
    }

    private void removeMediaOfOtherStripes(GalleryMediaContent currentSelection, List<GalleryMediaContent> selectedMedia) {
        GalleryStripe currentSelectionStripe = getLogicalStructureOfMedia(currentSelection);
        List<GalleryMediaContent> mediaToBeRemoved = new ArrayList<>();
        for (GalleryMediaContent galleryMediaContent : selectedMedia) {
            if (!currentSelectionStripe.getMedias().contains(galleryMediaContent)) {
                mediaToBeRemoved.add(galleryMediaContent);
            }
        }
        selectedMedia.removeAll(mediaToBeRemoved);
    }
}

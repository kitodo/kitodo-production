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
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.Subfolder;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Backing bean for the gallery panel of the metadata editor.
 */
public class GalleryPanel {
    private static final Logger logger = LogManager.getLogger(GalleryPanel.class);

    // Structured media
    private static final Pattern DRAG_STRIPE_IMAGE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList:(\\d+):structuredPagePanel");

    private static final Pattern DROP_STRIPE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList");

    private static final Pattern DROP_MEDIA_AREA = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList:(\\d+):structuredPageDropArea");

    private static final Pattern DROP_MEDIA_LAST_AREA = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList:(\\d+):structuredPageLastDropArea");

    // Unstructured media
    private static final Pattern DRAG_UNSTRUCTURED_MEDIA = Pattern
            .compile("imagePreviewForm:unstructuredMediaList:(\\d+):unstructuredMediaPanel");

    private static final Pattern DROP_UNSTRUCTURED_STRIPE = Pattern
            .compile("imagePreviewForm:unstructuredMediaList");

    private static final Pattern DROP_UNSTRUCTURED_MEDIA_AREA = Pattern
            .compile("imagePreviewForm:unstructuredMediaList:(\\d+):unstructuredPageDropArea");

    private static final Pattern DROP_UNSTRUCTURED_MEDIA_LAST_AREA = Pattern
            .compile("imagePreviewForm:unstructuredMediaList:(\\d+):unstructuredPageLastDropArea");

    private final DataEditorForm dataEditor;
    private GalleryViewMode galleryViewMode = GalleryViewMode.LIST;
    private List<GalleryMediaContent> medias = Collections.emptyList();

    private MediaVariant mediaViewVariant;
    private Map<String, GalleryMediaContent> previewImageResolver = new HashMap<>();
    private MediaVariant previewVariant;
    private String selectionType = "";
    private Pair<MediaUnit, IncludedStructuralElement> lastSelection;

    private List<GalleryStripe> stripes;

    private Subfolder previewFolder;

    GalleryPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    void clear() {
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
    public Pair<MediaUnit, IncludedStructuralElement> getLastSelection() {
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
        int toStripeIndex = getDropStripeIndex(event);
        int toMediaIndex = getMediaIndex(event);

        if (toStripeIndex == -1 || !dragStripeIndexMatches(event)) {
            logger.error("Unsupported drag'n'drop event from {} to {}", event.getDragId(), event.getDropId());
            return;
        }

        GalleryStripe toStripe = stripes.get(toStripeIndex);

        // move views
        List<Pair<View, IncludedStructuralElement>> viewsToBeMoved = new ArrayList<>();
        for (Pair<MediaUnit, IncludedStructuralElement> selectedElememt : dataEditor.getSelectedMedia()) {
            for (View view : selectedElememt.getValue().getViews()) {
                if (Objects.equals(view.getMediaUnit(), selectedElememt.getKey())) {
                    viewsToBeMoved.add(new ImmutablePair<>(view, selectedElememt.getValue()));
                }
            }
        }
        dataEditor.getStructurePanel().moveViews(toStripe.getStructure(), viewsToBeMoved, toMediaIndex);

        // update stripes
        for (Pair<View, IncludedStructuralElement> viewToBeMoved : viewsToBeMoved) {
            GalleryStripe fromStripe = getGalleryStripe(viewToBeMoved.getValue());
            if (Objects.nonNull(fromStripe)) {
                fromStripe.getMedias().clear();
                for (View remainingView : fromStripe.getStructure().getViews()) {
                    fromStripe.getMedias().add(createGalleryMediaContent(remainingView));
                }
            }
        }
        toStripe.getMedias().clear();

        List<View> movedViews = viewsToBeMoved.stream().map(Pair::getKey).collect(Collectors.toList());

        dataEditor.getSelectedMedia().clear();

        for (View toStripeView : toStripe.getStructure().getViews()) {
            GalleryMediaContent galleryMediaContent = createGalleryMediaContent(toStripeView);
            toStripe.getMedias().add(galleryMediaContent);
            if (movedViews.contains(toStripeView)) {
                selectionType = "multi";
                select(galleryMediaContent, toStripe);
            }
        }
        dataEditor.getStructurePanel().show();
    }

    private boolean dragStripeIndexMatches(DragDropEvent event) {
        Matcher dragStripeImageMatcher = DRAG_STRIPE_IMAGE.matcher(event.getDragId());
        Matcher dragUnstructuredMediaMatcher = DRAG_UNSTRUCTURED_MEDIA.matcher(event.getDragId());
        return dragUnstructuredMediaMatcher.matches() || dragStripeImageMatcher.matches();
    }

    private int getDropStripeIndex(DragDropEvent event) {
        // empty stripe of structure element
        Matcher dropStripeMatcher = DROP_STRIPE.matcher(event.getDropId());
        // between two pages of structure element
        Matcher dropMediaAreaMatcher = DROP_MEDIA_AREA.matcher(event.getDropId());
        // after last page of structure element
        Matcher dropMediaLastAreaMatcher = DROP_MEDIA_LAST_AREA.matcher(event.getDropId());
        // empty unstructured media stripe
        Matcher dropUnstructuredMediaStripeMatcher = DROP_UNSTRUCTURED_STRIPE.matcher(event.getDropId());
        // between two pages of unstructured media stripe
        Matcher dropUnstructuredMediaAreaMatcher = DROP_UNSTRUCTURED_MEDIA_AREA.matcher(event.getDropId());
        // after last page of unstructured media stripe
        Matcher dropUnstructuredMediaLastAreaMatcher = DROP_UNSTRUCTURED_MEDIA_LAST_AREA.matcher(event.getDropId());
        if (dropStripeMatcher.matches()) {
            return Integer.parseInt(dropStripeMatcher.group(1));
        } else if (dropMediaAreaMatcher.matches()) {
            return Integer.parseInt(dropMediaAreaMatcher.group(1));
        } else if (dropMediaLastAreaMatcher.matches()) {
            return Integer.parseInt(dropMediaLastAreaMatcher.group(1));
        } else if (dropUnstructuredMediaStripeMatcher.matches()
                || dropUnstructuredMediaLastAreaMatcher.matches()
                || dropUnstructuredMediaAreaMatcher.matches()) {
            // First (0) stripe represents logical root element (unstructured media)
            return 0;
        } else {
            return -1;
        }
    }

    private int getMediaIndex(DragDropEvent event) {
        Matcher dropMediaAreaMatcher = DROP_MEDIA_AREA.matcher(event.getDropId());
        Matcher dropMediaLastAreaMatcher = DROP_MEDIA_LAST_AREA.matcher(event.getDropId());
        Matcher dropUnstructuredMediaAreaMatcher = DROP_UNSTRUCTURED_MEDIA_AREA.matcher(event.getDropId());
        Matcher dropUnstructuredMediaLastAreaMatcher = DROP_UNSTRUCTURED_MEDIA_LAST_AREA.matcher(event.getDropId());
        if (dropMediaAreaMatcher.matches()) {
            return Integer.parseInt(dropMediaAreaMatcher.group(2));
        } else if (dropMediaLastAreaMatcher.matches()) {
            return Integer.parseInt(dropMediaLastAreaMatcher.group(2)) + 1;
        } else if (dropUnstructuredMediaAreaMatcher.matches()) {
            return Integer.parseInt(dropUnstructuredMediaAreaMatcher.group(1));
        } else if (dropUnstructuredMediaLastAreaMatcher.matches()) {
            return Integer.parseInt(dropUnstructuredMediaLastAreaMatcher.group(1)) + 1;
        } else {
            return -1;
        }
    }

    private GalleryStripe getGalleryStripe(IncludedStructuralElement structuralElement) {
        for (GalleryStripe galleryStripe : stripes) {
            if (Objects.equals(galleryStripe.getStructure(), structuralElement)) {
                return galleryStripe;
            }
        }
        return null;
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
     * Update the selected TreeNode in the physical structure tree.
     */
    private void updateStructure(GalleryMediaContent galleryMediaContent) {
        dataEditor.getStructurePanel().updateNodeSelection(galleryMediaContent);
    }

    void updateSelection(MediaUnit mediaUnit) {
        if (mediaUnit.getMediaFiles().size() > 0) {

            // Update structured view
            if (this.galleryViewMode.equals(GalleryViewMode.LIST)) {
                for (GalleryStripe galleryStripe : getStripes()) {
                    for (GalleryMediaContent galleryMediaContent : galleryStripe.getMedias()) {
                        if (Objects.equals(mediaUnit, galleryMediaContent.getView().getMediaUnit())) {
                            dataEditor.getSelectedMedia().clear();
                            dataEditor.getSelectedMedia().add(new ImmutablePair<>(mediaUnit, galleryStripe.getStructure()));
                            lastSelection = new ImmutablePair<>(mediaUnit, galleryStripe.getStructure());
                            break;
                        }
                    }
                }
            }
            // Update unstructured view
            else {
                for (GalleryMediaContent galleryMediaContent : getMedias()) {
                    if (Objects.equals(mediaUnit, galleryMediaContent.getView().getMediaUnit())) {
                        dataEditor.getSelectedMedia().clear();
                        dataEditor.getSelectedMedia().add(new ImmutablePair<>(
                                mediaUnit, getLogicalStructureOfMedia(galleryMediaContent).getStructure()));
                        lastSelection = new ImmutablePair<>(mediaUnit, getLogicalStructureOfMedia(galleryMediaContent).getStructure());
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

    /**
     * Get the GalleryMediaContent object of the passed MediaUnit.
     * @param mediaUnit Object to find the GalleryMediaContent for
     * @return GalleryMediaContent
     */
    public GalleryMediaContent getGalleryMediaContent(MediaUnit mediaUnit) {
        for (GalleryStripe galleryStripe : stripes) {
            for (GalleryMediaContent media : galleryStripe.getMedias()) {
                if (Objects.equals(media.getView().getMediaUnit(), mediaUnit)) {
                    return media;
                }
            }
        }
        return null;
    }

    private List<Pair<MediaUnit, IncludedStructuralElement>> getMediaWithinRange(Pair<MediaUnit, IncludedStructuralElement> first,
                                                          Pair<MediaUnit, IncludedStructuralElement> last) {
        // Pairs of stripe index and media index
        Pair<Integer, Integer> firstIndices = getIndices(first.getKey(), first.getValue());
        Pair<Integer, Integer> lastIndices = getIndices(last.getKey(), last.getValue());
        Pair<Integer, Integer> minIndices;
        Pair<Integer, Integer> maxIndices;

        if (!Objects.nonNull(firstIndices) || !Objects.nonNull(lastIndices)) {
            return null;
        }

        /* Stripe with index 0 represents "unstructured media".
           This stripe is displayed last, but is actually the root element (first stripe). */
        if (!Objects.equals(firstIndices.getKey(), lastIndices.getKey())
                && ((lastIndices.getKey() == 0) || ((firstIndices.getKey() < lastIndices.getKey()) && (firstIndices.getKey() != 0)))) {
            minIndices = firstIndices;
            maxIndices = lastIndices;
        } else if (Objects.equals(firstIndices.getKey(), lastIndices.getKey())) {
            if (firstIndices.getValue() < lastIndices.getValue()) {
                minIndices = firstIndices;
                maxIndices = new ImmutablePair<>(firstIndices.getKey(), lastIndices.getValue());
            } else {
                minIndices = new ImmutablePair<>(firstIndices.getKey(), lastIndices.getValue());
                maxIndices = firstIndices;
            }
        } else {
            minIndices = lastIndices;
            maxIndices = firstIndices;
        }

        List<GalleryStripe> stripesWithinRange = new LinkedList<>();
        if (maxIndices.getKey() == 0 && minIndices.getKey() != 0) {
            for (int i = minIndices.getKey(); i <= stripes.size() - 1; i++) {
                stripesWithinRange.add(stripes.get(i));
            }
            if (!stripesWithinRange.contains(stripes.get(0))) {
                stripesWithinRange.add(stripes.get(0));
            }
        } else {
            for (int i = minIndices.getKey(); i <= maxIndices.getKey(); i++) {
                stripesWithinRange.add(stripes.get(i));
            }
        }
        return getMediaWithinRange(minIndices.getValue(), maxIndices.getValue(), stripesWithinRange);
    }

    private List<Pair<MediaUnit, IncludedStructuralElement>> getMediaWithinRange(int firstMediaIndex, int lastMediaIndex,
                                                                                 List<GalleryStripe> galleryStripes) {
        LinkedList<Pair<MediaUnit, IncludedStructuralElement>> mediaWithinRange = new LinkedList<>();
        if (galleryStripes.size() > 1) {
            GalleryStripe firstStripe = galleryStripes.get(0);
            for (int i = firstMediaIndex; i < galleryStripes.get(0).getMedias().size(); i++) {
                mediaWithinRange.add(new ImmutablePair<>(firstStripe.getMedias().get(i).getView().getMediaUnit(),
                        firstStripe.getStructure()));
            }

            for (int i = 1; i < galleryStripes.size() - 1; i++) {
                GalleryStripe galleryStripe = galleryStripes.get(i);
                for (GalleryMediaContent media : galleryStripe.getMedias()) {
                    mediaWithinRange.add(new ImmutablePair<>(media.getView().getMediaUnit(), galleryStripe.getStructure()));
                }
            }

            GalleryStripe lastStripe = galleryStripes.get(galleryStripes.size() - 1);
            for (int i = 0; i <= lastMediaIndex; i++) {
                mediaWithinRange.add(new ImmutablePair<>(lastStripe.getMedias().get(i).getView().getMediaUnit(),
                        lastStripe.getStructure()));
            }
        } else if (galleryStripes.size() == 1) {
            GalleryStripe stripe = galleryStripes.get(0);
            for (int i = firstMediaIndex; i <= lastMediaIndex; i++) {
                mediaWithinRange.add(new ImmutablePair<>(stripe.getMedias().get(i).getView().getMediaUnit(), stripe.getStructure()));
            }
        }
        return mediaWithinRange;
    }

    private Pair<Integer, Integer> getIndices(MediaUnit mediaUnit, IncludedStructuralElement structuralElement) {
        for (GalleryStripe galleryStripe : stripes) {
            if (Objects.equals(galleryStripe.getStructure(), structuralElement)) {
                for (GalleryMediaContent media : galleryStripe.getMedias()) {
                    if (Objects.equals(media.getView().getMediaUnit(), mediaUnit)) {
                        return new ImmutablePair<>(stripes.indexOf(galleryStripe), galleryStripe.getMedias().indexOf(media));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check whether the passed GalleryMediaContent is selected.
     * @param galleryMediaContent the GalleryMediaContent to be checked
     * @return Boolean whether passed GalleryMediaContent is selected
     */
    public boolean isSelected(GalleryMediaContent galleryMediaContent, GalleryStripe galleryStripe) {
        if (Objects.nonNull(galleryMediaContent) && Objects.nonNull(galleryMediaContent.getView())) {
            MediaUnit mediaUnit = galleryMediaContent.getView().getMediaUnit();
            if (Objects.nonNull(mediaUnit)) {
                if (Objects.nonNull(galleryStripe)) {
                    return dataEditor.isSelected(mediaUnit, galleryStripe.getStructure());
                } else {
                    return dataEditor.isSelected(mediaUnit, getLogicalStructureOfMedia(galleryMediaContent).getStructure());
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
    public void select(GalleryMediaContent currentSelection, GalleryStripe parentStripe) {
        IncludedStructuralElement structureElement;
        if (Objects.nonNull(parentStripe)) {
            structureElement = parentStripe.getStructure();
        } else {
            parentStripe = getLogicalStructureOfMedia(currentSelection);
            structureElement = parentStripe.getStructure();
        }

        MediaUnit mediaUnit;
        if (Objects.nonNull(currentSelection)) {
            mediaUnit = currentSelection.getView().getMediaUnit();
        } else {
            Helper.setErrorMessage("Passed GalleryMediaContent must not be null.");
            return;
        }

        switch (selectionType) {
            case "multi":
                multiSelect(currentSelection, parentStripe);
                break;
            case "range":
                rangeSelect(currentSelection, parentStripe);
                break;
            default:
                defaultSelect(currentSelection, parentStripe);
                break;
        }

        selectionType = "default";
        lastSelection = new ImmutablePair<>(mediaUnit, structureElement);
        updateStructure(currentSelection);
    }

    private void defaultSelect(GalleryMediaContent currentSelection, GalleryStripe parentStripe) {
        if (Objects.isNull(currentSelection)) {
            return;
        }

        lastSelection = new ImmutablePair<>(currentSelection.getView().getMediaUnit(), parentStripe.getStructure());
        dataEditor.getSelectedMedia().clear();
        dataEditor.getSelectedMedia().add(new ImmutablePair<>(currentSelection.getView().getMediaUnit(), parentStripe.getStructure()));
    }

    private void rangeSelect(GalleryMediaContent currentSelection, GalleryStripe parentStripe) {
        if (Objects.isNull(currentSelection)) {
            return;
        }

        if (!Objects.nonNull(lastSelection)) {
            lastSelection = new ImmutablePair<>(currentSelection.getView().getMediaUnit(), parentStripe.getStructure());
        }

        Pair<MediaUnit, IncludedStructuralElement> selectedMediaPair =
                new ImmutablePair<>(currentSelection.getView().getMediaUnit(), parentStripe.getStructure());
        dataEditor.getSelectedMedia().clear();
        dataEditor.getSelectedMedia().addAll(getMediaWithinRange(selectedMediaPair, lastSelection));
    }

    private void multiSelect(GalleryMediaContent currentSelection, GalleryStripe parentStripe) {
        Pair<MediaUnit, IncludedStructuralElement> selectedMediaPair = new ImmutablePair<>(
                currentSelection.getView().getMediaUnit(), parentStripe.getStructure());

        if (Objects.isNull(lastSelection)) {
            lastSelection = selectedMediaPair;
        }

        if (dataEditor.getSelectedMedia().contains(selectedMediaPair)) {
            if (dataEditor.getSelectedMedia().size() > 1) {
                dataEditor.getSelectedMedia().remove(selectedMediaPair);
            }
        } else {
            dataEditor.getSelectedMedia().add(selectedMediaPair);
        }
    }

    /**
     * Get the index of this GalleryMediaContent's MediaUnit out of all MediaUnits
     * which are assigned to more than one IncludedStructuralElement.
     *
     * @param galleryMediaContent object to find the index for
     * @return index of the GalleryMediaContent's MediaUnit if present in the List of several assignments, or -1 if not present in the list.
     */
    public int getSeveralAssignmentsIndex(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(galleryMediaContent.getView()) && Objects.nonNull(galleryMediaContent.getView().getMediaUnit())) {
            return dataEditor.getStructurePanel().getSeveralAssignments().indexOf(galleryMediaContent.getView().getMediaUnit());
        }
        return -1;
    }
}

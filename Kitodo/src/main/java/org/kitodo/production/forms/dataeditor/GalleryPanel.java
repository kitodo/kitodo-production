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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.model.Subfolder;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Backing bean for the gallery panel of the meta-data editor.
 */
public class GalleryPanel {
    private static final Logger logger = LogManager.getLogger(GalleryPanel.class);

    private static final Pattern DRAG_STRIPE_IMAGE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList:(\\d+):structuredPagePanel");

    private static final Pattern DROP_STRIPE = Pattern
            .compile("imagePreviewForm:structuredPages:(\\d+):structureElementDataList");

    private final DataEditorForm dataEditor;
    private GalleryViewMode galleryViewMode = GalleryViewMode.LIST;
    private List<GalleryMediaContent> medias = Collections.emptyList();

    private MediaVariant mediaViewVariant;
    private int pageIndex;
    private Map<String, GalleryMediaContent> previewImageResolver = new HashMap<>();
    private MediaVariant previewVariant;
    private GalleryMediaContent selectedMedia;
    private GalleryStripe selectedStripe;

    private ArrayList<GalleryStripe> stripes;

    private Subfolder previewFolder;

    GalleryPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
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
     * Checks and returns whether access is granted to the image with the given
     * filepath "imagePath".
     *
     * @param imagePath
     *            the filepath of the image for which access rights are checked
     * @return true if access is granted and false otherwise
     */
    public boolean getImageAccessible(String imagePath) {
        // TODO implement
        // like method isAccessGranted(String imagePath) in
        // MetadataProcessor:2643?
        return false;
    }

    LockResult getLocks() {
        return dataEditor.getLocks();
    }

    /**
     * Get list of all pages allocated to the passed logical element.
     *
     * @param logicalElement
     *            // TODO add param description
     * @return List of all allocated pages
     */
    public List<LegacyDocStructHelperInterface> getLogicalElementPageList(
            LegacyDocStructHelperInterface logicalElement) {
        // TODO implement
        // get list of all pages allocated to the logicalElement
        return new ArrayList<>();
    }

    /**
     * Get the logical page number for a paginated docstruct.
     *
     * @param docStruct
     *            The DocStruct object
     * @return The logical page number
     */
    public String getLogicalPageNumber(LegacyDocStructHelperInterface docStruct) {
        // TODO implement
        return "";
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
     * Get file path to png image for passed LegacyDocStructHelperInterface
     * 'page' representing a single scanned image.
     *
     * @param page
     *            LegacyDocStructHelperInterface for which the corresponding png
     *            image file path is returned
     * @return File path to the png image
     */
    public String getPageImageFilePath(LegacyDocStructHelperInterface page) {
        // TODO implement
        // like getPageImageFilePath(LegacyDocStructHelperInterface
        // pageDocStruct) in MetadataProcessor:2560?
        return "";
    }

    /**
     * Get pageIndex.
     *
     * @return value of pageIndex
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Get the physical page number for the passed
     * LegacyDocStructHelperInterface 'page'.
     *
     * @param page
     *            LegacyDocStructHelperInterface which physical page number is
     *            returned
     * @return physical page number
     */
    public int getPhysicalPageNumber(LegacyDocStructHelperInterface page) {
        // TODO implement
        return 0;
    }

    /**
     * Returns the media content of the preview media. This is the method that
     * is called when the web browser wants to retrieve the media file itself.
     *
     * @return a Primefaces object that handles the output of media data
     */
    public StreamedContent getPreviewData() {
        try {
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
        } catch (Exception e) {
            logger.catching(e);
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
    public GalleryMediaContent getSelectedMedia() {
        return selectedMedia;
    }

    public GalleryStripe getSelectedStripe() {
        return selectedStripe;
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
     * Get the path to the thumbnail for the with the passed image path.
     *
     * @param imagePath
     *            Path to the fullsize PNG image
     * @return Path to the thumnail PNG image
     */
    public String getThumbnail(String imagePath) {
        File imageFile = new File(imagePath);
        String filename = imageFile.getName();
        return imagePath.replace("FULLSIZE_FOLDER_NAME", "THUMBNAIL_FOLDER_NAME").replace(filename,
            "thumbnail." + filename);
    }

    /**
     * Handle event of page being dragged and dropped.
     *
     * @param event
     *            JSF drag'n'drop event description object
     */
    public void onPageDrop(DragDropEvent event) {
        Matcher dragStripeImageMatcher = DRAG_STRIPE_IMAGE.matcher(event.getDragId());
        Matcher dropStripeMatcher = DROP_STRIPE.matcher(event.getDropId());
        if (dragStripeImageMatcher.matches() && dropStripeMatcher.matches()) {
            int fromStripeIndex = Integer.parseInt(dragStripeImageMatcher.group(1));
            int fromStripeMediaIndex = Integer.parseInt(dragStripeImageMatcher.group(2));
            int toStripeIndex = Integer.parseInt(dropStripeMatcher.group(1));
            if (fromStripeIndex == toStripeIndex) {
                return;
            }

            GalleryStripe fromStripe = stripes.get(fromStripeIndex);
            GalleryMediaContent mediaContent = fromStripe.getMedias().get(fromStripeMediaIndex);
            GalleryStripe toStripe = stripes.get(toStripeIndex);

            MetadataEditor.moveView(mediaContent.getView(), fromStripe.getStructure(), toStripe.getStructure());

            // update stripes
            fromStripe.getMedias().clear();
            for (View fromStripeView : fromStripe.getStructure().getViews()) {
                fromStripe.getMedias().add(createGalleryMediaContent(fromStripeView));
            }
            toStripe.getMedias().clear();
            for (View toStripeView : toStripe.getStructure().getViews()) {
                toStripe.getMedias().add(createGalleryMediaContent(toStripeView));
            }
            return;
        }

        // TODO: add more drag'n'drop events here

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
     * Set pageIndex.
     *
     * @param pageIndex
     *            as int
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Set selectedMedia.
     *
     * @param selectedMedia
     *            as org.kitodo.production.forms.dataeditor.GalleryMediaContent
     */
    public void setSelectedMedia(GalleryMediaContent selectedMedia) {
        this.selectedMedia = selectedMedia;
    }

    /**
     * Update the selected TreeNode in the physical structure tree.
     */
    public void updateStructure(GalleryMediaContent galleryMediaContent) {
        dataEditor.getStructurePanel().updateNodeSelection(galleryMediaContent);
    }

    public void setSelectedStripe(GalleryStripe selectedStripe) {
        this.selectedStripe = selectedStripe;
    }

    void updateSelection(MediaUnit mediaUnit) {
        if (mediaUnit.getMediaFiles().size() > 0) {
            Map<MediaVariant, URI> mediaVariants = mediaUnit.getMediaFiles();

            // Update structured view
            if (this.galleryViewMode.equals(GalleryViewMode.LIST)) {
                for (GalleryStripe galleryStripe : getStripes()) {
                    for (GalleryMediaContent galleryMediaContent : galleryStripe.getMedias()) {
                        if (mediaVariants.values().contains(galleryMediaContent.getPreviewUri())) {
                            setSelectedMedia(galleryMediaContent);
                            setSelectedStripe(galleryStripe);
                            break;
                        }
                    }
                }

            }
            // Update unstructured view
            else {
                for (GalleryMediaContent galleryMediaContent : getMedias()) {
                    if (mediaVariants.values().contains(galleryMediaContent.getPreviewUri())) {
                        setSelectedMedia(galleryMediaContent);
                        break;
                    }
                }
            }
        }
    }

    void show() throws IOException {
        Process process = dataEditor.getProcess();
        Project project = process.getProject();
        List<MediaUnit> mediaUnits = dataEditor.getWorkpiece().getMediaUnits();

        Folder previewSettings = project.getPreview();
        previewVariant = Objects.nonNull(previewSettings) ? getMediaVariant(previewSettings, mediaUnits) : null;
        Folder mediaViewSettings = project.getMediaView();
        mediaViewVariant = Objects.nonNull(mediaViewSettings) ? getMediaVariant(mediaViewSettings, mediaUnits) : null;

        medias = new ArrayList<>(mediaUnits.size());
        stripes = new ArrayList<>();
        previewImageResolver = new HashMap<>();

        previewFolder = new Subfolder(process, project.getPreview());
        Map<URI, LockingMode> lockRequests = new HashMap<>();
        for (MediaUnit mediaUnit : mediaUnits) {
            View wholeMediaUnitView = new View();
            wholeMediaUnitView.setMediaUnit(mediaUnit);
            GalleryMediaContent mediaContent = createGalleryMediaContent(wholeMediaUnitView);
            medias.add(mediaContent);
            if (mediaContent.isShowingInPreview()) {
                previewImageResolver.put(mediaContent.getId(), mediaContent);
            }
            lockRequests.putAll(mediaContent.getRequiredLocks());
        }

        lockRequests.putAll(addStripesRecursive(dataEditor.getWorkpiece().getRootElement()));
        int imagesInStructuredView = stripes.parallelStream().mapToInt(stripe -> stripe.getMedias().size()).sum();
        if (imagesInStructuredView > 200) {
            logger.warn("Number of images in structured view: {}", imagesInStructuredView);
        }

        Map<URI, Collection<String>> conflicts = dataEditor.getLocks().tryLock(lockRequests);
        if (!conflicts.isEmpty()) {
            throw new LockingConflictsException(conflicts);
        }
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

    private Map<URI, LockingMode> addStripesRecursive(IncludedStructuralElement structure) {
        GalleryStripe galleryStripe = new GalleryStripe(this, structure);
        Map<URI, LockingMode> lockRequests = new HashMap<>();
        for (View view : structure.getViews()) {
            GalleryMediaContent mediaContent = createGalleryMediaContent(view);
            galleryStripe.getMedias().add(mediaContent);
            lockRequests.putAll(mediaContent.getRequiredLocks());
            if (mediaContent.isShowingInPreview()) {
                previewImageResolver.put(mediaContent.getId(), mediaContent);
            }
        }
        stripes.add(galleryStripe);
        for (IncludedStructuralElement child : structure.getChildren()) {
            lockRequests.putAll(addStripesRecursive(child));
        }
        return lockRequests;
    }

    private GalleryMediaContent createGalleryMediaContent(View view) {
        MediaUnit mediaUnit = view.getMediaUnit();
        URI previewUri = mediaUnit.getMediaFiles().get(previewVariant);
        URI mediaViewUri = mediaUnit.getMediaFiles().get(mediaViewVariant);
        String canonical = Objects.nonNull(previewUri) ? previewFolder.getCanonical(previewUri) : null;
        return new GalleryMediaContent(this, view, canonical, mediaUnit.getOrder(),
                mediaUnit.getOrderlabel(), previewUri, mediaViewUri);
    }
}

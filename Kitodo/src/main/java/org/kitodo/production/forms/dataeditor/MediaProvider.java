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

import static org.kitodo.constants.StringConstants.MEDIA_ID;
import static org.kitodo.constants.StringConstants.MEDIA_VIEW;
import static org.kitodo.constants.StringConstants.PREVIEW;
import static org.kitodo.constants.StringConstants.PROCESS;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Session scoped media provider bean.
 */
@SessionScoped
@Named
public class MediaProvider implements Serializable {

    private static final Logger logger = LogManager.getLogger(MediaProvider.class);

    private final Map<Integer, Map<String, GalleryMediaContent>> mediaResolver = new HashMap<>();

    /**
     * Get the media resolver.
     *
     * @return value of media resolver
     */
    public Map<String, GalleryMediaContent> getMediaResolver(int processId) {
        if (!mediaResolver.containsKey(processId)) {
            mediaResolver.put(processId, new HashMap<>());

        }
        return mediaResolver.get(processId);
    }

    /**
     * Add media content to the media resolver.
     */
    public void addMediaContent(int processId, GalleryMediaContent galleryMediaContent) {
        if (galleryMediaContent.isShowingInPreview() || galleryMediaContent.isShowingInMediaView()) {
            getMediaResolver(processId).put(galleryMediaContent.getId(), galleryMediaContent);
        }
    }

    /**
     * Reset media resolver for process with provided ID 'processId'
     * by removing the corresponding map.
     *
     * @param processId process ID
     */
    public void resetMediaResolverForProcess(int processId) {
        mediaResolver.remove(processId);
    }

    /**
     * Returns the media content of the preview media.
     *
     * @return preview of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getPreviewData() {
        Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return getMediaContent(PREVIEW, parameterMap.get(PROCESS), parameterMap.get(MEDIA_ID));
    }

    /**
     * Returns the media content of the media view.
     *
     * @return media view of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getMediaView() {
        Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return getMediaContent(MEDIA_VIEW, parameterMap.get(PROCESS), parameterMap.get(MEDIA_ID));
    }

    /**
     * Returns if media content has preview variant.
     *
     * @param galleryMediaContent
     *         The gallery media content object
     * @return True if media content has preview variant
     */
    public boolean hasPreviewVariant(GalleryMediaContent galleryMediaContent) {
        return Objects.nonNull(galleryMediaContent) && galleryMediaContent.isShowingInPreview();
    }

    /**
     * Returns if media content has media view variant.
     *
     * @param galleryMediaContent
     *         The gallery media content object
     * @return True if media content has media view variant
     */
    public boolean hasMediaViewVariant(GalleryMediaContent galleryMediaContent) {
        return Objects.nonNull(galleryMediaContent) && galleryMediaContent.isShowingInMediaView();
    }

    private StreamedContent getMediaContent(String mediaVariant, String processIdString, String mediaIdString) {
        if (Objects.nonNull(processIdString) && Objects.nonNull(mediaIdString)) {
            try {
                int processId = Integer.parseInt(processIdString);
                if (mediaResolver.containsKey(processId)) {
                    Map<String, GalleryMediaContent> processPreviewData = mediaResolver.get(processId);
                    GalleryMediaContent mediaContent = processPreviewData.get(mediaIdString);
                    if (Objects.nonNull(mediaContent)) {
                        logger.trace("Serving image request {}", mediaIdString);
                        if (PREVIEW.equals(mediaVariant)) {
                            return mediaContent.getPreviewData();
                        }
                        if (MEDIA_VIEW.equals(mediaVariant)) {
                            return mediaContent.getMediaViewData();
                        }
                        logger.error("Error: Unknown media variant '{}'", mediaVariant);
                    }
                    logger.debug("Cannot serve image request, mediaId = {}", mediaIdString);
                }
                logger.debug("Media resolver does not contain media content for process with ID {}", processId);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage("Process ID '" + processIdString + "' is not numeric!");
            }
        }
        return DefaultStreamedContent.builder().build();
    }
}

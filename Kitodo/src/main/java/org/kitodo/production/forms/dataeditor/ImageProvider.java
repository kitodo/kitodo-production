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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


/**
 * Application scoped image provider bean.
 */
@ApplicationScoped
@Named
public class ImageProvider {

    private static final Logger logger = LogManager.getLogger(ImageProvider.class);

    private final Map<Integer, Map<String, GalleryMediaContent>> previewImageResolver = new HashMap<>();

    private static final String PREVIEW = "preview";

    private static final String MEDIA_VIEW = "mediaView";

    /**
     * Get previewImageResolver.
     *
     * @return value of previewImageResolver
     */
    public Map<String, GalleryMediaContent> getPreviewImageResolver(int processId) {
        if (!previewImageResolver.containsKey(processId)) {
            previewImageResolver.put(processId, new HashMap<>());

        }
        return previewImageResolver.get(processId);
    }

    /**
     * Reset preview image resolver for process with provided ID 'processId'
     * by removing the corresponding map.
     *
     * @param processId process ID
     */
    public void resetPreviewImageResolverForProcess(int processId) {
        previewImageResolver.remove(processId);
    }

    /**
     * Returns the media content of the preview media.
     *
     * @return preview of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getPreviewData() {
        return getMediaContent(PREVIEW);
    }

    /**
     * Returns the media content of the media view.
     *
     * @return media view of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getMediaView() {
        return getMediaContent(MEDIA_VIEW);
    }

    private StreamedContent getMediaContent(String mediaVariant) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE) {
            String processIdString = context.getExternalContext().getRequestParameterMap().get("process");
            try {
                int processId = Integer.parseInt(processIdString);
                if (previewImageResolver.containsKey(processId)) {
                    String id = context.getExternalContext().getRequestParameterMap().get("mediaId");
                    Map<String, GalleryMediaContent> processPreviewData = previewImageResolver.get(processId);
                    GalleryMediaContent mediaContent = processPreviewData.get(id);
                    if (Objects.nonNull(mediaContent)) {
                        logger.trace("Serving image request {}", id);
                        if (PREVIEW.equals(mediaVariant)) {
                            return mediaContent.getPreviewData();
                        }
                        if (MEDIA_VIEW.equals(mediaVariant)) {
                            return mediaContent.getMediaViewData();
                        }
                        logger.error("Error: Unknown media variant '" + mediaVariant + "'");
                    }
                    logger.debug("Cannot serve image request, mediaId = {}", id);
                }
                logger.debug("Preview image resolver does not contain media content for process with ID {}", processId);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage("Process ID '" + processIdString + "' is not numeric!");
            }
        }
        return DefaultStreamedContent.builder().build();
    }

}

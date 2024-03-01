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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.MediaPartial;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.enums.MediaContentType;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * A single media content in the media gallery.
 */
public class GalleryMediaContent {

    private static final Logger logger = LogManager.getLogger(GalleryMediaContent.class);

    /**
     * Identifier for the media content.
     */
    private final String id;

    /**
     * URI to content for media preview.
     */
    private final URI previewUri;

    /**
     * URI to the content for the media view.
     */
    private final URI mediaViewUri;
    private final View view;

    private final String previewMimeType;

    private final String mediaViewMimeType;

    /**
     * Type of the current object.
     *
     * <p>Mime types are used for streaming, but they are not the representative type of the object. Therefore, we need
     * this fixed type for distinction.</p>
     */
    private final MediaContentType type;

    /**
     * Stores the primefaces tree node id of the corresponding tree node of the logical structure 
     * tree. This id can be used in the user interface to identify which gallery thumbnail 
     * corresponds to which tree node in the logical structure tree.
     *
     * <p>It consists of a sequence of numbers separated by underscore, e.g. "0_1_4". Each number
     * describes the position of a child amongst its siblings at that level. For example, "0_1_4" 
     * references the node that is reached when moving from root node to leaf node using the first 
     * child, then the second child, and then the fifth child.</p>
     * 
     * <p>The root node itself is never referenced, as it is not visualized anyway.</p>
     */
    private String logicalTreeNodeId;

    /**
     * Creates a new gallery media content.
     *
     * @param canonical
     *         the canonical part of the file name, used as the identifier for the media content
     * @param previewUri
     *         URI to content for media preview. Can be {@code null}, then a placeholder is used.
     * @param mediaViewUri
     *         URI to the content for the media view. Can be {@code null}, then no media view is offered.
     */
    GalleryMediaContent(MediaContentType type, View view, String canonical, String previewMimeType, URI previewUri,
            String mediaViewMimeType, URI mediaViewUri, String logicalTreeNodeId) {
        this.type = type;
        this.view = view;
        this.id = canonical;
        this.previewMimeType = previewMimeType;
        this.previewUri = previewUri;
        this.mediaViewMimeType = mediaViewMimeType;
        this.mediaViewUri = mediaViewUri;
        this.logicalTreeNodeId = logicalTreeNodeId;
    }

    /**
     * Returns the ID of the media file. The ID is needed to create a unique URI
     * to correctly reference the media file within a JSF output loop.
     *
     * @return the media file ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the shortened ID of the media by removing leading zeros.
     * @return the shortened id of the media file
     */
    public String getShortId() {
        if (Objects.nonNull(id)) {
            return id.replaceFirst("^0+(?!$)", "");
        } else {
            return "-";
        }
    }

    /**
     * Get the mime type of the selected media view of the project.
     *
     * @return the mime type of media view
     */
    public String getMediaViewMimeType() {
        return mediaViewMimeType;
    }

    /**
     * Get the mime type of the selected preview of the project.
     *
     * @return the mime type of preview
     */
    public String getPreviewMimeType() {
        return previewMimeType;
    }

    /**
     * Returns the media content. This is the method that is called when the web browser wants to retrieve the media
     * file itself.
     *
     * @return a Primefaces object that handles the output of media data
     */
    public StreamedContent getMediaViewData() {
        return sendData(mediaViewUri, mediaViewMimeType);
    }

    /**
     * Returns the order number of the medium (to be displayed to the user).
     *
     * @return the order number
     */
    public String getOrder() {
        return Integer.toString(view.getPhysicalDivision().getOrder());
    }

    /**
     * Returns the order label of the medium.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return view.getPhysicalDivision().getOrderlabel();
    }

    /**
     * Returns the media content of the preview media. This is the method that
     * is called through the gallery panel using a media ID.
     *
     * @return a Primefaces object that handles the output of media data
     */
    StreamedContent getPreviewData() {
        return sendData(previewUri, previewMimeType);
    }

    /**
     * Indicates if there is a media view for this media. Production is able to
     * work with media files for which there is currently no adequate display
     * capability in web browsers. In this case, the media view is not offered.
     * This is configured by selecting a folder for the media view in the
     * project or not.
     *
     * @return if there is a media view for this media
     */
    public boolean isShowingInMediaView() {
        return Objects.nonNull(mediaViewUri);
    }

    /**
     * Indicates if there is a media preview for this media. Production is able
     * to work with media files for which there is currently no adequate display
     * capability in web browsers. In this case, a placeholder is displayed.
     * This is configured by selecting a folder for the media preview in the
     * project or not.
     *
     * @return if there is a media preview for this media
     */
    public boolean isShowingInPreview() {
        return Objects.nonNull(previewUri);
    }

    /**
     * Check if physical division of view has a media partial.
     *
     * @return True if has a media partial
     */
    public boolean isMediaPartial() {
        return Objects.nonNull(view) && Objects.nonNull(view.getPhysicalDivision()) && view.getPhysicalDivision()
                .hasMediaPartial();
    }

    /**
     * Get the media partial of physical division.
     *
     * @return The media partial or null
     */
    public MediaPartial getMediaPartial() {
        if (isMediaPartial()) {
            return view.getPhysicalDivision().getMediaPartial();
        }
        return null;
    }

    /**
     * Returns the type of gallery media content object.
     *
     * @return the type of gallery media content object.
     */
    public String getType() {
        return type.name();
    }

    /**
     * Method for output of URL-referenced media content.
     *
     * @param uri
     *         internal URI of the media file to be transferred
     * @param mimeType
     *         the Internet MIME type of the media file
     * @return a Primefaces object that handles the output of media data
     */
    private StreamedContent sendData(URI uri, String mimeType) {
        /*
         * During the construction of the HTML page, only an URL for the media
         * file is generated.
         */
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return DefaultStreamedContent.builder().build();
        }

        /*
         * Output of media data when the browser retrieves this URL. Note that
         * we are NOT ALLOWED TO close the InputStream at this point. Faces does
         * that after transferring the data.
         */
        try {
            InputStream viewData = ServiceManager.getFileService().read(uri);
            return DefaultStreamedContent.builder().stream(() -> viewData).contentType(mimeType)
                    .name(Paths.get(uri.getPath()).getFileName().toString()).contentLength(viewData.available())
                    .build();
        } catch (IOException e) {
            logger.catching(e);
            String errorpage = "<html>" + System.lineSeparator() + "<h1>Error!</h1>" + System.lineSeparator() + "<p>"
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + "</p>" + System.lineSeparator() + "</html>"
                    + System.lineSeparator();
            ByteArrayInputStream errorPage = new ByteArrayInputStream(errorpage.getBytes(StandardCharsets.UTF_8));
            return DefaultStreamedContent.builder().stream(() -> errorPage).contentType("text/html")
                    .name("errorpage.html").contentEncoding("UTF-8").build();
        }
    }

    public View getView() {
        return view;
    }

    /**
     * Check if the GalleryMediaContent's PhysicalDivision is assigned to several LogicalDivisions.
     *
     * @return {@code true} when the PhysicalDivision is assigned to more than one logical element
     */
    public boolean isAssignedSeveralTimes() {
        if (Objects.nonNull(view) && Objects.nonNull(view.getPhysicalDivision())) {
            return view.getPhysicalDivision().getLogicalDivisions().size() > 1;
        }
        return false;
    }

    /**
     * Returns the id to the corresponding tree node of the primefaces tree component used to 
     * visualize the logical structure tree.
     * 
     * @return the logical tree node id
     */
    public String getLogicalTreeNodeId() {
        return this.logicalTreeNodeId;
    }

    /**
     * Sets the id of the corresponding tree node of the primefaces tree component used to visualize the logical
     * structure tree.
     *
     * @param treeNodeId
     *         the tree node id
     */
    public void setLogicalTreeNodeId(String treeNodeId) {
        this.logicalTreeNodeId = treeNodeId;
    }

}

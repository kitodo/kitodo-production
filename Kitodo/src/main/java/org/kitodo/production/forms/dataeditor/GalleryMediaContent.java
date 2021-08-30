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
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * A single media content in the media gallery.
 */
public class GalleryMediaContent {
    private static final Logger logger = LogManager.getLogger(GalleryMediaContent.class);
    /**
     * Gallery panel in which the medium is displayed.
     */
    private final GalleryPanel panel;

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

    /**
     * Creates a new gallery media content.
     *
     * @param panel
     *            gallery panel in which the medium is displayed. The panel
     *            provides configuration information.
     * @param canonical
     *            the canonical part of the file name, used as the identifier
     *            for the media content
     * @param previewUri
     *            URI to content for media preview. Can be {@code null}, then a
     *            placeholder is used.
     * @param mediaViewUri
     *            URI to the content for the media view. Can be {@code null},
     *            then no media view is offered.
     */
    GalleryMediaContent(GalleryPanel panel, View view, String canonical, URI previewUri, URI mediaViewUri) {
        this.panel = panel;
        this.view = view;
        this.id = canonical;
        this.previewUri = previewUri;
        this.mediaViewUri = mediaViewUri;
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
     * Returns the media content. This is the method that is called when the web
     * browser wants to retrieve the media file itself.
     *
     * @return a Primefaces object that handles the output of media data
     */
    public StreamedContent getMediaViewData() {
        return sendData(mediaViewUri, panel.getMediaViewMimeType());
    }

    /**
     * Returns the order number of the medium (to be displayed to the user).
     *
     * @return the order number
     */
    public String getOrder() {
        return Integer.toString(view.getMediaUnit().getOrder());
    }

    /**
     * Returns the order label of the medium.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return view.getMediaUnit().getOrderlabel();
    }

    /**
     * Returns the media content of the preview media. This is the method that
     * is called through the gallery panel using a media ID.
     *
     * @return a Primefaces object that handles the output of media data
     */
    StreamedContent getPreviewData() {
        return sendData(previewUri, panel.getPreviewMimeType());
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
     * Method for output of URL-referenced media content.
     *
     * @param uri
     *            internal URI of the media file to be transferred
     * @param mimeType
     *            the Internet MIME type of the media file
     * @return a Primefaces object that handles the output of media data
     */
    private StreamedContent sendData(URI uri, String mimeType) {
        /*
         * During the construction of the HTML page, only an URL for the media
         * file is generated.
         */
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        }

        /*
         * Output of media data when the browser retrieves this URL. Note that
         * we are NOT ALLOWED TO close the InputStream at this point. Faces does
         * that after transferring the data.
         */
        try {
            InputStream previewData = ServiceManager.getFileService().read(uri);
            return new DefaultStreamedContent(previewData, mimeType);
        } catch (IOException e) {
            logger.catching(e);
            String errorpage = "<html>" + System.lineSeparator() + "<h1>Error!</h1>" + System.lineSeparator() + "<p>"
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + "</p>" + System.lineSeparator() + "</html>"
                    + System.lineSeparator();
            ByteArrayInputStream errorPage = new ByteArrayInputStream(errorpage.getBytes(StandardCharsets.UTF_8));
            return new DefaultStreamedContent(errorPage, "text/html", "errorpage.html", "UTF-8");
        }
    }

    public View getView() {
        return view;
    }

    /**
     * Check if the GalleryMediaContent's MediaUnit is assigned to several LogicalDivisions.
     *
     * @return {@code true} when the MediaUnit is assigned to more than one logical element
     */
    public boolean isAssignedSeveralTimes() {
        if (Objects.nonNull(view) && Objects.nonNull(view.getMediaUnit())) {
            return view.getMediaUnit().getLogicalDivisions().size() > 1;
        }
        return false;
    }
}

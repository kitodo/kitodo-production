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

package org.kitodo.production.renderer;

import java.io.IOException;
import java.util.Optional;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.media.Media;
import org.primefaces.component.media.MediaRenderer;
import org.primefaces.component.media.player.MediaPlayer;
import org.primefaces.model.StreamedContent;
import org.primefaces.util.AgentUtils;
import org.primefaces.util.HTML;

public class KitodoMediaRenderer extends MediaRenderer {

    private static final String PLAYER_HTML_VIDEO = "html-video";

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Media media = (Media) component;
        ResponseWriter writer = context.getResponseWriter();
        String src;
        try {
            src = getMediaSrc(context, media);
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        Object value = media.getValue();
        if (value instanceof StreamedContent) {
            if (PLAYER_HTML_VIDEO.equals(media.getPlayer())) {
                buildVideoTag(context, media, writer, src);
                return;
            }
        }

        boolean isIE = AgentUtils.isIE(context);
        MediaPlayer player = resolvePlayer(context, media);
        String sourceParam = player.getSourceParam();

        // Fix npe exception https://forum.primefaces.org/viewtopic.php?t=62518
        // This fix is deprecated cause primeface fix this in 7.0.13
        if (value instanceof StreamedContent && "application/pdf".equals(player.getType())) {
            StreamedContent streamedContent = (StreamedContent) value;
            if (streamedContent.getName() != null) {
                int index = src.indexOf("?");
                src = src.substring(0, index) + ";/" + streamedContent.getName() + ""
                        + src.substring(index);
            }
        }

        String type = player.getType();
        if (type != null && type.equals("application/pdf")) {
            String view = media.getView();
            String zoom = media.getZoom();

            if (view != null) {
                src = src + "#view=" + view;
            }

            if (zoom != null) {
                src += (view != null) ? "&zoom=" + zoom : "#zoom=" + zoom;
            }
        }

        buildObjectTag(context, media, writer, src, isIE, player, sourceParam);
    }

    private void buildVideoTag(FacesContext context, Media media, ResponseWriter writer, String src)
            throws IOException {
        writer.startElement("video", media);
        if (media.getStyleClass() != null) {
            writer.writeAttribute("class", media.getStyleClass(), null);
        }

        Optional uiParameter = media.getChildren().stream().filter(param -> "controls".equals(((UIParameter) param).getName())).findFirst();
        if (uiParameter.isPresent() && !Boolean.FALSE.toString().equals(((UIParameter) uiParameter.get()).getValue())) {
            writer.writeAttribute("controls", "", null);
        }

        renderPassThruAttributes(context, media, HTML.MEDIA_ATTRS);
        writer.startElement("source", media);
        writer.writeAttribute("src", src, null);
        writer.writeAttribute("type", "video/mp4", null);
        writer.endElement("source");
        writer.endElement("video");
    }

    private void buildObjectTag(FacesContext context, Media media, ResponseWriter writer, String src, boolean isIE,
            MediaPlayer player, String sourceParam) throws IOException {
        writer.startElement("object", media);
        writer.writeAttribute("type", player.getType(), null);
        writer.writeAttribute("data", src, null);

        if (isIE) {
            encodeIEConfig(writer, player);
        }

        if (media.getStyleClass() != null) {
            writer.writeAttribute("class", media.getStyleClass(), null);
        }

        renderPassThruAttributes(context, media, HTML.MEDIA_ATTRS);

        if (sourceParam != null) {
            encodeParam(writer, player.getSourceParam(), src, false);
        }

        for (UIComponent child : media.getChildren()) {
            if (child instanceof UIParameter) {
                UIParameter param = (UIParameter) child;

                encodeParam(writer, param.getName(), param.getValue(), false);
            }
        }

        renderChildren(context, media);

        writer.endElement("object");
    }

}

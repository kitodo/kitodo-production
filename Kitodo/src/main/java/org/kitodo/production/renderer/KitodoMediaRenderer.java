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
import org.primefaces.util.HTML;

public class KitodoMediaRenderer extends MediaRenderer {

    private static final String PLAYER_HTML_VIDEO = "html-video";

    private static final String PLAYER_HTML_AUDIO = "html-audio";

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

        if (PLAYER_HTML_VIDEO.equals(media.getPlayer())) {
            buildVideoTag(context, media, writer, src);
            return;
        } else if (PLAYER_HTML_AUDIO.equals(media.getPlayer())) {
            buildAudioTag(context, media, writer, src);
            return;
        }

        super.encodeEnd(context, component);
    }

    private void buildVideoTag(FacesContext context, Media media, ResponseWriter writer, String src)
            throws IOException {
        writer.startElement("video", media);
        Optional<UIComponent> controlsParameter = media.getChildren().stream()
                .filter(param -> "controls".equals(((UIParameter) param).getName())).findAny();
        if (controlsParameter.isEmpty() || !Boolean.FALSE.toString()
                .equals(((UIParameter) controlsParameter.get()).getValue())) {
            writer.writeAttribute("controls", "", null);
        }
        buildMediaSource(context, media, writer, src);
        writer.endElement("video");
    }

    private void buildAudioTag(FacesContext context, Media media, ResponseWriter writer, String src)
            throws IOException {
        writer.startElement("audio", media);
        writer.writeAttribute("controls", "", null);
        buildMediaSource(context, media, writer, src);
        writer.endElement("audio");
    }

    private void buildMediaSource(FacesContext context, Media media, ResponseWriter writer, String src)
            throws IOException {
        if (media.getStyleClass() != null) {
            writer.writeAttribute("class", media.getStyleClass(), null);
        }

        renderPassThruAttributes(context, media, HTML.MEDIA_ATTRS);
        writer.startElement("source", media);
        writer.writeAttribute("src", src, null);

        Optional<UIComponent> typeParameter = media.getChildren().stream()
                .filter(param -> "type".equals(((UIParameter) param).getName())).findFirst();
        if (typeParameter.isPresent()) {
            writer.writeAttribute("type", ((UIParameter) typeParameter.get()).getValue(), null);
        }

        writer.endElement("source");
    }

}

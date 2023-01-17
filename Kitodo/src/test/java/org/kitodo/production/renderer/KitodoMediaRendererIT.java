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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Writer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;

import org.junit.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.primefaces.component.media.Media;

public class KitodoMediaRendererIT {

    @Test
    public void contentShouldBeValid() throws Exception {
        FacesContext facesContext = mock(FacesContext.class);
        StringBufferResponseWriter stringBufferResponseWriter = new StringBufferResponseWriter();
        when(facesContext.getResponseWriter()).thenReturn(stringBufferResponseWriter);
        KitodoMediaRenderer kitodoMediaRenderer = spy(new KitodoMediaRenderer());
        Media media = mock(Media.class);

        // call protected method without using powermock as dependency
        ReflectionSupport.invokeMethod(kitodoMediaRenderer.getClass().getSuperclass()
                        .getDeclaredMethod("getMediaSrc", FacesContext.class, Media.class),
                doReturn("http://www.example.com").when(kitodoMediaRenderer), facesContext, media);

        when(media.getPlayer()).thenReturn("html-video");
        kitodoMediaRenderer.encodeEnd(facesContext, media);
        assertEquals("<video controls=\"\"><source src=\"http://www.example.com\"></source></video>",
                stringBufferResponseWriter.getResponse());

        stringBufferResponseWriter.reset();
        when(media.getPlayer()).thenReturn("html-audio");
        kitodoMediaRenderer.encodeEnd(facesContext, media);
        assertEquals("<audio controls=\"\"><source src=\"http://www.example.com\"></source></audio>",
                stringBufferResponseWriter.getResponse());
    }

    public class StringBufferResponseWriter extends ResponseWriterWrapper {
        private StringBuffer response = new StringBuffer();

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void startDocument() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void endDocument() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void startElement(String name, UIComponent component) {
            if (response.length() > 0 && !response.substring(response.length() - 1).equals(">")) {
                response.append('>');
            }
            response.append('<');
            response.append(name);
        }

        @Override
        public void endElement(String name) {
            if (response.length() > 0 && !response.substring(response.length() - 1).equals(">")) {
                response.append('>');
            }
            response.append("</");
            response.append(name);
            response.append('>');
        }

        @Override
        public void writeAttribute(String name, Object value, String property) {
            response.append(' ');
            response.append(name);
            response.append("=\"");
            response.append(value);
            response.append('\"');
        }

        @Override
        public void writeURIAttribute(String name, Object value, String property) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeComment(Object comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeText(Object text, String property) {
            response.append(text);

        }

        @Override
        public void writeText(char[] text, int off, int len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResponseWriter cloneWithWriter(Writer writer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getResponse() {
            return response.toString();
        }

        public void reset() {
            this.response.delete(0, response.length());
        }

    }

}

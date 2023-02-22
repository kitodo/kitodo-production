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

package org.kitodo.production.handler;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.junit.Assert.assertEquals;
import static org.kitodo.production.helper.RangeStreamHelper.DEFAULT_BUFFER_SIZE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.el.ValueExpression;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.el.WeldExpressionFactory;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.BasePrimefaceTest;
import org.mockito.Mock;
import org.mockito.Spy;
import org.omnifaces.application.OmniApplication;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.util.Constants;

public class RangeStreamContentHandlerIT extends BasePrimefaceTest {

    public static final String FILENAME = "test.mp4";
    public static final String MIMETYPE = "video/mp4";
    private final String data = "some test data for my input stream";

    @Mock
    protected HttpServletRequest httpServletRequest;

    @Mock
    protected HttpServletResponse httpServletResponse;

    private ByteArrayOutputStream byteArrayOutputStream;

    @Spy
    private RangeStreamContentHandler rangeStreamContentHandler;

    @Mock
    private OmniApplication omniApplication;

    @Mock
    private WeldExpressionFactory weldExpressionFactory;

    @Mock
    private ValueExpression valueExpression;

    /**
     * Init before every test.
     *
     * @throws Exception the exceptions thrown by method
     */
    @Before
    public void init() throws Exception {
        when(externalContext.getRequest()).thenReturn(httpServletRequest);
        when(externalContext.getResponse()).thenReturn(httpServletResponse);

        Map<String, String> requestParameter = new HashMap<>();
        requestParameter.put("ln", Constants.LIBRARY);
        String dynamicContentKey = "1234";
        String dynamicContentValue = "4321";
        requestParameter.put(Constants.DYNAMIC_CONTENT_PARAM, dynamicContentKey);
        when(externalContext.getRequestParameterMap()).thenReturn(requestParameter);

        Map<String, Object> session = new HashMap<>();
        Map<String, String> dynamicResourcesMapping = new HashMap<>();
        dynamicResourcesMapping.put(dynamicContentKey, dynamicContentValue);
        session.put(Constants.DYNAMIC_RESOURCES_MAPPING, dynamicResourcesMapping);

        when(externalContext.getSessionMap()).thenReturn(session);
        when(facesContext.getApplication()).thenReturn(omniApplication);
        when(omniApplication.getExpressionFactory()).thenReturn(weldExpressionFactory);
        when(weldExpressionFactory.createValueExpression(facesContext.getELContext(), dynamicContentValue,
                StreamedContent.class)).thenReturn(valueExpression);

        InputStream inputStream = toInputStream(data, StandardCharsets.UTF_8);
        StreamedContent streamedContent = DefaultStreamedContent.builder().stream(() -> inputStream)
                .contentType(MIMETYPE).name(Paths.get(FILENAME).getFileName().toString())
                .contentLength(inputStream.available()).build();

        when(valueExpression.getValue(facesContext.getELContext())).thenReturn(streamedContent);

        byteArrayOutputStream = new ByteArrayOutputStream();
        when(externalContext.getResponseOutputStream()).thenReturn(byteArrayOutputStream);
    }

    /**
     * Test requesting full content.
     *
     * @throws Exception the exceptions thrown by method
     */
    @Test
    public void fullContent() throws Exception {
        int start = 0;
        int end = 100;

        when(httpServletRequest.getHeader("Range")).thenReturn("bytes=" + start + "-" + end);
        rangeStreamContentHandler.handle(facesContext);

        assertEquals(data, byteArrayOutputStream.toString(StandardCharsets.UTF_8));

        verify(httpServletResponse).setHeader("Content-Disposition", "inline;filename=\"" + FILENAME + "\"");
        verify(httpServletResponse).setHeader("ETag", FILENAME);
        verify(httpServletResponse).setHeader("Accept-Ranges", "bytes");
        verify(httpServletResponse).setBufferSize(DEFAULT_BUFFER_SIZE);
        verify(httpServletResponse).setContentType(MIMETYPE);
        verify(httpServletResponse).setHeader("Content-Range", "bytes 0-33/34");
        verify(httpServletResponse).setHeader("Content-Length", "34");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    }

    /**
     * Test using request header with wrong range.
     *
     * @throws Exception the exceptions thrown by method
     */
    @Test
    public void wrongRange() throws Exception {
        when(httpServletRequest.getHeader("Range")).thenReturn("");

        rangeStreamContentHandler.handle(facesContext);

        assertEquals(data, byteArrayOutputStream.toString(StandardCharsets.UTF_8));

        verify(httpServletResponse).setHeader("Content-Disposition", "inline;filename=\"" + FILENAME + "\"");
        verify(httpServletResponse).setHeader("ETag", FILENAME);
        verify(httpServletResponse).setHeader("Accept-Ranges", "bytes");
        verify(httpServletResponse).setBufferSize(DEFAULT_BUFFER_SIZE);
        verify(httpServletResponse).setContentType(MIMETYPE);
        verify(httpServletResponse).setHeader("Content-Range", "bytes */34");
        verify(httpServletResponse).setHeader("Content-Length", "34");
        verify(httpServletResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    /**
     * Test requesting partial content.
     *
     * @throws Exception the exceptions thrown by method
     */
    @Test
    public void partialContent() throws Exception {
        int start = 5;
        int end = 22;

        when(httpServletRequest.getHeader("Range")).thenReturn("bytes=" + start + "-" + end);
        when(httpServletRequest.getHeader("If-Range")).thenReturn(FILENAME);

        rangeStreamContentHandler.handle(facesContext);

        verify(httpServletResponse).setHeader("Content-Disposition", "inline;filename=\"" + FILENAME + "\"");
        verify(httpServletResponse).setHeader("ETag", FILENAME);
        verify(httpServletResponse).setHeader("Accept-Ranges", "bytes");
        verify(httpServletResponse).setBufferSize(DEFAULT_BUFFER_SIZE);
        verify(httpServletResponse).setContentType(MIMETYPE);
        verify(httpServletResponse).setHeader("Content-Range", "bytes " + start + "-" + end + "/34");
        verify(httpServletResponse).setHeader("Content-Length", "18");
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        assertEquals(data.substring(start,end + 1), byteArrayOutputStream.toString(StandardCharsets.UTF_8));
    }

}

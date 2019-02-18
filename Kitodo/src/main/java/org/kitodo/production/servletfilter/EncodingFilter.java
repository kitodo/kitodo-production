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

package org.kitodo.production.servletfilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(filterName = "EncodingFilter", urlPatterns = "/*", initParams = {
        @WebInitParam(name = "requestEncoding", value = "UTF-8") })
public class EncodingFilter implements Filter {

    private String encoding;

    /**
     * Initialize this filter by reading its configuration parameters.
     * 
     * @param filterConfig
     *            The Configuration from web.xml file.
     */
    @Override
    public void init(FilterConfig filterConfig) {
        encoding = filterConfig.getInitParameter("requestEncoding");
        if (Objects.isNull(encoding)) {
            encoding = StandardCharsets.UTF_8.name();
        }
    }

    /**
     * Sets the character encoding of request and response.
     *
     * @param request
     *            The ServletRequest.
     * @param response
     *            The ServletResponse.
     * @param chain
     *            The FilterChain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Respect the client-specified character encoding
        if (Objects.isNull(request.getCharacterEncoding())) {
            request.setCharacterEncoding(encoding);
        }

        response.setContentType("text/html; charset=" + encoding);
        response.setCharacterEncoding(encoding);

        chain.doFilter(request, response);
    }

    /**
     * Nothing is done here.
     */
    @Override
    public void destroy() {
        // nothing is done here
    }
}

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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;

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

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

package de.sub.goobi.helper.servletfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(filterName = "SecurityCheckFilter", urlPatterns = "*.jsf")
public class SecurityCheckFilter implements Filter {

    public SecurityCheckFilter() {
        // called once. no method arguments allowed here!
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    /**
     * Creates a new instance of SecurityCheckFilter.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hres = (HttpServletResponse) response;
        HttpSession session = hreq.getSession();

        if (session.isNew() && !hreq.getRequestURI().contains("pages/Main.jsf")) {
            hres.sendRedirect(hreq.getContextPath());
            return;
        }

        // deliver request to next filter
        chain.doFilter(request, response);
    }
}

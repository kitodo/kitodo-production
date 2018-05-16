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

package org.kitodo.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.security.SecurityAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.GenericFilterBean;

public class SecurityObjectAccessFilter extends GenericFilterBean {
    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private SecurityAccessService securityAccessService = new ServiceManager().getSecurityAccessService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String id = httpServletRequest.getParameter("id");

        if (Objects.nonNull(id)) {
            int idInt;
            try {
                idInt = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/clientEdit")
                    && !securityAccessService.isAdminOrHasAuthorityGlobalOrForClient("editClient", idInt)) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/projectEdit")
                    && !isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient("editProject", idInt)) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient(String authority, int id)
            throws IOException {
        try {
            return securityAccessService.isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient(authority, id);
        } catch (DataException e) {
            throw new IOException(e);
        }

    }

    private void denyAccess(HttpServletRequest hreq, HttpServletResponse hres) throws IOException, ServletException {
        accessDeniedHandler.handle(hreq, hres, new AccessDeniedException("Access is denied"));
    }
}

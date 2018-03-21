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

import org.kitodo.services.ServiceManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.GenericFilterBean;

public class SecurityObjectAccessFilter extends GenericFilterBean {
    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private ServiceManager serviceManager = new ServiceManager();
    private HttpServletRequest httpServletRequest;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        this.httpServletRequest = (HttpServletRequest) request;

        String id = httpServletRequest.getParameter("id");

        if (Objects.nonNull(id)) {
            int idInt = Integer.parseInt(id);
            if (!hasAccessToUrl("pages/clientEdit", "editClient", idInt, true)) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }
            if (!hasAccessToUrl("pages/projectEdit", "editProject", idInt, false)) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean hasAccessToUrl(String urlIdentifier, String authority, int id, boolean checkClientOnly) {
        if (httpServletRequest.getRequestURI().contains(urlIdentifier)) {
            if (checkClientOnly) {
                return serviceManager.getSecurityAccessService().isAdminOrHasAuthorityGlobalOrForClient(authority, id);
            } else {
                return serviceManager.getSecurityAccessService()
                    .isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient(authority, id);
            }
        }
        return true;
    }

    private void denyAccess(HttpServletRequest hreq, HttpServletResponse hres) throws IOException, ServletException {
        accessDeniedHandler.handle(hreq, hres, new AccessDeniedException("Access is denied"));
    }
}

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.GenericFilterBean;

public class SecurityObjectAccessFilter extends GenericFilterBean {
    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private ServiceManager serviceManager = new ServiceManager();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hres = (HttpServletResponse) response;

        if (hreq.getRequestURI().contains("pages/clientEdit")) {
            int id = getIdByRequest(hreq);
            if (!serviceManager.getSecurityAccessService().isAdminOrHasAuthorityGlobalOrForClient("editClient", id)) {
                denyAccess(hreq, hres);
                return;
            }
        }

        if (hreq.getRequestURI().contains("pages/projectEdit")) {
            if (Objects.nonNull(hreq.getParameter("id"))) {
                int id = Integer.parseInt(hreq.getParameter("id"));
                if (!serviceManager.getSecurityAccessService().isAdminOrHasAuthorityGlobalOrForProjectOrForRelatedClient("editProject",id)) {
                    denyAccess(hreq, hres);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void denyAccess(HttpServletRequest hreq, HttpServletResponse hres) throws IOException, ServletException {
        accessDeniedHandler.handle(hreq, hres, new AccessDeniedException("Access is denied"));
    }

    private Integer getIdByRequest(HttpServletRequest hreq) {
        Integer id = null;
        if (Objects.nonNull(hreq.getParameter("id"))) {
            id = Integer.parseInt(hreq.getParameter("id"));
        }
        return id;
    }
}

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
import org.kitodo.services.security.SecurityAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.GenericFilterBean;

/**
 * This filter handles the accessibility of urls which contains the parameter
 * "id". The access is denied if the user does not have the corresponding
 * authority for the current id.
 */
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
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException e) {
                if (httpServletRequest.getRequestURI().contains("pages/workflowEdit")
                        && securityAccessService.hasAuthorityToViewWorkflow()) {
                    chain.doFilter(request, response);
                } else {
                    denyAccess(httpServletRequest, httpServletResponse);
                }
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/workflowEdit")
                    && !securityAccessService.hasAuthorityToViewWorkflow()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/processEdit")
                    && !securityAccessService.hasAuthorityToViewProcess()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/projectEdit")
                    && !securityAccessService.hasAuthorityToViewProject()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/templateEdit")
                    && !securityAccessService.hasAuthorityToViewTemplate()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/docketEdit")
                    && !securityAccessService.hasAuthorityToViewDocket()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/rulesetEdit")
                    && !securityAccessService.hasAuthorityToViewRuleset()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/userEdit")
                    && !securityAccessService.hasAuthorityToViewUser()
                    && !securityAccessService.hasAuthorityToConfigUser(Integer.parseInt(id))) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/roleEdit")
                    && !securityAccessService.hasAuthorityToViewRole()) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }

            if (httpServletRequest.getRequestURI().contains("pages/clientEdit")
                    && !securityAccessService.hasAuthorityGlobalOrForClient("viewClient")) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void denyAccess(HttpServletRequest hreq, HttpServletResponse hres) throws IOException, ServletException {
        accessDeniedHandler.handle(hreq, hres, new AccessDeniedException("Access is denied"));
    }
}

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

package org.kitodo.production.security;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;
import org.omnifaces.cdi.viewscope.ViewScopeManager;
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
    private final AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private final SecurityAccessService securityAccessService = ServiceManager.getSecurityAccessService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String idString = httpServletRequest.getParameter("id");
        if (Objects.nonNull(idString)) {
            int id;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException e) {
                if (httpServletRequest.getRequestURI().contains("pages/workflowEdit")
                        && securityAccessService.hasAuthorityToViewWorkflow()) {
                    chain.doFilter(request, response);
                } else if (ViewScopeManager.isUnloadRequest(httpServletRequest)) {
                    chain.doFilter(request, response);
                } else {
                    denyAccess(httpServletRequest, httpServletResponse);
                }
                return;
            }
            if (!isAccessAllowed(httpServletRequest.getRequestURI(), id)) {
                denyAccess(httpServletRequest, httpServletResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isAccessAllowed(String requestUri, int id) {
        boolean allowed = true;
        String objectType = "unknown object type";

        try {
            if (requestUri.contains("pages/processEdit")) {
                objectType = "process";
                allowed = securityAccessService.hasAuthorityToViewProcess(id);
            } else if (requestUri.contains("pages/taskEdit")) {
                objectType = "task";
                allowed = securityAccessService.hasAuthorityToEditTask(id);
            } else if (requestUri.contains("pages/projectEdit")) {
                objectType = "project";
                allowed = securityAccessService.hasAuthorityToViewProject(id);
            } else if (requestUri.contains("pages/templateEdit")) {
                objectType = "template";
                allowed = securityAccessService.hasAuthorityToViewTemplate(id);
            } else if (requestUri.contains("pages/workflowEdit")) {
                objectType = "workflow";
                allowed = securityAccessService.hasAuthorityToViewWorkflow(id);
            } else if (requestUri.contains("pages/docketEdit")) {
                objectType = "docket";
                allowed = securityAccessService.hasAuthorityToViewDocket(id);
            } else if (requestUri.contains("pages/rulesetEdit")) {
                objectType = "ruleset";
                allowed = securityAccessService.hasAuthorityToViewRuleset(id);
            } else if (requestUri.contains("pages/roleEdit")) {
                objectType = "role";
                allowed = securityAccessService.hasAuthorityToViewRole(id);
            } else if (requestUri.contains("pages/clientEdit")) {
                objectType = "client";
                allowed = securityAccessService.hasAuthorityToViewClient(id);
            } else if (requestUri.contains("pages/userEdit")) {
                objectType = "user";
                allowed = securityAccessService.hasAuthorityToViewUser() || securityAccessService.hasAuthorityToConfigUser(id);
            } else if (requestUri.contains("pages/importConfigurationEdit")) {
                objectType = "importConfiguration";
                allowed = securityAccessService.hasAuthorityToViewImportConfiguration(id);
            }
        } catch (DAOException e) {
            logger.error(String.format("Error loading %s with ID %d for access permission check", objectType, id), e);
            allowed = false;
        }
        return allowed;
    }

    private void denyAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        accessDeniedHandler.handle(request, response, new AccessDeniedException("Access is denied"));
    }
}

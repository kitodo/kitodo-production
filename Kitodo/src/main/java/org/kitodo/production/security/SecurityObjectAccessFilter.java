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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;
import org.omnifaces.cdi.viewscope.ViewScopeManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter handles the accessibility of urls which contains the parameter
 * "id". The access is denied if the user does not have the corresponding
 * authority for the current id.
 */
public class SecurityObjectAccessFilter extends OncePerRequestFilter {
    private final AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private final SecurityAccessService securityAccessService = ServiceManager.getSecurityAccessService();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain chain)
            throws IOException, ServletException {
        String idString = httpServletRequest.getParameter("id");
        if (Objects.nonNull(idString)) {
            int id;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException e) {
                if (httpServletRequest.getRequestURI().contains("pages/workflowEdit")
                        && securityAccessService.hasAuthorityToViewWorkflow()) {
                    chain.doFilter(httpServletRequest, httpServletResponse);
                } else if (ViewScopeManager.isUnloadRequest(httpServletRequest)) {
                    chain.doFilter(httpServletRequest, httpServletResponse);
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
        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean isAccessAllowed(String requestUri, int id) {
        boolean processSubEditPageAccessAllowed = isAccessAllowedToProcessAndTaskSubPages(requestUri, id);
        boolean projectSubEditPageAccessAllowed = isAccessAllowedToProjectSubPage(requestUri, id);
        boolean userSubEditPageAccessAllowed = isAccessAllowedToUserSubPage(requestUri, id);
        return processSubEditPageAccessAllowed || projectSubEditPageAccessAllowed || userSubEditPageAccessAllowed;
    }

    private boolean isAccessAllowedToProcessAndTaskSubPages(String requestUri, int id) {
        boolean allowed = false;
        String objectType = "unknown object type";
        try {
            if (requestUri.contains("pages/processEdit")) {
                objectType = "process";
                allowed = securityAccessService.hasAuthorityToViewProcess(id);
            } else if (requestUri.contains("pages/processFromTemplate")) {
                objectType = "process";
                allowed = securityAccessService.hasAuthorityToAddProcess(); // no id because new process is added
            } else if (requestUri.contains("pages/batchProperties")) {
                objectType = "batch";
                allowed = securityAccessService.hasAuthorityToEditBatch(); // no id specific variant available, yet
            } else if (requestUri.contains("pages/taskEdit")) {
                objectType = "task";
                allowed = securityAccessService.hasAuthorityToEditTask(id);
            } else if (requestUri.contains("pages/taskBatchEdit")) {
                allowed = true; // no specific permission/authority for batchTasks available, yet
            } else if (requestUri.contains("/pages/calendarEdit")) {
                objectType = "process";
                allowed = securityAccessService.hasAuthorityToAddProcess(); // no id because new processes are added
            } else if (requestUri.contains("/pages/currentTasksEdit")) {
                allowed = true; // access to 'currentTasksEdit' is currently not restricted to specific permissions
            } else if (requestUri.contains("/pages/metadataEditor")) {
                allowed = true; // metadata editor contains its own access checks
            }
        } catch (DAOException e) {
            logger.error(String.format("Error loading %s with ID %d for access permission check", objectType, id), e);
        }
        return allowed;
    }

    private boolean isAccessAllowedToProjectSubPage(String requestUri, int id) {
        boolean allowed = false;
        String objectType = "unknown object type";
        try {
            if (requestUri.contains("pages/projectEdit")) {
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
            } else if (requestUri.contains("pages/importConfigurationEdit")) {
                objectType = "importConfiguration";
                allowed = securityAccessService.hasAuthorityToViewImportConfiguration(id);
            } else if (requestUri.contains("pages/mappingFileEdit")) {
                objectType = "mappingFile";
                allowed = securityAccessService.hasAuthorityToViewMappingFile(); // no id specific variant available, yet
            } else if (requestUri.contains("pages/ltpValidationConfigurationEdit")) {
                objectType = "ltpValidationConfiguration";
                allowed = securityAccessService.hasAuthorityToViewLtpValidationConfiguration(); // no id specific variant available, yet
            }
        } catch (DAOException e) {
            logger.error(String.format("Error loading %s with ID %d for access permission check", objectType, id), e);
        }
        return allowed;
    }

    private boolean isAccessAllowedToUserSubPage(String requestUri, int id) {
        boolean allowed = false;
        String objectType = "unknown object type";
        try {
            if (requestUri.contains("pages/userEdit")) {
                objectType = "user";
                allowed = securityAccessService.hasAuthorityToViewUser() || securityAccessService.hasAuthorityToConfigUser(id);
            } else if (requestUri.contains("pages/roleEdit")) {
                objectType = "role";
                allowed = securityAccessService.hasAuthorityToViewRole(id);
            } else if (requestUri.contains("pages/clientEdit")) {
                objectType = "client";
                allowed = securityAccessService.hasAuthorityToViewClient(id);
            } else if (requestUri.contains("pages/authorityEdit")) {
                objectType = "authority";
                allowed = securityAccessService.hasAuthorityToViewAuthority();  // no id specific variant available, yet
            } else if (requestUri.contains("pages/ldapgroupEdit")) {
                objectType = "ldapGroup";
                allowed = securityAccessService.hasAuthorityToViewLdapGroup(); // no id specific variant available, yet
            } else if (requestUri.contains("pages/ldapserverEdit")) {
                objectType = "ldapServer";
                allowed = securityAccessService.hasAuthorityToViewLdapServer(); // no id specific variant available, yet
            }
        } catch (DAOException e) {
            logger.error(String.format("Error loading %s with ID %d for access permission check", objectType, id), e);
        }
        return allowed;
    }

    private void denyAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        accessDeniedHandler.handle(request, response, new AccessDeniedException("Access is denied"));
    }
}

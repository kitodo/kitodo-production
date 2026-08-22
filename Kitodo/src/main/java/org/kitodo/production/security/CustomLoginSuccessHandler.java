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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.naming.NamingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.DataException;
import org.kitodo.api.logintask.LoginTaskType;
import org.kitodo.data.database.beans.LoginTask;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.controller.SessionClientController;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.LoginTaskService;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LogManager.getLogger(CustomLoginSuccessHandler.class);
    private static final LoginTaskService loginTaskService = ServiceManager.getLoginTaskService();
    private static final String DESKTOP_LANDING_PAGE = "/pages/desktop";
    private static final String EMPTY_LANDING_PAGE = "/pages/checks";
    private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    private static final String OMNIFACES_EVENT = "omnifaces.event";
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {

        try {
            doLoginTasks(authentication);
            SessionClientController controller = new SessionClientController();
            if (ServiceManager.getIndexingService().isIndexCorrupted()
                    || (controller.getAvailableClientsOfCurrentUser().size() > 1
                    && Objects.isNull(controller.getDefaultClientOfCurrentUser()))) {
                // redirect to empty landing page, where dialogs are displayed depending on both checks!
                redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, EMPTY_LANDING_PAGE);
            } else {
                if (Objects.nonNull(httpServletRequest.getSession())) {
                    // calling showClientSelectDialog automatically sets the only one available client here
                    controller.showClientSelectDialog();
                    redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
                            getOriginalRequest(httpServletRequest.getSession().getAttribute(SAVED_REQUEST)));
                }
            }
        } catch (DataException | DAOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves the highest priority pending login task for a user and execute it.
     * 
     * @param authentication Authentication object which was created during the authentication process
     */
    public static void doLoginTasks(Authentication authentication) {
        User user = ServiceManager.getUserService().getCurrentUser();
        Optional<LoginTask> pendingLoginTask = loginTaskService.getNextPendingLoginTaskForUser(user);

        if (pendingLoginTask.isEmpty()) {
            return;
        }

        if (LoginTaskType.SAVE_USER_TO_LDAP.equals(pendingLoginTask.get().getType())) {
            try {
                ServiceManager.getLdapServerService().createNewUser(user, String.valueOf(authentication.getCredentials()));
                loginTaskService.finishTaskAsSuccessfullyCompleted(pendingLoginTask.get());
            } catch (NameAlreadyBoundException e) {
                Helper.setErrorMessage("Ldap entry already exists", logger, e);
                loginTaskService.finishTaskWithError(pendingLoginTask.get(), "Ldap entry already exists");
            } catch (NoSuchAlgorithmException | NamingException | IOException | RuntimeException e) {
                Helper.setErrorMessage("Could not generate ldap entry", logger, e);
                loginTaskService.finishTaskWithError(pendingLoginTask.get(), "Could not generate ldap entry: " + e.getMessage());
            }
        }

        // in the future, add more login tasks, e.g. redirect user to reset user password, setup 2fa
    }

    /**
     * Determine the original request URI from a given 'savedRequest' object and return it. If the object is null or
     * does not contain a saved request URI, return a default landing page URI instead.
     *
     * @param savedRequest object containing the previous request object
     *
     * @return the URI of the previous request or a default URI as String
     */
    public static String getOriginalRequest(Object savedRequest) {
        String redirect = DESKTOP_LANDING_PAGE;
        if (savedRequest instanceof DefaultSavedRequest) {
            DefaultSavedRequest request = (DefaultSavedRequest) savedRequest;
            if (Objects.nonNull(request.getServletPath()) && !request.getServletPath().isEmpty()
                    && (!request.getParameterMap().containsKey(OMNIFACES_EVENT)
                    || Arrays.stream(request.getParameterMap().get(OMNIFACES_EVENT)).noneMatch("unload"::equals))) {
                redirect = request.getServletPath();
                if (Objects.nonNull(request.getQueryString()) && !request.getQueryString().isEmpty()) {
                    redirect = redirect + "?" + request.getQueryString();
                }
            }
        }
        return redirect;
    }

    /**
     * Return String constant containing the session attribute parameter name for saved requests.
     *
     * @return saved requests session attribute parameter string constant
     */
    public static String getSavedRequestString() {
        return SAVED_REQUEST;
    }
}

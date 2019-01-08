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

package org.kitodo;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityTestUtils {

    /**
     * Adds a given user as SecurityUserDetails object to security context. Can be
     * used for unit testing.
     * 
     * @param user
     *            user object
     * @param clientId
     *            id of client dor session client
     */
    public static void addUserDataToSecurityContext(User user, int clientId) throws DAOException {
        SecurityUserDetails securityUserDetails = new SecurityUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(securityUserDetails, null,
                securityUserDetails.getAuthorities());
        securityUserDetails.setSessionClient(ServiceManager.getClientService().getById(clientId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Explicitly clears the context value from the current thread.
     */
    public static void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}

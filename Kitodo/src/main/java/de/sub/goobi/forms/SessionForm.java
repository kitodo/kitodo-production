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

package de.sub.goobi.forms;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.kitodo.security.SecuritySession;
import org.kitodo.services.ServiceManager;

@Named
@ApplicationScoped
public class SessionForm {

    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * Gets all active sessions.
     *
     * @return The active sessions.
     */
    public List<SecuritySession> getActiveSessions() {
        return serviceManager.getSessionService().getActiveSessions();
    }
}

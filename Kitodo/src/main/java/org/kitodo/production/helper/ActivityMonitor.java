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

package org.kitodo.production.helper;

import java.util.Iterator;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;

@Named
@RequestScoped
public class ActivityMonitor {

    /**
     * Event handler for 'idle' event. Triggered when user becomes idle and is about to be logged out automatically.
     * Displays a warning message to inform the user he is about to get logged out soon.
     */
    public void onIdle() {
        String warningTitle = Helper.getTranslation("automaticLogoutWarningTitle");
        String warningDescription = Helper.getTranslation("automaticLogoutWarningDescription");
        PrimeFaces.current().executeScript("PF('sticky-notifications').renderMessage("
                + "{'summary':'" + warningTitle + "','detail':'" + warningDescription + "','severity':'error'});");
    }

    /**
     * Event handler for 'active' event. Triggered when user becomes active again after being idle.
     * Removes the warning message about pending automatic logout.
     */
    public void onActive() {
        Iterator<FacesMessage> messageIterator = FacesContext.getCurrentInstance().getMessages();
        while (messageIterator.hasNext()) {
            messageIterator.next();
            messageIterator.remove();
        }
    }
}

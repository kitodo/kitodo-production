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

package org.kitodo.selenium;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class LoginST extends BaseTestSelenium {

    @Test
    public void indexWarningTest() throws Exception {
        // remove one process from index but not from DB to provoke index warning
        ServiceManager.getProcessService().removeFromIndex(1, true);

        // log into Kitodo with non-admin user to be redirected to 'checks' page
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getById(2));
        assertEquals("http://localhost:8080/kitodo/pages/checks.jsf", Browser.getCurrentUrl());
        Pages.getPostLoginChecksPage().logout();

        // log into kitodo with admin user to be redirected to 'system' page
        Pages.getLoginPage().goTo().performLoginAsAdmin();
        assertEquals("http://localhost:8080/kitodo/pages/system.jsf?tabIndex=2", Browser.getCurrentUrl());
        Pages.getTopNavigation().logout();

        // restore deleted process to index
        Process unindexedProcess = ServiceManager.getProcessService().getById(1);
        ServiceManager.getProcessService().addAllObjectsToIndex(Collections.singletonList(unindexedProcess));
    }

    @Test
    public void defaultClientTest() throws Exception {
        User userNowak = ServiceManager.getUserService().getByLogin("nowak");
        assertTrue(userNowak.getClients().size() > 1, "Test user should have more than one client");
        Client defaultClient = userNowak.getDefaultClient();
        assertNull(defaultClient, "Default client should be null.");

        Pages.getLoginPage().goTo().performLogin(userNowak);
        assertEquals("http://localhost:8080/kitodo/pages/checks.jsf", Browser.getCurrentUrl(),
                "User with multiple clients but no default client should get redirected to 'checks' page.");
        Pages.getTopNavigation().cancelClientSelection();

        // set default client of user
        Client firstClient = ServiceManager.getClientService().getById(1);
        userNowak.setDefaultClient(firstClient);
        ServiceManager.getUserService().saveToDatabase(userNowak);

        Pages.getLoginPage().goTo().performLogin(userNowak);
        assertEquals("http://localhost:8080/kitodo/pages/desktop.jsf", Browser.getCurrentUrl(),
                "User with default client should get redirected to 'desktop' page.");
        Pages.getTopNavigation().logout();

        // restore users original settings
        userNowak.setDefaultClient(null);
        ServiceManager.getUserService().saveToDatabase(userNowak);
    }
}

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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;

public class LoginST extends BaseTestSelenium {

    @BeforeClass
    public static void manipulateIndex() throws DataException, CustomResponseException {
        // remove one process from index but not from DB to provoke index warning
        ServiceManager.getProcessService().removeFromIndex(1, true);
    }

    @Test
    public void indexWarningTest() throws Exception {
        // log into Kitodo with non-admin user to be redirected to 'checks' page
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getById(2));
        Assert.assertEquals("http://localhost:8080/kitodo/pages/checks.jsf", Browser.getCurrentUrl());
        Pages.getPostLoginChecksPage().logout();

        // log into kitodo with admin user to be redirected to 'system' page
        Pages.getLoginPage().goTo().performLoginAsAdmin();
        Assert.assertEquals("http://localhost:8080/kitodo/pages/system.jsf?tabIndex=2", Browser.getCurrentUrl());
        Pages.getTopNavigation().logout();
    }
}

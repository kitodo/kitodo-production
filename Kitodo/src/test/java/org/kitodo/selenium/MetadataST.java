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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;

public class MetadataST extends BaseTestSelenium {

    @Before
    public void login() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("verylast");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
    }

    /**
     * Tests whether structure tree is hidden when user lacks permission to see a process structure in metadata editor.
     */
    @Test
    public void hideStructureDataTest() throws Exception {
        Pages.getProcessesPage().goTo().editMetadata();
        Assert.assertFalse(Pages.getMetadataEditorPage().isStructureTreeFormVisible());
    }

    /**
     * Tests if process metadata lock is being removed when the user leaves the metadata editor
     * without clicking the close button.
     */
    @Test
    public void removeMetadataLockTest() throws Exception {
        // Open process in metadata editor by default user to set metadata lock for this process and user
        Pages.getProcessesPage().goTo().editMetadata();
        // Leave metadata editor without explicitly clicking the 'close' button
        Pages.getMetadataEditorPage().clickPortalLogo();
        // Try to open metadata editor with separate user to check whether metadata lock is still in place
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getByLogin("kowal"));
        Pages.getProcessesPage().goTo().editMetadata();
        Assert.assertEquals("Unable to open metadata editor that was not closed by 'close' button",
                "http://localhost:8080/kitodo/pages/metadataEditor.jsf?referer=processes&id=2",
                Browser.getCurrentUrl());
    }

    /**
     * Tests total number of scans.
     */
    @Test
    public void totalNumberOfScansTest() throws Exception {
        Pages.getProcessesPage().goTo().editMetadata();
        assertEquals("Total number of scans is not correct", "(Anzahl von Scans: 1)",
                Pages.getMetadataEditorPage().getNumberOfScans());
    }

    /**
     * Verifies that turning the "pagination panel switch" on in the user settings
     * results in pagination panel being displayed by default in the metadata editor.
     */
    @Test
    public void showPaginationByDefaultTest() throws Exception {
        Pages.getProcessesPage().goTo().editMetadata();
        assertFalse(Pages.getMetadataEditorPage().isPaginationPanelVisible());
        Pages.getUserEditPage().setPaginationToShowByDefault();
        Pages.getProcessesPage().goTo().editMetadata();
        assertTrue(Pages.getMetadataEditorPage().isPaginationPanelVisible());
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }
}

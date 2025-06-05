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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.LtpValidationConfigurationEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LtpValidationConfigurationST extends BaseTestSelenium {

    private static final String LTP_VALIDATION_CONFIGURATION_TABLE_ID = "ltpValidationConfigurationsTable";
    
    /**
     * Checks that there are two LTP validation configurations listed in the projects page tab.
     */
    @Test
    public void shouldListTwoLtpValidationConfigurationsTest() throws Exception {
        login("kowal");
        Pages.getProjectsPage().goTo().goToLtpValidationConfigurationsTab();

        // check table is displayed
        pollAssertTrue(() -> getLtpValidationConfigurationTable().isDisplayed());

        // check table contains two configurations
        String tableText = getLtpValidationConfigurationTable().getText();
        assertTrue(tableText.contains("Valid Tif"));
        assertTrue(tableText.contains("image/tiff"));
        assertTrue(tableText.contains("Wellformed Jpeg"));
        assertTrue(tableText.contains("image/jpeg"));

        // verify that table contains 3 rows (header + 2 configurations)
        assertEquals(3, getLtpValidationConfigurationTable().findElements(By.tagName("tr")).size());
    }

    /**
     * Checks that the first LTP validation configuration stored in the database contains correct information.
     */
    @Test
    public void firstLtpValidationConfigurationTest() throws Exception {
        login("kowal");
        Pages.getProjectsPage().goTo().goToLtpValidationConfigurationsTab();

        // click on edit button of first ltp validation configuration
        Browser.getDriver().findElementById("ltpValidationConfigurationsTable:0:actionForm:editLtpValidationConfiguration").click();
        pollAssertTrue(() -> Pages.getLtpValidationConfigurationEditPage().isDisplayed());

        // check details of first ltp validation configuration
        LtpValidationConfigurationEditPage editPage = Pages.getLtpValidationConfigurationEditPage();
        
        editPage.goToDetailsTab();
        pollAssertTrue(() -> editPage.isDetailsTabDisplayed());
        assertEquals("Valid Tif", editPage.getTitle());
        assertEquals("image/tiff", editPage.getMimeType());
        assertTrue(editPage.isRequireNoErrorToFinishTask());
        assertTrue(editPage.isRequireNoErrorToUploadImage());
        assertEquals("ERROR", editPage.getSimpleWellFormedSeverity());
        assertEquals("ERROR", editPage.getSimpleValidSeverity());
        
        editPage.goToAllConditionsTab();
        pollAssertTrue(() -> editPage.isAllConditionsTabDisplayed());
        assertEquals("wellformed", editPage.getConditionProperty(0));
        assertEquals("valid", editPage.getConditionProperty(1));
    }

    /**
     * Checks that a new LTP validation configuration can be created, all information is saved, and deletes the configuration again.
     */
    @Test 
    public void canAddNewLtpValidationConfigurationTest() throws Exception {
        login("kowal");

        // click on create new ltp validation configuration menu entry
        LtpValidationConfigurationEditPage editPage = Pages.getProjectsPage().goTo().createNewLtpValidationConfiguration();

        // verify form has default values
        editPage.goToDetailsTab();
        pollAssertTrue(() -> editPage.isDetailsTabDisplayed());
        assertTrue(editPage.getTitle().isEmpty());
        assertFalse(editPage.getMimeType().isEmpty());
        assertFalse(editPage.isRequireNoErrorToFinishTask());
        assertFalse(editPage.isRequireNoErrorToUploadImage());
        assertTrue(editPage.getSimpleWellFormedSeverity().isEmpty());
        assertTrue(editPage.getSimpleValidSeverity().isEmpty());

        // set new configuration details
        editPage.setMimeType("image/gif");
        editPage.setRequireNoErrorToFinishTask(true);
        editPage.setRequireNoErrorToUploadImage(false);
        editPage.setSimpleWellFormedSeverity("ERROR");
        editPage.setSimpleValidSeverity("WARNING");
        editPage.setSimpleFilenamePatternSeverity("ERROR");
        editPage.setFilenamePattern("*.\\.gif");
        editPage.setTitle("Wellformed Gif");

        // click save button
        editPage.save().goToLtpValidationConfigurationsTab();

        // verify that table contains 4 rows (header + 3 configurations)
        assertEquals(4, getLtpValidationConfigurationTable().findElements(By.tagName("tr")).size());
        
        // click edit button of newly created configuration
        Browser.getDriver().findElementById("ltpValidationConfigurationsTable:1:actionForm:editLtpValidationConfiguration").click();
        pollAssertTrue(() -> Pages.getLtpValidationConfigurationEditPage().isDisplayed());

        // verify that details tab is open
        editPage.goToDetailsTab();
        pollAssertTrue(() -> editPage.isDetailsTabDisplayed());

        // check that settings were saved correctly
        assertEquals("Wellformed Gif", editPage.getTitle());
        assertEquals("image/gif", editPage.getMimeType());
        assertTrue(editPage.isRequireNoErrorToFinishTask());
        assertFalse(editPage.isRequireNoErrorToUploadImage());
        assertEquals("ERROR", editPage.getSimpleWellFormedSeverity());
        assertEquals("WARNING", editPage.getSimpleValidSeverity());
        assertEquals("*.\\.gif", editPage.getFilenamePattern());
        assertEquals("ERROR", editPage.getSimpleFilenamePatternSeverity());

        // go to all conditions table
        editPage.goToAllConditionsTab();
        pollAssertTrue(() -> editPage.isAllConditionsTabDisplayed());

        // check that 3 conditions were created
        assertEquals("wellformed", editPage.getConditionProperty(0));
        assertEquals("valid", editPage.getConditionProperty(1));
        assertEquals("filename", editPage.getConditionProperty(2)); 

        // delete newly created ltp configuration
        Pages.getProjectsPage().goTo().goToLtpValidationConfigurationsTab();
        Browser.getDriver().findElementById("ltpValidationConfigurationsTable:1:actionForm:deleteLtpValidationConfiguration").click();
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id("deleteConfirmDialog_content")).getText().contains("Wellformed Gif"));
        Browser.getDriver().findElement(By.id("yesButton")).click();

        // verify that table contains 3 rows (header + 2 configurations) again
        Pages.getProjectsPage().goTo().goToLtpValidationConfigurationsTab();
        assertEquals(3, getLtpValidationConfigurationTable().findElements(By.tagName("tr")).size());
    }

    /**
     * Return the table the lists all LTP validation configurations on the projects page.
     */
    private WebElement getLtpValidationConfigurationTable() {
        return Browser.getDriver().findElementById(LTP_VALIDATION_CONFIGURATION_TABLE_ID);
    }

    /**
     * Checks a condition repeatedly.
     * 
     * @param conditionEvaluator the condition
     */
    private void pollAssertTrue(Callable<Boolean> conditionEvaluator) {
        Awaitility.await().ignoreExceptions()
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(conditionEvaluator);
    }

    /**
     * Performs login.
     * 
     * @param username the user name
     */
    private void login(String username) throws InstantiationException, IllegalAccessException, InterruptedException {
        User metadataUser = ServiceManager.getUserService().getByLogin(username);
        Pages.getLoginPage().goTo().performLogin(metadataUser);
    }

}

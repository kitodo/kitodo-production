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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.constants.StringConstants;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.MassImportPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MassImportST extends BaseTestSelenium {

    private static final String ID = "ID";
    private static final String TITLE = "Title";
    private static final String PLACE = "Place";
    private static final List<String> METADATA_KEYS  = Arrays.asList(ID, TITLE, PLACE);
    private static File csvUploadFile;
    private static MassImportPage massImportPage;
    private static final String CSV_UPLOAD_FILENAME = "test_import";
    private static final String CSV_UPLOAD_FILE_EXTENSION = ".csv";
    private static final String CSV_CELL_SELECTOR = "#editForm\\:recordsTable_data tr .ui-cell-editor-output";

    @BeforeClass
    public static void setup() throws Exception {
        massImportPage = Pages.getMassImportPage();
        csvUploadFile = createCsvFile();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
        Pages.getProjectsPage().goTo();
        Pages.getProjectsPage().clickMassImportAction();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @AfterClass
    public static void cleanup() throws IOException {
        deleteCsvFile();
    }

    /**
     * Tests whether uploaded CSV files in mass import form are parsed correctly or not.
     */
    @Test
    public void handleCsvFileUpload() throws InterruptedException {
        massImportPage.uploadTestCsvFile(csvUploadFile.getAbsolutePath());
        Thread.sleep(Browser.getDelayAfterLogout());
        List<WebElement> csvRows = Browser.getDriver().findElement(By.id("editForm:recordsTable_data"))
                .findElements(By.tagName("tr"));
        Assert.assertEquals("CSV file not parsed correctly", 3, csvRows.size());
        List<WebElement> csvCells = Browser.getDriver().findElements(By.cssSelector(CSV_CELL_SELECTOR));
        Assert.assertEquals("CSV lines should not be segmented correctly into multiple cells when using wrong CSV "
                + "separator", 3, csvCells.size());
        massImportPage.updateSeparator(StringConstants.COMMA_DELIMITER.trim());
        List<WebElement> updatedCsvCells = Browser.getDriver().findElements(By.cssSelector(CSV_CELL_SELECTOR));
        Assert.assertEquals("CSV lines should be segmented correctly into multiple cells when using correct CSV "
                + "separator", 9, updatedCsvCells.size());
    }

    private static File createCsvFile() throws IOException {
        File csvFile = File.createTempFile(CSV_UPLOAD_FILENAME, CSV_UPLOAD_FILE_EXTENSION);
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write(String.join(StringConstants.COMMA_DELIMITER, METADATA_KEYS) + "\n");
            writer.write("123, Band 1, Hamburg\n");
            writer.write("456, Band 2, Dresden\n");
            writer.write("789, Band 3, Berlin");
        }
        return csvFile;
    }

    private static void deleteCsvFile() throws IOException {
        boolean successfullyDeleted = csvUploadFile.delete();
        if (!successfullyDeleted) {
            throw new IOException(String.format("Error deleting CSV test file '%s'", csvUploadFile.getName()));
        }
    }

}

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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CalendarPage extends Page<CalendarPage> {

    private static final String BUTTON_CANCEL = "editForm:cancel";
    private static final String BUTTON_ADD_BLOCK = "editForm:calendarTabView:addBlock";
    private static final String DATEPICKER_FROM = "#editForm\\:calendarTabView\\:blockList td:first-child .p-datepicker input";
    private static final String DATEPICKER_TO_LINK = "(//div[@id='editForm:calendarTabView:blockList:0:blockLastAppearance_panel']//"
            + "a[text()='7'])[last()]";
    private static final String DATEPICKER_TO = "#editForm\\:calendarTabView\\:blockList td:last-child .p-datepicker input";
    private static final String BUTTON_ADD_ISSUE = "#editForm\\:calendarTabView\\:blockList button[title='Ausgabe hinzufügen']";
    private static final String INPUT_ISSUE = "#editForm\\:calendarTabView\\:blockList\\:0\\:issueList_data tr:last-child .ui-inputtext";
    private static final String HEADER_TEXT = "headerText";
    private static final String CHECKBOX_MONDAY = "#editForm\\:calendarTabView\\:blockList\\:0\\"
            + ":issueList_data tr:last-child td:nth-child(2) .ui-chkbox-icon";
    private static final String CHECKBOX_TUESDAY = "#editForm\\:calendarTabView\\:blockList\\:0\\"
            + ":issueList_data tr:last-child td:nth-child(3) .ui-chkbox-icon";
    private static final String CALENDAR_ENTRY = "(//tbody[@id='editForm:calendarTabView:calendarTable_data']//span[@title='erschien'])[1]";
    private static final String CALENDAR_ENTRY_BUTTON = "(//tbody[@id='editForm:calendarTabView:calendarTable_data']//"
            + "span[@title='erschien']/preceding-sibling::button/span)[1]";
    private static final String BUTTON_ADD_METADATA_TO_THIS = "calendarDayForm:issuesAccordion:0:addMetadataToThisIssue";
    private static final String BUTTON_ADD_METADATA_TO_ALL = "calendarDayForm:issuesAccordion:0:addMetadata";
    private static final String METADATA_TYPE = "calendarDayForm:issuesAccordion:0:metadataDataView:0:metadataType";
    private static final String METADATA_TYPE_PANEL = METADATA_TYPE + "_panel";
    private static final String METADATA_VALUE = "calendarDayForm:issuesAccordion:0:metadataDataView:0:startValue";
    private static final String CALENDAR_DIALOG_CLOSE_BUTTON = "calendarDayForm:close";
    private static final String CALENDAR = "editForm:calendarTabView:calendarTable";
    private static final String CALENDAR_ISSUES = ".issue.match";


    public CalendarPage() {
        super("pages/calendar.jsf");
    }

    @Override
    public CalendarPage goTo() {
        return null;
    }

    /**
     * Add a new block in the calendar and add the from and to dates.
     */
    public void addBlock() {
        await("Wait for button to be displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getById(BUTTON_ADD_BLOCK).isEnabled()));
        getById(BUTTON_ADD_BLOCK).click();

        await("Wait for datepicker from being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByCSS(DATEPICKER_FROM).isEnabled()));
        getByCSS(DATEPICKER_FROM).click();
        getByCSS(DATEPICKER_FROM).sendKeys("01.02.2023");
        getPageHeader().click();

        await("Wait for datepicker to being displayed")
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByXPath(DATEPICKER_TO_LINK).isEnabled()));
        getByCSS(DATEPICKER_TO).click();
        await("Wait for datepicker to being displayed")
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByXPath(DATEPICKER_TO_LINK).isEnabled()));
        getByXPath(DATEPICKER_TO_LINK).click();
    }

    /**
     * Add a new issue.
     * @param title name for the issue to be created
     */
    public void addIssue(String title) {
        await("Wait for issue button being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByCSS(BUTTON_ADD_ISSUE).isEnabled()));
        getByCSS(BUTTON_ADD_ISSUE).click();

        await("Wait for issue input being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByCSS(INPUT_ISSUE).isEnabled()));
        getByCSS(INPUT_ISSUE).click();
        getByCSS(INPUT_ISSUE).sendKeys(title);
        getById(HEADER_TEXT).click();

        await("Wait for checkbox being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByCSS(CHECKBOX_MONDAY).isDisplayed()));
        getByCSS(CHECKBOX_MONDAY).click();
        await("Wait for checkbox being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByCSS(CHECKBOX_MONDAY).isDisplayed()));
        getByCSS(CHECKBOX_TUESDAY).click();
    }

    /**
     * Add metadata to this issue. Type "Process title" and value "Test" will be inserted.
     */
    public void addMetadataToThis() {
        addMetadata("Process title", "Test", BUTTON_ADD_METADATA_TO_THIS);
    }

    /**
     * Add metadata to this and all following issues. Type "Signatur" and value "1234" will be used.
     */
    public void addMetadataToAll() {
        addMetadata("Signatur", "1234", BUTTON_ADD_METADATA_TO_ALL);
    }

    /**
     * Get all metadata types for the issue identified by its name.
     * @param issueName name of the issue that should be looked at
     * @return List of metadata types
     */
    public List<String> getMetadata(String issueName) {
        await("Wait for calendar entry being displayed")
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByXPath(CALENDAR_ENTRY).isDisplayed()));
        getByXPath(CALENDAR_ENTRY_BUTTON).click();

        await("Wait for issue '" + issueName + "' being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getIssue(issueName).isDisplayed()));
        if (Objects.equals(getIssue(issueName).getAttribute("aria-expanded"), "false")) {
            getByXPath("//div[@aria-expanded='true']").click();
            await("Wait for issue '" + issueName + "' being displayed")
                    .pollDelay(400, TimeUnit.MILLISECONDS)
                    .atMost(10, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .untilAsserted(() -> assertTrue(getIssue(issueName).isDisplayed()));
            getIssue(issueName).click();
        }

        await("Wait for issue content for '" + issueName + "' being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getIssueContent(issueName).isDisplayed()));

        List<String> metadataList = readMetadataTypes(issueName);
        getById(CALENDAR_DIALOG_CLOSE_BUTTON).click();

        return metadataList;
    }

    /**
     * Click cancel button and leave calendar.
     */
    public void closePage() {
        getById(BUTTON_CANCEL).click();
    }

    public int countIssues() {
        await("Wait for calendar issues to be displayed")
                .untilAsserted(() -> assertTrue(getById(CALENDAR).isDisplayed()));
        return getById(CALENDAR).findElements(By.cssSelector(CALENDAR_ISSUES)).size();
    }

    private void addMetadata(String type, String value, String addButton) {
        await("Wait for calendar entry being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getByXPath(CALENDAR_ENTRY).isDisplayed()));
        getByXPath(CALENDAR_ENTRY_BUTTON).click();
        await("Wait for button to add metadata to this issue being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getById(addButton).isEnabled()));
        getById(addButton).click();

        await("Wait for button to add metadata to this issue being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getById(METADATA_TYPE).isEnabled()));
        getById(METADATA_TYPE).click();
        getById(METADATA_TYPE_PANEL).findElement(By.xpath("//li[text()='" + type + "']")).click();

        await("Wait for metadata input being displayed")
                .pollDelay(400, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getById(METADATA_VALUE).isEnabled()));
        getById(METADATA_VALUE).sendKeys(value);
        getById(CALENDAR_DIALOG_CLOSE_BUTTON).click();
    }

    private WebElement getIssue(String name) {
        return getByXPath( "//div[text()='" + name + " erschien']");
    }

    private WebElement getIssueContent(String name) {
        return getByXPath("//div[text()='" + name + " erschien']/following-sibling::div");
    }

    private List<String> readMetadataTypes(String issueName) {
        List<WebElement> metadataTypeLabels = Browser.getDriver().findElementsByXPath("//div[text()='" + issueName
                + " erschien']/following-sibling::div[@aria-hidden='false']//div[@title='Art']/label");
        return metadataTypeLabels.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private WebElement getById(String id) {
        return Browser.getDriver().findElementById(id);
    }

    private WebElement getByCSS(String cssSelector) {
        return Browser.getDriver().findElementByCssSelector(cssSelector);
    }

    private WebElement getByXPath(String xpath) {
        return Browser.getDriver().findElementByXPath(xpath);
    }
}
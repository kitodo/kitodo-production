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
import org.openqa.selenium.support.FindBy;

public class CalendarPage extends Page<CalendarPage> {

    private static final String BUTTON_CANCEL = "editForm:cancel";
    private static final String BUTTON_ADD_BLOCK = "editForm:calendarTabView:addBlock";
    private static final String DATEPICKER_FROM = "#editForm\\:calendarTabView\\:blockList td:first-child .p-datepicker input";
    private static final String DATEPICKER_FROM_LINK = "//div[@id='editForm:calendarTabView:blockList:0:blockFirstAppearance_panel']//"
            + "a[text()='1']";
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


    @SuppressWarnings("unused")
    @FindBy(id = BUTTON_ADD_BLOCK)
    private WebElement buttonAddBlock;

    @SuppressWarnings("unused")
    @FindBy(id = BUTTON_CANCEL)
    private WebElement buttonCancel;

    @SuppressWarnings("unused")
    @FindBy(css = DATEPICKER_FROM)
    private WebElement datepickerFrom;

    @SuppressWarnings("unused")
    @FindBy(xpath = DATEPICKER_FROM_LINK)
    private WebElement datepickerFromLink;

    @SuppressWarnings("unused")
    @FindBy(css = DATEPICKER_TO)
    private WebElement datepickerTo;

    @SuppressWarnings("unused")
    @FindBy(xpath = DATEPICKER_TO_LINK)
    private WebElement datepickerToLink;

    @SuppressWarnings("unused")
    @FindBy(css = BUTTON_ADD_ISSUE)
    private WebElement buttonAddIssue;

    @SuppressWarnings("unused")
    @FindBy(css = INPUT_ISSUE)
    private WebElement inputIssueName;

    @SuppressWarnings("unused")
    @FindBy(id = HEADER_TEXT)
    private WebElement headerText;

    @SuppressWarnings("unused")
    @FindBy(css = CHECKBOX_MONDAY)
    private WebElement checkboxMonday;

    @SuppressWarnings("unused")
    @FindBy(css = CHECKBOX_TUESDAY)
    private WebElement checkboxTuesday;

    @SuppressWarnings("unused")
    @FindBy(xpath = CALENDAR_ENTRY)
    private WebElement calendarEntry;

    @SuppressWarnings("unused")
    @FindBy(xpath = CALENDAR_ENTRY_BUTTON)
    private WebElement calendarEntryButton;

    @SuppressWarnings("unused")
    @FindBy(id = BUTTON_ADD_METADATA_TO_THIS)
    private WebElement buttonAddMetadataToThis;

    @SuppressWarnings("unused")
    @FindBy(id = BUTTON_ADD_METADATA_TO_ALL)
    private WebElement buttonAddMetadataToAll;

    @SuppressWarnings("unused")
    @FindBy(id = METADATA_TYPE)
    private WebElement metadataType;

    @SuppressWarnings("unused")
    @FindBy(id = METADATA_TYPE_PANEL)
    private WebElement metadataTypePanel;

    @SuppressWarnings("unused")
    @FindBy(id = METADATA_VALUE)
    private WebElement metadataValue;

    @SuppressWarnings("unused")
    @FindBy(id = CALENDAR_DIALOG_CLOSE_BUTTON)
    private WebElement calendarDialogCloseButton;

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
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(buttonAddBlock.isEnabled()));
        buttonAddBlock.click();

        await("Wait for datepicker from being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(datepickerFrom.isEnabled()));
        datepickerFrom.click();
        datepickerFromLink.click();

        await("Wait for datepicker to being displayed")
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(datepickerToLink.isEnabled()));
        datepickerToLink.click();
    }

    /**
     * Add a new issue.
     * @param title name for the issue to be created
     */
    public void addIssue(String title) {
        await("Wait for issue button being displayed")
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(buttonAddIssue.isEnabled()));
        buttonAddIssue.click();

        await("Wait for issue input being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(inputIssueName.isEnabled()));
        inputIssueName.click();
        inputIssueName.sendKeys(title);
        headerText.click();

        await("Wait for checkbox being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(checkboxMonday.isDisplayed()));
        checkboxMonday.click();
        await("Wait for checkbox being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(checkboxMonday.isDisplayed()));
        checkboxTuesday.click();
    }

    /**
     * Add metadata to this issue. Type "Process title" and value "Test" will be inserted.
     */
    public void addMetadataToThis() {
        addMetadata("Process title", "Test", buttonAddMetadataToThis);
    }

    /**
     * Add metadata to this and all following issues. Type "Signatur" and value "1234" will be used.
     */
    public void addMetadataToAll() {
        addMetadata("Signatur", "1234", buttonAddMetadataToAll);
    }

    /**
     * Get all metadata types for the issue identified by its name.
     * @param issueName name of the issue that should be looked at
     * @return List of metadata types
     */
    public List<String> getMetadata(String issueName) {
        await("Wait for calendar entry being displayed")
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue( calendarEntry.isDisplayed()));
        calendarEntryButton.click();

        await("Wait for issue '" + issueName + "' being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getIssue(issueName).isDisplayed()));
        if (Objects.equals(getIssue(issueName).getAttribute("aria-expanded"), "false")) {
            Browser.getDriver().findElementByXPath("//div[@aria-expanded='true']").click();
            await("Wait for issue '" + issueName + "' being displayed")
                    .pollDelay(700, TimeUnit.MILLISECONDS)
                    .atMost(10, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .untilAsserted(() -> assertTrue(getIssue(issueName).isDisplayed()));
            getIssue(issueName).click();
        }

        await("Wait for issue content for '" + issueName + "' being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(getIssueContent(issueName).isDisplayed()));

        List<String> metadataList = readMetadataTypes(issueName);
        calendarDialogCloseButton.click();

        return metadataList;
    }

    /**
     * Click cancel button and leave calendar.
     */
    public void closePage() {
        buttonCancel.click();
    }

    private void addMetadata(String type, String value, WebElement addButton) {
        await("Wait for calendar entry being displayed")
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue( calendarEntry.isDisplayed()));
        calendarEntryButton.click();
        await("Wait for button to add metadata to this issue being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(addButton.isEnabled()));
        addButton.click();

        await("Wait for button to add metadata to this issue being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(metadataType.isEnabled()));
        metadataType.click();
        metadataTypePanel.findElement(By.xpath("//li[text()='" + type + "']")).click();

        await("Wait for metadata input being displayed")
                .pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> assertTrue(metadataValue.isEnabled()));
        metadataValue.sendKeys(value);
        calendarDialogCloseButton.click();
    }
    
    private WebElement getIssue(String name) {
        return Browser.getDriver().findElementByXPath( "//div[text()='" + name + " erschien']");
    }

    private WebElement getIssueContent(String name) {
        return Browser.getDriver().findElementByXPath("//div[text()='" + name + " erschien']/following-sibling::div");
    }

    private List<String> readMetadataTypes(String issueName) {
        List<WebElement> metadataTypeLabels = Browser.getDriver().findElementsByXPath("//div[text()='" + issueName
                + " erschien']/following-sibling::div[@aria-hidden='false']//div[@title='Art']/label");
        return metadataTypeLabels.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}

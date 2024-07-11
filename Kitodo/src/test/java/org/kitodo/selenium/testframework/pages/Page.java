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
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Page<T> {

    private static final Logger logger = LogManager.getLogger(Page.class);
    static final String DATA = "_data";
    static final String CSS_SELECTOR_DROPDOWN_TRIGGER =  ".ui-selectonemenu-trigger";
    static final String WAIT_FOR_FILTER_FORM_MENU = "Wait for filter form menu to open";

    @SuppressWarnings("unused")
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-growl-item-container")
    private WebElement errorPopup;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-messages-error-summary")
    private WebElement errorMessage;

    @SuppressWarnings("unused")
    @FindBy(id = "yesButton")
    private WebElement confirmRemoveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "noButton")
    WebElement cancelRemoveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "headerText")
    private WebElement header;

    @SuppressWarnings("unused")
    @FindBy(id = "search-form:search-field")
    private WebElement searchField;

    @SuppressWarnings("unused")
    @FindBy(id = "search-form:search")
    private WebElement searchButton;

    private String URL;

    Page(String URL) {
        this.URL = URL;
    }

    /**
     * Get page URL.
     *
     * @return page URL
     */
    public String getUrl() {
        return URL;
    }

    abstract public T goTo() throws Exception;

    /**
     * Check if the browser is currently at given page.
     *
     * @return true if browser is at given page
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains(URL);
    }

    public boolean isResultSetNotEmpty(List<String> recordTitles) {
        if (recordTitles.isEmpty()) {
            return false;
        } else {
            if (recordTitles.size() == 1) {
                return !recordTitles.get(0).equals("No records found.");
            } else {
                return true;
            }
        }
    }

    /**
     * Check if the browser is currently not at given page.
     *
     * @return true if browser is not at given page
     */
    boolean isNotAt() {
        return !isAt();
    }

    /**
     * Find row matching to give table, click toggle row and return index of
     * found row.
     *
     * @param dataTable
     *            table for search
     * @param objectTitle
     *            searched row
     * @return index of found row
     */
    int triggerRowToggle(WebElement dataTable, String objectTitle) {
        List<WebElement> tableRows = getRowsOfTable(dataTable);

        int index = getRowIndex(dataTable, objectTitle, 1);
        WebElement tableRow = tableRows.get(index);
        tableRow.findElement(By.className("ui-row-toggler")).click();

        return index;
    }

    /**
     * Find row in table matching to give title, return index of found row.
     *
     * @param dataTable
     *            table for search
     * @param objectTitle
     *            searched row
     * @param columnIndex
     *            index of cell used for identification
     * @return index of found row
     */
    int getRowIndex(WebElement dataTable, String objectTitle, int columnIndex) {
        List<WebElement> tableRows = getRowsOfTable(dataTable);

        for (int i = 0; i < tableRows.size(); i++) {
            WebElement tableRow = tableRows.get(i);
            if (Browser.getCellDataByRow(tableRow, columnIndex).equals(objectTitle)) {
                return i;
            }
        }

        throw new NotFoundException("Row for title " + objectTitle + " was not found!");
    }

    @SuppressWarnings("unchecked")
    T switchToTabByIndex(int index, WebElement tabView) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickTab(index, tabView);
        return (T) this;
    }

    private void clickTab(int index, WebElement tabView) {
        List<WebElement> listTabs = tabView.findElements(By.tagName("li"));
        WebElement tab = listTabs.get(index);
        await("Wait for tab to be visible").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> tab.isDisplayed());
        tab.click();
    }

    /**
     * Clicks a button which could be be stale, e.g. because of disabling and
     * enabling via Ajax. After click was performed, the browser waits for
     * redirecting to given url.
     *
     * @param button
     *            the button to be clicked
     * @param url
     *            the url to which is redirected after click
     */
    protected void clickButtonAndWaitForRedirect(WebElement button, String url) {
        WebDriverWait webDriverWait = new WebDriverWait(Browser.getDriver(), 60);
        for (int attempt = 1; attempt < 4; attempt++) {
            try {
                await("Wait for button clicked").pollDelay(700, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS)
                        .ignoreExceptions().until(() -> isButtonClicked.test(button));
                if (Browser.isAlertPresent() && url.contains("login")) {
                    Browser.getDriver().switchTo().alert().accept();
                }
                webDriverWait.until(ExpectedConditions.urlContains(url));
                return;
            } catch (TimeoutException e) {
                logger.error(
                    "Clicking on button with id " + button.getAttribute("id") + " was not successful. Retrying now.");
            }
        }
        throw new TimeoutException("Could not access button: " + button.getAttribute("id") + "!");
    }

    protected void clickElement(WebElement element) {
        await("Wait for element " + element.getText() + ", " + element.getTagName() + " to be clicked").pollDelay(500, TimeUnit.MILLISECONDS).atMost(20, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.test(element));
    }

    /**
     * Get header text.
     *
     * @return value of header text
     */
    public String getHeaderText() {
        return getWebElementText(header);
    }

    /**
     * Get web element text.
     *
     * @param webElement
     *            web element
     * @return text value of web element
     */
    private String getWebElementText(WebElement webElement) {
        return webElement.getText();
    }

    Predicate<WebElement> isButtonClicked = (webElement) -> {
        webElement.click();
        return true;
    };

    Predicate<WebElement> isInputValueNotEmpty = (webElement) -> {
        return !webElement.getAttribute("value").isEmpty();
    };

    Predicate<File> isFileDownloaded = (file) -> {
        return file.exists();
    };

    Predicate<WebElement> isDisplayed = (webElement) -> {
        return webElement.isDisplayed();
    };

    void deleteElement(String objectType, int removableID, int tabIndex, WebElement tabView) throws Exception {
        if (!isAt()) {
            goTo();
        }
        switchToTabByIndex(tabIndex, tabView);
        Browser.getDriver()
                .findElement(By.xpath("//a[@href='/kitodo/pages/" + objectType.toLowerCase() + "Edit.jsf?id="
                        + removableID + "']/following-sibling::a[@id[contains(., 'delete" + objectType + "')]]"))
                .click();
        await("Wait for 'confirm delete' dialog to be displayed")
                .atMost(Browser.getDelayAfterDelete(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(() -> confirmRemoveButton.isDisplayed());
        confirmRemoveButton.click();
        Thread.sleep(Browser.getDelayAfterDelete());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60);
        wait.until(ExpectedConditions.urlContains(getUrl()));
    }

    /**
     * Searches in the header-searchfield with the given query.
     * @param query the input to search for.
     * @throws Exception if Page could not be instantiated.
     */
    public void searchInSearchField(String query) throws Exception {
        searchField.clear();
        searchField.sendKeys(query);
        clickButtonAndWaitForRedirect(searchButton, Pages.getSearchResultPage().getUrl());
    }
}

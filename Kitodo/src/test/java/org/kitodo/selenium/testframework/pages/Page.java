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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.Predicate;
import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Page<T> {

    @SuppressWarnings("unused")
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-growl-item-container")
    private WebElement errorPopup;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-messages-error-summary")
    private WebElement errorMessage;

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
        await("Wait for save button clicked").pollDelay(500, TimeUnit.MILLISECONDS).atMost(40, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.matches(button));
        new WebDriverWait(Browser.getDriver(), 60).until(ExpectedConditions.urlContains(url));
    }

    Predicate<WebElement> isButtonClicked = (webElement) -> {
        webElement.click();
        return true;
    };

    Predicate<WebElement> isInputValueNotEmpty = (webElement) -> {
        return !webElement.getAttribute("value").equals("");
    };
}

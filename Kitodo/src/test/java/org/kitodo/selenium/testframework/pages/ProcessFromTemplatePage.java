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

import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProcessFromTemplatePage extends EditPage<ProcessFromTemplatePage> {

    private static final String TAB_VIEW = EDIT_FORM + ":processFromTemplateTabView";
    private static final String OPAC_SEARCH_FORM = "catalogSearchForm";
    private static final String CSS_SELECTOR_DROPDOWN_TRIGGER =  ".ui-selectonemenu-trigger";

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW)
    private WebElement processFromTemplateTabView;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":processTitle")
    private WebElement processTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":guessImages")
    private WebElement guessImagesInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":logicalStructure")
    private WebElement logicalStructureTree;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":docType")
    private WebElement docTypeSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":metadataTable:0:inputText")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":metadataTable:1:inputText")
    private WebElement titleSortInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":metadataTable:2:inputText")
    private WebElement ppnAnalogInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":metadataTable:3:inputText")
    private WebElement ppnDigitalInput;

    @SuppressWarnings("unused")
    @FindBy(id = OPAC_SEARCH_FORM + ":catalogueSelectMenu")
    private WebElement catalogSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":chooseParent")
    private WebElement chooseParentSelect;

    @SuppressWarnings("unused")
    @FindBy(id = OPAC_SEARCH_FORM + ":fieldSelectMenu")
    private WebElement fieldSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":searchForParent")
    private WebElement searchForParentInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":searchParent")
    private WebElement searchParentButton;

    @SuppressWarnings("unused")
    @FindBy(id = OPAC_SEARCH_FORM + ":searchTerm")
    private WebElement searchTermInput;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":performCatalogSearch")
    private WebElement performCatalogSearchButton;

    @SuppressWarnings("unused")
    @FindBy(id = "hitlistDialogForm:hitlistDialogTable:0:selectRecord")
    private WebElement selectRecord;

    @SuppressWarnings("unused")
    @FindBy(id = TAB_VIEW + ":generateTitleButton")
    private WebElement generateTitleButton;

    @FindBy(id = "catalogSearchForm:cancel")
    private WebElement cancelCatalogSearchButton;

    public ProcessFromTemplatePage() {
        super("pages/processFromTemplate.jsf");
    }

    @Override
    public ProcessFromTemplatePage goTo() {
        return null;
    }

    public WebElement getCatalogMenu() {
        return Browser.getDriver().findElementById(OPAC_SEARCH_FORM + ":catalogueSelectMenu_input");
    }

    public WebElement getSearchFieldMenu() {
        return Browser.getDriver().findElementById(OPAC_SEARCH_FORM + ":fieldSelectMenu_input");
    }

    /**
     * Select GBV catalog.
     * @throws InterruptedException when thread is interrupted
     */
    public void selectGBV() throws InterruptedException {
        clickElement(catalogSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(catalogSelect.getAttribute("id") + "_1")));
        Thread.sleep(Browser.getDelayAfterCatalogSelection());
    }

    public String createProcess() throws Exception {
        clickElement(cancelCatalogSearchButton);
        await("Wait for OPAC search dialog to disappear").pollDelay(150, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)).isEnabled());
        clickElement(docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_2")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        switchToTabByIndex(1);
        titleInput.sendKeys("TestProcess");
        titleSortInput.sendKeys("TestProcess");
        ppnAnalogInput.sendKeys("12345");
        ppnDigitalInput.sendKeys("12345");

        switchToTabByIndex(0);
        guessImagesInput.sendKeys("299");
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        String generatedTitle = processTitleInput.getAttribute("value");
        save();
        return generatedTitle;
    }

    /**
     * Creates a process as child.
     *
     * @param parentProcessTitle
     *            parent process title
     * @return generated title
     */
    public String createProcessAsChild(String parentProcessTitle) throws Exception {
        clickElement(cancelCatalogSearchButton);
        await("Wait for OPAC search dialog to disappear").pollDelay(150, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)).isEnabled());
        clickElement(docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_2")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        switchToTabByIndex(1);
        titleInput.sendKeys("TestProcessChild");
        titleSortInput.sendKeys("TestProcessChild");
        ppnAnalogInput.sendKeys("123456");
        ppnDigitalInput.sendKeys("123456");

        switchToTabByIndex(0);
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        final String generatedTitle = processTitleInput.getAttribute("value");

        switchToTabByIndex(3);
        searchForParentInput.sendKeys(parentProcessTitle);
        searchParentButton.click();
        await("Wait for search").pollDelay(500, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(chooseParentSelect));
        clickElement(chooseParentSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(chooseParentSelect.getAttribute("id") + "_1")));
        await("Wait for tree shows").pollDelay(500, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isDisplayed.test(logicalStructureTree));
        save();
        return generatedTitle;
    }

    /**
     * Tries to create a process as child which is not possible.
     *
     * @return whether an error message is showing
     */
    public boolean createProcessAsChildNotPossible() throws Exception {
        clickElement(cancelCatalogSearchButton);
        await("Wait for OPAC search dialog to disappear").pollDelay(150, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)).isEnabled());
        clickElement(docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_0")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        switchToTabByIndex(1);
        titleInput.sendKeys("TestProcessChildNotPossible");
        titleSortInput.sendKeys("TestProcessChildNotPossible");
        ppnAnalogInput.sendKeys("1234567");
        ppnDigitalInput.sendKeys("1234567");

        switchToTabByIndex(0);
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));

        switchToTabByIndex(3);
        searchForParentInput.sendKeys("Second");
        searchParentButton.click();
        await("Wait for search").pollDelay(500, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(chooseParentSelect));
        clickElement(chooseParentSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(chooseParentSelect.getAttribute("id") + "_1")));
        try {
            await("Wait for error message").pollDelay(100, TimeUnit.MILLISECONDS).atMost(4, TimeUnit.SECONDS)
                    .ignoreExceptions().until(() -> isDisplayed.test(errorMessages));
            return true;
        } catch (ConditionTimeoutException e) {
            return false;
        }
    }

    public String createProcessFromCatalog() throws Exception {
        clickElement(catalogSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(catalogSelect.getAttribute("id") + "_2")));

        clickElement(fieldSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(fieldSelect.getAttribute("id") + "_1")));

        searchTermInput.sendKeys("test");
        performCatalogSearchButton.click();
        selectRecord.click();

        Thread.sleep(Browser.getDelayAfterPickListClick());
        titleSortInput.sendKeys("Test");
        ppnAnalogInput.sendKeys("12345");

        switchToTabByIndex(1);
        guessImagesInput.sendKeys("299");
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        String generatedTitle = processTitleInput.getAttribute("value");
        save();
        return generatedTitle;
    }

    public ProcessesPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProcessesPage().getUrl());
        return Pages.getProcessesPage();
    }

    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, processFromTemplateTabView);
    }

    public void cancel() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(cancelButton, Pages.getProjectsPage().getUrl());
        Pages.getProcessesPage();
    }
}

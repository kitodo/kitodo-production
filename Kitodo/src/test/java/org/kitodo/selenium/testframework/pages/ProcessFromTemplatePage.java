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
import java.util.stream.Collectors;

import org.awaitility.core.ConditionTimeoutException;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.test.utils.TestConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProcessFromTemplatePage extends EditPage<ProcessFromTemplatePage> {

    private static final String TAB_VIEW = EDIT_FORM + ":processFromTemplateTabView";
    private static final String OPAC_SEARCH_FORM = "catalogSearchForm";
    private static final String HIERARCHY_PANEL = "editForm:processFromTemplateTabView:processHierarchyContent";
    private static final String IMPORT_CHILD_PROCESSES_SWITCH = "#catalogSearchForm\\:importChildren .ui-chkbox-box";

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
    @FindBy(id = OPAC_SEARCH_FORM + ":performCatalogSearch")
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
     * Get 'Search' button.
     * @return 'Search' button as WebElement.
     */
    public WebElement getSearchButton() {
        return Browser.getDriver().findElementById(OPAC_SEARCH_FORM + ":performCatalogSearch");
    }

    /**
     * Get template process menu.
     * @return template process menu
     */
    public WebElement getTemplateProcessMenu() {
        return Browser.getDriver().findElementById("searchEditForm:processSelect_input");
    }

    private void selectCatalog(String catalogName) throws InterruptedException {
        clickElement(catalogSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("li[data-label='" + catalogName + "']")));
        Thread.sleep(Browser.getDelayAfterCatalogSelection());
    }

    /**
     * Select GBV catalog.
     * @throws InterruptedException when thread is interrupted
     */
    public void selectGBV() throws InterruptedException {
        selectCatalog(TestConstants.GBV);
    }

    /**
     * Select Kalliope catalog.
     * @throws InterruptedException when thread is interrupted
     */
    public void selectKalliope() throws InterruptedException {
        selectCatalog(TestConstants.KALLIOPE);
    }

    /**
     * Get list of ImportConfiguration titles.
     * @return list of ImportConfiguration titles
     */
    public List<String> getImportConfigurationsTitles() {
        clickElement(catalogSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        WebElement selectMenuItems = Browser.getDriver().findElement(By.id("catalogSearchForm:catalogueSelectMenu_items"));
        return selectMenuItems.findElements(By.className("ui-selectonemenu-list-item"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public String createProcess() throws Exception {
        clickElement(cancelCatalogSearchButton);
        await("Wait for OPAC search dialog to disappear").pollDelay(150, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)).isEnabled());
        clickElement(docTypeSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_3")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        titleInput.sendKeys("TestProcess");
        titleSortInput.sendKeys("TestProcess");
        ppnAnalogInput.sendKeys("12345");
        ppnDigitalInput.sendKeys("12345");

        guessImagesInput.sendKeys("299");
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        String generatedTitle = processTitleInput.getAttribute(TestConstants.VALUE);
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
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_3")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        titleInput.sendKeys("TestProcessChild");
        titleSortInput.sendKeys("TestProcessChild");
        ppnAnalogInput.sendKeys("123456");
        ppnDigitalInput.sendKeys("123456");

        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        final String generatedTitle = processTitleInput.getAttribute(TestConstants.VALUE);

        switchToTabByIndex(1);
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
        clickElement(Browser.getDriver().findElement(By.id(docTypeSelect.getAttribute("id") + "_1")));
        await("Page ready").pollDelay(150, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isDisplayed.test(processFromTemplateTabView));

        titleInput.sendKeys("TestProcessChildNotPossible");
        titleSortInput.sendKeys("TestProcessChildNotPossible");
        ppnAnalogInput.sendKeys("1234567");
        ppnDigitalInput.sendKeys("1234567");

        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));

        switchToTabByIndex(1);
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

    /**
     * Tests importing metadata from a simulated OPAC and saving it as a process in Kitodo.Production.
     * @return title of the created process as String.
     * @throws Exception when saving the imported process fails.
     */
    public String createProcessFromCatalog() throws Exception {
        clickElement(catalogSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("li[data-label='K10Plus']")));
        clickElement(fieldSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("li[data-label='PPN']")));
        await("Wait for 'searchInput' field to become active").pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> searchTermInput.isEnabled());
        searchTermInput.sendKeys("test");
        await("Wait for 'performSearch' button to become active").pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> performCatalogSearchButton.isEnabled());
        performCatalogSearchButton.click();
        await("Wait for popup dialog and loading screen to disappear").pollDelay(1, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> !Browser.getDriver()
                        .findElement(By.id("loadingScreen")).isDisplayed());
        titleSortInput.sendKeys("Test");
        ppnAnalogInput.sendKeys("12345");
        ppnDigitalInput.sendKeys("67890");
        guessImagesInput.sendKeys("299");
        generateTitleButton.click();
        await("Wait for title generation").pollDelay(3, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isInputValueNotEmpty.test(processTitleInput));
        String generatedTitle = processTitleInput.getAttribute(TestConstants.VALUE);
        save();
        return generatedTitle;
    }

    /**
     * Get process title from corresponding "CreateProcessForm" input field.
     * @return process title input field value
     */
    public String getProcessTitle() {
        return processTitleInput.getAttribute(TestConstants.VALUE);
    }

    /**
     * Check and return whether hierarchy panel is visible or not after triggering catalog import.
     * @return whether hierarchy panel is visible
     */
    public boolean isHierarchyPanelVisible() {
        WebElement hierarchyPanel = Browser.getDriver().findElement(By.id(HIERARCHY_PANEL));
        return hierarchyPanel.isDisplayed();
    }

    /**
     * Activate automatic import of child records by clicking on the switch labeled "Import child processes"
     */
    public void activateChildProcessImport() {
        // activate child process search
        WebElement childProcessSwitch = Browser.getDriver().findElement(By.cssSelector(IMPORT_CHILD_PROCESSES_SWITCH));
        await("Wait for 'childProcessImport' switch to become active").pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(childProcessSwitch::isEnabled);
        clickElement(childProcessSwitch);
    }

    /**
     * Decrease the import depth for the catalog by clicking on the chevron-down arrow in the corresponding input field.
     * @throws InterruptedException when putting the thread to sleep fails
     */
    public void decreaseImportDepth() throws InterruptedException {
        WebElement spinnerDown = Browser.getDriver().findElement(By.cssSelector("#catalogSearchForm .ui-spinner-down"));
        spinnerDown.click();
        Thread.sleep(Browser.getDelayAfterCatalogSelection());
    }

    /**
     * Enter test value into search term field.
     */
    public void enterTestSearchValue(String searchTerm) {
        searchTermInput.sendKeys(searchTerm);
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

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

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ImportConfigurationEditPage extends EditPage<ImportConfigurationEditPage> {

    private static final String IMPORT_CONFIGURATION_TAB_VIEW = EDIT_FORM + ":importConfigurationTabView";
    private static final String CONFIGURATION_TYPE_MENU = IMPORT_CONFIGURATION_TAB_VIEW + ":configurationType";
    private static final String INTERFACE_TYPE_MENU = IMPORT_CONFIGURATION_TAB_VIEW + ":interfaceType";
    private static final String ID_SEARCH_FIELD_MENU = IMPORT_CONFIGURATION_TAB_VIEW + ":idSearchField";
    private static final String MAPPING_FILES = IMPORT_CONFIGURATION_TAB_VIEW + ":mappingFiles";

    @FindBy(id = IMPORT_CONFIGURATION_TAB_VIEW)
    private WebElement importConfigurationsTabView;

    @FindBy(id = IMPORT_CONFIGURATION_TAB_VIEW + ":title")
    private WebElement titleInput;

    @FindBy(id = CONFIGURATION_TYPE_MENU)
    private WebElement configurationTypeMenu;

    @FindBy(id = IMPORT_CONFIGURATION_TAB_VIEW + ":host")
    private WebElement hostInput;

    @FindBy(id = INTERFACE_TYPE_MENU)
    private WebElement interfaceTypeMenu;

    @FindBy(id = IMPORT_CONFIGURATION_TAB_VIEW + ":addUrlParameter")
    private WebElement addUrlParameterButton;

    @FindBy(id = "addUrlParameterForm:urlParameterKey")
    private WebElement parameterKey;

    @FindBy(id = "addUrlParameterForm:urlParameterValue")
    private WebElement parameterValue;

    @FindBy(id = "addUrlParameterForm:apply")
    private WebElement applyAddingUrlParameterButton;

    @FindBy(id = IMPORT_CONFIGURATION_TAB_VIEW + ":addSearchField")
    private WebElement addSearchFieldButton;

    @FindBy(id = "addSearchFieldForm:searchFieldLabel")
    private WebElement searchFieldLabel;

    @FindBy(id = "addSearchFieldForm:searchFieldValue")
    private WebElement searchFieldValue;

    @FindBy(id = "addSearchFieldForm:apply")
    private WebElement applyAddingSearchFieldButton;

    @FindBy(id = ID_SEARCH_FIELD_MENU)
    private WebElement searchFieldMenu;

    @FindBy(id = MAPPING_FILES)
    private WebElement mappingFiles;

    public ImportConfigurationEditPage() {
        super("importConfigurationEdit.jsf");
    }

    @Override
    public ImportConfigurationEditPage goTo() throws Exception {
        return null;
    }

    /**
     * Insert mandatory data to create import configuration with custom URL parameters.
     * @throws Exception when switching to mapping files tab fails
     */
    public void insertImportConfigurationDataWithUrlParameters() throws Exception {
        titleInput.sendKeys("Custom configuration");
        selectCatalogSearch();
        hostInput.sendKeys("localhost");
        selectCustomInterfaceType();
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> addUrlParameterButton.isEnabled());
        addUrlParameterButton.click();
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> applyAddingUrlParameterButton.isEnabled());
        parameterKey.sendKeys("testkey");
        parameterValue.sendKeys("testvalue");
        applyAddingUrlParameterButton.click();
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> addSearchFieldButton.isEnabled());
        addSearchFieldButton.click();
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> applyAddingSearchFieldButton.isEnabled());
        searchFieldLabel.sendKeys("testsearchfieldlabel");
        searchFieldValue.sendKeys("testsearchfieldvalue");
        applyAddingSearchFieldButton.click();
        selectIdSearchField();
        switchToTabByIndex(TabIndex.IMPORT_CONFIGURATION_MAPPING_FILES.getIndex());
        assignMappingFile();
    }

    public void save() throws InstantiationException, IllegalAccessException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
    }

    private void selectCatalogSearch() {
        clickElement(configurationTypeMenu.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(configurationTypeMenu.getAttribute("id") + "_1")));
    }

    private void selectCustomInterfaceType() {
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(4, TimeUnit.SECONDS)
                .until(() -> interfaceTypeMenu.isEnabled());
        clickElement(interfaceTypeMenu.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(interfaceTypeMenu.getAttribute("id") + "_4")));
    }

    private void selectIdSearchField() {
        await().ignoreExceptions()
                .pollDelay(1000, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> searchFieldMenu.isEnabled());
        clickElement(searchFieldMenu.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(searchFieldMenu.getAttribute("id") + "_1")));
    }

    private void assignMappingFile() {
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> mappingFiles.isDisplayed());
        WebElement sourcePickList = Browser.getDriver().findElement(By.className("ui-picklist-source"));
        sourcePickList.findElements(By.cssSelector("li.ui-picklist-item")).get(0).click();
        WebElement addMappingFileButton = Browser.getDriver().findElement(By.className("ui-picklist-button-add"));
        addMappingFileButton.click();
        await().ignoreExceptions()
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> Browser.getDriver().findElement(By.cssSelector(".ui-picklist-target li")).isDisplayed());
    }

    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, importConfigurationsTabView);
    }

}

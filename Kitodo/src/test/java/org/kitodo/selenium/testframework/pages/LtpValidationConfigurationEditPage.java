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

import java.util.List;
import java.util.stream.IntStream;

import org.awaitility.Awaitility;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class LtpValidationConfigurationEditPage extends EditPage<LtpValidationConfigurationEditPage> {

    private static final String TABVIEW = "editForm:ltpValidationConfigurationTabView";

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW)
    private WebElement tabView;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":detailsTab")
    private WebElement detailsTab;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":conditionsTab")
    private WebElement allConditionsTab;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":mimeType")
    private WebElement mimeTypeDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":mimeType_input")
    private WebElement mimeTypeSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":mimeType_label")
    private WebElement mimeTypeLabel;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":requireNoErrorToFinishTask_input")
    private WebElement requireNoErrorToFinishTaskCheckBox;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":requireNoErrorToFinishTask")
    private WebElement requireNoErrorToFinishTaskButton;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":requireNoErrorToUploadImage_input")
    private WebElement requireNoErrorToUploadImageCheckBox;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":requireNoErrorToUploadImage")
    private WebElement requireNoErrorToUploadImageButton;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleWellFormed")
    private WebElement simpleWellFormedDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleWellFormed_input")
    private WebElement simpleWellFormedSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleValid")
    private WebElement simpleValidDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleValid_input")
    private WebElement simpleValidSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleFilenamePattern")
    private WebElement simpleFilenamePatternInput;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleFilenamePatternSeverity")
    private WebElement simpleFilenamePatternSeverityDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":simpleFilenamePatternSeverity_input")
    private WebElement simpleFilenamePatternSeveritySelect;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":ltpValidationConditionsTable")
    private WebElement allConditionsTable;

    @SuppressWarnings("unused")
    @FindBy(id = TABVIEW + ":addConditionButton")
    private WebElement addConditionButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveButton;

    /**
     * Initialize edit page for LTP validation configurations.
     */
    public LtpValidationConfigurationEditPage() {
        super("pages/ltpValidationConfigurationEdit.jsf");
    }

    /**
     * Not used
     */
    @Override
    public LtpValidationConfigurationEditPage goTo() throws Exception {
        return null;
    }

    /**
     * Returns true if edit page is displayed.
     * 
     * @return true if edit page is displayed
     */
    public boolean isDisplayed() throws Exception {
        return tabView.isDisplayed();
    }

    /**
     * Returns true if details tab is displayed.
     * 
     * @return true if details tab is displayed
     */
    public boolean isDetailsTabDisplayed() throws Exception {
        return detailsTab.isDisplayed();
    }

    /**
     * Return true if all conditions tab is displayed.
     * 
     * @return true if all conditions tab is displayed
     */
    public boolean isAllConditionsTabDisplayed() throws Exception {
        return allConditionsTab.isDisplayed();
    }

    /**
     * Navigate to details tab.
     */
    public void goToDetailsTab() throws Exception {
        switchToTabByIndex(0, tabView);
        Awaitility.await().until(() -> isDetailsTabDisplayed());
    }

    /**
     * Navigate to all conditions tab
     */
    public void goToAllConditionsTab() throws Exception {
        switchToTabByIndex(1, tabView);
        Awaitility.await().until(() -> isAllConditionsTabDisplayed());
    }

    /**
     * Return the title of the LTP validation configuration.
     * 
     * @return the title
     */
    public String getTitle() throws Exception {
        return titleInput.getAttribute("value");
    }

    /**
     * Set the title of the LTP validation configuration.
     * 
     * @param title
     *            the title
     */
    public void setTitle(String title) throws Exception {
        titleInput.clear();
        titleInput.sendKeys(title);
        Awaitility.await().until(() -> getTitle().equals(title));
    }

    /**
     * Return the currently selected mime type value (not label).
     * 
     * @return the currrently selected mime type value (not label)
     */
    public String getMimeType() throws Exception {
        return new Select(mimeTypeSelect).getFirstSelectedOption().getAttribute("value");
    }

    /**
     * Set the mime type based on an option value (not label).
     * 
     * @param mimeType
     *            the option value of the mime type to be selected
     */
    public void setMimeType(String mimeType) throws Exception {
        setDropDownValue(mimeTypeDropDown, mimeType);
    }

    /**
     * Return true if the checkbox "require no errors to finish task" is
     * checked.
     * 
     * @return true if checked
     */
    public boolean isRequireNoErrorToFinishTask() throws Exception {
        return requireNoErrorToFinishTaskCheckBox.isSelected();
    }

    /**
     * Set the checkbox "require no errors to finish task".
     * 
     * @param required
     *            true if checkbox should be checked
     */
    public void setRequireNoErrorToFinishTask(boolean required) throws Exception {
        if (isRequireNoErrorToFinishTask() != required) {
            requireNoErrorToFinishTaskButton.findElement(By.className("ui-chkbox-box")).click();
            Awaitility.await().until(() -> isRequireNoErrorToFinishTask() == required);
        }
    }

    /**
     * Return true if the checkbox "require no error to upload image" is
     * checked.
     * 
     * @return true if checked
     */
    public boolean isRequireNoErrorToUploadImage() throws Exception {
        return requireNoErrorToUploadImageCheckBox.isSelected();
    }

    /**
     * Set the checkbox "require no error to upload image".
     * 
     * @param required
     *            true if checkbox should be checked
     */
    public void setRequireNoErrorToUploadImage(boolean required) throws Exception {
        if (isRequireNoErrorToUploadImage() != required) {
            requireNoErrorToUploadImageButton.findElement(By.className("ui-chkbox-box")).click();
            Awaitility.await().until(() -> isRequireNoErrorToUploadImage() == required);
        }
    }

    /**
     * Return currently selected option value (not label) of the simple
     * wellformed dropdown.
     * 
     * @return the selected option value (not label)
     */
    public String getSimpleWellFormedSeverity() throws Exception {
        return new Select(simpleWellFormedSelect).getFirstSelectedOption().getAttribute("value");
    }

    /**
     * Set the currently selected option value (not label) for the simple
     * wellformed dropdown.
     * 
     * @param severity
     *            the newly selected option value (not label)
     */
    public void setSimpleWellFormedSeverity(String severity) throws Exception {
        setDropDownValue(simpleWellFormedDropDown, severity);
    }

    /**
     * Return currently selected option value (not label) of the simple valid
     * dropdown.
     * 
     * @return the selected option value (not label)
     */
    public String getSimpleValidSeverity() throws Exception {
        return new Select(simpleValidSelect).getFirstSelectedOption().getAttribute("value");
    }

    /**
     * Set the currently selected option value (not label) for the simple valid
     * dropdown.
     * 
     * @param severity
     *            the newly selected option value (not label)
     */
    public void setSimpleValidSeverity(String severity) throws Exception {
        setDropDownValue(simpleValidDropDown, severity);
    }

    /**
     * Return the simple filename pattern of the LTP validation configuration.
     * 
     * @return the simple filename pattern
     */
    public String getFilenamePattern() throws Exception {
        return simpleFilenamePatternInput.getAttribute("value");
    }

    /**
     * Set the simple filename pattern of the LTP validation configuration.
     * 
     * @param pattern
     *            the new pattern to be set
     */
    public void setFilenamePattern(String pattern) throws Exception {
        simpleFilenamePatternInput.clear();
        simpleFilenamePatternInput.sendKeys(pattern);
        Awaitility.await().until(() -> getFilenamePattern().equals(pattern));
    }

    /**
     * Return currently selected option value (not label) of the simple filename
     * pattern severity dropdown.
     * 
     * @return the selected option value (not label)
     */
    public String getSimpleFilenamePatternSeverity() throws Exception {
        return new Select(simpleFilenamePatternSeveritySelect).getFirstSelectedOption().getAttribute("value");
    }

    /**
     * Set the currently selected option value (not label) for the simple
     * filename pattern severity dropdown.
     * 
     * @param severity
     *            the newly selected option value (not label)
     */
    public void setSimpleFilenamePatternSeverity(String severity) throws Exception {
        setDropDownValue(simpleFilenamePatternSeverityDropDown, severity);
    }

    /**
     * Triggers save button of the LTP validation configuration.
     * 
     * @return projects page
     */
    public ProjectsPage save() throws Exception {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }

    /**
     * Returns the property name for the n-th condition listed in the all
     * conditions table.
     * 
     * @param row
     *            the row in the table
     * @return the property name of the condition in that row
     */
    public String getConditionProperty(int row) throws Exception {
        return getPropertyInputForCondition(row).getAttribute("value");
    }

    /**
     * Sets the property name for the n-th condition listed in the all
     * conditions table.
     * 
     * @param row
     *            the row in the table
     * @param name
     *            the new property name of the condition in that row
     */
    public void setConditionProperty(int row, String name) throws Exception {
        WebElement input = getPropertyInputForCondition(row);
        input.clear();
        input.sendKeys(name);
    }

    /**
     * Click the add condition button and wait until a new row was added to the
     * table.
     */
    public void clickAddConditionButton() throws Exception {
        int countBefore = getNumberOfValidationConditions();
        addConditionButton.click();
        Awaitility.await().until(() -> getNumberOfValidationConditions() == countBefore + 1);
    }

    /**
     * Click the trash button for a validation condition and wait until the row
     * was removed from the table.
     * 
     * @param idx
     *            the index of the validation condition to be removed
     */
    public void clickRemoveConditionButton(int idx) throws Exception {
        int countBefore = getNumberOfValidationConditions();
        allConditionsTable.findElements(By.tagName("tr")).get(idx + 1).findElements(By.tagName("a")).getFirst().click();
        Awaitility.await().until(() -> getNumberOfValidationConditions() == countBefore - 1);
    }

    /**
     * Return the input element of the property column for the n-th validation
     * condition listed in the table.
     * 
     * @param row
     *            the row in the table
     * @return the input element of the property column
     */
    private WebElement getPropertyInputForCondition(int row) {
        return allConditionsTable.findElements(By.tagName("tr")).get(row + 1).findElements(By.tagName("td")).getFirst()
                .findElement(By.tagName("input"));
    }

    /**
     * Return the number of validation conditions that are listed in the table
     * of all conditions.
     * 
     * @return the number of validation conditions
     */
    private int getNumberOfValidationConditions() {
        return allConditionsTable.findElements(By.cssSelector("tr:not(.ui-datatable-empty-message)")).size() - 1;
    }

    /**
     * Sets the value for a dropdown UI element. Waits until the form is
     * refreshed an the newly selected value is rendered.
     * 
     * @param dropDown
     *            the primefaces dropdown UI element
     * @param value
     *            the option value to be set (not the label)
     */
    private void setDropDownValue(WebElement dropDown, String value) {
        // wait until dropdown is available
        Awaitility.await().until(() -> dropDown.isDisplayed());

        // check which item idx corresponds to value
        String selectId = dropDown.getAttribute("id") + "_input";
        List<WebElement> options = new Select(Browser.getDriver().findElement(By.id(selectId))).getOptions();
        int idx = IntStream.range(0, options.size()).filter((i) -> value.equals(options.get(i).getAttribute("value")))
                .findFirst().orElse(-1);

        if (idx < 0) {
            throw new IllegalArgumentException("value not found amongst dropdown list items");
        }

        // click on drop down trigger
        dropDown.findElement(By.className("ui-selectonemenu-trigger")).click();

        // find items list
        String itemsId = dropDown.getAttribute("id") + "_items";
        Awaitility.await().until(() -> Browser.getDriver().findElement(By.id(itemsId)).isDisplayed());

        // select item
        Browser.getDriver().findElement(By.id(itemsId)).findElements(By.tagName("li")).get(idx).click();

        // wait until item was selected
        Awaitility.await().until(() -> new Select(Browser.getDriver().findElement(By.id(selectId)))
                .getFirstSelectedOption().getAttribute("value").equals(value));
    }
}

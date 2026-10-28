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
import static org.kitodo.selenium.testframework.Browser.hoverWebElement;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UserEditPage extends EditPage<UserEditPage> {

    private static final String USER_TAB_VIEW = EDIT_FORM + ":userTabView";
    private static final String CSS_SELECTOR_DROPDOWN_TRIGGER =  ".ui-selectonemenu-trigger";

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW)
    private WebElement userEditTabView;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":firstName")
    private WebElement firstNameInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":surname")
    private WebElement lastNameInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":login")
    private WebElement loginInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":password")
    private WebElement passwordInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":passwordValidation")
    private WebElement confirmPasswordInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":location")
    private WebElement locationInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":metaDataLanguage")
    private WebElement metadataLanguageSelect;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":table-size")
    private WebElement tableSizeInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":languages")
    private WebElement languageSelect;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":addRoleButton")
    private WebElement addUserToRoleButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":addProjectButton")
    private WebElement addUserToProjectButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":addClientButton")
    private WebElement addUserToClientButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "roleForm:selectRoleTable_data")
    private WebElement selectRoleTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "userProjectForm:selectProjectTable_data")
    private WebElement selectProjectTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "userClientForm:selectClientTable_data")
    private WebElement selectClientTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "addRoleDialog")
    private WebElement addToRoleDialog;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "addProjectDialog")
    private WebElement addToProjectDialog;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "addClientDialog")
    private WebElement addToClientDialog;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":clientDatatable_data")
    private WebElement userClientsTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":projectDatatable_data")
    private WebElement userProjectsTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = USER_TAB_VIEW + ":roleDatatable_data")
    private WebElement userRolesTable;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "removeClientDialog")
    private WebElement removeClientDialog;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "removeFromProjectsForm:confirm")
    private WebElement confirmRemoveClientButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "removeFromProjectsForm:cancelButton")
    private WebElement cancelRemoveClientButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings(UNUSED)
    @FindBy(partialLinkText = "Benutzerdaten & Einstellungen")
    private WebElement userConfigButton;

    @FindBy(id = "editForm:userTabView:showPaginationPanelByDefault")
    private WebElement showPaginationByDefaultSwitch;

    @FindBy(id = "editForm:userTabView:defaultPaginationType")
    private WebElement defaultPaginationType;

    @FindBy(id = "editForm:userTabView:showLogicalPageNumberBelowThumbnail")
    private WebElement showLogicalPageNumberBelowThumbnailSwitch;

    @FindBy(id = "editForm:userTabView:showPhysicalPageNumberBelowThumbnail")
    private WebElement showPhysicalPageNumberBelowThumbnailSwitch;

    public UserEditPage() {
        super("pages/userEdit");
    }

    @Override
    public UserEditPage goTo() {
        return null;
    }

    public UserEditPage insertUserData(User user, String password) {
        passwordInput.sendKeys(password);
        confirmPasswordInput.sendKeys(password);
        firstNameInput.sendKeys(user.getName());
        lastNameInput.sendKeys(user.getSurname());
        loginInput.sendKeys(user.getLogin());
        locationInput.sendKeys(user.getLocation());
        return this;
    }

    public UsersPage save() throws ReflectiveOperationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getUsersPage().getUrl());
        return Pages.getUsersPage();
    }

    public void addUserToRole(String roleTitle) throws Exception {
        switchToTabByIndex(TabIndex.USER_ROLES.getIndex());
        addUserToRoleButton.click();

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("roleForm:selectRoleTable:2:addRole")));
        List<WebElement> tableRows = Browser.getRowsOfTable(selectRoleTable);
        addRow(tableRows, roleTitle, addToRoleDialog);

    }

    public void addUserToClient(String clientName) throws Exception {
        switchToTabByIndex(TabIndex.USER_CLIENT_LIST.getIndex());
        addUserToClientButton.click();

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("userClientForm:selectClientTable:1:addUserClient")));

        List<WebElement> tableRows = Browser.getRowsOfTable(selectClientTable);
        addRow(tableRows, clientName, addToClientDialog);

    }

    public UserEditPage addUserToProject(String projectName) throws Exception {
        switchToTabByIndex(TabIndex.USER_PROJECT_LIST.getIndex());
        addUserToProjectButton.click();

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(By.id(selectProjectTable.getAttribute("id"))));

        List<WebElement> tableRows = Browser.getRowsOfTable(selectProjectTable);
        addRow(tableRows, projectName, addToProjectDialog);
        return this;
    }

    public void changeUserSettings() throws Exception {
        openUserConfig();
        tableSizeInput.clear();
        tableSizeInput.sendKeys("50");
        clickElement(languageSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(languageSelect.getAttribute("id") + "_1")));
        switchToTabByIndex(TabIndex.USER_METADATA_EDITOR_SETTINGS.getIndex());
        clickElement(metadataLanguageSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(metadataLanguageSelect.getAttribute("id") + "_1")));
        save();
    }

    /**
     * Set pagination panel to show by default in metadata editor.
     */
    public void togglePaginationToShowByDefault() throws Exception {
        openUserConfig();
        switchToTabByIndex(TabIndex.USER_METADATA_EDITOR_SETTINGS.getIndex());
        WebElement switchCheckBox = showPaginationByDefaultSwitch.findElement(By.className("ui-chkbox-box"));
        switchCheckBox.click();
        save();
    }

    /**
     * Toggle user setting that controls whether the physical page number is shown below a thumbnail in 
     * the metadata editor.
     */
    public void toggleShowPhysicalPageNumberBelowThumbnail() throws Exception {
        openUserConfig();
        switchToTabByIndex(TabIndex.USER_METADATA_EDITOR_SETTINGS.getIndex());
        WebElement checkBox = showPhysicalPageNumberBelowThumbnailSwitch.findElement(By.className("ui-chkbox-box"));
        checkBox.click();
        save();
    }

    /**
     * Toggle the user setting that controls whether the logical page number is shown below a thumbnail in
     * the metadata editor.
     */
    public void toggleShowLogicalPageNumberBelowThumbnail() throws Exception {
        openUserConfig();
        switchToTabByIndex(TabIndex.USER_METADATA_EDITOR_SETTINGS.getIndex());
        WebElement checkBox = showLogicalPageNumberBelowThumbnailSwitch.findElement(By.className("ui-chkbox-box"));
        checkBox.click();
        save();
    }


    /**
     * Select the default pagination type in the user setting that will be preselected in the metadata editor.
     */
    public void selectDefaultPaginationType() throws Exception {
        openUserConfig();
        switchToTabByIndex(TabIndex.USER_METADATA_EDITOR_SETTINGS.getIndex());
        clickElement(defaultPaginationType.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("#editForm\\:userTabView\\:defaultPaginationType_items [data-label=\"Freitext\"]")));
        save();
    }

    /**
     * Open the edit page of the user with the given ID.
     *
     * @param userId ID of the user to edit
     * @return this user edit page
     */
    public UserEditPage goTo(int userId) {
        Browser.goTo(getUrl() + "?id=" + userId);
        await("Wait for user edit page to be loaded").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isAt() && userEditTabView.isDisplayed());
        return this;
    }

    /**
     * Get the names of all clients listed in the client tab of the user edit page.
     *
     * @return list of client names
     */
    public List<String> getClientNames() throws Exception {
        switchToTabByIndex(TabIndex.USER_CLIENT_LIST.getIndex());
        return Browser.getTableDataByColumn(userClientsTable, 0);
    }

    /**
     * Get the titles of all projects listed in the project tab of the user edit page.
     *
     * @return list of project titles
     */
    public List<String> getProjectTitles() throws Exception {
        switchToTabByIndex(TabIndex.USER_PROJECT_LIST.getIndex());
        return Browser.getTableDataByColumn(userProjectsTable, 0);
    }

    /**
     * Get the client names of all roles listed in the role tab of the user edit page.
     *
     * @return list of client names of the user's roles
     */
    public List<String> getRoleClientNames() throws Exception {
        switchToTabByIndex(TabIndex.USER_ROLES.getIndex());
        return Browser.getTableDataByColumn(userRolesTable, 1);
    }

    /**
     * Click the 'remove' link of the client with the given name in the client tab of the user edit page.
     *
     * @param clientName name of the client to remove from the user
     */
    public void clickRemoveClient(String clientName) throws Exception {
        switchToTabByIndex(TabIndex.USER_CLIENT_LIST.getIndex());
        for (WebElement tableRow : Browser.getRowsOfTable(userClientsTable)) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(clientName)) {
                WebElement removeLink = tableRow.findElement(By.cssSelector("a[id$=':deleteFromClient']"));
                new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(removeLink));
                removeLink.click();
                return;
            }
        }
        throw new NoSuchElementException("No client with name '" + clientName + "' assigned to user");
    }

    /**
     * Wait until the dialog for confirming the removal of client projects from the user is displayed.
     */
    public void waitForRemoveClientDialog() {
        await("Wait for remove client dialog to be displayed").atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> removeClientDialog.isDisplayed());
    }

    /**
     * Check whether the dialog for confirming the removal of client projects from the user is displayed.
     *
     * @return true if the dialog is displayed
     */
    public boolean isRemoveClientDialogDisplayed() {
        try {
            return removeClientDialog.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Get the confirmation message displayed in the remove client dialog.
     *
     * @return confirmation message
     */
    public String getRemoveClientDialogMessage() {
        return removeClientDialog.findElement(By.className("select-note")).getText();
    }

    /**
     * Get the titles of the projects listed in the remove client dialog.
     *
     * @return list of project titles
     */
    public List<String> getRemoveClientDialogProjectTitles() {
        WebElement projectsTableBody = removeClientDialog.findElement(By.cssSelector(".client-projects tbody"));
        return Browser.getTableDataByColumn(projectsTableBody, 0);
    }

    /**
     * Confirm the removal of the client and its projects from the user in the remove client dialog.
     */
    public void confirmRemoveClient() {
        confirmRemoveClientButton.click();
        waitForRemoveClientDialogToClose();
        await("Wait for loading screen to disappear").pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> Browser.getDriver().findElements(By.id("loadingScreen")).stream()
                        .noneMatch(WebElement::isDisplayed));
    }

    /**
     * Cancel the removal of the client from the user in the remove client dialog.
     */
    public void cancelRemoveClient() {
        cancelRemoveClientButton.click();
        waitForRemoveClientDialogToClose();
    }

    private void waitForRemoveClientDialogToClose() {
        await("Wait for remove client dialog to close").atMost(10, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> !isRemoveClientDialogDisplayed());
    }

    private void openUserConfig() {
        await("Wait for visible user menu button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> userMenuButton.isDisplayed());

        userMenuButton.click();
        hoverWebElement(userConfigButton);
        userConfigButton.click();
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     */
    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, userEditTabView);
    }
}

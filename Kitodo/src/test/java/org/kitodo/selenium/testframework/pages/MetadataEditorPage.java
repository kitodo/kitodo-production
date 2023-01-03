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

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class MetadataEditorPage extends Page<MetadataEditorPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "structureTreeForm")
    private WebElement structureTreeForm;

    @SuppressWarnings("unused")
    @FindBy(id = "numberOfScans")
    private WebElement numberOfScans;

    @FindBy(id = "paginationPanel")
    private WebElement paginationPanel;

    @FindBy(css = "#portal-logo a")
    private WebElement poralLogoLink;

    @FindBy(id = "fileReferencesUpdatedDialog")
    private WebElement fileReferencesUpdatedDialog;

    @FindBy(id = "ok")
    private WebElement okButton;

    @FindBy(id = "buttonForm:close")
    private WebElement closeButton;

    @FindBy(id = "buttonForm:save")
    private WebElement saveButton;

    @FindBy(id = "buttonForm:saveExit")
    private WebElement saveAndExitButton;

    @FindBy(id = "logicalTree:0")
    private WebElement logicalTree;

    @FindBy(id = "logicalTree:0_0")
    private WebElement firstChildProcess;

    @FindBy(id = "logicalTree:0_1")
    private WebElement secondChildProcess;

    public MetadataEditorPage() {
        super("metadataEditor.jsf");
    }

    @Override
    public MetadataEditorPage goTo() {
        return null;
    }

    public boolean isStructureTreeFormVisible() {
        return structureTreeForm.isDisplayed();
    }

    /**
     * Gets numberOfScans.
     *
     * @return value of numberOfScans
     */
    public String getNumberOfScans() {
        return numberOfScans.getText();
    }

    /**
     * Return whether pagination panel is displayed or not.
     * @return whether pagination panel is displayed or not
     */
    public boolean isPaginationPanelVisible() {
        return paginationPanel.isDisplayed();
    }

    /**
     * Click Kitodo portal logo to return to desktop page.
     */
    public void clickPortalLogo() {
        poralLogoLink.click();
    }

    /**
     * Check and return whether information dialog about updated media references is displayed or not.
     * @return whether information dialog about updated media references is displayed or not
     */
    public boolean isFileReferencesUpdatedDialogVisible() {
        return fileReferencesUpdatedDialog.isDisplayed();
    }

    /**
     * Acknowledge file reference changes by clicking "OK" button on corresponding popup dialog.
     */
    public void acknowledgeFileReferenceChanges() {
        okButton.click();
    }

    /**
     * Close Metadata editor to release metadata lock.
     */
    public void closeEditor() {
        closeButton.click();
    }

    /**
     * Click save button.
     */
    public void save() {
        saveButton.click();
    }

    public void changeOrderOfLinkedChildProcesses() {
        secondChildProcess.click();
        WebDriver webDriver = Browser.getDriver();
        Actions moveAction = new Actions(webDriver);
        WebElement dropArea = logicalTree.findElement(By.className("ui-tree-droppoint"));
        moveAction.dragAndDrop(secondChildProcess, dropArea).build().perform();
    }

    public ProcessesPage saveAndExit() throws InstantiationException, IllegalAccessException {
        clickButtonAndWaitForRedirect(saveAndExitButton, Pages.getProcessesPage().getUrl());
        return Pages.getProcessesPage();
    }

    public String getNameOfFirstLinkedChildProcess() {
        return firstChildProcess.getText();
    }

    public String getNameOfSecondLinkedChildProcess() {
        return secondChildProcess.getText();
    }
}

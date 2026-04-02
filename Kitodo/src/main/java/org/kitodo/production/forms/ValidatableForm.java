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

package org.kitodo.production.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.kitodo.config.ConfigCore;
import org.kitodo.exceptions.FileStructureValidationException;
import org.primefaces.PrimeFaces;

public class ValidatableForm extends BaseEditView {

    protected String validationErrorTitle;
    protected String validationErrorDescription;
    protected String validationErrorNote;
    protected String validationErrorNoteEmphasized;
    protected Collection<String> validationErrors = new ArrayList<>();
    protected String redirectionPath;
    protected String rulesetValidationError;
    protected String validationErrorUpdateComponents = "editForm";
    protected boolean schemaValidationSkippable = false;

    /**
     * Get validationErrors.
     *
     * @return validationErrors
     */
    public Collection<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Set validationErrors.
     *
     * @param validationErrors as Collection of Strings
     */
    public void setValidationErrors(Collection<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    /**
     * Get validationErrorTitle.
     *
     * @return value of validationErrorTitle
     */
    public String getValidationErrorTitle() {
        return validationErrorTitle;
    }

    /**
     * Set validationErrorTitle.
     *
     * @param validationErrorTitle as java.lang.String
     */
    public void setValidationErrorTitle(String validationErrorTitle) {
        this.validationErrorTitle = validationErrorTitle;
    }

    /**
     * Get validationErrorDescription.
     *
     * @return value of validationErrorDescription
     */
    public String getValidationErrorDescription() {
        return validationErrorDescription;
    }

    /**
     * Set validationErrorDescription.
     *
     * @param validationErrorDescription as java.lang.String
     */
    public void setValidationErrorDescription(String validationErrorDescription) {
        this.validationErrorDescription = validationErrorDescription;
    }

    /**
     * Get validationErrorNote.
     *
     * @return value of validationErrorNote
     */
    public String getValidationErrorNote() {
        return validationErrorNote;
    }

    /**
     * Set validationErrorNote.
     *
     * @param validationErrorNote as java.lang.String
     */
    public void setValidationErrorNote(String validationErrorNote) {
        this.validationErrorNote = validationErrorNote;
    }

    /**
     * Get validationErrorNoteEmphasized.
     *
     * @return value of validationErrorNoteEmphasized
     */
    public String getValidationErrorNoteEmphasized() {
        return validationErrorNoteEmphasized;
    }

    /**
     * Set validationErrorNoteEmphasized.
     *
     * @param validationErrorNoteEmphasized as java.lang.String
     */
    public void setValidationErrorNoteEmphasized(String validationErrorNoteEmphasized) {
        this.validationErrorNoteEmphasized = validationErrorNoteEmphasized;
    }

    /**
     * Get redirectionPath.
     *
     * @return value of redirectionPath
     */
    public String getRedirectionPath() {
        return redirectionPath;
    }

    /**
     * Set redirectionPath.
     *
     * @param redirectionPath as java.lang.String
     */
    public void setRedirectionPath(String redirectionPath) {
        this.redirectionPath = redirectionPath;
    }

    /**
     * Check and return if the debug folder is configured.
     *
     * @return if debug folder is configured
     */
    public boolean isDebugFolderConfigured() {
        return Objects.nonNull(ConfigCore.getKitodoDebugDirectory());
    }

    /**
     * Get rulesetValidationError.
     *
     * @return rulesetValidationError
     */
    public String getRulesetValidationError() {
        return rulesetValidationError;
    }

    /**
     * Set rulesetValidationError.
     *
     * @return rulesetValidationError
     */
    public boolean isSchemaValidationSkippable() {
        return schemaValidationSkippable;
    }

    /**
     * Set schemaValidationSkippable.
     *
     * @param schemaValidationSkippable as boolean
     */
    public void setSchemaValidationSkippable(boolean schemaValidationSkippable) {
        this.schemaValidationSkippable = schemaValidationSkippable;
    }

    /**
     * Empty method to be overridden by subclasses (e.g. 'CreateProcessForm').
     */
    public void proceed() {
    }

    /**
     * Displays a validation exception dialog based on the provided exception and redirection path.
     * Updates the relevant UI components and sets necessary validation details for display.
     *
     * @param exception the {@code FileStructureValidationException} containing validation data,
     *                  including error messages and additional validation context.
     * @param redirectionPath the path to which the user is redirected when closing the dialog.
     */
    public void showValidationExceptionDialog(FileStructureValidationException exception, String redirectionPath) {
        if (Objects.isNull(redirectionPath) || redirectionPath.contains(REDIRECT_PARAMETER)) {
            setRedirectionPath(redirectionPath);
        } else {
            if (redirectionPath.contains("?")) {
                setRedirectionPath(redirectionPath + "&" + REDIRECT_PARAMETER);
            } else {
                setRedirectionPath(redirectionPath + "?" + REDIRECT_PARAMETER);
            }
        }
        setValidationErrorDescription(exception.getMessage());
        setValidationErrors(exception.getValidationResult().getResultMessages());
        setSchemaValidationSkippable(exception.isExternalDataValidation());
        PrimeFaces.current().ajax().update("validationErrorsDialog");
        PrimeFaces.current().executeScript("PF('validationErrorsDialog').show();");
    }

    /**
     * Get value of validationErrorUpdateComponents.
     *
     * @return value of validationErrorUpdateComponents
     */
    public String getValidationErrorUpdateComponents() {
        return validationErrorUpdateComponents;
    }
}

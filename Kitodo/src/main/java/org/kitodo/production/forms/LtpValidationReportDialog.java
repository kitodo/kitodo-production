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

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.validation.LtpValidationHelper;
import org.primefaces.PrimeFaces;

/**
 * Backend bean for the LTP validation report dialog that is shown if the user
 * clicks on the "validate images" link of an image validation task.
 */
@Named("LtpValidationReportDialog")
@ViewScoped
public class LtpValidationReportDialog implements Serializable {

    /**
     * The maximum number of folders (that contain validation errors) that are
     * shown in the dialog.
     * 
     * <p>
     * The maximum prevents the dialog from containing thousands of errors
     * messages in case the validation configuration is set up incorrectly by
     * the user and all files in all folders have validation errors.
     * </p>
     */
    private static final int MAX_NUMBER_OF_REPORTED_FOLDERS = ConfigCore
            .getIntParameter(ParameterCore.LTP_VALIDATION_MAX_REPORTED_FOLDERS);

    /**
     * The maximum number of files (that contain validation errors) that are
     * shown in the dialog.
     * 
     * <p>
     * The maximum prevents the dialog from containing thousands of errors
     * messages in case the validation configuration is set up incorrectly by
     * the user and all files in all folders have validation errors.
     * </p>
     */
    private static final int MAX_NUMBER_OF_REPORTED_FILES_PER_FOLDER = ConfigCore
            .getIntParameter(ParameterCore.LTP_VALIDATION_MAX_REPORTED_FILES_PER_FOLDER);

    /**
     * Contains all validation results for all folders and files.
     */
    private Map<Folder, Map<URI, LtpValidationResult>> resultsByFolder;

    /**
     * Is called after uploading files from the metadata editor.
     * 
     * @param resultsByFile
     *            the validation results for each uploaded file
     * @param folder
     *            the folder to which files were uploaded
     */
    public void openAfterFileUpload(Map<URI, LtpValidationResult> resultsByFile, Folder folder) {
        this.resultsByFolder = Collections.singletonMap(folder, resultsByFile);
        if (!resultsByFile.isEmpty()) {
            PrimeFaces.current().executeScript("PF('ltpValidationReportDialog').show();");
        }
    }

    /**
     * Is called when the user clicks on the "validate images" link in an image
     * validation task.
     * 
     * @param currentTask
     *            the task
     */
    public void validateTaskAndOpen(Task currentTask) {
        // trigger validation of all folder and files
        this.resultsByFolder = LtpValidationHelper.validateImageFoldersForTask(currentTask);
        PrimeFaces.current().executeScript("PF('ltpValidationReportDialog').show();");
    }

    /**
     * Return the number of files in all folders that were deemed valid.
     * 
     * @return the number of valid files
     */
    public int getNumberOfValidFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return resultsByFolder.values().stream()
                .reduce(
                    0, (sum,
                            resultsByFile) -> sum + (int) resultsByFile.values().stream()
                                    .filter((r) -> r.getState().equals(LtpValidationResultState.VALID)).count(),
                    Integer::sum);
    }

    /**
     * Return the total number of files in all folders.
     * 
     * @return the total number of files in all folders
     */
    public int getTotalNumberOfFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return resultsByFolder.values().stream().reduce(0,
            (sum, resultsByFile) -> sum + (int) resultsByFile.values().stream().count(), Integer::sum);
    }

    /**
     * Return the number of files with validation errors for a given folder.
     * 
     * @param folder
     *            the folder
     * @return the number of files with validation errors
     */
    public int getNumberOfFilesWithValidationErrorsForFolder(Folder folder) {
        if (Objects.isNull(folder) || Objects.isNull(resultsByFolder) || !resultsByFolder.containsKey(folder)) {
            return 0;
        }
        return (int) resultsByFolder.get(folder).values().stream()
                .filter((result) -> result.getState().equals(LtpValidationResultState.ERROR)).count();
    }

    /**
     * Return the number of invalid files (with warnings or errors) in all
     * folders.
     * 
     * @return the number of invalid files (with warnings or errors) in all
     *         folders
     */
    public int getNumberOfInvalidFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return getTotalNumberOfFiles() - getNumberOfValidFiles();
    }

    /**
     * Return a list of folders with at least one file that has validation
     * issues (capped by the maximum number of reported folders).
     * 
     * @return the list of folder with at least one file that has validation
     *         issues
     */
    public List<Folder> getFoldersWithValidationIssues() {
        if (Objects.isNull(resultsByFolder)) {
            return Collections.emptyList();
        }
        return this.resultsByFolder.keySet().stream()
                .filter((folder) -> getFilesWithValidationIssues(folder).size() > 0)
                .sorted((fA, fB) -> getNumberOfFilesWithValidationErrorsForFolder(fB)
                        - getNumberOfFilesWithValidationErrorsForFolder(fA))
                .limit(MAX_NUMBER_OF_REPORTED_FOLDERS).collect(Collectors.toList());
    }

    /**
     * Return the list of files that have validation issues for a given folder
     * (capped at the maximum number of reported files).
     * 
     * @param folder
     *            the folder
     * @return the list of files that have validation issues for the provided
     *         folder
     */
    public List<URI> getFilesWithValidationIssues(Folder folder) {
        if (Objects.isNull(resultsByFolder) || !resultsByFolder.containsKey(folder)) {
            return Collections.emptyList();
        }
        return this.resultsByFolder.get(folder).keySet().stream().filter(
            (filepath) -> !resultsByFolder.get(folder).get(filepath).getState().equals(LtpValidationResultState.VALID))
                .sorted((fA,
                        fB) -> -resultsByFolder.get(folder).get(fA).getState()
                                .compareTo(resultsByFolder.get(folder).get(fB).getState()))
                .limit(MAX_NUMBER_OF_REPORTED_FILES_PER_FOLDER).collect(Collectors.toList());
    }

    /**
     * Return the validation result for a specific file in a specific folder.
     * 
     * @param folder
     *            the folder
     * @param filepath
     *            the filepath
     * @return the validation result for that file
     */
    public LtpValidationResult getValidationResultForFile(Folder folder, URI filepath) {
        if (Objects.isNull(resultsByFolder) || !resultsByFolder.containsKey(folder)
                || !resultsByFolder.get(folder).containsKey(filepath)) {
            return null;
        }
        return resultsByFolder.get(folder).get(filepath);
    }

    /**
     * Return a list of translated general validation error messages for a file.
     * 
     * @param folder
     *            the folder
     * @param filepath
     *            the filepath
     * @return a list of translated general validation error message for the
     *         file
     */
    public List<String> getGeneralErrorMessagesForFile(Folder folder, URI filepath) {
        LtpValidationResult result = getValidationResultForFile(folder, filepath);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        return LtpValidationHelper.translateGeneralErrorsList(result.getErrors());
    }

    /**
     * Return a list of translated condition failure messages.
     */
    public List<String> getValidationConditionMessagesForFile(Folder folder, URI filepath) {
        LtpValidationResult result = getValidationResultForFile(folder, filepath);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();
        return LtpValidationHelper.translateConditionResultsThatDidNotPass(result.getConditionResults(), conditions);
    }

    /**
     * Return any additional (untranslated) validation message for a file.
     * 
     * @param folder
     *            the folder
     * @param filepath
     *            the filepath
     * @return the list of (untranslated) additional validation message
     */
    public List<String> getAdditionalMessagesForFile(Folder folder, URI filepath) {
        LtpValidationResult result = getValidationResultForFile(folder, filepath);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        return result.getAdditionalMessages();
    }

    /**
     * Return a translated message describing that all files were deemed as
     * valid.
     * 
     * @return the translated message
     */
    public String getTranslatedAllFilesValidMessage() {
        return Helper.getTranslation("ltpValidation.report.allFilesValid", String.valueOf(getTotalNumberOfFiles()));
    }

    /**
     * Return a translated message describing that validation issues were found.
     * 
     * @return the translated message
     */
    public String getTranslatedIssuesFoundMessage() {
        return Helper.getTranslation("ltpValidation.report.issuesFound", String.valueOf(getNumberOfValidFiles()),
            String.valueOf(getNumberOfInvalidFiles()));
    }

}

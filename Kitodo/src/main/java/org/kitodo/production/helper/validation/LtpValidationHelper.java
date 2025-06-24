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

package org.kitodo.production.helper.validation;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;

/**
 * Helper class providing common functions related to long-term-preservation validation.
 */
public class LtpValidationHelper {

    private static final Logger logger = LogManager.getLogger(LtpValidationHelper.class);

    private static final LongTermPreservationValidationService ltpService = new LongTermPreservationValidationService();
    private static final FileService fileService = ServiceManager.getFileService();

    /**
     * Return the absolute URI for a folder in a specific process.
     *
     * @param folder the folder
     * @param process the process
     * @return the absolute URI to this folder
     */
    private static URI getAbsoluteFolderUri(Folder folder, Process process) {
        Subfolder subfolder = new Subfolder(process, folder);

        return Paths.get(
                KitodoConfig.getKitodoDataDirectory(), 
                ServiceManager.getProcessService().getProcessDataDirectory(process).getPath(),
                subfolder.getRelativeDirectoryPath()
            ).toUri();
    }

    /**
     * Return the absolute URI from a filepath relative to Kitodo's data directory.
     * 
     * @param relativeToDataDir the filepath relative to Kitodo's data directory
     * @return the absolute filepath
     */
    private static URI getAbsoluteFileUri(URI relativeToDataDir) {
        return new File(KitodoConfig.getKitodoDataDirectory().concat(relativeToDataDir.getPath())).toURI();
    }

    /**
     * Return relative URI for absolute URI with respect to Kitodo's data directory.
     * 
     * @param absoluteUri the absolute filepath to a file inside Kitodo's data directory
     * @return the relative uri to the file
     */
    private static URI getRelativeFileUri(URI absoluteUri) {
        return new File(KitodoConfig.getKitodoDataDirectory()).toURI().relativize(absoluteUri);
    }

    /**
     * Translate a condition operation to a string that is presented to the user.
     * 
     * @param operation the condition operation
     * @return the translation for a condition operation
     */
    public static String translateConditionOperation(LtpValidationConditionOperation operation) {
        if (operation.equals(LtpValidationConditionOperation.EQUAL)) {
            return Helper.getTranslation("ltpValidation.condition.operation.equal");
        } else if (operation.equals(LtpValidationConditionOperation.ONE_OF)) {
            return Helper.getTranslation("ltpValidation.condition.operation.oneOf");
        } else if (operation.equals(LtpValidationConditionOperation.MATCHES)) {
            return Helper.getTranslation("ltpValidation.condition.operation.matches");
        } else if (operation.equals(LtpValidationConditionOperation.NONE_OF)) {
            return Helper.getTranslation("ltpValidation.condition.operation.noneOf");
        } else if (operation.equals(LtpValidationConditionOperation.LARGER_THAN)) {
            return Helper.getTranslation("ltpValidation.condition.operation.largerThan");
        } else if (operation.equals(LtpValidationConditionOperation.SMALLER_THAN)) {
            return Helper.getTranslation("ltpValidation.condition.operation.smallerThan");
        } else if (operation.equals(LtpValidationConditionOperation.IN_BETWEEN)) {
            return Helper.getTranslation("ltpValidation.condition.operation.inBetween");
        }
        // should never happen
        return "unknown operation";
    }

    /**
     * Translate the failure severity of a validation confition to a string.
     * 
     * @param severity the failure severity of a validation condition
     * @return the translation for the failure severity
     */
    public static String translateConditionSeverity(LtpValidationConditionSeverity severity) {
        if (severity.equals(LtpValidationConditionSeverity.WARNING)) {
            return Helper.getTranslation("ltpValidation.condition.severity.warning");
        } else if (severity.equals(LtpValidationConditionSeverity.ERROR)) {
            return Helper.getTranslation("ltpValidation.condition.severity.error");
        }
        return "unknown severity";
    }
    
    /**
     * Translate a validation condition result to a simple string that is presented to the user.
     * 
     * @param result the validation condition results that is translated
     * @param condition the corresponding validation condition
     * @return the translation for the condition result
     */
    public static String translateConditionResult(LtpValidationConditionResult result, LtpValidationCondition condition) {
        if (result.getPassed()) {
            // currently never displayed (only warnings and errors are shown)
            return "Condition passed";
        } else {
            String severity = translateConditionSeverity(condition.getSeverity()) + ": ";
            if (result.getError().equals(LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST)) {
                return severity + Helper.getTranslation(
                    "ltpValidation.condition.error.propertyDoesNotExist", 
                    condition.getProperty()
                );
            } else if (result.getError().equals(LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES)) {
                return severity + Helper.getTranslation(
                    "ltpValidation.condition.error.incorrectNumberOfValues", 
                    String.valueOf(condition.getValues().size()), 
                    translateConditionOperation(condition.getOperation())
                );
            } else if (result.getError().equals(LtpValidationConditionError.NOT_A_NUMBER)) {
                return severity + Helper.getTranslation(
                    "ltpValidation.condition.error.notANumber", 
                    result.getValue(), 
                    translateCondition(condition)
                );
            } else if (result.getError().equals(LtpValidationConditionError.PATTERN_INVALID_SYNTAX)) {
                return severity + Helper.getTranslation(
                    "ltpValidation.condition.error.patternInvalidSyntax", 
                    translateCondition(condition)
                );
            } else if (result.getError().equals(LtpValidationConditionError.CONDITION_FALSE)) {
                return severity + Helper.getTranslation(
                    "ltpValidation.condition.error.conditionFalse", 
                    result.getValue(), 
                    translateCondition(condition)
                );
            } else if (result.getError().equals(LtpValidationConditionError.UNKNOWN_OPERATION)) {
                // should never happen
                return severity + "Condition operation '" + condition.getOperation().name() +  "' not supported"; 
            }
            // should never happen
            return severity + "Unknown condition error";
        }
    }

    /**
     * Translate a list of validation condition results by translating only results for conditions that did not pass.
     * 
     * @param results the list of validation conditions to be translated
     * @param conditions the list of corresponding validation conditions
     * @return the list of translations for each validation condition results with issues (errors or warnings)
     */
    public static List<String> translateConditionResultsThatDidNotPass(
        List<LtpValidationConditionResult> results, 
        List<LtpValidationCondition> conditions
    ) {
        return IntStream
                .range(0, Math.min(results.size(), conditions.size()))
                .filter((i) -> !results.get(i).getPassed())
                .mapToObj((i) -> translateConditionResult(results.get(i), conditions.get(i)))
                .collect(Collectors.toList());
    }

    /**
     * Translate a general validation error.
     * 
     * @param error the error to be translated
     * @return the translation of the error
     */
    public static String translateGeneralError(LtpValidationError error) {
        if (error.equals(LtpValidationError.FILE_NOT_FOUND)) {
            return Helper.getTranslation("ltpValidation.result.error.fileNotFound");
        } else if (error.equals(LtpValidationError.FILE_TYPE_NOT_SUPPORTED)) {
            return Helper.getTranslation("ltpValidation.result.error.fileTypeNotSupported");
        } else if (error.equals(LtpValidationError.IO_ERROR)) {
            return Helper.getTranslation("ltpValidation.result.error.ioError");
        } else if (error.equals(LtpValidationError.UNKNOWN_ERROR)) {
            return Helper.getTranslation("ltpValidation.result.error.unknownError");
        }
        // should never happen
        return "unknown error";
    }

    /**
     * Translate a validation condition to a simple string.
     * 
     * @param condition the validation condition to be translated
     * @return the translation for the validation condition
     */
    public static String translateCondition(LtpValidationCondition condition) {
        return Helper.getTranslation(
            "ltpValidation.condition", 
            condition.getProperty(), 
            translateConditionOperation(condition.getOperation()),
            StringUtils.join(condition.getValues(), ",")
        );
    }

    /**
     * Translate a validation result for a single file.
     * 
     * @param result the validation result for a single file
     * @param conditions the validation conditions that were checked for that file
     * @param filepath the relative filepath of the file
     * @return a simple translated string describing the validation result
     */
    public static String translateValidationResult(LtpValidationResult result, List<LtpValidationCondition> conditions, URI filepath) {
        if (result.getState().equals(LtpValidationResultState.VALID)) {
            // currently never displayed (only warnings and errors are shown)
            return "File '" + filepath.getPath() + "'' passed validation";
        } else { 
            List<String> errorMessages = translateGeneralErrorsList(result.getErrors());
            List<String> conditionMessages = translateConditionResultsThatDidNotPass(result.getConditionResults(), conditions);
            List<String> additionalMessages = result.getAdditionalMessages();

            List<String> allMessages = Stream.of(errorMessages, conditionMessages, additionalMessages)
                .flatMap(Collection::stream).collect(Collectors.toList());

            String allMessagesString = StringUtils.join(allMessages, ", ");

            if (result.getState().equals(LtpValidationResultState.ERROR)) {
                return Helper.getTranslation("ltpValidation.result.description.error", filepath.getPath(), allMessagesString);
            } else {
                return Helper.getTranslation("ltpValidation.result.description.warning", filepath.getPath(), allMessagesString);
            }
        }
    }

    /**
     * Translate the list of general validation errors.
     * 
     * @param errors the list of validation errors to be translated
     * @return the list of translated errors
     */
    public static List<String> translateGeneralErrorsList(List<LtpValidationError> errors) {
        return errors.stream().map((e) -> translateGeneralError(e)).collect(Collectors.toList());
    }

    /**
     * Validate all images of a single folder for a task.
     * 
     * @param task the task
     * @param folder the folder whose images are to be validated
     * @return the map of validation results for each file in the folder
     */
    public static Map<URI, LtpValidationResult> validateImageFolderForTask(Task task, Folder folder) {
        logger.debug("validating images in folder: " + folder.getRelativePath());

        Subfolder subfolder = new Subfolder(task.getProcess(), folder);
        Optional<FileType> fileType = subfolder.getFileFormat().getFileType();
        List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();
        Map<URI, LtpValidationResult> resultsByFile = new TreeMap<>();
        URI absoluteFolderUri = getAbsoluteFolderUri(folder, task.getProcess());

        for (URI relativeFileURI : fileService.getSubUris(absoluteFolderUri)) {
            URI absoluteFileURI = getAbsoluteFileUri(relativeFileURI);
            LtpValidationResult result = ltpService.validate(absoluteFileURI, fileType.orElse(null), conditions);
            resultsByFile.put(relativeFileURI, result);
        }

        return resultsByFile;
    }

    /**
     * Validate all images of all folders for a given task.
     * 
     * @param task the task
     * @return the validation results for each folder and file as a two-step map
     */
    public static Map<Folder, Map<URI, LtpValidationResult>> validateImageFoldersForTask(Task task) {
        Comparator<Folder> ignoreCaseComparator = (fA, fB) -> fA.getRelativePath().compareTo(fB.getRelativePath());
        Map<Folder, Map<URI, LtpValidationResult>> resultsByFolder = new TreeMap<>(ignoreCaseComparator);

        for (Folder folder : task.getValidationFolders()) {
            if (!folder.isValidateFolder()) {
                logger.debug("folder has validation disabled");
                continue;
            }

            if (Objects.isNull(folder.getLtpValidationConfiguration())) {
                logger.debug("folder has no validation configuration assigned");
                continue;
            }

            Map<URI, LtpValidationResult> resultsByFile = validateImageFolderForTask(task, folder);
            if (Objects.nonNull(resultsByFile)) {
                resultsByFolder.put(folder, resultsByFile);
            }
        }

        return resultsByFolder;
    }

    /**
     * Returns true if the validation result contains any errors, either general errors or condition errors.
     * 
     * @param result the validation result to be checked for errors
     * @param conditions the conditions that were applied
     * @param generalErrorSeverity the severity level of general errors (if warning, general errors are ignored)
     * @return true if the validation result contains any errors
     */
    public static boolean validationResultHasError(
        LtpValidationResult result, List<LtpValidationCondition> conditions, 
        LtpValidationConditionSeverity generalErrorSeverity
    ) {
        if (generalErrorSeverity == LtpValidationConditionSeverity.ERROR && result.getErrors().size() > 0) {
            return true;
        }
        return IntStream
                .range(0, Math.min(result.getConditionResults().size(), conditions.size()))
                .filter((i) -> !result.getConditionResults().get(i).getPassed() 
                    && conditions.get(i).getSeverity() == LtpValidationConditionSeverity.ERROR)
                .findAny()
                .isPresent();
    }

    /**
     * Validate all images of all folders for a task when clicking on the "finish task" link.
     * 
     * <p>Reports the first error that was found to the user as a ErrorMessage</p>
     * 
     * @param task the task
     * @return true if all files of all folders passed validation
     */
    public static boolean validateImagesWhenFinishingTask(Task task) {
        for (Folder folder : task.getValidationFolders()) {
            if (!folder.isValidateFolder()) {
                logger.debug("folder has validation disabled");
                continue;
            }

            if (Objects.isNull(folder.getLtpValidationConfiguration())) {
                logger.debug("folder has no validation configuration assigned");
                continue;
            }

            if (!folder.getLtpValidationConfiguration().getRequireNoErrorToFinishTask()) {
                logger.debug("folder does not need to be validated when finishing task");
                continue;
            }

            Map<URI, LtpValidationResult> fileResults = validateImageFolderForTask(task, folder);
            List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();

            for (Map.Entry<URI, LtpValidationResult> fileEntry : fileResults.entrySet()) {
                URI relativeFilePath = fileEntry.getKey();
                LtpValidationResult result = fileEntry.getValue();

                if (validationResultHasError(result, conditions, LtpValidationConditionSeverity.ERROR)) {

                    Helper.setErrorMessage(translateValidationResult(result, conditions, relativeFilePath));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate a single file that was uploaded by a user inside of the metadata editor.
     * 
     * @param absoluteFilePath the absolute filepath of the uploaded file
     * @param folder the folder the file was uploaded to
     * @param subfolder the subfolder instance the file was uploaded to
     * @param validationResults the map that will be filled with a new validation result
     * @return false if there were errors during validation and the uploaded file needs to be deleted
     */
    public static boolean validateUploadedFile(
        URI absoluteFilePath, 
        Folder folder, 
        Subfolder subfolder, 
        Map<URI, LtpValidationResult> validationResults
    ) {
        if (Objects.isNull(folder)) {
            // no valid folder provided
            return true;
        }
        if (!folder.isValidateFolder()) {
            // validation is not enabled for folder
            return true;
        }
        if (Objects.isNull(folder.getLtpValidationConfiguration())) {
            // no validation configuration assigned to folder
            return true;
        }

        logger.debug("validate uploaded file: " + absoluteFilePath);
        Optional<FileType> fileType = subfolder.getFileFormat().getFileType();
        List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();
        LtpValidationResult result = ltpService.validate(absoluteFilePath, fileType.orElse(null), conditions);
        URI relativeFilePath = getRelativeFileUri(absoluteFilePath);
        validationResults.put(relativeFilePath, result);

        if (validationResultHasError(result, conditions, LtpValidationConditionSeverity.ERROR)) {
            logger.error("uploaded file has validation errors: " + result);
            return false;
        }

        // there are no validation errors (maybe warnings)
        return true;
    }
}

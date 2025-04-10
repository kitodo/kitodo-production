package org.kitodo.production.helper.validation;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionOperation;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class LtpValidationHelper {

    private static final FileService fileService = ServiceManager.getFileService();

    public static Map<String, URI> getMapOfFilesInFolder(Folder folder, Process process) {
        Subfolder subfolder = new Subfolder(process, folder);
        
        URI absoluteFolderURI = Paths.get(
                KitodoConfig.getKitodoDataDirectory(), 
                ServiceManager.getProcessService().getProcessDataDirectory(process).getPath(),
                subfolder.getRelativeDirectoryPath()
            ).toUri();

        return fileService.getSubUris(absoluteFolderURI).stream()
            .map((relativeToDataDir) -> new File(KitodoConfig.getKitodoDataDirectory().concat(relativeToDataDir.getPath())).toURI())
            .collect(Collectors.toMap((uri) -> absoluteFolderURI.relativize(uri).getPath(), (uri) -> uri));
    }

    public static String translateConditionOperation(LtpValidationConditionOperation operation) {
        if (operation.equals(LtpValidationConditionOperation.EQUAL)) {
            return Helper.getTranslation("ltpValidation.condition.operation.equal");
        } else if (operation.equals(LtpValidationConditionOperation.ONE_OF)) {
            return Helper.getTranslation("ltpValidation.condition.operation.oneOf");
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

    public static String translateConditionSeverity(LtpValidationConditionSeverity severity) {
        if (severity.equals(LtpValidationConditionSeverity.WARNING)) {
            return Helper.getTranslation("ltpValidation.condition.severity.warning");
        } else if (severity.equals(LtpValidationConditionSeverity.ERROR)) {
            return Helper.getTranslation("ltpValidation.condition.severity.error");
        }
        return "unknown severity";
    }
    
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

    public static List<String> translateConditionResults(List<LtpValidationConditionResult> results, List<LtpValidationCondition> conditions) {
        return IntStream
                .range(0, Math.min(results.size(), conditions.size()))
                .filter((i) -> !results.get(i).getPassed())
                .mapToObj((i) -> translateConditionResult(results.get(i), conditions.get(i)))
                .collect(Collectors.toList());
    }

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

    public static String translateCondition(LtpValidationCondition condition) {
        return Helper.getTranslation(
            "ltpValidation.condition", 
            condition.getProperty(), 
            translateConditionOperation(condition.getOperation()),
            StringUtils.join(condition.getValues(), ",")
        );
    }

    public static String translateValidationResult(LtpValidationResult result, List<LtpValidationCondition> conditions, String fileName) {
        if (result.getState().equals(LtpValidationResultState.VALID)) {
            // currently never displayed (only warnings and errors are shown)
            return "File '" + fileName.toString() + "'' passed validation";
        } else { 
            List<String> errorMessages = translateGeneralErrorsList(result.getErrors());
            List<String> conditionMessages = translateConditionResults(result.getConditionResults(), conditions);
            List<String> additionalMessages = result.getAdditionalMessages();

            List<String> allMessages = Stream.of(errorMessages, conditionMessages, additionalMessages)
                .flatMap(Collection::stream).collect(Collectors.toList());

            String allMessagesString = StringUtils.join(allMessages, ", ");

            if (result.getState().equals(LtpValidationResultState.ERROR)) {
                return Helper.getTranslation("ltpValidation.result.description.error", fileName, allMessagesString);
            } else {
                return Helper.getTranslation("ltpValidation.result.description.warning", fileName, allMessagesString);
            }
        }
    }

    public static List<String> translateGeneralErrorsList(List<LtpValidationError> errors) {
        return errors.stream().map((e) -> translateGeneralError(e)).collect(Collectors.toList());
    }

}

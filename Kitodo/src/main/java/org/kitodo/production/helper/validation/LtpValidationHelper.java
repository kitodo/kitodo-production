package org.kitodo.production.helper.validation;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionSeverity;
import org.kitodo.api.validation.longtermpreservation.LtpValidationError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.LtpValidationCondition;
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

    
    public static String translateConditionResult(LtpValidationConditionResult result, LtpValidationCondition condition) {
        if (result.getPassed()) {
            return "Condition passed";
        } else {
            String severity = translateConditionSeverity(condition.getSeverity()) + ": ";
            if (result.getError().equals(LtpValidationConditionError.PROPERTY_DOES_NOT_EXIST)) {
                return severity + "Property '" + condition.getProperty() + "'' does not exist";
            } else if (result.getError().equals(LtpValidationConditionError.INCORRECT_NUMBER_OF_CONDITION_VALUES)) {
                return severity + "Incorrect number (" + condition.getValues().size() + ") of values for condition of type '" + condition.getOperation().name() +  "'";
            } else if (result.getError().equals(LtpValidationConditionError.CONDITION_FALSE)) {
                return severity + "Value '" + result.getValue() + "' does not match condition (" + translateCondition(condition) + ")";
            } else if (result.getError().equals(LtpValidationConditionError.UNKNOWN_OPERATION)) {
                return severity + "Condition operation '" + condition.getOperation().name() +  "' not supported";
            }
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
        return error.name();
    }

    public static String translateCondition(LtpValidationCondition condition) {
        return condition.getProperty() + " " + condition.getOperation().name() + " " + StringUtils.join(condition.getValues(), ","); 
    }

    public static String translateConditionSeverity(LtpValidationConditionSeverity severity) {
        return severity.name();
    }

    public static List<String> translateValidationResultToMessageList(LtpValidationResult result, List<LtpValidationCondition> conditions, String fileName) {
        if (result.getState().equals(LtpValidationResultState.VALID)) {
            return Collections.singletonList("File " + fileName.toString() + " passed validation");
        } else { 
            List<String> errorMessages = translateGeneralErrorsList(result.getErrors());
            List<String> conditionMessages = translateConditionResults(result.getConditionResults(), conditions);
            List<String> additionalMessages = result.getAdditionalMessages();

            List<String> allMessages = Stream.of(errorMessages, conditionMessages, additionalMessages)
                .flatMap(Collection::stream).collect(Collectors.toList());

            return allMessages;
        }
    }

    public static String translateValidationResult(LtpValidationResult results, List<LtpValidationCondition> conditions, String fileName) {
        return StringUtils.join(translateValidationResultToMessageList(results, conditions, fileName), ", ");
    }

    public static List<String> translateGeneralErrorsList(List<LtpValidationError> errors) {
        return errors.stream().map((e) -> translateGeneralError(e)).collect(Collectors.toList());
    }

}

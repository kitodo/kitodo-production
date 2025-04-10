package org.kitodo.production.forms;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LtpValidationCondition;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.validation.LtpValidationHelper;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;

@Named("LtpValidationReportDialog")
@ViewScoped
public class LtpValidationReportDialog implements Serializable {

    private static final Logger logger = LogManager.getLogger(LtpValidationReportDialog.class);

    private final LongTermPreservationValidationService ltpService = new LongTermPreservationValidationService();

    private static final int MAX_NUMBER_OF_REPORTED_FOLDERS = 3;
    private static final int MAX_NUMBER_OF_REPORTED_FILES_PER_FOLDER = 5;

    private Map<Folder, Map<String, LtpValidationResult>> resultsByFolder;
    
    public void open(Task currentTask) {
        logger.info("initialize validation report by performing validation");
        
        resultsByFolder = new TreeMap<>((f1, f2) -> f1.getRelativePath().compareTo(f2.getRelativePath()));

        for (Folder folder : currentTask.getValidationFolders()) {
            if (!folder.isValidateFolder()) {
                logger.debug("folder has validation disabled");
                continue;
            }

            if (Objects.isNull(folder.getLtpValidationConfiguration())) {
                logger.debug("folder has no validation configuration assigned");
                continue;
            }

            Subfolder subfolder = new Subfolder(currentTask.getProcess(), folder);
            Optional<FileType> fileType = subfolder.getFileFormat().getFileType();

            if (!(fileType.isPresent())) {
                logger.debug("folder has unknown mime type: " + folder.getMimeType());
                continue;
            }

            List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();
            resultsByFolder.put(folder, new TreeMap<>());

            logger.info("validating images in folder: " + folder.getRelativePath() + " containing files of type: " + fileType.get());
            Map<String, URI> filesOfFolder = LtpValidationHelper.getMapOfFilesInFolder(folder, currentTask.getProcess());
            for (Map.Entry<String, URI> fileEntry : filesOfFolder.entrySet()) {
                String fileName = fileEntry.getKey();
                URI absoluteFileURI = fileEntry.getValue();
                logger.info("validate file: " + absoluteFileURI);
                LtpValidationResult result = ltpService.validate(absoluteFileURI, fileType.get(), conditions);
                logger.info("validation result: " + result);
                resultsByFolder.get(folder).put(fileName, result);
            }
        }
    }

    public int getNumberOfValidFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return resultsByFolder.values().stream()
                .reduce(0, (sum, resultsByFile) -> sum + (int)resultsByFile.values().stream()
                    .filter((r) -> r.getState().equals(LtpValidationResultState.VALID))
                    .count(),
                Integer::sum);
    }

    public int getTotalNumberOfFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return resultsByFolder.values().stream()
                .reduce(0, (sum, resultsByFile) -> sum + (int)resultsByFile.values().stream().count(),
                Integer::sum);
    }

    public int getNumberOfInvalidFiles() {
        if (Objects.isNull(resultsByFolder)) {
            return 0;
        }
        return getTotalNumberOfFiles() - getNumberOfValidFiles();
    }

    public List<Folder> getFoldersWithValidationIssues() {
        if (Objects.isNull(resultsByFolder)) {
            return Collections.emptyList();
        }
        return this.resultsByFolder.keySet().stream()
            .filter((folder) -> getFilesWithValidationIssues(folder).size() > 0)
            .limit(MAX_NUMBER_OF_REPORTED_FOLDERS)
            .collect(Collectors.toList());
    }

    public List<String> getFilesWithValidationIssues(Folder folder) {
        if (Objects.isNull(resultsByFolder) || !resultsByFolder.containsKey(folder)) {
            return Collections.emptyList();
        }
        return this.resultsByFolder.get(folder).keySet().stream()
            .filter((fileName) -> !resultsByFolder.get(folder).get(fileName).getState().equals(LtpValidationResultState.VALID))
            .limit(MAX_NUMBER_OF_REPORTED_FILES_PER_FOLDER)
            .collect(Collectors.toList());
    }

    public LtpValidationResult getValidationResultForFile(Folder folder, String fileName) {
        if (Objects.isNull(resultsByFolder) 
                || !resultsByFolder.containsKey(folder) 
                || !resultsByFolder.get(folder).containsKey(fileName)) {
            return null;
        }
        return resultsByFolder.get(folder).get(fileName);
    }

    public List<String> getGeneralErrorMessagesForFile(Folder folder, String fileName) {
        LtpValidationResult result = getValidationResultForFile(folder, fileName);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        return LtpValidationHelper.translateGeneralErrorsList(result.getErrors()); 
    }

    public List<String> getValidationConditionMessagesForFile(Folder folder, String fileName) {
        LtpValidationResult result = getValidationResultForFile(folder, fileName);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        List<LtpValidationCondition> conditions = folder.getLtpValidationConfiguration().getValidationConditions();
        return LtpValidationHelper.translateConditionResults(result.getConditionResults(), conditions);
    }

    public List<String> getAdditionalMessagesForFile(Folder folder, String fileName) {
        LtpValidationResult result = getValidationResultForFile(folder, fileName);
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }
        return result.getAdditionalMessages();
    }

    public String getTranslatedAllFilesValidMessage() {
        return Helper.getTranslation(
            "ltpValidation.report.allFilesValid", 
            String.valueOf(getTotalNumberOfFiles())
        );
    }

    public String getTranslatedIssuesFoundMessage() {
        return Helper.getTranslation(
            "ltpValidation.report.issuesFound", 
            String.valueOf(getNumberOfValidFiles()), 
            String.valueOf(getNumberOfInvalidFiles())
        );
    }

}

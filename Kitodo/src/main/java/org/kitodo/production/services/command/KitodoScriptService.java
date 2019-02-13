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

package org.kitodo.production.services.command;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class KitodoScriptService {
    private Map<String, String> parameters;
    private static final Logger logger = LogManager.getLogger(KitodoScriptService.class);
    private final FileService fileService = ServiceManager.getFileService();
    private static final String DIRECTORY_SUFFIX = "_tif";
    private static final String KITODO_SCRIPT_FIELD = "kitodoScriptfield";
    private static final String RULESET = "ruleset";
    private static final String SCRIPT = "script";
    private static final String SOURCE_FOLDER = "sourcefolder";
    private static final String STATUS = "status";
    private static final String TASK_TITLE = "steptitle";
    private static final String ROLE = "role";

    /**
     * Start the script execution.
     *
     * @param processes
     *            list of Process objects
     * @param script
     *            from frontend passed as String
     */
    public void execute(List<Process> processes, String script) throws DataException {
        this.parameters = new HashMap<>();
        // decompose and capture all script parameters
        StrTokenizer tokenizer = new StrTokenizer(script, ' ', '\"');
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken();
            if (Objects.isNull(tok) || !tok.contains(":")) {
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "missing delimiter / unknown parameter: ", tok);
            } else {
                String key = tok.substring(0, tok.indexOf(':'));
                String value = tok.substring(tok.indexOf(':') + 1);
                this.parameters.put(key, value);
            }
        }

        // pass the appropriate method with the correct parameters
        if (Objects.isNull(this.parameters.get("action"))) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "missing action",
                " - possible: 'action:swapsteps, action:adduser, action:addrole, "
                        + "action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, "
                        + "action:importFromFileSystem'");
            return;
        }

        // call the correct method via the parameter
        switch (this.parameters.get("action")) {
            case "importFromFileSystem":
                importFromFileSystem(processes);
                break;
            case "addRole":
                addRole(processes);
                break;
            case "setTaskProperty":
                setTaskProperty(processes);
                break;
            case "setStepStatus":
                setTaskStatus(processes);
                break;
            case "addShellScriptToStep":
                addShellScriptToStep(processes);
                break;
            case "updateImagePath":
                updateImagePath(processes);
                break;
            case "updateContentFiles":
                updateContentFiles(processes);
                break;
            case "deleteTiffHeaderFile":
                deleteTiffHeaderFile(processes);
                break;
            case "setRuleset":
                setRuleset(processes);
                break;
            case "exportDms":
                exportDms(processes, this.parameters.get("exportImages"), true);
                break;
            case "export":
                exportDms(processes, this.parameters.get("exportImages"),
                    Boolean.valueOf(this.parameters.get("exportOcr")));
                break;
            case "doit":
                exportDms(processes, String.valueOf(Boolean.FALSE), false);
                break;
            case "doit2":
                exportDms(processes, String.valueOf(Boolean.FALSE), true);
                break;
            case "runscript":
                String taskName = this.parameters.get("stepname");
                String scriptName = this.parameters.get(SCRIPT);
                if (Objects.isNull(scriptName)) {
                    Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "", "Missing parameter");
                } else {
                    runScript(processes, taskName, scriptName);
                }
                break;
            case "deleteProcess":
                String value = parameters.get("contentOnly");
                boolean contentOnly = true;
                if (Objects.nonNull(value) && value.equalsIgnoreCase("false")) {
                    contentOnly = false;
                }
                deleteProcess(processes, contentOnly);
                break;
            default:
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Unknown action",
                    " - use: 'action:swapsteps, action:adduser, action:addrole, "
                            + "action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, "
                            + "action:importFromFileSystem'");
                return;
        }

        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "kitodoScript finished");
    }

    private void updateContentFiles(List<Process> processes) {
        for (Process process : processes) {
            try {
                LegacyMetsModsDigitalDocumentHelper rdf = ServiceManager.getProcessService().readMetadataFile(process);
                rdf.getDigitalDocument().addAllContentFiles();
                fileService.writeMetadataFile(rdf, process);
                Helper.setMessage(KITODO_SCRIPT_FIELD, "ContentFiles updated: ", process.getTitle());
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Error while updating content files", logger, e);
            }
        }
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "updateContentFiles finished");
    }

    private void deleteProcess(List<Process> processes, boolean contentOnly) {
        for (Process process : processes) {
            String title = process.getTitle();
            if (contentOnly) {
                try {
                    URI ocr = fileService.getOcrDirectory(process);
                    if (fileService.fileExist(ocr)) {
                        fileService.delete(ocr);
                    }
                    URI images = fileService.getImagesDirectory(process);
                    if (fileService.fileExist(images)) {
                        fileService.delete(images);
                    }
                    Helper.setMessage("Content deleted for " + title);
                } catch (IOException | RuntimeException e) {
                    Helper.setErrorMessage("errorDeleting", new Object[] {"content for " + title }, logger, e);
                }
            }
            if (!contentOnly) {
                try {
                    deleteMetadataDirectory(process);
                    ServiceManager.getProcessService().remove(process);
                    Helper.setMessage("Process " + title + " deleted.");
                } catch (DataException | IOException e) {
                    Helper.setErrorMessage("errorDeleting",
                        new Object[] {Helper.getTranslation("process") + " " + title }, logger, e);
                }
            }
        }
    }

    private void deleteMetadataDirectory(Process process) throws IOException {
        fileService.deleteProcessContent(process);
    }

    private void runScript(List<Process> processes, String taskName, String scriptName) throws DataException {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equalsIgnoreCase(taskName)) {
                    if (Objects.nonNull(scriptName)) {
                        if (task.getScriptName().equals(scriptName)) {
                            String path = task.getScriptPath();
                            ServiceManager.getTaskService().executeScript(task, path, false);
                        }
                    } else {
                        ServiceManager.getTaskService().executeScript(task, false);
                    }
                }
            }
        }
    }

    /**
     * Import the data from a directories of the given processes.
     *
     * @param processes
     *            list of Process objects
     */
    private void importFromFileSystem(List<Process> processes) {
        if (isActionParameterInvalid(SOURCE_FOLDER)) {
            return;
        }

        URI sourceFolder = new File(this.parameters.get(SOURCE_FOLDER)).toURI();
        try {
            if (!fileService.isDirectory(sourceFolder)) {
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD,
                    "Directory " + this.parameters.get(SOURCE_FOLDER) + " does not exisist");
                return;
            }
            for (Process process : processes) {
                Integer processId = process.getId();
                String processTitle = process.getTitle();
                URI imagesFolder = ServiceManager.getProcessService().getImagesOriginDirectory(false, process);
                if (!fileService.getSubUris(imagesFolder).isEmpty()) {
                    Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "",
                        "The process " + processTitle + " [" + processId + "] has already data in image folder");
                } else {
                    URI sourceFolderProcess = fileService.createResource(sourceFolder, processTitle);
                    if (!fileService.isDirectory(sourceFolder)) {
                        Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "",
                            "The directory for process " + processTitle + " [" + processId + "] is not existing");
                    } else {
                        fileService.copyDirectory(sourceFolderProcess, imagesFolder);
                        Helper.setMessage(KITODO_SCRIPT_FIELD, "",
                            "The directory for process " + processTitle + " [" + processId + "] is copied");
                    }
                    Helper.setMessage(KITODO_SCRIPT_FIELD, "",
                        "The process " + processTitle + " [" + processId + "] is copied");
                }
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Set ruleset.
     *
     * @param processes
     *            list of Process objects
     */
    private void setRuleset(List<Process> processes) {
        if (isActionParameterInvalid(RULESET)) {
            return;
        }

        try {
            List<Ruleset> rulesets = ServiceManager.getRulesetService()
                    .getByQuery("from Ruleset where title='" + this.parameters.get(RULESET) + "'");
            if (rulesets.isEmpty()) {
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Could not find ruleset: ", RULESET);
                return;
            }
            Ruleset ruleset = rulesets.get(0);

            for (Process process : processes) {
                process.setRuleset(ruleset);
                ServiceManager.getProcessService().save(process);
            }
        } catch (DataException | RuntimeException e) {
            Helper.setErrorMessage(e);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Add ShellScript to task of the given processes.
     *
     * @param processes
     *            list of Process objects
     */
    private void addShellScriptToStep(List<Process> processes) {
        if (isActionParameterInvalid(TASK_TITLE) || isActionParameterInvalid("label")
                || isActionParameterInvalid(SCRIPT)) {
            return;
        }

        executeActionForAddShellToScript(processes);
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "addShellScriptToStep finished: ");
    }

    private void executeActionForAddShellToScript(List<Process> processes) {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equals(this.parameters.get(TASK_TITLE))) {
                    task.setScriptPath(this.parameters.get(SCRIPT));
                    task.setScriptName(this.parameters.get("label"));
                    saveProcess(process);
                    Helper.setMessage(KITODO_SCRIPT_FIELD, "Added script to step: ", process.getTitle());
                    break;
                }
            }
        }
    }

    /**
     * Flag von Schritten setzen.
     *
     * @param processes
     *            list of Process objects
     */
    private void setTaskProperty(List<Process> processes) {
        if (isActionParameterInvalid(TASK_TITLE) || isActionParameterInvalid("property")
                || isActionParameterInvalid("value")) {
            return;
        }

        String property = this.parameters.get("property");
        String value = this.parameters.get("value");

        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages")
                && !property.equals("validate") && !property.equals("exportdms") && !property.equals("batch")
                && !property.equals("automatic")) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "",
                "wrong parameter 'property'; possible values: metadata, readimages, writeimages, "
                        + "validate, exportdms");
            return;
        }

        if (!value.equals("true") && !value.equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "wrong parameter 'value'; possible " + "values: true, false");
            return;
        }

        executeActionForSetTaskProperty(processes, property, value);
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "setTaskProperty abgeschlossen: ");
    }

    private void executeActionForSetTaskProperty(List<Process> processes, String property, String value) {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equals(this.parameters.get(TASK_TITLE))) {
                    if (property.equals("metadata")) {
                        task.setTypeMetadata(Boolean.parseBoolean(value));
                    }
                    if (property.equals("automatic")) {
                        task.setTypeAutomatic(Boolean.parseBoolean(value));
                    }
                    if (property.equals("batch")) {
                        task.setBatchStep(Boolean.parseBoolean(value));
                    }
                    if (property.equals("readimages")) {
                        task.setTypeImagesRead(Boolean.parseBoolean(value));
                    }
                    if (property.equals("writeimages")) {
                        task.setTypeImagesWrite(Boolean.parseBoolean(value));
                    }
                    if (property.equals("validate")) {
                        task.setTypeCloseVerify(Boolean.parseBoolean(value));
                    }
                    if (property.equals("exportdms")) {
                        task.setTypeExportDMS(Boolean.parseBoolean(value));
                    }

                    saveProcess(process);
                    Helper.setMessage(KITODO_SCRIPT_FIELD, "Error while saving process: ", process.getTitle());
                    break;
                }
            }
        }
    }

    /**
     * Set task status for the given processes.
     *
     * @param processes
     *            list of Process objects
     */
    private void setTaskStatus(List<Process> processes) {
        if (isActionParameterInvalid(TASK_TITLE) || isActionParameterInvalid(STATUS)) {
            return;
        }

        if (!this.parameters.get(STATUS).equals("0") && !this.parameters.get(STATUS).equals("1")
                && !this.parameters.get(STATUS).equals("2") && !this.parameters.get(STATUS).equals("3")) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Wrong status parameter: status ",
                "(possible: 0=closed, 1=open, 2=in work, 3=finished");
            return;
        }

        executeActionForSetTaskStatus(processes);
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "setStepStatus finished: ");
    }

    private void executeActionForSetTaskStatus(List<Process> processes) {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equals(this.parameters.get(TASK_TITLE))) {
                    task.setProcessingStatus(
                        ServiceManager.getTaskService().setProcessingStatusAsString(this.parameters.get(STATUS)));
                    saveTask(process.getTitle(), task);
                    Helper.setMessage(KITODO_SCRIPT_FIELD, "stepstatus set in process: ", process.getTitle());
                    break;
                }
            }
        }
    }

    /**
     * Add role to the task of given processes.
     *
     * @param processes
     *            list of Process objects
     */
    private void addRole(List<Process> processes) {
        if (isActionParameterInvalid(TASK_TITLE) || isActionParameterInvalid(ROLE)) {
            return;
        }

        // check if role exists
        Role role;
        List<Role> foundRoles = ServiceManager.getRoleService()
                .getByQuery("FROM Role WHERE title='" + this.parameters.get(ROLE) + "'");
        if (!foundRoles.isEmpty()) {
            role = foundRoles.get(0);
        } else {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Unknown role: ", this.parameters.get(ROLE));
            return;
        }

        executeActionForAddRole(processes, role);
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "addRole finished");
    }

    private void executeActionForAddRole(List<Process> processes, Role role) {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equals(this.parameters.get(TASK_TITLE))) {
                    List<Role> roles = task.getRoles();
                    if (!roles.contains(role)) {
                        roles.add(role);
                        saveTask(process.getTitle(), task);
                    }
                }
            }
            Helper.setMessage(KITODO_SCRIPT_FIELD, "added role to task: ", process.getTitle());
        }
    }

    /**
     * Delete TiffHeader file from given processes.
     *
     * @param processes
     *            list of Process objects
     */
    public void deleteTiffHeaderFile(List<Process> processes) {
        for (Process process : processes) {
            try {
                File tiffHeaderFile = new File(fileService.getImagesDirectory(process) + "tiffwriter.conf");
                if (tiffHeaderFile.exists()) {
                    Files.delete(tiffHeaderFile.toPath());
                }
                Helper.setMessage(KITODO_SCRIPT_FIELD, "TiffHeaderFile deleted: ", process.getTitle());
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Error while deleting TiffHeader", logger, e);
            }
        }
        Helper.setMessage(KITODO_SCRIPT_FIELD, "", "deleteTiffHeaderFile finished");
    }

    /**
     * Reset image path in the metadata (possibly delete existing ones first).
     *
     * @param processes
     *            list of Process objects
     */
    public void updateImagePath(List<Process> processes) {
        // TODO remove
        /*
         * In the new internal METS format, the path to the image directory is
         * no longer saved. All image URIs are relative to the process directory
         * and thus relative to the directory where the METS file resides.
         * Therefore, there is nothing left to do here.
         */
    }

    private void exportDms(List<Process> processes, String exportImages, boolean exportFulltext) {
        boolean withoutImages = Objects.nonNull(exportImages) && exportImages.equalsIgnoreCase("false");
        for (Process process : processes) {
            try {
                ExportDms dms = new ExportDms(!withoutImages);
                if (withoutImages) {
                    dms.setExportFullText(exportFulltext);
                }
                dms.startExport(process);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean isActionParameterInvalid(String parameter) {
        if (Objects.isNull(this.parameters.get(parameter)) || Objects.equals(this.parameters.get(parameter), "")) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "missing parameter: ", parameter);
            return true;
        }
        return false;
    }

    private void saveProcess(Process process) {
        try {
            ServiceManager.getProcessService().save(process);
        } catch (DataException e) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Error while saving process: " + process.getTitle(), logger, e);
        }
    }

    private void saveTask(String processTitle, Task task) {
        try {
            ServiceManager.getTaskService().save(task);
        } catch (DataException e) {
            Helper.setErrorMessage(KITODO_SCRIPT_FIELD, "Error while saving - " + processTitle, logger, e);
        }
    }
}

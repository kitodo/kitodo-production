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

package de.sub.goobi.helper;

import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

// TODO: Delete me, this should be part of the Plugins...
// TODO: Break this up into multiple classes with a common interface
// TODO: add funny observer pattern here for more complexity
// TODO: add some general mechanism for string-output of goobi scripts in jsp

public class GoobiScript {
    private HashMap<String, String> parameters;
    private static final Logger logger = LogManager.getLogger(GoobiScript.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();
    private static final String DIRECTORY_SUFFIX = "_tif";

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
                Helper.setFehlerMeldung("kitodoScriptfield", "missing delimiter / unknown parameter: ", tok);
            } else {
                String key = tok.substring(0, tok.indexOf(":"));
                String value = tok.substring(tok.indexOf(":") + 1);
                this.parameters.put(key, value);
            }
        }

        // pass the appropriate method with the correct parameters
        if (this.parameters.get("action") == null) {
            Helper.setFehlerMeldung("kitodoScriptfield", "missing action",
                " - possible: 'action:swapsteps, action:adduser, action:addusergroup, "
                        + "action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, "
                        + "action:importFromFileSystem'");
            return;
        }

        // call the correct method via the parameter
        switch (this.parameters.get("action")) {
            case "swapSteps":
                swapSteps(processes);
                break;
            case "importFromFileSystem":
                importFromFileSystem(processes);
                break;
            case "addUser":
                adduser(processes);
                break;
            case "addUserGroup":
                addusergroup(processes);
                break;
            case "setTaskProperty":
                setTaskProperty(processes);
                break;
            case "deleteStep":
                deleteStep(processes);
                break;
            case "addStep":
                addStep(processes);
                break;
            case "setStepNumber":
                setStepNumber(processes);
                break;
            case "setStepStatus":
                setStepStatus(processes);
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
                exportDms(processes, "false", false);
                break;
            case "doit2":
                exportDms(processes, "false", true);
                break;
            case "runscript":
                String taskName = this.parameters.get("stepname");
                String scriptName = this.parameters.get("script");
                if (scriptName == null) {
                    Helper.setFehlerMeldung("kitodoScriptfield", "", "Missing parameter");
                } else {
                    runScript(processes, taskName, scriptName);
                }
                break;
            case "deleteProcess":
                String value = parameters.get("contentOnly");
                boolean contentOnly = true;
                if (value != null && value.equalsIgnoreCase("false")) {
                    contentOnly = false;
                }
                deleteProcess(processes, contentOnly);
                break;
            default:
                Helper.setFehlerMeldung("kitodoScriptfield", "Unknown action",
                    " - use: 'action:swapsteps, action:adduser, action:addusergroup, "
                            + "action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, "
                            + "action:importFromFileSystem'");
                return;
        }

        Helper.setMeldung("kitodoScriptfield", "", "kitodoScript finished");
    }

    private void updateContentFiles(List<Process> processes) {
        for (Process proz : processes) {
            try {
                Fileformat myRdf = serviceManager.getProcessService().readMetadataFile(proz);
                myRdf.getDigitalDocument().addAllContentFiles();
                serviceManager.getFileService().writeMetadataFile(myRdf, proz);
                Helper.setMeldung("kitodoScriptfield", "ContentFiles updated: ", proz.getTitle());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("kitodoScriptfield", "Error while updating content files", e);
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "updateContentFiles finished");
    }

    private void deleteProcess(List<Process> processes, boolean contentOnly) {
        for (Process p : processes) {
            String title = p.getTitle();
            if (contentOnly) {
                try {
                    File ocr = new File(serviceManager.getFileService().getOcrDirectory(p));
                    if (ocr.exists()) {
                        fileService.delete(ocr.toURI());
                    }
                    File images = new File(serviceManager.getFileService().getImagesDirectory(p));
                    if (images.exists()) {
                        fileService.delete(images.toURI());
                    }
                    Helper.setMeldung("Content deleted for " + title);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Can not delete content for " + p.getTitle(), e);
                }
            }
            if (!contentOnly) {
                try {
                    deleteMetadataDirectory(p);
                    serviceManager.getProcessService().remove(p);
                    Helper.setMeldung("Process " + title + " deleted.");
                } catch (DataException | IOException e) {
                    Helper.setFehlerMeldung("could not delete process " + p.getTitle(), e);
                }
            }
        }
    }

    private void deleteMetadataDirectory(Process process) throws IOException {
        serviceManager.getFileService().deleteProcessContent(process);
    }

    private void runScript(List<Process> processes, String taskName, String scriptName) throws DataException {
        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                if (task.getTitle().equalsIgnoreCase(taskName)) {
                    if (scriptName != null) {
                        if (task.getScriptName().equals(scriptName)) {
                            String path = task.getScriptPath();
                            serviceManager.getTaskService().executeScript(task, path, false);
                        }
                    } else {
                        serviceManager.getTaskService().executeScript(task, false);
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
        // validation od the action parameters
        if (this.parameters.get("sourcefolder") == null || this.parameters.get("sourcefolder").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "missing parameter: ", "sourcefolder");
            return;
        }

        URI sourceFolder = new File(this.parameters.get("sourcefolder")).toURI();
        try {
            if (!fileService.isDirectory(sourceFolder)) {
                Helper.setFehlerMeldung("kitodoScriptfield",
                    "Directory " + this.parameters.get("sourcefolder") + " does not exisist");
                return;
            }
            for (Process p : processes) {
                URI imagesFolder = serviceManager.getProcessService().getImagesOrigDirectory(false, p);
                if (fileService.getSubUris(imagesFolder).size() > 0) {
                    Helper.setFehlerMeldung("kitodoScriptfield", "",
                        "The process " + p.getTitle() + " [" + p.getId() + "] has already data in image folder");
                } else {
                    URI sourceFolderProzess = fileService.createResource(sourceFolder, p.getTitle());
                    if (!fileService.isDirectory(sourceFolder)) {
                        Helper.setFehlerMeldung("kitodoScriptfield", "",
                            "The directory for process " + p.getTitle() + " [" + p.getId() + "] is not existing");
                    } else {
                        fileService.copyDirectory(sourceFolderProzess, imagesFolder);
                        Helper.setMeldung("kitodoScriptfield", "",
                            "The directory for process " + p.getTitle() + " [" + p.getId() + "] is copied");
                    }
                    Helper.setMeldung("kitodoScriptfield", "",
                        "The process " + p.getTitle() + " [" + p.getId() + "] is copied");
                }
            }
        } catch (IOException e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Set ruleset.
     * 
     * @param processes
     *            list of Process objects
     */
    private void setRuleset(List<Process> processes) {
        // validation od the action parameters
        if (this.parameters.get("ruleset") == null || this.parameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "ruleset");
            return;
        }

        try {
            List<Ruleset> rulesets = serviceManager.getRulesetService()
                    .getByQuery("from Ruleset where title='" + this.parameters.get("ruleset") + "'");
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("kitodoScriptfield", "Could not find ruleset: ", "ruleset");
                return;
            }
            Ruleset ruleset = rulesets.get(0);

            for (Process p : processes) {
                p.setRuleset(ruleset);
                serviceManager.getProcessService().save(p);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Swap two tasks against each other.
     * 
     * @param processes
     *            list of Process objects
     */
    private void swapSteps(List<Process> processes) {
        // validation od the action parameters
        if (this.parameters.get("swap1nr") == null || this.parameters.get("swap1nr").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "swap1nr");
            return;
        }
        if (this.parameters.get("swap2nr") == null || this.parameters.get("swap2nr").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "swap2nr");
            return;
        }
        if (this.parameters.get("swap1title") == null || this.parameters.get("swap1title").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "swap1title");
            return;
        }
        if (this.parameters.get("swap2title") == null || this.parameters.get("swap2title").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "swap2title");
            return;
        }
        int firstOrder;
        int secondOrder;
        try {
            firstOrder = Integer.parseInt(this.parameters.get("swap1nr"));
            secondOrder = Integer.parseInt(this.parameters.get("swap2nr"));
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Invalid order number used: ",
                this.parameters.get("swap1nr") + " - " + this.parameters.get("swap2nr"));
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            Task firstTask = null;
            Task secondTask = null;
            for (Task task : proz.getTasks()) {
                if (task.getTitle().equals(this.parameters.get("swap1title")) && task.getOrdering() == firstOrder) {
                    firstTask = task;
                }
                if (task.getTitle().equals(this.parameters.get("swap2title")) && task.getOrdering() == secondOrder) {
                    secondTask = task;
                }
            }
            if (firstTask != null && secondTask != null) {
                TaskStatus statusTemp = firstTask.getProcessingStatusEnum();
                firstTask.setProcessingStatusEnum(secondTask.getProcessingStatusEnum());
                secondTask.setProcessingStatusEnum(statusTemp);
                firstTask.setOrdering(secondOrder);
                secondTask.setOrdering(firstOrder);
                try {
                    serviceManager.getTaskService().save(firstTask);
                    serviceManager.getTaskService().save(secondTask);
                } catch (DataException e) {
                    Helper.setFehlerMeldung("kitodoScriptfield", "Error on save while swapping tasks in process: ",
                        proz.getTitle() + " - " + firstTask.getTitle() + " : " + secondTask.getTitle());
                    logger.error("Error on save while swapping process: " + proz.getTitle() + " - "
                            + firstTask.getTitle() + " : " + secondTask.getTitle(),
                        e);
                }
                Helper.setMeldung("kitodoScriptfield", "Swapped tasks in: ", proz.getTitle());
            }

        }
        Helper.setMeldung("kitodoScriptfield", "swapsteps finished: ");
    }

    /**
     * Delete task for the given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void deleteStep(List<Process> processes) {
        // validation od the action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            if (proz.getTasks() != null) {
                for (Task task : proz.getTasks()) {
                    if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                        proz.getTasks().remove(task);
                        try {
                            serviceManager.getProcessService().save(proz);
                        } catch (DataException e) {
                            Helper.setFehlerMeldung("kitodoScriptfield",
                                "Error while saving process: " + proz.getTitle(), e);
                            logger.error("kitodoScriptfield" + "Error while saving process: " + proz.getTitle(), e);
                        }
                        Helper.setMeldung("kitodoScriptfield", "Removed step from process: ", proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "deleteStep finished: ");
    }

    /**
     * Add tasks to the given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void addStep(List<Process> processes) {
        // validation od the action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.parameters.get("number") == null || this.parameters.get("number").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.parameters.get("number"))) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            Task s = new Task();
            s.setTitle(this.parameters.get("steptitle"));
            s.setOrdering(Integer.parseInt(this.parameters.get("number")));
            s.setProcess(proz);
            if (proz.getTasks() == null) {
                proz.setTasks(new ArrayList<>());
            }
            proz.getTasks().add(s);
            try {
                serviceManager.getProcessService().save(proz);
            } catch (DataException e) {
                Helper.setFehlerMeldung("kitodoScriptfield", "Error while saving process: " + proz.getTitle(), e);
                logger.error("kitodoScriptfield" + "Error while saving process: " + proz.getTitle(), e);
            }
            Helper.setMeldung("kitodoScriptfield", "Added task to process: ", proz.getTitle());
        }
        Helper.setMeldung("kitodoScriptfield", "", "addStep finished: ");
    }

    /**
     * Add ShellScript to task of the given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void addShellScriptToStep(List<Process> processes) {
        // validation of the action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Fehlender Parameter: ", "steptitle");
            return;
        }

        if (this.parameters.get("label") == null || this.parameters.get("label").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Fehlender Parameter: ", "label");
            return;
        }

        if (this.parameters.get("script") == null || this.parameters.get("script").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Fehlender Parameter: ", "script");
            return;
        }

        // execution pf the action
        for (Process process : processes) {
            if (process.getTasks() != null) {
                for (Task task : process.getTasks()) {
                    if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                        task.setScriptPath(this.parameters.get("script"));
                        task.setScriptName(this.parameters.get("label"));
                        try {
                            serviceManager.getProcessService().save(process);
                        } catch (DataException e) {
                            Helper.setFehlerMeldung("kitodoScriptfield",
                                "Error while saving process: " + process.getTitle(), e);
                            logger.error("kitodoScriptfield" + "Error while saving process: " + process.getTitle(), e);
                        }
                        Helper.setMeldung("kitodoScriptfield", "Added script to step: ", process.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "addShellScriptToStep finished: ");
    }

    /**
     * Flag von Schritten setzen.
     * 
     * @param processes
     *            list of Process objects
     */
    private void setTaskProperty(List<Process> processes) {
        // validation od the action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.parameters.get("property") == null || this.parameters.get("property").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "property");
            return;
        }

        if (this.parameters.get("value") == null || this.parameters.get("value").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "value");
            return;
        }

        String property = this.parameters.get("property");
        String value = this.parameters.get("value");

        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages")
                && !property.equals("validate") && !property.equals("exportdms") && !property.equals("batch")
                && !property.equals("automatic")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "",
                "wrong parameter 'property'; possible values: metadata, readimages, writeimages, "
                        + "validate, exportdms");
            return;
        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "wrong parameter 'value'; possible " + "values: true, false");
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            if (proz.getTasks() != null) {
                for (Task task : proz.getTasks()) {
                    if (task.getTitle().equals(this.parameters.get("steptitle"))) {

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

                        try {
                            serviceManager.getProcessService().save(proz);
                        } catch (DataException e) {
                            Helper.setFehlerMeldung("kitodoScriptfield",
                                "Error while saving process: " + proz.getTitle(), e);
                            logger.error("kitodoScriptfield" + "Error while saving process: " + proz.getTitle(), e);
                        }
                        Helper.setMeldung("kitodoScriptfield", "Error while saving process: ", proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "setTaskProperty abgeschlossen: ");
    }

    /**
     * Set task status for the given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void setStepStatus(List<Process> processes) {
        // validation of the action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.parameters.get("status") == null || this.parameters.get("status").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "status");
            return;
        }

        if (!this.parameters.get("status").equals("0") && !this.parameters.get("status").equals("1")
                && !this.parameters.get("status").equals("2") && !this.parameters.get("status").equals("3")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Wrong status parameter: status ",
                "(possible: 0=closed, 1=open, 2=in work, 3=finished");
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            for (Task task : proz.getTasks()) {
                if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                    serviceManager.getTaskService().setProcessingStatusAsString(this.parameters.get("status"));
                    try {
                        serviceManager.getTaskService().save(task);
                    } catch (DataException e) {
                        Helper.setFehlerMeldung("kitodoScriptfield", "Error while saving process: " + proz.getTitle(),
                            e);
                        logger.error("kitodoScriptfield" + "Error while saving process: " + proz.getTitle(), e);
                    }
                    Helper.setMeldung("kitodoScriptfield", "stepstatus set in process: ", proz.getTitle());
                    break;
                }
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "setStepStatus finished: ");
    }

    /**
     * Schritte auf bestimmten Reihenfolge setzen.
     * 
     * @param processes
     *            list of Process objects
     */
    private void setStepNumber(List<Process> processes) {
        // validation of action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.parameters.get("number") == null || this.parameters.get("number").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.parameters.get("number"))) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            for (Task task : proz.getTasks()) {
                if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                    task.setOrdering(Integer.parseInt(this.parameters.get("number")));
                    try {
                        serviceManager.getTaskService().save(task);
                    } catch (DataException e) {
                        Helper.setFehlerMeldung("kitodoScriptfield", "Error while saving process: " + proz.getTitle(),
                            e);
                        logger.error("kitodoScriptfield" + "Error while saving process: " + proz.getTitle(), e);
                    }
                    Helper.setMeldung("kitodoScriptfield", "step order changed in process: ", proz.getTitle());
                    break;
                }
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "setStepNumber finished ");
    }

    /**
     * Add user to task of the given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void adduser(List<Process> processes) {
        // validate action parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.parameters.get("username") == null || this.parameters.get("username").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "username");
            return;
        }
        // checks if user exists
        User user;
        List<User> foundUsers = serviceManager.getUserService()
                .getByQuery("from User where login='" + this.parameters.get("username") + "'");
        if (foundUsers != null && foundUsers.size() > 0) {
            user = foundUsers.get(0);
        } else {
            Helper.setFehlerMeldung("kitodoScriptfield", "Unknown user: ", this.parameters.get("username"));
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            for (Task task : proz.getTasks()) {
                if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                    List<User> users = task.getUsers();
                    if (users == null) {
                        users = new ArrayList<>();
                        task.setUsers(users);
                    }
                    if (!users.contains(user)) {
                        users.add(user);
                        try {
                            serviceManager.getTaskService().save(task);
                        } catch (DataException e) {
                            Helper.setFehlerMeldung("kitodoScriptfield", "Error while saving - " + proz.getTitle(), e);
                            logger.error("kitodoScriptfield" + "Error while saving - " + proz.getTitle(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("kitodoScriptfield", "Added user to step: ", proz.getTitle());
        }
        Helper.setMeldung("kitodoScriptfield", "", "adduser finished.");
    }

    /**
     * Add user group to the task of given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    private void addusergroup(List<Process> processes) {
        // validate parameters
        if (this.parameters.get("steptitle") == null || this.parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.parameters.get("group") == null || this.parameters.get("group").equals("")) {
            Helper.setFehlerMeldung("kitodoScriptfield", "Missing parameter: ", "group");
            return;
        }
        // check if user group exists
        UserGroup userGroup;
        List<UserGroup> foundUserGroups = serviceManager.getUserGroupService()
                .getByQuery("from UserGroup where title='" + this.parameters.get("group") + "'");
        if (foundUserGroups != null && foundUserGroups.size() > 0) {
            userGroup = foundUserGroups.get(0);
        } else {
            Helper.setFehlerMeldung("kitodoScriptfield", "Unknown group: ", this.parameters.get("group"));
            return;
        }

        // execution of the action
        for (Process proz : processes) {
            for (Task task : proz.getTasks()) {
                if (task.getTitle().equals(this.parameters.get("steptitle"))) {
                    List<UserGroup> userGroups = task.getUserGroups();
                    if (userGroups == null) {
                        userGroups = new ArrayList<>();
                        task.setUserGroups(userGroups);
                    }
                    if (!userGroups.contains(userGroup)) {
                        userGroups.add(userGroup);
                        try {
                            serviceManager.getTaskService().save(task);
                        } catch (DataException e) {
                            Helper.setFehlerMeldung("kitodoScriptfield", "Error while saving - " + proz.getTitle(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("kitodoScriptfield", "added usergroup to step: ", proz.getTitle());
        }
        Helper.setMeldung("kitodoScriptfield", "", "addusergroup finished");
    }

    /**
     * Delete TiffHeader file from given processes.
     * 
     * @param processes
     *            list of Process objects
     */
    public void deleteTiffHeaderFile(List<Process> processes) {
        for (Process proz : processes) {
            try {
                File tiffHeaderFile = new File(
                        serviceManager.getFileService().getImagesDirectory(proz) + "tiffwriter.conf");
                if (tiffHeaderFile.exists()) {
                    tiffHeaderFile.delete();
                }
                Helper.setMeldung("kitodoScriptfield", "TiffHeaderFile deleted: ", proz.getTitle());
            } catch (Exception e) {
                Helper.setFehlerMeldung("kitodoScriptfield", "Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("kitodoScriptfield", "", "deleteTiffHeaderFile finished");
    }

    /**
     * Reset image path in the metadata (possibly delete existing ones first).
     * 
     * @param processes
     *            list of Process objects
     */
    public void updateImagePath(List<Process> processes) {
        for (Process proz : processes) {
            try {
                Fileformat myRdf = serviceManager.getProcessService().readMetadataFile(proz);
                MetadataType mdt = UghHelper.getMetadataType(proz, "pathimagefiles");
                List<? extends ugh.dl.Metadata> allImagePaths = myRdf.getDigitalDocument().getPhysicalDocStruct()
                        .getAllMetadataByType(mdt);
                if (allImagePaths.size() > 0) {
                    for (Metadata md : allImagePaths) {
                        myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + serviceManager.getFileService().getImagesDirectory(proz) + proz.getTitle()
                            + DIRECTORY_SUFFIX);
                } else {
                    newmd.setValue("file://" + serviceManager.getFileService().getImagesDirectory(proz)
                            + proz.getTitle() + DIRECTORY_SUFFIX);
                }
                myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
                serviceManager.getFileService().writeMetadataFile(myRdf, proz);
                Helper.setMeldung("kitodoScriptfield", "ImagePath updated: ", proz.getTitle());

            } catch (DocStructHasNoTypeException | UghHelperException | MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung(e.getMessage());
            } catch (Exception e) {
                Helper.setFehlerMeldung("kitodoScriptfield", "Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("kitodoScriptfield", "", "updateImagePath finished");

    }

    private void exportDms(List<Process> processes, String exportImages, boolean exportFulltext) {
        boolean withoutImages = exportImages != null && exportImages.equals("false");
        for (Process process : processes) {
            try {
                ExportDms dms = new ExportDms(!withoutImages);
                if (withoutImages) {
                    dms.setExportFullText(exportFulltext);
                }
                dms.startExport(process);
            } catch (DocStructHasNoTypeException | PreferencesException | WriteException
                    | MetadataTypeNotAllowedException | ReadException | TypeNotAllowedForParentException | IOException
                    | ExportFileException e) {
                logger.error(e);
            }
        }
    }
}

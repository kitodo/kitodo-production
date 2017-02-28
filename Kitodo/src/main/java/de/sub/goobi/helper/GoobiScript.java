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
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.helper.tasks.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import org.goobi.io.SafeFile;

import org.hibernate.Hibernate;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.services.ProcessService;
import org.kitodo.services.RulesetService;
import org.kitodo.services.TaskService;
import org.kitodo.services.UserGroupService;
import org.kitodo.services.UserService;

import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

//TODO: Delete me, this should be part of the Plugins...
//TODO: Break this up into multiple classes with a common interface
//TODO: add funny observer pattern here for more complexity
//TODO: add some general mechanism for string-output of goobi scripts in jsp

public class GoobiScript {
    HashMap<String, String> myParameters;
    private static final Logger logger = Logger.getLogger(GoobiScript.class);
	private ProcessService processService = new ProcessService();
	private RulesetService rulesetService = new RulesetService();
	private TaskService taskService = new TaskService();
	private UserService userService = new UserService();
	private UserGroupService userGroupService = new UserGroupService();
    public final static String DIRECTORY_SUFFIX = "_tif";

    /**
     * Starten des Scripts.
     */
    public void execute(List<Process> inProzesse, String inScript) {
        this.myParameters = new HashMap<String, String>();
        /*
         * alle Suchparameter zerlegen und erfassen
         */
        StrTokenizer tokenizer = new StrTokenizer(inScript, ' ', '\"');
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken();
            if (tok.indexOf(":") == -1) {
                Helper.setFehlerMeldung("goobiScriptfield", "missing delimiter / unknown parameter: ",
						tok);
            } else {
                String myKey = tok.substring(0, tok.indexOf(":"));
                String myValue = tok.substring(tok.indexOf(":") + 1);
                this.myParameters.put(myKey, myValue);
            }
        }

        /*
         * die passende Methode mit den richtigen Parametern übergeben
         */
        if (this.myParameters.get("action") == null) {
            Helper.setFehlerMeldung(
                    "goobiScriptfield",
                    "missing action",
                    " - possible: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
            return;
        }

        /*
         * Aufruf der richtigen Methode über den Parameter
         */
        if (this.myParameters.get("action").equals("swapSteps")) {
            swapSteps(inProzesse);
        } else if (this.myParameters.get("action").equals("swapProzessesOut")) {
            swapOutProzesses(inProzesse);
        } else if (this.myParameters.get("action").equals("swapProzessesIn")) {
            swapInProzesses(inProzesse);
        } else if (this.myParameters.get("action").equals("importFromFileSystem")) {
            importFromFileSystem(inProzesse);
        } else if (this.myParameters.get("action").equals("addUser")) {
            adduser(inProzesse);
        } else if (this.myParameters.get("action").equals("addUserGroup")) {
            addusergroup(inProzesse);
        } else if (this.myParameters.get("action").equals("setTaskProperty")) {
            setTaskProperty(inProzesse);
        } else if (this.myParameters.get("action").equals("deleteStep")) {
            deleteStep(inProzesse);
        } else if (this.myParameters.get("action").equals("addStep")) {
            addStep(inProzesse);
        } else if (this.myParameters.get("action").equals("setStepNumber")) {
            setStepNumber(inProzesse);
        } else if (this.myParameters.get("action").equals("setStepStatus")) {
            setStepStatus(inProzesse);
        } else if (this.myParameters.get("action").equals("addShellScriptToStep")) {
            addShellScriptToStep(inProzesse);
        } else if (this.myParameters.get("action").equals("addModuleToStep")) {
            addModuleToStep(inProzesse);
        } else if (this.myParameters.get("action").equals("updateImagePath")) {
            updateImagePath(inProzesse);
        } else if (this.myParameters.get("action").equals("updateContentFiles")) {
            updateContentFiles(inProzesse);
        } else if (this.myParameters.get("action").equals("deleteTiffHeaderFile")) {
            deleteTiffHeaderFile(inProzesse);
        } else if (this.myParameters.get("action").equals("setRuleset")) {
            setRuleset(inProzesse);
        } else if (this.myParameters.get("action").equals("exportDms")) {
            exportDms(inProzesse, this.myParameters.get("exportImages"), true);
        } else if (this.myParameters.get("action").equals("export")) {
            exportDms(inProzesse, this.myParameters.get("exportImages"), Boolean.valueOf(this.myParameters.get("exportOcr")));
        } else if (this.myParameters.get("action").equals("doit")) {
            exportDms(inProzesse, "false", false);
        } else if (this.myParameters.get("action").equals("doit2")) {
            exportDms(inProzesse, "false", true);

        } else if (this.myParameters.get("action").equals("runscript")) {
            String stepname = this.myParameters.get("stepname");
            String scriptname = this.myParameters.get("script");
            if (stepname == null) {
                Helper.setFehlerMeldung("goobiScriptfield", "", "Missing parameter");
            } else {
                runScript(inProzesse, stepname, scriptname);
            }
        } else if (this.myParameters.get("action").equals("deleteProcess")) {
            String value = myParameters.get("contentOnly");
            boolean contentOnly = true;
            if (value != null && value.equalsIgnoreCase("false")) {
                contentOnly = false;
            }
            deleteProcess(inProzesse, contentOnly);
        } else {
            Helper.setFehlerMeldung(
                    "goobiScriptfield",
                    "Unknown action",
                    " - use: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
            return;
        }

        Helper.setMeldung("goobiScriptfield", "", "GoobiScript finished");
    }

    private void updateContentFiles(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {
                Fileformat myRdf = processService.readMetadataFile(proz);
                myRdf.getDigitalDocument().addAllContentFiles();
                processService.writeMetadataFile(myRdf, proz);
                Helper.setMeldung("goobiScriptfield", "ContentFiles updated: ", proz.getTitle());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating content files", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "updateContentFiles finished");
    }
    
    private void deleteProcess(List<Process> inProzesse, boolean contentOnly) {
        for (Process p : inProzesse) {
            String title = p.getTitle();
            if (contentOnly) {
                try {
                    SafeFile ocr = new SafeFile(processService.getOcrDirectory(p));
                    if (ocr.exists()) {
                        ocr.deleteDir();
                    }
                    SafeFile images = new SafeFile(processService.getImagesDirectory(p));
                    if (images.exists()) {
                        images.deleteDir();
                    }
                    Helper.setMeldung("Content deleted for " + title);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Can not delete content for " + p.getTitle(), e);
                }
            }
            if (!contentOnly) {
                deleteMetadataDirectory(p);
                try {
                    processService.remove(p);
                    Helper.setMeldung("Process " + title + " deleted.");
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("could not delete process " + p.getTitle(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("could not delete document for process " + p.getTitle(), e);
                }
            }
        }
    }

    private void deleteMetadataDirectory(Process p) {
        try {
            new SafeFile(processService.getProcessDataDirectory(p)).deleteDir();
            SafeFile ocr = new SafeFile(processService.getOcrDirectory(p));
            if (ocr.exists()) {
                ocr.deleteDir();
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
    }

    private void runScript(List<Process> inProzesse, String stepname, String scriptname) {
        HelperSchritteWithoutHibernate hs = new HelperSchritteWithoutHibernate();
        for (Process p : inProzesse) {
            for (Task step : p.getTasks()) {
                if (step.getTitle().equalsIgnoreCase(stepname)) {
                    StepObject so = StepManager.getStepById(step.getId());
                    if (scriptname != null) {
                        if (taskService.getAllScripts(step).containsKey(scriptname)) {
                            String path = taskService.getAllScripts(step).get(scriptname);
                            hs.executeScriptForStepObject(so, path, false);
                        }
                    } else {
                        hs.executeAllScriptsForStep(so, false);
                    }
                }
            }
        }

    }

    /**
     * Prozesse auslagern,
     */
    private void swapOutProzesses(List<Process> inProzesse) {
        for (Process p : inProzesse) {
            ProcessSwapOutTask task = new ProcessSwapOutTask();
            task.initialize(p);
			TaskManager.addTask(task);
			task.start();
        }
    }

    /**
     * Prozesse wieder einlagern,
     */
    private void swapInProzesses(List<Process> inProzesse) {
        for (Process p : inProzesse) {
            ProcessSwapInTask task = new ProcessSwapInTask();
            task.initialize(p);
			TaskManager.addTask(task);
			task.start();
        }
    }

    /**
     * von allen gewählten Prozessen die Daten aus einem Verzeichnis einspielen.
     */
    private void importFromFileSystem(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("sourcefolder") == null || this.myParameters.get("sourcefolder").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "missing parameter: ", "sourcefolder");
            return;
        }

        SafeFile sourceFolder = new SafeFile(this.myParameters.get("sourcefolder"));
        if (!sourceFolder.isDirectory()) {
            Helper.setFehlerMeldung("goobiScriptfield", "Directory " + this.myParameters.get("sourcefolder") + " does not exisist");
            return;
        }
        try {
            for (Process p : inProzesse) {
                SafeFile imagesFolder = new SafeFile(processService.getImagesOrigDirectory(false, p));
                if (imagesFolder.list().length > 0) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "The process " + p.getTitle()
							+ " [" + p.getId().intValue()
                            + "] has already data in image folder");
                } else {
                    SafeFile sourceFolderProzess = new SafeFile(sourceFolder, p.getTitle());
                    if (!sourceFolder.isDirectory()) {
                        Helper.setFehlerMeldung("goobiScriptfield", "", "The directory for process "
								+ p.getTitle() + " [" + p.getId().intValue()
                                + "] is not existing");
                    } else {
                    	sourceFolderProzess.copyDir(imagesFolder);
                        Helper.setMeldung("goobiScriptfield", "", "The directory for process " + p.getTitle()
								+ " [" + p.getId().intValue()
                                + "] is copied");
                    }
                    Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitle() + " ["
							+ p.getId().intValue() + "] is copied");
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Regelsatz setzen.
     */
    private void setRuleset(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("ruleset") == null || this.myParameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
            return;
        }

        try {
            List<Ruleset> rulesets = rulesetService.search("from Ruleset where title='"
					+ this.myParameters.get("ruleset") + "'");
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", "ruleset");
                return;
            }
            Ruleset regelsatz = rulesets.get(0);

            for (Process p : inProzesse) {
                p.setRuleset(regelsatz);
                processService.save(p);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Tauschen zweier Schritte gegeneinander.
     */
    private void swapSteps(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("swap1nr") == null || this.myParameters.get("swap1nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1nr");
            return;
        }
        if (this.myParameters.get("swap2nr") == null || this.myParameters.get("swap2nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2nr");
            return;
        }
        if (this.myParameters.get("swap1title") == null || this.myParameters.get("swap1title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1title");
            return;
        }
        if (this.myParameters.get("swap2title") == null || this.myParameters.get("swap2title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2title");
            return;
        }
        int reihenfolge1;
        int reihenfolge2;
        try {
            reihenfolge1 = Integer.parseInt(this.myParameters.get("swap1nr"));
            reihenfolge2 = Integer.parseInt(this.myParameters.get("swap2nr"));
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", this.myParameters.get("swap1nr") + " - "
                    + this.myParameters.get("swap2nr"));
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            /*
             * Swapsteps
             */
            Task s1 = null;
            Task s2 = null;
            for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                Task s = iterator.next();
                if (s.getTitle().equals(this.myParameters.get("swap1title")) && s.getOrdering() == reihenfolge1) {
                    s1 = s;
                }
                if (s.getTitle().equals(this.myParameters.get("swap2title")) && s.getOrdering() == reihenfolge2) {
                    s2 = s;
                }
            }
            if (s1 != null && s2 != null) {
                TaskStatus statustemp = s1.getProcessingStatusEnum();
                s1.setProcessingStatusEnum(s2.getProcessingStatusEnum());
                s2.setProcessingStatusEnum(statustemp);
                s1.setOrdering(reihenfolge2);
                s2.setOrdering(reihenfolge1);
                try {
                    taskService.save(s1);
                    taskService.save(s2);
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("goobiScriptfield", "Error on save while swapping steps in process: ",
							proz.getTitle() + " - " + s1.getTitle() + " : " + s2.getTitle());
                    logger.error("Error on save while swapping process: " + proz.getTitle() + " - " + s1.getTitle()
							+ " : " + s2.getTitle(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("goobiScriptfield", "Error on insert while swapping steps in process: ",
                            proz.getTitle() + " - " + s1.getTitle() + " : " + s2.getTitle());
                    logger.error("Error on insert while swapping process: " + proz.getTitle() + " - " + s1.getTitle()
                            + " : " + s2.getTitle(), e);
                }

                Helper.setMeldung("goobiScriptfield", "Swapped steps in: ", proz.getTitle());
            }

        }
        Helper.setMeldung("goobiScriptfield", "swapsteps finished: ");
    }

    /**
     * Schritte löschen.
     */
    private void deleteStep(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            if (proz.getTasks() != null) {
                for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                    Task s = iterator.next();
                    if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                        proz.getTasks().remove(s);
                        try {
                            processService.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
									+ proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: "
									+ proz.getTitle(), e);
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Removed step from process: ",
								proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteStep finished: ");
    }

    /**
     * Schritte hinzufuegen.
     */
    private void addStep(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            Task s = new Task();
            s.setTitle(this.myParameters.get("steptitle"));
            s.setOrdering(Integer.parseInt(this.myParameters.get("number")));
            s.setProcess(proz);
            if (proz.getTasks() == null) {
                proz.setTasks(new ArrayList<Task>());
            }
            proz.getTasks().add(s);
            try {
               processService.save(proz);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitle(), e);
                logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitle(), e);
            } catch (IOException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                        + proz.getTitle(), e);
                logger.error("goobiScriptfield" + "Error while inserting to index process: " + proz.getTitle(), e);
            }
            Helper.setMeldung("goobiScriptfield", "Added step to process: ", proz.getTitle());
        }
        Helper.setMeldung("goobiScriptfield", "", "addStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen.
     */
    private void addShellScriptToStep(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("label") == null || this.myParameters.get("label").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "label");
            return;
        }

        if (this.myParameters.get("script") == null || this.myParameters.get("script").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            if (proz.getTasks() != null) {
                for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                    Task s = iterator.next();
                    if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                        s.setTypeAutomaticScriptPath(this.myParameters.get("script"));
                        s.setScriptName1(this.myParameters.get("label"));
                        s.setTypeScriptStep(true);
                        try {
                            processService.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
									+ proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: "
									+ proz.getTitle(), e);
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added script to step: ", proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addShellScriptToStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen.
     */
    private void addModuleToStep(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("module") == null || this.myParameters.get("module").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "module");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            if (proz.getTasks() != null) {
                for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                    Task s = iterator.next();
                    if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                        s.setTypeModuleName(this.myParameters.get("module"));
                        try {
                            processService.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
									+ proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: "
									+ proz.getTitle(), e);
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added module to step: ", proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addModuleToStep finished: ");
    }

    /**
     * Flag von Schritten setzen.
     */
    private void setTaskProperty(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("property") == null || this.myParameters.get("property").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
            return;
        }

        if (this.myParameters.get("value") == null || this.myParameters.get("value").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
            return;
        }

        String property = this.myParameters.get("property");
        String value = this.myParameters.get("value");

        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
                && !property.equals("exportdms") && !property.equals("batch") && !property.equals("automatic")) {
            Helper.setFehlerMeldung("goobiScriptfield", "",
                    "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
            return;
        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "wrong parameter 'value'; possible values: true, false");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            if (proz.getTasks() != null) {
                for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                    Task s = iterator.next();
                    if (s.getTitle().equals(this.myParameters.get("steptitle"))) {

                        if (property.equals("metadata")) {
                            s.setTypeMetadata(Boolean.parseBoolean(value));
                        }
                        if (property.equals("automatic")) {
                            s.setTypeAutomatic(Boolean.parseBoolean(value));
                        }
                        if (property.equals("batch")) {
                            s.setBatchStep(Boolean.parseBoolean(value));
                        }
                        if (property.equals("readimages")) {
                            s.setTypeImagesRead(Boolean.parseBoolean(value));
                        }
                        if (property.equals("writeimages")) {
                            s.setTypeImagesWrite(Boolean.parseBoolean(value));
                        }
                        if (property.equals("validate")) {
                            s.setTypeCloseVerify(Boolean.parseBoolean(value));
                        }
                        if (property.equals("exportdms")) {
                            s.setTypeExportDMS(Boolean.parseBoolean(value));
                        }

                        try {
                            processService.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
									+ proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: "
									+ proz.getTitle(), e);
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while inserting to index process: "
                                    + proz.getTitle(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Error while saving process: ", proz.getTitle());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setTaskProperty abgeschlossen: ");
    }

    /**
     * Schritte auf bestimmten Status setzen.
     */
    private void setStepStatus(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("status") == null || this.myParameters.get("status").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
            return;
        }

        if (!this.myParameters.get("status").equals("0") && !this.myParameters.get("status").equals("1")
                && !this.myParameters.get("status").equals("2") && !this.myParameters.get("status").equals("3")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ", "(possible: 0=closed, 1=open, 2=in work, 3=finished");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                Task s = iterator.next();
                if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                    taskService.setProcessingStatusAsString(this.myParameters.get("status"));
                    try {
                        taskService.save(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
								+ proz.getTitle(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: "
								+ proz.getTitle(), e);
                    } catch (IOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                + proz.getTitle(), e);
                        logger.error("goobiScriptfield" + "Error while inserting to index  process: "
                                + proz.getTitle(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "stepstatus set in process: ", proz.getTitle());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepStatus finished: ");
    }

    /**
     * Schritte auf bestimmten Reihenfolge setzen.
     */
    private void setStepNumber(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                Task s = iterator.next();
                if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                    s.setOrdering(Integer.parseInt(this.myParameters.get("number")));
                    try {
                        taskService.save(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: "
								+ proz.getTitle(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: "
								+ proz.getTitle(), e);
                    } catch (IOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting to index process: "
                                + proz.getTitle(), e);
                        logger.error("goobiScriptfield" + "Error while inserting to index process: "
                                + proz.getTitle(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "step order changed in process: ", proz.getTitle());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepNumber finished ");
    }

    /**
     * Benutzer zu Schritt hinzufügen.
     */
    private void adduser(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("username") == null || this.myParameters.get("username").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        User myUser = null;
        try {
            List<User> treffer = userService.search("from User where login='"
					+ this.myParameters.get("username") + "'");
            if (treffer != null && treffer.size() > 0) {
                myUser = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", this.myParameters.get("username"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
            logger.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                Task s = iterator.next();
                if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                    List<User> myBenutzer = s.getUsers();
                    if (myBenutzer == null) {
                        myBenutzer = new ArrayList<>();
                        s.setUsers(myBenutzer);
                    }
                    if (!myBenutzer.contains(myUser)) {
                        myBenutzer.add(myUser);
                        try {
                            taskService.save(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while saving - " + proz.getTitle(), e);
                            return;
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting - " + proz.getTitle(), e);
                            logger.error("goobiScriptfield" + "Error while inserting - " + proz.getTitle(), e);
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "Added user to step: ", proz.getTitle());
        }
        Helper.setMeldung("goobiScriptfield", "", "adduser finished.");
    }

    /**
     * Benutzergruppe zu Schritt hinzufügen.
     */
    private void addusergroup(List<Process> inProzesse) {
        /*
         * Validierung der Actionparameter
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("group") == null || this.myParameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        UserGroup myGroup = null;
        try {
            List<UserGroup> treffer =
                    userGroupService.search("from UserGroup where title='" + this.myParameters.get("group") + "'");
            if (treffer != null && treffer.size() > 0) {
                myGroup = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", this.myParameters.get("group"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.addusergroup", e);
            return;
        }

        /*
         * Durchführung der Action
         */
        for (Process proz : inProzesse) {
            for (Iterator<Task> iterator = proz.getTasks().iterator(); iterator.hasNext();) {
                Task s = iterator.next();
                if (s.getTitle().equals(this.myParameters.get("steptitle"))) {
                    List<UserGroup> myBenutzergruppe = s.getUserGroups();
                    if (myBenutzergruppe == null) {
                        myBenutzergruppe = new ArrayList<>();
                        s.setUserGroups(myBenutzergruppe);
                    }
                    if (!myBenutzergruppe.contains(myGroup)) {
                        myBenutzergruppe.add(myGroup);
                        try {
                            taskService.save(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitle(), e);
                            return;
                        } catch (IOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while inserting - "
                                    + proz.getTitle(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "added usergroup to step: ", proz.getTitle());
        }
        Helper.setMeldung("goobiScriptfield", "", "addusergroup finished");
    }

    /**
     * TiffHeader von den Prozessen löschen.
     */
    public void deleteTiffHeaderFile(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {
                SafeFile tiffheaderfile = new SafeFile(processService.getImagesDirectory(proz)
						+ "tiffwriter.conf");
                if (tiffheaderfile.exists()) {
                    tiffheaderfile.delete();
                }
                Helper.setMeldung("goobiScriptfield", "TiffHeaderFile deleted: ", proz.getTitle());
            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteTiffHeaderFile finished");
    }

    /**
     * Imagepfad in den Metadaten neu setzen (evtl. vorhandene zunächst löschen).
     */
    public void updateImagePath(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {
                Fileformat myRdf = processService.readMetadataFile(proz);
                MetadataType mdt = UghHelper.getMetadataType(proz, "pathimagefiles");
                List<? extends ugh.dl.Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                if (alleImagepfade.size() > 0) {
                    for (Metadata md : alleImagepfade) {
                        myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + processService.getImagesDirectory(proz) + proz.getTitle()
							+ DIRECTORY_SUFFIX);
                } else {
                    newmd.setValue("file://" + processService.getImagesDirectory(proz) + proz.getTitle()
							+ DIRECTORY_SUFFIX);
                }
                myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
                processService.writeMetadataFile(myRdf, proz);
                Helper.setMeldung("goobiScriptfield", "ImagePath updated: ", proz.getTitle());

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("UghHelperException", e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("goobiScriptfield", "", "updateImagePath finished");

    }

    private void exportDms(List<Process> processes, String exportImages, boolean exportFulltext) {
		boolean withoutImages = exportImages != null && exportImages.equals("false");
        for (Process prozess : processes) {
            try {
				Hibernate.initialize(prozess.getProject());
				Hibernate.initialize(prozess.getProject().getProjectFileGroups());
				Hibernate.initialize(prozess.getRuleset());
				ExportDms dms = new ExportDms(!withoutImages);
				if (withoutImages) {
					dms.setExportFullText(exportFulltext);
				}
                dms.startExport(prozess);
            } catch (DocStructHasNoTypeException e) {
                logger.error("DocStructHasNoTypeException", e);
            } catch (PreferencesException e) {
                logger.error("PreferencesException", e);
            } catch (WriteException e) {
                logger.error("WriteException", e);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("MetadataTypeNotAllowedException", e);
            } catch (ReadException e) {
                logger.error("ReadException", e);
            } catch (TypeNotAllowedForParentException e) {
                logger.error("TypeNotAllowedForParentException", e);
            } catch (IOException e) {
                logger.error("IOException", e);
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
            } catch (ExportFileException e) {
                logger.error("ExportFileException", e);
            } catch (UghHelperException e) {
                logger.error("UghHelperException", e);
            } catch (SwapException e) {
                logger.error("SwapException", e);
            } catch (DAOException e) {
                logger.error("DAOException", e);
            }
        }
    }
}

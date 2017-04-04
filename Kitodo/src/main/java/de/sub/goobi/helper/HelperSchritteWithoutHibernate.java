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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.dms.AutomaticDmsExportWithoutHibernate;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.tasks.EmptyTask;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.persistence.apache.FolderInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.helper.enums.HistoryType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.database.persistence.apache.ProcessObject;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.services.ServiceManager;

import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class HelperSchritteWithoutHibernate {
    private static final Logger logger = Logger.getLogger(HelperSchritteWithoutHibernate.class);
    private final ServiceManager serviceManager = new ServiceManager();
    public static final String DIRECTORY_PREFIX = "orig_";

    /**
     * The field task holds an optional task instance. Its progress and its
     * errors will be passed to the task manager screen (if available) for
     * visualisation.
     */
    private EmptyTask task;

    /**
     * Schritt abschliessen und dabei parallele Schritte berücksichtigen.
     */

    public void CloseStepObjectAutomatic(StepObject currentStep) {
        closeStepObject(currentStep, currentStep.getProcessId(), false);
    }

    public void CloseStepObjectAutomatic(StepObject currentStep, boolean requestFromGUI) {
        closeStepObject(currentStep, currentStep.getProcessId(), requestFromGUI);
    }

    private void closeStepObject(StepObject currentStep, int processId, boolean requestFromGUI) {
        if (logger.isDebugEnabled()) {
            logger.debug("closing step with id " + currentStep.getId() + " and process id " + processId);
        }
        currentStep.setProcessingStatus(3);
        Date myDate = new Date();
        logger.debug("set new date for edit time");
        currentStep.setProcessingTime(myDate);
        try {
            LoginForm lf = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
            if (lf != null) {
                User ben = lf.getMyBenutzer();
                if (ben != null) {
                    logger.debug("set new user");
                    currentStep.setProcessingUser(ben.getId());
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setException(e);
            }
            logger.debug("cannot resolve LoginForm", e);
        }
        logger.debug("set new end date");
        currentStep.setProcessingEnd(myDate);
        logger.debug("saving step");
        StepManager.updateStep(currentStep);
        List<StepObject> automatischeSchritte = new ArrayList<StepObject>();
        List<StepObject> stepsToFinish = new ArrayList<StepObject>();

        logger.debug("create history events for step");

        StepManager.addHistory(myDate, currentStep.getOrdering(), currentStep.getTitle(),
                HistoryType.taskDone.getValue(), processId);
        /*
         * prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht
         * abgeschlossen sind
         */

        List<StepObject> steps = StepManager.getStepsForProcess(processId);
        List<StepObject> allehoeherenSchritte = new ArrayList<StepObject>();
        int offeneSchritteGleicherReihenfolge = 0;
        for (StepObject so : steps) {
            if (so.getOrdering() == currentStep.getOrdering() && so.getProcessingStatus() != 3
                    && so.getId() != currentStep.getId()) {
                offeneSchritteGleicherReihenfolge++;
            } else if (so.getOrdering() > currentStep.getOrdering()) {
                allehoeherenSchritte.add(so);
            }
        }
        /*
         * wenn keine offenen parallelschritte vorhanden sind, die nächsten
         * Schritte aktivieren
         */
        if (offeneSchritteGleicherReihenfolge == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("found " + allehoeherenSchritte.size() + " tasks");
            }
            int reihenfolge = 0;
            boolean matched = false;
            for (StepObject myStep : allehoeherenSchritte) {
                if (reihenfolge < myStep.getOrdering() && !matched) {
                    reihenfolge = myStep.getOrdering();
                }

                if (reihenfolge == myStep.getOrdering() && myStep.getProcessingStatus() != 3
                        && myStep.getProcessingStatus() != 2) {
                    /*
                     * den Schritt aktivieren, wenn es kein vollautomatischer
                     * ist
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("open step " + myStep.getTitle());
                    }
                    myStep.setProcessingStatus(1);
                    myStep.setProcessingTime(myDate);
                    myStep.setEditType(4);
                    logger.debug("create history events for next step");
                    StepManager.addHistory(myDate, myStep.getOrdering(), myStep.getTitle(),
                            HistoryType.taskOpen.getValue(), processId);
                    /* wenn es ein automatischer Schritt mit Script ist */
                    if (logger.isDebugEnabled()) {
                        logger.debug("check if step is an automatic task: " + myStep.isTypeAutomatic());
                    }
                    if (myStep.isTypeAutomatic()) {
                        logger.debug("add step to list of automatic tasks");
                        automatischeSchritte.add(myStep);
                    } else if (myStep.isTypeFinishImmediately()) {
                        stepsToFinish.add(myStep);
                    }
                    logger.debug("");
                    StepManager.updateStep(myStep);
                    matched = true;

                } else {
                    if (matched) {
                        break;
                    }
                }
            }
        }
        ProcessObject po = ProcessManager.getProcessObjectForId(processId);
        FolderInformation fi = new FolderInformation(po.getId(), po.getTitle());
        if (po.getSortHelperImages() != FileUtils.getNumberOfFiles(new SafeFile(fi.getImagesOrigDirectory(true)))) {
            ProcessManager.updateImages(FileUtils.getNumberOfFiles(new SafeFile(fi.getImagesOrigDirectory(true))),
                    processId);
        }
        logger.debug("update process status");
        updateProcessStatus(processId);
        if (logger.isDebugEnabled()) {
            logger.debug("start " + automatischeSchritte.size() + " automatic tasks");
        }
        for (StepObject automaticStep : automatischeSchritte) {
            if (logger.isDebugEnabled()) {
                logger.debug("creating scripts task for step with stepId " + automaticStep.getId() + " and processId "
                        + automaticStep.getProcessId());
            }
            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(automaticStep);
            TaskManager.addTask(myThread);
        }
        for (StepObject finish : stepsToFinish) {
            if (logger.isDebugEnabled()) {
                logger.debug("closing task " + finish.getTitle());
            }
            CloseStepObjectAutomatic(finish);
        }
        // TODO remove this later
        try {
            logger.debug("update hibernate cache");
            if (requestFromGUI && ConfigCore.getBooleanParameter("DatabaseShareHibernateSessionWithUser", true)) {
                RefreshObject.refreshProcess_GUI(processId);
            } else {
                if (ConfigCore.getBooleanParameter("DatabaseRefreshSessionWithoutUser", true)) {
                    RefreshObject.refreshProcess(processId);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error("Exception during update of hibernate cache", e);
        }
    }

    /**
     * Update process status.
     *
     * @param processId
     *            int
     */
    public void updateProcessStatus(int processId) {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        List<StepObject> stepsForProcess = StepManager.getStepsForProcess(processId);
        for (StepObject step : stepsForProcess) {
            if (step.getProcessingStatus() == 3) {
                abgeschlossen++;
            } else if (step.getProcessingStatus() == 0) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        double offen2 = 0;
        double inBearbeitung2 = 0;
        double abgeschlossen2 = 0;

        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }

        offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        abgeschlossen2 = 100 - offen2 - inBearbeitung2;
        // (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        String value = df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);

        ProcessManager.updateProcessStatus(value, processId);
    }

    /**
     * Execute all scripts for step.
     *
     * @param step
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeAllScriptsForStep(StepObject step, boolean automatic) {
        List<String> scriptpaths = StepManager.loadScripts(step.getId());
        int count = 1;
        int size = scriptpaths.size();
        int returnParameter = 0;
        for (String script : scriptpaths) {
            if (logger.isDebugEnabled()) {
                logger.debug("starting script " + script);
            }
            if (returnParameter != 0) {
                abortStep(step);
                break;
            }
            if (script != null && !script.equals(" ") && script.length() != 0) {
                if (task != null) {
                    task.setWorkDetail(script);
                }
                returnParameter = executeScriptForStepObject(step, script, automatic && (count == size));
            }
            if (task != null) {
                task.setProgress((int) (100d * count / size));
            }
            count++;
        }
    }

    /**
     * Execute script for StepObject.
     *
     * @param step
     *            StepObject
     * @param script
     *            String
     * @param automatic
     *            boolean
     * @return int
     */
    public int executeScriptForStepObject(StepObject step, String script, boolean automatic) {
        if (script == null || script.length() == 0) {
            return -1;
        }
        script = script.replace("{", "(").replace("}", ")");
        DigitalDocument dd = null;
        ProcessObject po = ProcessManager.getProcessObjectForId(step.getProcessId());

        FolderInformation fi = new FolderInformation(po.getId(), po.getTitle());
        Prefs prefs = serviceManager.getRulesetService().getPreferences(ProcessManager.getRuleset(po.getRulesetId()));

        try {
            dd = po.readMetadataFile(fi.getMetadataFilePath(), prefs).getDigitalDocument();
        } catch (PreferencesException e2) {
            if (task != null) {
                task.setException(e2);
            }
            logger.error(e2);
        } catch (ReadException e2) {
            if (task != null) {
                task.setException(e2);
            }
            logger.error(e2);
        } catch (IOException e2) {
            if (task != null) {
                task.setException(e2);
            }
            logger.error(e2);
        }
        VariableReplacerWithoutHibernate replacer = new VariableReplacerWithoutHibernate(dd, prefs, po, step);

        script = replacer.replace(script);
        int rueckgabe = -1;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Calling the shell: " + script);
            }
            rueckgabe = ShellScript.legacyCallShell2(script);
            if (automatic) {
                if (rueckgabe == 0) {
                    step.setEditType(TaskEditType.AUTOMATIC.getValue());
                    step.setProcessingStatus(TaskStatus.DONE.getValue());
                    if (step.getValidationPlugin() != null && step.getValidationPlugin().length() > 0) {
                        IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation,
                                step.getValidationPlugin());
                        ivp.setStepObject(step);
                        if (!ivp.validate()) {
                            step.setProcessingStatus(TaskStatus.OPEN.getValue());
                            StepManager.updateStep(step);
                        } else {
                            CloseStepObjectAutomatic(step);
                        }
                    } else {
                        CloseStepObjectAutomatic(step);
                    }

                } else {
                    step.setEditType(TaskEditType.AUTOMATIC.getValue());
                    step.setProcessingStatus(TaskStatus.OPEN.getValue());
                    StepManager.updateStep(step);
                }
            }
        } catch (IOException e) {
            if (task != null) {
                task.setException(e);
            }
            Helper.setFehlerMeldung("IOException: ", e.getMessage());
        } catch (InterruptedException e) {
            if (task != null) {
                task.setException(e);
            }
            Helper.setFehlerMeldung("InterruptedException: ", e.getMessage());
        }
        return rueckgabe;
    }

    /**
     * Execute DMS export.
     *
     * @param step
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeDmsExport(StepObject step, boolean automatic) {
        AutomaticDmsExportWithoutHibernate dms = new AutomaticDmsExportWithoutHibernate(
                ConfigCore.getBooleanParameter("automaticExportWithImages", true));
        if (!ConfigCore.getBooleanParameter("automaticExportWithOcr", true)) {
            dms.setExportFulltext(false);
        }
        dms.setTask(task);
        ProcessObject po = ProcessManager.getProcessObjectForId(step.getProcessId());
        try {
            boolean validate = dms.startExport(po);
            if (validate) {
                CloseStepObjectAutomatic(step);
            } else {
                abortStep(step);
            }
        } catch (DAOException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (PreferencesException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (WriteException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (SwapException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (TypeNotAllowedForParentException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (IOException e) {
            if (task != null) {
                task.setException(e);
            }
            logger.error(e);
            abortStep(step);
            return;
        } catch (InterruptedException e) {
            // validation error
            if (task != null) {
                task.setException(e);
            }
            abortStep(step);
            return;
        }

    }

    private void abortStep(StepObject step) {

        step.setProcessingStatus(TaskStatus.OPEN.getValue());
        step.setEditType(TaskEditType.AUTOMATIC.getValue());

        StepManager.updateStep(step);
    }

    /**
     * The method setTask() can be used to pass in a task instance. If that is
     * passed in, the progress in it will be updated during processing and
     * occurring errors will be passed to it to be visible in the task manager
     * screen.
     *
     * @param obj
     *            task object to submit progress updates and errors to
     */
    public void setTask(EmptyTask obj) {
        this.task = obj;
    }
}

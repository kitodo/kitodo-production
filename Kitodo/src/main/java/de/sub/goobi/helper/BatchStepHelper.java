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
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.forms.AktuelleSchritteForm;
import de.sub.goobi.metadaten.MetadatenImagesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.naming.AuthenticationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class BatchStepHelper {
    private List<Task> steps;
    private static final Logger logger = LogManager.getLogger(BatchStepHelper.class);
    private Task currentStep;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;
    private Map<Integer, PropertyListObject> containers = new TreeMap<>();
    private Integer container;
    private String myProblemStep;
    private String mySolutionStep;
    private String problemMessage;
    private String solutionMessage;
    private String processName = "";
    private String addToWikiField = "";
    private String script;
    private final ServiceManager serviceManager = new ServiceManager();
    private final WebDav myDav = new WebDav();
    private List<String> processNameList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param steps
     *            list of tasks
     */
    public BatchStepHelper(List<Task> steps) {
        this.steps = steps;
        for (Task s : steps) {
            this.processNameList.add(s.getProcess().getTitle());
        }
        if (steps.size() > 0) {
            this.currentStep = steps.get(0);
            this.processName = this.currentStep.getProcess().getTitle();
            loadProcessProperties(this.currentStep);
        }
    }

    public List<Task> getSteps() {
        return this.steps;
    }

    public void setSteps(List<Task> steps) {
        this.steps = steps;
    }

    public Task getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(Task currentStep) {
        this.currentStep = currentStep;
    }

    /*
     * properties
     */

    public ProcessProperty getProcessProperty() {
        return this.processProperty;
    }

    public void setProcessProperty(ProcessProperty processProperty) {
        this.processProperty = processProperty;
    }

    public List<ProcessProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    public int getPropertyListSize() {
        return this.processPropertyList.size();
    }

    public List<String> getProcessNameList() {
        return this.processNameList;
    }

    public void setProcessNameList(List<String> processNameList) {
        this.processNameList = processNameList;
    }

    public String getProcessName() {
        return this.processName;
    }

    /**
     * Set process' name.
     *
     * @param processName
     *            String
     */
    public void setProcessName(String processName) {
        this.processName = processName;
        for (Task s : this.steps) {
            if (s.getProcess().getTitle().equals(processName)) {
                this.currentStep = s;
                loadProcessProperties(this.currentStep);
                break;
            }
        }
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!prepareProcessPropertyForTransfer()) {
                return;
            }
            this.processProperty.transfer();

            Process p = this.currentStep.getProcess();
            List<Property> props = p.getProperties();
            for (Property processProperty : props) {
                if (processProperty.getTitle() == null) {
                    p.getProperties().remove(processProperty);
                }
            }
            for (Process process : this.processProperty.getProzesseigenschaft().getProcesses()) {
                if (!process.getProperties().contains(this.processProperty.getProzesseigenschaft())) {
                    process.getProperties().add(this.processProperty.getProzesseigenschaft());
                }
            }
            try {
                this.serviceManager.getProcessService().save(this.currentStep.getProcess());
                Helper.setMeldung("Property saved");
            } catch (DataException e) {
                logger.error(e);
                Helper.setFehlerMeldung("Properties could not be saved");
            }
        }
    }

    /**
     * Save current property for all.
     */
    public void saveCurrentPropertyForAll() {
        boolean error = false;
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!prepareProcessPropertyForTransfer()) {
                return;
            }
            this.processProperty.transfer();

            Property processProperty = new Property();
            processProperty.setTitle(this.processProperty.getName());
            processProperty.setValue(this.processProperty.getValue());
            processProperty.setContainer(this.processProperty.getContainer());

            for (Task s : this.steps) {
                Process process = s.getProcess();
                if (!s.equals(this.currentStep)) {
                    if (processProperty.getTitle() != null) {
                        boolean match = false;
                        for (Property processPe : process.getProperties()) {
                            if (processPe.getTitle() != null) {
                                if (processProperty.getTitle().equals(processPe.getTitle())
                                        && processProperty.getContainer() == null ? processPe.getContainer() == null
                                                : processProperty.getContainer().equals(processPe.getContainer())) {
                                    processPe.setValue(processProperty.getValue());
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            Property property = new Property();
                            property.setTitle(processProperty.getTitle());
                            property.setValue(processProperty.getValue());
                            property.setContainer(processProperty.getContainer());
                            property.setType(processProperty.getType());
                            property.getProcesses().add(process);
                            process.getProperties().add(property);
                        }
                    }
                } else {
                    if (!process.getProperties().contains(this.processProperty.getProzesseigenschaft())) {
                        process.getProperties().add(this.processProperty.getProzesseigenschaft());
                    }
                }

                List<Property> props = process.getProperties();
                for (Property nextProcessProperty : props) {
                    if (nextProcessProperty.getTitle() == null) {
                        process.getProperties().remove(nextProcessProperty);
                    }
                }

                try {
                    this.serviceManager.getProcessService().save(process);
                } catch (DataException e) {
                    error = true;
                    logger.error(e);
                    Helper.setFehlerMeldung("Properties for process " + process.getTitle() + " could not be saved");
                }
            }
        }
        if (!error) {
            Helper.setMeldung("Properties saved");
        }
    }

    private boolean prepareProcessPropertyForTransfer() {
        if (!this.processProperty.isValid()) {
            Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
            return false;
        }
        if (this.processProperty.getProzesseigenschaft() == null) {
            Property processProperty = new Property();
            processProperty.getProcesses().add(this.currentStep.getProcess());
            this.processProperty.setProzesseigenschaft(processProperty);
            this.currentStep.getProcess().getProperties().add(processProperty);
        }
        return true;
    }

    private void loadProcessProperties(Task s) {
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getPropertiesForStep(s);
        List<Process> pList = new ArrayList<>();
        for (Task step : this.steps) {
            pList.add(step.getProcess());
        }
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                Property processProperty = new Property();
                processProperty.getProcesses().add(s.getProcess());
                pt.setProzesseigenschaft(processProperty);
                s.getProcess().getProperties().add(processProperty);
                pt.transfer();
            }
            if (!this.containers.keySet().contains(pt.getContainer())) {
                PropertyListObject plo = new PropertyListObject(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            } else {
                PropertyListObject plo = this.containers.get(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            }
        }

        for (Process p : pList) {
            for (Property processProperty : p.getProperties()) {
                if (!this.containers.keySet().contains(processProperty.getContainer())) {
                    this.containers.put(processProperty.getContainer(), null);
                }
            }
        }
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    /**
     * Get container size.
     *
     * @return size
     */
    public int getContainersSize() {
        if (this.containers == null) {
            return 0;
        }
        return this.containers.size();
    }

    /**
     * Get sorted properties.
     *
     * @return list of sorted properties
     */
    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    /**
     * Get containerless properties.
     *
     * @return list of containerless properties
     */
    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0 && pp.getName() != null) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public Integer getContainer() {
        return this.container;
    }

    /**
     * Set container.
     *
     * @param container
     *            Integer
     */
    public void setContainer(Integer container) {
        this.container = container;
        if (container != null && container > 0) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    /**
     * Get container properties.
     *
     * @return list of container properties
     */
    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<>();

        if (this.container != null && this.container > 0) {
            for (ProcessProperty pp : this.processPropertyList) {
                if (pp.getContainer() == this.container && pp.getName() != null) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    /**
     * Duplicate container for single.
     *
     * @return empty String
     */
    public String duplicateContainerForSingle() {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }
        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            // find new unused container number
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentProperty();
        }
        loadProcessProperties(this.currentStep);

        return "";
    }

    private void saveStep() {
        Process p = this.currentStep.getProcess();
        List<Property> props = p.getProperties();
        for (Property processProperty : props) {
            if (processProperty.getTitle() == null) {
                p.getProperties().remove(processProperty);
            }
        }
        try {
            this.serviceManager.getProcessService().save(this.currentStep.getProcess());
        } catch (DataException e) {
            logger.error(e);
        }
    }

    /**
     * Duplicate container for all.
     *
     * @return empty String
     */
    public String duplicateContainerForAll() {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }

        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentPropertyForAll();
        }
        loadProcessProperties(this.currentStep);
        return "";
    }

    /**
     * Error management for single.
     */
    public String reportProblemForSingle() {

        this.myDav.uploadFromHome(this.currentStep.getProcess());
        reportProblem();
        this.problemMessage = "";
        this.myProblemStep = "";
        saveStep();
        AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        return asf.filterAll();
    }

    /**
     * Error management for all.
     */
    public String reportProblemForAll() {
        for (Task s : this.steps) {
            this.currentStep = s;
            this.myDav.uploadFromHome(this.currentStep.getProcess());
            reportProblem();
            saveStep();
        }
        this.problemMessage = "";
        this.myProblemStep = "";
        AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        return asf.filterAll();
    }

    private void reportProblem() {
        Date myDate = new Date();
        this.currentStep.setProcessingStatusEnum(TaskStatus.LOCKED);
        this.currentStep.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentStep.setProcessingTime(new Date());
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            currentStep.setProcessingUser(ben);
        }
        this.currentStep.setProcessingBegin(null);

        try {
            Task temp = null;
            for (Task s : this.currentStep.getProcess().getTasks()) {
                if (s.getTitle().equals(this.myProblemStep)) {
                    temp = s;
                }
            }
            if (temp != null) {
                temp.setProcessingStatusEnum(TaskStatus.OPEN);
                temp = serviceManager.getTaskService().setCorrectionStep(temp);
                temp.setProcessingEnd(null);

                Property processProperty = new Property();
                processProperty.setTitle(Helper.getTranslation("Korrektur notwendig"));
                processProperty.setValue("[" + this.formatter.format(new Date()) + ", "
                        + serviceManager.getUserService().getFullName(ben) + "] " + this.problemMessage);
                processProperty.setType(PropertyType.messageError);
                processProperty.getProcesses().add(this.currentStep.getProcess());
                this.currentStep.getProcess().getProperties().add(processProperty);

                String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitle() + ": "
                        + this.problemMessage + " (" + serviceManager.getUserService().getFullName(ben) + ")";
                this.currentStep.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(this.currentStep.getProcess(),
                        this.currentStep.getProcess().getWikiField(), "error", message));

                this.serviceManager.getTaskService().save(temp);
                this.currentStep.getProcess().getHistory().add(new History(myDate, temp.getOrdering().doubleValue(),
                        temp.getTitle(), HistoryTypeEnum.taskError, temp.getProcess()));
                /*
                 * alle Schritte zwischen dem aktuellen und dem Korrekturschritt
                 * wieder schliessen
                 */
                List<Task> tasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(
                        this.currentStep.getOrdering(), temp.getOrdering(), this.currentStep.getProcess().getId());
                for (Task task : tasksInBetween) {
                    task.setProcessingStatusEnum(TaskStatus.LOCKED);
                    task = serviceManager.getTaskService().setCorrectionStep(task);
                    task.setProcessingEnd(null);
                }
            }
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper
             * gespeichert wird
             */
        } catch (DataException e) {
            logger.error(e);
        }
    }

    /**
     * Get previous tasks for problem reporting.
     *
     * @return list of selected items
     */
    public List<SelectItem> getPreviousStepsForProblemReporting() {
        List<SelectItem> answer = new ArrayList<>();
        List<Task> previousTasksForProblemReporting = serviceManager.getTaskService()
                .getPreviousTasksForProblemReporting(this.currentStep.getOrdering(),
                        this.currentStep.getProcess().getId());
        for (Task task : previousTasksForProblemReporting) {
            answer.add(new SelectItem(task.getTitle(), serviceManager.getTaskService().getTitleWithUserName(task)));
        }
        return answer;
    }

    /**
     * Get next tasks for problem solution.
     *
     * @return list of selected items
     */
    public List<SelectItem> getNextStepsForProblemSolution() {
        List<SelectItem> answer = new ArrayList<>();
        List<Task> nextTasksForProblemSolution = serviceManager.getTaskService()
                .getNextTasksForProblemSolution(this.currentStep.getOrdering(), this.currentStep.getProcess().getId());
        for (Task task : nextTasksForProblemSolution) {
            answer.add(new SelectItem(task.getTitle(), serviceManager.getTaskService().getTitleWithUserName(task)));
        }
        return answer;
    }

    /**
     * Solve problem for single.
     *
     * @return String
     */
    public String solveProblemForSingle() {
        try {
            solveProblem();
            saveStep();
            this.solutionMessage = "";
            this.mySolutionStep = "";

            AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
            return asf.filterAll();
        } catch (AuthenticationException e) {
            Helper.setFehlerMeldung(e.getMessage());
            return "";
        }
    }

    /**
     * Solve problem for all.
     *
     * @return String
     */
    public String solveProblemForAll() {
        try {
            for (Task s : this.steps) {
                this.currentStep = s;
                solveProblem();
                saveStep();
            }
            this.solutionMessage = "";
            this.mySolutionStep = "";

            AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
            return asf.filterAll();
        } catch (AuthenticationException e) {
            Helper.setFehlerMeldung(e.getMessage());
            return "";
        }
    }

    private void solveProblem() throws AuthenticationException {
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben == null) {
            throw new AuthenticationException("userNotFound");
        }
        Date now = new Date();
        this.myDav.uploadFromHome(this.currentStep.getProcess());
        this.currentStep.setProcessingStatusEnum(TaskStatus.DONE);
        this.currentStep.setProcessingEnd(now);
        this.currentStep.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentStep.setProcessingTime(new Date());
        currentStep.setProcessingUser(ben);

        try {
            Task temp = null;
            for (Task task : this.currentStep.getProcess().getTasks()) {
                if (task.getTitle().equals(this.mySolutionStep)) {
                    temp = task;
                }
            }
            if (temp != null) {
                /*
                 * alle Schritte zwischen dem aktuellen und dem Korrekturschritt
                 * wieder schliessen
                 */
                List<Task> tasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(temp.getOrdering(),
                        this.currentStep.getOrdering(), this.currentStep.getProcess().getId());
                for (Task task : tasksInBetween) {
                    task.setProcessingStatusEnum(TaskStatus.DONE);
                    task.setProcessingEnd(now);
                    task.setPriority(0);
                    if (task.getId().intValue() == temp.getId().intValue()) {
                        task.setProcessingStatusEnum(TaskStatus.OPEN);
                        task = serviceManager.getTaskService().setCorrectionStep(task);
                        task.setProcessingEnd(null);
                        task.setProcessingTime(now);
                    }
                    this.serviceManager.getTaskService().save(task);
                }

                Property processProperty = new Property();
                processProperty.setTitle(Helper.getTranslation("Korrektur durchgefuehrt"));
                processProperty.setValue("[" + this.formatter.format(new Date()) + ", "
                        + serviceManager.getUserService().getFullName(ben) + "] "
                        + Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                        + this.solutionMessage);
                processProperty.getProcesses().add(this.currentStep.getProcess());
                processProperty.setType(PropertyType.messageImportant);
                this.currentStep.getProcess().getProperties().add(processProperty);

                String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                        + this.solutionMessage + " (" + serviceManager.getUserService().getFullName(ben) + ")";
                this.currentStep.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(this.currentStep.getProcess(),
                        this.currentStep.getProcess().getWikiField(), "info", message));
                /*
                 * den Prozess aktualisieren, so dass der Sortierungshelper
                 * gespeichert wird
                 */
            }
        } catch (DataException e) {
            logger.error(e);
        }
    }

    public String getProblemMessage() {
        return this.problemMessage;
    }

    public void setProblemMessage(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public String getMyProblemStep() {
        return this.myProblemStep;
    }

    public void setMyProblemStep(String myProblemStep) {
        this.myProblemStep = myProblemStep;
    }

    public String getSolutionMessage() {
        return this.solutionMessage;
    }

    public void setSolutionMessage(String solutionMessage) {
        this.solutionMessage = solutionMessage;
    }

    public String getMySolutionStep() {
        return this.mySolutionStep;
    }

    public void setMySolutionStep(String mySolutionStep) {
        this.mySolutionStep = mySolutionStep;
    }

    /**
     * sets new value for wiki field.
     *
     * @param inString
     *            input String
     */
    public void setWikiField(String inString) {
        this.currentStep.getProcess().setWikiField(inString);
    }

    public String getWikiField() {
        return this.currentStep.getProcess().getWikiField();
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    /**
     * Add to wiki field.
     */
    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            String message = this.addToWikiField + " (" + serviceManager.getUserService().getFullName(user) + ")";
            this.currentStep.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(this.currentStep.getProcess(),
                    this.currentStep.getProcess().getWikiField(), "user", message));
            this.addToWikiField = "";
            try {
                this.serviceManager.getProcessService().save(this.currentStep.getProcess());
            } catch (DataException e) {
                logger.error(e);
            }
        }
    }

    /**
     * Add to wiki field for all.
     */
    public void addToWikiFieldForAll() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            String message = this.addToWikiField + " (" + serviceManager.getUserService().getFullName(user) + ")";
            for (Task s : this.steps) {
                s.getProcess().setWikiField(
                        WikiFieldHelper.getWikiMessage(s.getProcess(), s.getProcess().getWikiField(), "user", message));
                try {
                    this.serviceManager.getProcessService().save(s.getProcess());
                } catch (DataException e) {
                    logger.error(e);
                }
            }
            this.addToWikiField = "";
        }
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Execute script.
     */
    public void executeScript() throws DataException {
        for (Task task : this.steps) {
            if (task.getScriptName().equals(this.script)) {
                String scriptPath = task.getTypeAutomaticScriptPath();
                serviceManager.getTaskService().executeScript(task, scriptPath, false);
            }
        }
    }

    /**
     * Export DMS.
     */
    public void exportDMS() {
        for (Task step : this.steps) {
            ExportDms export = new ExportDms();
            try {
                export.startExport(step.getProcess());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error on export", e.getMessage());
                logger.error(e);
            }
        }
    }

    /**
     * Not sure.
     *
     * @return String
     */
    public String batchDurchBenutzerZurueckgeben() {

        for (Task s : this.steps) {

            this.myDav.uploadFromHome(s.getProcess());
            s.setProcessingStatusEnum(TaskStatus.OPEN);
            if (serviceManager.getTaskService().isCorrectionStep(s)) {
                s.setProcessingBegin(null);
            }
            s.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
            currentStep.setProcessingTime(new Date());
            User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            if (ben != null) {
                currentStep.setProcessingUser(ben);
            }

            try {
                this.serviceManager.getProcessService().save(s.getProcess());
            } catch (DataException e) {
                logger.error(e);
            }
        }
        AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        return asf.filterAll();
    }

    /**
     * Not sure.
     *
     * @return String
     */
    public String batchDurchBenutzerAbschliessen() throws DAOException, DataException {

        // for (ProcessProperty pp : this.processPropertyList) {
        // this.processProperty = pp;
        // saveCurrentPropertyForAll();
        // }
        for (Task s : this.steps) {
            boolean error = false;

            if (s.isTypeImagesWrite()) {
                try {
                    HistoryAnalyserJob.updateHistory(s.getProcess());
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Error while calculation of storage and images", e);
                }
            }

            if (s.isTypeCloseVerify()) {
                if (s.isTypeMetadata() && ConfigCore.getBooleanParameter("useMetadatenvalidierung")) {
                    serviceManager.getMetadataValidationService().setAutoSave(true);
                    if (!serviceManager.getMetadataValidationService().validate(s.getProcess())) {
                        error = true;
                    }
                }
                if (s.isTypeImagesWrite()) {
                    MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                    try {
                        if (!mih.checkIfImagesValid(s.getProcess().getTitle(),
                                serviceManager.getProcessService().getImagesOrigDirectory(false, s.getProcess()))) {
                            error = true;
                        }
                    } catch (Exception e) {
                        Helper.setFehlerMeldung("Error on image validation: ", e);
                    }
                }

                loadProcessProperties(s);

                for (ProcessProperty prop : processPropertyList) {

                    if (prop.getCurrentStepAccessCondition().equals(AccessCondition.WRITEREQUIRED)
                            && (prop.getValue() == null || prop.getValue().equals(""))) {
                        List<String> parameter = new ArrayList<>();
                        parameter.add(prop.getName());
                        parameter.add(s.getProcess().getTitle());
                        Helper.setFehlerMeldung(Helper.getTranslation("BatchPropertyEmpty", parameter));
                        error = true;
                    } else if (!prop.isValid()) {
                        List<String> parameter = new ArrayList<>();
                        parameter.add(prop.getName());
                        parameter.add(s.getProcess().getTitle());
                        Helper.setFehlerMeldung(Helper.getTranslation("BatchPropertyValidation", parameter));
                        error = true;
                    }
                }
            }
            if (!error) {
                this.myDav.uploadFromHome(s.getProcess());
                TaskDAO taskDAO = new TaskDAO();
                Task task = taskDAO.getById(s.getId());
                task.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                serviceManager.getTaskService().close(s, true);
            }
        }
        AktuelleSchritteForm asf = (AktuelleSchritteForm) Helper.getManagedBeanValue("#{AktuelleSchritteForm}");
        return asf.filterAll();
    }

    /**
     * Get script' names.
     *
     * @return list of names
     */
    public String getScriptName() {
        return getCurrentStep().getScriptName();
    }

    public List<Integer> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
    }
}

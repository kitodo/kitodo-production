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

package org.kitodo.services.data;

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ShellScript;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.persistence.apache.FolderInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.hibernate.Session;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class TaskService extends TitleSearchService<Task> {

    private TaskDAO taskDAO = new TaskDAO();
    private TaskType taskType = new TaskType();
    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public TaskService() {
        super(new Searcher(Task.class));
        this.indexer = new Indexer<>(Task.class);
    }

    /**
     * Method saves task object to database.
     *
     * @param task
     *            object
     */
    public void saveToDatabase(Task task) throws DAOException {
        taskDAO.save(task);
    }

    /**
     * Method saves task document to the index of Elastic Search.
     *
     * @param task
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(Task task) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (task != null) {
            indexer.performSingleRequest(task, taskType);
        }
    }

    /**
     * Method saves or removes dependencies with process, users and user's
     * groups related to modified task.
     *
     * @param task
     *            object
     */
    protected void manageDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        manageProcessDependenciesForIndex(task);
        manageProcessingUserDependenciesForIndex(task);
        manageUsersDependenciesForIndex(task);
        manageUserGroupsDependenciesForIndex(task);
    }

    private void manageProcessDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            Process process = task.getProcess();
            if (process != null) {
                process.getTasks().remove(task);
                serviceManager.getProcessService().saveToIndex(process);
            }
        } else {
            Process process = task.getProcess();
            serviceManager.getProcessService().saveToIndex(process);
        }
    }

    private void manageProcessingUserDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            User user = task.getProcessingUser();
            if (user != null) {
                user.getProcessingTasks().remove(task);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            User user = task.getProcessingUser();
            serviceManager.getUserService().saveToIndex(user);
        }
    }

    private void manageUsersDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            for (User user : task.getUsers()) {
                user.getTasks().remove(task);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            for (User user : task.getUsers()) {
                serviceManager.getUserService().saveToIndex(user);
            }
        }
    }

    private void manageUserGroupsDependenciesForIndex(Task task) throws CustomResponseException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            for (UserGroup userGroup : task.getUserGroups()) {
                userGroup.getTasks().remove(task);
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        } else {
            for (UserGroup userGroup : task.getUserGroups()) {
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        }
    }

    public Task find(Integer id) throws DAOException {
        return taskDAO.find(id);
    }

    public List<Task> findAll() {
        return taskDAO.findAll();
    }

    /**
     * Gets the task titles distinct.
     * 
     * @return Al list of titles
     */
    public List<String> getTaskTitlesDistinct() {
        return taskDAO.getTaskTitlesDistict();
    }

    /**
     * Method removes task object from database.
     *
     * @param task
     *            object
     */
    public void removeFromDatabase(Task task) throws DAOException {
        taskDAO.remove(task);
    }

    /**
     * Method removes task object from database.
     *
     * @param id
     *            of task object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        taskDAO.remove(id);
    }

    /**
     * Method removes task object from index of Elastic Search.
     *
     * @param task
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(Task task) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (task != null) {
            indexer.performSingleRequest(task, taskType);
        }
    }

    public List<Task> search(String query) throws DAOException {
        return taskDAO.search(query);
    }

    public Long count(String query) throws DAOException {
        return taskDAO.count(query);
    }

    /**
     * Find tasks by id of process.
     *
     * @param id
     *            of process
     * @return list of search results with tasks for specific process id
     */
    public List<SearchResult> findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("process", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws InterruptedException, IOException, CustomResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), taskType);
    }

    /**
     * Convert date of processing begin to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingBeginAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingBegin());
    }

    /**
     * Convert date of processing end to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingEndAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingEnd());
    }

    /**
     * Convert date of processing day to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingTimeAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingTime());
    }

    // a parameter is given here (even if not used) because jsf expects setter
    // convention
    public void setProcessingTimeNow(Task task) {
        task.setProcessingTime(new Date());
    }

    public int getProcessingTimeNow() {
        return 1;
    }

    /**
     * If you change anything in the logic of priorities make sure that you
     * catch dependencies on this system which are not directly related to
     * priorities. TODO: check it!
     */
    public Boolean isCorrectionStep(Task task) {
        return (task.getPriority() == 10);
    }

    public Task setCorrectionStep(Task task) {
        task.setPriority(10);
        return task;
    }

    public String getLocalizedTitle(Task task) {
        return Helper.getTranslation(task.getTitle());
    }

    public String getNormalizedTitle(Task task) {
        return task.getTitle().replace(" ", "_");
    }

    /**
     * Get users' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUsersSize(Task task) {
        if (task.getUsers() == null) {
            return 0;
        } else {
            return task.getUsers().size();
        }
    }

    /**
     * Get user groups' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUserGroupsSize(Task task) {
        if (task.getUserGroups() == null) {
            return 0;
        } else {
            return task.getUserGroups().size();
        }
    }

    /**
     * Set processing status up.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusUp(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task.setProcessingStatus(task.getProcessingStatus() + 1);
        }
        return task;
    }

    /**
     * Set processing status down.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusDown(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
            task.setProcessingStatus(task.getProcessingStatus() - 1);
        }
        return task;
    }

    /**
     * Get title with user.
     *
     * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit
     *         vollständigem Namen
     */
    public String getTitleWithUserName(Task task) {
        String result = task.getTitle();
        UserService userService = new UserService();
        if (task.getProcessingUser() != null && task.getProcessingUser().getId() != null
                && task.getProcessingUser().getId() != 0) {
            result += " (" + userService.getFullName(task.getProcessingUser()) + ")";
        }
        return result;
    }

    public String getProcessingStatusAsString(Task task) {
        return String.valueOf(task.getProcessingStatus().intValue());
    }

    public Integer setProcessingStatusAsString(String inputProcessingStatus) {
        return Integer.parseInt(inputProcessingStatus);
    }

    /**
     * Get all script paths.
     *
     * @param task
     *            object
     * @return array list
     */
    public ArrayList<String> getAllScriptPaths(Task task) {
        ArrayList<String> answer = new ArrayList<>();
        if (task.getTypeAutomaticScriptPath() != null && !task.getTypeAutomaticScriptPath().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath());
        }
        if (task.getTypeAutomaticScriptPath2() != null && !task.getTypeAutomaticScriptPath2().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath2());
        }
        if (task.getTypeAutomaticScriptPath3() != null && !task.getTypeAutomaticScriptPath3().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath3());
        }
        if (task.getTypeAutomaticScriptPath4() != null && !task.getTypeAutomaticScriptPath4().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath4());
        }
        if (task.getTypeAutomaticScriptPath5() != null && !task.getTypeAutomaticScriptPath5().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath5());
        }
        return answer;
    }

    /**
     * Get all scripts and their paths.
     *
     * @param task
     *            object
     * @return hash map
     */
    public HashMap<String, String> getAllScripts(Task task) {
        HashMap<String, String> answer = new HashMap<>();
        if (task.getTypeAutomaticScriptPath() != null && !task.getTypeAutomaticScriptPath().equals("")) {
            answer.put(task.getScriptName1(), task.getTypeAutomaticScriptPath());
        }
        if (task.getTypeAutomaticScriptPath2() != null && !task.getTypeAutomaticScriptPath2().equals("")) {
            answer.put(task.getScriptName2(), task.getTypeAutomaticScriptPath2());
        }
        if (task.getTypeAutomaticScriptPath3() != null && !task.getTypeAutomaticScriptPath3().equals("")) {
            answer.put(task.getScriptName3(), task.getTypeAutomaticScriptPath3());
        }
        if (task.getTypeAutomaticScriptPath4() != null && !task.getTypeAutomaticScriptPath4().equals("")) {
            answer.put(task.getScriptName4(), task.getTypeAutomaticScriptPath4());
        }
        if (task.getTypeAutomaticScriptPath5() != null && !task.getTypeAutomaticScriptPath5().equals("")) {
            answer.put(task.getScriptName5(), task.getTypeAutomaticScriptPath5());
        }
        return answer;
    }

    /**
     * Execute script for StepObject.
     *
     * @param task
     *            StepObject
     * @param script
     *            String
     * @param automatic
     *            boolean
     * @return int
     */
    public int executeScript(Task task, String script, boolean automatic) throws DataException {
        if (script == null || script.length() == 0) {
            return -1;
        }
        script = script.replace("{", "(").replace("}", ")");
        DigitalDocument dd = null;
        Process po = task.getProcess();

        FolderInformation fi = new FolderInformation(po.getId(), po.getTitle());
        Prefs prefs = serviceManager.getRulesetService().getPreferences(po.getRuleset());

        try {
            dd = serviceManager.getProcessService().readMetadataFile(fi.getMetadataFilePath(), prefs)
                    .getDigitalDocument();
        } catch (PreferencesException | ReadException | IOException e2) {
            logger.error(e2);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, po, task);

        script = replacer.replace(script);
        int rueckgabe = -1;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Calling the shell: " + script);
            }
            rueckgabe = ShellScript.legacyCallShell2(script);
            if (automatic) {
                if (rueckgabe == 0) {
                    task.setEditType(TaskEditType.AUTOMATIC.getValue());
                    task.setProcessingStatus(TaskStatus.DONE.getValue());
                    if (task.getValidationPlugin() != null && task.getValidationPlugin().length() > 0) {
                        IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation,
                                task.getValidationPlugin());
                        ivp.setStep(task);
                        if (!ivp.validate()) {
                            task.setProcessingStatus(TaskStatus.OPEN.getValue());
                            save(task);
                        } else {
                            close(task, false);
                        }
                    } else {
                        close(task, false);
                    }

                } else {
                    task.setEditType(TaskEditType.AUTOMATIC.getValue());
                    task.setProcessingStatus(TaskStatus.OPEN.getValue());
                    save(task);
                }
            }
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException: ", e.getMessage());
        }
        return rueckgabe;
    }

    /**
     * Execute all scripts for step.
     *
     * @param task
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeAllScripts(Task task, boolean automatic) throws DataException {
        List<String> scriptpaths = getAllScriptPaths(task);
        int count = 1;
        int size = scriptpaths.size();
        int returnParameter = 0;
        for (String script : scriptpaths) {
            if (logger.isDebugEnabled()) {
                logger.debug("starting script " + script);
            }
            if (returnParameter != 0) {
                abortTask(task);
                break;
            }
            if (script != null && !script.equals(" ") && script.length() != 0) {
                returnParameter = executeScript(task, script, automatic && (count == size));
            }
            count++;
        }
    }

    private void abortTask(Task task) throws DataException {

        task.setProcessingStatus(TaskStatus.OPEN.getValue());
        task.setEditType(TaskEditType.AUTOMATIC.getValue());

        save(task);
    }

    /**
     * Set all scripts and their paths.
     *
     * @param paths
     *            hash map of strings
     * @param task
     *            object
     * @return task object
     */
    public Task setAllScripts(HashMap<String, String> paths, Task task) {
        Set<String> keys = paths.keySet();
        ArrayList<String> keyList = new ArrayList<>();
        keyList.addAll(keys);
        int size = keyList.size();
        if (size > 0) {
            task.setScriptName1(keyList.get(0));
            task.setTypeAutomaticScriptPath(paths.get(keyList.get(0)));
        }
        if (size > 1) {
            task.setScriptName2(keyList.get(1));
            task.setTypeAutomaticScriptPath2(paths.get(keyList.get(1)));
        }
        if (size > 2) {
            task.setScriptName3(keyList.get(2));
            task.setTypeAutomaticScriptPath3(paths.get(keyList.get(2)));
        }
        if (size > 3) {
            task.setScriptName4(keyList.get(3));
            task.setTypeAutomaticScriptPath4(paths.get(keyList.get(3)));
        }
        if (size > 4) {
            task.setScriptName5(keyList.get(4));
            task.setTypeAutomaticScriptPath5(paths.get(keyList.get(4)));
        }
        return task;
    }

    /**
     * Get list of paths. TODO: inappropriate name of method - change during
     * next phase of refactoring
     * 
     * @param task
     *            object
     * @return string containing paths.
     */
    public String getListOfPaths(Task task) {
        String answer = "";
        if (task.getScriptName1() != null) {
            answer += task.getScriptName1();
        }
        if (task.getScriptName2() != null) {
            answer = answer + "; " + task.getScriptName2();
        }
        if (task.getScriptName3() != null) {
            answer = answer + "; " + task.getScriptName3();
        }
        if (task.getScriptName4() != null) {
            answer = answer + "; " + task.getScriptName4();
        }
        if (task.getScriptName5() != null) {
            answer = answer + "; " + task.getScriptName5();
        }
        return answer;

    }

    /**
     * Get the current object for this row.
     *
     * @return the current object representing a row.
     */
    public Task getCurrent(Task task) {
        boolean hasOpen = HibernateUtilOld.hasOpenSession();
        Session session = Helper.getHibernateSession();

        Task current = (Task) session.get(Task.class, task.getId());
        if (current == null) {
            current = (Task) session.load(Task.class, task.getId());
        }
        if (!hasOpen) {
            session.close();
        }
        return current;
    }

    /**
     * Returns whether this is a step of a process that is part of at least one
     * batch as read-only property "batchSize".
     *
     * @return whether this step’s process is in a batch
     */
    public boolean isBatchSize(Task task) {
        ProcessService processService = new ProcessService();
        return processService.getBatchesInitialized(task.getProcess()).size() > 0;
    }

    public void close(Task task, boolean requestFromGUI) throws DataException {
        Integer processId = task.getProcess().getId();
        if (logger.isDebugEnabled()) {
            logger.debug("closing step with id " + task.getId() + " and process id " + processId);
        }
        task.setProcessingStatus(3);
        Date myDate = new Date();
        logger.debug("set new date for edit time");
        task.setProcessingTime(myDate);
        LoginForm lf = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (lf != null) {
            User ben = lf.getMyBenutzer();
            if (ben != null) {
                logger.debug("set new user");
                task.setProcessingUser(ben);
            }
        }
        logger.debug("set new end date");
        task.setProcessingEnd(myDate);
        logger.debug("saving step");
        serviceManager.getTaskService().save(task);
        List<Task> automatischeSchritte = new ArrayList<>();
        List<Task> stepsToFinish = new ArrayList<>();

        logger.debug("create history events for step");

        History history = new History(myDate, task.getOrdering(), task.getTitle(), HistoryTypeEnum.taskDone,
                task.getProcess());
        serviceManager.getHistoryService().save(history);
        /*
         * prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht
         * abgeschlossen sind
         */

        List<Task> steps = task.getProcess().getTasks();
        List<Task> allehoeherenSchritte = new ArrayList<>();
        int offeneSchritteGleicherReihenfolge = 0;
        for (Task so : steps) {
            if (so.getOrdering().equals(task.getOrdering()) && so.getProcessingStatus() != 3
                    && !so.getId().equals(task.getId())) {
                offeneSchritteGleicherReihenfolge++;
            } else if (so.getOrdering() > task.getOrdering()) {
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
            for (Task myTask : allehoeherenSchritte) {
                if (reihenfolge < myTask.getOrdering() && !matched) {
                    reihenfolge = myTask.getOrdering();
                }

                if (reihenfolge == myTask.getOrdering() && myTask.getProcessingStatus() != 3
                        && myTask.getProcessingStatus() != 2) {
                    /*
                     * den Schritt aktivieren, wenn es kein vollautomatischer
                     * ist
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("open step " + myTask.getTitle());
                    }
                    myTask.setProcessingStatus(1);
                    myTask.setProcessingTime(myDate);
                    myTask.setEditType(4);
                    logger.debug("create history events for next step");
                    History historyOpen = new History(myDate, myTask.getOrdering(), myTask.getTitle(),
                            HistoryTypeEnum.taskOpen, task.getProcess());
                    serviceManager.getHistoryService().save(history);
                    /* wenn es ein automatischer Schritt mit Script ist */
                    if (logger.isDebugEnabled()) {
                        logger.debug("check if step is an automatic task: " + myTask.isTypeAutomatic());
                    }
                    if (myTask.isTypeAutomatic()) {
                        logger.debug("add step to list of automatic tasks");
                        automatischeSchritte.add(myTask);
                    } else if (myTask.isTypeAcceptClose()) {
                        stepsToFinish.add(myTask);
                    }
                    logger.debug("");
                    serviceManager.getTaskService().save(myTask);
                    matched = true;

                } else {
                    if (matched) {
                        break;
                    }
                }
            }
        }
        Process po = task.getProcess();
        FolderInformation fi = new FolderInformation(po.getId(), po.getTitle());
        if (po.getSortHelperImages() != serviceManager.getFileService()
                .getNumberOfFiles(fi.getImagesOrigDirectory(true))) {
            po.setSortHelperImages(serviceManager.getFileService().getNumberOfFiles(fi.getImagesOrigDirectory(true)));
            serviceManager.getProcessService().save(po);
        }
        logger.debug("update process status");
        updateProcessStatus(po);
        if (logger.isDebugEnabled()) {
            logger.debug("start " + automatischeSchritte.size() + " automatic tasks");
        }
        for (Task automaticStep : automatischeSchritte) {
            if (logger.isDebugEnabled()) {
                logger.debug("creating scripts task for step with stepId " + automaticStep.getId() + " and processId "
                        + automaticStep.getId());
            }
            TaskScriptThread myThread = new TaskScriptThread(automaticStep);
            TaskManager.addTask(myThread);
        }
        for (Task finish : stepsToFinish) {
            if (logger.isDebugEnabled()) {
                logger.debug("closing task " + finish.getTitle());
            }
            serviceManager.getTaskService().close(finish, false);
        }
    }

    /**
     * Update process status.
     *
     * @param process
     *            the process
     */
    public void updateProcessStatus(Process process) throws DataException {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        List<Task> stepsForProcess = process.getTasks();
        for (Task task : stepsForProcess) {
            if (task.getProcessingStatus() == 3) {
                abgeschlossen++;
            } else if (task.getProcessingStatus() == 0) {
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

        process.setSortHelperStatus(value);
        serviceManager.getProcessService().save(process);

    }

    /**
     * Execute DMS export.
     *
     * @param step
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeDmsExport(Task step, boolean automatic) throws DataException, ConfigurationException {
        ConfigCore.getBooleanParameter("automaticExportWithImages", true);
        if (!ConfigCore.getBooleanParameter("automaticExportWithOcr", true)) {
        }
        Process po = step.getProcess();
        try {
            boolean validate = serviceManager.getProcessService().startDmsExport(po,
                    ConfigCore.getBooleanParameter("automaticExportWithImages", true), false);
            if (validate) {
                close(step, true);
            } else {
                abortTask(step);
            }
        } catch (PreferencesException | WriteException | IOException e) {
            logger.error(e);
            abortTask(step);
            return;
        }
    }

}

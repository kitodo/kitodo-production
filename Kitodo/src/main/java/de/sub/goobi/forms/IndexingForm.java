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

package de.sub.goobi.forms;

import de.sub.goobi.helper.IndexWorker;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.*;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

@Named
@ApplicationScoped
public class IndexingForm {

    class IndexAllThread extends Thread {

        private int pause = 1000;

        @Override
        public void run() {
            try {
                indexingAll = true;

                startUserIndexing();
                indexerThread.join();
                sleep(pause);

                startUserGroupIndexing();
                indexerThread.join();
                sleep(pause);

                startProjectIndexing();
                indexerThread.join();
                sleep(pause);

                startRulesetIndexing();
                indexerThread.join();
                sleep(pause);

                startDocketIndexing();
                indexerThread.join();
                sleep(pause);

                startTaskIndexing();
                indexerThread.join();
                sleep(pause);

                startTemplateIndexing();
                indexerThread.join();
                sleep(pause);

                startProcessIndexing();
                indexerThread.join();
                sleep(pause);

                startBatchIndexing();
                indexerThread.join();
                sleep(pause);

                startPropertyIndexing();
                indexerThread.join();
                sleep(pause);

                startWorkpieceIndexing();
                indexerThread.join();
                sleep(pause);

                currentIndexState = ObjectTypes.NONE;
                indexingAll = false;

            } catch (InterruptedException e) {
                logger.debug("'Index all' process interrupted: " + e.getMessage());
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(IndexingForm.class);

    private transient ServiceManager serviceManager = new ServiceManager();

    private enum ObjectTypes {
        BATCH, DOCKET, PROCESS, PROJECT, PROPERTY, RULESET, TASK, TEMPLATE, USER, USERGROUP, WORKPIECE, NONE
    }

    private ObjectTypes currentIndexState = ObjectTypes.NONE;

    private boolean indexingAll = false;

    private Map<ObjectTypes, LocalDateTime> lastIndexed = new EnumMap<>(ObjectTypes.class);

    private List<Batch> batches = serviceManager.getBatchService().findAll();
    private List<Docket> dockets = serviceManager.getDocketService().findAll();
    private List<Process> processes = serviceManager.getProcessService().findAll();
    private List<Project> projects = serviceManager.getProjectService().findAll();
    private List<Property> properties = serviceManager.getPropertyService().findAll();
    private List<Ruleset> rulesets = serviceManager.getRulesetService().findAll();
    private List<Task> tasks = serviceManager.getTaskService().findAll();
    private List<Template> templates = serviceManager.getTemplateService().findAll();
    private List<User> users = serviceManager.getUserService().findAll();
    private List<UserGroup> userGroups = serviceManager.getUserGroupService().findAll();
    private List<Workpiece> workpieces = serviceManager.getWorkpieceService().findAll();

    private int indexedBatches = 0;
    private int indexedDockets = 0;
    private int indexedProcesses = 0;
    private int indexedProjects = 0;
    private int indexedProperties = 0;
    private int indexedRulesetes = 0;
    private int indexedTasks = 0;
    private int indexedTemplates = 0;
    private int indexedUsers = 0;
    private int indexedUsergroups = 0;
    private int indexedWorkpieces = 0;

    private IndexWorker batchWorker;
    private IndexWorker docketWorker;
    private IndexWorker processWorker;
    private IndexWorker projectWorker;
    private IndexWorker propertyWorker;
    private IndexWorker rulesetWorker;
    private IndexWorker taskWorker;
    private IndexWorker templateWorker;
    private IndexWorker userWorker;
    private IndexWorker usergroupWorker;
    private IndexWorker workpieceWorker;

    private Thread indexerThread;

    /**
     * Standard constructor.
     */
    public IndexingForm() {
        this.batchWorker = new IndexWorker(serviceManager.getBatchService(), this.batches);
        this.docketWorker = new IndexWorker(serviceManager.getDocketService(), this.dockets);
        this.processWorker = new IndexWorker(serviceManager.getProcessService(), this.processes);
        this.projectWorker = new IndexWorker(serviceManager.getProjectService(), this.projects);
        this.propertyWorker = new IndexWorker(serviceManager.getPropertyService(), this.properties);
        this.rulesetWorker = new IndexWorker(serviceManager.getRulesetService(), this.rulesets);
        this.taskWorker = new IndexWorker(serviceManager.getTaskService(), this.tasks);
        this.templateWorker = new IndexWorker(serviceManager.getTemplateService(), this.templates);
        this.userWorker = new IndexWorker(serviceManager.getUserService(), this.users);
        this.usergroupWorker = new IndexWorker(serviceManager.getUserGroupService(), this.userGroups);
        this.workpieceWorker = new IndexWorker(serviceManager.getWorkpieceService(), this.workpieces);
    }

    /**
     * Return the number of batches.
     *
     * @return int number of batches
     */
    public int getBatchCount() {
        return this.batches.size();
    }

    /**
     * Return the number of dockets.
     *
     * @return int number of dockets
     */
    public int getDocketCount() {
        return this.dockets.size();
    }

    /**
     * Return the number of processes.
     *
     * @return int number of processes
     */
    public int getProcessCount() {
        return this.processes.size();
    }

    /**
     * Return the number of projects.
     *
     * @return int number of projects
     */
    public int getProjectCount() {
        return this.projects.size();
    }

    /**
     * Return the number of properties.
     *
     * @return int number of properties
     */
    public int getPropertyCount() {
        return this.properties.size();
    }

    /**
     * Return the number of rulesets.
     *
     * @return int number of rulesets
     */
    public int getRulesetCount() {
        return this.rulesets.size();
    }

    /**
     * Return the number of tasks.
     *
     * @return int number of tasks
     */
    public int getTaskCount() {
        return this.tasks.size();
    }

    /**
     * Return the number of templates.
     *
     * @return int number of templates
     */
    public int getTemplateCount() {
        return this.templates.size();
    }

    /**
     * Return the number of users.
     *
     * @return int number of users
     */
    public int getUserCount() {
        return this.users.size();
    }

    /**
     * Return the number of user groups.
     *
     * @return int number of user groups
     */
    public int getUserGroupCount() {
        return this.userGroups.size();
    }

    /**
     * Return the number of workpieces.
     *
     * @return int number of workpieces
     */
    public int getWorkpieceCount() {
        return this.workpieces.size();
    }

    /**
     * Return the total number of all objects that can be indexed.
     *
     * @return int number of all items that can be written to the index
     */
    public int getTotalCount() {
        return (this.getBatchCount() + this.getDocketCount() + this.getProcessCount() + this.getProjectCount()
                + this.getPropertyCount() + this.getRulesetCount() + this.getTaskCount() + this.getTemplateCount()
                + this.getUserCount() + this.getUserGroupCount() + this.getWorkpieceCount());
    }

    /**
     * Return the number of currently indexed batches.
     *
     * @return int number of currently indexed batches
     */
    public int getIndexedBatches() {
        if (currentIndexState == ObjectTypes.BATCH) {
            indexedBatches = batchWorker.getIndexedObjects();
        }
        return indexedBatches;
    }

    /**
     * Return the number of currently indexed dockets.
     *
     * @return int number of currently indexed dockets
     */
    public int getIndexedDockets() {
        if (currentIndexState == ObjectTypes.DOCKET) {
            indexedDockets = docketWorker.getIndexedObjects();
        }
        return indexedDockets;
    }

    /**
     * Return the number of currently indexed processes.
     *
     * @return int number of currently indexed processes
     */
    public int getIndexedProcesses() {
        if (currentIndexState == ObjectTypes.PROCESS) {
            indexedProcesses = processWorker.getIndexedObjects();
        }
        return indexedProcesses;
    }

    /**
     * Return the number of currently indexed projects.
     *
     * @return int number of currently indexed projects
     */
    public int getIndexedProjects() {
        if (currentIndexState == ObjectTypes.PROJECT) {
            indexedProjects = projectWorker.getIndexedObjects();
        }
        return indexedProjects;
    }

    /**
     * Return the number of currently indexed properties.
     *
     * @return int number of currently indexed properties
     */
    public int getIndexedProperties() {
        if (currentIndexState == ObjectTypes.PROPERTY) {
            indexedProperties = propertyWorker.getIndexedObjects();
        }
        return indexedProperties;
    }

    /**
     * Return the number of currently indexed rulesets.
     *
     * @return int number of currently indexed rulesets
     */
    public int getIndexedRulesets() {
        if (currentIndexState == ObjectTypes.RULESET) {
            indexedRulesetes = rulesetWorker.getIndexedObjects();
        }
        return indexedRulesetes;
    }

    /**
     * Return the number of currently indexed templates.
     *
     * @return int number of currently indexed templates
     */
    public int getIndexedTemplates() {
        if (currentIndexState == ObjectTypes.TEMPLATE) {
            indexedTemplates = templateWorker.getIndexedObjects();
        }
        return indexedTemplates;
    }

    /**
     * Return the number of currently indexed tasks.
     *
     * @return int number of currently indexed tasks
     */
    public int getIndexedTasks() {
        if (currentIndexState == ObjectTypes.TASK) {
            indexedTasks = taskWorker.getIndexedObjects();
        }
        return indexedTasks;
    }

    /**
     * Return the number of currently indexed users.
     *
     * @return int number of currently indexed users
     */
    public int getIndexedUsers() {
        if (currentIndexState == ObjectTypes.USER) {
            indexedUsers = userWorker.getIndexedObjects();
        }
        return indexedUsers;
    }

    /**
     * Return the number of currently indexed user groups.
     *
     * @return int number of currently indexed user groups
     */
    public int getIndexedUserGroups() {
        if (currentIndexState == ObjectTypes.USERGROUP) {
            indexedUsergroups = usergroupWorker.getIndexedObjects();
        }
        return indexedUsergroups;
    }

    /**
     * Return the number of currently indexed user workpieces.
     *
     * @return int number of currently indexed user workpieces
     */
    public int getIndexedWorkpieces() {
        if (currentIndexState == ObjectTypes.WORKPIECE) {
            indexedWorkpieces = workpieceWorker.getIndexedObjects();
        }
        return indexedWorkpieces;
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return int number of all currently indexed objects
     */
    public int getAllIndexed() {
        return (getIndexedBatches() + getIndexedDockets() + getIndexedProcesses() + getIndexedProjects()
                + getIndexedProperties() + getIndexedRulesets() + getIndexedTasks() + getIndexedTemplates()
                + getIndexedUsers() + getIndexedUserGroups() + getIndexedWorkpieces());
    }

    /**
     * Return the date and time the last batch indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last batch indexing process
     */
    public LocalDateTime getBatchesLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.BATCH);
    }

    /**
     * Return the date and time the last docket indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last docket indexing process
     */
    public LocalDateTime getDocketsLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.DOCKET);
    }

    /**
     * Return the date and time the last process indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last process indexing process
     */
    public LocalDateTime getProcessesLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.PROCESS);
    }

    /**
     * Return the date and time the last project indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last project indexing process
     */
    public LocalDateTime getProjectsLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.PROJECT);
    }

    /**
     * Return the date and time the last properties indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last properties indexing process
     */
    public LocalDateTime getPropertiesLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.PROPERTY);
    }

    /**
     * Return the date and time the last ruleset indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last ruleset indexing process
     */
    public LocalDateTime getRulsetsLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.RULESET);
    }

    /**
     * Return the date and time the last template indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last template indexing process
     */
    public LocalDateTime getTemplatesLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.TEMPLATE);
    }

    /**
     * Return the date and time the last task indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last task indexing process
     */
    public LocalDateTime getTasksLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.TASK);
    }

    /**
     * Return the date and time the last user indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last user indexing process
     */
    public LocalDateTime getUsersLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.USER);
    }

    /**
     * Return the date and time the last user group indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last user group indexing process
     */
    public LocalDateTime getUserGroupsLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.USERGROUP);
    }

    /**
     * Return the date and time the last workpiece indexing process finished.
     *
     * @return LocalDateTime the timestamp of the last workpiece indexing process
     */
    public LocalDateTime getWorkpiecesLastIndexedDate() {
        return lastIndexed.get(ObjectTypes.WORKPIECE);
    }

    /**
     * Returns the progress of the current batch indexing process in percent.
     *
     * @return the batch indexing progress
     */
    public int getBatchIndexingProgress() {
        return getProgress(batches.size(), ObjectTypes.BATCH, getIndexedBatches());
    }

    /**
     * Returns the progress of the current docket indexing process in percent.
     *
     * @return the docket indexing progress
     */
    public int getDocketsIndexingProgress() {
        return getProgress(dockets.size(), ObjectTypes.DOCKET, getIndexedDockets());
    }

    /**
     * Returns the progress of the current process indexing process in percent.
     *
     * @return the process indexing progress
     */
    public int getProcessIndexingProgress() {
        return getProgress(processes.size(), ObjectTypes.PROCESS, getIndexedProcesses());
    }

    /**
     * Returns the progress of the current project indexing process in percent.
     *
     * @return the project indexing progress
     */
    public int getProjectsIndexingProgress() {
        return getProgress(projects.size(), ObjectTypes.PROJECT, getIndexedProjects());
    }

    /**
     * Returns the progress of the current properties indexing process in percent.
     *
     * @return the properties indexing progress
     */
    public int getPropertiesIndexingProgress() {
        return getProgress(properties.size(), ObjectTypes.PROPERTY, getIndexedProperties());
    }

    /**
     * Returns the progress of the current ruleset indexing process in percent.
     *
     * @return the ruleset indexing progress
     */
    public int getRulesetsIndexingProgress() {
        return getProgress(rulesets.size(), ObjectTypes.RULESET, getIndexedRulesets());
    }

    /**
     * Returns the progress of the current template indexing process in percent.
     *
     * @return the template indexing progress
     */
    public int getTemplatesIndexingProgress() {
        return getProgress(templates.size(), ObjectTypes.TEMPLATE, getIndexedTemplates());
    }

    /**
     * Returns the progress of the current task indexing process in percent.
     *
     * @return the task indexing progress
     */
    public int getTasksIndexingProgress() {
        return getProgress(tasks.size(), ObjectTypes.TASK, getIndexedTasks());
    }

    /**
     * Returns the progress of the current user indexing process in percent.
     *
     * @return the user indexing progress
     */
    public int getUserIndexingProgress() {
        return getProgress(users.size(), ObjectTypes.USER, getIndexedUsers());
    }

    /**
     * Returns the progress of the current usergroup indexing process in percent.
     *
     * @return the usergroup indexing progress
     */
    public int getUserGroupIndexingProgress() {
        return getProgress(userGroups.size(), ObjectTypes.USERGROUP, getIndexedUserGroups());
    }

    /**
     * Returns the progress of the current workpiece indexing process in percent.
     *
     * @return the workpiece indexing progress
     */
    public int getWorkpieceIndexingProgress() {
        return getProgress(workpieces.size(), ObjectTypes.WORKPIECE, getIndexedWorkpieces());
    }

    /**
     * Starts the process of indexing batches to the ElasticSearch index.
     */
    public void startBatchIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.BATCH;
            indexerThread = new Thread((this.batchWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Batch' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing dockets to the ElasticSearch index.
     */
    public void startDocketIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.DOCKET;
            indexerThread = new Thread((this.docketWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Docket' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing processes to the ElasticSearch index.
     */
    public void startProcessIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.PROCESS;
            indexerThread = new Thread((this.processWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Process' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing projects to the ElasticSearch index.
     */
    public void startProjectIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.PROJECT;
            indexerThread = new Thread((this.projectWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Project' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing properties to the ElasticSearch index.
     */
    public void startPropertyIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.PROPERTY;
            indexerThread = new Thread((this.propertyWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug("Cannot start 'Property' indexing, different indexing process running: '" + currentIndexState
                    + "'");
        }
    }

    /**
     * Starts the process of indexing rulesets to the ElasticSearch index.
     */
    public void startRulesetIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.RULESET;
            indexerThread = new Thread((this.rulesetWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Ruleset' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing tasks to the ElasticSearch index.
     */
    public void startTaskIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.TASK;
            indexerThread = new Thread((this.taskWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'Task' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing templates to the ElasticSearch index.
     */
    public void startTemplateIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.TEMPLATE;
            indexerThread = new Thread((this.templateWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug("Cannot start 'Template' indexing, different indexing process running: '" + currentIndexState
                    + "'");
        }
    }

    /**
     * Starts the process of indexing users to the ElasticSearch index.
     */
    public void startUserIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.USER;
            indexerThread = new Thread((this.userWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug(
                    "Cannot start 'User' indexing, different indexing process running: '" + currentIndexState + "'");
        }
    }

    /**
     * Starts the process of indexing user groups to the ElasticSearch index.
     */
    public void startUserGroupIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.USERGROUP;
            indexerThread = new Thread((this.usergroupWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug("Cannot start 'Usergroup' indexing, different indexing process running: '" + currentIndexState
                    + "'");
        }
    }

    /**
     * Starts the process of indexing workpieces to the ElasticSearch index.
     */
    public void startWorkpieceIndexing() {
        if (Objects.equals(currentIndexState, ObjectTypes.NONE)) {
            currentIndexState = ObjectTypes.WORKPIECE;
            indexerThread = new Thread((this.workpieceWorker));
            indexerThread.setDaemon(true);
            indexerThread.start();
        } else {
            logger.debug("Cannot start 'Workpiece' indexing, different indexing process running: '" + currentIndexState
                    + "'");
        }
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        IndexAllThread indexAllThread = new IndexAllThread();
        indexAllThread.start();
    }

    /**
     * Return the overall progress in percent of the currently running indexing
     * process, incorporating the total number of indexed and all objects.
     *
     * @return the overall progress of the indexing process
     */
    public int getAllIndexingProgress() {
        return (int) ((getAllIndexed() / (float) getTotalCount()) * 100);
    }

    /**
     * Return whether any indexing process is currently in progress or not.
     *
     * @return boolean Value indicating whether any indexing process is currently in
     *         progress or not
     */
    public boolean indexingInProgress() {
        return (!Objects.equals(this.currentIndexState, ObjectTypes.NONE) || indexingAll);
    }

    /**
     * Return server information provided by the searchService and gathered by the
     * rest client.
     *
     * @return String information about the server
     */
    public String getServerInformation() {
        return this.serviceManager.getBatchService().getServerInformation();
    }

    /**
     * Return the progress in percent of the currently running indexing process. If
     * the list of entries to be indexed is empty, this will return "0".
     *
     * @param numberOfObjects
     *            the number of existing objects of the given ObjectType
     * @param currentType
     *            the ObjectType for which the progress will be determined
     * @param nrOfindexedObjects
     *            the number of objects of the given ObjectType that have already
     *            been indexed
     *
     * @return the progress of the current indexing process in percent
     */
    private int getProgress(int numberOfObjects, ObjectTypes currentType, int nrOfindexedObjects) {
        int progress = numberOfObjects > 0 ? (int) ((nrOfindexedObjects / (float) numberOfObjects) * 100) : 0;
        if (Objects.equals(currentIndexState, currentType)) {
            if (numberOfObjects == 0 || progress == 100) {
                lastIndexed.put(currentIndexState, LocalDateTime.now());
                currentIndexState = ObjectTypes.NONE;
                indexerThread.interrupt();
            }
        }
        return progress;
    }
}

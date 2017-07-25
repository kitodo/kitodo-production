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

import de.sub.goobi.helper.IndexerThread;
import org.kitodo.services.ServiceManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.LocalDateTime;

@Named
@ApplicationScoped
public class IndexingForm {

    private transient ServiceManager serviceManager = new ServiceManager();

    private LocalDateTime batchLastIndexedDate = null;
    private LocalDateTime docketLastIndexedDate = null;
    private LocalDateTime historyLastIndexedDate = null;
    private LocalDateTime processesLastIndexedDate = null;
    private LocalDateTime projectsLastIndexedDate = null;
    private LocalDateTime propertiesLastIndexedDate = null;
    private LocalDateTime rulsetsLastIndexedDate = null;
    private LocalDateTime tasksLastIndexedDate = null;
    private LocalDateTime templatesLastIndexedDate = null;
    private LocalDateTime usersLastIndexedDate = null;
    private LocalDateTime userGroupsLastIndexedDate = null;
    private LocalDateTime workpiecesLastIndexedDate = null;

    private IndexerThread batchThread;
    private IndexerThread docketThread;
    private IndexerThread historyThread;
    private IndexerThread processThread;
    private IndexerThread projectThread;
    private IndexerThread propertyThread;
    private IndexerThread rulesetThread;
    private IndexerThread taskThread;
    private IndexerThread templateThread;
    private IndexerThread userThread;
    private IndexerThread usergroupThread;
    private IndexerThread workpieceThread;

    /**
     * Standard constructor.
     */
    public IndexingForm() {
        this.batchThread = new IndexerThread(serviceManager.getBatchService(), serviceManager.getBatchService().findAll());
        this.docketThread = new IndexerThread(serviceManager.getDocketService(), serviceManager.getDocketService().findAll());
        this.historyThread = new IndexerThread(serviceManager.getHistoryService(), serviceManager.getHistoryService().findAll());
        this.processThread = new IndexerThread(serviceManager.getProcessService(), serviceManager.getProcessService().findAll());
        this.projectThread = new IndexerThread(serviceManager.getProjectService(), serviceManager.getProjectService().findAll());
        this.propertyThread = new IndexerThread(serviceManager.getPropertyService(), serviceManager.getPropertyService().findAll());
        this.rulesetThread = new IndexerThread(serviceManager.getRulesetService(), serviceManager.getRulesetService().findAll());
        this.taskThread = new IndexerThread(serviceManager.getTaskService(), serviceManager.getTaskService().findAll());
        this.templateThread = new IndexerThread(serviceManager.getTemplateService(), serviceManager.getTemplateService().findAll());
        this.userThread = new IndexerThread(serviceManager.getUserService(), serviceManager.getUserService().findAll());
        this.usergroupThread = new IndexerThread(serviceManager.getUserGroupService(), serviceManager.getUserGroupService().findAll());
        this.workpieceThread = new IndexerThread(serviceManager.getWorkpieceService(), serviceManager.getWorkpieceService().findAll());
    }

    public int getBatchCount(){
        return this.batchThread.getObjectCount();
    }
    public int getIndexedBatches(){
        return this.batchThread.getIndexedObjects();
    }
    public int getBatchIndexingProgress() {
        return this.batchThread.getIndexingProgress();
    }
    public LocalDateTime getBatchesLastIndexedDate() {
        return batchLastIndexedDate;
    }

    public int getHistoryCount(){
        return this.historyThread.getObjectCount();
    }
    public int getIndexedHistory(){
        return this.historyThread.getIndexedObjects();
    }
    public int getHistoryIndexingProgress() {
        return this.historyThread.getIndexingProgress();
    }
    public LocalDateTime getHistoryLastIndexedDate() {
        return historyLastIndexedDate;
    }

    public int getDocketCount(){
        return this.docketThread.getObjectCount();
    }
    public int getIndexedDockets(){
        return this.docketThread.getIndexedObjects();
    }
    public int getDocketsIndexingProgress() {
        return this.docketThread.getIndexingProgress();
    }
    public LocalDateTime getDocketsLastIndexedDate() {
        return docketLastIndexedDate;
    }

    public int getProcessCount(){
        return this.processThread.getObjectCount();
    }
    public int getIndexedProcesses(){
        return this.processThread.getIndexedObjects();
    }
    public int getProcessIndexingProgress() {
        return this.processThread.getIndexingProgress();
    }
    public LocalDateTime getProcessesLastIndexedDate() {
        return processesLastIndexedDate;
    }

    public int getProjectCount(){
        return this.projectThread.getObjectCount();
    }
    public int getIndexedProjects(){
        return this.projectThread.getIndexedObjects();
    }
    public int getProjectsIndexingProgress() { return this.projectThread.getIndexingProgress();
    }
    public LocalDateTime getProjectsLastIndexedDate() {
        return projectsLastIndexedDate;
    }

    public int getPropertyCount(){
        return this.propertyThread.getObjectCount();
    }
    public int getIndexedProperties(){
        return this.propertyThread.getIndexedObjects();
    }
    public int getPropertiesIndexingProgress() {
        return this.propertyThread.getIndexingProgress();
    }
    public LocalDateTime getPropertiesLastIndexedDate() {
        return propertiesLastIndexedDate;
    }

    public int getRulesetCount(){
        return this.rulesetThread.getObjectCount();
    }
    public int getIndexedRulesets(){
        return this.rulesetThread.getIndexedObjects();
    }
    public int getRulesetsIndexingProgress() {
        return this.rulesetThread.getIndexingProgress();
    }
    public LocalDateTime getRulsetsLastIndexedDate() {
        return rulsetsLastIndexedDate;
    }

    public int getTemplateCount(){
        return this.templateThread.getObjectCount();
    }
    public int getIndexedTemplates(){
        return this.templateThread.getIndexedObjects();
    }
    public int getTemplatesIndexingProgress() {
        return this.templateThread.getIndexingProgress();
    }
    public LocalDateTime getTemplatesLastIndexedDate() {
        return templatesLastIndexedDate;
    }

    public int getTaskCount(){
        return this.taskThread.getObjectCount();
    }
    public int getIndexedTasks(){
        return this.taskThread.getIndexedObjects();
    }
    public int getTasksIndexingProgress() {
        return this.taskThread.getIndexingProgress();
    }
    public LocalDateTime getTasksLastIndexedDate() {
        return tasksLastIndexedDate;
    }

    public int getUserCount(){
        return this.userThread.getObjectCount();
    }
    public int getIndexedUsers(){
        return this.userThread.getIndexedObjects();
    }
    public int getUserIndexingProgress() { return this.userThread.getIndexingProgress(); }
    public LocalDateTime getUsersLastIndexedDate() {
        return usersLastIndexedDate;
    }

    public int getUserGroupCount(){
        return this.usergroupThread.getObjectCount();
    }
    public int getIndexedUserGroups(){
        return this.usergroupThread.getIndexedObjects();
    }
    public int getUserGroupIndexingProgress() {
        return this.usergroupThread.getIndexingProgress();
    }
    public LocalDateTime getUserGroupsLastIndexedDate() {
        return userGroupsLastIndexedDate;
    }

    public int getWorkpieceCount(){
        return this.workpieceThread.getObjectCount();
    }
    public int getIndexedWorkpieces(){
        return this.workpieceThread.getIndexedObjects();
    }
    public int getWorkpieceIndexingProgress() {
        return this.workpieceThread.getIndexingProgress();
    }
    public LocalDateTime getWorkpiecesLastIndexedDate() {
        return workpiecesLastIndexedDate;
    }

    /**
     * Starts the process of indexing batches to the ElasticSearch index.
     */
    public void startBatchIndexing() {
        Thread batchIndexingThread = new Thread((this.batchThread));
        batchIndexingThread.setDaemon(true);
        batchIndexingThread.start();
        batchLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing dockets to the ElasticSearch index.
     */
    public void startDocketIndexing() {
        Thread docketIndexingThread = new Thread((this.docketThread));
        docketIndexingThread.setDaemon(true);
        docketIndexingThread.start();
        docketLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing the history to the ElasticSearch index.
     */
    public void startHistoryIndexing() {
        Thread historyIndexingThread = new Thread((this.historyThread));
        historyIndexingThread.setDaemon(true);
        historyIndexingThread.start();
        historyLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing processes to the ElasticSearch index.
     */
    public void startProcessIndexing() {
        Thread processIndexingThread = new Thread((this.processThread));
        processIndexingThread.setDaemon(true);
        processIndexingThread.start();
        processesLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing projects to the ElasticSearch index.
     */
    public void startProjectIndexing() {
        Thread projetIndexingThread = new Thread((this.projectThread));
        projetIndexingThread.setDaemon(true);
        projetIndexingThread.start();
        projectsLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing properties to the ElasticSearch index.
     */
    public void startPropertyIndexing() {
        Thread propertyIndexingThread = new Thread((this.propertyThread));
        propertyIndexingThread.setDaemon(true);
        propertyIndexingThread.start();
        propertiesLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing rulesets to the ElasticSearch index.
     */
    public void startRulesetIndexing() {
        Thread rulesetIndexingThread = new Thread((this.rulesetThread));
        rulesetIndexingThread.setDaemon(true);
        rulesetIndexingThread.start();
        rulsetsLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing tasks to the ElasticSearch index.
     */
    public void startTaskIndexing() {
        Thread taskIndexingThread = new Thread((this.taskThread));
        taskIndexingThread.setDaemon(true);
        taskIndexingThread.start();
        tasksLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing templates to the ElasticSearch index.
     */
    public void startTemplateIndexing() {
        Thread templateIndexingThread = new Thread((this.templateThread));
        templateIndexingThread.setDaemon(true);
        templateIndexingThread.start();
        templatesLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing user groups to the ElasticSearch index.
     */
    public void startUserGroupIndexing() {
        Thread usergroupIndexingThread = new Thread((this.usergroupThread));
        usergroupIndexingThread.setDaemon(true);
        usergroupIndexingThread.start();
        userGroupsLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing users to the ElasticSearch index.
     */
    public void startUserIndexing() {
        Thread userIndexingThread = new Thread((this.userThread));
        userIndexingThread.setDaemon(true);
        userIndexingThread.start();
        usersLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing workpieces to the ElasticSearch index.
     */
    public void startWorkpieceIndexing() {
        Thread userIndexingThread = new Thread((this.userThread));
        userIndexingThread.setDaemon(true);
        userIndexingThread.start();
        workpiecesLastIndexedDate = LocalDateTime.now();
    }

    /**
     * Starts the process of indexing all objects to the ElasticSearch index.
     */
    public void startAllIndexing() {
        this.startBatchIndexing();
        this.startDocketIndexing();
        this.startHistoryIndexing();
        this.startProcessIndexing();
        this.startProcessIndexing();
        this.startProjectIndexing();
        this.startPropertyIndexing();
        this.startRulesetIndexing();
        this.startTaskIndexing();
        this.startTemplateIndexing();
        this.startUserGroupIndexing();
        this.startUserIndexing();
        this.startWorkpieceIndexing();
    }

    /**
     * Return whether any indexing process is currently in progress or not.
     *
     * @return boolean
     *      Value indicating whether any indexing process is currently in progress or not
     */
    public boolean indexingInProgress() {
        return (
            (getBatchIndexingProgress() > 0 && getBatchIndexingProgress() < 100) ||
            (getHistoryIndexingProgress() > 0 && getHistoryIndexingProgress() < 100) ||
            (getDocketsIndexingProgress() > 0 && getDocketsIndexingProgress() < 100) ||
            (getProcessIndexingProgress() > 0 && getProcessIndexingProgress() < 100) ||
            (getProjectsIndexingProgress() > 0 && getProjectsIndexingProgress() < 100) ||
            (getPropertiesIndexingProgress() > 0 && getPropertiesIndexingProgress() < 100) ||
            (getRulesetsIndexingProgress() > 0 && getRulesetsIndexingProgress() < 100) ||
            (getTemplatesIndexingProgress() > 0 && getTemplatesIndexingProgress() < 100) ||
            (getTasksIndexingProgress() > 0 && getTasksIndexingProgress() < 100) ||
            (getUserIndexingProgress() > 0 && getUserIndexingProgress() < 100) ||
            (getUserGroupIndexingProgress() > 0 && getUserGroupIndexingProgress() < 100) ||
            (getWorkpieceIndexingProgress() > 0 && getWorkpieceIndexingProgress() < 100)
        );
    }

    /**
     * Return server information provided by the searchService and gathered by the rest client.
     *
     * @return String
     *             information about the server
     */
    public String getServerInformation() {
        return this.serviceManager.getBatchService().getServerInformation();
    }
}

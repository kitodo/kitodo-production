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

package org.kitodo.workflow.model;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.beans.Diagram;
import org.kitodo.workflow.model.beans.KitodoScriptTask;
import org.kitodo.workflow.model.beans.KitodoTask;
import org.kitodo.workflow.model.beans.TaskInfo;

public class Reader {

    private String diagramName;
    private BpmnModelInstance modelInstance;
    private ServiceManager serviceManager = new ServiceManager();
    private FileService fileService = serviceManager.getFileService();
    private Map<Task, TaskInfo> tasks;
    private Diagram workflow;

    /**
     * Constructor with diagram name as parameter. It loads modelInstance from file
     * for given name.
     *
     * @param diagramName
     *            as String
     * @throws IOException
     *             in case if file for given name doesn't exist
     */
    public Reader(String diagramName) throws IOException {
        this.diagramName = diagramName;
        loadProcess();
    }

    /**
     * Convert BPMN process (workflow) to template stored in database.
     *
     * @return Template bean
     */
    public Template convertWorkflowToTemplate(Template template) throws DAOException, WorkflowException {
        this.tasks = new HashMap<>();

        getWorkflowTasks();

        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            org.kitodo.data.database.beans.Task task = getTask(entry.getKey(), entry.getValue());
            task.setTemplate(template);
            template.getTasks().add(task);
        }

        return template;
    }

    /**
     * Read the workflow from diagram.
     */
    private void loadProcess() throws IOException {
        String diagramPath = ConfigCore.getKitodoDiagramDirectory() + this.diagramName + ".bpmn20.xml";
        modelInstance = Bpmn.readModelFromStream(fileService.read(Paths.get(diagramPath).toUri()));
        getWorkflowFromProcess();
    }

    /**
     * Get workflow from process inside the given file.
     */
    private void getWorkflowFromProcess() throws IOException {
        Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();

        if (Objects.isNull(process)) {
            throw new IOException("It looks that given file contains invalid BPMN diagram!");
        }
        this.workflow = new Diagram(process);
    }

    private org.kitodo.data.database.beans.Task getTask(Task workflowTask, TaskInfo taskInfo) throws DAOException {
        org.kitodo.data.database.beans.Task task = new org.kitodo.data.database.beans.Task();
        KitodoTask kitodoTask = new KitodoTask(workflowTask);
        task.setWorkflowId(kitodoTask.getWorkflowId());
        task.setTitle(kitodoTask.getTitle());
        task.setPriority(kitodoTask.getPriority());
        task.setEditType(kitodoTask.getEditType());
        task.setProcessingStatus(kitodoTask.getProcessingStatus());
        task.setBatchStep(kitodoTask.getBatchStep());
        task.setTypeAutomatic(kitodoTask.getTypeAutomatic());
        task.setTypeImagesRead(kitodoTask.getTypeImagesRead());
        task.setTypeImagesWrite(kitodoTask.getTypeImagesWrite());
        task.setTypeExportDMS(kitodoTask.getTypeExportDms());
        task.setTypeAcceptClose(kitodoTask.getTypeAcceptClose());
        task.setTypeCloseVerify(kitodoTask.getTypeCloseVerify());
        task.setWorkflowCondition(taskInfo.getCondition());
        task.setPreviousTasks(taskInfo.getPreviousTasks());
        task.setConcurrentTasks(taskInfo.getConcurrentTasks());
        task.setNextTasks(taskInfo.getNextTasks());
        Integer userRoleId = kitodoTask.getUserRole();
        if (userRoleId > 0) {
            task.getRoles().add(serviceManager.getRoleService().getById(userRoleId));
        }

        if (workflowTask instanceof ScriptTask) {
            KitodoScriptTask kitodoScriptTask = new KitodoScriptTask((ScriptTask) workflowTask);
            task.setScriptName(kitodoScriptTask.getScriptName());
            task.setScriptPath(kitodoScriptTask.getScriptPath());
        }

        return task;
    }

    private void getWorkflowTasks() throws WorkflowException {
        StartEvent startEvent = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        iterateOverNodes(startEvent.getOutgoing().iterator().next().getTarget());
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     */
    private void iterateOverNodes(FlowNode node) throws WorkflowException {
        iterateOverNodes(node, "", "", "");
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     * @param workflowCondition
     *            given from exclusive gateway
     */
    private void iterateOverNodes(FlowNode node, String workflowCondition, String previousTasks, String concurrentTasks)
            throws WorkflowException {
        if (node instanceof Task) {
            addTask(node, new TaskInfo(workflowCondition, previousTasks), concurrentTasks);

            Query<FlowNode> nextNode = node.getSucceedingNodes();
            if (nextNode.count() == 1) {
                iterateOverNodes(nextNode.singleResult(), workflowCondition, node.getId(), concurrentTasks);
            } else {
                throw new WorkflowException("Task has more than one following tasks without any gateway in between!");
            }
        } else if (node instanceof ExclusiveGateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            previousTasks = getTaskSequence(node.getPreviousNodes().list());
            if (nextNodes.count() == 1) {
                iterateOverNodes(nextNodes.singleResult(), "", previousTasks, "");
            } else if (nextNodes.count() > 1) {
                addConditionalTasksBranch(nextNodes.list(), previousTasks);
            } else {
                throw new WorkflowException("Exclusive gateway is not followed by any tasks!");
            }
        } else if (node instanceof ParallelGateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            previousTasks = getTaskSequence(node.getPreviousNodes().list());
            if (nextNodes.count() == 1) {
                iterateOverNodes(nextNodes.singleResult(), workflowCondition, previousTasks, "");
            } else if (nextNodes.count() > 1) {
                addParallelTasksBranch(nextNodes.list(), workflowCondition, previousTasks);
            } else {
                throw new WorkflowException("Parallel gateway is not followed by any tasks!");
            }
        }
    }

    /**
     * Add all tasks in exact branch - following given exclusive gateway.
     *
     * @param nextNodes
     *            nodes of exclusive gateway
     */
    private void addConditionalTasksBranch(List<FlowNode> nextNodes, String previousTasks) throws WorkflowException {
        for (FlowNode node : nextNodes) {
            ConditionExpression conditionExpression = node.getIncoming().iterator().next().getConditionExpression();
            String workflowCondition = "default";
            if (Objects.nonNull(conditionExpression)) {
                workflowCondition = conditionExpression.getTextContent();
            }

            iterateOverNodes(node, workflowCondition, previousTasks, "");
        }
    }

    /**
     * Add all tasks for parallel execution - following given parallel gateway until
     * ending parallel gateway.
     *
     * @param nextNodes
     *            nodes of parallel gateway
     * @param workflowCondition
     *            workflow condition is carried over from previous states
     */
    private void addParallelTasksBranch(List<FlowNode> nextNodes, String workflowCondition, String previousTasks)
            throws WorkflowException {
        for (FlowNode node : nextNodes) {
            List<FlowNode> concurrentNodes = new ArrayList<>(nextNodes);
            concurrentNodes.remove(node);
            iterateOverNodes(node, workflowCondition, previousTasks, getTaskSequence(concurrentNodes));
        }
    }

    /**
     * Add task to tasks list.
     * 
     * @param node
     *            for task
     * @param taskInfo
     *            which contains workflow condition and previous tasks
     * @param concurrentTasks
     *            workflow id of concurrent task(s)
     */
    private void addTask(FlowNode node, TaskInfo taskInfo, String concurrentTasks) {
        taskInfo.setConcurrentTasks(concurrentTasks);

        String nextTasks = "";
        List<FlowNode> nextNodes = node.getSucceedingNodes().list();

        for (FlowNode nextNode : nextNodes) {
            if (nextNode instanceof EndEvent) {
                break;
            } else if (nextNode instanceof Gateway) {
                List<FlowNode> nodesAfterGateway = nextNode.getSucceedingNodes().list();
                nextTasks = getTaskSequence(nodesAfterGateway);
            } else {
                nextTasks += nextNode.getId();
            }
        }

        taskInfo.setNextTasks(nextTasks);

        tasks.put((Task) node, taskInfo);
    }

    private String getTaskSequence(List<FlowNode> nodes) {
        StringBuilder taskSequence = new StringBuilder();
        for (FlowNode node : nodes) {
            taskSequence.append(node.getId());
            taskSequence.append(",");
        }
        return taskSequence.substring(0, taskSequence.length() - 1);
    }

    /**
     * Get modelInstance.
     *
     * @return value of modelInstance
     */
    BpmnModelInstance getModelInstance() {
        return modelInstance;
    }

    /**
     * Get tasks.
     *
     * @return value of tasks
     */
    public Map<Task, TaskInfo> getTasks() {
        return tasks;
    }

    /**
     * Set tasks.
     *
     * @param tasks
     *            as list of BPMN Task objects
     */
    public void setTasks(Map<Task, TaskInfo> tasks) {
        this.tasks = tasks;
    }

    /**
     * Get workflow.
     *
     * @return value of workflow
     */
    public Diagram getWorkflow() {
        return workflow;
    }
}

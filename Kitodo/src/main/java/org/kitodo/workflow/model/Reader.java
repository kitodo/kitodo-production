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
import java.io.InputStream;
import java.nio.file.Paths;
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
import org.kitodo.workflow.model.beans.Diagram;
import org.kitodo.workflow.model.beans.KitodoScriptTask;
import org.kitodo.workflow.model.beans.KitodoTask;
import org.kitodo.workflow.model.beans.TaskInfo;

public class Reader {

    private BpmnModelInstance modelInstance;
    private ServiceManager serviceManager = new ServiceManager();
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
        String diagramPath = ConfigCore.getKitodoDiagramDirectory() + diagramName + ".bpmn20.xml";
        loadProcess(serviceManager.getFileService().read(Paths.get(diagramPath).toUri()));
    }

    /**
     * Constructor with diagram name as parameter. It loads modelInstance from file
     * for given name.
     *
     * @param diagramXmlContent
     *            as String
     * @throws IOException
     *             in case if file for given name doesn't exist
     */
    public Reader(InputStream diagramXmlContent) throws IOException {
        loadProcess(diagramXmlContent);
    }

    /**
     * Convert BPMN process (workflow) to template stored in database.
     *
     * @return Template bean
     */
    public Template convertWorkflowToTemplate(Template template) throws DAOException, WorkflowException {
        this.tasks = new HashMap<>();

        readWorkflowTasks();

        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            org.kitodo.data.database.beans.Task task = getTask(entry.getKey(), entry.getValue());
            task.setTemplate(template);
            template.getTasks().add(task);
        }

        return template;
    }

    public void validateWorkflowTasks() throws WorkflowException {
        readWorkflowTasks();
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

    /**
     * Read the workflow from diagram.
     * 
     * @param diagramXmlContent
     *            as InputStream
     */
    private void loadProcess(InputStream diagramXmlContent) throws IOException {
        modelInstance = Bpmn.readModelFromStream(diagramXmlContent);
        getWorkflowFromProcess();
    }

    /**
     * Get workflow from process inside the given file.
     */
    private void getWorkflowFromProcess() throws IOException {
        Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();

        if (Objects.isNull(process)) {
            throw new IOException("It looks that given file or input stream contains invalid BPMN diagram!");
        }
        this.workflow = new Diagram(process);
    }

    private org.kitodo.data.database.beans.Task getTask(Task workflowTask, TaskInfo taskInfo) throws DAOException {
        org.kitodo.data.database.beans.Task task = new org.kitodo.data.database.beans.Task();
        KitodoTask kitodoTask = new KitodoTask(workflowTask);
        task.setWorkflowId(kitodoTask.getWorkflowId());
        task.setTitle(kitodoTask.getTitle());
        task.setOrdering(taskInfo.getOrdering());
        task.setPriority(kitodoTask.getPriority());
        task.setEditType(kitodoTask.getEditType());
        task.setProcessingStatus(kitodoTask.getProcessingStatus());
        task.setConcurrent(kitodoTask.isConcurrent());
        task.setLast(taskInfo.isLast());
        task.setBatchStep(kitodoTask.isBatchStep());
        task.setTypeAutomatic(kitodoTask.isTypeAutomatic());
        task.setTypeImagesRead(kitodoTask.isTypeImagesRead());
        task.setTypeImagesWrite(kitodoTask.isTypeImagesWrite());
        task.setTypeExportDMS(kitodoTask.isTypeExportDms());
        task.setTypeAcceptClose(kitodoTask.isTypeAcceptClose());
        task.setTypeCloseVerify(kitodoTask.isTypeCloseVerify());
        task.setWorkflowCondition(taskInfo.getCondition());
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

    private void readWorkflowTasks() throws WorkflowException {
        StartEvent startEvent = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        iterateOverNodes(startEvent.getOutgoing().iterator().next().getTarget(), 1);
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     */
    private void iterateOverNodes(FlowNode node, int ordering) throws WorkflowException {
        iterateOverNodes(node, ordering, "");
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     * @param workflowCondition
     *            given from exclusive gateway
     */
    private void iterateOverNodes(FlowNode node, int ordering, String workflowCondition) throws WorkflowException {
        if (node instanceof Task) {
            addTask(node, ordering, workflowCondition);
        } else if (node instanceof ExclusiveGateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            if (nextNodes.count() == 1) {
                iterateOverNodes(nextNodes.singleResult(), ordering);
            } else if (nextNodes.count() > 1) {
                addConditionalTasksBranch(nextNodes.list(), ordering);
            } else {
                throw new WorkflowException("Exclusive gateway is not followed by any tasks!");
            }
        } else if (node instanceof ParallelGateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            if (nextNodes.count() == 1) {
                iterateOverNodes(nextNodes.singleResult(), ordering, workflowCondition);
            } else if (nextNodes.count() > 1) {
                addParallelTasksBranch(nextNodes.list(), ordering, workflowCondition);
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
    private void addConditionalTasksBranch(List<FlowNode> nextNodes, int ordering) throws WorkflowException {
        for (FlowNode node : nextNodes) {
            if (isBranchInvalid(node)) {
                throw new WorkflowException(
                        "Task in conditional branch can not have second task. Please remove task after task with name '"
                                + node.getName() + "'.");
            }

            ConditionExpression conditionExpression = node.getIncoming().iterator().next().getConditionExpression();
            String workflowCondition = "default";
            if (Objects.nonNull(conditionExpression)) {
                workflowCondition = conditionExpression.getTextContent();
            }

            iterateOverNodes(node, ordering, workflowCondition);
        }
    }

    /**
     * Add all tasks for parallel execution - following given parallel gateway until
     * ending parallel gateway.
     *
     * @param nodes
     *            nodes of parallel gateway
     * @param workflowCondition
     *            workflow condition is carried over from previous states
     */
    private void addParallelTasksBranch(List<FlowNode> nodes, int ordering, String workflowCondition)
            throws WorkflowException {
        for (FlowNode node : nodes) {
            if (isBranchInvalid(node)) {
                throw new WorkflowException(
                        "Task in parallel branch can not have second task. Please remove task after task with name '"
                                + node.getName() + "'.");
            }

            iterateOverNodes(node, ordering, workflowCondition);
        }
    }

    private boolean isBranchInvalid(FlowNode node) {
        List<FlowNode> nextNodes = node.getSucceedingNodes().list();
        for (FlowNode nextNode : nextNodes) {
            if (nextNode instanceof Task) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add task to tasks list.
     *
     * @param node
     *            for task
     * @param ordering
     *            for task
     * @param workflowCondition
     *            for task
     */
    private void addTask(FlowNode node, int ordering, String workflowCondition) throws WorkflowException {
        Query<FlowNode> nextNodes = node.getSucceedingNodes();

        if (nextNodes.count() == 1) {
            FlowNode nextNode = nextNodes.singleResult();

            if (nextNode instanceof EndEvent) {
                tasks.put((Task) node, new TaskInfo(ordering, true, workflowCondition));
            } else {
                tasks.put((Task) node, new TaskInfo(ordering, false, workflowCondition));
                ordering++;
                iterateOverNodes(nextNode, ordering, workflowCondition);
            }
        } else {
            if (nextNodes.count() == 0) {
                // TODO: implement here case for tasks with end event
                // selenium test has problem as it doesn't add end event
                // TODO: find solution for selenium test
            } else {
                throw new WorkflowException("Task with title '" + node.getName()
                        + "' has more than one following tasks without any gateway in between!");
            }
        }
    }

    /**
     * Get modelInstance.
     *
     * @return value of modelInstance
     */
    BpmnModelInstance getModelInstance() {
        return modelInstance;
    }
}

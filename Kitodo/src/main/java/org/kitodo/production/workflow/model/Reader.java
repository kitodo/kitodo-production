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

package org.kitodo.production.workflow.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.kitodo.config.ConfigCore;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.beans.TaskInfo;

public class Reader {

    private BpmnModelInstance modelInstance;
    private Map<Task, TaskInfo> tasks;

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
        loadProcess(ServiceManager.getFileService().read(Paths.get(diagramPath).toUri()));
    }

    /**
     * Constructor with diagram name as parameter. It loads modelInstance for xml
     * with given content.
     *
     * @param diagramXmlContent
     *            as InputStream
     * @throws IOException
     *             in case if input stream contains incorrect data
     */
    public Reader(InputStream diagramXmlContent) throws IOException {
        loadProcess(diagramXmlContent);
    }

    /**
     * Method reads workflow tasks, in case reading fails it throws
     * WorkflowException.
     *
     * @throws WorkflowException
     *             is thrown when reading of the tasks fail, exception message
     *             explains what caused problem
     */
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
     * Set tasks as map of BPMN Task objects. Key is BPMN Task and value is
     * TaskInfo.
     *
     * @param tasks
     *            as map of BPMN Task objects
     */
    public void setTasks(Map<Task, TaskInfo> tasks) {
        this.tasks = tasks;
    }

    void readWorkflowTasks() throws WorkflowException {
        tasks = new LinkedHashMap<>();

        StartEvent startEvent = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        if (startEvent.getOutgoing().iterator().hasNext()) {
            iterateOverNodes(startEvent.getOutgoing().iterator().next().getTarget());
        }
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
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     */
    private void iterateOverNodes(FlowNode node) throws WorkflowException {
        iterateOverNodes(node, 1);
    }

    /**
     * Iterate over diagram nodes.
     *
     * @param node
     *            for current iteration call
     * @param ordering
     *            of task
     */
    private void iterateOverNodes(FlowNode node, int ordering) throws WorkflowException {
        if (node instanceof Task) {
            addTask((Task) node, ordering);
        } else if (node instanceof Gateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            if (nextNodes.count() == 1) {
                if (node.getPreviousNodes().count() > 1) {
                    iterateOverNodes(nextNodes.singleResult(), ordering);
                } else {
                    throw new WorkflowException(Helper.getTranslation("workflowExceptionParallelGatewayOneTask"));
                }
            } else if (nextNodes.count() > 1) {
                addParallelTasksBranch(nextNodes.list(), ordering);
            } else {
                throw new WorkflowException(Helper.getTranslation("workflowExceptionParallelGatewayNoTask"));
            }
        }
    }

    /**
     * Add all tasks for parallel execution - following given parallel gateway until
     * ending parallel gateway.
     *
     * @param nodes
     *            nodes of parallel gateway
     * @param ordering
     *            of task
     */
    private void addParallelTasksBranch(List<FlowNode> nodes, int ordering) throws WorkflowException {
        for (FlowNode node : nodes) {
            if (isBranchInvalid(node)) {
                throw new WorkflowException(Helper.getTranslation("workflowExceptionParallelBranch",
                    node.getName()));
            }

            iterateOverNodes(node, ordering);
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
     * Add task to tasks list. If task has 0 following nodes, it makes assumption
     * that it is the last task in the workflow.
     *
     * @param task
     *            for task
     * @param ordering
     *            for task
     */
    private void addTask(Task task, int ordering) throws WorkflowException {
        Query<FlowNode> nextNodes = task.getSucceedingNodes();
        int nextNodesSize = nextNodes.count();

        if (nextNodesSize == 1) {
            FlowNode nextNode = nextNodes.singleResult();

            if (nextNode instanceof EndEvent) {
                addTaskIfThereIsNoLoop(task, new TaskInfo(ordering, true));
            } else {
                addTaskIfThereIsNoLoop(task, new TaskInfo(ordering, false));
                ordering++;
                iterateOverNodes(nextNode, ordering);
            }
        } else if (nextNodesSize == 0) {
            addTaskIfThereIsNoLoop(task, new TaskInfo(ordering, true));
        } else {
            throw new WorkflowException(Helper.getTranslation("workflowExceptionMissingGateway",
                task.getName()));
        }
    }

    /**
     * If there are more than one incoming node - it can mean that loop has appeared
     * (more incoming nodes than outgoing).
     *
     * @param task
     *            for verification if there is no loop
     * @param taskInfo
     *            additional information needed for add task
     */
    private void addTaskIfThereIsNoLoop(Task task, TaskInfo taskInfo) throws WorkflowException {
        Query<FlowNode> previousNodes = task.getPreviousNodes();
        if (previousNodes.count() == 1) {
            tasks.put(task, taskInfo);
        } else {
            throw new WorkflowException(
                    Helper.getTranslation("workflowExceptionLoop", task.getName()));
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

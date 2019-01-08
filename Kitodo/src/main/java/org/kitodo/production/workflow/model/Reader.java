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
import java.util.Collections;
import java.util.LinkedHashMap;
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
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.kitodo.config.ConfigCore;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.beans.Diagram;
import org.kitodo.production.workflow.model.beans.TaskInfo;

public class Reader {

    private BpmnModelInstance modelInstance;
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

    /**
     * Get workflow.
     *
     * @return value of workflow
     */
    public Diagram getWorkflow() {
        return workflow;
    }

    void readWorkflowTasks() throws WorkflowException {
        tasks = new LinkedHashMap<>();

        StartEvent startEvent = modelInstance.getModelElementsByType(StartEvent.class).iterator().next();
        if (startEvent.getOutgoing().iterator().hasNext()) {
            iterateOverNodes(startEvent.getOutgoing().iterator().next().getTarget(), 1);
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
        this.workflow = new Diagram(process);
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
                throw new WorkflowException(Helper.getTranslation("workflowExceptionExclusiveGateway"));
            }
        } else if (node instanceof ParallelGateway) {
            Query<FlowNode> nextNodes = node.getSucceedingNodes();
            if (nextNodes.count() == 1) {
                iterateOverNodes(nextNodes.singleResult(), ordering, workflowCondition);
            } else if (nextNodes.count() > 1) {
                addParallelTasksBranch(nextNodes.list(), ordering, workflowCondition);
            } else {
                throw new WorkflowException(Helper.getTranslation("workflowExceptionParallelGateway"));
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
                throw new WorkflowException(Helper.getTranslation("workflowExceptionConditionalBranch",
                    Collections.singletonList(node.getName())));
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
                throw new WorkflowException(Helper.getTranslation("workflowExceptionParallelBranch",
                    Collections.singletonList(node.getName())));
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
     * Add task to tasks list. If node has 0 following nodes, it makes assumption
     * that it is the last task in the workflow.
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
        int nextNodesSize = nextNodes.count();

        if (nextNodesSize == 1) {
            FlowNode nextNode = nextNodes.singleResult();

            if (nextNode instanceof EndEvent) {
                tasks.put((Task) node, new TaskInfo(ordering, true, workflowCondition));
            } else {
                tasks.put((Task) node, new TaskInfo(ordering, false, workflowCondition));
                ordering++;
                iterateOverNodes(nextNode, ordering, workflowCondition);
            }
        } else if (nextNodesSize == 0) {
            tasks.put((Task) node, new TaskInfo(ordering, true, workflowCondition));
        } else {
            throw new WorkflowException(Helper.getTranslation("workflowExceptionMissingGateway",
                Collections.singletonList(node.getName())));
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

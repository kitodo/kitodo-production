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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.goobi.production.flow.statistics.StepInformation;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class ProjectHelper {

    /**
     * static to reduce load
     *
     * @param project
     *            object
     * @return a GoobiCollection of the following structure: GoobiCollection 1-n
     *         representing the steps each step has the following properties @
     *         stepTitle, stepOrder,
     *         stepCount,stepImageCount,totalProcessCount,totalImageCount which can
     *         get extracted by the IGoobiCollection Interface using the
     *         getItem(&lt;name&gt;) method standard workflow of the project
     *         according to the definition that only steps shared by all processes
     *         are returned. The workflow order is returned according to the average
     *         order return by a grouping by step title consider workflow structure
     *         to be a prototype, it would probably make things easier, to either
     *         assemble the underlying construction in separate classes or to create
     *         a new class with these properties
     */
    public static synchronized List<StepInformation> getProjectWorkFlowOverview(Project project) throws DataException {
        ServiceManager serviceManager = new ServiceManager();

        Long totalNumberOfProcesses = serviceManager.getProcessService()
                .findNumberOfNotTemplateProcessesForProjectId(project.getId());
        Double totalNumberOfImages = serviceManager.getProcessService()
                .findAmountOfImagesForNotTemplatesAndProjectId(project.getId());

        List<Task> tasks = serviceManager.getTaskService()
                .getTasksForProjectHelper(project.getId());
        List<Long> tasksSize = serviceManager.getTaskService()
                .getSizeOfTasksForProjectHelper(project.getId());
        List<Double> averageTaskOrders = serviceManager.getTaskService()
                .getAverageOrderingOfTasksForProjectHelper(project.getId());

        // now we have to discriminate the hits where the max number of hits
        // doesn't reach numberOfProcesses and extract a workflow, which is
        // the workflow common for all processes according to its title
        // the position will be calculated by the average of 'ordering' of
        // tasks
        String title;
        Double averageTaskOrder;
        Long numberOfTasks;
        Long numberOfImages;

        List<StepInformation> workFlow = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            title = tasks.get(i).getTitle();
            numberOfTasks = tasksSize.get(i);
            averageTaskOrder = averageTaskOrders.get(i);

            // in this step we only take the steps which are present in each of
            // the workflows
            if (numberOfTasks.equals(totalNumberOfProcesses)) {
                StepInformation newStep = new StepInformation(title, averageTaskOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProcesses.intValue());
                workFlow.add(newStep);
            }
        }

        List<Task> doneTasks = serviceManager.getTaskService()
                .getTasksWithProcessingStatusForProjectHelper(
                        TaskStatus.DONE.getValue(), project.getId());
        List<Long> doneTasksSize = serviceManager.getTaskService()
                .getSizeOfTasksWithProcessingStatusForProjectHelper(
                        TaskStatus.DONE.getValue(), project.getId());
        List<Long> amountOfImages = serviceManager.getTaskService()
                .getAmountOfImagesForTasksWithProcessingStatusForProjectHelper(
                        TaskStatus.DONE.getValue(), project.getId());

        for (int i = 0; i < doneTasks.size(); i++) {
            title = doneTasks.get(i).getTitle();
            numberOfTasks = doneTasksSize.get(i);
            numberOfImages = amountOfImages.get(i);

            // getting from the workflow collection the collection which
            // represents task <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(title)) {
                    currentStep.setNumberOfStepsDone(numberOfTasks.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }

        Comparator<StepInformation> comp = new CompareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }

    // TODO: move this class to StepInformation
    private static class CompareWorkflowSteps implements Comparator<StepInformation>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * uses the field "stepOrder".
         */
        @Override
        public int compare(StepInformation firstStepInformation, StepInformation secondStepInformation) {
            Double firstAverageStepOrder = firstStepInformation.getAverageStepOrder();
            Double secondAverageStepOrder = secondStepInformation.getAverageStepOrder();
            return firstAverageStepOrder.compareTo(secondAverageStepOrder);
        }
    }
}

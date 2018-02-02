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

package org.goobi.production.chart;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.StepInformation;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * This implementation get the workflow from the project.
 *
 * @author Wulf Riebensahm
 *
 */
public class WorkflowProjectTaskList implements IProvideProjectTaskList {
    private static final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(WorkflowProjectTaskList.class);

    @Override
    public List<IProjectTask> calculateProjectTasks(Project inProject, Boolean countImages, Integer inMax) {
        List<IProjectTask> myTaskList = new ArrayList<>();
        try {
            calculate(inProject, myTaskList, countImages, inMax);
        } catch (DataException e) {
            logger.error(e);
        }
        return myTaskList;
    }

    private static synchronized void calculate(Project inProject, List<IProjectTask> taskList, Boolean countImages,
            Integer inMax) throws DataException {
        List<StepInformation> workFlow = serviceManager.getProjectService().getWorkFlow(inProject);

        for (StepInformation step : workFlow) {
            Integer usedMax;
            ProjectTask projectTask;

            // get workflow contains steps with the following structure
            // stepTitle,stepOrder,stepCount,stepImageCount,totalProcessCount,totalImageCount
            String title = step.getTitle();
            if (title.length() > 40) {
                title = title.substring(0, 40) + "...";
            }

            String stepsCompleted = String.valueOf(step.getNumberOfStepsDone());
            String imagesCompleted = String.valueOf(step.getNumberOfImagesDone());

            if (countImages) {
                usedMax = step.getNumberOfTotalImages();
                if (usedMax > inMax) {
                    // TODO notify calling object, that the inMax is not set
                    // right
                } else {
                    usedMax = inMax;
                }

                projectTask = new ProjectTask(title, Integer.parseInt(imagesCompleted), usedMax);
            } else {
                usedMax = step.getNumberOfTotalSteps();
                if (usedMax > inMax) {
                    // TODO notify calling object, that the inMax is not set
                    // right
                } else {
                    usedMax = inMax;
                }

                projectTask = new ProjectTask(title, Integer.parseInt(stepsCompleted), usedMax);
            }
            taskList.add(projectTask);
        }
    }

}

package org.goobi.production.chart;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *   - http://gdz.sub.uni-goettingen.de
 *   - http://www.intranda.com
 *
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.statistics.StepInformation;

import de.sub.goobi.beans.Projekt;




/**
 * This implementation get the workflow from the project.
 * 
 * @author Wulf Riebensahm
 *
 */

public class WorkflowProjectTaskList implements IProvideProjectTaskList {

	public List<IProjectTask> calculateProjectTasks(Projekt inProject, Boolean countImages, Integer inMax) {
		List<IProjectTask> myTaskList = new ArrayList<IProjectTask>();
		calculate(inProject, myTaskList, countImages, inMax);
		return myTaskList;
	}

	private static synchronized void calculate(Projekt inProject, List<IProjectTask> myTaskList, Boolean countImages, Integer inMax) {

		List<StepInformation> workFlow = inProject.getWorkFlow();
		Integer usedMax = 0;

		for (StepInformation step : workFlow) {
			ProjectTask pt = null;

			// get workflow contains steps with the following structure
			// stepTitle,stepOrder,stepCount,stepImageCount,totalProcessCount,totalImageCount
			String title = step.getTitle();
			if (title.length() > 40) {
				title = title.substring(0, 40) + "...";
			}

			String stepsCompleted = String.valueOf(step.getNumberOfStepsDone());
			String imagesCompleted = String.valueOf(step.getNumberOfImagesDone());

			if (stepsCompleted == null) {
				stepsCompleted = "0";
			}

			if (imagesCompleted == null) {
				imagesCompleted = "0";
			}

			if (countImages) {
				usedMax = step.getNumberOfTotalImages();
				if (usedMax > inMax) {
					//TODO notify calling object, that the inMax is not set right
				} else {
					usedMax = inMax;
				}

				pt = new ProjectTask(title, Integer.parseInt(imagesCompleted), usedMax);
			} else {
				usedMax = step.getNumberOfTotalSteps();
				if (usedMax > inMax) {
					//TODO notify calling object, that the inMax is not set right
				} else {
					usedMax = inMax;
				}

				pt = new ProjectTask(title, Integer.parseInt(stepsCompleted), usedMax);
			}
			myTaskList.add(pt);

		}
	}

}

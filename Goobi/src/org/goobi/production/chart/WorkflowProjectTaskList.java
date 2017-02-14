package org.goobi.production.chart;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
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

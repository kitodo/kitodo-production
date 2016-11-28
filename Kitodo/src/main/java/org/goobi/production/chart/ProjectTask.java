package org.goobi.production.chart;

import java.io.Serializable;

//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
//CHECKSTYLE:ON

public class ProjectTask implements IProjectTask, Serializable {
	private static final long serialVersionUID = 1L;
	private String taskTitle;
	private Integer taskStepsCompleted;
	private Integer taskStepsMax;

	/**
	 * @param title add description
	 * @param stepsCompleted add description
	 * @param stepsMax add description
	 */
	public ProjectTask(String title, Integer stepsCompleted, Integer stepsMax) {
		taskTitle = title;
		taskStepsCompleted = stepsCompleted;
		taskStepsMax = stepsMax;
		checkSizes();
	}

	@Override
	public String getTitle() {
		return taskTitle;
	}

	@Override
	public Integer getStepsCompleted() {
		return taskStepsCompleted;
	}

	@Override
	public Integer getStepsMax() {
		return taskStepsMax;
	}

	@Override
	public void setStepsCompleted(Integer stepsCompleted) {
		taskStepsCompleted = stepsCompleted;
	}

	@Override
	public void setStepsMax(Integer stepsMax) {
		taskStepsMax = stepsMax;
	}

	private void checkSizes() {
		if (taskStepsCompleted > taskStepsMax) {
			taskStepsMax = taskStepsCompleted;
		}
	}
}

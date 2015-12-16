package org.goobi.production.chart;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*************************************************************************************
 * A ProjectStatusDataTable object holds all the information needed
 * for rendering a project status chart 
 * 
 * A ProjectStatusDataTable consists of
 * - a name
 * - dates of project begin and project end
 * - a list of project tasks
 * 
 * @author Karsten Köhler
 * @author Hendrik Söhnholz
 * @author Steffen Hankiewicz
 * @version 30.10.2009
 * 
 * @see ProjectTask
 *************************************************************************************/
public class ProjectStatusDataTable implements Serializable {
	private static final long serialVersionUID = -6649337945039135394L;
	private String name;

	private Date projectBegin;
	private Date projectEnd;
	
	private List<ProjectTask> projectTasks;
	private List<String> taskTitles;

	/************************************************************************************
	 * public constructor, the name is set here
	 * 
	 * @param title
	 *            the title to set
	 ************************************************************************************/
	public ProjectStatusDataTable(String inName, Date begin, Date end) {
		super();
		this.name = inName;
		projectBegin = begin;
		projectEnd = end;
		projectTasks = new ArrayList<ProjectTask>();
		taskTitles = new ArrayList<String>();
	}

	/** 
	 * Remove a task from the list. 
	 * 
	 * @param title
	 */
	public void removeTask(String title) {
		if (taskTitles.contains(title)) {
			int taskIndex = taskTitles.indexOf(title);
			projectTasks.remove(taskIndex);
			taskTitles.remove(taskIndex);

		}
	}

	/************************************************************************************
	 * Add a task to the list
	 * 
	 * @param title
	 * 		The title of the task to add
	 * @param stepsCompleted
	 * 		Number of steps completed
	 * @param
	 * 		Total number of steps
	 *            
	 ************************************************************************************/
	public void addTask(IProjectTask inTask) {
		if (!(taskTitles.contains(inTask.getTitle()))) {
			projectTasks.add(new ProjectTask(inTask.getTitle(), inTask.getStepsCompleted(), inTask.getStepsMax()));
			taskTitles.add(inTask.getTitle());

		} else {
			// There can be only one task with this title.
			int taskIndex = taskTitles.indexOf(inTask.getTitle());
			ProjectTask task = projectTasks.get(taskIndex);
			task.setStepsCompleted(inTask.getStepsCompleted());
			task.setStepsMax(inTask.getStepsMax());
		}

	}

	/************************************************************************************
	 * getter for name
	 * 
	 * @return name as string
	 ************************************************************************************/
	public String getName() {
		return name;
	}

	/************************************************************************************
	 * setter for name
	 * 
	 * @param name
	 *            as string
	 ************************************************************************************/
	public void setName(String name) {
		this.name = name;
	}

	/************************************************************************************
	 * getter for all tasks
	 * 
	 * @return list of {@link ProjectTask}
	 ************************************************************************************/
	public List<ProjectTask> getTasks() {
		return projectTasks;
	}

	/************************************************************************************
	 * getter for size of task list
	 * 
	 * @return number of tasks
	 ************************************************************************************/
	public int getNumberOfTasks() {
		if (projectTasks == null) {
			return 0;
		} else {
			return projectTasks.size();
		}
	}

	/************************************************************************************
	 * getter for index of task in list
	 * 
	 * @return index of task
	 ************************************************************************************/
	public int getTaskIndex(String title) {
		if (taskTitles.contains(title)) {
			return taskTitles.indexOf(title);
		} else {
			return -1;
		}
	}

	/************************************************************************************
	 * getter for projectBegin
	 * 
	 * @return projectBegin as Date
	 ************************************************************************************/
	public Date getProjectBegin() {
		return projectBegin;
	}

	/************************************************************************************
	 * getter for projectEnd
	 * 
	 * @return projectEnd as Date
	 ************************************************************************************/
	public Date getProjectEnd() {
		return projectEnd;
	}

}

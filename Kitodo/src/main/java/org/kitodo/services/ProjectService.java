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

package org.kitodo.services;

import de.sub.goobi.helper.ProjectHelper;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.webapi.beans.Field;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectDAO;

public class ProjectService {

	private List<StepInformation> commonWorkFlow = null;

	private ProjectDAO projectDao = new ProjectDAO();

	public void save(Project project) throws DAOException {
		projectDao.save(project);
	}

	public Project find(Integer id) throws DAOException {
		return projectDao.find(id);
	}

	public void remove(Project project) throws DAOException {
		projectDao.remove(project);
	}

	public void remove(Integer id) throws DAOException {
		projectDao.remove(id);
	}

	public List<Project> search(String query) throws DAOException {
		return projectDao.search(query);
	}

	/**
	 * Get workflow.
	 *
	 * @return a list with information for each step on workflow
	 */

	public List<StepInformation> getWorkFlow(Project project) {
		if (this.commonWorkFlow == null) {
			if (project.getId() != null) {
				this.commonWorkFlow = ProjectHelper.getProjectWorkFlowOverview(project);
			} else {
				this.commonWorkFlow = new ArrayList<>();
			}
		}
		return this.commonWorkFlow;
	}

	@XmlElement(name = "field")
	public List<Field> getFieldConfig(Project project) throws IOException {
		return Field.getFieldConfigForProject(project);
	}
}

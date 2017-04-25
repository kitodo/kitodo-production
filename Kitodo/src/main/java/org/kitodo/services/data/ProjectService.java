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

package org.kitodo.services.data;

import com.sun.research.ws.wadl.HTTPMethods;

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
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.SearchService;

public class ProjectService extends SearchService {

    private List<StepInformation> commonWorkFlow = null;

    private ProjectDAO projectDao = new ProjectDAO();
    private ProjectType projectType = new ProjectType();
    private Indexer<Project, ProjectType> indexer = new Indexer<>(Project.class);

    /**
     * Constructor with searcher's assigning.
     */
    public ProjectService() {
        super(new Searcher(Project.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param project
     *            object
     */
    public void save(Project project) throws CustomResponseException, DAOException, IOException {
        projectDao.save(project);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(project, projectType);
    }

    public Project find(Integer id) throws DAOException {
        return projectDao.find(id);
    }

    public List<Project> findAll() throws DAOException {
        return projectDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param project
     *            object
     */
    public void remove(Project project) throws CustomResponseException, DAOException, IOException {
        projectDao.remove(project);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(project, projectType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        projectDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    public List<Project> search(String query) throws DAOException {
        return projectDao.search(query);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), projectType);
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

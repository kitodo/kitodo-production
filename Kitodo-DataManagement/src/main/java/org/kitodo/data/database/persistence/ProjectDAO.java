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

package org.kitodo.data.database.persistence;

import java.util.List;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;

public class ProjectDAO extends BaseDAO<Project> {
    private static final long serialVersionUID = -9050627256118458325L;

    public Project save(Project project) throws DAOException {
        storeObject(project);
        return retrieveObject(Project.class, project.getId());
    }

    /**
     * Find project object.
     *
     * @param id
     *            of search object
     * @return project object
     * @throws DAOException
     *             hibernate
     */
    public Project find(Integer id) throws DAOException {
        Project result = retrieveObject(Project.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all projects from the database.
     *
     * @return all persisted projects
     */
    public List<Project> findAll() {
        return retrieveAllObjects(Project.class);
    }

    /**
     * Retrieves all projects in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<Project> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM Project ORDER BY id ASC", first, max);
    }

    /**
     * Remove project object.
     *
     * @param project
     *            object
     * @throws DAOException
     *             hibernate
     */
    public void remove(Project project) throws DAOException {
        if (project.getId() != null) {
            removeObject(project);
        }
    }

    /**
     * Remove project object vy id.
     *
     * @param id
     *            of project object
     * @throws DAOException
     *             hibernate
     */
    public void remove(Integer id) throws DAOException {
        if (id != null) {
            removeObject(Project.class, id);
        }
    }

    public List<Project> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }

    /**
     * Get all projects sorted by title.
     * 
     * @return all projects sorted by title as Project objects
     */
    public List<Project> getAllProjectsSortedByTitle() {
        return search("FROM Project ORDER BY title ASC");
    }

    /**
     * Get all not archived projects sorted by title.
     * 
     * @return all not archived projects sorted by title as Project objects
     */
    public List<Project> getAllNotArchivedProjectsSortedByTitle() {
        return search("FROM Project WHERE projectIsArchived = 0 ORDER BY title ASC");
    }
}

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

public class ProjectDAO extends BaseDAO {
    private static final long serialVersionUID = -9050627256118458325L;

    public Project save(Project project) throws DAOException {
        storeObject(project);
        return (Project) retrieveObject(Project.class, project.getId());
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
        Project result = (Project) retrieveObject(Project.class, id);
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
    @SuppressWarnings("unchecked")
    public List<Project> findAll() throws DAOException {
        return retrieveAllObjects(Project.class);
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

    @SuppressWarnings("unchecked")
    public List<Project> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;

public class ProjectDAO extends BaseDAO<Project> {
    private static final long serialVersionUID = -9050627256118458325L;

    @Override
    public Project save(Project project) throws DAOException {
        storeObject(project);
        return retrieveObject(Project.class, project.getId());
    }

    @Override
    public Project getById(Integer id) throws DAOException {
        Project result = retrieveObject(Project.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Project> getAll() throws DAOException {
        return retrieveAllObjects(Project.class);
    }

    @Override
    public List<Project> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Project ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Project> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC",
            offset, size);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        if (id != null) {
            removeObject(Project.class, id);
        }
    }

    /**
     * Get all projects sorted by title.
     *
     * @return all projects sorted by title as Project objects
     */
    public List<Project> getAllProjectsSortedByTitle() {
        return getByQuery("FROM Project ORDER BY title ASC");
    }

    /**
     * Get all active projects sorted by title.
     *
     * @return all active projects sorted by title as Project objects
     */
    public List<Project> getAllActiveProjectsSortedByTitle() {
        return getByQuery("FROM Project WHERE active = 1 ORDER BY title ASC");
    }

    /**
     * Gets projects from given list of ids.
     *
     * @param projectIds
     *            list of project ids
     * @return list of projects
     */
    public List<Project> getByIds(List<Integer> projectIds) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectIds", projectIds);
        return getByQuery("FROM Project WHERE id IN :projectIds", parameters);
    }
}

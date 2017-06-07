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

import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;

public class ProjectFileGroupDAO extends BaseDAO {
    private static final long serialVersionUID = -5506252462891480484L;

    /**
     * Find project file group object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public ProjectFileGroup find(Integer id) throws DAOException {
        ProjectFileGroup result = (ProjectFileGroup) retrieveObject(ProjectFileGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all projects' file groups from the
     * database.
     *
     * @return all persisted projects
     */
    @SuppressWarnings("unchecked")
    public List<ProjectFileGroup> findAll() {
        return retrieveAllObjects(ProjectFileGroup.class);
    }

    public ProjectFileGroup save(ProjectFileGroup projectFileGroup) throws DAOException {
        storeObject(projectFileGroup);
        return (ProjectFileGroup) retrieveObject(ProjectFileGroup.class, projectFileGroup.getId());
    }

    /**
     * The function remove() removes a project file group from database.
     *
     * @param projectFileGroup
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(ProjectFileGroup projectFileGroup) throws DAOException {
        if (projectFileGroup.getId() != null) {
            removeObject(projectFileGroup);
        }
    }

    /**
     * The function remove() removes a project file group from database.
     *
     * @param id
     *            of the task to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        @SuppressWarnings("unused")
        ProjectFileGroup workpiece = (ProjectFileGroup) retrieveObject(ProjectFileGroup.class, id);
        removeObject(ProjectFileGroup.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<ProjectFileGroup> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Integer count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}

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

public class ProjectFileGroupDAO extends BaseDAO<ProjectFileGroup> {
    private static final long serialVersionUID = -5506252462891480484L;

    @Override
    public ProjectFileGroup getById(Integer id) throws DAOException {
        ProjectFileGroup result = retrieveObject(ProjectFileGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<ProjectFileGroup> getAll() throws DAOException {
        return retrieveAllObjects(ProjectFileGroup.class);
    }

    @Override
    public List<ProjectFileGroup> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM ProjectFileGroup ORDER BY id ASC", offset, size);
    }

    @Override
    public ProjectFileGroup save(ProjectFileGroup projectFileGroup) throws DAOException {
        storeObject(projectFileGroup);
        return retrieveObject(ProjectFileGroup.class, projectFileGroup.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(ProjectFileGroup.class, id);
    }
}

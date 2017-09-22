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

import java.util.List;

import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectFileGroupDAO;
import org.kitodo.services.data.base.SearchDatabaseService;

public class ProjectFileGroupService extends SearchDatabaseService<ProjectFileGroup, ProjectFileGroupDAO> {

    public ProjectFileGroupService() {
        super(new ProjectFileGroupDAO());
    }

    public void save(ProjectFileGroup projectFileGroup) throws DAOException {
        dao.save(projectFileGroup);
    }

    public ProjectFileGroup find(Integer id) throws DAOException {
        return dao.getById(id);
    }

    public List<ProjectFileGroup> findAll() {
        return dao.getAll();
    }

    public void remove(ProjectFileGroup projectFileGroup) throws DAOException {
        dao.remove(projectFileGroup);
    }

    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Process");
    }
}

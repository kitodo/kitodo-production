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

import java.util.List;

import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectFileGroupDAO;

public class ProjectFileGroupService {

    private ProjectFileGroupDAO projectFileGroupDao = new ProjectFileGroupDAO();

    public void save(ProjectFileGroup projectFileGroup) throws DAOException {
        projectFileGroupDao.save(projectFileGroup);
    }

    public ProjectFileGroup find(Integer id) throws DAOException {
        return projectFileGroupDao.find(id);
    }

    public List<ProjectFileGroup> findAll() throws DAOException {
        return projectFileGroupDao.findAll();
    }

    public void remove(ProjectFileGroup projectFileGroup) throws DAOException {
        projectFileGroupDao.remove(projectFileGroup);
    }

    public void remove(Integer id) throws DAOException {
        projectFileGroupDao.remove(id);
    }
}

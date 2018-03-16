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

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;

public class ProcessDAO extends BaseDAO<Process> {

    private static final long serialVersionUID = 3538712266212954394L;

    @Override
    public Process getById(Integer id) throws DAOException {
        Process result = retrieveObject(Process.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Process> getAll() {
        return retrieveAllObjects(Process.class);
    }

    @Override
    public List<Process> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Process ORDER BY id ASC", offset, size);
    }

    @Override
    public Process save(Process process) throws DAOException {
        storeObject(process);
        return retrieveObject(Process.class, process.getId());
    }

    /**
     * @param process
     *            object
     * @param progress
     *            service
     * @return process object
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure failure.
     */
    public Process save(Process process, String progress) throws DAOException {
        process.setSortHelperStatus(progress);
        return save(process);
    }

    /**
     * @param list
     *            of processes
     * @throws DAOException
     *             an exception that can be thrown from the underlying saveList()
     *             procedure failure.
     */
    public void saveList(List<Process> list) throws DAOException {
        storeList(list);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Process.class, id);
    }

    /**
     * Refresh process object after some changes.
     *
     * @param process
     *            object
     */
    public void refresh(Process process) {
        refreshObject(process);
    }

    /**
     * Update process object after some changes.
     *
     * @param process
     *            object
     */
    public void update(Process process) {
        updateObject(process);
    }

    /**
     * Get all active processes.
     *
     * @return list of all active processes as Process objects
     */
    public List<Process> getActiveProcesses() {
        return getByQuery("SELECT process FROM Process AS process INNER JOIN process.project AS project WHERE project.active = 1");
    }
}

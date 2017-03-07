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

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;

public class ProcessDAO extends BaseDAO {

    private static final long serialVersionUID = 3538712266212954394L;

    /**
     * Find process object by id.
     *
     * @param id of searched object
     * @return result
     * @throws DAOException an exception that can be thrown from the underlying find() procedure failure.
     */
    public Process find(Integer id) throws DAOException {
        Process result = (Process) retrieveObject(Process.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all processes from the database.
     *
     * @return all persisted processes
     */
    @SuppressWarnings("unchecked")
    public List<Process> findAll() {
        return retrieveAllObjects(Process.class);
    }

    /**
     *
     * @param process object
     * @param progress service
     * @return process object
     * @throws DAOException an exception that can be thrown from the underlying save() procedure failure.
     */
    public Process save(Process process, String progress) throws DAOException {
        process.setSortHelperStatus(progress);
        storeObject(process);
        return (Process) retrieveObject(Process.class, process.getId());
    }

    /**
     *
     * @param list of processes
     * @throws DAOException an exception that can be thrown from the underlying saveList() procedure failure.
     */
    public void saveList(List<Process> list) throws DAOException {
        List<Object> l = new ArrayList<>();
        l.addAll(list);
        storeList(l);
    }

    /**
     * The function remove() removes a process from database.
     *
     * @param process to be removed
     * @throws DAOException an exception that can be thrown from the underlying save() procedure upon database
     * 				failure.
     */
    public void remove(Process process) throws DAOException {
        if (process.getId() != null) {
            removeObject(process);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(Process.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Process> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }

    /**
     * Never ending loop...
     * @param process object
     */
    public void refresh(Process process) {
        Object o = process;
        refresh(o);
    }

    /**
     * Never ending loop...
     * @param process object
     */
    public void update(Process process) {
        Object o = process;
        updateObject(o);
    }
}

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
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;

public class ProcessDAO extends BaseDAO<Process> {

    private static final int UPDATE_CHUNK_SIZE = 1000;

    @Override
    public Process getById(Integer id) throws DAOException {
        Process process = retrieveObject(Process.class, id);
        if (process == null) {
            throw new DAOException("Process " + id + " cannot be found in database");
        }
        return process;
    }

    @Override
    public List<Process> getAll() throws DAOException {
        return retrieveAllObjects(Process.class);
    }

    @Override
    public List<Process> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Process WHERE " + getDateFilter("creationDate") + " ORDER BY id ASC", offset,
            size);
    }

    @Override
    public List<Process> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Process WHERE " + getDateFilter("creationDate")
                + " AND (indexAction = 'INDEX' OR indexAction IS NULL) ORDER BY id ASC",
            offset, size);
    }

    @Override
    public void save(Process process) throws DAOException {
        process.dropKeywords();
        storeObject(process);
    }

    /**
     * Save process with regard to its progress.
     *
     * @param process
     *            object
     * @param progress
     *            service
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure failure.
     */
    public void save(Process process, String progress) throws DAOException {
        process.setSortHelperStatus(progress);
        save(process);
    }

    /**
     * Save list of processes.
     *
     * @param list
     *            of processes
     * @throws DAOException
     *             an exception that can be thrown from the underlying saveList()
     *             procedure failure.
     */
    public void saveList(List<Process> list) throws DAOException {
        for (Process process : list) {
            process.dropKeywords();
        }
        storeList(list);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Process.class, id);
    }

    /**
     * Sets the given import configuration for the processes identified by the
     * provided IDs.
     *
     * @param processIds
     *            IDs of processes to update
     * @param configuration
     *            import configuration to assign
     */
    public void setImportConfigurationForProcesses(
            List<Integer> processIds,
            ImportConfiguration configuration) throws DAOException {

        for (List<Integer> processIdChunk
                : ListUtils.partition(processIds, UPDATE_CHUNK_SIZE)) {

            executeUpdate(
                    "UPDATE Process process "
                            + "SET process.importConfiguration = :configuration "
                            + "WHERE process.id IN (:processIds)",
                    Map.of(
                            "configuration", configuration,
                            "processIds", processIdChunk
                    ));
        }
    }
}

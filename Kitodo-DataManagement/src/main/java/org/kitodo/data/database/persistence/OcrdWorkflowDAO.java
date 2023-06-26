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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.OcrdWorkflow;
import org.kitodo.data.database.exceptions.DAOException;

public class OcrdWorkflowDAO extends BaseDAO<OcrdWorkflow> {

    @Override
    public OcrdWorkflow getById(Integer id) throws DAOException {
        OcrdWorkflow ocrdWorkflow = retrieveObject(OcrdWorkflow.class, id);
        if (Objects.isNull(ocrdWorkflow)) {
            throw new DAOException("Object cannot be found in database");
        }
        return ocrdWorkflow;
    }

    @Override
    public List<OcrdWorkflow> getAll() throws DAOException {
        return retrieveAllObjects(OcrdWorkflow.class);
    }

    @Override
    public List<OcrdWorkflow> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM OCRWorkflow ORDER BY id ASC", offset, size);
    }

    @Override
    public List<OcrdWorkflow> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer ocrWorkflowId) throws DAOException {
        removeObject(OcrdWorkflow.class, ocrWorkflowId);
    }

    /**
     * Get available ocr workflows - available means that ocr workflow has status active and is
     * assigned to client with given id.
     *
     * @param clientId
     *            id of client to which searched ocr workflows should be assigned
     * @return list of available ocr workflow objects
     */
    public List<OcrdWorkflow> getAvailableOcrdWorkflows(int clientId) {
        return getByQuery(
                "SELECT w FROM OCRWorkflow AS w INNER JOIN w.client AS c WITH c.id = :clientId",
                Collections.singletonMap("clientId", clientId));
    }

}

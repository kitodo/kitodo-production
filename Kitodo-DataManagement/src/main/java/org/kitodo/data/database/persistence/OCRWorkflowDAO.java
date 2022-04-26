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

import org.kitodo.data.database.beans.OCRWorkflow;
import org.kitodo.data.database.exceptions.DAOException;

public class OCRWorkflowDAO extends BaseDAO<OCRWorkflow> {

    @Override
    public OCRWorkflow getById(Integer id) throws DAOException {
        OCRWorkflow ocrWorkflow = retrieveObject(OCRWorkflow.class, id);
        if (ocrWorkflow == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return ocrWorkflow;
    }

    @Override
    public List<OCRWorkflow> getAll() throws DAOException {
        return retrieveAllObjects(OCRWorkflow.class);
    }

    @Override
    public List<OCRWorkflow> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM OCRWorkflow ORDER BY id ASC", offset, size);
    }

    @Override
    public List<OCRWorkflow> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer ocrWorkflowId) throws DAOException {
        removeObject(OCRWorkflow.class, ocrWorkflowId);
    }
}

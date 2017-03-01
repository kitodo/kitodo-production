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

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;

import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpieceDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.WorkpieceType;

public class WorkpieceService {

    private WorkpieceDAO workpieceDao = new WorkpieceDAO();
    private WorkpieceType workpieceType = new WorkpieceType();
    private Indexer<Workpiece, WorkpieceType> indexer = new Indexer<>("kitodo", Workpiece.class);

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param workpiece object
     */
    public void save(Workpiece workpiece) throws DAOException, IOException {
        workpieceDao.save(workpiece);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    public Workpiece find(Integer id) throws DAOException {
        return workpieceDao.find(id);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param workpiece object
     */
    public void remove(Workpiece workpiece) throws DAOException, IOException {
        workpieceDao.remove(workpiece);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    public void remove(Integer id) throws DAOException {
        workpieceDao.remove(id);
    }

    /**
     * Get size of properties list.
     *
     * @param workpiece object
     * @return properties list size
     */
    public int getPropertiesSize(Workpiece workpiece) {
        if (workpiece.getProperties() == null) {
            return 0;
        } else {
            return workpiece.getProperties().size();
        }
    }
}

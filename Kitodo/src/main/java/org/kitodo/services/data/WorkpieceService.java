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

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.List;

import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpieceDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkpieceType;

public class WorkpieceService {

    private WorkpieceDAO workpieceDao = new WorkpieceDAO();
    private WorkpieceType workpieceType = new WorkpieceType();
    private Indexer<Workpiece, WorkpieceType> indexer = new Indexer<>(Workpiece.class);

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param workpiece
     *            object
     */
    public void save(Workpiece workpiece) throws DAOException, IOException {
        workpieceDao.save(workpiece);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    public Workpiece find(Integer id) throws DAOException {
        return workpieceDao.find(id);
    }

    public List<Workpiece> findAll() throws DAOException {
        return workpieceDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param workpiece
     *            object
     */
    public void remove(Workpiece workpiece) throws DAOException, IOException {
        workpieceDao.remove(workpiece);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws DAOException, IOException {
        workpieceDao.remove(id);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(id);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), workpieceType);
    }

    /**
     * Get size of properties list.
     *
     * @param workpiece
     *            object
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

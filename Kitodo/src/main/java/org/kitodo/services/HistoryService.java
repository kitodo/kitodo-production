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

import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HistoryDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.HistoryType;

/**
 * HistoryService.
 */
public class HistoryService {

    private HistoryDAO historyDao = new HistoryDAO();
    private HistoryType historyType = new HistoryType();
    private Indexer<History, HistoryType> indexer = new Indexer<>("kitodo", History.class);

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param history object
     */
    public void save(History history) throws DAOException, IOException {
        historyDao.save(history);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(history, historyType);
    }

    public History find(Integer id) throws DAOException {
        return historyDao.find(id);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param history object
     */
    public void remove(History history) throws DAOException, IOException {
        historyDao.remove(history);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(history, historyType);
    }

    public void remove(Integer id) throws DAOException {
        historyDao.remove(id);
    }
}

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

import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HistoryDAO;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.HistoryType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.SearchService;

/**
 * HistoryService.
 */
public class HistoryService extends SearchService {

    private HistoryDAO historyDao = new HistoryDAO();
    private HistoryType historyType = new HistoryType();
    private Indexer<History, HistoryType> indexer = new Indexer<>(History.class);

    /**
     * Constructor with searcher's assigning.
     */
    public HistoryService() {
        super(new Searcher(History.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param history
     *            object
     */
    public void save(History history) throws DAOException, IOException, ResponseException {
        historyDao.save(history);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(history, historyType);
    }

    public History find(Integer id) throws DAOException {
        return historyDao.find(id);
    }

    public List<History> findAll() throws DAOException {
        return historyDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param history
     *            object
     */
    public void remove(History history) throws DAOException, IOException, ResponseException {
        historyDao.remove(history);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(history, historyType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws DAOException, IOException, ResponseException {
        historyDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException, ResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), historyType);
    }
}

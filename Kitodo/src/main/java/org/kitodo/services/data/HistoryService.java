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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.database.persistence.HistoryDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.HistoryType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

/**
 * HistoryService.
 */
public class HistoryService extends SearchService<History> {

    private HistoryDAO historyDAO = new HistoryDAO();
    private HistoryType historyType = new HistoryType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(HistoryService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public HistoryService() {
        super(new Searcher(History.class));
        this.indexer = new Indexer<>(History.class);
    }

    /**
     * Method saves history object to database.
     *
     * @param history
     *            object
     */
    public void saveToDatabase(History history) throws DAOException {
        historyDAO.save(history);
    }

    /**
     * Method saves history document to the index of Elastic Search.
     *
     * @param history
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(History history) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (history != null) {
            indexer.performSingleRequest(history, historyType);
        }
    }

    /**
     * Method saves process related to modified history.
     *
     * @param history
     *            object
     */
    protected void manageDependenciesForIndex(History history) throws CustomResponseException, IOException {
        // TODO: is it possible that process is modified during save to history?
        if (history.getProcess() != null) {
            serviceManager.getProcessService().saveToIndex(history.getProcess());
        }
    }

    public History find(Integer id) throws DAOException {
        return historyDAO.find(id);
    }

    public List<History> findAll() {
        return historyDAO.findAll();
    }

    /**
     * Search History objects by given query.
     *
     * @param query
     *            as String
     * @return list of History objects
     */
    public List<History> search(String query) throws DAOException {
        return historyDAO.search(query);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return historyDAO.count("FROM History");
    }

    @Override
    public Long countDatabaseRows(String query) throws DAOException {
        return historyDAO.count(query);
    }

    /**
     * Method removes history object from database.
     *
     * @param history
     *            object
     */
    public void removeFromDatabase(History history) throws DAOException {
        historyDAO.remove(history);
    }

    /**
     * Method removes history object from database.
     *
     * @param id
     *            of history object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        historyDAO.remove(id);
    }

    /**
     * Method removes history object from index of Elastic Search.
     *
     * @param history
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(History history) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (history != null) {
            indexer.performSingleRequest(history, historyType);
        }
    }

    /**
     * Find histories with exact numeric value.
     *
     * @param numericValue
     *            of the searched histories
     * @return list of JSON objects
     */
    public List<JSONObject> findByNumericValue(Double numericValue) throws DataException {
        QueryBuilder query = createSimpleQuery("numericValue", numericValue.toString(), true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find histories with exact string value.
     *
     * @param stringValue
     *            of the searched histories
     * @return list of JSON objects
     */
    public List<JSONObject> findByStringValue(String stringValue) throws DataException {
        QueryBuilder query = createSimpleQuery("stringValue", stringValue, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find histories with exact type. Necessary to assure that user pickup type
     * from the list which contains enums.
     *
     * @param type
     *            of the searched histories as HistoryTypeEnum
     * @return list of JSON objects
     */
    public List<JSONObject> findByType(HistoryTypeEnum type) throws DataException {
        QueryBuilder query = createSimpleQuery("type", type.getValue(), true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find histories for exact date.
     *
     * @param date
     *            of the searched histories as Date
     * @return list of JSON objects
     */
    public List<JSONObject> findByDate(Date date) throws DataException {
        QueryBuilder queryBuilder = createSimpleCompareDateQuery("date", date, SearchCondition.EQUAL);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find history by id of process.
     *
     * @param id
     *            of process
     * @return search result with history for specific process id
     */
    public JSONObject findByProcessId(Integer id) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("process", id, true);
        return searcher.findDocument(queryBuilder.toString());
    }

    /**
     * Find history by title of process.
     *
     * @param processTitle
     *            title of process
     * @return JSON objects with history for specific process title
     */
    public List<JSONObject> findByProcessTitle(String processTitle) throws DataException {
        List<JSONObject> histories = new ArrayList<>();

        List<JSONObject> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (JSONObject process : processes) {
            histories.add(findByProcessId(getIdFromJSONObject(process)));
        }
        return histories;
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex() throws InterruptedException, IOException, CustomResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), historyType);
    }
}

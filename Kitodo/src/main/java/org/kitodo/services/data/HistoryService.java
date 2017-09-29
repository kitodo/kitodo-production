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

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.kitodo.dto.HistoryDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

/**
 * HistoryService.
 */
public class HistoryService extends SearchService<History, HistoryDTO, HistoryDAO> {

    private HistoryType historyType = new HistoryType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(HistoryService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public HistoryService() {
        super(new HistoryDAO(), new HistoryType(), new Indexer<>(History.class), new Searcher(History.class));
    }

    /**
     * Method saves process related to modified history.
     *
     * @param history
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(History history) throws CustomResponseException, IOException {
        // TODO: is it possible that process is modified during save to history?
        if (history.getProcess() != null) {
            serviceManager.getProcessService().saveToIndex(history.getProcess());
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM History");
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
        Set<Integer> processIds = new HashSet<>();

        List<JSONObject> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (JSONObject process : processes) {
            processIds.add(getIdFromJSONObject(process));
        }
        return searcher.findDocuments(createSetQuery("process", processIds, true).toString());
    }

    @Override
    public HistoryDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        HistoryDTO historyDTO = new HistoryDTO();
        historyDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject historyJSONObject = getSource(jsonObject);
        historyDTO.setStringValue(getStringPropertyForDTO(historyJSONObject,"stringValue"));
        return historyDTO;
    }
}

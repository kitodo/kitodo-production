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
import java.util.Objects;
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

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(HistoryService.class);
    private static HistoryService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private HistoryService() {
        super(new HistoryDAO(), new HistoryType(), new Indexer<>(History.class), new Searcher(History.class));
    }

    /**
     * Return singleton variable of type HistoryService.
     *
     * @return unique instance of HistoryService
     */
    public static HistoryService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (HistoryService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new HistoryService();
                }
            }
        }
        return instance;
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

    @Override
    public HistoryDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        HistoryDTO historyDTO = new HistoryDTO();
        historyDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject historyJSONObject = getSource(jsonObject);
        historyDTO.setStringValue(getStringPropertyForDTO(historyJSONObject,"stringValue"));
        return historyDTO;
    }
}

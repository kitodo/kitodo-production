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

package org.kitodo.production.services.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.DocketType;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ClientDTO;
import org.kitodo.production.dto.DocketDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.ClientSearchService;
import org.primefaces.model.SortOrder;

public class DocketService extends ClientSearchService<Docket, DocketDTO, DocketDAO> {

    private static volatile DocketService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private DocketService() {
        super(new DocketDAO(), new DocketType(), new Indexer<>(Docket.class), new Searcher(Docket.class),
                DocketTypeField.CLIENT_ID.getKey());
    }

    /**
     * Return singleton variable of type DocketService.
     *
     * @return unique instance of DocketService
     */
    public static DocketService getInstance() {
        DocketService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (DocketService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new DocketService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Docket");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Docket WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(getDocketsForCurrentUserQuery());
    }

    @Override
    public List<DocketDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(getDocketsForCurrentUserQuery(), getSortBuilder(sortField, sortOrder), first, pageSize,
            false);
    }

    @Override
    public List<Docket> getAllNotIndexed() {
        return getByQuery("FROM Docket WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Docket> getAllForSelectedClient() {
        return dao.getByQuery("SELECT d FROM Docket AS d INNER JOIN d.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public DocketDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        DocketDTO docketDTO = new DocketDTO();
        docketDTO.setId(getIdFromJSONObject(jsonObject));
        docketDTO.setTitle(DocketTypeField.TITLE.getStringValue(jsonObject));
        docketDTO.setFile(DocketTypeField.FILE.getStringValue(jsonObject));

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(DocketTypeField.CLIENT_ID.getIntValue(jsonObject));
        clientDTO.setName(DocketTypeField.CLIENT_NAME.getStringValue(jsonObject));

        docketDTO.setClientDTO(clientDTO);
        return docketDTO;
    }

    /**
     * Get list of dockets for given title.
     * 
     * @param title
     *            for get from database
     * @return list of dockets
     */
    public List<Docket> getByTitle(String title) {
        return dao.getByQuery("FROM Docket WHERE title = :title", Collections.singletonMap("title", title));
    }

    /**
     * Find docket with exact file name.
     *
     * @param file
     *            of the searched docket
     * @return search result
     */
    Map<String, Object> findByFile(String file) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND);
        return findDocument(query);
    }

    /**
     * Find dockets for client id.
     *
     * @param clientId
     *            of the searched dockets
     * @return search result
     */
    List<Map<String, Object>> findByClientId(Integer clientId) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.CLIENT_ID.getKey(), clientId, true);
        return findDocuments(query);
    }

    /**
     * Find docket with exact title and file name.
     * 
     * @param title
     *            of the searched docket
     * @param file
     *            of the searched docket
     * @return search result
     */
    Map<String, Object> findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(DocketTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.must(createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND));
        return findDocument(query);
    }

    /**
     * Find docket with exact title or file name.
     *
     * @param title
     *            of the searched docket
     * @param file
     *            of the searched docket
     * @return search result
     */
    List<Map<String, Object>> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(DocketTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.should(createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND));
        return findDocuments(query);
    }

    private QueryBuilder getDocketsForCurrentUserQuery() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(DocketTypeField.CLIENT_ID.getKey(),
                ServiceManager.getUserService().getSessionClientId(), true));
        return query;
    }
}

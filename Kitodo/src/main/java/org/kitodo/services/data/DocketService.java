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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.DocketType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.DocketDTO;
import org.kitodo.services.data.base.TitleSearchService;

public class DocketService extends TitleSearchService<Docket, DocketDTO, DocketDAO> {

    private DocketType docketType = new DocketType();
    private static final Logger logger = LogManager.getLogger(DocketService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public DocketService() {
        super(new DocketDAO(), new Searcher(Docket.class));
        this.indexer = new Indexer<>(Docket.class);
    }

    @Override
    public List<DocketDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    /**
     * Method saves docket document to the index of Elastic Search.
     *
     * @param docket
     *            object
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveToIndex(Docket docket) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (docket != null) {
            indexer.performSingleRequest(docket, docketType);
        }
    }

    /**
     * Method removes docket object from index of Elastic Search.
     *
     * @param docket
     *            object
     */
    @SuppressWarnings("unchecked")
    @Override
    public void removeFromIndex(Docket docket) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (docket != null) {
            indexer.performSingleRequest(docket, docketType);
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Docket");
    }

    /**
     * Find docket with exact file name.
     *
     * @param file
     *            of the searched docket
     * @return search result
     */
    JSONObject findByFile(String file) throws DataException {
        QueryBuilder query = createSimpleQuery("file", file, true, Operator.AND);
        return searcher.findDocument(query.toString());
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
    JSONObject findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("title", title, true, Operator.AND));
        query.must(createSimpleQuery("file", file, true, Operator.AND));
        return searcher.findDocument(query.toString());
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
    List<JSONObject> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery("title", title, true, Operator.AND));
        query.should(createSimpleQuery("file", file, true, Operator.AND));
        return searcher.findDocuments(query.toString());
    }

    /**
     * Get all dockets from index an convert them for frontend.
     *
     * @return list of DocketDTO objects
     */
    public List<DocketDTO> findAll() throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(), false);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex() throws InterruptedException, IOException, CustomResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(getAll(), docketType);
    }

    @Override
    public DocketDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        DocketDTO docketDTO = new DocketDTO();
        docketDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject docketJSONObject = getSource(jsonObject);
        docketDTO.setTitle(getStringPropertyForDTO(docketJSONObject, "title"));
        docketDTO.setFile(getStringPropertyForDTO(docketJSONObject, "file"));
        return docketDTO;
    }
}

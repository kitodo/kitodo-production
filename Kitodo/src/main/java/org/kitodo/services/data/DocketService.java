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

import java.util.List;
import java.util.Objects;

import javax.json.JsonObject;

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
import org.kitodo.dto.DocketDTO;
import org.kitodo.services.data.base.TitleSearchService;

public class DocketService extends TitleSearchService<Docket, DocketDTO, DocketDAO> {

    private static DocketService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private DocketService() {
        super(new DocketDAO(), new DocketType(), new Indexer<>(Docket.class), new Searcher(Docket.class));
    }

    /**
     * Return singleton variable of type DocketService.
     *
     * @return unique instance of DocketService
     */
    public static DocketService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (DocketService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new DocketService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Docket");
    }

    /**
     * Find docket with exact file name.
     *
     * @param file
     *            of the searched docket
     * @return search result
     */
    JsonObject findByFile(String file) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find dockets for client id.
     *
     * @param clientId
     *            of the searched dockets
     * @return search result
     */
    List<JsonObject> findByClientId(Integer clientId) throws DataException {
        QueryBuilder query = createSimpleQuery(DocketTypeField.CLIENT_ID.getKey(), clientId, true);
        return searcher.findDocuments(query.toString());
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
    JsonObject findByTitleAndFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(DocketTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.must(createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND));
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
    List<JsonObject> findByTitleOrFile(String title, String file) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(DocketTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.should(createSimpleQuery(DocketTypeField.FILE.getKey(), file, true, Operator.AND));
        return searcher.findDocuments(query.toString());
    }

    @Override
    public DocketDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        DocketDTO docketDTO = new DocketDTO();
        docketDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject docketJSONObject = jsonObject.getJsonObject("_source");
        docketDTO.setTitle(DocketTypeField.TITLE.getStringValue(docketJSONObject));
        docketDTO.setFile(DocketTypeField.FILE.getStringValue(docketJSONObject));
        return docketDTO;
    }
}

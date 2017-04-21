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

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.DocketType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.TitleSearchService;

public class DocketService extends TitleSearchService {
    private DocketDAO docketDao = new DocketDAO();
    private DocketType docketType = new DocketType();
    private Indexer<Docket, DocketType> indexer = new Indexer<>(Docket.class);

    /**
     * Constructor with searcher's assigning.
     */
    public DocketService() {
        super(new Searcher(Docket.class));
    }

    public Docket find(Integer id) throws DAOException {
        return docketDao.find(id);
    }

    public List<Docket> findAll() throws DAOException {
        return docketDao.findAll();
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param docket
     *            object
     */
    public void save(Docket docket) throws DAOException, IOException, ResponseException {
        docketDao.save(docket);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(docket, docketType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param docket
     *            object
     */
    public void remove(Docket docket) throws DAOException, IOException, ResponseException {
        docketDao.remove(docket);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(docket, docketType);
    }

    public List<Docket> search(String query) throws DAOException {
        return docketDao.search(query);
    }

    public Long count(String query) throws DAOException {
        return docketDao.count(query);
    }

    /**
     * Find docket with exact file name.
     *
     * @param file
     *            of the searched docket
     * @return search result
     */
    public SearchResult findByFile(String file) throws IOException, ParseException {
        QueryBuilder query = createSimpleQuery("file", file, true);
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
    public SearchResult findByTitleAndFile(String title, String file) throws IOException, ParseException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("title", title, true));
        query.must(createSimpleQuery("file", file, true));
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
    public List<SearchResult> findByTitleOrFile(String title, String file) throws IOException, ParseException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery("title", title, true));
        query.should(createSimpleQuery("file", file, true));
        System.out.println(query.toString());
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException, ResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), docketType);
    }
}

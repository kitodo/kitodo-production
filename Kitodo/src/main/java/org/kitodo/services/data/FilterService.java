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

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.FilterDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.FilterType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.data.base.SearchService;

/**
 * Service for Filter bean.
 */
public class FilterService extends SearchService<Filter> {

    private FilterDAO filterDAO = new FilterDAO();
    private FilterType filterType = new FilterType();

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public FilterService() {
        super(new Searcher(Filter.class));
        this.indexer = new Indexer<>(Filter.class);
    }

    /**
     * Method saves filter object to database.
     *
     * @param filter
     *            object
     */
    public void saveToDatabase(Filter filter) throws DAOException {
        filterDAO.save(filter);
    }

    /**
     * Method saves filter document to the index of Elastic Search.
     *
     * @param filter
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(Filter filter) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (filter != null) {
            indexer.performSingleRequest(filter, filterType);
        }
    }

    /**
     * Find in database.
     *
     * @param id
     *            as Integer
     * @return Property
     */
    public Filter find(Integer id) throws DAOException {
        return filterDAO.find(id);
    }

    /**
     * Find all properties in database.
     *
     * @return list of all properties
     */
    public List<Filter> findAll() {
        return filterDAO.findAll();
    }

    /**
     * Search by query in database.
     *
     * @param query
     *            as String
     * @return list of properties
     */
    public List<Filter> search(String query) throws DAOException {
        return filterDAO.search(query);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return filterDAO.count("FROM Filter");
    }

    @Override
    public Long countDatabaseRows(String query) throws DAOException {
        return filterDAO.count(query);
    }

    /**
     * Method removes filter object from database.
     *
     * @param filter
     *            object
     */
    public void removeFromDatabase(Filter filter) throws DAOException {
        filterDAO.remove(filter);
    }

    /**
     * Method removes property object from database.
     *
     * @param id
     *            of property object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        filterDAO.remove(id);
    }

    /**
     * Method removes filter object from index of Elastic Search.
     *
     * @param filter
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(Filter filter) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (filter != null) {
            indexer.performSingleRequest(filter, filterType);
        }
    }

    /**
     * Find filters with exact value.
     *
     * @param value
     *            of the searched filter
     * @param contains
     *            of the searched filter
     * @return list of JSON objects with properties
     */
    public List<JSONObject> findByValue(String value, boolean contains) throws DataException {
        QueryBuilder query = createSimpleQuery("value", value, contains, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex() throws CustomResponseException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), filterType);
    }
}

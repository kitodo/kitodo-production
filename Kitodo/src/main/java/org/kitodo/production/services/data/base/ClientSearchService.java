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

package org.kitodo.production.services.data.base;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;
import org.kitodo.production.services.ServiceManager;

public abstract class ClientSearchService<T extends BaseIndexedBean, S extends BaseDTO, V extends BaseDAO<T>>
        extends TitleSearchService<T, S, V> {

    private final String clientKey;

    /**
     * Constructor necessary to use searcher in child classes.
     *
     * @param dao
     *            DAO object for executing operations on database
     * @param type
     *            Type object for ElasticSearch
     * @param indexer
     *            for executing insert / updates to ElasticSearch
     * @param searcher
     *            for executing queries to ElasticSearch
     */
    public ClientSearchService(V dao, BaseType type, Indexer indexer, Searcher searcher, String clientKey) {
        super(dao, type, indexer, searcher);
        this.clientKey = clientKey;
    }

    /**
     * Get list of all objects for selected client from database.
     *
     * @return list of all objects for selected client from database
     */
    public abstract List<T> getAllForSelectedClient();

    private QueryBuilder createClientQuery() {
        int sessionClientId = ServiceManager.getUserService().getSessionClientId();
        return matchQuery(clientKey, sessionClientId);
    }

    private QueryBuilder queryForClient(QueryBuilder query) {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(query);
        return boolQuery.must(createClientQuery());
    }

    @Override
    protected QueryBuilder createSetQuery(String key, Set<?> values, boolean contains) {
        return queryForClient(super.createSetQuery(key, values, contains));
    }

    @Override
    protected QueryBuilder createSetQuery(String key, List<Map<String, Object>> values, boolean contains) {
        return queryForClient(super.createSetQuery(key, values, contains));
    }

    @Override
    protected QueryBuilder createSetQueryForBeans(String key, List<? extends BaseBean> values, boolean contains) {
        return queryForClient(super.createSetQueryForBeans(key, values, contains));
    }

    @Override
    protected QueryBuilder createSimpleQuery(String key, Integer id, boolean contains) {
        return queryForClient(super.createSimpleQuery(key, id, contains));
    }

    @Override
    protected QueryBuilder createSimpleQuery(String key, Boolean condition, boolean contains) {
        return queryForClient(super.createSimpleQuery(key, condition, contains));
    }

    @Override
    protected QueryBuilder createSimpleQuery(String key, String value, boolean contains) {
        return queryForClient(super.createSimpleQuery(key, value, contains));
    }

    @Override
    protected QueryBuilder createSimpleQuery(String key, String value, boolean contains, Operator operator) {
        return queryForClient(super.createSimpleQuery(key, value, contains, operator));
    }

    @Override
    protected QueryBuilder createSimpleCompareDateQuery(String key, Date date, SearchCondition searchCondition) {
        return queryForClient(super.createSimpleCompareDateQuery(key, date, searchCondition));
    }

    @Override
    protected QueryBuilder createSimpleCompareQuery(String key, Integer value, SearchCondition searchCondition) {
        return queryForClient(super.createSimpleCompareQuery(key, value, searchCondition));
    }

    @Override
    protected QueryBuilder createSimpleWildcardQuery(String key, String value) {
        return queryForClient(super.createSimpleWildcardQuery(key, value));
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, boolean related) throws DataException {
        return super.findByQuery(queryForClient(query), related);
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, boolean related) throws DataException {
        return super.findByQuery(queryForClient(query), sort, related);
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, Integer offset, Integer size, boolean related) throws DataException {
        return super.findByQuery(queryForClient(query), sort, offset, size, related);
    }

    @Override
    public Long countDocuments(QueryBuilder query) throws DataException {
        return super.countDocuments(queryForClient(query));
    }
}

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

import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;

/**
 * Class for implementing methods used by service classes which search for title
 * in ElasticSearch index.
 */
public abstract class TitleSearchService<T extends BaseIndexedBean, S extends BaseDTO, V extends BaseDAO<T>>
        extends SearchService<T, S, V> {

    private static final String TITLE = "title";

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
    public TitleSearchService(V dao, BaseType type, Indexer indexer, Searcher searcher) {
        super(dao, type, indexer, searcher);
    }

    /**
     * Find object matching to given title.
     *
     * @param title
     *            of the searched process
     * @param contains
     *            if true result should contain given plain text, if false it should
     *            not contain
     * @param withKeyword
     *             if query should be made for keyword
     * @return list of search result
     */
    public List<Map<String, Object>> findByTitle(String title, boolean contains, boolean withKeyword) throws DataException {
        return findDocuments(getQueryTitle(title, contains, withKeyword));
    }

    /**
     * Find object matching to given title.
     *
     * @param title
     *            of the searched process
     * @param contains
     *            if true result should contain given plain text, if false it should
     *            not contain
     * @return list of search result
     */
    public List<Map<String, Object>> findByTitle(String title, boolean contains) throws DataException {
        return findByTitle(title, contains, false);
    }

    /**
     * Find object matching to given title with wildcard.
     *
     * @param title
     *            of the searched process
     * @return list of search result
     */
    protected List<Map<String, Object>> findByTitleWithWildcard(String title) throws DataException {
        return findDocuments(getWildcardQueryTitle(title));
    }

    /**
     * Get query to find object matching to given title with keyword (exactMatch).
     *
     * @param title
     *            of the searched process
     * @param contains
     *            if true result should contain given plain text, if false it should
     *            not contain
     * @return query
     */
    public QueryBuilder getQueryTitle(String title, boolean contains, boolean withKeyword) {
        String titleKey = withKeyword ? TITLE + ".keyword" : TITLE;
        return createSimpleQuery(titleKey, title, contains, Operator.AND);
    }

    /**
     * Get wildcard query to given title.
     *
     * @param title
     *            of the searched process
     * @return query
     */
    public QueryBuilder getWildcardQueryTitle(String title) {
        return createSimpleWildcardQuery(TITLE, title);
    }

    /**
     * Sort results by title.
     * 
     * @param sortOrder
     *            ASC or DESC as SortOrder
     * @return sort as String
     */
    public SortBuilder sortByTitle(SortOrder sortOrder) {
        return SortBuilders.fieldSort(TITLE).order(sortOrder);
    }
}

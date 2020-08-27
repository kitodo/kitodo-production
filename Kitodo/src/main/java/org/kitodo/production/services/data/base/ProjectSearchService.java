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
import java.util.Objects;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.persistence.BaseDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;
import org.kitodo.production.services.ServiceManager;

public abstract class ProjectSearchService<T extends BaseIndexedBean, S extends BaseDTO, V extends BaseDAO<T>>
        extends ClientSearchService<T, S, V> {

    private final String projectKey;

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
    public ProjectSearchService(V dao, BaseType type, Indexer indexer, Searcher searcher, String clientKey,
            String projectKey) {
        super(dao, type, indexer, searcher, clientKey);
        this.projectKey = projectKey;
    }

    private QueryBuilder createUserProjectQuery() {
        User currentUser = ServiceManager.getUserService().getCurrentUser();

        if (Objects.nonNull(currentUser)) {
            List<Project> projects = currentUser.getProjects();
            return createSetQueryForBeans(projectKey, projects, true);
        }

        return null;
    }

    private BoolQueryBuilder queryForProjects(QueryBuilder query) {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(query);
        QueryBuilder userProjectQuery = createUserProjectQuery();
        if (Objects.nonNull(userProjectQuery)) {
            boolQuery.must(userProjectQuery);
        }
        return boolQuery;
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, boolean related) throws DataException {
        return super.findByQuery(queryForProjects(query), related);
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, boolean related) throws DataException {
        return super.findByQuery(queryForProjects(query), sort, related);
    }

    @Override
    public List<S> findByQuery(QueryBuilder query, SortBuilder sort, Integer offset, Integer size, boolean related)
            throws DataException {
        return super.findByQuery(queryForProjects(query), sort, offset, size, related);
    }

    @Override
    public Long countDocuments(QueryBuilder query) throws DataException {
        return super.countDocuments(queryForProjects(query));
    }
}

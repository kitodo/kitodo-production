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

package org.kitodo.production.services.index;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.DataException;
import org.hibernate.search.engine.search.projection.SearchProjection;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.BeanQuery;

public class IndexingService {

    private static final Logger logger = LogManager.getLogger(IndexingService.class);

    private static volatile IndexingService instance = null;

    String serverInformation;
    long serverLastCheck;
    long serverCheckThreadId;

    /**
     * Return singleton variable of type IndexingService.
     *
     * @return unique instance of IndexingService
     */
    public static IndexingService getInstance() {
        IndexingService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (IndexingService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new IndexingService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Standard constructor.
     */
    private IndexingService() {
        new Thread(new ServerConnectionChecker(this)).start();
    }

    /**
     * Returns the server information. This consists of the server service and
     * the version number as returned by the search server.
     * 
     * <!-- A thread to retrieve the server information is started when the
     * IndexingService is constructed. If the server information is still null,
     * the result of this thread is waited for. Otherwise, another thread is
     * started to retrieve the server information (which will only affect the
     * next time this information is retrieved) and then the stored server
     * information is returned. This keeps the comparatively slow HTTP requests
     * out of the GUI thread and a failure of the search server is still
     * displayed very promptly. -->
     * 
     * @return the server information
     */
    public String getServerInformation() {
        if (Objects.isNull(this.serverInformation)) {
            try {
                while (Objects.isNull(this.serverInformation)) {
                    Thread.sleep(25);
                }
            } catch (InterruptedException e) {
                logger.error(e);
                return "";
            }
        } else {
            new Thread(new ServerConnectionChecker(this)).start();
        }
        if (this.serverInformation.isEmpty()) {
            Helper.setErrorMessage("searchServerNotRunning");
        }
        return this.serverInformation;
    }

    /**
     * Starts indexing for a bean type.
     * 
     * @param type
     *            class of beans to be indexed
     * @param monitor
     *            object to be notified of progress changes
     * @return a CompletionStage that can react asynchronously when the indexing
     *         ends (including to exceptions)
     */
    public CompletionStage<?> startIndexing(Class<? extends BaseBean> type, MassIndexingMonitor monitor) {
        MassIndexer massIndexer = Search.session(HibernateUtil.getSession()).massIndexer(type);
        massIndexer.dropAndCreateSchemaOnStart(true);
        if (Objects.nonNull(monitor)) {
            massIndexer.monitor(monitor);
        }
        massIndexer.idFetchSize(Integer.MIN_VALUE).batchSizeToLoadObjects(1000);
        return massIndexer.start();
    }

    /**
     * Searches for entities matching all given field/value terms and returns their IDs.
     * 
     * @param beanClass
     *            class of beans to search for
     * @param terms
     *            list of field/value pairs to match (AND-combined)
     * @return ids of the found beans
     */
    public Collection<Integer> searchIds(Class<? extends BaseBean> beanClass, List<Pair<String, String>> terms) {
        SearchSession searchSession = Search.session(HibernateUtil.getSession());
        SearchProjection<Integer> idField = searchSession.scope(beanClass).projection().field("id", Integer.class)
                .toProjection();
        var query = searchSession.search(beanClass)
                .select(idField)
                .where(f -> {
                    var bool = f.bool();
                    for (var term : terms) {
                        bool.must(
                                f.match()
                                        .field(term.getLeft())
                                        .matching(term.getRight())
                        );
                    }
                    return bool;
                });
        List<Integer> ids = query.fetchAll().hits();

        logger.debug(
                "Searching {} IDs with terms {}: {} hits",
                beanClass.getSimpleName(),
                terms.stream()
                        .map(t -> t.getLeft() + "=\"" + t.getRight() + "\"")
                        .collect(Collectors.joining(", ")),
                ids.size()
        );
        return ids;
    }

    /**
     * Returns whether the search index is corrupted.
     * 
     * @return whether the index is corrupted
     */
    public boolean isIndexCorrupted() throws DAOException, DataException {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        Long totalCount = ServiceManager.getProcessService().count(beanQuery.formCountQuery(), beanQuery
                .getQueryParameters());
        return totalCount != getAllIndexed();
    }

    /**
     * Return the number of all objects processed during the current indexing
     * progress.
     *
     * @return long number of all currently indexed objects
     */
    public long getAllIndexed() {
        SearchSession searchSession = Search.session(HibernateUtil.getSession());
        long allIndexed = searchSession.search(Process.class).where(f -> f.matchAll()).fetchTotalHitCount();
        return allIndexed;
    }
}

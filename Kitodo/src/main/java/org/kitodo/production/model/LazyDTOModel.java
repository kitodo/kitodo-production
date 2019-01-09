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

package org.kitodo.production.model;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDTOModel extends LazyDataModel<Object> {

    private static final long serialVersionUID = 8782111495680176505L;
    private SearchDatabaseService searchService;
    private static final Logger logger = LogManager.getLogger(LazyDTOModel.class);
    private static IndexRestClient indexRestClient = IndexRestClient.getInstance();
    private transient List entities = new ArrayList();

    /**
     * Creates a LazyDTOModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in the
     * frontend.
     *
     * @param searchService
     *            the searchService which is used to retrieve data from the data
     *            source
     */
    public LazyDTOModel(SearchDatabaseService searchService) {
        indexRestClient.setIndex(ConfigMain.getParameter("elasticsearch.index", "kitodo"));
        this.searchService = searchService;

        try {
            this.setRowCount(toIntExact(searchService.countDatabaseRows()));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            this.setRowCount(0);
        }
    }

    @Override
    public Object getRowData(String rowKey) {
        try {
            return searchService.getById(Integer.parseInt(rowKey));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Object getRowKey(Object inObject) {
        if (inObject instanceof BaseDTO) {
            BaseDTO dto = (BaseDTO) inObject;
            return dto.getId();
        } else if (inObject instanceof BaseBean) {
            BaseBean bean = (BaseBean) inObject;
            return bean.getId();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List load(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        if (indexRunning()) {
            try {
                setRowCount(toIntExact(searchService.countDatabaseRows(searchService.createCountQuery(filters))));

                entities = searchService.loadData(first, pageSize, sortField, sortOrder, filters);
                logger.info(entities.size() + " entities loaded!");
                return entities;
            } catch (DAOException | DataException e) {
                logger.error(e.getMessage(), e);
                return new LinkedList();
            }
        } else {
            logger.info("Index not found!");
            return new LinkedList();
        }
    }

    /**
     * Checks and returns whether the ElasticSearch index is running or not.
     *
     * <p>
     * NOTE: This wrapper function is necessary because the calling "load" function
     * overwrites a function from the PrimeFaces LazyDataModel and therefore its
     * method signature cannot be changed, e.g. now thrown exceptions can be added
     * to it.
     * </p>
     *
     * @return whether the ElasticSearch index is running or not
     */
    private boolean indexRunning() {
        try {
            return indexRestClient.indexExists();
        } catch (IOException | CustomResponseException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Get entities.
     *
     * @return value of entities
     */
    public List getEntities() {
        return entities;
    }
}

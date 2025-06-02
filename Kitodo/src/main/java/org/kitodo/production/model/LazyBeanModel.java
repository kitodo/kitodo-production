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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.SQLGrammarException;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.services.data.BaseBeanService;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyBeanModel extends LazyDataModel<Object> {

    final transient BaseBeanService searchService;
    static final Logger logger = LogManager.getLogger(LazyBeanModel.class);
    transient List entities = new ArrayList<>();
    String filterString = "";

    /**
     * Creates a lazyBeanModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in
     * the frontend.
     *
     * @param searchService
     *            the searchService which is used to retrieve data from the data
     *            source
     */
    public LazyBeanModel(BaseBeanService searchService) {
        Stopwatch stopwatch = new Stopwatch(this, "LazyBeanModel");
        this.searchService = searchService;

        try {
            this.setRowCount(toIntExact(searchService.count()));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            this.setRowCount(0);
        }
        stopwatch.stop();
    }

    @Override
    public Object getRowData(String rowKey) {
        Stopwatch stopwatch = new Stopwatch(this, "getRowData");
        try {
            return stopwatch.stop(searchService.getById(Integer.parseInt(rowKey)));
        } catch (DAOException | NumberFormatException e) {
            logger.error(e.getMessage());
            return stopwatch.stop(null);
        }
    }

    @Override
    public Object getRowKey(Object inObject) {
        Stopwatch stopwatch = new Stopwatch(this, "getRowKey");
        if (inObject instanceof BaseBean) {
            BaseBean bean = (BaseBean) inObject;
            return stopwatch.stop(bean.getId());
        }
        return stopwatch.stop(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<String, FilterMeta> filters) {
        Stopwatch stopwatch = new Stopwatch(this, "load", "first", Integer.toString(first), "pageSize", Integer
                .toString(pageSize), "sortField", sortField, "sortOrder", Objects.toString(sortOrder), "filters",
                Objects.toString(filters));
        try {
            HashMap<String, String> filterMap = new HashMap<>();
            if (!StringUtils.isBlank(this.filterString)) {
                filterMap.put(FilterService.FILTER_STRING, this.filterString);
            }
            setRowCount(toIntExact(searchService.countResults(filterMap)));
            entities = searchService.loadData(first, pageSize, sortField, sortOrder, filterMap);
            logger.info("{} entities loaded!", entities.size());
            return stopwatch.stop(entities);
        } catch (DAOException | SQLGrammarException e) {
            setRowCount(0);
            logger.error(e.getMessage(), e);
        } catch (FilterException e) {
            setRowCount(0);
            PrimeFaces.current().executeScript("PF('sticky-notifications').renderMessage("
                    + "{'summary':'Filter error','detail':'" + e.getMessage() + "','severity':'error'});");
            logger.error(e.getMessage(), e);
        }
        return stopwatch.stop(new LinkedList<>());
    }

    /**
     * Get entities.
     *
     * @return value of entities
     */
    public List getEntities() {
        Stopwatch stopwatch = new Stopwatch(this, "getEntities");
        return stopwatch.stop(entities);
    }

    /**
     * Set filter String.
     *
     * @param filter
     *      as String
     */
    public void setFilterString(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "setFilterString");
        this.filterString = filter;
        stopwatch.stop();
    }
}

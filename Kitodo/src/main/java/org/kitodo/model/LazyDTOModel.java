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

package org.kitodo.model;

import static java.lang.Math.toIntExact;

import de.sub.goobi.forms.ProzessverwaltungForm;
import de.sub.goobi.helper.Helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.BaseDTO;
import org.kitodo.services.data.base.SearchService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDTOModel extends LazyDataModel<Object> {

    private static final long serialVersionUID = 8782111495680176505L;
    private boolean additionalFiltering = false;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(LazyDTOModel.class);

    /**
     * Creates a LazyDTOModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in the
     * frontend.
     *
     * @param searchService
     *            the searchService which is used to retrieve data from the data
     *            source
     */
    public LazyDTOModel(SearchService searchService) {
        this.searchService = searchService;

        try {
            this.setRowCount(toIntExact(searchService.count()));
        } catch (DataException e) {
            logger.error(e.getMessage());
            this.setRowCount(0);
        }
    }

    /**
     * Creates a LazyDTOModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in the
     * frontend.
     *
     * @param additionalFiltering true, set up only in ProzessverwaltungForm or SearchForm
     * @param searchService
     *            the searchService which is used to retrieve data from the data
     *            source
     */
    public LazyDTOModel(boolean additionalFiltering, SearchService searchService) {
        this.additionalFiltering = additionalFiltering;
        this.searchService = searchService;
        try {
            this.setRowCount(toIntExact(searchService.count()));
        } catch (DataException e) {
            logger.error(e.getMessage());
            this.setRowCount(0);
        }
    }

    @Override
    public Object getRowData(String rowKey) {
        try {
            return searchService.findById(Integer.parseInt(rowKey));
        } catch (DataException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Object getRowKey(Object inObject) {
        BaseDTO dto = (BaseDTO) inObject;
        return dto.getId();
    }

    @Override
    public List load(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        try {
            List entities;
            if (additionalFiltering) {
                ProzessverwaltungForm form = (ProzessverwaltungForm) Helper.getManagedBeanValue("#{ProzessverwaltungForm}");
                Map <String, String> filterMap = (Map<String, String>) filters;
                String filterString = null;
                if (filters.isEmpty()) {
                    form.setFilter("");
                } else {
                    for (Map.Entry<String, String> entry : filterMap.entrySet()) {
                        form.setFilter(entry.getValue());
                    }
                }
                form.filterAll();
                entities = form.getProcessDTOS();
            } else {
                if (!Objects.equals(sortField, null)
                        && Objects.equals(sortOrder, SortOrder.ASCENDING)) {
                    entities = searchService.findAll("{\"" + sortField + "\":\"asc\" }", first, pageSize);
                } else if (!Objects.equals(sortField, null)
                        && Objects.equals(sortOrder, SortOrder.DESCENDING)) {
                    entities = searchService.findAll("{\"" + sortField + "\":\"desc\" }", first, pageSize);
                } else {
                    entities = searchService.findAll(null, first, pageSize);
                }
            }
            logger.info(entities.size() + " entities loaded!");
            return entities;
        } catch (DataException e) {
            logger.error(e.getMessage());
            return new LinkedList();
        }
    }
}

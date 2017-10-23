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

package de.sub.goobi.model;

import static java.lang.Math.toIntExact;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.data.base.SearchService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDTOModel extends LazyDataModel {

    private static final long serialVersionUID = 8782111495680176505L;
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
    public List load(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        try {
            List entities;
            if (!Objects.equals(sortField, null)
                    && Objects.equals(sortOrder, SortOrder.ASCENDING)) {
                entities = searchService.findAll("{\"" + sortField + "\":\"asc\" }", first, pageSize);
            } else if (!Objects.equals(sortField, null)
                    && Objects.equals(sortOrder, SortOrder.DESCENDING)) {
                entities = searchService.findAll("{\"" + sortField + "\":\"desc\" }", first, pageSize);
            } else {
                entities = searchService.findAll(null, first, pageSize);
            }
            logger.info(entities.size() + " entities loaded!");
            return entities;
        } catch (DataException e) {
            logger.error(e.getMessage());
            return new LinkedList();
        }
    }
}

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.query.QueryShardException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.ProcessService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortOrder;

public class LazyProcessDTOModel extends LazyDTOModel {

    /**
     * The elastic search field that is used to sort processes by their correction comment status.
     */
    private static final String CORRECTION_COMMENT_STATUS_FIELD = "correctionCommentStatus";

    /**
     * The elastic search field that is used to sort processes by their progress status.
     */
    private static final String PROGRESS_COMBINED_FIELD = "progressCombined";

    /**
     * The elastic search field that is used to sort processes by their creation date.
     */
    private static final String CREATION_DATE_FIELD = "creationDate";

    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;

    /**
     * Creates a LazyDTOModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in the
     * frontend.
     *
     * @param searchService the searchService which is used to retrieve data from the data
     *                      source
     */
    public LazyProcessDTOModel(ProcessService searchService) {
        super(searchService);
    }

    /**
     * Set showClosedProcesses.
     *
     * @param showClosedProcesses
     *            as boolean
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    /**
     * Get showClosedProcesses.
     *
     * @return value of showClosedProcesses
     */
    public boolean isShowClosedProcesses() {
        return showClosedProcesses;
    }

    /**
     * Set showInactiveProjects.
     *
     * @param showInactiveProjects
     *            as boolean
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    /**
     * Get showInactiveProjects.
     *
     * @return value of showInactiveProjects
     */
    public boolean isShowInactiveProjects() {
        return showInactiveProjects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<String, FilterMeta> filters) {
        // reverse sort order for some process list columns such that first click on column yields more useful ordering
        if (sortField.equals(CORRECTION_COMMENT_STATUS_FIELD) || sortField.equals(PROGRESS_COMBINED_FIELD)
                || sortField.equals(CREATION_DATE_FIELD)) {
            sortOrder = sortOrder.equals(SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        }
        if (indexRunning()) {
            try {
                HashMap<String, String> filterMap = new HashMap<>();
                if (!StringUtils.isBlank(this.filterString)) {
                    filterMap.put(FilterService.FILTER_STRING, this.filterString);
                }
                setRowCount(toIntExact(((ProcessService)searchService).countResults(filterMap, this.showClosedProcesses,
                        this.showInactiveProjects)));
                entities = ((ProcessService)searchService).loadData(first, pageSize, sortField, sortOrder, filterMap,
                        this.showClosedProcesses, this.showInactiveProjects);
                logger.trace("{} entities loaded!", entities.size());
                return entities;
            } catch (DataException | ElasticsearchStatusException | QueryShardException e) {
                setRowCount(0);
                logger.error(e.getMessage(), e);
            } catch (FilterException e) {
                setRowCount(0);
                PrimeFaces.current().executeScript("PF('sticky-notifications').renderMessage("
                        + "{'summary':'Filter error','detail':'" + e.getMessage() + "','severity':'error'});");
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.info("Index not found!");
        }
        return new LinkedList<>();
    }

    @Override
    public Object getRowData() {
        List<Object> data = getWrappedData();
        if (isRowAvailable()){
            return data.get(getRowIndex());
        } else {
            return null;
        }
    }
}

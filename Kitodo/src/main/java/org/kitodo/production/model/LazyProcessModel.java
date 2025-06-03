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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class LazyProcessModel extends LazyBeanModel {

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
     * Creates a lazyBeanModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in
     * the frontend.
     *
     * @param searchService
     *            the searchService which is used to retrieve data from the data
     *            source
     */
    public LazyProcessModel(ProcessService searchService) {
        super(searchService);
    }

    /**
     * Set showClosedProcesses.
     *
     * @param showClosedProcesses
     *            as boolean
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowClosedProcesses", "showClosedProcesses", Boolean.toString(
            showClosedProcesses));
        this.showClosedProcesses = showClosedProcesses;
        stopwatch.stop();
    }

    /**
     * Get showClosedProcesses.
     *
     * @return value of showClosedProcesses
     */
    public boolean isShowClosedProcesses() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowClosedProcesses");
        return stopwatch.stop(showClosedProcesses);
    }

    /**
     * Set showInactiveProjects.
     *
     * @param showInactiveProjects
     *            as boolean
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowInactiveProjects", "showInactiveProjects", Boolean.toString(
            showInactiveProjects));
        this.showInactiveProjects = showInactiveProjects;
        stopwatch.stop();
    }

    /**
     * Get showInactiveProjects.
     *
     * @return value of showInactiveProjects
     */
    public boolean isShowInactiveProjects() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowInteractiveProjects");
        return stopwatch.stop(showInactiveProjects);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filters) {
        String sortField = "";
        SortOrder sortOrder = SortOrder.ASCENDING;
        Stopwatch stopwatch = new Stopwatch(this, "load", "first", Integer.toString(first), "pageSize", Integer
                .toString(pageSize), "sortField", sortField, "sortOrder", Objects.toString(sortOrder), "filters",
                Objects.toString(filters));
        if (!sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            sortField = sortMeta.getField();
            sortOrder = sortMeta.getOrder();
            // reverse sort order for some process list columns such that first click on column yields more useful ordering
            if (sortField.equals(CORRECTION_COMMENT_STATUS_FIELD) || sortField.equals(PROGRESS_COMBINED_FIELD)
                    || sortField.equals(CREATION_DATE_FIELD)) {
                sortOrder = sortOrder.equals(SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
            }
        }
        try {
            HashMap<String, String> filterMap = new HashMap<>();
            if (!StringUtils.isBlank(this.filterString)) {
                filterMap.put(FilterService.FILTER_STRING, this.filterString);
            }
            setRowCount(toIntExact(((ProcessService) searchService).countResults(filterMap, this.showClosedProcesses,
                this.showInactiveProjects)));
            entities = ((ProcessService) searchService).loadData(first, pageSize, sortField, sortOrder, filterMap,
                this.showClosedProcesses, this.showInactiveProjects);
            logger.trace("{} entities loaded!", entities.size());
            return stopwatch.stop(entities);
        } catch (DAOException e) {
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

    @Override
    public Object getRowData() {
        Stopwatch stopwatch = new Stopwatch(this, "getRowData");
        List<Object> data = getWrappedData();
        if (isRowAvailable()) {
            return stopwatch.stop(data.get(getRowIndex()));
        } else {
            return stopwatch.stop(null);
        }
    }
}

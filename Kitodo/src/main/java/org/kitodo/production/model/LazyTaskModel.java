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
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortOrder;

public class LazyTaskModel extends LazyBeanModel {

    /**
     * The elastic search field that is used to sort tasks by their processing status.
     */
    private static final String TASK_STATUS_FIELD = "processingStatus";

    /**
     * The elastic search field that is used to sort tasks by their correction comment status.
     */
    private static final String CORRECTION_COMMENT_STATUS_FIELD = "correctionCommentStatus";

    /**
     * The elastic search field that is used to sort tasks by the creation date of their process.
     */
    private static final String PROCESS_CREATION_DATE_FIELD = "process.creationDate";

    private boolean onlyOwnTasks = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;
    private List<TaskStatus> taskStatusRestriction = new LinkedList<>();

    /**
     * Creates a lazyBeanModel instance that allows fetching data from the data
     * source lazily, e.g. only the number of datasets that will be displayed in
     * the frontend.
     *
     * @param taskService
     *            the TaskService which is used to retrieve data from the data
     *            source
     */
    public LazyTaskModel(TaskService taskService) {
        super(taskService);
        Stopwatch stopwatch = new Stopwatch(this, "LazyTaskModel", "taskService", Objects.toString(taskService));
        this.taskStatusRestriction.add(TaskStatus.OPEN);
        this.taskStatusRestriction.add(TaskStatus.INWORK);
        stopwatch.stop();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<String, FilterMeta> filters) {
        Stopwatch stopwatch = new Stopwatch(this, "load", "first", Integer.toString(first), "pageSize", Integer
                .toString(pageSize), "sortField", sortField, "sortOrder", Objects.toString(sortOrder), "filters",
                Objects.toString(filters));
        // reverse sort order for some task list columns such that first click on column yields more useful ordering
        if (sortField.equals(TASK_STATUS_FIELD) || sortField.equals(CORRECTION_COMMENT_STATUS_FIELD) 
                || sortField.equals(PROCESS_CREATION_DATE_FIELD)) {
            sortOrder = sortOrder.equals(SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        }
        try {
            HashMap<String, String> filterMap = new HashMap<>();
            if (!StringUtils.isBlank(this.filterString)) {
                filterMap.put(FilterService.FILTER_STRING, this.filterString);
            }
            setRowCount(toIntExact(((TaskService) searchService).countResults(filterMap, this.onlyOwnTasks,
                this.hideCorrectionTasks, this.showAutomaticTasks, this.taskStatusRestriction)));
            entities = ((TaskService) searchService).loadData(first, pageSize, sortField, sortOrder, filterMap,
                this.onlyOwnTasks, this.hideCorrectionTasks, this.showAutomaticTasks, this.taskStatusRestriction);
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

    /**
     * Set onlyOwnTasks.
     *
     * @param onlyOwnTasks as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setOnlyOwnTasks");
        this.onlyOwnTasks = onlyOwnTasks;
        stopwatch.stop();
    }

    /**
     * Set taskStatusRestriction.
     *
     * @param taskStatusRestriction as org.kitodo.data.database.enums.TaskStatus
     */
    public void setTaskStatusRestriction(List<TaskStatus> taskStatusRestriction) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusRestriction", "taskStatusRestriction", Objects.toString(
            taskStatusRestriction));
        this.taskStatusRestriction = taskStatusRestriction;
        stopwatch.stop();
    }

    /**
     * Set showAutomaticTasks.
     *
     * @param showAutomaticTasks as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowAutomaticTasks", "showAutomaticTasks", Boolean.toString(
            showAutomaticTasks));
        this.showAutomaticTasks = showAutomaticTasks;
        stopwatch.stop();
    }

    /**
     * Get hideCorrectionTasks.
     *
     * @return value of hideCorrectionTasks
     */
    public boolean isHideCorrectionTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "isHideCorrectionTasks");
        return stopwatch.stop(hideCorrectionTasks);
    }

    /**
     * Set hideCorrectionTasks.
     *
     * @param hideCorrectionTasks as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setHideCorrectionTasks", "hideCorrectionTasks", Boolean.toString(
            hideCorrectionTasks));
        this.hideCorrectionTasks = hideCorrectionTasks;
        stopwatch.stop();
    }

}

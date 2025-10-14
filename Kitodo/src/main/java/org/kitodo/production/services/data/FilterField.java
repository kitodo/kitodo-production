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

package org.kitodo.production.services.data;

import java.util.Objects;

import net.bytebuddy.utility.nullability.MaybeNull;

import org.kitodo.data.database.beans.ProcessKeywords;
import org.kitodo.data.database.enums.TaskStatus;

/**
 * Constants for known search field names in filters.
 */
enum FilterField {
    SEARCH(null, null, null, null, null, null, "search", ProcessKeywords.LENGTH_MIN_REASONABLE),
    PROCESS_ID(null, null, null, "id", "process.id", null, null, -1),
    PARENT_PROCESS_ID(null, null, null, "parent.id", "process.parent.id", null, null, -1),
    PROCESS_TITLE("title", "process.title", LikeSearch.NO, null, null, null, "searchTitle",
            ProcessKeywords.LENGTH_MIN_DEFAULT),
    PROJECT("project.title", "process.project.title", LikeSearch.ALLOWED, "project.id", "process.project.id", null,
            null, -1),
    PROJECT_LOOSE("project.title", "process.project.title", LikeSearch.ALWAYS_RIGHT, "project.id", "process.project.id",
            null, null, -1),
    BATCH("process.batches AS batch WITH batch.title", "process.batches AS batch WITH batch.title",
            LikeSearch.NO, "batches AS batch WITH batch.id", "process.batches AS batch WITH batch.id", null, null, -1),
    TASK("tasks AS task WITH task.title", "title", LikeSearch.NO, "tasks AS task WITH task.id", "id", null, null, -1),
    TASK_AUTOMATIC("tasks AS task WITH task.typeAutomatic = :queryObject AND task.title",
            "~.typeAutomatic = :queryObject AND ~.title", LikeSearch.NO,
            "tasks AS task WITH task.typeAutomatic = :queryObject AND task.id", "typeAutomatic = :queryObject AND id",
            Boolean.TRUE, null, -1),
    TASK_UNREADY("tasks AS task WITH task.processingStatus = :queryObject AND task.title",
            "~.processingStatus = :queryObject AND ~.title", LikeSearch.NO,
            "tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.LOCKED, null, -1),
    TASK_READY("tasks AS task WITH task.processingStatus = :queryObject AND task.title",
            "~.processingStatus = :queryObject AND ~.title", LikeSearch.NO,
            "tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.OPEN, null, -1),
    TASK_ONGOING("tasks AS task WITH task.processingStatus = :queryObject AND task.title",
            "~.processingStatus = :queryObject AND ~.title", LikeSearch.NO,
            "tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.INWORK, null, -1),
    TASK_FINISHED("tasks AS task WITH task.processingStatus = :queryObject AND task.title",
            "~.processingStatus = :queryObject AND ~.title", LikeSearch.NO,
            "tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.DONE, null, -1),
    TASK_FINISHED_USER(
            "tasks AS task WITH task.processingStatus = :queryObject AND (task.processingUser.name = # OR task.processingUser.surname = # "
                    .concat("OR task.processingUser.login = # OR task.processingUser.ldapLogin = #)"),
            "~.processingStatus = :queryObject AND (~.processingUser.name = # OR ~.processingUser.surname = # "
                    .concat("OR ~.processingUser.login = # OR ~.processingUser.ldapLogin = #)"), LikeSearch.NO,
            "tasks AS task WITH task.processingStatus = :queryObject AND task.processingUser.id",
            "processingStatus = :queryObject AND processingUser.id", TaskStatus.DONE, null, -1);

    /**
     * Here the string search field names (user input) are mapped to the
     * filters.
     * 
     * @param fieldName
     *            user input string
     * @return the constant
     */
    static FilterField ofString(String fieldName) {
        if (Objects.isNull(fieldName)) {
            return null;
        }
        switch (fieldName.toLowerCase()) {
            case "": return null;
            case "id": return PROCESS_ID;
            case "parentprocessid":
                return PARENT_PROCESS_ID;
            case "process": return PROCESS_TITLE;
            case "search": return SEARCH;
            case "project": return PROJECT;
            case "project_loose":
                return PROJECT_LOOSE;
            case "batch": return BATCH;
            case "step": return TASK;
            case "stepautomatic": return TASK_AUTOMATIC;
            case "steplocked": return TASK_UNREADY;
            case "stepopen": return TASK_READY;
            case "stepinwork": return TASK_ONGOING;
            case "stepdone": return TASK_FINISHED;
            case "stepdonetitle": return TASK_FINISHED;
            case "stepdoneuser": return TASK_FINISHED_USER;

            case "prozess": return PROCESS_TITLE;
            case "elternprozessid":
                return PARENT_PROCESS_ID;
            case "projekt": return PROJECT;
            case "projekt_trunkiert":
                return PROJECT_LOOSE;
            case "gruppe": return BATCH;
            case "schritt": return TASK;
            case "schrittautomatisch": return TASK_AUTOMATIC;
            case "schrittgesperrt": return TASK_UNREADY;
            case "schrittoffen": return TASK_READY;
            case "schrittinarbeit": return TASK_ONGOING;
            case "schrittabgeschlossen": return TASK_FINISHED;
            case "abgeschlossenerschritttitel": return TASK_FINISHED;
            case "abgeschlossenerschrittbenutzer": return TASK_FINISHED_USER;
            default: return null;
        }
    }

    private final String processTitleQuery;
    private final String taskTitleQuery;
    private final LikeSearch likeSearch;
    private final String processIdQuery;
    private final String taskIdQuery;
    private final Object queryObject;
    private final String searchField;
    private final int minTokenLength;

    /**
     * Creates a filter field enum constant.
     * 
     * @param processTitleQuery
     *            search for processes by label in the database
     * @param taskTitleQuery
     *            search for tasks by label in the database
     * @param likeSearch
     *            like search settings
     * @param processIdQuery
     *            search for processes by id in the database
     * @param taskIdQuery
     *            search for tasks by id in the database
     * @param queryObject
     *            object {@code :queryObject}, if used in the query
     * @param searchField
     *            search field for index search
     * @param minTokenLength
     *            minimum length of searchable token
     */
    FilterField(String processTitleQuery, String taskTitleQuery, LikeSearch likeSearch, String processIdQuery,
            String taskIdQuery, Object queryObject, String searchField, int minTokenLength) {
        this.processTitleQuery = processTitleQuery;
        this.taskTitleQuery = taskTitleQuery;
        this.likeSearch = likeSearch;
        this.processIdQuery = processIdQuery;
        this.taskIdQuery = taskIdQuery;
        this.queryObject = queryObject;
        this.searchField = searchField;
        this.minTokenLength = minTokenLength;
    }

    /**
     * If not null, this query can be used to search for a process object by
     * title.
     * 
     * @return query to search for a process object
     */
    @MaybeNull
    String getProcessTitleQuery() {
        return processTitleQuery;
    }

    /**
     * If not null, this query can be used to search for a task object by title.
     * 
     * @return query to search for a task object
     */
    @MaybeNull
    String getTaskTitleQuery() {
        return taskTitleQuery;
    }

    /**
     * Returns the field-bound like search settings for the search field. It is
     * {@code null} only if the search field itself cannot perform text database
     * searches.
     * 
     * @return like search settings
     */
    @MaybeNull
    LikeSearch getLikeSearch() {
        return likeSearch;
    }

    /**
     * If not null, this query can be used to search for a process object by ID.
     * 
     * @return query to search for a process object
     */
    @MaybeNull
    String getProcessIdQuery() {
        return processIdQuery;
    }

    /**
     * If not null, this query can be used to search for a task object by ID.
     * 
     * @return query to search for a task object
     */
    @MaybeNull
    String getTaskIdQuery() {
        return taskIdQuery;
    }

    /**
     * If not null, this object must be added as parameter "queryObject" to the
     * query parameters.
     * 
     * @return object to be added to the query parameters
     */
    @MaybeNull
    Object getQueryObject() {
        return queryObject;
    }

    /**
     * Search field on the process index to search for keywords. Returns
     * {@code null} if this search field does not allow index search.
     * 
     * @return search field, may be {@code null}
     */
    @MaybeNull
    String getSearchField() {
        return searchField;
    }

    /**
     * Minimum length for searchable tokens. Tokens that are too short are not
     * indexed because they bloat the index and quickly lead to all candidate
     * matches, resulting in unnecessary computing and no benefit. Therefore,
     * they must be filtered out of the query; otherwise, searching for them
     * will return 0 matches.
     * 
     * @return minimum length of searchable token
     */
    int getMinTokenLength() {
        return minTokenLength;
    }
}

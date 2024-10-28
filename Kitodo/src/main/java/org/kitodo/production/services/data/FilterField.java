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

import org.kitodo.data.database.enums.TaskStatus;

/**
 * Constants for known search field names in filters.
 */
enum FilterField {
    MISC(null, null, null, "search", null),
    PROCESS_ID("id", "process.id", null, null, null),
    PROCESS_TITLE(null, null, null, "searchTitle", null),
    PROJECT("project.id", "process.project.id", null, "searchProject", null),
    BATCH("batches AS batch WITH batch.id", "process.batches AS batch WITH batch.id", null, "searchBatch", null),
    TASK("tasks AS task WITH task.id", "id", null, "searchTask", null),
    TASK_AUTOMATIC("tasks AS task WITH task.typeAutomatic = :queryObject AND task.id",
            "typeAutomatic = :queryObject AND id", Boolean.TRUE, "searchTask", "automatic"),
    TASK_UNREADY("tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.LOCKED, "searchTask", "locked"),
    TASK_READY("tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.OPEN, "searchTask", "open"),
    TASK_ONGOING("tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.INWORK, "searchTask", "inwork"),
    TASK_FINISHED("tasks AS task WITH task.processingStatus = :queryObject AND task.id",
            "processingStatus = :queryObject AND id", TaskStatus.DONE, "searchTask", "closed"),
    TASK_FINISHED_USER("tasks AS task WITH task.processingStatus = :queryObject AND task.processingUser.id",
            "processingStatus = :queryObject AND processingUser.id", TaskStatus.DONE, "searchTask", "closeduser"),
    METADATA_SOURCEMD(null, null, null, "searchMetadata", "source"),
    METADATA_TECHMD(null, null, null, "searchMetadata", "technical"),
    METADATA_DMDSEC(null, null, null, "searchMetadata", "description");

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
            return MISC;
        }
        switch (fieldName.toLowerCase()) {
            case "":
                return MISC;
            case "id":
                return PROCESS_ID;
            case "process":
                return PROCESS_TITLE;
            case "project":
                return PROJECT;
            case "batch":
                return BATCH;
            case "step":
                return TASK;
            case "stepautomatic":
                return TASK_AUTOMATIC;
            case "steplocked":
                return TASK_UNREADY;
            case "stepopen":
                return TASK_READY;
            case "stepinwork":
                return TASK_ONGOING;
            case "stepdone":
                return TASK_FINISHED;
            case "stepdonetitle":
                return TASK_FINISHED;
            case "stepdoneuser":
                return TASK_FINISHED_USER;
            case "template":
                return METADATA_SOURCEMD;
            case "processproperty":
                return METADATA_TECHMD;
            case "workpiece":
                return METADATA_DMDSEC;

            case "prozess":
                return PROCESS_TITLE;
            case "projekt":
                return PROJECT;
            case "gruppe":
                return BATCH;
            case "schritt":
                return TASK;
            case "schrittautomatisch":
                return TASK_AUTOMATIC;
            case "schrittgesperrt":
                return TASK_UNREADY;
            case "schrittoffen":
                return TASK_READY;
            case "schrittinarbeit":
                return TASK_ONGOING;
            case "schrittabgeschlossen":
                return TASK_FINISHED;
            case "abgeschlossenerschritttitel":
                return TASK_FINISHED;
            case "abgeschlossenerschrittbenutzer":
                return TASK_FINISHED_USER;
            case "vorlage":
                return METADATA_SOURCEMD;
            case "prozesseigenschaft":
                return METADATA_TECHMD;
            case "werkstueck":
                return METADATA_TECHMD;
            default:
                return null;
        }
    }

    private final String processQuery;
    private final String taskQuery;
    private final Object queryObject;
    private final String searchField;
    private final String domain;

    private FilterField(String processQuery, String taskQuery, Object queryObject, String searchField, String domain) {
        this.processQuery = processQuery;
        this.taskQuery = taskQuery;
        this.queryObject = queryObject;
        this.searchField = searchField;
        this.domain = domain;
    }

    /**
     * If true, searches can be performed on various metadata.
     * 
     * @return whether different metadata can be searched
     */
    boolean isDivisible() {
        return Objects.isNull(processQuery) && Objects.nonNull(domain);
    }

    /**
     * If not null, this query can be used to search for a process object by ID.
     * 
     * @return query to search for a process object
     */
    String getProcessQuery() {
        return processQuery;
    }

    /**
     * If not null, this query can be used to search for a task object by ID.
     * 
     * @return query to search for a task object
     */
    String getTaskQuery() {
        return taskQuery;
    }

    /**
     * If not null, this object must be added as parameter "queryObject" to the
     * query parameters.
     * 
     * @return object to be added to the query parameters
     */
    Object getQueryObject() {
        return queryObject;
    }

    /**
     * Search field on the process index or task index to search for keywords.
     * 
     * @return search field
     */
    String getSearchField() {
        return searchField;
    }

    /**
     * Word component to limit the index search.
     * 
     * @return word component
     */
    String getDomain() {
        return domain;
    }
}

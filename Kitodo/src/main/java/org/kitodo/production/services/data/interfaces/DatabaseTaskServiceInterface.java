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

package org.kitodo.production.services.data.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.primefaces.model.SortOrder;

public interface DatabaseTaskServiceInterface extends SearchDatabaseServiceInterface<Task> {

    /**
     * Returns the number of objects of the implementing type that the filter
     * matches.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     *
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     * @param onlyOwnTasks
     *            whether only tasks, to which the current user is already
     *            assigned as processor (usually not)
     * @param hideCorrectionTasks
     *            whether tasks in correction runs should be hidden (usually
     *            not)
     * @param showAutomaticTasks
     *            whether automatic tasks should be included (usually not)
     * @param taskStatus
     *            tasks in what status Tasks in what status. Must not be
     *            {@code} null. One of: [OPEN, INWORK], [OPEN], [INWORK] or [].
     * @return the number of matching objects
     * @throws DAOException
     *             database access error
     * @throws DataException
     *             index access error
     */
    /*
     * Here, an additional function countResults() is specified with additional
     * parameters, and the generally specified function from
     * SearchDatabaseServiceInterface is not used. However, in
     * DatabaseTemplateServiceInterface, a value is set that affects the
     * generally specified functions countResults() and loadData() in
     * SearchDatabaseServiceInterface. This could be equalized at some point in
     * the future.
     */
    Long countResults(Map<?, String> filters, boolean onlyOwnTasks, boolean hideCorrectionTasks,
            boolean showAutomaticTasks, List<TaskStatus> taskStatus) throws DataException;

    /**
     * Runs a script for a task. The script can be a programmatic branch into
     * the application, a so-called Kitodo Script, or a command line call. If it
     * is an automatic task, depending on the result of the script, the task is
     * completed or set to open and the changed state is immediately saved to
     * the database.
     *
     * @param task
     *            task in which the script is executed
     * @param automatic
     *            if so, the task is set to completed if the outcome is positive
     *            (without errors, for a command line call with the return code
     *            0), and if the outcome is negative it is set to open; and the
     *            object is then immediately updated in the database.
     */
    void executeScript(Task task, boolean automatic) throws DataException;

    /**
     * Runs a script for a task. The script can be a programmatic branch into
     * the application, a so-called Kitodo Script, or a command line call. If it
     * is an automatic task, depending on the result of the script, the task is
     * completed or set to open and the changed state is immediately saved to
     * the database.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * In previous versions, up to five scripts could be available in a task,
     * and the operator would then start one or another of them manually.
     * Therefore, the script can still be passed here (because there were
     * several of them). Actually, this is no longer necessary, since there is
     * only one script per task remaining, and that can be cleaned up one day.
     *
     * @param task
     *            task in which the script is executed
     * @param script
     *            The command to be executed. If it starts with "action:", it is
     *            an internal function call, otherwise a command line that the
     *            JVM executes with the user it runs under.
     * @param automatic
     *            if so, the task is set to completed if the outcome is positive
     *            (without errors, for a command line call with the return code
     *            0), and if the outcome is negative it is set to open; and the
     *            object is then immediately updated in the database.
     * @return whether it had a successful outcome
     */
    boolean executeScript(Task task, String script, boolean automatic) throws DataException;

    /**
     * Determines all processes with a specific production template.
     *
     * @param templateId
     *            record number of the production template
     * @return list that is not empty if something was found, otherwise empty
     *         list
     * @throws DataException
     *             if an error occurred during the search
     */
    /*
     * Used in TemplateForm to find out whether a production template is used in
     * a process. (Then it may not be deleted.) Is only checked for isEmpty().
     */
    public Collection<?> findByTemplateId(Integer id) throws DataException;

    /**
     * Finds all task names that each differ from any other. That means, no
     * doubles.
     *
     * <p>
     * <b>API Note:</b><br>
     * This method actually returns all task names of all clients and is
     * therefore more suitable for operational purposes, rather not for display
     * purposes.
     *
     * @return all different task names
     */
    List<String> findTaskTitlesDistinct() throws DataException, DAOException;

    /**
     * Generates browser-friendly images. (thumbnails, or even full images in
     * any format used by browsers)
     *
     * @param executingThread
     *            Background thread that controls image generation. In this
     *            thread, display fields are updated so that the user can see
     *            the progress.
     * @param task
     *            task whose status should be set upon completion
     * @param automatic
     *            if so, the task is set to completed if the processing was
     *            completed without errors, and else it is set to open; and the
     *            object is then immediately updated in the database.
     * @throws DataException
     *             if the task cannot be saved
     */
    void generateImages(EmptyTask executingThread, Task task, boolean automatic) throws DataException;

    /**
     * Returns all tasks that lie between the two specified tasks in the
     * processing sequence.
     *
     * @param previousOrdering
     *            processing sequence number of the previous task, that is the
     *            task in which the error can be corrected
     * @param laterOrdering
     *            processing sequence number of the later task, that is the task
     *            in which the error was discovered
     * @param processId
     *            record number of the process whose tasks are being searched
     * @return the tasks in between
     */
    List<Task> getAllTasksInBetween(Integer previousOrdering, Integer laterOrdering, Integer processId);

    /**
     * Returns all tasks in the batch with the specified name.
     *
     * @param title
     *            task title
     * @param batchId
     *            batch number
     * @return all identified tasks
     */
    List<Task> getCurrentTasksOfBatch(String title, Integer batchId);

    /**
     * Finds all tasks that preceded the current one. This is used when a user
     * wants to send an error message to a previous task, so the user can choose
     * which task they want to send the message to.
     *
     * @param ordering
     *            processing sequence number of the task in which the error was
     *            discovered
     * @param processId
     *            record number of the process whose tasks are being searched
     * @return list of Task objects
     */
    List<Task> getPreviousTasksForProblemReporting(Integer ordering, Integer processId);

    /**
     * Provides a window onto the task objects. This makes it possible to
     * navigate through the tasks page by page, without having to load all
     * objects into memory.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function filters the data according to the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * @param offset
     *            number of objects to be skipped at the list head
     * @param limit
     *            maximum number of objects to return
     * @param sortField
     *            by which column the data should be sorted. Must not be
     *            {@code null} or empty.<br>
     *            One of:<br>
     *            <ul>
     *            <li>"title.keyword": Title</li>
     *            <li>"processForTask.id": Process ID</li>
     *            <li>"processForTask.title.keyword": Process</li>
     *            <li>"processingStatus": Status</li>
     *            <li>"processingUser.name.keyword": Last editing user</li>
     *            <li>"processingBegin": Start of work</li>
     *            <li>"processingEnd": End of work</li>
     *            <li>"correctionCommentStatus": Comments</li>
     *            <li>"projectForTask.title.keyword": Project</li>
     *            <li>"processForTask.creationDate": Duration (Process)
     *            [sic!]</li>
     *            </ul>
     * @param sortOrder
     *            sort ascending or descending?
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     * @param onlyOwnTasks
     *            whether only tasks, to which the current user is already
     *            assigned as processor (usually not)
     * @param hideCorrectionTasks
     *            whether tasks in correction runs should be hidden (usually
     *            not)
     * @param showAutomaticTasks
     *            whether automatic tasks should be included (usually not)
     * @param taskStatus
     *            Tasks in what status. Must not be {@code} null. One of: [OPEN,
     *            INWORK], [OPEN], [INWORK] or [].
     * @return the data objects to be displayed
     * @throws DataException
     *             if processes cannot be loaded from search index
     */
    /*
     * Here, an additional function loadData() is specified with additional
     * parameters, and the generally specified function from
     * SearchDatabaseServiceInterface is not used. However, in
     * DatabaseTemplateServiceInterface, a value is set that affects the
     * generally specified functions countResults() and loadData() in
     * SearchDatabaseServiceInterface. This could be equalized at some point in
     * the future.
     */
    List<Task> loadData(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<?, String> filters, boolean onlyOwnTasks, boolean hideCorrectionTasks, boolean showAutomaticTasks,
            List<TaskStatus> taskStatus) throws DataException;

    // === alternative functions that are no longer required ===

    /**
     * Find object in ES and convert it to Interface.
     *
     * @param id
     *            object id
     * @return Interface object
     * @deprecated Use {@link #getById(Integer)}.
     */
    @Deprecated
    default TaskInterface findById(Integer id) throws DataException {
        try {
            return getById(id);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }
}

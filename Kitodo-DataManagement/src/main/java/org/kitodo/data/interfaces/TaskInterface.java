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

package org.kitodo.data.interfaces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;

public interface TaskInterface extends DataInterface {

    /**
     * Returns the name of the task. This is usually a human-readable,
     * aphoristic summary of the activity to be performed.
     *
     * @return the name
     */
    String getTitle();

    /**
     * Sets the name of the task.
     *
     * @param title
     *            name to set
     */
    void setTitle(String title);

    /**
     * Returns the translated name of the task at runtime. This can be
     * translated at runtime for the user currently performing this task via the
     * application's language resource files. Can be {@code null} if currently
     * no value is available.
     *
     * @return translated name
     */
    String getLocalizedTitle();

    /**
     * Sets the translated name of the task at runtime. This is a transient
     * value that is not persisted.
     *
     * @param localizedTitle
     *            translated name to set
     */
    void setLocalizedTitle(String localizedTitle);

    /**
     * Returns the ordinal number of the task. Tasks can be processed
     * sequentially. When a task with a lower ordinal number is completed, the
     * task with the next higher ordinal number can be processed. If several
     * tasks share the same ordinal number, they can be processed in parallel.
     *
     * @return ordinal number of the task
     */
    Integer getOrdering();

    /**
     * Sets the ordinal number of the task. This sets the consecutive execution
     * point relative to the other tasks in the process. May be the same as the
     * number of another task if they are to be processed in parallel. Must not
     * be {@code null}.
     *
     * @param ordering
     *            ordinal number of the task
     */
    void setOrdering(Integer ordering);

    /**
     * Returns the processing status of the task. A task can:
     * <dl>
     * <dt>LOCKED</dt>
     * <dd>wait for previous tasks to complete to become ready to start</dd>
     * <dt>OPEN</dt>
     * <dd>ready to go and waiting to be taken over</dd>
     * <dt>INWORK</dt>
     * <dd>currently being carried out</dd>
     * <dt>DONE</dt>
     * <dd>be finished</dd>
     * </dl>
     *
     * @return the processing status
     */
    TaskStatus getProcessingStatus();

    /**
     * Sets the processing status of the task.
     *
     * @param processingStatus
     *            processing status to set
     */
    void setProcessingStatus(TaskStatus processingStatus);

    /**
     * Returns the translated processing status of the task at runtime. This can
     * be translated at runtime for the user currently displaying this task. Can
     * be {@code null} if currently no value is available.
     *
     * @return translated processing status
     */
    String getProcessingStatusTitle();

    /**
     * Sets the translated processing status of the task at runtime. This is a
     * transient value that is not persisted.
     *
     * @param processingStatusTitle
     *            translated processing status to set
     */
    void setProcessingStatusTitle(String processingStatusTitle);

    /**
     * Returns the processing type of the task. Possible are:
     * <dl>
     * <dt>UNNOWKN</dt>
     * <dd>The processing type of the task is not defined.</dd>
     * <dt>MANUAL_SINGLE</dt>
     * <dd>The task was taken over and carried out by a user.</dd>
     * <dt>MANUAL_MULTI</dt>
     * <dd>The task was taken over and carried out by a user as part of the
     * batch processing functionality.</dd>
     * <dt>ADMIN</dt>
     * <dd>The task status has been changed administratively using the status
     * increase or status decrease functions.</dd>
     * <dt>AUTOMATIC</dt>
     * <dd>The automatic task was carried out by the workflow system.</dd>
     * <dt>QUEUE</dt>
     * <dd>The task status was changed via the Active MQ interface.</dd>
     * </dl>
     *
     * @return the processing type
     */
    TaskEditType getEditType();

    /**
     * Sets the processing type of the task.
     *
     * @param editType
     *            processing type to set
     */
    void setEditType(TaskEditType editType);

    /**
     * Returns the translated processing type of the task at runtime. This can
     * be translated at runtime for the user currently displaying this task. Can
     * be {@code null} if currently no value is available.
     *
     * @return translated processing status
     */
    String getEditTypeTitle();

    /**
     * Sets the translated processing type of the task at runtime. This is a
     * transient value that is not persisted.
     *
     * @param editTypeTitle
     *            translated processing type to set
     */
    void setEditTypeTitle(String editTypeTitle);

    /**
     * Returns the user who last worked on the task.
     *
     * @return the user who last worked on the task
     */
    UserInterface getProcessingUser();

    /**
     * Sets the user who last worked on the task.
     *
     * @param processingUser
     *            user to set
     */
    void setProcessingUser(UserInterface processingUser);

    /**
     * Returns the time the task status was last changed. This references
     * <i>any</i> activity on the task that involves a change in status. The
     * string is formatted according to
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the time the task status was last changed
     * @deprecated Use {@link #getProcessingTime()}.
     */
    @Deprecated
    default String getProcessingMoment() {
        Date processingTime = getProcessingTime();
        return Objects.nonNull(processingTime) ? new SimpleDateFormat(DATE_FORMAT).format(processingTime) : null;
    }

    /**
     * Returns the time the task status was last changed. This references
     * <i>any</i> activity on the task that involves a change in status.
     * {@link Date} is a specific instant in time, with millisecond precision.
     *
     * @return the time the task status was last changed
     */
    Date getProcessingTime();

    /**
     * Sets the time the task status was last changed. The string must be
     * parsable with {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param processingTime
     *            time to set
     * @throws ParseException
     *             if the time cannot be converted
     * @deprecated Use {@link #setProcessingTime(Date)}.
     */
    @Deprecated
    default void setProcessingMoment(String processingTime) throws ParseException {
        setProcessingTime(
            Objects.nonNull(processingTime) ? new SimpleDateFormat(DATE_FORMAT).parse(processingTime) : null);
    }

    /**
     * Sets the time the task status was last changed. The string must be
     * parsable with {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param processingTime
     *            time to set
     */
    void setProcessingTime(Date processingTime);

    /**
     * Returns the time when the task was accepted for processing. The string is
     * formatted according to
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the time when the task was accepted for processing
     * @deprecated Use {@link #getProcessingBegin()}.
     */
    @Deprecated
    default String getProcessingBeginTime() {
        Date processingBegin = getProcessingBegin();
        return Objects.nonNull(processingBegin) ? new SimpleDateFormat(DATE_FORMAT).format(processingBegin) : null;
    }

    /**
     * Returns the time when the task was accepted for processing.
     *
     * @return the time when the task was accepted for processing
     */
    Date getProcessingBegin();

    /**
     * Sets the time the task was accepted for processing. The string must be
     * parsable with {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param processingBegin
     *            time to set
     * @throws ParseException
     *             if the time cannot be converted
     * @deprecated Use {@link #setProcessingBegin(Date)}.
     */
    @Deprecated
    default void setProcessingBeginTime(String processingBegin) throws ParseException {
        setProcessingBegin(
            Objects.nonNull(processingBegin) ? new SimpleDateFormat(DATE_FORMAT).parse(processingBegin) : null);
    }

    /**
     * Sets the time the task was accepted for processing.
     *
     * @param processingBegin
     *            time to set
     */
    void setProcessingBegin(Date processingBegin);

    /**
     * Returns the time when the task was completed. The string is formatted
     * according to {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the time when the task was completed
     * @deprecated Use {@link #getProcessingEnd()}.
     */
    @Deprecated
    default String getProcessingEndTime() {
        Date processingEnd = getProcessingEnd();
        return Objects.nonNull(processingEnd) ? new SimpleDateFormat(DATE_FORMAT).format(processingEnd) : null;
    }

    /**
     * Returns the time when the task was completed.
     *
     * @return the time when the task was completed
     */
    Date getProcessingEnd();

    /**
     * Sets the time the task was completed. The string must be parsable with
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param processingEnd
     *            time to set
     * @throws ParseException
     *             if the time cannot be converted
     * @deprecated Use {@link #setProcessingEnd(Date)}.
     */
    @Deprecated
    default void setProcessingEndTime(String processingEnd) throws ParseException {
        setProcessingEnd(
            Objects.nonNull(processingEnd) ? new SimpleDateFormat(DATE_FORMAT).parse(processingEnd) : null);
    }

    /**
     * Sets the time the task was completed.
     *
     * @param processingEnd
     *            time to set
     */
    void setProcessingEnd(Date processingEnd);

    /**
     * Returns the process this task belongs to. Can be {@code null} if the task
     * belongs to a production template, and not a process.
     *
     * @return the process this task belongs to
     */
    ProcessInterface getProcess();

    /**
     * Sets the process this task belongs to. A task can only ever be assigned
     * to <i>either</i> a process <i>or</i> a production template.
     *
     * @param process
     *            process this task belongs to
     */
    void setProcess(ProcessInterface process);

    /**
     * Returns the project the process belongs to.
     *
     * @return the project the process belongs to
     * @deprecated Use {@link #getProcess()}{@code .getProject()}.
     */
    @Deprecated
    default ProjectInterface getProject() {
        return getProcess().getProject();
    }

    /**
     * Sets the project the process belongs to.
     *
     * @param project
     *            project to set
     * @deprecated Use
     *             {@link #getProcess()}{@code .setProject(ProjectInterface)}.
     */
    @Deprecated
    default void setProject(ProjectInterface project) {
        getProcess().setProject(project);
    }

    /**
     * Returns the production template this task belongs to. Can be {@code null}
     * if the task belongs to a process, and not a production template.
     *
     * @return the production template this task belongs to
     */
    TemplateInterface getTemplate();

    /**
     * Sets the production template this task belongs to. A task can only ever
     * be assigned to <i>either</i> a production template <i>or</i> a process.
     *
     * @param template
     *            template this task belongs to
     */
    void setTemplate(TemplateInterface template);

    /**
     * Returns a list of the IDs of the roles, whose holders are allowed to
     * perform this task. This list is not guaranteed to be in reliable order.
     *
     * @return list of the IDs of the roles
     */
    List<Integer> getRoleIds();

    /**
     * Specifies a list of role IDs whose holders are allowed to perform this
     * task. The list should not contain duplicates, and must not contain
     * {@code null}s. It is not specified whether setting the roles using IDs is
     * an action at the current time. Subsequent edits to the ID list may or may
     * not affect the stored roles.
     *
     * @param roleIds
     *            list of role IDs
     * @throws UnsupportedOperationException
     *             if setting is not supported
     */
    default void setRoleIds(List<Integer> roleIds) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Returns how many roles are allowed to take on this task.
     *
     * @return how many roles are allowed to take on this task
     */
    default int getRolesSize() {
        List<Integer> roles = getRoleIds();
        return Objects.nonNull(roles) ? roles.size() : 0;
    }

    /**
     * Sets how many roles are allowed to take on this task. The setter can be
     * used when representing data from a third-party source. Internally it
     * depends on, whether there are role objects in the database linked to the
     * process. No additional roles can be added to the process here.
     *
     * @param rolesSize
     *            how many users hold this role to set
     * @throws SecurityException
     *             when trying to assign unspecified roles to this process
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     */
    default void setRolesSize(int rolesSize) {
        int newSize = Objects.nonNull(rolesSize) ? rolesSize : 0;
        List<Integer> users = Optional.of(getRoleIds()).orElse(Collections.emptyList());
        int currentSize = users.size();
        if (newSize == currentSize) {
            return;
        }
        if (newSize > currentSize) {
            throw new SecurityException("cannot add arbitrary roles");
        }
        setRoleIds(users.subList(0, newSize));
    }

    /**
     * Returns whether the task is in a correction run. In a five-step workflow,
     * if a correction request occurs from the fourth step to the second step,
     * steps two and three are in the correction run.
     *
     * @return whether the task is in a correction run
     */
    boolean isCorrection();

    /**
     * Sets whether the task is in a correction run.
     *
     * @param correction
     *            whether the task is in a correction run
     */
    void setCorrection(boolean correction);

    /**
     * Returns whether the task is of automatic type. Automatic tasks are taken
     * over by the workflow system itself, the actions selected in it are
     * carried out, such as an export or a script call, and then the task is
     * completed.
     *
     * @return whether the task is automatic
     */
    boolean isTypeAutomatic();

    /**
     * Sets whether the task is of automatic type.
     *
     * @param typeAutomatic
     *            whether the task is automatic
     */
    void setTypeAutomatic(boolean typeAutomatic);

    /**
     * Returns whether the editor for the digital copy is made available to the
     * user. The editor offers a UI in which the structure of the previously
     * loosely digitized media can be detailed.
     *
     * @return whether the editor is available
     */
    boolean isTypeMetadata();

    /**
     * Sets whether the editor is available in the task.
     *
     * @param typeMetadata
     *            whether the editor is available
     */
    void setTypeMetadata(boolean typeMetadata);

    /**
     * Returns whether this task gives the user file system access. They can
     * then access the folder for or containing the digitized files and create,
     * view or edit files.
     *
     * @return whether the user gets file access
     */
    boolean isTypeImagesRead();

    /**
     * Sets whether this task gives the user file system access.
     *
     * @param typeImagesRead
     *            whether the user gets file access
     */
    void setTypeImagesRead(boolean typeImagesRead);

    /**
     * Returns whether the user is allowed to create or modify files. When
     * digitizing, the user probably needs write access to the file area of the
     * process, but perhaps not for control tasks.
     *
     * @return whether the user gets write access
     */
    boolean isTypeImagesWrite();

    /**
     * Sets whether the user is allowed to create or modify files.
     *
     * @param typeImagesWrite
     *            whether the user gets write access
     */
    void setTypeImagesWrite(boolean typeImagesWrite);

    /**
     * Returns whether accepting or completing this task applies to the batch.
     * If so, with a single UI interaction, the user automatically executes an
     * action for all tasks of all processes in the batch that, have the same
     * name and are in the same state. This saves users from a lot of mouse
     * work.
     *
     * @return whether actions apply to the batch
     */
    boolean isBatchStep();

    /**
     * Sets whether batch automation is possible for this task. The setter can
     * be used when representing data from a third-party source. Internally it
     * depends on whether the task's process is assigned to exactly one batch.
     * Setting this to true cannot fix problems.
     *
     * @param batchAvailable
     *            as boolean
     * @throws UnsupportedOperationException
     *             if setting is not supported
     */
    default void setBatchAvailable(boolean batchAvailable) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Returns whether batch automation is possible for this task. For
     * automation to work, the process containing the task must be assigned to
     * exactly one batch.
     *
     * @return whether batch automation is possible
     */
    default boolean isBatchAvailable() {
        var batches = getProcess().getBatches();
        return Objects.nonNull(batches) && batches.size() == 1;
    }

    /**
     * Sets whether batch automation is possible for this task. The setter can
     * be used when representing data from a third-party source. Internally it
     * depends on, whether the process containing the task is assigned to
     * exactly one batch.
     *
     * @param batchStep
     *            whether batch automation is possible
     * @throws UnsupportedOperationException
     *             when the value doesn't match the background database
     */
    void setBatchStep(boolean batchStep);

    /**
     * Returns the status of the task regarding necessary corrections. Possible
     * are:
     * <dl>
     * <dt>0</dt>
     * <dd>There are no corrections.</dd>
     * <dt>1</dt>
     * <dd>All necessary corrections have been made.</dd>
     * <dt>2</dt>
     * <dd>Corrections are pending.</dd>
     * </dl>
     * 
     * @return the status regarding corrections
     * @see org.kitodo.data.database.enums.CorrectionComments
     * @deprecated Use
     *             {@link #getProcess()}{@code .getCorrectionCommentStatus()}.
     */
    @Deprecated
    default Integer getCorrectionCommentStatus() {
        return getProcess().getCorrectionCommentStatus();
    }

    /**
     * Sets the status of the task regarding necessary corrections. Must be a
     * number from 0 to 2.
     * 
     * @param status
     *            the status regarding corrections
     * @see org.kitodo.data.database.enums.CorrectionComments
     * @throws UnsupportedOperationException
     *             if setting is not supported
     */
    default void setCorrectionCommentStatus(Integer status) {
        throw new UnsupportedOperationException("not supported");
    }
}

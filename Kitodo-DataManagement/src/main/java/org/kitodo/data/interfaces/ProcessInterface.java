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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.enums.CorrectionComments;

/**
 * An interface for processes. A process represents one production process of
 * creating a digital copy of an archival item, according to a workflow based on
 * a production template. It consists of several tasks that must be carried out
 * by humans or automatically.
 */
public interface ProcessInterface extends DataInterface {

    /**
     * Returns the process name.
     *
     * @return the process name
     */
    String getTitle();

    /**
     * Sets the process name. Since the process name is used in file paths, it
     * should only contain characters compatible with the operating file system.
     * Also, for scripting, there should be no spaces in the process name.
     *
     * @param title
     *            the process name
     */
    void setTitle(String title);

    /**
     * Returns the time the process was created. The string is formatted
     * according to {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the time the process was created
     */
    String getCreationDate();

    /**
     * Sets the time of process creation. The string must be parsable with
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param creationDate
     *            the time of process creation
     * @throws ParseException
     *             if the time cannot be converted
     */
    void setCreationDate(String creationDate);

    /**
     * Returns the docket generation statement to use when creating a docket for
     * this process.
     *
     * @return the docket generation statement
     */
    DocketInterface getDocket();

    /**
     * Sets the docket generation statement to use when creating a docket for
     * this process.
     *
     * @param docket
     *            the docket generation statement
     */
    void setDocket(DocketInterface docket);

    /**
     * Returns the business domain specification this process is using.
     *
     * @return the business domain specification
     */
    RulesetInterface getRuleset();

    /**
     * Sets the business domain specification this process is using.
     *
     * @param ruleset
     *            the business domain specification
     */
    void setRuleset(RulesetInterface ruleset);

    /**
     * Returns the task list of this process.
     *
     * @return the task list
     */
    List<? extends TaskInterface> getTasks();

    /**
     * Sets the task list of this process.
     *
     * @param tasks
     *            the task list
     */
    void setTasks(List<? extends TaskInterface> tasks);

    /**
     * Returns the project the process belongs to. Digitization processes are
     * organized in projects.
     *
     * @return the project the process belongs to
     */
    ProjectInterface getProject();

    /**
     * Specifies the project to which the process belongs.
     *
     * @param project
     *            project to which the process should belong
     */
    void setProject(ProjectInterface project);

    /**
     * Specifies the batches to which the process is assigned. A process can
     * belong to several batches, but for batch automation to work, a process
     * must be assigned to exactly one batch.
     *
     * @return the batches to which the process is assigned
     */
    List<? extends BatchInterface> getBatches();

    /**
     * Sets the list that specifies the batches to which the process is
     * associated. A process can belong to several batches, but but for batch
     * automation to work, a process must be assigned to exactly one batch. The
     * list should not contain duplicates, and must not contain {@code null}s.
     *
     * @param batches
     *            list of batches to which the process is associated
     */
    void setBatches(List<? extends BatchInterface> batches);

    /**
     * Returns the operational properties of the process. Properties are a tool
     * for third-party modules to store operational properties as key-value
     * pairs, that the application has no knowledge of. This list is not
     * guaranteed to be in reliable order.
     *
     * @return list of properties
     */
    List<? extends PropertyInterface> getProperties();

    /**
     * Sets the list of operational properties of the process. This list is not
     * guaranteed to preserve its order. It must not contain {@code null}s.
     *
     * @param properties
     *            list of properties as PropertyInterface
     */
    void setProperties(List<? extends PropertyInterface> properties);

    /**
     * Returns the user who is currently blocking the process's business data.
     * Since the business data is in a file, a user can be granted exclusive
     * access to this file, so that several users do not overwrite concurrent
     * changes by each other. Can be {@code null} if the business data is not
     * currently blocked by any user.
     *
     * @return the user blocking the process
     */
    UserInterface getBlockedUser();

    /**
     * Sets exclusive (write) access to the business data in this process for a
     * user. Or, set {@code null} to release the blockage. This is a transient
     * value that is not persisted.
     *
     * @param blockedUser
     *            user to grant write access to
     */
    void setBlockedUser(UserInterface blockedUser);

    /**
     * Returns the percentage of tasks in the process that are completed. The
     * total of tasks awaiting preconditions, startable, in progress, and
     * completed is {@code 100.0d}.
     *
     * @return percentage of tasks completed
     */
    Double getProgressClosed();

    /**
     * Sets the percentage of completed tasks. This should only be set manually
     * if this information is obtained from a third party source. Normally, the
     * percentage is determined from the statuses of the tasks in the process.
     * If you set this, it will only be used for display to the user, the status
     * of the tasks will not be changed. The value must be between {@code 0.0d}
     * and {@code 100.0d}, and the values set by
     * {@link #setProgressLocked(Double)}, {@link #setProgressOpen(Double)},
     * {@link #setProgressInProcessing(Double)} and
     * {@link #setProgressClosed(Double)} must total {@code 100.0d}. For a
     * process with no tasks, set {@code 0.0d}.
     *
     * @param progressClosed
     *            the percentage of completed tasks
     */
    void setProgressClosed(Double progressClosed);

    /**
     * Returns the percentage of tasks in the process that are currently being
     * processed. The progress total of tasks waiting for preconditions,
     * startable, in progress, and completed is {@code 100.0d}.
     *
     * @return percentage of tasks in progress
     */
    Double getProgressInProcessing();

    /**
     * Sets the percentage of tasks that are currently being processed. This
     * should only be set manually if this information is obtained from a third
     * party source. Normally, the percentage is determined from the statuses of
     * the tasks in the process. If you set this, it will only be used for
     * display to the user, the status of the tasks will not be changed. The
     * value must be between {@code 0.0d} and {@code 100.0d}, and the values set
     * by {@link #setProgressLocked(Double)}, {@link #setProgressOpen(Double)},
     * {@link #setProgressInProcessing(Double)} and
     * {@link #setProgressClosed(Double)} must total {@code 100.0d}. For a
     * process with no tasks, set {@code 0.0d}.
     *
     * @param progressInProcessing
     *            percentage of tasks currently being processed
     */
    void setProgressInProcessing(Double progressInProcessing);

    /**
     * Returns the percentage of tasks in the process, that cannot yet be
     * carried out, because previous tasks have not yet been completed. The
     * progress total of tasks waiting for preconditions, startable, in
     * progress, and completed is {@code 100.0d}.
     *
     * @return percentage of tasks waiting
     */
    Double getProgressLocked();

    /**
     * Sets the percentage of tasks, that cannot yet be carried out, because
     * previous tasks have not yet been completed. This should only be set
     * manually if this information is obtained from a third party source.
     * Normally, the percentage is determined from the statuses of the tasks in
     * the process. If you set this, it will only be used for display to the
     * user, the status of the tasks will not be changed. The value must be
     * between {@code 0.0d} and {@code 100.0d}, and the values set by
     * {@link #setProgressLocked(Double)}, {@link #setProgressOpen(Double)},
     * {@link #setProgressInProcessing(Double)} and
     * {@link #setProgressClosed(Double)} must total {@code 100.0d}. For a
     * process with no tasks, set {@code 100.0d}.
     *
     * @param progressLocked
     *            percentage of tasks waiting
     */
    void setProgressLocked(Double progressLocked);

    /**
     * Returns the contents of the wiki field as HTML. Wiki means that something
     * can be changed quickly by anyone. It is a kind of sticky note on which
     * editors can exchange information about a process.
     *
     * @return wiki field as HTML
     */
    String getWikiField();

    /**
     * Sets the content of the wiki field. Primitive HTML tags formatting may be
     * used.
     *
     * @param wikiField
     *            wiki field as HTML
     */
    void setWikiField(String wikiField);

    /**
     * Returns the percentage of the process's tasks that are now ready to be
     * processed but have not yet been started. The progress total of tasks
     * waiting for preconditions, startable, in progress, and completed is
     * {@code 100.0d}.
     *
     * @return percentage of startable tasks
     */
    Double getProgressOpen();

    /**
     * Sets the percentage of tasks, that are now ready to be processed. This
     * should only be set manually if this information is obtained from a third
     * party source. Normally, the percentage is determined from the statuses of
     * the tasks in the process. If you set this, it will only be used for
     * display to the user, the status of the tasks will not be changed. The
     * value must be between {@code 0.0d} and {@code 100.0d}, and the values set
     * by {@link #setProgressLocked(Double)}, {@link #setProgressOpen(Double)},
     * {@link #setProgressInProcessing(Double)} and
     * {@link #setProgressClosed(Double)} must total {@code 100.0d}. For a
     * process with no tasks, set {@code 0.0d}.
     *
     * @param progressOpen
     *            percentage of startable tasks
     */
    void setProgressOpen(Double progressOpen);

    /**
     * Returns a coded overview of the progress of the process. The larger the
     * number, the more advanced the process is, so it can be used to sort by
     * progress. The numeric code consists of twelve digits, each three digits
     * from 000 to 100 indicate the percentage of tasks completed, currently in
     * progress, ready to start and not yet ready, in that order. For example,
     * 000000025075 means that 25% of the tasks are ready to be started and 75%
     * of the tasks are not yet ready to be started because previous tasks have
     * not yet been processed.
     * 
     * @return overview of the processing status
     */
    String getProgressCombined();

    /**
     * Sets the coded overview of the processing status of the process. This
     * should only be set manually if this information comes from a third-party
     * source. Typically, sorting progress is determined from the progress
     * properties of the tasks in the process. The numeric code consists of
     * twelve digits, each three digits from 000 to 100 indicate the percentage
     * of tasks completed, currently in progress, ready to start and not yet
     * ready, in that order. The sum of the four groups of numbers must be 100.
     * 
     * @param progressCombined
     *            coded overview of the progress with pattern
     *            <code>([01]\d{2}){4}</code>
     */
    void setProgressCombined(String progressCombined);

    /**
     * Returns a process identifier URI. Internally, this is the record number
     * of the process in the processes table of the database, but for external
     * data it can also be another identifier that resolves to a directory in
     * the application's processes directory on the file system.
     *
     * @return the union resource identifier of the process
     * @deprecated Use {@link #getProcessBaseUri()}.
     */
    @Deprecated
    default String getProcessBase() {
        URI processBaseUri = getProcessBaseUri();
        return Objects.isNull(processBaseUri) ? null : processBaseUri.toString();
    }

    /**
     * Returns a process identifier URI. Internally, this is the record number
     * of the process in the processes table of the database, but for external
     * data it can also be another identifier that resolves to a directory in
     * the application's processes directory on the file system.
     *
     * @return the union resource identifier of the process
     */
    URI getProcessBaseUri();

    /**
     * Sets the union resource identifier of the process. This should only be
     * set manually if the data comes from a third party source, otherwise, this
     * is the process record number set by the database.
     *
     * @param processBaseUri
     *            the identification URI of the process
     * @deprecated Use {@link #setProcessBaseUri(URI)}.
     */
    @Deprecated
    default void setProcessBase(String processBaseUri) {
        setProcessBaseUri(Objects.isNull(processBaseUri) ? null : URI.create(processBaseUri));
    }

    /**
     * Sets the union resource identifier of the process. This should only be
     * set manually if the data comes from a third party source, otherwise, this
     * is the process record number set by the database.
     *
     * @param processBaseUri
     *            the identification URI of the process
     */
    void setProcessBaseUri(URI processBaseUri);

    /**
     * Returns all batches to which the process belongs. A comma-space-separated
     * list of the batch labels is returned. If not, it's a blank string.
     *
     * @return batches to which the process belongs
     */
    String getBatchID();

    /**
     * Sets all batches to which the process belongs as a comma-space-separated
     * list (for display). The setter should be used by data from a third party
     * source. Internally, this information is fetched from the database. Set to
     * "" if not.
     *
     * @param batchID
     *            human-readable information about which batches the process
     *            belongs to
     */
    void setBatchID(String batchID);

    /**
     * Returns the record number of the parent process, if any. Is {@code null}
     * if there is no parent process above.
     *
     * @return record number of the parent process
     */
    Integer getParentID();

    /**
     * Sets a parent process based on its record number. Or {@code null} to not
     * set a parent process.
     *
     * @param parentID
     *            record number of the parent process
     */
    void setParentID(Integer parentID);

    /**
     * Returns whether the process has children.
     *
     * @return whether the process has children
     */
    boolean hasChildren();

    /**
     * Sets whether the process is a parent. The setter can be used when
     * representing data from a third-party source. Internally, parenthood
     * results from a parent relationship of the process in the database.
     * Setting this to true cannot insert child processes into the database.
     *
     * @param hasChildren
     *            whether the process has children
     * @throws UnsupportedOperationException
     *             when trying to set this to true for a process without
     *             children
     */
    void setHasChildren(boolean hasChildren);

    /**
     * Returns the sort count. Sort counting is applicable to a process, if it
     * is a child process and is a counted item of a series. This allows to sort
     * the children according to their count. Can be {@code null} if there is no
     * count.
     *
     * @return the sort count
     */
    Integer getSortHelperArticles();

    /**
     * Sets the sort count.
     *
     * @param sortHelperArticles
     *            the sort count
     */
    void setSortHelperArticles(Integer sortHelperArticles);

    /**
     * Returns the number of outline elements in a process. This is a business
     * statistical characteristic.
     *
     * @return the number of outline elements in a process
     */
    Integer getSortHelperDocstructs();

    /**
     * Sets the number of outline elements in a process. Since the detailed
     * business objects are in a file, the number can be stored here when
     * saving, so that statistics on the number of outline elements can be
     * obtained with an acceptable response time.
     *
     * @param sortHelperDocstructs
     *            the number of outline elements in a process
     */
    void setSortHelperDocstructs(Integer sortHelperDocstructs);

    /**
     * Returns a coded overview of the progress of the process. The larger the
     * number, the more advanced the process is, so it can be used to sort by
     * progress. The numeric code consists of twelve digits, each three digits
     * from 000 to 100 indicate the percentage of tasks completed, currently in
     * progress, ready to start and not yet ready, in that order. For example,
     * 000000025075 means that 25% of the tasks are ready to be started and 75%
     * of the tasks are not yet ready to be started because previous tasks have
     * not yet been processed.
     * 
     * @return overview of the processing status
     */
    String getSortHelperStatus();

    /**
     * Sets the coded overview of the processing status of the process. This
     * should only be set manually if this information comes from a third-party
     * source. Typically, sorting progress is determined from the progress
     * properties of the tasks in the process. The numeric code consists of
     * twelve digits, each three digits from 000 to 100 indicate the percentage
     * of tasks completed, currently in progress, ready to start and not yet
     * ready, in that order. The sum of the four groups of numbers must be 100.
     * 
     * @param sortHelperStatus
     *            coded overview of the progress with pattern
     *            <code>([01]\d{2}){4}</code>
     */
    void setSortHelperStatus(String sortHelperStatus);

    /**
     * Returns the number of media in a process. This is a business statistical
     * characteristic.
     *
     * @return the number of media in a process
     */
    Integer getSortHelperImages();

    /**
     * Sets the number of media in a process. Since counting all media files on
     * the file system for many processes is slow, the number can be stored here
     * when saving, so that statistics on the number of media can be obtained
     * with an acceptable response time.
     *
     * @param sortHelperImages
     *            the number of media in a process
     */
    void setSortHelperImages(Integer sortHelperImages);

    /**
     * Returns the number of metadata entries in a process. This is a business
     * statistical characteristic.
     *
     * @return the number of media in a process
     */
    Integer getSortHelperMetadata();

    /**
     * Sets the number of metadata entries in a process. Since the detailed
     * business objects are in a file, the number can be stored here when
     * saving, so that statistics on the number of metadata entries can be
     * obtained with an acceptable response time.
     *
     * @param sortHelperMetadata
     *            the number of metadata entries in a process
     */
    void setSortHelperMetadata(Integer sortHelperMetadata);

    /**
     * Returns the media form of the business object at runtime. Can be
     * {@code null} if no runtime value is available.
     *
     * @return the id of the division representing the media form
     */
    String getBaseType();

    /**
     * Sets the media form of the business object in the database. This is a
     * transient value that is not persisted.
     *
     * @param baseType
     *            id of the division representing the media form
     */
    void setBaseType(String baseType);

    /**
     * Returns the amount of metadata of the process at runtime. Can be
     * {@code null} if no runtime value is available.
     *
     * @return the amount of metadata
     */
    Integer getNumberOfMetadata();

    /**
     * Sets the number of metadata entries in a process. This is a transient
     * value that is not persisted.
     *
     * @param numberOfMetadata
     *            the number of metadata entries in a process
     */
    void setNumberOfMetadata(Integer numberOfMetadata);

    /**
     * Returns the number of media in a process at runtime. Can be {@code null}
     * if no runtime value is available.
     *
     * @return the number of media in a process
     */
    Integer getNumberOfImages();

    /**
     * Sets the number of media in a process. This is a transient value that is
     * not persisted.
     *
     * @param numberOfImages
     *            the number of media in a process
     */
    void setNumberOfImages(Integer numberOfImages);

    /**
     * Returns the number of outline elements in a process. Can be {@code null}
     * if no runtime value is available.
     *
     * @return the number of outline elements in a process
     */
    Integer getNumberOfStructures();

    /**
     * Sets the number of outline elements in a process at runtime. This is a
     * transient value that is not persisted.
     *
     * @param numberOfStructures
     *            the number of outline elements in a process
     */
    void setNumberOfStructures(Integer numberOfStructures);

    /**
     * Returns the name of the last user who was involved in the process. This
     * is the user who recently has a task of the process in progress, or who
     * most recently had one in progress. The name is returned comma-separated,
     * last name first. Can be {@code null} if no user has worked on the process
     * yet.
     *
     * @return name of last user handling task
     */
    String getLastEditingUser();

    /**
     * Sets the name of the last user who was involved in the process. This
     * should only be set if the data comes from a third party; internally, it
     * is determined in the database.
     *
     * @param lastEditingUser
     *            user name, comma-separated, last name first
     */
    void setLastEditingUser(String lastEditingUser);

    /**
     * Returns the day on which a task of this process was last started.
     *
     * @return day on which a task of this process was last started
     */
    Date getProcessingBeginLastTask();

    /**
     * Sets the day on which a task of this process was last started. This
     * should only be set if the data comes from a third party; internally, it
     * is determined in the database.
     * 
     * @param processingBeginLastTask
     *            the day on which a task of this process was last started
     */
    void setProcessingBeginLastTask(Date processingBeginLastTask);

    /**
     * Returns the day on which a task from this process was last completed.
     *
     * @return day on which a task from this process was last completed
     */
    Date getProcessingEndLastTask();

    /**
     * Sets the day on which a task of this process was last completed. This
     * should only be set if the data comes from a third party; internally, it
     * is determined in the database.
     * 
     * @param processingEndLastTask
     *            the day on which a task of this process was last completed
     */
    void setProcessingEndLastTask(Date processingEndLastTask);

    /**
     * Returns the error corrections processing state of the process. The value
     * is specified as integer of {@link CorrectionComments}.
     * 
     * @return the error corrections processing state
     */
    Integer getCorrectionCommentStatus();

    /**
     * Sets the error corrections processing state of the process. The value
     * must be specified as integer of {@link CorrectionComments}.
     * 
     * @param status
     *            the error corrections processing state
     */
    void setCorrectionCommentStatus(Integer status);

    /**
     * Returns whether the process has any comments.
     *
     * @return whether the process has comments
     */
    boolean hasComments();

    /**
     * Sets whether the process has any comments. This should only be set if the
     * data comes from a third party; internally, it is determined in the
     * database.
     *
     * @param hasComments
     *            whether the process has comments
     */
    void setHasComments(boolean hasComments);
}

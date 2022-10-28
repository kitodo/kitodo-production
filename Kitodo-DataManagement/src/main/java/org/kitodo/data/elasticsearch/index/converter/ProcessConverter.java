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

package org.kitodo.data.elasticsearch.index.converter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.CorrectionComments;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TaskDAO;

/**
 * This class provides static methods that derive basic information from a process, 
 * e.g. the current task progress status.
 * 
 * <p>The derived information is needed as for indexing, such that processes can be 
 * filtered or sorted by these derived attributes.</p>
 */
public class ProcessConverter {

    private static final Logger logger = LogManager.getLogger(ProcessConverter.class);

    /**
     * Returns tasks of a process that have the "in_work" status.
     * 
     * @param process the process
     * @return the list of in processing tasks
     */
    private static List<Task> getTasksInWork(Process process) {
        return process.getTasks().stream()
                .filter(t -> TaskStatus.INWORK.equals(t.getProcessingStatus())).collect(Collectors.toList());
    }

    /**
     * Returns tasks of a process that have "done" status.
     * 
     * @param process the process
     * @return the list of done tasks
     */
    private static List<Task> getCompletedTasks(Process process) {
        return process.getTasks().stream()
                .filter(t -> TaskStatus.DONE.equals(t.getProcessingStatus())).collect(Collectors.toList());
    }
    
    /**
     * Returns the last task that is currently or was recently worked on.
     * @param process the process
     * @return the last processed task (either done or in work)
     */
    private static Task getLastProcessedTask(Process process) {
        List<Task> tasks = getTasksInWork(process);
        if (tasks.isEmpty()) {
            tasks = getCompletedTasks(process);
        }
        tasks = tasks.stream()
            .filter(t -> Objects.nonNull(t.getProcessingUser()) && Objects.nonNull(t.getProcessingBegin()))
            .collect(Collectors.toList());
        if (tasks.isEmpty()) {
            return null;
        } else {
            tasks.sort(Comparator.comparing(Task::getProcessingBegin));
            return tasks.get(0);
        }
    }

    /**
     * Find and adds all tasks of children processes to the list of tasks.
     * 
     * @param process the process
     * @param tasks the task list to be populated with tasks
     */
    private static void findTasksOfChildProcessesRecursive(Process process, List<Task> tasks) {
        tasks.addAll(process.getTasks());
        List<Process> children = process.getChildren();
        for (Process child : children) {
            findTasksOfChildProcessesRecursive(child, tasks);
        }
    }

    /**
     * Returns a list of tasks that are the basis of calculating the status of the process.
     * 
     * <p>For processes that have children, their respective tasks are also included.</p>
     * 
     * @param process the process
     * @param considerChildren whether to include tasks of children processes
     * @return the list of tasks of the process (and potentially its children)
     */
    private static List<Task> getListOfTasksForProgressCalculation(Process process, boolean considerChildren) {
        // consider the tasks of the process for progress calculation
        List<Task> tasks = new ArrayList<>(process.getTasks());

        // if the process has children, also consider these tasks for progress calculation
        if (considerChildren) {
            List<Process> children = process.getChildren();
            for (Process child : children) {
                findTasksOfChildProcessesRecursive(child, tasks);
            }
        }
        return tasks;
    }

    /**
     * Counts how many tasks have a certain status.
     * 
     * @param tasks the list of tasks
     * @return a map providing the count for each task status (done, open, locked, inwork)
     */
    private static Map<TaskStatus, Integer> countTasksStatusOfProcessViaBeans(List<Task> tasks) {
        Map<TaskStatus, Integer> results = new HashMap<>();
        int open = 0;
        int inProcessing = 0;
        int closed = 0;
        int locked = 0;

        for (Task task : tasks) {
            if (task.getProcessingStatus().equals(TaskStatus.DONE)) {
                closed++;
            } else if (task.getProcessingStatus().equals(TaskStatus.OPEN)) {
                open++;
            } else if (task.getProcessingStatus().equals(TaskStatus.LOCKED)) {
                locked++;
            } else {
                inProcessing++;
            }
        }

        results.put(TaskStatus.DONE, closed);
        results.put(TaskStatus.INWORK, inProcessing);
        results.put(TaskStatus.OPEN, open);
        results.put(TaskStatus.LOCKED, locked);

        return results;
    }

    private static Map<TaskStatus, Integer> countTaskStatusOfProcess(Process process, boolean considerChildren) {
        if (!considerChildren) {
            // if children are of no concern, just use basic counting via beans
            return countTasksStatusOfProcessViaBeans(getListOfTasksForProgressCalculation(process, considerChildren));
        }
        try {
            // use custom SQL query to count task status including tasks of ancestor processes for performance
            return new TaskDAO().countTaskStatusForProcessAndItsAncestors(process);
        } catch (DAOException e) {
            logger.warn("error counting task status via custom SQL query, continue with slow calculation", e);
            return countTasksStatusOfProcessViaBeans(getListOfTasksForProgressCalculation(process, considerChildren));
        }
    }

    /**
     * Return the user name of the user that handled the last task of the given process (either the newest task 
     * INWORK or the newest DONE task, if no task is INWORK). Return null if no task is INWORK or DONE.
     *
     * @param process the process
     * @return name of processing user
     */
    public static String getLastEditingUser(Process process) {
        Task lastTask = getLastProcessedTask(process);
        if (Objects.isNull(lastTask)) {
            return null;
        } else {
            return lastTask.getProcessingUser().getFullName();
        }
    }

    /**
     * Return processing begin of last processed task of given process.
     *
     * @param process the process
     * @return processing begin of last processed task
     */
    public static Date getLastProcessingBegin(Process process) {
        Task lastTask = getLastProcessedTask(process);
        if (Objects.isNull(lastTask)) {
            return null;
        } else {
            return lastTask.getProcessingBegin();
        }
    }

    /**
     * Return processing end of last processed task of given process.
     *
     * @param process the process
     * @return processing end of last processed task
     */
    public static Date getLastProcessingEnd(Process process) {
        Task lastTask = getLastProcessedTask(process);
        if (Objects.isNull(lastTask) || TaskStatus.INWORK.equals(lastTask.getProcessingStatus())) {
            return null;
        } else {
            return lastTask.getProcessingEnd();
        }
    }

    /**
     * Return whether there is a correction comment and whether it has been corrected as status.
     * 
     * @param process the process being checked for its correction comment status
     * @return an enum representing the status
     */
    public static CorrectionComments getCorrectionCommentStatus(Process process) {
        if (Objects.isNull(process)) {
            return CorrectionComments.NO_CORRECTION_COMMENTS;
        }
        List<Comment> correctionComments = process.getComments()
                .stream().filter(c -> CommentType.ERROR.equals(c.getType())).collect(Collectors.toList());
        if (correctionComments.size() < 1) {
            return CorrectionComments.NO_CORRECTION_COMMENTS;
        } else if (correctionComments.stream().anyMatch(c -> !c.isCorrected())) {
            return CorrectionComments.OPEN_CORRECTION_COMMENTS;
        } else {
            return CorrectionComments.NO_OPEN_CORRECTION_COMMENTS;
        }
    }

    /**
     * Returns the percentaged task progress of a process as a map of doubles.
     * 
     * @param process the process
     * @param considerChildren whether to also count tasks of children processes
     * @return a map providing the percentage of tasks having a certain status (done, open, locked, inwork)
     */
    public static Map<TaskStatus, Double> getTaskProgressPercentageOfProcess(Process process, boolean considerChildren) {
        Map<TaskStatus, Integer> counts = countTaskStatusOfProcess(process, considerChildren);
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        
        // report processes without any tasks as if they had a single locked task
        if (total == 0) {
            counts.put(TaskStatus.LOCKED, 1);
            total = 1;
        }

        Map<TaskStatus, Double> percentages = new HashMap<>();
        percentages.put(TaskStatus.DONE, 100.0 * (double)counts.get(TaskStatus.DONE) / (double) total);
        percentages.put(TaskStatus.INWORK, 100.0 * (double)counts.get(TaskStatus.INWORK) / (double) total);
        percentages.put(TaskStatus.OPEN, 100.0 * (double)counts.get(TaskStatus.OPEN) / (double) total);
        percentages.put(TaskStatus.LOCKED, 100.0 * (double)counts.get(TaskStatus.LOCKED) / (double) total);
        return percentages;
    }

    /**
     * Return a string representing the combined progress of a process. 
     * 
     * <p>It consists of 3-digit percentage numbers (e.g. 000, 025, 100) 
     * for each task status (DONE, INWORK, OPEN, LOCKED). For example, the status
     * "000000025075" means that 25% of tasks are open, and 75% of tasks are locked.</p>
     *
     * @param percentages the task percentages as calculated with getTaskProgressPercentageOfProcess
     * @return string the string representing the combined progress of the process
     */
    public static String getCombinedProgressFromTaskPercentages(Map<TaskStatus, Double> percentages) {
        DecimalFormat decimalFormat = new DecimalFormat("#000");
        return decimalFormat.format(percentages.get(TaskStatus.DONE)) 
            + decimalFormat.format(percentages.get(TaskStatus.INWORK)) 
            + decimalFormat.format(percentages.get(TaskStatus.OPEN))
            + decimalFormat.format(percentages.get(TaskStatus.LOCKED));
    }

    /**
     * Return a string representing the combined progress of a process. 
     * 
     * <p>See `ProcessConverter.getCombinedProgressFromTaskPercentages`</p>
     *
     * @param process the process
     * @param considerChildren whether to also count tasks of children processes
     * @return string the string representing the combined progress of the process
     */
    public static String getCombinedProgressAsString(Process process, boolean considerChildren) {
        return getCombinedProgressFromTaskPercentages(
            getTaskProgressPercentageOfProcess(process, considerChildren)
        );
    }

}

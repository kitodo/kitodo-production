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


package org.kitodo.production.forms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
@RequestScoped
public class CommentTooltipView {

    private final Map<Process, List<Comment>> comments;

    /**
     * Default constructor.
     */
    public CommentTooltipView() {
        comments = new HashMap<>();
    }

    /**
     * Get comments of given process.
     *
     * @param process process as Process
     * @return List of Comment objects
     */
    public List<Comment> getComments(Process process) {
        if (comments.containsKey(process)) {
            return comments.get(process);
        }
        try {
            comments.put(process, ServiceManager.getProcessService().getComments(process));
            return comments.get(process);
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return Collections.emptyList();
        }
    }

    /**
     * Get comments of process containing the given task.
     *
     * @param task task as Task
     * @return List of Comment objects
     */
    public List<Comment> getComments(Task task) {
        return getComments(task.getProcess());
    }

    /**
     * Get the most recent comment of the given process.
     *
     * @param process process as Process
     * @return message of the comment
     */
    public String getLastComment(Process process) {
        List<Comment> comments = getComments(process);
        if (comments.isEmpty()) {
            return "";
        }
        return comments.get(comments.size() - 1).getMessage();
    }

    /**
     * Get the most recent comment of process containing the given task.
     *
     * @param task task as Task
     * @return message of the comment
     */
    public String getLastComment(Task task) {
        return getLastComment(task.getProcess());
    }
}

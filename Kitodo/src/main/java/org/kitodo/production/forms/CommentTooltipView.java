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

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
@RequestScoped
public class CommentTooltipView {

    private final Map<ProcessInterface, List<Comment>> comments;

    /**
     * Default constructor.
     */
    public CommentTooltipView() {
        comments = new HashMap<>();
    }

    /**
     * Get comments of given process.
     *
     * @param processInterface process as ProcessInterface
     * @return List of Comment objects
     */
    public List<Comment> getComments(ProcessInterface processInterface) {
        if (comments.containsKey(processInterface)) {
            return comments.get(processInterface);
        }
        try {
            comments.put(processInterface, ServiceManager.getProcessService().getComments(processInterface));
            return comments.get(processInterface);
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return Collections.emptyList();
        }
    }

    /**
     * Get comments of process containing the given task.
     *
     * @param taskInterface task as TaskInterface
     * @return List of Comment objects
     */
    public List<Comment> getComments(TaskInterface taskInterface) {
        return getComments(taskInterface.getProcess());
    }
}

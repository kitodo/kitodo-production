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
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
@RequestScoped
public class CommentTooltipView {

    private final Map<ProcessDTO, List<Comment>> comments;

    /**
     * Default constructor.
     */
    public CommentTooltipView() {
        comments = new HashMap<>();
    }

    /**
     * Get comments of given process.
     *
     * @param processDTO process as ProcessDTO
     * @return List of Comment objects
     */
    public List<Comment> getComments(ProcessDTO processDTO) {
        if (comments.containsKey(processDTO)) {
            return comments.get(processDTO);
        }
        try {
            comments.put(processDTO, ServiceManager.getProcessService().getComments(processDTO));
            return comments.get(processDTO);
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return Collections.emptyList();
        }
    }

    /**
     * Get comments of process containing the given task.
     *
     * @param taskDTO task as TaskDTO
     * @return List of Comment objects
     */
    public List<Comment> getComments(TaskDTO taskDTO) {
        return getComments(taskDTO.getProcess());
    }

    /**
     * Get the most recent comment of the given process.
     *
     * @param processDTO process as ProcessDTO
     * @return message of the comment
     */
    public String getLastComment(ProcessDTO processDTO) {
        List<Comment> comments = getComments(processDTO);
        if (comments.isEmpty()) {
            return "";
        }
        return comments.get(0).getMessage();
    }

    /**
     * Get the most recent comment of process containing the given task.
     *
     * @param taskDTO task as TaskDTO
     * @return message of the comment
     */
    public String getLastComment(TaskDTO taskDTO) {
        return getLastComment(taskDTO.getProcess());
    }
}

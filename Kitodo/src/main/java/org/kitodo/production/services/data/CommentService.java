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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.CommentDAO;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class CommentService extends BaseBeanService<Comment, CommentDAO> {

    private static volatile CommentService instance = null;

    /**
     * Constructor.
     */
    private CommentService() {
        super(new CommentDAO());
    }

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static CommentService getInstance() {
        CommentService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (CommentService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new CommentService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM Comment");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return count();
    }

    public List<Comment> getAllCommentsByProcess(Process process) {
        return dao.getAllByProcess(process);
    }

    /**
     * Get all comments by task ordered by id ascending.
     *
     * @param task
     *         The current task to get the comments for
     * @return List of comments
     */
    public List<Comment> getAllCommentsByTask(Task task) {
        return dao.getAllByTask(task);
    }

    /**
     * Save list of comments to database.
     *
     * @param list of comments
     */
    public void saveList(List<Comment> list) throws DAOException {
        dao.saveList(list);
    }
    
    /**
     * Remove comment from database and resolve associations.
     * 
     * @param comment to be removed.
     */
    public void removeComment(Comment comment) throws DAOException {
        Process process = comment.getProcess();
        if (Objects.nonNull(process)) {
            // Force reload of the process to ensure consistency
            process = ServiceManager.getProcessService().getById(comment.getProcess().getId());
            process.getComments().remove(comment);
            ServiceManager.getProcessService().save(process);
        }
    }
}

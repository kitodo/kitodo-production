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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.logintask.LoginTaskStatus;
import org.kitodo.api.logintask.LoginTaskType;
import org.kitodo.data.database.beans.LoginTask;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LoginTaskDAO;
import org.kitodo.production.helper.Helper;

public class LoginTaskService extends BaseBeanService<LoginTask, LoginTaskDAO> {

    private static List<LoginTaskType> PRIORITY_SEQUENCE = List.of(LoginTaskType.SAVE_USER_TO_LDAP);

    private static final Logger logger = LogManager.getLogger(UserService.class);
    
    /**
     * Constructor.
     */
    public LoginTaskService() {
        super(new LoginTaskDAO());
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM LoginTask");
    }

    /**
     * Add a new login task of a specific type for a user.
     *  
     * @param user the user
     * @param type the login task type
     */
    public void addLoginTask(User user, LoginTaskType type) {
        try {
            LoginTask loginTask = new LoginTask();
            loginTask.setUser(user);
            loginTask.setStatus(LoginTaskStatus.PENDING);
            loginTask.setType(type);
            loginTask.setCreatedAt(new Date());
            save(loginTask);
        } catch (DAOException e) {
            Helper.setErrorMessage("could not add login task", logger, e);
        }
    }

    /**
     * Cancels a login task by marking it as failed with simple cancel error message.
     * 
     * @param loginTask the login task to be canceled
     */
    public void cancelLoginTask(LoginTask loginTask) {
        try {
            loginTask.setStatus(LoginTaskStatus.FAILED);
            loginTask.setError(Helper.getTranslation("loginTaskCanceled"));
            save(loginTask);
        } catch (DAOException e) {
            Helper.setErrorMessage("could not cancel login task", logger, e);
        }
    }

    /**
     * Return login task if a user has a pending login task for a given type.
     * 
     * @param user the user 
     * @param type the task type
     * @return optional containing the login task if this user has a pending task of the given type
     */
    public Optional<LoginTask> getPendingLoginTaskForUserAndType(User user, LoginTaskType type) {
        return dao.getByUserAndStatusAndType(user, LoginTaskStatus.PENDING, type).stream().findFirst();
    }

    /**
     * Return last login task of a user for a given type.
     * 
     * @param user the user 
     * @param type the task type
     * @return optional containing the last login task if this user has a task of the given type
     */
    public Optional<LoginTask> getLastLoginTaskForUserAndType(User user, LoginTaskType type) {
        return dao.getByUserAndType(user, type).stream().findFirst();
    }

    /**
     * Return the highest priority login task for a user that is pending.
     * 
     * @param user the user
     * @return optional containing highest priority login task if this user has a pending task
     */
    public Optional<LoginTask> getNextPendingLoginTaskForUser(User user) {
        List<LoginTask> tasks = dao.getByUserAndStatus(user, LoginTaskStatus.PENDING);

        // return pending task in given priority
        for (LoginTaskType type : PRIORITY_SEQUENCE) {
            Optional<LoginTask> task = tasks.stream().filter(t -> t.getType().equals(type)).findFirst();
            if (task.isPresent()) {
                return task;
            };
        }

        return Optional.empty();
    }

    /**
     * Mark login task as successfully completed.
     * 
     * @param loginTask the login task
     */
    public void finishTaskAsSuccessfullyCompleted(LoginTask loginTask) {
        try {
            loginTask.setExecutedAt(new Date());
            loginTask.setStatus(LoginTaskStatus.COMPLETED);
            save(loginTask);
        } catch (DAOException e) {
            Helper.setErrorMessage("login task could not be saved as successfully completed", logger, e);
        }
    }

    /**
     * Mark login task as failed.
     * 
     * @param loginTask the login task
     * @param error optional error message
     */
    public void finishTaskWithError(LoginTask loginTask, String error) {
        try {
            loginTask.setExecutedAt(new Date());
            loginTask.setStatus(LoginTaskStatus.FAILED);
            loginTask.setError(error);
            save(loginTask);
        } catch (DAOException e) {
            Helper.setErrorMessage("login task could not be saved as failed", logger, e);
        }
    }

}

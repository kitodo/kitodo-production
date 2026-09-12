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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.logintask.LoginTaskStatus;
import org.kitodo.api.logintask.LoginTaskType;
import org.kitodo.data.database.beans.LoginTask;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LoginTaskDAO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.Authentication;

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
            logger.error("Failed to add new login task", e);
            Helper.setErrorMessage("loginTaskAddError");
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
            logger.error("Failed to cancel login task", e);
            Helper.setErrorMessage("loginTaskCancelError");
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
            logger.error("Failed to mark login task as successfully completed", e);
            Helper.setErrorMessage("loginTaskFinishError");
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
            logger.error("Failed to mark login task as completed with error", e);
            Helper.setErrorMessage("loginTaskFailedError");
        }
    }

    /**
     * Retrieves the highest priority pending login task for a user and execute it.
     * 
     * @param user the user
     * @param authentication authentication object which was created during the authentication process
     */
    public void doLoginTasks(User user, Authentication authentication) {
        Optional<LoginTask> pendingLoginTask = getNextPendingLoginTaskForUser(user);

        if (pendingLoginTask.isEmpty()) {
            return;
        }

        if (LoginTaskType.SAVE_USER_TO_LDAP.equals(pendingLoginTask.get().getType())) {
            try {
                LdapServerService ldapServerService = ServiceManager.getLdapServerService();
                ldapServerService.createNewUser(user, String.valueOf(authentication.getCredentials()));
                finishTaskAsSuccessfullyCompleted(pendingLoginTask.get());
            } catch (NameAlreadyBoundException e) {
                Helper.setErrorMessage("Ldap entry already exists", logger, e);
                finishTaskWithError(pendingLoginTask.get(), "Ldap entry already exists");
            } catch (NoSuchAlgorithmException | NamingException | IOException | RuntimeException e) {
                Helper.setErrorMessage("Could not generate ldap entry", logger, e);
                finishTaskWithError(pendingLoginTask.get(), "Could not generate ldap entry: " + e.getMessage());
            }
        }

        // in the future, add more login tasks, e.g. redirect user to reset user password, setup 2fa
    }

}

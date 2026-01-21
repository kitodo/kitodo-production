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

package org.kitodo.production.forms.user;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.filters.FilterMenu;
import org.kitodo.production.forms.BaseListView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyUserModel;
import org.kitodo.production.security.SecuritySession;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("UserListView")
@ViewScoped
public class UserListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "&tab=usersTab";
    
    private static final Logger logger = LogManager.getLogger(UserListView.class);

    private static final UserService userService = ServiceManager.getUserService();
    private final transient FilterMenu filterMenu = new FilterMenu(this);

    private User confirmResetTasksDialogUser;

    /**
     * Initialize UserListView.
     */
    @PostConstruct
    public void init() {
        setLazyBeanModel(new LazyUserModel(userService));

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("user"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("user");

        sortBy = SortMeta.builder().field("surname").order(SortOrder.ASCENDING).build();
    }

    /**
     * Navigate to create a new user.
     *
     * @return the user edit view path
     */
    public String newUser() {
        return UserEditView.VIEW_PATH;
    }

    /**
     * Retrieve and return the list of tasks that are assigned to the user and
     * that are "INWORK" and belong to process, not template.
     *
     * @return list of tasks that are currently assigned to the user and that
     *         are "INWORK" and belong to process, not template
     */
    public static List<Task> getTasksInProgress(User user) {
        return ServiceManager.getTaskService().getTasksInProgress(user);
    }

    /**
     * Unassign all tasks in work from user and set their status back to open.
     *
     * @param userObject user
     */
    public static void resetTasksToOpen(User userObject) {
        List<Task> tasksInProgress = getTasksInProgress(userObject);
        for (Task taskInProgress : tasksInProgress) {
            ServiceManager.getTaskService().replaceProcessingUser(taskInProgress, null);
            taskInProgress.setProcessingStatus(TaskStatus.OPEN);
            try {
                ServiceManager.getTaskService().save(taskInProgress);
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.TASK.getTranslationSingular()}, logger, e);
            }
        }
    }

    /**
     * View action to delete a user if he does not have tasks in progress.
     *
     * <p>
     * Please note that deleting a user in Production will not delete the
     * user from a connected LDAP service.
     */
    public void delete(User userObject) {
        if (hasTasksInProgress(userObject)) {
            confirmResetTasksDialogUser = userObject;
            PrimeFaces.current().ajax().update("usersTabView:confirmResetTasksDialog");
            PrimeFaces.current().executeScript("PF('confirmResetTasksDialog').show();");
        } else {
            deleteUser(userObject);
        }
    }

    /**
     * Return user object that was selected to reset tasks.
     * 
     * @return the user object that was selected to reset tasks
     */
    public User getConfirmResetTasksDialogUser() {
        return confirmResetTasksDialogUser;
    }

    /**
     * Unassign all tasks in work from user and set their status back to open and delete the user.
     */
    public static void resetTasksAndDeleteUser(User userObject) {
        resetTasksToOpen(userObject);
        deleteUser(userObject);
    }

    /**
     * Check and return whether given User 'user' is logged in.
     *
     * @param userObject
     *            User to check
     * @return whether given User is checked in
     */
    public static boolean checkUserLoggedIn(User userObject) {
        for (SecuritySession securitySession : ServiceManager.getSessionService().getActiveSessions()) {
            if (securitySession.getUserName().equals(userObject.getLogin())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the filter of the UserListView and reloads it.
     *
     * @param filter
     *            the filter to apply.
     * @return reload path of the page.
     */
    public String changeFilter(String filter) {
        filterMenu.parseFilters(filter);
        setFilter(filter);
        return VIEW_PATH + "&" + getCombinedListOptions();
    }

    /**
     * Sets the user list filter to the user specified filter value.
     * 
     * @param filter the filter value specified by the user
     */
    @Override
    public void setFilter(String filter) {
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
        String script = "kitodo.updateQueryParameter('filter', '" + filter.replace("&", "%26") +  "');";
        PrimeFaces.current().executeScript(script);
    }

    /**
     * Set filter based on the URL query parameter "filter", which can be any string.
     * 
     * @param encodedFilter the filter as URL query parameter to be set as new filter
     */
    public void setFilterFromTemplate(String encodedFilter) {
        if (Objects.nonNull(encodedFilter) && !encodedFilter.isEmpty()) {
            String decodedFilter = encodedFilter.replace("%26", "&");
            this.filterMenu.parseFilters(decodedFilter);
            this.setFilter(decodedFilter);
        }
    }

    /**
     * Get filterMenu.
     *
     * @return value of filterMenu
     */
    public FilterMenu getFilterMenu() {
        return filterMenu;
    }

    /**
     * Returns a comma-separated list of role titles for the given user.
     *
     * @param user the user whose roles are returned
     */
    public String getRoleTitles(User user) {
        List<String> roles = getLazyUserModel().getRolesCache().get(user.getId());
        return (Objects.isNull(roles) || roles.isEmpty()) ? "" : String.join(", ", roles);
    }

    /**
     * Returns a comma-separated list of project titles for the given user.
     *
     * @param user the user whose projects are returned
     */
    public String getProjectTitles(User user) {
        List<String> projects = getLazyUserModel().getProjectsCache().get(user.getId());
        return (Objects.isNull(projects) || projects.isEmpty()) ? "" : String.join(", ", projects);
    }

    /**
     * Returns a comma-separated list of client names for the given user.
     *
     * @param user the user whose clients are returned
     */
    public String getClientNames(User user) {
        List<String> clients = getLazyUserModel().getClientsCache().get(user.getId());
        return (Objects.isNull(clients) || clients.isEmpty()) ? "" : String.join(", ", clients);
    }

    /**
     * Indicates whether the given user has at least one task currently in progress.
     *
     * @param user the user to check
     */
    public boolean hasTasksInProgress(User user) {
        return Boolean.TRUE.equals(getLazyUserModel().getTasksCache().get(user.getId()));
    }

    /**
     * Declare the allowed sort fields for sanitizing the query parameter "sortField".
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("surname", "location");
    }

    /**
     * Delete a user from the database.
     * 
     * @param userObject the user object to be deleted
     */
    private static void deleteUser(User userObject) {
        try {
            userService.remove(userObject);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.USER.getTranslationSingular()}, logger, e);
        }
    }


    private LazyUserModel getLazyUserModel() {
        return (LazyUserModel) lazyBeanModel;
    }

}

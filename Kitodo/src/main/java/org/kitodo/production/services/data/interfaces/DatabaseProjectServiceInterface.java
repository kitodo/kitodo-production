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

package org.kitodo.production.services.data.interfaces;

import static org.kitodo.constants.StringConstants.COMMA_DELIMITER;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;

/**
 * Specifies the special database-related functions of the projects service.
 */
public interface DatabaseProjectServiceInterface extends SearchDatabaseServiceInterface<Project> {

    /**
     * Returns all projects that can still be assigned to a user. Returns the
     * projects that are not already assigned to the user to be edited, and that
     * belong to the client for which the logged-in user is currently working.
     * These are displayed in the addProjectsPopup.
     * 
     * <p>
     * {P} := currentUser.currentClient.projects - userAccount.projects
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @param user
     *            user being edited
     * @return projects that can be assigned
     */
    List<? extends ProjectInterface> findAllAvailableForAssignToUser(User user) throws DataException;

    /**
     * Returns all projects the user is assigned to for the current client. This
     * returns all projects, that the user, identified by the current session,
     * is assigned to, and that belong to the client, that they are currently
     * working for.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @return all projects the user is assigned to for the current client
     * @throws DataException
     *             on errors
     */
    List<? extends ProjectInterface> findAllProjectsForCurrentUser() throws DataException;

    /**
     * Returns all projects of the client, for which the logged in user is
     * currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @return all projects for the selected client
     */
    List<Project> getAllForSelectedClient();

    /**
     * Returns the names of the projects the user is allowed to see. If the user
     * has the {@code AuthorityToViewProjectList} and
     * {@link AuthorityToViewClientList} permissions, the list is returned
     * unfiltered. Otherwise it will be limited to those projects that belong to
     * the client for which the user is currently working and to which the user
     * is assigned.
     * 
     * @param projects
     *            projects to filter if necessary
     * @return Returns a string with the names, separated by ", "
     */
    default String getProjectTitles(List<Project> projects) throws DataException {
        final SecurityAccessService permissions = ServiceManager.getSecurityAccessService();

        Stream<Project> projectsStream = projects.stream();
        if (permissions.hasAuthorityToViewProjectList() && permissions.hasAuthorityToViewClientList()) {
            List<Integer> permitted = findAllProjectsForCurrentUser().stream().map(ProjectInterface::getId)
                    .collect(Collectors.toList());
            projectsStream = projectsStream.filter(project -> permitted.contains(project.getId()));
        }
        return projectsStream.map(Project::getTitle).collect(Collectors.joining(COMMA_DELIMITER));
    }

    /**
     * Returns all projects of the specified client and name.
     *
     * @param title
     *            naming of the projects
     * @param clientId
     *            record number of the client whose projects are queried
     * @return all projects of the specified client and name
     */
    List<Project> getProjectsWithTitleAndClient(String title, Integer clientId);

    // === alternative functions that are no longer required ===

    /**
     * Returns all projects from the database.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This method actually returns all objects of all clients and is therefore
     * more suitable for operational purposes, rather not for display purposes.
     *
     * @return all objects of the implementing type
     * @deprecated Use {@link #getAll()}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    default List<ProjectInterface> findAll() throws DataException {
        try {
            return (List<ProjectInterface>) (List<?>) getAll();
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }
}

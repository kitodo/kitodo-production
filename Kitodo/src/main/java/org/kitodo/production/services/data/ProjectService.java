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

import static org.kitodo.constants.StringConstants.COMMA_DELIMITER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class ProjectService extends BaseBeanService<Project, ProjectDAO> {

    private static final Map<String, String> SORT_FIELD_MAPPING;

    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title", "title");
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("metsRightsOwner.keyword", "metsRightsOwner");
        SORT_FIELD_MAPPING.put("active", "active");
    }

    private static volatile ProjectService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ProjectService() {
        super(new ProjectDAO());
    }

    /**
     * Return singleton variable of type ProjectService.
     *
     * @return unique instance of ProcessService
     */
    public static ProjectService getInstance() {
        ProjectService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ProjectService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ProjectService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM Project");
    }

    @Override
    public Long countResults(Map<?, String> filters) throws DAOException {
        BeanQuery query = getProjectsQuery();
        return count(query.formCountQuery(), query.getQueryParameters());
    }

    /**
     * Returns all projects of the client, for which the logged-in user is
     * currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @return all projects for the selected client
     */
    public List<Project> getAllForSelectedClient() {
        return dao.getByQuery(
                "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId ORDER BY title",
                Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<Project> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DAOException {

        BeanQuery query = getProjectsQuery();
        query.defineSorting(SORT_FIELD_MAPPING.get(sortField), sortOrder);
        return getByQuery(query.formQueryForAll(), query.getQueryParameters(), first, pageSize);
    }

    private static BeanQuery getProjectsQuery() {
        BeanQuery projectQuery = new BeanQuery(Project.class);
        projectQuery.addXIdRestriction("users", ServiceManager.getUserService().getCurrentUser().getId());
        projectQuery.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        return projectQuery;
    }

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
    public List<Project> findAllAvailableForAssignToUser(User user) throws DAOException {
        Map<String, Object> parameters = new HashMap<>(7);
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        if (user.getProjects().isEmpty()) {
            return getByQuery("FROM Project WHERE client_id = :sessionClientId", parameters);
        } else {
            String assignedProjects = user.getProjects().stream().map(Project::getId).map(Objects::toString)
                    .collect(Collectors.joining(COMMA_DELIMITER, "(", ")"));
            parameters.put("assignedProjects", assignedProjects);
            return getByQuery("FROM Project WHERE client_id = :sessionClientId AND id NOT IN :assignedProjects",
                parameters);
        }
    }

    private List<Project> findAvailableForAssignToUser(User user) throws DataException {
        // TODO delete method stub
        throw new UnsupportedOperationException("no longer used function");
    }

    /**
     * Checks if a project can be actually used.
     *
     * @param project
     *            The project to check
     * @return true, if project is complete and can be used, false, if project is
     *         incomplete
     */
    public boolean isProjectComplete(Project project) {
        boolean projectsXmlExists = KitodoConfigFile.PROJECT_CONFIGURATION.exists();

        return Objects.nonNull(project.getTitle()) && projectsXmlExists;
    }

    /**
     * Creates a deep copy of a project, but without the title.
     *
     * @param baseProject
     *            project to duplicate
     *
     * @return the duplicated project
     */
    public Project duplicateProject(Project baseProject) {
        Project duplicatedProject = new Project();

        duplicatedProject.setTitle(baseProject.getTitle() + "_" + Helper.generateRandomString(3));
        duplicatedProject.setClient(baseProject.getClient());
        duplicatedProject.setStartDate(baseProject.getStartDate());
        duplicatedProject.setEndDate(baseProject.getEndDate());
        duplicatedProject.setNumberOfPages(baseProject.getNumberOfPages());
        duplicatedProject.setNumberOfVolumes(baseProject.getNumberOfVolumes());
        duplicatedProject.setDmsImportRootPath(baseProject.getDmsImportRootPath());
        duplicatedProject.setMetsRightsOwner(baseProject.getMetsRightsOwner());
        duplicatedProject.setMetsRightsOwnerLogo(baseProject.getMetsRightsOwnerLogo());
        duplicatedProject.setMetsRightsOwnerSite(baseProject.getMetsRightsOwnerSite());
        duplicatedProject.setMetsRightsOwnerMail(baseProject.getMetsRightsOwnerMail());
        duplicatedProject.setMetsDigiprovPresentation(baseProject.getMetsDigiprovPresentation());
        duplicatedProject.setMetsDigiprovReference(baseProject.getMetsDigiprovReference());
        duplicatedProject.setMetsPointerPath(baseProject.getMetsPointerPath());
        duplicatedProject.setMetsPurl(baseProject.getMetsPurl());
        duplicatedProject.setMetsContentIDs(baseProject.getMetsContentIDs());

        FolderService folderService = ServiceManager.getFolderService();
        List<Folder> duplicatedFolders = new ArrayList<>();
        Folder generatorSource = null;
        Folder mediaView = null;
        Folder preview = null;

        for (Folder folder : baseProject.getFolders()) {
            Folder duplicatedFolder = folderService.cloneFolder(folder);
            duplicatedFolder.setProject(duplicatedProject);
            duplicatedFolders.add(duplicatedFolder);

            if (folder.equals(baseProject.getGeneratorSource())) {
                generatorSource = duplicatedFolder;
            }
            if (folder.equals(baseProject.getMediaView())) {
                mediaView = duplicatedFolder;
            }
            if (folder.equals(baseProject.getPreview())) {
                preview = duplicatedFolder;
            }
        }
        duplicatedProject.setFolders(duplicatedFolders);
        duplicatedProject.setGeneratorSource(generatorSource);
        duplicatedProject.setMediaView(mediaView);
        duplicatedProject.setPreview(preview);

        return duplicatedProject;
    }

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
     * @throws DAOException
     *             on errors
     */
    public List<Project> findAllProjectsForCurrentUser() throws DAOException {
        return ServiceManager.getUserService().getCurrentUser().getProjects();
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
    public List<Project> getProjectsWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId WHERE p.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }

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
    public String getProjectTitles(List<Project> projects) throws DAOException {
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewProjectList()
                && ServiceManager.getSecurityAccessService().hasAuthorityToViewClientList()) {
            return projects.stream().map(Project::getTitle).collect(Collectors.joining(COMMA_DELIMITER));
        } else {
            List<Integer> userProjectIds = findAllProjectsForCurrentUser().stream().map(Project::getId)
                    .collect(Collectors.toList());
            return projects.stream().filter(project -> userProjectIds.contains(project.getId())).map(Project::getTitle)
                    .collect(Collectors.joining(COMMA_DELIMITER));
        }
    }

    /**
     * Delete project with ID 'projectID'.
     *
     * @param projectID ID of project to be deleted
     */
    public static void delete(int projectID) throws DAOException, ProjectDeletionException {
        Project project = ServiceManager.getProjectService().getById(projectID);
        if (!project.getProcesses().isEmpty()) {
            throw new ProjectDeletionException("cannotDeleteProject");
        }
        for (User user : project.getUsers()) {
            user.getProjects().remove(project);
            ServiceManager.getUserService().save(user);
        }
        for (Template template : project.getTemplates()) {
            template.getProjects().remove(project);
            ServiceManager.getTemplateService().save(template);
        }
        ServiceManager.getProjectService().remove(project);
    }

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
    public List<Project> findAll() throws DAOException {
        return (List<Project>) (List<?>) getAll();
    }
}

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
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseProjectServiceInterface;
import org.primefaces.model.SortOrder;

public class ProjectService extends SearchDatabaseService<Project, ProjectDAO>
        implements DatabaseProjectServiceInterface {

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
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Project");
    }

    @Override
    public Long countResults(Map<?, String> filters) throws DataException {
        try {
            BeanQuery query = getProjectsQuery();
            return countDatabaseRows(query.formCountQuery(), query.getQueryParameters());
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Project> getAllForSelectedClient() {
        return dao.getByQuery(
                "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId ORDER BY title",
                Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<Project> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException {

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

    @Override
    public List<Project> findAllAvailableForAssignToUser(User user) throws DataException {
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

    @Override
    public List<Project> findAllProjectsForCurrentUser() throws DataException {
        return ServiceManager.getUserService().getCurrentUser().getProjects();
    }

    @Override
    public List<Project> getProjectsWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId WHERE p.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }

    @Override
    public String getProjectTitles(List<Project> projects) throws DataException {
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewProjectList()
                && ServiceManager.getSecurityAccessService().hasAuthorityToViewClientList()) {
            return projects.stream().map(Project::getTitle).collect(Collectors.joining(COMMA_DELIMITER));
        } else {
            List<Integer> userProjectIds = findAllProjectsForCurrentUser().stream().map(ProjectInterface::getId)
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
    public static void delete(int projectID) throws DAOException, DataException, ProjectDeletionException {
        Project project = ServiceManager.getProjectService().getById(projectID);
        if (!project.getProcesses().isEmpty()) {
            throw new ProjectDeletionException("cannotDeleteProject");
        }
        for (User user : project.getUsers()) {
            user.getProjects().remove(project);
            ServiceManager.getUserService().saveToDatabase(user);
        }
        for (Template template : project.getTemplates()) {
            template.getProjects().remove(project);
            ServiceManager.getTemplateService().saveToDatabase(template);
        }
        ServiceManager.getProjectService().remove(project);
    }
}

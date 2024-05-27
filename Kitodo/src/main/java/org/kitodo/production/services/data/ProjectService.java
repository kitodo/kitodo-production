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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ClientInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.production.dto.DTOFactory;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.ClientSearchService;
import org.kitodo.production.services.data.interfaces.DatabaseProjectServiceInterface;
import org.primefaces.model.SortOrder;

public class ProjectService extends ClientSearchService<Project, ProjectInterface, ProjectDAO>
        implements DatabaseProjectServiceInterface {

    private static volatile ProjectService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ProjectService() {
        super(new ProjectDAO(), new ProjectType(), new Indexer<>(Project.class), new Searcher(Project.class),
                ProjectTypeField.CLIENT_ID.getKey());
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

    /**
     * Method saves processes and templates related to modified project.
     *
     * @param project
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        manageProcessesDependenciesForIndex(project);
        manageTemplatesDependenciesForIndex(project);
    }

    /**
     * Management of processes for project object.
     *
     * @param project
     *            object
     */
    private void manageProcessesDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (Process process : project.getProcesses()) {
                ServiceManager.getProcessService().removeFromIndex(process, false);
            }
        }
    }

    /**
     * Management of templates for project object.
     *
     * @param project
     *            object
     */
    private void manageTemplatesDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (Template template : project.getTemplates()) {
                template.getProjects().remove(project);
            }
        }

        for (Template template : project.getTemplates()) {
            ServiceManager.getTemplateService().saveToIndex(template, false);
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Project");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(getProjectsForCurrentUserQuery());
    }

    @Override
    public List<Project> getAllNotIndexed() {
        return getByQuery("FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Project> getAllForSelectedClient() {
        return dao.getByQuery(
                "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId ORDER BY title",
                Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<ProjectInterface> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(getProjectsForCurrentUserQuery(), getSortBuilder(sortField, sortOrder), first, pageSize,
            false);
    }

    /**
     * Find all projects available to assign to the edited user. It will be
     * displayed in the addProjectsPopup.
     *
     * @param user
     *            user which is going to be edited
     * @return list of all matching projects
     */
    public List<ProjectInterface> findAllAvailableForAssignToUser(User user) throws DataException {
        return findAvailableForAssignToUser(user);
    }

    private List<ProjectInterface> findAvailableForAssignToUser(User user) throws DataException {

        BoolQueryBuilder query = new BoolQueryBuilder();
        for (Client client : user.getClients()) {
            query.should(createSimpleQuery(ProjectTypeField.CLIENT_ID.getKey(), client.getId(), true));
        }

        List<ProjectInterface> projects = findByQuery(query, true);
        List<ProjectInterface> alreadyAssigned = new ArrayList<>();
        for (Project project : user.getProjects()) {
            alreadyAssigned.addAll(
                projects.stream().filter(currentProject -> currentProject.getId().equals(project.getId()))
                        .collect(Collectors.toList()));
        }
        projects.removeAll(alreadyAssigned);
        return projects;
    }

    @Override
    public ProjectInterface convertJSONObjectTo(Map<String, Object> jsonObject, boolean related) throws DataException {
        ProjectInterface project = DTOFactory.instance().newProject();
        project.setId(getIdFromJSONObject(jsonObject));
        project.setTitle(ProjectTypeField.TITLE.getStringValue(jsonObject));
        try {
            project.setStartTime(ProjectTypeField.START_DATE.getStringValue(jsonObject));
            project.setEndTime(ProjectTypeField.END_DATE.getStringValue(jsonObject));
        } catch (ParseException e) {
            throw new DataException(e);
        }
        project.setMetsRightsOwner(ProjectTypeField.METS_RIGTS_OWNER.getStringValue(jsonObject));
        project.setNumberOfPages(ProjectTypeField.NUMBER_OF_PAGES.getIntValue(jsonObject));
        project.setNumberOfVolumes(ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(jsonObject));
        project.setActive(ProjectTypeField.ACTIVE.getBooleanValue(jsonObject));
        ClientInterface client = DTOFactory.instance().newClient();
        client.setId(ProjectTypeField.CLIENT_ID.getIntValue(jsonObject));
        client.setName(ProjectTypeField.CLIENT_NAME.getStringValue(jsonObject));
        project.setClient(client);
        project.setHasProcesses(ProjectTypeField.HAS_PROCESSES.getBooleanValue(jsonObject));
        if (!related) {
            convertRelatedJSONObjects(jsonObject, project);
        } else {
            project.setActiveTemplates(getTemplatesForProject(jsonObject));
        }
        return project;
    }

    private List<TemplateInterface> getTemplatesForProject(Map<String, Object> jsonObject) throws DataException {
        List<TemplateInterface> templates = new ArrayList<>();
        List<Map<String, Object>> jsonArray = ProjectTypeField.TEMPLATES.getJsonArray(jsonObject);

        for (Map<String, Object> singleObject : jsonArray) {
            TemplateInterface template = DTOFactory.instance().newTemplate();
            template.setId(TemplateTypeField.ID.getIntValue(singleObject));
            template.setTitle(TemplateTypeField.TITLE.getStringValue(singleObject));
            templates.add(template);
        }
        return templates.stream().filter(TemplateInterface::isActive).collect(Collectors.toList());
    }

    private void convertRelatedJSONObjects(Map<String, Object> jsonObject, ProjectInterface project) throws DataException {
        // TODO: not clear if project lists will need it
        project.setUsers(new ArrayList<>());
        project.setActiveTemplates(convertRelatedJSONObjectTo(jsonObject, ProjectTypeField.TEMPLATES.getKey(),
            ServiceManager.getTemplateService()).stream().filter(TemplateInterface::isActive).collect(Collectors.toList()));
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
     * Get query for finding projects for current user.
     *
     * @return query for finding projects for current user
     */
    public QueryBuilder getProjectsForCurrentUserQuery() {
        List<Project> projects = ServiceManager.getUserService().getAuthenticatedUser().getProjects();
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
        for (Project project : projects) {
            idsQueryBuilder.addIds(project.getId().toString());
        }
        return idsQueryBuilder;
    }

    /**
     * Find all Projects for Current User.
     * @return A list of all Projects assigned tot he current user
     * @throws DataException when elasticsearch query is failing
     */
    public List<ProjectInterface> findAllProjectsForCurrentUser() throws DataException {
        return findByQuery(getProjectsForCurrentUserQuery(), false);
    }

    /**
     * Get all projects templates for given title and client id.
     *
     * @param title
     *            of Project
     * @param clientId
     *            id of client
     * @return list of all projects templates as Project objects
     */
    public List<Project> getProjectsWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId WHERE p.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }

    /**
     * Create and return String containing the titles of all given projects joined by a ", ".
     * @param projects list of roles
     * @return String containing project titles
     */
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

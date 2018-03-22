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

package org.kitodo.services.data;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.goobi.production.constants.FileNames;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ClientDTO;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class ProjectService extends TitleSearchService<Project, ProjectDTO, ProjectDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ProjectService.class);
    private static ProjectService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ProjectService() {
        super(new ProjectDAO(), new ProjectType(), new Indexer<>(Project.class), new Searcher(Project.class));
    }

    /**
     * Return singleton variable of type ProjectService.
     *
     * @return unique instance of ProcessService
     */
    public static ProjectService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (ProjectService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new ProjectService();
                }
            }
        }
        return instance;
    }

    /**
     * Method saves processes, users and clients related to modified project.
     *
     * @param project
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Project project) throws CustomResponseException, IOException, DAOException, DataException {
        manageProcessesDependenciesForIndex(project);
        manageUsersDependenciesForIndex(project);
        manageClientDependenciesForIndex(project);
    }

    private void manageClientDependenciesForIndex(Project project) throws CustomResponseException, IOException, DataException, DAOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            Client client = project.getClient();
            client.getProjects().remove(project);
            serviceManager.getClientService().saveToIndex(client);
        } else {
            JSONObject client = serviceManager.getClientService().findByProjectId(project.getId());
            Integer id = getIdFromJSONObject(client);
            if (id > 0) {
                if (!Objects.equals(id, project.getClient().getId())) {
                    Client oldClient = serviceManager.getClientService().getById(id);
                    serviceManager.getClientService().saveToIndex(oldClient);
                    serviceManager.getClientService().saveToIndex(project.getClient());
                }
            }
        }
    }

    /**
     * Management od processes for project object.
     *
     * @param project
     *            object
     */
    private void manageProcessesDependenciesForIndex(Project project) throws CustomResponseException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (Process process : project.getProcesses()) {
                serviceManager.getProcessService().removeFromIndex(process);
            }
        } else {
            for (Process process : project.getProcesses()) {
                serviceManager.getProcessService().saveToIndex(process);
            }
        }
    }

    /**
     * Management od users for project object.
     *
     * @param project
     *            object
     */
    private void manageUsersDependenciesForIndex(Project project) throws CustomResponseException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (User user : project.getUsers()) {
                user.getProjects().remove(project);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            for (User user : project.getUsers()) {
                serviceManager.getUserService().saveToIndex(user);
            }
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Project");
    }

    /**
     * Find active or inactive projects.
     *
     * @param active
     *            if true - find active projects, if false - find not active
     *            projects
     * @param related
     *            if true - found project is related to some other DTO object, if
     *            false - not and it collects all related objects
     * @return list of ProjectDTO objects
     */
    List<ProjectDTO> findByActive(Boolean active, boolean related) throws DataException {
        QueryBuilder query = createSimpleQuery("active", active, true);
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), related);
    }

    /**
     * Find project by id of process.
     *
     * @param id
     *            of process
     * @return search result
     */
    public JsonObject findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("processes.id", id, true);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find projects by title of process.
     *
     * @param title
     *            of process
     * @return list of JSON objects with projects for specific process title
     */
    public List<JsonObject> findByProcessTitle(String title) throws DataException {
        Set<Integer> processIds = new HashSet<>();

        List<JsonObject> processes = serviceManager.getProcessService().findByTitle(title, true);
        for (JsonObject process : processes) {
            processIds.add(getIdFromJSONObject(process));
        }
        return searcher.findDocuments(createSetQuery("processes.id", processIds, true).toString());
    }

    /**
     * Find project by id of user.
     *
     * @param id
     *            of user
     * @return list of JSON objects
     */
    List<JsonObject> findByUserId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("users.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find projects by login of user.
     *
     * @param login
     *            of user
     * @return list of search result with projects for specific user login
     */
    List<JsonObject> findByUserLogin(String login) throws DataException {
        JsonObject user = serviceManager.getUserService().findByLogin(login);
        return findByUserId(getIdFromJSONObject(user));
    }

    /**
     * Get all projects from index an convert them for frontend.
     *
     * @return list of ProjectDTO objects
     */
    public List<ProjectDTO> findAll() throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(), false);
    }

    /**
     * Get all projects sorted by title.
     *
     * @return all projects sorted by title as Project objects
     */
    public List<Project> getAllProjectsSortedByTitle() {
        return dao.getAllProjectsSortedByTitle();
    }

    /**
     * Get all active projects sorted by title.
     *
     * @return all active projects sorted by title as Project objects
     */
    public List<Project> getAllActiveProjectsSortedByTitle() {
        return dao.getAllActiveProjectsSortedByTitle();
    }

    @Override
    public ProjectDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject projectJSONObject = jsonObject.getJsonObject("_source");
        projectDTO.setTitle(projectJSONObject.getString("title"));
        projectDTO.setStartDate(projectJSONObject.getString("startDate"));
        projectDTO.setEndDate(projectJSONObject.getString("endDate"));
        projectDTO.setFileFormatDmsExport(projectJSONObject.getString("fileFormatDmsExport"));
        projectDTO.setFileFormatInternal(projectJSONObject.getString("fileFormatInternal"));
        projectDTO.setMetsRightsOwner(projectJSONObject.getString("metsRightsOwner"));
        projectDTO.setNumberOfPages(projectJSONObject.getInt("numberOfPages"));
        projectDTO.setNumberOfVolumes(projectJSONObject.getInt("numberOfVolumes"));
        projectDTO.setActive(projectJSONObject.getBoolean("active"));
        projectDTO.setProcesses(getTemplatesForProjectDTO(projectJSONObject));
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(getIntegerPropertyForDTO(projectJSONObject, "client.id"));
        clientDTO.setName(getStringPropertyForDTO(projectJSONObject, "client.clientName"));
        projectDTO.setClient(clientDTO);
        if (!related) {
            projectDTO = convertRelatedJSONObjects(projectJSONObject, projectDTO);
        }
        return projectDTO;
    }

    private List<ProcessDTO> getTemplatesForProjectDTO(JsonObject jsonObject) {
        List<ProcessDTO> processDTOS = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getJsonArray("processes");

        for (JsonValue singleObject : jsonArray) {
            JsonObject processJson = singleObject.asJsonObject();
            boolean template = processJson.getBoolean("template");
            if (template) {
                ProcessDTO processDTO = new ProcessDTO();
                processDTO.setId(processJson.getInt("id"));
                processDTO.setTitle(processJson.getString("title"));
                processDTOS.add(processDTO);
            }
        }
        return processDTOS;
    }

    private ProjectDTO convertRelatedJSONObjects(JsonObject jsonObject, ProjectDTO projectDTO) throws DataException {
        // TODO: not clear if project lists will need it
        projectDTO.setUsers(new ArrayList<>());
        return projectDTO;
    }

    /**
     * Checks if a project can be actually used.
     *
     * @param project
     *            The project to check
     * @return true, if project is complete and can be used, false, if project
     *         is incomplete
     */
    public boolean isProjectComplete(Project project) {
        boolean projectsXmlExists = (new File(ConfigCore.getKitodoConfigDirectory() + FileNames.PROJECT_CONFIGURATION_FILE)).exists();
        boolean digitalCollectionsXmlExists = (new File(
                ConfigCore.getKitodoConfigDirectory() + FileNames.DIGITAL_COLLECTIONS_FILE)).exists();

        return project.getTitle() != null && project.template != null && project.getFileFormatDmsExport() != null
                && project.getFileFormatInternal() != null && digitalCollectionsXmlExists && projectsXmlExists;
    }

    /**
     * Duplicate the project with the given ID 'itemId'.
     *
     * @return the duplicated Project
     */
    public Project duplicateProject(Integer itemId) throws DAOException {
        Project duplicatedProject = new Project();

        Project baseProject = getById(itemId);

        // Project _title_ should explicitly _not_ be duplicated!
        duplicatedProject.setStartDate(baseProject.getStartDate());
        duplicatedProject.setEndDate(baseProject.getEndDate());
        duplicatedProject.setNumberOfPages(baseProject.getNumberOfPages());
        duplicatedProject.setNumberOfVolumes(baseProject.getNumberOfVolumes());

        duplicatedProject.setFileFormatInternal(baseProject.getFileFormatInternal());
        duplicatedProject.setFileFormatDmsExport(baseProject.getFileFormatDmsExport());
        duplicatedProject.setDmsImportErrorPath(baseProject.getDmsImportErrorPath());
        duplicatedProject.setDmsImportSuccessPath(baseProject.getDmsImportSuccessPath());

        duplicatedProject.setDmsImportTimeOut(baseProject.getDmsImportTimeOut());
        duplicatedProject.setUseDmsImport(baseProject.isUseDmsImport());
        duplicatedProject.setDmsImportCreateProcessFolder(baseProject.isDmsImportCreateProcessFolder());

        duplicatedProject.setMetsRightsOwner(baseProject.getMetsRightsOwner());
        duplicatedProject.setMetsRightsOwnerLogo(baseProject.getMetsRightsOwnerLogo());
        duplicatedProject.setMetsRightsOwnerSite(baseProject.getMetsRightsOwnerSite());
        duplicatedProject.setMetsRightsOwnerMail(baseProject.getMetsRightsOwnerMail());

        duplicatedProject.setMetsDigiprovPresentation(baseProject.getMetsDigiprovPresentation());
        duplicatedProject.setMetsDigiprovPresentationAnchor(baseProject.getMetsDigiprovPresentationAnchor());
        duplicatedProject.setMetsDigiprovReference(baseProject.getMetsDigiprovReference());
        duplicatedProject.setMetsDigiprovReferenceAnchor(baseProject.getMetsDigiprovReferenceAnchor());

        duplicatedProject.setMetsPointerPath(baseProject.getMetsPointerPath());
        duplicatedProject.setMetsPointerPathAnchor(baseProject.getMetsPointerPathAnchor());
        duplicatedProject.setMetsPurl(baseProject.getMetsPurl());
        duplicatedProject.setMetsContentIDs(baseProject.getMetsContentIDs());

        ArrayList<ProjectFileGroup> duplicatedFileGroups = new ArrayList<>();
        for (ProjectFileGroup projectFileGroup : baseProject.getProjectFileGroups()) {
            ProjectFileGroup duplicatedGroup = new ProjectFileGroup();
            duplicatedGroup.setMimeType(projectFileGroup.getMimeType());
            duplicatedGroup.setName(projectFileGroup.getName());
            duplicatedGroup.setPath(projectFileGroup.getPath());
            duplicatedGroup.setPreviewImage(projectFileGroup.isPreviewImage());
            duplicatedGroup.setSuffix(projectFileGroup.getSuffix());
            duplicatedGroup.setFolder(projectFileGroup.getFolder());

            duplicatedGroup.setProject(duplicatedProject);
            duplicatedFileGroups.add(duplicatedGroup);
        }
        duplicatedProject.setProjectFileGroups(duplicatedFileGroups);

        return duplicatedProject;
    }

    /**
     * Return a string containing a comma separated list of process templates
     * associated with this project.
     *
     * @return process templates associated with this project
     */
    public String getProjectTemplatesTitlesAsString(int id) throws DAOException {
        Project project = serviceManager.getProjectService().getById(id);
        return String.join(", ", project.getProcesses().stream().filter(Process::isTemplate).map(Process::getTitle)
                .collect(Collectors.toList()));
    }
}

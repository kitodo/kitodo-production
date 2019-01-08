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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ClientDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.TitleSearchService;
import org.primefaces.model.SortOrder;

public class ProjectService extends TitleSearchService<Project, ProjectDTO, ProjectDAO> {

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
     * Method saves processes and users related to modified project.
     *
     * @param project
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Project project) throws CustomResponseException, IOException {
        manageProcessesDependenciesForIndex(project);
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
                ServiceManager.getProcessService().removeFromIndex(process, false);
            }
        } else {
            for (Process process : project.getProcesses()) {
                ServiceManager.getProcessService().saveToIndex(process, false);
            }
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
        return searcher.countDocuments(getProjectsForCurrentUserQuery());
    }

    @Override
    public List<Project> getAllNotIndexed() {
        return getByQuery("FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Project> getAllForSelectedClient() {
        return dao.getByQuery("SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<ProjectDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return convertJSONObjectsToDTOs(
            searcher.findDocuments(getProjectsForCurrentUserQuery(), getSort(sortField, sortOrder), first, pageSize),
            false);
    }

    /**
     * Find all projects available to assign to the edited user. It will be
     * displayed in the addProjectsPopup.
     *
     * @param userId
     *            id of user which is going to be edited
     * @return list of all matching projects
     */
    public List<ProjectDTO> findAllAvailableForAssignToUser(Integer userId) throws DataException {
        return findAvailableForAssignToUser(userId);
    }

    private List<ProjectDTO> findAvailableForAssignToUser(Integer userId) throws DataException {
        int sessionClientId = ServiceManager.getUserService().getSessionClientId();

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForUserId(userId, false));
        query.must(createSimpleQuery(ProjectTypeField.CLIENT_ID.getKey(), sessionClientId, true));
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), true);
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
        QueryBuilder query = createSimpleQuery(ProjectTypeField.ACTIVE.getKey(), active, true);
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), related);
    }

    /**
     * Find project by id of process.
     *
     * @param id
     *            of process
     * @return search result
     */
    JsonObject findByProcessId(Integer id) throws DataException {
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
    List<JsonObject> findByProcessTitle(String title) throws DataException {
        List<JsonObject> processes = ServiceManager.getProcessService().findByTitle(title, true);

        return searcher.findDocuments(createSetQuery(ProjectTypeField.PROCESSES + ".id", processes, true).toString());
    }

    /**
     * Find project by id of user.
     *
     * @param id
     *            of user
     * @return list of JSON objects
     */
    List<JsonObject> findByUserId(Integer id) throws DataException {
        return searcher.findDocuments(getQueryForUserId(id, true).toString());
    }

    private QueryBuilder getQueryForUserId(Integer id, boolean contains) {
        return createSimpleQuery(ProjectTypeField.USERS.getKey() + ".id", id, contains);
    }

    /**
     * Find projects by login of user.
     *
     * @param login
     *            of user
     * @return list of search result with projects for specific user login
     */
    List<JsonObject> findByUserLogin(String login) throws DataException {
        return searcher.findDocuments(getQueryForUserLogin(login, true).toString());
    }

    private QueryBuilder getQueryForUserLogin(String login, boolean contains) {
        return createSimpleQuery(ProjectTypeField.USERS + ".login", login, contains);
    }

    /**
     * Get all projects sorted by title.
     *
     * @return all projects sorted by title as Project objects
     */
    public List<Project> getAllProjectsSortedByTitle() {
        return dao.getAllProjectsSortedByTitle();
    }

    @Override
    public ProjectDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(getIdFromJSONObject(jsonObject));

        JsonObject projectJSONObject = jsonObject.getJsonObject("_source");
        projectDTO.setTitle(ProjectTypeField.TITLE.getStringValue(projectJSONObject));
        projectDTO.setStartDate(ProjectTypeField.START_DATE.getStringValue(projectJSONObject));
        projectDTO.setEndDate(ProjectTypeField.END_DATE.getStringValue(projectJSONObject));
        projectDTO.setFileFormatDmsExport(ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getStringValue(projectJSONObject));
        projectDTO.setFileFormatInternal(ProjectTypeField.FILE_FORMAT_INTERNAL.getStringValue(projectJSONObject));
        projectDTO.setMetsRightsOwner(ProjectTypeField.METS_RIGTS_OWNER.getStringValue(projectJSONObject));
        projectDTO.setNumberOfPages(ProjectTypeField.NUMBER_OF_PAGES.getIntValue(projectJSONObject));
        projectDTO.setNumberOfVolumes(ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(projectJSONObject));
        projectDTO.setActive(ProjectTypeField.ACTIVE.getBooleanValue(projectJSONObject));
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(ProjectTypeField.CLIENT_ID.getIntValue(projectJSONObject));
        clientDTO.setName(ProjectTypeField.CLIENT_NAME.getStringValue(projectJSONObject));
        projectDTO.setClient(clientDTO);
        if (!related) {
            convertRelatedJSONObjects(projectJSONObject, projectDTO);
        } else {
            projectDTO.setTemplates(getTemplatesForProjectDTO(projectJSONObject));
        }
        return projectDTO;
    }

    private List<TemplateDTO> getTemplatesForProjectDTO(JsonObject jsonObject) throws DataException {
        List<TemplateDTO> templateDTOS = new ArrayList<>();
        JsonArray jsonArray = ProjectTypeField.TEMPLATES.getJsonArray(jsonObject);

        for (JsonValue singleObject : jsonArray) {
            JsonObject templateJson = singleObject.asJsonObject();
            TemplateDTO templateDTO = new TemplateDTO();
            templateDTO.setId(TemplateTypeField.ID.getIntValue(templateJson));
            templateDTO.setTitle(TemplateTypeField.TITLE.getStringValue(templateJson));
            templateDTOS.add(templateDTO);
        }
        return templateDTOS;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, ProjectDTO projectDTO) throws DataException {
        projectDTO.setTemplates(convertRelatedJSONObjectToDTO(jsonObject, ProjectTypeField.TEMPLATES.getKey(),
            ServiceManager.getTemplateService()));
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
        boolean digitalCollectionsXmlExists = KitodoConfigFile.DIGITAL_COLLECTIONS.exists();

        return project.getTitle() != null && project.template != null && project.getFileFormatDmsExport() != null
                && project.getFileFormatInternal() != null && digitalCollectionsXmlExists && projectsXmlExists;
    }

    /**
     * Duplicate the project with the given ID 'itemId'.
     *
     * @return the duplicated Project
     */
    public Project duplicateProject(Project baseProject) {
        Project duplicatedProject = new Project();

        // Project _title_ should explicitly _not_ be duplicated!
        duplicatedProject.setClient(baseProject.getClient());
        duplicatedProject.setStartDate(baseProject.getStartDate());
        duplicatedProject.setEndDate(baseProject.getEndDate());
        duplicatedProject.setNumberOfPages(baseProject.getNumberOfPages());
        duplicatedProject.setNumberOfVolumes(baseProject.getNumberOfVolumes());

        duplicatedProject.setFileFormatInternal(baseProject.getFileFormatInternal());
        duplicatedProject.setFileFormatDmsExport(baseProject.getFileFormatDmsExport());
        duplicatedProject.setDmsImportErrorPath(baseProject.getDmsImportErrorPath());
        duplicatedProject.setDmsImportSuccessPath(baseProject.getDmsImportSuccessPath());

        duplicatedProject.setDmsImportImagesPath(baseProject.getDmsImportImagesPath());
        duplicatedProject.setDmsImportRootPath(baseProject.getDmsImportRootPath());
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

        List<Folder> duplicatedFolders = new ArrayList<>();
        for (Folder folder : baseProject.getFolders()) {
            Folder duplicatedFolder = new Folder();
            duplicatedFolder.setMimeType(folder.getMimeType());
            duplicatedFolder.setFileGroup(folder.getFileGroup());
            duplicatedFolder.setUrlStructure(folder.getUrlStructure());
            duplicatedFolder.setPath(folder.getPath());

            duplicatedFolder.setProject(duplicatedProject);
            duplicatedFolder.setCopyFolder(folder.isCopyFolder());
            duplicatedFolder.setCreateFolder(folder.isCreateFolder());
            duplicatedFolder.setDerivative(folder.getDerivative().orElse(null));
            duplicatedFolder.setDpi(folder.getDpi().orElse(null));
            duplicatedFolder.setImageScale(folder.getImageScale().orElse(null));
            duplicatedFolder.setImageSize(folder.getImageSize().orElse(null));
            duplicatedFolder.setLinkingMode(folder.getLinkingMode());
            duplicatedFolders.add(duplicatedFolder);
        }
        duplicatedProject.setFolders(duplicatedFolders);

        return duplicatedProject;
    }

    private String getProjectsForCurrentUserQuery() {
        int currentUserId = ServiceManager.getUserService().getAuthenticatedUser().getId();
        int sessionClientId = ServiceManager.getUserService().getSessionClientId();

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForUserId(currentUserId, true));
        query.must(createSimpleQuery(ProjectTypeField.CLIENT_ID.getKey(), sessionClientId, true));
        return query.toString();
    }
}

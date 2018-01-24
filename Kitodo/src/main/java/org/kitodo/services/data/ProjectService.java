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
import de.sub.goobi.helper.ProjectHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.goobi.production.constants.FileNames;
import org.goobi.production.flow.statistics.StepInformation;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class ProjectService extends TitleSearchService<Project, ProjectDTO, ProjectDAO> {

    private List<StepInformation> commonWorkFlow = null;
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
     * Method saves processes and users related to modified project.
     *
     * @param project
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Project project) throws CustomResponseException, IOException {
        manageProcessesDependenciesForIndex(project);
        manageUsersDependenciesForIndex(project);
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
     * Find archived or not archived projects.
     *
     * @param archived
     *            if true - find archived projects, if false - find not archived
     *            projects
     * @param related
     *            if true - found project is related to some other DTO object, if
     *            false - not and it collects all related objects
     * @return list of ProjectDTO objects
     */
    List<ProjectDTO> findByArchived(Boolean archived, boolean related) throws DataException {
        QueryBuilder query = createSimpleQuery("archived", archived, true);
        return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), related);
    }

    /**
     * Find project by id of process.
     *
     * @param id
     *            of process
     * @return search result
     */
    public JSONObject findByProcessId(Integer id) throws DataException {
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
    public List<JSONObject> findByProcessTitle(String title) throws DataException {
        Set<Integer> processIds = new HashSet<>();

        List<JSONObject> processes = serviceManager.getProcessService().findByTitle(title, true);
        for (JSONObject process : processes) {
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
    List<JSONObject> findByUserId(Integer id) throws DataException {
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
    List<JSONObject> findByUserLogin(String login) throws DataException {
        JSONObject user = serviceManager.getUserService().findByLogin(login);
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
     * Get all not archived projects sorted by title.
     *
     * @return all not archived projects sorted by title as Project objects
     */
    public List<Project> getAllNotArchivedProjectsSortedByTitle() {
        return dao.getAllNotArchivedProjectsSortedByTitle();
    }

    @Override
    public ProjectDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject projectJSONObject = getSource(jsonObject);
        projectDTO.setTitle(getStringPropertyForDTO(projectJSONObject, "title"));
        projectDTO.setStartDate(getStringPropertyForDTO(projectJSONObject, "startDate"));
        projectDTO.setEndDate(getStringPropertyForDTO(projectJSONObject, "endDate"));
        projectDTO.setFileFormatDmsExport(getStringPropertyForDTO(projectJSONObject, "fileFormatDmsExport"));
        projectDTO.setFileFormatInternal(getStringPropertyForDTO(projectJSONObject, "fileFormatInternal"));
        projectDTO.setMetsRightsOwner(getStringPropertyForDTO(projectJSONObject, "metsRightsOwner"));
        projectDTO.setNumberOfPages(getIntegerPropertyForDTO(projectJSONObject, "numberOfPages"));
        projectDTO.setNumberOfVolumes(getIntegerPropertyForDTO(projectJSONObject, "numberOfVolumes"));
        projectDTO.setProjectIsArchived(getBooleanPropertyForDTO(projectJSONObject, "archived"));
        if (!related) {
            projectDTO = convertRelatedJSONObjects(projectJSONObject, projectDTO);
        }
        return projectDTO;
    }

    private ProjectDTO convertRelatedJSONObjects(JSONObject jsonObject, ProjectDTO projectDTO) throws DataException {
        projectDTO.setProcesses(
                convertRelatedJSONObjectToDTO(jsonObject, "processes", serviceManager.getProcessService()));
        projectDTO.setUsers(convertRelatedJSONObjectToDTO(jsonObject, "users", serviceManager.getUserService()));
        return projectDTO;
    }

    /**
     * Get workflow.
     *
     * @return a list with information for each step on workflow
     */
    public List<StepInformation> getWorkFlow(Project project)throws DataException {
        if (this.commonWorkFlow == null) {
            if (project.getId() != null) {
                this.commonWorkFlow = ProjectHelper.getProjectWorkFlowOverview(project);
            } else {
                this.commonWorkFlow = new ArrayList<>();
            }
        }
        return this.commonWorkFlow;
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

        // only duplicate templates, not all processes, of given base project
        duplicatedProject.setProcesses(new ArrayList<>(
                baseProject.getProcesses().stream().filter(Process::isTemplate).collect(Collectors.toList())));
        return duplicatedProject;
    }
}

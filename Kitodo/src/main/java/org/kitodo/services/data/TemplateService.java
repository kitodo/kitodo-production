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

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TemplateType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.dto.TaskDTO;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.forms.TemplateForm;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class TemplateService extends TitleSearchService<Template, TemplateDTO, TemplateDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static TemplateService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TemplateService() {
        super(new TemplateDAO(), new TemplateType(), new Indexer<>(Template.class), new Searcher(Template.class));
    }

    /**
     * Return singleton variable of type TemplateService.
     *
     * @return unique instance of TemplateService
     */
    public static TemplateService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (TemplateService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new TemplateService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Template");
    }

    /**
     * Method saves or removes tasks and project related to modified template.
     *
     * @param template
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Template template)
            throws CustomResponseException, DAOException, DataException, IOException {
        manageProjectDependenciesForIndex(template);
        manageTaskDependenciesForIndex(template);
    }

    /**
     * Add process to project, if project is assigned to process.
     *
     * @param template
     *            object
     */
    private void manageProjectDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        if (Objects.nonNull(template.getProject())) {
            serviceManager.getProjectService().saveToIndex(template.getProject(), false);
        }
    }

    /**
     * Check IndexAction flag in for process object. If DELETE remove all tasks from
     * index, if other call saveOrRemoveTaskInIndex() method.
     *
     * @param template
     *            object
     */
    private void manageTaskDependenciesForIndex(Template template)
            throws CustomResponseException, DAOException, IOException, DataException {
        if (template.getIndexAction().equals(IndexAction.DELETE)) {
            for (Task task : template.getTasks()) {
                serviceManager.getTaskService().removeFromIndex(task, false);
            }
        } else {
            saveOrRemoveTasksInIndex(template);
        }
    }

    /**
     * Compare index and database, according to comparisons results save or remove
     * tasks.
     *
     * @param template
     *            object
     */
    private void saveOrRemoveTasksInIndex(Template template)
            throws CustomResponseException, DAOException, IOException, DataException {
        List<Integer> database = new ArrayList<>();
        List<Integer> index = new ArrayList<>();

        for (Task task : template.getTasks()) {
            database.add(task.getId());
            serviceManager.getTaskService().saveToIndex(task, false);
        }

        List<JsonObject> searchResults = serviceManager.getTaskService().findByProcessId(template.getId());
        for (JsonObject object : searchResults) {
            index.add(getIdFromJSONObject(object));
        }

        List<Integer> missingInIndex = findMissingValues(database, index);
        List<Integer> notNeededInIndex = findMissingValues(index, database);
        for (Integer missing : missingInIndex) {
            serviceManager.getTaskService().saveToIndex(serviceManager.getTaskService().getById(missing), false);
        }

        for (Integer notNeeded : notNeededInIndex) {
            serviceManager.getTaskService().removeFromIndex(notNeeded, false);
        }
    }

    /**
     * Compare two list and return difference between them.
     *
     * @param firstList
     *            list from which records can be remove
     * @param secondList
     *            records stored here will be removed from firstList
     * @return difference between two lists
     */
    private List<Integer> findMissingValues(List<Integer> firstList, List<Integer> secondList) {
        List<Integer> newList = new ArrayList<>(firstList);
        newList.removeAll(secondList);
        return newList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TemplateDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        Map<String, String> filterMap = (Map<String, String>) filters;

        BoolQueryBuilder query;

        if (Objects.equals(filters, null) || filters.isEmpty()) {
            return convertJSONObjectsToDTOs(
                    findBySort(false, true, sort, offset, size), false);
        }

        query = readFilters(filterMap);

        String queryString = "";
        if (!Objects.equals(query, null)) {
            queryString = query.toString();
        }
        return convertJSONObjectsToDTOs(searcher.findDocuments(queryString, sort, offset, size), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String createCountQuery(Map filters) throws DataException {
        Map<String, String> filterMap = (Map<String, String>) filters;

        BoolQueryBuilder query;

        if (Objects.equals(filters, null) || filters.isEmpty()) {
            query = new BoolQueryBuilder();
            query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
            query.must(serviceManager.getProcessService().getQueryProjectActive(true));
        } else {
            query = readFilters(filterMap);
        }

        if (Objects.nonNull(query)) {
            return query.toString();
        }
        return "";
    }

    private BoolQueryBuilder readFilters(Map<String, String> filterMap) throws DataException {
        TemplateForm form = (TemplateForm) Helper.getManagedBeanValue("TemplateForm");
        if (Objects.isNull(form)) {
            form = new TemplateForm();
        }
        BoolQueryBuilder query = null;

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            query = serviceManager.getFilterService().queryBuilder(entry.getValue(), ObjectType.TEMPLATE, false,
                false);
            if (!form.isShowClosedProcesses()) {
                query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
            }
            if (!form.isShowInactiveProjects()) {
                query.must(serviceManager.getProcessService().getQueryProjectActive(true));
            }
        }
        return query;
    }

    @Override
    public TemplateDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject templateJSONObject = jsonObject.getJsonObject("_source");
        templateDTO.setTitle(templateJSONObject.getString("title"));
        templateDTO.setOutputName(templateJSONObject.getString("outputName"));
        templateDTO.setWikiField(templateJSONObject.getString("wikiField"));
        templateDTO.setCreationDate(templateJSONObject.getString("creationDate"));

        if (!related) {
            convertRelatedJSONObjects(templateJSONObject, templateDTO);
        } else {
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setId(templateJSONObject.getInt("project.id"));
            projectDTO.setTitle(templateJSONObject.getString("project.title"));
            projectDTO.setActive(templateJSONObject.getBoolean("project.active"));
            templateDTO.setProject(projectDTO);
        }

        return templateDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, TemplateDTO templateDTO) throws DataException {
        Integer project = jsonObject.getInt("project.id");
        templateDTO.setProject(serviceManager.getProjectService().findById(project));
        templateDTO.setTasks(convertRelatedJSONObjectToDTO(jsonObject, "tasks", serviceManager.getTaskService()));
        templateDTO.setContainsUnreachableSteps(containsDtoUnreachableSteps(templateDTO.getTasks()));
    }

    private List<JsonObject> findBySort(boolean closed, boolean active, String sort, Integer offset, Integer size)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(serviceManager.getProcessService().getQuerySortHelperStatus(closed));
        query.must(serviceManager.getProcessService().getQueryProjectActive(active));
        return searcher.findDocuments(query.toString(), sort, offset, size);
    }

    /**
     * Check whether the template contains tasks that are not assigned to a user or
     * user group.
     *
     * @param tasks
     *            list of tasks for testing
     * @return true or false
     */
    public boolean containsBeanUnreachableSteps(List<Task> tasks) {
        TaskService taskService = serviceManager.getTaskService();
        if (tasks.isEmpty()) {
            return true;
        }
        for (Task task : tasks) {
            if (taskService.getUserGroupsSize(task) == 0 && taskService.getUsersSize(task) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the template contains tasks that are not assigned to a user or
     * user group.
     *
     * @param tasks
     *            list of tasks for testing
     * @return true or false
     */
    public boolean containsDtoUnreachableSteps(List<TaskDTO> tasks) {
        if (tasks.isEmpty()) {
            return true;
        }
        for (TaskDTO task : tasks) {
            if (task.getUserGroupsSize() == 0 && task.getUsersSize() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find templates of active projects, sorted according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted processes as ProcessDTO objects
     */
    public List<TemplateDTO> findTemplatesOfActiveProjects(String sort) throws DataException {
        return convertJSONObjectsToDTOs(findByActive(true, sort), false);
    }

    private List<JsonObject> findByActive(boolean active, String sort) throws DataException {
        return searcher.findDocuments(serviceManager.getProcessService().getQueryProjectActive(active).toString(),
            sort);
    }

    /**
     * Get all process templates for given title.
     *
     * @param title
     *            of Template
     * @return list of all process templates as Template objects
     */
    public List<Template> getProcessTemplatesWithTitle(String title) {
        return dao.getTemplatesWithTitle(title);
    }

    /**
     * Get process templates for users.
     *
     * @param projects
     *            list of project ids for user's projects
     * @return list of all process templates for user as Template objects
     */
    public List<Template> getProcessTemplatesForUser(List<Integer> projects) {
        return dao.getTemplatesForUser(projects);
    }

    /**
     * Get all active process templates.
     *
     * @return A list of all process templates as Template objects.
     */
    public List<Template> getActiveTemplates() {
        return dao.getActiveTemplates();
    }
}

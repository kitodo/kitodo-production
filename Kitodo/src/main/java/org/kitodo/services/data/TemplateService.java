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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TemplateType;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.dto.WorkflowDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class TemplateService extends TitleSearchService<Template, TemplateDTO, TemplateDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static TemplateService instance = null;
    private boolean showInactiveTemplates = false;
    private boolean showInactiveProjects = false;

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
    protected void manageDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        manageProjectDependenciesForIndex(template);
    }

    /**
     * Add process to project, if project is assigned to process.
     *
     * @param template
     *            object
     */
    private void manageProjectDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        for (Project project : template.getProjects()) {
            if (template.getIndexAction().equals(IndexAction.DELETE)) {
                project.getTemplates().remove(template);
                serviceManager.getProjectService().saveToIndex(project, false);
            } else {
                serviceManager.getProjectService().saveToIndex(project, false);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TemplateDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        Map<String, String> filterMap = (Map<String, String>) filters;

        BoolQueryBuilder query;

        if (Objects.equals(filters, null) || filters.isEmpty()) {
            return convertJSONObjectsToDTOs(findBySort(false, true, sort, offset, size), false);
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
            query.must(getQueryProjectActive(true));
        } else {
            query = readFilters(filterMap);
        }

        if (Objects.nonNull(query)) {
            return query.toString();
        }
        return "";
    }

    private BoolQueryBuilder readFilters(Map<String, String> filterMap) throws DataException {
        BoolQueryBuilder query = null;

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            query = serviceManager.getFilterService().queryBuilder(entry.getValue(), ObjectType.TEMPLATE, false, false);
            if (!showInactiveTemplates) {
                query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
            }
            if (!showInactiveProjects) {
                query.must(getQueryProjectActive(true));
            }
        }
        return query;
    }

    /**
     * Duplicate the template with the given ID 'itemId'.
     *
     * @return the duplicated Template
     */
    public Template duplicateTemplate(Template baseTemplate) {
        Template duplicatedTemplate = new Template();

        // Template _title_ should explicitly _not_ be duplicated!
        duplicatedTemplate.setCreationDate(new Date());
        duplicatedTemplate.setInChoiceListShown(baseTemplate.getInChoiceListShown());
        duplicatedTemplate.setDocket(baseTemplate.getDocket());
        duplicatedTemplate.setRuleset(baseTemplate.getRuleset());
        // tasks don't need to be duplicated - will be created out of copied workflow
        duplicatedTemplate.setWorkflow(baseTemplate.getWorkflow());

        //TODO: make sure if copy should be assigned automatically to all projects
        for (Project project : baseTemplate.getProjects()) {
            duplicatedTemplate.getProjects().add(project);
            project.getTemplates().add(duplicatedTemplate);
        }

        return duplicatedTemplate;
    }

    @Override
    public TemplateDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject templateJSONObject = jsonObject.getJsonObject("_source");
        templateDTO.setTitle(TemplateTypeField.TITLE.getStringValue(templateJSONObject));
        templateDTO.setWikiField(TemplateTypeField.WIKI_FIELD.getStringValue(templateJSONObject));
        templateDTO.setActive(TemplateTypeField.ACTIVE.getBooleanValue(templateJSONObject));
        templateDTO.setCreationDate(TemplateTypeField.CREATION_DATE.getStringValue(templateJSONObject));
        templateDTO.setDocket(
            serviceManager.getDocketService().findById(TemplateTypeField.DOCKET.getIntValue(templateJSONObject)));
        templateDTO.setRuleset(
            serviceManager.getRulesetService().findById(TemplateTypeField.RULESET.getIntValue(templateJSONObject)));
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setTitle(templateJSONObject.getString(TemplateTypeField.WORKFLOW_TITLE.getKey()));
        workflowDTO.setFileName(templateJSONObject.getString(TemplateTypeField.WORKFLOW_FILE_NAME.getKey()));
        templateDTO.setWorkflow(workflowDTO);

        if (!related) {
            convertRelatedJSONObjects(templateJSONObject, templateDTO);
        }

        return templateDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, TemplateDTO templateDTO) throws DataException {
        templateDTO.setProjects(convertRelatedJSONObjectToDTO(jsonObject, TemplateTypeField.PROJECTS.getKey(),
            serviceManager.getProjectService()));
    }

    private List<JsonObject> findBySort(boolean closed, boolean active, String sort, Integer offset, Integer size)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(serviceManager.getProcessService().getQuerySortHelperStatus(closed));
        query.must(getQueryProjectActive(active));
        return searcher.findDocuments(query.toString(), sort, offset, size);
    }

    /**
     * Set show inactive projects.
     *
     * @param showInactiveProjects
     *            as boolean
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    /**
     * Set show inactive templates.
     *
     * @param showInactiveTemplates
     *            as boolean
     */
    public void setShowInactiveTemplates(boolean showInactiveTemplates) {
        this.showInactiveTemplates = showInactiveTemplates;
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
        return searcher.findDocuments(getQueryProjectActive(active).toString(), sort);
    }

    /**
     * Get query for active projects.
     *
     * @param active
     *            true or false
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryProjectActive(boolean active) {
        return createSimpleQuery(TemplateTypeField.PROJECTS.getKey() + "." + ProjectTypeField.ACTIVE, active, true);
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

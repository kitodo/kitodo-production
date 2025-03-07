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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class TemplateService extends BaseBeanService<Template, TemplateDAO> {

    private static final Map<String, String> SORT_FIELD_MAPPING;

    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("ruleset.title.keyword", "ruleset_id");
        SORT_FIELD_MAPPING.put("active", "active");
    }

    private static final Logger logger = LogManager.getLogger(TemplateService.class);
    private static volatile TemplateService instance = null;
    private boolean showInactiveTemplates = false;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TemplateService() {
        super(new TemplateDAO());
    }

    /**
     * Return singleton variable of type TemplateService.
     *
     * @return unique instance of TemplateService
     */
    public static TemplateService getInstance() {
        TemplateService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (TemplateService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new TemplateService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Retrieves a map indicating the usage status of templates.
     * The method executes an HQL query to determine whether each template is used
     * (i.e., has associated processes).
     *
     * @return a map where the key is the template ID and the value is a boolean
     *         indicating whether the template is used
     */
    public Map<Integer, Boolean> getTemplateUsageMap() {
        String hql = "SELECT t.id AS templateId, "
                + " CASE WHEN EXISTS (SELECT 1 FROM Process p WHERE p.template.id = t.id) "
                + " THEN true ELSE false END AS isUsed "
                + " FROM Template t";
        List<Object[]> results = dao.getProjectionByQuery(hql, Collections.emptyMap());
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0], // templateId
                        row -> (Boolean) row[1]  // isUsed
                ));
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM Template");
    }

    @Override
    public Long countResults(Map<?, String> filtersNotImplemented) throws DAOException {
        BeanQuery beanQuery = new BeanQuery(Template.class);
        beanQuery.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        if (!this.showInactiveTemplates) {
            beanQuery.addBooleanRestriction("active", Boolean.TRUE);
        }
        return count(beanQuery.formCountQuery(), beanQuery.getQueryParameters());
    }

    @Override
    public List<Template> loadData(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<?, String> filtersNotImplemented) throws DAOException {
        BeanQuery beanQuery = new BeanQuery(Template.class);
        beanQuery.restrictToClient(ServiceManager.getUserService().getSessionClientId());
        if (!this.showInactiveTemplates) {
            beanQuery.addBooleanRestriction("active", Boolean.TRUE);
        }
        beanQuery.defineSorting(SORT_FIELD_MAPPING.getOrDefault(sortField, sortField), sortOrder);
        return getByQuery(beanQuery.formQueryForAll(), beanQuery.getQueryParameters(), first, pageSize);
    }

    /**
     * Returns all process templates that can still be assigned to a project.
     * Returns the process templates that
     * <ul>
     * <li>are active</li>
     * <li>and that are not already assigned to the project to be
     * edited<sup>✻</sup>,</li>
     * <li>and that belong to the client for which the logged-in user is
     * currently working.</li>
     * </ul>
     * These are displayed in the templateAddPopup.
     * 
     * <p>
     * {T} := currentUser.currentClient.processTemplates[active] -
     * projectEdited.processTemplates
     *
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * ✻) If the project has already been saved. If not, if {@code projectID} is
     * {@code null}, returns all active process templates for the current
     * client.
     * 
     * @param projectId
     *            ID of project which is going to be edited. May be
     *            {@code null}.
     * @return process templates that can be assigned
     */
    public List<Template> findAllAvailableForAssignToProject(Integer projectId) throws DAOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        List<Template> templates = getByQuery("FROM Template WHERE client_id = :sessionClientId AND active = true",
            parameters);
        if (Objects.nonNull(projectId)) {
            List<Template> assigned = ServiceManager.getProjectService().getById(projectId).getTemplates();
            templates.removeAll(assigned);
        }
        return templates;
    }

    /**
     * Duplicate the template with the given ID 'itemId'.
     *
     * @return the duplicated Template
     */
    public Template duplicateTemplate(Template baseTemplate) {
        Template duplicatedTemplate = new Template();

        duplicatedTemplate.setTitle(baseTemplate.getTitle() + "_" + Helper.generateRandomString(3));
        duplicatedTemplate.setCreationDate(new Date());
        duplicatedTemplate.setClient(baseTemplate.getClient());
        duplicatedTemplate.setDocket(baseTemplate.getDocket());
        duplicatedTemplate.setRuleset(baseTemplate.getRuleset());
        // tasks don't need to be duplicated - will be created out of copied workflow
        duplicatedTemplate.setWorkflow(baseTemplate.getWorkflow());

        // TODO: make sure if copy should be assigned automatically to all projects
        for (Project project : baseTemplate.getProjects()) {
            duplicatedTemplate.getProjects().add(project);
            project.getTemplates().add(duplicatedTemplate);
        }

        return duplicatedTemplate;
    }

    /**
     * Determines all process templates with a specific docket.
     *
     * @param docketId
     *            record number of the docket
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    public Collection<?> findByDocket(int docketId) throws DAOException {
        Map<String, Object> parameters = Collections.singletonMap("docketId", docketId);
        return getByQuery("FROM Template WHERE docket_id = :docketId", parameters, 1);
    }

    /**
     * Determines all process templates with a specific ruleset.
     *
     * @param rulesetId
     *            record number of the ruleset
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    /*
     * Used in RulesetForm to find out whether a ruleset is used in a process
     * template. (Then it may not be deleted.) Is only checked for isEmpty().
     */
    public Collection<?> findByRuleset(int rulesetId) throws DAOException {
        Map<String, Object> parameters = Collections.singletonMap("rulesetId", rulesetId);
        return getByQuery("FROM Template WHERE ruleset_id = :rulesetId", parameters, 1);
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram(String fileName) {
        if (Objects.nonNull(fileName) && !fileName.isEmpty()) {
            File tasksDiagram = new File(ConfigCore.getKitodoDiagramDirectory(), fileName + ".svg");
            try {
                return new FileInputStream(tasksDiagram);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
                return getEmptyInputStream();
            }
        }
        return getEmptyInputStream();
    }

    private InputStream getEmptyInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }
        };
    }

    /**
     * Check for unreachable tasks. Unreachable task is this one which has no roles
     * assigned to itself. Other possibility is that given list is empty. It means
     * that whole workflow is unreachable.
     *
     * @param tasks
     *            list of tasks for check
     */
    public void checkForUnreachableTasks(List<Task> tasks) throws ProcessGenerationException {
        if (tasks.isEmpty()) {
            throw new ProcessGenerationException(Helper.getTranslation("noStepsInWorkflow"));
        }
        for (Task task : tasks) {
            if (ServiceManager.getTaskService().getRolesSize(task) == 0) {
                throw new ProcessGenerationException(
                        Helper.getTranslation("noUserInStep", task.getTitle()));
            }
        }
    }

    /**
     * Check whether the tasks assigned to template are complete. If it contains
     * tasks that are not assigned to a user or user group - tasks are not complete.
     *
     * @param list
     *            list of tasks for testing
     * @return true or false
     */
    boolean hasCompleteTasks(List<Task> list) {
        if (list.isEmpty()) {
            return false;
        }
        for (Task task : list) {
            if (task.getRolesSize() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets whether to display non-active production templates in the list.
     *
     * <p>
     * <b>API Note:</b><br>
     * Affects the results of functions {@link #count()} and
     * {@link #loadData(int, int, String, SortOrder, Map)} in
     * {@link SearchDatabaseServiceInterface}.
     * 
     * @param showInactiveTemplates
     *            as boolean
     */
    /*
     * Here, a value is set that affects the generally specified functions
     * countResults() and loadData() in SearchDatabaseServiceInterface. However,
     * in DatabaseProjectServiceInterface and DatabaseTaskServiceInterface, an
     * additional functions countResults() and loadData() are specified with
     * additional parameters, and the generally specified functions
     * countResults() and loadData() from SearchDatabaseServiceInterface are not
     * used. This could be equalized at some point in the future.
     */
    public void setShowInactiveTemplates(boolean showInactiveTemplates) {
        this.showInactiveTemplates = showInactiveTemplates;
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
     * Returns all process templates of the specified client and name.
     *
     * @param title
     *            naming of the projects
     * @param clientId
     *            record number of the client whose projects are queried
     * @return all process templates of the specified client and name
     */
    public List<Template> getTemplatesWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT t FROM Template AS t INNER JOIN t.client AS c WITH c.id = :clientId WHERE t.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }
}

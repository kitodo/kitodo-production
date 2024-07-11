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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseTemplateServiceInterface;
import org.primefaces.model.SortOrder;

public class TemplateService extends SearchDatabaseService<Template, TemplateDAO>
        implements DatabaseTemplateServiceInterface {

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

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Template");
    }

    @Override
    public Long countResults(Map<?, String> filters) throws DataException {
        try {
            Map<String, Object> parameters = Collections.singletonMap("sessionClientId",
                ServiceManager.getUserService().getSessionClientId());
            return countDatabaseRows(this.showInactiveTemplates
                    ? "SELECT COUNT(*) FROM Template WHERE client_id = :sessionClientId"
                    : "SELECT COUNT(*) FROM Template WHERE client_id = :sessionClientId AND active = 1",
                parameters);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Template> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        String desiredOrder = SORT_FIELD_MAPPING.get(sortField) + ' ' + SORT_ORDER_MAPPING.get(sortOrder);
        return getByQuery("FROM Template WHERE client_id = :sessionClientId "
                + (this.showInactiveTemplates ? "" : "AND active = 1 ") + "ORDER BY " + desiredOrder,
                parameters, first, pageSize);
    }

    @Override
    public List<Template> findAllAvailableForAssignToProject(Integer projectId) throws DataException {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
            List<Template> templates = getByQuery("FROM Template WHERE client_id = :sessionClientId AND active = 1",
                parameters);
            if (Objects.nonNull(projectId)) {
                List<Template> assigned = ServiceManager.getProjectService().getById(projectId).getTemplates();
                templates.removeAll(assigned);
            }
            return templates;
        } catch (DAOException e) {
            throw new DataException(e);
        }
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

    @Override
    public Collection<?> findByDocket(int docketId) throws DataException {
        Map<String, Object> parameters = Collections.singletonMap("docketId", docketId);
        return getByQuery("FROM Template WHERE docket_id = :docketId", parameters, 1);
    }

    @Override
    public Collection<?> findByRuleset(int rulesetId) throws DataException {
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
    boolean hasCompleteTasks(List<? extends TaskInterface> list) {
        if (list.isEmpty()) {
            return false;
        }
        for (TaskInterface task : list) {
            if (task.getRolesSize() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
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

    @Override
    public List<Template> getTemplatesWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT t FROM Template AS t INNER JOIN t.client AS c WITH c.id = :clientId WHERE t.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }
}

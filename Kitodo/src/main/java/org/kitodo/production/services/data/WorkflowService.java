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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkflowDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkflowType;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.WorkflowDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchService;
import org.primefaces.model.SortOrder;

public class WorkflowService extends SearchService<Workflow, WorkflowDTO, WorkflowDAO> {

    private static WorkflowService instance = null;

    /**
     * Private constructor with Searcher and Indexer assigning.
     */
    private WorkflowService() {
        super(new WorkflowDAO(), new WorkflowType(), new Indexer<>(Workflow.class), new Searcher(Workflow.class));
    }

    /**
     * Return singleton variable of type WorkflowService.
     *
     * @return unique instance of WorkflowService
     */
    public static WorkflowService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (WorkflowService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new WorkflowService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Workflow");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Workflow WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return searcher.countDocuments(getWorkflowsForCurrentUserQuery());
    }

    @Override
    public List<WorkflowDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return convertJSONObjectsToDTOs(
            searcher.findDocuments(getWorkflowsForCurrentUserQuery(), getSort(sortField, sortOrder), first, pageSize),
            false);
    }

    @Override
    public List<Workflow> getAllNotIndexed() {
        return getByQuery("FROM Workflow WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Workflow> getAllForSelectedClient() {
        return dao.getByQuery("SELECT w FROM Workflow AS w INNER JOIN w.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public WorkflowDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject workflowJSONObject = jsonObject.getJsonObject("_source");
        workflowDTO.setTitle(WorkflowTypeField.TITLE.getStringValue(workflowJSONObject));
        workflowDTO.setFileName(WorkflowTypeField.FILE_NAME.getStringValue(workflowJSONObject));
        workflowDTO.setReady(WorkflowTypeField.READY.getBooleanValue(workflowJSONObject));
        workflowDTO.setActive(WorkflowTypeField.ACTIVE.getBooleanValue(workflowJSONObject));
        return workflowDTO;
    }

    private String getWorkflowsForCurrentUserQuery() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(WorkflowTypeField.CLIENT_ID.getKey(),
            ServiceManager.getUserService().getSessionClientId(), true));
        return query.toString();
    }

    /**
     * Duplicate the given workflow.
     *
     * @param baseWorkflow
     *            to copy
     * @return the duplicated Workflow
     */
    public Workflow duplicateWorkflow(Workflow baseWorkflow) {
        Workflow duplicatedWorkflow = new Workflow();

        // Workflow _title_ should explicitly _not_ be duplicated!
        duplicatedWorkflow.setFileName(baseWorkflow.getFileName() + "_" + Helper.generateRandomString(3));
        duplicatedWorkflow.setActive(baseWorkflow.isActive());
        duplicatedWorkflow.setReady(false);
        duplicatedWorkflow.setClient(baseWorkflow.getClient());

        return duplicatedWorkflow;
    }

    /**
     * Get workflows for given title and file name.
     * 
     * @param title
     *            as String
     * @param file
     *            as String
     * @return list of Workflow objects, desired is that only 1 or 0 workflows are
     *         returned
     */
    public List<Workflow> getWorkflowsForTitleAndFile(String title, String file) {
        return dao.getByTitleAndFile(title, file);
    }

    /**
     * Get available workflows - available means that workflow is active, ready and
     * assigned to selected session client.
     *
     * @return list of available Workflow objects
     */
    public List<Workflow> getAvailableWorkflows() {
        return dao.getAvailableWorkflows(ServiceManager.getUserService().getSessionClientId());
    }
}

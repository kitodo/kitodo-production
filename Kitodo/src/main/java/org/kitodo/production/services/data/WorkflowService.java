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

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;
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
import org.kitodo.production.services.data.base.ClientSearchService;
import org.primefaces.model.SortOrder;

public class WorkflowService extends ClientSearchService<Workflow, WorkflowDTO, WorkflowDAO> {

    private static volatile WorkflowService instance = null;

    /**
     * Private constructor with Searcher and Indexer assigning.
     */
    private WorkflowService() {
        super(new WorkflowDAO(), new WorkflowType(), new Indexer<>(Workflow.class), new Searcher(Workflow.class),
                WorkflowTypeField.CLIENT_ID.getKey());
    }

    /**
     * Return singleton variable of type WorkflowService.
     *
     * @return unique instance of WorkflowService
     */
    public static WorkflowService getInstance() {
        WorkflowService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (WorkflowService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new WorkflowService();
                    instance = localReference;
                }
            }
        }
        return localReference;
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
        return countDocuments(getWorkflowsForCurrentUserQuery());
    }

    @Override
    public List<WorkflowDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(getWorkflowsForCurrentUserQuery(), getSortBuilder(sortField, sortOrder), first, pageSize,
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
    public WorkflowDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setId(getIdFromJSONObject(jsonObject));
        workflowDTO.setTitle(WorkflowTypeField.TITLE.getStringValue(jsonObject));
        workflowDTO.setStatus(WorkflowTypeField.STATUS.getStringValue(jsonObject));
        return workflowDTO;
    }

    private QueryBuilder getWorkflowsForCurrentUserQuery() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(WorkflowTypeField.CLIENT_ID.getKey(),
            ServiceManager.getUserService().getSessionClientId(), true));
        return query;
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

        duplicatedWorkflow.setTitle(baseWorkflow.getTitle() + "_" + Helper.generateRandomString(3));
        duplicatedWorkflow.setStatus(WorkflowStatus.DRAFT);
        duplicatedWorkflow.setClient(baseWorkflow.getClient());

        return duplicatedWorkflow;
    }

    /**
     * Get available workflows - available means that workflow has status active
     * and is assigned to selected session client.
     *
     * @return list of available Workflow objects
     */
    public List<Workflow> getAvailableWorkflows() {
        return dao.getAvailableWorkflows(ServiceManager.getUserService().getSessionClientId());
    }

    /**
     * Get all workflows with status 'active'.
     * @return a list of active workflows
     */
    public List<Workflow> getAllActiveWorkflows() {
        return dao.getAllActive();
    }
}

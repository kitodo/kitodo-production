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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkflowDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseWorkflowServiceInterface;
import org.primefaces.model.SortOrder;

public class WorkflowService extends SearchDatabaseService<Workflow, WorkflowDAO>
        implements DatabaseWorkflowServiceInterface {

    private static final Map<String, String> SORT_FIELD_MAPPING;

    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("active", "active");
    }

    private static volatile WorkflowService instance = null;

    /**
     * Private constructor with Searcher and Indexer assigning.
     */
    private WorkflowService() {
        super(new WorkflowDAO());
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
    public Long countResults(Map<?, String> filters) throws DataException {
        try {
            Map<String, Object> parameters = Collections.singletonMap("sessionClientId",
                ServiceManager.getUserService().getSessionClientId());
            return countDatabaseRows("SELECT COUNT(*) FROM Workflow WHERE client_id = :sessionClientId", parameters);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Workflow> loadData(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<?, String> filters) throws DataException {
        Map<String, Object> parameters = new HashMap<>(7);
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        String desiredOrder = SORT_FIELD_MAPPING.get(sortField) + ' ' + SORT_ORDER_MAPPING.get(sortOrder);
        return getByQuery("FROM Workflow WHERE client_id = :sessionClientId ORDER BY ".concat(desiredOrder), parameters,
            first, pageSize);
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

    @Override
    public List<Workflow> getAvailableWorkflows() {
        return dao.getAvailableWorkflows(ServiceManager.getUserService().getSessionClientId());
    }

    @Override
    public List<Workflow> getAllActiveWorkflows() {
        return dao.getAllActive();
    }
}

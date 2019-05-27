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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.WorkflowCondition;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkflowConditionDAO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class WorkflowConditionService extends SearchDatabaseService<WorkflowCondition, WorkflowConditionDAO> {

    private static volatile WorkflowConditionService instance = null;

    /**
     * Return singleton variable of type WorkflowConditionService.
     *
     * @return unique instance of WorkflowConditionService
     */
    public static WorkflowConditionService getInstance() {
        WorkflowConditionService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (WorkflowConditionService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new WorkflowConditionService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Constructor.
     */
    private WorkflowConditionService() {
        super(new WorkflowConditionDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM WorkflowCondition");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    public List<WorkflowCondition> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        throw new UnsupportedOperationException();
    }
}

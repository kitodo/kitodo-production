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

import java.util.List;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkflowDAO;
import org.kitodo.services.data.base.SearchDatabaseService;

public class WorkflowService extends SearchDatabaseService<Workflow, WorkflowDAO> {

    /**
     * Public constructor.
     */
    public WorkflowService() {
        super(new WorkflowDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Workflow");
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
}

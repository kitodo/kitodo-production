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

package org.kitodo.production.services.ocrd;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.OcrdWorkflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.OcrdWorkflowDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class OcrdWorkflowService extends SearchDatabaseService<OcrdWorkflow, OcrdWorkflowDAO> {

    private static volatile OcrdWorkflowService instance = null;

    /**
     * Constructor necessary to use searcher in child classes.
     */
    private OcrdWorkflowService() {
        super(new OcrdWorkflowDAO());
    }

    /**
     * Return singleton variable of type OCRWorkflowService.
     *
     * @return unique instance of OCRWorkflowService
     */
    public static OcrdWorkflowService getInstance() {
        OcrdWorkflowService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (OcrdWorkflowService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new OcrdWorkflowService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }


    @Override
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) throws DataException {
        return null;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return null;
    }

    @Override
    public Long countResults(Map filters) throws DAOException, DataException {
        return null;
    }

    /**
     * Get available ocr workflows - available means that ocr workflow has status active
     * and is assigned to selected session client.
     *
     * @return list of available OCRWorkflow objects
     */
    public List<OcrdWorkflow> getAvailableOcrdWorkflows() {
        return dao.getAvailableOcrdWorkflows(ServiceManager.getUserService().getSessionClientId());
    }

}

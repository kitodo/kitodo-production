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
import java.util.Objects;

import javax.json.JsonObject;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkflowDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkflowType;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.dto.WorkflowDTO;
import org.kitodo.services.data.base.SearchService;

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
    public WorkflowDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject workflowJSONObject = jsonObject.getJsonObject("_source");
        workflowDTO.setTitle(workflowJSONObject.getString(WorkflowTypeField.TITLE.getName()));
        workflowDTO.setFileName(workflowJSONObject.getString(WorkflowTypeField.FILE_NAME.getName()));
        workflowDTO.setReady(workflowJSONObject.getBoolean(WorkflowTypeField.READY.getName()));
        workflowDTO.setActive(workflowJSONObject.getBoolean(WorkflowTypeField.ACTIVE.getName()));
        return new WorkflowDTO();
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

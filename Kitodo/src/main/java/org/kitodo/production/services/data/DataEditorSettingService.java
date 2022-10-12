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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DataEditorSettingDAO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class DataEditorSettingService extends SearchDatabaseService<DataEditorSetting, DataEditorSettingDAO> {

    private static volatile DataEditorSettingService instance = null;

    /**
     * Constructor.
     */
    private DataEditorSettingService() {
        super(new DataEditorSettingDAO());
    }

    /**
     * Return signleton variable of type DataEditorSettingService.
     *
     * @return unique instance of DataEditorSettingService
     */
    public static DataEditorSettingService getInstance() {
        DataEditorSettingService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (DataEditorSettingService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new DataEditorSettingService();
                    instance = localReference;
                }
            }
        }
        return localReference;

    }

    @Override
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM DataEditorSetting");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    /**
     * Check if there are data editor settings for the tasks of the current workflow.
     *
     * @return true if one of the tasks has data editor settings defined
     */
    public boolean areDataEditorSettingsDefinedForWorkflow(Workflow workflow) {
        String query =
            "SELECT d FROM DataEditorSetting AS d"
            + " INNER JOIN Task AS ta ON ta.id = d.taskId"
            + " INNER JOIN Template AS te ON te.id = ta.template"
            + " INNER JOIN Workflow AS w ON w.id = te.workflow"
            + " where w.id = :workflowId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("workflowId", workflow.getId());
        List<DataEditorSetting> dataEditorSettings = getByQuery(query, parameters);
        return !dataEditorSettings.isEmpty();
    }

    /**
     * Delete data editor settings identified by task id. 
     * @param taskId ID of the associated task
     * @throws DAOException if data editor setting could not be deleted from database
     * 
     */
    public void removeFromDatabaseByTaskId(int taskId) throws DAOException {
        List<DataEditorSetting> dataEditorSettings = getByTaskId(taskId);
        for (DataEditorSetting dataEditorSetting: dataEditorSettings) {
            dao.remove(dataEditorSetting.getId());
        }
    }

    /**
     * Retrieve data editor settings by task id.
     * @param taskId ID of the task
     *
     * @return List of DataEditorSetting objects
     */
    public List<DataEditorSetting> getByTaskId(int taskId) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("taskId", taskId);
        return getByQuery("FROM DataEditorSetting WHERE task_id = :taskId ORDER BY id ASC", parameterMap);
    }
    
    private List<DataEditorSetting> getByUserAndTask(int userId, int taskId) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("userId", userId);
        parameterMap.put("taskId", taskId);
        return getByQuery("FROM DataEditorSetting WHERE user_id = :userId AND task_id = :taskId ORDER BY id ASC", parameterMap);
    }

    /**
     * Load DataEditorSetting from database or return null if no entry matches the specified ids.
     * @param userId id of the user
     * @param taskId id of the corresponding template task for the task that is currently edited
     * @return settings for the data editor
     */
    public DataEditorSetting loadDataEditorSetting(int userId, int taskId) {
        List<DataEditorSetting> results = getByUserAndTask(userId, taskId);
        if (Objects.nonNull(results) && !results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }


}

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
import org.primefaces.model.SortOrder;

public class DataEditorSettingService extends BaseBeanService<DataEditorSetting, DataEditorSettingDAO> {

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
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM DataEditorSetting");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return count();
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
            + " INNER JOIN Template AS te ON te.id = ta.template.id"
            + " INNER JOIN Workflow AS w ON w.id = te.workflow.id"
            + " where w.id = :workflowId";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("workflowId", workflow.getId());
        List<DataEditorSetting> dataEditorSettings = getByQuery(query, parameters);
        return !dataEditorSettings.isEmpty();
    }

    /**
     * Delete data editor settings identified by task id. 
     * @param taskId ID of the associated task (or null for task-independent default layout)
     * @throws DAOException if data editor setting could not be deleted from database
     * 
     */
    public void removeFromDatabaseByTaskId(Integer taskId) throws DAOException {
        List<DataEditorSetting> dataEditorSettings = getByTaskId(taskId);
        for (DataEditorSetting dataEditorSetting: dataEditorSettings) {
            dao.remove(dataEditorSetting.getId());
        }
    }

    /**
     * Retrieve data editor settings by task id.
     * @param taskId ID of the task (or null for task-independent default layout)
     *
     * @return List of DataEditorSetting objects
     */
    public List<DataEditorSetting> getByTaskId(Integer taskId) {
        Map<String, Object> parameterMap = new HashMap<>();
        if (Objects.nonNull(taskId)) {
            parameterMap.put("taskId", taskId);
            return getByQuery("FROM DataEditorSetting WHERE taskId = :taskId ORDER BY id ASC", parameterMap);
        }
        return getByQuery("FROM DataEditorSetting WHERE taskId is NULL ORDER BY id ASC", parameterMap);
    }
    
    private List<DataEditorSetting> getByUserAndTask(int userId, Integer taskId) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("userId", userId);
        if (Objects.nonNull(taskId)) {
            parameterMap.put("taskId", taskId);
            return getByQuery(
                "FROM DataEditorSetting WHERE userId = :userId AND taskId = :taskId ORDER BY id ASC", parameterMap
            );
        }
        return getByQuery(
            "FROM DataEditorSetting WHERE userId = :userId AND taskId IS NULL ORDER BY id ASC", parameterMap
        );
    }

    /**
     * Load DataEditorSetting from database or return null if no entry matches the specified ids.
     * @param userId id of the user
     * @param taskId id of the corresponding template task for the task that is currently edited 
     *               (or null for task-independent default layout)
     * @return settings for the data editor
     */
    public DataEditorSetting loadDataEditorSetting(int userId, Integer taskId) {
        List<DataEditorSetting> results = getByUserAndTask(userId, taskId);
        if (Objects.nonNull(results) && !results.isEmpty()) {
            return results.getFirst();
        }
        return null;
    }


}

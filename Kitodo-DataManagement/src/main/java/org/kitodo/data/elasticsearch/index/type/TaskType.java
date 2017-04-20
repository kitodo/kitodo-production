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

package org.kitodo.data.elasticsearch.index.type;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;

/**
 * Implementation of Task Type.
 */
public class TaskType extends BaseType<Task> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Task task) {
        JSONObject taskObject = new JSONObject();
        taskObject.put("title", task.getTitle());
        taskObject.put("priority", task.getPriority());
        taskObject.put("ordering", task.getOrdering());
        String processingStatus = task.getProcessingStatusEnum() != null ? task.getProcessingStatusEnum().toString()
                : null;
        taskObject.put("processingStatus", processingStatus);
        String processingTime = task.getProcessingTime() != null ? formatDate(task.getProcessingTime()) : null;
        taskObject.put("processingTime", processingTime);
        String processingBegin = task.getProcessingBegin() != null ? formatDate(task.getProcessingBegin()) : null;
        taskObject.put("processingBegin", processingBegin);
        String processingEnd = task.getProcessingEnd() != null ? formatDate(task.getProcessingEnd()) : null;
        taskObject.put("processingEnd", processingEnd);
        taskObject.put("homeDirectory", String.valueOf(task.getHomeDirectory()));
        taskObject.put("typeMetadata", String.valueOf(task.isTypeMetadata()));
        taskObject.put("typeAutomatic", String.valueOf(task.isTypeAutomatic()));
        taskObject.put("typeImportFileUpload", String.valueOf(task.isTypeImportFileUpload()));
        taskObject.put("typeExportRussian", String.valueOf(task.isTypeExportRussian()));
        taskObject.put("typeImagesRead", String.valueOf(task.isTypeImagesRead()));
        taskObject.put("typeImagesWrite", String.valueOf(task.isTypeImagesWrite()));
        taskObject.put("batchStep", String.valueOf(task.isBatchStep()));
        Integer processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId() : null;
        taskObject.put("processingUser", processingUser);
        Integer process = task.getProcess() != null ? task.getProcess().getId() : null;
        taskObject.put("process", process);
        taskObject.put("users", addUserRelation(task.getUsers()));
        taskObject.put("userGroups", addUserGroupRelation(task.getUserGroups()));

        return new NStringEntity(taskObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}

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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Task;

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
        Integer processingStatus = task.getProcessingStatusEnum() != null ? task.getProcessingStatusEnum().getValue()
                : null;
        taskObject.put("processingStatus", processingStatus);
        Integer editType = task.getEditTypeEnum() != null ? task.getEditTypeEnum().getValue()
                : null;
        taskObject.put("editType", editType);
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
        taskObject.put("typeModuleName", task.getTypeModuleName());
        taskObject.put("batchStep", String.valueOf(task.isBatchStep()));
        Integer processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId() : null;
        taskObject.put("processingUser", processingUser);
        Integer process = task.getProcess() != null ? task.getProcess().getId() : null;
        taskObject.put("process", process);
        taskObject.put("users", addObjectRelation(task.getUsers()));
        taskObject.put("userGroups", addObjectRelation(task.getUserGroups()));

        return new NStringEntity(taskObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}

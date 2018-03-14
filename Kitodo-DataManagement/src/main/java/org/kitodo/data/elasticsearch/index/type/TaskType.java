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

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Task;

/**
 * Implementation of Task Type.
 */
public class TaskType extends BaseType<Task> {

    @Override
    public HttpEntity createDocument(Task task) {
        Integer processingStatus = task.getProcessingStatusEnum() != null ? task.getProcessingStatusEnum().getValue()
                : 0;
        Integer editType = task.getEditTypeEnum() != null ? task.getEditTypeEnum().getValue() : 0;
        Integer processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId() : 0;
        Integer processId = task.getProcess() != null ? task.getProcess().getId() : 0;
        String processTitle = task.getProcess() != null ? task.getProcess().getTitle() : "";

        JsonObject taskObject = Json.createObjectBuilder()
                .add("title", preventNull(task.getTitle()))
                .add("priority", task.getPriority())
                .add("ordering", task.getOrdering())
                .add("processingStatus", processingStatus)
                .add("editType", editType)
                .add("processingTime", getFormattedDate(task.getProcessingTime()))
                .add("processingBegin", getFormattedDate(task.getProcessingBegin()))
                .add("processingEnd", getFormattedDate(task.getProcessingEnd()))
                .add("homeDirectory", preventNull(String.valueOf(task.getHomeDirectory())))
                .add("typeMetadata", task.isTypeMetadata())
                .add("typeAutomatic", task.isTypeAutomatic())
                .add("typeImportFileUpload", task.isTypeImportFileUpload())
                .add("typeExportRussian", task.isTypeExportRussian())
                .add("typeImagesRead", task.isTypeImagesRead())
                .add("typeImagesWrite", task.isTypeImagesWrite())
                .add("batchStep", task.isBatchStep())
                .add("processingUser", processingUser)
                .add("processForTask.id", processId)
                .add("processForTask.title", processTitle)
                .add("users", addObjectRelation(task.getUsers()))
                .add("userGroups", addObjectRelation(task.getUserGroups()))
                .build();

        return new NStringEntity(taskObject.toString(), ContentType.APPLICATION_JSON);
    }
}

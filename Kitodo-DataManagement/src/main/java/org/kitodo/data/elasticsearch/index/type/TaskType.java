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
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Task;

/**
 * Implementation of Task Type.
 */
public class TaskType extends BaseType<Task> {

    @Override
    JsonObject getJsonObject(Task task) {
        Integer processingStatus = task.getProcessingStatusEnum() != null ? task.getProcessingStatusEnum().getValue()
                : 0;
        Integer editType = task.getEditTypeEnum() != null ? task.getEditTypeEnum().getValue() : 0;
        Integer processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId() : 0;
        Integer processId = task.getProcess() != null ? task.getProcess().getId() : 0;
        String processTitle = task.getProcess() != null ? task.getProcess().getTitle() : "";
        Integer templateId = task.getTemplate() != null ? task.getTemplate().getId() : 0;
        String templateTitle = task.getTemplate() != null ? task.getTemplate().getTitle() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("title", preventNull(task.getTitle()));
        jsonObjectBuilder.add("priority", task.getPriority());
        jsonObjectBuilder.add("ordering", task.getOrdering());
        jsonObjectBuilder.add("processingStatus", processingStatus);
        jsonObjectBuilder.add("editType", editType);
        jsonObjectBuilder.add("processingTime", getFormattedDate(task.getProcessingTime()));
        jsonObjectBuilder.add("processingBegin", getFormattedDate(task.getProcessingBegin()));
        jsonObjectBuilder.add("processingEnd", getFormattedDate(task.getProcessingEnd()));
        jsonObjectBuilder.add("homeDirectory", preventNull(String.valueOf(task.getHomeDirectory())));
        jsonObjectBuilder.add("typeMetadata", task.isTypeMetadata());
        jsonObjectBuilder.add("typeAutomatic", task.isTypeAutomatic());
        jsonObjectBuilder.add("typeImportFileUpload", task.isTypeImportFileUpload());
        jsonObjectBuilder.add("typeExportRussian", task.isTypeExportRussian());
        jsonObjectBuilder.add("typeImagesRead", task.isTypeImagesRead());
        jsonObjectBuilder.add("typeImagesWrite", task.isTypeImagesWrite());
        jsonObjectBuilder.add("batchStep", task.isBatchStep());
        jsonObjectBuilder.add("processingUser", processingUser);
        jsonObjectBuilder.add("processForTask.id", processId);
        jsonObjectBuilder.add("processForTask.title", processTitle);
        jsonObjectBuilder.add("templateForTask.id", templateId);
        jsonObjectBuilder.add("templateForTask.title", templateTitle);
        jsonObjectBuilder.add("users", addObjectRelation(task.getUsers()));
        jsonObjectBuilder.add("userGroups", addObjectRelation(task.getUserGroups()));
        return jsonObjectBuilder.build();
    }
}

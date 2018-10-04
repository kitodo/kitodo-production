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
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;

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
        jsonObjectBuilder.add(TaskTypeField.TITLE.getKey(), preventNull(task.getTitle()));
        jsonObjectBuilder.add(TaskTypeField.PRIORITY.getKey(), task.getPriority());
        jsonObjectBuilder.add(TaskTypeField.ORDERING.getKey(), task.getOrdering());
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus);
        jsonObjectBuilder.add(TaskTypeField.EDIT_TYPE.getKey(), editType);
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_TIME.getKey(), getFormattedDate(task.getProcessingTime()));
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_BEGIN.getKey(), getFormattedDate(task.getProcessingBegin()));
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_END.getKey(), getFormattedDate(task.getProcessingEnd()));
        jsonObjectBuilder.add(TaskTypeField.HOME_DIRECTORY.getKey(), preventNull(String.valueOf(task.getHomeDirectory())));
        jsonObjectBuilder.add(TaskTypeField.TYPE_METADATA.getKey(), task.isTypeMetadata());
        jsonObjectBuilder.add(TaskTypeField.TYPE_AUTOMATIC.getKey(), task.isTypeAutomatic());
        jsonObjectBuilder.add(TaskTypeField.TYPE_IMAGES_READ.getKey(), task.isTypeImagesRead());
        jsonObjectBuilder.add(TaskTypeField.TYPE_IMAGES_WRITE.getKey(), task.isTypeImagesWrite());
        jsonObjectBuilder.add(TaskTypeField.BATCH_STEP.getKey(), task.isBatchStep());
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_USER.getKey(), processingUser);
        jsonObjectBuilder.add(TaskTypeField.PROCESS_ID.getKey(), processId);
        jsonObjectBuilder.add(TaskTypeField.PROCESS_TITLE.getKey(), processTitle);
        jsonObjectBuilder.add(TaskTypeField.TEMPLATE_ID.getKey(), templateId);
        jsonObjectBuilder.add(TaskTypeField.TEMPLATE_TITLE.getKey(), templateTitle);
        jsonObjectBuilder.add(TaskTypeField.USERS.getKey(), addObjectRelation(task.getUsers()));
        jsonObjectBuilder.add(TaskTypeField.USER_GROUPS.getKey(), addObjectRelation(task.getUserGroups()));
        return jsonObjectBuilder.build();
    }
}

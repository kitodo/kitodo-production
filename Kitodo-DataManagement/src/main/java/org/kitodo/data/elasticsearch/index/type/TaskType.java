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
        jsonObjectBuilder.add(TaskTypeField.TITLE.getName(), preventNull(task.getTitle()));
        jsonObjectBuilder.add(TaskTypeField.PRIORITY.getName(), task.getPriority());
        jsonObjectBuilder.add(TaskTypeField.ORDERING.getName(), task.getOrdering());
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_STATUS.getName(), processingStatus);
        jsonObjectBuilder.add(TaskTypeField.EDIT_TYPE.getName(), editType);
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_TIME.getName(), getFormattedDate(task.getProcessingTime()));
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_BEGIN.getName(), getFormattedDate(task.getProcessingBegin()));
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_END.getName(), getFormattedDate(task.getProcessingEnd()));
        jsonObjectBuilder.add(TaskTypeField.HOME_DIRECTORY.getName(), preventNull(String.valueOf(task.getHomeDirectory())));
        jsonObjectBuilder.add(TaskTypeField.TYPE_METADATA.getName(), task.isTypeMetadata());
        jsonObjectBuilder.add(TaskTypeField.TYPE_AUTOMATIC.getName(), task.isTypeAutomatic());
        jsonObjectBuilder.add(TaskTypeField.TYPE_IMPORT_FILE_UPLOAD.getName(), task.isTypeImportFileUpload());
        jsonObjectBuilder.add(TaskTypeField.TYPE_EXPORT_RUSSIAN.getName(), task.isTypeExportRussian());
        jsonObjectBuilder.add(TaskTypeField.TYPE_IMAGES_READ.getName(), task.isTypeImagesRead());
        jsonObjectBuilder.add(TaskTypeField.TYPE_IMAGES_WRITE.getName(), task.isTypeImagesWrite());
        jsonObjectBuilder.add(TaskTypeField.BATCH_STEP.getName(), task.isBatchStep());
        jsonObjectBuilder.add(TaskTypeField.PROCESSING_USER.getName(), processingUser);
        jsonObjectBuilder.add(TaskTypeField.PROCESS_ID.getName(), processId);
        jsonObjectBuilder.add(TaskTypeField.PROCESS_TITLE.getName(), processTitle);
        jsonObjectBuilder.add(TaskTypeField.TEMPLATE_ID.getName(), templateId);
        jsonObjectBuilder.add(TaskTypeField.TEMPLATE_TITLE.getName(), templateTitle);
        jsonObjectBuilder.add(TaskTypeField.USERS.getName(), addObjectRelation(task.getUsers()));
        jsonObjectBuilder.add(TaskTypeField.USER_GROUPS.getName(), addObjectRelation(task.getUserGroups()));
        return jsonObjectBuilder.build();
    }
}

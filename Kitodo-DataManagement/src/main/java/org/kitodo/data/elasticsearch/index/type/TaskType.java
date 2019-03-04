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

import java.util.HashMap;
import java.util.Map;

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;

/**
 * Implementation of Task Type.
 */
public class TaskType extends BaseType<Task> {

    @Override
    Map<String, Object> getJsonObject(Task task) {
        int processingStatus = task.getProcessingStatus() != null ? task.getProcessingStatus().getValue() : 0;
        int editType = task.getEditType() != null ? task.getEditType().getValue() : 0;
        int processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId() : 0;

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(TaskTypeField.TITLE.getKey(), preventNull(task.getTitle()));
        jsonObject.put(TaskTypeField.PRIORITY.getKey(), task.getPriority());
        jsonObject.put(TaskTypeField.ORDERING.getKey(), task.getOrdering());
        jsonObject.put(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus);
        jsonObject.put(TaskTypeField.EDIT_TYPE.getKey(), editType);
        jsonObject.put(TaskTypeField.PROCESSING_TIME.getKey(), getFormattedDate(task.getProcessingTime()));
        jsonObject.put(TaskTypeField.PROCESSING_BEGIN.getKey(), getFormattedDate(task.getProcessingBegin()));
        jsonObject.put(TaskTypeField.PROCESSING_END.getKey(), getFormattedDate(task.getProcessingEnd()));
        jsonObject.put(TaskTypeField.HOME_DIRECTORY.getKey(), preventNull(String.valueOf(task.getHomeDirectory())));
        jsonObject.put(TaskTypeField.TYPE_METADATA.getKey(), task.isTypeMetadata());
        jsonObject.put(TaskTypeField.TYPE_AUTOMATIC.getKey(), task.isTypeAutomatic());
        jsonObject.put(TaskTypeField.TYPE_IMAGES_READ.getKey(), task.isTypeImagesRead());
        jsonObject.put(TaskTypeField.TYPE_IMAGES_WRITE.getKey(), task.isTypeImagesWrite());
        jsonObject.put(TaskTypeField.BATCH_STEP.getKey(), task.isBatchStep());
        jsonObject.put(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUser);
        if (processingUser > 0) {
            User user = task.getProcessingUser();
            jsonObject.put(TaskTypeField.PROCESSING_USER_LOGIN.getKey(), user.getLogin());
            jsonObject.put(TaskTypeField.PROCESSING_USER_NAME.getKey(), user.getName());
            jsonObject.put(TaskTypeField.PROCESSING_USER_SURNAME.getKey(), user.getSurname());
        } else {
            jsonObject.put(TaskTypeField.PROCESSING_USER_LOGIN.getKey(),"");
            jsonObject.put(TaskTypeField.PROCESSING_USER_NAME.getKey(), "");
            jsonObject.put(TaskTypeField.PROCESSING_USER_SURNAME.getKey(), "");
        }
        jsonObject.put(TaskTypeField.PROCESS_ID.getKey(), getId(task.getProcess()));
        jsonObject.put(TaskTypeField.PROCESS_TITLE.getKey(), getTitle(task.getProcess()));
        jsonObject.put(TaskTypeField.TEMPLATE_ID.getKey(), getId(task.getTemplate()));
        jsonObject.put(TaskTypeField.TEMPLATE_TITLE.getKey(), getTitle(task.getTemplate()));
        jsonObject.put(TaskTypeField.ROLES.getKey(), addObjectRelation(task.getRoles()));
        return jsonObject;
    }
}

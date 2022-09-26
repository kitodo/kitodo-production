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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.converter.ProcessConverter;
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
        Date processCreationDate = Objects.isNull(task.getProcess()) ? null : task.getProcess().getCreationDate();

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(TaskTypeField.TITLE.getKey(), preventNull(task.getTitle()));
        jsonObject.put(TaskTypeField.ORDERING.getKey(), task.getOrdering());
        jsonObject.put(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus);
        jsonObject.put(TaskTypeField.EDIT_TYPE.getKey(), editType);
        jsonObject.put(TaskTypeField.PROCESSING_TIME.getKey(), getFormattedDate(task.getProcessingTime()));
        jsonObject.put(TaskTypeField.PROCESSING_BEGIN.getKey(), getFormattedDate(task.getProcessingBegin()));
        jsonObject.put(TaskTypeField.PROCESSING_END.getKey(), getFormattedDate(task.getProcessingEnd()));
        jsonObject.put(TaskTypeField.HOME_DIRECTORY.getKey(), preventNull(String.valueOf(task.getHomeDirectory())));
        jsonObject.put(TaskTypeField.CORRECTION.getKey(), task.isCorrection());
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
            jsonObject.put(TaskTypeField.PROCESSING_USER_FULLNAME.getKey(), user.getFullName());
        } else {
            jsonObject.put(TaskTypeField.PROCESSING_USER_LOGIN.getKey(),"");
            jsonObject.put(TaskTypeField.PROCESSING_USER_NAME.getKey(), "");
            jsonObject.put(TaskTypeField.PROCESSING_USER_SURNAME.getKey(), "");
            jsonObject.put(TaskTypeField.PROCESSING_USER_FULLNAME.getKey(), "");
        }
        jsonObject.put(TaskTypeField.PROCESS_ID.getKey(), getId(task.getProcess()));
        jsonObject.put(TaskTypeField.PROCESS_TITLE.getKey(), getTitle(task.getProcess()));
        jsonObject.put(TaskTypeField.PROCESS_CREATION_DATE.getKey(), getFormattedDate(processCreationDate));
        jsonObject.put(TaskTypeField.CLIENT_ID.getKey(), getClientId(task));
        jsonObject.put(TaskTypeField.PROJECT_ID.getKey(), getProjectId(task));
        jsonObject.put(TaskTypeField.PROJECT_TITLE.getKey(), getProjectTitle(task));
        jsonObject.put(TaskTypeField.RELATED_PROJECT_IDS.getKey(), getRelatedProjectIds(task));
        jsonObject.put(TaskTypeField.TEMPLATE_ID.getKey(), getId(task.getTemplate()));
        jsonObject.put(TaskTypeField.TEMPLATE_TITLE.getKey(), getTitle(task.getTemplate()));
        jsonObject.put(TaskTypeField.ROLES.getKey(), addObjectRelation(task.getRoles()));
        jsonObject.put(
            TaskTypeField.CORRECTION_COMMENT_STATUS.getKey(), 
            ProcessConverter.getCorrectionCommentStatus(task.getProcess()).getValue()
        );
        return jsonObject;
    }

    /**
     * Returns related project ids for a task.
     * 
     * <p>This includes both the project id of the task's process, 
     * but also the project ids of the task's templates.</p>
     * 
     * <p>Presumbly, this is used to be able to filter tasks based on the 
     * the projects the current user is allowed to see, see TaskService.</p>
     * 
     * @param task the task
     * @return the list of related project ids
     */
    private List<Integer> getRelatedProjectIds(Task task) {
        ArrayList<Integer> projectIds = new ArrayList<>();

        if (Objects.nonNull(task.getProcess())) {
            projectIds.add(getId(task.getProcess().getProject()));
            return projectIds;
        }
        if (Objects.nonNull(task.getTemplate())) {
            List<Project> projects = task.getTemplate().getProjects();
            for (Project project : projects) {
                projectIds.add(getId(project));
            }
            return projectIds;
        }
        return projectIds;
    }

    /**
     * Extracts the project id from a task if it has a process.
     * 
     * @param task the task
     * @return the project id or null
     */
    private Integer getProjectId(Task task) {
        if (Objects.nonNull(task.getProcess())) {
            return getId(task.getProcess().getProject());
        }
        return null;
    }

    /**
     * Extracts the project title from a task if it has a process.
     * 
     * @param task the task
     * @return the project title or null
     */
    private String getProjectTitle(Task task) {
        if (Objects.nonNull(task.getProcess())) {
            return getTitle(task.getProcess().getProject());
        }
        return null;
    }

    private int getClientId(Task task) {
        if (Objects.nonNull(task.getProcess()) && Objects.nonNull(task.getProcess().getProject())) {
            return getId(task.getProcess().getProject().getClient());
        }
        if (Objects.nonNull(task.getTemplate()) && Objects.nonNull(task.getTemplate().getClient())) {
            return getId(task.getTemplate().getClient());
        }
        return 0;
    }

}

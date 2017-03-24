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

package org.kitodo.data.index.elasticsearch.type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
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

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        LinkedHashMap<String, String> orderedTaskMap = new LinkedHashMap<>();
        orderedTaskMap.put("title", task.getTitle());
        String priority = task.getPriority() != null ? task.getPriority().toString() : "null";
        orderedTaskMap.put("priority", priority);
        String ordering = task.getOrdering() != null ? task.getOrdering().toString() : "null";
        orderedTaskMap.put("ordering", ordering);
        String processingStatus = task.getProcessingStatusEnum() != null ? task.getProcessingStatusEnum().toString()
                : "null";
        orderedTaskMap.put("processingStatus", processingStatus);
        String processingTime = task.getProcessingTime() != null ? dateFormat.format(task.getProcessingTime()) : null;
        orderedTaskMap.put("processingTime", processingTime);
        String processingBegin = task.getProcessingBegin() != null ? dateFormat.format(task.getProcessingBegin())
                : null;
        orderedTaskMap.put("processingBegin", processingBegin);
        String processingEnd = task.getProcessingEnd() != null ? dateFormat.format(task.getProcessingEnd()) : null;
        orderedTaskMap.put("processingEnd", processingEnd);
        orderedTaskMap.put("homeDirectory", String.valueOf(task.getHomeDirectory()));
        orderedTaskMap.put("typeMetadata", String.valueOf(task.isTypeMetadata()));
        orderedTaskMap.put("typeAutomatic", String.valueOf(task.isTypeAutomatic()));
        orderedTaskMap.put("typeImportFileUpload", String.valueOf(task.isTypeImportFileUpload()));
        orderedTaskMap.put("typeExportRussian", String.valueOf(task.isTypeExportRussian()));
        orderedTaskMap.put("typeImagesRead", String.valueOf(task.isTypeImagesRead()));
        orderedTaskMap.put("typeImagesWrite", String.valueOf(task.isTypeImagesWrite()));
        orderedTaskMap.put("batchStep", String.valueOf(task.isBatchStep()));
        String processingUser = task.getProcessingUser() != null ? task.getProcessingUser().getId().toString() : "null";
        orderedTaskMap.put("processingUser", processingUser);
        String process = task.getProcess() != null ? task.getProcess().getId().toString() : "null";
        orderedTaskMap.put("process", process);

        JSONObject taskObject = new JSONObject(orderedTaskMap);

        JSONArray users = new JSONArray();
        List<User> taskUsers = task.getUsers();
        for (User user : taskUsers) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("id", user.getId());
            users.add(propertyObject);
        }
        taskObject.put("users", users);

        JSONArray userGroups = new JSONArray();
        List<UserGroup> taskUserGroups = task.getUserGroups();
        for (UserGroup userGroup : taskUserGroups) {
            JSONObject userGroupObject = new JSONObject();
            userGroupObject.put("id", userGroup.getId().toString());
            userGroups.add(userGroupObject);
        }
        taskObject.put("userGroups", userGroups);

        return new NStringEntity(taskObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}

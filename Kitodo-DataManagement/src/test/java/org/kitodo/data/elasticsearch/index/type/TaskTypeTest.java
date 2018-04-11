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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserGroupTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Test class for TaskType.
 */
public class TaskTypeTest {

    private static List<Task> prepareData() {

        List<Task> tasks = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<UserGroup> userGroups = new ArrayList<>();

        Process process = new Process();
        process.setTitle("First");
        process.setId(1);

        User firstUser = new User();
        firstUser.setId(1);
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        users.add(secondUser);

        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setId(1);
        userGroups.add(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setId(2);
        userGroups.add(secondUserGroup);

        Task firstTask = new Task();
        firstTask.setId(1);
        firstTask.setTitle("Testing");
        firstTask.setPriority(1);
        firstTask.setOrdering(1);
        firstTask.setProcessingStatusEnum(TaskStatus.DONE);
        firstTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        LocalDate localDate = new LocalDate(2017, 2, 17);
        firstTask.setProcessingTime(localDate.toDate());
        localDate = new LocalDate(2017, 2, 1);
        firstTask.setProcessingBegin(localDate.toDate());
        localDate = new LocalDate(2017, 2, 17);
        firstTask.setProcessingEnd(localDate.toDate());
        firstTask.setHomeDirectory((short) 1);
        firstTask.setTypeMetadata(true);
        firstTask.setTypeAutomatic(false);
        firstTask.setBatchStep(true);
        firstTask.setProcessingUser(users.get(0));
        firstTask.setProcess(process);
        firstTask.setUsers(users);
        firstTask.setUserGroups(userGroups);
        tasks.add(firstTask);

        Task secondTask = new Task();
        secondTask.setId(2);
        secondTask.setTitle("Rendering");
        secondTask.setPriority(2);
        secondTask.setOrdering(2);
        secondTask.setProcessingStatusEnum(TaskStatus.INWORK);
        localDate = new LocalDate(2017, 2, 17);
        secondTask.setProcessingTime(localDate.toDate());
        localDate = new LocalDate(2017, 2, 10);
        secondTask.setProcessingBegin(localDate.toDate());
        secondTask.setProcessingUser(users.get(1));
        secondTask.setUsers(users);
        secondTask.setUserGroups(userGroups);
        tasks.add(secondTask);

        Task thirdTask = new Task();
        thirdTask.setId(3);
        thirdTask.setTitle("Incomplete");
        thirdTask.setProcessingStatusEnum(TaskStatus.OPEN);
        tasks.add(thirdTask);

        return tasks;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(0);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Testing",
            actual.getString(TaskTypeField.TITLE.getName()));
        assertEquals("Key ordering doesn't match to given value!", 1, actual.getInt(TaskTypeField.ORDERING.getName()));
        assertEquals("Key priority doesn't match to given value!", 1, actual.getInt(TaskTypeField.PRIORITY.getName()));
        assertEquals("Key editType doesn't match to given value!", 1, actual.getInt(TaskTypeField.EDIT_TYPE.getName()));
        assertEquals("Key processingStatus doesn't match to given value!", 3,
            actual.getInt(TaskTypeField.PROCESSING_STATUS.getName()));
        assertEquals("Key processingUser doesn't match to given value!", 1,
            actual.getInt(TaskTypeField.PROCESSING_USER.getName()));
        assertEquals("Key processingBegin doesn't match to given value!", "2017-02-01",
            actual.getString(TaskTypeField.PROCESSING_BEGIN.getName()));
        assertEquals("Key processingEnd doesn't match to given value!", "2017-02-17",
            actual.getString(TaskTypeField.PROCESSING_END.getName()));
        assertEquals("Key processingTime doesn't match to given value!", "2017-02-17",
            actual.getString(TaskTypeField.PROCESSING_TIME.getName()));
        assertEquals("Key homeDirectory doesn't match to given value!", "1",
            actual.getString(TaskTypeField.HOME_DIRECTORY.getName()));
        assertEquals("Key batchStep doesn't match to given value!", true,
            actual.getBoolean(TaskTypeField.BATCH_STEP.getName()));
        assertEquals("Key typeAutomatic doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_AUTOMATIC.getName()));
        assertEquals("Key typeMetadata doesn't match to given value!", true,
            actual.getBoolean(TaskTypeField.TYPE_METADATA.getName()));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMPORT_FILE_UPLOAD.getName()));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_WRITE.getName()));
        assertEquals("Key typeImagesRead doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_READ.getName()));
        assertEquals("Key typeExportRussian doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_EXPORT_RUSSIAN.getName()));
        assertEquals("Key processForTask.id doesn't match to given value!", 1,
            actual.getInt(TaskTypeField.PROCESS_ID.getName()));
        assertEquals("Key processForTask.title doesn't match to given value!", "First",
            actual.getString(TaskTypeField.PROCESS_TITLE.getName()));

        JsonArray users = actual.getJsonArray(TaskTypeField.USERS.getName());
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt(UserTypeField.ID.getName()));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt(UserTypeField.ID.getName()));

        JsonArray userGroups = actual.getJsonArray(TaskTypeField.USER_GROUPS.getName());
        assertEquals("Size users doesn't match to given value!", 2, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1,
            userGroup.getInt(UserGroupTypeField.ID.getName()));

        userGroup = userGroups.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2,
            userGroup.getInt(UserGroupTypeField.ID.getName()));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(1);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering",
            actual.getString(TaskTypeField.TITLE.getName()));
        assertEquals("Key ordering doesn't match to given value!", 2, actual.getInt(TaskTypeField.ORDERING.getName()));
        assertEquals("Key priority doesn't match to given value!", 2, actual.getInt(TaskTypeField.PRIORITY.getName()));
        assertEquals("Key editType doesn't match to given value!", 0, actual.getInt(TaskTypeField.EDIT_TYPE.getName()));
        assertEquals("Key processingStatus doesn't match to given value!", 2,
            actual.getInt(TaskTypeField.PROCESSING_STATUS.getName()));
        assertEquals("Key processingUser doesn't match to given value!", 2,
            actual.getInt(TaskTypeField.PROCESSING_USER.getName()));
        assertEquals("Key processingBegin doesn't match to given value!", "2017-02-10",
            actual.getString(TaskTypeField.PROCESSING_BEGIN.getName()));
        assertEquals("Key processingEnd doesn't match to given value!", JsonValue.NULL,
            actual.get(TaskTypeField.PROCESSING_END.getName()));
        assertEquals("Key processingTime doesn't match to given value!", "2017-02-17",
            actual.getString(TaskTypeField.PROCESSING_TIME.getName()));
        assertEquals("Key homeDirectory doesn't match to given value!", "0",
            actual.getString(TaskTypeField.HOME_DIRECTORY.getName()));
        assertEquals("Key batchStep doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.BATCH_STEP.getName()));
        assertEquals("Key typeAutomatic doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_AUTOMATIC.getName()));
        assertEquals("Key typeMetadata doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_METADATA.getName()));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMPORT_FILE_UPLOAD.getName()));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_WRITE.getName()));
        assertEquals("Key typeImagesRead doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_READ.getName()));
        assertEquals("Key typeExportRussian doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_EXPORT_RUSSIAN.getName()));
        assertEquals("Key processForTask.id doesn't match to given value!", 0,
            actual.getInt(TaskTypeField.PROCESS_ID.getName()));
        assertEquals("Key processForTask.title doesn't match to given value!", "",
            actual.getString(TaskTypeField.PROCESS_TITLE.getName()));

        JsonArray users = actual.getJsonArray(TaskTypeField.USERS.getName());
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt(UserTypeField.ID.getName()));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt(UserTypeField.ID.getName()));

        JsonArray userGroups = actual.getJsonArray(TaskTypeField.USER_GROUPS.getName());
        assertEquals("Size users doesn't match to given value!", 2, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1,
            userGroup.getInt(UserGroupTypeField.ID.getName()));

        userGroup = userGroups.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2,
            userGroup.getInt(UserGroupTypeField.ID.getName()));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(2);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete",
            actual.getString(TaskTypeField.TITLE.getName()));
        assertEquals("Key ordering doesn't match to given value!", 0, actual.getInt(TaskTypeField.ORDERING.getName()));
        assertEquals("Key priority doesn't match to given value!", 0, actual.getInt(TaskTypeField.PRIORITY.getName()));
        assertEquals("Key editType doesn't match to given value!", 0, actual.getInt(TaskTypeField.EDIT_TYPE.getName()));
        assertEquals("Key processingStatus doesn't match to given value!", 1,
            actual.getInt(TaskTypeField.PROCESSING_STATUS.getName()));
        assertEquals("Key processingUser doesn't match to given value!", 0,
            actual.getInt(TaskTypeField.PROCESSING_USER.getName()));
        assertEquals("Key processingBegin doesn't match to given value!", JsonValue.NULL,
            actual.get(TaskTypeField.PROCESSING_BEGIN.getName()));
        assertEquals("Key processingEnd doesn't match to given value!", JsonValue.NULL,
            actual.get(TaskTypeField.PROCESSING_END.getName()));
        assertEquals("Key processingTime doesn't match to given value!", JsonValue.NULL,
            actual.get(TaskTypeField.PROCESSING_TIME.getName()));
        assertEquals("Key homeDirectory doesn't match to given value!", "0",
            actual.getString(TaskTypeField.HOME_DIRECTORY.getName()));
        assertEquals("Key batchStep doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.BATCH_STEP.getName()));
        assertEquals("Key typeAutomatic doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_AUTOMATIC.getName()));
        assertEquals("Key typeMetadata doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_METADATA.getName()));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMPORT_FILE_UPLOAD.getName()));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_WRITE.getName()));
        assertEquals("Key typeImagesRead doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_IMAGES_READ.getName()));
        assertEquals("Key typeExportRussian doesn't match to given value!", false,
            actual.getBoolean(TaskTypeField.TYPE_EXPORT_RUSSIAN.getName()));
        assertEquals("Key processForTask.id doesn't match to given value!", 0,
            actual.getInt(TaskTypeField.PROCESS_ID.getName()));
        assertEquals("Key processForTask.title doesn't match to given value!", "",
            actual.getString(TaskTypeField.PROCESS_TITLE.getName()));

        JsonArray users = actual.getJsonArray(TaskTypeField.USERS.getName());
        assertEquals("Size users doesn't match to given value!", 0, users.size());

        JsonArray userGroups = actual.getJsonArray(TaskTypeField.USER_GROUPS.getName());
        assertEquals("Size users doesn't match to given value!", 0, userGroups.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(0);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 23, actual.keySet().size());

        JsonArray users = actual.getJsonArray(TaskTypeField.USERS.getName());
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 1, user.keySet().size());

        JsonArray userGroups = actual.getJsonArray(TaskTypeField.USER_GROUPS.getName());
        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Amount of keys in userGroups is incorrect!", 1, userGroup.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        TaskType taskType = new TaskType();

        List<Task> tasks = prepareData();
        HashMap<Integer, HttpEntity> documents = taskType.createDocuments(tasks);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}

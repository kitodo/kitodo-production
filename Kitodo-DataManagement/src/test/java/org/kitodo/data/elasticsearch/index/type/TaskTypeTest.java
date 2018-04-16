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

        assertEquals("Key title doesn't match to given value!", "Testing", actual.getString("title"));
        assertEquals("Key ordering doesn't match to given value!", 1, actual.getInt("ordering"));
        assertEquals("Key priority doesn't match to given value!", 1, actual.getInt("priority"));
        assertEquals("Key editType doesn't match to given value!", 1, actual.getInt("editType"));
        assertEquals("Key processingStatus doesn't match to given value!", 3, actual.getInt("processingStatus"));
        assertEquals("Key processingUser doesn't match to given value!", 1, actual.getInt("processingUser"));
        assertEquals("Key processingBegin doesn't match to given value!", "2017-02-01", actual.getString("processingBegin"));
        assertEquals("Key processingEnd doesn't match to given value!", "2017-02-17", actual.getString("processingEnd"));
        assertEquals("Key processingTime doesn't match to given value!", "2017-02-17", actual.getString("processingTime"));
        assertEquals("Key homeDirectory doesn't match to given value!", "1", actual.getString("homeDirectory"));
        assertEquals("Key batchStep doesn't match to given value!", true, actual.getBoolean("batchStep"));
        assertEquals("Key typeAutomatic doesn't match to given value!", false, actual.getBoolean("typeAutomatic"));
        assertEquals("Key typeMetadata doesn't match to given value!", true, actual.getBoolean("typeMetadata"));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false, actual.getBoolean("typeImportFileUpload"));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false, actual.getBoolean("typeImagesWrite"));
        assertEquals("Key typeImagesRead doesn't match to given value!", false, actual.getBoolean("typeImagesRead"));
        assertEquals("Key typeExportRussian doesn't match to given value!", false, actual.getBoolean("typeExportRussian"));
        assertEquals("Key processForTask.id doesn't match to given value!", 1, actual.getInt("processForTask.id"));
        assertEquals("Key processForTask.title doesn't match to given value!", "First", actual.getString("processForTask.title"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt("id"));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt("id"));

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size users doesn't match to given value!", 2, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, userGroup.getInt("id"));

        userGroup = userGroups.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, userGroup.getInt("id"));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(1);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering", actual.getString("title"));
        assertEquals("Key ordering doesn't match to given value!", 2, actual.getInt("ordering"));
        assertEquals("Key priority doesn't match to given value!", 2, actual.getInt("priority"));
        assertEquals("Key editType doesn't match to given value!", 0, actual.getInt("editType"));
        assertEquals("Key processingStatus doesn't match to given value!", 2, actual.getInt("processingStatus"));
        assertEquals("Key processingUser doesn't match to given value!", 2, actual.getInt("processingUser"));
        assertEquals("Key processingBegin doesn't match to given value!", "2017-02-10", actual.getString("processingBegin"));
        assertEquals("Key processingEnd doesn't match to given value!", JsonValue.NULL, actual.get("processingEnd"));
        assertEquals("Key processingTime doesn't match to given value!", "2017-02-17", actual.getString("processingTime"));
        assertEquals("Key homeDirectory doesn't match to given value!", "0", actual.getString("homeDirectory"));
        assertEquals("Key batchStep doesn't match to given value!", false, actual.getBoolean("batchStep"));
        assertEquals("Key typeAutomatic doesn't match to given value!", false, actual.getBoolean("typeAutomatic"));
        assertEquals("Key typeMetadata doesn't match to given value!", false, actual.getBoolean("typeMetadata"));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false, actual.getBoolean("typeImportFileUpload"));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false, actual.getBoolean("typeImagesWrite"));
        assertEquals("Key typeImagesRead doesn't match to given value!", false, actual.getBoolean("typeImagesRead"));
        assertEquals("Key typeExportRussian doesn't match to given value!", false, actual.getBoolean("typeExportRussian"));
        assertEquals("Key processForTask.id doesn't match to given value!", 0, actual.getInt("processForTask.id"));
        assertEquals("Key processForTask.title doesn't match to given value!", "", actual.getString("processForTask.title"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt("id"));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt("id"));

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size users doesn't match to given value!", 2, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, userGroup.getInt("id"));

        userGroup = userGroups.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, userGroup.getInt("id"));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(2);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete", actual.getString("title"));
        assertEquals("Key ordering doesn't match to given value!", 0, actual.getInt("ordering"));
        assertEquals("Key priority doesn't match to given value!", 0, actual.getInt("priority"));
        assertEquals("Key editType doesn't match to given value!", 0, actual.getInt("editType"));
        assertEquals("Key processingStatus doesn't match to given value!", 1, actual.getInt("processingStatus"));
        assertEquals("Key processingUser doesn't match to given value!", 0, actual.getInt("processingUser"));
        assertEquals("Key processingBegin doesn't match to given value!", JsonValue.NULL, actual.get("processingBegin"));
        assertEquals("Key processingEnd doesn't match to given value!", JsonValue.NULL, actual.get("processingEnd"));
        assertEquals("Key processingTime doesn't match to given value!", JsonValue.NULL, actual.get("processingTime"));
        assertEquals("Key homeDirectory doesn't match to given value!", "0", actual.getString("homeDirectory"));
        assertEquals("Key batchStep doesn't match to given value!", false, actual.getBoolean("batchStep"));
        assertEquals("Key typeAutomatic doesn't match to given value!", false, actual.getBoolean("typeAutomatic"));
        assertEquals("Key typeMetadata doesn't match to given value!", false, actual.getBoolean("typeMetadata"));
        assertEquals("Key typeImportFileUpload doesn't match to given value!", false, actual.getBoolean("typeImportFileUpload"));
        assertEquals("Key typeImagesWrite doesn't match to given value!", false, actual.getBoolean("typeImagesWrite"));
        assertEquals("Key typeImagesRead doesn't match to given value!", false, actual.getBoolean("typeImagesRead"));
        assertEquals("Key typeExportRussian doesn't match to given value!", false, actual.getBoolean("typeExportRussian"));
        assertEquals("Key processForTask.id doesn't match to given value!", 0, actual.getInt("processForTask.id"));
        assertEquals("Key processForTask.title doesn't match to given value!", "", actual.getString("processForTask.title"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 0, users.size());

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size users doesn't match to given value!", 0, userGroups.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        TaskType taskType = new TaskType();

        Task task = prepareData().get(0);
        HttpEntity document = taskType.createDocument(task);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 23, actual.keySet().size());

        JsonArray users = actual.getJsonArray("users");
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 1, user.keySet().size());

        JsonArray userGroups = actual.getJsonArray("userGroups");
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

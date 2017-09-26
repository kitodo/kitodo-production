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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
    public void shouldCreateDocument() throws Exception {
        TaskType taskType = new TaskType();
        JSONParser parser = new JSONParser();

        Task task = prepareData().get(0);
        HttpEntity document = taskType.createDocument(task);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser.parse("{\"title\":\"Testing\",\"processForTask.id\":1,"
                + "\"processForTask.title\":\"First\",\"homeDirectory\":\"1\",\"typeAutomatic\":false,"
                + "\"ordering\":1,\"typeMetadata\":true,\"priority\":1,\"typeImportFileUpload\":false,"
                + "\"processingTime\":\"2017-02-17\",\"processingBegin\":\"2017-02-01\",\"batchStep\":true,"
                + "\"users\":[{\"id\":1},{\"id\":2}],\"processingUser\":1,\"processingStatus\":3,"
                + "\"userGroups\":[{\"id\":1},{\"id\":2}],\"editType\":1,\"typeImagesWrite\":false,"
                + "\"processingEnd\":\"2017-02-17\",\"typeImagesRead\":false,\"typeExportRussian\":false,"
                + "\"typeModuleName\":null}");

        assertEquals("Task JSONObject doesn't match to given JSONObject!", expected, actual);

        task = prepareData().get(1);
        document = taskType.createDocument(task);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Rendering\",\"processForTask.id\":null,"
                + "\"processForTask.title\":null,\"homeDirectory\":\"0\",\"typeAutomatic\":false,\"ordering\":2,"
                + "\"typeMetadata\":false,\"title\":\"Rendering\",\"priority\":2,\"typeImportFileUpload\":false,"
                + "\"processingTime\":\"2017-02-17\",\"processingBegin\":\"2017-02-10\",\"batchStep\":false,"
                + "\"users\":[{\"id\":1},{\"id\":2}],\"processingUser\":2,\"processingStatus\":2,\"editType\":0,"
                + "\"userGroups\":[{\"id\":1},{\"id\":2}],\"typeImagesWrite\":false,\"processingEnd\":null,"
                + "\"typeImagesRead\":false,\"typeExportRussian\":false,\"typeModuleName\":null}");
        assertEquals("Task JSONObject doesn't match to given JSONObject!", expected, actual);

        task = prepareData().get(2);
        document = taskType.createDocument(task);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Incomplete\",\"processForTask.id\":null,"
                + "\"processForTask.title\":null,\"homeDirectory\":\"0\",\"typeAutomatic\":false,\"ordering\":0,"
                + "\"typeMetadata\":false,\"priority\":0,\"typeImportFileUpload\":false,"
                + "\"processingTime\":null,\"processingBegin\":null,\"batchStep\":false,\"users\":[],"
                + "\"processingUser\":null,\"processingStatus\":1,\"userGroups\":[],\"editType\":0,"
                + "\"typeImagesWrite\":false,\"processingEnd\":null,\"typeImagesRead\":false,"
                + "\"typeExportRussian\":false,\"typeModuleName\":null}");
        assertEquals("Task JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        TaskType taskType = new TaskType();

        List<Task> tasks = prepareData();
        HashMap<Integer, HttpEntity> documents = taskType.createDocuments(tasks);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.User;

/**
 * Test class for ProjectType.
 */
public class ProjectTypeTest {

    private static List<Project> prepareData() {

        List<Project> projects = new ArrayList<>();
        List<ProjectFileGroup> projectFileGroups = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<User> users = new ArrayList<>();

        ProjectFileGroup firstProjectFileGroup = new ProjectFileGroup();
        firstProjectFileGroup.setName("MAX");
        firstProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstProjectFileGroup.setMimeType("image/jpeg");
        firstProjectFileGroup.setSuffix("jpg");
        firstProjectFileGroup.setPreviewImage(false);
        projectFileGroups.add(firstProjectFileGroup);

        ProjectFileGroup secondProjectFileGroup = new ProjectFileGroup();
        secondProjectFileGroup.setName("DEFAULT");
        secondProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondProjectFileGroup.setMimeType("image/jpeg");
        secondProjectFileGroup.setSuffix("jpg");
        secondProjectFileGroup.setPreviewImage(false);
        projectFileGroups.add(secondProjectFileGroup);

        ProjectFileGroup thirdProjectFileGroup = new ProjectFileGroup();
        thirdProjectFileGroup.setName("THUMBS");
        thirdProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdProjectFileGroup.setMimeType("image/jpeg");
        thirdProjectFileGroup.setSuffix("jpg");
        thirdProjectFileGroup.setPreviewImage(false);
        projectFileGroups.add(thirdProjectFileGroup);

        ProjectFileGroup fourthProjectFileGroup = new ProjectFileGroup();
        fourthProjectFileGroup.setName("FULLTEXT");
        fourthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthProjectFileGroup.setMimeType("text/xml");
        fourthProjectFileGroup.setSuffix("xml");
        fourthProjectFileGroup.setPreviewImage(false);
        projectFileGroups.add(fourthProjectFileGroup);

        ProjectFileGroup fifthProjectFileGroup = new ProjectFileGroup();
        fifthProjectFileGroup.setName("DOWNLOAD");
        fifthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthProjectFileGroup.setMimeType("application/pdf");
        fifthProjectFileGroup.setSuffix("pdf");
        fifthProjectFileGroup.setPreviewImage(false);
        projectFileGroups.add(fifthProjectFileGroup);

        Process firstProcess = new Process();
        firstProcess.setId(1);
        firstProcess.setTitle("First");
        firstProcess.setTemplate(true);
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(2);
        secondProcess.setTitle("Second");
        secondProcess.setTemplate(true);
        processes.add(secondProcess);

        User firstUser = new User();
        firstUser.setId(1);
        firstUser.setLogin("first");
        firstUser.setName("Tic");
        firstUser.setSurname("Tac");
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        secondUser.setLogin("second");
        secondUser.setName("Ted");
        secondUser.setSurname("Barney");
        users.add(secondUser);

        Client client = new Client();
        client.setId(1);
        client.setName("TestClient");

        Project firstProject = new Project();
        firstProject.setId(1);
        firstProject.setTitle("Testing");
        LocalDate localDate = new LocalDate(2017, 1, 1);
        firstProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017, 3, 1);
        firstProject.setEndDate(localDate.toDate());
        firstProject.setNumberOfPages(100);
        firstProject.setNumberOfVolumes(10);
        firstProject.setProcesses(processes);
        firstProject.setProjectFileGroups(projectFileGroups);
        firstProject.setUsers(users);
        firstProject.setClient(client);
        projects.add(firstProject);

        Project secondProject = new Project();
        secondProject.setId(2);
        secondProject.setTitle("Rendering");
        localDate = new LocalDate(2017, 1, 10);
        secondProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017, 9, 10);
        secondProject.setEndDate(localDate.toDate());
        secondProject.setNumberOfPages(2000);
        secondProject.setNumberOfVolumes(20);
        secondProject.setProcesses(processes);
        secondProject.setProjectFileGroups(projectFileGroups);
        secondProject.setUsers(users);
        projects.add(secondProject);

        Project thirdProject = new Project();
        thirdProject.setId(3);
        thirdProject.setTitle("Incomplete");
        projects.add(thirdProject);

        return projects;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(0);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Testing", actual.getString("title"));
        assertEquals("Key startDate doesn't match to given value!", "2017-01-01", actual.getString("startDate"));
        assertEquals("Key endDate doesn't match to given value!", "2017-03-01", actual.getString("endDate"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "", actual.getString("metsRightsOwner"));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 10, actual.getInt("numberOfVolumes"));
        assertEquals("Key numberOfPages doesn't match to given value!", 100, actual.getInt("numberOfPages"));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream", actual.getString("fileFormatInternal"));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream", actual.getString("fileFormatDmsExport"));

        assertEquals("Key client.id doesn't match to given value!", 1, actual.getInt("client.id"));
        assertEquals("Key client.clientName doesn't match to given value!", "TestClient", actual.getString("client.clientName"));

        JsonArray processes = actual.getJsonArray("processes");
        assertEquals("Size processes doesn't match to given value!", 2, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 1, process.getInt("id"));
        assertEquals("Key processes.title doesn't match to given value!", "First", process.getString("title"));
        assertEquals("Key processes.template doesn't match to given value!", true, process.getBoolean("template"));

        process = processes.getJsonObject(1);
        assertEquals("Key processes.id doesn't match to given value!", 2, process.getInt("id"));
        assertEquals("Key processes.title doesn't match to given value!", "Second", process.getString("title"));
        assertEquals("Key processes.template doesn't match to given value!", true, process.getBoolean("template"));

        JsonArray projectFileGroups = actual.getJsonArray("projectFileGroups");
        assertEquals("Size projectFileGroups doesn't match to given value!", 5, projectFileGroups.size());

        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "MAX", projectFileGroup.getString("name"));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(1);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DEFAULT", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(2);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "THUMBS", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(3);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "FULLTEXT", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "text/xml", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "xml", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(4);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DOWNLOAD", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "application/pdf", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "pdf", projectFileGroup.getString("suffix"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Tic", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Tac", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "first", user.getString("login"));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Ted", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Barney", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "second", user.getString("login"));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(1);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering", actual.getString("title"));
        assertEquals("Key startDate doesn't match to given value!", "2017-01-10", actual.getString("startDate"));
        assertEquals("Key endDate doesn't match to given value!", "2017-09-10", actual.getString("endDate"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "", actual.getString("metsRightsOwner"));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 20, actual.getInt("numberOfVolumes"));
        assertEquals("Key numberOfPages doesn't match to given value!", 2000, actual.getInt("numberOfPages"));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream", actual.getString("fileFormatInternal"));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream", actual.getString("fileFormatDmsExport"));

        assertEquals("Key client.id doesn't match to given value!", 0, actual.getInt("client.id"));
        assertEquals("Key client.clientName doesn't match to given value!", "", actual.getString("client.clientName"));

        JsonArray processes = actual.getJsonArray("processes");
        assertEquals("Size processes doesn't match to given value!", 2, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 1, process.getInt("id"));
        assertEquals("Key processes.title doesn't match to given value!", "First", process.getString("title"));
        assertEquals("Key processes.template doesn't match to given value!", true, process.getBoolean("template"));

        process = processes.getJsonObject(1);
        assertEquals("Key processes.id doesn't match to given value!", 2, process.getInt("id"));
        assertEquals("Key processes.title doesn't match to given value!", "Second", process.getString("title"));
        assertEquals("Key processes.template doesn't match to given value!", true, process.getBoolean("template"));

        JsonArray projectFileGroups = actual.getJsonArray("projectFileGroups");
        assertEquals("Size projectFileGroups doesn't match to given value!", 5, projectFileGroups.size());

        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "MAX", projectFileGroup.getString("name"));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(1);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DEFAULT", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(2);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "THUMBS", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(3);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "FULLTEXT", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "text/xml", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "xml", projectFileGroup.getString("suffix"));

        projectFileGroup = projectFileGroups.getJsonObject(4);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DOWNLOAD", projectFileGroup.getString("name"));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path, projectFileGroup.getString("path"));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "", projectFileGroup.getString("folder"));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "application/pdf", projectFileGroup.getString("mimeType"));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "pdf", projectFileGroup.getString("suffix"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Tic", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Tac", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "first", user.getString("login"));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Ted", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Barney", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "second", user.getString("login"));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        ProjectType processType = new ProjectType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Project project = prepareData().get(2);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete", actual.getString("title"));
        assertEquals("Key startDate doesn't match to given value!", dateFormat.format(project.getStartDate()), actual.getString("startDate"));
        assertEquals("Key endDate doesn't match to given value!", dateFormat.format(project.getEndDate()), actual.getString("endDate"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "", actual.getString("metsRightsOwner"));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 0, actual.getInt("numberOfVolumes"));
        assertEquals("Key numberOfPages doesn't match to given value!", 0, actual.getInt("numberOfPages"));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream", actual.getString("fileFormatInternal"));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream", actual.getString("fileFormatDmsExport"));

        assertEquals("Key client.id doesn't match to given value!", 0, actual.getInt("client.id"));
        assertEquals("Key client.clientName doesn't match to given value!", "", actual.getString("client.clientName"));

        JsonArray processes = actual.getJsonArray("processes");
        assertEquals("Size processes doesn't match to given value!", 0, processes.size());

        JsonArray projectFileGroups = actual.getJsonArray("projectFileGroups");
        assertEquals("Size projectFileGroups doesn't match to given value!", 0, projectFileGroups.size());

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 0, users.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(0);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        System.out.println(actual);
        assertEquals("Amount of keys is incorrect!", 14, actual.keySet().size());

        JsonArray processes = actual.getJsonArray("processes");
        JsonObject process = processes.getJsonObject(0);
        assertEquals("Amount of keys in processes is incorrect!", 3, process.keySet().size());

        JsonArray projectFileGroups = actual.getJsonArray("projectFileGroups");
        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Amount of keys in projectFileGroups is incorrect!", 5, projectFileGroup.keySet().size());

        JsonArray users = actual.getJsonArray("users");
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 4, user.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        ProjectType processType = new ProjectType();

        List<Project> processes = prepareData();
        HashMap<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}

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
    public void shouldCreateDocument() throws Exception {
        ProjectType processType = new ProjectType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Project project = prepareData().get(0);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        JsonObject expected = Json.createReader(new StringReader("{\"title\":\"Testing\",\"active\":true,"
                + "\"processes\":[{\"id\":1,\"title\":\"First\",\"template\":true},{\"id\":2,\"title\":\"Second\","
                + "\"template\":true}],\"numberOfPages\":100, \"endDate\":\"2017-03-01\",\"metsRightsOwner\":\"\","
                + "\"numberOfVolumes\":10,\"projectFileGroups\":[{\"path\":\"http:\\/\\/www.example.com\\/content\\/$"
                + "(meta.CatalogIDDigital)\\/jpgs\\/max\\/\",\"folder\":\"\",\"name\":\"MAX\",\"mimeType\":"
                + "\"image\\/jpeg\",\"suffix\":\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta."
                + "CatalogIDDigital)\\/jpgs\\/default\\/\",\"folder\":\"\",\"name\":\"DEFAULT\",\"mimeType\":"
                + "\"image\\/jpeg\",\"suffix\":\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta."
                + "CatalogIDDigital)\\/jpgs\\/thumbs\\/\",\"folder\":\"\",\"name\":\"THUMBS\",\"mimeType\":\"image\\/"
                + "jpeg\",\"suffix\":\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta."
                + "CatalogIDDigital)\\/ocr\\/alto\\/\",\"folder\":\"\",\"name\":\"FULLTEXT\",\"mimeType\":\"text\\/"
                + "xml\",\"suffix\":\"xml\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta."
                + "CatalogIDDigital)\\/pdf\\/\",\"folder\":\"\",\"name\":\"DOWNLOAD\",\"mimeType\":\"application\\/"
                + "pdf\",\"suffix\":\"pdf\"}],\"startDate\":\"2017-01-01\",\"fileFormatInternal\":\"XStream\","
                + "\"fileFormatDmsExport\":\"XStream\",\"users\":[{\"surname\":\"Tac\",\"name\":\"Tic\",\"id\":1,"
                + "\"login\":\"first\"},{\"surname\":\"Barney\",\"name\":\"Ted\",\"id\":2,\"login\":\"second\"}]}")).readObject();
        assertEquals("Project JSONObject doesn't match to given JSONObject!", expected, actual);

        project = prepareData().get(1);
        document = processType.createDocument(project);

        actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        expected = Json.createReader(new StringReader("{\"title\":\"Rendering\",\"active\":true,\"processes\":"
                + "[{\"id\":1,\"title\":\"First\",\"template\":true},{\"id\":2,\"title\":\"Second\",\"template\":true}],"
                + "\"numberOfPages\":2000,\"endDate\":\"2017-09-10\",\"numberOfVolumes\":20,\"metsRightsOwner\":\"\","
                + "\"projectFileGroups\":[{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta.CatalogIDDigital"
                + ")\\/jpgs\\/max\\/\",\"folder\":\"\",\"name\":\"MAX\",\"mimeType\":\"image\\/jpeg\",\"suffix\":"
                + "\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta.CatalogIDDigital)"
                + "\\/jpgs\\/default\\/\"," + "\"folder\":\"\",\"name\":\"DEFAULT\",\"mimeType\":\"image\\/jpeg\","
                + "\"suffix\":\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta.CatalogIDDigital)"
                + "\\/jpgs\\/thumbs\\/\",\"folder\":\"\",\"name\":\"THUMBS\",\"mimeType\":\"image\\/jpeg\","
                + "\"suffix\":\"jpg\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta.CatalogIDDigital)"
                + "\\/ocr\\/alto\\/\",\"folder\":\"\",\"name\":\"FULLTEXT\",\"mimeType\":\"text\\/xml\",\"suffix\":"
                + "\"xml\"},{\"path\":\"http:\\/\\/www.example.com\\/content\\/$(meta.CatalogIDDigital)\\/pdf\\/\","
                + "\"folder\":\"\",\"name\":\"DOWNLOAD\",\"mimeType\":\"application\\/pdf\",\"suffix\":\"pdf\"}],"
                + "\"startDate\":\"2017-01-10\",\"fileFormatInternal\":\"XStream\",\"fileFormatDmsExport\":\"XStream\","
                + "\"users\":[{\"surname\":\"Tac\",\"name\":\"Tic\",\"id\":1,\"login\":\"first\"},{\"surname\":"
                + "\"Barney\",\"name\":\"Ted\",\"id\":2,\"login\":\"second\"}]}")).readObject();
        assertEquals("Project JSONObject doesn't match to given JSONObject!", expected, actual);

        project = prepareData().get(2);
        document = processType.createDocument(project);

        actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        expected = Json.createReader(new StringReader("{\"title\":\"Incomplete\",\"active\":true,\"processes\":[],"
                + "\"numberOfPages\":0,\"metsRightsOwner\":\"\",\"endDate\":\"" + dateFormat.format(project.getEndDate())
                + "\",\"numberOfVolumes\":0,\"projectFileGroups\":[],\"startDate\":\""
                + dateFormat.format(project.getEndDate()) + "\",\"fileFormatInternal\":\"XStream\","
                + "\"fileFormatDmsExport\":\"XStream\",\"users\":[]}")).readObject();
        assertEquals("Project JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        ProjectType processType = new ProjectType();

        List<Project> processes = prepareData();
        HashMap<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}

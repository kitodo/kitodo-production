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
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Test class for ProjectType.
 */
public class ProjectTypeTest {

    private static List<Project> prepareData() {

        List<Project> projects = new ArrayList<>();
        List<ProjectFileGroup> projectFileGroups = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<Template> templates = new ArrayList<>();
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

        Template firstTemplate = new Template();
        firstTemplate.setId(1);
        firstTemplate.setTitle("First");
        templates.add(firstTemplate);

        Process firstProcess = new Process();
        firstProcess.setId(2);
        firstProcess.setTitle("Second");
        processes.add(firstProcess);

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
        firstProject.setTemplates(templates);
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
        secondProject.setTemplates(templates);
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

        assertEquals("Key title doesn't match to given value!", "Testing",
            ProjectTypeField.TITLE.getStringValue(actual));
        assertEquals("Key startDate doesn't match to given value!", "2017-01-01",
            ProjectTypeField.START_DATE.getStringValue(actual));
        assertEquals("Key endDate doesn't match to given value!", "2017-03-01",
            ProjectTypeField.END_DATE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", ProjectTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "",
            ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 10,
            ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual));
        assertEquals("Key numberOfPages doesn't match to given value!", 100,
            ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_INTERNAL.getStringValue(actual));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getStringValue(actual));

        assertEquals("Key client.id doesn't match to given value!", 1, ProjectTypeField.CLIENT_ID.getIntValue(actual));
        assertEquals("Key client.clientName doesn't match to given value!", "TestClient",
            ProjectTypeField.CLIENT_NAME.getStringValue(actual));

        JsonArray processes = ProjectTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 1, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 2, ProcessTypeField.ID.getIntValue(process));
        assertEquals("Key processes.title doesn't match to given value!", "Second",
            ProcessTypeField.TITLE.getStringValue(process));

        JsonArray templates = ProjectTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 1, templates.size());

        JsonObject template = templates.getJsonObject(0);
        assertEquals("Key templates.id doesn't match to given value!", 1, TemplateTypeField.ID.getIntValue(template));
        assertEquals("Key templates.title doesn't match to given value!", "First",
            TemplateTypeField.TITLE.getStringValue(template));

        JsonArray projectFileGroups = ProjectTypeField.PROJECT_FILE_GROUPS.getJsonArray(actual);
        assertEquals("Size projectFileGroups doesn't match to given value!", 5, projectFileGroups.size());

        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "MAX",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(1);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DEFAULT",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(2);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "THUMBS",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(3);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "FULLTEXT",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "text/xml",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "xml",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(4);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DOWNLOAD",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "application/pdf",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "pdf",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        JsonArray users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Tic", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Tac",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "first",
            UserTypeField.LOGIN.getStringValue(user));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Ted", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Barney",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "second",
            UserTypeField.LOGIN.getStringValue(user));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(1);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering",
            ProjectTypeField.TITLE.getStringValue(actual));
        assertEquals("Key startDate doesn't match to given value!", "2017-01-10",
            ProjectTypeField.START_DATE.getStringValue(actual));
        assertEquals("Key endDate doesn't match to given value!", "2017-09-10",
            ProjectTypeField.END_DATE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", ProjectTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "",
            ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 20,
            ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual));
        assertEquals("Key numberOfPages doesn't match to given value!", 2000,
            ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_INTERNAL.getStringValue(actual));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getStringValue(actual));

        assertEquals("Key client.id doesn't match to given value!", 0, ProjectTypeField.CLIENT_ID.getIntValue(actual));
        assertEquals("Key client.clientName doesn't match to given value!", "",
            ProjectTypeField.CLIENT_NAME.getStringValue(actual));

        JsonArray processes = ProjectTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 1, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 2, ProcessTypeField.ID.getIntValue(process));
        assertEquals("Key processes.title doesn't match to given value!", "Second",
            ProcessTypeField.TITLE.getStringValue(process));

        JsonArray templates = ProjectTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 1, templates.size());

        JsonObject template = templates.getJsonObject(0);
        assertEquals("Key templates.id doesn't match to given value!", 1, TemplateTypeField.ID.getIntValue(template));
        assertEquals("Key templates.title doesn't match to given value!", "First",
            TemplateTypeField.TITLE.getStringValue(template));

        JsonArray projectFileGroups = ProjectTypeField.PROJECT_FILE_GROUPS.getJsonArray(actual);
        assertEquals("Size projectFileGroups doesn't match to given value!", 5, projectFileGroups.size());

        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "MAX",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(1);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DEFAULT",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(2);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "THUMBS",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "jpg",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(3);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "FULLTEXT",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "text/xml",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "xml",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        projectFileGroup = projectFileGroups.getJsonObject(4);
        assertEquals("Key projectFileGroups.name doesn't match to given value!", "DOWNLOAD",
            ProjectTypeField.PFG_NAME.getStringValue(projectFileGroup));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key projectFileGroups.path doesn't match to given value!", path,
            ProjectTypeField.PFG_PATH.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.folder doesn't match to given value!", "",
            ProjectTypeField.PFG_FOLDER.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.mimeType doesn't match to given value!", "application/pdf",
            ProjectTypeField.PFG_MIME_TYPE.getStringValue(projectFileGroup));
        assertEquals("Key projectFileGroups.suffix doesn't match to given value!", "pdf",
            ProjectTypeField.PFG_SUFFIX.getStringValue(projectFileGroup));

        JsonArray users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Tic", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Tac",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "first",
            UserTypeField.LOGIN.getStringValue(user));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Ted", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Barney",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "second",
            UserTypeField.LOGIN.getStringValue(user));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        ProjectType processType = new ProjectType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Project project = prepareData().get(2);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete",
            ProjectTypeField.TITLE.getStringValue(actual));
        assertEquals("Key startDate doesn't match to given value!", dateFormat.format(project.getStartDate()),
            ProjectTypeField.START_DATE.getStringValue(actual));
        assertEquals("Key endDate doesn't match to given value!", dateFormat.format(project.getEndDate()),
            ProjectTypeField.END_DATE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", ProjectTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key metsRightsOwner doesn't match to given value!", "",
            ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual));
        assertEquals("Key numberOfVolumes doesn't match to given value!", 0,
            ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual));
        assertEquals("Key numberOfPages doesn't match to given value!", 0,
            ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual));
        assertEquals("Key fileFormatInternal doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_INTERNAL.getStringValue(actual));
        assertEquals("Key fileFormatDmsExport doesn't match to given value!", "XStream",
            ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getStringValue(actual));

        assertEquals("Key client.id doesn't match to given value!", 0, ProjectTypeField.CLIENT_ID.getIntValue(actual));
        assertEquals("Key client.clientName doesn't match to given value!", "",
            ProjectTypeField.CLIENT_NAME.getStringValue(actual));

        JsonArray processes = ProjectTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 0, processes.size());

        JsonArray projectFileGroups = ProjectTypeField.PROJECT_FILE_GROUPS.getJsonArray(actual);
        assertEquals("Size projectFileGroups doesn't match to given value!", 0, projectFileGroups.size());

        JsonArray users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 0, users.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(0);
        HttpEntity document = processType.createDocument(project);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 15, actual.keySet().size());

        JsonArray processes = ProjectTypeField.PROCESSES.getJsonArray(actual);
        JsonObject process = processes.getJsonObject(0);
        assertEquals("Amount of keys in processes is incorrect!", 2, process.keySet().size());

        JsonArray templates = actual.getJsonArray("templates");
        JsonObject template = templates.getJsonObject(0);
        assertEquals("Amount of keys in templates is incorrect!", 2, template.keySet().size());

        JsonArray projectFileGroups = ProjectTypeField.PROJECT_FILE_GROUPS.getJsonArray(actual);
        JsonObject projectFileGroup = projectFileGroups.getJsonObject(0);
        assertEquals("Amount of keys in projectFileGroups is incorrect!", 5, projectFileGroup.keySet().size());

        JsonArray users = ProjectTypeField.USERS.getJsonArray(actual);
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 4, user.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        ProjectType processType = new ProjectType();

        List<Project> processes = prepareData();
        Map<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}

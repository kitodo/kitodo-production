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
import org.kitodo.data.database.beans.SubfolderType;
import org.kitodo.data.database.beans.LinkingMode;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
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
        List<SubfolderType> subfolderTypes = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<Template> templates = new ArrayList<>();
        List<User> users = new ArrayList<>();

        SubfolderType firstFolder = new SubfolderType();
        firstFolder.setFileGroup("MAX");
        firstFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstFolder.setMimeType("image/jpeg");
        firstFolder.setPath("jpgs/max");
        firstFolder.setCopyFolder(true);
        firstFolder.setCreateFolder(true);
        firstFolder.setDerivative(1.0);
        firstFolder.setLinkingMode(LinkingMode.ALL);
        subfolderTypes.add(firstFolder);

        SubfolderType secondFolder = new SubfolderType();
        secondFolder.setFileGroup("DEFAULT");
        secondFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondFolder.setMimeType("image/jpeg");
        secondFolder.setPath("jpgs/default");
        secondFolder.setCopyFolder(true);
        secondFolder.setCreateFolder(true);
        secondFolder.setDerivative(0.8);
        secondFolder.setLinkingMode(LinkingMode.ALL);

        subfolderTypes.add(secondFolder);

        SubfolderType thirdFolder = new SubfolderType();
        thirdFolder.setFileGroup("THUMBS");
        thirdFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdFolder.setMimeType("image/jpeg");
        thirdFolder.setPath("jpgs/thumbs");
        thirdFolder.setCopyFolder(true);
        thirdFolder.setCreateFolder(true);
        thirdFolder.setImageSize(150);
        thirdFolder.setLinkingMode(LinkingMode.ALL);

        subfolderTypes.add(thirdFolder);

        SubfolderType fourthFolder = new SubfolderType();
        fourthFolder.setFileGroup("FULLTEXT");
        fourthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthFolder.setMimeType("text/xml");
        fourthFolder.setPath("ocr/alto");
        fourthFolder.setCopyFolder(true);
        fourthFolder.setCreateFolder(true);
        fourthFolder.setLinkingMode(LinkingMode.ALL);

        subfolderTypes.add(fourthFolder);

        SubfolderType fifthFolder = new SubfolderType();
        fifthFolder.setFileGroup("DOWNLOAD");
        fifthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthFolder.setMimeType("application/pdf");
        fifthFolder.setPath("pdf");
        fifthFolder.setCopyFolder(true);
        fifthFolder.setCreateFolder(true);
        fifthFolder.setLinkingMode(LinkingMode.ALL);

        subfolderTypes.add(fifthFolder);

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
        firstProject.setFolders(subfolderTypes);
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
        secondProject.setFolders(subfolderTypes);
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
        assertEquals("Key startDate doesn't match to given value!", "2017-01-01 00:00:00",
            ProjectTypeField.START_DATE.getStringValue(actual));
        assertEquals("Key endDate doesn't match to given value!", "2017-03-01 00:00:00",
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

        JsonArray folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals("Size folders doesn't match to given value!", 5, folders.size());

        JsonObject folder = folders.getJsonObject(0);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "MAX",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/max",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(1);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "DEFAULT",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/default",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(2);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "THUMBS",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/thumbs",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(3);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "FULLTEXT",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "ocr/alto",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "text/xml",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(4);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "DOWNLOAD",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "pdf",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "application/pdf",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

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
        assertEquals("Key startDate doesn't match to given value!", "2017-01-10 00:00:00",
            ProjectTypeField.START_DATE.getStringValue(actual));
        assertEquals("Key endDate doesn't match to given value!", "2017-09-10 00:00:00",
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

        JsonArray folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals("Size folders doesn't match to given value!", 5, folders.size());

        JsonObject folder = folders.getJsonObject(0);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "MAX",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/max",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(1);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "DEFAULT",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/default",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(2);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "THUMBS",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "jpgs/thumbs",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "image/jpeg",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(3);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "FULLTEXT",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "ocr/alto",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "text/xml",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

        folder = folders.getJsonObject(4);
        assertEquals("Key folders.fileGroup doesn't match to given value!", "DOWNLOAD",
            ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder));
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals("Key folders.urlStructure doesn't match to given value!", path,
            ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder));
        assertEquals("Key folders.path doesn't match to given value!", "pdf",
            ProjectTypeField.FOLDER_PATH.getStringValue(folder));
        assertEquals("Key folders.mimeType doesn't match to given value!", "application/pdf",
            ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder));

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

        JsonArray folder = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals("Size projectFileGroups doesn't match to given value!", 0, folder.size());

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

        JsonArray folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        JsonObject folder = folders.getJsonObject(0);
        assertEquals("Amount of keys in folders is incorrect!", 4, folder.keySet().size());

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

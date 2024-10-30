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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kitodo.constants.StringConstants.DEFAULT_DATE_FORMAT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Test class for ProjectType.
 */
public class ProjectTypeTest {

    private static List<Project> prepareData() {

        List<Project> projects = new ArrayList<>();
        List<Folder> folders = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<Template> templates = new ArrayList<>();
        List<User> users = new ArrayList<>();

        Folder firstFolder = new Folder();
        firstFolder.setFileGroup("MAX");
        firstFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstFolder.setMimeType("image/jpeg");
        firstFolder.setPath("jpgs/max");
        firstFolder.setCopyFolder(true);
        firstFolder.setCreateFolder(true);
        firstFolder.setDerivative(1.0);
        firstFolder.setLinkingMode(LinkingMode.ALL);
        folders.add(firstFolder);

        Folder secondFolder = new Folder();
        secondFolder.setFileGroup("DEFAULT");
        secondFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondFolder.setMimeType("image/jpeg");
        secondFolder.setPath("jpgs/default");
        secondFolder.setCopyFolder(true);
        secondFolder.setCreateFolder(true);
        secondFolder.setDerivative(0.8);
        secondFolder.setLinkingMode(LinkingMode.ALL);

        folders.add(secondFolder);

        Folder thirdFolder = new Folder();
        thirdFolder.setFileGroup("THUMBS");
        thirdFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdFolder.setMimeType("image/jpeg");
        thirdFolder.setPath("jpgs/thumbs");
        thirdFolder.setCopyFolder(true);
        thirdFolder.setCreateFolder(true);
        thirdFolder.setImageSize(150);
        thirdFolder.setLinkingMode(LinkingMode.ALL);

        folders.add(thirdFolder);

        Folder fourthFolder = new Folder();
        fourthFolder.setFileGroup("FULLTEXT");
        fourthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthFolder.setMimeType("text/xml");
        fourthFolder.setPath("ocr/alto");
        fourthFolder.setCopyFolder(true);
        fourthFolder.setCreateFolder(true);
        fourthFolder.setLinkingMode(LinkingMode.ALL);

        folders.add(fourthFolder);

        Folder fifthFolder = new Folder();
        fifthFolder.setFileGroup("DOWNLOAD");
        fifthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthFolder.setMimeType("application/pdf");
        fifthFolder.setPath("pdf");
        fifthFolder.setCopyFolder(true);
        fifthFolder.setCreateFolder(true);
        fifthFolder.setLinkingMode(LinkingMode.ALL);

        folders.add(fifthFolder);

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
        LocalDate localDate = LocalDate.of(2017, 1, 1);
        firstProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2017, 3, 1);
        firstProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstProject.setNumberOfPages(100);
        firstProject.setNumberOfVolumes(10);
        firstProject.setTemplates(templates);
        firstProject.setProcesses(processes);
        firstProject.setFolders(folders);
        firstProject.setUsers(users);
        firstProject.setClient(client);
        projects.add(firstProject);

        Project secondProject = new Project();
        secondProject.setId(2);
        secondProject.setTitle("Rendering");
        localDate = LocalDate.of(2017, 1, 10);
        secondProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2017, 9, 10);
        secondProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondProject.setNumberOfPages(2000);
        secondProject.setNumberOfVolumes(20);
        secondProject.setTemplates(templates);
        secondProject.setProcesses(processes);
        secondProject.setFolders(folders);
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
        Map<String, Object> actual = processType.createDocument(project);

        assertEquals("Testing", ProjectTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("2017-01-01 00:00:00", ProjectTypeField.START_DATE.getStringValue(actual), "Key startDate doesn't match to given value!");
        assertEquals("2017-03-01 00:00:00", ProjectTypeField.END_DATE.getStringValue(actual), "Key endDate doesn't match to given value!");
        assertTrue(ProjectTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals("", ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual), "Key metsRightsOwner doesn't match to given value!");
        assertEquals(10, ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual), "Key numberOfVolumes doesn't match to given value!");
        assertEquals(100, ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual), "Key numberOfPages doesn't match to given value!");

        assertEquals(1, ProjectTypeField.CLIENT_ID.getIntValue(actual), "Key client.id doesn't match to given value!");
        assertEquals("TestClient", ProjectTypeField.CLIENT_NAME.getStringValue(actual), "Key client.clientName doesn't match to given value!");

        assertFalse(ProjectTypeField.HAS_PROCESSES.getBooleanValue(actual), "Has processes can only be false, because property is queried from db");

        List<Map<String, Object>> templates = ProjectTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals(1, templates.size(), "Size templates doesn't match to given value!");

        Map<String, Object> template = templates.get(0);
        assertEquals(1, TemplateTypeField.ID.getIntValue(template), "Key templates.id doesn't match to given value!");
        assertEquals("First", TemplateTypeField.TITLE.getStringValue(template), "Key templates.title doesn't match to given value!");

        List<Map<String, Object>> folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals(5, folders.size(), "Size folders doesn't match to given value!");

        Map<String, Object> folder = folders.get(0);
        assertEquals("MAX", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/max", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(1);
        assertEquals("DEFAULT", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/default", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(2);
        assertEquals("THUMBS", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/thumbs", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(3);
        assertEquals("FULLTEXT", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("ocr/alto", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("text/xml", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(4);
        assertEquals("DOWNLOAD", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("pdf", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("application/pdf", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        List<Map<String, Object>> users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals(2, users.size(), "Size users doesn't match to given value!");

        Map<String, Object> user = users.get(0);
        assertEquals(1, UserTypeField.ID.getIntValue(user), "Key users.id doesn't match to given value!");
        assertEquals("Tic", UserTypeField.NAME.getStringValue(user), "Key users.name doesn't match to given value!");
        assertEquals("Tac", UserTypeField.SURNAME.getStringValue(user), "Key users.surname doesn't match to given value!");
        assertEquals("first", UserTypeField.LOGIN.getStringValue(user), "Key users.login doesn't match to given value!");

        user = users.get(1);
        assertEquals(2, UserTypeField.ID.getIntValue(user), "Key users.id doesn't match to given value!");
        assertEquals("Ted", UserTypeField.NAME.getStringValue(user), "Key users.name doesn't match to given value!");
        assertEquals("Barney", UserTypeField.SURNAME.getStringValue(user), "Key users.surname doesn't match to given value!");
        assertEquals("second", UserTypeField.LOGIN.getStringValue(user), "Key users.login doesn't match to given value!");
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(1);
        Map<String, Object> actual = processType.createDocument(project);

        assertEquals("Rendering", ProjectTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("2017-01-10 00:00:00", ProjectTypeField.START_DATE.getStringValue(actual), "Key startDate doesn't match to given value!");
        assertEquals("2017-09-10 00:00:00", ProjectTypeField.END_DATE.getStringValue(actual), "Key endDate doesn't match to given value!");
        assertTrue(ProjectTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals("", ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual), "Key metsRightsOwner doesn't match to given value!");
        assertEquals(20, ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual), "Key numberOfVolumes doesn't match to given value!");
        assertEquals(2000, ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual), "Key numberOfPages doesn't match to given value!");

        assertEquals(0, ProjectTypeField.CLIENT_ID.getIntValue(actual), "Key client.id doesn't match to given value!");
        assertEquals("", ProjectTypeField.CLIENT_NAME.getStringValue(actual), "Key client.clientName doesn't match to given value!");

        assertFalse(ProjectTypeField.HAS_PROCESSES.getBooleanValue(actual), "Has processes can only be false, because property is queried from db");

        List<Map<String, Object>> templates = ProjectTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals(1, templates.size(), "Size templates doesn't match to given value!");

        Map<String, Object> template = templates.get(0);
        assertEquals(1, TemplateTypeField.ID.getIntValue(template), "Key templates.id doesn't match to given value!");
        assertEquals("First", TemplateTypeField.TITLE.getStringValue(template), "Key templates.title doesn't match to given value!");

        List<Map<String, Object>> folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals(5, folders.size(), "Size folders doesn't match to given value!");

        Map<String, Object> folder = folders.get(0);
        assertEquals("MAX", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        String path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/max", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(1);
        assertEquals("DEFAULT", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/default", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(2);
        assertEquals("THUMBS", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("jpgs/thumbs", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("image/jpeg", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(3);
        assertEquals("FULLTEXT", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("ocr/alto", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("text/xml", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        folder = folders.get(4);
        assertEquals("DOWNLOAD", ProjectTypeField.FOLDER_FILE_GROUP.getStringValue(folder), "Key folders.fileGroup doesn't match to given value!");
        path = "http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/";
        assertEquals(path, ProjectTypeField.FOLDER_URL_STRUCTURE.getStringValue(folder), "Key folders.urlStructure doesn't match to given value!");
        assertEquals("pdf", ProjectTypeField.FOLDER_PATH.getStringValue(folder), "Key folders.path doesn't match to given value!");
        assertEquals("application/pdf", ProjectTypeField.FOLDER_MIME_TYPE.getStringValue(folder), "Key folders.mimeType doesn't match to given value!");

        List<Map<String, Object>> users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals(2, users.size(), "Size users doesn't match to given value!");

        Map<String, Object> user = users.get(0);
        assertEquals(1, UserTypeField.ID.getIntValue(user), "Key users.id doesn't match to given value!");
        assertEquals("Tic", UserTypeField.NAME.getStringValue(user), "Key users.name doesn't match to given value!");
        assertEquals("Tac", UserTypeField.SURNAME.getStringValue(user), "Key users.surname doesn't match to given value!");
        assertEquals("first", UserTypeField.LOGIN.getStringValue(user), "Key users.login doesn't match to given value!");

        user = users.get(1);
        assertEquals(2, UserTypeField.ID.getIntValue(user), "Key users.id doesn't match to given value!");
        assertEquals("Ted", UserTypeField.NAME.getStringValue(user), "Key users.name doesn't match to given value!");
        assertEquals("Barney", UserTypeField.SURNAME.getStringValue(user), "Key users.surname doesn't match to given value!");
        assertEquals("second", UserTypeField.LOGIN.getStringValue(user), "Key users.login doesn't match to given value!");
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        ProjectType processType = new ProjectType();
        DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

        Project project = prepareData().get(2);
        Map<String, Object> actual = processType.createDocument(project);

        assertEquals("Incomplete", ProjectTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals(dateFormat.format(project.getStartDate()), ProjectTypeField.START_DATE.getStringValue(actual), "Key startDate doesn't match to given value!");
        assertEquals(dateFormat.format(project.getEndDate()), ProjectTypeField.END_DATE.getStringValue(actual), "Key endDate doesn't match to given value!");
        assertTrue(ProjectTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals("", ProjectTypeField.METS_RIGTS_OWNER.getStringValue(actual), "Key metsRightsOwner doesn't match to given value!");
        assertEquals(0, ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(actual), "Key numberOfVolumes doesn't match to given value!");
        assertEquals(0, ProjectTypeField.NUMBER_OF_PAGES.getIntValue(actual), "Key numberOfPages doesn't match to given value!");

        assertEquals(0, ProjectTypeField.CLIENT_ID.getIntValue(actual), "Key client.id doesn't match to given value!");
        assertEquals("", ProjectTypeField.CLIENT_NAME.getStringValue(actual), "Key client.clientName doesn't match to given value!");

        assertFalse(ProjectTypeField.HAS_PROCESSES.getBooleanValue(actual), "Has processes can only be false, because property is queried from db");

        List<Map<String, Object>> folder = ProjectTypeField.FOLDER.getJsonArray(actual);
        assertEquals(0, folder.size(), "Size projectFileGroups doesn't match to given value!");

        List<Map<String, Object>> users = ProjectTypeField.USERS.getJsonArray(actual);
        assertEquals(0, users.size(), "Size users doesn't match to given value!");
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ProjectType processType = new ProjectType();

        Project project = prepareData().get(0);
        Map<String, Object> actual = processType.createDocument(project);

        assertEquals(13, actual.keySet().size(), "Amount of keys is incorrect!");

        List<Map<String, Object>> templates = ProjectTypeField.TEMPLATES.getJsonArray(actual);
        Map<String, Object> template = templates.get(0);
        assertEquals(2, template.keySet().size(), "Amount of keys in templates is incorrect!");

        List<Map<String, Object>> folders = ProjectTypeField.FOLDER.getJsonArray(actual);
        Map<String, Object> folder = folders.get(0);
        assertEquals(4, folder.keySet().size(), "Amount of keys in folders is incorrect!");

        List<Map<String, Object>> users = ProjectTypeField.USERS.getJsonArray(actual);
        Map<String, Object> user = users.get(0);
        assertEquals(4, user.keySet().size(), "Amount of keys in users is incorrect!");
    }

    @Test
    public void shouldCreateDocuments() {
        ProjectType processType = new ProjectType();

        List<Project> processes = prepareData();
        Map<Integer, Map<String, Object>> documents = processType.createDocuments(processes);
        assertEquals(3, documents.size(), "HashMap of documents doesn't contain given amount of elements!");
    }
}

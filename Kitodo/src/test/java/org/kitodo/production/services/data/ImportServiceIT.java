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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;

import com.xebialabs.restito.server.StubServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.UrlParameter;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ImportException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
import org.kitodo.production.helper.ProcessHelper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;
import org.kitodo.test.utils.TestConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class ImportServiceIT {

    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final ImportService importService = ServiceManager.getImportService();
    private static StubServer server;
    private static final String TEST_FILE_PATH = "src/test/resources/sruTestRecord.xml";
    private static final String TEST_FILE_PATH_NUMBER_OF_HITS = "src/test/resources/importRecords/sruResponseNumberOfHits.xml";
    private static final String TEST_FILE_SUCCESS_RESPONSE_PATH = "src/test/resources/customInterfaceSuccessResponse.xml";
    private static final String TEST_FILE_ERROR_RESPONSE_PATH = "src/test/resources/customInterfaceErrorResponse.xml";
    private static final String PARENT_PROCESS_TEST_FILE = "testMetadataForKalliopeParentProcess.xml";
    private static final String CHILD_RECORDS_PATH = "src/test/resources/importRecords/importMultipleChildRecords.xml";
    private static final String MODS_TEST_RECORD_PATH = "src/test/resources/importRecords/modsTestRecord.xml";
    private static final String TEST_RULESET = "src/test/resources/rulesets/ruleset_test.xml";
    private static final String TEST_KITODO_METADATA_FILE = "testMetadataFileTempProcess.xml";
    private static final String TEST_METADATA_WITH_AUTHOR_FILE = "testMetadataWithAuthor.xml";
    private static final String TEST_KITODO_METADATA_FILE_PATH = "src/test/resources/metadata/metadataFiles/"
            + TEST_KITODO_METADATA_FILE;
    private static final String TEST_METADATA_WITH_AUTHOR_FILE_PATH = "src/test/resources/metadata/metadataFiles/"
            + TEST_METADATA_WITH_AUTHOR_FILE;
    private static final String RECORD_ID = "11111";
    private static final String PARENT_RECORD_CATALOG_ID = "123123";
    private static final List<String> CHILD_RECORD_IDS = Arrays.asList("9991", "9992", "9993");
    private static final String KALLIOPE_RECORD_ID = "999";
    private static final String CUSTOM_INTERFACE_RECORD_ID = "12345";
    private static final int TEMPLATE_ID = 1;
    private static final int PROJECT_ID = 1;
    private static final int RULESET_ID = 1;
    private static final String TITLE = "Title";
    private static final String PLACE = "Place";
    private static final int EXPECTED_NR_OF_CHILDREN = 23;
    private static final String PICA_XML = "picaxml";
    private static final String PICA_PPN = "pica.ppn";
    private static final String PICA_PARENT_ID = "pica.parentId";
    private static final String firstProcess = "First process";
    private static final String TEST_PROCESS_TITLE = "Testtitel";
    private static final String KITODO = "kitodo";
    private static final String METADATA = "metadata";
    private static final String EXPECTED_AUTHOR = "HansMeier";
    private static final String KITODO_NAMESPACE = "http://meta.kitodo.org/v1/";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.insertImportconfigurationWithCustomUrlParameters();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
        server = new StubServer(MockDatabase.PORT).run();
        setupServer();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        server.stop();
    }

    /**
     * Tests whether basic catalog metadata import to a single process succeeds or not.
     *
     * @throws DAOException when loading ImportConfiguration or removing test process from test database fails.
     * @throws DataException when counting processes in test database fails
     * @throws ImportException when importing metadata fails
     * @throws IOException when importing metadata fails
     */
    @Test
    public void testImportProcess() throws DAOException, DataException, ImportException, IOException {
        Assert.assertEquals("Not the correct amount of processes found", 7, (long) processService.count());
        Process importedProcess = importProcess(RECORD_ID, MockDatabase.getK10PlusImportConfiguration());
        try {
            Assert.assertEquals("WrongProcessTitle", "Kitodo_" + RECORD_ID, importedProcess.getTitle());
            Assert.assertEquals("Wrong project used", 1, (long) importedProcess.getProject().getId());
            Assert.assertEquals("Wrong template used", 1, (long) importedProcess.getTemplate().getId());
            Assert.assertEquals("Not the correct amount of processes found", 8, (long) processService.count());
        } finally {
            ProcessTestUtils.removeTestProcess(importedProcess.getId());
        }
    }

    /**
     * Tests whether basic catalog metadata import with additional preset metadata to a single process succeeds or not.
     *
     * @throws DAOException when loading ImportConfiguration or removing test process from test database fails.
     * @throws ImportException when importing metadata fails
     * @throws IOException when importing metadata fails
     */
    @Test
    public void testImportProcessWithAdditionalMetadata() throws DAOException, ImportException, IOException {
        Map<String, List<String>> presetMetadata = new HashMap<>();
        presetMetadata.put(TITLE, List.of("Band 1"));
        presetMetadata.put(PLACE, List.of("Hamburg", "Berlin"));
        Process processWithAdditionalMetadata = importProcessWithAdditionalMetadata(RECORD_ID,
                MockDatabase.getK10PlusImportConfiguration(), presetMetadata);
        Workpiece workpiece = ServiceManager.getMetsService()
                .loadWorkpiece(processService.getMetadataFileUri(processWithAdditionalMetadata));
        HashSet<Metadata> metadata = workpiece.getLogicalStructure().getMetadata();
        try {
            Assert.assertTrue("Process does not contain correct metadata",
                    assertMetadataSetContainsMetadata(metadata, TITLE, "Band 1"));
            Assert.assertTrue("Process does not contain correct metadata",
                    assertMetadataSetContainsMetadata(metadata, PLACE, "Hamburg"));
            Assert.assertTrue("Process does not contain correct metadata",
                    assertMetadataSetContainsMetadata(metadata, PLACE, "Berlin"));
        } finally {
            ProcessTestUtils.removeTestProcess(processWithAdditionalMetadata.getId());
        }
    }

    private boolean assertMetadataSetContainsMetadata(HashSet<Metadata> metadataSet, String metadataKey, String metadataValue) {
        return metadataSet.stream()
                .filter(metadata -> metadata.getKey().equals(metadataKey))
                .anyMatch(metadata -> metadata instanceof MetadataEntry &&
                        ((MetadataEntry) metadata).getValue().equals(metadataValue));
    }

    @Test
    public void shouldCreateUrlWithCustomParameters() throws DAOException, ImportException, IOException {
        Process importedProcess = importProcess(CUSTOM_INTERFACE_RECORD_ID, MockDatabase.getCustomTypeImportConfiguration());
        try {
            Assert.assertNotNull(importedProcess);
        } finally {
            ProcessTestUtils.removeTestProcess(importedProcess.getId());
        }
    }

    @Test(expected = ImportException.class)
    public void shouldFailToImportFromCustomInterfaceWithoutConfiguredUrlParameters() throws DAOException,
            ImportException, IOException {
        ImportConfiguration customConfiguration = MockDatabase.getCustomTypeImportConfiguration();
        UrlParameter wrongUrlParameter = new UrlParameter();
        wrongUrlParameter.setParameterKey("firstKey");
        wrongUrlParameter.setParameterValue("wrongValue");
        customConfiguration.setUrlParameters(Collections.singletonList(wrongUrlParameter));
        importProcess(CUSTOM_INTERFACE_RECORD_ID, customConfiguration);
    }

    /**
     * Tests whether all document types in allow a functional metadata of type 'recordIdentifier' or not.
     * @throws IOException when test ruleset file cannot be loaded
     */
    @Test
    public void shouldTestWhetherRecordIdentifierMetadataIsConfigured() throws IOException {
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();
        rulesetManagement.load(new File(TEST_RULESET));
        Assert.assertTrue("Should determine that recordIdentifier is configured for all document types",
                ServiceManager.getImportService().isRecordIdentifierMetadataConfigured(rulesetManagement));
    }

    /**
     * Tests whether creating TempProcess from document succeeds or not.
     * @throws Exception when creating TempProcess fails.
     */
    @Test
    public void shouldCreateTempProcessFromDocument() throws Exception {
        ImportConfiguration importConfiguration = MockDatabase.getK10PlusImportConfiguration();
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_KITODO_METADATA_FILE_PATH))) {
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Document xmlDocument = XMLUtils.parseXMLString(fileContent);
            TempProcess tempProcess = ServiceManager.getImportService().createTempProcessFromDocument(importConfiguration,
                    xmlDocument, TEMPLATE_ID, PROJECT_ID);
            Assert.assertNotNull("TempProcess should not be null", tempProcess);
        }
    }

    /**
     * Tests whether the parent process with the provided process ID exists and whether the parent TempProcess was
     * created from it.
     *
     * @throws DAOException when test ruleset cannot be loaded from database
     * @throws ProcessGenerationException when checking for parent process fails
     * @throws IOException when checking for parent process fails
     * @throws DataException when copying test metadata file fails
     */
    @Test
    @Ignore("index currently not available")
    public void shouldCheckForParent() throws DAOException, ProcessGenerationException, IOException, DataException {
        int parentTestId = MockDatabase.insertTestProcess("Test parent process", PROJECT_ID, TEMPLATE_ID, RULESET_ID);
        ProcessTestUtils.copyTestMetadataFile(parentTestId, TEST_KITODO_METADATA_FILE);
        Ruleset ruleset = ServiceManager.getRulesetService().getById(RULESET_ID);
        try {
            ProcessTestUtils.updateIdentifier(parentTestId);
            importService.checkForParent(String.valueOf(parentTestId), ruleset, PROJECT_ID);
            Assert.assertNotNull(importService.getParentTempProcess());
        } finally {
            ProcessTestUtils.removeTestProcess(parentTestId);
        }
    }

    /**
     * Tests retrieving number of child records.
     *
     * @throws DAOException when loading K10Plus ImportConfiguration from test database fails
     */
    @Test
    public void shouldGetNumberOfChildren() throws DAOException {
        int numberOfChildren = importService.getNumberOfChildren(MockDatabase.getK10PlusImportConfiguration(), RECORD_ID);
        Assert.assertEquals("Number of children is incorrect", EXPECTED_NR_OF_CHILDREN , numberOfChildren);
    }

    /**
     * Tests whether importing child processes via an ImportConfiguration that does not support it fails or not.
     *
     * @throws Exception when loading ImportConfiguration from test database fails
     */
    @Test(expected = NoRecordFoundException.class)
    public void shouldFailToLoadK1PlusChildProcesses() throws Exception {
        importService.getChildProcesses(MockDatabase.getK10PlusImportConfiguration(), RECORD_ID, PROJECT_ID,
                TEMPLATE_ID, 1, Collections.emptyList());
    }

    /**
     * Tests whether importing child processes via an ImportConfiguration with corresponding support succeeds or not.
     *
     * @throws Exception when loading ImportConfiguration from test database fails
     */
    @Test
    public void shouldSucceedToLoadKalliopeChildProcesses() throws Exception {
        List<TempProcess> childRecords = importService.getChildProcesses(MockDatabase.getKalliopeImportConfiguration(),
                KALLIOPE_RECORD_ID, PROJECT_ID, TEMPLATE_ID, 3, Collections.emptyList());
        Assert.assertEquals("Wrong number of Kalliope child records", 3, childRecords.size());
    }

    /**
     * Tests whether retrieving child records for a parent process with a given ID succeeds or not.
     *
     * @throws Exception when preparing test environment fails
     */
    @Test
    public void shouldGetChildProcesses() throws Exception {
        int parentProcessId = MockDatabase.insertTestProcess("Parent process", PROJECT_ID, TEMPLATE_ID, RULESET_ID);
        try {
            ProcessTestUtils.copyTestMetadataFile(parentProcessId, PARENT_PROCESS_TEST_FILE);
            Process parentProcess = ServiceManager.getProcessService().getById(parentProcessId);
            URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(parentProcess);
            Workpiece parentWorkpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            TempProcess parentTempProcess = new TempProcess(parentProcess, parentWorkpiece);
            LinkedList<TempProcess> childProcesses = ServiceManager.getImportService().getChildProcesses(
                    MockDatabase.getKalliopeImportConfiguration(), PARENT_RECORD_CATALOG_ID, PROJECT_ID, TEMPLATE_ID, 3,
                    Collections.singletonList(parentTempProcess));
            Assert.assertEquals("Wrong number of imported child processes", 3, childProcesses.size());
            for (TempProcess childProcess : childProcesses) {
                String childId = CHILD_RECORD_IDS.get(childProcesses.indexOf(childProcess));
                Optional<Metadata> importedChildId = childProcess.getWorkpiece().getLogicalStructure().getMetadata().stream()
                        .filter(metadata -> metadata instanceof MetadataEntry && metadata.getKey().equals("CatalogIDDigital")
                                && ((MetadataEntry) metadata).getValue().equals(childId)).findAny();
                Assert.assertTrue(String.format("Retrieved child records should contain process with CatalogIDDigital '%s'", childId),
                        importedChildId.isPresent());
            }
        } finally {
            ProcessTestUtils.removeTestProcess(parentProcessId);
        }
    }

    /**
     * Tests whether transforming external SRU MODS record to internal succeeds or not.
     *
     * @throws DAOException when loading test ImportConfiguration from database fails
     * @throws UnsupportedFormatException when DataRecord metadata format does not match ImportConfigurations format
     * @throws XPathExpressionException when ImportConfiguration contains invalid XPaths properties
     * @throws ProcessGenerationException when the created Document is null
     * @throws URISyntaxException when converting the external record to internal record fails
     * @throws IOException when creating external document fails
     * @throws ParserConfigurationException when created internal data record does not contain valid XML
     * @throws SAXException when created internal data record does not contain valid XML
     */
    @Test
    public void shouldConvertDataRecordToInternal() throws DAOException, UnsupportedFormatException,
            XPathExpressionException, ProcessGenerationException, URISyntaxException, IOException,
            ParserConfigurationException, SAXException {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setFileFormat(FileFormat.XML);
        dataRecord.setMetadataFormat(MetadataFormat.MODS);
        try (InputStream inputStream = Files.newInputStream(Paths.get(MODS_TEST_RECORD_PATH))) {
            dataRecord.setOriginalData(IOUtils.toString(inputStream, Charset.defaultCharset()));
            Document internalDocument = ServiceManager.getImportService().convertDataRecordToInternal(dataRecord,
                    MockDatabase.getKalliopeImportConfiguration(), false);
            Assert.assertEquals("Converted data should contain one 'kitodo' root node", 1,
                    internalDocument.getElementsByTagNameNS(KITODO_NAMESPACE, KITODO).getLength());
            Assert.assertEquals("Converted data should contain three 'metadata' nodes", 3,
                    internalDocument.getElementsByTagNameNS(KITODO_NAMESPACE, METADATA).getLength());
        }
    }

    /**
     * Test whether processing TempProcess succeeds or not.
     *
     * @throws DAOException when loading Ruleset or ImportConfiguration from test database fails
     * @throws IOException when opening test Ruleset or test metadata file fails
     * @throws ProcessGenerationException when creating TempProcess from test Document fails
     * @throws InvalidMetadataValueException when creating TempProcess from test Document fails
     * @throws NoSuchMetadataFieldException when creating TempProcess from test Document fails
     * @throws ParserConfigurationException when creating test Document from test metadata XML file fails
     * @throws SAXException when creating test Document from test metadata XML file fails
     * @throws TransformerException when creating TempProcess from test Document fails
     */
    @Test
    public void shouldProcessTempProcess() throws DAOException, IOException, ProcessGenerationException,
            InvalidMetadataValueException, NoSuchMetadataFieldException, ParserConfigurationException, SAXException,
            TransformerException {
        Ruleset ruleset = ServiceManager.getRulesetService().getById(RULESET_ID);
        RulesetManagementInterface management = ServiceManager.getRulesetService().openRuleset(ruleset);
        ImportConfiguration importConfiguration = MockDatabase.getK10PlusImportConfiguration();
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_KITODO_METADATA_FILE_PATH))) {
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Document xmlDocument = XMLUtils.parseXMLString(fileContent);
            TempProcess tempProcess = ServiceManager.getImportService().createTempProcessFromDocument(
                    importConfiguration, xmlDocument, TEMPLATE_ID, PROJECT_ID);
            ImportService.processTempProcess(tempProcess, management,
                    ImportService.ACQUISITION_STAGE_CREATE, ServiceManager.getUserService()
                            .getCurrentMetadataLanguage(), null);
            Assert.assertFalse("Process should have some properties",
                    tempProcess.getProcess().getProperties().isEmpty());
            Assert.assertTrue("Process title should not be empty",
                    StringUtils.isNotBlank(tempProcess.getProcess().getTitle()));
        }
    }

    /**
     * Tests retrieving value of ProcessDetail.
     *
     * @throws Exception when loading process details from test process fails
     */
    @Test
    public void shouldGetProcessDetailValue() throws Exception {
        List<ProcessDetail> processDetails = loadProcessDetailsFromTestProcess(TEST_KITODO_METADATA_FILE_PATH);
        Assert.assertFalse("List of process details should not be empty", processDetails.isEmpty());
        Assert.assertEquals("Value of first process details should not be empty",
                TEST_PROCESS_TITLE, ImportService.getProcessDetailValue(processDetails.get(0)));
    }

    /**
     * Tests setting value of a ProcessDetail.
     *
     * @throws Exception when loading process details from test process fails
     */
    @Test
    public void shouldSetProcessDetailValue() throws Exception {
        String newProcessTitle = "New process title";
        List<ProcessDetail> processDetails = loadProcessDetailsFromTestProcess(TEST_KITODO_METADATA_FILE_PATH);
        Assert.assertFalse("Process detail list should not be empty", processDetails.isEmpty());
        ProcessDetail title = processDetails.get(0);
        Assert.assertEquals("Wrong title process detail before setting it", TEST_PROCESS_TITLE,
                ImportService.getProcessDetailValue(title));
        ImportService.setProcessDetailValue(title, newProcessTitle);
        Assert.assertEquals("Wrong title process detail after setting it", newProcessTitle,
                ImportService.getProcessDetailValue(title));
    }

    /**
     * Tests retrieving list of creators from ProcessDetails.
     *
     * @throws Exception when loading process details from test process fails
     */
    @Test
    public void shouldGetListOfCreators() throws Exception {
        List<ProcessDetail> processDetails = loadProcessDetailsFromTestProcess(TEST_METADATA_WITH_AUTHOR_FILE_PATH);
        Assert.assertFalse("Process detail list should not be empty", processDetails.isEmpty());
        String creators = ImportService.getListOfCreators(processDetails);
        Assert.assertEquals("Author metadata is not correct", EXPECTED_AUTHOR, creators);
    }

    /**
     * Tests selecting an exemplar record.
     *
     * @throws Exception when loading process details from test process fails
     */
    @Test
    public void shouldSetSelectedExemplarRecord() throws Exception {
        String expectedOwner = "SUB Hamburg";
        String expectedSignature = "222";
        ImportConfiguration importConfiguration = MockDatabase.getGbvImportConfiguration();
        List<ProcessDetail> processDetails = loadProcessDetailsFromTestProcess(TEST_METADATA_WITH_AUTHOR_FILE_PATH);
        String exemplarDataOwner = getProcessDetailByMetadataId(importConfiguration.getItemFieldOwnerMetadata(), processDetails);
        String exemplarDataSignature = getProcessDetailByMetadataId(importConfiguration.getItemFieldSignatureMetadata(), processDetails);
        Assert.assertNotEquals("Wrong exemplar data owner BEFORE selecting exemplar", exemplarDataOwner, expectedOwner);
        Assert.assertNotEquals("Wrong exemplar data signature BEFORE selecting exemplar", exemplarDataSignature, expectedSignature);
        ExemplarRecord exemplarRecord = new ExemplarRecord(expectedOwner, expectedSignature);
        ImportService.setSelectedExemplarRecord(exemplarRecord, importConfiguration, processDetails);
        exemplarDataOwner = getProcessDetailByMetadataId(importConfiguration.getItemFieldOwnerMetadata(), processDetails);
        exemplarDataSignature = getProcessDetailByMetadataId(importConfiguration.getItemFieldSignatureMetadata(), processDetails);
        Assert.assertEquals("Wrong exemplar data owner AFTER selecting exemplar", exemplarDataOwner, expectedOwner);
        Assert.assertEquals("Wrong exemplar data signature AFTER selecting exemplar", exemplarDataSignature, expectedSignature);
    }

    /**
     * Tests whether ensuring non-empty process titles succeeds or not.
     *
     * @throws DAOException when preparing test process fails
     * @throws DataException when copying test metadata file fails
     * @throws IOException when loading workpiece fails
     */
    @Test
    public void shouldEnsureNonEmptyTitles() throws DAOException, DataException, IOException {
        int parentProcessId = MockDatabase.insertTestProcess("", PROJECT_ID, TEMPLATE_ID, RULESET_ID);
        try {
            ProcessTestUtils.copyTestMetadataFile(parentProcessId, TEST_KITODO_METADATA_FILE);
            Process parentProcess = ServiceManager.getProcessService().getById(parentProcessId);
            URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(parentProcess);
            Workpiece parentWorkpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            TempProcess parentTempProcess = new TempProcess(parentProcess, parentWorkpiece);
            Assert.assertTrue("Process title should be empty before setting it",
                    StringUtils.isBlank(parentTempProcess.getProcess().getTitle()));
            LinkedList<TempProcess> tempProcesses = new LinkedList<>();
            tempProcesses.add(parentTempProcess);
            boolean titleChanged = ImportService.ensureNonEmptyTitles(tempProcesses);
            Assert.assertTrue("Process titles should have been changed", titleChanged);
        } finally {
            ProcessTestUtils.removeTestProcess(parentProcessId);
        }
    }

    /**
     * Tests adding properties to a process.
     *
     * @throws Exception when preparing the test process environment fails
     */
    @Test
    public void shouldAddProperties() throws Exception {
        String monograph = "monograph";
        String imageDescription = "Image Description";
        String docType = "DocType";
        Template template = ServiceManager.getTemplateService().getById(TEMPLATE_ID);
        int processId = MockDatabase.insertTestProcess("Test process", PROJECT_ID, TEMPLATE_ID, RULESET_ID);
        try {
            ProcessTestUtils.copyTestMetadataFile(processId, TEST_KITODO_METADATA_FILE);
            Process process = ServiceManager.getProcessService().getById(processId);
            URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
            Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            TempProcess tempProcess = new TempProcess(process, workpiece);
            List<ProcessDetail> processDetails = loadProcessDetailsFromTestProcess(TEST_METADATA_WITH_AUTHOR_FILE_PATH);
            Assert.assertFalse("Process should NOT contain 'DocType' property before adding it",
                    process.getWorkpieces().stream().anyMatch(property -> docType.equals(property.getTitle())));
            ImportService.addProperties(tempProcess, template, processDetails, monograph, imageDescription);
            Assert.assertTrue("Process should contain 'DocType' property after adding it",
                    process.getWorkpieces().stream().anyMatch(property -> docType.equals(property.getTitle())));
        } finally {
            ProcessTestUtils.removeTestProcess(processId);
        }
    }

    private String getProcessDetailByMetadataId(String metadataId, List<ProcessDetail> processDetails) {
        for (ProcessDetail processDetail : processDetails) {
            if (Objects.equals(processDetail.getMetadataID(), metadataId) && processDetail instanceof ProcessTextMetadata) {
                return ((ProcessTextMetadata)processDetail).getValue();
            }
        }
        return null;
    }

    private static List<ProcessDetail> loadProcessDetailsFromTestProcess(String filepath) throws IOException,
            DAOException, ProcessGenerationException, InvalidMetadataValueException, TransformerException,
            NoSuchMetadataFieldException, ParserConfigurationException, SAXException {
        ImportConfiguration importConfiguration = MockDatabase.getK10PlusImportConfiguration();
        Ruleset ruleset = ServiceManager.getRulesetService().getById(RULESET_ID);
        RulesetManagementInterface management = ServiceManager.getRulesetService().openRuleset(ruleset);
        try (InputStream inputStream = Files.newInputStream(Paths.get(filepath))) {
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Document xmlDocument = XMLUtils.parseXMLString(fileContent);
            TempProcess tempProcess = ServiceManager.getImportService().createTempProcessFromDocument(
                    importConfiguration, xmlDocument, TEMPLATE_ID, PROJECT_ID);
            return ProcessHelper.transformToProcessDetails(tempProcess, management,
                    ImportService.ACQUISITION_STAGE_CREATE, ServiceManager.getUserService()
                            .getCurrentMetadataLanguage());
        }
    }

    private static void setupServer() throws IOException {
        // REST endpoint for testing metadata import
        MockDatabase.addRestEndPointForSru(server, PICA_PPN + "=" + RECORD_ID, TEST_FILE_PATH, PICA_XML, 1);
        // REST endpoint for testing retrieval of number of child records
        MockDatabase.addRestEndPointForSru(server, PICA_PARENT_ID + "=" + RECORD_ID, TEST_FILE_PATH_NUMBER_OF_HITS, PICA_XML, 0);
        // REST endpoint for testing failed import of child records
        MockDatabase.addRestEndPointForSru(server, PICA_PARENT_ID + "=" + RECORD_ID, TEST_FILE_PATH_NUMBER_OF_HITS, PICA_XML, 1);
        // REST endpoint for testing successful import of child records
        MockDatabase.addRestEndPointForSru(server, TestConstants.EAD_PARENT_ID + "=" + KALLIOPE_RECORD_ID, CHILD_RECORDS_PATH, TestConstants.MODS, 3);
        // REST endpoint for testing retrieval of child records given existing parent process
        MockDatabase.addRestEndPointForSru(server, TestConstants.EAD_PARENT_ID + "=" + PARENT_RECORD_CATALOG_ID, CHILD_RECORDS_PATH, TestConstants.MODS,3);
        // REST endpoint for successful import from custom search interface
        MockDatabase.addRestEndPointForCustom(server, TEST_FILE_SUCCESS_RESPONSE_PATH, CUSTOM_INTERFACE_RECORD_ID,
                "firstValue");
        // REST endpoint for failed import from custom search interface
        MockDatabase.addRestEndPointForCustom(server, TEST_FILE_ERROR_RESPONSE_PATH, CUSTOM_INTERFACE_RECORD_ID,
                "wrongValue");
    }

    private Process importProcess(String recordId, ImportConfiguration importConfiguration)
            throws IOException, ImportException {
        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        Process importedProcess = importService.importProcess(recordId, 1, 1,
                importConfiguration, new HashMap<>());
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
        return importedProcess;
    }

    private Process importProcessWithAdditionalMetadata(String recordId, ImportConfiguration importConfiguration,
                                                        Map<String, List<String>> presetMetadata)
            throws IOException, ImportException {
        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        Process importedProcess = importService.importProcess(recordId, 1, 1,
                importConfiguration, presetMetadata);
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
        return importedProcess;
    }
}

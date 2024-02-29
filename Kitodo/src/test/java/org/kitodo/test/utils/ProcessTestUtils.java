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

package org.kitodo.test.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.MockDatabase;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.primefaces.model.DefaultTreeNode;

public class ProcessTestUtils {

    private static final String TEST_IMAGES_DIR = "images";
    public static final String METADATA_DIR = "metadataFiles";
    public static final String METADATA_BASE_DIR = "src/test/resources/metadata/";
    public static final String META_XML = "/meta.xml";
    private static final int TEST_PROJECT_ID = 1;
    private static final int TEST_TEMPLATE_ID = 1;
    private static final String ID_PLACEHOLDER = "IDENTIFIER_PLACEHOLDER";
    public static final String testFileForHierarchyParent = "multivalued_metadata.xml";
    public static final String testFileForLongNumbers = "testMetadataWithLongNumbers.xml";
    public static final String testFileChildProcessToKeep = "testMetadataForChildProcessToKeep.xml";
    private static final String testFileChildProcessToRemove = "testMetadataForKitodoScript.xml";
    private static final Map<String, String> hierarchyProcessTitlesAndFiles;

    static {
        hierarchyProcessTitlesAndFiles = Map.of(
                MockDatabase.HIERARCHY_PARENT, testFileForHierarchyParent,
                MockDatabase.HIERARCHY_CHILD_TO_KEEP, testFileChildProcessToKeep,
                MockDatabase.HIERARCHY_CHILD_TO_REMOVE, testFileChildProcessToRemove);
    }
    private static final Logger logger = LogManager.getLogger(ProcessTestUtils.class);

    /**
     * Get a tree node with ProcessTextMetadata item.
     *
     * @param metadataId
     *         The metadata id
     * @param metadataKey
     *         The metadata key
     * @param metadataValue
     *         The metadata value
     * @return the tree node
     */
    public static DefaultTreeNode getTreeNode(String metadataId, String metadataKey, String metadataValue) {
        DefaultTreeNode metadataTreeNode = new DefaultTreeNode();
        MetadataEntry metadataEntry = new MetadataEntry();
        metadataEntry.setKey(metadataKey);
        metadataEntry.setValue(metadataValue);
        metadataTreeNode.setData(new ProcessTextMetadata(null, getSimpleMetadataView(metadataId), metadataEntry));
        return metadataTreeNode;
    }

    private static SimpleMetadataViewInterface getSimpleMetadataView(String metadataId) {
        SimpleMetadataViewInterface simpleMetadataView = mock(SimpleMetadataViewInterface.class);
        when(simpleMetadataView.getId()).thenReturn(metadataId);
        return simpleMetadataView;
    }

    /**
     * Copy metadata test file with provided filename "filename" to process directory of test process with ID
     * "processId". Additionally, copy test images to said test processes image directory.
     * @param processId ID of test process to whose process directory test files are copied
     * @param filename filename of metadata file to copy
     * @throws IOException when copying test metadata file fails
     * @throws DAOException when copying test metadata file fails
     * @throws DataException when copying test metadata file fails
     */
    public static void copyTestFiles(int processId, String filename) throws IOException, DAOException, DataException {
        // copy test meta xml
        copyTestMetadataFile(processId, filename);
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId))
                .toUri();
        // copy test images
        URI testImagesUri = Paths.get(ConfigCore.getKitodoDataDirectory(), TEST_IMAGES_DIR).toUri();
        URI targetImages = Paths.get(ConfigCore.getKitodoDataDirectory(), processId
                + "/" + TEST_IMAGES_DIR + "/").toUri();
        try {
            if (!ServiceManager.getFileService().isDirectory(targetImages)) {
                ServiceManager.getFileService().createDirectory(processDir, TEST_IMAGES_DIR);
            }
            ServiceManager.getFileService().copyDirectory(testImagesUri, targetImages);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Copy test resources from source directory specified in second parameter 'sourceDirectory' to metadata directory
     * of test process with provided ID 'processId'.
     * @param processId ID of process to which test resources are copied
     * @param sourceDirectory directory containing test resources to be copied
     * @throws IOException when listing files in source directory fails
     */
    public static void copyTestResources(int processId, String sourceDirectory) throws IOException {
        String sourceDir = Paths.get(ConfigCore.getKitodoDataDirectory(), sourceDirectory).toString();
        String targetDir = Paths.get(ConfigCore.getKitodoDataDirectory(), processId + "/").toString();

        try (Stream<Path> pathStream = Files.walk(Paths.get(sourceDir))) {
            for (Path source : (Iterable<Path>) pathStream::iterator) {
                Path destination = Paths.get(targetDir, source.toString().substring(sourceDir.length()));
                try {
                    Files.copy(source, destination);
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Copy test metadata xml file with provided 'filename' to process directory of process with provided ID
     * 'processId' and rename the file to 'meta.xml'. Creates directory if it does not exist.
     * @param processId process ID
     * @param filename filename of metadata file
     * @throws IOException when subdirectory cannot be created or metadata file cannot be copied
     * @throws DAOException when retrieving process from database fails
     * @throws DataException when saving process fails
     */
    public static void copyTestMetadataFile(int processId, String filename) throws IOException, DAOException,
            DataException {
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId))
                .toUri();
        URI processDirTargetFile = Paths.get(ConfigCore.getKitodoDataDirectory(), processId
                + META_XML).toUri();
        URI metaFileUri = Paths.get(ConfigCore.getKitodoDataDirectory(), METADATA_DIR, filename).toUri();
        if (!ServiceManager.getFileService().isDirectory(processDir)) {
            ServiceManager.getFileService().createDirectory(Paths.get(ConfigCore.getKitodoDataDirectory()).toUri(),
                    String.valueOf(processId));
        }
        ServiceManager.getFileService().copyFile(metaFileUri, processDirTargetFile);
        // re-save process to add meta xml contents to index
        Process process = ServiceManager.getProcessService().getById(processId);
        ServiceManager.getProcessService().save(process, true);
    }

    /**
     * Add process with given title "processTitle" and to project with configured ID 'TEST_PROJECT_ID' and template
     * 'TEST_TEMPLATE_ID' to mock database.
     * @param processTitle title of process to add
     * @return created process
     * @throws DAOException when adding process fails
     * @throws DataException when adding process fails
     */
    public static Process addProcess(String processTitle) throws DAOException, DataException {
        return MockDatabase.addProcess(processTitle, TEST_PROJECT_ID, TEST_TEMPLATE_ID);
    }

    /**
     * Delete test process with given ID including potential parent processes.
     * @param testProcessId ID of process to delete
     * @throws DAOException when retrieving process from database for deletion fails
     */
    public static void removeTestProcess(int testProcessId) throws DAOException {
        if (testProcessId > 0) {
            deleteProcessHierarchy(ServiceManager.getProcessService().getById(testProcessId));
        }
    }

    private static void deleteProcessHierarchy(Process process) {
        for (Process childProcess : process.getChildren()) {
            deleteProcessHierarchy(childProcess);
        }
        try {
            ProcessService.deleteProcess(process.getId());
        } catch (Exception e) {
            logger.error("Error removing process " + process.getTitle() + " (" + process.getId() + "): " + e.getMessage());
        }
    }

    /**
     * Copy metadata test files for hierarchical processes t
     * @param processTitlesAndIds Map containing process titles as keys and IDs as values
     * @throws IOException when copying test resources for a process fails
     * @throws DAOException when copying test resources for a process or loading the process from the database fails
     * @throws DataException when saving a test process fails
     */
    public static void copyHierarchyTestFiles(Map<String, Integer> processTitlesAndIds) throws IOException,
            DAOException, DataException {
        for (Map.Entry<String, String> hierarchyProcess : hierarchyProcessTitlesAndFiles.entrySet()) {
            int processId = processTitlesAndIds.get(hierarchyProcess.getKey());
            ProcessTestUtils.copyTestFiles(processId, hierarchyProcess.getValue());
            // re-save to index metadata file
            Process testProcess = ServiceManager.getProcessService().getById(processId);
            ServiceManager.getProcessService().save(testProcess);
        }
    }

    /**
     * Replace string ID_PLACEHOLDER with given integer 'processId' in metadata file of corresponding test process.
     * @param processId ID of process
     * @throws DAOException when process cannot be loaded from test database
     * @throws IOException when metadata file of process cannot be read or saved
     * @throws DataException when process cannot be re-saved
     */
    public static void updateIdentifier(int processId) throws DAOException, IOException, DataException {
        Process process = ServiceManager.getProcessService().getById(processId);
        URI metadataFileUri = ServiceManager.getFileService().getMetadataFilePath(process);
        try (InputStream fileContent = ServiceManager.getFileService().readMetadataFile(process)) {
            InputStreamReader inputStreamReader = new InputStreamReader(fileContent);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            List<String> textLines = bufferedReader.lines().collect(Collectors.toList());
            String textContent = String.join("", textLines);
            textContent = textContent.replace(ID_PLACEHOLDER, String.valueOf(processId));
            try (OutputStream updatedFileContent = ServiceManager.getFileService().write(metadataFileUri)) {
                updatedFileContent.write(textContent.getBytes());
                // re-save process to update index
                ServiceManager.getProcessService().save(process);
            }
        }
    }

    /**
     * Log title and ID of given test process.
     *
     * @param process test process
     */
    public static void logTestProcessInfo(Process process) {
        logger.info(" ************* ");
        logger.info(" Process '" + process.getTitle() + "' has ID " + process.getId());
        logger.info(" ************* ");
    }
}

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

package org.kitodo.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.primefaces.model.DefaultTreeNode;

public class ProcessTestUtils {

    private static final String TEST_IMAGES_DIR = "images";
    public static final String META_XML = "/meta.xml";
    private static final int TEST_PROJECT_ID = 1;
    private static final int TEST_TEMPLATE_ID = 1;
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
     */
    public static void copyTestFiles(int processId, String filename) throws IOException {
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
     * Copy test metadata xml file with provided 'filename' to process directory of process with provided ID
     * 'processId'. Creates directory if it does not exist.
     * @param processId process ID
     * @param filename filename of metadata file
     * @throws IOException when subdirectory cannot be created or metadata file cannot be copied
     */
    public static void copyTestMetadataFile(int processId, String filename) throws IOException {
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId))
                .toUri();
        URI processDirTargetFile = Paths.get(ConfigCore.getKitodoDataDirectory(), processId
                + META_XML).toUri();
        URI metaFileUri = Paths.get(ConfigCore.getKitodoDataDirectory(), filename).toUri();
        if (!ServiceManager.getFileService().isDirectory(processDir)) {
            ServiceManager.getFileService().createDirectory(Paths.get(ConfigCore.getKitodoDataDirectory()).toUri(),
                    String.valueOf(processId));
        }
        ServiceManager.getFileService().copyFile(metaFileUri, processDirTargetFile);
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
        insertDummyProcesses();
        return MockDatabase.addProcess(processTitle, TEST_PROJECT_ID, TEST_TEMPLATE_ID);
    }

    /**
     * Insert dummy processes into database to avoid conflicts with existing test processes and process directories with
     * static identifiers.
     * @return list of dummy process IDs
     * @throws DAOException when retrieving existing processes from or inserting dummy processes into database fails
     * @throws DataException when inserting dummy processes into database fails
     */
    public static List<Integer> insertDummyProcesses() throws DAOException, DataException {
        List<Integer> dummyProcessIds = new LinkedList<>();
        List<Integer> processIds = ServiceManager.getProcessService().getAll().stream().map(Process::getId)
                .collect(Collectors.toList());
        int id = Collections.max(processIds) + 1;
        while (processDirExists(id)) {
            dummyProcessIds.add(MockDatabase.insertDummyProcess(id));
            id++;
        }
        return dummyProcessIds;
    }

    private static boolean processDirExists(int processId) {
        URI uri = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId)).toUri();
        return ServiceManager.getFileService().isDirectory(uri);
    }
}

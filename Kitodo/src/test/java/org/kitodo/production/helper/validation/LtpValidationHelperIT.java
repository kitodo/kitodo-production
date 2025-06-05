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

package org.kitodo.production.helper.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class LtpValidationHelperIT {

    private static Path VALID_TIF_SOURCE = Paths.get("../Kitodo-LongTermPreservationValidation/src/test/resources/rose.tif");
    private static Path INVALID_TIF_SOURCE = Paths.get("../Kitodo-LongTermPreservationValidation/src/test/resources/corrupted.tif");
    private static Path VALID_JPEG_SOURCE = Paths.get("../Kitodo-LongTermPreservationValidation/src/test/resources/rose.jpg");

    private static Process imageValidationProcess = null;

    /**
     * Function to run before test is executed.
     *
     * @throws Exception
     *         the exception when set up test
     */
    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        cleanFiles();
        copyImagesToFolders();
    }

    /**
     * Check that image validation will iterate over all folders with LTP validation configuration and
     * determine valid and invalid files.
     */
    @Test
    public void shouldValidateAllFoldersForTask() throws DAOException {
        Process process = ServiceManager.getProcessService().getById(1);
        
        // set up validation task
        Task validationTask = new Task();
        validationTask.setProcess(process);
        validationTask.setTypeValidateImages(true);

        // do validation for task
        Map<Folder, Map<URI, LtpValidationResult>> results = LtpValidationHelper.validateImageFoldersForTask(validationTask);

        assertEquals(2, results.keySet().size(), 
            "validation results for task should contain two folders (which are set up with an ltp validation configuration)"
        );

        // identify tif and jpeg folder results
        Folder tifFolder = results.keySet().stream().filter((f) -> f.getMimeType().equals("image/tiff")).findFirst().get();
        Folder jpegFolder = results.keySet().stream().filter((f) -> f.getMimeType().equals("image/jpeg")).findFirst().get();

        assertEquals(2, results.get(tifFolder).size(), "tif folder does not contain exactly 2 files");
        assertEquals(1, results.get(jpegFolder).size(), "jpeg folder does not contain exactly 1 file");

        // identify validation results for each file
        URI validTifUri = results.get(tifFolder).keySet().stream()
            .filter((uri) -> uri.toString().endsWith("00000001.tif")).findFirst().get();
        URI invalidTifUri = results.get(tifFolder).keySet().stream()
            .filter((uri) -> uri.toString().endsWith("00000002.tif")).findFirst().get();
        URI validJpegUri = results.get(jpegFolder).keySet().stream()
            .filter((uri) -> uri.toString().endsWith("00000001.jpg")).findFirst().get();

        assertEquals(LtpValidationResultState.VALID, results.get(tifFolder).get(validTifUri).getState());
        assertEquals(LtpValidationResultState.ERROR, results.get(tifFolder).get(invalidTifUri).getState());
        assertEquals(LtpValidationResultState.VALID, results.get(jpegFolder).get(validJpegUri).getState());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        cleanFiles();
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Return the directory of the process in the Kitodo data directory.
     * 
     * @return the directory of the process in the Kitodo data directory
     * @throws DAOException if retrieving the process fails
     */
    private static Path getProcessDirectory() throws DAOException {
        String dataDirectory = KitodoConfig.getKitodoDataDirectory();
        Path processDirectory = Paths.get(dataDirectory, getImageValidationProcess().getId().toString());
        return processDirectory;
    }

    /**
     * Copy prepared image files (both valid and corrupted) to the corresponding folders in the Kitodo data directory
     * such that they will be validated when triggering the image validation task.
     * 
     * @throws DAOException if retrieving process information fails
     * @throws IOException if copying of files fails
     */
    private static void copyImagesToFolders() throws DAOException, IOException {
        Path tifFolder = Paths.get(getProcessDirectory().toString(), "images/" + getImageValidationProcess().getTitle() + "_media");
        Path jpegFolder = Paths.get(getProcessDirectory().toString(), "jpgs/default");

        // create folders for tif and jpeg files
        Files.createDirectories(tifFolder);
        Files.createDirectories(jpegFolder);

        // copy valid and corrupted files to folders
        Path validTifTarget = Paths.get(tifFolder.toString(), "00000001.tif");
        Path invalidTifTarget = Paths.get(tifFolder.toString(), "00000002.tif");
        Path validJpegTarget = Paths.get(jpegFolder.toString(), "00000001.jpg");

        Files.copy(VALID_TIF_SOURCE, validTifTarget, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(INVALID_TIF_SOURCE, invalidTifTarget, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(VALID_JPEG_SOURCE, validJpegTarget, StandardCopyOption.REPLACE_EXISTING);

        Awaitility.await().until(() -> validTifTarget.toFile().exists());
        Awaitility.await().until(() -> invalidTifTarget.toFile().exists());
        Awaitility.await().until(() -> validJpegTarget.toFile().exists());
    }

    /** 
     * Delete all files that were created by this test.
     */
    private static void cleanFiles() throws DAOException, IOException {
        Path processDirectory = getProcessDirectory();
        if (processDirectory.toFile().exists()) {
            Files.walk(processDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    /**
     * Return the process that is used as a reference for performing the image validation.
     * 
     * @return the process
     * @throws DAOException if retrieving the process fails
     */
    private static Process getImageValidationProcess() throws DAOException {
        if (Objects.isNull(imageValidationProcess)) {
            imageValidationProcess = ServiceManager.getProcessService().getById(1);
        }
        return imageValidationProcess;        
    }
    
}

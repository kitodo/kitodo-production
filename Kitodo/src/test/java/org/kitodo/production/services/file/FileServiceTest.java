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

package org.kitodo.production.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.production.services.ServiceManager;

public class FileServiceTest {

    private static final FileService fileService = new FileService();
    private static final Logger logger = LogManager.getLogger(FileServiceTest.class);
    private static final String OLD_DIRECTORY_NAME = "oldDirectoryName";
    private static final String NEW_DIRECTORY_NAME = "newDirectoryName";

    @BeforeClass
    public static void setUp() throws IOException {
        fileService.createDirectory(URI.create(""), "fileServiceTest");
        URI directory = fileService.createDirectory(URI.create(""), "12");
        fileService.createResource(directory, "meta.xml");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        fileService.delete(URI.create("fileServiceTest"));
        fileService.delete(URI.create("12"));
    }

    @Test
    public void testCreateMetaDirectory() throws IOException, CommandException {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        ExecutionPermission.setExecutePermission(script);

        URI parentFolderUri = URI.create("fileServiceTest");
        URI result = fileService.createMetaDirectory(parentFolderUri, "testMetaScript");
        File file = fileService.getFile((URI.create("fileServiceTest/testMetaScript")));
        ExecutionPermission.setNoExecutePermission(script);

        assertEquals("Result of execution was incorrect!", URI.create((parentFolderUri.getPath()
                + '/' + "testMetaScript")), result);
        assertTrue("Created resource is not directory!", file.isDirectory());
        assertFalse("Created resource is file!", file.isFile());
        assertTrue("Directory was not created!", file.exists());
    }

    @Test
    public void testCreateDirectory() throws IOException {
        URI testMetaUri = fileService.createDirectory(URI.create("fileServiceTest"), "testMeta");
        File file = fileService.getFile(URI.create("fileServiceTest/testMeta"));

        assertTrue("Created resource is not directory!", file.isDirectory());
        assertFalse("Created resource is file!", file.isFile());
        assertTrue("Directory was not created!", file.exists());
        assertTrue("Incorrect path!",
            Paths.get(file.getPath()).toUri().getPath().contains(testMetaUri.getPath()));
    }

    @Test
    public void testCreateDirectoryWithMissingRoot() {
        try {
            fileService.createDirectory(URI.create("fileServiceTestMissing"), "testMeta");
        } catch (IOException e) {
            logger.error("Directory was not created what is expected behaviour. {}", e.getMessage());
        }
        File file = fileService.getFile(URI.create("fileServiceTestMissing/testMeta"));

        assertFalse(file.exists());
    }

    @Test
    public void testCreateDirectoryWithAlreadyExistingDirectory() throws IOException {
        fileService.createDirectory(URI.create("fileServiceTest"), "testMetaExisting");

        File file = fileService.getFile(URI.create("fileServiceTest/testMetaExisting"));
        assertTrue(file.exists());

        URI testMetaUri = fileService.createDirectory(URI.create("fileServiceTest"), "testMetaExisting");
        file = fileService.getFile(URI.create("fileServiceTest/testMetaExisting"));

        assertTrue(file.exists());
        assertTrue("Incorrect path!",
            Paths.get(file.getPath()).toUri().getPath().contains(testMetaUri.getPath()));
    }

    @Test
    public void testCreateDirectoryWithNameOnly() throws IOException {
        URI testMetaNameOnly = fileService.createDirectory(URI.create("fileServiceTest"), "testMetaNameOnly");
        assertTrue(fileService.fileExist(testMetaNameOnly));

        URI uri = URI.create("fileServiceTest/testMetaNameOnly/");
        assertEquals(testMetaNameOnly, uri);

    }

    @Test
    public void testRenameFile() throws IOException {
        URI resource = fileService.createResource(URI.create("fileServiceTest"), "oldName.xml");
        URI oldUri = URI.create("fileServiceTest/oldName.xml");
        assertTrue(fileService.fileExist(oldUri));
        assertEquals(resource, oldUri);

        fileService.renameFile(resource, "newName.xml");
        URI newUri = URI.create("fileServiceTest/newName.xml");
        assertFalse(fileService.fileExist(oldUri));
        assertTrue(fileService.fileExist(newUri));
    }

    @Test
    public void testRenameDirectory() throws Exception {
        URI oldDirUri = fileService.createDirectory(URI.create(""), OLD_DIRECTORY_NAME);
        assertTrue(fileService.isDirectory(oldDirUri));
        URI newDirUri = fileService.renameFile(oldDirUri, NEW_DIRECTORY_NAME);
        assertTrue(fileService.isDirectory(newDirUri));
        assertFalse(fileService.isDirectory(oldDirUri));
        fileService.delete(newDirUri);
    }

    /**
     * Tests searchForMedia function if a MediaNotFoundException is thrown.
     *
     * <p>MediaNotFoundException will be thrown instead of removing file references from workpiece, if no media are
     * present but workpiece contains file references.</p>
     */
    @Test(expected = MediaNotFoundException.class)
    public void testSearchForMedia()
            throws MediaNotFoundException, IOException, InvalidImagesException, URISyntaxException {
        Process process = mock(Process.class);
        Project project = mock(Project.class);
        Folder folder = mock(Folder.class);

        String processBasePath = "/usr/local/kitodo/metadata/231263";

        when(folder.getFileGroup()).thenReturn("LOCAL");
        when(folder.getPath()).thenReturn(processBasePath + "images");
        when(folder.getMimeType()).thenReturn("image/tiff");
        when(project.getFolders()).thenReturn(Collections.singletonList(folder));
        when(process.getProject()).thenReturn(project);

        when(process.getProcessBaseUri()).thenReturn(new URI(processBasePath));
        URI testmeta = Paths.get("./src/test/resources/metadata/metadataFiles/testmeta.xml").toUri();
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(testmeta);

        fileService.searchForMedia(process, workpiece);
    }

    @Test(expected = IOException.class)
    public void testRenameFileWithExistingTarget() throws IOException {
        FileService fileService = new FileService();

        URI oldUri = fileService.createResource(URI.create("fileServiceTest"), "oldName.xml");
        URI newUri = fileService.createResource(URI.create("fileServiceTest"), "newName.xml");
        assertTrue(fileService.fileExist(oldUri));
        assertTrue(fileService.fileExist(newUri));

        fileService.renameFile(oldUri, "newName.xml");
    }

    @Test(expected = FileNotFoundException.class)
    public void testRenameFileWithMissingSource() throws IOException {
        URI oldUri = URI.create("fileServiceTest/oldNameMissing.xml");
        assertFalse(fileService.fileExist(oldUri));

        fileService.renameFile(oldUri, "newName.xml");
    }

    @Test
    public void testGetNumberOfFiles() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "countFiles0");
        fileService.createResource(directory, "test.xml");
        fileService.createResource(directory, "test2.xml");

        int numberOfFiles = fileService.getNumberOfFiles(directory);

        assertEquals(2, numberOfFiles);

    }

    @Test
    public void testGetNumberOfFilesWithSubDirectory() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "countFiles1");
        fileService.createResource(directory, "test.pdf");
        URI subDirectory = fileService.createDirectory(directory, "subdirectory");
        fileService.createResource(subDirectory, "subTest.xml");
        fileService.createResource(subDirectory, "subTest2.jpg");

        int numberOfFiles = fileService.getNumberOfFiles(directory);

        assertEquals(3, numberOfFiles);

    }

    @Test
    public void testGetNumberOfImageFiles() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "countFiles2");
        fileService.createResource(directory, "test.pdf");
        fileService.createResource(directory, "subTest.xml");
        fileService.createResource(directory, "subTest2.jpg");

        int numberOfFiles = fileService.getNumberOfImageFiles(directory);

        assertEquals(1, numberOfFiles);

    }

    @Test
    public void testGetNumberOfImageFilesWithSubDirectory() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "countFiles3");
        fileService.createResource(directory, "test.pdf");
        URI subDirectory = fileService.createDirectory(directory, "subdirectory");
        fileService.createResource(subDirectory, "subTest.xml");
        fileService.createResource(subDirectory, "subTest2.jpg");

        int numberOfFiles = fileService.getNumberOfImageFiles(directory);

        assertEquals(1, numberOfFiles);

    }

    @Test
    public void testCopyDirectory() throws IOException {
        URI fromDirectory = fileService.createDirectory(URI.create("fileServiceTest"), "copyDirectory");
        fileService.createResource(fromDirectory, "test.pdf");
        URI toDirectory = URI.create("fileServiceTest/copyDirectoryTo/");

        assertFalse(fileService.fileExist(toDirectory));

        fileService.copyDirectory(fromDirectory, toDirectory);

        assertTrue(fileService.fileExist(toDirectory));
        assertTrue(fileService.fileExist(toDirectory.resolve("test.pdf")));
        assertTrue(fileService.fileExist(fromDirectory));

    }

    @Test(expected = FileNotFoundException.class)
    public void testCopyDirectoryWithMissingSource() throws IOException {
        URI fromDirectory = URI.create("fileServiceTest/copyDirectoryNotExisting/");
        URI toDirectory = URI.create("fileServiceTest/copyDirectoryNotExistingTo/");

        assertFalse(fileService.fileExist(fromDirectory));
        assertFalse(fileService.fileExist(toDirectory));

        fileService.copyDirectory(fromDirectory, toDirectory);

    }

    @Test
    public void testCopyDirectoryWithExistingTarget() throws IOException {
        URI fromDirectory = fileService.createDirectory(URI.create("fileServiceTest"), "copyDirectoryExistingTarget");
        fileService.createResource(fromDirectory, "testToCopy.pdf");

        URI toDirectory = fileService.createDirectory(URI.create("fileServiceTest"), "copyDirectoryNotExistingTarget");
        fileService.createResource(toDirectory, "testExisting.pdf");

        assertTrue(fileService.fileExist(fromDirectory));
        assertTrue(fileService.fileExist(toDirectory));

        fileService.copyDirectory(fromDirectory, toDirectory);

        assertTrue(fileService.fileExist(toDirectory));
        assertTrue(fileService.fileExist(toDirectory.resolve("testToCopy.pdf")));
        assertTrue(fileService.fileExist(toDirectory.resolve("testExisting.pdf")));
        assertTrue(fileService.fileExist(fromDirectory));
        assertTrue(fileService.fileExist(fromDirectory.resolve("testToCopy.pdf")));
        assertFalse(fileService.fileExist(fromDirectory.resolve("testExisting.pdf")));

    }

    @Test
    public void testCopyFile() throws IOException {
        URI originFile = fileService.createResource(URI.create("fileServiceTest"), "copyFile");
        URI targetFile = URI.create("fileServiceTest/copyFileTarget");

        assertTrue(fileService.fileExist(originFile));
        assertFalse(fileService.fileExist(targetFile));

        fileService.copyFile(originFile, targetFile);

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetFile));

    }

    @Test(expected = FileNotFoundException.class)
    public void testCopyFileWithMissingSource() throws IOException {
        URI originFile = URI.create("fileServiceTest/copyFileMissing");
        URI targetFile = URI.create("fileServiceTest/copyFileTargetMissing");

        assertFalse(fileService.fileExist(originFile));
        assertFalse(fileService.fileExist(targetFile));

        fileService.copyFile(originFile, targetFile);

    }

    @Test
    public void testCopyFileWithExistingTarget() throws IOException {
        URI originFile = fileService.createResource(URI.create("fileServiceTest"), "copyFileExisting.txt");
        URI targetFile = fileService.createResource(URI.create("fileServiceTest"), "copyFileExistingTarget.txt");

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetFile));

        fileService.copyFile(originFile, targetFile);

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetFile));

    }

    @Test
    public void testCopyFileToDirectory() throws IOException {
        URI originFile = fileService.createResource(URI.create("fileServiceTest"), "copyFileToDirectory.txt");
        URI targetDirectory = fileService.createDirectory(URI.create("fileServiceTest"), "copyFileToDirectoryTarget");

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetDirectory));
        assertFalse(fileService.fileExist(targetDirectory.resolve("copyFileToDirectory.txt")));

        fileService.copyFileToDirectory(originFile, targetDirectory);

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetDirectory.resolve("copyFileToDirectory.txt")));

    }

    @Test
    public void testCopyFileToDirectoryWithMissingDirectory() throws IOException {
        URI originFile = fileService.createResource(URI.create("fileServiceTest"), "copyFileToDirectoryMissing");
        URI targetDirectory = URI.create("fileServiceTest/copyFileToDirectoryMissingTarget/");

        assertTrue(fileService.fileExist(originFile));
        assertFalse(fileService.fileExist(targetDirectory));
        assertFalse(fileService.fileExist(targetDirectory.resolve("copyFileToDirectoryMissing")));

        fileService.copyFileToDirectory(originFile, targetDirectory);

        assertTrue(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetDirectory.resolve("copyFileToDirectoryMissing")));

    }

    @Test(expected = FileNotFoundException.class)
    public void testCopyFileToDirectoryWithMissingSource() throws IOException {
        URI originFile = URI.create("fileServiceTest/copyFileToDirectoryMissingSource");
        URI targetDirectory = fileService.createDirectory(URI.create("fileServiceTest"),
            "copyFileToDirectoryMissingSourceTarget");

        assertFalse(fileService.fileExist(originFile));
        assertTrue(fileService.fileExist(targetDirectory));
        assertFalse(fileService.fileExist(targetDirectory.resolve("copyFileToDirectoryMissingSource")));

        fileService.copyFileToDirectory(originFile, targetDirectory);

    }

    @Test
    public void testDeleteFile() throws IOException {
        URI originFile = fileService.createResource(URI.create("fileServiceTest"), "deleteFile");
        assertTrue(fileService.fileExist(originFile));

        fileService.delete(originFile);
        assertFalse(fileService.fileExist(originFile));
    }

    @Test
    public void testDeleteDirectory() throws IOException {
        URI originFile = fileService.createDirectory(URI.create("fileServiceTest"), "deleteDirectory");
        assertTrue(fileService.fileExist(originFile));

        fileService.delete(originFile);
        assertFalse(fileService.fileExist(originFile));
    }

    @Test
    public void testDeleteWithNotExisting() throws IOException {
        URI originFile = URI.create("fileServiceTest/deleteNotExisting");
        assertFalse(fileService.fileExist(originFile));

        boolean delete = fileService.delete(originFile);
        assertFalse(fileService.fileExist(originFile));
        assertTrue(delete);
    }

    @Test
    public void testFileExist() throws IOException {
        URI notExisting = URI.create("fileServiceTest/fileExists");
        assertFalse(fileService.fileExist(notExisting));

        URI existing = fileService.createResource(URI.create("fileServiceTest"), "fileExists");
        assertTrue(fileService.fileExist(existing));

    }

    @Test
    public void testGetFileName() throws IOException {
        URI existing = fileService.createResource(URI.create("fileServiceTest"), "fileName.xml");

        String fileName = fileService.getFileName(existing);

        assertEquals("fileName", fileName);
    }

    @Test
    public void testGetFileNameWithMultipleDots() throws IOException {
        URI existing = fileService.createResource(URI.create("fileServiceTest"), "fileName.with.dots.xml");

        String fileName = fileService.getFileName(existing);

        assertEquals("fileName.with.dots", fileName);
    }

    @Test
    public void testGetFileNameFromDirectory() throws IOException {
        URI existing = fileService.createDirectory(URI.create("fileServiceTest"), "directoryName");

        String fileName = fileService.getFileName(existing);

        assertEquals("", fileName);
    }

    @Test
    public void testGetFileNameFromNotExisting() {
        URI notExisting = URI.create("fileServiceTest/fileName.xml");

        String fileName = fileService.getFileName(notExisting);

        assertEquals("fileName", fileName);
    }

    @Test
    public void testMoveDirectory() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "movingDirectory");
        fileService.createResource(directory, "test.xml");
        URI target = URI.create("fileServiceTest/movingDirectoryTarget/");

        assertTrue(fileService.fileExist(directory));
        assertTrue(fileService.fileExist(directory.resolve("test.xml")));
        assertFalse(fileService.fileExist(target));

        fileService.moveDirectory(directory, target);

        assertFalse(fileService.fileExist(directory));
        assertFalse(fileService.fileExist(directory.resolve("test.xml")));
        assertTrue(fileService.fileExist(target));
        assertTrue(fileService.fileExist(target.resolve("test.xml")));

    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveDirectoryWithMissingSource() throws IOException {
        URI directory = URI.create("fileServiceTest/movingDirectoryMissing/");
        URI target = URI.create("fileServiceTest/movingDirectoryMissingTarget/");

        assertFalse(fileService.fileExist(directory));
        assertFalse(fileService.fileExist(target));

        fileService.moveDirectory(directory, target);

    }

    @Test
    public void testMoveDirectoryWithExistingTarget() throws IOException {
        URI directory = fileService.createDirectory(URI.create("fileServiceTest"), "movingDirectoryTargetMissing");
        fileService.createResource(directory, "test.xml");
        URI target = fileService.createDirectory(URI.create("fileServiceTest"), "movingTargetMissing");
        fileService.createResource(target, "testTarget.xml");

        assertTrue(fileService.fileExist(directory));
        assertTrue(fileService.fileExist(directory.resolve("test.xml")));
        assertTrue(fileService.fileExist(target));
        assertTrue(fileService.fileExist(target.resolve("testTarget.xml")));

        fileService.moveDirectory(directory, target);

        assertFalse(fileService.fileExist(directory));
        assertFalse(fileService.fileExist(directory.resolve("test.xml")));
        assertTrue(fileService.fileExist(target));
        assertTrue(fileService.fileExist(target.resolve("test.xml")));
        assertTrue(fileService.fileExist(target.resolve("testTarget.xml")));

    }

    @Test
    public void testMoveFile() throws IOException {
        URI file = fileService.createResource(URI.create("fileServiceTest"), "movingFile");
        URI target = URI.create("fileServiceTest/movingFileTarget");

        assertTrue(fileService.fileExist(file));
        assertFalse(fileService.fileExist(target));

        fileService.moveFile(file, target);

        assertFalse(fileService.fileExist(file));
        assertTrue(fileService.fileExist(target));

    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveFileWithMissingSource() throws IOException {
        URI file = URI.create("fileServiceTest/movingFileMissing");
        URI target = URI.create("fileServiceTest/movingFileMissingTarget");

        assertFalse(fileService.fileExist(file));
        assertFalse(fileService.fileExist(target));

        fileService.moveDirectory(file, target);

    }

    @Test
    public void testMoveFileWithExistingTarget() throws IOException {
        URI file = fileService.createDirectory(URI.create("fileServiceTest"), "movingFileTargetMissing");
        URI target = fileService.createDirectory(URI.create("fileServiceTest"), "movingFileTargetMissingTarget");

        assertTrue(fileService.fileExist(file));
        assertTrue(fileService.fileExist(target));

        fileService.moveDirectory(file, target);

        assertFalse(fileService.fileExist(file));
        assertTrue(fileService.fileExist(target));

    }

    @Test
    public void testCreateBackupFile() throws IOException {
        Process process = new Process();
        process.setId(12);
        process.setProcessBaseUri(URI.create("12"));

        assertFalse(fileService.fileExist(URI.create("12/meta.xml.1")));
        assertFalse(fileService.fileExist(URI.create("12/meta.xml.2")));

        fileService.createBackupFile(process);

        assertTrue(fileService.fileExist(URI.create("12/meta.xml.1")));
        assertFalse(fileService.fileExist(URI.create("12/meta.xml.2")));

        fileService.createResource(URI.create("12"), "meta.xml");
        fileService.createBackupFile(process);

        assertTrue(fileService.fileExist(URI.create("12/meta.xml.1")));
        assertTrue(fileService.fileExist(URI.create("12/meta.xml.2")));

        // No third backup file is created, when numberOfBackups is set to two
        fileService.createResource(URI.create("12"), "meta.xml");
        fileService.createBackupFile(process);

        assertTrue(fileService.fileExist(URI.create("12/meta.xml.1")));
        assertTrue(fileService.fileExist(URI.create("12/meta.xml.2")));
        assertFalse(fileService.fileExist(URI.create("12/meta.xml.3")));
    }

    @Test
    public void testDeleteFirstSlashFromPath() {
        URI uri = URI.create("/test/test");
        URI actualUri = fileService.deleteFirstSlashFromPath(uri);
        assertEquals("Paths of Uri did not match", "test/test", actualUri.getPath());
    }

    @Test
    public void shouldCreateSymLink() throws IOException {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        URI symLinkSource = URI.create("symLinkSource");
        URI symLinkTarget = URI.create("symLinkTarget");

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_SYMLINK));
        URI directory = fileService.createDirectory(URI.create(""), "symLinkSource");
        fileService.createResource(directory, "meta.xml");
        User user = new User();
        user.setLogin(SystemUtils.USER_NAME);
        ExecutionPermission.setExecutePermission(script);
        boolean result = fileService.createSymLink(symLinkSource, symLinkTarget, false, user);
        ExecutionPermission.setNoExecutePermission(script);
        assertTrue("Create symbolic link has failed!", result);

        File scriptClean = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_DELETE_SYMLINK));
        ExecutionPermission.setExecutePermission(scriptClean);
        fileService.deleteSymLink(symLinkTarget);
        ExecutionPermission.setNoExecutePermission(scriptClean);
        fileService.delete(symLinkSource);
        fileService.delete(symLinkTarget);
    }

    @Test
    public void shouldDeleteSymLink() throws IOException {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        URI symLinkSource = URI.create("symLinkSource");
        URI symLinkTarget = URI.create("symLinkTarget");

        File scriptPrepare = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_SYMLINK));
        URI directory = fileService.createDirectory(URI.create(""), "symLinkSource");
        fileService.createResource(directory, "meta.xml");
        User user = new User();
        user.setLogin(SystemUtils.USER_NAME);
        ExecutionPermission.setExecutePermission(scriptPrepare);
        fileService.createSymLink(symLinkSource, symLinkTarget, false, user);
        ExecutionPermission.setNoExecutePermission(scriptPrepare);

        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_DELETE_SYMLINK));
        ExecutionPermission.setExecutePermission(script);
        boolean result = fileService.deleteSymLink(symLinkTarget);
        ExecutionPermission.setNoExecutePermission(script);
        assertTrue("Delete symbolic link has failed!", result);

        fileService.delete(symLinkSource);
        fileService.delete(symLinkTarget);
    }

}

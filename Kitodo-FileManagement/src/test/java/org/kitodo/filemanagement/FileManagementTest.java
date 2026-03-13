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

package org.kitodo.filemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.config.KitodoConfig;

public class FileManagementTest {

    private static final FileManagement fileManagement = new FileManagement();

    private static final String FILE_TEST = "fileTest";
    private static final String DIRECTORY_SIZE = "directorySize";
    private static final String SYMLINK_SOURCE = "symLinkSource";
    private static final String SYMLINK_TARGET = "symLinkTarget";
    private static final String FILE_NOT_CREATED = "File not created";

    private static final File script = new File(KitodoConfig.getParameter("script_createDirMeta"));

    @BeforeAll
    public static void setUp() throws IOException {
        fileManagement.create(URI.create(""), FILE_TEST, false);
        fileManagement.create(URI.create(""), DIRECTORY_SIZE, false);
        URI directory = fileManagement.create(URI.create(""), "2", false);
        fileManagement.create(directory, "meta.xml", true);
        ExecutionPermission.setExecutePermission(script);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        fileManagement.delete(URI.create(FILE_TEST));
        fileManagement.delete(URI.create(DIRECTORY_SIZE));
        fileManagement.delete(URI.create("2"));
        ExecutionPermission.setNoExecutePermission(script);
    }

    @Test
    public void shouldCreateDirectory() throws IOException {
        URI testDirectory = fileManagement.create(URI.create(FILE_TEST), "testDirectory", false);
        assertTrue(fileManagement.isDirectory(testDirectory), "Directory not created");
        assertTrue(fileManagement.fileExist(testDirectory), "Directory not created");
    }

    @Test
    public void shouldCreateResource() throws IOException {
        URI testDirectory = fileManagement.create(URI.create(FILE_TEST), "newResource.xml", true);
        assertTrue(fileManagement.fileExist(testDirectory), FILE_NOT_CREATED);
    }

    @Test
    public void shouldRead() throws IOException {
        int testContent = 8;

        URI testRead = fileManagement.create(URI.create(FILE_TEST), "testRead.txt", true);
        try (OutputStream outputStream = fileManagement.write(testRead)) {
            outputStream.write(testContent);
        }

        InputStream inputStream = fileManagement.read(testRead);
        assertEquals(testContent, inputStream.read(), "Did not read right content");

        inputStream.close();
    }

    @Test
    public void shouldWrite() throws IOException {
        int testContent = 7;

        URI testWrite = fileManagement.create(URI.create(FILE_TEST), "testWrite.txt", true);

        OutputStream outputStream = fileManagement.write(testWrite);
        try {
            outputStream.write(testContent);
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        InputStream inputStream = fileManagement.read(testWrite);
        assertEquals(testContent, inputStream.read(), "Did not write right content");

        inputStream.close();
    }

    @Test
    public void shouldCanRead() {
        assertTrue(fileManagement.canRead(URI.create(FILE_TEST)), "URI cannot be read!");
    }

    @Test
    public void shouldGetNumberOfFiles() {
        int numberOfFiles = fileManagement.getNumberOfFiles(null, URI.create("2"));
        assertEquals(1, numberOfFiles, "URI cannot be read!");
    }

    @Test
    public void shouldCreateUriForExistingProcess() {
        assertEquals(URI.create("10"), fileManagement.createUriForExistingProcess("10"), "URI cannot be created!");
    }

    @Test
    public void shouldRenameFile() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "oldName.xml", true);
        URI oldUri = URI.create("fileTest/oldName.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertEquals(resource, oldUri);

        fileManagement.rename(resource, "newName.xml");
        URI newUri = URI.create("fileTest/newName.xml");
        assertFalse(fileManagement.fileExist(oldUri));
        assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldSkipRenamingDirectory() throws Exception {
        String directoryName = "testDir";
        URI resource = fileManagement.create(URI.create(""), directoryName, false);
        Assumptions.assumeTrue(fileManagement.isDirectory(resource));
        URI expectedUri = new File(KitodoConfig.getKitodoDataDirectory() + resource).toURI();
        assertEquals(expectedUri, fileManagement.rename(resource, directoryName), "Renaming directory to the identical name should return identical URI");
        String directoryWithTrailingSlash = directoryName + "/";
        assertEquals(expectedUri, fileManagement.rename(resource, directoryWithTrailingSlash), "Renaming directory to the identical name with trailing slash should return identical URI");
        fileManagement.delete(resource);
    }

    @Test
    public void shouldCopyDirectory() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "toCopy", false);
        URI file = fileManagement.create(resource, "fileToCopy.xml", true);
        URI oldUri = URI.create("fileTest/toCopy/fileToCopy.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertEquals(file, oldUri);

        fileManagement.copy(resource, URI.create("fileTest/copiedDirectory"));
        URI newUri = URI.create("fileTest/copiedDirectory/fileToCopy.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldCopyFile() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "fileToCopy.xml", true);
        URI oldUri = URI.create("fileTest/fileToCopy.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertEquals(resource, oldUri);

        fileManagement.copy(resource, URI.create("fileTest/copiedFile.xml"));
        URI newUri = URI.create("fileTest/copiedFile.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldCopyFileToDirectory() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "fileToCopy.xml", true);
        URI oldUri = URI.create("fileTest/fileToCopy.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertEquals(resource, oldUri);

        fileManagement.copy(resource, URI.create("fileTest/newDirectory/"));
        URI newUri = URI.create("fileTest/newDirectory/fileToCopy.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldDeleteFile() throws IOException {
        URI fileForDeletion = fileManagement.create(URI.create(""), "testDelete.txt", true);
        assertTrue(fileManagement.fileExist(fileForDeletion), FILE_NOT_CREATED);

        fileManagement.delete(fileForDeletion);
        assertFalse(fileManagement.fileExist(fileForDeletion), "File not deleted");
        assertTrue(fileManagement.fileExist(URI.create(FILE_TEST)), "File should not be deleted");

        Exception exception = assertThrows(IOException.class,
            () -> fileManagement.delete(new URI(""))
        );
        String expectedMessage = "Attempt to delete subdirectory with URI that is empty or null!";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void shouldMoveDirectory() throws IOException {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "testMove", false);
        URI file = fileManagement.create(resource, "testMove.txt", true);
        assertTrue(fileManagement.fileExist(file), FILE_NOT_CREATED);

        fileManagement.move(resource, URI.create("fileTest/moved"));
        URI movedFile = URI.create("fileTest/moved/testMove.txt");
        assertFalse(fileManagement.fileExist(resource), "Directory not deleted");
        assertTrue(fileManagement.fileExist(movedFile), "Directory not moved");
    }

    @Test
    public void shouldMoveFile() throws IOException {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "testMove.txt", true);
        assertTrue(fileManagement.fileExist(resource), FILE_NOT_CREATED);

        URI movedFile = URI.create("fileTest/moved.txt");
        fileManagement.move(resource, movedFile);
        assertFalse(fileManagement.fileExist(resource), "File not deleted");
        assertTrue(fileManagement.fileExist(movedFile), "File not moved");
    }

    @Test
    public void shouldDeleteDirectory() throws IOException {
        URI directory = fileManagement.create(URI.create(""), "testDelete", false);

        assertTrue(fileManagement.fileExist(directory), "Directory not created");
        assertTrue(fileManagement.isDirectory(directory), "Directory is not a directory");

        fileManagement.delete(directory);
        assertFalse(fileManagement.fileExist(directory), "Directory not deleted");
    }

    @Test
    public void shouldGetSizeOfDirectory() throws Exception {
        int testContent = 156575;

        URI resource = fileManagement.create(URI.create(DIRECTORY_SIZE), "size.txt", true);
        assertTrue(fileManagement.fileExist(resource));

        try (OutputStream outputStream = fileManagement.write(URI.create("directorySize/size.txt"))) {
            outputStream.write(testContent);
        }

        long directorySize = fileManagement.getSizeOfDirectory(URI.create(DIRECTORY_SIZE));
        assertEquals(1, directorySize, "Incorrect size of directory");
    }

    @Test
    public void shouldGetFileNameWithExtension() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "fileName.xml", true);
        assertTrue(fileManagement.fileExist(resource));

        String fileName = fileManagement.getFileNameWithExtension(resource);
        assertEquals("fileName.xml", fileName, "File name is incorrect!");
    }

    @Test
    public void shouldGetSubUris() throws Exception {
        URI directory = fileManagement.create(URI.create(FILE_TEST), "testSub", false);
        URI firstSub = fileManagement.create(directory, "first.txt", true);
        URI secondSub = fileManagement.create(directory, "second.xml", true);
        URI thirdSub = fileManagement.create(directory, "third.jpg", true);

        List<URI> subUris = fileManagement.getSubUris(null, directory);
        Collections.sort(subUris);
        assertEquals(subUris.get(0), firstSub);
        assertEquals(subUris.get(1), secondSub);
        assertEquals(subUris.get(2), thirdSub);

        FilenameFilter filter = new FileNameEndsWithFilter(".xml");
        List<URI> subUrisWithFilter = fileManagement.getSubUris(filter, directory);
        assertEquals(1, subUrisWithFilter.size());
        assertEquals(secondSub, subUrisWithFilter.getFirst());
    }

    @Test
    public void testCreateProcessLocation() throws IOException {
        String processFolder = "testProcess";

        URI processLocation = fileManagement.createProcessLocation("testProcess");
        assertTrue(processLocation.toString().contains(processFolder), "wrong processLocation");

        fileManagement.delete(processLocation);
    }

    @Test
    public void shouldGetProcessSubTypeURI() {
        URI processSubTypeURI = fileManagement.getProcessSubTypeUri(URI.create("1"), "test", ProcessSubType.IMAGE, "");
        assertEquals(URI.create("1/images/"), processSubTypeURI, "Process subtype URI was incorrectly generated!");

        processSubTypeURI = fileManagement.getProcessSubTypeUri(URI.create("1"), "test", ProcessSubType.OCR_ALTO, "");
        assertEquals(URI.create("1/ocr/test_alto/"), processSubTypeURI, "Process subtype URI was incorrectly generated!");
    }

    @Test
    public void shouldCreateSymLink() throws IOException {
        Assumptions.assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);
        URI symLinkSource = URI.create(SYMLINK_SOURCE);
        URI symLinkTarget = URI.create(SYMLINK_TARGET);

        File script = new File(KitodoConfig.getParameter("script_createSymLink"));
        URI directory = fileManagement.create(URI.create(""), SYMLINK_SOURCE, false);
        fileManagement.create(directory, "meta.xml", true);
        setFileExecutable(script);
        boolean result = fileManagement.createSymLink(symLinkSource, symLinkTarget, false, SystemUtils.USER_NAME);
        setFileNotExecutable(script);
        assertTrue(result, "Create symbolic link has failed!");

        File scriptClean = new File(KitodoConfig.getParameter("script_deleteSymLink"));
        setFileExecutable(scriptClean);
        fileManagement.deleteSymLink(symLinkTarget);
        setFileNotExecutable(scriptClean);
        fileManagement.delete(symLinkSource);
        fileManagement.delete(symLinkTarget);
    }

    @Test
    public void shouldDeleteSymLink() throws IOException {
        Assumptions.assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        URI symLinkSource = URI.create(SYMLINK_SOURCE);
        URI symLinkTarget = URI.create(SYMLINK_TARGET);

        File scriptPrepare = new File(KitodoConfig.getParameter("script_createSymLink"));
        URI directory =  fileManagement.create(URI.create(""), SYMLINK_SOURCE, false);
        fileManagement.create(directory, "meta.xml", true);
        setFileExecutable(scriptPrepare);
        fileManagement.createSymLink(symLinkSource, symLinkTarget, false, SystemUtils.USER_NAME);
        setFileNotExecutable(scriptPrepare);

        File script = new File(KitodoConfig.getParameter("script_deleteSymLink"));
        setFileExecutable(script);
        boolean result = fileManagement.deleteSymLink(symLinkTarget);
        setFileNotExecutable(script);
        assertTrue(result, "Delete symbolic link has failed!");

        fileManagement.delete(symLinkSource);
        fileManagement.delete(symLinkTarget);
    }

    private static void setFileExecutable(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Files.setPosixFilePermissions(file.toPath(), perms);
    }

    private static void setFileNotExecutable(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);

        Files.setPosixFilePermissions(file.toPath(), perms);
    }
}

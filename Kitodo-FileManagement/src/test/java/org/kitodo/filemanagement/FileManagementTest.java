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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws IOException {
        fileManagement.create(URI.create(""), FILE_TEST, false);
        fileManagement.create(URI.create(""), DIRECTORY_SIZE, false);
        URI directory = fileManagement.create(URI.create(""), "2", false);
        fileManagement.create(directory, "meta.xml", true);
        ExecutionPermission.setExecutePermission(script);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        fileManagement.delete(URI.create(FILE_TEST));
        fileManagement.delete(URI.create(DIRECTORY_SIZE));
        fileManagement.delete(URI.create("2"));
        ExecutionPermission.setNoExecutePermission(script);
    }

    @Test
    public void shouldCreateDirectory() throws IOException {
        URI testDirectory = fileManagement.create(URI.create(FILE_TEST), "testDirectory", false);
        assertTrue("Directory not created", fileManagement.isDirectory(testDirectory));
        assertTrue("Directory not created", fileManagement.fileExist(testDirectory));
    }

    @Test
    public void shouldCreateResource() throws IOException {
        URI testDirectory = fileManagement.create(URI.create(FILE_TEST), "newResource.xml", true);
        assertTrue(FILE_NOT_CREATED, fileManagement.fileExist(testDirectory));
    }

    @Test
    public void shouldRead() throws IOException {
        int testContent = 8;

        URI testRead = fileManagement.create(URI.create(FILE_TEST), "testRead.txt", true);
        try (OutputStream outputStream = fileManagement.write(testRead)) {
            outputStream.write(testContent);
        }

        InputStream inputStream = fileManagement.read(testRead);
        assertEquals("Did not read right content", testContent, inputStream.read());

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
        assertEquals("Did not write right content", testContent, inputStream.read());

        inputStream.close();
    }

    @Test
    public void shouldCanRead() {
        assertTrue("URI cannot be read!", fileManagement.canRead(URI.create(FILE_TEST)));
    }

    @Test
    public void shouldGetNumberOfFiles() {
        int numberOfFiles = fileManagement.getNumberOfFiles(null, URI.create("2"));
        assertEquals("URI cannot be read!", 1, numberOfFiles);
    }

    @Test
    public void shouldCreateUriForExistingProcess() {
        assertEquals("URI cannot be created!", URI.create("10"), fileManagement.createUriForExistingProcess("10"));
    }

    @Test
    public void shouldRenameFile() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "oldName.xml", true);
        URI oldUri = URI.create("fileTest/oldName.xml");
        assertTrue(fileManagement.fileExist(oldUri));
        assertEquals(resource, oldUri);

        fileManagement.rename(resource, "newName.xml");
        URI newUri = URI.create("fileTest/newName.xml");
        Assert.assertFalse(fileManagement.fileExist(oldUri));
        assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldSkipRenamingDirectory() throws Exception {
        String directoryName = "testDir";
        URI resource = fileManagement.create(URI.create(""), directoryName, false);
        assumeTrue(fileManagement.isDirectory(resource));
        URI expectedUri = new File(KitodoConfig.getKitodoDataDirectory() + resource).toURI();
        assertEquals("Renaming directory to the identical name should return identical URI", expectedUri,
                fileManagement.rename(resource, directoryName));
        String directoryWithTrailingSlash = directoryName + "/";
        assertEquals("Renaming directory to the identical name with trailing slash should return identical URI",
                expectedUri, fileManagement.rename(resource, directoryWithTrailingSlash));
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
    public void shouldDeleteFile() throws URISyntaxException, IOException {
        URI fileForDeletion = fileManagement.create(URI.create(""), "testDelete.txt", true);
        assertTrue(FILE_NOT_CREATED, fileManagement.fileExist(fileForDeletion));

        fileManagement.delete(fileForDeletion);
        Assert.assertFalse("File not deleted", fileManagement.fileExist(fileForDeletion));
        assertTrue("File should not be deleted", fileManagement.fileExist(URI.create(FILE_TEST)));

        exception.expect(IOException.class);
        exception.expectMessage("Attempt to delete subdirectory with URI that is empty or null!");
        fileManagement.delete(new URI(""));

    }

    @Test
    public void shouldMoveDirectory() throws IOException {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "testMove", false);
        URI file = fileManagement.create(resource, "testMove.txt", true);
        assertTrue(FILE_NOT_CREATED, fileManagement.fileExist(file));

        fileManagement.move(resource, URI.create("fileTest/moved"));
        URI movedFile = URI.create("fileTest/moved/testMove.txt");
        Assert.assertFalse("Directory not deleted", fileManagement.fileExist(resource));
        assertTrue("Directory not moved", fileManagement.fileExist(movedFile));
    }

    @Test
    public void shouldMoveFile() throws IOException {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "testMove.txt", true);
        assertTrue(FILE_NOT_CREATED, fileManagement.fileExist(resource));

        URI movedFile = URI.create("fileTest/moved.txt");
        fileManagement.move(resource, movedFile);
        Assert.assertFalse("File not deleted", fileManagement.fileExist(resource));
        assertTrue("File not moved", fileManagement.fileExist(movedFile));
    }

    @Test
    public void shouldDeleteDirectory() throws IOException {
        URI directory = fileManagement.create(URI.create(""), "testDelete", false);

        assertTrue("Directory not created", fileManagement.fileExist(directory));
        assertTrue("Directory is not a directory", fileManagement.isDirectory(directory));

        fileManagement.delete(directory);
        Assert.assertFalse("Directory not deleted", fileManagement.fileExist(directory));
    }

    @Test
    public void shouldGetSizeOfDirectory() throws Exception {
        int testContent = 156575;

        URI resource = fileManagement.create(URI.create(DIRECTORY_SIZE), "size.txt", true);
        assertTrue(fileManagement.fileExist(resource));

        try (OutputStream outputStream = fileManagement.write(URI.create("directorySize/size.txt"))){
            outputStream.write(testContent);
        }

        long directorySize = fileManagement.getSizeOfDirectory(URI.create(DIRECTORY_SIZE));
        assertEquals("Incorrect size of directory", 1, directorySize);
    }

    @Test
    public void shouldGetFileNameWithExtension() throws Exception {
        URI resource = fileManagement.create(URI.create(FILE_TEST), "fileName.xml", true);
        assertTrue(fileManagement.fileExist(resource));

        String fileName = fileManagement.getFileNameWithExtension(resource);
        assertEquals("File name is incorrect!", "fileName.xml", fileName);
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
        assertEquals(secondSub, subUrisWithFilter.get(0));
    }

    @Test
    public void testCreateProcessLocation() throws IOException {
        String processFolder = "testProcess";

        URI processLocation = fileManagement.createProcessLocation("testProcess");
        assertTrue("wrong processLocation", processLocation.toString().contains(processFolder));

        fileManagement.delete(processLocation);
    }

    @Test
    public void shouldGetProcessSubTypeURI() {
        URI processSubTypeURI = fileManagement.getProcessSubTypeUri(URI.create("1"), "test", ProcessSubType.IMAGE, "");
        assertEquals("Process subtype URI was incorrectly generated!", URI.create("1/images/"), processSubTypeURI);

        processSubTypeURI = fileManagement.getProcessSubTypeUri(URI.create("1"), "test", ProcessSubType.OCR_ALTO, "");
        assertEquals("Process subtype URI was incorrectly generated!", URI.create("1/ocr/test_alto/"), processSubTypeURI);
    }

    @Test
    public void shouldCreateSymLink() throws IOException {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);
        URI symLinkSource = URI.create(SYMLINK_SOURCE);
        URI symLinkTarget = URI.create(SYMLINK_TARGET);

        File script = new File(KitodoConfig.getParameter("script_createSymLink"));
        URI directory = fileManagement.create(URI.create(""), SYMLINK_SOURCE, false);
        fileManagement.create(directory, "meta.xml", true);
        setFileExecutable(script);
        boolean result = fileManagement.createSymLink(symLinkSource, symLinkTarget, false, SystemUtils.USER_NAME);
        setFileNotExecutable(script);
        assertTrue("Create symbolic link has failed!", result);

        File scriptClean = new File(KitodoConfig.getParameter("script_deleteSymLink"));
        setFileExecutable(scriptClean);
        fileManagement.deleteSymLink(symLinkTarget);
        setFileNotExecutable(scriptClean);
        fileManagement.delete(symLinkSource);
        fileManagement.delete(symLinkTarget);
    }

    @Test
    public void shouldDeleteSymLink() throws IOException {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

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
        assertTrue("Delete symbolic link has failed!", result);

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

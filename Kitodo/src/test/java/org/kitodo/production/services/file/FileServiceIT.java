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

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileServiceIT {

    private static final FileVisitor<Path> DELETE_TREE = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path child, BasicFileAttributes unused) throws IOException {
            Files.delete(child);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path theDirectory, IOException e) throws IOException {
            Files.delete(theDirectory);
            return FileVisitResult.CONTINUE;
        }
    };

    @BeforeClass
    public static void setUp() throws IOException {
        FileService fileService = new FileService();
        fileService.createDirectory(URI.create(""), "fileServiceTest");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileService fileService = new FileService();
        fileService.delete(URI.create("fileServiceTest"));
    }

    @Test
    public void testRenameFileWithLockedFile() throws IOException {
        FileService fileService = new FileService();

        URI oldUri = fileService.createResource(URI.create("fileServiceTest"), "oldName.xml");
        Assert.assertTrue(fileService.fileExist(oldUri));
        // Open stream to file and lock it, so it cannot be renamed
        FileOutputStream outputStream = new FileOutputStream(fileService.getFile(oldUri));
        outputStream.getChannel().lock();

        try {
            fileService.renameFile(oldUri, "newName.xml");
        } catch (IOException e) {
            URI newUri = URI.create("fileServiceTest/newName.xml");
            Assert.assertFalse(fileService.fileExist(newUri));
            Assert.assertTrue(fileService.fileExist(oldUri));

        } finally {
            outputStream.close();
        }

    }

    @Test
    public void testCreateDirectories() throws IOException {
        Path testBaseDirectoryPath = Paths.get("src/test/resources");

        // delete existing directories so that directories are actually created
        Path firstDirectoryOfTestPath = testBaseDirectoryPath.resolve("several");
        if (Files.isDirectory(firstDirectoryOfTestPath)) {
            Files.walkFileTree(firstDirectoryOfTestPath, DELETE_TREE);
        }

        String testBaseDirectoryURI = testBaseDirectoryPath.toUri().toString();
        String createDirectories = "several/directories/can/be/created/at/once";
        new FileService().createDirectories(URI.create(testBaseDirectoryURI + createDirectories));
        assertTrue(Files.isDirectory(firstDirectoryOfTestPath.resolve("directories/can/be/created/at/once")));

        // clean up
        Files.walkFileTree(firstDirectoryOfTestPath, DELETE_TREE);
    }

    @Test
    public void testCreateDirectoriesWithTrailingSlash() throws IOException {
        Path testBaseDirectoryPath = Paths.get("src/test/resources");

        // delete existing directories so that directories are actually created
        Path firstDirectoryOfTestPath = testBaseDirectoryPath.resolve("several");
        if (Files.isDirectory(firstDirectoryOfTestPath)) {
            Files.walkFileTree(firstDirectoryOfTestPath, DELETE_TREE);
        }

        String testBaseDirectoryURI = testBaseDirectoryPath.toUri().toString();
        String createDirectories = "several/directories/can/be/created/with/trailing/";
        new FileService().createDirectories(URI.create(testBaseDirectoryURI + createDirectories));
        assertTrue(Files.isDirectory(firstDirectoryOfTestPath.resolve("directories/can/be/created/with/trailing")));

        // clean up
        Files.walkFileTree(firstDirectoryOfTestPath, DELETE_TREE);
    }
}

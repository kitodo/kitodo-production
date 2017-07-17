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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileManagementTest {

    private static FileManagement fileManagement = new FileManagement();

    @BeforeClass
    public static void setUp() throws IOException {
        fileManagement.create(URI.create(""), "fileTest", false);
        URI directory = fileManagement.create(URI.create(""), "2", false);
        fileManagement.create(directory, "meta.xml", true);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        fileManagement.delete(URI.create("fileTest"));
        fileManagement.delete(URI.create("2"));
    }

    @Test
    public void shouldRead() throws IOException {
        int testContent = 8;

        URI testRead = fileManagement.create(URI.create("fileTest"), "testRead.txt", true);
        OutputStream outputStream = fileManagement.write(testRead);
        try {
            outputStream.write(testContent);
        } finally {
            outputStream.close();
        }

        InputStream inputStream = fileManagement.read(testRead);
        Assert.assertEquals("Did not read right content", testContent, inputStream.read());

        inputStream.close();
    }

    @Test
    public void shouldWrite() throws IOException {
        int testContent = 7;

        URI testWrite = fileManagement.create(URI.create("fileTest"), "testWrite.txt", true);

        OutputStream outputStream = fileManagement.write(testWrite);
        try {
            outputStream.write(testContent);
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        InputStream inputStream = fileManagement.read(testWrite);
        Assert.assertEquals("Did not write right content", testContent, inputStream.read());

        inputStream.close();
    }

    @Test
    public void shouldRenameFile() throws Exception {
        URI resource = fileManagement.create(URI.create("fileTest"), "oldName.xml", true);
        URI oldUri = URI.create("fileTest/oldName.xml");
        Assert.assertTrue(fileManagement.fileExist(oldUri));
        Assert.assertEquals(resource, oldUri);

        fileManagement.rename(resource, "newName.xml");
        URI newUri = URI.create("fileTest/newName.xml");
        Assert.assertFalse(fileManagement.fileExist(oldUri));
        Assert.assertTrue(fileManagement.fileExist(newUri));
    }

    @Test
    public void shouldDeleteFile() throws IOException {
        URI resource = fileManagement.create(URI.create(""), "testDelete.txt", true);
        OutputStream outputStream = fileManagement.write(resource);
        try {
            outputStream.write(5);
        } finally {
            outputStream.close();
        }
        Assert.assertTrue("File not created", fileManagement.fileExist(resource));

        fileManagement.delete(resource);
        Assert.assertFalse("File not deleted", fileManagement.fileExist(resource));
    }

    @Test
    public void shouldCreateDirectory() throws IOException {
        URI testDirectory = fileManagement.create(URI.create("fileTest"), "testDirectory", false);
        Assert.assertTrue("Directory not created", fileManagement.isDirectory(testDirectory));
        Assert.assertTrue("Directory not created", fileManagement.fileExist(testDirectory));
    }

    @Test
    public void shouldDeleteDirectory() throws IOException {
        URI directory = fileManagement.create(URI.create(""), "testDelete", false);

        Assert.assertTrue("Directory not created", fileManagement.fileExist(directory));
        Assert.assertTrue("Directory is not a directory", fileManagement.isDirectory(directory));

        fileManagement.delete(directory);
        Assert.assertFalse("Directory not deleted", fileManagement.fileExist(directory));
    }

    @Test
    public void testCreateProcessLocation() throws IOException {
        String processFolder = "testProcess";

        URI processLocation = fileManagement.createProcessLocation("testProcess");
        Assert.assertTrue("wrong processLocation", processLocation.toString().contains(processFolder));

        fileManagement.delete(processLocation);
    }

}

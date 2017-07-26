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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class FileManagementTest {

    private static final String testFolder = "src" + File.separator + "test" + File.separator;

    @Test
    public void testRead() throws IOException {

        int testContent = 8;

        File file = new File(testFolder + "test.txt");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(testContent);
        } finally {
            close(os);
        }

        URI uri = file.toURI();

        FileManagement fileManagement = new FileManagement();

        InputStream is = fileManagement.read(uri);

        Assert.assertEquals("Did not read right content", testContent, is.read());

        is.close();
        file.delete();

    }

    @Test
    public void testWrite() throws IOException {
        int testContent = 7;
        File file = new File(testFolder + "test.txt");

        FileManagement fileManagement = new FileManagement();
        OutputStream outputStream = fileManagement.write(file.toURI());
        try {
            outputStream.write(testContent);
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        InputStream is = file.toURI().toURL().openStream();
        Assert.assertEquals("Did not write right content", testContent, is.read());

        is.close();
        file.delete();

    }

    @Test
    public void testDeleteFile() throws IOException {
        File file = new File(testFolder + "testDelete.txt");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(5);
        } finally {
            close(os);
        }
        Assert.assertTrue("File not created", file.exists());

        FileManagement fileManagement = new FileManagement();
        fileManagement.delete(file.toURI());

        Assert.assertFalse("File not deleted", file.exists());

    }

    @Test
    public void testDeleteDirectory() throws IOException {
        File file = new File("src" + File.separator + "test" + File.separator + "testDelete");
        if (!file.mkdir()) {
            throw new IOException();
        }
        Assert.assertTrue("Directory not created", file.exists());
        Assert.assertTrue("Directory is not a directory", file.isDirectory());

        FileManagement fileManagement = new FileManagement();
        fileManagement.delete(file.toURI());

        Assert.assertFalse("Directory not deleted", file.exists());

    }

    @Test
    public void testCreateDirectory() throws IOException {

        String directoryName = "testDir";
        File file = new File(testFolder + directoryName);
        Assert.assertFalse("Directory already exists", file.exists());

        File currentDirFile = new File(testFolder);
        FileManagement fileManagement = new FileManagement();
        URI testDir = fileManagement.create(currentDirFile.toURI(), directoryName, false);
        File testDirFile = new File(testDir);
        Assert.assertTrue("Directory not created", testDirFile.isDirectory());
        Assert.assertTrue("Directory not created", testDirFile.exists());

        FileUtils.deleteDirectory(testDirFile);

    }

    @Test
    public void testCreateProcessLocation() throws IOException {
        FileManagement fileManagement = new FileManagement();
        String processFolder = "src/test/testProcess";

        URI processLocation = fileManagement.createProcessLocation("testProcess");
        Assert.assertTrue("wrong processLocation", processLocation.toString().contains(processFolder));

        File testFolder = new File(processFolder);
        FileUtils.deleteDirectory(testFolder);

    }

    private void close(FileOutputStream os) throws IOException {
        if (os == null) {
            return;
        }
        os.close();
    }

}

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileServiceIT {

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
}

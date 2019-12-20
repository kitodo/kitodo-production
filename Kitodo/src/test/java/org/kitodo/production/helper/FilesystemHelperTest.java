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

package org.kitodo.production.helper;

import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;
import org.kitodo.config.ConfigCore;
import org.kitodo.production.services.file.FileService;

public class FilesystemHelperTest {

    @After
    public void tearDown() throws Exception {
        FileService fileService = new FileService();
        fileService.delete(URI.create("old.xml"));
        fileService.delete(URI.create("new.xml"));
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void renamingOfNonExistingFileShouldThrowFileNotFoundException() throws IOException {
        FileService fileService = new FileService();
        URI oldFileName = Paths.get(ConfigCore.getKitodoDataDirectory() + "none.xml").toUri();
        String newFileName = "new.xml";
        fileService.renameFile(oldFileName, newFileName);
    }

    @Test
    public void shouldRenameAFile() throws IOException {
        FileService fileService = new FileService();
        URI file = createFile("old.xml");
        fileService.renameFile(file, "new.xml");
        assertFileExists("new.xml");
        assertFileNotExists("old.xml");
    }

    @Test
    public void nothingHappensIfSourceFilenameIsNotSet() throws IOException {
        FileService fileService = new FileService();
        fileService.renameFile(null, "new.xml");
        assertFileNotExists("new.xml");
    }

    @Test
    public void nothingHappensIfTargetFilenameIsNotSet() throws IOException {
        URI file = createFile("old.xml");
        FileService fileService = new FileService();
        fileService.renameFile(file, null);
        assertFileNotExists("new.xml");
    }

    private void assertFileExists(String fileName) {
        FileService fileService = new FileService();
        if (!fileService.fileExist(URI.create(fileName))) {
            fail("File " + fileName + " does not exist.");
        }
    }

    private void assertFileNotExists(String fileName) {
        File newFile = new File(fileName);
        if (newFile.exists()) {
            fail("File " + fileName + " should not exist.");
        }
    }

    private URI createFile(String fileName) throws IOException {
        FileService fileService = new FileService();
        URI resource = fileService.createResource(fileName);
        OutputStream outputStream = fileService.write(resource);
        outputStream.write(4);
        outputStream.close();
        return resource;
    }
}

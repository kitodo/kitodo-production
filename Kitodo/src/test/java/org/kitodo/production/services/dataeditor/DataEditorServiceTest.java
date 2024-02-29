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

package org.kitodo.production.services.dataeditor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.production.services.ServiceManager;

public class DataEditorServiceTest {

    private final DataEditorService dataEditorService = ServiceManager.getDataEditorService();
    private static byte[] testMetaOldFormat;
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";
    private static final String metadataFilesDir = "./src/test/resources/metadata/metadataFiles/";

    @Before
    public void saveFile() throws IOException {
        File file = new File(metadataFilesDir + "testmetaOldFormat.xml");
        testMetaOldFormat = IOUtils.toByteArray(file.toURI());
    }

    @After
    public void revertFile() throws IOException {
        IOUtils.write( testMetaOldFormat, Files.newOutputStream(Paths.get(pathOfOldMetaFormat)));
    }

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditorService.readData(Paths.get(metadataFilesDir + "testmeta.xml").toUri());
    }

    @Test
    public void shouldReadOldMetadata() throws IOException {
        dataEditorService.readData(Paths.get(metadataFilesDir + "testmetaOldFormat.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void shouldNotReadMetadataOfNotExistingFile() throws IOException {
        dataEditorService.readData(Paths.get("notExisting.xml").toUri());
    }
}

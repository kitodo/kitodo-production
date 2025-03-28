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

package org.kitodo.dataeditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataEditorTest {

    private final DataEditor dataEditor = new DataEditor();
    private final URI xsltFile = Paths.get("src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static byte[] testMetaOldFormat;
    private static byte[] testmetaUnsupportedFormat;
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";

    @BeforeEach
    public void saveFile() throws IOException {
        File file = new File(pathOfOldMetaFormat);
        testMetaOldFormat = IOUtils.toByteArray(file.toURI());
        file = new File("src/test/resources/testmetaUnsupportedFormat.xml");
        testmetaUnsupportedFormat = IOUtils.toByteArray(file.toURI());
    }

    @AfterEach
    public void revertFile() throws IOException {
        IOUtils.write( testMetaOldFormat, Files.newOutputStream(Paths.get(pathOfOldMetaFormat)));
        IOUtils.write( testmetaUnsupportedFormat, Files.newOutputStream(Paths.get("src/test/resources/testmetaUnsupportedFormat.xml")));
    }

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditor.readData(Paths.get("src/test/resources/testmeta.xml").toUri(), xsltFile);
    }

    @Test
    public void shouldReadEmptyMetadata() throws IOException {
        dataEditor.readData(Paths.get("src/test/resources/testmetaEmpty.xml").toUri(), xsltFile);
    }

    @Test
    public void shouldReadOldMetadata() throws IOException {
        dataEditor.readData(Paths.get(pathOfOldMetaFormat).toUri(), xsltFile);
    }

    @Test
    public void shouldNotReadOldMetadataWithNotExistingXslt() {
        URI xsltFile = Paths.get("src/test/resources/xslt/not_existing.xsl").toUri();
        Exception exception = assertThrows(IOException.class,
            () -> dataEditor.readData(Paths.get(pathOfOldMetaFormat).toUri(), xsltFile)
        );

        assertEquals("Xslt file [" + xsltFile.getPath() + "] for transformation of goobi format metadata files was not found. Please check your local config!",
            exception.getMessage());
    }

    @Test
    public void shouldNotReadInvalidMetadata() {
        Exception exception;
        exception = assertThrows(IOException.class,
            () -> dataEditor.readData(Paths.get("src/test/resources/testmetaInvalid.xml").toUri(), xsltFile)
        );

        assertEquals("Unable to read file", exception.getMessage());
    }

    @Test
    public void shouldNotReadUnsupportedMetadata() {
        Exception exception = assertThrows(IOException.class,
            () -> dataEditor.readData(Paths.get("src/test/resources/testmetaUnsupportedFormat.xml").toUri(), xsltFile)
        );

        assertEquals("Can not read data because of not supported format!", exception.getMessage());
    }

    @Test
    public void shouldNotReadMetadataOfNotExistingFile() {
        URI notExistingUri = Paths.get("notExisting.xml").toUri();
        Exception exception = assertThrows(IOException.class,
            () -> dataEditor.readData(notExistingUri, xsltFile)
        );

        assertEquals("File was not found: " + notExistingUri.getPath(), exception.getMessage());
    }
}

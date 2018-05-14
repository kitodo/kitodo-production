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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataEditorTest {

    private URI xsltFile = Paths.get("src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DataEditor dataEditor = new DataEditor();

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditor.readData(URI.create("src/test/resources/testmeta.xml"), xsltFile);
    }

    @Test
    public void shouldReadEmptyMetadata() throws IOException {
        dataEditor.readData(URI.create("src/test/resources/testmetaEmpty.xml"), xsltFile);
    }

    @Test
    public void shouldReadOldMetadata() throws IOException {
        dataEditor.readData(URI.create("src/test/resources/testmetaOldFormat.xml"), xsltFile);
    }

    @Test
    public void shouldNotReadOldMetadataWithNotExistingXslt() throws IOException {
        URI xsltFile = Paths.get("src/test/resources/xslt/not_existing.xsl").toUri();
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Xslt file [" + xsltFile.getPath()
                + "] for transformation of goobi format metadata files was not found. Please check your local config!");
        dataEditor.readData(URI.create("src/test/resources/testmetaOldFormat.xml"), xsltFile);
    }

    @Test
    public void shouldNotReadInvalidMetadata() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Unable to read file");
        dataEditor.readData(URI.create("src/test/resources/testmetaInvalid.xml"), xsltFile);
    }

    @Test
    public void shouldNotReadUnsupportedMetadata() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Can not read data because of not supported format!");
        dataEditor.readData(URI.create("src/test/resources/testmetaUnsupportedFormat.xml"), xsltFile);
    }

    @Test
    public void shouldNotReadMetadataOfNotExistingFile() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Unable to read file");
        dataEditor.readData(URI.create("notExisting.xml"), xsltFile);
    }
}

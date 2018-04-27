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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataEditorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DataEditor dataEditor = new DataEditor();

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditor.readData(URI.create("./src/test/resources/testmeta.xml"));
    }

    @Test
    public void shouldReadEmptyMetadata() throws IOException {
        dataEditor.readData(URI.create("./src/test/resources/testmetaEmpty.xml"));
    }

    @Test
    public void shouldReadOldMetadata() throws IOException {
        dataEditor.readData(URI.create("./src/test/resources/testmetaOldFormat.xml"));
    }

    @Test
    public void shouldNotReadInvalidMetadata() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Unable to read file");
        dataEditor.readData(URI.create("./src/test/resources/testmetaInvalid.xml"));
    }

    @Test
    public void shouldNotReadUnsupportedMetadata() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Can not read data because of not supported format!");
        dataEditor.readData(URI.create("./src/test/resources/testmetaUnsupportedFormat.xml"));
    }

    @Test
    public void shouldNotReadMetadataOfNotExistingFile() throws IOException {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Unable to read file");
        dataEditor.readData(URI.create("notExisting.xml"));
    }
}

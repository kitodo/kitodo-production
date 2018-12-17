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

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;
import org.kitodo.production.services.ServiceManager;

public class DataEditorServiceTest {

    private DataEditorService dataEditorService = ServiceManager.getDataEditorService();

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditorService.readData(Paths.get("./src/test/resources/metadata/testmeta.xml").toUri());
    }

    @Test
    public void shouldReadOldMetadata() throws IOException {
        dataEditorService.readData(Paths.get("./src/test/resources/metadata/testmetaOldFormat.xml").toUri());
    }

    @Test(expected = IOException.class)
    public void shouldNotReadMetadataOfNotExistingFile() throws IOException {
        dataEditorService.readData(Paths.get("notExisting.xml").toUri());
    }
}

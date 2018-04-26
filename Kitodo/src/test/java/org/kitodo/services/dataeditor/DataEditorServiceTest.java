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

package org.kitodo.services.dataeditor;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.kitodo.services.ServiceManager;

public class DataEditorServiceTest {

    private DataEditorService dataEditorService = new ServiceManager().getDataEditorService();

    @Test
    public void shouldReadMetadata() throws IOException {
        dataEditorService.readData(URI.create("../Kitodo-DataEditor/src/test/resources/testmeta.xml"));
    }

    @Test(expected = IOException.class)
    public void shouldNotReadMetadataOfNotExistingFile() throws IOException {
        dataEditorService.readData(URI.create("notExisting.xml"));
    }
}

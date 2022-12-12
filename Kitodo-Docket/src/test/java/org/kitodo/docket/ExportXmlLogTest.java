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

package org.kitodo.docket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.docket.DocketData;

public class ExportXmlLogTest extends ExportXmlLog {

    public ExportXmlLogTest() throws URISyntaxException {
        super(getDocketData());
    }

    static final DocketData getDocketData() throws URISyntaxException {
        DocketData data = new DocketData();
        data.setMetadataFile(Paths.get("src/test/resources/meta.xml").toAbsolutePath().toUri());
        return data;
    }

    /**
     * Tests if ExportXmlLog works.
     */
    @Test
    public void shouldExportXmlLogWithMetadata() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        super.startExport(((OutputStream) buffer));
        Assert.assertTrue("Output should contain test string",
            new String(buffer.toByteArray()).contains("findMeInOutput"));
    }
}

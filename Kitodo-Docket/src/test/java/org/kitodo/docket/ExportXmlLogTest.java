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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.kitodo.api.docket.DocketData;

public class ExportXmlLogTest extends ExportXmlLog {

    public ExportXmlLogTest() {
        super(getDocketData());
    }

    static DocketData getDocketData() {
        DocketData data = new DocketData();
        data.setMetadataFile(Paths.get("src/test/resources/meta.xml").toAbsolutePath().toUri());
        return data;
    }

    /**
     * Tests if ExportXmlLog works.
     */
    @Test
    public void shouldExportXmlLogWithMetadata() throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            super.startExport(buffer);
            assertTrue(buffer.toString().contains("findMeInOutput"), "Output should contain test string");
        }
    }
}

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.kitodo.api.docket.DocketData;

public class ExportXmlLogTest extends ExportXmlLog {

    private static final String CANARY = "XXE-CANARY-SECRET";

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
            assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("findMeInOutput"), "Output should contain test string");
        }
    }

    @Test
    public void shouldNotResolveExternalEntitiesInMetadataFile() throws IOException {
        Path secret = Files.createTempFile("xxe-canary", ".txt");
        secret.toFile().deleteOnExit();
        Files.writeString(secret, CANARY);

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<!DOCTYPE kitodo [ <!ENTITY xxe SYSTEM \"file://" + secret.toFile().getAbsolutePath() + "\"> ]>\n"
                + "<kitodo:kitodo xmlns:kitodo=\"http://meta.kitodo.org/v1/\">"
                + "<kitodo:metadata name=\"ValueMetadata\">&xxe;</kitodo:metadata>"
                + "</kitodo:kitodo>";
        Path metsFile = Files.createTempFile("xxe-mets", ".xml");
        Files.writeString(metsFile, payload);

        DocketData data = new DocketData();
        data.setMetadataFile(metsFile.toUri());
        ExportXmlLog exportXmlLog = new ExportXmlLog(data);
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            exportXmlLog.startExport(buffer);
            assertFalse(buffer.toString(StandardCharsets.UTF_8).contains(CANARY), "secret must not leak into the output");
        }
    }
}

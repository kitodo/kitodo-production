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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExportDocketTest {
    private DocketDataGenerator docketDataGenerator;

    @Before
    public void initialize() {
        this.docketDataGenerator = new DocketDataGenerator();
    }

    @After
    public void tearDown() {
        new File("docket.pdf").delete();
    }

    @Test
    public void testStartExport() throws IOException, URISyntaxException {
        File xslFile = new File("src/test/resources/docket.xsl");
        ExportDocket exportDocket = new ExportDocket(xslFile);
        File file = new File("docket.pdf");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            exportDocket.startExport(docketDataGenerator.createDocketData("processId", "signature", "doctype"),
                fileOutputStream);
        }

        assertTrue(file.exists());
    }

}

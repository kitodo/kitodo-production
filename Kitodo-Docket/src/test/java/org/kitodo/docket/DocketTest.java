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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadMemoryMappedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DocketTest {

    private DocketDataGenerator docketDataGenerator;

    @Before
    public void initialize() {
        this.docketDataGenerator = new DocketDataGenerator();
    }

    @After
    public void tearDown() {
        new File("docket.pdf").delete();
        new File("docket_multipage.pdf").delete();
    }

    @Test
    public void testCorrectExportDocket() throws IOException, URISyntaxException {
        String processId = "processID";
        String signature = "AZ-234";
        String docType = "manuscript";
        File expectedFile = new File("src/test/resources/docket.pdf");
        File generatedFile = generateDocket(processId, signature, docType);

        String expectedPdfText = getPDFText(expectedFile);
        String generatedPdfText = getPDFText(generatedFile);

        assertTrue(generatedPdfText.contains(processId));
        assertTrue(generatedPdfText.contains(signature));
        assertFalse(generatedPdfText.contains(docType));
        assertEquals(expectedPdfText, generatedPdfText);
    }

    @Test
    public void testIncorrectExportDocket() throws IOException, URISyntaxException {
        File expectedFile = new File("src/test/resources/docket.pdf");
        File generatedFile = generateDocket("falscheId", "AZ-234", "manuscript");

        String expectedPdfText = getPDFText(expectedFile);
        String generatedPdfText = getPDFText(generatedFile);

        assertNotEquals("Compared results are different!", expectedPdfText, generatedPdfText);
    }

    @Test
    public void testExportMultipleDockets() throws IOException, URISyntaxException {
        String expectedFileStrings = getPDFText(new File("src/test/resources/docket_multipage.pdf"));

        ArrayList<String> processIds = new ArrayList<>();
        processIds.add("processId1");
        processIds.add("processId2");
        processIds.add("processId3");

        File generatedDocket = generateMultipleDockets(processIds);

        String generatedFileStrings = getPDFText(generatedDocket);

        assertEquals("Compared results are different!", expectedFileStrings, generatedFileStrings);
    }

    private String getPDFText(File pdfFile) throws IOException {
        try (RandomAccessRead memoryMappedFile = new RandomAccessReadMemoryMappedFile(pdfFile)) {
            return new PDFTextStripper().getText(new PDFParser(memoryMappedFile).parse());
        }
    }

    private File generateDocket(String processId, String signatur, String docType)
            throws IOException, URISyntaxException {
        URI pathToXslFile = new File("src/test/resources/docket.xsl").toURI();

        Docket docket = new Docket();
        return docket.generateDocket(docketDataGenerator.createDocketData(processId, signatur, docType),
                pathToXslFile);
    }

    private File generateMultipleDockets(ArrayList<String> processIds) throws IOException, URISyntaxException {
        URI pathToXslFile = new File("src/test/resources/docket_multipage.xsl").toURI();

        Docket docket = new Docket();
        return docket.generateMultipleDockets(docketDataGenerator.createDocketData(processIds),
                pathToXslFile);
    }

}

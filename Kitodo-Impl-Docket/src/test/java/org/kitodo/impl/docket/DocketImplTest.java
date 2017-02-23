package org.kitodo.impl.docket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class DocketImplTest {

    DocketDataGenerator docketDataGenerator;

    @Before
    public void initialize() {
        this.docketDataGenerator = new DocketDataGenerator();
    }

    @Test
    public void testCorrectExportDocket() throws IOException {
        String processId = "processID";
        String signatur = "AZ-234";
        String docType = "manuscript";
        File expectedFile = new File("src/test/resources/docket.pdf");
        File generatedFile = generateDocket(processId, signatur, docType);

        String expectedPdfText = getPDFText(expectedFile);
        String generatedPdfText = getPDFText(generatedFile);

        assertTrue(generatedPdfText.contains(processId));
        assertTrue(generatedPdfText.contains(signatur));
        assertFalse(generatedPdfText.contains(docType));
        assertEquals(expectedPdfText, generatedPdfText);
    }

    @Test
    public void testIncorrectExportDocket() throws IOException {
        File expectedFile = new File("src/test/resources/docket.pdf");
        File generatedFile = generateDocket("falscheId", "AZ-234", "manuscript");

        String expectedPdfText = getPDFText(expectedFile);
        String generatedPdfText = getPDFText(generatedFile);

        assertNotEquals(expectedPdfText, generatedPdfText);
    }

    @Test
    public void testExportMultipleDockets() throws IOException {
        String expectedFileStrings = getPDFText(new File("src/test/resources/docket_multipage.pdf"));

        ArrayList<String> processIds = new ArrayList<>();
        processIds.add("processId1");
        processIds.add("processId2");
        processIds.add("processId3");

        File generatedDocket = generateMultipleDockets(processIds);

        String generatedFileStrings = getPDFText(generatedDocket);

        assertEquals(expectedFileStrings, generatedFileStrings);
    }

    public String getPDFText(File pdfFile) throws IOException {
        Document pdf = PDF.open(pdfFile);
        StringWriter buffer = new StringWriter();
        pdf.pipe(new OutputTarget(buffer));
        pdf.close();
        return buffer.toString();
    }

    public File generateDocket(String processId, String signatur, String docType) throws IOException {
        URI pathToXslFile = new File("src/test/resources/docket.xsl").toURI();

        DocketImpl docketImpl = new DocketImpl();
        File generatedFile = docketImpl
                .generateDocket(docketDataGenerator.createDocketData(processId, signatur, docType), pathToXslFile);

        return generatedFile;
    }

    public File generateMultipleDockets(ArrayList<String> processIds) throws IOException {
        URI pathToXslFile = new File("src/test/resources/docket_multipage.xsl").toURI();

        DocketImpl docketImpl = new DocketImpl();
        File generatedFile = docketImpl.generateMultipleDockets(docketDataGenerator.createDocketData(processIds),
                pathToXslFile);

        return generatedFile;
    }

}

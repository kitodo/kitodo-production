package org.kitodo.docket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ExportDocketTest {
    DocketDataGenerator docketDataGenerator;

    @Before
    public void initialize() {
        this.docketDataGenerator = new DocketDataGenerator();
    }

    @Test
    public void testStartExport() throws IOException {
        ExportDocket exportDocket = new ExportDocket();
        File file = new File("docket.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        File xslFile = new File("src/test/resources/docket.xsl");

        exportDocket.startExport(docketDataGenerator.createDocketData("processId", "signature", "doctype"),
                fileOutputStream, xslFile);
    }

}

package org.kitodo.impl.docket;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportDocketTest {
    DocketDataGenerator docketDataGenerator;

    @Before
    public void initialize(){
        this.docketDataGenerator = new DocketDataGenerator();
    }

    @Test
    public void testStartExport() throws IOException {
        ExportDocket exportDocket = new ExportDocket();
        File file = new File("docket.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        Path pathToXslFile = Paths.get("src/test/resources/docket.xsl");

        exportDocket.startExport(docketDataGenerator.createDocketData("processId","signature","doctype"), fileOutputStream, pathToXslFile.toString());
    }


}

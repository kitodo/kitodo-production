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

package org.kitodo.dataeditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import jakarta.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetsKitodoWriterTest {

    private static MetsKitodoWriter metsKitodoWriter;
    private final URI xsltFile = Paths.get("./src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static final File manifestFile = new File("./target/classes/META-INF/MANIFEST.MF");
    private static byte[] testMetaOldFormat;
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";

    @BeforeEach
    public void saveFile() throws IOException {
        File file = new File("src/test/resources/testmetaOldFormat.xml");
        testMetaOldFormat = IOUtils.toByteArray(file.toURI());
    }

    @AfterEach
    public void revertFile() throws IOException {
        IOUtils.write( testMetaOldFormat, Files.newOutputStream(Paths.get(pathOfOldMetaFormat)));
    }

    @BeforeAll
    public static void setUp() throws IOException, JAXBException {

        metsKitodoWriter = new MetsKitodoWriter();

        String manifest =
            "Manifest-Version: 1.0\n" +
            "Archiver-Version: Plexus Archiver\n" +
            "Created-By: Apache Maven\n" +
            "Built-By: tester\n" +
            "Build-Jdk: 1.8.0_144\n" +
            "Specification-Title: Kitodo - Data Editor\n" +
            "Specification-Version: 3.0-SNAPSHOT\n" +
            "Specification-Vendor: kitodo.org\n" +
            "Implementation-Title: Kitodo - Data Editor\n" +
            "Implementation-Version: 3.0-SNAPSHOT\n" +
            "Implementation-Vendor-Id: org.kitodo\n" +
            "Implementation-Vendor: kitodo.org\n" +
            "Implementation-Build-Date: 2018-05-03T08:41:49Z\n";

        FileUtils.write(manifestFile, manifest, "UTF-8");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Files.deleteIfExists(manifestFile.toPath());
    }

    @Test
    public void shouldWriteMetsFile()
            throws TransformerException, JAXBException, IOException, DatatypeConfigurationException {
        URI xmlFile = Paths.get("./src/test/resources/testmeta.xml").toUri();

        URI xmlTestFile = Paths.get(System.getProperty("user.dir") + "/target/test-classes/newtestmeta.xml").toUri();

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlFile, xsltFile);
        metsKitodoWriter.writeSerializedToFile(metsKitodoWrapper.getMets(), xmlTestFile);
        MetsKitodoWrapper savedMetsKitodoWrapper = new MetsKitodoWrapper(xmlTestFile, xsltFile);
        Files.deleteIfExists(Paths.get(xmlTestFile));

        String loadedMetadata = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType().getMetadata().getFirst().getValue();
        String savedMetadata = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType().getMetadata().getFirst()
                .getValue();

        assertEquals(loadedMetadata,
            savedMetadata,
            "The metadata of the loaded and the saved mets file are not equal");
        assertEquals(metsKitodoWrapper.getDmdSecs().size(),
            savedMetsKitodoWrapper.getDmdSecs().size(),
            "The number of dmdSec elements of the loaded and the saved mets file are not equal");

        assertEquals(savedMetsKitodoWrapper.getMets().getMetsHdr().getLASTMODDATE().getHour(),
            LocalDateTime.now().getHour(),
            "Lastmoddate of Mets header was wrong");

        String result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());

        assertTrue(result.contains("kitodo:metadata"), "Prefix mapping for kitodo namespace is wrong");
        assertTrue(result.contains("mets:dmdSec"), "Prefix mapping for mets namespace is wrong");
    }

    @Test
    public void shouldWriteMetsFileFromOldFormat()
            throws TransformerException, JAXBException, IOException, DatatypeConfigurationException {
        URI xmlFile = Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri();

        URI xmlTestFile = Paths.get(System.getProperty("user.dir") + "/target/test-classes/newtestmetaold.xml").toUri();

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlFile, xsltFile);
        metsKitodoWriter.writeSerializedToFile(metsKitodoWrapper.getMets(), xmlTestFile);
        MetsKitodoWrapper savedMetsKitodoWrapper = new MetsKitodoWrapper(xmlTestFile, xsltFile);
        Files.deleteIfExists(Paths.get(xmlTestFile));

        String loadedMetadata = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType().getMetadata().getFirst().getValue();
        String savedMetadata = savedMetsKitodoWrapper.getDmdSecs().getFirst().getKitodoType().getMetadata().getFirst().getValue();

        assertEquals(loadedMetadata,
            savedMetadata,
            "The metadata of the loaded and the saved mets file are not equal");
        assertEquals(metsKitodoWrapper.getDmdSecs().size(),
            savedMetsKitodoWrapper.getDmdSecs().size(),
            "The number of dmdSec elements of the loaded and the saved mets file are not equal");

        assertEquals(2,
            metsKitodoWrapper.getMets().getMetsHdr().getAgent().getFirst().getNote().size(),
            "Conversion note was not inserted to mets header");
    }

    @Test
    public void shouldWriteMetsString()
        throws TransformerException, JAXBException, IOException, DatatypeConfigurationException {
        URI xmlFile = Paths.get("./src/test/resources/testmeta.xml").toUri();

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlFile, xsltFile);
        String result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());

        String expectedResult =
            "   <mets:dmdSec ID=\"DMDLOG_0001\">\n" +
            "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
            "            <mets:xmlData>\n" +
            "                <kitodo:kitodo version=\"1.0\">\n" +
            "                    <kitodo:metadata name=\"TitleDocMain\">Chapter 1</kitodo:metadata>\n" +
            "                </kitodo:kitodo>\n" +
            "            </mets:xmlData>\n" +
            "        </mets:mdWrap>\n" +
            "    </mets:dmdSec>";

        assertTrue(result.contains(expectedResult), "The written String of the loaded mets was wrong");
    }

    @Test
    public void shouldWriteMetsStringWithPrefixes()
        throws JAXBException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
        metsKitodoWrapper.getDmdSecs().add(objectFactory.createDmdSecByKitodoMetadata(objectFactory.createKitodoType(),"testId"));
        String result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());
        assertTrue(result.contains("kitodo:kitodo"), "Prefix mapping for kitodo namespace is wrong");
        assertTrue(result.contains("mets:dmdSec"), "Prefix mapping for mets namespace is wrong");
    }
}

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.dataeditor.handlers.MetsKitodoMdSecHandler;

public class MetsKitodoWriterTest {

    private static MetsKitodoWriter metsKitodoWriter;
    private URI xsltFile = Paths.get("./src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static File manifestFile = new File("./target/classes/META-INF/MANIFEST.MF");

    @BeforeClass
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

    @AfterClass
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

        String loadedMetadata = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0)).getMetadata().get(0).getValue();
        String savedMetadata = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(savedMetsKitodoWrapper.getDmdSecs().get(0)).getMetadata().get(0)
                .getValue();

        Assert.assertEquals("The metadata of the loaded and the saved mets file are not equal", loadedMetadata,
            savedMetadata);
        Assert.assertEquals("The number of dmdSec elements of the loaded and the saved mets file are not equal",
            metsKitodoWrapper.getDmdSecs().size(), savedMetsKitodoWrapper.getDmdSecs().size());

        Assert.assertEquals("Lastmoddate of Mets header was wrong", savedMetsKitodoWrapper.getMets().getMetsHdr().getLASTMODDATE().getHour(),
            new DateTime().getHourOfDay());

        String result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());

        Assert.assertTrue("Prefix mapping for kitodo namespace is wrong", result.contains("kitodo:metadata"));
        Assert.assertTrue("Prefix mapping for mets namespace is wrong", result.contains("mets:dmdSec"));
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

        String loadedMetadata = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0)).getMetadata().get(0).getValue();
        String savedMetadata = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(savedMetsKitodoWrapper.getDmdSecs().get(0)).getMetadata().get(0)
                .getValue();

        Assert.assertEquals("The metadata of the loaded and the saved mets file are not equal", loadedMetadata,
            savedMetadata);
        Assert.assertEquals("The number of dmdSec elements of the loaded and the saved mets file are not equal",
            metsKitodoWrapper.getDmdSecs().size(), savedMetsKitodoWrapper.getDmdSecs().size());

        Assert.assertEquals("Conversation note was not inserted to mets header", 2,
            metsKitodoWrapper.getMets().getMetsHdr().getAgent().get(0).getNote().size());
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

        Assert.assertTrue("The written String of the loaded mets was wrong", result.contains(expectedResult));
    }

    @Test
    public void shouldWriteMetsStringWithPrefixes()
        throws JAXBException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
        metsKitodoWrapper.getDmdSecs().add(objectFactory.createDmdSecByKitodoMetadata(objectFactory.createKitodoType(),"testId"));
        String result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());
        Assert.assertTrue("Prefix mapping for kitodo namespace is wrong", result.contains("kitodo:kitodo"));
        Assert.assertTrue("Prefix mapping for mets namespace is wrong", result.contains("mets:dmdSec"));
    }
}

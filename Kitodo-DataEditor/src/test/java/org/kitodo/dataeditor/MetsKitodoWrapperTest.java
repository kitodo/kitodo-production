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
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.OperationNotSupportedException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.dataeditor.enums.FileLocationType;
import org.kitodo.dataeditor.enums.PositionOfNewDiv;
import org.kitodo.dataeditor.handlers.MetsKitodoMdSecHandler;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.StructLinkType;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class MetsKitodoWrapperTest {

    private URI xmlfile = Paths.get("./src/test/resources/testmeta.xml").toUri();
    private URI xsltFile = Paths.get("./src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static File manifestFile = new File("./target/classes/META-INF/MANIFEST.MF");

    @BeforeClass
    public static void setUp() throws IOException {

        String manifest = "Manifest-Version: 1.0\n" + "Archiver-Version: Plexus Archiver\n"
                + "Created-By: Apache Maven\n" + "Built-By: tester\n" + "Build-Jdk: 1.8.0_144\n"
                + "Specification-Title: Kitodo - Data Editor\n" + "Specification-Version: 3.0-SNAPSHOT\n"
                + "Specification-Vendor: kitodo.org\n" + "Implementation-Title: Kitodo - Data Editor\n"
                + "Implementation-Version: 3.0-SNAPSHOT\n" + "Implementation-Vendor-Id: org.kitodo\n"
                + "Implementation-Vendor: kitodo.org\n" + "Implementation-Build-Date: 2018-05-03T08:41:49Z\n";

        FileUtils.write(manifestFile, manifest, "UTF-8");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(manifestFile.toPath());
    }

    @Test
    public void shouldAddSmLink() throws DatatypeConfigurationException, IOException {
        String from = "from test";
        String to = "to test";

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        metsKitodoWrapper.addSmLink(from, to);

        StructLinkType.SmLink smLink = (StructLinkType.SmLink) metsKitodoWrapper.getMets().getStructLink()
                .getSmLinkOrSmLinkGrp().get(0);

        Assert.assertEquals("'from' attribute of smLink was wrong", from, smLink.getFrom());
        Assert.assertEquals("'to' attribute of smLink was wrong", to, smLink.getTo());
    }

    @Test
    public void shouldAddMetsHeader() throws DatatypeConfigurationException, IOException {

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        String role = metsKitodoWrapper.getMets().getMetsHdr().getAgent().get(0).getROLE();
        String name = metsKitodoWrapper.getMets().getMetsHdr().getAgent().get(0).getName();
        String type = metsKitodoWrapper.getMets().getMetsHdr().getAgent().get(0).getTYPE();
        String otherType = metsKitodoWrapper.getMets().getMetsHdr().getAgent().get(0).getOTHERTYPE();

        Assert.assertEquals("Role of mets header agent was inserted wrong", "CREATOR", role);
        Assert.assertEquals("Type of mets header agent was inserted wrong", "OTHER", type);
        Assert.assertEquals("OtherType of mets header agent was inserted wrong", "SOFTWARE", otherType);
        Assert.assertTrue("Name of mets header agent was inserted wrong", name.contains("Kitodo"));
    }

    @Test
    public void shouldCreateMetsByFile()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        Assert.assertEquals("Number of dmdSec elements was wrong!", 3, metsKitodoWrapper.getDmdSecs().size());
    }

    @Test
    public void shouldReadValues()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        String id = metsKitodoWrapper.getMets().getDmdSec().get(0).getID();
        Assert.assertEquals("Reading id of dmdSec data out of mets was not correct", "DMDLOG_0000", id);
    }

    @Test
    public void shouldReadKitodoMetadata()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        KitodoType kitodoType = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0));

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading value out of kitodo metadata format was not correct", "Test Publisher",
            metadataType.getValue());
    }

    @Test
    public void shouldReadKitodoMetadataGroup()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        KitodoType kitodoType = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0));

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().get(0).getMetadataGroup().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "subTypIdentifierPPN",
            metadataGroupType.getMetadata().get(1).getName());
        Assert.assertEquals("Reading value out of kitodo metadata was not correct", "sub10457187X",
            metadataGroupType.getMetadata().get(1).getValue());
    }

    @Test
    public void shouldReadGoobiMetadata()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        URI oldXmlfile = Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri();
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(oldXmlfile, xsltFile);
        KitodoType kitodoType = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0));

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading value out of kitodo metadata was not correct", "Test Publisher",
            metadataType.getValue());
    }

    @Test
    public void shouldReadGoobiMetadataGroup()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        URI oldXmlfile = Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri();
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(oldXmlfile, xsltFile);
        KitodoType kitodoType = MetsKitodoMdSecHandler
                .getKitodoTypeOfDmdSecElement(metsKitodoWrapper.getDmdSecs().get(0));

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "typIdentifierPPN",
            metadataGroupType.getMetadata().get(1).getName());
        Assert.assertEquals("Reading value out of kitodo metadata was not correct", "10457187X",
            metadataGroupType.getMetadata().get(1).getValue());
    }

    @Test
    public void shouldInsertFileGroup() throws IOException, DatatypeConfigurationException {
        Path path = Paths.get("images");
        int numberOfFiles = 5;
        List<MediaFile> mediaFiles = new ArrayList<>();
        for (int i = 1; i <= numberOfFiles; i++) {
            mediaFiles.add(
                new MediaFile(Paths.get(path + "/0000" + i + ".tif").toUri(), FileLocationType.URL, "image/tiff"));
        }

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        metsKitodoWrapper.insertMediaFiles(mediaFiles);

        Assert.assertEquals("Wrong number of divs in physical structMap", numberOfFiles,
            metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().size());
        Assert.assertEquals("Wrong number of fils in fileSec", numberOfFiles,
            metsKitodoWrapper.getMets().getFileSec().getFileGrp().get(0).getFile().size());

        DivType divType = metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().get(1);

        Assert.assertEquals("Wrong order label at second div", "uncounted", divType.getORDERLABEL());
        Assert.assertEquals("Wrong order at second div", BigInteger.valueOf(2), divType.getORDER());
        Assert.assertEquals("Wrong type at second div", "page", divType.getTYPE());
        Assert.assertEquals("Wrong id at second div", "PHYS_0002", divType.getID());

        FileType fileType = (FileType) divType.getFptr().get(0).getFILEID();
        Assert.assertEquals("Wrong file id at second div", "FILE_0002", fileType.getID());

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldInsertRootLogicalDivAndMdSecAtCreation() throws DatatypeConfigurationException, IOException {
        String documentType = "Manuscript";
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(documentType);
        StructMapType logicalStructMap = metsKitodoWrapper.getLogicalStructMap();
        MdSecType rootDmdSec = (MdSecType) logicalStructMap.getDiv().getDMDID().get(0);

        Assert.assertEquals("Type of logical root div was wrong", documentType, logicalStructMap.getDiv().getTYPE());
        Assert.assertEquals("Id of root div related DmdSec was wrong", "DMDLOG_ROOT", rootDmdSec.getID());
    }

    @Test
    public void shouldGetKitodoTypeByDiv()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        KitodoType kitodoTypeOfDiv = metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(rootDiv);
        Assert.assertEquals("Reading metadata of dmdSec logical root div was wrong", "Test Publisher",
            kitodoTypeOfDiv.getMetadata().get(1).getValue());

        DivType div = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1);
        kitodoTypeOfDiv = metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(div);
        Assert.assertEquals("Reading metadata of dmdSec logical div was wrong", "[Seite 134r-156v]",
            kitodoTypeOfDiv.getMetadata().get(0).getValue());

        DivType divWithoutMetadata = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0);
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Div element with id: LOG_0001 does not have metadata!");
        metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(divWithoutMetadata);
    }

    @Test
    public void shouldGenerateIdsForDivsOfLogicalStructMap() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);

        Assert.assertEquals("Id of first div at logical struct map was wrong", "LOG_0001",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0).getID());
        Assert.assertEquals("Id of second div at logical struct map was wrong", "LOG_0005",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1).getID());
        Assert.assertEquals("Id of third div at logical struct map was wrong", "LOG_0006",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(2).getID());
        Assert.assertEquals("Id of fourth div at logical struct map was wrong", "LOG_0009",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(3).getID());
        Assert.assertEquals("Id of second sub div of third div at logical struct map was wrong", "LOG_0012",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getID());
        Assert.assertEquals("Id of fifth sub div of third div at logical struct map was wrong", "LOG_0018",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(4).getID());
    }

    @Test
    public void shouldAddDivsAsFirstChild() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "AddedSubChapter", PositionOfNewDiv.FIRST_CHILD_OF_ELEMENT);
        Assert.assertEquals("New div was not added", "AddedSubChapter", fifthDiv.getDiv().get(0).getTYPE());
    }

    @Test
    public void shouldAddDivsBefor() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "AddedSubChapter", PositionOfNewDiv.BEFOR_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        Assert.assertEquals("New div was not added", "AddedSubChapter", addedDiv.getTYPE());
    }

    @Test
    public void shouldAddDivsAfter() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "AddedSubChapter", PositionOfNewDiv.AFTER_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(5);
        Assert.assertEquals("New div was not added", "AddedSubChapter", addedDiv.getTYPE());

    }

    @Test
    public void shouldDeepAddDivsAfter() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getDiv()
                .get(0);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthSubDiv, "AddedSubSubChapter", PositionOfNewDiv.BEFOR_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getDiv()
                .get(0);
        Assert.assertEquals("New div was not added", "AddedSubSubChapter", addedDiv.getTYPE());
    }

    @Test
    public void shouldNotAddDivAfterRoot() throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        expectedException.expect(OperationNotSupportedException.class);
        expectedException.expectMessage("Root element can not have a parent!");
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthSubDiv, "AddedRoot", PositionOfNewDiv.BEFOR_ELEMENT);
    }

    private void fillLogicalStructMap(MetsKitodoWrapper metsKitodoWrapper) throws OperationNotSupportedException {
        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        metsKitodoWrapper.addNewDivToLogicalSructMap(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType firstDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0);
        metsKitodoWrapper.addNewDivToLogicalSructMap(firstDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(firstDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(firstDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType thirdDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(2);
        metsKitodoWrapper.addNewDivToLogicalSructMap(thirdDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(thirdDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthDiv, "SubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthSubDiv, "SubSubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthSubDiv, "SubSubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.addNewDivToLogicalSructMap(fifthSubDiv, "SubSubChapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
    }

    @Test
    public void shouldRemoveNestedLogicalDiv()
            throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        metsKitodoWrapper.removeDivFromLogicalStructMap(fifthSubDiv);
        Assert.assertEquals("Could not remove div al logical structMap", 4,
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().size());
    }

    @Test
    public void shouldMovedLogicalDiv()
            throws IOException, DatatypeConfigurationException, OperationNotSupportedException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        DivType firstDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0);
        metsKitodoWrapper.moveDivToDivAtIndexAtLogicalStructMap(fifthSubDiv, firstDiv, 0);

        List<DivType> movedDivs = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0).getDiv().get(0)
                .getDiv();

        Assert.assertEquals("Could not remove div al logical structMap", 3, movedDivs.size());
        Assert.assertEquals("Could not remove div al logical structMap", "SubSubChapter", movedDivs.get(0).getTYPE());
    }
}

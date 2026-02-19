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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.dataeditor.enums.FileLocationType;
import org.kitodo.dataeditor.enums.PositionOfNewDiv;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.StructLinkType;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class MetsKitodoWrapperTest {

    private final URI xmlfile = Paths.get("./src/test/resources/testmeta.xml").toUri();
    private final URI xsltFile = Paths.get("./src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static final File manifestFile = new File("./target/classes/META-INF/MANIFEST.MF");
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";
    private static byte[] testMetaOldFormat;

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
    public static void setUp() throws IOException {

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
    public void shouldAddSmLink() throws DatatypeConfigurationException, IOException {
        List<DivType> physicalDivTypes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DivType physicalDiv = new DivType();
            physicalDiv.setID("test_ID_PHY" + i);
            physicalDivTypes.add(physicalDiv);
        }

        DivType logicalDiv = new DivType();
        logicalDiv.setID("test_ID_LOG");

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        metsKitodoWrapper.getStructLink().addSmLinks(logicalDiv, physicalDivTypes);

        StructLinkType.SmLink smLink = (StructLinkType.SmLink) metsKitodoWrapper.getMets().getStructLink()
                .getSmLinkOrSmLinkGrp().getFirst();
        int objectCount = metsKitodoWrapper.getMets().getStructLink().getSmLinkOrSmLinkGrp().size();

        assertEquals(logicalDiv.getID(), smLink.getFrom(), "'from' attribute of smLink was wrong");
        assertEquals(physicalDivTypes.getFirst().getID(), smLink.getTo(), "'to' attribute of smLink was wrong");
        assertEquals(physicalDivTypes.size(), objectCount, "Number of inserted smLinks was wrong");
    }

    @Test
    public void shouldAddMetsHeader() throws DatatypeConfigurationException, IOException {

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        String role = metsKitodoWrapper.getMets().getMetsHdr().getAgent().getFirst().getROLE();
        String name = metsKitodoWrapper.getMets().getMetsHdr().getAgent().getFirst().getName();
        String type = metsKitodoWrapper.getMets().getMetsHdr().getAgent().getFirst().getTYPE();
        String otherType = metsKitodoWrapper.getMets().getMetsHdr().getAgent().getFirst().getOTHERTYPE();

        assertEquals("CREATOR", role, "Role of mets header agent was inserted wrong");
        assertEquals("OTHER", type, "Type of mets header agent was inserted wrong");
        assertEquals("SOFTWARE", otherType, "OtherType of mets header agent was inserted wrong");
        assertTrue(name.contains("Kitodo"), "Name of mets header agent was inserted wrong");
    }

    @Test
    public void shouldCreateMetsByFile()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        assertEquals(6, metsKitodoWrapper.getMets().getDmdSec().size(), "Number of dmdSec elements was wrong!");
    }

    @Test
    public void shouldReadValues()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        String id = metsKitodoWrapper.getMets().getDmdSec().getFirst().getID();
        assertEquals("DMDLOG_ROOT", id, "Reading id of dmdSec data out of mets was not correct");
    }

    @Test
    public void shouldReadKitodoMetadata()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        KitodoType kitodoType = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType();

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        assertEquals("PublisherName", metadataType.getName(), "Reading data of type 'name' out of kitodo format was not correct");
        assertEquals("Test Publisher", metadataType.getValue(), "Reading value out of kitodo metadata format was not correct");
    }

    @Test
    public void shouldReadKitodoMetadataGroup()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        KitodoType kitodoType = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType();

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().getFirst().getMetadataGroup().getFirst();
        assertEquals("subTypIdentifierPPN", metadataGroupType.getMetadata().get(1).getName(), "Reading data of type 'name' out of kitodo format was not correct");
        assertEquals("sub10457187X", metadataGroupType.getMetadata().get(1).getValue(), "Reading value out of kitodo metadata was not correct");
    }

    @Test
    public void shouldReadGoobiMetadata()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        URI oldXmlfile = Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri();
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(oldXmlfile, xsltFile);
        KitodoType kitodoType = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType();

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        assertEquals("PublisherName", metadataType.getName(), "Reading data of type 'name' out of kitodo format was not correct");
        assertEquals("Test Publisher", metadataType.getValue(), "Reading value out of kitodo metadata was not correct");
    }

    @Test
    public void shouldReadGoobiMetadataGroup()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        URI oldXmlfile = Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri();
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(oldXmlfile, xsltFile);
        KitodoType kitodoType = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType();

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().getFirst();
        assertEquals("typIdentifierPPN", metadataGroupType.getMetadata().get(1).getName(), "Reading data of type 'name' out of kitodo format was not correct");
        assertEquals("10457187X", metadataGroupType.getMetadata().get(1).getValue(), "Reading value out of kitodo metadata was not correct");
    }

    @Test
    public void shouldInsertFileGroup() throws DatatypeConfigurationException, IOException {
        Path path = Paths.get("images");
        int numberOfFiles = 5;
        List<MediaFile> mediaFiles = new ArrayList<>();
        for (int i = 1; i <= numberOfFiles; i++) {
            mediaFiles.add(
                    new MediaFile(Paths.get(path + "/0000" + i + ".tif").toUri(), FileLocationType.URL, "image/tiff"));
        }

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        metsKitodoWrapper.insertMediaFiles(mediaFiles);

        assertEquals(numberOfFiles, metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().size(), "Wrong number of divs in physical structMap");
        assertEquals(numberOfFiles, metsKitodoWrapper.getMets().getFileSec().getFileGrp().getFirst().getFile().size(), "Wrong number of files in fileSec");

        DivType divType = metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().get(1);

        assertEquals("uncounted", divType.getORDERLABEL(), "Wrong order label at second div");
        assertEquals(BigInteger.valueOf(2), divType.getORDER(), "Wrong order at second div");
        assertEquals("page", divType.getTYPE(), "Wrong type at second div");
        assertEquals("PHYS_0002", divType.getID(), "Wrong id at second div");

        FileType fileType = (FileType) divType.getFptr().getFirst().getFILEID();
        assertEquals("FILE_0002", fileType.getID(), "Wrong file id at second div");

    }


    @Test
    public void testPhysicalDivisionType() throws IOException, DatatypeConfigurationException {
        List<MediaFile> mediaFiles = new ArrayList<>();
        mediaFiles.add(new MediaFile(Paths.get("image/jpeg/0001.jpeg").toUri(), FileLocationType.URL, "image/jpeg"));
        mediaFiles.add(new MediaFile(Paths.get("audio/mpeg/0002.jpeg").toUri(), FileLocationType.URL, "audio/mpeg"));
        mediaFiles.add(new MediaFile(Paths.get("video/mp4/0003.jpeg").toUri(), FileLocationType.URL, "video/mp4"));

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("MultiMedia");
        metsKitodoWrapper.insertMediaFiles(mediaFiles);

        assertEquals(3, metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().size(), "Wrong number of divs in physical structMap");
        assertEquals(3, metsKitodoWrapper.getMets().getFileSec().getFileGrp().getFirst().getFile().size(), "Wrong number of files in fileSec");

        List<DivType> divTypes = metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv();
        assertEquals(PhysicalDivision.TYPE_PAGE, divTypes.get(0).getTYPE());
        assertEquals(PhysicalDivision.TYPE_TRACK, divTypes.get(1).getTYPE());
        assertEquals(PhysicalDivision.TYPE_TRACK, divTypes.get(2).getTYPE());
    }

    @Test
    public void shouldInsertRootLogicalDivAndMdSecAtCreation() throws DatatypeConfigurationException, IOException {
        String documentType = "Manuscript";
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(documentType);
        StructMapType logicalStructMap = metsKitodoWrapper.getLogicalStructMap();
        MdSecType rootDmdSec = (MdSecType) logicalStructMap.getDiv().getDMDID().getFirst();

        assertEquals(documentType, logicalStructMap.getDiv().getTYPE(), "Type of logical root div was wrong");
        assertEquals("DMDLOG_ROOT", rootDmdSec.getID(), "Id of root div related DmdSec was wrong");
    }

    @Test
    public void shouldGetKitodoTypeByDiv()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        KitodoType kitodoTypeOfDiv = metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(rootDiv);
        assertEquals("Test Publisher",
            kitodoTypeOfDiv.getMetadata().get(1).getValue(),
            "Reading metadata of dmdSec logical root div was wrong");

        DivType div = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1);
        kitodoTypeOfDiv = metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(div);
        assertEquals("Chapter 1",
            kitodoTypeOfDiv.getMetadata().getFirst().getValue(),
            "Reading metadata of dmdSec logical div was wrong");

        DivType divWithoutMetadata = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().getFirst();
        Exception exception = assertThrows(NoSuchElementException.class,
                () -> metsKitodoWrapper.getFirstKitodoTypeOfLogicalDiv(divWithoutMetadata)
            );
        assertEquals("Div element with id: LOG_0001 does not have metadata!", exception.getMessage());
    }

    @Test
    public void shouldGenerateIdsForDivsOfLogicalStructMap() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);

        assertEquals("LOG_0001",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0).getID(),
            "Id of first div at logical struct map was wrong");
        assertEquals("LOG_0005",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1).getID(),
            "Id of second div at logical struct map was wrong");
        assertEquals("LOG_0006",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(2).getID(),
            "Id of third div at logical struct map was wrong");
        assertEquals("LOG_0009",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(3).getID(),
            "Id of fourth div at logical struct map was wrong");
        assertEquals("LOG_0012",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getID(),
            "Id of second sub div of third div at logical struct map was wrong");
        assertEquals("LOG_0018",
            metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(4).getID(),
            "Id of fifth sub div of third div at logical struct map was wrong");
    }

    @Test
    public void shouldAddDivsAsFirstChild() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "AddedSubChapter",
            PositionOfNewDiv.FIRST_CHILD_OF_ELEMENT);
        assertEquals("AddedSubChapter", fifthDiv.getDiv().getFirst().getTYPE(), "New div was not added");
    }

    @Test
    public void shouldAddDivsBefor() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "AddedSubChapter", PositionOfNewDiv.BEFORE_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        assertEquals("AddedSubChapter", addedDiv.getTYPE(), "New div was not added");
    }

    @Test
    public void shouldAddDivsAfter() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "AddedSubChapter", PositionOfNewDiv.AFTER_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(5);
        assertEquals("AddedSubChapter", addedDiv.getTYPE(), "New div was not added");

    }

    @Test
    public void shouldDeepAddDivsAfter() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getDiv()
                .getFirst();
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "AddedSubSubChapter",
            PositionOfNewDiv.BEFORE_ELEMENT);
        DivType addedDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1).getDiv()
                .getFirst();
        assertEquals("AddedSubSubChapter", addedDiv.getTYPE(), "New div was not added");
    }

    @Test
    public void shouldNotAddDivAfterRoot() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        Exception exception = assertThrows(UnsupportedOperationException.class,
            () -> metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "AddedRoot", PositionOfNewDiv.BEFORE_ELEMENT));
        assertEquals("Root element cannot have a parent!", exception.getMessage());
    }

    private void fillLogicalStructMap(MetsKitodoWrapper metsKitodoWrapper) {
        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(rootDiv, "Chapter", PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType firstDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().getFirst();
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(firstDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(firstDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(firstDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType thirdDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(2);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(thirdDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(thirdDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType fifthDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthDiv, "SubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);

        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "SubSubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "SubSubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
        metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "SubSubChapter",
            PositionOfNewDiv.LAST_CHILD_OF_ELEMENT);
    }

    @Test
    public void shouldRemoveNestedLogicalDiv() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        metsKitodoWrapper.getLogicalStructMap().removeDiv(fifthSubDiv);
        assertEquals(4, metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().size(), "Could not remove div al logical structMap");
    }

    @Test
    public void shouldMovedLogicalDiv() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(4).getDiv().get(1);
        DivType firstDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(0);
        metsKitodoWrapper.getLogicalStructMap().moveDivToDivAtIndex(fifthSubDiv, firstDiv, 0);

        List<DivType> movedDivs = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().getFirst().getDiv().getFirst()
                .getDiv();

        assertEquals(3, movedDivs.size(), "Could not remove div al logical structMap");
        assertEquals("SubSubChapter", movedDivs.getFirst().getTYPE(), "Could not remove div al logical structMap");
    }

    @Test
    public void shouldAddNewDivBeforNotExistingDiv() throws IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("TestType");
        fillLogicalStructMap(metsKitodoWrapper);
        DivType fifthSubDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        fifthSubDiv.setID("notExisting");
        Exception exception = assertThrows(NoSuchElementException.class,
                () -> metsKitodoWrapper.getLogicalStructMap().addNewDiv(fifthSubDiv, "AddedRoot", PositionOfNewDiv.BEFORE_ELEMENT)
            );
        assertEquals("Child div element not found", exception.getMessage());
    }

    @Test
    public void shouldGetPhysicalDivsByLogicalDiv()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        DivType firstSubChapterDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1).getDiv().getFirst();
        List<DivType> physicalDivs = metsKitodoWrapper.getPhysicalDivsByLinkingLogicalDiv(firstSubChapterDiv);
        assertEquals(4, physicalDivs.size(), "Number of physical divs of first chapter was wrong");
        assertEquals("3", physicalDivs.get(2).getORDERLABEL(), "Orderlabel of last physical divs of first chapter was wrong");

        DivType secondChapterDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(2);
        physicalDivs = metsKitodoWrapper.getPhysicalDivsByLinkingLogicalDiv(secondChapterDiv);
        assertEquals(3, physicalDivs.size(), "Number of physical divs of second chapter was wrong");
        assertEquals("9", physicalDivs.get(2).getORDERLABEL(), "Orderlabel of last physical divs of first chapter was wrong");
    }

    @Test
    public void shouldInheritPhysicalDivsByChildLogicalDivs()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);

        DivType firstChapterDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1);
        metsKitodoWrapper.linkLogicalDivByInheritFromChildDivs(firstChapterDiv);

        List<DivType> physicalDivs = metsKitodoWrapper.getPhysicalDivsByLinkingLogicalDiv(firstChapterDiv);
        assertEquals(7, physicalDivs.size(), "Number of created linked physical divs was wrong");
    }

    @Test
    public void shouldRemoveSmLink()
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        DivType firstChapterDiv = metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().get(1).getDiv().get(2);
        List<DivType> physicalDivs = metsKitodoWrapper.getPhysicalDivsByLinkingLogicalDiv(firstChapterDiv);

        for (DivType physicalDiv : physicalDivs) {
            metsKitodoWrapper.getStructLink().removeSmLink(firstChapterDiv, physicalDiv);
        }

        assertEquals(0,
            metsKitodoWrapper.getPhysicalDivsByLinkingLogicalDiv(firstChapterDiv).size(),
            "Removing of smLinks was not successful");
    }

    @Test
    public void shouldGetDivOfLogicalStructMap() throws DatatypeConfigurationException, IOException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        DivType rootDivFromMets = metsKitodoWrapper.getMets().getStructMap().getFirst().getDiv();
        assertEquals(rootDiv, rootDivFromMets, "Div elements from getter and directly from mets objects were not the same");
    }

    @Test
    public void shouldGetDivOfLogicalStructMapFromExistingFile() throws DatatypeConfigurationException, IOException, JAXBException, TransformerException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlfile, xsltFile);
        DivType rootDiv = metsKitodoWrapper.getLogicalStructMap().getDiv();
        DivType rootDivFromMets = metsKitodoWrapper.getMets().getStructMap().get(1).getDiv();
        assertEquals(rootDiv, rootDivFromMets, "Div elements from getter and directly from mets objects were not the same");
    }

    @Test
    public void shouldWriteKitodoDataFormatVersion() throws DatatypeConfigurationException, IOException {
        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper("Manuscript");
        KitodoType kitodoType = metsKitodoWrapper.getDmdSecs().getFirst().getKitodoType();
        assertEquals("1.0", kitodoType.getVersion());
    }
}

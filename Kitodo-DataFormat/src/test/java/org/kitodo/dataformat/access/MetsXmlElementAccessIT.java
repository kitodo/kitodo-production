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

package org.kitodo.dataformat.access;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.ProcessingNote;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.KitodoConfig;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetsXmlElementAccessIT {

    private static final File OUT_FILE = new File("src/test/resources/out.xml");

    public static void clean() throws Exception {
        Files.deleteIfExists(OUT_FILE.toPath());
    }

    /**
     * Tests loading a workpiece from a METS file.
     */
    @Test
    public void testRead() throws Exception {
        Workpiece workpiece = new MetsXmlElementAccess()
                .read(new FileInputStream(new File("src/test/resources/meta.xml")));

        // METS file has 183 associated images
        assertEquals(183, workpiece.getPhysicalStructure().getChildren().size());

        // METS file has 17 unstructured images
        assertEquals(17, workpiece.getLogicalStructure().getViews().size());

        // root node has 16 children
        assertEquals(16, workpiece.getLogicalStructure().getChildren().size());

        // root node has 11 metadata entries
        assertEquals(11, workpiece.getLogicalStructure().getMetadata().size());

        // file URIs can be read
        assertEquals(new URI("images/ThomPhar_644901748_media/00000001.tif"),
            workpiece.getPhysicalStructure().getChildren().get(0).getMediaFiles().entrySet().iterator().next().getValue());

        // pagination can be read
        assertEquals(
            Arrays.asList("uncounted", "uncounted", "uncounted", "uncounted", "[I]", "[II]", "[III]", "[IV]", "V", "VI",
                "[VII]", "[VIII]", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
                "33", "34", "35", "36", "37", "38", "39", "[40]", "uncounted", "uncounted", "41", "42", "43", "44",
                "45", "46", "uncounted", "uncounted", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56",
                "[57]", "[58]", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "[72]",
                "73", "74", "[75]", "[76]", "[77]", "[78]", "79", "80", "uncounted", "uncounted", "uncounted",
                "uncounted", "81", "82", "uncounted", "uncounted", "83", "84", "uncounted", "uncounted", "85", "86",
                "[87]", "uncounted", "[88]", "uncounted", "89", "90", "91", "92", "uncounted", "[93]", "[94]",
                "uncounted", "95", "96", "uncounted", "uncounted", "97", "uncounted", "[98]", "uncounted", "99", "100",
                "uncounted", "uncounted", "uncounted", "uncounted", "101", "102", "103", "104", "105", "106",
                "uncounted", "uncounted", "107", "108", "109", "[110]", "111", "112", "uncounted", "uncounted",
                "uncounted", "uncounted", "113", "114", "115", "116", "117", "118", "uncounted", "uncounted", "119",
                "120", "uncounted", "uncounted", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130",
                "131", "132", "133", "134", "uncounted", "uncounted", "uncounted"),
            workpiece.getPhysicalStructure().getChildren().stream().map(PhysicalDivision::getOrderlabel)
                    .collect(Collectors.toList()));
    }

    @Test
    public void testSave() throws Exception {
        Workpiece workpiece = new Workpiece();
        workpiece.setId("1");

        List<PhysicalDivision> pages = new ArrayList<>();

        // add partial orders
        for (int i = 1; i <= 4; i++) {
            PhysicalDivision partialOrder = new PhysicalDivision();
            MetadataEntry numImages = new MetadataEntry();
            numImages.setKey("numImages");
            numImages.setDomain(MdSec.TECH_MD);
            numImages.setValue("100");
            partialOrder.getMetadata().add(numImages);
            workpiece.getPhysicalStructure().getChildren().add(partialOrder);
        }

        // add files
        MediaVariant local = new MediaVariant();
        local.setUse("LOCAL");
        local.setMimeType("image/tiff");
        for (int i = 1; i <= 4; i++) {
            URI path = new URI(String.format("images/leaflet_media/%08d.tif", i));
            PhysicalDivision physicalDivision = new PhysicalDivision();
            physicalDivision.setOrder(i);
            physicalDivision.getMediaFiles().put(local, path);
            pages.add(physicalDivision);
            workpiece.getPhysicalStructure().getChildren().add(physicalDivision);
        }

        // create document structure
        workpiece.getLogicalStructure().setType("leaflet");
        workpiece.getLogicalStructure().setLabel("The Leaflet");
        for (PhysicalDivision page : pages) {
            View view = new View();
            view.setPhysicalDivision(page);
            workpiece.getLogicalStructure().getViews().add(view);
            page.getLogicalDivisions().add(workpiece.getLogicalStructure());
        }

        LogicalDivision frontCover = new LogicalDivision();
        frontCover.setType("frontCover");
        frontCover.setLabel("Front cover");
        View view = new View();
        view.setPhysicalDivision(pages.get(0));
        frontCover.getViews().add(view);
        view.getPhysicalDivision().getLogicalDivisions().add(frontCover);
        workpiece.getLogicalStructure().getChildren().add(frontCover);

        LogicalDivision inside = new LogicalDivision();
        inside.setType("inside");
        inside.setLabel("Inside");
        view = new View();
        view.setPhysicalDivision(pages.get(1));
        inside.getViews().add(view);
        view.getPhysicalDivision().getLogicalDivisions().add(inside);
        view = new View();
        view.setPhysicalDivision(pages.get(2));
        inside.getViews().add(view);
        view.getPhysicalDivision().getLogicalDivisions().add(inside);
        workpiece.getLogicalStructure().getChildren().add(inside);

        LogicalDivision backCover = new LogicalDivision();
        backCover.setType("backCover");
        backCover.setLabel("Back cover");
        view = new View();
        view.setPhysicalDivision(pages.get(3));
        backCover.getViews().add(view);
        view.getPhysicalDivision().getLogicalDivisions().add(backCover);
        workpiece.getLogicalStructure().getChildren().add(backCover);

        // add metadata
        MetadataEntry title = new MetadataEntry();
        title.setKey("title");
        title.setDomain(MdSec.DMD_SEC);
        title.setValue("The title");
        frontCover.getMetadata().add(title);
        MetadataGroup author = new MetadataGroup();
        author.setKey("author");
        author.setDomain(MdSec.DMD_SEC);
        MetadataEntry firstName = new MetadataEntry();
        firstName.setKey("firstName");
        firstName.setValue("Alice");
        author.getMetadata().add(firstName);
        MetadataEntry lastName = new MetadataEntry();
        lastName.setKey("firstName");
        lastName.setValue("Smith");
        author.getMetadata().add(lastName);
        frontCover.getMetadata().add(author);

        MetadataEntry imagesConverted = new MetadataEntry();
        imagesConverted.setKey("imageConversionHint");
        imagesConverted.setDomain(MdSec.DIGIPROV_MD);
        imagesConverted.setValue("Images have been converted from TIFF to JPEG.");
        workpiece.getLogicalStructure().getMetadata().add(imagesConverted);
        frontCover.getMetadata().add(imagesConverted);
        inside.getMetadata().add(imagesConverted);
        backCover.getMetadata().add(imagesConverted);

        MetadataEntry copyright = new MetadataEntry();
        copyright.setKey("rights");
        copyright.setDomain(MdSec.RIGHTS_MD);
        copyright.setValue("© 2018 All rights reserved");
        backCover.getMetadata().add(copyright);

        // add derivatives
        MediaVariant max = new MediaVariant();
        max.setUse("MAX");
        max.setMimeType("image/jpeg");
        for (PhysicalDivision physicalDivision : workpiece.getPhysicalStructure().getChildren()) {
            URI tiffFile = physicalDivision.getMediaFiles().get(local);
            if (tiffFile != null) {
                String jpgFile = tiffFile.toString().replaceFirst("^.*?(\\d+)\\.tif$", "images/max/$1.jpg");
                URI path = new URI(jpgFile);
                physicalDivision.getMediaFiles().put(max, path);
            }
        }

        // leave a note
        ProcessingNote note = new ProcessingNote();
        note.setName("Workpiece integration test");
        note.setNote(new SimpleDateFormat("[dd.MM.yyyy HH:mm] ").format(new Date()) + "Process created");
        note.setRole("CREATOR");
        note.setType("software");
        workpiece.getEditHistory().add(note);

        // write file
        try (OutputStream out = new FileOutputStream(new File("src/test/resources/out.xml"))) {
            new MetsXmlElementAccess().save(workpiece, out);
        }

        // read the file and see if everything is in it
        Workpiece reread = new MetsXmlElementAccess().read(new FileInputStream(new File("src/test/resources/out.xml")));

        assertEquals(1, reread.getEditHistory().size());
        List<PhysicalDivision> physicalDivisions = reread.getPhysicalStructure().getChildren();
        assertEquals(8, physicalDivisions.size());
        for (int i = 0; i <= 3; i++) {
            PhysicalDivision physicalDivision = physicalDivisions.get(i);
            assertEquals(0, physicalDivision.getMediaFiles().size());
            assertEquals(1, physicalDivision.getMetadata().size());
        }
        for (int i = 4; i <= 7; i++) {
            PhysicalDivision physicalDivision = physicalDivisions.get(i);
            assertEquals(2, physicalDivision.getMediaFiles().size());
        }
        LogicalDivision logicalStructure = reread.getLogicalStructure();
        assertEquals(1, logicalStructure.getChildren().get(0).getViews().size());
        assertEquals(2, logicalStructure.getChildren().get(1).getViews().size());
        assertEquals(1, logicalStructure.getChildren().get(2).getViews().size());
        assertEquals(3, logicalStructure.getChildren().size());
        assertEquals(1, logicalStructure.getMetadata().size());

        clean();
    }

    @Test
    public void missingMetsHeaderCreationDateDidNotThrowNullPointerException() throws IOException {
        try (InputStream fileContent = new FileInputStream("src/test/resources/meta_missing_createdate.xml")) {
            Workpiece workpiece = new MetsXmlElementAccess()
                    .read(fileContent);
            assertNotNull(workpiece.getCreationDate());
        }
    }

    @Test
    public void missingMetsFileForPointer() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new MetsXmlElementAccess().read(
                        new FileInputStream("src/test/resources/meta_missing_file.xml")
                )
            );

        assertEquals("Corrupt file: file id for <mets:fptr> not found for div PHYS_0001", exception.getMessage());
    }

    @Test
    public void duplicateMetsFileDefinitionWithoutStrictFileIdCheck() throws IOException {
        try (InputStream fileContent = new FileInputStream("src/test/resources/meta_duplicate_file.xml")) {
            assertDoesNotThrow(
                () -> new MetsXmlElementAccess().read(fileContent)
            );
        }
    }

    @Test
    public void duplicateMetsFileDefinitionWithStrictFileIdCheck() {
        // mock access to KitodoConfig usage
        PropertiesConfiguration propertiesConfiguration = Mockito.mock(PropertiesConfiguration.class);
        MockedStatic<KitodoConfig> mockedConfig = Mockito.mockStatic(KitodoConfig.class);
        mockedConfig.when(KitodoConfig::getConfig).thenReturn(propertiesConfiguration);
        // mock getBoolean method call like in the main class
        Mockito.when(propertiesConfiguration.getBoolean("useStrictMetsFileIdCheck", false)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new MetsXmlElementAccess().read(
                        new FileInputStream("src/test/resources/meta_duplicate_file.xml")
                )
            );

        assertEquals("Corrupt file: each METS file ID has to be unique but FILE_0001 is used multiple times!", exception.getMessage());
    }

    @Test
    public void missingMetsFileGroupUse() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new MetsXmlElementAccess().read(
                    new FileInputStream("src/test/resources/meta_missing_file_use.xml")
                )
            );

        assertEquals("Corrupt file: file use for <mets:fptr> with id FILE_0001 not found in <mets:fileGrp>", exception.getMessage());
    }
}

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.ExistingOrLinkedStructure;
import org.kitodo.api.dataformat.LinkedStructure;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.ProcessingNote;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.InputStreamProviderInterface;

public class MetsXmlElementAccessIT {

    private static final File OUT_FILE = new File("src/test/resources/out.xml");

    private static final InputStreamProviderInterface INPUT_STREAM_PROVIDER = (uri, unused) -> {
        try {
            return new FileInputStream(new File("src/test/resources/" + uri.getPath()));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    };

    public static void clean() throws Exception {
        Files.deleteIfExists(OUT_FILE.toPath());
    }

    /**
     * Tests loading a workpiece from a METS file.
     */
    @Test
    public void testRead() throws Exception {
        Workpiece workpiece = new MetsXmlElementAccess()
                .read(new FileInputStream(new File("src/test/resources/meta.xml")), null);

        // METS file has 183 associated images
        assertEquals(183, workpiece.getMediaUnits().size());

        // all pages are linked to the root element
        assertEquals(workpiece.getMediaUnits().size(), workpiece.getStructure().getViews().size());

        // root node has 16 children
        assertEquals(16, workpiece.getStructure().getChildren().size());

        // root node has 11 meta-data entries
        assertEquals(11, workpiece.getStructure().getMetadata().size());

        // file URIs can be read
        assertEquals(new URI("images/ThomPhar_644901748_media/00000001.tif"),
            workpiece.getMediaUnits().get(0).getMediaFiles().entrySet().iterator().next().getValue());

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
            workpiece.getMediaUnits().stream().map(MediaUnit::getOrderlabel)
                    .collect(Collectors.toList()));
    }

    @Test
    public void testReadingHierarchyOfTop() throws Exception {
        Workpiece workpiece = new MetsXmlElementAccess()
                .read(new FileInputStream(new File("src/test/resources/top.xml")), INPUT_STREAM_PROVIDER);

        List<ExistingOrLinkedStructure> topStructMapChildren = workpiece.getStructure().getChildren();
        ExistingOrLinkedStructure firstBranch = topStructMapChildren.get(0);
        assertEquals("Other branch", firstBranch.getLabel());
        assertEquals("one", firstBranch.getType());
        ExistingOrLinkedStructure firstDownlink = ((Structure) firstBranch).getChildren().get(0);
        assertEquals(LinkedStructure.class, firstDownlink.getClass());
        assertEquals("Other METS file", firstDownlink.getLabel());
        assertEquals(BigInteger.valueOf(1), ((LinkedStructure) firstDownlink).getOrder());
        assertEquals("leaf", firstDownlink.getType());
        assertEquals("other.xml", ((LinkedStructure) firstDownlink).getUri().getPath());

        ExistingOrLinkedStructure secondBranch = topStructMapChildren.get(1);
        assertEquals("My branch", secondBranch.getLabel());
        assertEquals("two", secondBranch.getType());
        ExistingOrLinkedStructure secondDownlink = ((Structure) secondBranch).getChildren().get(0);
        assertEquals(LinkedStructure.class, secondDownlink.getClass());
        assertEquals("Between the METS files", secondDownlink.getLabel());
        assertEquals(BigInteger.valueOf(10), ((LinkedStructure) secondDownlink).getOrder());
        assertEquals("between", secondDownlink.getType());
        assertEquals("between.xml", ((LinkedStructure) secondDownlink).getUri().getPath());

        ExistingOrLinkedStructure thirdBranch = topStructMapChildren.get(2);
        assertEquals("Another branch", thirdBranch.getLabel());
        assertEquals("three", thirdBranch.getType());
        ExistingOrLinkedStructure thirdDownlink = ((Structure) thirdBranch).getChildren().get(0);
        assertEquals(LinkedStructure.class, thirdDownlink.getClass());
        assertEquals("Anther METS file", thirdDownlink.getLabel());
        assertEquals(BigInteger.valueOf(100), ((LinkedStructure) thirdDownlink).getOrder());
        assertEquals("leaf", thirdDownlink.getType());
        assertEquals("another.xml", ((LinkedStructure) thirdDownlink).getUri().getPath());
    }

    @Test
    public void testReadingHierarchyOfBetween() throws Exception {
        Workpiece workpiece = new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/between.xml")),
            INPUT_STREAM_PROVIDER);

        ExistingOrLinkedStructure downlink = workpiece.getStructure().getChildren().get(0);
        assertEquals(LinkedStructure.class, downlink.getClass());
        assertEquals("Leaf METS file", downlink.getLabel());
        assertEquals(BigInteger.valueOf(1), ((LinkedStructure) downlink).getOrder());
        assertEquals("leaf", downlink.getType());
        assertEquals("leaf.xml", ((LinkedStructure) downlink).getUri().getPath());

        List<LinkedStructure> uplinks = workpiece.getUplinks();
        assertEquals(2, uplinks.size());
        LinkedStructure top = uplinks.get(0);
        assertEquals("Top METS file", top.getLabel());
        assertEquals(null, top.getOrder());
        assertEquals("top", top.getType());
        assertEquals("top.xml", top.getUri().getPath());

        LinkedStructure second = uplinks.get(1);
        assertEquals("My branch", second.getLabel());
        assertEquals(BigInteger.valueOf(10), second.getOrder());
        assertEquals("two", second.getType());
        assertEquals("top.xml", second.getUri().getPath());
    }

    @Test
    public void testReadingHierarchyOfLeaf() throws Exception {
        Workpiece workpiece = new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/leaf.xml")),
            INPUT_STREAM_PROVIDER);

        List<LinkedStructure> leafUplinks = workpiece.getUplinks();
        assertEquals(3, leafUplinks.size());
        LinkedStructure top = leafUplinks.get(0);
        assertEquals("Top METS file", top.getLabel());
        assertEquals(null, top.getOrder());
        assertEquals("top", top.getType());
        assertEquals("top.xml", top.getUri().getPath());

        LinkedStructure second = leafUplinks.get(1);
        assertEquals("My branch", second.getLabel());
        assertEquals(BigInteger.valueOf(10), second.getOrder());
        assertEquals("two", second.getType());
        assertEquals("top.xml", second.getUri().getPath());

        LinkedStructure third = leafUplinks.get(2);
        assertEquals("Between the METS files", third.getLabel());
        assertEquals(BigInteger.valueOf(1), third.getOrder());
        assertEquals("between", third.getType());
        assertEquals("between.xml", third.getUri().getPath());
    }

    @Test(expected = IllegalStateException.class)
    public void testReadingHierarchyFailsIfSubordinateFileHasNoBackreference() throws Exception {
        new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/subordinate-no-backreference_between.xml")),
            INPUT_STREAM_PROVIDER);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadingHierarchyFailsIfSubordinateFileHasWrongBackreference() throws Exception {
        new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/subordinate-wrong-backreference_between.xml")),
            INPUT_STREAM_PROVIDER);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadingHierarchyFailsIfSuperordinateFileHasNoBackreference() throws Exception {
        new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/superordinate-no-backreference_leaf.xml")),
            INPUT_STREAM_PROVIDER);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadingHierarchyFailsIfSuperordinateFileHasAmbiguousBackreference() throws Exception {
        new MetsXmlElementAccess().read(
            new FileInputStream(new File("src/test/resources/superordinate-ambiguous-backreference_leaf.xml")),
            INPUT_STREAM_PROVIDER);
    }

    @Test
    public void testSave() throws Exception {
        Workpiece workpiece = new Workpiece();
        workpiece.setId("1");

        List<MediaUnit> pages = new ArrayList<>();

        // add partial orders
        for (int i = 1; i <= 4; i++) {
            MediaUnit partialOrder = new MediaUnit();
            MetadataEntry numImages = new MetadataEntry();
            numImages.setKey("numImages");
            numImages.setDomain(MdSec.TECH_MD);
            numImages.setValue("100");
            partialOrder.getMetadata().add(numImages);
            workpiece.getMediaUnits().add(partialOrder);
        }

        // add files
        MediaVariant local = new MediaVariant();
        local.setUse("LOCAL");
        local.setMimeType("image/tiff");
        for (int i = 1; i <= 4; i++) {
            URI path = new URI(String.format("images/leaflet_media/%08d.tif", i));
            MediaUnit mediaUnit = new MediaUnit();
            mediaUnit.setOrder(i);
            mediaUnit.getMediaFiles().put(local, path);
            pages.add(mediaUnit);
            workpiece.getMediaUnits().add(mediaUnit);
        }

        // create document structure
        workpiece.getStructure().setType("leaflet");
        workpiece.getStructure().setLabel("The Leaflet");
        for (MediaUnit page : pages) {
            View view = new View();
            view.setMediaUnit(page);
            workpiece.getStructure().getViews().add(view);
        }

        Structure frontCover = new Structure();
        frontCover.setType("frontCover");
        frontCover.setLabel("Front cover");
        View view = new View();
        view.setMediaUnit(pages.get(0));
        frontCover.getViews().add(view);
        workpiece.getStructure().getChildren().add(frontCover);

        Structure inside = new Structure();
        inside.setType("inside");
        inside.setLabel("Inside");
        view = new View();
        view.setMediaUnit(pages.get(1));
        inside.getViews().add(view);
        view = new View();
        view.setMediaUnit(pages.get(2));
        inside.getViews().add(view);
        workpiece.getStructure().getChildren().add(inside);

        Structure backCover = new Structure();
        backCover.setType("backCover");
        backCover.setLabel("Back cover");
        view = new View();
        view.setMediaUnit(pages.get(3));
        backCover.getViews().add(view);
        workpiece.getStructure().getChildren().add(backCover);

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
        author.getGroup().add(firstName);
        MetadataEntry lastName = new MetadataEntry();
        lastName.setKey("firstName");
        lastName.setValue("Smith");
        author.getGroup().add(lastName);
        frontCover.getMetadata().add(author);

        MetadataEntry imagesConverted = new MetadataEntry();
        imagesConverted.setKey("imageConversionHint");
        imagesConverted.setDomain(MdSec.DIGIPROV_MD);
        imagesConverted.setValue("Images have been converted from TIFF to JPEG.");
        workpiece.getStructure().getMetadata().add(imagesConverted);
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
        for (MediaUnit mediaUnit : workpiece.getMediaUnits()) {
            URI tiffFile = mediaUnit.getMediaFiles().get(local);
            if (tiffFile != null) {
                String jpgFile = tiffFile.toString().replaceFirst("^.*?(\\d+)\\.tif$", "images/max/$1.jpg");
                URI path = new URI(jpgFile);
                mediaUnit.getMediaFiles().put(max, path);
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
        Workpiece reread;
        try (InputStream inputStream = new FileInputStream(OUT_FILE)) {
            reread = new MetsXmlElementAccess().read(inputStream, null);
        }

        assertEquals(1, reread.getEditHistory().size());
        List<MediaUnit> mediaUnits = reread.getMediaUnits();
        assertEquals(8, mediaUnits.size());
        for (int i = 0; i <= 3; i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            assertEquals(0, mediaUnit.getMediaFiles().size());
            assertEquals(1, mediaUnit.getMetadata().size());
        }
        for (int i = 4; i <= 7; i++) {
            MediaUnit mediaUnit = mediaUnits.get(i);
            assertEquals(2, mediaUnit.getMediaFiles().size());
        }
        Structure structureRoot = reread.getStructure();
        assertEquals(4, structureRoot.getViews().size());
        assertEquals(3, structureRoot.getChildren().size());
        assertEquals(1, structureRoot.getMetadata().size());

        clean();
    }
}

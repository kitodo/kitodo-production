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

package org.kitodo.services.dataformat;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kitodo.api.dataformat.mets.AgentXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;

public class MetsServiceIT {

    /**
     * Tests loading a workpiece from a METS file.
     */
    @Test
    public void testReadXML() throws Exception {
        MetsXmlElementAccessInterface workpiece = MetsService.getInstance().createMets();
        workpiece.read(new FileInputStream(new File("../kitodo-dataformat/src/test/resources/meta.xml")));

        // METS file has 183 associated images
        assertEquals(183, workpiece.getFileGrp().size());

        // all pages are linked to the root element
        assertEquals(workpiece.getFileGrp().size(), workpiece.getStructMap().getAreas().size());

        // root node has 16 children
        assertEquals(16, workpiece.getStructMap().getChildren().size());

        // root node has 11 meta-data entries
        assertEquals(11, workpiece.getStructMap().getMetadata().size());

        // file URIs can be read
        assertEquals(new URI("images/ThomPhar_644901748_media/00000001.tif"),
            workpiece.getFileGrp().get(0).getAllUsesWithFLocats().iterator().next().getValue().getUri());

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
            workpiece.getFileGrp().stream().map(FileXmlElementAccessInterface::getOrderlabel)
                    .collect(Collectors.toList()));
    }

    @Test
    public void testWriteXML() throws Exception {
        MetsXmlElementAccessInterface workpiece = MetsService.getInstance().createMets();
        workpiece.setId("1");

        List<FileXmlElementAccessInterface> pages = new ArrayList<>();

        // add files
        UseXmlAttributeAccessInterface local = MetsService.getInstance().createUse();
        local.setUse("LOCAL");
        local.setMimeType("image/tiff");
        for (int i = 1; i <= 4; i++) {
            FLocatXmlElementAccessInterface path = MetsService.getInstance().createFLocat();
            path.setUri(new URI(String.format("images/leaflet_media/%08d.tif", i)));
            FileXmlElementAccessInterface mediaUnit = MetsService.getInstance().createFile();
            mediaUnit.setOrder(i);
            mediaUnit.putFLocatForUse(local, path);
            pages.add(mediaUnit);
            workpiece.getFileGrp().add(mediaUnit);
        }

        // create document structure
        workpiece.getStructMap().setType("leaflet");
        workpiece.getStructMap().setLabel("The Leaflet");
        for (FileXmlElementAccessInterface page : pages) {
            AreaXmlElementAccessInterface view = MetsService.getInstance().createArea();
            view.setFile(page);
            workpiece.getStructMap().getAreas().add(view);
        }

        DivXmlElementAccessInterface frontCover = MetsService.getInstance().createDiv();
        frontCover.setType("frontCover");
        frontCover.setLabel("Front cover");
        AreaXmlElementAccessInterface view = MetsService.getInstance().createArea();
        view.setFile(pages.get(0));
        frontCover.getAreas().add(view);
        workpiece.getStructMap().getChildren().add(frontCover);

        DivXmlElementAccessInterface inside = MetsService.getInstance().createDiv();
        inside.setType("inside");
        inside.setLabel("Inside");
        view = MetsService.getInstance().createArea();
        view.setFile(pages.get(1));
        inside.getAreas().add(view);
        view = MetsService.getInstance().createArea();
        view.setFile(pages.get(2));
        inside.getAreas().add(view);
        workpiece.getStructMap().getChildren().add(inside);

        DivXmlElementAccessInterface backCover = MetsService.getInstance().createDiv();
        backCover.setType("backCover");
        backCover.setLabel("Back cover");
        view = MetsService.getInstance().createArea();
        view.setFile(pages.get(3));
        backCover.getAreas().add(view);
        workpiece.getStructMap().getChildren().add(backCover);

        // add metadata
        MetadataXmlElementAccessInterface title = MetsService.getInstance().createMetadata();
        title.setType("title");
        title.setDomain(MdSec.DMD_SEC);
        title.setValue("The title");
        frontCover.getMetadata().add(title);
        MetadataGroupXmlElementAccessInterface author = MetsService.getInstance().createMetadataGroup();
        author.setType("author");
        author.setDomain(MdSec.DMD_SEC);
        MetadataXmlElementAccessInterface firstName = MetsService.getInstance().createMetadata();
        firstName.setType("firstName");
        firstName.setValue("Alice");
        author.getMetadata().add(firstName);
        MetadataXmlElementAccessInterface lastName = MetsService.getInstance().createMetadata();
        lastName.setType("firstName");
        lastName.setValue("Smith");
        author.getMetadata().add(lastName);
        frontCover.getMetadata().add(author);

        MetadataXmlElementAccessInterface imagesConverted = MetsService.getInstance().createMetadata();
        imagesConverted.setType("imageConversionHint");
        imagesConverted.setDomain(MdSec.DIGIPROV_MD);
        imagesConverted.setValue("Images have been converted from TIFF to JPEG.");
        workpiece.getStructMap().getMetadata().add(imagesConverted);
        frontCover.getMetadata().add(imagesConverted);
        inside.getMetadata().add(imagesConverted);
        backCover.getMetadata().add(imagesConverted);

        MetadataXmlElementAccessInterface copyright = MetsService.getInstance().createMetadata();
        copyright.setType("rights");
        copyright.setDomain(MdSec.RIGHTS_MD);
        copyright.setValue("© 2018 All rights reserved");
        backCover.getMetadata().add(copyright);

        // add derivatives
        UseXmlAttributeAccessInterface max = MetsService.getInstance().createUse();
        max.setUse("MAX");
        max.setMimeType("image/jpeg");
        for (FileXmlElementAccessInterface mediaUnit : workpiece.getFileGrp()) {
            String tiffFile = mediaUnit.getFLocatForUse(local).getUri().toString();
            String jpgFile = tiffFile.replaceFirst("^.*?(\\d+)\\.tif$", "images/max/$1.jpg");
            FLocatXmlElementAccessInterface path = MetsService.getInstance().createFLocat();
            path.setUri(new URI(jpgFile));
            mediaUnit.putFLocatForUse(max, path);
        }

        // leave a note
        AgentXmlElementAccessInterface note = MetsService.getInstance().createAgent();
        note.setName("METS service integration test");
        note.setNote(new SimpleDateFormat("[dd.MM.yyyy HH:mm] ").format(new Date()) + "Process created");
        note.setRole("CREATOR");
        note.setType("software");
        workpiece.getMetsHdr().add(note);

        // write file
        workpiece.save(new FileOutputStream(new File("src/test/resources/out.xml")));
    }
}

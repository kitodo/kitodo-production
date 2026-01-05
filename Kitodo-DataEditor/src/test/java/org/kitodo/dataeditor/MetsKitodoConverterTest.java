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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoConverterTest {
    private final URI xmlfile = Paths.get("src/test/resources/testmetaOldFormat.xml").toUri();
    private final URI xsltFile = Paths.get("src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
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

    @Test
    public void shouldReadKitodoMetadataFormOldFormatFile() throws JAXBException, TransformerException, IOException {
        Mets mets = MetsKitodoConverter.convertToMetsKitodoByXslt(xmlfile, xsltFile);
        JAXBElement jaxbElement = (JAXBElement) mets.getDmdSec().getFirst().getMdWrap().getXmlData().getAny().getFirst();
        KitodoType kitodoType = (KitodoType) jaxbElement.getValue();

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        assertEquals("PublisherName",
            metadataType.getName(),
            "Reading data of type 'name' out of kitodo format was not correct");
        assertEquals("Test Publisher",
            metadataType.getValue(),
            "Reading content metadata out of kitodo format was not correct");

        MetadataGroupType metadataGroup = kitodoType.getMetadataGroup().getFirst();
        assertEquals("TypeOfResource",
            metadataGroup.getName(),
            "Converting of metadata group was wrong at name attribute");
        assertEquals("Handschrift",
            metadataGroup.getMetadata().getFirst().getValue(),
            "Converting of metadata group was wrong at metadata child element");

        MetadataGroupType personMetadataGroup = kitodoType.getMetadataGroup().get(1);
        assertEquals("person",
            personMetadataGroup.getName(),
            "Converting of person was wrong at name attribute");
        assertEquals("FormerOwner",
            personMetadataGroup.getMetadata().getFirst().getValue(),
            "Converting of person was wrong at metadata child element");

    }
}

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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoConverterTest {
    private URI xmlfile = Paths.get("src/test/resources/testmetaOldFormat.xml").toUri();
    private URI xsltFile = Paths.get("src/test/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl").toUri();
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";
    private static byte[] testMetaOldFormat;

    @Before
    public void saveFile() throws IOException {
        File file = new File("src/test/resources/testmetaOldFormat.xml");
        testMetaOldFormat = IOUtils.toByteArray(file.toURI());
    }

    @After
    public void revertFile() throws IOException {
        IOUtils.write( testMetaOldFormat, Files.newOutputStream(Paths.get(pathOfOldMetaFormat)));
    }

    @Test
    public void shouldReadKitodoMetadataFormOldFormatFile() throws JAXBException, TransformerException, IOException {
        Mets mets = MetsKitodoConverter.convertToMetsKitodoByXslt(xmlfile, xsltFile);
        JAXBElement jaxbElement = (JAXBElement) mets.getDmdSec().get(0).getMdWrap().getXmlData().getAny().get(0);
        KitodoType kitodoType = (KitodoType) jaxbElement.getValue();

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getValue());

        MetadataGroupType metadataGroup = kitodoType.getMetadataGroup().get(0);
        Assert.assertEquals("Converting of metadata group was wrong at name attribute","TypeOfResource", metadataGroup.getName());
        Assert.assertEquals("Converting of metadata group was wrong at metadata child element","Handschrift", metadataGroup.getMetadata().get(0).getValue());

        MetadataGroupType personMetadataGroup = kitodoType.getMetadataGroup().get(1);
        Assert.assertEquals("Converting of person was wrong at name attribute","person", personMetadataGroup.getName());
        Assert.assertEquals("Converting of person was wrong at metadata child element","FormerOwner", personMetadataGroup.getMetadata().get(0).getValue());

    }
}

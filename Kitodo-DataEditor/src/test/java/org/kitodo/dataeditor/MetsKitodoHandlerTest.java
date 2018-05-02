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

import java.io.IOException;
import java.net.URI;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoHandlerTest {
    private URI xmlfile = URI.create("./src/test/resources/testmetaOldFormat.xml");

    @Test
    public void shouldReadKitodoMetadataFormOldFormatFile() throws JAXBException, TransformerException, IOException {
        Mets mets = MetsKitodoReader.readUriToMetsFromOldFormat(xmlfile);
        JAXBElement jaxbElement = (JAXBElement) mets.getDmdSec().get(0).getMdWrap().getXmlData().getAny().get(0);
        KitodoType kitodoType = (KitodoType) jaxbElement.getValue();

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getValue());
    }
}

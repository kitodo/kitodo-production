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
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MetadataType;

public class MetsKitodoUtilsTest {
    private URI xmlfile = URI.create("./src/test/resources/testmetaOldFormat.xml");

    @Test
    public void shouldReadKitodoMetadataFormOldFormatFile() throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(MetsKitodoReader.readUriToMetsFromOldFormat(xmlfile));
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecIndex(0);

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getValue());
    }

}

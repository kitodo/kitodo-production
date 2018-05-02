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
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;

public class MetsKitodoWriterTest {

    private URI xmlFile = URI.create("./src/test/resources/testmeta.xml");
    private MetsKitodoWriter metsKitodoWriter = new MetsKitodoWriter();

    @Test
    public void shouldWriteMetsFile() throws TransformerException, JAXBException, IOException, DatatypeConfigurationException {

        URI xmlTestFile = Paths.get(System.getProperty("user.dir") + "/target/test-classes/newtestmeta.xml").toUri();

        MetsKitodoWrapper metsKitodoWrapper = new MetsKitodoWrapper(xmlFile);
        metsKitodoWriter.save(metsKitodoWrapper.getMets(), xmlTestFile);
        MetsKitodoWrapper savedMetsKitodoWrapper = new MetsKitodoWrapper(xmlTestFile);
        Files.deleteIfExists(Paths.get(xmlTestFile));

        String loadedMetadata = metsKitodoWrapper.getKitodoTypeByMdSecId("DMDLOG_0000").getMetadata().get(0).getValue();
        String savedMetadata = savedMetsKitodoWrapper.getKitodoTypeByMdSecId("DMDLOG_0000").getMetadata().get(0)
                .getValue();

        Assert.assertEquals("The metadata of the loaded and the saved mets file are not equal", loadedMetadata,
            savedMetadata);
        Assert.assertEquals("The number of dmdSec elements of the loaded and the saved mets file are not equal",
            metsKitodoWrapper.getDmdSecs().size(), savedMetsKitodoWrapper.getDmdSecs().size());
    }
}

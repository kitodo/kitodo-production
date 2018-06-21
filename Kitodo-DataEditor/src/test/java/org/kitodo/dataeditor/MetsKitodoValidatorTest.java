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
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoValidatorTest {

    @Test
    public void shouldCheckValidMetsObject() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmeta.xml").toUri());
        Assert.assertTrue("Result of validation of Mets object was not true!",
            MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets));
    }

    @Test
    public void shouldCheckOldFormatMetsObject() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri());
        Assert.assertFalse("Result of validation of Mets object was not false!",
            MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets));
    }

    @Test
    public void shouldMetsContainsMetadataAtMdSecIndex() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmeta.xml").toUri());
        Assert.assertTrue("Result of checking if mets contains metadata at dmdSec index was wrong!",
            MetsKitodoValidator.metsContainsMetadataAtDmdSecIndex(mets, 2));
        Assert.assertFalse(
            "Result of checking if mets contains metadata at dmdSec index which does not exist was wrong!",
            MetsKitodoValidator.metsContainsMetadataAtDmdSecIndex(mets, 6));
    }
}

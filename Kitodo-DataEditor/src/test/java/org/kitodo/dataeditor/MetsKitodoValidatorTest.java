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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoValidatorTest {
    private static byte[] testMetaOldFormat;
    private static final String pathOfOldMetaFormat = "src/test/resources/testmetaOldFormat.xml";

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
    public void shouldCheckValidMetsObject() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmeta.xml").toUri());
        assertTrue(MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets),
            "Result of validation of Mets object was not true!");
    }

    @Test
    public void shouldCheckOldFormatMetsObject() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmetaOldFormat.xml").toUri());
        assertFalse(MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets),
            "Result of validation of Mets object was not false!");
    }

    @Test
    public void shouldMetsContainsMetadataAtMdSecIndex() throws JAXBException, IOException {
        Mets mets = MetsKitodoReader.readUriToMets(Paths.get("./src/test/resources/testmeta.xml").toUri());
        assertTrue(MetsKitodoValidator.metsContainsMetadataAtDmdSecIndex(mets, 2),
            "Result of checking if mets contains metadata at dmdSec index was wrong!");
        assertFalse(MetsKitodoValidator.metsContainsMetadataAtDmdSecIndex(mets, 6),
            "Result of checking if mets contains metadata at dmdSec index which does not exist was wrong!");
    }
}

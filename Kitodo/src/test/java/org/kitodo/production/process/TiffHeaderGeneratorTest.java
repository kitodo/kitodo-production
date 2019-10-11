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

package org.kitodo.production.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TiffHeaderGeneratorTest {

    @Test
    // TODO: add more test cases
    public void shouldGenerateTiffHeader() throws Exception {
        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator("TestTest", TitleGeneratorTest.createProcessDetailsList());
        String created = tiffHeaderGenerator.generateTiffHeader(
            "'|[[DOC_TYPE]]'+$Doctype+'|[[HAUPTTITEL]]'+TitleDocMain+'|[[AUTOREN/HERAUSGEBER]]'+Autoren+"
                    + "'|[[JAHR]]'+PublicationYear+'|[[ERSCHEINUNGSORT]]'+PlaceOfPublication+'|[[VERZ_STRCT]]'+"
                    + "TSL_ATS+'_'+CatalogIDDigital+'|'",
            "monograph");
        assertEquals("Created hash doesn't match the precomputed one!",
            "|<DOC_TYPE>Monographie|<HAUPTTITEL>Test|<AUTOREN/HERAUSGEBER>TestAuthor|<JAHR>|<ERSCHEINUNGSORT>|"
                    + "<VERZ_STRCT>TestTest_123|",
            created);
    }
}

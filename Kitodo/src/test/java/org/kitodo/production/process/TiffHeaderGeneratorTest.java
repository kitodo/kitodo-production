/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
        assertEquals("|<DOC_TYPE>Monographie|<HAUPTTITEL>Test|<AUTOREN/HERAUSGEBER>TestAuthor|<JAHR>|<ERSCHEINUNGSORT>|"
                + "<VERZ_STRCT>TestTest_123|", created, "Created hash doesn't match the precomputed one!");
    }
}

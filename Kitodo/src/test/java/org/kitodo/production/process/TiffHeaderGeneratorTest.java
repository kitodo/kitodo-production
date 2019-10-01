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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTab;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTableRow;
import org.kitodo.production.forms.createprocess.FieldedAdditionalDetailsTableRow;
import org.kitodo.production.services.ServiceManager;

public class TiffHeaderGeneratorTest {

    @Test
    // TODO: add more test cases
    public void shouldGenerateTiffHeader() throws Exception {
        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator("TestTest", createAdditionalDetailsRows());
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

    private List<AdditionalDetailsTableRow> createAdditionalDetailsRows() throws IOException {
        Workpiece workpiece = new Workpiece();
        workpiece.getRootElement().setType("Monograph");
        RulesetManagementInterface rulesetManagementInterface = ServiceManager.getRulesetManagementService().getRulesetManagement();
        rulesetManagementInterface.load(new File("src/test/resources/rulesets/monograph.xml"));
        StructuralElementViewInterface monograph = rulesetManagementInterface.getStructuralElementView(
                "Monograph", "", Locale.LanguageRange.parse("en"));
        FieldedAdditionalDetailsTableRow additionalDetailsTable = new FieldedAdditionalDetailsTableRow(
                null, workpiece.getRootElement(), monograph);
        for (AdditionalDetailsTableRow row : additionalDetailsTable.getRows()) {
            switch (row.getMetadataID()) {
                case "TitleDocMain":
                case "TitleDocMainShort":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, "Test");
                    break;
                case "TSL_ATS":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, "");
                    break;
                case "CatalogIDSource":
                case "CatalogIDDigital":
                    AdditionalDetailsTab.setAdditionalDetailsRow(row, "123");
                    break;
                case "Person":
                    for (AdditionalDetailsTableRow personMetadataRow : ((FieldedAdditionalDetailsTableRow) row).getRows()) {
                        switch (personMetadataRow.getMetadataID()) {
                            case "Role":
                            case "LastName":
                                AdditionalDetailsTab.setAdditionalDetailsRow(personMetadataRow, "Author");
                                break;
                            case "FirstName":
                                AdditionalDetailsTab.setAdditionalDetailsRow(personMetadataRow, "Test");
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return additionalDetailsTable.getRows();
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTab;
import org.kitodo.production.forms.createprocess.AdditionalDetailsTableRow;
import org.kitodo.production.forms.createprocess.FieldedAdditionalDetailsTableRow;
import org.kitodo.production.services.ServiceManager;

/**
 * Created for test switch statement.
 */
public class TitleGeneratorTest {

    private static Map<String, Map<String, String>> testData;

    static {
        testData = new HashMap<>();

        testData.put("LuthSpoc", new HashMap<String, String>() {
            {
                put("Luther, Martin", "Spocžatki kscheszijanskeje wucžby aby D. M. Luthera mały Katechismuß");
            }
        });
        testData.put("BrunITr", new HashMap<String, String>() {
            {
                put("Bruno, Vincenzo", "I Tre Dialoghi Del Dottor Fisico Vincenzo Bruno Di Melfi");
            }
        });
        testData.put("PallAhi", new HashMap<String, String>() {
            {
                put("Palliser, Fanny", "A history of lace");
            }
        });
        testData.put("HaywDer", new HashMap<String, String>() {
            {
                put("Haywood, Eliza Fowler", "Der unsichtbare Kundschafter");
            }
        });
        testData.put("Schrdeund", new HashMap<String, String>() {
            {
                put("", "Schriften des unter dem Hohen Protektorate S. K. Hoheit des Prinzen Georg stehenden Sächsischen Fischereivereines");
            }
        });
        testData.put("DieacKid", new HashMap<String, String>() {
            {
                put("", "Die achtzig Kirchenlieder der Schulregulative");
            }
        });
        testData.put("Luth", new HashMap<String, String>() {
            {
                put("Luther, Martin", "");
            }
        });
        testData.put("", new HashMap<String, String>() {
            {
                put("", "");
            }
        });
    }

    @Test
    public void shouldCreateAtstsl() {
        for (String givenHash : testData.keySet()) {
            for (Map.Entry<String, String> entry : testData.get(givenHash).entrySet()) {
                String created = TitleGenerator.createAtstsl(entry.getValue(), entry.getKey());
                assertEquals("Created hash doesn't match the precomputed one!", givenHash, created);
            }
        }
    }

    @Test
    //TODO: add more test cases
    public void shouldGenerateTitle() throws Exception {
        TitleGenerator titleGenerator = new TitleGenerator("", createAdditionalDetailsRows());
        String created = titleGenerator.generateTitle("TSL/ATS+'_'+PPN (digital)", null);
        assertEquals("Created hash doesn't match the precomputed one!", "TestTest_123", created);
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

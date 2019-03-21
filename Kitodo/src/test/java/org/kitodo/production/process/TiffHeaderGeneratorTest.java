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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kitodo.production.helper.AdditionalField;

public class TiffHeaderGeneratorTest {

    @Test
    // TODO: add more test cases
    public void shouldGenerateTiffHeader() {
        List<AdditionalField> additionalFields = createAdditionalFields();

        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator("TestTest", additionalFields);
        String created = tiffHeaderGenerator.generateTiffHeader(
            "'|<DOC_TYPE>'+$Doctype+'|<HAUPTTITEL>'+Titel+'|<AUTOREN/HERAUSGEBER>'+Autoren+'|"
                    + "<JAHR>'+Erscheinungsjahr+'|<ERSCHEINUNGSORT>'+Erscheinungsort+'|<VERZ_STRCT>'+ATS+'_'+PPN digital a-Satz+'|'",
            "monograph");
        assertEquals("Created hash doesn't match the precomputed one!",
            "|<DOC_TYPE>Monographie|<HAUPTTITEL>Test|<AUTOREN/HERAUSGEBER>Test Author|<JAHR>|<ERSCHEINUNGSORT>|"
                    + "<VERZ_STRCT>TestTest_123|",
            created);
    }

    private List<AdditionalField> createAdditionalFields() {
        List<AdditionalField> additionalFields = new ArrayList<>();
        additionalFields.add(createAdditionalField("Artist", "", ""));
        additionalFields.add(createAdditionalField("Schrifttyp", "", ""));
        additionalFields.add(createAdditionalField("Titel", "Test", "TitleDocMain"));
        additionalFields.add(createAdditionalField("Titel (Sortierung)", "Test", "TitleDocMainShort"));
        additionalFields.add(createAdditionalField("Autoren", "Test Author", "ListOfCreators"));
        additionalFields.add(createAdditionalField("ATS", "", "TSL_ATS"));
        additionalFields.add(createAdditionalField("TSL", "", "TSL_ATS"));
        additionalFields.add(createAdditionalField("PPN analog a-Satz", "123", "CatalogIDSource"));
        additionalFields.add(createAdditionalField("PPN digital a-Satz", "123", "CatalogIDDigital"));
        return additionalFields;
    }

    private AdditionalField createAdditionalField(String title, String value, String metadata) {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setTitle(title);
        additionalField.setValue(value);
        additionalField.setMetadata(metadata);
        additionalField.setIsdoctype("monograph");
        return additionalField;
    }
}

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

package org.kitodo.data.elasticsearch.index.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;

/**
 * Test class for DocketType.
 */
public class RulesetTypeTest {

    private static List<Ruleset> prepareData() {

        List<Ruleset> rulesets = new ArrayList<>();

        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setId(1);
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_slubdd.xml");
        rulesets.add(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setId(2);
        secondRuleset.setTitle("SUBHH");
        secondRuleset.setFile("ruleset_subhh.xml");
        rulesets.add(secondRuleset);

        return rulesets;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        RulesetType rulesetType = new RulesetType();
        Ruleset ruleset = prepareData().get(0);

        Map<String, Object> actual = rulesetType.createDocument(ruleset);

        assertEquals("SLUBDD", RulesetTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("ruleset_slubdd.xml", RulesetTypeField.FILE.getStringValue(actual), "Key file doesn't match to given value!");
        assertFalse(RulesetTypeField.ORDER_METADATA_BY_RULESET.getBooleanValue(actual), "Key orderMetadataByRuleset doesn't match to given value!");
        assertTrue(RulesetTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        RulesetType rulesetType = new RulesetType();
        Ruleset ruleset = prepareData().get(0);

        Map<String, Object> actual = rulesetType.createDocument(ruleset);

        assertEquals(6, actual.keySet().size(), "Amount of keys is incorrect!");
    }

    @Test
    public void shouldCreateDocuments() {
        RulesetType rulesetType = new RulesetType();

        List<Ruleset> rulesets = prepareData();
        Map<Integer, Map<String, Object>> documents = rulesetType.createDocuments(rulesets);
        assertEquals(2, documents.size(), "HashMap of documents doesn't contain given amount of elements!");
    }
}

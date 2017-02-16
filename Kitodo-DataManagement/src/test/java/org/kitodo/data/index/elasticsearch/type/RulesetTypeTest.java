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

package org.kitodo.data.index.elasticsearch.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.junit.Test;

import org.kitodo.data.database.beans.Ruleset;

import static org.junit.Assert.*;

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

        HttpEntity document = rulesetType.createDocument(ruleset);
        String actual = EntityUtils.toString(document);
        String excepted = "{\"title\":\"SLUBDD\",\"file\":\"ruleset_slubdd.xml\"}";
        assertEquals("Ruleset JSON string doesn't match to given plain text!", excepted, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        RulesetType rulesetType = new RulesetType();

        List<Ruleset> rulesets = prepareData();
        HashMap<Integer, HttpEntity> documents = rulesetType.createDocuments(rulesets);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}

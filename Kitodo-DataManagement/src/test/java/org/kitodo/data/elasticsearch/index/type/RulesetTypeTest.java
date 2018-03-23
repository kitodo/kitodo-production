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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Ruleset;

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

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "SLUBDD", actual.getString("title"));
        assertEquals("Key file doesn't match to given value!", "ruleset_slubdd.xml", actual.getString("file"));
        assertEquals("Key orderMetadataByRuleset doesn't match to given value!", false, actual.getBoolean("orderMetadataByRuleset"));
        assertEquals("Key fileContent doesn't match to given value!", "", actual.getString("fileContent"));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        RulesetType rulesetType = new RulesetType();
        Ruleset ruleset = prepareData().get(0);

        HttpEntity document = rulesetType.createDocument(ruleset);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 4, actual.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        RulesetType rulesetType = new RulesetType();

        List<Ruleset> rulesets = prepareData();
        HashMap<Integer, HttpEntity> documents = rulesetType.createDocuments(rulesets);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}

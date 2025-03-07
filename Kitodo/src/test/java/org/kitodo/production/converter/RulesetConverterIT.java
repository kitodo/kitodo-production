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

package org.kitodo.production.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Ruleset;

public class RulesetConverterIT {

    private static final String MESSAGE = "Ruleset was not converted correctly!";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertRulesets();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset ruleset = (Ruleset) rulesetConverter.getAsObject(null, null, "2");
        assertEquals(2, ruleset.getId().intValue(), MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = (String) rulesetConverter.getAsObject(null, null, "in");
        assertEquals("0", ruleset, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = (String) rulesetConverter.getAsObject(null, null, "10");
        assertEquals("0", ruleset, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Object ruleset = rulesetConverter.getAsObject(null, null, null);
        assertNull(ruleset, MESSAGE);
    }

    @Test
    public void shouldGetAsString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset newRuleset = new Ruleset();
        newRuleset.setId(20);
        String ruleset = rulesetConverter.getAsString(null, null, newRuleset);
        assertEquals("20", ruleset, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset newRuleset = new Ruleset();
        String ruleset = rulesetConverter.getAsString(null, null, newRuleset);
        assertEquals("0", ruleset, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = rulesetConverter.getAsString(null, null, "20");
        assertEquals("20", ruleset, MESSAGE);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = rulesetConverter.getAsString(null, null, null);
        assertNull(ruleset, MESSAGE);
    }
}

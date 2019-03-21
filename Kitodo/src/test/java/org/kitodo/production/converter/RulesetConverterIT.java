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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Ruleset;

public class RulesetConverterIT {

    private static final String MESSAGE = "Ruleset was not converted correctly!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertRulesets();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset ruleset = (Ruleset) rulesetConverter.getAsObject(null, null, "2");
        assertEquals(MESSAGE, 2, ruleset.getId().intValue());
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = (String) rulesetConverter.getAsObject(null, null, "in");
        assertEquals(MESSAGE, "0", ruleset);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = (String) rulesetConverter.getAsObject(null, null, "10");
        assertEquals(MESSAGE, "0", ruleset);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Object ruleset = rulesetConverter.getAsObject(null, null, null);
        assertNull(MESSAGE, ruleset);
    }

    @Test
    public void shouldGetAsString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset newRuleset = new Ruleset();
        newRuleset.setId(20);
        String ruleset = rulesetConverter.getAsString(null, null, newRuleset);
        assertEquals(MESSAGE, "20", ruleset);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        Ruleset newRuleset = new Ruleset();
        String ruleset = rulesetConverter.getAsString(null, null, newRuleset);
        assertEquals(MESSAGE, "0", ruleset);
    }

    @Test
    public void shouldGetAsStringWithString() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = rulesetConverter.getAsString(null, null, "20");
        assertEquals(MESSAGE, "20", ruleset);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        RulesetConverter rulesetConverter = new RulesetConverter();
        String ruleset = rulesetConverter.getAsString(null, null, null);
        assertNull(MESSAGE, ruleset);
    }
}

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

package org.kitodo.services;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Ruleset;

import org.kitodo.data.database.exceptions.DAOException;
import ugh.dl.Prefs;

import static org.junit.Assert.*;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertRulesets();
    }

    @AfterClass
    public static void cleanDatabase() {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindRuleset() throws Exception {
        RulesetService rulesetService = new RulesetService();

        Ruleset ruleset = rulesetService.find(1);
        boolean condition = ruleset.getTitle().equals("SLUBDD") && ruleset.getFile().equals("ruleset_slubdd.xml");
        assertTrue("Ruleset was not found in database!", condition);
    }

    @Test
    public void shouldFindAllRulesets() throws Exception {
        RulesetService rulesetService = new RulesetService();

        List<Ruleset> rulesets = rulesetService.findAll();
        assertEquals("Not all rulesets were found in database!", 2, rulesets.size());
    }

    @Test
    public void shouldGetPreferences() throws Exception {
        RulesetService rulesetService = new RulesetService();

        Ruleset ruleset = rulesetService.find(1);
        String actual = rulesetService.getPreferences(ruleset).getVersion();
        //not sure how to really check if Pref is correct
        System.out.println("Preferences: " + actual);
        assertEquals("Preference is incorrect!", "1.1-20091117", actual);
    }
}

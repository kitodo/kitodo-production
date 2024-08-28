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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    private static final RulesetService rulesetService = ServiceManager.getRulesetService();

    private static final String slubDD = "SLUBDD";
    private static final String rulesetNotFound = "Ruleset was not found in index!";

    @Before
    public void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertRulesets();
        MockDatabase.setUpAwaitility();
        User userOne = new User();
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !Collections.singleton(rulesetService.getByTitle(slubDD)).isEmpty();
        });
    }

    @After
    public void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllRulesets() throws DataException {
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(3), rulesetService.count());
    }

    @Test
    public void shouldCountAllDatabaseRowsForRulesets() throws Exception {
        Long amount = rulesetService.countDatabaseRows();
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindRuleset() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        boolean condition = ruleset.getTitle().equals(slubDD) && ruleset.getFile().equals("ruleset_test.xml");
        assertTrue("Ruleset was not found in database!", condition);
    }

    @Test
    public void shouldFindAllRulesets() throws Exception {
        List<Ruleset> rulesets = rulesetService.getAll();
        assertEquals("Not all rulesets were found in database!", 3, rulesets.size());
    }

    @Test
    public void shouldGetAllRulesetsInGivenRange() throws Exception {
        List<Ruleset> rulesets = rulesetService.getAll(1, 10);
        assertEquals("Not all rulesets were found in database!", 2, rulesets.size());
    }

    @Test
    public void shouldFindById() throws DataException {
        assertEquals(rulesetNotFound, slubDD, rulesetService.findById(1).getTitle());
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(rulesetNotFound, 1, rulesetService.getByTitle(slubDD).size());
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws DAOException {
        assertEquals("Not all rulesets were found in index!", 3, rulesetService.getAll().size());
    }

    @Test
    public void shouldRemoveRuleset() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("To Remove");
        rulesetService.save(ruleset);
        Ruleset foundRuleset = rulesetService.getById(4);
        assertEquals("Additional ruleset was not inserted in database!", "To Remove", foundRuleset.getTitle());

        rulesetService.remove(ruleset);
        exception.expect(DAOException.class);
        rulesetService.getById(4);

        ruleset = new Ruleset();
        ruleset.setTitle("To remove");
        rulesetService.save(ruleset);
        foundRuleset = rulesetService.getById(5);
        assertEquals("Additional ruleset was not inserted in database!", "To remove", foundRuleset.getTitle());

        rulesetService.remove(foundRuleset);
        exception.expect(DAOException.class);
        rulesetService.getById(5);
    }
}

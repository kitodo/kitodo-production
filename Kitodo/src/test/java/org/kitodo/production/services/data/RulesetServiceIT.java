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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    private static final RulesetService rulesetService = ServiceManager.getRulesetService();

    private static final String slubDD = "SLUBDD";
    private static final String rulesetNotFound = "Ruleset was not found in index!";

    @BeforeEach
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

    @AfterEach
    public void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllRulesets() throws DAOException {
        assertEquals(Long.valueOf(3), rulesetService.count(), "Rulesets were not counted correctly!");
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldCountAllRulesetsAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForRulesets() throws Exception {
        Long amount = rulesetService.count();
        assertEquals(Long.valueOf(3), amount, "Rulesets were not counted correctly!");
    }

    @Test
    public void shouldFindRuleset() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        boolean condition = ruleset.getTitle().equals(slubDD) && ruleset.getFile().equals("ruleset_test.xml");
        assertTrue(condition, "Ruleset was not found in database!");
    }

    @Test
    public void shouldFindAllRulesets() throws Exception {
        List<Ruleset> rulesets = rulesetService.getAll();
        assertEquals(3, rulesets.size(), "Not all rulesets were found in database!");
    }

    @Test
    public void shouldGetAllRulesetsInGivenRange() throws Exception {
        List<Ruleset> rulesets = rulesetService.getAll(1, 10);
        assertEquals(2, rulesets.size(), "Not all rulesets were found in database!");
    }

    @Test
    public void shouldFindById() throws DAOException {
        assertEquals(slubDD, rulesetService.getById(1).getTitle(), rulesetNotFound);
    }

    @Test
    public void shouldFindByTitle() throws DAOException {
        assertEquals(1, rulesetService.getByTitle(slubDD).size(), rulesetNotFound);
    }

    @Test
    public void shouldFindByTitleAndClient() throws DAOException {
        int CLIENT_ID_MATCH = 1;
        assertEquals(1, rulesetService.getByTitleAndClient(slubDD,
                ServiceManager.getClientService().getById(CLIENT_ID_MATCH)).size(), rulesetNotFound);
    }

    @Test
    public void shouldNotFindByTitleAndWrongClient() throws DAOException {
        int CLIENT_ID_MISMATCH = 2;
        assertEquals(0, rulesetService.getByTitleAndClient(slubDD,
                ServiceManager.getClientService().getById(CLIENT_ID_MISMATCH)).size(), rulesetNotFound);
    }



    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindByFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByClientId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByClientId() throws Exception, DAOException {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByClientId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindByTitleAndFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByTitleAndFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindManyByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindOneByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldNotFindByTitleOrFile() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws DAOException {
        assertEquals(3, rulesetService.getAll().size(), "Not all rulesets were found in database!");
    }

    @Test
    public void shouldRemoveRuleset() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("To Remove");
        rulesetService.save(ruleset);
        Ruleset foundRuleset = rulesetService.getById(4);
        assertEquals("To Remove", foundRuleset.getTitle(), "Additional ruleset was not inserted in database!");

        rulesetService.remove(ruleset);
        assertThrows(DAOException.class, () -> rulesetService.getById(4));

        ruleset = new Ruleset();
        ruleset.setTitle("To remove");
        rulesetService.save(ruleset);
        foundRuleset = rulesetService.getById(5);
        assertEquals("To remove", foundRuleset.getTitle(), "Additional ruleset was not inserted in database!");

        rulesetService.remove(foundRuleset);
        assertThrows(DAOException.class, () -> rulesetService.getById(5));
    }
}

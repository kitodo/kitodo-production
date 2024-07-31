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
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;
import org.kitodo.data.exceptions.DataException;
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
            return !rulesetService.findByTitle(slubDD, true).isEmpty();
        });
    }

    @AfterEach
    public void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllRulesets() throws DataException {
        assertEquals(Long.valueOf(3), rulesetService.count(), "Rulesets were not counted correctly!");
    }

    @Test
    public void shouldCountAllRulesetsAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", slubDD).operator(Operator.AND);
        assertEquals(Long.valueOf(1), rulesetService.count(query), "Rulesets were not counted correctly!");
    }

    @Test
    public void shouldCountAllDatabaseRowsForRulesets() throws Exception {
        Long amount = rulesetService.countDatabaseRows();
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
    public void shouldFindById() throws DataException {
        assertEquals(slubDD, rulesetService.findById(1).getTitle(), rulesetNotFound);
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(1, rulesetService.findByTitle(slubDD, true).size(), rulesetNotFound);
    }

    @Test
    public void shouldFindByFile() throws DataException {
        String expected = "ruleset_test.xml";
        assertEquals(expected, rulesetService.findByFile("ruleset_test.xml").get(RulesetTypeField.FILE.getKey()), rulesetNotFound);
    }

    @Test
    public void shouldFindManyByClientId() throws DataException {
        assertEquals(2, rulesetService.findByClientId(1).size(), "Rulesets were not found in index!");
    }

    @Test
    public void shouldFindOneByClientId() throws DataException, DAOException {
        SecurityTestUtils.addUserDataToSecurityContext(new User(), 2);
        assertEquals(1, rulesetService.findByClientId(2).size(), rulesetNotFound);
    }

    @Test
    public void shouldNotFindByClientId() throws DataException {
        assertEquals(0, rulesetService.findByClientId(3).size(), "Ruleset was found in index!");
    }

    @Test
    public void shouldFindByTitleAndFile() throws DataException {
        Integer expected = 2;
        assertEquals(expected, rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile("SUBHH", "ruleset_subhh.xml")), rulesetNotFound);
    }

    @Test
    public void shouldNotFindByTitleAndFile() throws DataException {
        Integer expected = 0;
        assertEquals(expected, rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile(slubDD, "none")), "Ruleset was found in index!");
    }

    @Test
    public void shouldFindManyByTitleOrFile() throws DataException {
        assertEquals(2, rulesetService.findByTitleOrFile(slubDD, "ruleset_subhh.xml").size(), "Rulesets were not found in index!");
    }

    @Test
    public void shouldFindOneByTitleOrFile() throws DataException {
        assertEquals(1, rulesetService.findByTitleOrFile("default", "ruleset_subhh.xml").size(), rulesetNotFound);
    }

    @Test
    public void shouldNotFindByTitleOrFile() throws DataException {
        assertEquals(0, rulesetService.findByTitleOrFile("none", "none").size(), "Some rulesets were found in index!");
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws DataException {
        assertEquals(3, rulesetService.findAllDocuments().size(), "Not all rulesets were found in index!");
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

        rulesetService.remove(5);
        assertThrows(DAOException.class, () -> rulesetService.getById(5));
    }
}

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
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
            return !rulesetService.findByTitle(slubDD, true).isEmpty();
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
    public void shouldCountAllRulesetsAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", slubDD).operator(Operator.AND);
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(1), rulesetService.count(query));
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
        assertEquals(rulesetNotFound, 1, rulesetService.findByTitle(slubDD, true).size());
    }

    @Test
    public void shouldFindByFile() throws DataException {
        String expected = "ruleset_test.xml";
        assertEquals(rulesetNotFound, expected,
            rulesetService.findByFile("ruleset_test.xml").get(RulesetTypeField.FILE.getKey()));
    }

    @Test
    public void shouldFindManyByClientId() throws DataException {
        assertEquals("Rulesets were not found in index!", 2, rulesetService.findByClientId(1).size());
    }

    @Test
    public void shouldFindOneByClientId() throws DataException, DAOException {
        SecurityTestUtils.addUserDataToSecurityContext(new User(), 2);
        assertEquals(rulesetNotFound, 1, rulesetService.findByClientId(2).size());
    }

    @Test
    public void shouldNotFindByClientId() throws DataException {
        assertEquals("Ruleset was found in index!", 0, rulesetService.findByClientId(3).size());
    }

    @Test
    public void shouldFindByTitleAndFile() throws DataException {
        Integer expected = 2;
        assertEquals(rulesetNotFound, expected,
            rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile("SUBHH", "ruleset_subhh.xml")));
    }

    @Test
    public void shouldNotFindByTitleAndFile() throws DataException {
        Integer expected = 0;
        assertEquals("Ruleset was found in index!", expected,
            rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile(slubDD, "none")));
    }

    @Test
    public void shouldFindManyByTitleOrFile() throws DataException {
        assertEquals("Rulesets were not found in index!", 2,
            rulesetService.findByTitleOrFile(slubDD, "ruleset_subhh.xml").size());
    }

    @Test
    public void shouldFindOneByTitleOrFile() throws DataException {
        assertEquals(rulesetNotFound, 1, rulesetService.findByTitleOrFile("default", "ruleset_subhh.xml").size());
    }

    @Test
    public void shouldNotFindByTitleOrFile() throws DataException {
        assertEquals("Some rulesets were found in index!", 0, rulesetService.findByTitleOrFile("none", "none").size());
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws DataException {
        assertEquals("Not all rulesets were found in index!", 3, rulesetService.findAllDocuments().size());
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

        rulesetService.remove(5);
        exception.expect(DAOException.class);
        rulesetService.getById(5);
    }
}

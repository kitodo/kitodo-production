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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    private static final RulesetService rulesetService = ServiceManager.getRulesetService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertRulesets();
        MockDatabase.setUpAwaitility();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllRulesets() {
        await().untilAsserted(
            () -> assertEquals("Rulesets were not counted correctly!", Long.valueOf(3), rulesetService.count()));
    }

    @Test
    public void shouldCountAllRulesetsAccordingToQuery() {
        String query = matchQuery("title", "SLUBDD").operator(Operator.AND).toString();
        await().untilAsserted(
            () -> assertEquals("Rulesets were not counted correctly!", Long.valueOf(1), rulesetService.count(query)));
    }

    @Test
    public void shouldCountAllDatabaseRowsForRulesets() throws Exception {
        Long amount = rulesetService.countDatabaseRows();
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindRuleset() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        boolean condition = ruleset.getTitle().equals("SLUBDD") && ruleset.getFile().equals("ruleset_test.xml");
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
    public void shouldFindById() {
        String expected = "SLUBDD";
        await().untilAsserted(
            () -> assertEquals("Ruleset was not found in index!", expected, rulesetService.findById(1).getTitle()));
    }

    @Test
    public void shouldFindByTitle() {
        await().untilAsserted(() -> assertEquals("Ruleset was not found in index!", 1,
            rulesetService.findByTitle("SLUBDD", true).size()));
    }

    @Test
    public void shouldFindByFile() {
        String expected = "ruleset_test.xml";
        await().untilAsserted(() -> assertEquals("Ruleset was not found in index!", expected, rulesetService
                .findByFile("ruleset_test.xml").getJsonObject("_source").getString(RulesetTypeField.FILE.getKey())));
    }

    @Test
    public void shouldFindManyByClientId() {
        await().untilAsserted(
            () -> assertEquals("Rulesets were not found in index!", 2, rulesetService.findByClientId(1).size()));
    }

    @Test
    public void shouldFindOneByClientId() {
        await().untilAsserted(
            () -> assertEquals("Ruleset was not found in index!", 1, rulesetService.findByClientId(2).size()));
    }

    @Test
    public void shouldNotFindByClientId() {
        await().untilAsserted(
            () -> assertEquals("Ruleset was found in index!", 0, rulesetService.findByClientId(3).size()));
    }

    @Test
    public void shouldFindByTitleAndFile() {
        Integer expected = 2;
        await().untilAsserted(() -> assertEquals("Ruleset was not found in index!", expected,
            rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile("SUBHH", "ruleset_subhh.xml"))));
    }

    @Test
    public void shouldNotFindByTitleAndFile() {
        Integer expected = 0;
        await().untilAsserted(() -> assertEquals("Ruleset was found in index!", expected,
            rulesetService.getIdFromJSONObject(rulesetService.findByTitleAndFile("SLUBDD", "none"))));
    }

    @Test
    public void shouldFindManyByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Rulesets were not found in index!", 2,
            rulesetService.findByTitleOrFile("SLUBDD", "ruleset_subhh.xml").size()));
    }

    @Test
    public void shouldFindOneByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Ruleset was not found in index!", 1,
            rulesetService.findByTitleOrFile("default", "ruleset_subhh.xml").size()));
    }

    @Test
    public void shouldNotFindByTitleOrFile() {
        await().untilAsserted(() -> assertEquals("Some rulesets were found in index!", 0,
            rulesetService.findByTitleOrFile("none", "none").size()));
    }

    @Test
    public void shouldFindAllRulesetsDocuments() {
        await().untilAsserted(
            () -> assertEquals("Not all rulesets were found in index!", 3, rulesetService.findAllDocuments().size()));
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

    @Test
    public void shouldGetPreferences() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        List<DocStructTypeInterface> docStructTypes = rulesetService.getPreferences(ruleset).getAllDocStructTypes();

        int actual = docStructTypes.size();
        assertEquals("Size of docstruct types in ruleset file is incorrect!", 6, actual);

        String firstName = docStructTypes.get(0).getName();
        assertEquals("Name of first docstruct type in ruleset file is incorrect!", "page", firstName);

        String secondName = docStructTypes.get(1).getName();
        assertEquals("Name of second docstruct type in ruleset file is incorrect!", "BoundBook", secondName);

        String thirdName = docStructTypes.get(2).getName();
        assertEquals("Name of third docstruct type in ruleset file is incorrect!", "Chapter", thirdName);
    }
}

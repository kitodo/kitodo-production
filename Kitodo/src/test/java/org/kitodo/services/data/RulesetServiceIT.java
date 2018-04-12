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

package org.kitodo.services.data;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.json.JsonObject;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    private static final RulesetService rulesetService = new ServiceManager().getRulesetService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRulesets();

        FileLoader.createRulesetFile();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        FileLoader.deleteRulesetFile();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllRulesets() throws Exception {
        Long amount = rulesetService.count();
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllRulesetsAccordingToQuery() throws Exception {
        String query = matchQuery("title", "SLUBDD").operator(Operator.AND).toString();
        Long amount = rulesetService.count(query);
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForRulesets() throws Exception {
        Long amount = rulesetService.countDatabaseRows();
        assertEquals("Rulesets were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindRuleset() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        boolean condition = ruleset.getTitle().equals("SLUBDD") && ruleset.getFile().equals("ruleset_test.xml");
        assertTrue("Ruleset was not found in database!", condition);
    }

    @Test
    public void shouldFindAllRulesets() {
        List<Ruleset> rulesets = rulesetService.getAll();
        assertEquals("Not all rulesets were found in database!", 2, rulesets.size());
    }

    @Test
    public void shouldGetAllRulesetsInGivenRange() throws Exception {
        List<Ruleset> rulesets = rulesetService.getAll(1, 10);
        assertEquals("Not all rulesets were found in database!", 1, rulesets.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = rulesetService.findById(1).getTitle();
        String expected = "SLUBDD";
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JsonObject> rulesets = rulesetService.findByTitle("SLUBDD", true);
        Integer actual = rulesets.size();
        Integer expected = 1;
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFile() throws Exception {
        JsonObject ruleset = rulesetService.findByFile("ruleset_test.xml");
        JsonObject jsonObject = ruleset.getJsonObject("_source");
        String actual = jsonObject.getString("file");
        String expected = "ruleset_test.xml";
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndFile() throws Exception {
        JsonObject ruleset = rulesetService.findByTitleAndFile("SLUBHH", "ruleset_slubhh.xml");
        Integer actual = rulesetService.getIdFromJSONObject(ruleset);
        Integer expected = 2;
        assertEquals("Ruleset was not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleAndFile("SLUBDD", "none");
        actual = rulesetService.getIdFromJSONObject(ruleset);
        expected = 0;
        assertEquals("Ruleset was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleOrFile() throws Exception {
        List<JsonObject> ruleset = rulesetService.findByTitleOrFile("SLUBDD", "ruleset_slubhh.xml");
        Integer actual = ruleset.size();
        Integer expected = 2;
        assertEquals("Rulesets were not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleOrFile("default", "ruleset_slubhh.xml");
        actual = ruleset.size();
        expected = 1;
        assertEquals("Ruleset was not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleOrFile("none", "none");
        actual = ruleset.size();
        expected = 0;
        assertEquals("Some rulesets were found in index!", expected, actual);
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws Exception {
        List<JsonObject> rulesets = rulesetService.findAllDocuments();
        assertEquals("Not all rulesets were found in index!", 2, rulesets.size());
    }

    @Test
    public void shouldRemoveRuleset() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("To Remove");
        rulesetService.save(ruleset);
        Ruleset foundRuleset = rulesetService.getById(3);
        assertEquals("Additional ruleset was not inserted in database!", "To Remove", foundRuleset.getTitle());

        rulesetService.remove(ruleset);
        exception.expect(DAOException.class);
        rulesetService.getById(3);

        ruleset = new Ruleset();
        ruleset.setTitle("To remove");
        rulesetService.save(ruleset);
        foundRuleset = rulesetService.getById(4);
        assertEquals("Additional ruleset was not inserted in database!", "To remove", foundRuleset.getTitle());

        rulesetService.remove(4);
        exception.expect(DAOException.class);
        rulesetService.getById(4);
    }

    @Test
    public void shouldGetPreferences() throws Exception {
        Ruleset ruleset = rulesetService.getById(1);
        List<DocStructTypeInterface> docStructTypes = rulesetService.getPreferences(ruleset).getAllDocStructTypes();

        int actual = docStructTypes.size();
        assertEquals("Size of docstruct types in ruleset file is incorrect!", 3, actual);

        String firstName = docStructTypes.get(0).getName();
        assertEquals("Name of first docstruct type in ruleset file is incorrect!", "Acknowledgment", firstName);

        String secondName = docStructTypes.get(1).getName();
        assertEquals("Name of first docstruct type in ruleset file is incorrect!", "Article", secondName);

        String thirdName = docStructTypes.get(2).getName();
        assertEquals("Name of first docstruct type in ruleset file is incorrect!", "Monograph", thirdName);
    }
}

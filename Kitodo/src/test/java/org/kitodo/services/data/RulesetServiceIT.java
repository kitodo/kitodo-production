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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for RulesetService class.
 */
public class RulesetServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, CustomResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(2000);
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
    public void shouldFindById() throws Exception {
        RulesetService rulesetService = new RulesetService();

        SearchResult ruleset = rulesetService.findById(1);
        String actual = ruleset.getProperties().get("title");
        String expected = "SLUBDD";
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        RulesetService rulesetService = new RulesetService();

        List<SearchResult> rulesets = rulesetService.findByTitle("SLUBDD", true);
        Integer actual = rulesets.size();
        Integer expected = 1;
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByFile() throws Exception {
        RulesetService rulesetService = new RulesetService();

        SearchResult ruleset = rulesetService.findByFile("ruleset_slubdd.xml");
        String actual = ruleset.getProperties().get("file");
        String expected = "ruleset_slubdd.xml";
        assertEquals("Ruleset was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndFile() throws Exception {
        RulesetService rulesetService = new RulesetService();

        SearchResult ruleset = rulesetService.findByTitleAndFile("SLUBHH","ruleset_slubhh.xml");
        Integer actual = ruleset.getId();
        Integer expected = 2;
        assertEquals("Ruleset was not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleAndFile("SLUBDD","none");
        actual = ruleset.getId();
        expected = null;
        assertEquals("Ruleset was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleOrFile() throws Exception {
        RulesetService rulesetService = new RulesetService();

        List<SearchResult> ruleset = rulesetService.findByTitleOrFile("SLUBDD","ruleset_slubhh.xml");
        Integer actual = ruleset.size();
        Integer expected = 2;
        assertEquals("Rulesets were not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleOrFile("default","ruleset_slubhh.xml");
        actual = ruleset.size();
        expected = 1;
        assertEquals("Ruleset was not found in index!", expected, actual);

        ruleset = rulesetService.findByTitleOrFile("none","none");
        actual = ruleset.size();
        expected = 0;
        assertEquals("Some rulesets were found in index!", expected, actual);
    }

    @Test
    public void shouldFindAllRulesetsDocuments() throws Exception {
        RulesetService rulesetService = new RulesetService();

        List<SearchResult> rulesets = rulesetService.findAllDocuments();
        assertEquals("Not all rulesets were found in index!", 2, rulesets.size());
    }

    @Test
    public void shouldRemoveRuleset() throws Exception {
        RulesetService rulesetService = new RulesetService();

        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("To Remove");
        rulesetService.save(ruleset);
        Ruleset foundRuleset = rulesetService.convertSearchResultToObject(rulesetService.findById(3));
        assertEquals("Additional ruleset was not inserted in database!", "To Remove", foundRuleset.getTitle());

        rulesetService.remove(ruleset);
        foundRuleset = rulesetService.convertSearchResultToObject(rulesetService.findById(3));
        assertEquals("Additional ruleset was not removed from database!", null, foundRuleset);

        ruleset = new Ruleset();
        ruleset.setTitle("To remove");
        rulesetService.save(ruleset);
        foundRuleset = rulesetService.convertSearchResultToObject(rulesetService.findById(4));
        assertEquals("Additional ruleset was not inserted in database!", "To remove", foundRuleset.getTitle());

        rulesetService.remove(4);
        foundRuleset = rulesetService.convertSearchResultToObject(rulesetService.findById(4));
        assertEquals("Additional ruleset was not removed from database!", null, foundRuleset);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSearchResultsToObjectList() throws Exception {
        RulesetService rulesetService = new RulesetService();

        List<SearchResult> searchResults = rulesetService.findAllDocuments();
        List<Ruleset> rulesets = (List<Ruleset>) rulesetService.convertSearchResultsToObjectList(searchResults, "Ruleset");
        assertEquals("Not all rulesets were converted!", 2, rulesets.size());
    }

    @Test
    public void shouldGetPreferences() throws Exception {
        RulesetService rulesetService = new RulesetService();

        Ruleset ruleset = rulesetService.find(1);
        String actual = rulesetService.getPreferences(ruleset).getVersion();
        // not sure how to really check if Pref is correct
        System.out.println("Preferences: " + actual);
        assertEquals("Preference is incorrect!", "1.1-20091117", actual);
    }
}

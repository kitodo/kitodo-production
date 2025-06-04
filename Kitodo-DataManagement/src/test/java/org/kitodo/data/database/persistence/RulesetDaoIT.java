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

package org.kitodo.data.database.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockIndex;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;

public class RulesetDaoIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockIndex.startNode();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockIndex.stopNode();
    }

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Ruleset> rulesets = getAuthorities();

        RulesetDAO rulesetDAO = new RulesetDAO();
        rulesetDAO.save(rulesets.get(0));
        rulesetDAO.save(rulesets.get(1));
        rulesetDAO.save(rulesets.get(2));

        assertEquals(3, rulesetDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, rulesetDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_ruleset", rulesetDAO.getById(1).getTitle(), "Object was not saved or not found!");

        rulesetDAO.remove(1);
        rulesetDAO.remove(rulesets.get(1));
        assertEquals(1, rulesetDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> rulesetDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<Ruleset> getAuthorities() {
        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("first_ruleset");

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("second_ruleset");

        Ruleset thirdRuleset = new Ruleset();
        thirdRuleset.setTitle("third_ruleset");

        List<Ruleset> rulesets = new ArrayList<>();
        rulesets.add(firstRuleset);
        rulesets.add(secondRuleset);
        rulesets.add(thirdRuleset);
        return rulesets;
    }
}

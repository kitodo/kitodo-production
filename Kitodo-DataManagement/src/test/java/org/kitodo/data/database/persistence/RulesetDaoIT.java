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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;

public class RulesetDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Ruleset> rulesets = getAuthorities();

        RulesetDAO rulesetDAO = new RulesetDAO();
        rulesetDAO.save(rulesets.get(0));
        rulesetDAO.save(rulesets.get(1));
        rulesetDAO.save(rulesets.get(2));

        assertEquals("Objects were not saved or not found!", 3, rulesetDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, rulesetDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_ruleset", rulesetDAO.getById(1).getTitle());

        rulesetDAO.remove(1);
        rulesetDAO.remove(rulesets.get(1));
        assertEquals("Objects were not removed or not found!", 1, rulesetDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        rulesetDAO.getById(1);
    }

    private List<Ruleset> getAuthorities() {
        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("first_ruleset");
        firstRuleset.setIndexAction(IndexAction.DONE);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("second_ruleset");
        secondRuleset.setIndexAction(IndexAction.INDEX);

        Ruleset thirdRuleset = new Ruleset();
        thirdRuleset.setTitle("third_ruleset");

        List<Ruleset> rulesets = new ArrayList<>();
        rulesets.add(firstRuleset);
        rulesets.add(secondRuleset);
        rulesets.add(thirdRuleset);
        return rulesets;
    }
}

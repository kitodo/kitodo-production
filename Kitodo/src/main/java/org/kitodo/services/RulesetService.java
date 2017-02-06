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

import de.sub.goobi.config.ConfigMain;

import java.util.List;

import org.apache.log4j.Logger;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

public class RulesetService {

    private static final Logger logger = Logger.getLogger(RulesetService.class);

    private RulesetDAO rulesetDao = new RulesetDAO();

    public void save(Ruleset ruleset) throws DAOException {
        rulesetDao.save(ruleset);
    }

    public Ruleset find(Integer id) throws DAOException {
        return rulesetDao.find(id);
    }

    public List<Ruleset> search(String query) throws DAOException {
        return rulesetDao.search(query);
    }

    public void remove(Ruleset ruleset) throws DAOException {
        rulesetDao.remove(ruleset);
    }

    public void remove(Integer id) throws DAOException {
        rulesetDao.remove(id);
    }

    /**
     * Get preferences.
     *
     * @param ruleset object
     * @return preferences
     */
    public Prefs getPreferences(Ruleset ruleset) {
        Prefs myPreferences = new Prefs();
        try {
            myPreferences.loadPrefs(ConfigMain.getParameter("RegelsaetzeVerzeichnis")
                    + ruleset.getFile());
        } catch (PreferencesException e) {
            logger.error(e);
        }
        return myPreferences;
    }
}

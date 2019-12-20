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

import java.util.List;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;

public class RulesetDAO extends BaseDAO<Ruleset> {

    @Override
    public Ruleset getById(Integer id) throws DAOException {
        Ruleset ruleset = retrieveObject(Ruleset.class, id);
        if (ruleset == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return ruleset;
    }

    @Override
    public List<Ruleset> getAll() throws DAOException {
        return retrieveAllObjects(Ruleset.class);
    }

    @Override
    public List<Ruleset> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Ruleset ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Ruleset> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Ruleset WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC",
            offset, size);
    }

    @Override
    public void remove(Integer rulesetId) throws DAOException {
        removeObject(Ruleset.class, rulesetId);
    }
}

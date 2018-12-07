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

    private static final long serialVersionUID = 1913256950316879121L;

    @Override
    public Ruleset save(Ruleset ruleset) throws DAOException {
        storeObject(ruleset);
        return retrieveObject(Ruleset.class, ruleset.getId());
    }

    @Override
    public Ruleset getById(Integer id) throws DAOException {
        Ruleset result = retrieveObject(Ruleset.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
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
    public void remove(Integer id) throws DAOException {
        removeObject(Ruleset.class, id);
    }
}

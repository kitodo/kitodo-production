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

    public Ruleset save(Ruleset ruleset) throws DAOException {
        storeObject(ruleset);
        return retrieveObject(Ruleset.class, ruleset.getId());
    }

    /**
     * Find ruleset object by id.
     *
     * @param id
     *            of ruleset
     * @return ruleset object
     * @throws DAOException
     *             hibernate
     */
    public Ruleset find(Integer id) throws DAOException {
        Ruleset result = retrieveObject(Ruleset.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all docket from the database.
     *
     * @return all persisted rulesets
     */
    public List<Ruleset> findAll() {
        return retrieveAllObjects(Ruleset.class);
    }

    /**
     * Retrieves all rulesets in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<Ruleset> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM Ruleset ORDER BY id ASC", first, max);
    }

    /**
     * Remove ruleset object.
     *
     * @param ruleset
     *            object
     * @throws DAOException
     *             hibernate
     */
    public void remove(Ruleset ruleset) throws DAOException {
        if (ruleset.getId() != null) {
            removeObject(ruleset);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(Ruleset.class, id);
    }

    public List<Ruleset> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}

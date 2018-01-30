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

import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;

public class UserGroupClientAuthorityRelationDAO extends BaseDAO<UserGroupClientAuthorityRelation> {

    private static final long serialVersionUID = 4647176626562271217L;

    @Override
    public UserGroupClientAuthorityRelation getById(Integer id) throws DAOException {
        UserGroupClientAuthorityRelation result = retrieveObject(UserGroupClientAuthorityRelation.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<UserGroupClientAuthorityRelation> getAll() {
        return retrieveAllObjects(UserGroupClientAuthorityRelation.class);
    }

    @Override
    public List<UserGroupClientAuthorityRelation> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM UserGroupClientAuthorityRelation ORDER BY id ASC", offset, size);
    }

    @Override
    public UserGroupClientAuthorityRelation save(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation)
            throws DAOException {
        storeObject(userGroupClientAuthorityRelation);
        return retrieveObject(UserGroupClientAuthorityRelation.class, userGroupClientAuthorityRelation.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(UserGroupClientAuthorityRelation.class, id);
    }

    /**
     * Refresh UserGroupClientAuthorityRelation object after some changes.
     *
     * @param userGroupClientAuthorityRelation
     *            object
     */
    public void refresh(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation) {
        refreshObject(userGroupClientAuthorityRelation);
    }
}

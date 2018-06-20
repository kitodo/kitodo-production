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

import org.kitodo.data.database.beans.UserGroupProjectAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;

public class UserGroupProjectAuthorityRelationDAO extends BaseDAO<UserGroupProjectAuthorityRelation> {

    private static final long serialVersionUID = 4647176626562271217L;

    @Override
    public UserGroupProjectAuthorityRelation getById(Integer id) throws DAOException {
        UserGroupProjectAuthorityRelation result = retrieveObject(UserGroupProjectAuthorityRelation.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<UserGroupProjectAuthorityRelation> getAll() throws DAOException {
        return retrieveAllObjects(UserGroupProjectAuthorityRelation.class);
    }

    @Override
    public List<UserGroupProjectAuthorityRelation> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM UserGroupProjectAuthorityRelation ORDER BY id ASC", offset, size);
    }

    @Override
    public UserGroupProjectAuthorityRelation save(UserGroupProjectAuthorityRelation userGroupProjectAuthorityRelation)
            throws DAOException {
        storeObject(userGroupProjectAuthorityRelation);
        return retrieveObject(UserGroupProjectAuthorityRelation.class, userGroupProjectAuthorityRelation.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(UserGroupProjectAuthorityRelation.class, id);
    }

    /**
     * Refresh UserGroupProjectAuthorityRelation object after some changes.
     *
     * @param userGroupProjectAuthorityRelation
     *            object
     */
    public void refresh(UserGroupProjectAuthorityRelation userGroupProjectAuthorityRelation) {
        refreshObject(userGroupProjectAuthorityRelation);
    }

    /**
     * Get all UserGroupProjectAuthorityRelations which contains given project and
     * user group id.
     *
     * @param userGroupId
     *            The user group id.
     * @param projectId
     *            The project id.
     * @return The list of UserGroupProjectAuthorityRelations.
     */
    public List<UserGroupProjectAuthorityRelation> getAuthoritiesByUserGroupAndProjectId(int userGroupId,
            int projectId) {

        return getByQuery(
            "FROM UserGroupProjectAuthorityRelation WHERE userGroup = " + userGroupId + " AND project = " + projectId);
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupClientAuthorityRelationDAO;
import org.kitodo.services.data.base.SearchDatabaseService;

public class UserGroupClientAuthorityRelationService
        extends SearchDatabaseService<UserGroupClientAuthorityRelation, UserGroupClientAuthorityRelationDAO> {

    private static UserGroupClientAuthorityRelationService instance = null;

    private UserGroupClientAuthorityRelationService() {
        super(new UserGroupClientAuthorityRelationDAO());
    }

    /**
     * Return singleton variable of type UserGroupClientAuthorityRelationService.
     *
     * @return unique instance of UserGroupClientAuthorityRelationService
     */
    public static UserGroupClientAuthorityRelationService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (UserGroupClientAuthorityRelationService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new UserGroupClientAuthorityRelationService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM UserGroupClientAuthorityRelation");
    }

    /**
     * Get all authorities with are related to given client and user group id.
     *
     * @param userGroupId
     *            The user group id.
     * @param clientId
     *            The client id.
     * @return The list of authorities.
     */
    public List<Authority> getAuthoritiesByUserGroupAndClient(int userGroupId, int clientId) {
        List<UserGroupClientAuthorityRelation> relations = dao.getAuthoritiesByUserGroupAndClientId(userGroupId,
            clientId);
        List<Authority> authorities = new ArrayList<>();
        for (UserGroupClientAuthorityRelation relation : relations) {
            authorities.add(relation.getAuthority());
        }
        return authorities;
    }

    /**
     * Saves UserGroupProjectAuthorityRelation to database.
     *
     * @param userGroupClientAuthorityRelation
     *            The UserGroupProjectAuthorityRelation.
     */
    public void save(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation) throws DAOException {
        dao.save(userGroupClientAuthorityRelation);
    }

    /**
     * Removes UserGroupProjectAuthorityRelation from database.
     *
     * @param userGroupClientAuthorityRelation
     *            The UserGroupProjectAuthorityRelation.
     */
    public void remove(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation) throws DAOException {
        dao.remove(userGroupClientAuthorityRelation);
    }

    /**
     * Removes UserGroupProjectAuthorityRelation from database by id.
     *
     * @param id
     *            The UserGroupProjectAuthorityRelation id.
     */
    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }
}

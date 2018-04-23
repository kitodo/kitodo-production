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
import org.kitodo.data.database.beans.UserGroupProjectAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupProjectAuthorityRelationDAO;
import org.kitodo.services.data.base.SearchDatabaseService;

public class UserGroupProjectAuthorityRelationService
        extends SearchDatabaseService<UserGroupProjectAuthorityRelation, UserGroupProjectAuthorityRelationDAO> {

    private static UserGroupProjectAuthorityRelationService instance = null;

    private UserGroupProjectAuthorityRelationService() {
        super(new UserGroupProjectAuthorityRelationDAO());
    }

    /**
     * Return singleton variable of type UserGroupClientAuthorityRelationService.
     *
     * @return unique instance of UserGroupClientAuthorityRelationService
     */
    public static UserGroupProjectAuthorityRelationService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (UserGroupProjectAuthorityRelationService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new UserGroupProjectAuthorityRelationService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM UserGroupProjectAuthorityRelation");
    }

    /**
     * Get all authorities with are related to given project and user group id.
     *
     * @param userGroupId
     *            The user group id.
     * @param projectId
     *            The project id.
     * @return The list of authorities.
     */
    public List<Authority> getAuthoritiesByUserGroupAndProjectId(int userGroupId, int projectId) {
        List<UserGroupProjectAuthorityRelation> relations = dao.getAuthoritiesByUserGroupAndProjectId(userGroupId,
            projectId);
        List<Authority> authorities = new ArrayList<>();
        for (UserGroupProjectAuthorityRelation relation : relations) {
            authorities.add(relation.getAuthority());
        }
        return authorities;
    }
}

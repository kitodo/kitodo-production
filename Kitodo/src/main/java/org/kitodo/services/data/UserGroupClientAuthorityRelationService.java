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

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupClientAuthorityRelationDAO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchDatabaseService;

public class UserGroupClientAuthorityRelationService
        extends SearchDatabaseService<UserGroupClientAuthorityRelation, UserGroupClientAuthorityRelationDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private final Logger logger = LogManager.getLogger(this.getClass());
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
     * Saves ldap server to database.
     *
     * @param userGroupClientAuthorityRelation
     *            The ldap server.
     */
    public void save(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation) throws DAOException {
        dao.save(userGroupClientAuthorityRelation);
    }

    /**
     * Removes ldap server from database.
     *
     * @param userGroupClientAuthorityRelation
     *            The ldap server.
     */
    public void remove(UserGroupClientAuthorityRelation userGroupClientAuthorityRelation) throws DAOException {
        dao.remove(userGroupClientAuthorityRelation);
    }

    /**
     * Removes ldap server from database by id.
     *
     * @param id
     *            The ldap server id.
     */
    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }
}

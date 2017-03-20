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

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.List;

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.UserGroupType;

public class UserGroupService {
    private UserGroupDAO userGroupDao = new UserGroupDAO();
    private UserGroupType userGroupType = new UserGroupType();
    private Indexer<UserGroup, UserGroupType> indexer = new Indexer<>("kitodo", UserGroup.class);

    public UserGroup find(Integer id) throws DAOException {
        return userGroupDao.find(id);
    }

    public List<UserGroup> findAll() throws DAOException {
        return userGroupDao.findAll();
    }

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param userGroup object
     */
    public void save(UserGroup userGroup) throws DAOException, IOException {
        userGroupDao.save(userGroup);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(userGroup, userGroupType);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param userGroup object
     */
    public void remove(UserGroup userGroup) throws DAOException, IOException {
        userGroupDao.remove(userGroup);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(userGroup, userGroupType);
    }

    public List<UserGroup> search(String query) throws DAOException {
        return userGroupDao.search(query);
    }

    public Long count(String query) throws DAOException {
        return userGroupDao.count(query);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), userGroupType);
    }

    /**
     * Get permission as a string.
     *
     * @param userGroup object
     * @return permission as a string
     */
    public String getPermissionAsString(UserGroup userGroup) {
        if (userGroup.getPermission() == null) {
            userGroup.setPermission(4);
        } else if (userGroup.getPermission() == 3) {
            userGroup.setPermission(4);
        }
        return String.valueOf(userGroup.getPermission().intValue());
    }

    public void setPermissionAsString(UserGroup userGroup, String permission) {
        userGroup.setPermission(Integer.parseInt(permission));
    }

    /**
     * Get tasks' list size.
     *
     * @param userGroup object
     * @return size
     */
    public int getTasksSize(UserGroup userGroup) {
        if (userGroup.getTasks() == null) {
            return 0;
        } else {
            return userGroup.getTasks().size();
        }
    }
}

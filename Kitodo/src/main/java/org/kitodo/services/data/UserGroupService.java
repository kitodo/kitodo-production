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

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserGroupType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class UserGroupService extends TitleSearchService<UserGroup> {

    private UserGroupDAO userGroupDAO = new UserGroupDAO();
    private UserGroupType userGroupType = new UserGroupType();
    private Indexer<UserGroup, UserGroupType> indexer = new Indexer<>(UserGroup.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(UserGroupService.class);

    /**
     * Constructor with searcher's assigning.
     */
    public UserGroupService() {
        super(new Searcher(UserGroup.class));
    }

    public UserGroup find(Integer id) throws DAOException {
        return userGroupDAO.find(id);
    }

    public List<UserGroup> findAll() throws DAOException {
        return userGroupDAO.findAll();
    }

    /**
     * Method saves workpiece object to database.
     *
     * @param userGroup
     *            object
     */
    public void saveToDatabase(UserGroup userGroup) throws DAOException {
        userGroupDAO.save(userGroup);
    }

    /**
     * Method saves workpiece document to the index of Elastic Search.
     *
     * @param userGroup
     *            object
     */
    public void saveToIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(userGroup, userGroupType);
    }

    /**
     * Method saves users and tasks related to modified user group.
     *
     * @param userGroup
     *            object
     */
    protected void saveDependenciesToIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        for (User user : userGroup.getUsers()) {
            serviceManager.getUserService().saveToIndex(user);
        }

        for (Task task : userGroup.getTasks()) {
            serviceManager.getTaskService().saveToIndex(task);
        }
    }

    /**
     * Method removes user group object from database.
     *
     * @param userGroup
     *            object
     */
    public void removeFromDatabase(UserGroup userGroup) throws DAOException {
        userGroupDAO.remove(userGroup);
    }

    /**
     * Method removes user group object from database.
     *
     * @param id
     *            of template object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        userGroupDAO.remove(id);
    }

    /**
     * Method removes user group object from index of Elastic Search.
     *
     * @param userGroup
     *            object
     */
    public void removeFromIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(userGroup, userGroupType);
    }

    public List<UserGroup> search(String query) throws DAOException {
        return userGroupDAO.search(query);
    }

    public Long count(String query) throws DAOException {
        return userGroupDAO.count(query);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), userGroupType);
    }

    /**
     * Get permission as a string.
     *
     * @param userGroup
     *            object
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
     * @param userGroup
     *            object
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

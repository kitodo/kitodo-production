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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.UserGroupDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserGroupType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class UserGroupService extends TitleSearchService<UserGroup, UserGroupDTO, UserGroupDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(UserGroupService.class);
    private static UserGroupService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private UserGroupService() {
        super(new UserGroupDAO(), new UserGroupType(), new Indexer<>(UserGroup.class), new Searcher(UserGroup.class));
        this.indexer = new Indexer<>(UserGroup.class);
    }

    /**
     * Return singleton variable of type UserGroupService.
     *
     * @return unique instance of UserGroupService
     */
    public static UserGroupService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (UserGroupService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new UserGroupService();
                }
            }
        }
        return instance;
    }

    /**
     * Get all user groups from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @return list of UserGroupDTO objects
     */
    @Override
    public List<UserGroupDTO> findAll() throws DataException {
        return findAll(true);
    }

    /**
     * Get all user groups from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of UserGroupDTO objects
     */
    @Override
    public List<UserGroupDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return findAll(sort, offset, size, true);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM UserGroup");
    }

    /**
     * Method saves users and tasks related to modified user group.
     *
     * @param userGroup
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        manageTasksDependenciesForIndex(userGroup);
        manageUsersDependenciesForIndex(userGroup);
    }

    /**
     * Check if IndexAction flag is delete. If true remove user group from list
     * of user groups and re-save task, if false only re-save task object.
     *
     * @param userGroup
     *            object
     */
    private void manageTasksDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        if (userGroup.getIndexAction() == IndexAction.DELETE) {
            for (Task task : userGroup.getTasks()) {
                task.getUserGroups().remove(userGroup);
                serviceManager.getTaskService().saveToIndex(task);
            }
        } else {
            for (Task task : userGroup.getTasks()) {
                serviceManager.getTaskService().saveToIndex(task);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user group from list
     * of user groups and re-save user, if false only re-save user object.
     *
     * @param userGroup
     *            object
     */
    private void manageUsersDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        if (userGroup.getIndexAction() == IndexAction.DELETE) {
            for (User user : userGroup.getUsers()) {
                user.getUserGroups().remove(userGroup);
                serviceManager.getUserService().saveToIndex(user);
            }
        } else {
            for (User user : userGroup.getUsers()) {
                serviceManager.getUserService().saveToIndex(user);
            }
        }
    }

    /**
     * Refresh user's group object after update.
     *
     * @param userGroup
     *            object
     */
    public void refresh(UserGroup userGroup) {
        dao.refresh(userGroup);
    }

    /**
     * Find user groups with exact permissions.
     *
     * @param permission
     *            of the searched user group
     * @return list of JSON objects
     */
    List<JSONObject> findByPermission(Integer permission) throws DataException {
        QueryBuilder query = createSimpleQuery("permission", permission, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user groups by id of user.
     *
     * @param id
     *            of user
     * @return list of JSON objects with users for specific user group id
     */
    List<JSONObject> findByUserId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("users.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user groups by login of user.
     *
     * @param login
     *            of user
     * @return list of search result with user groups for specific user login
     */
    List<JSONObject> findByUserLogin(String login) throws DataException {
        QueryBuilder query = createSimpleQuery("users.login", login, true);
        return searcher.findDocuments(query.toString());
    }

    @Override
    public UserGroupDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        UserGroupDTO userGroupDTO = new UserGroupDTO();
        userGroupDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject userGroupJSONObject = getSource(jsonObject);
        userGroupDTO.setTitle(getStringPropertyForDTO(userGroupJSONObject, "title"));
        userGroupDTO.setUsersSize(getSizeOfRelatedPropertyForDTO(userGroupJSONObject, "users"));
        if (!related) {
            userGroupDTO = convertRelatedJSONObjects(userGroupJSONObject, userGroupDTO);
        } else {
            userGroupDTO = addBasicUserRelation(userGroupDTO, userGroupJSONObject);
        }
        return userGroupDTO;
    }

    private UserGroupDTO convertRelatedJSONObjects(JSONObject jsonObject, UserGroupDTO userGroupDTO) throws DataException {
        userGroupDTO.setUsers(convertRelatedJSONObjectToDTO(jsonObject, "users", serviceManager.getUserService()));
        return userGroupDTO;
    }

    private UserGroupDTO addBasicUserRelation(UserGroupDTO userGroupDTO, JSONObject jsonObject) {
        if (userGroupDTO.getUsersSize() > 0) {
            List<UserDTO> users = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add("name");
            subKeys.add("surname");
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject, "users", subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                UserDTO user = new UserDTO();
                user.setId(relatedProperty.getId());
                if (relatedProperty.getValues().size() > 0) {
                    user.setName(relatedProperty.getValues().get(0));
                    user.setSurname(relatedProperty.getValues().get(1));
                }
                user.setFullName(serviceManager.getUserService().getFullName(user));
                users.add(user);
            }
            userGroupDTO.setUsers(users);
        }
        return userGroupDTO;
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
}

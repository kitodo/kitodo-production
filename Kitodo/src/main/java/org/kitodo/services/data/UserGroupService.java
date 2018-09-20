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
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.UserGroupDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserGroupType;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserGroupTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class UserGroupService extends TitleSearchService<UserGroup, UserGroupDTO, UserGroupDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static UserGroupService instance = null;
    private static String AUTHORITY_TITLE_VIEW_ALL = "viewAllUserGroups";

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
     * @param filters
     *            filter for requestes results
     * @return list of UserGroupDTO objects
     */
    @Override
    public List<UserGroupDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        if (serviceManager.getSecurityAccessService().isAdminOrHasAuthorityGlobal(AUTHORITY_TITLE_VIEW_ALL)) {
            return findAll(sort, offset, size, true);
        }
        if (serviceManager.getSecurityAccessService().hasAuthorityForAnyClient(AUTHORITY_TITLE_VIEW_ALL)) {
            return getAllUserGroupsVisibleForCurrentUser();
        }
        return new ArrayList<>();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM UserGroup");
    }

    /**
     * Method saves users and tasks related to modified user group.
     *
     * @param userGroup
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        manageAuthorizationsDependenciesForIndex(userGroup);
        manageTasksDependenciesForIndex(userGroup);
        manageUsersDependenciesForIndex(userGroup);
    }

    /**
     * Check if IndexAction flag is delete. If true remove user group from list of
     * user groups and re-save authorization, if false only re-save authorization
     * object.
     *
     * @param userGroup
     *            object
     */
    private void manageAuthorizationsDependenciesForIndex(UserGroup userGroup)
            throws CustomResponseException, IOException {
        if (userGroup.getIndexAction() == IndexAction.DELETE) {
            for (Authority authority : userGroup.getAuthorities()) {
                authority.getUserGroups().remove(userGroup);
                serviceManager.getAuthorityService().saveToIndex(authority, false);
            }
        } else {
            for (Authority authority : userGroup.getAuthorities()) {
                serviceManager.getAuthorityService().saveToIndex(authority, false);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user group from list of
     * user groups and re-save task, if false only re-save task object.
     *
     * @param userGroup
     *            object
     */
    private void manageTasksDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        if (userGroup.getIndexAction() == IndexAction.DELETE) {
            for (Task task : userGroup.getTasks()) {
                task.getUserGroups().remove(userGroup);
                serviceManager.getTaskService().saveToIndex(task, false);
            }
        } else {
            for (Task task : userGroup.getTasks()) {
                serviceManager.getTaskService().saveToIndex(task, false);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user group from list of
     * user groups and re-save user, if false only re-save user object.
     *
     * @param userGroup
     *            object
     */
    private void manageUsersDependenciesForIndex(UserGroup userGroup) throws CustomResponseException, IOException {
        if (userGroup.getIndexAction() == IndexAction.DELETE) {
            for (User user : userGroup.getUsers()) {
                user.getUserGroups().remove(userGroup);
                serviceManager.getUserService().saveToIndex(user, false);
            }
        } else {
            for (User user : userGroup.getUsers()) {
                serviceManager.getUserService().saveToIndex(user, false);
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
     * Find user groups with authorization title.
     *
     * @param authorizationTitle
     *            of the searched user group
     * @return list of JSON objects
     */
    List<JsonObject> findByAuthorizationTitle(String authorizationTitle) throws DataException {
        QueryBuilder query = createSimpleQuery("authorities.title", authorizationTitle, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user groups with authorization id.
     *
     * @param authorizationId
     *            of the searched user group
     * @return list of JSON objects
     */
    List<JsonObject> findByAuthorizationId(Integer authorizationId) throws DataException {
        QueryBuilder query = createSimpleQuery("authorities.id", authorizationId, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user groups by id of user.
     *
     * @param id
     *            of user
     * @return list of JSON objects with user groups for specific user id.
     */
    List<JsonObject> findByUserId(Integer id) throws DataException {
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
    List<JsonObject> findByUserLogin(String login) throws DataException {
        QueryBuilder query = createSimpleQuery("users.login", login, true);
        return searcher.findDocuments(query.toString());
    }

    @Override
    public UserGroupDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        UserGroupDTO userGroupDTO = new UserGroupDTO();
        userGroupDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject userGroupJSONObject = jsonObject.getJsonObject("_source");
        userGroupDTO.setTitle(UserGroupTypeField.TITLE.getStringValue(userGroupJSONObject));
        userGroupDTO.setUsersSize(UserGroupTypeField.USERS.getSizeOfProperty(userGroupJSONObject));
        userGroupDTO.setAuthorizationsSize(UserGroupTypeField.AUTHORITIES.getSizeOfProperty(userGroupJSONObject));
        if (!related) {
            convertRelatedJSONObjects(userGroupJSONObject, userGroupDTO);
        } else {
            addBasicAuthorizationsRelation(userGroupDTO, userGroupJSONObject);
            addBasicUsersRelation(userGroupDTO, userGroupJSONObject);
        }
        return userGroupDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, UserGroupDTO userGroupDTO) throws DataException {
        userGroupDTO.setUsers(convertRelatedJSONObjectToDTO(jsonObject, UserGroupTypeField.USERS.getKey(),
            serviceManager.getUserService()));
    }

    private void addBasicAuthorizationsRelation(UserGroupDTO userGroupDTO, JsonObject jsonObject) {
        if (userGroupDTO.getAuthorizationsSize() > 0) {
            List<AuthorityDTO> authorizations = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(AuthorityTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                UserGroupTypeField.AUTHORITIES.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                AuthorityDTO authorization = new AuthorityDTO();
                authorization.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    authorization.setTitle(relatedProperty.getValues().get(0));
                }
                authorizations.add(authorization);
            }
            userGroupDTO.setAuthorities(authorizations);
        }
    }

    private void addBasicUsersRelation(UserGroupDTO userGroupDTO, JsonObject jsonObject) {
        if (userGroupDTO.getUsersSize() > 0) {
            List<UserDTO> users = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(UserTypeField.NAME.getKey());
            subKeys.add(UserTypeField.SURNAME.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                UserGroupTypeField.USERS.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                UserDTO user = new UserDTO();
                user.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    user.setName(relatedProperty.getValues().get(0));
                    user.setSurname(relatedProperty.getValues().get(1));
                }
                user.setFullName(serviceManager.getUserService().getFullName(user));
                users.add(user);
            }
            userGroupDTO.setUsers(users);
        }
    }

    /**
     * Get authorizations for given user group.
     *
     * @param userGroup
     *            object
     * @return authorizations as list of Strings
     */
    public List<String> getAuthorizationsAsString(UserGroup userGroup) {
        List<Authority> authorities = userGroup.getAuthorities();
        List<String> stringAuthorizations = new ArrayList<>();
        for (Authority authority : authorities) {
            stringAuthorizations.add(authority.getTitle());
        }
        return stringAuthorizations;
    }

    /**
     * Get all active user groups visible for current user - user assigned to
     * projects with certain clients.
     *
     * @return list of user groups
     */
    public List<UserGroupDTO> getAllUserGroupsVisibleForCurrentUser() throws DataException {
        List<Integer> clientIdList = serviceManager.getSecurityAccessService()
                .getClientIdListForAuthority(AUTHORITY_TITLE_VIEW_ALL);
        return convertListIdToDTO(getAllUserGroupIdsByClientIds(clientIdList), this);
    }

    public List<UserGroup> getAllUserGroupsByClientIds(List<Integer> clientIdList) {
        return dao.getAllUserGroupsByClientIds(clientIdList);
    }

    /**
     * Get ids of all user groups which hold users which are assigned to projects of
     * the given clients.
     * 
     * @param clientIdList
     *            The list of client ids.
     * @return The list of user ids.
     */
    public List<Integer> getAllUserGroupIdsByClientIds(List<Integer> clientIdList) {
        List<UserGroup> users = getAllUserGroupsByClientIds(clientIdList);
        List<Integer> userIdList = new ArrayList<>();
        for (UserGroup userGroup : users) {
            userIdList.add(userGroup.getId());
        }
        return userIdList;
    }
}

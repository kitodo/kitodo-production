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
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.RoleDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.RoleType;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.dto.ClientDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.RoleDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class RoleService extends TitleSearchService<Role, RoleDTO, RoleDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static RoleService instance = null;
    private static String AUTHORITY_TITLE_VIEW_ALL = "viewAllUserGroups";

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private RoleService() {
        super(new RoleDAO(), new RoleType(), new Indexer<>(Role.class), new Searcher(Role.class));
        this.indexer = new Indexer<>(Role.class);
    }

    /**
     * Return singleton variable of type UserGroupService.
     *
     * @return unique instance of UserGroupService
     */
    public static RoleService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (RoleService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new RoleService();
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
    public List<RoleDTO> findAll() throws DataException {
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
    public List<RoleDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
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
    public List<RoleDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
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

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM UserGroup WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Role> getAllNotIndexed() {
        return getByQuery("FROM UserGroup WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    /**
     * Method saves users and tasks related to modified user group.
     *
     * @param userGroup
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Role userGroup) throws CustomResponseException, IOException {
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
    private void manageAuthorizationsDependenciesForIndex(Role userGroup)
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
    private void manageTasksDependenciesForIndex(Role userGroup) throws CustomResponseException, IOException {
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
    private void manageUsersDependenciesForIndex(Role userGroup) throws CustomResponseException, IOException {
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
    public void refresh(Role userGroup) {
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
    public RoleDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        RoleDTO userGroupDTO = new RoleDTO();
        userGroupDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject userGroupJSONObject = jsonObject.getJsonObject("_source");
        userGroupDTO.setTitle(RoleTypeField.TITLE.getStringValue(userGroupJSONObject));
        userGroupDTO.setUsersSize(RoleTypeField.USERS.getSizeOfProperty(userGroupJSONObject));
        userGroupDTO.setAuthorizationsSize(RoleTypeField.AUTHORITIES.getSizeOfProperty(userGroupJSONObject));
        if (!related) {
            convertRelatedJSONObjects(userGroupJSONObject, userGroupDTO);
        } else {
            addBasicAuthorizationsRelation(userGroupDTO, userGroupJSONObject);
            addBasicUsersRelation(userGroupDTO, userGroupJSONObject);
        }

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(RoleTypeField.CLIENT_ID.getIntValue(userGroupJSONObject));
        clientDTO.setName(RoleTypeField.CLIENT_NAME.getStringValue(userGroupJSONObject));

        userGroupDTO.setClient(clientDTO);
        return userGroupDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, RoleDTO userGroupDTO) throws DataException {
        userGroupDTO.setUsers(convertRelatedJSONObjectToDTO(jsonObject, RoleTypeField.USERS.getKey(),
            serviceManager.getUserService()));
    }

    private void addBasicAuthorizationsRelation(RoleDTO userGroupDTO, JsonObject jsonObject) {
        if (userGroupDTO.getAuthorizationsSize() > 0) {
            List<AuthorityDTO> authorizations = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(AuthorityTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                RoleTypeField.AUTHORITIES.getKey(), subKeys);
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

    private void addBasicUsersRelation(RoleDTO userGroupDTO, JsonObject jsonObject) {
        if (userGroupDTO.getUsersSize() > 0) {
            List<UserDTO> users = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(UserTypeField.NAME.getKey());
            subKeys.add(UserTypeField.SURNAME.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                RoleTypeField.USERS.getKey(), subKeys);
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
    public List<String> getAuthorizationsAsString(Role userGroup) {
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
    public List<RoleDTO> getAllUserGroupsVisibleForCurrentUser() throws DataException {
        List<Integer> clientIdList = serviceManager.getSecurityAccessService()
                .getClientIdListForAuthority(AUTHORITY_TITLE_VIEW_ALL);
        return convertListIdToDTO(getAllUserGroupIdsByClientIds(clientIdList), this);
    }
    
    /**
     * Get all user roles for a list of clients.
     *
     * @param clientIds
     *              The list of client IDs
     *
     * @return The list of all user roles for the given client IDs
     */
    public List<Role> getAllUserGroupsByClientIds(List<Integer> clientIds) {
        return dao.getAllUserGroupsByClientIds(clientIds);
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
        List<Role> users = getAllUserGroupsByClientIds(clientIdList);
        List<Integer> userIdList = new ArrayList<>();
        for (Role userGroup : users) {
            userIdList.add(userGroup.getId());
        }
        return userIdList;
    }
}

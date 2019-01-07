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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
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
import org.kitodo.dto.RoleDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class RoleService extends TitleSearchService<Role, RoleDTO, RoleDAO> {

    private static RoleService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private RoleService() {
        super(new RoleDAO(), new RoleType(), new Indexer<>(Role.class), new Searcher(Role.class));
        this.indexer = new Indexer<>(Role.class);
    }

    /**
     * Return singleton variable of type RoleService.
     *
     * @return unique instance of RoleService
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
     * Find all roles from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @return list of RoleDTO objects
     */
    @Override
    public List<RoleDTO> findAll() throws DataException {
        return findAll(true);
    }

    /**
     * Get all roles from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of RoleDTO objects
     */
    @Override
    public List<RoleDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return findAll(sort, offset, size, true);
    }

    /**
     * Get all roles from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @param filters
     *            filter for requested results
     * @return list of RoleDTO objects
     */
    @Override
    public List<RoleDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return findAll(sort, offset, size, false);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return convertJSONObjectsToDTOs(
                searcher.findDocuments(createQueryRolesForCurrentUser(filters).toString(), sort, offset, size), false);
        }
        return new ArrayList<>();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Role");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Role WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public String createCountQuery(Map filters) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            return null;
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewRoleList()) {
            return createQueryRolesForCurrentUser(filters).toString();
        }
        return null;
    }

    @Override
    public List<Role> getAllNotIndexed() {
        return getByQuery("FROM Role WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Role> getAllForSelectedClient() {
        return dao.getByQuery("SELECT r FROM Role AS r INNER JOIN r.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    /**
     * Method saves users and tasks related to modified role.
     *
     * @param role
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Role role) throws CustomResponseException, IOException {
        manageAuthorizationsDependenciesForIndex(role);
        manageTasksDependenciesForIndex(role);
        manageUsersDependenciesForIndex(role);
    }

    /**
     * Check if IndexAction flag is delete. If true remove role from list of
     * roles and re-save authorization, if false only re-save authorization
     * object.
     *
     * @param role
     *            object
     */
    private void manageAuthorizationsDependenciesForIndex(Role role) throws CustomResponseException, IOException {
        if (role.getIndexAction() == IndexAction.DELETE) {
            for (Authority authority : role.getAuthorities()) {
                authority.getRoles().remove(role);
                ServiceManager.getAuthorityService().saveToIndex(authority, false);
            }
        } else {
            for (Authority authority : role.getAuthorities()) {
                ServiceManager.getAuthorityService().saveToIndex(authority, false);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove role from list of
     * roles and re-save task, if false only re-save task object.
     *
     * @param role
     *            object
     */
    private void manageTasksDependenciesForIndex(Role role) throws CustomResponseException, IOException {
        if (role.getIndexAction() == IndexAction.DELETE) {
            for (Task task : role.getTasks()) {
                task.getRoles().remove(role);
                ServiceManager.getTaskService().saveToIndex(task, false);
            }
        } else {
            for (Task task : role.getTasks()) {
                ServiceManager.getTaskService().saveToIndex(task, false);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove role from list of
     * roles and re-save user, if false only re-save user object.
     *
     * @param role
     *            object
     */
    private void manageUsersDependenciesForIndex(Role role) throws CustomResponseException, IOException {
        if (role.getIndexAction() == IndexAction.DELETE) {
            for (User user : role.getUsers()) {
                user.getRoles().remove(role);
                ServiceManager.getUserService().saveToIndex(user, false);
            }
        } else {
            for (User user : role.getUsers()) {
                ServiceManager.getUserService().saveToIndex(user, false);
            }
        }
    }

    /**
     * Find all roles available to assign to the edited user. It will be displayed
     * in the addRolesPopup.
     *
     * @param userId
     *            id of user which is going to be edited
     * @param clients
     *            list of clients to which edited user is assigned
     * @return list of all matching roles
     */
    public List<RoleDTO> findAllAvailableForAssignToUser(Integer userId, List<Client> clients) throws DataException {
        return findAvailableForAssignToUser(userId, clients);
    }

    private List<RoleDTO> findAvailableForAssignToUser(Integer userId, List<Client> clients) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        if (Objects.nonNull(userId)) {
            query.must(createSimpleQuery(RoleTypeField.USERS + ".id", userId, false));
            //TODO: for new user this list is empty - only for edited condition to apply?
            if (!clients.isEmpty()) {
                query.must(createSetQueryForBeans(RoleTypeField.CLIENT_ID.getKey(), new ArrayList<>(clients), true));
            }
            return convertJSONObjectsToDTOs(searcher.findDocuments(query.toString()), true);
        }
        return findAll();
    }

    /**
     * Refresh user's role object after update.
     *
     * @param role
     *            object
     */
    public void refresh(Role role) {
        dao.refresh(role);
    }

    /**
     * Find roles with authorization title.
     *
     * @param authorizationTitle
     *            of the searched role
     * @return list of JSON objects
     */
    List<JsonObject> findByAuthorizationTitle(String authorizationTitle) throws DataException {
        QueryBuilder query = createSimpleQuery(RoleTypeField.AUTHORITIES + ".title", authorizationTitle, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find roles by id of user.
     *
     * @param id
     *            of user
     * @return list of JSON objects with roles for specific user id.
     */
    List<JsonObject> findByUserId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery(RoleTypeField.USERS + ".id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find roles by login of user.
     *
     * @param login
     *            of user
     * @return list of search result with roles for specific user login
     */
    List<JsonObject> findByUserLogin(String login) throws DataException {
        QueryBuilder query = createSimpleQuery(RoleTypeField.USERS + ".login", login, true);
        return searcher.findDocuments(query.toString());
    }

    @Override
    public RoleDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject roleJsonObject = jsonObject.getJsonObject("_source");
        roleDTO.setTitle(RoleTypeField.TITLE.getStringValue(roleJsonObject));
        roleDTO.setUsersSize(RoleTypeField.USERS.getSizeOfProperty(roleJsonObject));
        roleDTO.setAuthorizationsSize(RoleTypeField.AUTHORITIES.getSizeOfProperty(roleJsonObject));
        if (!related) {
            convertRelatedJSONObjects(roleJsonObject, roleDTO);
        } else {
            addBasicAuthorizationsRelation(roleDTO, roleJsonObject);
            addBasicUsersRelation(roleDTO, roleJsonObject);
        }

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(RoleTypeField.CLIENT_ID.getIntValue(roleJsonObject));
        clientDTO.setName(RoleTypeField.CLIENT_NAME.getStringValue(roleJsonObject));

        roleDTO.setClient(clientDTO);
        return roleDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, RoleDTO roleDTO) throws DataException {
        roleDTO.setUsers(
            convertRelatedJSONObjectToDTO(jsonObject, RoleTypeField.USERS.getKey(), ServiceManager.getUserService()));
    }

    private void addBasicAuthorizationsRelation(RoleDTO roleDTO, JsonObject jsonObject) {
        if (roleDTO.getAuthorizationsSize() > 0) {
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
            roleDTO.setAuthorities(authorizations);
        }
    }

    private void addBasicUsersRelation(RoleDTO roleDTO, JsonObject jsonObject) {
        if (roleDTO.getUsersSize() > 0) {
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
                user.setFullName(ServiceManager.getUserService().getFullName(user));
                users.add(user);
            }
            roleDTO.setUsers(users);
        }
    }

    /**
     * Get authorizations for given role.
     *
     * @param role
     *            object
     * @return authorizations as list of Strings
     */
    public List<String> getAuthorizationsAsString(Role role) {
        List<Authority> authorities = role.getAuthorities();
        List<String> stringAuthorizations = new ArrayList<>();
        for (Authority authority : authorities) {
            stringAuthorizations.add(authority.getTitle());
        }
        return stringAuthorizations;
    }

    // TODO: filtering functionality
    private QueryBuilder createQueryRolesForCurrentUser(Map filters) {
        return createSimpleQuery(RoleTypeField.CLIENT_ID.getKey(), ServiceManager.getUserService().getSessionClientId(),
            true);
    }

    /**
     * Get all user roles for the selected client.
     *
     * @param clientId
     *            the selected client id
     * @return The list of all user roles for the given client IDs
     */
    public List<Role> getAllRolesByClientId(int clientId) {
        return dao.getAllRolesByClientId(clientId);
    }
}

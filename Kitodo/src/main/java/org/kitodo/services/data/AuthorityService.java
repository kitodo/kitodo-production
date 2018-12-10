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

import javax.json.JsonObject;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.AuthorityDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.AuthorityType;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.dto.RoleDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class AuthorityService extends TitleSearchService<Authority, AuthorityDTO, AuthorityDAO> {

    private static AuthorityService instance = null;

    private static final String GLOBAL_AUTHORITY_SUFFIX = "_globalAssignable";
    private static final String CLIENT_AUTHORITY_SUFFIX = "_clientAssignable";

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private AuthorityService() {
        super(new AuthorityDAO(), new AuthorityType(), new Indexer<>(Authority.class), new Searcher(Authority.class));
        this.indexer = new Indexer<>(Authority.class);
    }

    /**
     * Return singleton variable of type AuthorityService.
     *
     * @return unique instance of AuthorityService
     */
    public static AuthorityService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (AuthorityService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new AuthorityService();
                }
            }
        }
        return instance;
    }

    /**
     * Get global authority suffix.
     *
     * @return the global authority suffix
     */
    public String getGlobalAuthoritySuffix() {
        return GLOBAL_AUTHORITY_SUFFIX;
    }

    /**
     * Get client authority suffix.
     *
     * @return the client authority suffix.
     */
    public String getClientAuthoritySuffix() {
        return CLIENT_AUTHORITY_SUFFIX;
    }

    /**
     * Get all authorities from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @return list of AuthorityDTO objects
     */
    @Override
    public List<AuthorityDTO> findAll() throws DataException {
        return findAll(true);
    }

    /**
     * Get all authorities from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of AuthorityDTO objects
     */
    @Override
    public List<AuthorityDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return findAll(sort, offset, size, true);
    }

    /**
     * Refresh user's group object after update.
     *
     * @param authority
     *            object
     */
    @Override
    public void refresh(Authority authority) {
        dao.refresh(authority);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Authority");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Authority WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Authority> getAllNotIndexed() {
        return getByQuery("FROM Authority WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Authority> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method saves roles related to modified authority.
     *
     * @param authority
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Authority authority) throws CustomResponseException, IOException {
        manageRolesDependenciesForIndex(authority);
    }

    /**
     * Check if IndexAction flag is delete. If true remove authority from list of
     * authorities and re-save role, if false only re-save role object.
     *
     * @param authority
     *            object
     */
    private void manageRolesDependenciesForIndex(Authority authority) throws CustomResponseException, IOException {
        if (authority.getIndexAction() == IndexAction.DELETE) {
            for (Role role : authority.getRoles()) {
                role.getAuthorities().remove(authority);
                ServiceManager.getRoleService().saveToIndex(role, false);
            }
        } else {
            for (Role role : authority.getRoles()) {
                ServiceManager.getRoleService().saveToIndex(role, false);
            }
        }
    }

    /**
     * Get authority by title.
     * 
     * @param title
     *            of the searched authority
     * @return matching authority
     */
    public Authority getByTitle(String title) throws DAOException {
        return dao.getByTitle(title);
    }

    /**
     * Gets all authorities which are assignable globally.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableGlobal() throws DAOException {
        return filterAuthorities(getAll(), GLOBAL_AUTHORITY_SUFFIX);
    }

    /**
     * Gets all authorities which are assignable for any client.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableToClients() throws DAOException {
        return filterAuthorities(getAll(), CLIENT_AUTHORITY_SUFFIX);
    }

    /**
     * Filters global assignable authorities out of an given list of authorities.
     *
     * @return The list of authorities.
     */
    public List<Authority> filterAssignableGlobal(List<Authority> authoritiesToFilter) {
        return filterAuthorities(authoritiesToFilter, GLOBAL_AUTHORITY_SUFFIX);
    }

    /**
     * Filters client assignable authorities out of an given list of authorities.
     *
     * @return The list of authorities.
     */
    public List<Authority> filterAssignableToClients(List<Authority> authoritiesToFilter) {
        return filterAuthorities(authoritiesToFilter, CLIENT_AUTHORITY_SUFFIX);
    }

    /**
     * Filters a list of authorities by checking if title contains the given filter.
     * 
     * @param authoritiesToFilter
     *            The list of Authorities to filter.
     * @param filter
     *            The filter as String object.
     * @return The filtered list of authorities.
     */
    private List<Authority> filterAuthorities(List<Authority> authoritiesToFilter, String filter) {
        List<Authority> filteredAuthorities = new ArrayList<>();
        for (Authority authority : authoritiesToFilter) {
            if (authority.getTitle().contains(filter)) {
                filteredAuthorities.add(authority);
            }
        }
        return filteredAuthorities;
    }

    @Override
    public AuthorityDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        AuthorityDTO authorityDTO = new AuthorityDTO();
        authorityDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject authorizationJSONObject = jsonObject.getJsonObject("_source");
        authorityDTO.setTitle(AuthorityTypeField.TITLE.getStringValue(authorizationJSONObject));
        authorityDTO.setRolesSize(AuthorityTypeField.ROLES.getSizeOfProperty(authorizationJSONObject));
        if (!related) {
            convertRelatedJSONObjects(authorizationJSONObject, authorityDTO);
        } else {
            addBasicRoleRelation(authorityDTO, authorizationJSONObject);
        }
        return authorityDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, AuthorityDTO authorityDTO) throws DataException {
        authorityDTO.setRoles(convertRelatedJSONObjectToDTO(jsonObject, AuthorityTypeField.ROLES.getKey(),
            ServiceManager.getRoleService()));
    }

    private void addBasicRoleRelation(AuthorityDTO authorityDTO, JsonObject jsonObject) {
        if (authorityDTO.getRolesSize() > 0) {
            List<RoleDTO> roles = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(RoleTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                AuthorityTypeField.ROLES.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                RoleDTO role = new RoleDTO();
                role.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    role.setTitle(relatedProperty.getValues().get(0));
                }
                roles.add(role);
            }
            authorityDTO.setRoles(roles);
        }
    }
}

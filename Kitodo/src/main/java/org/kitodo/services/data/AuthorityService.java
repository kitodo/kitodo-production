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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.AuthorityDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.AuthorityType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorityDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class AuthorityService extends TitleSearchService<Authority, AuthorityDTO, AuthorityDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(AuthorityService.class);
    private static AuthorityService instance = null;

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
    public void refresh(Authority authority) {
        dao.refresh(authority);
    }

    /**
     * Find authorities by id of user group.
     *
     * @param id
     *            of user group
     * @return list of JSON objects with authorities for specific user group id
     */
    List<JsonObject> findByUserGroupId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("userGroups.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Authority");
    }

    /**
     * Method saves user groups related to modified authority.
     *
     * @param authority
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Authority authority) throws CustomResponseException, IOException {
        manageUserGroupsDependenciesForIndex(authority);
    }

    /**
     * Check if IndexAction flag is delete. If true remove authority from list of
     * authorities and re-save user group, if false only re-save authority object.
     *
     * @param authority
     *            object
     */
    private void manageUserGroupsDependenciesForIndex(Authority authority) throws CustomResponseException, IOException {
        if (authority.getIndexAction() == IndexAction.DELETE) {
            for (UserGroup userGroup : authority.getUserGroups()) {
                userGroup.getGlobalAuthorities().remove(authority);
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        } else {
            for (UserGroup userGroup : authority.getUserGroups()) {
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        }
    }

    /**
     * Gets all authorities which are assignable for any client.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableToClients() {
        return getByQuery("FROM Authority WHERE clientAssignable = 1");
    }

    /**
     * Gets all authorities which are assignable for any project.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableToProjects() {
        return getByQuery("FROM Authority WHERE projectAssignable = 1");
    }

    @Override
    public AuthorityDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        AuthorityDTO authorityDTO = new AuthorityDTO();
        authorityDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject authorizationJSONObject = jsonObject.getJsonObject("_source");
        authorityDTO.setTitle(authorizationJSONObject.getString("title"));
        authorityDTO.setUserGroupsSize(getSizeOfRelatedPropertyForDTO(authorizationJSONObject, "userGroups"));
        if (!related) {
            authorityDTO = convertRelatedJSONObjects(authorizationJSONObject, authorityDTO);
        } else {
            authorityDTO = addBasicUserGroupRelation(authorityDTO, authorizationJSONObject);
        }
        return authorityDTO;
    }

    private AuthorityDTO convertRelatedJSONObjects(JsonObject jsonObject, AuthorityDTO authorityDTO)
            throws DataException {
        authorityDTO.setUserGroups(
            convertRelatedJSONObjectToDTO(jsonObject, "userGroups", serviceManager.getUserGroupService()));
        return authorityDTO;
    }

    private AuthorityDTO addBasicUserGroupRelation(AuthorityDTO authorityDTO, JsonObject jsonObject) {
        if (authorityDTO.getUserGroupsSize() > 0) {
            List<UserGroupDTO> userGroups = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add("title");
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject, "userGroups", subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                UserGroupDTO userGroup = new UserGroupDTO();
                userGroup.setId(relatedProperty.getId());
                if (relatedProperty.getValues().size() > 0) {
                    userGroup.setTitle(relatedProperty.getValues().get(0));
                }
                userGroups.add(userGroup);
            }
            authorityDTO.setUserGroups(userGroups);
        }
        return authorityDTO;
    }
}

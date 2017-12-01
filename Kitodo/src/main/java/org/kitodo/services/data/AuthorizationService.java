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
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.AuthorizationDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.AuthorizationType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.AuthorizationDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class AuthorizationService extends TitleSearchService<Authorization, AuthorizationDTO, AuthorizationDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(AuthorizationService.class);
    private static AuthorizationService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private AuthorizationService() {
        super(new AuthorizationDAO(), new AuthorizationType(), new Indexer<>(Authorization.class), new Searcher(Authorization.class));
        this.indexer = new Indexer<>(Authorization.class);
    }

    /**
     * Return singleton variable of type AuthorizationService.
     *
     * @return unique instance of AuthorizationService
     */
    public static AuthorizationService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (AuthorizationService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new AuthorizationService();
                }
            }
        }
        return instance;
    }

    /**
     * Get all authorizations from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @return list of AuthorizationDTO objects
     */
    @Override
    public List<AuthorizationDTO> findAll() throws DataException {
        return findAll(true);
    }

    /**
     * Get all authorizations from index and covert results to format accepted by
     * frontend. Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of AuthorizationDTO objects
     */
    @Override
    public List<AuthorizationDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return findAll(sort, offset, size, true);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Authorization");
    }

    /**
     * Method saves user groups related to modified authorization.
     *
     * @param authorization
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Authorization authorization) throws CustomResponseException, IOException {
        manageUserGroupsDependenciesForIndex(authorization);
    }

    /**
     * Check if IndexAction flag is delete. If true remove authorization from list
     * of authorizations and re-save user group, if false only re-save authorization object.
     *
     * @param authorization
     *            object
     */
    private void manageUserGroupsDependenciesForIndex(Authorization authorization) throws CustomResponseException, IOException {
        if (authorization.getIndexAction() == IndexAction.DELETE) {
            for (UserGroup userGroup : authorization.getUserGroups()) {
                userGroup.getAuthorizations().remove(authorization);
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        } else {
            for (UserGroup userGroup : authorization.getUserGroups()) {
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        }
    }

    @Override
    public AuthorizationDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        AuthorizationDTO authorizationDTO = new AuthorizationDTO();
        authorizationDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject authorizationJSONObject = getSource(jsonObject);
        authorizationDTO.setTitle(getStringPropertyForDTO(authorizationJSONObject, "title"));
        authorizationDTO.setUserGroupsSize(getSizeOfRelatedPropertyForDTO(authorizationJSONObject, "userGroups"));
        if (!related) {
            authorizationDTO = convertRelatedJSONObjects(authorizationJSONObject, authorizationDTO);
        } else {
            authorizationDTO = addBasicUserGroupRelation(authorizationDTO, authorizationJSONObject);
        }
        return authorizationDTO;
    }

    private AuthorizationDTO convertRelatedJSONObjects(JSONObject jsonObject, AuthorizationDTO authorizationDTO) throws DataException {
        authorizationDTO.setUserGroups(convertRelatedJSONObjectToDTO(jsonObject, "userGroups", serviceManager.getUserGroupService()));
        return authorizationDTO;
    }

    private AuthorizationDTO addBasicUserGroupRelation(AuthorizationDTO authorizationDTO, JSONObject jsonObject) {
        if (authorizationDTO.getUserGroupsSize() > 0) {
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
            authorizationDTO.setUserGroups(userGroups);
        }
        return authorizationDTO;
    }
}

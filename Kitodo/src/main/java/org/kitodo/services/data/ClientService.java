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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.JsonObject;

import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ClientDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ClientType;
import org.kitodo.data.elasticsearch.index.type.enums.ClientTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ClientDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class ClientService extends SearchService<Client, ClientDTO, ClientDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static ClientService instance = null;

    /**
     * Return singleton variable of type AuthorityService.
     *
     * @return unique instance of AuthorityService
     */
    public static ClientService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (ClientService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new ClientService();
                }
            }
        }
        return instance;
    }

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ClientService() {
        super(new ClientDAO(), new ClientType(), new Indexer<>(Client.class), new Searcher(Client.class));
        this.indexer = new Indexer<>(Client.class);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Client");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Client WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }


    @Override
    public List<Client> getAllNotIndexed() {
        return getByQuery("FROM Client WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Client> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject clientJSONObject = jsonObject.getJsonObject("_source");
        clientDTO.setName(ClientTypeField.NAME.getStringValue(clientJSONObject));
        clientDTO.setProjectsSize(ClientTypeField.PROJECTS.getSizeOfProperty(clientJSONObject));
        if (!related) {
            convertRelatedJSONObjects(clientJSONObject, clientDTO);
        } else {
            addBasicProjectRelation(clientDTO, clientJSONObject);
        }
        return clientDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, ClientDTO clientDTO) throws DataException {
        clientDTO.setProjects(convertRelatedJSONObjectToDTO(jsonObject, ClientTypeField.PROJECTS.getKey(),
            serviceManager.getProjectService()));
    }

    private void addBasicProjectRelation(ClientDTO clientDTO, JsonObject jsonObject) {
        if (clientDTO.getProjectsSize() > 0) {
            List<ProjectDTO> projects = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(ProjectTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                ClientTypeField.PROJECTS.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                ProjectDTO project = new ProjectDTO();
                project.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    project.setTitle(relatedProperty.getValues().get(0));
                }
                projects.add(project);
            }
            clientDTO.setProjects(projects);
        }
    }

    /**
     * Get all clients from index and covert results to format accepted by frontend.
     * Right now there is no usage which demands all relations.
     *
     * @return list of ClientDTO objects
     */
    @Override
    public List<ClientDTO> findAll() throws DataException {
        return findAll(true);
    }

    /**
     * Get all clients from index and covert results to format accepted by frontend.
     * Right now there is no usage which demands all relations.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @param offset
     *            start point for get results
     * @param size
     *            amount of requested results
     * @return list of ClientDTO objects
     */
    @Override
    public List<ClientDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return findAll(sort, offset, size, true);
    }

    /**
     * Refresh project object after update.
     *
     * @param client
     *            object
     */
    public void refresh(Client client) {
        dao.refresh(client);
    }

    /**
     * Find clients by id of projects.
     *
     * @param id
     *            The id of the project.
     * @return list of JSON objects with clients for specific project id
     */
    public JsonObject findByProjectId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("projects.id", id, true);
        return searcher.findDocument(query.toString());
    }

    /**
     * Get clients by a list of ids.
     * 
     * @param clientIds
     *            the list of ids
     * @return the list of clients
     */
    public List<Client> getByIds(List<Integer> clientIds) {
        if (!clientIds.isEmpty()) {
            return dao.getByIds(clientIds);
        }
        return new ArrayList<>();
    }
}

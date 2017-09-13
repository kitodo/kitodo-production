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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.WorkpieceDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkpieceType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.WorkpieceDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class WorkpieceService extends SearchService<Workpiece, WorkpieceDTO> {

    private WorkpieceDAO workpieceDAO = new WorkpieceDAO();
    private WorkpieceType workpieceType = new WorkpieceType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(WorkpieceService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public WorkpieceService() {
        super(new Searcher(Workpiece.class));
        this.indexer = new Indexer<>(Workpiece.class);
    }

    /**
     * Method saves workpiece object to database.
     *
     * @param workpiece
     *            object
     */
    @Override
    public void saveToDatabase(Workpiece workpiece) throws DAOException {
        workpieceDAO.save(workpiece);
    }

    /**
     * Method saves workpiece document to the index of Elastic Search.
     *
     * @param workpiece
     *            object
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveToIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (workpiece != null) {
            indexer.performSingleRequest(workpiece, workpieceType);
        }
    }

    /**
     * Method manages process and properties related to modified workpiece.
     *
     * @param workpiece
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        manageProcessDependenciesForIndex(workpiece);
        managePropertiesDependenciesForIndex(workpiece);
    }

    private void manageProcessDependenciesForIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        Process process = workpiece.getProcess();
        if (workpiece.getIndexAction() == IndexAction.DELETE) {
            process.getWorkpieces().remove(workpiece);
            serviceManager.getProcessService().saveToIndex(process);
        } else {
            serviceManager.getProcessService().saveToIndex(process);
        }
    }

    private void managePropertiesDependenciesForIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        if (workpiece.getIndexAction() == IndexAction.DELETE) {
            for (Property property : workpiece.getProperties()) {
                serviceManager.getPropertyService().removeFromIndex(property);
            }
        } else {
            for (Property property : workpiece.getProperties()) {
                serviceManager.getPropertyService().saveToIndex(property);
            }
        }
    }

    @Override
    public List<WorkpieceDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    @Override
    public Workpiece getById(Integer id) throws DAOException {
        return workpieceDAO.find(id);
    }

    @Override
    public List<Workpiece> getAll() {
        return workpieceDAO.findAll();
    }

    @Override
    public List<Workpiece> getAll(int offset, int size) throws DAOException {
        return workpieceDAO.getAll(offset, size);
    }

    /**
     * Search Workpiece objects by given query.
     *
     * @param query
     *            as String
     * @return list of Workpiece objects
     */
    @Override
    public List<Workpiece> getByQuery(String query) {
        return workpieceDAO.search(query);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return workpieceDAO.count("FROM Workpiece");
    }

    @Override
    public Long countDatabaseRows(String query) throws DAOException {
        return workpieceDAO.count(query);
    }

    /**
     * Method removes workpiece object from database.
     *
     * @param workpiece
     *            object
     */
    @Override
    public void removeFromDatabase(Workpiece workpiece) throws DAOException {
        workpieceDAO.remove(workpiece);
    }

    /**
     * Method removes workpiece object from database.
     *
     * @param id
     *            of workpiece object
     */
    @Override
    public void removeFromDatabase(Integer id) throws DAOException {
        workpieceDAO.remove(id);
    }

    /**
     * Method removes workpiece object from index of Elastic Search.
     *
     * @param workpiece
     *            object
     */
    @Override
    @SuppressWarnings("unchecked")
    public void removeFromIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (workpiece != null) {
            indexer.performSingleRequest(workpiece, workpieceType);
        }
    }

    /**
     * Find workpieces by id of process.
     *
     * @param id
     *            of process
     * @return search result with workpieces for specific process id
     */
    public List<JSONObject> findByProcessId(Integer id) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("process", id, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find workpieces by title of process.
     *
     * @param processTitle
     *            title of process
     * @return JSON objects with workpieces for specific process title
     */
    public List<JSONObject> findByProcessTitle(String processTitle) throws DataException {
        List<JSONObject> workpieces = new ArrayList<>();

        List<JSONObject> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (JSONObject process : processes) {
            workpieces.addAll(findByProcessId(getIdFromJSONObject(process)));
        }
        return workpieces;
    }

    /**
     * Find workpieces by property.
     *
     * @param title
     *            of property
     * @param value
     *            of property
     * @param contains
     *            true or false
     * @return list of JSON objects with workpieces for specific property
     */
    List<JSONObject> findByProperty(String title, String value, boolean contains) throws DataException {
        Set<Integer> propertyIds = new HashSet<>();

        List<JSONObject> properties;
        if (value == null) {
            properties = serviceManager.getPropertyService().findByTitle(title, "workpiece", contains);
        } else if (title == null) {
            properties = serviceManager.getPropertyService().findByValue(value, "workpiece", contains);
        } else {
            properties = serviceManager.getPropertyService().findByTitleAndValue(title, value, "workpiece", contains);
        }

        for (JSONObject property : properties) {
            propertyIds.add(getIdFromJSONObject(property));
        }
        return searcher.findDocuments(createSetQuery("properties.id", propertyIds, true).toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex() throws CustomResponseException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(getAll(), workpieceType);
    }

    @Override
    public WorkpieceDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        WorkpieceDTO workpieceDTO = new WorkpieceDTO();
        workpieceDTO.setId(getIdFromJSONObject(jsonObject));
        return workpieceDTO;
    }

    /**
     * Get size of properties list.
     *
     * @param workpiece
     *            object
     * @return properties list size
     */
    public int getPropertiesSize(Workpiece workpiece) {
        if (workpiece.getProperties() == null) {
            return 0;
        } else {
            return workpiece.getProperties().size();
        }
    }
}

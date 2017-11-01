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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

public class WorkpieceService extends SearchService<Workpiece, WorkpieceDTO, WorkpieceDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(WorkpieceService.class);
    private static WorkpieceService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private WorkpieceService() {
        super(new WorkpieceDAO(), new WorkpieceType(), new Indexer<>(Workpiece.class), new Searcher(Workpiece.class));
    }

    /**
     * Return singleton variable of type WorkpieceService.
     *
     * @return unique instance of WorkpieceService
     */
    public static WorkpieceService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (WorkpieceService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new WorkpieceService();
                }
            }
        }
        return instance;
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
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Workpiece");
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

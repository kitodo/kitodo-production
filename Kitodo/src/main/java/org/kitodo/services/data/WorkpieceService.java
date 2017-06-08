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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpieceDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkpieceType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class WorkpieceService extends SearchService<Workpiece> {

    private WorkpieceDAO workpieceDAO = new WorkpieceDAO();
    private WorkpieceType workpieceType = new WorkpieceType();
    private Indexer<Workpiece, WorkpieceType> indexer = new Indexer<>(Workpiece.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(WorkpieceService.class);

    /**
     * Constructor with searcher's assigning.
     */
    public WorkpieceService() {
        super(new Searcher(Workpiece.class));
    }

    /**
     * Method saves workpiece object to database.
     *
     * @param workpiece
     *            object
     */
    public void saveToDatabase(Workpiece workpiece) throws DAOException {
        workpieceDAO.save(workpiece);
    }

    /**
     * Method saves workpiece document to the index of Elastic Search.
     *
     * @param workpiece
     *            object
     */
    public void saveToIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    /**
     * Method saves process and properties related to modified workpiece.
     *
     * @param workpiece
     *            object
     */
    protected void saveDependenciesToIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        if (workpiece.getProcess() != null) {
            serviceManager.getProcessService().saveToIndex(workpiece.getProcess());
        }

        for (Property property : workpiece.getProperties()) {
            serviceManager.getPropertyService().saveToIndex(property);
        }
    }

    public Workpiece find(Integer id) throws DAOException {
        return workpieceDAO.find(id);
    }

    public List<Workpiece> findAll() {
        return workpieceDAO.findAll();
    }

    /**
     * Search Batch objects by given query.
     *
     * @param query
     *            as String
     * @return list of Batch objects
     */
    public List<Workpiece> search(String query) throws DAOException {
        return workpieceDAO.search(query);
    }

    /**
     * Method removes workpiece object from database.
     *
     * @param workpiece
     *            object
     */
    public void removeFromDatabase(Workpiece workpiece) throws DAOException {
        workpieceDAO.remove(workpiece);
    }

    /**
     * Method removes workpiece object from database.
     *
     * @param id
     *            of workpiece object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        workpieceDAO.remove(id);
    }

    /**
     * Method removes workpiece object from index of Elastic Search.
     *
     * @param workpiece
     *            object
     */
    public void removeFromIndex(Workpiece workpiece) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    /**
     * Find workpieces by id of process.
     *
     * @param id
     *            of process
     * @return search result with workpieces for specific process id
     */
    public List<SearchResult> findByProcessId(Integer id) throws CustomResponseException, IOException, ParseException {
        QueryBuilder queryBuilder = createSimpleQuery("process", id, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find workpieces by title of process.
     *
     * @param processTitle
     *            title of process
     * @return search results with workpieces for specific process title
     */
    public List<SearchResult> findByProcessTitle(String processTitle)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> workpieces = new ArrayList<>();

        List<SearchResult> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (SearchResult process : processes) {
            workpieces.addAll(findByProcessId(process.getId()));
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
     * @return list of search results with workpieces for specific property
     */
    public List<SearchResult> findByProperty(String title, String value)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> workpieces = new ArrayList<>();

        List<SearchResult> properties = serviceManager.getPropertyService().findByTitleAndValue(title, value);
        for (SearchResult property : properties) {
            workpieces.addAll(findByPropertyId(property.getId()));
        }
        return workpieces;
    }

    /**
     * Simulate relationship between property and workpiece type.
     *
     * @param id
     *            of property
     * @return list of search results with workpieces for specific property id
     */
    private List<SearchResult> findByPropertyId(Integer id)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("properties.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, DAOException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), workpieceType);
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

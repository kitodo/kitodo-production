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
import java.util.List;

import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpieceDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.WorkpieceType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class WorkpieceService extends SearchService<Workpiece> {

    private WorkpieceDAO workpieceDAO = new WorkpieceDAO();
    private WorkpieceType workpieceType = new WorkpieceType();
    private Indexer<Workpiece, WorkpieceType> indexer = new Indexer<>(Workpiece.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = Logger.getLogger(WorkpieceService.class);

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

    public List<Workpiece> findAll() throws DAOException {
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
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param workpiece
     *            object
     */
    public void remove(Workpiece workpiece) throws CustomResponseException, DAOException, IOException {
        workpieceDAO.remove(workpiece);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(workpiece, workpieceType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        workpieceDAO.remove(id);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(id);
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

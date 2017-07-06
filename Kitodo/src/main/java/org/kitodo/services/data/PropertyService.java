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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.PropertyDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.PropertyType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class PropertyService extends TitleSearchService<Property> {

    private PropertyDAO propertyDAO = new PropertyDAO();
    private PropertyType propertyType = new PropertyType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(PropertyService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public PropertyService() {
        super(new Searcher(Property.class));
        this.indexer = new Indexer<>(Property.class);
    }

    /**
     * Method saves property object to database.
     *
     * @param property
     *            object
     */
    public void saveToDatabase(Property property) throws DAOException {
        propertyDAO.save(property);
    }

    /**
     * Method saves property document to the index of Elastic Search.
     *
     * @param property
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(Property property) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (property != null) {
            indexer.performSingleRequest(property, propertyType);
        }
    }

    /**
     * Method saves processes related to modified batch.
     *
     * @param property
     *            object
     */
    protected void manageDependenciesForIndex(Property property)
            throws CustomResponseException, DataException, IOException {
        for (Process process : property.getProcesses()) {
            serviceManager.getProcessService().saveToIndex(process);
        }
        for (Template template : property.getTemplates()) {
            serviceManager.getTemplateService().saveToIndex(template);
        }
        for (Workpiece workpiece : property.getWorkpieces()) {
            serviceManager.getWorkpieceService().saveToIndex(workpiece);
        }
    }

    /**
     * Find in database.
     * 
     * @param id
     *            as Integer
     * @return Property
     */
    public Property find(Integer id) throws DAOException {
        return propertyDAO.find(id);
    }

    /**
     * Find all properties in database.
     * 
     * @return list of all properties
     */
    public List<Property> findAll() {
        return propertyDAO.findAll();
    }

    /**
     * Search by query in database.
     * 
     * @param query
     *            as String
     * @return list of properties
     */
    public List<Property> search(String query) throws DAOException {
        return propertyDAO.search(query);
    }

    /**
     * Method removes property object from database.
     *
     * @param property
     *            object
     */
    public void removeFromDatabase(Property property) throws DAOException {
        propertyDAO.remove(property);
    }

    /**
     * Method removes property object from database.
     *
     * @param id
     *            of property object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        propertyDAO.remove(id);
    }

    /**
     * Method removes property object from index of Elastic Search.
     *
     * @param property
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(Property property) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (property != null) {
            indexer.performSingleRequest(property, propertyType);
        }
    }

    /**
     * Find properties with exact value.
     *
     * @param value
     *            of the searched property
     * @param contains
     *            of the searched property
     * @return list of search results with properties
     */
    public List<SearchResult> findByValue(String value, boolean contains) throws DataException {
        QueryBuilder query = createSimpleQuery("value", value, contains, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find properties with exact title and type. Necessary to assure that user
     * pickup type from the list which contains enums.
     *
     * @param title
     *            of the searched property
     * @param value
     *            of the searched property
     * @return list of search results with batches of exact type
     */
    public List<SearchResult> findByTitleAndValue(String title, String value) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("title", title, true, Operator.AND));
        query.must(createSimpleQuery("value", value, true, Operator.AND));
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), propertyType);
    }

    /**
     * Get normalized title.
     * 
     * @param property
     *            object
     * @return normalized title
     */
    public String getNormalizedTitle(Property property) {
        return property.getTitle().replace(" ", "_").trim();
    }

    /**
     * Get normalized value.
     * 
     * @param property
     *            object
     * @return normalized value
     */
    public String getNormalizedValue(Property property) {
        return property.getValue().replace(" ", "_").trim();
    }
}

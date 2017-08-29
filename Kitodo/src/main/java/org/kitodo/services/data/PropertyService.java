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
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.PropertyDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.PropertyType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.PropertyDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class PropertyService extends TitleSearchService<Property, PropertyDTO> {

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
    @Override
    public void saveToDatabase(Property property) throws DAOException {
        propertyDAO.save(property);
    }

    /**
     * Method saves property document to the index of Elastic Search.
     *
     * @param property
     *            object
     */
    @Override
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
    @Override
    protected void manageDependenciesForIndex(Property property) throws CustomResponseException, IOException {
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

    @Override
    public List<PropertyDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    /**
     * Find in database.
     * 
     * @param id
     *            as Integer
     * @return Property
     */
    @Override
    public Property getById(Integer id) throws DAOException {
        return propertyDAO.find(id);
    }

    /**
     * Find all properties in database.
     * 
     * @return list of all properties
     */
    @Override
    public List<Property> getAll() {
        return propertyDAO.findAll();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return propertyDAO.count("FROM Property");
    }

    @Override
    public Long countDatabaseRows(String query) throws DAOException {
        return propertyDAO.count(query);
    }

    /**
     * Gets all titles from workpieceproperties.
     *
     * @return a list of titles.
     */
    public List<String> findWorkpiecePropertiesTitlesDistinct() {
        return propertyDAO.findWorkpiecePropertiesTitlesDistinct();
    }

    /**
     * Gets all titles from templateproperties.
     *
     * @return a list of titles.
     */
    public List<String> findTemplatePropertiesTitlesDistinct() {
        return propertyDAO.findTemplatePropertiesTitlesDistinct();
    }

    /**
     * Gets all titles from processProperties.
     *
     * @return a list of titles.
     */
    public List<String> findProcessPropertiesTitlesDistinct() {
        return propertyDAO.findProcessPropertiesTitlesDistinct();
    }

    /**
     * Search by query in database.
     * 
     * @param query
     *            as String
     * @return list of properties
     */
    @Override
    public List<Property> getByQuery(String query) throws DAOException {
        return propertyDAO.search(query);
    }

    /**
     * Method removes property object from database.
     *
     * @param property
     *            object
     */
    @Override
    public void removeFromDatabase(Property property) throws DAOException {
        propertyDAO.remove(property);
    }

    /**
     * Method removes property object from database.
     *
     * @param id
     *            of property object
     */
    @Override
    public void removeFromDatabase(Integer id) throws DAOException {
        propertyDAO.remove(id);
    }

    /**
     * Method removes property object from index of Elastic Search.
     *
     * @param property
     *            object
     */
    @Override
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
     * @return list of JSON objects with properties
     */
    public List<JSONObject> findByValue(String value, boolean contains) throws DataException {
        return findByValue(value, null, contains);
    }

    /**
     * Find properties with exact value.
     *
     * @param value
     *            of the searched property
     * @param type
     *            "process", "workpiece" or "template" as String
     * @param contains
     *            of the searched property
     * @return list of JSON objects with properties
     */
    public List<JSONObject> findByValue(String value, String type, boolean contains) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("value", value, contains, Operator.AND));
        if (type != null) {
            query.must(createSimpleQuery("type", type, true));
        }
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find properties with exact title and type. This one is used for searching in
     * all possible values.
     *
     * @param title
     *            of the searched property
     * @param value
     *            of the searched property
     * @param contains
     *            true or false
     * @return list of JSON objects with batches of exact type
     */
    public List<JSONObject> findByTitleAndValue(String title, String value, boolean contains) throws DataException {
        return findByTitleAndValue(title, value, null, contains);
    }

    /**
     * Find properties with exact title and type. Necessary to assure that user
     * pickup type from the list which contains enums. //TODO:add enum in future
     *
     * @param title
     *            of the searched property
     * @param value
     *            of the searched property
     * @param type
     *            "process", "workpiece" or "template" as String
     * @param contains
     *            true or false
     * @return list of JSON objects with batches of exact type
     */
    public List<JSONObject> findByTitleAndValue(String title, String value, String type, boolean contains)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("title", title, contains, Operator.AND));
        query.must(createSimpleQuery("value", value, contains, Operator.AND));
        if (type != null) {
            query.must(createSimpleQuery("type", type, true));
        }
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    @SuppressWarnings("unchecked")
    public void addAllObjectsToIndex() throws CustomResponseException, InterruptedException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(getAll(), propertyType);
    }

    @Override
    public PropertyDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setId(getIdFromJSONObject(jsonObject));
        propertyDTO.setTitle(getStringPropertyForDTO(jsonObject, "title"));
        propertyDTO.setValue(getStringPropertyForDTO(jsonObject, "value"));
        propertyDTO.setCreationDate(getStringPropertyForDTO(jsonObject, "creationDate"));
        return propertyDTO;
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

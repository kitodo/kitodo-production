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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.PropertyDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.PropertyType;
import org.kitodo.data.elasticsearch.index.type.enums.PropertyTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.TitleSearchService;
import org.primefaces.model.SortOrder;

public class PropertyService extends TitleSearchService<Property, PropertyDTO, PropertyDAO> {

    private static PropertyService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private PropertyService() {
        super(new PropertyDAO(), new PropertyType(), new Indexer<>(Property.class), new Searcher(Property.class));
    }

    /**
     * Return singleton variable of type PropertyService.
     *
     * @return unique instance of PropertyService
     */
    public static PropertyService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (PropertyService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new PropertyService();
                }
            }
        }
        return instance;
    }

    /**
     * Method saves processes related to modified batch.
     *
     * @param property
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Property property) throws CustomResponseException, DataException, IOException {
        for (Process process : property.getProcesses()) {
            ServiceManager.getProcessService().saveToIndex(process, false);
        }
        for (Process template : property.getTemplates()) {
            ServiceManager.getProcessService().saveToIndex(template, false);
        }
        for (Process workpiece : property.getWorkpieces()) {
            ServiceManager.getProcessService().saveToIndex(workpiece, false);
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Property");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Property WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(QueryBuilders.matchAllQuery());
    }

    @Override
    public List<Property> getAllNotIndexed() {
        return getByQuery("FROM Property WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Property> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Property> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Find all distinct titles from workpiece properties.
     *
     * @return a list of titles.
     */
    public List<String> findWorkpiecePropertiesTitlesDistinct() throws DataException {
        return findDistinctTitles("workpiece");
    }

    /**
     * Find all distinct titles from template properties.
     *
     * @return a list of titles.
     */
    public List<String> findTemplatePropertiesTitlesDistinct() throws DataException {
        return findDistinctTitles("template");
    }

    /**
     * Find all distinct titles from process properties.
     *
     * @return a list of titles.
     */
    public List<String> findProcessPropertiesTitlesDistinct() throws DataException {
        return findDistinctTitles("process");
    }

    private List<String> findDistinctTitles(String type) throws DataException {
        return findDistinctValues(getQueryForType(type), "title.keyword", true);
    }

    private QueryBuilder getQueryForType(String type) {
        return createSimpleQuery(PropertyTypeField.TYPE.getKey(), type, true, Operator.AND);
    }

    /**
     * Find properties with exact title for possible certain property type.
     *
     * @param title
     *            of the searched property
     * @param type
     *            "process", "workpiece" or "template" as String
     * @param contains
     *            of the searched property
     * @return list of JSON objects with properties
     */
    public List<Map<String,Object>> findByTitle(String title, String type, boolean contains) throws DataException {
        return findProperty(PropertyTypeField.TITLE.getKey(), title, type, contains);
    }

    /**
     * Find properties with exact value for possible certain property type.
     *
     * @param value
     *            of the searched property
     * @param type
     *            "process", "workpiece" or "template" as String
     * @param contains
     *            of the searched property
     * @return list of JSON objects with properties
     */
    List<Map<String,Object>> findByValue(String value, String type, boolean contains) throws DataException {
        return findProperty(PropertyTypeField.VALUE.getKey(), value, type, contains);
    }

    private List<Map<String,Object>> findProperty(String key, String value, String type, boolean contains) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(key, value, contains, Operator.AND));
        if (Objects.nonNull(type)) {
            query.must(createSimpleQuery(PropertyTypeField.TYPE.getKey(), type, true));
        }
        return findDocuments(query);
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
    List<Map<String, Object>> findByTitleAndValue(String title, String value, String type, boolean contains)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(PropertyTypeField.TITLE.getKey(), title, contains, Operator.AND));
        query.must(createSimpleQuery(PropertyTypeField.VALUE.getKey(), value, contains, Operator.AND));
        if (Objects.nonNull(type)) {
            query.must(createSimpleQuery(PropertyTypeField.TYPE.getKey(), type, true));
        }
        return findDocuments(query);
    }

    @Override
    public PropertyDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setId(getIdFromJSONObject(jsonObject));
        propertyDTO.setTitle(PropertyTypeField.TITLE.getStringValue(jsonObject));
        propertyDTO.setValue(PropertyTypeField.VALUE.getStringValue(jsonObject));
        propertyDTO.setCreationDate(PropertyTypeField.CREATION_DATE.getStringValue(jsonObject));
        return propertyDTO;
    }

    /**
     * Transfer property for duplication.
     * 
     * @param property
     *            as Property object
     * @return duplicated property as Property object
     */
    public Property transfer(Property property) {
        Property newProperty = new Property();
        newProperty.setTitle(property.getTitle());
        newProperty.setValue(property.getValue());
        return newProperty;
    }
}

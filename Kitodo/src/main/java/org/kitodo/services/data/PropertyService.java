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
import java.util.List;
import java.util.Objects;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
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
import org.kitodo.dto.PropertyDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class PropertyService extends TitleSearchService<Property, PropertyDTO, PropertyDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
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
    protected void manageDependenciesForIndex(Property property) throws CustomResponseException, IOException {
        for (Process process : property.getProcesses()) {
            serviceManager.getProcessService().saveToIndex(process, false);
        }
        for (Process template : property.getTemplates()) {
            serviceManager.getProcessService().saveToIndex(template, false);
        }
        for (Process workpiece : property.getWorkpieces()) {
            serviceManager.getProcessService().saveToIndex(workpiece, false);
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Property");
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
        return findDistinctValues(getQueryForType(type).toString(), "title.keyword", true);
    }

    private QueryBuilder getQueryForType(String type) {
        return createSimpleQuery(PropertyTypeField.TYPE.getName(), type, true, Operator.AND);
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
    public List<JsonObject> findByTitle(String title, String type, boolean contains) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(PropertyTypeField.TITLE.getName(), title, contains, Operator.AND));
        if (type != null) {
            query.must(createSimpleQuery(PropertyTypeField.TYPE.getName(), type, true));
        }
        return searcher.findDocuments(query.toString());
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
    List<JsonObject> findByValue(String value, String type, boolean contains) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(PropertyTypeField.VALUE.getName(), value, contains, Operator.AND));
        if (type != null) {
            query.must(createSimpleQuery(PropertyTypeField.TYPE.getName(), type, true));
        }
        return searcher.findDocuments(query.toString());
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
    List<JsonObject> findByTitleAndValue(String title, String value, String type, boolean contains)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(PropertyTypeField.TITLE.getName(), title, contains, Operator.AND));
        query.must(createSimpleQuery(PropertyTypeField.VALUE.getName(), value, contains, Operator.AND));
        if (type != null) {
            query.must(createSimpleQuery(PropertyTypeField.TYPE.getName(), type, true));
        }
        return searcher.findDocuments(query.toString());
    }

    @Override
    public PropertyDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) {
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject propertyJSONObject = jsonObject.getJsonObject("_source");
        propertyDTO.setTitle(propertyJSONObject.getString(PropertyTypeField.TITLE.getName()));
        propertyDTO.setValue(propertyJSONObject.getString(PropertyTypeField.VALUE.getName()));
        JsonValue creationDate = propertyJSONObject.get(PropertyTypeField.CREATION_DATE.getName());
        propertyDTO.setCreationDate(creationDate != JsonValue.NULL ? creationDate.toString() : null);
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

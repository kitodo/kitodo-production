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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.PropertyDAO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class PropertyService extends SearchDatabaseService<Property, PropertyDAO> {

    private static volatile PropertyService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private PropertyService() {
        super(new PropertyDAO());
    }

    /**
     * Return singleton variable of type PropertyService.
     *
     * @return unique instance of PropertyService
     */
    public static PropertyService getInstance() {
        PropertyService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (PropertyService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new PropertyService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Property");
    }


    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    public List<Property> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Find all distinct property titles.
     *
     * @return a list of titles.
     */
    public List<String> findDistinctTitles() {
        return dao.retrieveDistinctTitles();
    }

    /**
     * Find properties with exact title for possible certain property type.
     *
     * @param title
     *            of the searched property
     * @return list of JSON objects with properties
     */
    public List<Property> findByTitle(String title) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        return getByQuery("from Property as property where property.title=:title", parameters);
    }

    /**
     * Find properties with exact value for possible certain property type.
     *
     * @param value
     *            of the searched property
     * @return list of JSON objects with properties
     */
    List<Property> findByValue(String value) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("value", value);
        return getByQuery("from Property as property where property.value=:value", parameters);
    }

    /**
     * Find properties with exact title and type. Necessary to assure that user
     * pickup type from the list which contains enums. //TODO:add enum in future
     *
     * @param title
     *            of the searched property
     * @param value
     *            of the searched property
     * @return list of JSON objects with batches of exact type
     */
    List<Property> findByTitleAndValue(String title, String value) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("value", value);
        return getByQuery("from Property as property where property.title=:title and property.value=:value", parameters);
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

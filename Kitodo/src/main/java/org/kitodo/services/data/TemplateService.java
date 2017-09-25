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
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TemplateType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class TemplateService extends SearchService<Template, TemplateDTO> {

    private TemplateDAO templateDAO = new TemplateDAO();
    private TemplateType templateType = new TemplateType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(TemplateService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public TemplateService() {
        super(new Searcher(Template.class));
        this.indexer = new Indexer<>(Template.class);
    }

    /**
     * Method saves template object to database.
     *
     * @param template
     *            object
     */
    @Override
    public void saveToDatabase(Template template) throws DAOException {
        templateDAO.save(template);
    }

    /**
     * Method saves template document to the index of Elastic Search.
     *
     * @param template
     *            object
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveToIndex(Template template) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (template != null) {
            indexer.performSingleRequest(template, templateType);
        }
    }

    /**
     * Method manages process and properties related to modified template.
     *
     * @param template
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        manageProcessDependenciesForIndex(template);
        managePropertiesDependenciesForIndex(template);
    }

    private void manageProcessDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        Process process = template.getProcess();
        if (template.getIndexAction() == IndexAction.DELETE) {
            process.getTemplates().remove(template);
            serviceManager.getProcessService().saveToIndex(process);
        } else {
            serviceManager.getProcessService().saveToIndex(process);
        }
    }

    private void managePropertiesDependenciesForIndex(Template template) throws CustomResponseException, IOException {
        List<Property> properties = template.getProperties();
        if (template.getIndexAction() == IndexAction.DELETE) {
            for (Property property : properties) {
                serviceManager.getPropertyService().removeFromIndex(property);
            }
        } else {
            for (Property property : properties) {
                serviceManager.getPropertyService().saveToIndex(property);
            }
        }
    }

    @Override
    public List<TemplateDTO> findAll(String sort, Integer offset, Integer size) throws DataException {
        return convertJSONObjectsToDTOs(findAllDocuments(sort, offset, size), false);
    }

    @Override
    public Template getById(Integer id) throws DAOException {
        return templateDAO.find(id);
    }

    @Override
    public List<Template> getAll() {
        return templateDAO.findAll();
    }

    @Override
    public List<Template> getAll(int offset, int size) throws DAOException {
        return templateDAO.getAll(offset, size);
    }

    /**
     * Search Template objects by given query.
     *
     * @param query
     *            as String
     * @return list of Template objects
     */
    @Override
    public List<Template> getByQuery(String query) {
        return templateDAO.search(query);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return templateDAO.count("FROM Template");
    }

    @Override
    public Long countDatabaseRows(String query) throws DAOException {
        return templateDAO.count(query);
    }

    /**
     * Method removes template object from database.
     *
     * @param template
     *            object
     */
    @Override
    public void removeFromDatabase(Template template) throws DAOException {
        templateDAO.remove(template);
    }

    /**
     * Method removes template object from database.
     *
     * @param id
     *            of template object
     */
    @Override
    public void removeFromDatabase(Integer id) throws DAOException {
        templateDAO.remove(id);
    }

    /**
     * Method removes template object from index of Elastic Search.
     *
     * @param template
     *            object
     */
    @Override
    @SuppressWarnings("unchecked")
    public void removeFromIndex(Template template) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (template != null) {
            indexer.performSingleRequest(template, templateType);
        }
    }

    /**
     * Find templates by origin.
     *
     * @param origin
     *            of template
     * @return search result with templates for specific origin
     */
    public List<JSONObject> findByOrigin(String origin) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("origin", origin, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find templates by id of process.
     *
     * @param id
     *            of process
     * @return search result with templates for specific process id
     */
    public List<JSONObject> findByProcessId(Integer id) throws DataException {
        QueryBuilder queryBuilder = createSimpleQuery("process", id, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find templates by title of process.
     *
     * @param processTitle
     *            title of process
     * @return JSON objects with templates for specific process title
     */
    public List<JSONObject> findByProcessTitle(String processTitle) throws DataException {
        List<JSONObject> templates = new ArrayList<>();

        List<JSONObject> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (JSONObject process : processes) {
            templates.addAll(findByProcessId(getIdFromJSONObject(process)));
        }
        return templates;
    }

    /**
     * Find templates by property.
     *
     * @param title
     *            of property
     * @param value
     *            of property
     * @param contains
     *            true or false
     * @return list of JSON objects with templates for specific property
     */
    List<JSONObject> findByProperty(String title, String value, boolean contains) throws DataException {
        Set<Integer> propertyIds = new HashSet<>();

        List<JSONObject> properties;
        if (value == null) {
            properties = serviceManager.getPropertyService().findByTitle(title, "template", contains);
        } else if (title == null) {
            properties = serviceManager.getPropertyService().findByValue(value, "template", contains);
        } else {
            properties = serviceManager.getPropertyService().findByTitleAndValue(title, value, "template", contains);
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
        indexer.performMultipleRequests(getAll(), templateType);
    }

    @Override
    public TemplateDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject templateJSONObject = getSource(jsonObject);
        templateDTO.setPropertiesSize(getSizeOfRelatedPropertyForDTO(templateJSONObject, "properties"));
        return templateDTO;
    }
}

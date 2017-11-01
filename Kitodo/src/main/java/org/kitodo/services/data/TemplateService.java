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

public class TemplateService extends SearchService<Template, TemplateDTO, TemplateDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(TemplateService.class);
    private static TemplateService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TemplateService() {
        super(new TemplateDAO(), new TemplateType(), new Indexer<>(Template.class), new Searcher(Template.class));
    }

    /**
     * Return singleton variable of type TemplateService.
     *
     * @return unique instance of TemplateService
     */
    public static TemplateService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (TemplateService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new TemplateService();
                }
            }
        }
        return instance;
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
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Template");
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

    @Override
    public TemplateDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject templateJSONObject = getSource(jsonObject);
        templateDTO.setPropertiesSize(getSizeOfRelatedPropertyForDTO(templateJSONObject, "properties"));
        return templateDTO;
    }
}

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
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TemplateType;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class TemplateService extends SearchService<Template> {

    private TemplateDAO templateDAO = new TemplateDAO();
    private TemplateType templateType = new TemplateType();
    private Indexer<Template, TemplateType> indexer = new Indexer<>(Template.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(TemplateService.class);

    /**
     * Constructor with searcher's assigning.
     */
    public TemplateService() {
        super(new Searcher(Template.class));
    }

    /**
     * Method saves template object to database.
     *
     * @param template
     *            object
     */
    public void saveToDatabase(Template template) throws DAOException {
        templateDAO.save(template);
    }

    /**
     * Method saves template document to the index of Elastic Search.
     *
     * @param template
     *            object
     */
    public void saveToIndex(Template template) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(template, templateType);
    }

    /**
     * Method saves process and properties related to modified template.
     *
     * @param template
     *            object
     */
    protected void saveDependenciesToIndex(Template template) throws CustomResponseException, IOException {
        if (template.getProcess() != null) {
            serviceManager.getProcessService().saveToIndex(template.getProcess());
        }

        for (Property property : template.getProperties()) {
            serviceManager.getPropertyService().saveToIndex(property);
        }
    }

    public Template find(Integer id) throws DAOException {
        return templateDAO.find(id);
    }

    public List<Template> findAll() throws DAOException {
        return templateDAO.findAll();
    }

    /**
     * Search Template objects by given query.
     *
     * @param query
     *            as String
     * @return list of Template objects
     */
    public List<Template> search(String query) throws DAOException {
        return templateDAO.search(query);
    }

    /**
     * Method removes template object from database.
     *
     * @param template
     *            object
     */
    public void removeFromDatabase(Template template) throws DAOException {
        templateDAO.remove(template);
    }

    /**
     * Method removes template object from database.
     *
     * @param id
     *            of template object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        templateDAO.remove(id);
    }

    /**
     * Method removes template object from index of Elastic Search.
     *
     * @param template
     *            object
     */
    public void removeFromIndex(Template template) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(template, templateType);
    }

    /**
     * Find templates by origin.
     *
     * @param origin
     *            of template
     * @return search result with templates for specific origin
     */
    public List<SearchResult> findByOrigin(String origin) throws CustomResponseException, IOException, ParseException {
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
    public List<SearchResult> findByProcessId(Integer id) throws CustomResponseException, IOException, ParseException {
        QueryBuilder queryBuilder = createSimpleQuery("process", id, true);
        return searcher.findDocuments(queryBuilder.toString());
    }

    /**
     * Find templates by title of process.
     *
     * @param processTitle
     *            title of process
     * @return search results with templates for specific process title
     */
    public List<SearchResult> findByProcessTitle(String processTitle)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> templates = new ArrayList<>();

        List<SearchResult> processes = serviceManager.getProcessService().findByTitle(processTitle, true);
        for (SearchResult process : processes) {
            templates.addAll(findByProcessId(process.getId()));
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
     * @return list of search results with templates for specific property
     */
    public List<SearchResult> findByProperty(String title, String value)
            throws CustomResponseException, IOException, ParseException {
        List<SearchResult> templates = new ArrayList<>();

        List<SearchResult> properties = serviceManager.getPropertyService().findByTitleAndValue(title, value);
        for (SearchResult property : properties) {
            templates.addAll(findByPropertyId(property.getId()));
        }
        return templates;
    }

    /**
     * Simulate relationship between property and template type.
     *
     * @param id
     *            of property
     * @return list of search results with templates for specific property id
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
        indexer.performMultipleRequests(findAll(), templateType);
    }

    /**
     * Get size of properties list.
     *
     * @param template
     *            object
     * @return size of properties list
     */
    public int getPropertiesSize(Template template) {
        if (template.getProperties() == null) {
            return 0;
        } else {
            return template.getProperties().size();
        }
    }
}

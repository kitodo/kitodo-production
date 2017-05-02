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

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TemplateType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.SearchService;

public class TemplateService extends SearchService<Template> {

    private TemplateDAO templateDao = new TemplateDAO();
    private TemplateType templateType = new TemplateType();
    private Indexer<Template, TemplateType> indexer = new Indexer<>(Template.class);

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
        templateDao.save(template);
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

    public Template find(Integer id) throws DAOException {
        return templateDao.find(id);
    }

    public List<Template> findAll() throws DAOException {
        return templateDao.findAll();
    }

    /**
     * Search Template objects by given query.
     *
     * @param query
     *            as String
     * @return list of Template objects
     */
    public List<Template> search(String query) throws DAOException {
        return templateDao.search(query);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param template
     *            object
     */
    public void remove(Template template) throws CustomResponseException, DAOException, IOException {
        templateDao.remove(template);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(template, templateType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws CustomResponseException, DAOException, IOException {
        templateDao.remove(id);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(id);
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

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

package org.kitodo.services;

import com.sun.research.ws.wadl.HTTPMethods;

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.TemplateType;

import java.io.IOException;

public class TemplateService {

    private TemplateDAO templateDao = new TemplateDAO();
    private TemplateType templateType = new TemplateType();
    private Indexer<Template, TemplateType> indexer = new Indexer<>("kitodo", Template.class);

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param template object
     */
    public void save(Template template) throws DAOException, IOException {
        templateDao.save(template);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(template, templateType);
    }

    public Template find(Integer id) throws DAOException {
        return templateDao.find(id);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param template object
     */
    public void remove(Template template) throws DAOException, IOException {
        templateDao.remove(template);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(template, templateType);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param id of object
     */
    public void remove(Integer id) throws DAOException, IOException {
        templateDao.remove(id);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(id);
    }

    /**
     * Get size of properties list.
     *
     * @param template object
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

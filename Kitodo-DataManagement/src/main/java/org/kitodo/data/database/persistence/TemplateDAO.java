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

package org.kitodo.data.database.persistence;

import java.util.List;

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;

public class TemplateDAO extends BaseDAO<Template> {
    private static final long serialVersionUID = 1736135433162833277L;

    /**
     * Find template object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Template find(Integer id) throws DAOException {
        Template result = retrieveObject(Template.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all templates from the database.
     *
     * @return all persisted dockets
     */
    public List<Template> findAll() {
        return retrieveAllObjects(Template.class);
    }

    /**
     * Retrieves all templates in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<Template> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM Template", first, max);
    }

    public Template save(Template template) throws DAOException {
        storeObject(template);
        return retrieveObject(Template.class, template.getId());
    }

    /**
     * The function remove() removes a template from database.
     *
     * @param template
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Template template) throws DAOException {
        if (template.getId() != null) {
            removeObject(template);
        }
    }

    /**
     * The function remove() removes a template from database.
     *
     * @param id
     *            of the task to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        removeObject(Template.class, id);
    }

    public List<Template> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;

public class TemplateDAO extends BaseDAO<Template> {

    @Override
    public Template getById(Integer id) throws DAOException {
        Template template = retrieveObject(Template.class, id);
        if (template == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return template;
    }

    @Override
    public List<Template> getAll() throws DAOException {
        return retrieveAllObjects(Template.class);
    }

    @Override
    public List<Template> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Template ORDER BY title ASC", offset, size);
    }

    @Override
    public List<Template> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Template WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC",
            offset, size);
    }

    /**
     * Save list of templates.
     *
     * @param list
     *            of templates
     * @throws DAOException
     *             an exception that can be thrown from the underlying saveList()
     *             procedure failure.
     */
    public void saveList(List<Template> list) throws DAOException {
        storeList(list);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Template.class, id);
    }

    /**
     * Update process object after some changes.
     *
     * @param process
     *            object
     */
    public void update(Template process) {
        updateObject(process);
    }

    /**
     * Get all process templates with exact title.
     *
     * @return list of all process templates as Template objects
     */
    public List<Template> getTemplatesWithTitle(String title) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        return getByQuery("FROM Template WHERE title LIKE :title ORDER BY title ASC", parameters);
    }

    /**
     * Get all active templates for selected client.
     *
     * @return list of all active templates for selected client as Template objects
     */
    public List<Template> getActiveTemplates(int clientId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        return getByQuery("SELECT t FROM Template AS t WHERE active = 1 AND client_id = :clientId",
            parameters);
    }
}

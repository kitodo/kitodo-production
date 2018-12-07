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

    private static final long serialVersionUID = 3538712266212954394L;

    @Override
    public Template getById(Integer id) throws DAOException {
        Template result = retrieveObject(Template.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
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

    @Override
    public Template save(Template process) throws DAOException {
        storeObject(process);
        return retrieveObject(Template.class, process.getId());
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
     * Get process templates for users.
     *
     * @param projects
     *            list of project ids fof user's projects
     * @return list of all process templates for user as Process objects
     */
    public List<Template> getTemplatesForUser(List<Integer> projects) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projects", projects);
        return getByQuery(
            "SELECT t FROM Template AS t JOIN t.projects AS p WHERE p.id IN (:projects) ORDER BY t.title ASC",
            parameters);
    }

    /**
     * Get all active templates.
     *
     * @return list of all active templates as Template objects
     */
    public List<Template> getActiveTemplates() {
        return getByQuery("SELECT t FROM Template AS t INNER JOIN t.projects AS p WHERE p.active = 1");
    }
}

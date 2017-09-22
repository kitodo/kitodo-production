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

    @Override
    public Template getById(Integer id) throws DAOException {
        Template result = retrieveObject(Template.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Template> getAll() {
        return retrieveAllObjects(Template.class);
    }

    @Override
    public List<Template> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Template ORDER BY id ASC", offset, size);
    }

    @Override
    public Template save(Template template) throws DAOException {
        storeObject(template);
        return retrieveObject(Template.class, template.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Template.class, id);
    }
}

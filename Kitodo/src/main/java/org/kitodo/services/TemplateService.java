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

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplateDAO;

public class TemplateService {

    private TemplateDAO templateDao = new TemplateDAO();

    public void save(Template template) throws DAOException {
        templateDao.save(template);
    }

    public Template find(Integer id) throws DAOException {
        return templateDao.find(id);
    }

    public void remove(Template template) throws DAOException {
        templateDao.remove(template);
    }

    public void remove(Integer id) throws DAOException {
        templateDao.remove(id);
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

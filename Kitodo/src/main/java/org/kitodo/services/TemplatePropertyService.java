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

import java.util.List;

import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.TemplatePropertyDAO;

public class TemplatePropertyService {

    private TemplatePropertyDAO templatePropertyDao = new TemplatePropertyDAO();

    public void save(TemplateProperty templateProperty) throws DAOException {
        templatePropertyDao.save(templateProperty);
    }

    public TemplateProperty find(Integer id) throws DAOException {
        return templatePropertyDao.find(id);
    }

    public List<TemplateProperty> findAll() throws DAOException {
        return templatePropertyDao.findAll();
    }

    public void remove(TemplateProperty workpieceProperty) throws DAOException {
        templatePropertyDao.remove(workpieceProperty);
    }

    public String getNormalizedTitle(TemplateProperty templateProperty) {
        return templateProperty.getTitle().replace(" ", "_").trim();
    }

    public String getNormalizedValue(TemplateProperty templateProperty) {
        return templateProperty.getValue().replace(" ", "_").trim();
    }
}

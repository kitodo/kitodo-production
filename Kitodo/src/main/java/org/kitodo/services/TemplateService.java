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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
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

    public void remove(Template workpiece) throws DAOException {
        templateDao.remove(workpiece);
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

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

import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.exceptions.DAOException;

public class TemplatePropertyDAO extends BaseDAO {

    private static final long serialVersionUID = 834210846673022251L;

    /**
     * Find template property object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public TemplateProperty find(Integer id) throws DAOException {
        TemplateProperty result = (TemplateProperty) retrieveObject(TemplateProperty.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all templates' properties from the
     * database.
     *
     * @return all persisted templates' properties
     */
    @SuppressWarnings("unchecked")
    public List<TemplateProperty> findAll() {
        return retrieveAllObjects(TemplateProperty.class);
    }

    public TemplateProperty save(TemplateProperty templateProperty) throws DAOException {
        storeObject(templateProperty);
        return (TemplateProperty) retrieveObject(TemplateProperty.class, templateProperty.getId());
    }

    /**
     * The function remove() removes a template property
     *
     * @param templateProperty
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(TemplateProperty templateProperty) throws DAOException {
        removeObject(templateProperty);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}

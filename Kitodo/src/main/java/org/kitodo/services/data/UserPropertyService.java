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

import java.util.List;

import org.kitodo.data.database.beans.UserProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserPropertyDAO;

public class UserPropertyService {

    private UserPropertyDAO userPropertyDao = new UserPropertyDAO();

    public void save(UserProperty userProperty) throws DAOException {
        userPropertyDao.save(userProperty);
    }

    public UserProperty find(Integer id) throws DAOException {
        return userPropertyDao.find(id);
    }

    public List<UserProperty> findAll() throws DAOException {
        return userPropertyDao.findAll();
    }

    public void remove(UserProperty userProperty) throws DAOException {
        userPropertyDao.remove(userProperty);
    }

    public String getNormalizedTitle(UserProperty userProperty) {
        return userProperty.getTitle().replace(" ", "_").trim();
    }

    public String getNormalizedValue(UserProperty userProperty) {
        return userProperty.getValue().replace(" ", "_").trim();
    }
}

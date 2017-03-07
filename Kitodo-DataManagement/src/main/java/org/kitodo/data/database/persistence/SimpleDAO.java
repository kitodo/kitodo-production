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

import org.kitodo.data.database.exceptions.DAOException;

public class SimpleDAO extends BaseDAO {

    private static final long serialVersionUID = 599953115583442026L;

    public void save(Object t) throws DAOException {
        storeObject(t);
    }

    public void remove(Object t) throws DAOException {
        removeObject(t);
    }

    public void refreshObject(Object t) {
        Object o = t;
        refresh(o);
    }
}

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

package org.kitodo.production.services.data.base;

import java.util.List;

import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.persistence.BaseDAO;

public abstract class ClientSearchDatabaseService<T extends BaseBean, S extends BaseDAO<T>>
        extends SearchDatabaseService<T, S> {

    /**
     * Constructor necessary to use searcher in child classes.
     *
     * @param dao
     *            for executing queries
     */
    public ClientSearchDatabaseService(S dao) {
        super(dao);
    }

    /**
     * Get list of all objects for selected client from database.
     *
     * @return list of all objects for selected client from database
     */
    public abstract List<T> getAllForSelectedClient();
}

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
import java.util.Objects;

import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ListColumnDAO;

public class ListColumnService extends SearchDatabaseService<ListColumn, ListColumnDAO> {

    private static ListColumnService instance = null;

    /**
     * Constructor necessary to use searcher in child classes.
     */
    private ListColumnService() {
        super(new ListColumnDAO());
    }

    /**
     * Return singleton variable of type ListColumnService.
     *
     * @return unique instance of ListColumnService
     */
    public static ListColumnService getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (ListColumnService.class) {
                if (Objects.isNull(instance)) {
                    instance = new ListColumnService();
                }
            }
        }
        return instance;
    }

    @Override
    public List<ListColumn> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM ListColumn");
    }
}

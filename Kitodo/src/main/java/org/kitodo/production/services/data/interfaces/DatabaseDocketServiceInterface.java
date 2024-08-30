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

package org.kitodo.production.services.data.interfaces;

import java.util.List;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;

/**
 * Specifies the special database-related functions of the docket service.
 */
public interface DatabaseDocketServiceInterface extends SearchDatabaseServiceInterface<Docket> {

    /**
     * Returns all docket configuration objects of the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * The function requires that the thread is assigned to a logged-in user.
     * 
     * @return all dockets for the selected client
     */
    List<Docket> getAllForSelectedClient();

    /**
     * Returns all docket configuration objects with the specified label. This
     * can be used to check whether a label is still available.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * There is currently no filtering by client, so a label used by one client
     * cannot be used by another client.
     * 
     * @param title
     *            name to search for
     * @return list of dockets
     */
    List<Docket> getByTitle(String title);

    // === alternative functions that are no longer required ===

    /**
     * Find object in ES and convert it to Interface.
     *
     * @param id
     *            object id
     * @return Interface object
     * @deprecated Use {@link #getById(Integer)}.
     */
    default Docket findById(Integer id) throws DataException {
        try {
            return getById(id);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }
}

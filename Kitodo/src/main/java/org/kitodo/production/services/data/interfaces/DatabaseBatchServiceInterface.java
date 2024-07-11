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

import java.util.Collection;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.exceptions.DataException;

/**
 * Specifies the special database-related functions of the batch service.
 */
public interface DatabaseBatchServiceInterface extends SearchDatabaseServiceInterface<Batch> {

    /**
     * Deletes all given batches from the database.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * The function must get all processes from each batch and delete the
     * batches to be deleted in each process, before it deletes the batches.
     *
     * @param batches
     *            batches to delete
     */
    void removeAll(Collection<Batch> batches) throws DataException;
}

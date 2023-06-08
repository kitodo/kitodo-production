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

package org.kitodo.production.services.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.MappingFileDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class MappingFileService extends SearchDatabaseService<MappingFile, MappingFileDAO> {

    private static volatile MappingFileService instance = null;

    /**
     * Constructor necessary to use searcher in child classes.
     */
    public MappingFileService() {
        super(new MappingFileDAO());
    }

    /**
     * Return singleton variable of type MappingFileService.
     *
     * @return unique instance of MappingFileService
     */
    public static MappingFileService getInstance() {
        MappingFileService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MappingFileService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MappingFileService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Load data for frontend lists. Data can be loaded from database or index.
     *
     * @param first     searched objects
     * @param pageSize  size of page
     * @param sortField field by which data should be sorted
     * @param sortOrder order ascending or descending
     * @param filters   for search query
     * @return loaded data
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<MappingFile> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return dao.getByQuery("FROM MappingFile"  + getSort(sortField, sortOrder), filters, first, pageSize);
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM MappingFile");
    }

    /**
     * This function is used for count amount of results for frontend lists.
     *
     * @param filters Map of parameters used for filtering
     * @return amount of results
     * @throws DAOException  that can be caused by Hibernate
     * @throws DataException that can be caused by ElasticSearch
     */
    @Override
    public Long countResults(Map filters) throws DAOException, DataException {
        return countDatabaseRows();
    }
}

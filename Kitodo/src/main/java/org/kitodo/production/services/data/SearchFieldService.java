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

import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.SearchFieldDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class SearchFieldService extends SearchDatabaseService<SearchField, SearchFieldDAO> {

    private static volatile SearchFieldService instance = null;

    /**
     * Default constructor.
     */
    public SearchFieldService() {
        super(new SearchFieldDAO());
    }

    /**
     * Return singleton variable of type SearchFieldService.
     *
     * @return unique instance of SearchFieldService
     */
    public static SearchFieldService getInstance() {
        SearchFieldService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ImportConfigurationService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new SearchFieldService();
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
    public List loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) throws DataException {
        return null;
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM searchfield");
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

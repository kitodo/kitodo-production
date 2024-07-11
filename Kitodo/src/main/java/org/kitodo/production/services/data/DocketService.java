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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseDocketServiceInterface;
import org.primefaces.model.SortOrder;

public class DocketService extends SearchDatabaseService<Docket, DocketDAO> implements DatabaseDocketServiceInterface {

    private static final Map<String, String> SORT_FIELD_MAPPING;

    static {
        SORT_FIELD_MAPPING = new HashMap<>();
        SORT_FIELD_MAPPING.put("title.keyword", "title");
        SORT_FIELD_MAPPING.put("file.keyword", "file");
    }

    private static volatile DocketService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private DocketService() {
        super(new DocketDAO());
    }

    /**
     * Return singleton variable of type DocketService.
     *
     * @return unique instance of DocketService
     */
    public static DocketService getInstance() {
        DocketService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (DocketService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new DocketService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Docket");
    }

    @Override
    public Long countResults(Map<?, String> filters) throws DataException {
        try {
            Map<String, Object> parameters = Collections.singletonMap("sessionClientId",
                ServiceManager.getUserService().getSessionClientId());
            return countDatabaseRows("SELECT COUNT(*) FROM Docket WHERE client_id = :sessionClientId", parameters);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Docket> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters)
            throws DataException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sessionClientId", ServiceManager.getUserService().getSessionClientId());
        String desiredOrder = SORT_FIELD_MAPPING.getOrDefault(sortField, sortField) + ' '
            + SORT_ORDER_MAPPING.get(sortOrder);
        return getByQuery("FROM Docket WHERE client_id = :sessionClientId ORDER BY ".concat(desiredOrder), parameters,
            first, pageSize);
    }

    @Override
    public List<Docket> getAllForSelectedClient() {
        return dao.getByQuery("SELECT d FROM Docket AS d INNER JOIN d.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<Docket> getByTitle(String title) {
        return dao.getByQuery("FROM Docket WHERE title = :title", Collections.singletonMap("title", title));
    }
}

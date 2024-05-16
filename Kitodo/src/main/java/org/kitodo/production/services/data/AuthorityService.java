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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.AuthorityDAO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class AuthorityService extends SearchDatabaseService<Authority, AuthorityDAO> {

    private static final Logger logger = LogManager.getLogger(AuthorityService.class);

    private static volatile AuthorityService instance = null;

    /**
     * The maximum number of authorities to load from the database such that sorting can be done
     * in this service instead of the database (which doesn't know how to sort translated authority titles).
     */
    private static final Integer MAX_SORT_BUFFER_SIZE = 1000;

    private static final String GLOBAL_AUTHORITY_SUFFIX = "_globalAssignable";
    private static final String CLIENT_AUTHORITY_SUFFIX = "_clientAssignable";

    /**
     * Constructor.
     */
    private AuthorityService() {
        super(new AuthorityDAO());
    }

    /**
     * Return singleton variable of type AuthorityService.
     *
     * @return unique instance of AuthorityService
     */
    public static AuthorityService getInstance() {
        AuthorityService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (AuthorityService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new AuthorityService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Get global authority suffix.
     *
     * @return the global authority suffix
     */
    public String getGlobalAuthoritySuffix() {
        return GLOBAL_AUTHORITY_SUFFIX;
    }

    /**
     * Get client authority suffix.
     *
     * @return the client authority suffix.
     */
    public String getClientAuthoritySuffix() {
        return CLIENT_AUTHORITY_SUFFIX;
    }

    @Override
    public void refresh(Authority authority) {
        dao.refresh(authority);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Authority");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Authority> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        // query all authorities from database
        List<Authority> authorities = dao.getByQuery("FROM Authority", filters, 0, MAX_SORT_BUFFER_SIZE);
        if (authorities.size() >= MAX_SORT_BUFFER_SIZE) {
            // print warning but still return authorities directly from database with database sorting
            // that can not consider translated authority titles
            logger.warn("Can't sort more than " + MAX_SORT_BUFFER_SIZE + " authorities!");            
            return dao.getByQuery("FROM Authority"  + getSort(sortField, sortOrder), filters, first, pageSize);
        }

        // do sorting
        Integer sortFactor = sortOrder.equals(SortOrder.DESCENDING) ? -1 : 1;
        if (sortField.equals("title")) {
            authorities.sort((Authority a, Authority b) -> 
                Helper.getTranslation(a.getTitleWithoutSuffix()).toLowerCase().compareTo(
                    Helper.getTranslation(b.getTitleWithoutSuffix()).toLowerCase()
                ) * sortFactor
            );
        }
        if (sortField.equals("type")) {
            authorities.sort((Authority a, Authority b) -> 
                Helper.getTranslation(a.getType()).compareTo(Helper.getTranslation(b.getType())) * sortFactor
            );
        }

        // extract authorities of current page
        int fromIndex = first;
        int toIndex = Math.min(authorities.size(), first + pageSize);
        return IntStream.range(fromIndex, toIndex).mapToObj(authorities::get).collect(Collectors.toList());
    }

    /**
     * Get authority by title.
     * 
     * @param title
     *            of the searched authority
     * @return matching authority
     */
    public Authority getByTitle(String title) throws DAOException {
        return dao.getByTitle(title);
    }

    /**
     * Gets all authorities which are assignable globally.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableGlobal() throws DAOException {
        return filterAuthorities(getAll(), GLOBAL_AUTHORITY_SUFFIX);
    }

    /**
     * Gets all authorities which are assignable for any client.
     *
     * @return The list of authorities.
     */
    public List<Authority> getAllAssignableToClients() throws DAOException {
        return filterAuthorities(getAll(), CLIENT_AUTHORITY_SUFFIX);
    }

    /**
     * Filters global assignable authorities out of an given list of authorities.
     *
     * @return The list of authorities.
     */
    public List<Authority> filterAssignableGlobal(List<Authority> authoritiesToFilter) {
        return filterAuthorities(authoritiesToFilter, GLOBAL_AUTHORITY_SUFFIX);
    }

    /**
     * Filters client assignable authorities out of an given list of authorities.
     *
     * @return The list of authorities.
     */
    public List<Authority> filterAssignableToClients(List<Authority> authoritiesToFilter) {
        return filterAuthorities(authoritiesToFilter, CLIENT_AUTHORITY_SUFFIX);
    }

    /**
     * Filters a list of authorities by checking if title contains the given filter.
     * 
     * @param authoritiesToFilter
     *            The list of Authorities to filter.
     * @param filter
     *            The filter as String object.
     * @return The filtered list of authorities.
     */
    private List<Authority> filterAuthorities(List<Authority> authoritiesToFilter, String filter) {
        List<Authority> filteredAuthorities = new ArrayList<>();
        for (Authority authority : authoritiesToFilter) {
            if (authority.getTitle().contains(filter)) {
                filteredAuthorities.add(authority);
            }
        }
        return filteredAuthorities;
    }
}

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

package org.kitodo.production.model;

import static java.lang.Math.toIntExact;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.RoleService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class LazyRoleModel extends LazyBeanModel {

    public LazyRoleModel(RoleService roleService) {
        super(roleService);
    }

    /**
     * Get value of 'showRolesOfAllAvailableClients'.
     *
     * @return value of 'showRolesOfAllAvailableClients'
     */
    public boolean isShowRolesOfAllAvailableClients() {
        return ServiceManager.getUserService().getCurrentUser().isShowAllClientsRoles();
    }

    /**
     * Set value of 'showRolesOfAllAvailableClients'.
     *
     * @param showRolesOfAllAvailableClients boolean
     */
    public void setShowRolesOfAllAvailableClients(boolean showRolesOfAllAvailableClients) throws DAOException {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        if (Objects.nonNull(currentUser)) {
            currentUser.setShowAllClientsRoles(showRolesOfAllAvailableClients);
            ServiceManager.getUserService().save(currentUser);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filters) {
        String sortField = "";
        SortOrder sortOrder = SortOrder.ASCENDING;
        if (!sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            sortField = sortMeta.getField();
            sortOrder = sortMeta.getOrder();
        }
        try {
            Map<String, Boolean> filterMap = getFilterMap();
            setRowCount(toIntExact(searchService.countResults(filterMap)));
            entities = ((RoleService) searchService).loadData(first, pageSize, sortField, sortOrder, filterMap);
            return entities;
        } catch (DAOException e) {
            setRowCount(0);
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Boolean> filterMap = getFilterMap();
        try {
            return toIntExact(searchService.countResults(filterMap));
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    private Map<String, Boolean> getFilterMap() {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        Map<String, Boolean> filterMap = Collections.emptyMap();
        if (Objects.nonNull(currentUser)) {
            filterMap = Collections.singletonMap("allClients", currentUser.isShowAllClientsRoles());
        }
        return filterMap;
    }
}

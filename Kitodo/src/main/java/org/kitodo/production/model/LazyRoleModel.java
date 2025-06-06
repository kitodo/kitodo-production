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

import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.data.RoleService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortOrder;

public class LazyRoleModel extends LazyBeanModel {

    private boolean showRolesOfAllAvailableClients = true;

    public LazyRoleModel(RoleService roleService) {
        super(roleService);
    }

    /**
     * Get value of 'showRolesOfAllAvailableClients'.
     *
     * @return value of 'showRolesOfAllAvailableClients'
     */
    public boolean isShowRolesOfAllAvailableClients() {
        return showRolesOfAllAvailableClients;
    }

    /**
     * Set value of 'showRolesOfAllAvailableClients'.
     *
     * @param showRolesOfAllAvailableClients boolean
     */
    public void setShowRolesOfAllAvailableClients(boolean showRolesOfAllAvailableClients) {
        this.showRolesOfAllAvailableClients = showRolesOfAllAvailableClients;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,
            FilterMeta> filters) {
        try {
            Map<String, Boolean> filterMap = Collections.singletonMap("allClients", showRolesOfAllAvailableClients);
            setRowCount(toIntExact(searchService.countResults(filterMap)));
            entities = ((RoleService) searchService).loadData(first, pageSize, sortField, sortOrder, filterMap);
            return entities;
        } catch (DAOException e) {
            setRowCount(0);
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}

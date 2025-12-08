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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.SQLGrammarException;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.UserService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class LazyUserModel extends LazyBeanModel {

    private final UserService userService;

    private final Map<Integer, List<String>> rolesCache = new HashMap<>();
    private final Map<Integer, List<String>> clientsCache = new HashMap<>();
    private final Map<Integer, List<String>> projectsCache = new HashMap<>();
    private final Map<Integer, Boolean> tasksCache = new HashMap<>();

    public LazyUserModel(UserService service) {
        super(service);
        this.userService = service;   // ✔ store real type
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize,
                             Map<String, SortMeta> sortBy,
                             Map<String, FilterMeta> filters) {

        String sortField = "";
        SortOrder sortOrder = SortOrder.ASCENDING;
        if (!sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            sortField = sortMeta.getField();
            sortOrder = sortMeta.getOrder();
        }
        Stopwatch stopwatch = new Stopwatch(this, "load", "first", Integer.toString(first), "pageSize", Integer
                .toString(pageSize), "sortField", sortField, "sortOrder", Objects.toString(sortOrder), "filters",
                Objects.toString(filters));
        try {
            HashMap<String, String> filterMap = new HashMap<>();
            if (!StringUtils.isBlank(this.filterString)) {
                filterMap.put(FilterService.FILTER_STRING, this.filterString);
            }
            setRowCount(toIntExact(searchService.countResults(filterMap)));
            entities = searchService.loadData(first, pageSize, sortField, sortOrder, filterMap);
            rolesCache.clear();
            clientsCache.clear();
            projectsCache.clear();
            tasksCache.clear();
            List<Integer> ids = new ArrayList<>(entities.size());
            for (Object o : entities) {
                ids.add(((User) o).getId());
            }
            rolesCache.putAll(userService.loadRolesForUsers(ids));
            clientsCache.putAll(userService.loadClientsForUsers(ids));
            projectsCache.putAll(userService.loadProjectsForUsers(ids));
            tasksCache.putAll(userService.loadTasksInProgressForUsers(ids));
            logger.info("{} entities loaded!", entities.size());
            return stopwatch.stop(entities);
        } catch (DAOException | SQLGrammarException e) {
            setRowCount(0);
            logger.error(e.getMessage(), e);
        } catch (FilterException e) {
            setRowCount(0);
            PrimeFaces.current().executeScript("PF('sticky-notifications').renderMessage("
                    + "{'summary':'Filter error','detail':'" + e.getMessage() + "','severity':'error'});");
            logger.error(e.getMessage(), e);
        }
        return stopwatch.stop(new LinkedList<>());
    }

    public Map<Integer, List<String>> getRolesCache() {
        return rolesCache;
    }

    public Map<Integer, List<String>> getClientsCache() {
        return clientsCache;
    }

    public Map<Integer, List<String>> getProjectsCache() {
        return projectsCache;
    }

    public Map<Integer, Boolean> getTasksCache() {
        return tasksCache;
    }
}


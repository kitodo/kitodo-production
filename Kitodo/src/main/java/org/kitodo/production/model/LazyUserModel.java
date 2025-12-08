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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private Map<Integer, List<String>> rolesCache = Map.of();
    private Map<Integer, List<String>> clientsCache = Map.of();
    private Map<Integer, List<String>> projectsCache = Map.of();
    private Map<Integer, Boolean> tasksCache = Map.of();

    public LazyUserModel(UserService service) {
        super(service);
        this.userService = service;   // ✔ store real type
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                             Map<String, FilterMeta> filters) {
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
            logger.info("{} entities loaded!", entities.size());
            rolesCache = Map.of();
            clientsCache = Map.of();
            projectsCache = Map.of();
            tasksCache = Map.of();
            List<Integer> ids = (List<Integer>) entities.stream()
                    .map(u -> ((User)u).getId())
                    .collect(Collectors.toList());
            // Load roles for all users in one query
            rolesCache = userService.loadRolesForUsers(ids);
            clientsCache = userService.loadClientsForUsers(ids);
            projectsCache = userService.loadProjectsForUsers(ids);
            tasksCache = userService.loadTasksInProgressForUsers(ids);
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


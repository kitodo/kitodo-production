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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Role;
import org.kitodo.production.enums.ProcessState;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.index.IndexingService;
import org.primefaces.model.SortOrder;

/**
 * Provides programmatic composition of Hibernate queries.
 */
public class BeanQuery {
    private static final Pattern EXPLICIT_ID_SEARCH = Pattern.compile("id:(\\d+)");
    private static final Collection<Integer> NO_HIT = Collections.singletonList(0);
    private static final String JOIN_LAST_TASK = "process.tasks lastTask WITH "
            + "(lastTask.processingBegin IS NOT NULL OR lastTask.processingEnd IS NOT NULL) "
            + "AND (CASE WHEN lastTask.processingBegin IS NOT NULL AND lastTask.processingEnd IS NOT NULL "
            + "THEN CASE WHEN lastTask.processingBegin > lastTask.processingEnd THEN lastTask.processingBegin ELSE lastTask.processingEnd "
            + "END WHEN lastTask.processingBegin IS NOT NULL THEN lastTask.processingBegin "
            + "ELSE lastTask.processingEnd END) = (SELECT MAX(CASE "
            + "WHEN task.processingBegin IS NOT NULL AND task.processingEnd IS NOT NULL "
            + "THEN CASE WHEN task.processingBegin > task.processingEnd THEN task.processingBegin ELSE task.processingEnd END "
            + "WHEN task.processingBegin IS NOT NULL THEN task.processingBegin "
            + "ELSE task.processingEnd END) FROM Task task WHERE task.process = process "
            + "AND (task.processingBegin IS NOT NULL OR task.processingEnd IS NOT NULL))";
    private final FilterService filterService = ServiceManager.getFilterService();
    private final IndexingService indexingService = ServiceManager.getIndexingService();
    private final Class<? extends BaseBean> beanClass;
    private final String className;
    private final String varName;
    private final Collection<String> innerJoins = new ArrayList<>();
    private final Collection<String> leftJoins = new ArrayList<>();
    private final Collection<String> restrictions = new ArrayList<>();
    private final List<String> restrictionAlternatives = new ArrayList<>();
    private boolean indexFiltersAsAlternatives = false;
    private Pair<String, String> sorting = Pair.of("id", "ASC");
    private final Map<String, Pair<FilterField, String>> indexQueries = new HashMap<>();
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Constructor. Creates a new query builder instance.
     * 
     * @param beanClass
     *            class of beans to search for
     */
    public BeanQuery(Class<? extends BaseBean> beanClass) {
        this.beanClass = beanClass;
        className = beanClass.getSimpleName();
        varName = className.toLowerCase();
    }

    /**
     * Requires that the hits in a specific field must have a specific value.
     * 
     * @param fieldName
     *            class field that must have the specified value
     * @param value
     *            value that the field must have
     */
    public void addBooleanRestriction(String fieldName, Boolean value) {
        String joker = varName(fieldName);
        restrictions.add(varName + '.' + fieldName + " = :" + joker);
        parameters.put(joker, value);
    }

    /**
     * Requires that the hits must correspond to any of the specified values in
     * the specified class field.
     * 
     * @param fieldName
     *            class field in which the value must be
     * @param values
     *            value that the class field must accept one of
     */
    public void addInCollectionRestriction(String fieldName, Collection<?> values) {
        String parameterName = varName(fieldName);
        restrictions.add(varName + '.' + fieldName + " IN (:" + parameterName + ')');
        parameters.put(parameterName, values);
    }

    /**
     * Requires that the hits in a specific field must have a specific value.
     * 
     * @param field
     *            field that must have the specified value
     * @param value
     *            value that the field must have
     */
    public void addIntegerRestriction(String field, int value) {
        String parameterName = varName(field);
        restrictions.add(varName + '.' + field + " = :" + parameterName);
        parameters.put(parameterName, value);
    }

    /**
     * Requires that the hits do not correspond to any of the specified values
     * in the specified database field ​​(exclusion).
     * 
     * @param field
     *            field in which the value must not be
     * @param values
     *            value that the field must not accept
     */
    public void addNotInCollectionRestriction(String field, Collection<Integer> values) {
        String parameterName = varName(field);
        restrictions.add(varName + '.' + field + " NOT IN (:" + parameterName + ')');
        parameters.put(parameterName, values);
    }

    /**
     * Requires that the value in the given field is {@code null}.
     * 
     * @param field
     *            field that should be {@code null}
     */
    public void addNullRestriction(String field) {
        restrictions.add(varName + '.' + field + " IS NULL");
    }

    /**
     * Requires that hits in a specific field must contain specific string.
     * 
     * @param field
     *            field that must have the specified string
     * @param value
     *            string that must be contained in the field
     */
    public void addStringRestriction(String field, String value) {
        String parameterName = varName(field);
        restrictions.add(varName + '.' + field + " = :" + parameterName);
        parameters.put(parameterName, value);
    }

    /**
     * Requires that a member with the given ID is in the {@code @ManyToMany}
     * relationship.
     * 
     * @param xField
     *            name of {@code @ManyToMany}-mapped field
     * @param id
     *            ID of required member
     */
    public void addXIdRestriction(String xField, Integer id) {
        String otherName = varName(xField).replaceFirst("s$", "");
        String joker = otherName.concat("Id");
        innerJoins.add(varName + "." + xField + " AS " + otherName + " WITH " + otherName + ".id = :" + joker);
        parameters.put(joker, id);
    }

    /**
     * Requires that the search only finds objects where the user input either
     * matches the record number or is part of the <i>title</i>. Title here
     * means the label. If the input is not a number, the first option is
     * omitted.
     * 
     * @param searchInput
     *            single line input by the user
     */
    public void forIdOrInTitle(String searchInput) {
        if (searchInput.startsWith("\"") && searchInput.endsWith("\"")) {
            searchInput = searchInput.substring(1, searchInput.length() - 1);
        }
        String searchInputAnywhere = '%' + searchInput + '%';
        try {
            Matcher idSearchInput = EXPLICIT_ID_SEARCH.matcher(searchInput);
            if (idSearchInput.matches()) {
                Integer expectedId = Integer.valueOf(idSearchInput.group(1));
                if (className.equals("Task")) {
                    restrictions.add(varName + ".process.id = :id");
                } else {
                    restrictions.add(varName + ".id = :id");
                }
                parameters.put("id", expectedId);
            } else {
                Integer possibleId = Integer.valueOf(searchInput);
                restrictions.add('(' + varName + ".id = :possibleId OR " + varName + ".title LIKE :searchInput)");
                parameters.put("possibleId", possibleId);
                parameters.put("searchInput", searchInputAnywhere);
            }
        } catch (NumberFormatException e) {
            restrictions.add(varName + ".title LIKE :searchInput");
            parameters.put("searchInput", searchInputAnywhere);
        }
    }

    /**
     * Searches the index and inserts the IDs into the HQL query parameters.
     */
    public void performIndexSearches() {
        for (var iterator = indexQueries.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, Pair<FilterField, String>> entry = iterator.next();
            Collection<Integer> ids = indexingService.searchIds(Process.class, entry.getValue().getLeft()
                    .getSearchField(), entry.getValue().getRight());
            parameters.put(entry.getKey(), ids.isEmpty() ? NO_HIT : ids);
            iterator.remove();
        }
    }

    /**
     * Requires that the query only find objects owned by the specified client.
     * 
     * @param sessionClientId
     *            client record number
     */
    public void restrictToClient(int sessionClientId) {
        switch (className) {
            case "Docket":
            case "Project":
            case "Ruleset":
            case "Template":
            case "Workflow":
                restrictions.add(varName + ".client.id = :sessionClientId");
                break;
            case "Process":
                restrictions.add(varName + ".project.client.id = :sessionClientId");
                break;
            case "Task":
                restrictions.add(varName + ".process.project.client.id = :sessionClientId");
                break;
            default:
                throw new IllegalStateException("BeanQuery.restrictToClient() not yet implemented for ".concat(
                    className));
        }
        parameters.put("sessionClientId", sessionClientId);
    }

    /**
     * Requires that the search only finds processes that are not yet completed.
     */
    public void restrictToNotCompletedProcesses() {
        restrictions.add('(' + varName + ".sortHelperStatus IS NULL OR " + varName
                + ".sortHelperStatus != :completedState)");
        parameters.put("completedState", ProcessState.COMPLETED.getValue());
    }

    /**
     * Requires that the search only find items that belong to one of the
     * specified projects.
     * 
     * @param projectIDs
     *            record numbers of the projects to which the hits may belong
     */
    public void restrictToProjects(Collection<Integer> projectIDs) {
        switch (className) {
            case "Process":
                restrictions.add(varName + ".project.id IN (:projectIDs)");
                break;
            case "Task":
                restrictions.add(varName + ".process.project.id IN (:projectIDs)");
                break;
            default:
                throw new IllegalStateException("BeanQuery.restrictToProjects() not yet implemented for ".concat(
                    className));
        }
        parameters.put("projectIDs", projectIDs);
    }

    /**
     * Requires that the search only finds tasks that are allowed to be
     * processed by one of the specified roles.
     * 
     * @param roles
     *            roles of the user
     */
    public void restrictToRoles(List<Role> roles) {
        leftJoins.add("task.roles AS taskRoles");
        restrictions.add("taskRoles IN (:userRoles)");
        parameters.put("userRoles", roles);
    }

    /**
     * Adds search restrictions entered by the user in the filter input.
     * 
     * @param filterString
     *            user input
     * @see "https://github.com/kitodo/kitodo-production/wiki/Suche-und-Filter"
     */
    public void restrictWithUserFilterString(String filterString) {
        int userFilterCount = 0;
        for (var groupFilter : filterService.parse(filterString, beanClass.isAssignableFrom(Process.class))
                .entrySet()) {
            List<String> groupFilters = new ArrayList<>();
            for (UserSpecifiedFilter searchFilter : groupFilter.getValue()) {
                userFilterCount++;
                String parameterName = "userFilter".concat(Integer.toString(userFilterCount));
                if (searchFilter instanceof DatabaseQueryPart) {
                    DatabaseQueryPart databaseSearchQueryPart = (DatabaseQueryPart) searchFilter;
                    String query = databaseSearchQueryPart.getDatabaseQuery(className, varName, parameterName);
                    if (query.contains(" AS ")) {
                        innerJoins.add(query);
                    } else {
                        groupFilters.add(query);
                    }
                    if (!Objects.equals(query, DatabaseQueryPart.SQL_FALSE)) {
                        databaseSearchQueryPart.addParameters(parameterName, parameters);
                    }
                } else {
                    IndexQueryPart indexQueryPart = (IndexQueryPart) searchFilter;
                    indexQueryPart.putQueryParameters(varName, parameterName, (className.equals("Process") ? "id"
                            : "process.id"), indexQueries, indexFiltersAsAlternatives ? restrictionAlternatives
                                    : restrictions);
                }
            }
            if (groupFilters.size() == 1) {
                restrictions.add(groupFilters.get(0));
            } else if (groupFilters.size() > 1) {
                restrictions.add("( " + String.join(" OR ", groupFilters) + " )");
            }
        }
    }

    public void defineSorting(String sortField, SortOrder sortOrder) {
        sorting = Pair.of(sortField.startsWith("lastTask") || sortField.startsWith("CASE") ? sortField
                : varName + '.' + sortField, SortOrder.DESCENDING.equals(sortOrder) ? "DESC" : "ASC");
    }

    /**
     * Sets a flag that multiple index query filters are formed as an OR query.
     */
    public void setIndexFiltersAsAlternatives() {
        this.indexFiltersAsAlternatives = true;
    }

    /**
     * Disables the sort order of the query. This can speed up the check whether
     * there is <i>any</i> object for a specific search query. However, you no
     * longer have a reliable order when navigating the result list, and should
     * therefore only be used in special cases.
     */
    public void setUnordered() {
        sorting = null;
    }

    /**
     * Forms and returns a query to count all objects.
     * 
     * @return a query to count all objects
     */
    public String formCountQuery() {
        StringBuilder query = new StringBuilder(512);
        query.append("SELECT COUNT(*) ");
        innerFormQuery(query);
        return query.toString();
    }

    /**
     * Forms and returns a query for all objects.
     * 
     * @return a query for all objects
     */
    public String formQueryForAll() {
        boolean sorted = Objects.nonNull(sorting);
        StringBuilder query = new StringBuilder(512);
        if (!innerJoins.isEmpty()) {
            query.append("SELECT ").append(varName).append(' ');
        }
        if (sorted && sorting.getKey().startsWith("lastTask")) {
            leftJoins.add(JOIN_LAST_TASK);
        }
        innerFormQuery(query);
        if (sorted) {
            query.append(" ORDER BY ").append(sorting.getKey()).append(' ').append(sorting.getValue());
        }
        return query.toString();
    }

    /**
     * Forms and returns a query for a unique collection of strings.
     * 
     * @param field
     *            field of the bean being queried
     * @param sorted
     *            whether the list should be sorted
     */
    public String formQueryForDistinct(String field, boolean sorted) {
        StringBuilder query = new StringBuilder(512);
        query.append("SELECT DISTINCT ").append(varName).append('.').append(field).append(' ');
        innerFormQuery(query);
        if (sorted) {
            query.append(" ORDER BY ").append(varName).append('.').append(field).append(" ASC");
        }
        return query.toString();
    }

    private void innerFormQuery(StringBuilder query) {
        query.append("FROM ").append(className).append(" AS ").append(varName);
        for (String innerJoin : innerJoins) {
            query.append(" INNER JOIN ").append(innerJoin);
        }
        for (String leftJoin : leftJoins) {
            query.append(" LEFT JOIN ").append(leftJoin);
        }
        if (restrictionAlternatives.size() == 1) {
            restrictions.add(restrictionAlternatives.get(0));
        } else if (restrictionAlternatives.size() > 1) {
            restrictions.add(restrictionAlternatives.stream().collect(Collectors.joining(" OR ", "(", ")")));
        }
        restrictionAlternatives.clear();
        if (!restrictions.isEmpty()) {
            boolean first = true;
            for (String restriction : restrictions) {
                query.append(first ? " WHERE " : " AND ").append(restriction);
                first = false;
            }
        }
    }

    /**
     * Returns the query parameters.
     * 
     * @return the query parameters
     * @throws IllegalStateException
     *             if index queries still need to be made for parameterization
     */
    public Map<String, Object> getQueryParameters() {
        if (!indexQueries.isEmpty()) {
            throw new IllegalStateException("index searches not yet performed");
        }
        return parameters;
    }

    private String varName(String input) {
        StringBuilder result = new StringBuilder();
        CharacterIterator inputIterator = new StringCharacterIterator(input);
        boolean upperCase = false;
        while (inputIterator.current() != CharacterIterator.DONE) {
            char currentChar = inputIterator.current();
            if (currentChar < '0' || (currentChar > '9' && currentChar < 'A') || (currentChar > 'Z'
                    && currentChar < 'a') || currentChar > 'z') {
                upperCase = true;
            } else {
                result.append(upperCase ? Character.toUpperCase(currentChar) : Character.toLowerCase(currentChar));
                upperCase = false;
            }
            inputIterator.next();
        }
        return result.toString();
    }
}

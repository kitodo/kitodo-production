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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.FilterDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.DataInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.production.enums.FilterString;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseFilterServiceInterface;
import org.primefaces.model.SortOrder;

/**
 * Service for Filter bean.
 */
public class FilterService extends SearchDatabaseService<Filter, FilterDAO>
        implements DatabaseFilterServiceInterface {

    private static final Logger logger = LogManager.getLogger(FilterService.class);
    private static volatile FilterService instance = null;
    
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\(([^\\)]+)\\)|([^\\(\\)\\|]+)");
    public static final String FILTER_STRING = "filterString";

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private FilterService() {
        super(new FilterDAO());
    }

    /**
     * Return singleton variable of type FilterService.
     *
     * @return unique instance of FilterService
     */
    public static FilterService getInstance() {
        FilterService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (FilterService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new FilterService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Filter");
    }

    // functions countResults() and loadData() are not used in filters
    @Override
    public Long countResults(Map filters) throws DataException {
        return (long) 0;
    }

    @Override
    public List<Filter> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Splits a filter into multiple alternative conditions.
     * 
     * @param filter the filter string (that was enclosed in double quotes)
     * @return a list of conditions after splitting the filter at the "|" character
     */
    private List<String> splitConditions(String filter) {
        return CONDITION_PATTERN.matcher(filter).results()
            .flatMap(mr -> IntStream.rangeClosed(1, mr.groupCount()).mapToObj(mr::group))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.toList());
    }

    private String replaceLegacyFilters(String filter) {
        filter = filter.replace("processproperty","property");
        filter = filter.replace("workpiece","property");
        filter = filter.replace("template","property");
        return filter;
    }

    Set<Integer> collectIds(List<? extends DataInterface> dtos) {
        Set<Integer> ids = new HashSet<>();
        for (DataInterface process : dtos) {
            ids.add(process.getId());
        }
        return ids;
    }

    /**
     * Get value for find objects in ElasticSearch.
     *
     * @param filter
     *            as String eg. in form 'stepdone:1'
     * @param filterString
     *            as FilterString eg. 'stepdone:'
     * @return value for find object '1'
     */
    private String getFilterValueFromFilterString(String filter, FilterString filterString) {
        String filterEnglish = filterString.getFilterEnglish();
        String filterGerman = filterString.getFilterGerman();
        if (filter.contains(filterEnglish)) {
            return prepareStrings(filter, filterEnglish).get(0);
        } else if (filter.contains(filterGerman)) {
            return prepareStrings(filter, filterGerman).get(0);
        }
        return "";
    }

    /**
     * Get list of values for find objects in ElasticSearch.
     *
     * @param filter
     *            as String eg. in form 'stepdone:1 2 3'
     * @param filterString
     *            as FilterString e.g. 'stepdone:'
     * @return list of values for find objects e.g. '1' '2' and so on
     */
    private List<String> getFilterValuesFromFilterString(String filter, FilterString filterString) {
        String filterEnglish = filterString.getFilterEnglish();
        String filterGerman = filterString.getFilterGerman();
        List<String> filterValues = new ArrayList<>();
        if (filter.contains(filterEnglish)) {
            filterValues = prepareStrings(filter, filterEnglish);
        } else if (filter.contains(filterGerman)) {
            filterValues = prepareStrings(filter, filterGerman);
        }
        return filterValues;
    }

    /**
     * Prepare list of values for given filter. Regexp checks if it contains
     * only numbers and white spaces. In that case it treats it as list of ids.
     * If value contains words and white spaces or single word it treats it as
     * text search.
     *
     * @param filter
     *            full filter String
     * @param filterName
     *            String which contains only name of filter e.g. 'stepdone:'
     * @return list of values, in case if string this list has size one
     */
    private List<String> prepareStrings(String filter, String filterName) {
        List<String> filterValues = new ArrayList<>();
        String filterValue = filter.substring(filter.indexOf(filterName));
        filterValue = filterValue.substring(filterName.lastIndexOf(':') + 1);
        if (filterValue.matches("^[\\s\\d]+$")) {
            filterValues.addAll(Arrays.asList(filterValue.split("\\s+")));
        } else {
            filterValues.add(filterValue);
        }
        return filterValues;
    }

    /**
     * Filters for properties are special type. They can contain two times :
     * e.g. 'processproperty:title:value'.
     *
     * @param filter
     *            full filter String
     * @param filterString
     *            contains only name of filter e.g. 'processproperty:' as String
     * @return list of values in format property title and property value or
     *         only property value
     */
    private List<String> getFilterValueFromFilterStringForProperty(String filter, FilterString filterString) {
        List<String> titleValue = new ArrayList<>();
        String filterEnglish = filterString.getFilterEnglish();
        String filterGerman = filterString.getFilterGerman();
        if (filter.contains(filterEnglish)) {
            titleValue = prepareStringsForProperty(filter, filterEnglish);
        } else if (filter.contains(filterGerman)) {
            titleValue = prepareStringsForProperty(filter, filterGerman);
        }
        return titleValue;
    }

    /**
     * Prepare list of values in format property title and property value or
     * only property value.
     *
     * @param filter
     *            full filter String
     * @param filterName
     *            contains only name of filter e.g. 'processproperty:' as String
     * @return list of values in format property title and property value or
     *         only property value.
     */
    private List<String> prepareStringsForProperty(String filter, String filterName) {
        List<String> titleValue = new ArrayList<>();
        String filterValue = filter.replace(filterName, "");
        if (filterValue.contains(":")) {
            titleValue.add(filterValue.substring(0, filterValue.lastIndexOf(':')));
            titleValue.add(filterValue.substring(filterValue.lastIndexOf(':') + 1));
        } else {
            titleValue.add(filterValue);
        }
        return titleValue;
    }

    /**
     * Prepare list of single filters from given one long filter. Filters are
     * delimited by ".
     *
     * @param filter
     *            as String
     * @return list of single filters
     */
    private List<String> splitFilters(String filter) {
        List<String> filters = new ArrayList<>();
        String delimiter = "\"";
        StringTokenizer tokenizer = new StringTokenizer(filter, delimiter, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.equals(delimiter) && !token.equals(" ")) {
                filters.add(token);
            }
        }
        return filters;
    }

    /**
     * Evaluate FilterString objects in both possible languages.
     *
     * @param stringFilterString
     *            full filter String
     * @param filterString
     *            as FilterString object
     * @param prefix
     *            possible prefix is '-', if prefix not null it means that we
     *            are filtering for negated value
     * @return true or false
     */
    private boolean evaluateFilterString(String stringFilterString, FilterString filterString, String prefix) {
        String lowerCaseFilterString = stringFilterString.toLowerCase();
        if (Objects.nonNull(prefix)) {
            return lowerCaseFilterString.startsWith(prefix + filterString.getFilterEnglish())
                    || lowerCaseFilterString.startsWith(prefix + filterString.getFilterGerman());
        }
        return lowerCaseFilterString.startsWith(filterString.getFilterEnglish())
                || lowerCaseFilterString.startsWith(filterString.getFilterGerman());
    }

    private void logError(String filter) {
        logger.error("filter part '{}' in '{}' caused an error", filter.substring(filter.indexOf(':') + 1), filter);
    }

    /**
     * This function analyzes the parameters on a task filter and returns a
     * TaskFilter enum to direct further processing it reduces the necessity to
     * apply some filter keywords.
     *
     * @param parameters
     *            String
     * @return TaskFilter
     */
    private static TaskFilter getTaskFilter(String parameters) {

        if (parameters.contains("-")) {
            String[] strArray = parameters.split("-");
            if (Arrays.stream(strArray).allMatch(StringUtils::isNumeric)) {
                if (strArray.length >= 2) {
                    if (strArray[0].length() == 0) {
                        return TaskFilter.MAX;
                    } else {
                        return TaskFilter.RANGE;
                    }
                } else {
                    return TaskFilter.MIN;
                }
            } else {
                return TaskFilter.NAME;
            }
        } else if (!parameters.isEmpty() && StringUtils.isNumeric(parameters)) {
            return TaskFilter.EXACT;
        } else {
            return TaskFilter.NAME;
        }
    }

    /**
     * This enum represents the result of parsing the step&lt;modifier&gt;:
     * filter Restrictions.
     */
    private enum TaskFilter {
        EXACT,
        RANGE,
        MIN,
        MAX,
        NAME,
        UNKNOWN
    }

    /**
     * Filter processes by ids.
     *
     * @param filter
     *            part of filter string to use
     * @param filterString
     *            as FilterString
     * @return set of ids as Integers
     */
    private Set<Integer> filterValuesAsIntegers(String filter, FilterString filterString) {
        Set<Integer> ids = new HashSet<>();
        List<String> stringIds = getFilterValuesFromFilterString(filter, filterString);
        for (String tempId : stringIds) {
            if (!tempId.isEmpty() && StringUtils.isNumeric(tempId)) {
                Integer id = Integer.parseInt(tempId);
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Filter processes by Ids.
     *
     * @param filter
     *            part of filter string to use
     * @param filterString
     *            as FilterString
     * @return set of values as Strings
     */
    private Set<String> filterValuesAsStrings(String filter, FilterString filterString) {
        Set<String> ids = new HashSet<>();
        List<String> stringIds = getFilterValuesFromFilterString(filter, filterString);
        if (!stringIds.isEmpty()) {
            ids.addAll(stringIds);
        }
        return ids;
    }

    /**
     * This functions extracts the Integer from the parameters passed with the
     * step filter in first position.
     *
     * @param parameter
     *            the string, where the integer should be extracted
     * @return Integer
     */
    private Integer getTaskStart(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[0]);
    }

    /**
     * This functions extracts the Integer from the parameters passed with the
     * step filter in last position.
     *
     * @param parameter
     *            String
     * @return Integer
     */
    private Integer getTaskEnd(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[1]);
    }

    /**
     * Checks the given map for an entry with the key 'FILTER_STRING' and parses the corresponding value of the
     * entry as a filter.
     *
     * @param filters
     *      a Map containing a filter String
     * @return
     *      the only Entry's value as java.lang.String
     */
    String parseFilterString(Map<?, ?> filters) {
        if (Objects.nonNull(filters) && filters.containsKey(FILTER_STRING)
                && filters.get(FILTER_STRING) instanceof String) {
            return (String) filters.get(FILTER_STRING);
        }
        return "";
    }

    /**
     * Parse Map 'filters' and create a map containing filter fields as
     * keys and filter values as values.
     *
     * @param filters Map containing filterString to parse
     * @return HashMap containing filter fields as keys and filter values as values
     */
    public HashMap<String, Object> getSQLFilterMap(Map<?, ?> filters, Class<?> baseClass) throws NoSuchFieldException {
        HashMap<String, Object> filterMap = new HashMap<>();
        List<String> declaredFields = Arrays.stream(baseClass.getDeclaredFields()).map(Field::getName)
                .collect(Collectors.toList());
        for (String filter : splitFilters(parseFilterString(filters))) {
            String[] filterComponents = filter.split(":");
            if (filterComponents.length == 2) {
                String parameterName = filterComponents[0].trim();
                String parameterValue = filterComponents[1].trim();
                if (declaredFields.contains(parameterName)) {
                    // class contains parameter as column
                    filterMap.put(parameterName, parseFilterValue(baseClass.getDeclaredField(parameterName).getType(),
                            parameterValue));
                } else {
                    // otherwise check if parent class contains parameter as column
                    if (Objects.nonNull(baseClass.getSuperclass())) {
                        filterMap.put(parameterName, parseFilterValue(baseClass.getSuperclass()
                                .getDeclaredField(parameterName).getType(), parameterValue));
                    }
                }
            }
        }
        return filterMap;
    }

    private Object parseFilterValue(Class<?> clazz, String parameterValue) {
        if (Objects.equals(clazz.getSuperclass(), Number.class)) {
            return Integer.parseInt(parameterValue);
        } else if (Objects.equals(clazz, boolean.class)) {
            return Boolean.parseBoolean(parameterValue);
        } else {
            return parameterValue;
        }
    }

    /**
     * Create and return an SQL statement to filter users.
     *
     * @param filters
     *          as Set of filter Strings
     * @return SQL statement to filter users
     */
    public String mapToSQLFilterString(Set<String> filters) {
        StringBuilder sqlUserFilter = new StringBuilder();
        for (String filter : filters) {
            sqlUserFilter.append(" AND ").append(filter).append(" = :").append(filter);
        }
        return sqlUserFilter.toString();
    }

    /**
     * Initialise list of process property titles.
     *
     * @return List of String objects containing the process property labels.
     */
    public List<String> initProcessPropertyTitles() {
        return ServiceManager.getPropertyService().findDistinctTitles();
    }

    /**
     * Initialise list of projects.
     *
     * @return List of String objects containing the project
     */
    public List<String> initProjects() {
        List<Project> projectsSortedByTitle = Collections.emptyList();
        try {
            projectsSortedByTitle = ServiceManager.getProjectService().findAllProjectsForCurrentUser();
        } catch (DataException e) {
            Helper.setErrorMessage("errorInitializingProjects", logger, e);
        }

        return projectsSortedByTitle.stream().map(ProjectInterface::getTitle).sorted().collect(Collectors.toList());
    }

    /**
     * Initialise list of step statuses.
     *
     * @return List of TaskStatus objects
     */
    public List<TaskStatus> initStepStatus() {
        return List.of(TaskStatus.values());
    }

    /**
     * Initialise list of task titles.
     *
     * @return List of String objects containing the titles of all workflow steps
     */
    public List<String> initStepTitles() {
        List<String> taskTitles = new ArrayList<>();
        try {
            taskTitles = ServiceManager.getTaskService().findTaskTitlesDistinct();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return taskTitles;
    }


    /**
     * Initialise list of users.
     *
     * @return List of User objects
     */
    public List<User> initUserList() {
        try {
            return ServiceManager.getUserService().getAllActiveUsersSortedByNameAndSurname();
        } catch (RuntimeException e) {
            logger.warn("RuntimeException caught. List of users could be empty!");
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("activeUsers") }, logger, e);
        }
        return new ArrayList<>();
    }
}

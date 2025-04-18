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
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.FilterDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.FilterType;
import org.kitodo.data.elasticsearch.index.type.enums.FilterTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BaseDTO;
import org.kitodo.production.dto.FilterDTO;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.enums.FilterString;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchService;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.primefaces.model.SortOrder;

/**
 * Service for Filter bean.
 */
public class FilterService extends SearchService<Filter, FilterDTO, FilterDAO> {

    private static final Logger logger = LogManager.getLogger(FilterService.class);
    private static volatile FilterService instance = null;
    
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\(([^\\)]+)\\)|([^\\(\\)\\|]+)");
    public static final String FILTER_STRING = "filterString";

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private FilterService() {
        super(new FilterDAO(), new FilterType(), new Indexer<>(Filter.class), new Searcher(Filter.class));
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

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Filter WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(QueryBuilders.matchAllQuery());
    }

    @Override
    public List<Filter> getAllNotIndexed() {
        return getByQuery("FROM Filter WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Filter> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Find filters with exact value.
     *
     * @param value
     *            of the searched filter
     * @param contains
     *            of the searched filter
     * @return list of JSON objects with properties
     */
    List<Map<String, Object>> findByValue(String value, boolean contains) throws DataException {
        QueryBuilder query = createSimpleQuery(FilterTypeField.VALUE.getKey(), value, contains, Operator.AND);
        return findDocuments(query);
    }

    @Override
    public FilterDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setId(getIdFromJSONObject(jsonObject));
        filterDTO.setValue(FilterTypeField.VALUE.getStringValue(jsonObject));
        return filterDTO;
    }

    /**
     * This method builds a criteria depending on a filter string and some other
     * parameters passed on along the initial criteria. The filter is parsed and
     * depending on which data structures are used for applying filtering
     * restrictions conjunctions are formed and collect the restrictions and
     * then will be applied on the corresponding criteria. A criteria is only
     * added if needed for the presence of filters applying to it. 
     * 
     * <p>Filters are enclosed in double quotes and separated via a space. 
     * Each filter can be a disjunction of conditions separated by a "|". 
     * Conditions can be negated by adding the prefix "-".</p>
     * 
     * <p>Some examples are: the default filter "word", which filters task or 
     * processes by their title; a filter "stepinwork:Scanning", which filters 
     * processes or tasks by the task state "Scanning" which are also currently in 
     * progress. The negation thereof would be "-stepinwork:Scanning". A disjunction 
     * of conditions would be "stepinwork:Scanning | stepinwork:QC".</p>
     *
     * @param filters
     *            as String
     * @param objectType
     *            as ObjectType - "PROCESS", "TEMPLATE" or "TASK"
     * @param onlyOpenTasks
     *            as Boolean
     * @param onlyUserAssignedTasks
     *            as Boolean
     * @return query as {@link BoolQueryBuilder}
     */
    public BoolQueryBuilder queryBuilder(String filters, ObjectType objectType, Boolean onlyOpenTasks,
            Boolean onlyUserAssignedTasks) throws DataException {

        filters = replaceLegacyFilters(filters);
        BoolQueryBuilder query = new BoolQueryBuilder();

        // this is needed if we filter task
        if (objectType == ObjectType.TASK) {
            query = buildTaskQuery(onlyOpenTasks, onlyUserAssignedTasks);
        }

        for (String filter : splitFilters(filters)) {
            BoolQueryBuilder bool = new BoolQueryBuilder();
            for (String condition : splitConditions(filter)) {
                boolean negated = condition.startsWith("-");
                if (negated) {
                    bool.should(new BoolQueryBuilder().mustNot(
                        buildQueryFromCondition(condition.substring(1), objectType))
                    );
                } else {
                    bool.should(buildQueryFromCondition(condition, objectType));
                }
            }
            query.must(bool);
        }
        return query;
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

    /**
     * Builds a ElasticSearch query from a single condition.
     * 
     * @param condition the condition (e.g. a single word, or a pair of "property:value")
     * @param objectType the object type that is being filtered (either task or process)
     * @return a elastic search query builder object representing the condition
     */
    private QueryBuilder buildQueryFromCondition(String condition, ObjectType objectType) throws DataException {
        if (evaluateFilterString(condition, FilterString.TASK, null)) {
            return createHistoricFilter(condition);
        } else if (evaluateFilterString(condition, FilterString.TASKINWORK, null)) {
            return createTaskFilters(condition, FilterString.TASKINWORK, TaskStatus.INWORK, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKLOCKED, null)) {
            return createTaskFilters(condition, FilterString.TASKLOCKED, TaskStatus.LOCKED, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKOPEN, null)) {
            return createTaskFilters(condition, FilterString.TASKOPEN, TaskStatus.OPEN, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKDONE, null)) {
            return createTaskFilters(condition, FilterString.TASKDONE, TaskStatus.DONE, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKDONETITLE, null)) {
            String taskTitle = getFilterValueFromFilterString(condition, FilterString.TASKDONETITLE);
            return filterTaskTitle(taskTitle, TaskStatus.DONE, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKDONEUSER, null)
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.WITH_USER_STEP_DONE_SEARCH)) {
            return filterTaskDoneUser(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.TASKAUTOMATIC, null)) {
            return filterAutomaticTasks(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.PROJECT, null)) {
            return filterProject(condition, false, objectType);
        } else if (evaluateFilterString(condition, FilterString.ID, null)) {
            return createProcessIdFilter(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.PARENTPROCESSID, null)) {
            return createParentProcessIdFilter(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.PROPERTY, null)) {
            return createProcessPropertyFilter(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.PROCESS, null)) {
            return createProcessTitleFilter(condition, objectType);
        } else if (evaluateFilterString(condition, FilterString.BATCH, null)) {
            return createBatchIdFilter(condition, objectType, true);
        } else {
            /* standard-search parameter */
            return createDefaultQuery(condition, false, objectType);
        }
    }

    private String replaceLegacyFilters(String filter) {
        filter = filter.replace("processproperty","property");
        filter = filter.replace("workpiece","property");
        filter = filter.replace("template","property");
        return filter;
    }

    private BoolQueryBuilder buildTaskQuery(Boolean onlyOpenTasks, Boolean onlyUserAssignedTasks) {
        return limitToUserAssignedTasks(onlyOpenTasks, onlyUserAssignedTasks);
    }

    Set<Integer> collectIds(List<? extends BaseDTO> dtos) {
        Set<Integer> ids = new HashSet<>();
        for (BaseDTO processDTO : dtos) {
            ids.add(processDTO.getId());
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

    /**
     * Show only open tasks or those in use by current user.
     *
     * @param onlyOpenTask
     *            filter only by open tasks - true/false
     * @param onlyUserAssignedTask
     *            filter only open tasks - true/false
     * @return query as {@link BoolQueryBuilder}
     */
    private BoolQueryBuilder limitToUserAssignedTasks(Boolean onlyOpenTask, Boolean onlyUserAssignedTask) {
        // identify current user
        User user = ServiceManager.getUserService().getCurrentUser();

        // hits by user groups
        BoolQueryBuilder taskQuery = new BoolQueryBuilder();

        if (onlyOpenTask) {
            taskQuery.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), 1, true));
        } else if (onlyUserAssignedTask) {
            taskQuery.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), 1, true));
            taskQuery.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), user.getId(), true));
        } else {
            BoolQueryBuilder processingStatus = new BoolQueryBuilder();
            processingStatus.should(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), 1, true));
            processingStatus.should(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), 2, true));
            taskQuery.must(processingStatus);
        }

        // ignore template tasks
        taskQuery.mustNot(createSimpleQuery(TaskTypeField.PROCESS_ID.getKey(),(Integer) null, true));

        // only tasks assigned to the user groups the current user is member of
        List<Role> userRoles = user.getRoles();
        taskQuery.must(createSetQueryForBeans(TaskTypeField.ROLES + ".id", userRoles, true));

        return taskQuery;
    }

    /**
     * //TODO: why it is called historic filter? Create historic filer.
     *
     * @param filterPart
     *            String
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder createHistoricFilter(String filterPart) {
        /* filtering by a certain minimal status */
        int taskOrdering = 1;
        BoolQueryBuilder historicFilter = new BoolQueryBuilder();
        String taskTitle = getFilterValueFromFilterString(filterPart, FilterString.TASK);
        if (Objects.nonNull(taskTitle)) {
            try {
                taskOrdering = Integer.parseInt(taskTitle);
            } catch (NumberFormatException e) {
                taskTitle = filterPart.substring(filterPart.indexOf(':') + 1);
                historicFilter.must(createSimpleCompareQuery(TaskTypeField.PROCESSING_STATUS.getKey(),
                    TaskStatus.OPEN.getValue(), SearchCondition.EQUAL_OR_BIGGER));
                if (taskTitle.startsWith("-")) {
                    taskTitle = taskTitle.substring(1);
                    historicFilter.mustNot(createSimpleWildcardQuery(TaskTypeField.TITLE.getKey(), taskTitle));
                } else {
                    historicFilter.must(createSimpleWildcardQuery(TaskTypeField.TITLE.getKey(), taskTitle));
                }
            }
        }
        historicFilter.must(createSimpleQuery(TaskTypeField.ORDERING.getKey(), taskOrdering, true));
        return historicFilter;
    }

    private QueryBuilder createParentProcessIdFilter(String filter, ObjectType objectType) {
        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("parent.id", filterValuesAsIntegers(filter, FilterString.PARENTPROCESSID), true);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createProcessIdFilter(String filter, ObjectType objectType) {
        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("_id", filterValuesAsStrings(filter, FilterString.ID), true);
        } else if (objectType == ObjectType.TASK) {
            return createSetQuery(TaskTypeField.PROCESS_ID.getKey(), filterValuesAsIntegers(filter, FilterString.ID),
                true);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createProcessPropertyFilter(String filter, ObjectType objectType) throws DataException {
        BoolQueryBuilder propertyQuery = new BoolQueryBuilder();
        Set<String> strings = filterValuesAsStrings(filter, FilterString.PROPERTY);
        for (String string : strings) {
            String[] split = string.split(":");
            if (split.length > 1) {
                propertyQuery.should(ServiceManager.getProcessService().createPropertyQuery(split[0], split[1]));
            }
        }
        if (objectType == ObjectType.PROCESS) {
            return propertyQuery;
        } else if (objectType == ObjectType.TASK) {
            return getQueryAccordingToObjectTypeAndSearchInObject(ObjectType.TASK, ObjectType.PROCESS, propertyQuery);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createProcessTitleFilter(String filter, ObjectType objectType) {
        String processTitle = getFilterValueFromFilterString(filter, FilterString.PROCESS);
        if (objectType == ObjectType.PROCESS) {
            return ServiceManager.getProcessService().getWildcardQueryTitle(processTitle);
        } else if (objectType == ObjectType.TASK) {
            return createSimpleQuery(TaskTypeField.PROCESS_TITLE.getKey(), processTitle, true, Operator.AND);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createBatchIdFilter(String filter, ObjectType objectType, boolean negate) throws DataException {
        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("batches.id", filterValuesAsIntegers(filter, FilterString.BATCH), negate);
        } else if (objectType == ObjectType.TASK) {
            List<ProcessDTO> processDTOS = ServiceManager.getProcessService().findByQuery(
                createSetQuery("batches.id", filterValuesAsIntegers(filter, FilterString.BATCH), negate), true);
            return createSetQuery(TaskTypeField.PROCESS_ID.getKey(), collectIds(processDTOS), negate);
        }
        return new BoolQueryBuilder();
    }

    /**
     * Create task filters.
     *
     * @param filter
     *            String
     * @param filterString
     *            as {@link FilterString}
     * @param taskStatus
     *            {@link TaskStatus} of searched step
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder createTaskFilters(String filter, FilterString filterString, TaskStatus taskStatus,
            boolean negate, ObjectType objectType) {
        /*
         * extracting the substring into parameter (filter parameters e.g. 5,
         * -5, 5-10, 5- or "Qualitätssicherung")
         */
        String parameters = getFilterValueFromFilterString(filter, filterString);

        /*
         * Analyzing the parameters and what user intended (5->exact, -5 ->max,
         * 5-10 ->range, 5- ->min., Qualitätssicherung ->name) handling the
         * filter according to the parameters
         */
        switch (getTaskFilter(parameters)) {
            case EXACT:
                try {
                    return filterTaskExact(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e.getMessage(), e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (DataException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    logError(filter);
                }
                break;
            case MAX:
                try {
                    return filterTaskMax(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e.getMessage(), e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (DataException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    logError(filter);
                }
                break;
            case MIN:
                try {
                    return filterTaskMin(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e.getMessage(), e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (DataException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    logError(filter);
                }
                break;
            case NAME:
                /* filter for a specific done step by it's name (title) */
                try {
                    return filterTaskTitle(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e.getMessage(), e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (DataException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    logError(filter);
                }
                break;
            case RANGE:
                try {
                    return filterTaskRange(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e.getMessage(), e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (NumberFormatException e) {
                    logger.debug(e.getMessage(), e);
                    try {
                        return filterTaskTitle(parameters, taskStatus, negate, objectType);
                    } catch (NullPointerException e1) {
                        logger.error(e1.getMessage(), e1);
                        logger.error("stepdone is preset, don't use 'step' filters");
                    } catch (DataException | RuntimeException e1) {
                        logger.error(e1.getMessage(), e1);
                        logError(filter);
                    }
                } catch (DataException | RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    logError(filter);
                }
                break;
            case UNKNOWN:
                logger.info("Filter '{}' is not known!", filter);
                break;
            default:
                break;
        }
        return new BoolQueryBuilder();
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
     * Filter processes for done steps range.
     *
     * @param parameters
     *            String
     * @param taskStatus
     *            {@link TaskStatus} of searched step
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskRange(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskRange = new BoolQueryBuilder();
        if (!negate) {
            taskRange.must(createSimpleCompareQuery(TaskTypeField.ORDERING.getKey(), getTaskStart(parameters),
                SearchCondition.EQUAL_OR_BIGGER));
            taskRange.must(createSimpleCompareQuery(TaskTypeField.ORDERING.getKey(), getTaskEnd(parameters),
                SearchCondition.EQUAL_OR_SMALLER));
            taskRange.must(createSimpleCompareQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(),
                SearchCondition.EQUAL));
        } else {
            taskRange.mustNot(createSimpleCompareQuery(TaskTypeField.ORDERING.getKey(), getTaskStart(parameters),
                SearchCondition.EQUAL_OR_BIGGER));
            taskRange.mustNot(createSimpleCompareQuery(TaskTypeField.ORDERING.getKey(), getTaskEnd(parameters),
                SearchCondition.EQUAL_OR_SMALLER));
            taskRange.mustNot(createSimpleCompareQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(),
                SearchCondition.EQUAL));
        }
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, taskRange);
    }

    /**
     * Filter processes for steps name with given status.
     *
     * @param taskStatus
     *            {@link TaskStatus} of searched step
     * @param parameters
     *            part of filter string to use
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskTitle(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskTitle = new BoolQueryBuilder();
        taskTitle.must(createSimpleQuery(TaskTypeField.TITLE.getKey() + ".keyword", parameters, !negate));
        taskTitle.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), !negate));
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, taskTitle);
    }

    /**
     * Filter processes for done steps min.
     *
     * @param parameters
     *            part of filter string to use
     * @param taskStatus
     *            {@link TaskStatus} of searched step
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskMin(String parameters, TaskStatus taskStatus, boolean negate, ObjectType objectType)
            throws DataException {
        BoolQueryBuilder taskMin = new BoolQueryBuilder();
        taskMin.must(createSimpleQuery(TaskTypeField.ORDERING.getKey(), getTaskStart(parameters), !negate));
        taskMin.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), !negate));
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, taskMin);
    }

    /**
     * Filter processes for done tasks max.
     *
     * @param parameters
     *            part of filter string to use
     * @param taskStatus
     *            {@link TaskStatus} of searched task
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskMax(String parameters, TaskStatus taskStatus, boolean negate, ObjectType objectType)
            throws DataException {
        BoolQueryBuilder taskMax = new BoolQueryBuilder();
        taskMax.must(createSimpleQuery(TaskTypeField.ORDERING.getKey(), getTaskEnd(parameters), !negate));
        taskMax.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), !negate));
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, taskMax);
    }

    /**
     * Filter processes for done tasks exact.
     *
     * @param parameters
     *            part of filter string to use
     * @param taskStatus
     *            {@link TaskStatus} of searched task
     * @param negate
     *            true or false, if true create simple queries with contains
     *            false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskExact(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskExact = new BoolQueryBuilder();
        taskExact.must(createSimpleQuery(TaskTypeField.ORDERING.getKey(), getTaskStart(parameters), !negate));
        taskExact.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), !negate));
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, taskExact);
    }

    /**
     * Filter processes for done tasks by user.
     *
     * @param filter
     *            part of filter string to use - for user it looks it is login
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskDoneUser(String filter, ObjectType objectType) {
        /*
         * filtering by a certain done step, which the current user finished
         */
        /*List<TaskDTO> taskDTOS = new ArrayList<>();
        String login = getFilterValueFromFilterString(filter, FilterString.TASKDONEUSER);
        try {
            Map<String, Object> user = ServiceManager.getUserService().findByLogin(login);
            UserDTO userDTO = ServiceManager.getUserService().convertJSONObjectToDTO(user, false);
            taskDTOS = userDTO.getProcessingTasks();
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("tasks.id", collectIds(taskDTOS), true);
        } else if (objectType == ObjectType.TASK) {
            return createSetQuery("_id", collectIds(taskDTOS), true);
        }*/
        return new BoolQueryBuilder();
    }

    /**
     * Filter processes for tasks name with given status.
     *
     * @param filter
     *            as String
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterAutomaticTasks(String filter, ObjectType objectType) throws DataException {
        BoolQueryBuilder typeAutomatic = new BoolQueryBuilder();
        String value = getFilterValueFromFilterString(filter, FilterString.TASKAUTOMATIC);
        if (Objects.nonNull(value)) {
            typeAutomatic.must(
                createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), value.equalsIgnoreCase("true"), true));
        }
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.TASK, typeAutomatic);
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
     * Filter processes by project.
     *
     * @param filter
     *            part of filter string to use
     * @param negate
     *            true or false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterProject(String filter, boolean negate, ObjectType objectType) throws DataException {
        // filter according to linked project
        String projectTitle = getFilterValueFromFilterString(filter, FilterString.PROJECT);
        QueryBuilder projectQuery = ServiceManager.getProcessService().getQueryProjectTitle(projectTitle);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, projectQuery);
    }

    private QueryBuilder createDefaultQuery(String filter, boolean negate, ObjectType objectType) throws DataException {
        QueryBuilder titleQuery = ServiceManager.getProcessService().getWildcardQueryTitle(filter);
        QueryBuilder query = getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, titleQuery);
        return negate ? new BoolQueryBuilder().mustNot(query) : query;
    }

    private QueryBuilder getQueryAccordingToObjectTypeAndSearchInObject(ObjectType objectType,
            ObjectType objectSearchIn, QueryBuilder query) throws DataException {
        if (objectSearchIn == ObjectType.PROCESS) {
            return getQueryAccordingToObjectTypeAndSearchInProcess(objectType, query);
        } else if (objectSearchIn == ObjectType.TASK) {
            return getQueryAccordingToObjectTypeAndSearchInTask(objectType, query);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder getQueryAccordingToObjectTypeAndSearchInTask(ObjectType objectType, QueryBuilder query)
            throws DataException {
        if (objectType == ObjectType.PROCESS) {
            List<TaskDTO> taskDTOS = ServiceManager.getTaskService().findByQuery(query, true);
            return createSetQuery("tasks.id", collectIds(taskDTOS), true);
        } else if (objectType == ObjectType.TASK) {
            return query;
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder getQueryAccordingToObjectTypeAndSearchInProcess(ObjectType objectType, QueryBuilder query)
            throws DataException {
        if (objectType == ObjectType.PROCESS) {
            return query;
        } else if (objectType == ObjectType.TASK) {
            List<ProcessDTO> processDTOS = ServiceManager.getProcessService().findByQuery(query, true);
            return createSetQuery(TaskTypeField.PROCESS_ID.getKey(), collectIds(processDTOS), true);
        }
        return new BoolQueryBuilder();
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
        List<ProjectDTO> projectsSortedByTitle = Collections.emptyList();
        try {
            projectsSortedByTitle = ServiceManager.getProjectService().findAllProjectsForCurrentUser();
        } catch (DataException e) {
            Helper.setErrorMessage("errorInitializingProjects", logger, e);
        }

        return projectsSortedByTitle.stream().map(ProjectDTO::getTitle).sorted().collect(Collectors.toList());
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

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

package org.kitodo.services.data;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.FilterDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.FilterType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.FilterDTO;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.dto.PropertyDTO;
import org.kitodo.dto.TaskDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.enums.FilterString;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

/**
 * Service for Filter bean.
 */
public class FilterService extends SearchService<Filter, FilterDTO, FilterDAO> {

    private static final Logger logger = LogManager.getLogger(FilterService.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static FilterService instance = null;

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
        if (Objects.equals(instance, null)) {
            synchronized (FilterService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new FilterService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Filter");
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
    List<JSONObject> findByValue(String value, boolean contains) throws DataException {
        QueryBuilder query = createSimpleQuery("value", value, contains, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    @Override
    public FilterDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) {
        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject filterJSONObject = getSource(jsonObject);
        filterDTO.setValue(getStringPropertyForDTO(filterJSONObject, "value"));
        return filterDTO;
    }

    /**
     * This method builds a criteria depending on a filter string and some other
     * parameters passed on along the initial criteria. The filter is parsed and
     * depending on which data structures are used for applying filtering
     * restrictions conjunctions are formed and collect the restrictions and then
     * will be applied on the corresponding criteria. A criteria is only added if
     * needed for the presence of filters applying to it. Prefix "-" means that
     * negated query should be created.
     *
     * @param filter
     *            as String
     * @param objectType
     *            as ObjectType - "PROCESS" or "TASK"
     * @param template
     *            as Boolean
     * @param onlyOpenTasks
     *            as Boolean
     * @param onlyUserAssignedTasks
     *            as Boolean
     * @return query as {@link BoolQueryBuilder}
     */
    public BoolQueryBuilder queryBuilder(String filter, ObjectType objectType, Boolean template, Boolean onlyOpenTasks,
            Boolean onlyUserAssignedTasks) throws DataException {

        BoolQueryBuilder query = new BoolQueryBuilder();

        // this is needed if we filter processes
        if (objectType == ObjectType.PROCESS) {
            query = buildProcessQuery(template);
        }

        // this is needed if we filter task
        if (objectType == ObjectType.TASK) {
            query = buildTaskQuery(onlyOpenTasks, onlyUserAssignedTasks, template);
        }

        for (String tokenizedFilter : prepareFilters(filter)) {
            if (evaluateFilterString(tokenizedFilter, FilterString.PROCESSPROPERTY, null)) {
                query.must(filterProcessProperty(tokenizedFilter, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASK, null)) {
                query.must(createHistoricFilter(tokenizedFilter));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKINWORK, null)) {
                query.must(createTaskFilters(tokenizedFilter, FilterString.TASKINWORK, TaskStatus.INWORK, false,
                        objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKLOCKED, null)) {
                query.must(createTaskFilters(tokenizedFilter, FilterString.TASKLOCKED, TaskStatus.LOCKED, false,
                        objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKOPEN, null)) {
                query.must(
                        createTaskFilters(tokenizedFilter, FilterString.TASKOPEN, TaskStatus.OPEN, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKDONE, null)) {
                query.must(
                        createTaskFilters(tokenizedFilter, FilterString.TASKDONE, TaskStatus.DONE, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKDONETITLE, null)) {
                String taskTitle = getFilterValueFromFilterString(tokenizedFilter, FilterString.TASKDONETITLE);
                query.must(filterTaskTitle(taskTitle, TaskStatus.DONE, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKDONEUSER, null)
                    && ConfigCore.getBooleanParameter("withUserStepDoneSearch")) {
                query.must(filterTaskDoneUser(tokenizedFilter, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKAUTOMATIC, null)) {
                query.must(filterAutomaticTasks(tokenizedFilter, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.PROJECT, null)) {
                query.must(filterProject(tokenizedFilter, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TEMPLATE, null)) {
                query.must(filterScanTemplate(tokenizedFilter, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.ID, null)) {
                query.must(createProcessIdFilter(tokenizedFilter, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.PROCESS, null)) {
                query.must(createProcessTitleFilter(tokenizedFilter, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.BATCH, null)) {
                query.must(createBatchIdFilter(tokenizedFilter, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.WORKPIECE, null)) {
                query.must(filterWorkpiece(tokenizedFilter, false, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.PROCESSPROPERTY, "-")) {
                query.must(filterProcessProperty(tokenizedFilter, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKINWORK, "-")) {
                query.must(createTaskFilters(tokenizedFilter, FilterString.TASKINWORK, TaskStatus.INWORK, true,
                        objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKLOCKED, "-")) {
                query.must(createTaskFilters(tokenizedFilter, FilterString.TASKLOCKED, TaskStatus.LOCKED, true,
                        objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKOPEN, "-")) {
                query.must(
                        createTaskFilters(tokenizedFilter, FilterString.TASKOPEN, TaskStatus.OPEN, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKDONE, "-")) {
                query.must(
                        createTaskFilters(tokenizedFilter, FilterString.TASKDONE, TaskStatus.DONE, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TASKDONETITLE, "-")) {
                String taskTitle = getFilterValueFromFilterString(tokenizedFilter, FilterString.TASKDONETITLE);
                query.must(filterTaskTitle(taskTitle, TaskStatus.DONE, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.PROJECT, "-")) {
                query.must(filterProject(tokenizedFilter, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.TEMPLATE, "-")) {
                query.must(filterScanTemplate(tokenizedFilter, true, objectType));
            } else if (evaluateFilterString(tokenizedFilter, FilterString.WORKPIECE, "-")) {
                query.must(filterWorkpiece(tokenizedFilter, true, objectType));
            } else if (tokenizedFilter.startsWith("-")) {
                query.must(createDefaultQuery(tokenizedFilter.substring(1), true, objectType));
            } else {
                /* standard-search parameter */
                query.must(createDefaultQuery(tokenizedFilter, false, objectType));
            }
        }
        return query;
    }

    private BoolQueryBuilder buildProcessQuery(Boolean template) {
        BoolQueryBuilder processQuery = limitToUserAccessRights();

        // this is needed for the template filter (true) and the undefined
        // processes filter (false) in any other case it needs to be null
        if (template != null) {
            processQuery.must(serviceManager.getProcessService().getQueryTemplate(template));
        }
        return processQuery;
    }

    private BoolQueryBuilder buildTaskQuery(Boolean onlyOpenTasks, Boolean onlyUserAssignedTasks, Boolean template)
            throws DataException {
        BoolQueryBuilder taskQuery = limitToUserAssignedTasks(onlyOpenTasks, onlyUserAssignedTasks);

        // this is needed for the template filter (true) and the undefined
        // processes filter (false) in any other case it needs to be null
        if (template != null) {
            List<ProcessDTO> processDTOS;
            if (!template) {
                processDTOS = serviceManager.getProcessService().findAllWithoutTemplates(null);
            } else {
                processDTOS = serviceManager.getProcessService().findAllTemplates(null);
            }
            taskQuery.must(createSetQuery("processForTask.id", collectIds(processDTOS), true));
        }
        return taskQuery;
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
     * Prepare list of values for given filter. Regexp checks if it contains only
     * numbers and white spaces. In that case it treats it as list of ids. If value
     * contains words and white spaces or single word it treats it as text search.
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
        filterValue = filterValue.substring(filterName.lastIndexOf(":") + 1, filterValue.length());
        if (filterValue.matches("^[\\s\\d]+$")) {
            filterValues.addAll(Arrays.asList(filterValue.split("\\s+")));
        } else {
            filterValues.add(filterValue);
        }
        return filterValues;
    }

    /**
     * Filters for properties are special type. They can contain two times : e.g.
     * 'processproperty:title:value'.
     * 
     * @param filter
     *            full filter String
     * @param filterString
     *            contains only name of filter e.g. 'processproperty:' as String
     * @return list of values in format property title and property value or only
     *         property value
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
     * Prepare list of values in format property title and property value or only
     * property value.
     * 
     * @param filter
     *            full filter String
     * @param filterName
     *            contains only name of filter e.g. 'processproperty:' as String
     * @return list of values in format property title and property value or only
     *         property value.
     */
    private List<String> prepareStringsForProperty(String filter, String filterName) {
        List<String> titleValue = new ArrayList<>();
        String filterValue = filter.replace(filterName, "");
        if (filterValue.contains(":")) {
            titleValue.add(filterValue.substring(0, filterValue.lastIndexOf(":")));
            titleValue.add(filterValue.substring(filterValue.lastIndexOf(":") + 1, filterValue.length()));
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
    private List<String> prepareFilters(String filter) {
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
     *            possible prefix is '-', if prefix not null it means that we are
     *            filtering for negated value
     * @return true or false
     */
    private boolean evaluateFilterString(String stringFilterString, FilterString filterString, String prefix) {
        String lowerCaseFilterString = stringFilterString.toLowerCase();
        if (prefix != null) {
            return lowerCaseFilterString.startsWith(prefix + filterString.getFilterEnglish())
                    || lowerCaseFilterString.startsWith(prefix + filterString.getFilterGerman());
        }
        return lowerCaseFilterString.startsWith(filterString.getFilterEnglish())
                || lowerCaseFilterString.startsWith(filterString.getFilterGerman());
    }

    /**
     * Limit query to projects assigned to user. Restriction to specific projects if
     * not with admin rights.
     *
     * @return query as {@link BoolQueryBuilder}
     */
    private BoolQueryBuilder limitToUserAccessRights() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        UserDTO currentUser = null;
        try {
            if (loginForm != null && loginForm.getMyBenutzer() != null) {
                currentUser = serviceManager.getUserService().findById(loginForm.getMyBenutzer().getId());
            }
        } catch (DataException e) {
            logger.error(e);
        }
        if (currentUser != null) {
            if (loginForm.getMaximaleBerechtigung() > 1) {
                List<ProjectDTO> projects = currentUser.getProjects();
                query.must(createSetQuery("project", collectIds(projects), true));
            }
        }
        return query;
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
        /* identify current user */
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
            return new BoolQueryBuilder();
        }

        /*
         * hits by user groups
         */
        BoolQueryBuilder taskQuery = new BoolQueryBuilder();

        if (onlyOpenTask) {
            taskQuery.must(createSimpleQuery("processingStatus", 1, true));
        } else if (onlyUserAssignedTask) {
            taskQuery.must(createSimpleQuery("processingStatus", 1, true));
            taskQuery.must(createSimpleQuery("processingUser", login.getMyBenutzer().getId(), true));
        } else {
            BoolQueryBuilder processingStatus = new BoolQueryBuilder();
            processingStatus.should(createSimpleQuery("processingStatus", 1, true));
            processingStatus.should(createSimpleQuery("processingStatus", 2, true));
            taskQuery.must(processingStatus);
        }

        UserDTO userDTO = new UserDTO();

        /* only assigned projects */
        List<ProjectDTO> assignedProjects = new ArrayList<>();
        try {
            userDTO = serviceManager.getUserService().findById(login.getMyBenutzer().getId());
            assignedProjects = userDTO.getProjects();
        } catch (DataException e) {
            logger.error(e);
        }

        /* only processes which are not templates and are part of assigned projects */
        try {
            List<ProcessDTO> processDTOS = serviceManager.getProcessService()
                    .findNotTemplateByProjectIds(collectIds(assignedProjects), true);
            taskQuery.must(createSetQuery("processForTask.id", collectIds(processDTOS), true));
        } catch (DataException e) {
            logger.error(e);
        }

        /*
         * only tasks assigned to the user groups the current user is member of
         */
        List<UserGroupDTO> userUserGroups = userDTO.getUserGroups();
        taskQuery.must(createSetQuery("userGroups.id", collectIds(userUserGroups), true));

        /* only task where the user is assigned to */
        taskQuery.must(createSimpleQuery("users.id", login.getMyBenutzer().getId(), true));

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
        Integer taskOrdering = 1;
        BoolQueryBuilder historicFilter = new BoolQueryBuilder();
        String taskTitle = getFilterValueFromFilterString(filterPart, FilterString.TASK);
        if (taskTitle != null) {
            try {
                taskOrdering = Integer.parseInt(taskTitle);
            } catch (NumberFormatException e) {
                taskTitle = filterPart.substring(filterPart.indexOf(":") + 1);
                historicFilter.must(createSimpleCompareQuery("processingStatus", TaskStatus.OPEN.getValue(),
                        SearchCondition.EQUAL_OR_BIGGER));
                if (taskTitle.startsWith("-")) {
                    taskTitle = taskTitle.substring(1);
                    historicFilter.mustNot(createSimpleWildcardQuery("title", taskTitle));
                } else {
                    historicFilter.must(createSimpleWildcardQuery("title", taskTitle));
                }
            }
        }
        historicFilter.must(createSimpleQuery("ordering", taskOrdering, true));
        return historicFilter;
    }

    private QueryBuilder createProcessIdFilter(String filter, ObjectType objectType) {
        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("_id", filterValuesAsStrings(filter, FilterString.ID), true);
        } else if (objectType == ObjectType.TASK) {
            return createSetQuery("processForTask.id", filterValuesAsIntegers(filter, FilterString.ID), true);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createProcessTitleFilter(String filter, ObjectType objectType) {
        String processTitle = getFilterValueFromFilterString(filter, FilterString.PROCESS);
        if (objectType == ObjectType.PROCESS) {
            return serviceManager.getProcessService().getQueryTitle(processTitle, true);
        } else if (objectType == ObjectType.TASK) {
            return createSimpleQuery("processForTask.title", processTitle, true, Operator.AND);
        }
        return new BoolQueryBuilder();
    }

    private QueryBuilder createBatchIdFilter(String filter, ObjectType objectType) throws DataException {
        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("batches.id", filterValuesAsIntegers(filter, FilterString.BATCH), true);
        } else if (objectType == ObjectType.TASK) {
            List<ProcessDTO> processDTOS = serviceManager.getProcessService().findByQuery(
                    createSetQuery("batches.id", filterValuesAsIntegers(filter, FilterString.BATCH), true), true);
            return createSetQuery("processForTask.id", collectIds(processDTOS), true);
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
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder createTaskFilters(String filter, FilterString filterString, TaskStatus taskStatus,
            boolean negate, ObjectType objectType) {
        // extracting the substring into parameter (filter parameters e.g. 5, -5,
        // 5-10, 5- or "Qualitätssicherung")

        String parameters = getFilterValueFromFilterString(filter, filterString);

        /*
         * Analyzing the parameters and what user intended (5->exact, -5 ->max, 5-10
         * ->range, 5- ->min., Qualitätssicherung ->name) handling the filter according
         * to the parameters
         */
        switch (FilterService.getTaskFilter(parameters)) {
            case exact:
                try {
                    return filterTaskExact(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                            + "' caused an error\n");
                }
                break;
            case max:
                try {
                    return filterTaskMax(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                            + "' caused an error\n");
                }
                break;
            case min:
                try {
                    return filterTaskMin(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                            + "' caused an error\n");
                }
                break;
            case name:
                /* filter for a specific done step by it's name (title) */
                try {
                    return filterTaskTitle(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                            + "' caused an error\n");
                }
                break;
            case range:
                try {
                    return filterTaskRange(parameters, taskStatus, negate, objectType);
                } catch (NullPointerException e) {
                    logger.error(e);
                    logger.error("stepdone is preset, don't use 'step' filters");
                } catch (NumberFormatException e) {
                    try {
                        return filterTaskTitle(parameters, taskStatus, negate, objectType);
                    } catch (NullPointerException e1) {
                        logger.error(e);
                        logger.error("stepdone is preset, don't use 'step' filters");
                    } catch (Exception e1) {
                        logger.error(e1);
                        logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                                + "' caused an error\n");
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("filterpart '" + filter.substring(filter.indexOf(":") + 1) + "' in '" + filter
                            + "' caused an error\n");
                }
                break;
            case unknown:
                logger.info("Filter '" + filter + "' is not known!\n");
                break;
            default:
                break;
        }
        return new BoolQueryBuilder();
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
            if (!(strArray.length < 2)) {
                if (strArray[0].length() == 0) {
                    return TaskFilter.max;
                } else {
                    return TaskFilter.range;
                }
            } else {
                return TaskFilter.min;
            }
        } else if (!parameters.contains("-")) {
            try {
                // check if parseInt throws an exception
                Integer.parseInt(parameters);
                return TaskFilter.exact;
            } catch (NumberFormatException e) {
                return TaskFilter.name;
            }
        }
        return TaskFilter.unknown;
    }

    /**
     * This enum represents the result of parsing the step&lt;modifier&gt;: filter
     * Restrictions.
     */
    private enum TaskFilter {
        exact, range, min, max, name, unknown
    }

    /**
     * Filter processes for done steps range.
     *
     * @param parameters
     *            String
     * @param taskStatus
     *            {@link TaskStatus} of searched step
     * @param negate
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskRange(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskRange = new BoolQueryBuilder();
        if (!negate) {
            taskRange.must(
                    createSimpleCompareQuery("ordering", getTaskStart(parameters), SearchCondition.EQUAL_OR_BIGGER));
            taskRange.must(
                    createSimpleCompareQuery("ordering", getTaskEnd(parameters), SearchCondition.EQUAL_OR_SMALLER));
            taskRange.must(createSimpleCompareQuery("processingStatus", taskStatus.getValue(), SearchCondition.EQUAL));
        } else {
            taskRange.mustNot(
                    createSimpleCompareQuery("ordering", getTaskStart(parameters), SearchCondition.EQUAL_OR_BIGGER));
            taskRange.mustNot(
                    createSimpleCompareQuery("ordering", getTaskEnd(parameters), SearchCondition.EQUAL_OR_SMALLER));
            taskRange.mustNot(
                    createSimpleCompareQuery("processingStatus", taskStatus.getValue(), SearchCondition.EQUAL));
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
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskTitle(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskTitle = new BoolQueryBuilder();
        taskTitle.must(createSimpleQuery("title", parameters, !negate));
        taskTitle.must(createSimpleQuery("processingStatus", taskStatus.getValue(), !negate));
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
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskMin(String parameters, TaskStatus taskStatus, boolean negate, ObjectType objectType)
            throws DataException {
        BoolQueryBuilder taskMin = new BoolQueryBuilder();
        taskMin.must(createSimpleQuery("ordering", getTaskStart(parameters), !negate));
        taskMin.must(createSimpleQuery("processingStatus", taskStatus.getValue(), !negate));
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
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskMax(String parameters, TaskStatus taskStatus, boolean negate, ObjectType objectType)
            throws DataException {
        BoolQueryBuilder taskMax = new BoolQueryBuilder();
        taskMax.must(createSimpleQuery("ordering", getTaskEnd(parameters), !negate));
        taskMax.must(createSimpleQuery("processingStatus", taskStatus.getValue(), !negate));
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
     *            true or false, if true create simple queries with contains false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterTaskExact(String parameters, TaskStatus taskStatus, boolean negate,
            ObjectType objectType) throws DataException {
        BoolQueryBuilder taskExact = new BoolQueryBuilder();
        taskExact.must(createSimpleQuery("ordering", getTaskStart(parameters), !negate));
        taskExact.must(createSimpleQuery("processingStatus", taskStatus.getValue(), !negate));
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
        List<TaskDTO> taskDTOS = new ArrayList<>();
        String login = getFilterValueFromFilterString(filter, FilterString.TASKDONEUSER);
        try {
            JSONObject user = serviceManager.getUserService().findByLogin(login);
            UserDTO userDTO = serviceManager.getUserService().convertJSONObjectToDTO(user, false);
            taskDTOS = userDTO.getProcessingTasks();
        } catch (DataException e) {
            logger.error(e);
        }

        if (objectType == ObjectType.PROCESS) {
            return createSetQuery("tasks.id", collectIds(taskDTOS), true);
        } else if (objectType == ObjectType.TASK) {
            return createSetQuery("_id", collectIds(taskDTOS), true);
        }
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
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                typeAutomatic.must(createSimpleQuery("typeAutomatic", true, true));
            } else {
                typeAutomatic.must(createSimpleQuery("typeAutomatic", false, true));
            }
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
        if (stringIds.size() > 0) {
            for (String tempId : stringIds) {
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
        if (stringIds.size() > 0) {
            ids.addAll(stringIds);
        }
        return ids;
    }

    /**
     * Filter process properties.
     *
     * @param filter
     *            as String
     * @param negate
     *            true or false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterProcessProperty(String filter, boolean negate, ObjectType objectType)
            throws DataException {
        /* Filtering by signature */
        List<JSONObject> jsonObjects;
        List<String> titleValue = getFilterValueFromFilterStringForProperty(filter, FilterString.PROCESSPROPERTY);
        if (titleValue.size() > 1) {
            jsonObjects = serviceManager.getProcessService().findByProcessProperty(titleValue.get(0), titleValue.get(1),
                    !negate);
        } else {
            jsonObjects = serviceManager.getProcessService().findByProcessProperty(null, titleValue.get(0), !negate);
        }
        List<PropertyDTO> propertyDTOS = serviceManager.getPropertyService().convertJSONObjectsToDTOs(jsonObjects,
                true);
        QueryBuilder projectQuery = createSetQuery("properties.id", collectIds(propertyDTOS), true);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, projectQuery);
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
        /* filter according to linked project */
        String projectTitle = getFilterValueFromFilterString(filter, FilterString.PROJECT);
        QueryBuilder projectQuery = serviceManager.getProcessService().getQueryProjectTitle(projectTitle);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, projectQuery);
    }

    /**
     * Filter processes by scan template.
     *
     * @param filter
     *            part of filter string to use
     * @param negate
     *            true or false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterScanTemplate(String filter, boolean negate, ObjectType objectType) throws DataException {
        /* Filtering by signature */
        List<JSONObject> jsonObjects;
        List<String> templateProperty = getFilterValueFromFilterStringForProperty(filter, FilterString.TEMPLATE);
        if (templateProperty.size() > 1) {
            jsonObjects = serviceManager.getProcessService().findByTemplateProperty(templateProperty.get(0),
                    templateProperty.get(1), !negate);
        } else {
            jsonObjects = serviceManager.getProcessService().findByTemplateProperty(null, templateProperty.get(0), !negate);
        }
        List<PropertyDTO> templateDTOS = serviceManager.getPropertyService().convertJSONObjectsToDTOs(jsonObjects,
                true);
        QueryBuilder templateQuery = createSetQuery("template", collectIds(templateDTOS), true);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, templateQuery);
    }

    private QueryBuilder createDefaultQuery(String filter, boolean negate, ObjectType objectType) throws DataException {
        QueryBuilder titleQuery = serviceManager.getProcessService().getQueryTitle(filter, !negate);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, titleQuery);
    }

    /**
     * Filter processes by workpiece.
     *
     * @param filter
     *            part of filter string to use
     * @param negate
     *            true or false
     * @param objectType
     *            as {@link ObjectType}
     * @return query as {@link QueryBuilder}
     */
    private QueryBuilder filterWorkpiece(String filter, boolean negate, ObjectType objectType) throws DataException {
        /* filter according signature */
        List<JSONObject> jsonObjects;
        List<String> workpieceProperty = getFilterValueFromFilterStringForProperty(filter,
                FilterString.PROCESSPROPERTY);
        if (workpieceProperty.size() > 1) {
            jsonObjects = serviceManager.getProcessService().findByWorkpieceProperty(workpieceProperty.get(0),
                    workpieceProperty.get(1), !negate);
        } else {
            jsonObjects = serviceManager.getProcessService().findByWorkpieceProperty(null, workpieceProperty.get(0), !negate);
        }
        List<PropertyDTO> workpieceDTOS = serviceManager.getPropertyService().convertJSONObjectsToDTOs(jsonObjects,
                true);
        QueryBuilder workpieceQuery = createSetQuery("workpieces.id", collectIds(workpieceDTOS), true);
        return getQueryAccordingToObjectTypeAndSearchInObject(objectType, ObjectType.PROCESS, workpieceQuery);
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
            List<TaskDTO> taskDTOS = serviceManager.getTaskService().findByQuery(query, true);
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
            List<ProcessDTO> processDTOS = serviceManager.getProcessService().findByQuery(query, true);
            return createSetQuery("processForTask.id", collectIds(processDTOS), true);
        }
        return new BoolQueryBuilder();
    }

    /**
     * This functions extracts the Integer from the parameters passed with the step
     * filter in first position.
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
     * This functions extracts the Integer from the parameters passed with the step
     * filter in last position.
     *
     * @param parameter
     *            String
     * @return Integer
     */
    private Integer getTaskEnd(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[1]);
    }
}

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

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.DocketInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.FileNameBeginsAndEndsWithFilter;
import org.kitodo.api.filemanagement.filters.FileNameEndsAndDoesNotBeginWithFilter;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.enums.MetadataFormat;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProcessDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProcessType;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.export.ExportMets;
import org.kitodo.production.dto.BatchDTO;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.helper.metadata.ImageHelper;
import org.kitodo.production.helper.metadata.MetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.metadata.copier.CopierData;
import org.kitodo.production.metadata.copier.DataCopier;
import org.kitodo.production.process.TiffHeaderGenerator;
import org.kitodo.production.process.TitleGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.ProjectSearchService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class ProcessService extends ProjectSearchService<Process, ProcessDTO, ProcessDAO> {
    private final MetadataLock msp = new MetadataLock();
    private final FileService fileService = ServiceManager.getFileService();
    private static final Logger logger = LogManager.getLogger(ProcessService.class);
    private static volatile ProcessService instance = null;
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private static final String DIRECTORY_PREFIX = ConfigCore.getParameter(ParameterCore.DIRECTORY_PREFIX, "orig");
    private static final String DIRECTORY_SUFFIX = ConfigCore.getParameter(ParameterCore.DIRECTORY_SUFFIX, "tif");
    private static final String SUFFIX = ConfigCore.getParameter(ParameterCore.METS_EDITOR_DEFAULT_SUFFIX, "");
    private static final String EXPORT_DIR_DELETE = "errorDirectoryDeleting";
    private static final String ERROR_EXPORT = "errorExport";
    private static final String CLOSED = "closed";
    private static final String IN_PROCESSING = "inProcessing";
    private static final String LOCKED = "locked";
    private static final String OPEN = "open";
    private static final String PROCESS_TITLE = "(processtitle)";
    private static final String METADATA_SEARCH_KEY = ProcessTypeField.METADATA + ".mdWrap.xmlData.kitodo.metadata";
    private static final String METADATA_FILE_NAME = "meta.xml";
    private static final String NEW_LINE_ENTITY = "\n";
    private static final boolean USE_ORIG_FOLDER = ConfigCore
            .getBooleanParameterOrDefaultValue(ParameterCore.USE_ORIG_FOLDER);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ProcessService() {
        super(new ProcessDAO(), new ProcessType(), new Indexer<>(Process.class), new Searcher(Process.class),
                ProcessTypeField.PROJECT_CLIENT_ID.getKey(), ProcessTypeField.PROJECT_ID.getKey());
    }

    /**
     * Return singleton variable of type ProcessService.
     *
     * @return unique instance of ProcessService
     */
    public static ProcessService getInstance() {
        ProcessService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ProcessService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ProcessService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Process");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Process WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(createUserProcessesQuery(filters));
    }

    @Override
    public List<Process> getAllNotIndexed() {
        return getByQuery("FROM Process WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Process> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Process process) throws DataException {
        if (Objects.nonNull(process.getParent())) {
            save(process.getParent());
        }
        super.save(process);
        if (Objects.nonNull(process.getParent())) {
            save(process.getParent());
        }
    }

    @Override
    public void saveToIndex(Process process, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {
        process.setMetadata(getMetadataForIndex(process));
        super.saveToIndex(process, forceRefresh);
    }

    @Override
    public void addAllObjectsToIndex(List<Process> processes) throws CustomResponseException, DAOException {
        for (Process process : processes) {
            process.setMetadata(getMetadataForIndex(process, true));
        }
        super.addAllObjectsToIndex(processes);
    }

    /**
     * MetadataType aus Preferences eines Prozesses ermitteln.
     *
     * @param inProzess
     *            Process object
     * @param inName
     *            String
     * @return MetadataType
     */
    @Deprecated
    public static LegacyMetadataTypeHelper getMetadataType(Process inProzess, String inName) {
        LegacyPrefsHelper myPrefs = ServiceManager.getRulesetService().getPreferences(inProzess.getRuleset());
        return LegacyPrefsHelper.getMetadataType(myPrefs, inName);
    }

    @Override
    public List<ProcessDTO> loadData(int first, int pageSize, String sortField,
            org.primefaces.model.SortOrder sortOrder, Map filters) throws DataException {
        String filter = ServiceManager.getFilterService().parsePrimeFacesFilter(filters);
        SearchResultGeneration searchResultGeneration = new SearchResultGeneration(filter, this.showClosedProcesses,
                this.showInactiveProjects);
        return findByQuery(searchResultGeneration.getQueryForFilter(ObjectType.PROCESS),
                getSortBuilder(sortField, sortOrder), first, pageSize, false);
    }

    private BoolQueryBuilder readFilters(Map<String, String> filterMap) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            query.must(
                ServiceManager.getFilterService().queryBuilder(entry.getValue(), ObjectType.PROCESS, false, false));
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    private BoolQueryBuilder createUserProcessesQuery(Map filters) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();

        if (Objects.nonNull(filters) && !filters.isEmpty()) {
            query.must(readFilters(filters));
        }

        if (!this.showClosedProcesses) {
            query.mustNot(getQuerySortHelperStatus(true));
        }

        if (!this.showInactiveProjects) {
            query.mustNot(getQueryProjectActive(false));
        }
        return query;
    }

    /**
     * Method saves or removes batches, tasks and project related to modified
     * process.
     *
     * @param process
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Process process)
            throws CustomResponseException, DAOException, DataException, IOException {
        manageBatchesDependenciesForIndex(process);
        manageProjectDependenciesForIndex(process);
        manageTaskDependenciesForIndex(process);
        manageTemplatesDependenciesForIndex(process);
        manageWorkpiecesDependenciesForIndex(process);
        managePropertiesDependenciesForIndex(process);
    }

    /**
     * Check if IndexAction flag is delete. If true remove process from list of
     * processes and re-save batch, if false only re-save batch object.
     *
     * @param process
     *            object
     */
    private void manageBatchesDependenciesForIndex(Process process)
            throws CustomResponseException, DataException, IOException {
        if (process.getIndexAction() == IndexAction.DELETE) {
            for (Batch batch : process.getBatches()) {
                batch.getProcesses().remove(process);
                ServiceManager.getBatchService().saveToIndex(batch, false);
            }
        } else {
            for (Batch batch : process.getBatches()) {
                ServiceManager.getBatchService().saveToIndex(batch, false);
            }
        }
    }

    /**
     * Add process to project, if project is assigned to process.
     *
     * @param process
     *            object
     */
    private void manageProjectDependenciesForIndex(Process process)
            throws CustomResponseException, DataException, IOException {
        if (Objects.nonNull(process.getProject())) {
            ServiceManager.getProjectService().saveToIndex(process.getProject(), false);
        }
    }

    /**
     * Remove properties if process is removed, add properties if process is marked
     * as indexed.
     *
     * @param process
     *            object
     */
    private void managePropertiesDependenciesForIndex(Process process)
            throws CustomResponseException, DataException, IOException {
        if (process.getIndexAction() == IndexAction.DELETE) {
            for (Property property : process.getProperties()) {
                ServiceManager.getPropertyService().removeFromIndex(property, false);
            }
        } else {
            for (Property property : process.getProperties()) {
                ServiceManager.getPropertyService().saveToIndex(property, false);
            }
        }
    }

    /**
     * Check IndexAction flag in for process object. If DELETE remove all tasks
     * from index, if other call saveOrRemoveTaskInIndex() method.
     *
     * @param process
     *            object
     */
    private void manageTaskDependenciesForIndex(Process process)
            throws CustomResponseException, DAOException, IOException, DataException {
        if (process.getIndexAction() == IndexAction.DELETE) {
            for (Task task : process.getTasks()) {
                ServiceManager.getTaskService().removeFromIndex(task, false);
            }
        } else {
            saveOrRemoveTasksInIndex(process);
        }
    }

    /**
     * Compare index and database, according to comparisons results save or
     * remove tasks.
     *
     * @param process
     *            object
     */
    private void saveOrRemoveTasksInIndex(Process process)
            throws CustomResponseException, DAOException, IOException, DataException {
        List<Integer> database = new ArrayList<>();
        List<Integer> index = new ArrayList<>();

        for (Task task : process.getTasks()) {
            database.add(task.getId());
            ServiceManager.getTaskService().saveToIndex(task, false);
        }

        List<Map<String, Object>> searchResults = ServiceManager.getTaskService().findByProcessId(process.getId());
        for (Map<String, Object> object : searchResults) {
            index.add(getIdFromJSONObject(object));
        }

        List<Integer> missingInIndex = findMissingValues(database, index);
        List<Integer> notNeededInIndex = findMissingValues(index, database);
        for (Integer missing : missingInIndex) {
            ServiceManager.getTaskService().saveToIndex(ServiceManager.getTaskService().getById(missing), false);
        }

        for (Integer notNeeded : notNeededInIndex) {
            ServiceManager.getTaskService().removeFromIndex(notNeeded, false);
        }
    }

    /**
     * Remove template if process is removed, add template if process is marked as
     * template.
     *
     * @param process
     *            object
     */
    private void manageTemplatesDependenciesForIndex(Process process)
            throws CustomResponseException, DataException, IOException {
        if (process.getIndexAction() == IndexAction.DELETE) {
            for (Property template : process.getTemplates()) {
                ServiceManager.getPropertyService().removeFromIndex(template, false);
            }
        } else {
            for (Property template : process.getTemplates()) {
                ServiceManager.getPropertyService().saveToIndex(template, false);
            }
        }
    }

    /**
     * Remove workpiece if process is removed, add workpiece if process is marked as
     * workpiece.
     *
     * @param process
     *            object
     */
    private void manageWorkpiecesDependenciesForIndex(Process process)
            throws CustomResponseException, DataException, IOException {
        if (process.getIndexAction() == IndexAction.DELETE) {
            for (Property workpiece : process.getWorkpieces()) {
                ServiceManager.getPropertyService().removeFromIndex(workpiece, false);
            }
        } else {
            for (Property workpiece : process.getWorkpieces()) {
                ServiceManager.getPropertyService().saveToIndex(workpiece, false);
            }
        }
    }

    /**
     * Compare two list and return difference between them.
     *
     * @param firstList
     *            list from which records can be remove
     * @param secondList
     *            records stored here will be removed from firstList
     * @return difference between two lists
     */
    private List<Integer> findMissingValues(List<Integer> firstList, List<Integer> secondList) {
        List<Integer> newList = new ArrayList<>(firstList);
        newList.removeAll(secondList);
        return newList;
    }

    /**
     * Save list of processes to database.
     *
     * @param list
     *            of processes
     */
    public void saveList(List<Process> list) throws DAOException {
        dao.saveList(list);
    }

    @Override
    public void refresh(Process process) {
        dao.refresh(process);
    }

    List<Map<String, Object>> findForCurrentSessionClient() throws DataException {
        return findDocuments(
            getQueryProjectIsAssignedToSelectedClient(ServiceManager.getUserService().getSessionClientId()));
    }

    /**
     * Find processes by metadata.
     *
     * @param metadata
     *            key is metadata tag and value is metadata content
     * @return list of ProcessDTO objects with processes for specific metadata tag
     */
    public List<ProcessDTO> findByMetadata(Map<String, String> metadata) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            BoolQueryBuilder pairQuery = new BoolQueryBuilder();
            pairQuery.must(matchQuery(METADATA_SEARCH_KEY + ".name", entry.getKey()));
            pairQuery.must(matchQuery(METADATA_SEARCH_KEY + ".content", entry.getValue()));
            query.must(pairQuery);
        }

        return findByQuery(nestedQuery(METADATA_SEARCH_KEY, query, ScoreMode.Total), true);
    }

    /**
     * Find processes by title.
     *
     * @param title
     *            the title
     * @return a list of processes
     * @throws DataException
     *             when there is an error on conversion
     */
    public List<ProcessDTO> findByTitle(String title) throws DataException {
        return convertJSONObjectsToDTOs(findByTitle(title, true), true);
    }

    /**
     * Finds processes by searchQuery for a number of fields.
     *
     * @param searchQuery
     *            the query word or phrase
     * @return a List of found ProcessDTOs
     * @throws DataException
     *             when accessing the elasticsearch server fails
     */
    public List<ProcessDTO> findByAnything(String searchQuery) throws DataException {
        NestedQueryBuilder nestedQueryForMetadataContent = nestedQuery(METADATA_SEARCH_KEY,
            matchQuery(METADATA_SEARCH_KEY + ".content", searchQuery).operator(Operator.AND), ScoreMode.Total);
        MultiMatchQueryBuilder multiMatchQueryForProcessFields = multiMatchQuery(searchQuery,
                ProcessTypeField.TITLE.getKey(),
                ProcessTypeField.PROJECT_TITLE.getKey(),
                ProcessTypeField.COMMENTS.getKey(),
                ProcessTypeField.WIKI_FIELD.getKey(),
                ProcessTypeField.TEMPLATE_TITLE.getKey()).operator(Operator.AND);

        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.should(nestedQueryForMetadataContent);
        boolQuery.should(multiMatchQueryForProcessFields);

        if (!searchQuery.contains(" ")) {
            QueryBuilder wildcardQueryForProcessTitle = createSimpleWildcardQuery(ProcessTypeField.TITLE.getKey(),
                searchQuery);
            QueryBuilder wildcardQueryForProjectTitle = createSimpleWildcardQuery(
                ProcessTypeField.PROJECT_TITLE.getKey(), searchQuery);
            QueryBuilder wildcardQueryForComments = createSimpleWildcardQuery(
                    ProcessTypeField.COMMENTS_MESSAGE.getKey(), searchQuery);
            boolQuery.should(wildcardQueryForProcessTitle);
            boolQuery.should(wildcardQueryForProjectTitle);
            boolQuery.should(wildcardQueryForComments);
        }

        return findByQuery(boolQuery, false);
    }

    /**
     * Get query for find process by project title.
     *
     * @param title
     *            as String
     * @return QueryBuilder object
     */
    public QueryBuilder getQueryProjectTitle(String title) {
        return createSimpleQuery(ProcessTypeField.PROJECT_TITLE.getKey(), title, true, Operator.AND);
    }

    /**
     * Get query for find process by project id.
     *
     * @param projectId
     *            as Integer
     * @return QueryBuilder object
     */
    public QueryBuilder getQueryProjectId(Integer projectId) {
        return createSimpleQuery(ProcessTypeField.PROJECT_ID.getKey(), projectId.toString(), true, Operator.AND);
    }

    /**
     * Find processes by docket id.
     *
     * @param docketId
     *            id of docket for search
     * @return list of JSON objects with processes for specific docket id
     */
    public List<Map<String, Object>> findByDocket(int docketId) throws DataException {
        QueryBuilder query = createSimpleQuery(ProcessTypeField.DOCKET.getKey(), docketId, true);
        return findDocuments(query);
    }

    /**
     * Find processes by ruleset id.
     *
     * @param rulesetId
     *            id of ruleset for search
     * @return list of JSON objects with processes for specific ruleset id
     */
    public List<Map<String, Object>> findByRuleset(int rulesetId) throws DataException {
        QueryBuilder query = createSimpleQuery(ProcessTypeField.RULESET.getKey(), rulesetId, true);
        return findDocuments(query);
    }

    /**
     * Find process by property.
     *
     * @param title
     *            of property as String
     * @param value
     *            of property as String
     * @param contains
     *            true or false
     * @return list of process JSONObjects
     */
    public List<Map<String, Object>> findByProcessProperty(String title, String value, boolean contains)
            throws DataException {
        return findByProperty(title, value, "process", "properties.id", contains);
    }

    /**
     * Find process by template.
     *
     * @param title
     *            of property as String
     * @param value
     *            of property as String
     * @param contains
     *            true or false
     * @return list of process JSONObjects
     */
    public List<Map<String, Object>> findByTemplateProperty(String title, String value, boolean contains)
            throws DataException {
        return findByProperty(title, value, "template", "templates.id", contains);
    }

    /**
     * Find process by workpiece.
     *
     * @param title
     *            of property as String
     * @param value
     *            of property as String
     * @param contains
     *            true or false
     * @return list of process JSONObjects
     */
    public List<Map<String, Object>> findByWorkpieceProperty(String title, String value, boolean contains)
            throws DataException {
        return findByProperty(title, value, "workpiece", "workpieces.id", contains);
    }

    /**
     * Get query for projects assigned to selected client.
     *
     * @param id
     *            of selected client
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryProjectIsAssignedToSelectedClient(int id) {
        return createSimpleQuery(ProcessTypeField.PROJECT_CLIENT_ID.getKey(), id, true);
    }

    /**
     * Searches for linkable processes based on user input. A process can be
     * linked if it has the same rule set, belongs to the same client, and the
     * topmost element of the logical outline below the selected parent element
     * is an allowed child. For the latter, the data file must be read at the
     * moment. This will be aborted after a timeout so that the user gets an
     * answer (which may be incomplete) in finite time.
     *
     * @param searchInput
     *            user input
     * @param rulesetId
     *            the id of the allowed ruleset
     * @param allowedStructuralElementTypes
     *            allowed topmost logical structural elements
     * @return found processes
     * @throws DataException
     *             if the search engine fails
     */
    public List<ProcessDTO> findLinkableChildProcesses(String searchInput, int rulesetId,
            Collection<String> allowedStructuralElementTypes) throws DataException, IOException, DAOException {

        BoolQueryBuilder query = new BoolQueryBuilder()
                .should(new MatchQueryBuilder(ProcessTypeField.ID.getKey(), searchInput))
                .should(new MatchQueryBuilder(ProcessTypeField.TITLE.getKey(), "*" + searchInput + "*"))
                .must(new MatchQueryBuilder(ProcessTypeField.RULESET.getKey(), rulesetId));
        List<ProcessDTO> linkableProcesses = new LinkedList<>();

        List<ProcessDTO> processDTOS = findByQuery(query, false);
        for (ProcessDTO process : processDTOS) {
            if (allowedStructuralElementTypes.contains(getBaseType(process.getId()))) {
                linkableProcesses.add(process);
            }
        }
        return linkableProcesses;
    }

    /**
     * Searches for linkable processes based on user input. A process can be
     * linked if it has the same rule set, belongs to the same client, and the
     * topmost element of the logical outline below the selected parent element
     * is an allowed child. For the latter, the data file must be read at the
     * moment. This will be aborted after a timeout so that the user gets an
     * answer (which may be incomplete) in finite time.
     *
     * @param searchInput
     *            user input
     * @param projectId
     *            the id of the allowed project
     * @param rulesetId
     *            the id of the allowed ruleset
     * @return found processes
     * @throws DataException
     *             if the search engine fails
     */
    public List<ProcessDTO> findLinkableParentProcesses(String searchInput, int projectId, int rulesetId)
            throws DataException {

        BoolQueryBuilder query = new BoolQueryBuilder()
                .must(new BoolQueryBuilder().should(new MatchQueryBuilder(ProcessTypeField.ID.getKey(), searchInput))
                        .should(createSimpleWildcardQuery(ProcessTypeField.TITLE.getKey(), searchInput)))
                .must(new MatchQueryBuilder(ProcessTypeField.PROJECT_ID.getKey(), projectId))
                .must(new MatchQueryBuilder(ProcessTypeField.RULESET.getKey(), rulesetId));
        return findByQuery(query, false);
    }

    /**
     * Find processes by property.
     *
     * @param title
     *            of property
     * @param value
     *            of property
     * @param contains
     *            true or false
     * @return list of JSON objects with processes for specific property
     */
    private List<Map<String, Object>> findByProperty(String title, String value, String type, String key,
            boolean contains) throws DataException {
        List<Map<String, Object>> properties;
        if (Objects.isNull(value)) {
            properties = ServiceManager.getPropertyService().findByTitle(title, type, contains);
        } else if (Objects.isNull(title)) {
            properties = ServiceManager.getPropertyService().findByValue(value, type, contains);
        } else {
            properties = ServiceManager.getPropertyService().findByTitleAndValue(title, value, type, contains);
        }

        return findDocuments(createSetQuery(key, properties, true));
    }

    List<ProcessDTO> findByProjectIds(Set<Integer> projectIds, boolean related) throws DataException {
        QueryBuilder query = createSetQuery("project.id", projectIds, true);
        return findByQuery(query, related);
    }

    /**
     * Get query for sort helper status.
     *
     * @param closed
     *            true or false
     * @return query as QueryBuilder
     */
    public QueryBuilder getQuerySortHelperStatus(boolean closed) {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(ProcessTypeField.SORT_HELPER_STATUS.getKey(), "100000000", closed));
        query.should(createSimpleQuery(ProcessTypeField.SORT_HELPER_STATUS.getKey(), "100000000000", closed));
        return query;
    }

    /**
     * Get query for active projects.
     *
     * @param active
     *            true or false
     * @return query as QueryBuilder
     */
    public QueryBuilder getQueryProjectActive(boolean active) {
        return createSimpleQuery(ProcessTypeField.PROJECT_ACTIVE.getKey(), active, true);
    }

    /**
     * Sort results by creation date.
     *
     * @param sortOrder
     *            ASC or DESC as SortOrder
     * @return sort
     */
    public SortBuilder sortByCreationDate(SortOrder sortOrder) {
        return SortBuilders.fieldSort(ProcessTypeField.CREATION_DATE.getKey()).order(sortOrder);
    }

    /**
     * Convert list of DTOs to list of beans.
     *
     * @param dtos
     *            list of DTO objects
     * @return list of beans
     */
    public List<Process> convertDtosToBeans(List<ProcessDTO> dtos) throws DAOException {
        List<Process> processes = new ArrayList<>();
        for (ProcessDTO processDTO : dtos) {
            processes.add(getById(processDTO.getId()));
        }
        return processes;
    }

    @Override
    public ProcessDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        ProcessDTO processDTO = new ProcessDTO();
        if (!jsonObject.isEmpty()) {
            processDTO.setId(getIdFromJSONObject(jsonObject));
            processDTO.setTitle(ProcessTypeField.TITLE.getStringValue(jsonObject));
            processDTO.setWikiField(ProcessTypeField.WIKI_FIELD.getStringValue(jsonObject));
            processDTO.setCreationDate(ProcessTypeField.CREATION_DATE.getStringValue(jsonObject));
            processDTO.setProperties(convertRelatedJSONObjectToDTO(jsonObject, ProcessTypeField.PROPERTIES.getKey(),
                ServiceManager.getPropertyService()));
            processDTO.setSortedCorrectionSolutionMessages(getSortedCorrectionSolutionMessages(processDTO));
            processDTO.setSortHelperArticles(ProcessTypeField.SORT_HELPER_ARTICLES.getIntValue(jsonObject));
            processDTO.setSortHelperDocstructs(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getIntValue(jsonObject));
            processDTO.setSortHelperImages(ProcessTypeField.SORT_HELPER_IMAGES.getIntValue(jsonObject));
            processDTO.setSortHelperMetadata(ProcessTypeField.SORT_HELPER_METADATA.getIntValue(jsonObject));
            processDTO.setProcessBaseUri(ProcessTypeField.PROCESS_BASE_URI.getStringValue(jsonObject));
            processDTO.setHasChildren(ProcessTypeField.HAS_CHILDREN.getBooleanValue(jsonObject));
            processDTO.setParentID(ProcessTypeField.PARENT_ID.getIntValue(jsonObject));

            if (!related) {
                convertRelatedJSONObjects(jsonObject, processDTO);
            } else {
                ProjectDTO projectDTO = new ProjectDTO();
                projectDTO.setId(ProcessTypeField.PROJECT_ID.getIntValue(jsonObject));
                projectDTO.setTitle(ProcessTypeField.PROJECT_TITLE.getStringValue(jsonObject));
                projectDTO.setActive(ProcessTypeField.PROJECT_ACTIVE.getBooleanValue(jsonObject));
                processDTO.setProject(projectDTO);
            }
        }
        return processDTO;
    }

    private void convertRelatedJSONObjects(Map<String, Object> jsonObject, ProcessDTO processDTO) throws DataException {
        int project = ProcessTypeField.PROJECT_ID.getIntValue(jsonObject);
        if (project > 0) {
            processDTO.setProject(ServiceManager.getProjectService().findById(project));
        }

        processDTO.setBatchID(getBatchID(processDTO));
        processDTO.setBatches(getBatchesForProcessDTO(jsonObject));
        // TODO: leave it for now - right now it displays only status
        processDTO.setTasks(convertRelatedJSONObjectToDTO(jsonObject, ProcessTypeField.TASKS.getKey(),
            ServiceManager.getTaskService()));

        processDTO.setProgressClosed(getProgressClosed(null, processDTO.getTasks()));
        processDTO.setProgressInProcessing(getProgressInProcessing(null, processDTO.getTasks()));
        processDTO.setProgressOpen(getProgressOpen(null, processDTO.getTasks()));
        processDTO.setProgressLocked(getProgressLocked(null, processDTO.getTasks()));
    }

    private List<BatchDTO> getBatchesForProcessDTO(Map<String, Object> jsonObject) throws DataException {
        List<Map<String, Object>> jsonArray = ProcessTypeField.BATCHES.getJsonArray(jsonObject);
        List<BatchDTO> batchDTOList = new ArrayList<>();
        for (Map<String, Object> singleObject : jsonArray) {
            BatchDTO batchDTO = new BatchDTO();
            batchDTO.setId(BatchTypeField.ID.getIntValue(singleObject));
            batchDTO.setTitle(BatchTypeField.TITLE.getStringValue(singleObject));
            batchDTOList.add(batchDTO);
        }
        return batchDTOList;
    }

    /**
     * Check if process is assigned only to one batch.
     *
     * @param batchDTOList
     *            list of batches for checkout
     * @return true or false
     */
    boolean isProcessAssignedToOnlyOneBatch(List<BatchDTO> batchDTOList) {
        return batchDTOList.size() == 1;
    }

    /**
     * Get blocked user for Process.
     *
     * @return blocked metadata (user)
     */
    public User getBlockedUser(Process process) {
        User user = null;
        if (MetadataLock.isLocked(process.getId())) {
            String userID = this.msp.getLockUser(process.getId());
            try {
                user = ServiceManager.getUserService().getById(Integer.valueOf(userID));
            } catch (DAOException | RuntimeException e) {
                Helper.setErrorMessage("userNotFound", logger, e);
            }
        }
        return user;
    }

    public long getBlockedMinutes(Process process) {
        return this.msp.getLockSeconds(process.getId()) / 60;
    }

    public long getBlockedSeconds(Process process) {
        return this.msp.getLockSeconds(process.getId()) % 60;
    }

    /**
     * Get directory for TIFF images.
     *
     * @param useFallBack
     *            add description
     * @param processId
     *            id of process object
     * @param processTitle
     *            title of process object
     * @param processBaseURI
     *            base URI of process object
     * @return tif directory
     */
    public URI getImagesTifDirectory(boolean useFallBack, Integer processId, String processTitle, URI processBaseURI) {
        URI dir = fileService.getProcessSubTypeURI(processId, processTitle, processBaseURI, ProcessSubType.IMAGE, null);

        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
        FilenameFilter filterDirectory = new FileNameEndsAndDoesNotBeginWithFilter(DIRECTORY_PREFIX + "_",
                "_" + DIRECTORY_SUFFIX);
        URI tifDirectory = null;
        List<URI> directories = fileService.getSubUris(filterDirectory, dir);
        for (URI directory : directories) {
            tifDirectory = directory;
        }

        if (Objects.isNull(tifDirectory) && useFallBack && !SUFFIX.isEmpty()) {
            List<URI> folderList = fileService.getSubUrisForProcess(null, processId, processTitle, processBaseURI,
                ProcessSubType.IMAGE, "");
            for (URI folder : folderList) {
                if (folder.toString().endsWith(SUFFIX)) {
                    tifDirectory = folder;
                    break;
                }
            }
        }

        tifDirectory = getImageDirectory(useFallBack, dir, tifDirectory);

        URI result = fileService.getProcessSubTypeURI(processId, processTitle, processBaseURI, ProcessSubType.IMAGE,
            null);

        if (Objects.isNull(tifDirectory)) {
            tifDirectory = URI
                    .create(result.getRawPath() + Helper.getNormalizedTitle(processTitle) + "_" + DIRECTORY_SUFFIX);
        }

        return tifDirectory;
    }

    /**
     * Get images origin directory.
     *
     * @param useFallBack
     *            as boolean
     * @param process
     *            object
     * @return path
     */
    public URI getImagesOriginDirectory(boolean useFallBack, Process process) {
        if (USE_ORIG_FOLDER) {
            URI dir = fileService.getProcessSubTypeURI(process, ProcessSubType.IMAGE, null);

            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
            FilenameFilter filterDirectory = new FileNameBeginsAndEndsWithFilter(DIRECTORY_PREFIX + "_",
                    "_" + DIRECTORY_SUFFIX);
            URI origDirectory = null;
            List<URI> directories = fileService.getSubUris(filterDirectory, dir);
            for (URI directory : directories) {
                origDirectory = directory;
            }

            if (Objects.isNull(origDirectory) && useFallBack && !SUFFIX.isEmpty()) {
                List<URI> folderList = fileService.getSubUris(dir);
                for (URI folder : folderList) {
                    if (folder.toString().endsWith(SUFFIX)) {
                        origDirectory = folder;
                        break;
                    }
                }
            }

            origDirectory = getImageDirectory(useFallBack, dir, origDirectory);

            URI result = fileService.getProcessSubTypeURI(process, ProcessSubType.IMAGE, null);

            if (Objects.isNull(origDirectory)) {
                origDirectory = URI.create(result.toString() + DIRECTORY_PREFIX + "_"
                        + Helper.getNormalizedTitle(process.getTitle()) + "_" + DIRECTORY_SUFFIX);
            }

            return origDirectory;
        } else {
            return getImagesTifDirectory(useFallBack, process.getId(), process.getTitle(), process.getProcessBaseUri());
        }
    }

    private URI getImageDirectory(boolean useFallBack, URI directory, URI imageDirectory) {
        if (Objects.nonNull(imageDirectory) && useFallBack && !SUFFIX.isEmpty()) {
            URI tif = imageDirectory;
            List<URI> files = fileService.getSubUris(tif);
            if (files.isEmpty()) {
                List<URI> folderList = fileService.getSubUris(directory);
                for (URI folder : folderList) {
                    if (folder.toString().endsWith(SUFFIX) && !folder.getPath().startsWith(DIRECTORY_PREFIX)) {
                        imageDirectory = folder;
                        break;
                    }
                }
            }
        }
        return imageDirectory;
    }

    /**
     * Returns the URI of the metadata file of a process.
     *
     * @param process
     *            object
     * @return URI
     */
    public URI getMetadataFileUri(Process process) {
        URI workPathUri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
        String workDirectoryPath = workPathUri.getPath();
        try {
            return new URI(workPathUri.getScheme(), workPathUri.getUserInfo(), workPathUri.getHost(),
                    workPathUri.getPort(),
                workDirectoryPath.endsWith("/") ? workDirectoryPath.concat(METADATA_FILE_NAME)
                        : workDirectoryPath + '/' + METADATA_FILE_NAME,
                    workPathUri.getQuery(), null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Get process data directory.
     * Don't save it to the database, if it is for indexingAll.
     *
     * @param process
     *            object
     * @param forIndexingAll
     *            if the dataDirectory is created for indexingAll
     * @return path
     */
    public URI getProcessDataDirectory(Process process, boolean forIndexingAll) {
        if (Objects.isNull(process.getProcessBaseUri())) {
            process.setProcessBaseUri(fileService.getProcessBaseUriForExistingProcess(process));
            if (!forIndexingAll) {
                try {
                    saveToDatabase(process);
                } catch (DAOException e) {
                    logger.error(e.getMessage(), e);
                    return URI.create("");
                }
            }
        }
        return process.getProcessBaseUri();
    }

    /**
     * Get process data directory.
     *
     * @param process
     *            object
     * @return path
     */
    public URI getProcessDataDirectory(Process process) {
        return getProcessDataDirectory(process, false);
    }

    /**
     * Returns a URI that identifies the process. The URI has the form
     * {@code mysql://?process.id=42}, where {@code 42} is the process ID.
     *
     * @param process
     *            process for which a URI is to be formed that identifies it
     * @return a URI that identifies the process
     */
    public URI getProcessURI(Process process) {
        return getProcessURI(process.getId());
    }

    /**
     * Returns a URI that identifies the process. The URI has the form
     * {@code database://?process.id=42}, where {@code 42} is the process ID.
     *
     * @param processId
     *            process ID for which a URI is to be formed that identifies it
     * @return a URI that identifies the process
     */
    public URI getProcessURI(Integer processId) {
        try {
            return new URI("database", null, "//", "process.id=" + processId, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * The function getBatchID returns the batches the process is associated
     * with as readable text as read-only property "batchID".
     *
     * @return the batches the process is in
     */
    public String getBatchID(ProcessDTO process) {
        if (process.getBatches().isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (BatchDTO batch : process.getBatches()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(ServiceManager.getBatchService().getLabel(batch));
        }
        return result.toString();
    }

    /**
     * Get current task.
     *
     * @param process
     *            object
     * @return current task
     */
    public Task getCurrentTask(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatus().equals(TaskStatus.OPEN)
                    || task.getProcessingStatus().equals(TaskStatus.INWORK)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Get current tasks.
     *
     * @param process
     *            object
     * @return current tasks
     */
    public List<Task> getCurrentTasks(Process process) {
        List<Task> currentTasks = new ArrayList<>();
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatus().equals(TaskStatus.OPEN)
                    || task.getProcessingStatus().equals(TaskStatus.INWORK)) {
                currentTasks.add(task);
            }
        }
        return currentTasks;
    }

    private List<TaskDTO> getOpenTasks(ProcessDTO process) {
        return process.getTasks().stream()
                .filter(t -> TaskStatus.OPEN.equals(t.getProcessingStatus())).collect(Collectors.toList());
    }

    private List<TaskDTO> getTasksInWork(ProcessDTO process) {
        return process.getTasks().stream()
                .filter(t -> TaskStatus.INWORK.equals(t.getProcessingStatus())).collect(Collectors.toList());
    }

    /**
     * Create and return String used as progress tooltip for a given process. Tooltip contains OPEN tasks and tasks
     * INWORK.
     *
     * @param processDTO
     *          process for which the tooltop is created
     * @return String containing the progress tooltip for the given process
     */
    public String createProgressTooltip(ProcessDTO processDTO) {
        String openTasks = getOpenTasks(processDTO).stream()
                .map(t -> " - " + Helper.getTranslation(t.getTitle())).collect(Collectors.joining(NEW_LINE_ENTITY));
        if (!openTasks.isEmpty()) {
            openTasks = Helper.getTranslation(TaskStatus.OPEN.getTitle()) + ":" + NEW_LINE_ENTITY + openTasks;
        }
        String tasksInWork = getTasksInWork(processDTO).stream()
                .map(t -> " - " + Helper.getTranslation(t.getTitle())).collect(Collectors.joining(NEW_LINE_ENTITY));
        if (!tasksInWork.isEmpty()) {
            tasksInWork = Helper.getTranslation(TaskStatus.INWORK.getTitle()) + ":" + NEW_LINE_ENTITY + tasksInWork;
        }
        if (openTasks.isEmpty() && tasksInWork.isEmpty()) {
            return "";
        } else if (openTasks.isEmpty()) {
            return tasksInWork;
        } else if (tasksInWork.isEmpty()) {
            return openTasks;
        } else {
            return openTasks + NEW_LINE_ENTITY + tasksInWork;
        }
    }

    /**
     * Get current task.
     *
     * @param processDTO
     *            DTOobject
     * @return current task
     */
    public TaskDTO getCurrentTaskDTO(ProcessDTO processDTO) {
        for (TaskDTO task : processDTO.getTasks()) {
            if (task.getProcessingStatus().equals(TaskStatus.OPEN)
                    || task.getProcessingStatus().equals(TaskStatus.INWORK)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Get full progress for process.
     *
     * @param tasksBean
     *            list of Task bean objects
     * @param tasksDTO
     *            list of TaskDTO objects
     * @return string
     */
    public String getProgress(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        Map<String, Integer> tasks = getCalculationForProgress(tasksBean, tasksDTO);

        double closed = calculateProgressClosed(tasks);
        double inProcessing = calculateProgressInProcessing(tasks);
        double open = calculateProgressOpen(tasks);
        double locked = calculateProgressLocked(tasks);

        DecimalFormat decimalFormat = new DecimalFormat("#000");
        return decimalFormat.format(closed) + decimalFormat.format(inProcessing) + decimalFormat.format(open)
                + decimalFormat.format(locked);
    }

    /**
     * Get progress for closed tasks.
     *
     * @param tasksBean
     *            list of Task bean objects
     * @param tasksDTO
     *            list of TaskDTO objects
     * @return progress for closed steps
     */
    int getProgressClosed(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        Map<String, Integer> tasks = getCalculationForProgress(tasksBean, tasksDTO);

        return calculateProgressClosed(tasks);
    }

    /**
     * Get progress for processed tasks.
     *
     * @param tasksBean
     *            list of Task bean objects
     * @param tasksDTO
     *            list of TaskDTO objects
     * @return progress for processed tasks
     */
    int getProgressInProcessing(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        Map<String, Integer> tasks = getCalculationForProgress(tasksBean, tasksDTO);

        return calculateProgressInProcessing(tasks);
    }

    /**
     * Get progress for open tasks.
     *
     * @param tasksBean
     *            list of Task bean objects
     * @param tasksDTO
     *            list of TaskDTO objects
     * @return return progress for open tasks
     */
    int getProgressOpen(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        Map<String, Integer> tasks = getCalculationForProgress(tasksBean, tasksDTO);
        return calculateProgressOpen(tasks);
    }

    /**
     * Get progress for open tasks.
     *
     * @param tasksBean
     *            list of Task bean objects
     * @param tasksDTO
     *            list of TaskDTO objects
     * @return return progress for open tasks
     */
    int getProgressLocked(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        Map<String, Integer> tasks = getCalculationForProgress(tasksBean, tasksDTO);
        return calculateProgressLocked(tasks);
    }

    private int calculateProgressClosed(Map<String, Integer> tasks) {
        return tasks.get(CLOSED) * 100
                / (tasks.get(CLOSED) + tasks.get(IN_PROCESSING) + tasks.get(OPEN) + tasks.get(LOCKED));
    }

    private int calculateProgressInProcessing(Map<String, Integer> tasks) {
        return tasks.get(IN_PROCESSING) * 100
                / (tasks.get(CLOSED) + tasks.get(IN_PROCESSING) + tasks.get(OPEN) + tasks.get(LOCKED));
    }

    private int calculateProgressOpen(Map<String, Integer> tasks) {
        return tasks.get(OPEN) * 100
                / (tasks.get(CLOSED) + tasks.get(IN_PROCESSING) + tasks.get(OPEN) + tasks.get(LOCKED));
    }

    private int calculateProgressLocked(Map<String, Integer> tasks) {
        return tasks.get(LOCKED) * 100
                / (tasks.get(CLOSED) + tasks.get(IN_PROCESSING) + tasks.get(OPEN) + tasks.get(LOCKED));
    }

    private Map<String, Integer> getCalculationForProgress(List<Task> tasksBean, List<TaskDTO> tasksDTO) {
        List<TaskStatus> taskStatuses = new ArrayList<>();

        if (Objects.nonNull(tasksBean)) {
            for (Task task : tasksBean) {
                taskStatuses.add(task.getProcessingStatus());
            }
        } else {
            for (TaskDTO task : tasksDTO) {
                taskStatuses.add(task.getProcessingStatus());
            }
        }
        return calculationForProgress(taskStatuses);
    }

    private Map<String, Integer> calculationForProgress(List<TaskStatus> taskStatuses) {
        Map<String, Integer> results = new HashMap<>();
        int open = 0;
        int inProcessing = 0;
        int closed = 0;
        int locked = 0;

        for (TaskStatus taskStatus : taskStatuses) {
            if (taskStatus.equals(TaskStatus.DONE)) {
                closed++;
            } else if (taskStatus.equals(TaskStatus.OPEN)) {
                open++;
            } else if (taskStatus.equals(TaskStatus.LOCKED)) {
                locked++;
            } else {
                inProcessing++;
            }
        }

        results.put(CLOSED, closed);
        results.put(IN_PROCESSING, inProcessing);
        results.put(OPEN, open);
        results.put(LOCKED, locked);

        if (open + inProcessing + closed + locked == 0) {
            results.put(LOCKED, 1);
        }

        return results;
    }

    /**
     * Read metadata file.
     *
     * @param process
     *            object
     * @return filer format
     */
    public LegacyMetsModsDigitalDocumentHelper readMetadataFile(Process process) throws IOException {
        URI metadataFileUri = ServiceManager.getFileService().getMetadataFilePath(process);

        // check the format of the metadata - METS, XStream or RDF
        String type = MetadataHelper.getMetaFileType(metadataFileUri);
        logger.debug("current meta.xml file type for id {}: {}", process.getId(), type);

        LegacyMetsModsDigitalDocumentHelper ff = determineFileFormat(type, process);
        try {
            ff.read(ServiceManager.getFileService().getFile(metadataFileUri).toString());
        } catch (IOException e) {
            if (e.getMessage().startsWith("Parse error at line -1")) {
                Helper.setErrorMessage("metadataCorrupt", logger, e);
            } else {
                throw e;
            }
        }
        return ff;
    }

    /**
     * Reads the metadata File.
     *
     * @param metadataFile
     *            The given metadataFile.
     * @param prefs
     *            The Preferences
     * @return The fileFormat.
     */
    public LegacyMetsModsDigitalDocumentHelper readMetadataFile(URI metadataFile, LegacyPrefsHelper prefs)
            throws IOException {
        String type = MetadataHelper.getMetaFileType(metadataFile);
        LegacyMetsModsDigitalDocumentHelper fileFormat = determineFileFormat(type, prefs);
        fileFormat.read(ConfigCore.getKitodoDataDirectory() + metadataFile.getPath());
        return fileFormat;
    }

    private LegacyMetsModsDigitalDocumentHelper determineFileFormat(String type, Process process) {
        RulesetService rulesetService = ServiceManager.getRulesetService();
        return determineFileFormat(type, rulesetService.getPreferences(process.getRuleset()));
    }

    private LegacyMetsModsDigitalDocumentHelper determineFileFormat(String type, LegacyPrefsHelper prefs) {
        LegacyMetsModsDigitalDocumentHelper fileFormat;

        if ("metsmods".equals(type) || "mets".equals(type)) {
            fileFormat = new LegacyMetsModsDigitalDocumentHelper(prefs.getRuleset());
        } else {
            throw new UnsupportedOperationException("Dead code pending removal");
        }
        return fileFormat;
    }

    /**
     * Read metadata as template file.
     *
     * @param process
     *            object
     * @return file format
     */
    public LegacyMetsModsDigitalDocumentHelper readMetadataAsTemplateFile(Process process) throws IOException {
        URI processSubTypeURI = fileService.getProcessSubTypeURI(process, ProcessSubType.TEMPLATE, null);
        if (fileService.fileExist(processSubTypeURI)) {
            String type = MetadataHelper.getMetaFileType(processSubTypeURI);
            logger.debug("current template.xml file type: {}", type);
            LegacyMetsModsDigitalDocumentHelper ff = determineFileFormat(type, process);
            String processSubTypePath = fileService.getFile(processSubTypeURI).getAbsolutePath();
            ff.read(processSubTypePath);
            return ff;
        } else {
            throw new IOException("File does not exist: " + processSubTypeURI);
        }
    }

    /**
     * Check if there is one task in edit mode, where the user has the rights to
     * write to image folder.
     *
     * @param process
     *            bean object
     * @return true or false
     */
    public boolean isImageFolderInUse(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatus() == TaskStatus.INWORK && task.isTypeImagesWrite()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get user of task in edit mode with rights to write to image folder.
     */
    public User getImageFolderInUseUser(Process process) {
        for (Task task : process.getTasks()) {
            if (task.getProcessingStatus() == TaskStatus.INWORK && task.isTypeImagesWrite()) {
                return task.getProcessingUser();
            }
        }
        return null;
    }

    /**
     * Download docket for given process.
     *
     * @param process
     *            object
     * @throws IOException
     *             when xslt file could not be loaded, or write to output failed
     */
    public void downloadDocket(Process process) throws IOException {
        logger.debug("generate docket for process with id {}", process.getId());
        URI rootPath = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)).toUri();
        URI xsltFile;
        if (Objects.nonNull(process.getDocket())) {
            xsltFile = ServiceManager.getFileService().createResource(rootPath, process.getDocket().getFile());
            if (!fileService.fileExist(xsltFile)) {
                Helper.setErrorMessage("docketMissing");
            }
        } else {
            xsltFile = ServiceManager.getFileService().createResource(rootPath, "docket.xsl");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            // write run note to servlet output stream
            DocketInterface module = initialiseDocketModule();

            File file = module.generateDocket(getDocketData(process), xsltFile);
            writeToOutputStream(facesContext, file, Helper.getNormalizedTitle(process.getTitle()) + ".pdf");
            Files.deleteIfExists(file.toPath());
        }
    }

    /**
     * Writes a multi page docket for a list of processes to an output stream.
     *
     * @param processes
     *            The list of processes
     * @throws IOException
     *             when xslt file could not be loaded, or write to output failed
     */
    public void downloadDocket(List<Process> processes) throws IOException {
        logger.debug("generate docket for processes {}", processes);
        URI rootPath = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)).toUri();
        URI xsltFile = ServiceManager.getFileService().createResource(rootPath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            DocketInterface module = initialiseDocketModule();
            File file = module.generateMultipleDockets(getDocketData(processes),
                xsltFile);

            writeToOutputStream(facesContext, file, "batch_docket.pdf");
            Files.deleteIfExists(file.toPath());
        }
    }

    /**
     * Generate result as PDF.
     *
     * @param filter
     *            for generating search results
     */
    public void generateResultAsPdf(String filter) throws DocumentException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            ExternalContext response = prepareHeaderInformation(facesContext, "search.pdf");
            try (OutputStream out = response.getResponseOutputStream()) {
                SearchResultGeneration sr = new SearchResultGeneration(filter, this.showClosedProcesses,
                        this.showInactiveProjects);
                HSSFWorkbook wb = sr.getResult();
                List<List<HSSFCell>> rowList = new ArrayList<>();
                HSSFSheet mySheet = wb.getSheetAt(0);
                Iterator<Row> rowIter = mySheet.rowIterator();
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    List<HSSFCell> row = new ArrayList<>();
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        row.add(myCell);
                    }
                    rowList.add(row);
                }
                Document document = new Document();
                Rectangle rectangle = new Rectangle(PageSize.A3.getHeight(), PageSize.A3.getWidth());
                PdfWriter.getInstance(document, out);
                document.setPageSize(rectangle);
                document.open();
                if (!rowList.isEmpty()) {
                    Paragraph paragraph = new Paragraph(rowList.get(0).get(0).toString());
                    document.add(paragraph);
                    document.add(getPdfTable(rowList));
                }

                document.close();
                out.flush();
                facesContext.responseComplete();
            }
        }
    }

    /**
     * Generate result set.
     *
     * @param filter
     *            for generating search results
     */
    public void generateResult(String filter) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            ExternalContext response = prepareHeaderInformation(facesContext, "search.xls");
            try (OutputStream out = response.getResponseOutputStream()) {
                SearchResultGeneration sr = new SearchResultGeneration(filter, this.showClosedProcesses,
                        this.showInactiveProjects);
                HSSFWorkbook wb = sr.getResult();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();
            }
        }
    }

    /**
     * Good explanation how it should be implemented:
     * https://stackoverflow.com/a/9394237/2701807.
     *
     * @param facesContext
     *            context
     * @param file
     *            temporal file which contains content to save
     * @param fileName
     *            name of the new docket file
     */
    private void writeToOutputStream(FacesContext facesContext, File file, String fileName) throws IOException {
        ExternalContext externalContext = prepareHeaderInformation(facesContext, fileName);

        try (OutputStream outputStream = externalContext.getResponseOutputStream();
                FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = IOUtils.toByteArray(fileInputStream);
            outputStream.write(bytes);
            outputStream.flush();
        }
        facesContext.responseComplete();
    }

    private ExternalContext prepareHeaderInformation(FacesContext facesContext, String outputFileName) {
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.responseReset();

        String contentType = externalContext.getMimeType(outputFileName);
        externalContext.setResponseContentType(contentType);
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + outputFileName + "\"");

        return externalContext;
    }

    private PdfPTable getPdfTable(List<List<HSSFCell>> rowList) {
        // create formatter for cells with default locale
        DataFormatter formatter = new DataFormatter();

        PdfPTable table = new PdfPTable(9);
        table.setSpacingBefore(20);
        for (List<HSSFCell> row : rowList) {
            for (HSSFCell hssfCell : row) {
                String stringCellValue = formatter.formatCellValue(hssfCell);
                table.addCell(stringCellValue);
            }
        }

        return table;
    }

    private DocketInterface initialiseDocketModule() {
        KitodoServiceLoader<DocketInterface> loader = new KitodoServiceLoader<>(DocketInterface.class);
        return loader.loadModule();
    }

    /**
     * Returns the digital act of this process.
     *
     * @return the digital act of this process
     * @throws IOException
     *             if creating the process directory or reading the meta data file
     *             fails
     */
    public LegacyMetsModsDigitalDocumentHelper getDigitalDocument(Process process) throws IOException {
        return readMetadataFile(process).getDigitalDocument();
    }

    /**
     * Returns the type of the top element of the root element, and thus the
     * type of the workpiece of the process.
     *
     * @param processId
     *            process whose root type is to be determined
     * @return the type of root element of the root element of the workpiece
     * @throws RuntimeException
     *             because it is used in a parallel stream
     */
    private String getBaseType(Integer processId) throws DAOException, IOException {
        return getBaseType(getById(processId));
    }

    /**
     * Returns the type of the top element of the root element, and thus the
     * type of the workpiece of the process.
     *
     * @param process
     *            process whose root type is to be determined
     * @return the type of root element of the root element of the workpiece
     * @throws IOException
     *             if the file cannot be read (for example, because the file was
     *             not found)
     */
    public String getBaseType(Process process) throws IOException {
        URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
        return ServiceManager.getMetsService().getBaseType(metadataFilePath);
    }

    /**
     * Filter and sort after creation date list of process properties for
     * correction and solution messages.
     *
     * @return list of ProcessProperty objects
     */
    public List<PropertyDTO> getSortedCorrectionSolutionMessages(ProcessDTO process) {
        List<PropertyDTO> filteredList = filterForCorrectionSolutionMessages(process.getProperties());

        if (filteredList.size() > 1) {
            filteredList.sort(
                Comparator.comparing(PropertyDTO::getCreationDate, Comparator.nullsFirst(Comparator.naturalOrder())));
        }

        return filteredList;
    }

    /**
     * Filter for correction / solution messages.
     *
     * @param lpe
     *            List of process properties
     * @return List of filtered correction / solution messages
     */
    private List<PropertyDTO> filterForCorrectionSolutionMessages(List<PropertyDTO> lpe) {
        List<PropertyDTO> filteredList = new ArrayList<>();

        if (lpe.isEmpty()) {
            return filteredList;
        }

        List<String> translationList = Arrays.asList("Correction required", "Correction performed",
            "Korrektur notwendig", "Korrektur durchgef\u00FChrt");

        // filtering for correction and solution messages
        for (PropertyDTO property : lpe) {
            if (translationList.contains(property.getTitle())) {
                filteredList.add(property);
            }
        }
        return filteredList;
    }

    /**
     * Find amount of processes for given title.
     *
     * @param title
     *            as String
     * @return amount as Long
     */
    public Long findNumberOfProcessesWithTitle(String title) throws DataException {
        return count(createSimpleQuery(ProcessTypeField.TITLE.getKey(), title, true, Operator.AND));
    }

    /**
     * Sanitizes a possibly dirty reference URI and extracts from it the
     * operation ID of the referenced process. In case of error, a speaking
     * exception is thrown.
     *
     * @param uri
     *            URI of the reference to the process number
     * @return process number
     * @throws SecurityException
     *             if the URI names a forbidden protocol or column
     * @throws IllegalArgumentException
     *             if the URI tries to reference a foreign database or if the
     *             query has a wrong format
     * @throws ClassCastException
     *             if the URI references a wrong class / table
     */
    public int processIdFromUri(URI uri) {
        if (!"database".equals(uri.getScheme())) {
            throw new SecurityException("Protocol not allowed: " + uri.getScheme());
        }
        if (Objects.nonNull(uri.getAuthority()) || Objects.nonNull(uri.getPath()) && !uri.getPath().isEmpty()) {
            throw new IllegalArgumentException("Linking across databases is not supported");
        }
        if (Objects.isNull(uri.getQuery())) {
            throw new IllegalArgumentException("No query in database request");
        }
        String[] queryArguments = uri.getQuery().split("&");
        String[] key = queryArguments[0].split("=", 2);
        String[] keySegments = key[0].split("\\.");
        if (queryArguments.length > 1 || keySegments.length > 2) {
            throw new IllegalArgumentException("Complex queries is not supported");
        }
        if (keySegments.length > 1 && !"id".equals(keySegments[1])) {
            throw new SecurityException("Filtering on '" + keySegments[1] + "' is not allowed");
        }
        if (!"process".equals(keySegments[0])) {
            throw new ClassCastException("'" + keySegments[0] + "' cannot be cast to 'process'");
        }
        return Integer.parseInt(key[1]);
    }

    /**
     * DMS-Export to a desired location.
     *
     * @param process
     *            object
     * @param exportWithImages
     *            true or false
     * @param exportFullText
     *            true or false
     * @return true or false
     */

    public boolean startDmsExport(Process process, boolean exportWithImages, boolean exportFullText)
            throws IOException {
        LegacyPrefsHelper preferences = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        String atsPpnBand = Helper.getNormalizedTitle(process.getTitle());

        // read document
        LegacyMetsModsDigitalDocumentHelper gdzfile = readDocument(preferences, process);
        if (Objects.isNull(gdzfile)) {
            return false;
        }

        trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

        // validate metadata
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.USE_META_DATA_VALIDATION)
                && !ServiceManager.getMetadataValidationService().validate(gdzfile, preferences, process)) {
            return false;
        }

        Project project = process.getProject();

        // prepare place for save and download
        URI targetDirectory = new File(project.getDmsImportRootPath()).toURI();
        URI userHome = targetDirectory;

        // if necessary, create an operation folder
        if (project.isDmsImportCreateProcessFolder()) {
            targetDirectory = userHome.resolve(File.separator + Helper.getNormalizedTitle(process.getTitle()));
            boolean created = createOperationDirectory(userHome, process);
            if (!created) {
                return false;
            }
        }

        try {
            if (exportWithImages) {
                downloadImages(process, userHome, atsPpnBand, DIRECTORY_SUFFIX);
                downloadFullText(process, userHome, atsPpnBand);
            } else if (exportFullText) {
                downloadFullText(process, userHome, atsPpnBand);
            }
            directoryDownload(process, targetDirectory);
        } catch (RuntimeException e) {
            Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            return false;
        }

        /*
         * zum Schluss Datei an gewünschten Ort exportieren entweder direkt in den
         * Import-Ordner oder ins Benutzerhome anschliessend den Import-Thread starten
         */
        if (project.isUseDmsImport()) {
            if (MetadataFormat.findFileFormatsHelperByName(project.getFileFormatDmsExport()) == MetadataFormat.METS) {
                // if METS, then write by writeMetsFile...
                writeMetsFile(process, userHome + File.separator + atsPpnBand + ".xml", gdzfile);
            } else {
                // ...if not, just write a Fileformat
                gdzfile.write(userHome + File.separator + atsPpnBand + ".xml");
            }

            // if necessary, METS and RDF should be written in the export
            if (MetadataFormat
                    .findFileFormatsHelperByName(project.getFileFormatDmsExport()) == MetadataFormat.METS_AND_RDF) {
                writeMetsFile(process, userHome + File.separator + atsPpnBand + ".mets.xml", gdzfile);
            }

            Helper.setMessage(process.getTitle() + ": ", "DMS-Export started");

            if (!ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITHOUT_TIME_LIMIT)
                    && project.isDmsImportCreateProcessFolder()) {
                // again remove success folder
                File successFile = new File(project.getDmsImportRootPath() + File.separator
                        + Helper.getNormalizedTitle(process.getTitle()));
                fileService.delete(successFile.toURI());
            }
        }
        return true;
    }

    private LegacyMetsModsDigitalDocumentHelper readDocument(LegacyPrefsHelper preferences, Process process)
            throws IOException {
        LegacyMetsModsDigitalDocumentHelper gdzFile;
        LegacyMetsModsDigitalDocumentHelper newFile;
        try {
            URI metadataPath = fileService.getMetadataFilePath(process);
            gdzFile = readMetadataFile(metadataPath, preferences);
            switch (MetadataFormat.findFileFormatsHelperByName(process.getProject().getFileFormatDmsExport())) {
                case METS:
                    newFile = new LegacyMetsModsDigitalDocumentHelper(preferences.getRuleset());
                    break;
                case METS_AND_RDF:
                default:
                    throw new UnsupportedOperationException("Dead code pending removal");
            }
            newFile.setDigitalDocument(gdzFile.getDigitalDocument());
            gdzFile = newFile;
        } catch (RuntimeException e) {
            Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            return null;
        }

        if (handleExceptionsForConfiguration(newFile, process)) {
            return null;
        }
        return gdzFile;
    }

    private boolean createOperationDirectory(URI userHome, Process process) throws IOException {
        // remove old import folder
        if (!fileService.delete(userHome)) {
            Helper.setErrorMessage(Helper.getTranslation(ERROR_EXPORT, Collections.singletonList(process.getTitle())),
                Helper.getTranslation(EXPORT_DIR_DELETE, Collections.singletonList("Import")));
            return false;
        }
        if (!fileService.fileExist(userHome)) {
            fileService.createDirectory(userHome, File.separator + Helper.getNormalizedTitle(process.getTitle()));
        }
        return true;
    }

    /**
     * Method for avoiding redundant code for exception handling. //TODO: should
     * this exceptions be handled that way?
     *
     * @param newFile
     *            as Fileformat
     * @param process
     *            as Process object
     * @return false if no exception appeared
     */
    public boolean handleExceptionsForConfiguration(LegacyMetsModsDigitalDocumentHelper newFile, Process process) {
        Optional<String> rules = ConfigCore.getOptionalString(ParameterCore.COPY_DATA_ON_EXPORT);
        if (rules.isPresent()) {
            try {
                new DataCopier(rules.get()).process(new CopierData(newFile, process));
            } catch (ConfigurationException e) {
                Helper.setErrorMessage("dataCopier.syntaxError", logger, e);
                return true;
            }
        }
        return false;
    }

    /**
     * Run through all metadata and children of given docStruct to trim the strings
     * calls itself recursively.
     *
     * @param docStruct
     *            metadata to be trimmed
     */
    private void trimAllMetadata(LegacyDocStructHelperInterface docStruct) {
        // trim all metadata values
        for (LegacyMetadataHelper md : docStruct.getAllMetadata()) {
            if (Objects.nonNull(md.getValue())) {
                md.setStringValue(md.getValue().trim());
            }
        }

        // run through all children of docStruct
        for (LegacyDocStructHelperInterface child : docStruct.getAllChildren()) {
            trimAllMetadata(child);
        }
    }

    /**
     * Download full text.
     *
     * @param userHome
     *            safe file
     * @param atsPpnBand
     *            String
     */
    private void downloadFullText(Process process, URI userHome, String atsPpnBand) throws IOException {
        downloadSources(process, userHome, atsPpnBand);
        downloadOCR(process, userHome, atsPpnBand);
    }

    private void downloadSources(Process process, URI userHome, String atsPpnBand) throws IOException {
        URI source = fileService.getSourceDirectory(process);
        if (fileService.fileExist(source) && !fileService.getSubUris(source).isEmpty()) {
            URI destination = userHome.resolve(File.separator + atsPpnBand + "_src");
            if (!fileService.fileExist(destination)) {
                fileService.createDirectory(userHome, atsPpnBand + "_src");
            }
            copyProcessFiles(source, destination, null);
        }
    }

    private void downloadOCR(Process process, URI userHome, String atsPpnBand) throws IOException {
        URI ocr = fileService.getOcrDirectory(process);
        if (fileService.fileExist(ocr)) {
            List<URI> directories = fileService.getSubUris(ocr);
            for (URI directory : directories) {
                if (fileService.isDirectory(directory) && !fileService.getSubUris(directory).isEmpty()
                        && fileService.getFileName(directory).contains("_")) {
                    String suffix = fileService.getFileNameWithExtension(directory)
                            .substring(fileService.getFileNameWithExtension(directory).lastIndexOf('_'));
                    URI destination = userHome.resolve(File.separator + atsPpnBand + suffix);
                    if (!fileService.fileExist(destination)) {
                        fileService.createDirectory(userHome, atsPpnBand + suffix);
                    }
                    copyProcessFiles(directory, destination, null);
                }
            }
        }
    }

    /**
     * Download images.
     *
     * @param process
     *            process object
     * @param userHome
     *            save file
     * @param atsPpnBand
     *            String
     * @param directorySuffix
     *            String
     */
    public void downloadImages(Process process, URI userHome, String atsPpnBand, final String directorySuffix)
            throws IOException {
        Project project = process.getProject();

        // determine the output path
        URI tifDirectory = getImagesTifDirectory(true, process.getId(), process.getTitle(),
            process.getProcessBaseUri());

        // copy the source folder to the destination folder
        if (fileService.fileExist(tifDirectory) && !fileService.getSubUris(tifDirectory).isEmpty()) {
            URI destination = userHome.resolve(File.separator + atsPpnBand + directorySuffix);

            // with Agora import simply create the folder
            if (project.isUseDmsImport()) {
                if (!fileService.fileExist(destination)) {
                    fileService.createDirectory(userHome, atsPpnBand + directorySuffix);
                }
            } else {
                // if no Agora import, then create again the folder with user authorization
                User user = ServiceManager.getUserService().getAuthenticatedUser();
                try {
                    fileService.createDirectoryForUser(destination, user.getLogin());
                } catch (RuntimeException e) {
                    Helper.setErrorMessage(ERROR_EXPORT, "could not create destination directory", logger, e);
                }
            }
            copyProcessFiles(tifDirectory, destination, ImageHelper.dataFilter);
        }
    }

    private void copyProcessFiles(URI source, URI destination, FilenameFilter filter) throws IOException {
        List<URI> files = fileService.getSubUris(filter, source);

        for (URI file : files) {
            if (fileService.isFile(file)) {
                URI target = destination.resolve(File.separator + fileService.getFileNameWithExtension(file));
                fileService.copyFile(file, target);
            }
        }
    }

    /**
     * write MetsFile to given Path.
     *
     * @param process
     *            the Process to use
     * @param targetFileName
     *            the filename where the metsfile should be written
     * @param gdzfile
     *            the FileFormat-Object to use for Mets-Writing
     */
    protected boolean writeMetsFile(Process process, String targetFileName, LegacyMetsModsDigitalDocumentHelper gdzfile)
            throws IOException {
        LegacyPrefsHelper preferences = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        LegacyMetsModsDigitalDocumentHelper mm = new LegacyMetsModsDigitalDocumentHelper(preferences.getRuleset());
        // before creating mets file, change relative path to absolute -
        LegacyMetsModsDigitalDocumentHelper dd = gdzfile.getDigitalDocument();
        if (Objects.isNull(dd.getFileSet())) {
            Helper.setErrorMessage(process.getTitle() + ": digital document does not contain images; aborting");
            return false;
        }

        //get the topstruct element of the digital document depending on anchor property
        LegacyDocStructHelperInterface topElement = dd.getLogicalDocStruct();

        //if the top element does not have any image related, set them all
        if (topElement.getAllToReferences("logical_physical").isEmpty()) {
            if (Objects.nonNull(dd.getPhysicalDocStruct()) && dd.getPhysicalDocStruct().getAllChildren().isEmpty()) {
                Helper.setMessage(process.getTitle()
                        + ": topstruct element does not have any referenced images yet; temporarily adding them "
                        + "for mets file creation");
                for (LegacyDocStructHelperInterface mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
                    topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                }
            } else {
                Helper.setErrorMessage(process.getTitle() + ": could not find any referenced images, export aborted");
                return false;
            }
        }

        mm.setDigitalDocument(dd);
        mm.write(targetFileName);
        Helper.setMessage(process.getTitle() + ": ", "exportFinished");
        return true;
    }

    /**
     * Set showClosedProcesses.
     *
     * @param showClosedProcesses
     *            as boolean
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    /**
     * Set showInactiveProjects.
     *
     * @param showInactiveProjects
     *            as boolean
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    /**
     * Get data files.
     *
     * @return String
     */
    public List<URI> getDataFiles(Process process) throws InvalidImagesException {
        URI dir;
        try {
            dir = getImagesTifDirectory(true, process.getId(), process.getTitle(), process.getProcessBaseUri());
        } catch (RuntimeException e) {
            throw new InvalidImagesException(e);
        }

        List<URI> dataList = new ArrayList<>();
        List<URI> files = fileService.getSubUris(ImageHelper.dataFilter, dir);
        if (!files.isEmpty()) {
            dataList.addAll(files);
            Collections.sort(dataList);
        }
        return dataList;
    }

    /**
     * Starts copying all directories configured in kitodo_config.properties
     * parameter "processDirs" to export folder.
     *
     * @param myProcess
     *            the process object
     * @param targetDirectory
     *            the destination directory
     */
    private void directoryDownload(Process myProcess, URI targetDirectory) throws IOException {
        String[] processDirs = ConfigCore.getStringArrayParameter(ParameterCore.PROCESS_DIRS);
        String normalizedTitle = Helper.getNormalizedTitle(myProcess.getTitle());

        for (String processDir : processDirs) {
            URI sourceDirectory = URI.create(getProcessDataDirectory(myProcess).toString() + "/"
                    + processDir.replace(PROCESS_TITLE, normalizedTitle));
            URI destinationDirectory = URI
                    .create(targetDirectory.toString() + "/" + processDir.replace(PROCESS_TITLE, normalizedTitle));

            if (fileService.isDirectory(sourceDirectory)) {
                fileService.copyFile(sourceDirectory, destinationDirectory);
            }
        }
    }

    /**
     * Creates a List of Docket data for the given processes.
     *
     * @param processes
     *            the process to create the docket data for.
     * @return A List of DocketData objects
     */
    private List<DocketData> getDocketData(List<Process> processes) {
        List<DocketData> docketData = new ArrayList<>();
        for (Process process : processes) {
            docketData.add(getDocketData(process));
        }
        return docketData;
    }

    /**
     * Creates the DocketData for a given Process.
     *
     * @param process
     *            The process to create the docket data for.
     * @return The DocketData for the process.
     */
    private DocketData getDocketData(Process process) {
        DocketData docketdata = new DocketData();

        docketdata.setCreationDate(process.getCreationDate().toString());
        docketdata.setProcessId(process.getId().toString());
        docketdata.setProcessName(process.getTitle());
        docketdata.setProjectName(process.getProject().getTitle());
        docketdata.setRulesetName(process.getRuleset().getTitle());
        docketdata.setComment(process.getWikiField());

        if (!process.getTemplates().isEmpty()) {
            docketdata.setTemplateProperties(getDocketDataForProperties(process.getTemplates()));
        }
        if (!process.getWorkpieces().isEmpty()) {
            docketdata.setWorkpieceProperties(getDocketDataForProperties(process.getWorkpieces()));
        }
        docketdata.setProcessProperties(getDocketDataForProperties(process.getProperties()));

        return docketdata;
    }

    private ArrayList<org.kitodo.api.docket.Property> getDocketDataForProperties(List<Property> properties) {
        ArrayList<org.kitodo.api.docket.Property> propertiesForDocket = new ArrayList<>();
        for (Property property : properties) {
            org.kitodo.api.docket.Property propertyForDocket = new org.kitodo.api.docket.Property();
            propertyForDocket.setId(property.getId());
            propertyForDocket.setTitle(property.getTitle());
            propertyForDocket.setValue(property.getValue());

            propertiesForDocket.add(propertyForDocket);

        }

        return propertiesForDocket;
    }

    private List<Map<String, Object>> getMetadataForIndex(Process process) {
        return getMetadataForIndex(process, false);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMetadataForIndex(Process process, boolean forIndexingAll) {
        try {
            URI metadataFileUri = ServiceManager.getFileService().getMetadataFilePath(process, false, true);
            if (!ServiceManager.getFileService().fileExist(metadataFileUri)) {
                logger.info("No metadata file for indexing: {}", metadataFileUri);
                return Collections.emptyList();
            }
            String metadataFile;
            try (InputStream inputStream = ServiceManager.getFileService().readMetadataFile(process, forIndexingAll)) {
                metadataFile = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
            JSONObject xmlJSONObject = XML.toJSONObject(metadataFile);
            Map<String, Object> json = iterateOverJsonObject(xmlJSONObject);
            if (json.keySet().contains("mets")) {
                Map<String, Object> mets = (Map<String, Object>) json.get("mets");
                Object dmdSec = mets.get("dmdSec");
                List<Map<String, Object>> metadata = new ArrayList<>();
                if (dmdSec instanceof List) {
                    metadata = (List<Map<String, Object>>) dmdSec;
                } else if (dmdSec instanceof Map) {
                    metadata.add((Map<String, Object>) dmdSec);
                }
                return metadata;
            }
        } catch (NullPointerException | IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private Map<String, Object> iterateOverJsonObject(JSONObject xmlJSONObject) {
        Iterator<String> keys = xmlJSONObject.keys();
        Map<String, Object> json = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = xmlJSONObject.get(key);
            if (value instanceof String || value instanceof Integer) {
                json.put(prepareKey(key), value);
            } else if (value instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) value;
                Map<String, Object> map = iterateOverJsonObject(jsonObject);
                json.put(prepareKey(key), map);
            } else if (value instanceof JSONArray) {
                json.put(prepareKey(key), iterateOverJsonArray((JSONArray) value));
            }
        }
        return json;
    }

    private Object iterateOverJsonArray(JSONArray jsonArray) {
        int jsonArraySize = jsonArray.length();
        List<Object> json = new ArrayList<>(jsonArraySize);
        for (int i = 0; i < jsonArraySize; i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                json.add(iterateOverJsonObject((JSONObject) value));
            } else if (value instanceof String) {
                json.add(value);
            } else if (value instanceof JSONArray) {
                json.add(iterateOverJsonArray((JSONArray) value));
            }
        }
        return json;
    }

    private String prepareKey(String key) {
        if (key.contains(":")) {
            return key.substring(key.indexOf(':') + 1);
        }
        return key;
    }

    /**
     * Retrieve and return process property value of property with given name
     * 'propertyName' from given ProcessDTO 'process'.
     *
     * @param process
     *            the ProcessDTO object from which the property value is retrieved
     * @param propertyName
     *            name of the property for the property value is retrieved
     * @return property value if process has property with name 'propertyName',
     *         empty String otherwise
     */
    public static String getPropertyValue(ProcessDTO process, String propertyName) {
        for (PropertyDTO property : process.getProperties()) {
            if (property.getTitle().equals(propertyName)) {
                return property.getValue();
            }
        }
        return "";
    }

    /**
     * Calculate and return duration/age of given process as a String.
     *
     * @param process
     *            ProcessDTO object for which duration/age is calculated
     * @return process age of given process
     */
    public static String getProcessDuration(ProcessDTO process) {
        String creationDateTimeString = process.getCreationDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime createLocalDate = LocalDateTime.parse(creationDateTimeString, formatter);
        Duration duration = Duration.between(createLocalDate, LocalDateTime.now());
        return String.format("%sd; %sh", duration.toDays(),
            duration.toHours() - TimeUnit.DAYS.toHours(duration.toDays()));
    }

    /**
     * Updates the linked child processes to the level specified in the root
     * element. Processes linked in the root element are linked in the database.
     * For processes that are not linked in the root element, the link in the
     * database is removed.
     *
     * @param process
     *            parent process
     * @param rootElement
     *            the current state of the root element
     * @throws DAOException
     *             if a process is referenced with a URI whose ID does not
     *             appear in the database
     * @throws DataException
     *             if the process cannot be saved
     */
    public void updateChildrenFromRootElement(Process process, IncludedStructuralElement rootElement)
            throws DAOException, DataException {
        removeLinksFromNoLongerLinkedProcesses(process, rootElement);
        addNewLinks(process, rootElement);
    }

    private void removeLinksFromNoLongerLinkedProcesses(Process process, IncludedStructuralElement rootElement)
            throws DAOException, DataException {
        ArrayList<Process> childrenToRemove = new ArrayList<>(process.getChildren());
        childrenToRemove.removeAll(getProcessesLinkedInIncludedStructuralElement(rootElement));
        for (Process childToRemove : childrenToRemove) {
            childToRemove.setParent(null);
            process.getChildren().remove(childToRemove);
            save(childToRemove);
        }
        if (!childrenToRemove.isEmpty()) {
            save(process);
        }
    }

    private void addNewLinks(Process process, IncludedStructuralElement rootElement)
            throws DAOException, DataException {
        HashSet<Process> childrenToAdd = getProcessesLinkedInIncludedStructuralElement(rootElement);
        childrenToAdd.removeAll(process.getChildren());
        for (Process childToAdd : childrenToAdd) {
            childToAdd.setParent(process);
            process.getChildren().add(childToAdd);
            save(childToAdd);
        }
        if (!childrenToAdd.isEmpty()) {
            save(process);
        }
    }

    private HashSet<Process> getProcessesLinkedInIncludedStructuralElement(
            IncludedStructuralElement includedStructuralElement) throws DAOException {
        HashSet<Process> processesLinkedInIncludedStructuralElement = new HashSet<>();
        if (Objects.nonNull(includedStructuralElement.getLink())) {
            int processId = processIdFromUri(includedStructuralElement.getLink().getUri());
            processesLinkedInIncludedStructuralElement.add(getById(processId));
        }
        for (IncludedStructuralElement child : includedStructuralElement.getChildren()) {
            processesLinkedInIncludedStructuralElement.addAll(getProcessesLinkedInIncludedStructuralElement(child));
        }
        return processesLinkedInIncludedStructuralElement;
    }

    /**
     * Generate process title.
     */
    public static String generateProcessTitle(String atstsl, List<ProcessDetail> processDetails, String titleDefinition,
                                              Process process) throws ProcessGenerationException {
        TitleGenerator titleGenerator = new TitleGenerator(atstsl, processDetails);
        String newTitle = titleGenerator.generateTitle(titleDefinition, null);
        process.setTitle(newTitle);
        // atstsl is created in title generator and next used in tiff header generator
        return titleGenerator.getAtstsl();
    }

    /**
     * Calculate tiff header.
     */
    public static String generateTiffHeader(List<ProcessDetail> processDetails, String atstsl,
                                            String tiffDefinition, String docType) throws ProcessGenerationException {
        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(atstsl, processDetails);
        return tiffHeaderGenerator.generateTiffHeader(tiffDefinition, docType);
    }

    /**
     * Set given Process "parentProcess" as parent of given Process "childProcess" and Process "childProcess" as child
     * of given Process "parentProcess".
     * @param parentProcess
     *          parentProcess of given childProcess
     * @param childProcess
     *          childProcess of given parentProcess
     */
    public static void setParentRelations(Process parentProcess, Process childProcess) {
        childProcess.setParent(parentProcess);
        parentProcess.getChildren().add(childProcess);
    }

    /**
     * Get all parent processes of given Process recursively.
     * @param process the Process to get the parent process for
     * @return List of parent Processes
     */
    public static List<Process> getAllParentProcesses(Process process) {
        List<Process> parents = new ArrayList<>();
        while (Objects.nonNull(process.getParent())) {
            parents.add(0, process.getParent());
            process = process.getParent();
        }
        return parents;
    }

    /**
     * Get the number of direct children of the given process.
     * @param processId id of the process
     * @return number of direct children as int
     * @throws DAOException when query to database fails
     */
    public int getNumberOfChildren(int processId) throws DAOException {
        return Math.toIntExact(countDatabaseRows("SELECT COUNT(*) FROM Process WHERE parent_id = " + processId));
    }

    public static void deleteProcess(int processID) throws DAOException, DataException {
        Process process = ServiceManager.getProcessService().getById(processID);
        deleteProcess(process);
    }

    /**
     * Delete given process.
     *
     * @param processToDelete process to delete
     */
    public static void deleteProcess(Process processToDelete) throws DataException {
        deleteMetadataDirectory(processToDelete);

        processToDelete.getProject().getProcesses().remove(processToDelete);
        processToDelete.setProject(null);
        processToDelete.getTemplate().getProcesses().remove(processToDelete);
        processToDelete.setTemplate(null);
        Process parent = processToDelete.getParent();
        if (Objects.nonNull(parent)) {
            parent.getChildren().remove(processToDelete);
            processToDelete.setParent(null);
            ServiceManager.getProcessService().save(processToDelete);
            ServiceManager.getProcessService().save(parent);
        }
        List<Batch> batches = new CopyOnWriteArrayList<>(processToDelete.getBatches());
        for (Batch batch : batches) {
            batch.getProcesses().remove(processToDelete);
            processToDelete.getBatches().remove(batch);
            ServiceManager.getBatchService().save(batch);
        }
        ServiceManager.getProcessService().remove(processToDelete);
    }

    private static void deleteMetadataDirectory(Process process) {
        for (Task task : process.getTasks()) {
            deleteSymlinksFromUserHomes(task);
        }
        try {
            FileService fileService = ServiceManager.getFileService();
            fileService.delete(ServiceManager.getProcessService().getProcessDataDirectory(process));
            URI ocrDirectory = fileService.getOcrDirectory(process);
            if (fileService.fileExist(ocrDirectory)) {
                fileService.delete(ocrDirectory);
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("errorDirectoryDeleting", new Object[] {Helper.getTranslation("metadata") }, logger,
                    e);
        }
    }

    /**
     * Delete symlinks from user home directories.
     *
     * @param task Task for which symlinks are removed
     */
    public static void deleteSymlinksFromUserHomes(Task task) {
        WebDav webDav = new WebDav();

        for (Role role : task.getRoles()) {
            for (User user : role.getUsers()) {
                try {
                    webDav.uploadFromHome(user, task.getProcess());
                } catch (RuntimeException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
    }

    /**
     * Export METS.
     */
    public static void exportMets(int processId) throws IOException, DAOException {
        Process process = ServiceManager.getProcessService().getById(processId);
        ExportMets export = new ExportMets();
        export.startExport(process);
    }

    public static void downloadToHome(List<Process> processes) throws DAOException {
        WebDav webDav = new WebDav();
        for (Process processForDownload : processes) {
            downloadToHome(webDav, processForDownload.getId());
        }
    }

    /**
     * Download to home for single process. First check if this volume is currently
     * being edited by another user and placed in his home directory, otherwise
     * download.
     *
     * @param webDav
     *            for download
     * @param processId
     *            ID of process for which download is going to be performed
     */
    public static void downloadToHome(WebDav webDav, int processId) throws DAOException {
        Process process = ServiceManager.getProcessService().getById(processId);
        if (ServiceManager.getProcessService().isImageFolderInUse(process)) {
            Helper.setMessage(
                    Helper.getTranslation("directory ") + " " + process.getTitle() + " "
                            + Helper.getTranslation("isInUse"),
                    ServiceManager.getUserService()
                            .getFullName(ServiceManager.getProcessService().getImageFolderInUseUser(process)));
            webDav.downloadToHome(process, true);
        } else {
            webDav.downloadToHome(process, false);
        }
    }
}

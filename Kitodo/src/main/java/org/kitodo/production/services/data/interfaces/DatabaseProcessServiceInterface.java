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

package org.kitodo.production.services.data.interfaces;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;
import org.primefaces.model.SortOrder;

/**
 * Specifies the special database-related functions of the process service.
 */
public interface DatabaseProcessServiceInterface extends SearchDatabaseServiceInterface<Process> {

    /**
     * Returns the number of objects of the implementing type that the filter
     * matches.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     *
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     * @param showClosedProcesses
     *            whether completed processes should be displayed (usually not)
     * @param showInactiveProjects
     *            whether processes of deactivated projects should be displayed
     *            (usually not)
     * @return the number of matching objects
     * @throws DAOException
     *             that can be caused by Hibernate
     * @throws DataException
     *             that can be caused by ElasticSearch
     */
    /*
     * Here, an additional function countResults() is specified with additional
     * parameters, and the generally specified function from
     * SearchDatabaseServiceInterface is not used. However, in
     * DatabaseTemplateServiceInterface, a value is set that affects the
     * generally specified functions countResults() and loadData() in
     * SearchDatabaseServiceInterface. This could be equalized at some point in
     * the future.
     */
    Long countResults(Map<?, String> filters, boolean showClosedProcesses, boolean showInactiveProjects)
            throws DataException;

    /**
     * Finds all processes whose names contain the comparison string.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * If <i>batchMaxSize</i> is configured, at most the configured number of
     * processes will be returned.
     * 
     * <!-- To replace the existing search functionality in
     * BatchForm.filterProcesses(). -->
     * 
     * @param processfilter
     *            substring to search
     * @return all processes whose names contain the comparison string
     * @throws DAOException
     *             in the event of a database connection error
     */
    default List<Process> filterProcesses(String processfilter) throws DAOException {
        int batchMaxSize = ConfigCore.getIntParameter(ParameterCore.BATCH_DISPLAY_LIMIT, -1);
        String query = "FROM Process WHERE title LIKE '%" + processfilter + "%'";
        return batchMaxSize < 1 ? getByQuery(query) : getByQuery(query, Collections.emptyMap(), batchMaxSize);
    }

    /**
     * Finds processes by searchQuery for a number of fields.
     *
     * @param searchQuery
     *            the query word or phrase
     * @return a List of found ProcessInterfaces
     * @throws DataException
     *             when accessing the elasticsearch server fails
     */
    /*
     * Only used in SearchResultForm. SearchResultForm is thrown out in further
     * development. No new implementation required here. (However, the
     * functionality must be provided in countResults() and loadData().)
     */
    default List<ProcessInterface> findByAnything(String searchQuery) throws DataException {
        throw new UnsupportedOperationException("no longer provided this way");
    }

    /**
     * Determines all processes with a specific docket.
     *
     * @param docketId
     *            record number of the docket
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    /*
     * Used in DocketForm to find out whether a docket is used in a process.
     * (Then it may not be deleted.) Is only checked for isEmpty().
     */
    Collection<?> findByDocket(int docketId) throws DataException;

    /**
     * Finds all processes with specific metadata entries.
     *
     * @param metadata
     *            metadata entries that the process must have
     * @param exactMatch
     *            whether all metadata entries must be found in the process,
     *            otherwise at least one
     * @return all found processes
     * @throws DataException
     *             if there was an error during the search, or if the metadata
     *             is not indexed
     */
    /*
     * Used in the import service to locate parent processes. The map contains
     * exactly one entry: a metadata key which is use="recordIdentifier", and as
     * value the remote ID of the parent process (for example its PPN).
     * exactMatch is always true here.
     */
    default List<ProcessInterface> findByMetadata(Map<String, String> metadata, boolean exactMatch)
            throws DataException {
        throw new DataException("index currently not available");
    }

    /**
     * Determines all processes with a specific ruleset.
     *
     * @param rulesetId
     *            record number of the ruleset
     * @return list that is not empty if something was found, otherwise empty
     *         list
     */
    /*
     * Used in RulesetForm to find out whether a ruleset is used in a process.
     * (Then it may not be deleted.) Is only checked for isEmpty().
     */
    Collection<?> findByRuleset(int rulesetId) throws DataException;

    /**
     * Determines all processes with a specific production template.
     *
     * @param templateId
     *            record number of the production template
     * @return list that is not empty if something was found, otherwise empty
     *         list
     * @throws DataException
     *             if an error occurred during the search
     */
    /*
     * Used in TemplateForm to find out whether a production template is used in
     * a process. (Then it may not be deleted.) Is only checked for isEmpty().
     */
    public Collection<?> findByTemplate(int templateId) throws DataException;

    /**
     * Finds all processes with the specified name.
     *
     * @param title
     *            process name to search for
     * @return all processes with the specified name
     * @throws DataException
     *             when there is an error on conversation
     */
    /*
     * Only used in NewspaperProcessesGenerator and only checked there on
     * .isEmpty(), to see if a process title already exists.
     */
    @SuppressWarnings("unchecked")
    default List<ProcessInterface> findByTitle(String title) throws DataException {
        return (List<ProcessInterface>) (List<?>) getByQuery("FROM Process WHERE title = '" + title + "'");
    }

    /**
     * Searches for linkable processes based on user input. A process can be
     * linked if it has the samerule set, belongs to the same client, and the
     * topmost element of the logical outline below the selected parent element
     * is an allowed child.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * For the latter, the data file must be read at the moment. This will be
     * aborted after a timeout so that the user gets an answer (which may be
     * incomplete) in finite time.
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
    List<? extends ProcessInterface> findLinkableChildProcesses(String searchInput, int rulesetId,
            Collection<String> allowedStructuralElementTypes) throws DataException;

    /**
     * Searches for linkable processes based on user input. A process can be
     * linked if it belongs to the same project and has the same ruleset.
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
    List<? extends ProcessInterface> findLinkableParentProcesses(String searchInput, int projectId, int rulesetId)
            throws DataException;

    /**
     * Determines how many processes have a specific identifier.
     *
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     *
     * <!-- Used to check whether a process identifier is already in use. In
     * both places, the result is only checked for > 0. -->
     *
     * @param title
     *            process name to be searched for
     * @return number of processes with this title
     */
    default Long findNumberOfProcessesWithTitle(String title) throws DataException {
        int sessionClientId = ServiceManager.getUserService().getSessionClientId();
        String query = "FROM Process process WHERE process.title = '" + title + "' "
                + "AND process.project.client.id = " + sessionClientId;
        return (long) getByQuery(query).size();
    }

    /**
     * Returns all selected processes. This refers to when a user has ticked
     * "select all", and then optionally deselected individual processes. The
     * function then returns a list of all <i>remaining</i> processes.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working. <b>Use it with caution, only if the
     * number of objects is manageable.</b>
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * 
     * @param showClosedProcesses
     *            whether completed processes should also be displayed (usually
     *            not)
     * @param showInactiveProjects
     *            whether processes from projects that are no longer active
     *            should also be displayed (usually not)
     * @param filter
     *            user input in the filter input field
     * @param excludedProcessIds
     *            IDs of processes individually deselected after "select all"
     * @return the processes found
     * @throws DataException
     *             if an error occurs
     */
    List<? extends ProcessInterface> findSelectedProcesses(boolean showClosedProcesses, boolean showInactiveProjects,
            String filter, Collection<Integer> excludedProcessIds) throws DataException;

    /**
     * Determines the number of processes that match the specified filter
     * criteria.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function counts the data records for the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * @param showClosedProcesses
     *            whether completed processes should also be displayed (usually
     *            not)
     * @param showInactiveProjects
     *            whether processes from projects that are no longer active
     *            should also be displayed (usually not)
     * @param filter
     *            user input in the filter input field
     * @return the number of matching processes
     * @throws DataException
     *             if an error occurs
     */
    default Integer getAmountForFilter(boolean showClosedProcesses, boolean showInactiveProjects, String filter)
            throws DataException {
        return getResultsWithFilter(filter, showClosedProcesses, showInactiveProjects).size();
    }

    /**
     * Returns the type of the top element of the logical structure, and thus
     * the type of the workpiece of the process.
     *
     * @param processId
     *            id of the process whose root type is to be determined
     * @return the type of root element of the logical structure of the
     *         workpiece
     * @throws DataException
     *             if the type cannot be found in the index (e.g. because the
     *             process cannot be found in the index)
     */
    default String getBaseType(int processId) throws DataException {

        final FileService fileService = ServiceManager.getFileService();
        final MetsService metsService = ServiceManager.getMetsService();

        try {
            URI path = fileService.getMetadataFilePath(getById(processId), true, false);
            Workpiece workpiece = metsService.loadWorkpiece(path);
            String baseType = metsService.getBaseType(workpiece);
            return baseType;

        } catch (DAOException | IOException e) {
            throw new DataException(e);
        }
    }

    /**
     * Gets the number of immediate children of the given process.
     * 
     * @param processId
     *            id of the process
     * @return number of immediate children
     * @throws DAOException
     *             when query to database fails
     */
    default int getNumberOfChildren(int processId) throws DAOException {
        return getById(processId).getChildren().size();
    }

    /**
     * Sets the record number of the process into the processBase field.
     *
     * @param process
     *            process for which the data record number should be placed in
     *            the processBase field
     * @return the record number
     */
    default String getProcessDataDirectory(ProcessInterface process) {
        return getProcessDataDirectory((Process) process, false).toString();
    }

    /**
     * Sets the record number of the process into the processBaseUri field. Can
     * also save the process.
     * 
     * <!-- Since the moment this was introduced, I've never understood why this
     * exists. Nor why property processBaseUri exists at all. See #5856 -->
     *
     * @param process
     *            process for which the data record number should be placed in
     *            the processBaseUri field
     * @param forIndexingAll
     *            whether the process should <b>not</b> be saved
     * @return the record number in a URI object
     */
    default URI getProcessDataDirectory(Process process, boolean forIndexingAll) {
        try {
            Integer id = process.getId();
            URI uri = URI.create(id.toString());
            process.setProcessBaseUri(uri);
            if (!forIndexingAll) {
                saveToDatabase(process);
            }
            return uri;
        } catch (DAOException e) {
            LogManager.getLogger(DatabaseProcessServiceInterface.class).error(e.getMessage(), e);
            return URI.create("");
        }
    }

    /**
     * Returns the processes that match the specified filter criteria.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function returns the processes for the client, for which the logged
     * in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * @param filter
     *            user input in the filter input field
     * @param showClosedProcesses
     *            whether completed processes should also be displayed (usually
     *            not)
     * @param showInactiveProjects
     *            whether processes from projects that are no longer active
     *            should also be displayed (usually not)
     * @return the matching processes
     * @throws DataException
     *             if an error occurs
     */
    default List<? extends ProcessInterface> getResultsWithFilter(String filter, boolean showClosedProcesses,
            boolean showInactiveProjects) throws DataException {
        return loadData(0, Integer.MAX_VALUE, "id", SortOrder.ASCENDING,
            Collections.singletonMap(FilterService.FILTER_STRING, filter), showClosedProcesses, showInactiveProjects);
    }

    /**
     * Returns processes to be offered as templates in the selection list. If a
     * user wants to create a larger number of processes that are the same in
     * many metadata, they can create one sample process and then create copies
     * of it.
     * 
     * @return processes to be offered in the choice list
     */
    default List<Process> getTemplateProcesses() throws DataException, DAOException {
        return getByQuery("FROM Process WHERE inChoiceListShown IS true ORDER BY title ASC");
    }

    /**
     * Provides a window onto the process objects. This makes it possible to
     * navigate through the processes page by page, without having to load all
     * objects into memory.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function filters the data according to the client, for which the
     * logged in user is currently working.
     * 
     * <p>
     * <b>Implementation Requirements:</b><br>
     * This function requires that the thread is assigned to a logged-in user.
     * 
     * @param offset
     *            number of objects to be skipped at the list head
     * @param limit
     *            maximum number of objects to return
     * @param sortField
     *            by which column the data should be sorted. Must not be
     *            {@code null} or empty.
     *            <p>
     *            One of:
     *            <ul>
     *            <li>"id": ID</li>
     *            <li>"title.keyword": Process title</li>
     *            <li>"progressCombined": Status</li>
     *            <li>"lastEditingUser": Last editing user</li>
     *            <li>"processingBeginLastTask": Processing begin of last
     *            task</li>
     *            <li>"processingEndLastTask": Processing end of last task</li>
     *            <li>"correctionCommentStatus": Comments</li>
     *            <li>"project.title.keyword": Project</li>
     *            <li>"creationDate": Duration [sic!]</li>
     *            </ul>
     * @param sortOrder
     *            sort ascending or descending?
     * @param filters
     *            a map with exactly one entry, only the value is important, in
     *            which the content of the filter field is passed
     * @param showClosedProcesses
     *            whether completed processes should be displayed (usually not)
     * @param showInactiveProjects
     *            whether processes of deactivated projects should be displayed
     *            (usually not)
     * @return the data objects to be displayed
     * @throws DataException
     *             if processes cannot be loaded from search index
     */
    /*
     * Here, an additional function loadData() is specified with additional
     * parameters, and the generally specified function from
     * SearchDatabaseServiceInterface is not used. However, in
     * DatabaseTemplateServiceInterface, a value is set that affects the
     * generally specified functions countResults() and loadData() in
     * SearchDatabaseServiceInterface. This could be equalized at some point in
     * the future.
     */
    List<? extends ProcessInterface> loadData(int offset, int limit, String sortField, SortOrder sortOrder,
            Map<?, String> filters, boolean showClosedProcesses, boolean showInactiveProjects) throws DataException;

    /**
     * Saves multiple processes in the database.
     * 
     * <p>
     * <b>Implementation Note:</b><br>
     * Each object is stored in its own database transaction.
     *
     * @param list
     *            processes to save
     */
    void saveList(List<Process> list) throws DAOException;

    /**
     * Updates the children linked in the database to those specified in the
     * logical structure. Processes linked in the logical structure are linked
     * in the database. For processes that are not linked in the logical
     * structure, the link in the database is removed.
     *
     * @param process
     *            parent process
     * @param logicalStructure
     *            the current state of the logical structure
     * @throws DAOException
     *             if a process is referenced with a URI whose ID does not
     *             appear in the database
     * @throws DataException
     *             if the process cannot be saved
     */
    void updateChildrenFromLogicalStructure(Process process, LogicalDivision logicalStructure)
            throws DAOException, DataException;

    // === alternative functions that are no longer required ===

    /**
     * Find object in ES and convert it to Interface.
     *
     * @param id
     *            object id
     * @return Interface object
     * @deprecated Use {@link #getById(Integer)}.
     */
    @Deprecated
    default ProcessInterface findById(Integer id) throws DataException {
        try {
            return getById(id);
        } catch (DAOException e) {
            throw new DataException(e);
        }
    }

}

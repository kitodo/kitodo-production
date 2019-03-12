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

package org.kitodo.production.forms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

@Named("SearchResultForm")
@SessionScoped
public class SearchResultForm extends BaseForm {

    private String searchQuery;
    private List<ProcessDTO> filteredList = new ArrayList<>();
    private List<ProcessDTO> resultList;
    private String currentTaskFilter;
    private Integer currentProjectFilter;

    private String searchResultListPath = MessageFormat.format(REDIRECT_PATH, "searchResult");

    /**
     * Searches for processes with the entered searchQuery.
     *
     * @return The searchResultPage
     */
    public String search() {
        ProcessService processService = ServiceManager.getProcessService();
        if (searchQuery.equalsIgnoreCase("all")) {
            try {
                resultList = processService.findAll();
                filteredList.clear();
                filteredList.addAll(resultList);
            } catch (DataException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }
        } else {
            try {
                resultList = processService.findDTOsByTitleWithWildcard(searchQuery);
                resultList.addAll(processService.findByMetadataContent(searchQuery));
                resultList.addAll(processService.findByProjectTitleWithWildcard(searchQuery));
                filteredList.clear();
                filteredList.addAll(resultList);
            } catch (DataException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }
        }
        return searchResultListPath;
    }

    /**
     * Filters the searchResults by project.
     *
     * @param projectId
     *            The project id to be filtered by
     * @return a filtered list
     */
    void filterListByProject(Integer projectId) {
        if(Objects.nonNull(projectId)) {
            for (ProcessDTO result : new ArrayList<>(filteredList)) {
                if (!result.getProject().getId().equals(projectId)) {
                    filteredList.remove(result);
                }
            }
        }
    }

    /**
     * Filters the searchResults by task.
     *
     * @param taskTitle
     *            The title of the task to be filtered by
     * @return a filtered list
     */
    void filterListByTask(String taskTitle) {
        if(Objects.nonNull(taskTitle) && !taskTitle.isEmpty()) {
            for (ProcessDTO processDTO : new ArrayList<>(filteredList)) {
                try {
                    Process process = ServiceManager.getProcessService().getById(processDTO.getId());
                    Task currentTask = ServiceManager.getProcessService().getCurrentTask(process);
                    if (Objects.isNull(currentTask) || !currentTask.getTitle().equals(taskTitle)) {
                        filteredList.remove(processDTO);
                    }
                } catch (DAOException e) {
                    Helper.setErrorMessage("errorOnSearch", searchQuery);

                }

            }
        }
    }

    /**
     * filteres the searchresult list by the selected filters.
     */
    public void filterList(){
        filteredList.clear();
        filteredList.addAll(resultList);
        filterListByProject(currentProjectFilter);
        filterListByTask(currentTaskFilter);
    }


    /**
     * Get all Projects assigned to the search results.
     *
     * @return A list of Projects for filter list
     */
    public Collection<ProjectDTO> getProjectsForFiltering() {
        HashMap<Integer, ProjectDTO> projectsForFiltering = new HashMap<>();
        for (ProcessDTO process : resultList) {
            projectsForFiltering.put(process.getProject().getId(), process.getProject());
        }
        return projectsForFiltering.values();
    }

    /**
     * Get all current Tasks from to the search results.
     *
     * @return A list of Tasks for filter list
     */
    public Collection<TaskDTO> getTasksForFiltering() {
        HashMap<String, TaskDTO> tasksForFiltering = new HashMap<>();
        for (ProcessDTO processDTO : resultList) {
            try {
                Process process = ServiceManager.getProcessService().getById(processDTO.getId());
                Task currentTask = ServiceManager.getProcessService().getCurrentTask(process);
                if (Objects.nonNull(currentTask)) {
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setTitle(currentTask.getTitle());
                    taskDTO.setProcessingStatus(currentTask.getProcessingStatus());
                    tasksForFiltering.put(currentTask.getTitle(), taskDTO);
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }

        }
        return tasksForFiltering.values();
    }

    /**
     * Gets the filtered list.
     *
     * @return a list of ProcessDTO
     */
    public List<ProcessDTO> getFilteredList() {
        return filteredList;
    }

    /**
     * Sets the filtered list.
     *
     * @param filteredList
     *            a list of ProcessDTO
     */
    public void setFilteredList(List<ProcessDTO> filteredList) {
        this.filteredList = filteredList;
    }

    /**
     * Gets the search query.
     *
     * @return the search query
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * sets the searchQuery.
     *
     * @param searchQuery
     *            the query to search for
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Gets the current task filter.
     *
     * @return a List of Task title to filter
     */
    public String getCurrentTaskFilter() {
        return currentTaskFilter;
    }

    /**
     * Sets the current task filter.
     *
     * @param currentTaskFilter
     *            the task title to filter
     */
    public void setCurrentTaskFilter(String currentTaskFilter) {
        this.currentTaskFilter = currentTaskFilter;
    }

    /**
     * Gets the current project filter.
     *
     * @return a List of project id to filter
     */
    public Integer getCurrentProjectFilter() {
        return currentProjectFilter;
    }

    /**
     * Sets the current project filter.
     *
     * @param currentProjectFilter
     *            the project id to filter
     */
    public void setCurrentProjectFilter(Integer currentProjectFilter) {
        this.currentProjectFilter = currentProjectFilter;
    }

}

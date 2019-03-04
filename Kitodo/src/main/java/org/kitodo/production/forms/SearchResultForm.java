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
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("SearchResultForm")
@SessionScoped
public class SearchResultForm extends BaseForm {

    private String searchQuery;
    private List<ProcessDTO> filteredList = new ArrayList<>();
    private List<ProcessDTO> resultList;

    private String searchResultListPath = MessageFormat.format(REDIRECT_PATH, "searchResult");

    public String search(){
        if(searchQuery.equalsIgnoreCase("all")){
            try {
                resultList = ServiceManager.getProcessService().findAll();
                filteredList.clear();
                filteredList.addAll(resultList);
            } catch (DataException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }
        }
        else {
            try {
                resultList = ServiceManager.getProcessService().findByTitle(searchQuery);
                resultList.addAll(ServiceManager.getProcessService().findByProjectTitle(searchQuery));
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
     * @param projectDTO The project to be filtered by
     * @return a filtered list
     */
    public String filterListByProject(ProjectDTO projectDTO){
        filteredList.clear();
        filteredList.addAll(resultList);
            for(ProcessDTO result : resultList){
                if(!result.getProject().getId().equals(projectDTO.getId())){
                    filteredList.remove(result);
                }
            }
        return this.stayOnCurrentPage;
    }


    /**
     * Filters the searchResults by task.
     * @param task The project to be filtered by
     * @return a filtered list
     */
    public String filterListByTask(Task task){
        filteredList.clear();
        filteredList.addAll(resultList);
        for(ProcessDTO result : resultList){
            try {
                Process process = ServiceManager.getProcessService().getById(result.getId());
                Task currentTask = ServiceManager.getProcessService().getCurrentTask(process);
                if(Objects.isNull(currentTask) || !currentTask.getTitle().equals(task.getTitle())){
                    filteredList.remove(result);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }

        }
        return this.stayOnCurrentPage;
    }

    /**
     * Get all Projects assigned to the search results
     * @return A list of Projects for filter list
     */
    public Collection<ProjectDTO> getProjectsForFiltering(){
        HashMap<Integer,ProjectDTO> projectsForFiltering = new HashMap<>();
        for(ProcessDTO process : resultList){
                projectsForFiltering.put(process.getId(),process.getProject());
        }
        return projectsForFiltering.values();
    }

    /**
     * Get all current Tasks from to the search results
     * @return A list of Tasks for filter list
     */
    public Collection<Task> getTasksForFiltering(){
        HashMap<String,Task> tasksForFiltering = new HashMap<>();
        for(ProcessDTO processDTO : resultList){
            try {
                Process process = ServiceManager.getProcessService().getById(processDTO.getId());
                Task currentTask = ServiceManager.getProcessService().getCurrentTask(process);
                if(Objects.nonNull(currentTask)) {
                    tasksForFiltering.put(currentTask.getTitle(),currentTask);
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }

        }
        return tasksForFiltering.values();
    }

    /**
     * Gets the filtered list.
     * @return a list of ProcessDTO
     */
    public List<ProcessDTO> getFilteredList() {
        return filteredList;
    }

    /**
     * Sets the filtered list.
     * @param filteredList a list of ProcessDTO
     */
    public void setFilteredList(List<ProcessDTO> filteredList) {
        this.filteredList = filteredList;
    }

    /**
     * Gets the search query.
     * @return the search query
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * sets the searchQuery
     * @param searchQuery the query to search for
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}

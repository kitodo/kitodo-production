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
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

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
            for(ProcessDTO result : new ArrayList<>(resultList)){
                if(!result.getProject().getId().equals(projectDTO.getId())){
                    filteredList.remove(result);
                }
            }
        return this.stayOnCurrentPage;
    }

    /**
     * Get all Projects assigned to the search results
     * @return A list of Projects for filter list
     */
    public List<ProjectDTO> getProjectsForFiltering(){
        ArrayList<ProjectDTO> projectsForFiltering = new ArrayList<>();
        for(ProcessDTO process : resultList){
                projectsForFiltering.add(process.getProject());
        }
        projectsForFiltering = removeDuplicatesFromProjects(projectsForFiltering);
        return projectsForFiltering;
    }

    private ArrayList<ProjectDTO> removeDuplicatesFromProjects(ArrayList<ProjectDTO> projectsForFiltering) {
        ArrayList<ProjectDTO> projectsForFilteringCopy = new ArrayList<>();
        boolean contains = false;
        for(ProjectDTO project : projectsForFiltering){
            for (ProjectDTO projectDTO : projectsForFilteringCopy){
                if(project.getId().equals(projectDTO.getId())){
                    contains = true;
                    break;
                }
            }
            if(!contains){
                projectsForFilteringCopy.add(project);
            }
            contains = false;
        }
        return projectsForFilteringCopy;
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

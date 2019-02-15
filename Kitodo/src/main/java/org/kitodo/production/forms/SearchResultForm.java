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
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("SearchResultForm")
@RequestScoped
public class SearchResultForm extends BaseForm {

    private String searchQuery;
    private String testQuery = "";
    private List<ProcessDTO> resultList;

    private String searchResultListPath = MessageFormat.format(REDIRECT_PATH, "searchResult");

    public String search(){
        if(searchQuery.equalsIgnoreCase("all")){
            try {
                resultList = ServiceManager.getProcessService().findAll();
            } catch (DataException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }
        }
        else {
            try {
                resultList = ServiceManager.getProcessService().findByTitle(searchQuery);
            } catch (DataException e) {
                Helper.setErrorMessage("errorOnSearch", searchQuery);
                return this.stayOnCurrentPage;
            }
        }
        return searchResultListPath;
    }

    /**
     * Gets the result list.
     * @return the result list
     */
    public List<ProcessDTO> getResultList() {
        System.out.println("getting The result list: " + resultList.get(0).getTitle());
        return resultList;
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
        if(testQuery.isEmpty()){
            testQuery = searchQuery;
        }
    }

    public String getTestQuery() {
        return testQuery;
    }
}

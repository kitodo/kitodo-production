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

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("SearchResultForm")
@RequestScoped
public class SearchResultForm extends BaseForm {

    private String searchQuery;

    private String searchResultListPath = MessageFormat.format(REDIRECT_PATH, "searchResult");

    public String search(){
        return searchResultListPath;
    }

    /**
     * Gets the search query.
     * @return
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * sets the searchQuery
     * @param searchQuery
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}

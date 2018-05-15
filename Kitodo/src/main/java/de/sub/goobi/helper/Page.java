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

package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.User;
import org.kitodo.dto.BaseDTO;

/**
 * This class provides pagination for displaying results from a large result set
 * over a number of pages (i.e. with a given number of results per page). Taken
 * from http://blog.hibernate.org/cgi-bin/blosxom.cgi/2004/08/14#fn.html.
 *
 * @author Gavin King
 * @author Eric Broyles
 */
@Deprecated
public class Page<T extends BaseDTO> implements Serializable { // implements Iterator
    private static final long serialVersionUID = -290320409344472392L;
    private List<T> results;
    private int pageSize = 0;
    private int page = 0;
    private int totalResults = 0;

    /**
     * Construct a new Page with a Criteria. Page numbers are zero-based, so the
     * first page is page 0.
     *
     * @param page
     *            the page number (zero-based)
     */
    public Page(int page, List<T> results) {
        this.page = page;
        this.results = results;
        User currentUser = Helper.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            this.pageSize = 10;
        } else {
            this.pageSize = currentUser.getTableSize();
        }
        this.totalResults = results.size();
    }

    /**
     * Get paginated list of DTO objects with results.
     *
     * @return List of DTO objects
     */
    public List<T> getList() {
        /*
         * Since we retrieved one more than the specified pageSize when the class was
         * constructed, we now trim it down to the pageSize if a next page exists.
         */
        return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
    }

    /**
     * Get complete list of DTO objects with results.
     * 
     * @return List of DTO objects
     */
    public List<T> getCompleteList() {
        return results;
    }

    public int getTotalResults() {
        return this.totalResults;
    }

    /**
     * Get reloaded list of DTO objects.
     *
     * @return List of DTO objects
     */
    @Deprecated
    public List<T> getListReload() {
        /*
         * Since we retrieved one more than the specified pageSize when the class was
         * constructed, we now trim it down to the pageSize if a next page exists.
         */
        if (this.results != null && !this.results.isEmpty()) {
            return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
        } else {
            return new ArrayList<>();
        }
    }

    public boolean hasNextPage() {
        return this.results.size() > this.pageSize;
    }

}

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

import de.sub.goobi.forms.LoginForm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides pagination for displaying results from a large result set
 * over a number of pages (i.e. with a given number of results per page). Taken
 * from http://blog.hibernate.org/cgi-bin/blosxom.cgi/2004/08/14#fn.html.
 *
 * @author Gavin King
 * @author Eric Broyles
 */
public class Page implements Serializable { // implements Iterator
    private static final long serialVersionUID = -290320409344472392L;
    // TODO: Use generics
    @SuppressWarnings("rawtypes")
    private List results;
    private int pageSize = 0;
    private int page = 0;
    private int totalResults = 0;
    private static final Logger logger = LogManager.getLogger(Page.class);

    /**
     * Construct a new Page with a Criteria. Page numbers are zero-based, so the
     * first page is page 0.
     *
     * @param page
     *            the page number (zero-based)
     */
    public Page(int page, List results) {
        this.page = page;
        this.results = results;
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
            this.pageSize = 10;
        } else {
            this.pageSize = login.getMyBenutzer().getTableSize();
        }

        this.totalResults = results.size();

    }

    /**
     * Get last page number.
     *
     * @return int
     */
    public int getLastPageNumber() {
        /*
         * We use the Math.floor() method because page numbers are zero-based
         * (i.e. the first page is page 0).
         */
        int rueckgabe = Double.valueOf(Math.floor(this.totalResults / this.pageSize)).intValue();
        if (this.totalResults % this.pageSize == 0) {
            rueckgabe--;
        }
        return rueckgabe;
    }

    /**
     * Get list.
     *
     * @return List
     */
    // TODO: Use generics
    @SuppressWarnings("rawtypes")
    public List getList() {
        /*
         * Since we retrieved one more than the specified pageSize when the
         * class was constructed, we now trim it down to the pageSize if a next
         * page exists.
         */
        return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
    }

    // TODO: Use generics
    @SuppressWarnings("rawtypes")
    public List getCompleteList() {
        return results;
    }

    public int getTotalResults() {
        return this.totalResults;
    }

    public int getFirstResultNumber() {
        return this.page * this.pageSize + 1;
    }

    public int getLastResultNumber() {
        int fullPage = getFirstResultNumber() + this.pageSize - 1;
        return getTotalResults() < fullPage ? getTotalResults() : fullPage;
    }

    /**
     * Get list reload.
     *
     * @return List
     */
    // TODO: Use generics
    @SuppressWarnings("rawtypes")
    public List getListReload() {
        /*
         * Since we retrieved one more than the specified pageSize when the
         * class was constructed, we now trim it down to the pageSize if a next
         * page exists.
         */
        if (this.results != null && this.results.size() > 0) {
            return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
        } else {
            return new ArrayList();
        }
    }

    /*
     * einfache Navigationsaufgaben
     */

    public boolean isFirstPage() {
        return this.page == 0;
    }

    public boolean isLastPage() {
        return this.page >= getLastPageNumber();
    }

    public boolean hasNextPage() {
        return this.results.size() > this.pageSize;
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.page + 1);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1);
    }

    public String cmdMoveFirst() {
        this.page = 0;
        return "";
    }

    /**
     * Cmd move previous.
     *
     * @return empty String
     */
    public String cmdMovePrevious() {
        if (!isFirstPage()) {
            this.page--;
        }
        return "";
    }

    /**
     * Cmd move next.
     *
     * @return empty String
     */
    public String cmdMoveNext() {
        if (!isLastPage()) {
            this.page++;
        }
        return "";
    }

    /**
     * Cmd move last.
     *
     * @return empty String
     */
    public String cmdMoveLast() {
        this.page = getLastPageNumber();
        return "";
    }

    /**
     * Set txt move to.
     *
     * @param neueSeite
     *            int
     */
    public void setTxtMoveTo(int neueSeite) {
        if (neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
            this.page = neueSeite - 1;
        }
    }

    public int getTxtMoveTo() {
        return this.page + 1;
    }

}

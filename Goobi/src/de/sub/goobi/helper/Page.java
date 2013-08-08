package de.sub.goobi.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import de.sub.goobi.forms.LoginForm;

/**
 * This class provides pagination for displaying results from a large result set over a number of pages (i.e. with a given number of results per
 * page). Taken from http://blog.hibernate.org/cgi-bin/blosxom.cgi/2004/08/14#fn.html.
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
	private Criteria criteria;
	private static final Logger logger = Logger.getLogger(Page.class);

	/** Construct a new Page with a Criteria. Page numbers are zero-based, so the first page is page 0.
	 * 
	 * @param criteria the Hibernate Criteria
	 * 
	 * @param page the page number (zero-based)
	 * 
	 * @param pageSize the number of results to display on the page
	 */
	public Page(Criteria criteria, int page) {
		this.page = page;
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
        	this.pageSize = 10;
		} else {
			this.pageSize = login.getMyBenutzer().getTabellengroesse().intValue();
		}
		this.criteria = criteria;
		try {

			if (criteria instanceof PaginatingCriteria) {
				this.totalResults = ((PaginatingCriteria) criteria).count();
			} else {
				// this case should be avoided, especially if dealing with a large number of Objects
				logger.debug("Page-Object is working with a memory stressing Criteria. Try to replace by PaginatingCriteria, if performance or memory is going down");
				this.totalResults = criteria.list().size();
			}
			
		} catch (HibernateException e) {
			// no hits found, error is thrown
			logger.debug("Failed to get paginated results: " + e);
		}
	}

	public int getLastPageNumber() {
		/*
		 * We use the Math.floor() method because page numbers are zero-based (i.e. the first page is page 0).
		 */
		int rueckgabe = new Double(Math.floor(this.totalResults / this.pageSize)).intValue();
		if (this.totalResults % this.pageSize == 0) {
			rueckgabe--;
		}
		return rueckgabe;
	}

	// TODO: Use generics
	@SuppressWarnings("rawtypes")
	public List getList() {
		/*
		 * Since we retrieved one more than the specified pageSize when the class was constructed, we now trim it down to the pageSize if a next page
		 * exists.
		 */

		return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
	}

	// TODO: Use generics
	@SuppressWarnings("rawtypes")
	public List getCompleteList() {
		return this.criteria.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
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



	// TODO: Use generics
	@SuppressWarnings("rawtypes")
	public List getListReload() {
		/*
		 * Since we retrieved one more than the specified pageSize when the class was constructed, we now trim it down to the pageSize if a next page
		 * exists.
		 */

		if (this.criteria != null) {
			try {
				this.results = this.criteria.setFirstResult(this.page * this.pageSize).setMaxResults(this.pageSize + 1).list();
				if (this.results != null && this.results.size() > 0) {
					List answer = hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;

					return answer;
				} else {
					return new ArrayList();
				}
			} catch (HibernateException e) {
				return this.results;
			}
		}
		return new ArrayList();
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

	public String cmdMovePrevious() {
		if (!isFirstPage()) {
			this.page--;
		}
		return "";
	}

	public String cmdMoveNext() {
		if (!isLastPage()) {
			this.page++;
		}
		return "";
	}

	public String cmdMoveLast() {
		this.page = getLastPageNumber();
		return "";
	}

	public void setTxtMoveTo(int neueSeite) {
		if (neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
			this.page = neueSeite - 1;
		}
	}

	public int getTxtMoveTo() {
		return this.page + 1;
	}

}

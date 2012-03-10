/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on Oct 27, 2004
 */
package dubious.sub.goobi.helper;

//TODO: What's the licence of this file?
import java.io.Serializable;
import java.util.List;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;

import de.sub.goobi.forms.LoginForm;

/**
 * This class provides pagination for displaying results from a large result set
 * over a number of pages (i.e. with a given number of results per page). Taken
 * from http://blog.hibernate.org/cgi-bin/blosxom.cgi/2004/08/14#fn.html.
 * 
 * @author Gavin King
 * @author Eric Broyles
 */
@SuppressWarnings("unchecked")
public class Page implements Serializable {
	private static final long serialVersionUID = -290320409344472392L;
	//TODO: Use generics
	private List results;
	private int pageSize;
	private int page;
	private int totalResults = 0;
	private Criteria criteria;
	private static final Logger logger = Logger.getLogger(Page.class);

	/**
	 * Construct a new Page with a Criteria. Page numbers are zero-based, so the
	 * first page is page 0.
	 * 
	 * @param criteria
	 *            the Hibernate Criteria
	 * @param page
	 *            the page number (zero-based)
	 * @param pageSize
	 *            the number of results to display on the page
	 */
	public Page(Criteria criteria, int page) {
		this.page = page;
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() == null)
			this.pageSize = 10;
		else
			this.pageSize = login.getMyBenutzer().getTabellengroesse().intValue();
		this.criteria = criteria;
		try {

			if (criteria instanceof PaginatingCriteria) {
				totalResults = ((PaginatingCriteria) criteria).count();
			} else {
				//this case should be avoided, especially if dealing with a large number of Objects
				logger.debug("Page-Object is working with a memory stressing Criteria. Try to replace by PaginatingCriteria, if performance or memory is going down");
				totalResults = criteria.list().size();
			}
		} catch (HibernateException e) {
			logger.error("Failed to get paginated results: " + e);
		}
	}

	

	public int getLastPageNumber() {
		/*
		 * We use the Math.floor() method because page numbers are zero-based
		 * (i.e. the first page is page 0).
		 */
		int rueckgabe = new Double(Math.floor(totalResults / pageSize)).intValue();
		if (totalResults % pageSize == 0)
			rueckgabe--;
		return rueckgabe;
	}

	

	//TODO: Use generics
	public List getList() {
		/*
		 * Since we retrieved one more than the specified pageSize when the
		 * class was constructed, we now trim it down to the pageSize if a next
		 * page exists.
		 */
		return hasNextPage() ? results.subList(0, pageSize) : results;
	}

	

	//TODO: Use generics
	public List getCompleteList() {
		return criteria.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
	}

	

	public int getTotalResults() {
		return totalResults;
	}

	

	public int getFirstResultNumber() {
		return page * pageSize + 1;
	}

	

	public int getLastResultNumber() {
		int fullPage = getFirstResultNumber() + pageSize - 1;
		return getTotalResults() < fullPage ? getTotalResults() : fullPage;
	}

	//TODO: Use generics
	public List getListReload() {
		/*
		 * Since we retrieved one more than the specified pageSize when the
		 * class was constructed, we now trim it down to the pageSize if a next
		 * page exists.
		 */
		try {
			results = criteria.setFirstResult(page * pageSize).setMaxResults(pageSize + 1).list();
			return hasNextPage() ? results.subList(0, pageSize) : results;
		} catch (HibernateException e) {
			return results;
		}
	}

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##														einfache Navigationsaufgaben									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	

	public boolean isFirstPage() {
		return page == 0;
	}

	

	public boolean isLastPage() {
		return page >= getLastPageNumber();
	}

	

	public boolean hasNextPage() {
		return results.size() > pageSize;
	}

	

	public boolean hasPreviousPage() {
		return page > 0;
	}

	

	public Long getPageNumberCurrent() {
		return Long.valueOf(page + 1);
	}

	

	public Long getPageNumberLast() {
		return Long.valueOf(getLastPageNumber() + 1);
	}

	

	public String cmdMoveFirst() {
		page = 0;
		return "";
	}

	

	public String cmdMovePrevious() {
		if (!isFirstPage())
			page--;
		return "";
	}

	

	public String cmdMoveNext() {
		if (!isLastPage())
			page++;
		return "";
	}

	

	public String cmdMoveLast() {
		page = getLastPageNumber();
		return "";
	}

	

	public void setTxtMoveTo(int neueSeite) {
		if (neueSeite > 0 && neueSeite <= getLastPageNumber() + 1)
			page = neueSeite - 1;
	}

	

	public int getTxtMoveTo() {
		return page + 1;
	}

}

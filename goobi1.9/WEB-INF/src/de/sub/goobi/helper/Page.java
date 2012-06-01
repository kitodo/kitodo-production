package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Forms.LoginForm;

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

	/**
	 * Construct a new Page. Page numbers are zero-based, so the first page is page 0.
	 * 
	 * @param query
	 *            the Hibernate Query
	 * @param page
	 *            the page number (zero-based)
	 */
	// TODO: REmove this unused constructor
	/*
	 * public Page(Query query, int page) { this.page = page; LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}"); if
	 * (login.getMyBenutzer() == null) this.pageSize = 10; else this.pageSize = login.getMyBenutzer().getTabellengroesse().intValue();
	 * 
	 * try { scrollableResults = query.scroll(); /* We set the max results to one more than the specfied pageSize to determine if any more results
	 * exist (i.e. if there is a next page to display). The result set is trimmed down to just the pageSize before being displayed later (in
	 * getList()).
	 */
	/*
	 * results = query.setFirstResult(page * pageSize).setMaxResults(pageSize + 1).list(); } catch (HibernateException e) { //TODO use a logger.
	 * System.err.println("Failed to get paginated results: " + e.getMessage()); } }
	 * 
	 * 
	 * 
	 * /** Construct a new Page with a Criteria. Page numbers are zero-based, so the first page is page 0.
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
		if (login.getMyBenutzer() == null) {
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
			// ScrollableResults scrollableResults = criteria.scroll();
			// scrollableResults.last();
			// totalResults = scrollableResults.getRowNumber() + 1;

			/*
			 * We set the max results to one more than the specfied pageSize to determine if any more results exist (i.e. if there is a next page to
			 * display). The result set is trimmed down to just the pageSize before being displayed later (in getList()).
			 */
			// results = criteria.setFirstResult(page * pageSize).setMaxResults(pageSize + 1).list();
		} catch (HibernateException e) {
			// no hits found, error is thrown
			logger.debug("Failed to get paginated results: " + e);
		}
	}

	public int getLastPageNumber() {
		/*
		 * We use the Math.floor() method because page numbers are zero-based (i.e. the first page is page 0).
		 */
		// double totalResults = new Integer(getTotalResults()).doubleValue();
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
		// try {
		// getScrollableResults().last();
		// totalResults = getScrollableResults().getRowNumber();
		// } catch (HibernateException e) {
		// System.err.println(
		// "Failed to get last row number from scollable results: "
		// + e.getMessage());
		// }
		return this.totalResults;
	}

	public int getFirstResultNumber() {
		return this.page * this.pageSize + 1;
	}

	public int getLastResultNumber() {
		int fullPage = getFirstResultNumber() + this.pageSize - 1;
		return getTotalResults() < fullPage ? getTotalResults() : fullPage;
	}

	//
	//
	// public int getNextPageNumber() {
	// return page + 1;
	// }
	//
	//
	//
	// public int getPreviousPageNumber() {
	// return page - 1;
	// }

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
					if (answer != null && answer.size()>0) {
						Object objectToTest = answer.get(0);
						if (objectToTest instanceof Schritt || objectToTest instanceof Prozess) {
							Session session = Helper.getHibernateSession();
							for (Object o : answer) {
								// TODO hier prüfen ob valide ID?
								session.refresh(o);
							}
						} 
					}


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
	 * ##################################################### ##################################################### ## ## einfache Navigationsaufgaben
	 * ## ##################################################### ####################################################
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

	/*
	 * 
	 * public boolean hasNext() { return hasNextPage(); }
	 * 
	 * public Object next() { this.page++; return this; }
	 * 
	 * public void remove() { throw new UnsupportedOperationException("Not implemented");
	 * 
	 * }
	 */

}

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2007 Steve Liang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

/**
 * The source for this code was found at the web address http://laingsolutions.com/joomla/index.php?option=com_content&task=view&id=14&Itemid=1 and
 * changed by retaining the class name of the class which was used to instantiate the current instance of PaginatingCriteria
 * 
 * 
 * Implementation of Criteria specifically for paginated searches.
 * 
 * When used with HibernatePaginatedList the count() method produces SELECT count(*) SQL for search to query total number of elements.
 * 
 * 
 * 
 * 
 * @see com.sobyteme.pagination.HibernatePaginatedList
 * @see org.hibernate.Criteria
 * @see org.hibernate.CriteriaImpl
 * 
 * @author Steve
 * 
 */
public class PaginatingCriteria implements Criteria, Serializable {
	private static final long serialVersionUID = 5298336852980154554L;
	// Criteria to be used for results.
	private Criteria criteria;
	// Criteria to be used for count.
	private Criteria clone;
	private String myClassName;

	/**
	 * Constructor. Create 'real' Criteria and clone Criteria to do row count.
	 * 
	 * @param clazz
	 * @param session
	 */
	@SuppressWarnings("rawtypes")
	public PaginatingCriteria(Class clazz, Session session) {
		this.criteria = session.createCriteria(clazz);
		this.clone = session.createCriteria(clazz);
		this.clone.setProjection(Projections.rowCount());
		this.myClassName = clazz.getName();
	}

	/**
	 * Used internally.
	 * 
	 * @param criteria
	 * @param clone
	 */
	private PaginatingCriteria(Criteria criteria, Criteria clone) {
		this.criteria = criteria;
		this.clone = clone;
	}

	/**
	 * @see Criteria#getAlias()
	 * @return String
	 */
	@Override
	public String getAlias() {
		return this.criteria.getAlias();
	}

	/**
	 * @see Criteria#setProjection(org.hibernate.criterion.Projection)
	 * @param projection
	 * @return Criteria
	 */
	@Override
	public Criteria setProjection(Projection projection) {
		return this.criteria.setProjection(projection);
	}

	/**
	 * Adds Criterion to both the internal Criteria instances.
	 * 
	 * @param criterion
	 * @return Criteria
	 * @see Criteria#add(org.hibernate.criterion.Criterion)
	 */
	@Override
	public Criteria add(Criterion criterion) {
		this.clone.add(criterion);
		return this.criteria.add(criterion);
	}

	/**
	 * @param order
	 * @return Criteria
	 * @see Criteria#addOrder(org.hibernate.criterion.Order)
	 */
	@Override
	public Criteria addOrder(Order order) {
		return this.criteria.addOrder(order);
	}

	/**
	 * @param assocationPath
	 * @param mode
	 * @return Criteria
	 * @see Criteria#setFetchMode(java.lang.String, org.hibernate.FetchMode)
	 */
	@Override
	public Criteria setFetchMode(String associationPath, FetchMode mode) throws HibernateException {
		this.clone.setFetchMode(associationPath, mode);
		return this.criteria.setFetchMode(associationPath, mode);
	}

	/**
	 * @param lockMode
	 * @return Criteria
	 * @see Criteria#setLockMode(org.hibernate.LockMode)
	 */
	@Override
	public Criteria setLockMode(LockMode lockMode) {
		return this.criteria.setLockMode(lockMode);
	}

	/**
	 * @param alias
	 * @param lockMode
	 * @return Criteria
	 * @see Criteria#setLockMode(java.lang.String, org.hibernate.LockMode)
	 */
	@Override
	public Criteria setLockMode(String alias, LockMode lockMode) {
		this.clone.setLockMode(alias, lockMode);
		return this.criteria.setLockMode(alias, lockMode);
	}

	/**
	 * @param associationPath
	 * @param alias
	 * @return Criteria
	 * @see Criteria#createAlias(java.lang.String, java.lang.String)
	 */
	@Override
	public Criteria createAlias(String associationPath, String alias) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createAlias(associationPath, alias), this.clone.createAlias(associationPath, alias));
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return Criteria
	 * @see Criteria#createAlias(java.lang.String, java.lang.String, int)
	 */
	@Deprecated
	@Override
	public Criteria createAlias(String arg0, String arg1, int arg2) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createAlias(arg0, arg1, arg2), this.clone.createAlias(arg0, arg1, arg2));
	}

	/**
	 * @param associationPath
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String)
	 */
	@Override
	public Criteria createCriteria(String associationPath) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createCriteria(associationPath), this.clone.createCriteria(associationPath));
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, int)
	 */
	@Deprecated
	@Override
	public Criteria createCriteria(String arg0, int arg1) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createCriteria(arg0, arg1), this.clone.createCriteria(arg0, arg1));
	}

	/**
	 * @param associationPath
	 * @param alias
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, java.lang.String)
	 */
	@Override
	public Criteria createCriteria(String associationPath, String alias) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createCriteria(associationPath, alias), this.clone.createCriteria(associationPath, alias));
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, java.lang.String, int)
	 */
	@Deprecated
	@Override
	public Criteria createCriteria(String arg0, String arg1, int arg2) throws HibernateException {
		return new PaginatingCriteria(this.criteria.createCriteria(arg0, arg1, arg2), this.clone.createCriteria(arg0, arg1, arg2));
	}

	/**
	 * @param resultTransformer
	 * @return Criteria
	 * @see Criteria#setResultTransformer(org.hibernate.transform.ResultTransformer)
	 */
	@Override
	public Criteria setResultTransformer(ResultTransformer resultTransformer) {
		return new PaginatingCriteria(this.criteria.setResultTransformer(resultTransformer), this.clone.setResultTransformer(resultTransformer));
	}

	/**
	 * @param maxResults
	 * @return Criteria
	 * @see Criteria#setMaxResults(int)
	 */
	@Override
	public Criteria setMaxResults(int maxResults) {
		return this.criteria.setMaxResults(maxResults);
	}

	/**
	 * @param firstResult
	 * @return Criteria
	 * @see Criteria#setFirstResult(int)
	 */
	@Override
	public Criteria setFirstResult(int firstResult) {
		return this.criteria.setFirstResult(firstResult);
	}

	/**
	 * @param fetchSize
	 * @return Criteria
	 * @see Criteria#setFetchSize(int)
	 */
	@Override
	public Criteria setFetchSize(int fetchSize) {
		return this.criteria.setFetchSize(fetchSize);
	}

	/**
	 * @param timeout
	 * @return Criteria
	 * @see Criteria#setTimeout(int)
	 */
	@Override
	public Criteria setTimeout(int timeout) {
		this.clone.setTimeout(timeout);
		return this.criteria.setTimeout(timeout);
	}

	/**
	 * @param cacheable
	 * @return Criteria
	 * @see Criteria#setCacheable(boolean)
	 */
	@Override
	public Criteria setCacheable(boolean cacheable) {
		return this.criteria.setCacheable(cacheable);
	}

	/**
	 * @param cacheRegion
	 * @return Criteria
	 * @see Criteria#setCacheRegion(java.lang.String)
	 */
	@Override
	public Criteria setCacheRegion(String cacheRegion) {
		return this.criteria.setCacheRegion(cacheRegion);
	}

	/**
	 * Sets a comment on both internal Criteria instances
	 * 
	 * @param comment
	 * @return Criteria
	 * @see Criteria#setComment(java.lang.String)
	 */
	@Override
	public Criteria setComment(String comment) {
		this.clone.setComment(comment);
		return this.criteria.setComment(comment);
	}

	/**
	 * @param flushMode
	 * @return Criteria
	 * @see Criteria#setFlushMode(org.hibernate.FlushMode)
	 */
	@Override
	public Criteria setFlushMode(FlushMode flushMode) {
		return this.criteria.setFlushMode(flushMode);
	}

	/**
	 * @param cacheMode
	 * @return Criteria
	 * @see Criteria#setCacheMode(org.hibernate.CacheMode)
	 */
	@Override
	public Criteria setCacheMode(CacheMode cacheMode) {
		return this.criteria.setCacheMode(cacheMode);
	}

	/**
	 * @return List
	 * @see Criteria#list()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List list() throws HibernateException {
		return this.criteria.list();
	}

	/**
	 * @return ScrollableResults
	 * @see Criteria#scroll()
	 */
	@Override
	public ScrollableResults scroll() throws HibernateException {
		return this.criteria.scroll();
	}

	/**
	 * @return ScrollableResults
	 * @see Criteria#scroll(org.hibernate.ScrollMode)
	 */
	@Override
	public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
		return this.criteria.scroll(scrollMode);
	}

	/**
	 * @return Object
	 * @throws HibernateException
	 * @see Criteria#uniqueResult()
	 */
	@Override
	public Object uniqueResult() throws HibernateException {
		return this.criteria.uniqueResult();
	}

	/**
	 * Gets the row count applicable for this PaginatingCriteria.
	 * 
	 * @return
	 * @return Integer
	 * @throws HibernateException
	 * @see Criteria#uniqueResult()
	 */
	public Integer count() throws HibernateException {
		Long ur = (Long) this.clone.uniqueResult();
		return new Integer(ur.intValue());
	}

	/**
	 * Returns the class name of the class returned by criteria.list()
	 * 
	 * @return String
	 */
	public String getClassName() {
		return this.myClassName;
	}

	@Override
	public Criteria createAlias(String arg0, String arg1, JoinType arg2) throws HibernateException {
		return null;
	}

	@Override
	public Criteria createAlias(String arg0, String arg1, JoinType arg2, Criterion arg3) throws HibernateException {
		return null;
	}

	@Deprecated
	@Override
	public Criteria createAlias(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, JoinType arg1) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, String arg1, JoinType arg2) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String arg0, String arg1, JoinType arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public Criteria createCriteria(String arg0, String arg1, int arg2, Criterion arg3) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnlyInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Criteria setReadOnly(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

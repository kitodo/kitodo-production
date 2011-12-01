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
import org.hibernate.transform.ResultTransformer;

/**
 * The source for this code was found at the web address
 * http://laingsolutions.com/joomla/index.php?option=com_content&task=view&id=14&Itemid=1
 * and changed by retaining the class name of the class which was used to 
 * instantiate the current instance of PaginatingCriteria
 * 
 * 
 * Implementation of Criteria specifically for paginated searches.

 * When used with HibernatePaginatedList the count() method produces 
 * SELECT count(*) SQL for search to query total number of elements.
 * 


 * 
 * @see com.sobyteme.pagination.HibernatePaginatedList
 * @see org.hibernate.Criteria
 * @see org.hibernate.CriteriaImpl
 * 
 * @author Steve
 *
 */
public class PaginatingCriteria implements Criteria, Serializable{	
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
	@SuppressWarnings("unchecked")
	public PaginatingCriteria(Class clazz, Session session) {
		criteria = session.createCriteria(clazz);
		clone = session.createCriteria(clazz);
		clone.setProjection(Projections.rowCount());
		myClassName = clazz.getName();
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
	public String getAlias() {
		return criteria.getAlias();
	}

	/**
	 * @see Criteria#setProjection(org.hibernate.criterion.Projection)
	 * @param projection
	 * @return Criteria
	 */
	public Criteria setProjection(Projection projection) {
		return criteria.setProjection(projection);
	}

	/**
	 * Adds Criterion to both the internal Criteria instances.
	 * @param criterion
	 * @return Criteria
	 * @see Criteria#add(org.hibernate.criterion.Criterion)
	 */
	public Criteria add(Criterion criterion) {
		clone.add(criterion);
		return criteria.add(criterion);
	}

	/**
	 * @param order
	 * @return Criteria
	 * @see Criteria#addOrder(org.hibernate.criterion.Order)
	 */
	public Criteria addOrder(Order order) {
		return criteria.addOrder(order);
	}

	/**
	 * @param assocationPath
	 * @param mode
	 * @return Criteria
	 * @see Criteria#setFetchMode(java.lang.String, org.hibernate.FetchMode)
	 */
	public Criteria setFetchMode(String associationPath, FetchMode mode) 
            throws HibernateException {
		clone.setFetchMode(associationPath, mode);
		return criteria.setFetchMode(associationPath, mode);
	}

	/**
	 * @param lockMode
	 * @return Criteria
	 * @see Criteria#setLockMode(org.hibernate.LockMode)
	 */
	public Criteria setLockMode(LockMode lockMode) {
		return criteria.setLockMode(lockMode);
	}

	/**
	 * @param alias
	 * @param lockMode
	 * @return Criteria
	 * @see Criteria#setLockMode(java.lang.String, org.hibernate.LockMode)
	 */
	public Criteria setLockMode(String alias, LockMode lockMode) {
		clone.setLockMode(alias, lockMode);
		return criteria.setLockMode(alias, lockMode);
	}

	/**
	 * @param associationPath
	 * @param alias
	 * @return Criteria
	 * @see Criteria#createAlias(java.lang.String, java.lang.String)
	 */
	public Criteria createAlias(String associationPath, String alias) 
            throws HibernateException {
		return new PaginatingCriteria(
                    criteria.createAlias(associationPath, alias), 
                    clone.createAlias(associationPath, alias)
                );
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return Criteria
	 * @see Criteria#createAlias(java.lang.String, java.lang.String, int)
	 */
	public Criteria createAlias(String arg0, String arg1, int arg2) throws HibernateException {
	    return new PaginatingCriteria(
                criteria.createAlias(arg0, arg1, arg2), 
                clone.createAlias(arg0, arg1, arg2)
            );
	}

	/**
	 * @param associationPath
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String)
	 */
	public Criteria createCriteria(String associationPath) throws HibernateException {
	    return new PaginatingCriteria(
                criteria.createCriteria(associationPath), 
                clone.createCriteria(associationPath)
            );
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, int)
	 */
	public Criteria createCriteria(String arg0, int arg1) throws HibernateException {
	    return new PaginatingCriteria(
                criteria.createCriteria(arg0, arg1), 
                clone.createCriteria(arg0, arg1)
            );
	}

	/**
	 * @param associationPath
	 * @param alias
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, java.lang.String)
	 */
	public Criteria createCriteria(String associationPath, String alias) throws HibernateException {
	    return new PaginatingCriteria(
                criteria.createCriteria(associationPath, alias), 
                clone.createCriteria(associationPath, alias)
            );
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return Criteria
	 * @see Criteria#createCriteria(java.lang.String, java.lang.String, int)
	 */
	public Criteria createCriteria(String arg0, String arg1, int arg2) throws HibernateException {
	    return new PaginatingCriteria(
                criteria.createCriteria(arg0, arg1, arg2), 
                clone.createCriteria(arg0, arg1, arg2)
            );
	}

	/**
	 * @param resultTransformer
	 * @return Criteria
	 * @see Criteria#setResultTransformer(org.hibernate.transform.ResultTransformer)
	 */
	public Criteria setResultTransformer(ResultTransformer resultTransformer) {
	    return new PaginatingCriteria(
                criteria.setResultTransformer(resultTransformer), 
                clone.setResultTransformer(resultTransformer)
            );
	}

	/**
	 * @param maxResults
	 * @return Criteria
	 * @see Criteria#setMaxResults(int)
	 */
	public Criteria setMaxResults(int maxResults) {
		return criteria.setMaxResults(maxResults);
	}

	/**
	 * @param firstResult
	 * @return Criteria
	 * @see Criteria#setFirstResult(int)
	 */
	public Criteria setFirstResult(int firstResult) {
		return criteria.setFirstResult(firstResult);
	}

	/**
	 * @param fetchSize
	 * @return Criteria
	 * @see Criteria#setFetchSize(int)
	 */
	public Criteria setFetchSize(int fetchSize) {
		return criteria.setFetchSize(fetchSize);
	}

	/**
	 * @param timeout
	 * @return Criteria
	 * @see Criteria#setTimeout(int)
	 */
	public Criteria setTimeout(int timeout) {
		clone.setTimeout(timeout);
		return criteria.setTimeout(timeout);
	}

	/**
	 * @param cacheable
	 * @return Criteria
	 * @see Criteria#setCacheable(boolean)
	 */
	public Criteria setCacheable(boolean cacheable) {
		return criteria.setCacheable(cacheable);
	}

	/**
	 * @param cacheRegion
	 * @return Criteria
	 * @see Criteria#setCacheRegion(java.lang.String)
	 */
	public Criteria setCacheRegion(String cacheRegion) {
		return criteria.setCacheRegion(cacheRegion);
	}

	/**
	 * Sets a comment on both internal Criteria instances
	 * @param comment
	 * @return Criteria
	 * @see Criteria#setComment(java.lang.String)
	 */
	public Criteria setComment(String comment) {
		clone.setComment(comment);
		return criteria.setComment(comment);
	}

	/**
	 * @param flushMode
	 * @return Criteria
	 * @see Criteria#setFlushMode(org.hibernate.FlushMode)
	 */
	public Criteria setFlushMode(FlushMode flushMode) {
		return criteria.setFlushMode(flushMode);
	}

	/**
	 * @param cacheMode
	 * @return Criteria
	 * @see Criteria#setCacheMode(org.hibernate.CacheMode)
	 */
	public Criteria setCacheMode(CacheMode cacheMode) {
		return criteria.setCacheMode(cacheMode);
	}

	/**
	 * @return List
	 * @see Criteria#list()
	 */
	@SuppressWarnings("unchecked")
	public List list() throws HibernateException {
		return criteria.list();
	}

	/**
	 * @return ScrollableResults
	 * @see Criteria#scroll()
	 */
	public ScrollableResults scroll() throws HibernateException {
		return criteria.scroll();
	}

	/**
	 * @return ScrollableResults
	 * @see Criteria#scroll(org.hibernate.ScrollMode)
	 */
	public ScrollableResults scroll(ScrollMode scrollMode) 
            throws HibernateException {
		return criteria.scroll(scrollMode);
	}

	/**
	 * @return Object
	 * @throws HibernateException
	 * @see Criteria#uniqueResult()
	 */
	public Object uniqueResult() throws HibernateException {
		return criteria.uniqueResult();
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
		return (Integer) clone.uniqueResult();
	}
	
	
	/**
	 * Returns the class name of the class returned by criteria.list()
	 * 
	 * @return String
	 */
	public String getClassName(){
		return myClassName;
	}

}

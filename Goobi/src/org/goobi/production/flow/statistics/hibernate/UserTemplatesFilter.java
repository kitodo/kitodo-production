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

package org.goobi.production.flow.statistics.hibernate;

import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

/**
 * This class of IEvaluable filter implements the template filter
 *
 * @author Wulf Riebensahm
 ****************************************************************************/
public class UserTemplatesFilter implements IEvaluableFilter, Cloneable {
    private static final long serialVersionUID = -4062754600698521285L;
    private boolean clearSession = false;

    public UserTemplatesFilter (boolean clearSession) {
        this.clearSession = clearSession;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getCriteria
     * ()
     */
    @Override
    public Criteria getCriteria() {

        Session session = Helper.getHibernateSession();
        PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
        FilterHelper.criteriaBuilder(session, null, crit, true, null, false, null, clearSession);

        return crit;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'getName()'. This Filter is static");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter
     * (java.lang.String)
     */
    @Override
    public void setFilter(String Filter) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setFilter()'. This Filter is static");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setName
     * (java.lang.String)
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setName()'. This Filter is static");

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setSQL
     * (java.lang.String)
     */
    @Override
    public void setSQL(String sqlString) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setSQL()'. This Filter is static");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public IEvaluableFilter clone() {
        return new UserProcessesFilter(clearSession);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getSourceData() {
        return getCriteria().setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getIDList
     * ()
     */
    @Override
    public List<Integer> getIDList() {
        throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " does not support getIDList()");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getObservable
     * ()
     */
    @Override
    public Observable getObservable() {
        throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " is not observable at this point");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#stepDone
     * ()
     */
    @Override
    public Integer stepDone() {
        throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " does not support stepDone()");
    }

    @Override
    public String stepDoneName() {
        throw new UnsupportedOperationException("The class " + this.getClass().getName() + " does not implement stepDoneName() ");
    }

}

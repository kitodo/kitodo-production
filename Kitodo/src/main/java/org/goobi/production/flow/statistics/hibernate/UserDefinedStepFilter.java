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

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Task;

/**
 * This filter replaces the filter, which was integrated in class
 * AktuelleSchritteForm ... the purpose of refactoring was the goal to access
 * filter functions on the level of processes, which were already implemented in
 * UserDefinedFilter and combine them for the step filter.
 * 
 * @author Wulf Riebensahm
 * 
 */
public class UserDefinedStepFilter implements IEvaluableFilter, Cloneable {

    private static final long serialVersionUID = 7134772860962768932L;
    private String myFilter = null;
    private WeakReference<Criteria> myCriteria = null;
    private ArrayList<Integer> myIds = null;
    private Dispatcher myObservable;
    private Boolean stepOpenOnly = false;
    private boolean userAssignedStepsOnly = false;
    private boolean clearSession = false;

    public UserDefinedStepFilter(boolean clearSession) {
        this.clearSession = clearSession;
    }

    /*
     * setting basic filter modes
     */
    public void setFilterModes(Boolean stepOpenOnly, boolean userAssignedStepsOnly) {
        myCriteria = null;
        this.stepOpenOnly = stepOpenOnly;
        this.userAssignedStepsOnly = userAssignedStepsOnly;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#
     * getCriteria ()
     */
    @Override
    public Criteria getCriteria() {
        // myCriteria is a WeakReference ... both cases needs to be evaluated,
        // after gc the WeakReference
        // object is still referenced but not the object referenced by it
        if (myCriteria == null || myCriteria.get() == null) {
            if (myIds == null) {
                if (myFilter != null) {
                    myCriteria = new WeakReference<>(createCriteriaFromFilterString(myFilter));
                }
            } else {
                myCriteria = new WeakReference<>(createCriteriaFromIDList());
            }
        }

        return myCriteria.get();
    }

    private Criteria createCriteriaFromIDList() {
        Session session = Helper.getHibernateSession();
        Criteria crit = new PaginatingCriteria(Task.class, session);
        crit.add(Restrictions.in("id", myIds));
        return crit;
    }

    private Criteria createCriteriaFromFilterString(String filter) {
        Session session = Helper.getHibernateSession();

        PaginatingCriteria crit = new PaginatingCriteria(Task.class, session);

        /*
         * combine all parameters together this part was exported to
         * FilterHelper so that other Filters could access it
         */

        // following was moved to Filter Helper
        // limitToUserAssignedSteps(crit);

        String message = FilterHelper.criteriaBuilder(session, myFilter, crit, null, stepOpenOnly,
                userAssignedStepsOnly, clearSession);

        if (message.length() > 0) {
            myObservable.setMessage(message);
        }

        return crit;
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
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement getIDList() ");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement getName() ");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#
     * getObservable ()
     */
    @Override
    public Observable getObservable() {

        if (myObservable == null) {
            myObservable = new Dispatcher();
        }
        return myObservable;
    }

    /*
     * this internal class is extending the Observable Class and dispatches a
     * message to the Observers
     */
    private static class Dispatcher extends Observable {

        private void setMessage(String message) {
            super.setChanged();
            super.notifyObservers(message);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter
     * (java.lang.String)
     */
    @Override
    public void setFilter(String filter) {
        myCriteria = null;
        myFilter = filter;
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
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement setName() ");
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
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement setSQL() ");
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
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement stepDone() ");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
     */
    @Override
    public List<Object> getSourceData() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement getSourceData() ");
    }

    @Override
    public UserDefinedStepFilter clone() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement clone() ");

    }

    @Override
    public String stepDoneName() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement stepDoneName() ");
    }

}

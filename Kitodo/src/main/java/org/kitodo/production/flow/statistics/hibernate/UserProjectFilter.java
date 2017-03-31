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

package org.kitodo.production.flow.statistics.hibernate;

import de.sub.kitodo.helper.Helper;
import de.sub.kitodo.helper.PaginatingCriteria;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Process;

public class UserProjectFilter implements IEvaluableFilter, Cloneable {

    private static final long serialVersionUID = 441692997066826360L;

    private Integer projectID;
    // private Criteria myCriteria = null;
    private WeakReference<Criteria> myCriteria = null;
    private List<Integer> myIds = null;
    private Dispatcher myObservable;
    private String step = "";

    public UserProjectFilter(Integer projectID) {
        this.projectID = projectID;
    }

    @Override
    public Criteria getCriteria() {

        if (myCriteria == null || myCriteria.get() == null) {
            if (myIds == null) {
                if (projectID != null) {
                    myCriteria = new WeakReference<Criteria>(createCriteriaFromProjectID());
                }
            } else {
                myCriteria = new WeakReference<Criteria>(createCriteriaFromIDList());
            }
        }

        return myCriteria.get();
    }

    private Criteria createCriteriaFromProjectID() {
        Session session = Helper.getHibernateSession();
        PaginatingCriteria crit = new PaginatingCriteria(Process.class, session);
        crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        crit.createCriteria("project", "proj");
        crit.add(Restrictions.eq("proj.id", projectID));
        return crit;
    }

    /**
     * filter processes by id.
     */
    private PaginatingCriteria createCriteriaFromIDList() {
        Session session = Helper.getHibernateSession();
        PaginatingCriteria crit = new PaginatingCriteria(Process.class, session);
        crit.add(Restrictions.in("id", myIds));
        return crit;
    }

    @SuppressWarnings("unchecked")
    private void createIDListFromCriteria(Criteria crit) {
        myIds = new ArrayList<Integer>();
        for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it
                .hasNext();) {
            Process p = (Process) it.next();
            myIds.add(p.getId());
            myCriteria = null;
        }
    }

    @Override
    public List<Integer> getIDList() {
        createIDListFromCriteria(getCriteria());
        return myIds;
    }

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

    @Override
    public void setFilter(String filter) {
        step = filter;

    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement setName() ");
    }

    @Override
    public void setSQL(String sqlString) {
        // TODO Auto-generated method stub

    }

    @Override
    public Integer stepDone() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement stepDone() ");
    }

    @Override
    public List<Object> getSourceData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEvaluableFilter clone() {
        throw new UnsupportedOperationException(
                "The class " + this.getClass().getName() + " does not implement clone() ");
    }

    @Override
    public String stepDoneName() {
        return step;
    }

    /*
     * this internal class is extending the Observable Class and dispatches a
     * message to the Observers
     */
    private static class Dispatcher extends Observable {

        @SuppressWarnings("unused")
        private void setMessage(String message) {
            super.setChanged();
            super.notifyObservers(message);
        }
    }
}

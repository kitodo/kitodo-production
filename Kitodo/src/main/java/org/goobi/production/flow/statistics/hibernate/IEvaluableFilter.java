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

import org.goobi.production.flow.statistics.IDataSource;
import org.hibernate.Criteria;

/**
 * This interface defines a filter, which can be used in the goobi/hibernate
 * context. It may be implemented serializable so it could be saved and loaded.
 * It manages the creation and building of a criteria according to the users or
 * programmers input, holding the result.
 * 
 * @author Wulf Riebensahm
 */
public interface IEvaluableFilter extends IDataSource {

    /**
     * As an option name could be set, so that user could select filter by name in
     * case this feature is going to be implemented in later versions.
     * 
     * @param name
     *            as String
     */
    void setName(String name);

    /**
     * Get name.
     * 
     * @return name
     */
    String getName();

    /**
     * Get criteria.
     * 
     * @return Criteria based on the implemented filter
     */
    Criteria getCriteria();

    /**
     * Allows the creation of a second filter, independent from the original one.
     */
    IEvaluableFilter clone();

    /**
     * Set filter.
     * 
     * @param filter
     *            allows passing on a String which may define a filter
     */
    void setFilter(String filter);

    /**
     * Set SQL String.
     * 
     * @param sqlString
     *            allows passing on a String which may set an sql statement
     */
    void setSQL(String sqlString);

    /**
     * Get list of ids.
     * 
     * @return List containing all ID's from selected filter
     */
    List<Integer> getIDList();

    /**
     * Get observable.
     * 
     * @return Observable, exposing an Observable Object to register an Observer
     */

    Observable getObservable();

    /**
     * Get step done for exact filter.
     * 
     * @return Integer step if an exact stepDone filter is set (needed for Statistic
     *         AP2)
     */
    Integer stepDone();

    String stepDoneName();

}

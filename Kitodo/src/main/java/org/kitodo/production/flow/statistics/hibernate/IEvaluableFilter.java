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

import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.kitodo.production.flow.statistics.IDataSource;

/**
 * This interface defines a filter, which can be used in the kitodo/hibernate
 * context. It may be implemented serializable so it could be saved and loaded.
 * It manages the creation and building of a criteria according to the users or
 * programmers input, holding the result.
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public interface IEvaluableFilter extends IDataSource {

    /**
     * As an option name could be set, so that user could select filter by name
     * in case this feature is going to be implemented in leter versions
     ****************************************************************************/
    public void setName(String name);

    /**
     * @return name
     */
    public String getName();

    /**
     * @return Criteria based on the implemented filter
     */
    public Criteria getCriteria();

    /**
     * allows the creation of a second filter, independent from the original one
     */
    public IEvaluableFilter clone();

    /**
     * @param Filter
     *            - allows passing on a String which may define a filter
     * 
     */
    public void setFilter(String Filter);

    /**
     * @param sqlString
     *            allows passing on a String which may set an sql statement
     */
    public void setSQL(String sqlString);

    /**
     * @return List containing all ID's from selected filter
     */
    public List<Integer> getIDList();

    /**
     * 
     * @return Observable, exposing an Observable Object to register an Observer
     */

    public Observable getObservable();

    /**
     * 
     * @return Integer step if an exact stepDone filter is set (needed for
     *         Statistic AP2)
     */
    public Integer stepDone();

    public String stepDoneName();

}

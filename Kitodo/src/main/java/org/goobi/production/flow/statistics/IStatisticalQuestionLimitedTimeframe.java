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

package org.goobi.production.flow.statistics;

import java.util.Date;

/**
 * Extension of {@link IStatisticalQuestion}. Statistical request limited by
 * time frame.
 * 
 */
public interface IStatisticalQuestionLimitedTimeframe extends IStatisticalQuestion {

    /**
     * Define time frame.
     * 
     * @param timeFrom
     *            Date - from
     * @param timeTo
     *            Date - to
     */
    public void setTimeFrame(Date timeFrom, Date timeTo);

}

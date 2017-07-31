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

import java.io.Serializable;
import java.util.List;

/**
 * This interface defines some general dataset for all future kind of
 * statistical questions.
 * 
 * @author Wulf Riebensahm
 * @author Steffen Hankiewicz
 */
public interface IDataSource extends Serializable {

    /**
     * Get the original Data and enables the continued use of older Statistic
     * functions in the restructured data flow.
     * 
     * @return the original Data
     */
    List<Object> getSourceData();

}

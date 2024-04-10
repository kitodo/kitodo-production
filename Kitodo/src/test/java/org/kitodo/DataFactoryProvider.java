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

package org.kitodo;

import org.kitodo.data.interfaces.DataFactoryInterface;
import org.kitodo.production.dto.DTOFactory;

/**
 * Provider for the data factory used in the tests. This way, if you change the
 * implementation, only one location in the code needs to be changed.
 */
public class DataFactoryProvider {

    DataFactoryInterface instance = DTOFactory.instance();

    /**
     * Returns the data factory.
     * 
     * @return the data factory
     */
    DataFactoryInterface getDataFactory() {
        return instance;
    }
}

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

package org.kitodo.api.externaldatamanagement;

import java.util.EventListener;

public interface ExternalDataListener extends EventListener {

    /**
     * Is called, if external data has changed.
     *
     * @param event the event with the information of the changed data
     */
    void externalDataUpdated(ExternalDataEvent event);

}

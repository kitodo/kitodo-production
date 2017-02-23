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

import java.util.ArrayList;

public interface ExternalDataManagementInterface {

    /**
     * Searches for Data in a given source by term and field.
     *
     * @param field The field, to search for the term.
     * @param term The search term.
     * @param source The source to search in.
     * @return A list of result data.
     */
    public ArrayList<ImportData> getData(String field, String term, Source source);

    /**
     * A method from the listener pattern, adds a listener.
     *
     * @param listener The listener which should be notified, if external data is updated.
     */
    public void addListener(ExternalDataListener listener);

    /**
     * A method from the listener pattern, removes a listener.
     *
     * @param listener The listener, which should be removed.
     */
    public void removeListener(ExternalDataListener listener);

    /**
     * Notifies all listeners, that external data has changed.
     *
     * @param event The externalDataEvent.
     */
    public void notifyExternalDataListener(ExternalDataEvent event);

}

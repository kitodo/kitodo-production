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

import java.util.Collection;

import javax.swing.event.EventListenerList;

/**
 * Manages the import of data from an external source.
 */
public interface ExternalDataImportInterface {

    EventListenerList externalDataListeners = new EventListenerList();

    /**
     * Searches for Data in a given source by term and field.
     *
     * @param field
     *            The field, to search for the term.
     * @param term
     *            The search term.
     * @param source
     *            The source to search in.
     * @return A list of result data.
     */
    Collection<ImportData> getEntry(String field, String term, Source source);

    /**
     * Searches for Data in a given source by term and field.
     *
     * @param ids
     *            The ids from the entries to get.
     * @param source
     *            The source to search in.
     * @return A list of result data.
     */
    Collection<ImportData> getMultipleEntriesById(Collection<String> ids, Source source);
}

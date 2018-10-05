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

import org.w3c.dom.Document;

/**
 * Manages the import of data from an external source.
 */
public interface ExternalDataImportInterface {

    EventListenerList externalDataListeners = new EventListenerList();

    /**
     * Get the full record with the given ID from the catalog.
     *
     * @param catalogId
     *            The ID of the catalog that will be queried.
     * @param id
     *            The ID of the record that will be imported.
     * @return The queried record transformed into Kitodo internal format.
     */
    Document getFullRecordById(String catalogId, String id);

    /**
     * Perform search in catalog with given ID 'catalogId' Map 'searchTerms', which
     * contains search fields as keys and search terms as values. The parameter rows
     * controls how many records should be returned.
     *
     * @param catalogId
     *            ID of the catalog that will be queried.
     * @param field
     *            search field that will be queried
     * @param term
     *            value of search field that will be queried
     * @param rows
     *            number of records to be returned
     * @return Search result of performed query.
     */
    SearchResult search(String catalogId, String field, String term, int rows);

    /**
     * Searches for Data in a given source by term and field.
     *
     * @param ids
     *            The ids from the entries to get.
     * @param catalogId
     *            ID of the catalog that will be queried.
     * @return A list of result data.
     */
    Collection<Document> getMultipleEntriesById(Collection<String> ids, String catalogId);
}

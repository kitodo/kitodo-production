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

package de.sub.goobi.helper;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.data.base.SearchService;

public class IndexWorker implements Runnable {

    private int indexedObjects = 0;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(IndexWorker.class);

    /**
     * Constructor initializing an IndexWorker object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *            SearchService instance used for indexing
     */
    public IndexWorker(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        this.indexedObjects = 0;
        try {
            for (Object object : searchService.getAll()) {
                this.searchService.saveToIndex((BaseBean) object);
                this.indexedObjects++;
            }
        } catch (CustomResponseException | IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Return the number of objects that have already been indexed during the
     * current indexing process.
     *
     * @return int the number of objects indexed during the current indexing run
     */
    public int getIndexedObjects() {
        return indexedObjects;
    }
}

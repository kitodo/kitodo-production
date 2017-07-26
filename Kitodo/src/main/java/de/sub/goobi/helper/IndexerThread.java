package de.sub.goobi.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.data.base.SearchService;

import java.io.IOException;
import java.util.List;

public class IndexerThread<T> implements Runnable {

    private int indexedObjects;
    private List<BaseBean> entryList;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(IndexerThread.class);

    /**
     * Constructor initializing an IndexerThread object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *      SearchService instance used to index the entries in the given 'entries' list.
     * @param entries
     *      List of 'entries' that will be indexed.
     */
    public IndexerThread(SearchService searchService, List<BaseBean> entries) {
        this.searchService = searchService;
        this.indexedObjects = 0;
        this.entryList = entries;
    }

    @Override
    public void run() {
        this.indexObjects();
    }

    /**
     * Return the number of objects that have already been indexed during the current indexing process.
     *
     * @return int
     *      the number of objects indexed during the current indexing run
     */
    public int getIndexedObjects() {
        return this.indexedObjects;
    }

    /**
     * Return the number objects that will be indexed by this IndexerThread instance.
     *
     * @return
     *      the number of objects that are to be indexed by this IndexerThread
     */
    public int getObjectCount() {
        return this.entryList.size();
    }

    /**
     * Return the progress in percent of the currently running indexing process.
     * If the list of entries to be indexed is empty, this will return "0".
     *
     * @return
     *      the progress of the current indexing process in percent
     */
    public int getIndexingProgress() {
        if (this.entryList.size() > 0) {
            return (int) ((this.indexedObjects / (float)this.entryList.size()) * 100);
        }
        else {
            return 0;
        }

    }

    private void indexObjects(){
        this.indexedObjects = 0;
        try {
            for(BaseBean object : entryList) {
                this.searchService.saveToIndex(object);
                this.indexedObjects++;
            }
        } catch (CustomResponseException | IOException e) {
            logger.error(e.getMessage());
        }
    }
}

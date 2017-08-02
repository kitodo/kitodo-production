package de.sub.goobi.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.data.base.SearchService;

import java.io.IOException;
import java.util.List;

public class IndexWorker<T> implements Runnable {

    private int indexedObjects;
    private List<BaseBean> entryList;
    private SearchService searchService;
    private static final Logger logger = LogManager.getLogger(IndexWorker.class);

    /**
     * Constructor initializing an IndexWorker object with the given SearchService
     * and list of objects that will be indexed.
     *
     * @param searchService
     *      SearchService instance used to index the entries in the given 'entries' list.
     * @param entries
     *      List of 'entries' that will be indexed.
     */
    public IndexWorker(SearchService searchService, List<BaseBean> entries) {
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

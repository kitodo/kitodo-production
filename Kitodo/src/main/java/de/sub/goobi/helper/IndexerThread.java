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

    public IndexerThread(SearchService searchService, List<BaseBean> entries) {
        this.searchService = searchService;
        this.indexedObjects = 0;
        this.entryList = entries;
    }

    @Override
    public void run() {
        this.indexObjects();
    }

    public int getIndexedObjects() {
        return this.indexedObjects;
    }

    public int getObjectCount() {
        return this.entryList.size();
    }

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

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

package org.kitodo.data.elasticsearch.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.elasticsearch.MockEntity;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;

/**
 * Test class for Searcher.
 */
public class SearcherTest {

    @BeforeClass
    public static void prepareIndex() throws IOException, ResponseException {
        IndexRestClient indexRestClient = initializeIndexRestClient();
        indexRestClient.addDocument(MockEntity.createEntities().get(1), 1);
        indexRestClient.addDocument(MockEntity.createEntities().get(2), 2);
    }

    @Test
    public void shouldGetDocumentById() throws Exception {
        Searcher searcher = new Searcher("testget");
        SearchResult result = searcher.findDocument(1);

        boolean condition = result.getId().equals(1) == result.getProperties().get("title").equals("Batch1");
        assertTrue("Incorrect result - id or title don't match to given plain text!", condition);
    }

    @Test
    public void shouldGetDocumentByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testget");

        String query = "{\n\"query\" : {\n\"match_all\" : {}\n}\n}";
        SearchResult result = searcher.findDocument(query);
        Integer id = result.getId();
        assertEquals("Incorrect result - id doesn't match to given number!", 2, id.intValue());
        String title = result.getProperties().get("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch2", title);

        query = "{\n\"query\" : {\n\"match\" : {\n\"title\" : \"Batch1\"}\n}\n}";
        result = searcher.findDocument(query);
        id = result.getId();
        assertEquals("Incorrect result - id doesn't match to given plain text!", 1, id.intValue());
        title = result.getProperties().get("title");
        assertEquals("Incorrect result - title doesn't match to given plain text!", "Batch1", title);
    }

    @Test
    public void shouldGetDocumentsByQuery() throws Exception {
        Thread.sleep(2000);
        Searcher searcher = new Searcher("testget");

        String query = "{\n\"query\" : {\n\"match_all\" : {}\n}\n}";
        ArrayList<SearchResult> result = searcher.findDocuments(query);
        Integer id = result.get(0).getId();
        assertEquals("Incorrect result - id doesn't match to given int values!", 2, id.intValue());

        int size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 2, size);

        query = "{\n\"query\" : {\n\"match\" : {\n\"title\" : \"Batch1\"}\n}\n}";
        result = searcher.findDocuments(query);
        id = result.get(0).getId();
        assertEquals("Incorrect result - id doesn't match to given int values!", 1, id.intValue());

        size = result.size();
        assertEquals("Incorrect result - size doesn't match to given int value!", 1, size);
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = new IndexRestClient();
        restClient.initiateClient();
        restClient.setIndex("kitodo");
        restClient.setType("testget");
        return restClient;
    }
}

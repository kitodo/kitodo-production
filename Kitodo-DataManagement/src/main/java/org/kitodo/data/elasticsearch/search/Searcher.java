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

import java.io.IOException;
import java.util.*;

import javax.persistence.Table;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kitodo.data.elasticsearch.Index;

/**
 * Implementation of Elastic Search Searcher for Kitodo - Data Management
 * Module.
 */
public class Searcher extends Index {

    /**
     * Constructor for searcher with type names equal to table names.
     *
     * @param beanClass
     *            as Class
     */
    public Searcher(Class<?> beanClass) {
        super(beanClass);
    }

    /**
     * Constructor for searcher with type names not equal to table names.
     *
     * @param type
     *            as String
     */
    public Searcher(String type) {
        super(type);
    }

    /**
     * Count amount of documents responding to given query.
     * 
     * @param query
     *            of searched documents
     * @return amount of documents as Long
     */
    public Long countDocuments(String query) throws IOException, ParseException {
        SearchRestClient restClient = initiateRestClient();
        JSONParser parser = new JSONParser();

        String response = restClient.countDocuments(query);
        JSONObject result = (JSONObject) parser.parse(response);
        return (Long) result.get("count");
    }

    /**
     * Find document by id.
     *
     * @param id
     *            of searched document
     * @return search result
     */
    public SearchResult findDocument(Integer id) throws IOException, ParseException {
        SearchRestClient restClient = initiateRestClient();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(id);
        JSONObject result = (JSONObject) parser.parse(response);
        return convertJsonStringToSearchResult(result);
    }

    /**
     * Find document by query. It returns only first found document (last
     * inserted!).
     *
     * @param query
     *            as String
     * @return search result
     */
    public SearchResult findDocument(String query) throws IOException, ParseException {
        SearchRestClient restClient = initiateRestClient();
        SearchResult searchResult = new SearchResult();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(query);
        JSONObject jsonObject = (JSONObject) parser.parse(response);
        if (jsonObject.containsKey("hits")) {
            JSONObject hits = (JSONObject) jsonObject.get("hits");
            JSONArray inHits = (JSONArray) hits.get("hits");
            if (!inHits.isEmpty()) {
                searchResult = convertJsonStringToSearchResult((JSONObject) inHits.get(0));
            }
        } else {
            searchResult = convertJsonStringToSearchResult(jsonObject);
        }
        return searchResult;
    }

    /**
     * Find many documents by query.
     *
     * @param query
     *            as String
     * @return list of SearchResult objects
     */
    public List<SearchResult> findDocuments(String query) throws IOException, ParseException {
        SearchRestClient restClient = initiateRestClient();
        List<SearchResult> searchResults = new ArrayList<>();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(query);
        JSONObject jsonObject = (JSONObject) parser.parse(response);
        if (jsonObject.containsKey("hits")) {
            JSONObject hits = (JSONObject) jsonObject.get("hits");
            JSONArray inHits = (JSONArray) hits.get("hits");
            if (!inHits.isEmpty()) {
                for (Object hit : inHits) {
                    searchResults.add(convertJsonStringToSearchResult((JSONObject) hit));
                }
            }
        } else {
            searchResults.add(convertJsonStringToSearchResult(jsonObject));
        }
        return searchResults;
    }

    @SuppressWarnings("unchecked")
    private SearchResult convertJsonStringToSearchResult(JSONObject jsonObject) {
        SearchResult searchResult = new SearchResult();

        searchResult.setId(Integer.valueOf(jsonObject.get("_id").toString()));
        HashMap<String, String> properties = new HashMap<>();
        JSONObject result = (JSONObject) jsonObject.get("_source");
        Set<Map.Entry<String, String>> entries = result.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            properties.put(entry.getKey(), entry.getValue());
        }
        searchResult.setProperties(properties);

        return searchResult;
    }

    private SearchRestClient initiateRestClient() {
        SearchRestClient restClient = new SearchRestClient();
        restClient.initiateClient();
        restClient.setIndex(index);
        restClient.setType(type);
        return restClient;
    }
}

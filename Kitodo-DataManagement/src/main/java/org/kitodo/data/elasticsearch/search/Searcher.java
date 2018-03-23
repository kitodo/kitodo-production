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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.kitodo.data.elasticsearch.Index;
import org.kitodo.data.exceptions.DataException;

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
     * Count amount of all documents stored in index.
     *
     * @return amount of all documents
     */
    public Long countDocuments() throws DataException {
        return countDocuments(null);
    }

    /**
     * Count amount of documents responding to given query.
     *
     * @param query
     *            of searched documents
     * @return amount of documents as Long
     */
    public Long countDocuments(String query) throws DataException {
        SearchRestClient restClient = initiateRestClient();

        String response = restClient.countDocuments(overrideNullQuery(query));
        if (!response.equals("")) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
                JsonObject result = jsonReader.readObject();
                return result.getJsonNumber("count").longValue();
            }
        } else {
            return 0L;
        }
    }

    /**
     * Aggregate documents responding to given query.
     *
     * @param query
     *            of searched documents
     * @param aggregation
     *            condition as String
     * @return aggregate documents as JSONObject
     */
    public JsonObject aggregateDocuments(String query, String aggregation) throws DataException {
        SearchRestClient restClient = initiateRestClient();

        String response = restClient.aggregateDocuments(overrideNullQuery(query), aggregation);
        if (!response.equals("")) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
                JsonObject result = jsonReader.readObject();
                return result.getJsonObject("aggregations");
            }
        } else {
            return Json.createObjectBuilder().build();
        }
    }

    private String overrideNullQuery(String query) {
        if (query == null) {
            return "{\n\"match_all\" : {}\n}";
        }
        return query;
    }

    /**
     * Find document by id.
     *
     * @param id
     *            of searched document
     * @return JSONObject
     */
    public JsonObject findDocument(Integer id) throws DataException {
        SearchRestClient restClient = initiateRestClient();

        String response = restClient.getDocument(id);
        if (!response.equals("")) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
                return jsonReader.readObject();
            }
        } else {
            return Json.createObjectBuilder().build();
        }
    }

    /**
     * Find document by query. It returns only first found document (last
     * inserted!).
     *
     * @param query
     *            as String
     * @return search result
     */
    public JsonObject findDocument(String query) throws DataException {
        return findDocument(query, null);
    }

    /**
     * Find document by query. It returns only first found document (last inserted
     * or first matching to sort condition!).
     *
     * @param query
     *            as String
     * @return search result
     */
    public JsonObject findDocument(String query, String sort) throws DataException {
        SearchRestClient restClient = initiateRestClient();

        String response = restClient.getDocument(query, sort, 0, 1);
        try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
            JsonObject jsonObject = jsonReader.readObject();
            if (jsonObject.containsKey("hits")) {
                JsonObject hits = jsonObject.getJsonObject("hits");
                JsonArray inHits = hits.getJsonArray("hits");
                if (!inHits.isEmpty()) {
                    return inHits.getJsonObject(0);
                }
            } else {
                return jsonObject;
            }
        }
        return Json.createObjectBuilder().build();
    }

    /**
     * Find many documents by query.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JsonObject> findDocuments(String query) throws DataException {
        return findDocuments(query, null, null, null);
    }

    /**
     * Find many documents by query and sort condition.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JsonObject> findDocuments(String query, String sort) throws DataException {
        return findDocuments(query, sort, null, null);
    }

    /**
     * Find many documents by query, offset and size of results.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JsonObject> findDocuments(String query, Integer offset, Integer size) throws DataException {
        return findDocuments(query, null, offset, size);
    }

    /**
     * Find many documents by query, sort condition, offset and size of result set.
     *
     * @param query
     *            as String
     * @param sort
     *            as String
     * @return list of JSON objects
     */
    public List<JsonObject> findDocuments(String query, String sort, Integer offset, Integer size)
            throws DataException {
        SearchRestClient restClient = initiateRestClient();
        List<JsonObject> searchResults = new ArrayList<>();

        String response = restClient.getDocument(query, sort, offset, size);
        try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
            JsonObject jsonObject = jsonReader.readObject();
            if (jsonObject.containsKey("hits")) {
                JsonObject hits = jsonObject.getJsonObject("hits");
                JsonArray inHits = hits.getJsonArray("hits");
                if (!inHits.isEmpty()) {
                    for (Object hit : inHits) {
                        searchResults.add((JsonObject) hit);
                    }
                }
            } else {
                searchResults.add(jsonObject);
            }
        }
        return searchResults;
    }

    private SearchRestClient initiateRestClient() {
        SearchRestClient restClient = SearchRestClient.getInstance();
        restClient.setIndex(index);
        restClient.setType(type);
        return restClient;
    }
}

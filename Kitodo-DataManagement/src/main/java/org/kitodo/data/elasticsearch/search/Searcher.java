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

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
        JSONParser parser = new JSONParser();

        String response = restClient.countDocuments(overrideNullQuery(query));
        if (!response.equals("")) {
            try {
                JSONObject result = (JSONObject) parser.parse(response);
                return (Long) result.get("count");
            } catch (ParseException e) {
                throw new DataException(e);
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
    public JSONObject aggregateDocuments(String query, String aggregation) throws DataException {
        SearchRestClient restClient = initiateRestClient();
        JSONParser parser = new JSONParser();

        String response = restClient.aggregateDocuments(overrideNullQuery(query), aggregation);
        if (!response.equals("")) {
            try {
                JSONObject result = (JSONObject) parser.parse(response);
                return (JSONObject) result.get("aggregations");
            } catch (ParseException e) {
                throw new DataException(e);
            }
        } else {
            return new JSONObject();
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
    public JSONObject findDocument(Integer id) throws DataException {
        SearchRestClient restClient = initiateRestClient();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(id);
        if (!response.equals("")) {
            try {
                return (JSONObject) parser.parse(response);
            } catch (ParseException e) {
                throw new DataException(e);
            }
        } else {
            return new JSONObject();
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
    public JSONObject findDocument(String query) throws DataException {
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
    public JSONObject findDocument(String query, String sort) throws DataException {
        SearchRestClient restClient = initiateRestClient();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(query, sort, 0, 1);
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(response);
            if (jsonObject.containsKey("hits")) {
                JSONObject hits = (JSONObject) jsonObject.get("hits");
                JSONArray inHits = (JSONArray) hits.get("hits");
                if (!inHits.isEmpty()) {
                    return (JSONObject) inHits.get(0);
                }
            } else {
                return jsonObject;
            }
        } catch (ParseException e) {
            throw new DataException(e);
        }
        return new JSONObject();
    }

    /**
     * Find many documents by query.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JSONObject> findDocuments(String query) throws DataException {
        return findDocuments(query, null, null, null);
    }

    /**
     * Find many documents by query and sort condition.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JSONObject> findDocuments(String query, String sort) throws DataException {
        return findDocuments(query, sort, null, null);
    }

    /**
     * Find many documents by query, offset and size of results.
     *
     * @param query
     *            as String
     * @return list of JSON objects
     */
    public List<JSONObject> findDocuments(String query, Integer offset, Integer size) throws DataException {
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
    public List<JSONObject> findDocuments(String query, String sort, Integer offset, Integer size)
            throws DataException {
        SearchRestClient restClient = initiateRestClient();
        List<JSONObject> searchResults = new ArrayList<>();
        JSONParser parser = new JSONParser();

        String response = restClient.getDocument(query, sort, offset, size);
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(response);
            if (jsonObject.containsKey("hits")) {
                JSONObject hits = (JSONObject) jsonObject.get("hits");
                JSONArray inHits = (JSONArray) hits.get("hits");
                if (!inHits.isEmpty()) {
                    for (Object hit : inHits) {
                        searchResults.add((JSONObject) hit);
                    }
                }
            } else {
                searchResults.add(jsonObject);
            }
        } catch (ParseException e) {
            throw new DataException(e);
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

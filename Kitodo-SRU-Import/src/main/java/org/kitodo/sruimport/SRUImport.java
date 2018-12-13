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

package org.kitodo.sruimport;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.config.OPACConfig;
import org.w3c.dom.Document;

public class SRUImport implements ExternalDataImportInterface {

    private static final Logger logger = LogManager.getLogger(SRUImport.class);
    private static final String NAME_ATTRIBUTE = "[@name]";
    private static final String VALUE_ATTRIBUTE = "[@value]";
    private static final String LABEL_ATTRIBUTE = "[@label]";

    private static String protocol;
    private static String host;
    private static String path;
    private static LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    private static HashMap<String, String> searchFieldMapping = new HashMap<>();
    private static String equalsOperand = "==";

    private static HttpClient sruClient;

    /**
     * Standard constructor.
     */
    public SRUImport() {
        sruClient = HttpClientBuilder.create().build();
        // TODO: implement SchemaConverter and instantiate here
    }

    @Override
    public Document getFullRecordById(String catalogId, String id) {
        loadOPACConfiguration(catalogId);
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);
        try {
            URI queryURL = createQueryURI(queryParameters);
            return performQuerytoDocument(queryURL.toString() + "&query=ead.id==" + id);
        } catch (URISyntaxException e) {
            logger.error(e.getLocalizedMessage());
        }
        // TODO: transform hit to Kitodo internal format using SchemaConverter!
        return null;
    }

    @Override
    public SearchResult search(String catalogId, String field, String term, int rows) {
        loadOPACConfiguration(catalogId);
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, term);
        return search(catalogId, searchFields, rows);
    }

    @Override
    public SearchResult search(String catalogId, Map<String, String> searchParameters, int rows) {
        // TODO: check how the fields of hits from SRU interfaces can be configured via CQL (need only title and id!)
        loadOPACConfiguration(catalogId);
        if (searchFieldMapping.keySet().containsAll(searchParameters.keySet())) {

            // Query parameters for HTTP request
            LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);

            // Search fields and terms of query
            LinkedHashMap<String, String> searchFieldMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : searchParameters.entrySet()) {
                searchFieldMap.put(searchFieldMapping.get(entry.getKey()), entry.getValue());
            }

            try {
                // FIXME:
                // adding "query" to the list of parameters to be formatted by 'URLEncodedUtils.format'
                // results in the "==" between search fields and terms to become URL encoded and a failed query!
                //queryParameters.put("query", createSearchFieldString(searchFieldMap));
                URI queryURL = createQueryURI(queryParameters);
                return performQuery(queryURL.toString() + "&query=" + createSearchFieldString(searchFieldMap));
            } catch (URISyntaxException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return null;
    }

    @Override
    public Collection<Document> getMultipleEntriesById(Collection<String> ids, String catalogId) {
        return null;
    }

    private SearchResult performQuery(String queryURL) {
        try {
            HttpResponse response = sruClient.execute(new HttpGet(queryURL));
            if (Objects.equals(response.getStatusLine().getStatusCode(), SC_OK)) {
                return ResponseHandler.getSearchResult(response);
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return new SearchResult();
    }

    private Document performQuerytoDocument(String queryURL) {
        try {
            HttpResponse response = sruClient.execute(new HttpGet(queryURL));
            if (Objects.equals(response.getStatusLine().getStatusCode(), SC_OK)) {
                return ResponseHandler.transformResponseToDocument(response);
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    private URI createQueryURI(LinkedHashMap<String, String> searchFields) throws URISyntaxException {
        return new URI(protocol, null, host, -1, path, createQueryParameterString(searchFields), null);
    }

    private String createQueryParameterString(LinkedHashMap<String, String> searchFields) {
        List<BasicNameValuePair> nameValuePairList = searchFields.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return URLEncodedUtils.format(nameValuePairList, Charset.forName("UTF-8"));
    }

    private String createSearchFieldString(LinkedHashMap<String, String> searchFields) {
        List<String> searchOperands = searchFields.entrySet().stream()
                .map(entry -> entry.getKey() + equalsOperand + entry.getValue())
                .collect(Collectors.toList());
        return String.join(" AND ", searchOperands);
    }

    private static void loadOPACConfiguration(String opacName) {
        try {
            // XML configuration of OPAC
            HierarchicalConfiguration opacConfig = OPACConfig.getOPACConfiguration(opacName);

            for (HierarchicalConfiguration queryConfigParam : opacConfig.configurationsAt("param")) {
                if (queryConfigParam.getString(NAME_ATTRIBUTE).equals("scheme")) {
                    protocol = queryConfigParam.getString(VALUE_ATTRIBUTE);
                } else if (queryConfigParam.getString(NAME_ATTRIBUTE).equals("host")) {
                    host = queryConfigParam.getString(VALUE_ATTRIBUTE);
                } else if (queryConfigParam.getString(NAME_ATTRIBUTE).equals("path")) {
                    path = queryConfigParam.getString(VALUE_ATTRIBUTE);
                }
            }


            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opacName);

            for (HierarchicalConfiguration searchField : searchFields.configurationsAt("searchField")) {
                searchFieldMapping.put(searchField.getString(LABEL_ATTRIBUTE), searchField.getString(VALUE_ATTRIBUTE));
            }

            HierarchicalConfiguration urlParameters = OPACConfig.getUrlParameters(opacName);

            for (HierarchicalConfiguration queryParam : urlParameters.configurationsAt("param")) {
                parameters.put(queryParam.getString(NAME_ATTRIBUTE), queryParam.getString(VALUE_ATTRIBUTE));
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}

/*
 * The Fascinator - ReDBox/Mint SRU Client - NLA Identity Copyright (C) 2012
 * Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kitodo.production.plugin.importer.massimport.googlecode.fascinator.redbox.sru;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A light-weight SRU client implementation.
 *
 * @author Greg Pendlebury
 *
 * @author Credit for some of inspiration has to go to another light-weight
 *         implementation available under LGPL we looked at before we started
 *         coding: {@code SRUClient} from 'Sinciput'.
 * @see "https://code.google.com/archive/p/sinciput/source"
 */
public class SRUClient {
    private static final Logger logger = LogManager.getLogger(SRUClient.class);

    /**
     * Request a particular response packing.
     **/
    private static final String RESPONSE_PACKING = "xml";

    /**
     * Version parameter for the query.
     **/
    private static final String SRU_VERSION = "1.1";

    /**
     * Default URL.
     **/
    private final String baseUrl;

    /**
     * Default Schema.
     **/
    private final String recordSchema;

    /**
     * Constructor indicating the base URL and metadata schema.
     *
     * @param baseUrl
     *            The Base URL for the SRU interface. Required.
     * @param recordSchema
     *            The SRU 'recordSchema' to use.
     * @throws MalformedURLException
     *             Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String recordSchema) throws MalformedURLException {
        // Make sure our URL is valid first
        try {
            @SuppressWarnings("unused")
            URL url = new URL(baseUrl);
            this.baseUrl = baseUrl;
        } catch (MalformedURLException ex) {
            logger.error("Invalid URL passed to constructor: ", ex);
            throw ex;
        }

        this.recordSchema = recordSchema;
    }

    /**
     * Basic wrapper for safely encoding Strings used in URLs.
     *
     * @param value
     *            The String to be used in the URL
     * @return String A safely encoded version of 'value' for use in URLs.
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error UTF-8 encoding value '{}'", value, ex);
            return "";
        }
    }

    /**
     * Get the requested URL and return the GetMethod Object afterwards.
     *
     * @param url
     *            The URL to retrieve
     * @return GetMethod The instantiated and executed GetMethod Object.
     * @throws IOException
     *             If any network errors occur accessing the URL.
     */
    private GetMethod getUrl(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        return get;
    }

    /**
     * Generate a basic search URL for this SRU interface.
     *
     * @param query
     *            The query String to perform against the SRU interface.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query) {
        StringBuilder searchUrl = new StringBuilder(baseUrl);

        if (query == null) {
            logger.error("Cannot generate a search URL without a search! 'query' parameter is required.");
            return null;
        }

        // URL basics
        searchUrl.append(encode("?version="));
        searchUrl.append(SRU_VERSION);

        searchUrl.append("&recordSchema=");
        searchUrl.append(encode(recordSchema));

        searchUrl.append("&recordPacking=");
        searchUrl.append(RESPONSE_PACKING);

        // Search basics
        searchUrl.append("&operation=searchRetrieve&query=");
        searchUrl.append(encode(query));

        return searchUrl.toString();
    }

    /**
     * Perform a basic search and return the response body.
     *
     * @param query
     *            The query String to perform against the SRU interface.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query) {
        String searchUrl = generateSearchUrl(query);
        if (searchUrl == null) {
            logger.error("Invalid search URL. Cannot perform search.");
            return null;
        }

        // Perform the search
        GetMethod get;
        try {
            get = getUrl(searchUrl);
            int status = get.getStatusCode();
            if (status != 200) {
                String text = get.getStatusText();
                logger.error("Error access SRU interface, status code '{}' returned with message: {}", status, text);
                return null;
            }

        } catch (IOException ex) {
            logger.error("Error during search: ", ex);
            return null;
        }

        // Return our results body
        try {
            byte[] responseBody = get.getResponseBody();
            return new String(responseBody, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.error("Error accessing response body: ", ex);
            return null;
        }
    }
}

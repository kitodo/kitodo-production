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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * <p>
 * A light-weight SRU client implementation. Originally written for the purpose
 * of searching the National Library of Australia's Party Infrastructure Project
 * (PIP) via GET (ie. not POST or SOAP, both of which PIP also supports).
 * </p>
 * <p>
 * More information/documentation for PIP is
 * <a href="https://wiki.nla.gov.au/display/ARDCPIP/Documentation"> available on
 * the NLA wiki</a>.
 * </p>
 *
 * @author Greg Pendlebury
 *
 *         <p>
 *         Credit for some of inspiration has to go to another light-weight
 *         implementation available under LGPL we looked at before we started
 *         coding: <a href=
 *         "http://code.google.com/p/sinciput/source/browse/trunk/sinciput/src/com/technosophos/sinciput/sru/SRUClient.java">
 *         SRUClient</a> from 'Sinciput'.
 *         </p>
 *
 */
public class SRUClient {
    /** Logging. **/
    private static Logger logger = LogManager.getLogger(SRUClient.class);

    /** A SAX Reader for XML parsing. **/
    private SAXReader saxReader;

    /** Namespaces for XML parsing. **/
    private Map<String, String> namespaces;

    /** Default URL is for the NLA. **/
    private String baseUrl = "http://www.nla.gov.au/apps/srw/search/peopleaustralia";

    /** Default Schema is for EAC-CPF records from the NLA. **/
    private String recordSchema = "urn:isbn:1-931666-33-4";

    /** Version parameter for the query. **/
    private String sruVersion = "1.1";

    /** Request a particular response packing. **/
    private String responsePacking = "xml";

    /** Unit testing only. Fake search response. **/
    private String testingResponseString;

    private String maximumRecords = "2";

    /**
     * Default Constructor. Connect to the NLA unless otherwise instructed. This
     * will rely on the more complicated constructor defaulting to the searching
     * for EAC-CPF records as well.
     */
    public SRUClient() {
        saxInit();
    }

    /**
     * Constructor indicating the base URL for the SRU interface.
     *
     * @param baseUrl
     *            The Base URL for the SRU interface. Required.
     * @throws MalformedURLException
     *             Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl) throws MalformedURLException {
        this(baseUrl, null, null, null);
    }

    /**
     * Constructor indicating the base URL and metadata schema.
     *
     * @param baseUrl
     *            The Base URL for the SRU interface. Required.
     * @param schema
     *            The SRU 'recordSchema' to use. NULL values will default to
     *            EAC-CPC ('urn:isbn:1-931666-33-4')
     * @throws MalformedURLException
     *             Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema) throws MalformedURLException {
        this(baseUrl, schema, null, null);
    }

    /**
     * <p>
     * Constructor indicating the base URL, metadata schema and format packing
     * for responses.
     * </p>
     *
     * @param baseUrl
     *            The Base URL for the SRU interface. Required.
     * @param schema
     *            The SRU 'recordSchema' to use. NULL values will default to
     *            EAC-CPC ('urn:isbn:1-931666-33-4')
     * @param packing
     *            The SRU 'recordPacking' to use. NULL values will default to
     *            'xml'
     * @throws MalformedURLException
     *             Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema, String packing) throws MalformedURLException {
        this(baseUrl, schema, packing, null);
    }

    /**
     * <p>
     * This constructor is where the real work happens. All the constructors
     * above provide wrappers of this one based on how much you want to deviate
     * from the defaults (which assume you are connecting to the NLA.
     * </p>
     *
     * @param baseUrl
     *            The Base URL for the SRU interface. Required.
     * @param version
     *            The SRU 'version' to use. NULL values will default to v1.1
     * @param schema
     *            The SRU 'recordSchema' to use. NULL values will default to
     *            EAC-CPC ('urn:isbn:1-931666-33-4')
     * @param packing
     *            The SRU 'recordPacking' to use. NULL values will default to
     *            'xml'
     * @throws MalformedURLException
     *             Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema, String packing, String version) throws MalformedURLException {
        // Make sure our URL is valid first
        try {
            @SuppressWarnings("unused")
            URL url = new URL(baseUrl);
            this.baseUrl = baseUrl;
        } catch (MalformedURLException ex) {
            logger.error("Invalid URL passed to constructor: ", ex);
            throw ex;
        }

        // Start with the default NLA parameters if nothing has been configured
        // NLA = EAC-CPF
        if (schema != null) {
            recordSchema = schema;
        }
        // NLA = 1.1
        if (version != null) {
            sruVersion = version;
        }
        // NLA = xml
        if (packing != null) {
            responsePacking = packing;
        }

        saxInit();
    }

    /**
     * <p>
     * Used to change the 'recordSchema' after instantiation. All outgoing
     * requests sent after this call will use the new schema.
     * </p>
     *
     * @param newSchema
     *            The new schema to use.
     */
    public void setRecordSchema(String newSchema) {
        recordSchema = newSchema;
    }

    /**
     * <p>
     * Used to change the 'version' after instantiation. All outgoing requests
     * sent after this call will use the new version.
     * </p>
     *
     * @param newVersion
     *            The new version to use.
     */
    public void setVersion(String newVersion) {
        sruVersion = newVersion;
    }

    /**
     * <p>
     * Used to change 'recordPacking' after instantiation. All outgoing requests
     * sent after this call will use the new format.
     * </p>
     *
     * @param newPacking
     *            The new packing format to use.
     */
    public void setPacking(String newPacking) {
        responsePacking = newPacking;
    }

    /**
     * <p>
     * Simple init for the SAX Reader.
     * </p>
     */
    private void saxInit() {
        namespaces = new HashMap<>();
        DocumentFactory docFactory = new DocumentFactory();
        docFactory.setXPathNamespaceURIs(namespaces);
        saxReader = new SAXReader(docFactory);
    }

    /**
     * <p>
     * Used in unit testing to indicate a package resource to use as search
     * responses, rather then submitting a real SRU query.
     * </p>
     *
     * @param fileName
     *            The name of a resource 'file' to use as simulated search
     *            result.
     * @throws IOException
     *             If encoding/access issues occur accessing the resource.
     */
    public void testResponseResource(String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getClass().getResourceAsStream("/" + fileName);
        if (in != null) {
            IOUtils.copy(in, out);
            testingResponseString = out.toString("UTF-8");
            in.close();
        }
    }

    /**
     * Parse an XML document from a string
     *
     * @param xmlData
     *            The String to parse
     * @return Document The parsed XML Object. Null if any problems occur.
     */
    public Document parseXml(String xmlData) {
        try {
            byte[] bytes = xmlData.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            return saxReader.read(in);
        } catch (DocumentException ex) {
            logger.error("Failed to parse XML", ex);
            return null;
        }
    }

    /**
     * <p>
     * Parse an XML String response and populate a response Object.
     * </p>
     *
     * @param xmlData
     *            The XML String returned from the search
     * @return SRUResponse An instantiated response object
     */
    public SRUResponse getResponseObject(String xmlData) {
        // Parsing
        Document xmlResponse = parseXml(xmlData);
        if (xmlResponse == null) {
            logger.error("Can't get results after XML parsing failed.");
            return null;
        }

        // Processing
        SRUResponse response = null;
        try {
            response = new SRUResponse(xmlResponse);
        } catch (SRUException ex) {
            logger.error("Error processing XML response:", ex);
        }
        return response;
    }

    /**
     * <p>
     * Parse an XML String response and get a List Object containing all of the
     * SRU search results.
     * </p>
     *
     * @param xmlData
     *            The XML String returned from the search
     * @return list containing a Dom4j node for each search result
     */
    public List<Node> getResultList(String xmlData) {
        SRUResponse response = getResponseObject(xmlData);
        if (response == null) {
            logger.error("Unable to get results from response XML.");
            return null;
        }

        return response.getResults();
    }

    /**
     * <p>
     * Basic wrapper for safely encoding Strings used in URLs.
     * </p>
     *
     * @param value
     *            The String to be used in the URL
     * @return String A safely encoded version of 'value' for use in URLs.
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error UTF-8 encoding value '{}'", value, ex);
            return "";
        }
    }

    /**
     * <p>
     * Get the requested URL and return the GetMethod Object afterwards. To
     * access more info use its method: eg. GetMethod.getStatusCode() and
     * GetMethod.getResponseBodyAsString()
     * </p>
     *
     * <p>
     * Internally wraps a Fascinator BasicHttpClient Object, so any configured
     * proxy details from the system will be used automatically.
     * </p>
     *
     * @param url
     *            The URL to retrieve
     * @return GetMethod The instantiated and executed GetMethod Object.
     * @throws IOException
     *             If any network errors occur accessing the URL. Note this does
     *             not cover HTTP errors returned from the web server; use the
     *             returned Object to check for these.
     */
    private GetMethod getUrl(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        return get;
    }

    /**
     * <p>
     * Generate a basic search URL for this SRU interface.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query) {
        return this.generateSearchUrl(query, null, null, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. No sorting or pagination.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String operation) {
        return this.generateSearchUrl(query, operation, null, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. No pagination.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @param sortKeys
     *            Sorting. Optional, with no default.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String operation, String sortKeys) {
        return this.generateSearchUrl(query, operation, sortKeys, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. This is the actual
     * implementation method wrapped by the methods above with most parameters
     * as optional.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @param sortKeys
     *            Sorting. Optional, with no default.
     * @param startRecord
     *            Starting record number. Optional, with no default.
     * @param maxRecords
     *            Maximum rows to return. Optional, with no default.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String operation, String sortKeys, String startRecord,
            String maxRecords) {
        String searchUrl = baseUrl;

        if (query == null) {
            logger.error("Cannot generate a search URL without a search! 'query' parameter is required.");
            return null;
        }
        if (operation == null) {
            operation = "searchRetrieve";
        }

        // URL basics
        searchUrl += "?version=" + encode(sruVersion);
        searchUrl += "&recordSchema=" + encode(recordSchema);
        searchUrl += "&recordPacking=" + encode(responsePacking);

        // Search basics
        searchUrl += "&operation=" + encode(operation);
        searchUrl += "&query=" + encode(query);

        // Optional extras on search. Sorting and pagination
        if (sortKeys != null) {
            searchUrl += "&sortKeys=" + encode(sortKeys);
        }
        if (startRecord != null) {
            searchUrl += "&startRecord=" + encode(startRecord);
        }
        if (maximumRecords != null) {
            searchUrl += "&maximumRecords=" + encode(maximumRecords);
        }

        return searchUrl;
    }

    /**
     * <p>
     * Perform a basic search and return the response body.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query) {
        return getSearchResponse(query, null, null, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. No sorting or pagination.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation) {
        return getSearchResponse(query, operation, null, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. No pagination.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @param sortKeys
     *            Sorting. Optional, with no default.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation, String sortKeys) {
        return getSearchResponse(query, operation, sortKeys, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. This is the actual
     * implementation method wrapped by the methods above with most parameters
     * as optional.
     * </p>
     *
     * @param query
     *            The query String to perform against the SRU interface.
     *            Required.
     * @param operation
     *            The 'operation' perform. If null this will default to
     *            'searchRetrieve'.
     * @param sortKeys
     *            Sorting. Optional, with no default.
     * @param startRecord
     *            Starting record number. Optional, with no default.
     * @param maxRecords
     *            Maximum rows to return. Optional, with no default.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation, String sortKeys, String startRecord,
            String maxRecords) {
        // Get a search URL to execute first
        String searchUrl = generateSearchUrl(query, operation, sortKeys, startRecord, maxRecords);
        if (searchUrl == null) {
            logger.error("Invalid search URL. Cannot perform search.");
            return null;
        }

        // Unit testing... don't perform a real search
        if (testingResponseString != null) {
            return testingResponseString;
        }

        // Perform the search
        GetMethod get = null;
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
        String response = null;
        try {
            byte[] bla = get.getResponseBody();
            response = new String(bla, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.error("Error accessing response body: ", ex);
            return null;
        }
        return response;
    }

    /**
     * <p>
     * Make sure that the SAX Reader is aware of the XML namespaces used by the
     * NLA when parsing their.
     * </p>
     */
    private void nlaNamespaces() {
        if (!namespaces.containsKey("srw")) {
            namespaces.put("srw", "http://www.loc.gov/zing/srw/");
        }
        if (!namespaces.containsKey("eac")) {
            namespaces.put("eac", "urn:isbn:1-931666-33-4");
        }
    }

    /**
     * <p>
     * Search for a record from the National Library of Australia with the
     * provided identifier. If multiple records match this identifier only the
     * first will be returned.
     * </p>
     *
     * @param id
     *            The identifier to search for
     * @return String The record matching this identifier. Null if not found
     */
    private Node nlaGetRecordNodeById(String id) {
        nlaNamespaces();

        // Run a search
        String query = "rec.identifier=\"" + id + "\"";
        String rawXml = getSearchResponse(query);

        // Get the results nodes
        List<Node> results = getResultList(rawXml);
        if (results.isEmpty()) {
            logger.warn("This identifier matches no records.");
            return null;
        }
        if (results.size() > 1) {
            logger.warn("This identifier matches multiple records! Returning only the first.");
        }

        // Return first(only?) record
        if ("xml".equals(responsePacking)) {
            return results.get(0).selectSingleNode("*[1]");
        } else {
            return results.get(0);
        }
    }

    /**
     * <p>
     * Search for a record from the National Library of Australia with the
     * provided identifier. If multiple records match this identifier only the
     * first will be returned.
     * </p>
     *
     * @param id
     *            The identifier to search for
     * @return String The record matching this identifier. Null if not found
     */
    public String nlaGetRecordById(String id) {
        Node node = nlaGetRecordNodeById(id);

        if (node == null) {
            return null;
        }

        if ("xml".equals(responsePacking)) {
            return node.asXML();
        } else {
            return node.getText();
        }
    }

    /**
     * <p>
     * Search for a record from the National Library of Australia with the
     * provided identifier. If multiple records match this identifier only the
     * first will be returned.
     * </p>
     *
     * @param id
     *            The identifier to search for
     * @return String The record matching this identifier. Null if not found
     */
    public String nlaGetNationalId(String id) {
        Node node = nlaGetRecordNodeById(id);

        if (node == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Node> otherIds = node.selectNodes("eac:control/eac:otherRecordId");
        for (Node idNode : otherIds) {
            String otherId = idNode.getText();
            if (otherId.startsWith("http://nla.gov.au")) {
                return otherId;
            }
        }

        return null;
    }

    /**
     * <p>
     * Search for a record from the National Library of Australia with the
     * provided identifier. Process and return their Identity record.
     * </p>
     *
     * @param id
     *            The identifier to search for
     * @return NLAIdentity A processed Identity
     * @throws SRUException
     *             If processing the Identity fails.
     */
    public NLAIdentity nlaGetIdentityById(String id) throws SRUException {
        Node node = nlaGetRecordNodeById(id);
        return new NLAIdentity(node);
    }

    /**
     * Search for a records from the National Library of Australia and parse the
     * resultant XML is a wrapper object.
     *
     * @param search
     *            The search to submit to the NLA
     * @return SRUResponse A parsed response
     */
    public SRUResponse nlaGetResponseBySearch(String search) {
        return nlaGetResponseBySearch(search, null, null);
    }

    /**
     * Search for a records from the National Library of Australia and parse the
     * resultant XML is a wrapper object.
     *
     * @param search
     *            The search to submit to the NLA
     * @param startRecord
     *            Starting record number. Optional, with no default.
     * @param maxRecords
     *            Maximum rows to return. Optional, with no default.
     * @return SRUResponse A parsed response
     */
    public SRUResponse nlaGetResponseBySearch(String search, String startRecord, String maxRecords) {
        nlaNamespaces();

        // Search NLA
        String xmlResponse = getSearchResponse(search, null, null, startRecord, maxRecords);
        if (xmlResponse == null) {
            logger.error("Searching NLA failed!");
            return null;
        }

        // Parse results
        return getResponseObject(xmlResponse);
    }

    /**
     * <p>
     * Search for records from the National Library of Australia. Process and
     * return their Identity records. It is important to note that if any
     * Identity fails to process it will not appear in the List.
     * </p>
     *
     * @param search
     *            The search to submit to the NLA
     * @return the list of processed identities
     */
    public List<NLAIdentity> nlaGetIdentitiesBySearch(String search) {
        return nlaGetIdentitiesBySearch(search, null, null);
    }

    /**
     * <p>
     * Search for records from the National Library of Australia. Process and
     * return their Identity records. It is important to note that if any
     * Identity fails to process it will not appear in the List.
     * </p>
     *
     * @param search
     *            The search to submit to the NLA
     * @param startRecord
     *            Starting record number. Optional, with no default.
     * @param maxRecords
     *            Maximum rows to return. Optional, with no default.
     * @return the list of processed identities
     */
    public List<NLAIdentity> nlaGetIdentitiesBySearch(String search, String startRecord, String maxRecords) {
        SRUResponse response = nlaGetResponseBySearch(search);
        if (response == null) {
            logger.error("Searching NLA failed!");
            return null;
        }

        // Process Identities
        return NLAIdentity.convertNodesToIdentities(response.getResults());
    }
}

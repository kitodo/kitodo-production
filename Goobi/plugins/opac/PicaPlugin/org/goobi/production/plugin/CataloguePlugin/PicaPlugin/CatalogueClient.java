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

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Connects to OPAC system.
 *
 * TODO Talk with the GBV if the URLs are ok this way
 *
 * TODO check if correct character encodings are returned
 *
 * @author Ludwig
 */

class CatalogueClient {
    private static final Logger logger = Logger.getLogger(CatalogueClient.class);

    // the output xml
    private static final String PICA_COLLECTION_RECORDS = "collection";

    private static final String PICA_RECORD = "record";

    private static final String PICA_FIELD = "field";

    private static final String PICA_FIELD_NAME = "tag";

    private static final String PICA_FIELD_OCCURRENCES = "occurrence";

    private static final String PICA_SUBFIELD = "subfield";

    private static final int HTTP_CONNECTION_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private static final String PICA_SUBFIELD_NAME = "code";

    // the opac url parts
    private static final String SET_ID_URL = "/SET=";

    private static final String PICAPLUS_XML_URL_WITHOUT_LOCAL_DATA = "/XML=1.0/CHARSET=";

    /**
     * The url path part for retrieving picaplus as xml before the value of the
     * response charset
     */
    private static final String PICAPLUS_XML_URL = "/XML=1.0/PRS=PP%7F" + "/CHARSET=";
    private static final String DATABASE_URL = "/DB=";

    /**
     * The url part for a session id
     */
    private static final String SESSIONID_URL = "/SID=";

    /**
     * The url part for searching in a specified key field
     */
    private static final String SEARCH_URL_BEFORE_QUERY = "/CMD?ACT=SRCHM&";
    private static final String SORT_BY_YEAR_OF_PUBLISHING = "SRT=YOP";

    /**
     * the url part for getting the complete data set
     */
    private static final String SHOW_LONGTITLE_NR_URL = "/SHW?FRST=";

    // resources
    private final HttpClient opacClient;
    private final DocumentBuilder docBuilder;

    // STATE (Instance variables) *****************************************

    private final Catalogue catalogue;

    private final String sorting = SORT_BY_YEAR_OF_PUBLISHING;

    // for caching the last query and its result
    // TODO decide which result to cache (long or shortlist)? up to now its
    // shortlist so that caching is in principal only used for sessionid and
    // searchopac. is it reasonable?
    private String lastQuery = "";

    private Response lastOpacResult = null;

    private long timeout;

    // CREATION (Constructors, factory methods, static/inst init)

    /**
     * Constructor.
     *
     * Note that up to now the search item list is always retrieved and parsed.
     * TODO check for local availability.
     *
     * @param serverAddress
     *            the serveraddress of the opac
     * @param port
     *            the port of the opac
     * @since 0.1
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested
     */

    CatalogueClient(Catalogue catalogue) throws ParserConfigurationException {
        super();
        opacClient = new HttpClient();
        this.catalogue = catalogue;
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // MANIPULATION (Manipulation - what the object does) ******************

    /**
     * Gets the number of hits for the query in the specified field from the
     * OPAC.
     *
     * @param query
     *            The query string you are looking for.
     * @return returns the number of hits.
     * @throws Exception
     *             If something is wrong with the query
     * @throws IOException
     *             If connection to catalogue system failed
     */
    int getNumberOfHits(Query query) throws IOException, SAXException, ParserConfigurationException {
        getResult(query);
        return lastOpacResult.getNumberOfHits();
    }

    /**
     * Gets the formatted picaplus data of the specified hits for the query from
     * the OPAC.
     *
     * @param query
     *            The query string you are looking for.
     * @param fieldKey
     *            The pica mnemonic key (PPN, THM, etc.) for the pica field
     *            where the query should be found.
     * @param numberOfHits
     *            the number of hits to return. Set to a value lesser than 1 to
     *            return all hits.
     * @return returns the root node of the retrieved and formatted xml.
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    Node retrievePicaNode(Query query, int numberOfHits)
            throws IOException, SAXException, ParserConfigurationException {
        return retrievePicaNode(query, 0, numberOfHits < 1 ? -1 : numberOfHits, timeout);
    }

    /**
     * Gets the formatted picaplus data of the specified hits for the query from
     * the OPAC.
     *
     * @param query
     *            The query you are looking for.
     * @param start
     *            The index of the first result to be returned
     * @param end
     *            The index of the first result NOT to be returned
     * @param timeout
     * @return returns the root node of the retrieved and formatted xml.
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    Node retrievePicaNode(Query query, int start, int end, long timeout)
            throws IOException, SAXException, ParserConfigurationException {
        Element doc = getParsedDocument(new InputSource(new StringReader(retrievePica(query, start, end, timeout))))
                .getDocumentElement();
        applyResolveRules(catalogue.getResolveRules(), doc, this);
        return doc;
    }

    /**
     * Apply resolve rules on a result note set.
     *
     * @param rules
     *            Rules to apply
     * @param root
     *            rot node to examine
     * @param client
     *            catalogue client to use for queries
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    private void applyResolveRules(Map<String, ResolveRule> rules, Element root, CatalogueClient client)
            throws IOException, SAXException, ParserConfigurationException {
        for (Element field : new GetChildElements(root)) {
                if (field.getNodeName().equals("field")) {
                    String tag = field.getAttributeNode("tag").getTextContent();
                    for (Element subfield : new GetChildElements(field)) {
                        if (subfield.getNodeName().equals("subfield")) {
                            String subtag = subfield.getAttributeNode("code").getTextContent();
                            String ruleID = ResolveRule.getIdentifier(tag, subtag);
                            if (rules.containsKey(ruleID)) {
                                rules.get(ruleID).execute(subfield, client, field);
                            }
                        }
                    }
                } else {
                    applyResolveRules(rules, field, client);
                }
        }
    }

    /**
     * Gets the raw picaplus data for the specified hits for the query in the
     * specified field from the OPAC.
     *
     * @param query
     *            The query you are looking for.
     * @param start
     *            The index of the first result to be returned
     * @param end
     *            The index of the first result NOT to be returned. Set to -1 to
     *            return all hits from the start.
     * @param timeout
     * @return returns the root node of the retrieved xml. Beware, it is raw and
     *         pretty messy! It is recommended that you use
     *         retrieveXMLPicaPlus()
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    private String retrievePica(Query query, int start, int end, long timeout)
            throws IOException, SAXException, ParserConfigurationException {
        StringBuffer xmlResult = new StringBuffer();

        // querySummary is used to check if cached result and sessionid
        // can be used again
        String querySummary = query.getQueryUrl() + catalogue.getCharset() + catalogue.getDatabase()
                + catalogue.getAddress() + catalogue.getPort() + catalogue.getUncf();

        // if we cannot use the cached result
        if (!lastQuery.equals(querySummary)) {
            // then we need a new sessionid and resultstring
            getResult(query);
        }

        // make sure that upper limit of requested hits is not to high
        int maxNumberOfHits = lastOpacResult.getNumberOfHits();
        if (end > maxNumberOfHits) {
            end = maxNumberOfHits;
        }
        // return all hits if requested
        if (end == -1) {
            end = maxNumberOfHits;
        }

        xmlResult.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlResult.append("  <" + PICA_COLLECTION_RECORDS + ">\n");

        // retrieve and append the requested hits
        for (int i = start; i < end; i++) {

            xmlResult.append(xmlFormatPica(retrievePicaTitle(i, timeout)));
        }
        xmlResult.append("  </" + PICA_COLLECTION_RECORDS + ">\n");

        return xmlResult.toString();
    }

    /**
     * Retrieves a single hit from the catalogue system.
     *
     * @param numberOfHits
     *            The index of the hit to return
     * @param timeout
     * @throws IOException
     *             If the connection failed
     */
    private String retrievePicaTitle(int numberOfHits, long timeout) throws IOException {
        // get pica longtitle
        int retrieveNumber = numberOfHits + 1;
        return retrieveDataFromOPAC(DATABASE_URL + catalogue.getDatabase() + PICAPLUS_XML_URL + catalogue.getCharset()
                + SET_ID_URL + lastOpacResult.getSet() + SESSIONID_URL + lastOpacResult.getSessionId()
                + SHOW_LONGTITLE_NR_URL + retrieveNumber, timeout);
    }

    /**
     * Queries the catalogue system.
     *
     * @param query
     *            The query you are looking for.
     * @return The search result as xml string.
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    private Response getResult(Query query) throws IOException, SAXException, ParserConfigurationException {
        String result = null;

        String querySummary = query.getQueryUrl() + catalogue.getCharset() + catalogue.getDatabase()
                + catalogue.getAddress() + catalogue.getPort() + catalogue.getUncf();

        if (lastQuery.equals(querySummary)) {
            return lastOpacResult;
        }
        result = retrieveDataFromOPAC(DATABASE_URL + catalogue.getDatabase() + PICAPLUS_XML_URL_WITHOUT_LOCAL_DATA
                + catalogue.getCharset() + SEARCH_URL_BEFORE_QUERY + sorting + query.getQueryUrl(), timeout);

        Response opacResult = parseOpacResponse(result);

        // Caching query, result and sessionID
        lastQuery = querySummary;
        lastOpacResult = opacResult;

        return opacResult;
    }

    private String xmlFormatPica(String picaXmlRecord) {
        StringBuffer result = new StringBuffer("  <" + PICA_RECORD + ">\n");
        try {
            int startField = picaXmlRecord.indexOf("LONGTITLE");
            int nextField = 0;
            int endField = picaXmlRecord.indexOf("</LONGTITLE>");
            String field = picaXmlRecord.substring(startField, endField);

            // for some unknown reason the line break/record separator is
            // sometimes different
            String recordSeperator = "<br />";
            if (picaXmlRecord.indexOf(recordSeperator) != -1) {
                while (nextField != endField) {
                    startField = picaXmlRecord.indexOf(recordSeperator, startField) + 6;
                    nextField = picaXmlRecord.indexOf(recordSeperator, startField);
                    if (nextField == -1) {
                        nextField = endField;
                    }
                    field = picaXmlRecord.substring(startField, nextField).trim();
                    result.append(parseRecordField(field));
                }
            } else {
                String[] lines = field.split("\n");
                for (int i = 1; i < lines.length; i++) {
                    result.append(parseRecordField(lines[i]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.append("  </" + PICA_RECORD + ">\n");
        return result.toString();
    }

    private StringBuffer parseRecordField(String field) {
        StringBuffer result = new StringBuffer();

        String[] fieldComponents = null;
        String fieldName = null;
        String fieldOccurrence = null;
        int indexOfFieldOccurrence = -1;

        fieldComponents = field.split("\\$");
        indexOfFieldOccurrence = fieldComponents[0].indexOf("/");

        if (indexOfFieldOccurrence != -1) {
            fieldName = fieldComponents[0].substring(0, indexOfFieldOccurrence);
            fieldOccurrence = fieldComponents[0].substring(indexOfFieldOccurrence + 1);
            result.append("    <" + PICA_FIELD + " " + PICA_FIELD_NAME + "=\"" + fieldName + "\" "
                    + PICA_FIELD_OCCURRENCES + "=\"" + fieldOccurrence + "\">\n");
        } else {
            result.append("    <" + PICA_FIELD + " " + PICA_FIELD_NAME + "=\"" + fieldComponents[0] + "\">\n");
        }

        for (int i = 1; i < fieldComponents.length; i++) {
            result.append("      <" + PICA_SUBFIELD + " " + PICA_SUBFIELD_NAME + "=\"" + fieldComponents[i].charAt(0)
                    + "\">" + fieldComponents[i].substring(1) + "</" + PICA_SUBFIELD + ">\n");
        }

        result.append("    </" + PICA_FIELD + ">\n");
        return result;
    }

    /**
     * Helper method that parses an InputSource and returns a DOM Document.
     *
     * @param source
     *            The InputSource to parse
     * @return The resulting document
     */
    private Document getParsedDocument(InputSource source) {
        try {
            return docBuilder.parse(source);
        } catch (SAXException e) {
            logger.info("Dokument?");

            InputStream bs = source.getByteStream();

            logger.info(bs.toString());
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the content of the specified url from the serverAddress.
     *
     * @param url
     *            The requested url as string. Note that the string needs to be
     *            already url encoded.
     * @return The response.
     * @throws IOException
     *             If the connection failed
     */
    private String retrieveDataFromOPAC(String url, long timeout) throws IOException {
        String request = catalogue.getScheme() + "://" + catalogue.getAddress()
                + (catalogue.getPort() != 80 ? ":".concat(Integer.toString(catalogue.getPort())) : "") + url
                + catalogue.getUncf();

        // set timeout if no connection can be established
        opacClient.getParams().setParameter("http.connection.timeout", HTTP_CONNECTION_TIMEOUT);

        // set timeout if a connection is established but there is no response
        // (= time the database needs to search)
        if ((timeout > 0) && (timeout <= Integer.MAX_VALUE)) {
            opacClient.getParams().setParameter("http.socket.timeout", Long.valueOf(timeout).intValue());
        } else {
            opacClient.getParams().setParameter("http.socket.timeout", 0); // disable
        }

        GetMethod opacRequest = null;
        try {
            opacRequest = new GetMethod(request);
            opacClient.executeMethod(opacRequest);
            return opacRequest.getResponseBodyAsString();
        } finally {
            if (opacRequest != null) {
                opacRequest.releaseConnection();
            }
        }
    }

    /**
     * @throws IOException
     *             an IO exception from the parser, possibly from a byte stream
     *             or character stream supplied by the application.
     * @throws ParserConfigurationException
     *             if a parser cannot be created which satisfies the requested
     *             configuration.
     * @throws SAXException
     *             if any SAX errors occur during processing
     */
    private Response parseOpacResponse(String opacResponse)
            throws IOException, SAXException, ParserConfigurationException {
        opacResponse = opacResponse.replace("&amp;amp;", "&amp;").replace("&amp;quot;", "&quot;")
                .replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;");

        XMLReader parser = null;
        Response ids = new Response();
        /* Use Java 1.4 methods to create default parser. */
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newSAXParser().getXMLReader();

        parser.setContentHandler(ids);
        parser.parse(new InputSource(new StringReader(opacResponse)));

        return ids;
    }

    void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}

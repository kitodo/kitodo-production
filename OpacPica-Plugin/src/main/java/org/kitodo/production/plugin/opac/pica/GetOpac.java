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

package org.kitodo.production.plugin.opac.pica;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Connects to OPAC system.
 *
 * TODO Talk with the GBV if the URLs are ok this way TODO check if correct
 * character encodings are returned
 *
 * @author Ludwig
 * @version 0.1
 * @since 0.1.1
 * 
 *        CHANGELOG: 19.07.2005 Ludwig: first Version
 */

class GetOpac {
    private static final Logger logger = LogManager.getLogger(GetOpac.class);

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
     * response charset.
     */
    private static final String PICAPLUS_XML_URL = "/XML=1.0/PRS=PP%7F" + "/CHARSET=";
    private static final String DATABASE_URL = "/DB=";

    /**
     * The url part for a session id.
     */
    private static final String SESSIONID_URL = "/SID=";

    /**
     * The url part for searching in a specified key field.
     */
    private static final String SEARCH_URL_BEFORE_QUERY = "/CMD?ACT=SRCHM&";
    private static final String SORT_BY_YEAR_OF_PUBLISHING = "SRT=YOP";

    /**
     * the url part for getting the complete data set.
     */
    private static final String SHOW_LONGTITLE_NR_URL = "/SHW?FRST=";

    // resources
    private final HttpClient opacClient;
    private final DocumentBuilder docBuilder;

    // STATE (Instance variables)
    // This is now configured inside the Catalogue class.
    // TODO: Check if this should really be query specific
    private String charset = "iso-8859-1";

    private final Catalogue cat;

    private final String sorting = SORT_BY_YEAR_OF_PUBLISHING;

    // for caching the last query and its result
    // TODO decide which result to cache (long or shortlist)? up to now its
    // shortlist so that caching is in principal only used for sessionid and
    // searchopac. is it reasonable?
    private String lastQuery = "";

    private OpacResponseHandler lastOpacResult = null;

    // CREATION (Constructors, factory methods, static/inst init)

    /**
     * Constructor.
     *
     * Note that up to now the search item list is always retrieved and parsed.
     * TODO check for local availability.
     *
     * @param opac
     *            catalogue
     * @since 0.1
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested
     */

    GetOpac(Catalogue opac) throws ParserConfigurationException {
        super();
        this.opacClient = new HttpClient();
        this.cat = opac;
        this.docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // MANIPULATION (Manipulation - what the object does)

    /**
     * Gets the number of hits for the query in the specified field from the
     * OPAC.
     *
     * @param query
     *            The query string you are looking for.
     * @param timeout
     *            http.socket.timeout for catalog
     * @return returns the number of hits.
     * @throws IOException
     *             If connection to catalogue system failed
     */
    int getNumberOfHits(Query query, long timeout) throws IOException, SAXException, ParserConfigurationException {
        getResult(query, timeout);
        return this.lastOpacResult.getNumberOfHits();
    }

    /**
     * Gets the formatted picaplus data of the specified hits for the query from
     * the OPAC.
     *
     * @param query
     *            string you are looking for.
     * @param numberOfHits
     *            the number of hits to return. Set to a value lesser than 1 to
     *            return all hits.
     * @param timeout
     *            http.socket.timeout for catalog
     * @return returns the root node of the retrieved and formatted xml.
     * @throws IOException
     *             If connection to catalogue system failed
     */
    Node retrievePicaNode(Query query, int numberOfHits, long timeout)
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
     *            http.socket.timeout for catalog
     * @return returns the root node of the retrieved and formatted xml.
     * @throws IOException
     *             If connection to catalogue system failed
     */
    Node retrievePicaNode(Query query, int start, int end, long timeout)
            throws IOException, SAXException, ParserConfigurationException {
        StringReader picaReader = new StringReader(retrievePica(query, start, end, timeout));
        Document document = getParsedDocument(new InputSource(picaReader));
        if (Objects.nonNull(document)) {
            return document.getDocumentElement();
        }
        return null;
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
     *            http.socket.timeout for catalog
     * @return returns the root node of the retrieved xml. Beware, it is raw and
     *         pretty messy! It is recommended that you use
     *         retrieveXMLPicaPlus()
     * @throws IOException
     *             If connection to catalogue system failed
     */
    private String retrievePica(Query query, int start, int end, long timeout)
            throws IOException, SAXException, ParserConfigurationException {
        StringBuffer xmlResult = new StringBuffer();

        // querySummary is used to check if cached result and sessionid
        // can be used again
        String querySummary = query.getQueryUrl() + this.charset + this.cat.getDataBase() + this.cat.getServerAddress()
                + this.cat.getPort() + this.cat.getCbs();

        // if we can not use the cached result
        if (!this.lastQuery.equals(querySummary)) {
            // then we need a new sessionid and resultstring
            getResult(query, timeout);
        }

        // make sure that upper limit of requested hits is not to high
        int maxNumberOfHits = this.lastOpacResult.getNumberOfHits();
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
     *            http.socket.timeout for catalog
     * @throws IOException
     *             If the connection failed
     */
    private String retrievePicaTitle(int numberOfHits, long timeout) throws IOException {
        // get pica longtitle
        int retrieveNumber = numberOfHits + 1;
        return retrieveDataFromOPAC(DATABASE_URL + this.cat.getDataBase() + PICAPLUS_XML_URL + this.charset + SET_ID_URL
                + this.lastOpacResult.getSet() + SESSIONID_URL + this.lastOpacResult.getSessionId()
                + SHOW_LONGTITLE_NR_URL + retrieveNumber, timeout);
    }

    /**
     * Queries the catalogue system.
     *
     * @param query
     *            The query you are looking for.
     * @param timeout
     *            http.socket.timeout for catalog
     * @return The search result as xml string.
     * @throws IOException
     *             If connection to catalogue system failed.
     */
    private OpacResponseHandler getResult(Query query, long timeout)
            throws IOException, SAXException, ParserConfigurationException {
        String result = null;

        String querySummary = query.getQueryUrl() + this.charset + this.cat.getDataBase() + this.cat.getServerAddress()
                + this.cat.getPort() + this.cat.getCbs();

        if (this.lastQuery.equals(querySummary)) {
            return this.lastOpacResult;
        }
        result = retrieveDataFromOPAC(DATABASE_URL + this.cat.getDataBase() + PICAPLUS_XML_URL_WITHOUT_LOCAL_DATA
                + this.charset + SEARCH_URL_BEFORE_QUERY + this.sorting + query.getQueryUrl(), timeout);

        OpacResponseHandler opacResult = parseOpacResponse(result);

        // Caching query, result and sessionID
        this.lastQuery = querySummary;
        this.lastOpacResult = opacResult;

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
            if (picaXmlRecord.contains(recordSeperator)) {
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
            logger.error(e);
        }
        result.append("  </" + PICA_RECORD + ">\n");
        return result.toString();
    }

    private StringBuffer parseRecordField(String field) {
        StringBuffer result = new StringBuffer();

        String[] fieldComponents;
        String fieldName;
        String fieldOccurrence;
        int indexOfFieldOccurrence;

        fieldComponents = field.split("\\$");
        indexOfFieldOccurrence = fieldComponents[0].indexOf('/');

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
            return this.docBuilder.parse(source);
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
        String request = "http://" + cat.getServerAddress()
                + (cat.getPort() != 80 ? ":".concat(Integer.toString(cat.getPort())) : "") + url + cat.getCbs();

        // set timeout if no connection can be established
        opacClient.getParams().setParameter("http.connection.timeout", HTTP_CONNECTION_TIMEOUT);

        // set timeout if a connection is established but there is no response
        // (= time the database needs to search)
        if (timeout > 0 && timeout <= Integer.MAX_VALUE) {
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

    private OpacResponseHandler parseOpacResponse(String opacResponse)
            throws IOException, SAXException, ParserConfigurationException {
        opacResponse = opacResponse.replace("&amp;amp;", "&amp;").replace("&amp;quot;", "&quot;")
                .replace("&amp;lt;", "&lt;").replace("&amp;gt;", "&gt;");

        XMLReader parser = null;
        OpacResponseHandler ids = new OpacResponseHandler();
        /* Use Java 1.4 methods to create default parser. */
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newSAXParser().getXMLReader();

        parser.setContentHandler(ids);
        parser.parse(new InputSource(new StringReader(opacResponse)));

        return ids;
    }

    /**
     * Set requested character encoding for the response of the catalogue
     * system. For goettingen iso-8859-1 and utf-8 work, the default is
     * iso-8859-1.
     *
     * @param charset
     *            The character encoding to set.
     */

    void setCharset(String charset) {
        this.charset = charset;
    }

}

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

package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 * Connects to OPAC system.
 *
 * @author Arved Solth, Christopher Timm
 */

class GetOpac {
    private static final Logger logger = Logger.getLogger(GetOpac.class);

    private static final int HTTP_CONNECTION_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    /**
     * The url part for searching in a specified key field
     */
    private static final String SEARCH_URL_BEFORE_QUERY = "operation=searchRetrieve";
    private static final String RECORD_SCHEMA_MODS = "&recordSchema=mods";

    private final HttpClient opacClient;
    private String charset = "iso-8859-1";
    private final Catalogue cat;
    private String lastQuery = "";
    private int numberOfHits = -1;
    private String default_sru_path = "/sru?version=1.2&";

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

    GetOpac(Catalogue opac) throws ParserConfigurationException {
        super();
        this.opacClient = new HttpClient();
        this.cat = opac;

        this.default_sru_path = this.cat.getPath();

    }

    // MANIPULATION (Manipulation - what the object does) ******************

    /***********************************************************************
     * Gets the number of hits for the query in the specified field from the
     * OPAC.
     *
     * @param query
     *            The query string you are looking for.
     * @param timeout
     * @return returns the number of hits.
     * @throws Exception
     *             If something is wrong with the query
     * @throws IOException
     *             If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    int getNumberOfHits(Query query, long timeout) throws IOException, SAXException, ParserConfigurationException {
        return performQuery(query, timeout);
    }

    /**
     * Queries the catalogue system and returns the number of hits that the
     * query yielded.
     *
     * @param query
     *            The query you are looking for.
     * @param timeout
     * @return The search result as xml string.
     * @throws IOException
     *             If connection to catalogue system failed.
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private int performQuery(Query query, long timeout) throws IOException, SAXException, ParserConfigurationException {
        String result = null;

        String querySummary = query.getQueryUrl() + this.charset + this.cat.getServerAddress() + this.cat.getPort();

        logger.debug("last query: " + this.lastQuery);

        if (this.lastQuery.equals(querySummary)) {
            return this.numberOfHits;
        }

        result = retrieveDataFromOPAC(this.default_sru_path + SEARCH_URL_BEFORE_QUERY + query.getQueryUrl() + RECORD_SCHEMA_MODS,
                timeout);

        this.numberOfHits = retrieveNumberOfHitsFromOpacResponse(result);
        this.lastQuery = querySummary;

        logger.debug("new query: " + this.lastQuery);
        logger.debug("number of hits: " + this.numberOfHits);

        return this.numberOfHits;
    }

    /**
     * Retrieve and return the MODS record from the data being retrieved via the
     * given queryURL string.
     *
     * @param queryURL
     * @param timeout
     * @return The retrieved MODS record in string form
     */
    public String retrieveModsRecord(String queryURL, long timeout) {
        try {
            return retrieveDataFromOPAC(this.default_sru_path + SEARCH_URL_BEFORE_QUERY + queryURL + RECORD_SCHEMA_MODS, timeout);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Retrieve body as stream and return the response as string
     *
     * @param request
     * @return The retrieved MODS record in string form
     * @throws IOException
     */
    private String getResponse(HttpMethodBase request) throws IOException {
        InputStream s = request.getResponseBodyAsStream();
        int bytesRead = -1;
        int totalBytes = 0;
        int bytesToRead = 1000;
        byte[] buf = new byte[1000];
        while (true) {
            bytesRead = s.read(buf, totalBytes, bytesToRead);
            if (bytesRead < 0) {
                break;
            }
            totalBytes += bytesRead;
            bytesToRead -= bytesRead;
            if (bytesToRead == 0) {
                byte[] temp = buf;
                buf = new byte[temp.length * 2];
                System.arraycopy(temp, 0, buf, 0, temp.length);
                bytesToRead = temp.length;
            }
        }
        if (totalBytes > 0) {
            return EncodingUtil.getString(buf, 0, totalBytes, request.getResponseCharSet());
        } else {
            return "";
        }
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
        String request = cat.getScheme() + "://" + cat.getServerAddress()
                + (cat.getPort() != 80 ? ":".concat(Integer.toString(cat.getPort())) : "") + url;

        logger.debug("request url: " + request);

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

            return this.getResponse(opacRequest);
        } finally {
            if (opacRequest != null) {
                opacRequest.releaseConnection();
            }
        }
    }

    /**
     * Extract and return number of this from given String opacResponse.
     *
     * @param opacResponse
     * @return The number of this in given opacResponse as integer.
     */
    private int retrieveNumberOfHitsFromOpacResponse(String opacResponse) {
        int numberOfHits = -1;

        SAXBuilder sb = new SAXBuilder();
        try {
            Document doc = sb.build(new StringReader(opacResponse));
            XPath numberOfHitsPath = XPath.newInstance("//srw:numberOfRecords");
            numberOfHitsPath.addNamespace("srw", "http://www.loc.gov/zing/srw/");
            Element numberOfHitsElement = (Element) numberOfHitsPath.selectSingleNode(doc);
            numberOfHits = Integer.parseInt(numberOfHitsElement.getText());
        } catch (JDOMException | IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }

        return numberOfHits;
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

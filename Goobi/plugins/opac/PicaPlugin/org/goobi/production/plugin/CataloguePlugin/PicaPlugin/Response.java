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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.lang.CharEncoding;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class Response extends DefaultHandler {

    private boolean readTitle = false;
    private boolean readSessionVar = false;
    private String sessionVar = "";
    private String title = "";
    private String sessionId = "";
    private String cookie = "";
    private String set = "";
    private int numberOfHits = 0;

    private final ArrayList<String> opacResponseItemPpns = new ArrayList<>();
    private final ArrayList<String> opacResponseItemTitles = new ArrayList<>();

    Response() {
        super();
    }

    /**
     * SAX parser callback method.
     *
     * @throws SAXException
     *             if an illegal error was found
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("RESULT") && (atts.getValue("error") != null)
                && atts.getValue("error").equalsIgnoreCase("ILLEGAL")) {
            throw new SAXException(new IllegalArgumentException());
        }

        if (localName.equals("SESSIONVAR")) {
            sessionVar = atts.getValue("name");
            readSessionVar = true;
        }

        if (localName.equals("SET")) {
            String hits = atts.getValue("hits");
            if (hits == null) {
                throw new NumberFormatException("null");
            }
            numberOfHits = Integer.parseInt(hits);
        }

        if (localName.equals("SHORTTITLE")) {
            readTitle = true;
            title = "";
            opacResponseItemPpns.add(atts.getValue("PPN"));
        }
    }

    /**
     * SAX parser callback method.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (readTitle) {
            title += new String(ch, start, length);
        }

        if (readSessionVar) {
            if (sessionVar.equals("SID")) {
                sessionId = new String(ch, start, length);
            }
            if (sessionVar.equals("SET")) {
                set = new String(ch, start, length);
            }
            if (sessionVar.equals("COOKIE")) {
                cookie = new String(ch, start, length);
            }
        }
    }

    /**
     * SAX parser callback method.
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        if (localName.equals("SHORTTITLE")) {
            readTitle = false;
            opacResponseItemTitles.add(title);
        }

        if (localName.equals("SESSIONVAR")) {
            readSessionVar = false;
        }
    }

    ArrayList<String> getOpacResponseItemPpns() {
        return opacResponseItemPpns;
    }

    ArrayList<String> getOpacResponseItemTitles() {
        return opacResponseItemTitles;
    }

    String getSessionId() throws UnsupportedEncodingException {
        //TODO HACK
        String sessionIdUrlencoded = URLEncoder.encode(sessionId, CharEncoding.ISO_8859_1);
        if (!cookie.equals("")) {
            sessionIdUrlencoded = sessionIdUrlencoded + "/COOKIE=" + URLEncoder.encode(cookie, CharEncoding.ISO_8859_1);
        }
        return sessionIdUrlencoded;
    }

    String getSet() {
        return set;
    }

    int getNumberOfHits() {
        return numberOfHits;
    }

}

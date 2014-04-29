/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.lang.CharEncoding;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class OpacResponseHandler extends DefaultHandler {

	private boolean readTitle = false;
	private boolean readSessionVar = false;
	private String sessionVar = "";
	private String title = "";
	private String sessionId = "";
	private String cookie = "";
	private String set = "";
	private int numberOfHits = 0;

	private final ArrayList<String> opacResponseItemPpns = new ArrayList<String>();
	private final ArrayList<String> opacResponseItemTitles = new ArrayList<String>();

	OpacResponseHandler() {
		super();
	}

	/**
	 * SAX parser callback method.
	 * 
	 * @throws SAXException
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		//Eingefügt am 8.5.2007
		if (localName.equals("RESULT") && atts.getValue("error") != null
				&& atts.getValue("error").equalsIgnoreCase("ILLEGAL")) {
			throw new SAXException(new IllegalArgumentException());
		}

		if (localName.equals("SESSIONVAR")) {
			sessionVar = atts.getValue("name");
			readSessionVar = true;
		}

		if (localName.equals("SET")) {
			numberOfHits = Integer.valueOf(atts.getValue("hits")).intValue();
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
			opacResponseItemTitles.add(this.title);
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
		if (!this.cookie.equals("")) {
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

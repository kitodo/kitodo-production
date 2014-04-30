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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class Catalogue {
	/** The url path part of the search item list on the server */
	private static final String IKTLIST = "/XML=1.0/IKTLIST";

	private String cbs = "";

	private final String dataBase;
	private final String serverAddress;
	private final int port;

	private final HashMap<String, String> picaToKey;
	private final HashMap<String, String> picaToDescription;

	Catalogue(ConfigOpacCatalogue coc) throws IOException {
		this.picaToKey = new HashMap<String, String>();
		this.picaToDescription = new HashMap<String, String>();
		this.serverAddress = coc.getAddress();
		this.port = coc.getPort();
		this.dataBase = coc.getDatabase();
		this.cbs = coc.getCbs();
		this.parseIktList(retrieveIktList());
	}

	/**
	 * Tries to retrieve the search item key list from the catalogue system.
	 * 
	 * @return The list as a xml string
	 * @throws IOException
	 *             If the retrieval of the list failed.
	 */
	private String retrieveIktList() throws IOException {
		String requestUrl = "http://" + serverAddress + ":" + port + "/" + dataBase + IKTLIST;
		HttpClient opacClient = new HttpClient();
		GetMethod opacRequest = new GetMethod(requestUrl);
		opacClient.executeMethod(opacRequest);
		return opacRequest.getResponseBodyAsString();
	}

	/**
	 * Parses the search key list and puts mnemonic → key and mnemonic →
	 * description into hashmaps.
	 * 
	 * @param iktList
	 *            The search key list as xml string
	 */
	private void parseIktList(String iktList) {
		InputSource iktSource = new InputSource(new StringReader(iktList));
		DocumentBuilder docBuilder = null;
		Document document = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			//get xml-docment
			document = docBuilder.parse(iktSource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//get all keys
		NodeList keys = document.getElementsByTagName("KEY");

		//go through all keys and put them into their hashmap
		for (int i = 0; i < keys.getLength(); i++) {
			picaToKey.put(((Element) keys.item(i)).getAttribute("mnemonic"), keys.item(i).getFirstChild()
					.getNodeValue());
			picaToDescription.put(((Element) keys.item(i)).getAttribute("mnemonic"), ((Element) keys.item(i))
					.getAttribute("description").toString());
		}
	}

	String getDataBase() {
		return dataBase;
	}

	int getPort() {
		return port;
	}

	String getServerAddress() {
		return serverAddress;
	}

	String getCbs() {
		return cbs;
	}
}

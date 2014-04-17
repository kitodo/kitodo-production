package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class Catalogue {
    private static final Logger logger = Logger.getLogger(Catalogue.class);
    //dir of local iktLists
    private static final String DIR = "";

    /** The url path part of the search item list on the server */
    private static final String IKTLIST = "/XML=1.0/IKTLIST";

	static final String SUB_OPAC = "SUB";
	private final String iktList;
    private final String description;
    private String cbs = "";

    private final String dataBase;
    private final String serverAddress;
    private final int port;

    private final HashMap<String, String> picaToKey;
    private final HashMap<String, String> picaToDescription;

    private final boolean verbose = false;

	Catalogue(ConfigOpacCatalogue coc) throws IOException {
		this.picaToKey = new HashMap<String, String>();
		this.picaToDescription = new HashMap<String, String>();
		this.description = coc.getDescription();
		this.serverAddress = coc.getAddress();
		this.port = coc.getPort();
		this.dataBase = coc.getDatabase();
		this.iktList = "";
		this.cbs = coc.getCbs();
		this.parseIktList(this.retrieveIktList());
	}

	/***********************************************************************
	 * Tries to retrieve the search item key list. First it tries a local
	 * location, if that fails it tries the catalogue system.
	 * 
	 * @return The list as a xml string
	 * @throws IOException
	 *             If the retrieval of the list failed.
	 **********************************************************************/
    private String retrieveIktList() throws IOException {
        //try to use local list
        try {
            File iktlistFile = new File(DIR + this.iktList);
            if (this.verbose) {
                logger.info("Trying to load the IKTLIST from the file: " + iktlistFile.getAbsolutePath());
            }
            BufferedInputStream listStream = new BufferedInputStream(new FileInputStream(iktlistFile));
            StringBuffer result = new StringBuffer();

            //read until EOF
            int input = listStream.read();
            while (input != -1) {
                result.append((char) input);
                input = listStream.read();
            }
            listStream.close();

            return result.toString();
        } //we didn't succeed retrieving the search item list from the local file
          //so lets get it from the server
        catch (Exception e) {
            String requestUrl = "";
            if (port == 80) {
                requestUrl = "http://" + this.serverAddress + "/" + this.dataBase + IKTLIST;
            } else {
                requestUrl = "http://" + this.serverAddress + ":" + this.port + "/" + this.dataBase + IKTLIST;
            }
            if (this.verbose) {
                logger.info("Retrieving IKTLIST for opac " +

                this.description + ": " + requestUrl);
            }
            HttpClient opacClient = new HttpClient();
            GetMethod opacRequest = new GetMethod(requestUrl);

            opacClient.executeMethod(opacRequest);
            return opacRequest.getResponseBodyAsString();
        }
    }

    /***********************************************************************
     * Parses the search key list and puts mnemonic->key and mnemonic->description into hashmaps.
     * 
     * @param iktList The search key list as xml string
     **********************************************************************/
    private void parseIktList(String iktList) {
        if (this.verbose) {
            logger.info("Parsing the IKTLIST: " + iktList);
        }

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
            this.picaToKey.put(((Element) keys.item(i)).getAttribute("mnemonic"), keys.item(i).getFirstChild().getNodeValue());
            this.picaToDescription.put(((Element) keys.item(i)).getAttribute("mnemonic"), ((Element) keys.item(i)).getAttribute("description")
                    .toString());
        }
    }

	String getDataBase() {
        return this.dataBase;
    }

	int getPort() {
        return this.port;
    }

	String getServerAddress() {
        return this.serverAddress;
    }

	/**
	 * @return the cbs
	 */
	String getCbs() {
        return this.cbs;
    }

}

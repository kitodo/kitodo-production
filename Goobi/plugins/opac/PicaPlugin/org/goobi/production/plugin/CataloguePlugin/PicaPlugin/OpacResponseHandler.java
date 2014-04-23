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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

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
	 *  SAX parser callback method.
	 * @throws SAXException 
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException
	{
		//Eingefügt cm 8.5.2007
		if (localName.equals("RESULT") && atts.getValue("error") != null && atts.getValue("error").equalsIgnoreCase("ILLEGAL")){
			throw new SAXException(new IllegalArgumentException());
		}
		
		if(localName.equals("SESSIONVAR")){
			this.sessionVar = atts.getValue("name");
			this.readSessionVar = true;
		}
		
		if(localName.equals("SET")){
			this.numberOfHits = Integer.valueOf(atts.getValue("hits")).intValue();
		}
		
		if(localName.equals("SHORTTITLE")){
			this.readTitle = true;
			this.title = "";
			this.opacResponseItemPpns.add(atts.getValue("PPN"));
		}
	}
	
	/** 
	 *  SAX parser callback method.
	 */
	@Override
	public void characters(char[] ch, int start, int length)
	{
		if(this.readTitle){
			this.title += new String(ch, start, length);
		}
		
		if (this.readSessionVar){
			if (this.sessionVar.equals("SID")){
				this.sessionId = new String(ch, start, length);
			}
			if (this.sessionVar.equals("SET")){
				this.set = new String(ch, start, length);
			}
			if (this.sessionVar.equals("COOKIE")){
				this.cookie = new String(ch, start, length);
			}
		}
	}
	
	/** 
	 *  SAX parser callback method.
	 */
	@Override
	public void endElement(String namespaceURI, String localName,
			String qName)
	{
		if(localName.equals("SHORTTITLE")){
			this.readTitle = false;
			this.opacResponseItemTitles.add(this.title);
		}
		
		if(localName.equals("SESSIONVAR")){
			this.readSessionVar = false;
		}
	}
	
	ArrayList<String> getOpacResponseItemPpns() {
		return this.opacResponseItemPpns;
	}
	
	ArrayList<String> getOpacResponseItemTitles() {
		return this.opacResponseItemTitles;
	}


	String getSessionId() throws UnsupportedEncodingException {
		//TODO HACK
		String sessionIdUrlencoded = URLEncoder.encode(sessionId, GetOpac.URL_CHARACTER_ENCODING);

		if (!this.cookie.equals("")){
			sessionIdUrlencoded = sessionIdUrlencoded + "/COOKIE=" + URLEncoder.encode(cookie, GetOpac.URL_CHARACTER_ENCODING);
		}

		return sessionIdUrlencoded;
	}


	String getSet() {
		return this.set;
	}


	int getNumberOfHits() {
		return this.numberOfHits;
	}
	
	
}

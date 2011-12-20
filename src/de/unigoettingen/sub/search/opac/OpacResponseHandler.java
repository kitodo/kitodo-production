/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.unigoettingen.sub.search.opac;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OpacResponseHandler extends DefaultHandler {

	boolean readTitle = false;
	boolean readSessionVar = false;
	String sessionVar = "";
	String title = "";
	String sessionId = "";
	String cookie = "";
	String set = "";
	int numberOfHits = 0;
	
	ArrayList<String> opacResponseItemPpns = new ArrayList<String>();
	ArrayList<String> opacResponseItemTitles = new ArrayList<String>();

	
	public OpacResponseHandler() {
		super();
	}


	/** 
	 *  SAX parser callback method.
	 * @throws SAXException 
	 */
	public void startElement (String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException
	{
		//Eingefügt cm 8.5.2007
		if (localName.equals("RESULT") && atts.getValue("error") != null && atts.getValue("error").equalsIgnoreCase("ILLEGAL")){
			throw new SAXException(new IllegalQueryException());
		}
		
		if(localName.equals("SESSIONVAR")){
			sessionVar = atts.getValue("name");
			readSessionVar = true;
		}
		
		if(localName.equals("SET")){
			numberOfHits = Integer.valueOf(atts.getValue("hits")).intValue();
		}
		
		if(localName.equals("SHORTTITLE")){
			readTitle = true;
			title = "";
			opacResponseItemPpns.add(atts.getValue("PPN"));
		}
	}
	
	/** 
	 *  SAX parser callback method.
	 */
	public void characters (char [] ch, int start, int length)
	{
		if(this.readTitle){
			title += new String(ch, start, length);
		}
		
		if (this.readSessionVar){
			if (sessionVar.equals("SID")){
				sessionId = new String(ch, start, length);
			}
			if (sessionVar.equals("SET")){
				set = new String(ch, start, length);
			}
			if (sessionVar.equals("COOKIE")){
				cookie = new String(ch, start, length);
			}
		}
	}
	
	/** 
	 *  SAX parser callback method.
	 */
	public void endElement (String namespaceURI, String localName,
			String qName)
	{
		if(localName.equals("SHORTTITLE")){
			this.readTitle = false;
			opacResponseItemTitles.add(title);
		}
		
		if(localName.equals("SESSIONVAR")){
			readSessionVar = false;
		}
	}
	
	public ArrayList<String> getOpacResponseItemPpns() {
		return opacResponseItemPpns;
	}
	
	public ArrayList<String> getOpacResponseItemTitles() {
		return opacResponseItemTitles;
	}


	public String getSessionId() {
		//TODO HACK
		if (!cookie.equals("")){
			return sessionId + "/COOKIE=" + cookie;
		}
		return sessionId;
	}


	public String getSet() {
		return set;
	}


	public int getNumberOfHits() {
		return numberOfHits;
	}
	
	
}

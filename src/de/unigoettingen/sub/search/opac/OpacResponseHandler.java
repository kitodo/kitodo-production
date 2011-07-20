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
	
	ArrayList opacResponseItemPpns = new ArrayList();
	ArrayList opacResponseItemTitles = new ArrayList();

	
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
	
	public ArrayList getOpacResponseItemPpns() {
		return opacResponseItemPpns;
	}
	
	public ArrayList getOpacResponseItemTitles() {
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

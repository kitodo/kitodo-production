package de.unigoettingen.sub.search.opac;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

import de.sub.goobi.config.ConfigMain;

public class ConfigOpacCatalogue {
	private static final Logger myLogger = Logger.getLogger(ConfigOpacCatalogue.class);
	private String title = "";
	private String description = "";
	private String address = "";
	private String database = "";
	private String iktlist = "";
	private int port = 80;
	private String cbs;
	private String charset = "iso-8859-1";
	private ArrayList<ConfigOpacCatalogueBeautifier> beautifySetList;
	private String opacType;

	public ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist, int port,
			ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
		this.title = title;
		this.description = desciption;
		this.address = address;
		this.database = database;
		this.iktlist = iktlist;
		this.port = port;
		this.beautifySetList = inBeautifySetList;
		this.opacType = opacType;
	}

	// Constructor that also takes a charset, a quick hack for DPD-81
	public ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist, int port, String charset,
			String cbs, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
		// Call the contructor above
		this(title, desciption, address, database, iktlist, port, inBeautifySetList, opacType);
		this.charset = charset;
		this.setCbs(cbs);
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public String getAddress() {
		return this.address;
	}

	public String getDatabase() {
		return this.database;
	}

	public String getIktlist() {
		return this.iktlist;
	}

	public int getPort() {
		return this.port;
	}

	public String getCharset() {
		return this.charset;
	}

	@SuppressWarnings("unchecked")
	public Node executeBeautifier(Node myHitlist) {
		/* Ausgabe des Opac-Ergebnissen in Datei */

		if (!ConfigMain.getParameter("debugFolder", "").equals("") && new File(ConfigMain.getParameter("debugFolder")).canWrite()) {
			debugMyNode(myHitlist, ConfigMain.getParameter("debugFolder") + "/opacBeautifyBefore.xml");
		}

		/*
		 * --------------------- aus dem Dom-Node ein JDom-Object machen -------------------
		 */
		Document doc = new DOMBuilder().build(myHitlist.getOwnerDocument());
	
		/*
		 * --------------------- Im JDom-Object alle Felder durchlaufen und die notwendigen Ersetzungen vornehmen -------------------
		 */
		/* alle Records durchlaufen */
		List<Element> elements = doc.getRootElement().getChildren();
		for (Element el : elements) {
			// Element el = (Element) it.next();
			/* in jedem Record den Beautifier anwenden */
			executeBeautifierForElement(el);
		}

		/*
		 * --------------------- aus dem JDom-Object wieder ein Dom-Node machen -------------------
		 */
		DOMOutputter doutputter = new DOMOutputter();
		try {
			myHitlist = doutputter.output(doc);
			myHitlist = myHitlist.getFirstChild();
		} catch (JDOMException e) {
			myLogger.error("JDOMException in executeBeautifier(Node)", e);
		}

		/* Ausgabe des überarbeiteten Opac-Ergebnisses */
		if (!ConfigMain.getParameter("debugFolder", "").equals("") && new File(ConfigMain.getParameter("debugFolder")).canWrite()) {
			debugMyNode(myHitlist, ConfigMain.getParameter("debugFolder") + "/opacBeautifyAfter.xml");
		}
		return myHitlist;
	}

	/**
	 * Beautifier für ein JDom-Object durchführen ================================================================
	 */
	@SuppressWarnings("unchecked")
	private void executeBeautifierForElement(Element el) {
		for (ConfigOpacCatalogueBeautifier beautifier : this.beautifySetList) {
			Element elementToChange = null;
			/* eine Kopie der zu prüfenden Elemente anlegen (damit man darin löschen kann */
			ArrayList<ConfigOpacCatalogueBeautifierElement> prooflist = new ArrayList<ConfigOpacCatalogueBeautifierElement>(beautifier
					.getTagElementsToProof());
			/* von jedem Record jedes Field durchlaufen */
			List<Element> elements = el.getChildren("field");
			for (Element field : elements) {
				String tag = field.getAttributeValue("tag");
				/* von jedem Field alle Subfelder durchlaufen */
				List<Element> subelements = field.getChildren("subfield");
				for (Element subfield : subelements) {
					String subtag = subfield.getAttributeValue("code");
					String value = subfield.getText();

					if (beautifier.getTagElementToChange().getTag().equals(tag) && beautifier.getTagElementToChange().getSubtag().equals(subtag)) {
						elementToChange = subfield;
					}
					/*
					 * wenn die Werte des Subfeldes in der Liste der zu prüfenden Beutifier-Felder stehen, dieses aus der Liste der Beautifier
					 * entfernen
					 */
					for (ConfigOpacCatalogueBeautifierElement cocbe : beautifier.getTagElementsToProof()) {
						if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag) && value.matches(cocbe.getValue())) {
							prooflist.remove(cocbe);
						}
					}
				}
			}
			/*
			 * --------------------- wenn in der Kopie der zu prüfenden Elemente keine Elemente mehr enthalten sind, kann der zu ändernde Wert
			 * wirklich geändert werden -------------------
			 */
			if (prooflist.size() == 0 && elementToChange != null) {
				elementToChange.setText(beautifier.getTagElementToChange().getValue());
			}

		}

	}

	/**
	 * Print given DomNode to defined File ================================================================
	 */
	private void debugMyNode(Node inNode, String fileName) {
		try {
			XMLOutputter outputter = new XMLOutputter();
			Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
			FileOutputStream output = new FileOutputStream(fileName);
			outputter.output(tempDoc.getRootElement(), output);
		} catch (FileNotFoundException e) {
			myLogger.error("debugMyNode(Node, String)", e);
		} catch (IOException e) {
			myLogger.error("debugMyNode(Node, String)", e);
		}

	}

	/**
	 * @param cbs
	 *            the cbs to set
	 */
	public void setCbs(String cbs) {
		this.cbs = cbs;
	}

	/**
	 * @return the cbs
	 */
	public String getCbs() {
		return this.cbs;
	}

    public String getOpacType() {
        return opacType;
    }

    public void setOpacType(String opacType) {
        this.opacType = opacType;
    }

}

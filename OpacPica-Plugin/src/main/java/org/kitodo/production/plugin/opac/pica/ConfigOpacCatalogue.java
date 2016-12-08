package org.kitodo.production.plugin.opac.pica; /**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

class ConfigOpacCatalogue {
	private static final Logger myLogger = Logger.getLogger(ConfigOpacCatalogue.class);
	private String title = "";
	private String description = "";
	private String address = "";
	private String database = "";
	private int port = 80;
	private String cbs;
	private String charset = "iso-8859-1";
	private final ArrayList<ConfigOpacCatalogueBeautifier> beautifySetList;
	private ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist,
			int port,
			ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
		this.title = title;
		this.description = desciption;
		this.address = address;
		this.database = database;
		this.port = port;
		this.beautifySetList = inBeautifySetList;
	}

	// Constructor that also takes a charset, a quick hack for DPD-81
	ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist,
			int port, String charset,
			String cbs, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType) {
		// Call the contructor above
		this(title, desciption, address, database, iktlist, port, inBeautifySetList, opacType);
		this.charset = charset;
		this.setCbs(cbs);
	}

	String getTitle() {
		return this.title;
	}

	String getDescription() {
		return this.description;
	}

	String getAddress() {
		return this.address;
	}

	String getDatabase() {
		return this.database;
	}

	int getPort() {
		return this.port;
	}

	String getCharset() {
		return this.charset;
	}

	@SuppressWarnings("unchecked")
	Node executeBeautifier(Node myHitlist) {
		/* Ausgabe des Opac-Ergebnissen in Datei */

		if (!PicaPlugin.getTempDir().equals("") && new File(PicaPlugin.getTempDir()).canWrite()) {
			debugMyNode(myHitlist, FilenameUtils.concat(PicaPlugin.getTempDir(), "opacBeautifyBefore.xml"));
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
		if (!PicaPlugin.getTempDir().equals("") && new File(PicaPlugin.getTempDir()).canWrite()) {
			debugMyNode(myHitlist, FilenameUtils.concat(PicaPlugin.getTempDir(), "opacBeautifyAfter.xml"));
		}
		return myHitlist;
	}

	/**
	 * Beautifier für ein JDom-Object durchführen ================================================================
	 */
	@SuppressWarnings("unchecked")
	private void executeBeautifierForElement(Element el) {
		for (ConfigOpacCatalogueBeautifier beautifier : this.beautifySetList) {
			int moreOccurrences;
			HashSet<Element> processed = new HashSet<Element>();
			do {
			Element elementToChange = null;
			Element tagged = null;
			moreOccurrences = 0;
			boolean merelyCount = false;
			/* eine Kopie der zu prüfenden Elemente anlegen (damit man darin löschen kann */
			ArrayList<ConfigOpacCatalogueBeautifierElement> prooflist = new ArrayList<ConfigOpacCatalogueBeautifierElement>(beautifier
					.getTagElementsToProof());
			/* von jedem Record jedes Field durchlaufen */
			List<Element> elements = el.getChildren("field");
			Matcher matcher = null;
			for (Element field : elements) {
				String tag = field.getAttributeValue("tag");
				/* von jedem Field alle Subfelder durchlaufen */
				List<Element> subelements = field.getChildren("subfield");
				for (Element subfield : subelements) {
					String subtag = subfield.getAttributeValue("code");
					String value = subfield.getText();

					if (beautifier.getTagElementToChange().getTag().equals(tag)) {
						if (!merelyCount) tagged = field;
						if (beautifier.getTagElementToChange().getSubtag().equals(subtag) && !processed.contains(subfield)) {
							if(!merelyCount) elementToChange = subfield;
							moreOccurrences++;
						}
					}
					/*
					 * wenn die Werte des Subfeldes in der Liste der zu prüfenden Beutifier-Felder stehen, dieses aus der Liste der Beautifier
					 * entfernen
					 */
					if(!merelyCount){
					for (ConfigOpacCatalogueBeautifierElement cocbe : beautifier.getTagElementsToProof()) {
							if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag)
									&& !processed.contains(subfield)) {
							matcher = Pattern.compile(cocbe.getValue()).matcher(value);
							if (cocbe.getMode().equals("matches") && matcher.matches() || matcher.find()) {
								prooflist.remove(cocbe);
								if (prooflist.size() == 0 && subfield.equals(elementToChange)){
									merelyCount = true;
								}
							}
						}
					}
					}
				}
			}
			/*
			 * --------------------- wenn in der Kopie der zu prüfenden Elemente keine Elemente mehr enthalten sind, kann der zu ändernde Wert
			 * wirklich geändert werden -------------------
			 */
			if (prooflist.size() == 0) {
				if (elementToChange == null) {
					if (tagged == null) {
						tagged = new Element("field");
						tagged.setAttribute("tag", beautifier.getTagElementToChange().getTag());
						el.addContent(tagged);
					}
					elementToChange = new Element("subfield");
					elementToChange.setAttribute("code", beautifier.getTagElementToChange().getSubtag());
					tagged.addContent(elementToChange);
				}
				if (beautifier.getTagElementToChange().getMode().equals("replace")) {
					elementToChange.setText(fillIn(beautifier.getTagElementToChange().getValue(), matcher));
				} else if (beautifier.getTagElementToChange().getMode().equals("prepend")) {
					elementToChange.setText(fillIn(beautifier.getTagElementToChange().getValue(), matcher).concat(
							elementToChange.getText()));
				} else if (beautifier.getTagElementToChange().getMode().equals("unescapeXml")) {
					elementToChange.setText(StringEscapeUtils.unescapeXml(fillIn(beautifier.getTagElementToChange()
							.getValue(), matcher)));
				} else {
					elementToChange.setText(elementToChange.getText().concat(
							fillIn(beautifier.getTagElementToChange().getValue(), matcher)));
				}
			}
			if(elementToChange != null) {
				processed.add(elementToChange);
			}
			} while (moreOccurrences > 1);
		}

	}

	/**
	 * The function fillIn() replaces marks in a given string by values derived
	 * from match results. There are two different mechanisms available for
	 * replacement.
	 * 
	 * If the marked string contains the replacement mark <code>{@}</code>, the
	 * matcher’s find() operation will be invoked over and over again and all
	 * match results are concatenated and inserted in place of the replacement
	 * marks.
	 * 
	 * Otherwise, all replacement marks <code>{1}</code>, <code>{2}</code>,
	 * <code>{3}</code>, … will be replaced by the capturing groups matched by
	 * the matcher.
	 * 
	 * @param markedString
	 *            a string with replacement markers
	 * @param matcher
	 *            a matcher who’s values shall be inserted
	 * @return the string with the replacements filled in
	 * @throws IndexOutOfBoundsException
	 *             If there is no capturing group in the pattern with the given
	 *             index
	 */
	private static String fillIn(String markedString, Matcher matcher) {
		if (matcher == null) {
			return markedString;
		}
		if (markedString.contains("{@}")) {
			StringBuilder composer = new StringBuilder();
			composer.append(matcher.group());
			while (matcher.find()) {
				composer.append(matcher.group());
			}
			return markedString.replaceAll("\\{@\\}", composer.toString());
		} else {
			StringBuffer replaced = new StringBuffer();
			Matcher replacer = Pattern.compile("\\{(\\d+)\\}").matcher(markedString);
			while (replacer.find()) {
				replacer.appendReplacement(replaced, matcher.group(Integer.parseInt(replacer.group(1))));
			}
			replacer.appendTail(replaced);
			return replaced.toString();
		}
	}

	/**
	 * Print given DomNode to defined File ================================================================
	 */
	private void debugMyNode(Node inNode, String fileName) {
		try (FileOutputStream output = new FileOutputStream(fileName)) {
			XMLOutputter outputter = new XMLOutputter();
			Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
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
	private void setCbs(String cbs) {
		this.cbs = cbs;
	}

	/**
	 * @return the cbs
	 */
	String getCbs() {
		return this.cbs;
	}

}

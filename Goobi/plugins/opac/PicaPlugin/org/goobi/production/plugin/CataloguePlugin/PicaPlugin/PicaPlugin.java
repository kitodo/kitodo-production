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
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.w3c.dom.Node;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

/**
 * The class PicaPlugin is the main class of the Goobi PICA catalogue plugin
 * implementation. It provides the public methods
 * 
 *    void    configure(Map) [*]
 *    Object  find(String, long)
 *    String  getDescription() [*]
 *    Map     getHit(Object, long, long)
 *    long    getNumberOfHits(Object, long)
 *    String  getTitle() [*]
 *    void    setPreferences(Prefs)
 *    boolean supportsCatalogue(String)
 *    void    useCatalogue(String)
 * 
 * as specified by org.goobi.production.plugin.UnspecificPlugin [*] and
 * org.goobi.production.plugin.CataloguePlugin.CataloguePlugin.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@PluginImplementation
public class PicaPlugin implements Plugin {
	/**
	 * 
	 */
	static final String LANGUAGES_MAPPING_FILE = "goobi_opacLanguages.txt";
	static final String OPAC_CONFIGURATION_FILE = "goobi_opac.xml";
	private static String configDir;
	private static String tempDir;
	private String catalogue;
	private Prefs preferences;

	/**
	 * The method configure() accepts a Map with configuration parameters. Two
	 * entries, "configDir" and "tempDir", are expcted.
	 * 
	 * configDir must point to a directory on the local file system where the
	 * plug-in can read individual configuration files from. The files
	 * "goobi_opac.xml" and "goobi_opacLanguages.txt" are expected in that
	 * directory.
	 * 
	 * @param configuration
	 *            a Map with configuration parameters
	 * @see {@link
	 *      org.goobi.production.plugin.UnspecificPlugin#configure(HashMap<
	 *      String, String>)}
	 */
	public void configure(Map<String, String> configuration) {
		configDir = configuration.get("configDir");
		tempDir = configuration.get("tempDir");
	}

	// @see org.goobi.production.plugin.CataloguePlugin#find(String, long)
	public Object find(String query, long timeout) {
		try {
			ConfigOpacCatalogue configuration = ConfigOpac.getCatalogueByName(catalogue);
			Catalogue catalogue = new Catalogue(configuration);
			GetOpac accessor = new GetOpac(catalogue);
			accessor.setCharset(configuration.getCharset());
			Query queryObject = new Query(query);
			int hits = accessor.getNumberOfHits(queryObject, timeout);
			return new FindResult(configuration, accessor, queryObject, hits);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	static String getConfigDir() {
		return configDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getDescription(Locale)
	public static String getDescription(Locale language) {
		return "The PICA plugin can be used to access PICA library catalogue systems.";
	}

	// @see org.goobi.production.plugin.CataloguePlugin#getHit(Object, long, long)
	public Map<String, Object> getHit(Object searchResult, long index, long timeout) {
		if (!(searchResult instanceof FindResult))
			throw new ClassCastException();
		// Catalogue catalogue = ((FindResult) searchResult).getCatalogue();
		GetOpac myOpac = ((FindResult) searchResult).getCatalogueAccessor();
		ConfigOpacCatalogue coc = ((FindResult) searchResult).getConfiguration();
		Query myQuery = ((FindResult) searchResult).getQuery();

		Element myFirstHit;
		String gattung;
		Fileformat ff;
		try {
			/*
			 * -------------------------------- Opac abfragen und erhaltenes
			 * Dom-Dokument in JDom-Dokument umwandeln
			 * --------------------------------
			 */
			Node myHitlist = myOpac.retrievePicaNode(myQuery, (int) index, (int) index, timeout);
			/* Opac-Beautifier aufrufen */
			myHitlist = coc.executeBeautifier(myHitlist);
			Document myJdomDoc = new DOMBuilder().build(myHitlist.getOwnerDocument());
			myFirstHit = myJdomDoc.getRootElement().getChild("record");

			/* von dem Treffer den Dokumententyp ermitteln */
			gattung = getGattung(myFirstHit);
			// ----- inlined: getConfigOpacDoctype()
			ConfigOpacDoctype cod = ConfigOpac.getDoctypeByMapping(gattung.length() > 2 ? gattung.substring(0, 2)
					: gattung, coc.getTitle());
			if (cod == null) {
				cod = ConfigOpac.getAllDoctypes().get(0);
				gattung = cod.getMappings().get(0);
			}

			// ------ end of inlined function

			/*
			 * -------------------------------- wenn der Treffer ein Volume
			 * eines Multivolume-Bandes ist, dann das Sammelwerk überordnen
			 * --------------------------------
			 */
			// if (isMultivolume()) {
			if (cod.isMultiVolume()) {
				/* Sammelband-PPN ermitteln */
				String multiVolumePpn = getPpnFromParent(myFirstHit, "036D", "9");
				if (multiVolumePpn != "") {
					/* Sammelband aus dem Opac holen */

					myQuery = new Query(multiVolumePpn, "12");
					/* wenn ein Treffer des Parents im Opac gefunden wurde */
					if (myOpac.getNumberOfHits(myQuery, timeout) == 1) {
						Node myParentHitlist = myOpac.retrievePicaNode(myQuery, 1, timeout);
						/* Opac-Beautifier aufrufen */
						myParentHitlist = coc.executeBeautifier(myParentHitlist);
						/* Konvertierung in jdom-Elemente */
						Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

						/* dem Rootelement den Volume-Treffer hinzufügen */
						myFirstHit.getParent().removeContent(myFirstHit);
						myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

						myJdomDoc = myJdomDocMultivolumeband;
						myFirstHit = myJdomDoc.getRootElement().getChild("record");

						/* die Jdom-Element wieder zurück zu Dom konvertieren */
						DOMOutputter doutputter = new DOMOutputter();
						myHitlist = doutputter.output(myJdomDocMultivolumeband);
						/*
						 * dabei aber nicht das Document, sondern das erste Kind
						 * nehmen
						 */
						myHitlist = myHitlist.getFirstChild();
					}
				}
			}

			/*
			 * -------------------------------- wenn der Treffer ein Contained
			 * Work ist, dann übergeordnetes Werk
			 * --------------------------------
			 */
			// if (isContainedWork()) {
			if (cod.isContainedWork()) {
				/* PPN des übergeordneten Werkes ermitteln */
				String ueberGeordnetePpn = getPpnFromParent(myFirstHit, "021A", "9");
				if (ueberGeordnetePpn != "") {
					/* Sammelband aus dem Opac holen */
					myQuery = new Query(ueberGeordnetePpn, "12");
					/* wenn ein Treffer des Parents im Opac gefunden wurde */
					if (myOpac.getNumberOfHits(myQuery, timeout) == 1) {
						Node myParentHitlist = myOpac.retrievePicaNode(myQuery, 1, timeout);
						/* Opac-Beautifier aufrufen */
						myParentHitlist = coc.executeBeautifier(myParentHitlist);
						/* Konvertierung in jdom-Elemente */
						Document myJdomDocParent = new DOMBuilder().build(myParentHitlist.getOwnerDocument());
						Element myFirstHitParent = myJdomDocParent.getRootElement().getChild("record");
						/*
						 * alle Elemente des Parents übernehmen, die noch nicht
						 * selbst vorhanden sind
						 */
						if (myFirstHitParent.getChildren() != null) {
							for (@SuppressWarnings("unchecked")
							Iterator<Element> iter = myFirstHitParent.getChildren().iterator(); iter.hasNext();) {
								Element ele = iter.next();
								if (getElementFromChildren(myFirstHit, ele.getAttributeValue("tag")) == null) {
									@SuppressWarnings("unchecked")
									List<Element> children = myFirstHit.getChildren();
									children.add(getCopyFromJdomElement(ele));
								}
							}
						}
					}
				}
			}

			/*
			 * -------------------------------- aus Opac-Ergebnis RDF-Datei
			 * erzeugen --------------------------------
			 */

			/* zugriff auf ugh-Klassen */
			PicaPlus pp = new PicaPlus(preferences);
			pp.read(myHitlist);
			DigitalDocument dd = pp.getDigitalDocument();
			ff = new XStream(preferences);
			ff.setDigitalDocument(dd);
			/* BoundBook hinzufügen */
			DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
			DocStruct dsBoundBook = dd.createDocStruct(dst);
			dd.setPhysicalDocStruct(dsBoundBook);
			/* Inhalt des RDF-Files überprüfen und ergänzen */
			checkMyOpacResult(ff.getDigitalDocument(), preferences, myFirstHit, cod, gattung);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return createResult(gattung, myFirstHit, ff);
	}

	/**
	 * DocType (Gattung) ermitteln
	 * 
	 * @param inHit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String getGattung(Element inHit) {
		if (inHit == null)
			return "";
		for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
			Element tempElement = iter.next();
			String feldname = tempElement.getAttributeValue("tag");
			// System.out.println(feldname);
			if (feldname.equals("002@")) {
				return getSubelementValue(tempElement, "0");
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private static String getSubelementValue(Element inElement, String attributeValue) {
		String rueckgabe = "";

		for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
			Element subElement = iter.next();
			if (subElement.getAttributeValue("code").equals(attributeValue)) {
				rueckgabe = subElement.getValue();
			}
		}
		return rueckgabe;
	}

	/**
	 * die PPN des übergeordneten Bandes (MultiVolume: 036D-9 und ContainedWork:
	 * 021A-9) ermitteln
	 * 
	 * @param inElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String getPpnFromParent(Element inHit, String inFeldName, String inSubElement) {
		for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
			Element tempElement = iter.next();
			String feldname = tempElement.getAttributeValue("tag");
			// System.out.println(feldname);
			if (feldname.equals(inFeldName)) {
				return getSubelementValue(tempElement, inSubElement);
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private static Element getElementFromChildren(Element inHit, String inTagName) {
		for (Iterator<Element> iter2 = inHit.getChildren().iterator(); iter2.hasNext();) {
			Element myElement = iter2.next();
			String feldname = myElement.getAttributeValue("tag");
			// System.out.println(feldname);
			/*
			 * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden
			 * Attribut zurückgeben
			 */
			if (feldname.equals(inTagName)) {
				return myElement;
			}
		}
		return null;
	}

	/**
	 * rekursives Kopieren von Elementen, weil das Einfügen eines Elements an
	 * einen anderen Knoten mit dem Fehler abbricht, dass das einzufügende
	 * Element bereits einen Parent hat
	 * ================================================================
	 */
	@SuppressWarnings("unchecked")
	private static Element getCopyFromJdomElement(Element inHit) {
		Element myElement = new Element(inHit.getName());
		myElement.setText(inHit.getText());
		/* jetzt auch alle Attribute übernehmen */
		if (inHit.getAttributes() != null) {
			for (Iterator<Attribute> iter = inHit.getAttributes().iterator(); iter.hasNext();) {
				Attribute att = iter.next();
				myElement.getAttributes().add(new Attribute(att.getName(), att.getValue()));
			}
		}
		/* jetzt auch alle Children übernehmen */
		if (inHit.getChildren() != null) {

			for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
				Element ele = iter.next();
				myElement.addContent(getCopyFromJdomElement(ele));
			}
		}
		return myElement;
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Erg�nze das
	 * Docstruct um zusätzliche Opac-Details ##
	 * #####################################################
	 * ####################################################
	 */

	private static void checkMyOpacResult(DigitalDocument inDigDoc, Prefs inPrefs, Element myFirstHit,
			ConfigOpacDoctype cod, String gattung) {
		DocStruct topstruct = inDigDoc.getLogicalDocStruct();
		DocStruct boundbook = inDigDoc.getPhysicalDocStruct();
		DocStruct topstructChild = null;
		Element mySecondHit = null;

		/*
		 * -------------------------------- bei Multivolumes noch das Child in
		 * xml und docstruct ermitteln --------------------------------
		 */
		// if (isMultivolume()) {
		if (cod.isMultiVolume()) {
			try {
				topstructChild = topstruct.getAllChildren().get(0);
			} catch (RuntimeException e) {
			}
			mySecondHit = (Element) myFirstHit.getParentElement().getChildren().get(1);
		}

		/*
		 * -------------------------------- vorhandene PPN als digitale oder
		 * analoge einsetzen --------------------------------
		 */
		String ppn = getElementFieldValue(myFirstHit, "003@", "0");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", "");
		if (gattung.toLowerCase().startsWith("o")) {
			UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", ppn);
		} else {
			UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDSource", ppn);
		}

		/*
		 * -------------------------------- wenn es ein multivolume ist, dann
		 * auch die PPN prüfen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String secondHitppn = getElementFieldValue(mySecondHit, "003@", "0");
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", "");
			if (gattung.toLowerCase().startsWith("o")) {
				UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", secondHitppn);
			} else {
				UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDSource", secondHitppn);
			}
		}

		/*
		 * -------------------------------- den Main-Title bereinigen
		 * --------------------------------
		 */
		String myTitle = getElementFieldValue(myFirstHit, "021A", "a");
		/*
		 * wenn der Fulltittle nicht in dem Element stand, dann an anderer
		 * Stelle nachsehen (vor allem bei Contained-Work)
		 */
		if (myTitle == null || myTitle.length() == 0) {
			myTitle = getElementFieldValue(myFirstHit, "021B", "a");
		}
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMain", myTitle.replaceAll("@", ""));

		/*
		 * -------------------------------- Sorting-Titel mit
		 * Umlaut-Konvertierung --------------------------------
		 */
		if (myTitle.indexOf("@") != -1) {
			myTitle = myTitle.substring(myTitle.indexOf("@") + 1);
		}
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMainShort", myTitle);

		/*
		 * -------------------------------- bei multivolumes den Main-Title
		 * bereinigen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String fulltitleMulti = getElementFieldValue(mySecondHit, "021A", "a").replaceAll("@", "");
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMain", fulltitleMulti);
		}

		/*
		 * -------------------------------- bei multivolumes den Sorting-Titel
		 * mit Umlaut-Konvertierung --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String sortingTitleMulti = getElementFieldValue(mySecondHit, "021A", "a");
			if (sortingTitleMulti.indexOf("@") != -1) {
				sortingTitleMulti = sortingTitleMulti.substring(sortingTitleMulti.indexOf("@") + 1);
			}
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMainShort", sortingTitleMulti);
			// sortingTitle = sortingTitleMulti;
		}

		/*
		 * -------------------------------- Sprachen - Konvertierung auf zwei
		 * Stellen --------------------------------
		 */
		String sprache = getElementFieldValue(myFirstHit, "010@", "a");
		sprache = UGHUtils.convertLanguage(sprache);
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "DocLanguage", sprache);

		/*
		 * -------------------------------- bei multivolumes die Sprachen -
		 * Konvertierung auf zwei Stellen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String spracheMulti = getElementFieldValue(mySecondHit, "010@", "a");
			spracheMulti = UGHUtils.convertLanguage(spracheMulti);
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "DocLanguage", spracheMulti);
		}

		/*
		 * -------------------------------- ISSN
		 * --------------------------------
		 */
		String issn = getElementFieldValue(myFirstHit, "005A", "0");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "ISSN", issn);

		/*
		 * -------------------------------- Copyright
		 * --------------------------------
		 */
		String copyright = getElementFieldValue(myFirstHit, "037I", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "copyrightimageset", copyright);

		/*
		 * -------------------------------- Format
		 * --------------------------------
		 */
		String format = getElementFieldValue(myFirstHit, "034I", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "FormatSourcePrint", format);

		/*
		 * -------------------------------- Umfang
		 * --------------------------------
		 */
		String umfang = getElementFieldValue(myFirstHit, "034D", "a");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "SizeSourcePrint", umfang);

		/*
		 * -------------------------------- Signatur
		 * --------------------------------
		 */
		String sig = getElementFieldValue(myFirstHit, "209A", "c");
		if (sig.length() > 0) {
			sig = "<" + sig + ">";
		}
		sig += getElementFieldValue(myFirstHit, "209A", "f") + " ";
		sig += getElementFieldValue(myFirstHit, "209A", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
		if (sig.trim().length() == 0) {
			sig = getElementFieldValue(myFirstHit, "209A/01", "c");
			if (sig.length() > 0) {
				sig = "<" + sig + ">";
			}
			sig += getElementFieldValue(myFirstHit, "209A/01", "f") + " ";
			sig += getElementFieldValue(myFirstHit, "209A/01", "a");
			if (mySecondHit != null) {
				sig += getElementFieldValue(mySecondHit, "209A", "f") + " ";
				sig += getElementFieldValue(mySecondHit, "209A", "a");
			}
			UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
		}

		/*
		 * -------------------------------- bei Zeitschriften noch ein
		 * PeriodicalVolume als Child einfügen --------------------------------
		 */
		// if (isPeriodical()) {
		if (cod.isPeriodical()) {
			try {
				DocStructType dstV = inPrefs.getDocStrctTypeByName("PeriodicalVolume");
				DocStruct dsvolume = inDigDoc.createDocStruct(dstV);
				topstruct.addChild(dsvolume);
			} catch (TypeNotAllowedForParentException e) {
				e.printStackTrace();
			} catch (TypeNotAllowedAsChildException e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static String getElementFieldValue(Element myFirstHit, String inFieldName, String inAttributeName) {

		for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
			Element myElement = iter2.next();
			String feldname = myElement.getAttributeValue("tag");
			/*
			 * wenn es das gesuchte Feld ist, dann den Wert mit dem passenden
			 * Attribut zurückgeben
			 */
			if (feldname.equals(inFieldName)) {
				return getFieldValue(myElement, inAttributeName);
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private static String getFieldValue(Element inElement, String attributeValue) {
		String rueckgabe = "";

		for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
			Element subElement = iter.next();
			if (subElement.getAttributeValue("code").equals(attributeValue)) {
				rueckgabe = subElement.getValue();
			}
		}
		return rueckgabe;
	}

	private Map<String, Object> createResult(String docType, Element hit, Fileformat fileformat) {
		final LocalTime DAYEND = new LocalTime(23, 59, 59, 999);

		Map<String, Object> result = new HashMap<String, Object>();
		LocalDate today = new LocalDate();

		result.put("type", docType);
		if (docType.startsWith("O"))
			result.put("format", "internet");
		result.put("fileformat", fileformat);

		// add some basic metadata
		String accessed = getElementFieldValue(hit, "208@", "a");
		try {
			LocalDate date = toRecentLocalDate(accessed, today);
			result.put("accessed", date.toDateTime(date.isEqual(today) ? new LocalTime() : DAYEND).toString());
		} catch (RuntimeException r) {
		}

		String lastName = getElementFieldValue(hit, "028A", "a");
		if (lastName.equals(""))
			lastName = getElementFieldValue(hit, "028A", "l");
		String firstName = getElementFieldValue(hit, "028A", "d");
		if (firstName.equals(""))
			firstName = getElementFieldValue(hit, "028A", "P");
		String middleTitle = getElementFieldValue(hit, "028A", "c");
		String author = lastName + (!firstName.equals("") ? ", " : "") + firstName
				+ (!middleTitle.equals("") ? " " : "") + middleTitle;
		if (author.equals(""))
			author = getElementFieldValue(hit, "028A", "8");
		if (author.equals("")) {
			String lastName2 = getElementFieldValue(hit, "028C", "a");
			if (lastName2.equals(""))
				lastName2 = getElementFieldValue(hit, "028C", "l");
			String firstName2 = getElementFieldValue(hit, "028C", "d");
			if (firstName2.equals(""))
				firstName2 = getElementFieldValue(hit, "028C", "P");
			String middleTitle2 = getElementFieldValue(hit, "028C", "c");
			author = lastName2 + (!firstName2.equals("") ? ", " : "") + firstName2
					+ (!middleTitle2.equals("") ? " " : "") + middleTitle2;
		}
		result.put("creator", author);

		String date = getElementFieldValue(hit, "201B", "0");
		try {
			LocalDate localDate = toRecentLocalDate(date, today);
			result.put("date", localDate.toString());
		} catch (RuntimeException r) {
		}

		result.put("edition", getElementFieldValue(hit, "032@", "a"));
		result.put("number", getElementFieldValue(hit, "036E", "l"));
		result.put("place", getElementFieldValue(hit, "033A", "p"));
		result.put("publisher", getElementFieldValue(hit, "033A", "n"));
		result.put("series", getElementFieldValue(hit, "036E", "a"));

		String subseries = getElementFieldValue(hit, "021A", "d");
		if (subseries == null || subseries.length() == 0)
			subseries = getElementFieldValue(hit, "021B", "d");
		if (subseries == null || subseries.length() == 0)
			subseries = getElementFieldValue(hit, "027D", "d");
		result.put("subseries", subseries);

		String title = getElementFieldValue(hit, "021A", "a");
		if (title == null || title.length() == 0)
			title = getElementFieldValue(hit, "021B", "a");
		if (title == null || title.length() == 0)
			title = getElementFieldValue(hit, "027D", "a");
		String titleLong = getElementFieldValue(hit, "021A", "d");
		if (titleLong != null && titleLong.length() > 0)
			title = title + " : " + titleLong;
		result.put("title", title.replaceAll("@", ""));

		result.put("url", getElementFieldValue(hit, "209R", "a"));
		result.put("year", getElementFieldValue(hit, "011@", "a"));
		return result;
	}

	private LocalDate toRecentLocalDate(String dd_mm_yy, LocalDate today) {
		int centuryPrefix = today.getYear() / 100;
		String[] fields = dd_mm_yy.split("-");
		int year = (100 * centuryPrefix) + Integer.parseInt(fields[2]);
		if (year > today.getYear())
			year -= 100;
		return new LocalDate(year, Integer.parseInt(fields[1]), Integer.parseInt(fields[0]));
	}

	// @see org.goobi.production.plugin.CataloguePlugin#getNumberOfHits(Object, long)
	public static long getNumberOfHits(Object searchResult, long timeout) {
		if (searchResult instanceof FindResult)
			return ((FindResult) searchResult).getHits();
		else
			throw new ClassCastException();
	}

	static String getTempDir() {
		return tempDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getTitle(Locale)
	public static String getTitle(Locale language) {
		return "PICA Catalogue Plugin";
	}

	// @see org.goobi.production.plugin.CataloguePlugin#setPreferences(Prefs)
	public void setPreferences(Prefs preferences) {
		this.preferences = preferences;
	}

	// @see org.goobi.production.plugin.CataloguePlugin#supportsCatalogue(String)
	public static boolean supportsCatalogue(String catalogue) {
		return ConfigOpac.getCatalogueByName(catalogue) != null;
	}

	// @see org.goobi.production.plugin.CataloguePlugin#useCatalogue(String)
	public void useCatalogue(String catalogue) {
		this.catalogue = catalogue;
	}
}
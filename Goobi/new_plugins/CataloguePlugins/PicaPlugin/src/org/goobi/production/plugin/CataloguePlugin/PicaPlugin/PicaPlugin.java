package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
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

@PluginImplementation
public class PicaPlugin {
	public static final String LANGUAGES_MAPPING_FILE = "goobi_opacLanguages.txt";

	private static String configDir;
	private static String tempDir;
	private String catalogue;
	private Prefs preferences;

	// @see org.goobi.production.plugin.UnspecificPlugin#configure(HashMap<String, String>)
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
			return new FindResult(configuration, catalogue, accessor, queryObject, hits);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String getConfigDir() {
		return configDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getDescription()
	public static String getDescription() {
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
			Node myHitlist = myOpac.retrievePicaNode(myQuery, (int) index, (int) index);
			/* Opac-Beautifier aufrufen */
			myHitlist = coc.executeBeautifier(myHitlist);
			Document myJdomDoc = new DOMBuilder().build(myHitlist.getOwnerDocument());
			myFirstHit = myJdomDoc.getRootElement().getChild("record");

			/* von dem Treffer den Dokumententyp ermitteln */
			gattung = getGattung(myFirstHit);
			// ----- inlined: getConfigOpacDoctype()
			ConfigOpacDoctype cod = ConfigOpac.getDoctypeByMapping(gattung.substring(0, 2), coc.getTitle());
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

						/* Testausgabe */
						// XMLOutputter outputter = new XMLOutputter();
						// FileOutputStream output = new
						// FileOutputStream("D:/fileParent.xml");
						// outputter.output(myJdomDocMultivolumeband.getRootElement(),
						// output);
						/* dem Rootelement den Volume-Treffer hinzufügen */
						myFirstHit.getParent().removeContent(myFirstHit);
						myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

						/* Testausgabe */
						// output = new FileOutputStream("D:/fileFull.xml");
						// outputter.output(myJdomDocMultivolumeband.getRootElement(),
						// output);
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
						/* Testausgabe */
						// XMLOutputter outputter = new XMLOutputter();
						// FileOutputStream output = new
						// FileOutputStream("D:/fileParent.xml");
						// outputter.output(myJdomDocParent.getRootElement(),
						// output);
						/*
						 * alle Elemente des Parents übernehmen, die noch nicht
						 * selbst vorhanden sind
						 */
						if (myFirstHitParent.getChildren() != null) {

							for (Iterator<Element> iter = myFirstHitParent.getChildren().iterator(); iter.hasNext();) {
								Element ele = iter.next();
								if (getElementFromChildren(myFirstHit, ele.getAttributeValue("tag")) == null) {
									myFirstHit.getChildren().add(getCopyFromJdomElement(ele));
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
			/* XML in Datei schreiben */
			//		 XMLOutputter outputter = new XMLOutputter();
			//		 FileOutputStream output = new
			//		 FileOutputStream("/home/robert/temp_opac.xml");
			//		 outputter.output(myJdomDoc.getRootElement(), output);

			/* myRdf temporär in Datei schreiben */
			// myRdf.write("D:/temp.rdf.xml");

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
			// rdftemp.write("D:/PicaRdf.xml");

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		String author = getElementFieldValue(myFirstHit, "028A", "a");
		if (author == null || author.equals(""))
			author = getElementFieldValue(myFirstHit, "028A", "8");

		String title = getElementFieldValue(myFirstHit, "021A", "a");
		if (title == null || title.length() == 0)
			title = getElementFieldValue(myFirstHit, "021B", "a");

		String bibliographicCitation = null; // TODO

		// return hit
		Map<String, Object> result = new HashMap<String, Object>(7);
		result.put("bibliographicCitation", bibliographicCitation);
		result.put("creator", author);
		result.put("docType", gattung);
		result.put("fileformat", ff);
		result.put("title", title);
		return result;
	}

	/**
	 * DocType (Gattung) ermitteln
	 * 
	 * @param inHit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String getGattung(Element inHit) {

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
		UghHelper.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", "");
		if (gattung.toLowerCase().startsWith("o")) {
			UghHelper.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", ppn);
		} else {
			UghHelper.replaceMetadatum(topstruct, inPrefs, "CatalogIDSource", ppn);
		}

		/*
		 * -------------------------------- wenn es ein multivolume ist, dann
		 * auch die PPN prüfen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String secondHitppn = getElementFieldValue(mySecondHit, "003@", "0");
			UghHelper.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", "");
			if (gattung.toLowerCase().startsWith("o")) {
				UghHelper.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", secondHitppn);
			} else {
				UghHelper.replaceMetadatum(topstructChild, inPrefs, "CatalogIDSource", secondHitppn);
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
		UghHelper.replaceMetadatum(topstruct, inPrefs, "TitleDocMain", myTitle.replaceAll("@", ""));

		/*
		 * -------------------------------- Sorting-Titel mit
		 * Umlaut-Konvertierung --------------------------------
		 */
		if (myTitle.indexOf("@") != -1) {
			myTitle = myTitle.substring(myTitle.indexOf("@") + 1);
		}
		UghHelper.replaceMetadatum(topstruct, inPrefs, "TitleDocMainShort", myTitle);

		/*
		 * -------------------------------- bei multivolumes den Main-Title
		 * bereinigen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String fulltitleMulti = getElementFieldValue(mySecondHit, "021A", "a").replaceAll("@", "");
			UghHelper.replaceMetadatum(topstructChild, inPrefs, "TitleDocMain", fulltitleMulti);
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
			UghHelper.replaceMetadatum(topstructChild, inPrefs, "TitleDocMainShort", sortingTitleMulti);
			// sortingTitle = sortingTitleMulti;
		}

		/*
		 * -------------------------------- Sprachen - Konvertierung auf zwei
		 * Stellen --------------------------------
		 */
		String sprache = getElementFieldValue(myFirstHit, "010@", "a");
		sprache = UghHelper.convertLanguage(sprache);
		UghHelper.replaceMetadatum(topstruct, inPrefs, "DocLanguage", sprache);

		/*
		 * -------------------------------- bei multivolumes die Sprachen -
		 * Konvertierung auf zwei Stellen --------------------------------
		 */
		if (topstructChild != null && mySecondHit != null) {
			String spracheMulti = getElementFieldValue(mySecondHit, "010@", "a");
			spracheMulti = UghHelper.convertLanguage(spracheMulti);
			UghHelper.replaceMetadatum(topstructChild, inPrefs, "DocLanguage", spracheMulti);
		}

		/*
		 * -------------------------------- ISSN
		 * --------------------------------
		 */
		String issn = getElementFieldValue(myFirstHit, "005A", "0");
		UghHelper.replaceMetadatum(topstruct, inPrefs, "ISSN", issn);

		/*
		 * -------------------------------- Copyright
		 * --------------------------------
		 */
		String copyright = getElementFieldValue(myFirstHit, "037I", "a");
		UghHelper.replaceMetadatum(boundbook, inPrefs, "copyrightimageset", copyright);

		/*
		 * -------------------------------- Format
		 * --------------------------------
		 */
		String format = getElementFieldValue(myFirstHit, "034I", "a");
		UghHelper.replaceMetadatum(boundbook, inPrefs, "FormatSourcePrint", format);

		/*
		 * -------------------------------- Umfang
		 * --------------------------------
		 */
		String umfang = getElementFieldValue(myFirstHit, "034D", "a");
		UghHelper.replaceMetadatum(topstruct, inPrefs, "SizeSourcePrint", umfang);

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
		UghHelper.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
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
			UghHelper.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", sig.trim());
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

	// @see org.goobi.production.plugin.CataloguePlugin#getNumberOfHits(Object, long)
	public static long getNumberOfHits(Object searchResult, long timeout) {
		if (searchResult instanceof FindResult)
			return ((FindResult) searchResult).getHits();
		else
			throw new ClassCastException();
	}

	public static String getTempDir() {
		return tempDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getTitle()
	public static String getTitle() {
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

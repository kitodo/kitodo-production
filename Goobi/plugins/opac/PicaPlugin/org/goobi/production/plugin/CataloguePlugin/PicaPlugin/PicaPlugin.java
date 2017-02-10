/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.jdom.*;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.joda.time.*;
import org.w3c.dom.Node;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.*;
import ugh.exceptions.*;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

/**
 * The class PicaPlugin is the main class of the Kitodo PICA catalogue plug-in
 * implementation. This plug-in can be used to access OCLC PICA library
 * catalogue systems. It provides the public methods
 * 
 * <pre>
 * void     configure(Map) [*]
 * Object   find(String, long)
 * String   getDescription() [*]
 * Map      getHit(Object, long, long)
 * long     getNumberOfHits(Object, long)
 * String   getTitle() [*]
 * void     setPreferences(Prefs)
 * boolean  supportsCatalogue(String)
 * void     useCatalogue(String)
 * </pre>
 * 
 * as specified by {@code org.goobi.production.plugin.UnspecificPlugin}
 * {@code [*]} and
 * {@code org.goobi.production.plugin.CataloguePlugin.CataloguePlugin}.
 *
 * @author unascribed
 * @author Matthias Ronge
 * 
 * @see "https://en.wikipedia.org/wiki/OCLC_PICA"
 */
@PluginImplementation
public class PicaPlugin implements Plugin {
	/**
	 * The constant LANGUAGES_MAPPING_FILE holds the name of the PICA plug-in
	 * languages mapping file. This is a text file with lines in form
	 * replacement—space—stringToReplace used to replace the value from PICA+
	 * field “010@” subfield “a” (the replacement will be saved in DocStruct
	 * “DocLanguage”) The file is optional. To use this functionality, the file
	 * must be located in {@link #configDir}.
	 */
	static final String LANGUAGES_MAPPING_FILE = "goobi_opacLanguages.txt";

	/**
	 * The constant OPAC_CONFIGURATION_FILE holds the name of the PICA plug-in
	 * main configuration file. Required. The file must be located in
	 * {@link #configDir}.
	 */
	static final String OPAC_CONFIGURATION_FILE = "kitodo_pica_opac.xml";

	/**
	 * The field configDir holds a reference to the file system directory where
	 * configuration files are read from. The field is initialised by Production
	 * that calls {@link #configure(Map)}.
	 */
	private static String configDir;

	/**
	 * The field tempDir holds a reference to the file system directory where
	 * temporary files are written in. Thus, servlet container needs write
	 * access to that directory. The field is initialised by Production that
	 * calls {@link #configure(Map)}.
	 */
	private static String tempDir;

	/**
	 * The field preferences holds the UGH preferences.
	 */
	private Prefs preferences;

	/**
	 * The field configuration holds the catalogue configuration.
	 */
	private Catalogue catalogue;

	/**
	 * Returns the XMLConfiguration of the plugin containing docType
	 * names and conditions for structureType classification.
	 *
	 * @return config
	 *            the XMLConfiguration of the plugin
	 */
	public XMLConfiguration getXMLConfiguration() {
		return OpacCatalogues.getConfig();
	}

	/**
	 * The field client holds the catalogue client used to access the catalogue.
	 */
	private CatalogueClient client;

	/**
	 * Injects the plug-in’s configuration. The method takes a Map with
	 * configuration parameters. Two entries, {@code configDir} and
	 * {@code tempDir}, are expected:
	 *
	 * <ul>
	 * <li>{@code configDir} must point to a directory on the local file system
	 * where the plug-in can read individual configuration files from. The
	 * configuration file {@code kitodo_pica_opac.xml} is expected in that
	 * directory.</li>
	 * <li>{@code tempDir} is a directory on the local file system where the
	 * plug-in can write temporary files to. Currently, two files are written
	 * there to help admins configure opac beautifiers correctly.</li>
	 * </ul>
	 *
	 * This method is called at runtime after the classloader has created the
	 * instance, because the plug-in constructor cannot take arguments. Confer
	 * to {@code UnspecificPlugin.configure(Map)} in package
	 * {@code org.goobi.production.plugin} of the core application, too.
	 *
	 * @param configuration
	 *            a Map with configuration parameters
	 */
	public void configure(Map<String, String> configuration) {
		configDir = configuration.get("configDir");
		tempDir = configuration.get("tempDir");
	}

	/**
	 * Initially queries the library catalogue with the given query. If
	 * successful, it returns a FindResult with the number of hits.
	 * <p>
	 * For the semantics of the query, see class {@code QueryBuilder} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application. Confer to {@code UnspecificPlugin.find(String, long)} in
	 * package {@code org.goobi.production.plugin} of the core application, too.
	 *
	 * @param query
	 *            the query to the catalogue
	 * @param timeout
	 *            timeout in milliseconds after which the operation shall return
	 * @return a FindResult that may be used for future operations on the query
	 */
	public Object find(String query, long timeout) {
		client.setTimeout(timeout);
		try {
			Query queryObject = new Query(query);
			int hits = client.getNumberOfHits(queryObject);
			if (hits > 0) {
				return new FindResult(queryObject, hits);
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Provides a reference to the file system directory where configuration
	 * files are read from.
	 *
	 * @return the file system directory with the configuration files
	 */
	static String getConfigDir() {
		return configDir;
	}

	/**
	 * Returns a human-readable description of the plug-in’s functionality in
	 * English. The parameter language is ignored.
	 * <p>
	 * Confer to {@code UnspecificPlugin.getDescription(Locale)} in package
	 * {@code org.goobi.production.plugin} of the core application, too.
	 *
	 * @param language
	 *            desired language of the human-readable description (support is
	 *            optional)
	 * @return a human-readable description of the plug-in’s functionality
	 */
	public static String getDescription(Locale language) {
		return "The PICA plugin can be used to access PICA library catalogue systems.";
	}

	/**
	 * Returns the hit with the given index from the given search result. The
	 * object returned is a Map&lt;String, Object>. It contains the full hit as
	 * {@code fileformat}, the docType as {@code type} and some bibliographic
	 * meta-data to show a bibliographic hit summary as supposed in class
	 * {@code Hit} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application.
	 * <p>
	 * Confer to {@code CataloguePlugin.getHit(Object, long, long)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @param searchResult
	 *            a FindResult created by {@link #find(String, long)}
	 * @param index
	 *            the zero-based index of the hit
	 * @param timeout
	 *            a timeout in milliseconds after which the operation shall
	 *            return
	 * @return a Map with the hit
	 */
	public Map<String, Object> getHit(Object searchResult, long index, long timeout) {
		client.setTimeout(timeout);
		if (!(searchResult instanceof FindResult)) {
			throw new ClassCastException();
		}
		Query myQuery = ((FindResult) searchResult).getQuery();

		Element myFirstHit;
		String typeID;
		Type type;
		Fileformat ff;
		try {

			// Query OPAC and convert the DOM document returned to JDOM

			Node myHitlist = client.retrievePicaNode(myQuery, (int) index, (int) index + 1, timeout);

			// call OPAC beautifier
			myHitlist = catalogue.executeBeautifier(myHitlist);
			Document myJdomDoc = new DOMBuilder().build(myHitlist.getOwnerDocument());
			myFirstHit = myJdomDoc.getRootElement().getChild("record");

			// detemine the document type from the first hit
			typeID = getTypeID(myFirstHit);
			type = OpacCatalogues.getDoctypeByMapping(typeID.length() > 2 ? typeID.substring(0, 2) : typeID,
					catalogue.getTitle());
			if (type == null) {
				type = OpacCatalogues.getDoctypes().get(0);
				typeID = type.getMappings().get(0);
			}

			/* if the hit is a volume from a multivolume volume, then
			 * superordinate the compilation */

			if (type.isMultiVolume()) {
				// determine the PPN of the anthology
				String multiVolumePpn = getPPNFromParent(myFirstHit, "036D", "9");
				if (!multiVolumePpn.equals("")) {
					// take the anthology out of the OPAC

					myQuery = new Query(multiVolumePpn, "12");
					// if a hit of the parent was found in the OPAC
					if (client.getNumberOfHits(myQuery) == 1) {
						Node myParentHitlist = client.retrievePicaNode(myQuery, 1);
						// call OPAC beautifier
						myParentHitlist = catalogue.executeBeautifier(myParentHitlist);
						// conversion to JDOM elements
						Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

						// add the volume-hit to the root element
						myFirstHit.getParent().removeContent(myFirstHit);
						myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

						myJdomDoc = myJdomDocMultivolumeband;
						myFirstHit = myJdomDoc.getRootElement().getChild("record");

						// convert the JDOM elements back to DOM
						DOMOutputter doutputter = new DOMOutputter();
						myHitlist = doutputter.output(myJdomDocMultivolumeband);

						/* nevertheless, do not take the document, but the first
						 * child */

						myHitlist = myHitlist.getFirstChild();
					}
				}
			}

			/* if the hit is a volume from a periodical volume, then
			 * superordinate the series */

			if (type.isPeriodical()) {
				// determine the PPN of the anthology
				String serialPublicationPpn = getPPNFromParent(myFirstHit, "036F", "9");
				if (!serialPublicationPpn.equals("")) {
					// take the anthology out of the OPAC

					myQuery = new Query(serialPublicationPpn, "12");
					// if a hit of the parent was found in the OPAC
					if (client.getNumberOfHits(myQuery) == 1) {
						Node myParentHitlist = client.retrievePicaNode(myQuery, 1);
						// call OPAC beautifier
						myParentHitlist = catalogue.executeBeautifier(myParentHitlist);
						// conversion to JDOM elements
						Document myJdomDocMultivolumeband = new DOMBuilder().build(myParentHitlist.getOwnerDocument());

						// add the volume-hit to the root element
						myFirstHit.getParent().removeContent(myFirstHit);
						myJdomDocMultivolumeband.getRootElement().addContent(myFirstHit);

						myJdomDoc = myJdomDocMultivolumeband;
						myFirstHit = myJdomDoc.getRootElement().getChild("record");

						// convert the JDOM elements back to DOM
						DOMOutputter doutputter = new DOMOutputter();
						myHitlist = doutputter.output(myJdomDocMultivolumeband);

						/* nevertheless, do not take the document, but the first
						 * child */

						myHitlist = myHitlist.getFirstChild();
					}
				}
			}

			// if the hit is a contained work, then superordinated work

			if (type.isContainedWork()) {
				// determine the PPN of the superordinated work
				String ueberGeordnetePpn = getPPNFromParent(myFirstHit, "021A", "9");
				if (!ueberGeordnetePpn.equals("")) {
					// take anthology out of the OPAC
					myQuery = new Query(ueberGeordnetePpn, "12");
					// if a hit of the parent was found in the OPAC
					if (client.getNumberOfHits(myQuery) == 1) {
						Node myParentHitlist = client.retrievePicaNode(myQuery, 1);
						// call OPAC beautifier
						myParentHitlist = catalogue.executeBeautifier(myParentHitlist);
						// conversion to JDOM elements
						Document myJdomDocParent = new DOMBuilder().build(myParentHitlist.getOwnerDocument());
						Element myFirstHitParent = myJdomDocParent.getRootElement().getChild("record");

						/* take over all elements of the parent that are not
						 * yet available by themselves */

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

			// create RDF file from the OPAC result

			// access to Ugh classes
			PicaPlus pp = new PicaPlus(preferences);
			pp.read(myHitlist);
			DigitalDocument dd = pp.getDigitalDocument();
			ff = new XStream(preferences);
			ff.setDigitalDocument(dd);
			// add BoundBook
			DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
			DocStruct dsBoundBook = dd.createDocStruct(dst);
			dd.setPhysicalDocStruct(dsBoundBook);
			// check and complement content of the RDF file
			checkMyOpacResult(ff.getDigitalDocument(), preferences, myFirstHit, type, typeID);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return createResult(type.getTitle(), myFirstHit, ff);
	}

	/**
	 * Determines the document type (genre).
	 */
	@SuppressWarnings("unchecked")
	private static String getTypeID(Element inHit) {
		if (inHit == null) {
			return "";
		}
		for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
			Element tempElement = iter.next();
			String feldname = tempElement.getAttributeValue("tag");
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
	 * Determines the PPN of the superodinated volume. (MultiVolume: 036D-9 and ContainedWork:
	 * 021A-9)
	 */
	@SuppressWarnings("unchecked")
	private static String getPPNFromParent(Element inHit, String inFeldName, String inSubElement) {
		for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
			Element tempElement = iter.next();
			String feldname = tempElement.getAttributeValue("tag");
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

			/* if it is the desired field, then return the value with the
			 * appropriate attribute */

			if (feldname.equals(inTagName)) {
				return myElement;
			}
		}
		return null;
	}

	/**
	 * Recursively copies elements. This method exists because inserting an
	 * element onto an other node fails with an error stating that the element
	 * to add already has a parent.
	 */
	@SuppressWarnings("unchecked")
	private static Element getCopyFromJdomElement(Element inHit) {
		Element myElement = new Element(inHit.getName());
		myElement.setText(inHit.getText());
		// now take over all attributes, too
		if (inHit.getAttributes() != null) {
			for (Iterator<Attribute> iter = inHit.getAttributes().iterator(); iter.hasNext();) {
				Attribute att = iter.next();
				myElement.getAttributes().add(new Attribute(att.getName(), att.getValue()));
			}
		}
		// now take over all children, too
		if (inHit.getChildren() != null) {

			for (Iterator<Element> iter = inHit.getChildren().iterator(); iter.hasNext();) {
				Element ele = iter.next();
				myElement.addContent(getCopyFromJdomElement(ele));
			}
		}
		return myElement;
	}

	/**
	 * Complements the DigitalDocument by additional OPAC details.
	 */
	private static void checkMyOpacResult(DigitalDocument inDigDoc, Prefs inPrefs, Element myFirstHit,
			Type type, String gattung) {
		DocStruct topstruct = inDigDoc.getLogicalDocStruct();
		DocStruct boundbook = inDigDoc.getPhysicalDocStruct();
		DocStruct topstructChild = null;
		Element mySecondHit = null;

		// at multivolumes, still determine the child in XML and DocStruct

		if (type.isMultiVolume()) {
			try {
				topstructChild = topstruct.getAllChildren().get(0);
			} catch (RuntimeException e) {
			}
			mySecondHit = (Element) myFirstHit.getParentElement().getChildren().get(1);
		}

		// insert available PPN as digital or analogous

		String ppn = getElementFieldValue(myFirstHit, "003@", "0");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", "");
		if (gattung.toLowerCase().startsWith("o")) {
			UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDDigital", ppn);
		} else {
			UGHUtils.replaceMetadatum(topstruct, inPrefs, "CatalogIDSource", ppn);
		}

		// if it is a multivolume, then check the PPN, too

		if ((topstructChild != null) && (mySecondHit != null)) {
			String secondHitppn = getElementFieldValue(mySecondHit, "003@", "0");
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", "");
			if (gattung.toLowerCase().startsWith("o")) {
				UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDDigital", secondHitppn);
			} else {
				UGHUtils.replaceMetadatum(topstructChild, inPrefs, "CatalogIDSource", secondHitppn);
			}
		}

		// clean up the main title

		String myTitle = getElementFieldValue(myFirstHit, "021A", "a");

		/* if the full title was not written in the element, then have a look
		 * elsewhere (especially at contained work) */

		if ((myTitle == null) || (myTitle.length() == 0)) {
			myTitle = getElementFieldValue(myFirstHit, "021B", "a");
		}
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMain", myTitle.replaceAll("@", ""));

		// sort title with conversion of diacritics

		if (myTitle.indexOf("@") != -1) {
			myTitle = myTitle.substring(myTitle.indexOf("@") + 1);
		}
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "TitleDocMainShort", myTitle);

		// clean up the main title at multivolumes

		if ((topstructChild != null) && (mySecondHit != null)) {
			String fulltitleMulti = getElementFieldValue(mySecondHit, "021A", "a").replaceAll("@", "");
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMain", fulltitleMulti);
		}

		// at multivolumes, the sort title with conversion of diacritics

		if ((topstructChild != null) && (mySecondHit != null)) {
			String sortTitleMulti = getElementFieldValue(mySecondHit, "021A", "a");
			if (sortTitleMulti.indexOf("@") != -1) {
				sortTitleMulti = sortTitleMulti.substring(sortTitleMulti.indexOf("@") + 1);
			}
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "TitleDocMainShort", sortTitleMulti);
		}

		// languages - conversion to two places

		Iterable<String> languages = getElementFieldValues(myFirstHit, "010@", "a");
		languages = UGHUtils.convertLanguages(languages);
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "DocLanguage", languages);

		// at multivolumes, the languages - conversion to two places

		if ((topstructChild != null) && (mySecondHit != null)) {
			Iterable<String> languagesMulti = getElementFieldValues(mySecondHit, "010@", "a");
			languagesMulti = UGHUtils.convertLanguages(languagesMulti);
			UGHUtils.replaceMetadatum(topstructChild, inPrefs, "DocLanguage", languagesMulti);
		}

		// ISSN

		String issn = getElementFieldValue(myFirstHit, "005A", "0");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "ISSN", issn);

		// Copyright

		String copyright = getElementFieldValue(myFirstHit, "037I", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "copyrightimageset", copyright);

		// Format

		String format = getElementFieldValue(myFirstHit, "034I", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "FormatSourcePrint", format);

		// Extent

		String extent = getElementFieldValue(myFirstHit, "034D", "a");
		UGHUtils.replaceMetadatum(topstruct, inPrefs, "SizeSourcePrint", extent);

		// Shelf mark

		String shm = getElementFieldValue(myFirstHit, "209A", "c");
		if (shm.length() > 0) {
			shm = "<" + shm + ">";
		}
		shm += getElementFieldValue(myFirstHit, "209A", "f") + " ";
		shm += getElementFieldValue(myFirstHit, "209A", "a");
		UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", shm.trim());
		if (shm.trim().length() == 0) {
			shm = getElementFieldValue(myFirstHit, "209A/01", "c");
			if (shm.length() > 0) {
				shm = "<" + shm + ">";
			}
			shm += getElementFieldValue(myFirstHit, "209A/01", "f") + " ";
			shm += getElementFieldValue(myFirstHit, "209A/01", "a");
			if (mySecondHit != null) {
				shm += getElementFieldValue(mySecondHit, "209A", "f") + " ";
				shm += getElementFieldValue(mySecondHit, "209A", "a");
			}
			UGHUtils.replaceMetadatum(boundbook, inPrefs, "shelfmarksource", shm.trim());
		}

		// In case of magazines, insert another PeriodicalVolume as child

		if (type.isPeriodical() && (topstruct.getAllChildren() == null)) {
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

	/**
	 * The function getElementFieldValue() returns the values of the last
	 * grandchild element of the first child element from a given JDOM Element
	 * whose “code” attribute equals the given attribute name and whose parents’
	 * “tag” attribute equals the given field name—or an empty String if there
	 * is no such grandchild element.
	 *
	 * @param myFirstHit
	 *            JDOM Element whose descendant elements are to be examined
	 * @param inFieldName
	 *            tag attribute value to compare to
	 * @param inAttributeName
	 *            code attribute value to compare to
	 * @return values of the last grandchild of the first child whose code
	 *         attribute equals and whose parents’ tag attribute equals, too
	 */
	@SuppressWarnings("unchecked")
	private static String getElementFieldValue(Element myFirstHit, String inFieldName, String inAttributeName) {

		for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
			Element myElement = iter2.next();
			String feldname = myElement.getAttributeValue("tag");

			/* if it is the desired field, then return the value with the
			 * appropriate attribute */

			if (feldname.equals(inFieldName)) {
				return getFieldValue(myElement, inAttributeName);
			}
		}
		return "";
	}

	/**
	 * The function getElementFieldValues() returns the values of all grandchild
	 * elements from a given JDOM Element whose “code” attribute equals the
	 * given attribute name and whose parents’ “tag” attribute equals the given
	 * field name—or an empty Collection if there is no such grandchild element.
	 *
	 * @param myFirstHit
	 *            JDOM Element whose descendant elements are to be examined
	 * @param inFieldName
	 *            tag attribute value to compare to
	 * @param inAttributeName
	 *            code attribute value to compare to
	 * @return values of all grandchild elements whose code attribute equals and
	 *         whose parents’ tag attribute equals, too
	 */
	@SuppressWarnings("unchecked")
	private static Iterable<String> getElementFieldValues(Element myFirstHit, String inFieldName, String inAttributeName) {
		LinkedList<String> result = new LinkedList<>();
		for (Iterator<Element> iter2 = myFirstHit.getChildren().iterator(); iter2.hasNext();) {
			Element myElement = iter2.next();
			String feldname = myElement.getAttributeValue("tag");

			/* if it is the desired field, then return the value with the
			 * appropriate attribute */

			if (feldname.equals(inFieldName)) {
				result.addAll(getFieldValues(myElement, inAttributeName));
			}
		}
		return result;
	}

	/**
	 * The function getFieldValue() returns the value of the last elements from
	 * a given JDOM Element whose “code” attribute equals the given attribute
	 * value or the empty String if there is no such child element.
	 *
	 * @param inElement
	 *            JDOM Element whose child elements are to be examined
	 * @param attributeValue
	 *            code attribute value to compare to
	 * @return values of all child elements whose code attribute equals the
	 *         attribute value
	 */
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

	/**
	 * The function getFieldValues() returns the values of all child elements
	 * from a given JDOM Element whose “code” attribute equals the given
	 * attribute value or an empty Collection if there is no such child element.
	 *
	 * @param inElement
	 *            JDOM Element whose child elements are to be examined
	 * @param attributeValue
	 *            code attribute value to compare to
	 * @return values of all child elements whose code attribute equals the
	 *         attribute value
	 */
	@SuppressWarnings("unchecked")
	private static Collection<String> getFieldValues(Element inElement, String attributeValue) {
		List<String> rueckgabe = new LinkedList<>();

		for (Iterator<Element> iter = inElement.getChildren().iterator(); iter.hasNext();) {
			Element subElement = iter.next();
			if (subElement.getAttributeValue("code").equals(attributeValue)) {
				rueckgabe.add(subElement.getValue());
			}
		}
		return rueckgabe;
	}

	/**
	 * The function createResult() creates a Map&lt;String, Object&gt; as result
	 * of getHit(). The map contains the full hit as "fileformat", the docType
	 * as "type" and some bibliographic metadata for Production to be able to
	 * show a short hit display as supposed in
	 * {@link org.goobi.production.plugin.CataloguePlugin.Hit}
	 *
	 * @param docType
	 *            the DocType of the hit
	 * @param hit
	 *            the hit data as JDom Element
	 * @param fileformat
	 *            the hit data as Fileformat
	 * @return a Map with the hit
	 */
	private static Map<String, Object> createResult(String docType, Element hit, Fileformat fileformat) {
		final LocalTime DAY_END = new LocalTime(23, 59, 59, 999);

		Map<String, Object> result = new HashMap<>(20);
		LocalDate today = new LocalDate();

		result.put("fileformat", fileformat);
		result.put("type", docType);

		// add some basic metadata
		String accessed = getElementFieldValue(hit, "208@", "a");
		try {
			LocalDate date = toRecentLocalDate(accessed, today);
			result.put("accessed", date.toDateTime(date.isEqual(today) ? new LocalTime() : DAY_END).toString());
		} catch (RuntimeException r) {
		}

		String lastName = getElementFieldValue(hit, "028A", "a");
		if (lastName.equals("")) {
			lastName = getElementFieldValue(hit, "028A", "l");
		}
		String firstName = getElementFieldValue(hit, "028A", "d");
		if (firstName.equals("")) {
			firstName = getElementFieldValue(hit, "028A", "P");
		}
		String middleTitle = getElementFieldValue(hit, "028A", "c");
		String author = lastName + (!firstName.equals("") ? ", " : "") + firstName
				+ (!middleTitle.equals("") ? " " : "") + middleTitle;
		if (author.equals("")) {
			author = getElementFieldValue(hit, "028A", "8");
		}
		if (author.equals("")) {
			String lastName2 = getElementFieldValue(hit, "028C", "a");
			if (lastName2.equals("")) {
				lastName2 = getElementFieldValue(hit, "028C", "l");
			}
			String firstName2 = getElementFieldValue(hit, "028C", "d");
			if (firstName2.equals("")) {
				firstName2 = getElementFieldValue(hit, "028C", "P");
			}
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
		result.put("format", docType.startsWith("O") ? "internet" : "monograph");
		result.put("number", getElementFieldValue(hit, "036E", "l"));
		result.put("place", getElementFieldValue(hit, "033A", "p"));
		result.put("publisher", getElementFieldValue(hit, "033A", "n"));
		result.put("series", getElementFieldValue(hit, "036E", "a"));

		String subseries = getElementFieldValue(hit, "021A", "d");
		if ((subseries == null) || (subseries.length() == 0)) {
			subseries = getElementFieldValue(hit, "021B", "d");
		}
		if ((subseries == null) || (subseries.length() == 0)) {
			subseries = getElementFieldValue(hit, "027D", "d");
		}
		result.put("subseries", subseries);

		String title = getElementFieldValue(hit, "021A", "a");
		if ((title == null) || (title.length() == 0)) {
			title = getElementFieldValue(hit, "021B", "a");
		}
		if ((title == null) || (title.length() == 0)) {
			title = getElementFieldValue(hit, "027D", "a");
		}
		String titleLong = getElementFieldValue(hit, "021A", "d");
		if ((titleLong != null) && (titleLong.length() > 0)) {
			title = title + " : " + titleLong;
		}
		result.put("title", title.replaceAll("@", ""));

		result.put("url", getElementFieldValue(hit, "209R", "a"));
		result.put("year", getElementFieldValue(hit, "011@", "a"));

		return result;
	}

	/**
	 * The function toRecentLocalDate() interprets a String of scheme "dd-mm-yy"
	 * as a LocalDate within the last 100 years up to a given reference date.
	 *
	 * @param dd_mm_yy
	 *            a date String to interpret
	 * @param upTo
	 *            a reference date
	 * @return the date value as LocalDate
	 */
	private static LocalDate toRecentLocalDate(String dd_mm_yy, LocalDate upTo) {
		int centuryPrefix = upTo.getYear() / 100;
		String[] fields = dd_mm_yy.split("-");
		int year = (100 * centuryPrefix) + Integer.parseInt(fields[2]);
		if (year > upTo.getYear()) {
			year -= 100;
		}
		return new LocalDate(year, Integer.parseInt(fields[1]), Integer.parseInt(fields[0]));
	}

	/**
	 * Returns the number of hits from a given search result.
	 * <p>
	 * Confer to {@code CataloguePlugin.getNumberOfHits(Object, long)} in
	 * package {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 * 
	 * @param searchResult
	 *            the reference to the search whose number of hits shall be
	 *            looked up
	 * @param timeout
	 *            ignored because there is no network acceess in this step
	 * @return the number of hits
	 */
	public static long getNumberOfHits(Object searchResult, long timeout) {
		if (searchResult instanceof FindResult) {
			return ((FindResult) searchResult).getHits();
		} else {
			throw new ClassCastException("Search result of class "+searchResult.getClass().getName()+" not supported.");
		}
	}

	/**
	 * The function getTempDir() provides a reference to the file system
	 * directory where temporary files are written in.
	 *
	 * @return the file system directory where to write temporary files
	 */
	static String getTempDir() {
		return tempDir;
	}

	/**
	 * Returns a human-readable name for the plug-in in English. The parameter
	 * language is ignored.
	 * <p>
	 * Confer to {@code UnspecificPlugin.getTitle(Locale)} in package
	 * {@code org.goobi.production.plugin} of the core application, too.
	 *
	 * @param language
	 *            desired language of the human-readable name (support is
	 *            optional)
	 * @return a human-readable name for the plug-in
	 */
	public static String getTitle(Locale language) {
		return "PICA Catalogue Plugin";
	}

	/**
	 * Sets the Ugh preferences to be used to create the main result.
	 * <p>
	 * Confer to {@code CataloguePlugin.setPreferences(Prefs)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 * 
	 * @param preferences
	 *            Ugh preferences to use
	 */
	public void setPreferences(Prefs preferences) {
		this.preferences = preferences;
	}

	/**
	 * Returns whether this plug-in is able to access the catalogue identified
	 * by the given String. This depends on the configuration.
	 * <p>
	 * Confer to {@code CataloguePlugin#supportsCatalogue(String)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @param catalogue
	 *            a String identifying the catalogue
	 * @return whether the plug-in is able to access that catalogue
	 */
	public static boolean supportsCatalogue(String catalogue) {
		return OpacCatalogues.getCatalogueByName(catalogue) != null;
	}

	/**
	 * Returns the names of all catalogues supported by this plug-in. This
	 * depends on the plug-in’s configuration.
	 * <p>
	 * Confer to {@code CataloguePlugin#getSupportedCatalogues()} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @return list of catalogue names
	 */
	public static List<String> getSupportedCatalogues() {
		return OpacCatalogues.getAllCatalogues();
	}

	/**
	 * Returns the names of all docTypes configured for this plug-in. This
	 * depends on the plug-in’s configuration.
	 * <p>
	 * Confer to {@code CataloguePlugin.getAllConfigDocTypes()} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @return list of ConfigOapcDocTypes
	 */
	public static List<String> getAllConfigDocTypes() {
		List<String> result = new ArrayList<>();
		for (Type type : OpacCatalogues.getDoctypes()) {
			result.add(type.getTitle());
		}
		return result;
	}

	/**
	 * Sets a catalogue to be used.
	 * <p>
	 * Confer to {@code CataloguePlugin.useCatalogue(String)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @param catalogueID
	 *            a String identifying the catalogue
	 * @throws ParserConfigurationException
	 *             if a DocumentBuilder cannot be created which satisfies the
	 *             configuration requested
	 */
	public void useCatalogue(String catalogueID) throws ParserConfigurationException {
		catalogue = OpacCatalogues.getCatalogueByName(catalogueID);
		client = new CatalogueClient(catalogue);
	}

	/**
	 * Returns the search fields for the given catalogue. The search fields are
	 * read from the configuration file of this plug-in, for the catalogue with
	 * the given String {@code catalogueName}. They are returned in a HashMap.
	 * The map contains the labels of the search fields as keys and the
	 * corresponding URL parameters as values.
	 * <p>
	 * Confer to {@code CataloguePlugin.getSearchFields(String)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @param catalogueName
	 *            the name of the catalogue for which the list of search fields
	 *            will be returned
	 * @return Map containing the search fields of the selected OPAC
	 */
	public HashMap<String, String> getSearchFields(String catalogueName) {
		LinkedHashMap<String, String> searchFields = new LinkedHashMap<>();
		if(!Objects.equals(OpacCatalogues.getConfig(), null)) {
			for (Object catalogueObject : OpacCatalogues.getConfig().configurationsAt("catalogue")) {
				SubnodeConfiguration catalogue = (SubnodeConfiguration)catalogueObject;
				for (Object titleAttrObject : catalogue.getRootNode().getAttributes("title")) {
					ConfigurationNode titleAttr = (ConfigurationNode)titleAttrObject;
					String currentOpacName = (String)titleAttr.getValue();
					if (Objects.equals(catalogueName, currentOpacName)) {
						for (Object fieldObject : catalogue.configurationsAt("searchFields.searchField")) {
							SubnodeConfiguration searchField = (SubnodeConfiguration)fieldObject;
							searchFields.put(searchField.getString("[@label]"), searchField.getString("[@value]"));
						}
					}
				}
			}
		}
		return searchFields;
	}

	/**
	 * Returns the institutions the result can be filtered by for the given
	 * catalogue. The institutions can be configured in the configuration file
	 * of this plug-in. Returns a map that contains the labels of the
	 * institutions as keys and the IDs used to for filtering as values. In PICA
	 * catalogues, ISIL IDs are used for that purpose.
	 * <p>
	 * Confer to {@code CataloguePlugin.getInstitutions(String)} in package
	 * {@code org.goobi.production.plugin.CataloguePlugin} of the core
	 * application, too.
	 *
	 * @param catalogueName
	 *            the name of the catalogue for which the list of search fields
	 *            will be returned
	 * @return Map containing the institutions for which the selected catalogue
	 *         supports filtering
	 */
	public HashMap<String, String> getInstitutions(String catalogueName) {
		LinkedHashMap<String, String> institutions = new LinkedHashMap<>();
		if(!Objects.equals(OpacCatalogues.getConfig(), null)) {
			for (Object catalogueObject : OpacCatalogues.getConfig().configurationsAt("catalogue")) {
				SubnodeConfiguration catalogue = (SubnodeConfiguration)catalogueObject;
				for (Object titleAttrObject : catalogue.getRootNode().getAttributes("title")) {
					ConfigurationNode titleAttr = (ConfigurationNode)titleAttrObject;
					String currentOpacName = (String)titleAttr.getValue();
					if (Objects.equals(catalogueName, currentOpacName)) {
						for (Object fieldObject : catalogue.configurationsAt("filterInstitutions.institution")) {
							SubnodeConfiguration institution = (SubnodeConfiguration)fieldObject;
							institutions.put(institution.getString("[@label]"), institution.getString("[@value]"));
						}
					}
				}
			}
		}
		return institutions;
	}

	/**
	 * Returns the URL parameter used for institution filtering in this plug-in.
	 * <p>
	 * This function is not yet used in the PicaPlugin.
	 * <p>
	 * Confer to {@code CataloguePlugin.getInstitutionFilterParameter(String)}
	 * in package {@code org.goobi.production.plugin.CataloguePlugin} of the
	 * core application, too.
	 *
	 * @param catalogueName
	 *            the name of the catalogue for which the institution filter
	 *            parameter is returned
	 * @return String the URL parameter used for institution filtering
	 */
	public String getInstitutionFilterParameter(String catalogueName) {
		return "";
	}
}

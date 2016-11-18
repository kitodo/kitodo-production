/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;

import net.sf.saxon.TransformerFactoryImpl;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.XStream;

/**
 * The class ModsPlugin is the main class of the Goobi Mods catalogue plugin
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
 * @author Arved Solth, Christopher Timm
 */
@PluginImplementation
public class ModsPlugin implements Plugin {

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
	 * The field xsltDir holds a reference to the file system directory where
	 * XSLT scripts used by plugins to perform transformations between Kitodo
	 * documents and external XML documents are located.
	 */
	private static String xsltDir;

	/**
	 * The constant OPAC_CONFIGURATION_FILE holds the name of the MODS plug-in
	 * main configuration file. Required. The file must be located in
	 * {@link #configDir}.
	 */
	static final String OPAC_CONFIGURATION_FILE = "kitodo_mods_opac.xml";

	/**
	 * The constant OPAC_CONFIGURATION_FILE holds the name of the PICA plug-in
	 * languages mapping file. This is a text file with lines in form
	 * replacement—space—stringToReplace used to replace the value from PICA+
	 * field “010@” subfield “a” (the replacement will be saved in DocStruct
	 * “DocLanguage”) The file is optional. To use this functionality, the file
	 * must be located in {@link #configDir}.
	 */
	static final String LANGUAGES_MAPPING_FILE = "goobi_opacLanguages.txt";

	/**
	 * MODS string.
	 */
	static final String MODS_STRING = "MODS";

	/**
	 * The field preferences holds the UGH preferences.
	 */
	private Prefs preferences;

	/**
	 * The field configuration holds the catalogue configuration.
	 */
	private ConfigOpacCatalogue configuration;

	public ConfigOpacCatalogue getConfiguration() {
		return configuration;
	}

	public XMLConfiguration getXMLConfiguration() {
		return ConfigOpac.getConfig();
	}

	/**
	 * The field catalogue holds the catalogue.
	 */
	private Catalogue catalogue;

	/**
	 * The field client holds the catalogue client used to access the catalogue.
	 */
	private GetOpac client;

	/**
	 * Namespace and tag name constants used to create METS documents encapsulating MODS documents
	 */
	private static final Namespace METS_NAMESPACE = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
	private static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
	private static final Namespace XSI_NAMESPACE = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	private static final Namespace GOOBI_NAMESPACE = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");

	private static final String METS_DMD_ID = "DMDID";
	private static final String METS_ID = "ID";
	private static final String METS_LOGICAL = "LOGICAL";
	private static final String METS_TYPE = "TYPE";
	private static final String METS_MDTYPE = "MDTYPE";
	private static final String METS_DIV_ID_VALUE = "LOG";
	private static final String METS_DMD_ID_VALUE = "DMDLOG";

	/**
	 * Hashmaps with structureTypes (String) as keys and lists of XPath instances values. The lists of XPaths instances describe
	 * the elements a MODS document must or must not contain in order to be classified as a specific structureType
	 * (e.g. the corresponding key in the Hashmap)
	 */
	private static HashMap<String, List<XPath>> structureTypeMandatoryElements = new HashMap<String, List<XPath>>();
	private static HashMap<String, List<XPath>> structureTypeForbiddenElements  = new HashMap<String, List<XPath>>();

	/**
	 * Filename of the XSL transformation file.
	 */
	private static final String MODS2GOOBI_TRANSFORMATION_RULES_FILENAME = "mods2kitodo.xsl";

	/**
	 * Path of the output file for the XSL transformation.
	 */
	private static final String TEMP_FILENAME = "tempMETSMODS.xml";

	/**
	 * Static XPath variables used to parse MetsModsGoobi documents.
	 */
	private static XPath srwRecordXPath = null;
	private static XPath modsPath = null;
	private static XPath authorPath = null;
	private static XPath titlePath = null;
	private static XPath urlPath = null;
	private static XPath datePath = null;
	private static XPath catalogueIDPath = null;
	private static XPath placePath = null;
	private static XPath shelfmarksourcePath = null;
	private static XPath parentIDPath = null;

	/**
	 * Static counter variables for constructing METS DmdSections for multiple imported MODS documents.
	 */
	private static int dmdSecCounter = 0;
	private static int dmdIdCounter = 1;
	private static int divIdCounter = 1;

	/**
	 * The method configure() accepts a Map with configuration parameters. Two
	 * entries, "configDir" and "tempDir", are expected.
	 *
	 * configDir must point to a directory on the local file system where the
	 * plug-in can read individual configuration files from. The configuration
	 * file of this plugin is expected in that directory.
	 *
	 * @param configuration
	 *            a Map with configuration parameters
	 * @see org.goobi.production.plugin.UnspecificPlugin#configure(Map)
	 */
	public void configure(Map<String, String> configuration) {
		configDir = configuration.get("configDir");
		tempDir = configuration.get("tempDir");
		xsltDir = configuration.get("xsltDir");
	}

	/**
	 * The function find() initially queries the library catalogue with the
	 * given query. If successful, it returns a FindResult with the number of
	 * hits.
	 *
	 * @param query
	 *            a query String. See
	 *            {@link org.goobi.production.plugin.CataloguePlugin.QueryBuilder}
	 *            for the semantics of the query.
	 * @param timeout
	 *            timeout in milliseconds after which the operation shall return
	 * @return a FindResult that may be used for future operations on the query
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#find(String,
	 *      long)
	 */
	public Object find(String query, long timeout) {
		try {
			Query queryObject = new Query(query);
			int hits = client.getNumberOfHits(queryObject, timeout);
			if (hits > 0) {
				return new FindResult(queryObject, hits);
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			modsLogger.error("Error while querying library catalogue: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			modsLogger.error("Error while querying library catalogue: " + e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Initializes static XPath variables used for parsing a MetsModsGoobi document.
	 */
	private void initializeXPath() {
		try {
			srwRecordXPath = XPath.newInstance("//srw:searchRetrieveResponse/srw:records/srw:record");
			modsPath = XPath.newInstance("//mods:mods");

			// the catalog ID, parent ID and URL are extracted from the original, untransformed document; therefore we need the original MODS XPaths here
			catalogueIDPath = XPath.newInstance("//mods:mods/mods:recordInfo/mods:recordIdentifier");
			parentIDPath = XPath.newInstance("//mods:mods/mods:relatedItem/mods:identifier[@type='localparentid']");
			urlPath = XPath.newInstance("//mods:mods/mods:location/mods:url");

			// all other fields are extracted from the transformed document; therefore we can use the "goobi" XPaths here
			authorPath = XPath.newInstance("//goobi:displayName");
			titlePath = XPath.newInstance("//goobi:metadata[@name='TitleDocMain']");
			datePath = XPath.newInstance("//goobi:metadata[@name='PublicationDate']");
			placePath = XPath.newInstance("//goobi:metadata[@name='PlaceOfPublication']");
			shelfmarksourcePath = XPath.newInstance("//goobi:metadata[@name='shelfmarksource']");
		} catch (JDOMException e) {
			modsLogger.error("Error while initializing XPath variables: " + e.getMessage());
		}
	}

	/**
	 * Checks and returns whether all static XPath variables used to parse MetsModsGoobi documents have been initialized.
	 *
	 * @return whether all static XPath variables have been initialized or not
	 */
	private boolean xpathsDefined() {
		return (
				!Objects.equals(srwRecordXPath, null) &&
				!Objects.equals(modsPath, null) &&
				!Objects.equals(authorPath, null) &&
				!Objects.equals(titlePath, null) &&
				!Objects.equals(urlPath, null) &&
				!Objects.equals(datePath, null) &&
				!Objects.equals(catalogueIDPath, null) &&
				!Objects.equals(placePath, null) &&
				!Objects.equals(shelfmarksourcePath, null) &&
				!Objects.equals(parentIDPath, null)
		);
	}

	/**
	 * The function getDescription() returns a human-readable description of the
	 * plug-in’s functionality in English. The parameter language is ignored.
	 *
	 * @param language
	 *            desired language of the human-readable description (support is
	 *            optional)
	 * @return a human-readable description of the plug-in’s functionality
	 * @see org.goobi.production.plugin.UnspecificPlugin#getDescription(Locale)
	 */
	public static String getDescription(Locale language) {
		return "The MODS plugin can be used to access MODS library catalogue systems.";
	}

	/**
	 * The function getHit() returns the hit with the given index from the given
	 * search result as a Map&lt;String, Object&gt;. The map contains the full
	 * hit as "fileformat", the docType as "type" and some bibliographic
	 * metadata for Production to be able to show a short hit display as
	 * supposed in {@link org.goobi.production.plugin.CataloguePlugin.Hit}.
	 *
	 * @param searchResult
	 *            a FindResult created by {@link #find(String, long)}
	 * @param index
	 *            the zero-based index of the hit
	 * @param timeout
	 *            a timeout in milliseconds after which the operation shall
	 *            return
	 * @return a Map with the hit
	 * @throws IOException
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getHit(Object,
	 *      long, long)
	 */
	public Map<String, Object> getHit(Object searchResult, long index, long timeout) throws IOException {

		dmdIdCounter = 1;
		divIdCounter = 1;

		String xsltFilepath = xsltDir + MODS2GOOBI_TRANSFORMATION_RULES_FILENAME;

		Map<String, Object> result = new HashMap<String, Object>();

		Query myQuery = ((FindResult) searchResult).getQuery();

		String resultXML = client.retrieveModsRecord(myQuery.getQueryUrl(), timeout);

		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());

		initializeXPath();

		Fileformat ff;

		Path xsltDirPath = FileSystems.getDefault().getPath(xsltDir);

		if (resultXML == null ) {
			String message = "Error: result empty!";
			modsLogger.error(message);
			throw new IllegalStateException(message);
		}

		else if (!xpathsDefined()) {
			String message = "Error: XPath variables not defined!";
			modsLogger.error(message);
			throw new IllegalStateException(message);
		}

		else if (!Files.isDirectory(xsltDirPath)) {
			String message = "Error: XSLT directory not found!";
			modsLogger.error(message);
			throw new IOException(message);
		}

		else{
			SAXBuilder sb = new SAXBuilder();
			try {
				Document doc = sb.build(new StringReader(resultXML));

				@SuppressWarnings("unchecked")
				ArrayList<Element> recordNodes = (ArrayList<Element>)srwRecordXPath.selectNodes(doc);

				for (int i = 0; i < recordNodes.size(); i++) {
					Element currentRecord = recordNodes.get(i);
					if (i != (int)index) {
						currentRecord.detach();
					}
				}

				LinkedList<Element> dmdSections = new LinkedList<Element>();
				LinkedList<String> docTypes = new LinkedList<String>();

				String parentXML = retrieveParentRecord(doc, timeout);

				docTypes.add(getStructureType((Element)modsPath.selectSingleNode(doc)));

				File transformationScript = new File(xsltFilepath);

				doc = transformXML(doc, transformationScript, sb);

				Element nameElement = (Element)authorPath.selectSingleNode(doc);
				Element titleElement = (Element)titlePath.selectSingleNode(doc);
				Element urlElement = (Element)urlPath.selectSingleNode(doc);
				Element dateElement = (Element)datePath.selectSingleNode(doc);
				Element placeElement = (Element)placePath.selectSingleNode(doc);
				Element shelfmarkSourceElement = (Element)shelfmarksourcePath.selectSingleNode(doc);

				if (nameElement != null) {
					result.put("creator", nameElement.getText());
				}
				if (titleElement != null) {
					result.put("title", titleElement.getText());
				}
				if (shelfmarkSourceElement != null) {
					result.put("shelfmarksource", shelfmarkSourceElement.getText());
				}
				if (placeElement != null) {
					result.put("place", placeElement.getText());
				}
				if (urlElement != null) {
					result.put("url", urlElement.getText());
				}
				if (dateElement != null) {
					result.put("date", dateElement.getText());
				}

				// XML MODS data of document itself
				Element modsElement = (Element)modsPath.selectSingleNode(doc);

				dmdSections.add(createMETSDescriptiveMetadata((Element)modsElement.clone()));

				while (!Objects.equals(parentXML, null)) {
					resultXML = parentXML;
					doc = sb.build(new StringReader(resultXML));
					parentXML = retrieveParentRecord(doc, timeout);
					// docType is determined using relatedItem ID; this field is not available after transformation anymore, therefore doctype has to be determined before 'transformXML' is called!
					docTypes.add(getStructureType((Element)modsPath.selectSingleNode(doc)));

					doc = transformXML(doc, transformationScript, sb);
					// 'doc' can become "null", when the last doc had a 'parentID', but trying to retrieve the element with this parentID yields an empty SRW container (e.g. not containing any MODS documents)
					// => break loop!
					if (Objects.equals(doc, null)) {
						modsLogger.info("Break: Transformed document is 'null'!");
						break;
					}
					// if 'doc' is null after the XSL transformation (e.g. just an empty XML header), 'selectSingleNode' can't be called on it anymore! Therefore the loop has to be terminated before reaching this point!
					modsElement = (Element)modsPath.selectSingleNode(doc);
					dmdSections.add(createMETSDescriptiveMetadata((Element)modsElement.clone()));
				}

				doc = createMetsContainer(dmdSections, docTypes);
				// reviewing the constructed XML mets document can be done via "xmlOutputter.output(doc.getRootElement(), System.out);"

				/* MetsModsKalliopeImport is subclass of MetsModsImportExport UGH class */
				MetsModsKalliopeImport mm = new MetsModsKalliopeImport(preferences);
				xmlOutputter.output(doc, new FileWriter(TEMP_FILENAME));

				mm.read(TEMP_FILENAME);
				// reviewing the constructed DigitalDocument can be done via "System.out.println(mm.getDigitalDocument());"

				deleteFile(TEMP_FILENAME);
				DigitalDocument dd = mm.getDigitalDocument();
				ff = new XStream(preferences);
				ff.setDigitalDocument(dd);

				DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
				DocStruct dsBoundBook = dd.createDocStruct(dst);
				dd.setPhysicalDocStruct(dsBoundBook);

				if (!Objects.equals(result.get("shelfmarksource"), null)) {
					UGHUtils.replaceMetadatum(dd.getPhysicalDocStruct(), preferences, "shelfmarksource", (String)result.get("shelfmarksource"));
				}

				// TODO: add all children - using query with parameter 'relatedItemID', once it becomes available - of retrieved document to docStruct!

				result.put("fileformat", ff);
				result.put("type", docTypes.getFirst());

			} catch (JDOMException | TypeNotAllowedForParentException | PreferencesException | ReadException | IOException e) {
				modsLogger.error("Error while retrieving document: " + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * This function loads the mandatory and forbidden elements of individual structureElement types
	 * from the plugin configuration file, used for structureType classification in the getStructureType function.
	 *
	 * @throws JDOMException
	 */
	private void initializeStructureElementTypeConditions() throws JDOMException {

		if (structureTypeMandatoryElements.keySet().size() < 1 && structureTypeForbiddenElements.keySet().size() < 1) {

			XMLConfiguration pluginConfiguration = ConfigOpac.getConfig();

			for (Object structureTypeObject : pluginConfiguration.configurationsAt("structuretypes.type")) {
				SubnodeConfiguration structureType = (SubnodeConfiguration)structureTypeObject;

				String structureTypeName = "";

				ConfigurationNode structureNode = structureType.getRootNode();
				for(Object rulesetObject : structureNode.getAttributes("rulesetType") ) {
					ConfigurationNode rulesetNode = (ConfigurationNode)rulesetObject;
					structureTypeName = (String)rulesetNode.getValue();
					break;
				}

				if (!structureTypeMandatoryElements.containsKey(structureTypeName)) {
					structureTypeMandatoryElements.put(structureTypeName, new LinkedList<XPath>());
				}
				for(Object mandatoryElement : structureType.getList("mandatoryElement")) {
					String mustHave = (String)mandatoryElement;
					structureTypeMandatoryElements.get(structureTypeName).add(XPath.newInstance(mustHave));
				}

				if (!structureTypeForbiddenElements.containsKey(structureTypeName)) {
					structureTypeForbiddenElements.put(structureTypeName, new LinkedList<XPath>());
				}
				for(Object forbiddenElement : structureType.getList("forbiddenElement")) {
					String maynot = (String)forbiddenElement;
					structureTypeForbiddenElements.get(structureTypeName).add(XPath.newInstance(maynot));
				}
			}
		}
	}

	/**
	 * Determines and returns structureType of a given modsElement.
	 *
	 * @param modsElement
	 * @return
	 * @throws JDOMException
	 */
	private String getStructureType(Element modsElement) throws JDOMException {

		initializeStructureElementTypeConditions();
		String structureType = "";

		boolean structureTypeFound = false;
		for (String st : structureTypeMandatoryElements.keySet()){
			structureTypeFound = true;
			for (XPath mandatoryXPath : structureTypeMandatoryElements.get(st)){
				Element mandatoryElement = (Element)mandatoryXPath.selectSingleNode(modsElement);
				if (Objects.equals(mandatoryElement, null)) {
					structureTypeFound = false;
					break;
				}
			}
			if(structureTypeForbiddenElements.containsKey(st)) {
				for (XPath forbiddenXPath : structureTypeForbiddenElements.get(st)) {
					Element forbiddenElement = (Element)forbiddenXPath.selectSingleNode(modsElement);
					if (!Objects.equals(forbiddenElement, null)) {
						structureTypeFound = false;
						break;
					}
				}
			}
			if (structureTypeFound) {
				structureType = st;
				break;
			}
		}
		return structureType;
	}

	/**
	 * Transforms the given JDOM document 'inputXML' using the given XSLT file 'stylesheetfile' and return the transformed Document.
	 *
	 * @param inputXML The Document that will be transformed
	 * @param stylesheetfile The XSLT file containing the transformation rules
	 * @param builder The SAXBuilder used to create the Document element from the transformed input document
	 * @return the transformed JDOM document
	 */
	private Document transformXML(Document inputXML, File stylesheetfile, SAXBuilder builder) {

		XMLOutputter xmlOutputter = new XMLOutputter();

		String xmlString = xmlOutputter.outputString(inputXML);

		String inputXMLFilename = dmdSecCounter + "_original_SRW_MODS.xml";
		String outputXMLFilename = dmdSecCounter + "_xslTransformedSRU.xml";
		dmdSecCounter ++;
		File outputFile = new File(outputXMLFilename);

		StreamSource transformSource = new StreamSource(stylesheetfile);

		TransformerFactoryImpl impl = new TransformerFactoryImpl();

		try {
			xmlOutputter.output(inputXML, new FileWriter(inputXMLFilename));

			FileOutputStream outputStream = new FileOutputStream(outputFile);

			Transformer xslfoTransformer = impl.newTransformer(transformSource);

			TransformerHandler transformHandler = ((SAXTransformerFactory)SAXTransformerFactory.newInstance()).newTransformerHandler();

			transformHandler.setResult(new StreamResult(outputStream));

			Result saxResult = new SAXResult(transformHandler);

			SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));

			xslfoTransformer.transform(saxSource, saxResult);

			return builder.build(outputFile);

		} catch (TransformerException | IOException | JDOMException e) {
			modsLogger.error("Error while transforming XML document: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

   /**
    * Retrieves and returns the given documents parent document from the Kalliope SRU interface.
    *
    * @param doc the Document whose parent Document is retrieved
    * @param timeout
    * @return the parent document of the given document or null, if the current document does not contain an element containing the ID of the parent document
    * @throws JDOMException
    * @see org.jdom.Document
    */
	private String retrieveParentRecord(Document doc, long timeout) throws JDOMException {
		Element parentIDElement = (Element)parentIDPath.selectSingleNode(doc);
		try {
			Query parentQuery = new Query("12:" + parentIDElement.getText());
			return client.retrieveModsRecord(parentQuery.getQueryUrl(), timeout);
		} catch(NullPointerException e) {
			modsLogger.info("Top level element reached. No further parent elements can be retrieved.");
			return null;
		}
	}

	/**
	 * Creates and returns a METS document containing METS descriptive metadata sections and a METS structural map
	 * constructed from the given List<Element> 'dmdElements' and List<String> 'documentTypes'.
	 *
	 * @param dmdElements
	 * @param documentTypes
	 * @return
	 */
	private static Document createMetsContainer(List<Element> dmdElements, List<String> documentTypes) {
		assert(dmdElements.size() == documentTypes.size());

		Document metsDocument = createMetsDocument();

		for (Element dmdSection : dmdElements) {
			metsDocument = addSectionToMETSDocument(metsDocument, dmdSection);
		}

		Element structMap = createMETSStructureMap(METS_LOGICAL);

		Element currentParent = structMap;
		for (int i = dmdElements.size()-1; i >= 0; i--) {
			currentParent = addDivToMETSStructureMap(currentParent, dmdElements.get(i), documentTypes.get(i));
		}

		metsDocument = addSectionToMETSDocument(metsDocument, structMap);
		return metsDocument;
	}

	/**
	 * Creates and returns a METS document.
	 *
	 * @return the created METS Document
	 * @see org.jdom.Document
	 * @see org.jdom.Element
	 */
	private static Document createMetsDocument() {

		Document metsDoc = new Document();
		Element metsRoot = new Element("mets", METS_NAMESPACE);
		metsRoot.addNamespaceDeclaration(METS_NAMESPACE);
		metsRoot.addNamespaceDeclaration(MODS_NAMESPACE);
		metsRoot.addNamespaceDeclaration(XSI_NAMESPACE);
		metsRoot.addNamespaceDeclaration(GOOBI_NAMESPACE);
		metsDoc.setRootElement(metsRoot);

		return metsDoc;
	}

	/**
	 * Adds the given Element 'section' containing a METS metadata section to the given Document 'metsDocument' and returns the Document.
	 *
	 * @param metsDocument the METS Document to which the given metadata section is added
	 * @param section the metadata section that is added to the given METS document
	 * @return the METS document
	 * @see org.jdom.Element
	 * @see org.jdom.Document
	 */
	private static Document addSectionToMETSDocument(Document metsDocument, Element section) {

		Element rootElement = metsDocument.getRootElement();
		rootElement.addContent(section);
		metsDocument.setRootElement(rootElement);

		return metsDocument;
	}

	/**
	 * Creates and returns a METS descriptive metadata element containing the given element as metadata.
	 *
	 * @param descriptiveMetadataElement
	 * @return
	 */
	private static Element createMETSDescriptiveMetadata(Element descriptiveMetadataElement) {

		Element metsDmdSec = new Element("dmdSec", METS_NAMESPACE);
		Element metsMdWrap = new Element("mdWrap", METS_NAMESPACE);
		Element metsXmlData = new Element("xmlData", METS_NAMESPACE);

		metsXmlData.addContent(descriptiveMetadataElement);
		metsMdWrap.addContent(metsXmlData);
		metsMdWrap.setAttribute(METS_MDTYPE, MODS_STRING);
		metsDmdSec.addContent(metsMdWrap);
		metsDmdSec.setAttribute(METS_ID, METS_DMD_ID_VALUE + "_" + String.format("%04d", dmdIdCounter));
		dmdIdCounter++;

		return metsDmdSec;
	}

	/**
	 * Creates and returns a StructureMap element of a METS document.
	 *
	 * @param type
	 * @return
	 */
	private static Element createMETSStructureMap(String type) {

		Element structureMap = new Element("structMap", METS_NAMESPACE);
		structureMap.setAttribute(METS_TYPE, type);

		return structureMap;
	}

	/**
	 * Creates a div for the given Element 'childElement', adds it to the given Element 'parentDiv' in the structure map and returns it.
	 *
	 * @param parentDiv
	 * @param childElement
	 * @param childType
	 * @return
	 */
	private static Element addDivToMETSStructureMap(Element parentDiv, Element childElement, String childType) {

		Element childDiv = new Element("div", METS_NAMESPACE);
		childDiv.setAttribute(METS_DMD_ID, childElement.getAttributeValue(METS_ID));
		childDiv.setAttribute(METS_ID, METS_DIV_ID_VALUE + "_" +String.format("%04d", divIdCounter));
		childDiv.setAttribute(METS_TYPE, childType);
		divIdCounter++;

		parentDiv.addContent(childDiv);

		return childDiv;
	}

	/**
	 * Removes the files with the provided String 'path' as filepath from the file system and performs exception handling.
	 *
	 * @param path
	 */
	private void deleteFile(String path) {
		FileSystem fs = FileSystems.getDefault();
		try {
		    Files.delete(fs.getPath(path));
		} catch (IOException x) {
		    modsLogger.error("Error while deleting file '" + path + "': " + x.getMessage());
		}
	}

	/**
	 * The function getNumberOfHits() returns the number of hits from a given
	 * search result.
	 *
	 * @param searchResult
	 *            the reference to the search whose number of hits shall be
	 *            looked up
	 * @param timeout
	 *            ignored because there is no network acceess in this step
	 * @return the number of hits
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getNumberOfHits(Object,
	 *      long)
	 */
	public static long getNumberOfHits(Object searchResult, long timeout) {
		return ((FindResult) searchResult).getHits();
	}

	/**
	 * The function getTitle() returns a human-readable name for the
	 * plug-in in English. The parameter language is ignored.
	 *
	 * @param language
	 *            desired language of the human-readable name (support is
	 *            optional)
	 * @return a human-readable name for the plug-in
	 * @see org.goobi.production.plugin.UnspecificPlugin#getTitle(Locale)
	 */
	public static String getTitle(Locale language) {
		return "MODS Catalogue Plugin";
	}

	/**
	 * The function setPreferences is called by Production to set the UGH
	 * preferences to be used.
	 *
	 * @param preferences
	 *            the UGH preferences
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#setPreferences(Prefs)
	 */
	public void setPreferences(Prefs preferences) {
		this.preferences = preferences;
	}

	/**
	 * The function supportsCatalogue() investigates whether the plug-in is able
	 * to acceess a catalogue identified by the given String. (This depends on
	 * the configuration.)
	 *
	 * @param catalogue
	 *            a String indentifying the catalogue
	 * @return whether the plug-in is able to acceess that catalogue
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#supportsCatalogue(String)
	 */
	public static boolean supportsCatalogue(String catalogue) {
		return ConfigOpac.getCatalogueByName(catalogue) != null;
	}

	/**
	 * The function getSupportedCatalogues(Object) returns the names of all catalogues supported
	 * by this plugin. (This depends on the plugin configuration.)
	 *
	 * @param catalogue
	 *            a String indentifying the catalogue
	 * @return whether the plug-in is able to acceess that catalogue
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#supportsCatalogue(String)
	 */
	public static List<String> getSupportedCatalogues(Object dummyObject){
		return ConfigOpac.getAllCatalogues();
	}

	/**
	 * The function getAllConfigDocTypes(Object) returns the names of all docTypes configured
	 * for this plugin. (This depends on the plugin configuration.)
	 *
	 * @param catalogue
	 *            a String indentifying the catalogue
	 * @return whether the plug-in is able to acceess that catalogue
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#supportsCatalogue(String)
	 */
	public static List<String> getAllConfigDocTypes(Object dummyObject) {
		List<String> result = new ArrayList<String>();
		for(ConfigOpacDoctype cod : ConfigOpac.getAllDoctypes()){
			result.add(cod.getTitle());
		}
		return result;
	}

	/**
	 * The function useCatalogue() sets a catalogue to be used
	 *
	 * @param catalogueID
	 *            a String indentifying the catalogue
	 * @throws ParserConfigurationException
	 *             if a DocumentBuilder cannot be created which satisfies the
	 *             configuration requested
	 * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#useCatalogue(String)
	 */
	public void useCatalogue(String catalogueID) throws ParserConfigurationException {
		this.configuration = ConfigOpac.getCatalogueByName(catalogueID);
		this.catalogue = new Catalogue(configuration);
		GetOpac catalogueClient = new GetOpac(catalogue);
		catalogueClient.setCharset(configuration.getCharset());
		this.client = catalogueClient;
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
	 * The function getConfigDir() provides a reference to the file system
	 * directory where configuration files are read from.
	 *
	 * @return the file system directory with the configuration files
	 */
	static String getConfigDir() {
		return configDir;
	}
}

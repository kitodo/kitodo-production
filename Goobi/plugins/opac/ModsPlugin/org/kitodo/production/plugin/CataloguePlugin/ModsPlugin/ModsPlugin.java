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

package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
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
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.XStream;

/**
 * The class ModsPlugin is the main class of the Goobi Mods catalogue plugin
 * implementation. It provides the public methods
 *
 *  void    configure(Map) [*]
 *  Object  find(String, long)
 *  String  getDescription() [*]
 *  Map     getHit(Object, long, long)
 *  long    getNumberOfHits(Object, long)
 *  String  getTitle() [*]
 *  void    setPreferences(Prefs)
 *  boolean supportsCatalogue(String)
 *  void    useCatalogue(String)
 *
 * as specified by org.goobi.production.plugin.UnspecificPlugin [*] and
 * org.goobi.production.plugin.CataloguePlugin.CataloguePlugin.
 *
 * @author Arved Solth, Christopher Timm
 */
@PluginImplementation
public class ModsPlugin implements Plugin {
    private static final Logger logger = Logger.getLogger(ModsPlugin.class);

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

    /**
     * @return XMLConfiguration of this plugin, containing - among others -
     *         docType names and rules for structType classification
     */
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
     * The SAXBuilder is used to transform XML documents using XSLT files.
     */
    private static SAXBuilder sb = new SAXBuilder();

    private static String xsltFilepath = "";

    private static File transformationScript = null;

    private static File tempFile = null;

    private static List<File> tempFiles = null;

    private static XMLOutputter xmlOutputter = new XMLOutputter();

    static {
        xmlOutputter.setFormat(Format.getPrettyFormat());
    }

    private HashMap<String, Element> structureMaps = null;

    /**
     * Namespace and tag name constants used to create METS documents
     * encapsulating MODS documents
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
     * Constants that are used in the ModsPlugin configuration file XML
     * structure.
     */
    private static final String CONF_LABEL = "label";
    private static final String CONF_VALUE = "value";
    private static final String CONF_NAME = "name";
    private static final String CONF_TITLE = "title";
    private static final String CONF_XPATH = "xpath";
    private static final String CONF_SEARCHFIELDS = "searchFields.searchField";
    private static final String CONF_DETAILS = "additionalDetails.detail";
    private static final String CONF_INSTITUTIONS = "filterInstitutions.institution";
    private static final String CONF_FILTER_PARAMETER = "institutionFilterParameter";
    private static final String CONF_ID_PARAMETER = "identifierParameter";
    private static final String CONF_PARENT_ELEMENT = "parentElement";
    private static final String CONF_RECORD_ELEMENT = "recordElement";
    private static final String CONF_ID_ELEMENT = "identifierElement";
    private static final String CONF_CATALOGUE = "catalogue";
    private static final String CONF_MAXIMUMCHILDRECORDS = "maximumChildRecords";

    /**
     * Human-readable description of the plug-in’s functionality in English.
     */
    private static final String PLUGIN_DESCRIPTION = "The MODS plugin can be used to access MODS library catalogue systems.";

    /**
     * Hashmaps with structureTypes (String) as keys and lists of XPath
     * instances values. The lists of XPaths instances describe the elements a
     * MODS document must or must not contain in order to be classified as a
     * specific structureType (e.g. the corresponding key in the Hashmap)
     */
    private static HashMap<String, List<XPath>> structureTypeMandatoryElements = new HashMap<>();
    private static HashMap<String, List<XPath>> structureTypeForbiddenElements = new HashMap<>();
    private static HashMap<String, String> structureTypeToDocTypeMapping = new HashMap<>();

    /**
     * Path of the output file for the XSL transformation.
     */
    private static final String TEMP_FILENAME = "tempMETSMODS";

    /**
     * Filename of the XSL transformation file. This filename is being loaded
     * from the plugin configuration file.
     */
    private static String MODS2GOOBI_TRANSFORMATION_RULES_FILENAME;

    /**
     * Static XPath variables used to parse MetsModsGoobi documents.
     */
    private static XPath srwRecordXPath = null;
    private static XPath modsXPath = null;
    private static XPath parentIDXPath = null;
    private static XPath identifierXPath = null;
    private static XPath metsDivXPath = null;
    private static XPath catalogIDDigitalXPath = null;
    private static XPath goobiXpath = null;


    /**
     * Static counter variables for constructing METS DmdSections for multiple
     * imported MODS documents.
     */
    private static int dmdSecCounter = 0;
    private static int dmdIdCounter = 1;
    private static int divIdCounter = 1;

    /**
     * The method configure() accepts a Map with configuration parameters. Three
     * entries, "configDir", "tempDir" and "xsltDir" are expected.
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
            logger.error("Error while querying library catalogue: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error while querying library catalogue: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Initializes static XPath variables used for parsing a MetsModsGoobi
     * document.
     */
    private void initializeXPath() {
        // initialize common XPaths first
        try {
            modsXPath = XPath.newInstance("//mods:mods");
            metsDivXPath = XPath.newInstance(".//mets:div");
            catalogIDDigitalXPath = XPath.newInstance(".//goobi:metadata[@name='CatalogIDDigital']");
            goobiXpath = XPath.newInstance(".//goobi:goobi");
            identifierXPath = XPath.newInstance(getIdentifierXPath(configuration.getTitle()));
        } catch (JDOMException e) {
            logger.error("Error while initializing global XPath variables: " + e.getMessage());
        }

        // TODO: load only xpath definied in specific OPAC configuration!
        // initialize custom catalog XPaths
        try {
            parentIDXPath = XPath.newInstance(getParentElementXPath(configuration.getTitle()));
            srwRecordXPath = XPath.newInstance(getRecordXPath(configuration.getTitle()));
        } catch (JDOMException e) {
            logger.error("Error while initializing catalog specific XPath variables: " + e.getMessage());
        }
    }

    /**
     * Checks and returns whether all static mandatory XPath variables used to parse
     * MetsModsGoobi documents have been initialized.
     *
     * @return whether all static XPath variables have been initialized or not
     */
    private boolean standardXPathsDefined() {
        return (!Objects.equals(modsXPath, null)
                && !Objects.equals(metsDivXPath, null)
                && !Objects.equals(catalogIDDigitalXPath, null)
                && !Objects.equals(goobiXpath, null)
                && !Objects.equals(identifierXPath, null));
    }

    private boolean customXPathsDefined() {
        return (!Objects.equals(srwRecordXPath, null)
                && !Objects.equals(parentIDXPath, null));
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
        return PLUGIN_DESCRIPTION;
    }

    /**
     * Extracts the structureMap Element from the given Element 'rootElement' and returns it.
     * @param rootElement
     *          the Element from which the structureMap is extracted
     * @return the structureMap Element if found
     * @throws JDOMException
     */
    private Element getStructureMap(Element rootElement) throws JDOMException {
        Element structureMap = null;
        ElementFilter structMapFilter = new ElementFilter("structMap", METS_NAMESPACE);
        for (Iterator structMapElementIter = rootElement.getDescendants(structMapFilter); structMapElementIter.hasNext();) {
            Element structMapElement = (Element) structMapElementIter.next();
            if (Objects.equals(structMapElement.getAttributeValue(METS_TYPE), METS_LOGICAL)) {
                structureMap = structMapElement;
                break;
            }
        }
        if (Objects.equals(structureMap, null)) {
            throw new JDOMException("ERROR: no logical structmap found in existing mets document!");
        }
        return structureMap;
    }

    /**
     * Transforms the given XML Document 'importDoc' via the static XSL transformation
     * script 'transformationScript' into the ModsGoobi format, wraps the resulting
     * ModsGoobi document inside a Mets Descriptive Metadata Section and saves it to
     * the given File 'metaFile'. If the file already exists and contains a Mets document,
     * the new Metadata section will be added to it. Otherwise, the file and the corresponding
     * Mets structures will be created.
     * The method returns the name of the DocStructType of 'importDoc'.
     *
     * @param importDoc
     *          the document that is being added to the given metadata file.
     * @param metaFile
     *          the metadata file to which the given document will be added
     * @param timeout
     *          a timeout in milliseconds after which the operation shall return
     * @return the name of the DocStructType of the given Document 'importDoc'
     * @throws IOException
     * @throws JDOMException
     */
    private Element addDocumentToFile(Document importDoc, File metaFile, long timeout) throws IOException, JDOMException {

        Document metsDocument = null;
        Element rootElement = null;

        // create new file and mets structures, if it doesn't exist
        if (!metaFile.exists() || metaFile.length() == 0) {

            metaFile.createNewFile();
            tempFiles.add(metaFile);

            // Build the metsDocument directly
            metsDocument = createMetsDocument();
            rootElement = metsDocument.getRootElement();
        }
        // read file and existing mets structures, if file already exists
        else {
            // read mets document from existing file
            metsDocument = sb.build(metaFile);
            rootElement = metsDocument.getRootElement();
        }

        Document transformedDoc = transformXML(importDoc, transformationScript);

        String documentID = ((Element) identifierXPath.selectSingleNode(transformedDoc)).getText();

        // XML MODS data of document itself
        Element modsElement = (Element) modsXPath.selectSingleNode(transformedDoc);

        Element metadataSection = createMETSDescriptiveMetadata((Element) modsElement.clone());
        rootElement.addContent(metadataSection);

        metsDocument.setRootElement(rootElement);

        // save updated file
        xmlOutputter.output(metsDocument, new FileWriter(metaFile.getAbsoluteFile()));

        return metadataSection;
    }

    /**
     * Adds a mets div with the given type 'docStructType' for the given metadata section 'dmdSec' to the given logical
     * structure map 'structureMap' and returns the updated structure map.
     *
     * @param structureMap
     *          The structure map to which a mets div will be added for the given metadata section 'dmdSec'
     * @param docStructType
     *          The type of the mets div to be added to the structure map
     * @param dmdSec
     *          The metadata section for which a mets div is added to the given structure map
     * @return The updated logical structure map
     */
    private Element addMetadataSectionToStructureMap(Element structureMap, String docStructType, Element dmdSec) {

        // 1: create mets:div for given metadata section
        Element structureMapDiv = createMETSStructureMapDiv(dmdSec.getAttributeValue(METS_ID), docStructType);

        // 2: add new mets:div to existing mets:divs
        for (Object childObject : structureMap.getChildren("div", METS_NAMESPACE)) {
            Element childElement = (Element) childObject;
            structureMapDiv.addContent((Element) childElement.clone());
        }

        // 3: remove children that have been moved to new mets:div
        structureMap.removeChildren("div", METS_NAMESPACE);

        // 4: add new topmost mets:div to structure map
        structureMap.addContent(structureMapDiv);

        // 5: return updated structure map
        return structureMap;
    }

    /**
     * Reads the METS object contained in the given file, adds divs for all its descriptive metadata sections to a
     * clone of the given structure map, adds it to the METS object and saves the updated METS object back to the file.
     * @param structureMap
     *          The structure map to which divs are added for all metadata sections of the METS object in the given file
     * @param file
     *          The file containing the metadata sections for which mets divs are added to the given structure map
     */
    private void addStructureMapToFile(Element structureMap, File file) throws JDOMException, IOException {
        Document metsDocument = sb.build(file);
        Element rootElement = metsDocument.getRootElement();
        Element customStructureMap = (Element) structureMap.clone();

        // collect all DMDIDs for which dmd sections exist in given file
        ArrayList<String> dmdids = new ArrayList<>();
        ElementFilter dmdSecFilter = new ElementFilter("dmdSec", METS_NAMESPACE);
        for (Iterator dmdSecIter = rootElement.getDescendants(dmdSecFilter); dmdSecIter.hasNext();) {
            Element dmdSecElement = (Element) dmdSecIter.next();
            dmdids.add(dmdSecElement.getAttributeValue(METS_ID));
        }

        // remove DMDID attribute from mets:div elements in custom structure map for which no corresponding
        // dmd section exists in the given file
        ArrayList<Element> metsDivNodes = (ArrayList<Element>) metsDivXPath.selectNodes(customStructureMap);
        for (Element metsDiv : metsDivNodes) {
            if (!dmdids.contains(metsDiv.getAttributeValue(METS_DMD_ID))) {
                metsDiv.removeAttribute(METS_DMD_ID);
            }
        }

        Element structureMapElement = rootElement.getChild("structMap", METS_NAMESPACE);
        if (Objects.equals(structureMapElement, null) || !structureMapElement.getAttributeValue(METS_TYPE).equals(METS_LOGICAL)){
            rootElement.addContent(customStructureMap);
            xmlOutputter.output(metsDocument, new FileWriter(file.getAbsoluteFile()));
        }
    }


    /**
     * Create DMD sections for all elements in the given list 'children' (imported via the SRU interface) and save them
     * to the given file.
     * If the given File already contains a METS structure, it is augmented with the created metadata sections.
     * Otherwise, a new basic METS structure is created to which the metadata sections are added.
     * @param file
     *          The file to which the METS structure with the created metadata sections is saved
     * @param children
     *          The imported elements for which DMD sections are created
     * @return A hash map containing the created DMD sections as keys and their doc struct types as values.
     * @throws JDOMException
     * @throws IOException
     */
    private HashMap<Element, String> saveChildMetadataSectionsToFile(File file,  List<Element> children) throws JDOMException, IOException {

        Document metsDocument;
        Element rootElement;

        // ensure the file contains correct base mets structure!
        if(file.length() > 0) {
            metsDocument = sb.build(file);
        }
        else {
            metsDocument = createMetsDocument();
        }

        rootElement = metsDocument.getRootElement();

        Element childElement;
        Document transformedChild;

        HashMap<Element, String> childMetadataSections = new HashMap<>();

        for (int i = 0; i < children.size(); i++) {
            childElement = children.get(i);

            // determine structType from original child doc
            String childStructureType = getStructureType(childElement);

            // transform child document with XSL
            transformedChild = transformXML(removeAllChildrenButOne(childElement.getDocument(), i), transformationScript);
            Element childMods = (Element) modsXPath.selectSingleNode(transformedChild);

            // create metadata section from transformed child doc
            Element childMetadataSection = createMETSDescriptiveMetadata((Element) childMods.clone());

            rootElement.addContent(childMetadataSection);

            childMetadataSections.put(childMetadataSection, childStructureType);
        }

        xmlOutputter.output(metsDocument, new FileWriter(file.getAbsoluteFile()));

        return childMetadataSections;
    }

    /**
     * Creates descriptive metadata sections for all elements in the given list 'childDMDSections'
     * and returns the resulting list.
     *
     * @param childDMDSections
     *          List of DMD sections for which structure map divs are created and returned
     * @return list of structure map divs
     */
    private List<Element> addChildDocumentsToStructureDiv(HashMap<Element, String> childDMDSections) {

        Element dmdSec;

        List<Element> childStructureMapDivs = new LinkedList<>();

        for (Map.Entry<Element, String> entry : childDMDSections.entrySet()) {
            dmdSec = entry.getKey();
            childStructureMapDivs.add(createMETSStructureMapDiv(dmdSec.getAttributeValue(METS_ID), entry.getValue()));
        }

        return childStructureMapDivs;
    }


    private Element addChildDocumentsToStructMap(List<Element> childDocuments, File file, Element structureMap) throws JDOMException, IOException {
        if (childDocuments.size() > 0) {
            HashMap<Element, String> childMetadataSections = saveChildMetadataSectionsToFile(file, childDocuments);
            for (Element childStructureMapDiv : addChildDocumentsToStructureDiv(childMetadataSections)) {
                structureMap.addContent(childStructureMapDiv);
            }
        }
        return structureMap;
    }

    /**
     * Retrieve and return a given documents ID. The document needs to be in
     * the internal MetsModsGoobi format to ensure the ID is found at a known location.
     *
     * @param transformedDocument
     *          document whose ID is extracted and returned
     * @return String ID of the given document
     */
    private String extractDocumentIdentifier(Document transformedDocument) throws JDOMException {
        return ((Element) identifierXPath.selectSingleNode(transformedDocument)).getText();
    }

    /**
     * Add the ID of the given metadata section 'anchorMetadataSection' to all topmost metadata sections referenced in
     * the given 'structureMap', saves the result to the given file 'metadataFile' and returns the updated structure
     * map.
     * @param metadataFile
     *          The file to which the updated metadata sections containing the extracted anchor ID are saved
     * @param structureMap
     *          The structure map whose topmost divs reference the metadata sections to which the anchor ID will be added
     * @param anchorMetadataSection
     *          The metadata section that acts as an anchor
     * @return The updated structure map
     * @throws JDOMException
     * @throws IOException
     */
    private Element addAnchorIDToMetadatasections(File metadataFile, Element structureMap, Element anchorMetadataSection, String ancherClassName) throws JDOMException, IOException {
        // anchor element that will be added to all corresponding dmd sections!
        Element anchorCatalogIDElement = (Element) catalogIDDigitalXPath.selectSingleNode(anchorMetadataSection);
        String anchorCatalogID = anchorCatalogIDElement.getText();

        Element anchorIDElement = new Element("metadata", GOOBI_NAMESPACE);

        anchorIDElement.setAttribute("anchorId", ancherClassName);
        anchorIDElement.setAttribute("name", "CatalogIDDigital");
        anchorIDElement.setText(anchorCatalogID);

        // retrieve all dmd sections from given metadata file
        Document childDoc = sb.build(metadataFile);
        Element childRoot = childDoc.getRootElement();

        // get all topmost mets:div elements in given structureMap
        // (hold DMDID references to metadata sections in file to which the anchor ID should be added!)
        XPath topMostDivXpath = XPath.newInstance("mets:div");

        for (Object topmostDiv : topMostDivXpath.selectNodes(structureMap)) {
            Element currentTopMostElement = (Element) topmostDiv;

            String metadatasectionID = currentTopMostElement.getAttributeValue(METS_DMD_ID);

            XPath dmdSecXPath = XPath.newInstance("mets:dmdSec[@ID='" + metadatasectionID + "']");

            Element childMetadataSection = (Element) dmdSecXPath.selectSingleNode(childRoot);

            if(!Objects.equals(childMetadataSection, null)) {

                Element goobiElement = (Element) goobiXpath.selectSingleNode(childMetadataSection);

                goobiElement.addContent((Element) anchorIDElement.clone());
            }
        }

        // save updated file
        xmlOutputter.output(childDoc, new FileWriter(metadataFile.getAbsoluteFile()));

        return structureMap;
    }

    /**
     * Create and add a descriptive metadata section and a corresponding mets div element for the given Document
     * 'document' in the given logical structure map, update the given metadataFile accordingly and return the updated
     * structure map.
     * If the given parameter 'addChildren' is true, the child elements of the given document will be retrieved and
     * processed as well.
     *
     * @param document
     *          The Document for which a DMD section and corresponding structure map div is created
     * @param metadataFile
     *          The metadata file to which the DMD section and updated structure map will be saved
     * @param structureMap
     *          The structure map to which a mets div will be added for the given Document 'document'
     * @param documentID
     *          The original ID of the given document which can be used to retrieve its children from the queried SRU interface
     * @param addChildren
     *          Flag indicating whether the child documents of the given documents are to be added as well or not
     * @param timeout
     *          Timeout in milliseconds after which the operation shall return
     * @return The updated logical structure map
     * @throws RuntimeException
     * @throws JDOMException
     * @throws IOException
     */
    private Element addDocumentToFileAndStructureMap(Document document, File metadataFile, Element structureMap, String documentID, boolean addChildren, long timeout) throws RuntimeException, JDOMException, IOException {

        Element modsElement = (Element) modsXPath.selectSingleNode(document);
        // modsElement is 'null' if last structural element pointed
        // to a "virtueller Bestand" as parent element;
        // this is not allowed in Kitodo, therefore throw an
        // exception here!
        if (Objects.equals(modsElement, null)) {
            throw new RuntimeException("Requested document with ID '" + documentID
                    + "' is not associated with a valid inventory.");
        }

        Element metadatasection;

        String lastStructureType = getStructureType(modsElement);

        // Check whether the current DocStructType has to be saved to separate anchor file or not
        DocStructType docStructType = preferences.getDocStrctTypeByName(lastStructureType);
        String anchorClass = docStructType.getAnchorClass();

        // use anchor file if current docstruct has anchor class
        if (!Objects.equals(anchorClass, null) && !anchorClass.isEmpty()) {
            String anchorFilenameSuffix = anchorClass.equals("true") ? "anchor" : anchorClass;
            String anchorFilename = FilenameUtils.removeExtension(metadataFile.getName()) + "_" + anchorFilenameSuffix;
            String anchorFileFullPath = metadataFile.getParent() + File.separator + anchorFilename + ".xml";

            File anchorFile = new File(anchorFileFullPath);

            metadatasection = addDocumentToFile(document, anchorFile, timeout);
        }
        // use metadata file if current docstruct has NO anchor class
        else {
            metadatasection = addDocumentToFile(document, metadataFile, timeout);
        }

        if (addChildren) {
            structureMap = addChildDocumentsToStructMap(retrieveChildDocuments(documentID, timeout), metadataFile, structureMap);
        }

        if (!Objects.equals(anchorClass, null) && !anchorClass.isEmpty()) {
            // add CatalogueIDDigital of anchor metadatasection to last metadatasection _before_ anchor!
            // TODO: find better (e.g. more robust!) way to select metadata sections to which the anchor ID should be added!
            if (tempFiles.size() > 0) {
                // add anchor references to _last_ anchor file
                if (tempFiles.size() > 1) {
                    addAnchorIDToMetadatasections(tempFiles.get(tempFiles.size() - 2), structureMap, metadatasection,
                            anchorClass);
                }
                // add anchor references to _base_ file (if the requested document itself has a docstruct type with "anchor=true" in the used ruleset!)
                else {
                    addAnchorIDToMetadatasections(metadataFile, structureMap, metadatasection, anchorClass);
                }
            }
        }

        structureMap = addMetadataSectionToStructureMap(structureMap, lastStructureType, metadatasection);

        // return the updated map
        return structureMap;
    }

    private void resetConfiguration() {
        loadMappingFile(configuration.getTitle());
        xsltFilepath = xsltDir + MODS2GOOBI_TRANSFORMATION_RULES_FILENAME;
        transformationScript = new File(xsltFilepath);
        tempFiles = new LinkedList<>();
        structureMaps = new HashMap<>();
        initializeXPath();
        initializeStructureToDocTypeMapping();
        dmdIdCounter = 1;
        divIdCounter = 1;
    }

    /**
     * Creates a list of documents from the given xmlString. The order in the resulting list
     * defines the linear hierarchy of the document structure, with the first Document in the list representing
     * the leaf and the last representing the root of the hierarchy.
     *
     * @param xmlString String containing the XML representing a list of documents
     * @return the list of documents parsed from the given String
     */
    private LinkedList<Document> createDocumentHistoryFromXMLString(String xmlString) {
        LinkedList<Document> documentHierarchy = new LinkedList<>();

        try {
            XPath relatedItemXPath = XPath.newInstance("//mods:mods/mods:relatedItem[@type='host']");

            // 1 create jdom.Document given string
            Document document = sb.build(new StringReader(xmlString));
            //xmlOutputter.output(document, System.out);

            // 2 separate original document and related item
            Element relatedItemElement = (Element) relatedItemXPath.selectSingleNode(document);
            if (relatedItemElement != null) {
                Element relatedItemDocElement = (Element) relatedItemElement.clone();
                relatedItemElement.detach();
                xmlOutputter.output(document, System.out);
                // 3 add jdom.Documents to list
                documentHierarchy.add(document);
                if (relatedItemDocElement != null) {
                    // TODO: create document for host item and add it to the list
                }
            } else {
                documentHierarchy.add(document);
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return documentHierarchy;
    }

    /**
     * Create a list of documents from the given FindResult instance. The order in the resulting list
     * defines the linear hierarchy of the document structure, with the first Document in the list representing
     * the leaf and the last representing the root of the hierarchy.
     *
     * @param findResult
     * @param index
     * @param timeout
     * @return
     */
    private LinkedList<Document> createDocumentHistoryFromFindResult(FindResult findResult, long index, long timeout) {
        Query myQuery = findResult.getQuery();
        String xmlString = client.retrieveModsRecord(myQuery.getQueryUrl(), timeout);

        LinkedList<Document> documentHierarchy = new LinkedList<>();
        if (xmlString == null) {
            String message = "Error: result empty!";
            logger.error(message);
            throw new IllegalStateException(message);
        } else {
            try {
                Document doc = sb.build(new StringReader(xmlString));
                @SuppressWarnings("unchecked")
                ArrayList<Element> recordNodes = (ArrayList<Element>) srwRecordXPath.selectNodes(doc);
                // Remove all records but the one identified by the given 'index'
                for (int i = 0; i < recordNodes.size(); i++) {
                    Element currentRecord = recordNodes.get(i);
                    if (i != index) {
                        currentRecord.detach();
                    }
                }

                // update xmlString so it only contains one record
                xmlString = xmlOutputter.outputString(doc);

                while (xmlString != null) {
                    doc = sb.build(new StringReader(xmlString));
                    documentHierarchy.add(doc);
                    xmlString = retrieveParentRecord(doc, timeout);
                }

            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }
        return documentHierarchy;
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
        resetConfiguration();

        if (!Files.isDirectory(FileSystems.getDefault().getPath(xsltDir))) {
            String message = "Error: XSLT directory not found!";
            logger.error(message);
            throw new IOException(message);
        }

        LinkedList<Document> documentHierarchy = null;
        if (searchResult instanceof String) {
            documentHierarchy = createDocumentHistoryFromXMLString((String) searchResult);
        } else if (searchResult instanceof FindResult) {
            if (!standardXPathsDefined()) {
                String message = "Error: XPath variables not defined!";
                logger.error(message);
                throw new IllegalStateException(message);
            }
            documentHierarchy = createDocumentHistoryFromFindResult((FindResult) searchResult, index, timeout);
        } else {
            logger.error("Unknown type '" + Object.class.getName() + "' of given searchResult => abort!");
        }

        Map<String, Object> result = new HashMap<>();

        tempFile = File.createTempFile(TEMP_FILENAME, ".xml");

        if (documentHierarchy == null || documentHierarchy.isEmpty()) {
            String message = "Error: document hierarchy is empty!";
            logger.error(message);
            throw new IllegalStateException(message);
        } else {
            try {
                Document transformedDocument = transformXML(documentHierarchy.get(0), transformationScript);
                xmlOutputter.output(transformedDocument, System.out);
                String docID = extractDocumentIdentifier(transformedDocument);
                result.putAll(getAdditionalDetails(transformedDocument));

                // Global structmap holding the whole structure map (will be added to all metadata files at the end of the loop)
                Element structMap = createMETSStructureMap(METS_LOGICAL);

                for (Document document : documentHierarchy) {
                    // TODO: there should be a configuration parameter controlling whether child nodes are added or not!
                    boolean addChildren = (documentHierarchy.indexOf(document) == 0) && searchResult instanceof FindResult;
                    structMap = addDocumentToFileAndStructureMap(document, tempFile, structMap, docID,  addChildren, timeout);
                }

                // add structure map to all tempFiles!
                for (File f : tempFiles) {
                    addStructureMapToFile(structMap, f);
                }
                // FIXME: this shouldn't be necessary (the structmap in "tempFile" should be correct to begin with!)
                addStructureMapToFile(structMap, tempFile);

                MetsMods mm = new MetsMods(preferences);

                mm.read(tempFile.getAbsolutePath());
                // reviewing the constructed DigitalDocument can be done via
                // "System.out.println(mm.getDigitalDocument());"

                deleteTemporaryFiles();

                DigitalDocument dd = mm.getDigitalDocument();
                Fileformat ff = new XStream(preferences);
                ff.setDigitalDocument(dd);

                DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
                DocStruct dsBoundBook = dd.createDocStruct(dst);
                dd.setPhysicalDocStruct(dsBoundBook);

                if (!Objects.equals(result.get("shelfmarksource"), null)) {
                    org.kitodo.production.plugin.CataloguePlugin.ModsPlugin.UGHUtils
                            .replaceMetadatum(dd.getPhysicalDocStruct(), preferences, "shelfmarksource", (String) result.get("shelfmarksource"));
                }

                String topStructType = getStructureType((Element) modsXPath.selectSingleNode(documentHierarchy.get(0)));

                result.put("fileformat", ff);
                result.put("type", structureTypeToDocTypeMapping.get(topStructType));

            } catch (JDOMException | TypeNotAllowedForParentException | PreferencesException | ReadException
                    | IOException e) {
                logger.error("Error while retrieving document: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * This function loads the mandatory and forbidden elements of individual
     * structureElement types from the plugin configuration file, used for
     * structureType classification in the getStructureType function.
     *
     * @throws JDOMException
     */
    private void initializeStructureElementTypeConditions() throws JDOMException {

        if (structureTypeMandatoryElements.keySet().size() < 1 && structureTypeForbiddenElements.keySet().size() < 1) {

            XMLConfiguration pluginConfiguration = ConfigOpac.getConfig();

            for (Object structureTypeObject : pluginConfiguration.configurationsAt("structuretypes.type")) {
                SubnodeConfiguration structureType = (SubnodeConfiguration) structureTypeObject;

                String structureTypeName = "";

                ConfigurationNode structureNode = structureType.getRootNode();
                for (Object rulesetObject : structureNode.getAttributes("rulesetType")) {
                    ConfigurationNode rulesetNode = (ConfigurationNode) rulesetObject;
                    structureTypeName = (String) rulesetNode.getValue();
                }

                if (!structureTypeMandatoryElements.containsKey(structureTypeName)) {
                    structureTypeMandatoryElements.put(structureTypeName, new LinkedList<XPath>());
                }
                for (Object mandatoryElement : structureType.getList("mandatoryElement")) {
                    String mustHave = (String) mandatoryElement;
                    structureTypeMandatoryElements.get(structureTypeName).add(XPath.newInstance(mustHave));
                }

                if (!structureTypeForbiddenElements.containsKey(structureTypeName)) {
                    structureTypeForbiddenElements.put(structureTypeName, new LinkedList<XPath>());
                }
                for (Object forbiddenElement : structureType.getList("forbiddenElement")) {
                    String maynot = (String) forbiddenElement;
                    structureTypeForbiddenElements.get(structureTypeName).add(XPath.newInstance(maynot));
                }
            }
        }
    }

    private void loadMappingFile(String opacName) {

        MODS2GOOBI_TRANSFORMATION_RULES_FILENAME = "";
        for (Object catalogueObject : ConfigOpac.getConfig().configurationsAt(CONF_CATALOGUE)) {
            SubnodeConfiguration catalogue = (SubnodeConfiguration) catalogueObject;
            for (Object titleAttrObject : catalogue.getRootNode().getAttributes(CONF_TITLE)) {
                ConfigurationNode titleAttr = (ConfigurationNode) titleAttrObject;
                if (Objects.equals(opacName, titleAttr.getValue())) {
                    SubnodeConfiguration catalogueConf = (SubnodeConfiguration) catalogueObject;
                    logger.debug("Setting mapping file for OPAC '" + opacName + "' to '"
                            + catalogueConf.getString("mappingFile") + "'");
                    MODS2GOOBI_TRANSFORMATION_RULES_FILENAME = catalogueConf.getString("mappingFile");
                    break;
                }
            }
            if (!Objects.equals(MODS2GOOBI_TRANSFORMATION_RULES_FILENAME, "")) {
                break;
            }
        }
    }

    /**
     * This function loads the rulesetType and title attributes of individual
     * docType elements and creates a mapping from docStructTypes to docTypes.
     *
     */
    private void initializeStructureToDocTypeMapping() {

        XMLConfiguration pluginConfiguration = ConfigOpac.getConfig();

        for (Object docTypeObject : pluginConfiguration.configurationsAt("doctypes.type")) {

            SubnodeConfiguration docType = (SubnodeConfiguration) docTypeObject;

            String rulesetTypeString = "";
            String titleString = "";

            ConfigurationNode docTypeNode = docType.getRootNode();

            for (Object rulesetTypeObject : docTypeNode.getAttributes("rulesetType")) {
                ConfigurationNode rulesetTypeNode = (ConfigurationNode) rulesetTypeObject;
                rulesetTypeString = (String) rulesetTypeNode.getValue();
            }

            for (Object titleObject : docTypeNode.getAttributes(CONF_TITLE)) {
                ConfigurationNode titleNode = (ConfigurationNode) titleObject;
                titleString = (String) titleNode.getValue();
            }

            if (!Objects.equals(titleString, "") && !Objects.equals(titleString, null)
                    && !Objects.equals(rulesetTypeString, "") && !Objects.equals(rulesetTypeString, null)) {
                structureTypeToDocTypeMapping.put(rulesetTypeString, titleString);
            }
        }
    }

    /**
     * Determines and returns structureType of a given modsElement.
     *
     * @param modsElement Element for which the structureType is determined and returned.
     * @return the name of the determined structureType
     * @throws JDOMException
     */
    private String getStructureType(Element modsElement) throws JDOMException {

        initializeStructureElementTypeConditions();
        String structureType = "";

        boolean structureTypeFound;
        for (String st : structureTypeMandatoryElements.keySet()) {
            structureTypeFound = true;
            for (XPath mandatoryXPath : structureTypeMandatoryElements.get(st)) {
                Element mandatoryElement = (Element) mandatoryXPath.selectSingleNode(modsElement);
                if (Objects.equals(mandatoryElement, null)) {
                    structureTypeFound = false;
                    break;
                }
            }
            if (structureTypeFound && structureTypeForbiddenElements.containsKey(st)) {
                for (XPath forbiddenXPath : structureTypeForbiddenElements.get(st)) {
                    Element forbiddenElement = (Element) forbiddenXPath.selectSingleNode(modsElement);
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
     * Transforms the given JDOM document 'inputXML' using the given XSLT file
     * 'stylesheetFile' and return the transformed Document.
     *
     * @param inputXML
     *            The Document that will be transformed
     * @param stylesheetFile
     *            The XSLT file containing the transformation rules
     * @return the transformed JDOM document
     */
    private Document transformXML(Document inputXML, File stylesheetFile) {

        String xmlString = xmlOutputter.outputString(inputXML);

        String outputXMLFilename = dmdSecCounter + "_xslTransformedSRU";
        dmdSecCounter++;

        StreamSource transformSource = new StreamSource(stylesheetFile);

        TransformerFactoryImpl impl = new TransformerFactoryImpl();

        try {
            File outputFile = File.createTempFile(outputXMLFilename, ".xml");

            FileOutputStream outputStream = new FileOutputStream(outputFile);

            Transformer xslfoTransformer = impl.newTransformer(transformSource);

            TransformerHandler transformHandler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();

            transformHandler.setResult(new StreamResult(outputStream));

            Result saxResult = new SAXResult(transformHandler);

            SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));

            xslfoTransformer.transform(saxSource, saxResult);

            Document resultDoc = sb.build(outputFile);
            deleteFile(outputFile.getAbsolutePath());

            return resultDoc;

        } catch (TransformerException | IOException | JDOMException e) {
            logger.error("Error while transforming XML document: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves and returns the given documents parent document from the
     * Kalliope SRU interface.
     *
     * @param doc
     *            the Document whose parent Document is retrieved
     * @param timeout
     * @return the parent document of the given document or null, if the current
     *         document does not contain an element containing the ID of the
     *         parent document
     * @throws JDOMException
     * @see org.jdom.Document
     */
    private String retrieveParentRecord(Document doc, long timeout) throws JDOMException {
        Element parentIDElement = (Element) parentIDXPath.selectSingleNode(doc);
        try {
            Query parentQuery = new Query(getIdentifierParameter(configuration.getTitle()) + ":"
                    + parentIDElement.getText());
            return client.retrieveModsRecord(parentQuery.getQueryUrl(), timeout);
        } catch (NullPointerException e) {
            logger.info("Top level element reached. No further parent elements can be retrieved.");
            return null;
        }
    }

    /**
     * Retrieves and returns the child elements of the document with the given ID 'documentId'
     * from the Kalliope SRU interface.
     *
     * @param documentId
     * @param timeout
     * @return the list of child elements
     * @throws JDOMException
     */
    private List<Element> retrieveChildDocuments(String documentId, long timeout) throws JDOMException {
        List<Element> childDocuments = new LinkedList<>();
        Query childrenQuery = new Query("context.ead.id:" + documentId);
        childrenQuery.setMaximumRecords(getMaximumChildRecordsParameter(configuration.getTitle()));
        String allChildren = client.retrieveModsRecord(childrenQuery.getQueryUrl(), timeout);

        try {
            Document childrenDoc = sb.build(new StringReader(allChildren));
            for (Object child : modsXPath.selectNodes(childrenDoc)) {
                childDocuments.add((Element) child);
            }
        } catch (IOException e) {
            logger.error("Unable to import child elements of document with given ID.");
        }
        return childDocuments;
    }

    /**
     * Clones the given document 'doc' and removes all children from the cloned document
     * except the child at position 'index'. Returns the cloned document with one remaining
     * child.
     *
     * @param doc
     * @param index
     * @return document with one remaining child
     */
    @SuppressWarnings("unchecked")
    private Document removeAllChildrenButOne(Document doc, int index) {
        try {
            Document clone = (Document) doc.clone();
            Element remainingChild = null;
            ArrayList<Element> recordNodes = (ArrayList<Element>) srwRecordXPath.selectNodes(clone);

            for (int i = 0; i < recordNodes.size(); i++) {
                Element currentRecord = recordNodes.get(i);
                if (i != index) {
                    currentRecord.detach();
                } else {
                    remainingChild = currentRecord;
                }
            }
            if (Objects.equals(remainingChild, null)) {
                return clone;
            }
            return remainingChild.getDocument();
        } catch (JDOMException e) {
            logger.error("Unable to remove children from given document.");
            return null;
        }
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
     * Creates and returns a METS descriptive metadata element containing the
     * given element as metadata.
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

    private static Element createMETSStructureMapDiv(String metadataSectionId, String structType) {

        Element structMapDiv = new Element("div", METS_NAMESPACE);
        structMapDiv.setAttribute(METS_DMD_ID, metadataSectionId);
        structMapDiv.setAttribute(METS_ID, METS_DIV_ID_VALUE + "_" + String.format("%04d", divIdCounter));
        structMapDiv.setAttribute(METS_TYPE, structType);
        divIdCounter++;

        return structMapDiv;
    }

    /**
     * Removes the files with the provided String 'path' as filepath from the
     * file system and performs exception handling.
     *
     * @param path
     */
    private void deleteFile(String path) {
        FileSystem fs = FileSystems.getDefault();
        try {
            Files.delete(fs.getPath(path));
        } catch (IOException x) {
            logger.error("Error while deleting file '" + path + "': " + x.getMessage());
        }
    }

    /**
     * Removes all temporary files potentially saved during an import
     */
    private void deleteTemporaryFiles() {
        for(File f : tempFiles) {
            deleteFile(f.getAbsolutePath());
        }
        tempFiles = new LinkedList<>();
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
     * The function getTitle() returns a human-readable name for the plug-in in
     * English. The parameter language is ignored.
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
     * The function getSupportedCatalogues() returns the names of all catalogues
     * supported by this plugin. (This depends on the plugin configuration.)
     *
     * @return list of catalogue names
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getSupportedCatalogues()
     */
    public static List<String> getSupportedCatalogues() {
        return ConfigOpac.getAllCatalogues();
    }

    /**
     * The function getAllConfigDocTypes() returns the names of all docTypes
     * configured for this plugin. (This depends on the plugin configuration.)
     *
     * @return list of ConfigOapcDocTypes
     * @see org.goobi.production.plugin.CataloguePlugin.CataloguePlugin#getAllConfigDocTypes()
     */
    public static List<String> getAllConfigDocTypes() {
        List<String> result = new ArrayList<String>();
        for (ConfigOpacDoctype cod : ConfigOpac.getAllDoctypes()) {
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

    /**
     * The function getCatalougeConfigutaration(String catalogueName) is a
     * helper function to retrieve and return the subnode configuration of a
     * catalogue whose name equals the given String "catalogueName" from the
     * plugin configuration file. If no subnode configuration for such a
     * catalogue exsits in the plugin configuration file, null is returned.
     *
     * @param catalogueName
     *            the name of the catalogue for which the subnode configuration
     *            is returned
     * @return SubnodeConfiguration for the catalogue with the name
     *         "catalogueName" or null if no such catalogue SubnodeConfiguration
     *         exists in the plugin configuration file
     */
    private SubnodeConfiguration getCatalogueConfiguration(String catalogueName) {
        if (!Objects.equals(ConfigOpac.getConfig(), null)) {
            for (Object catalogueObject : ConfigOpac.getConfig().configurationsAt(CONF_CATALOGUE)) {
                SubnodeConfiguration catalogue = (SubnodeConfiguration) catalogueObject;
                for (Object titleAttrObject : catalogue.getRootNode().getAttributes(CONF_TITLE)) {
                    ConfigurationNode titleAttr = (ConfigurationNode) titleAttrObject;
                    String currentOpacName = (String) titleAttr.getValue();
                    if (Objects.equals(catalogueName, currentOpacName)) {
                        return catalogue;
                    }
                }
            }
        }
        return null;
    }

    private HashMap<String, String> getConfigurationMapping(String catalogueName, String subConfigurationPath,
            String keyAttribute, String valueAttribute) {
        LinkedHashMap<String, String> configurationMapping = new LinkedHashMap<String, String>();
        SubnodeConfiguration catalogueConfiguration = getCatalogueConfiguration(catalogueName);
        if (!Objects.equals(catalogueConfiguration, null)) {
            for (Object fieldObject : catalogueConfiguration.configurationsAt(subConfigurationPath)) {
                SubnodeConfiguration conf = (SubnodeConfiguration) fieldObject;
                configurationMapping.put(conf.getString("[@" + keyAttribute + "]"), conf.getString("[@" + valueAttribute
                        + "]"));
            }
        }
        return configurationMapping;
    }

    private String getConfigurationAttributeValue(String catalogueName, String subConfigurationPath,
            String valueAttribute) {
        String parameterName = "";
        SubnodeConfiguration catalogueConfiguration = getCatalogueConfiguration(catalogueName);
        if (!Objects.equals(catalogueConfiguration, null)) {
            for (Object fieldObject : catalogueConfiguration.configurationsAt(subConfigurationPath)) {
                SubnodeConfiguration conf = (SubnodeConfiguration) fieldObject;
                parameterName = conf.getString("[@" + valueAttribute + "]");
            }
        }
        return parameterName;
    }

    private String getConfigurationValue(String catalogueName, String subConfigurationPath) {
        String configValue = "";

        SubnodeConfiguration catalogueConfiguration = getCatalogueConfiguration(catalogueName);
        if (!Objects.equals(catalogueConfiguration, null)) {
            for (Object fieldObject : catalogueConfiguration.configurationsAt(subConfigurationPath)) {
                SubnodeConfiguration conf = (SubnodeConfiguration) fieldObject;
                configValue = conf.getRoot().getValue().toString();
            }
        }

        return configValue;
    }

    /**
     * The function getParentElementXPath(String catalogueName) returns the
     * XPath pointing to the parent element ID in a document.
     *
     * @param catalogueName
     *            the name of the catalogue for which the institution filter
     *            parameter is returned
     * @return String the XPath for parent element IDs in query results
     *         documents
     */
    public String getParentElementXPath(String catalogueName) {
        return getConfigurationAttributeValue(catalogueName, CONF_PARENT_ELEMENT, CONF_XPATH);
    }

    /**
     * The function getRecordXPath(String catalogueName) returns the XPath
     * pointing to individual records in a query result XML document.
     *
     * @param catalogueName
     *            the name of the catalogue for which the institution filter
     *            parameter is returned
     * @return String the XPath for individual records in the query results for
     *         the configured catalogue
     */
    public String getRecordXPath(String catalogueName) {
        return getConfigurationAttributeValue(catalogueName, CONF_RECORD_ELEMENT, CONF_XPATH);
    }

    /**
     * The function getIdentifierXPath(String catalogueName) returns the XPath
     * pointing to the identifier of a query result XML document.
     *
     * @param catalogueName
     *            the name of the catalogue for which identifier XPath is
     *            returned
     * @return String the XPath for the identifier element in the query result
     *         XML document.
     */
    public String getIdentifierXPath(String catalogueName) {
        return getConfigurationAttributeValue(catalogueName, CONF_ID_ELEMENT, CONF_XPATH);
    }

    /**
     * The function getAdditionalDetailsFields(String catalogueName) load the
     * names of additional metadata fields to be displayed in the website form,
     * configured in the configuration file of this plugin, for the catalogue
     * with the name 'catalogueName'.
     *
     * @param catalogueName
     *            the name of the catalogue for which the list of additional
     *            metadata fields is returned
     * @return Map containing the additional metadata fields of the selected
     *         OPAC
     */
    public HashMap<String, String> getAdditionalDetailsFields(String catalogueName) {
        return getConfigurationMapping(catalogueName, CONF_DETAILS, CONF_NAME, CONF_XPATH);
    }

    /**
     * The function getSearchFields(String catalogueName) loads the search
     * fields, configured in the configuration file of this plugin, for the
     * catalogue with the given String 'catalogueName', and returns them in a
     * HashMap. The map contains the labels of the search fields as keys and the
     * corresponding URL parameters as values.
     *
     * @param catalogueName
     *            the name of the catalogue for which the list of search fields
     *            is returned
     * @return Map containing the search fields of the selected OPAC
     */
    public HashMap<String, String> getSearchFields(String catalogueName) {
        return getConfigurationMapping(catalogueName, CONF_SEARCHFIELDS, CONF_LABEL, CONF_VALUE);
    }

    /**
     * The function getInstitutions(String catalogueName) loads the institutions
     * usable for result filtering, configured in the configuration file of this
     * plugin, for the catalogue with the given String 'cagalogueName', and
     * returns them in a HashMap. The map contains the labels of the
     * institutions as keys and the corresponding ISIL IDs as values.
     *
     * @param catalogueName
     *            the name of the catalogue for which the list of search fields
     *            will be returned
     * @return Map containing the filter institutions of the selected OPAC
     */
    public HashMap<String, String> getInstitutions(String catalogueName) {
        return getConfigurationMapping(catalogueName, CONF_INSTITUTIONS, CONF_LABEL, CONF_VALUE);
    }

    /**
     * The function getInstitutionFilterParameter(String catalogueName) returns
     * the URL parameter used for institution filtering in this plugin.
     *
     * @param catalogueName
     *            the name of the catalogue for which the institution filter
     *            parameter is returned
     * @return String the URL parameter used for institution filtering
     */
    public String getInstitutionFilterParameter(String catalogueName) {
        return getConfigurationAttributeValue(catalogueName, CONF_FILTER_PARAMETER, CONF_VALUE);
    }

    /**
     * The function getIdentifierParameter(String catalogueName) returns the URL
     * parameter used for retrieving documents by identifier configured for the
     * given catalogue.
     *
     * @param catalogueName
     *            the name of the catalogue for which the ID parameter is
     *            returned
     * @return String the URL parameter used for retrieving documents by
     *         identifier
     */
    public String getIdentifierParameter(String catalogueName) {
        return getConfigurationAttributeValue(catalogueName, CONF_ID_PARAMETER, CONF_VALUE);
    }

    /**
     * Methods returns the amount of how many child records should be retrieved.
     * A value of 0 is returned if no entry is defined or a non numeric value is set.
     *
     * @param catalogueName
     *            the name of the catalogue for which the ID parameter is
     *            returned
     * @return value of maximum records
     */
    public int getMaximumChildRecordsParameter(String catalogueName) {
        int maximumRecords = 0;

        String configurationValue = getConfigurationValue(catalogueName, CONF_MAXIMUMCHILDRECORDS);

        if ((!Objects.equals(configurationValue, null))
                && (!configurationValue.isEmpty())) {
            try {
                maximumRecords = Integer.valueOf(configurationValue);
            } catch (NumberFormatException nfe) {
                logger.warn(CONF_MAXIMUMCHILDRECORDS + " entry contains a non numeric value!");
            }
        }

        return maximumRecords;
    }

    /**
     * Read "additionalDetails" from given document 'transformedDocument'
     * via XPaths elements specified in plugin configuration file.
     * @param transformedDocument document in internal format
     * @return Map containing additional details
     * @throws JDOMException
     */
    private Map<String, String> getAdditionalDetails(Document transformedDocument) throws JDOMException {
        Map<String, String> additionalDetails = new HashMap<>();
        for (Map.Entry<String, String> detailField : getAdditionalDetailsFields(configuration.getTitle()).entrySet()) {
            XPath detailPath = XPath.newInstance(detailField.getValue());
            Element detailElement = (Element) detailPath.selectSingleNode(transformedDocument);
            if (!Objects.equals(detailElement, null)) {
                additionalDetails.put(detailField.getKey(), detailElement.getText());
            }
        }
        return additionalDetails;
    }
}

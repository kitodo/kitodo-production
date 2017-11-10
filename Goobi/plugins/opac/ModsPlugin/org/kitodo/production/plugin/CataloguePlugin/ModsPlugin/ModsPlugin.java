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
import java.nio.file.Path;
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

    /**
     * Hashmaps with structureTypes (String) as keys and lists of XPath
     * instances values. The lists of XPaths instances describe the elements a
     * MODS document must or must not contain in order to be classified as a
     * specific structureType (e.g. the corresponding key in the Hashmap)
     */
    private static HashMap<String, List<XPath>> structureTypeMandatoryElements = new HashMap<String, List<XPath>>();
    private static HashMap<String, List<XPath>> structureTypeForbiddenElements = new HashMap<String, List<XPath>>();
    private static HashMap<String, String> structureTypeToDocTypeMapping = new HashMap<String, String>();

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
        try {
            srwRecordXPath = XPath.newInstance(getRecordXPath(configuration.getTitle()));
            modsXPath = XPath.newInstance("//mods:mods");
            parentIDXPath = XPath.newInstance(getParentElementXPath(configuration.getTitle()));
            identifierXPath = XPath.newInstance(getIdentifierXPath(configuration.getTitle()));
        } catch (JDOMException e) {
            logger.error("Error while initializing XPath variables: " + e.getMessage());
        }
    }

    /**
     * Checks and returns whether all static XPath variables used to parse
     * MetsModsGoobi documents have been initialized.
     *
     * @return whether all static XPath variables have been initialized or not
     */
    private boolean xpathsDefined() {
        return (!Objects.equals(srwRecordXPath, null) && !Objects.equals(modsXPath, null)
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

        loadMappingFile(configuration.getTitle());

        String xsltFilepath = xsltDir + MODS2GOOBI_TRANSFORMATION_RULES_FILENAME;

        Map<String, Object> result = new HashMap<String, Object>();

        Query myQuery = ((FindResult) searchResult).getQuery();

        String resultXML = client.retrieveModsRecord(myQuery.getQueryUrl(), timeout);

        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());

        initializeXPath();

        Fileformat ff;

        Path xsltDirPath = FileSystems.getDefault().getPath(xsltDir);

        if (resultXML == null) {
            String message = "Error: result empty!";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        else if (!xpathsDefined()) {
            String message = "Error: XPath variables not defined!";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        else if (!Files.isDirectory(xsltDirPath)) {
            String message = "Error: XSLT directory not found!";
            logger.error(message);
            throw new IOException(message);
        }

        else {
            SAXBuilder sb = new SAXBuilder();
            try {
                Document doc = sb.build(new StringReader(resultXML));
                initializeStructureToDocTypeMapping();

                @SuppressWarnings("unchecked")
                ArrayList<Element> recordNodes = (ArrayList<Element>) srwRecordXPath.selectNodes(doc);

                for (int i = 0; i < recordNodes.size(); i++) {
                    Element currentRecord = recordNodes.get(i);
                    if (i != (int) index) {
                        currentRecord.detach();
                    }
                }

                // Build the metsDocument directly
                Document metsDocument = createMetsDocument();
                Element rootElement = metsDocument.getRootElement();

                // initialize Mets structMap
                Element structureMap = createMETSStructureMap(METS_LOGICAL);
                String lastStructureType = getStructureType((Element) modsXPath.selectSingleNode(doc));

                // retrieve parent record before transforming requested document
                // (since "localParentID" is lost during XSL transformation)
                String parentXML = retrieveParentRecord(doc, timeout);

                File transformationScript = new File(xsltFilepath);

                doc = transformXML(doc, transformationScript, sb);

                String documentID = ((Element) identifierXPath.selectSingleNode(doc)).getText();

                // read "additionalDetails" from document via XPaths elements
                // specified in plugin configuration file
                for (Map.Entry<String, String> detailField : getAdditionalDetailsFields(configuration.getTitle()).entrySet()) {
                    XPath detailPath = XPath.newInstance(detailField.getValue());
                    Element detailElement = (Element) detailPath.selectSingleNode(doc);
                    if (!Objects.equals(detailElement, null)) {
                        result.put(detailField.getKey(), detailElement.getText());
                    }
                }

                // XML MODS data of document itself
                Element modsElement = (Element) modsXPath.selectSingleNode(doc);

                Element metadataSection = createMETSDescriptiveMetadata((Element) modsElement.clone());
                rootElement.addContent(metadataSection);

                Element structureMapDiv = createMETSStructureMapDiv(metadataSection.getAttributeValue(METS_ID), lastStructureType);
                Element structureMapDivTemp = null;

                List<Element> childDocuments = retrieveChildDocuments(documentID, timeout);
                Element childElement;
                Document transformedChild;

                // *** child elements
                for (int i = 0; i < childDocuments.size(); i++) {
                    childElement = childDocuments.get(i);

                    // determine structType from original child doc
                    String childStructureType = getStructureType(childElement);

                    // transform child document with XSL
                    transformedChild = transformXML(removeAllChildrenButOne(childElement.getDocument(), i), transformationScript, sb);
                    Element childMods = (Element) modsXPath.selectSingleNode(transformedChild);

                    // create metadata section from transformed child doc
                    metadataSection = createMETSDescriptiveMetadata((Element) childMods.clone());
                    rootElement.addContent(metadataSection);

                    // create structure map div for current child
                    structureMapDivTemp = createMETSStructureMapDiv(metadataSection.getAttributeValue(METS_ID), childStructureType);
                    structureMapDiv.addContent(structureMapDivTemp);
                }

                // reset variable to use it for ancestor elements as well
                structureMapDivTemp = null;

                File tempFile = File.createTempFile(TEMP_FILENAME, ".xml");

                List<File> anchorFiles = new LinkedList<File>();

                // Anchor file mets document
                Document anchorMetsDocument = null;
                Element anchorRootElement = null;
                Element anchorStructureMapDiv = null;
                Element anchorStructureMap = null;

                // *** ancestor elements
                while (!Objects.equals(parentXML, null)) {
                    resultXML = parentXML;
                    doc = sb.build(new StringReader(resultXML));
                    parentXML = retrieveParentRecord(doc, timeout);
                    modsElement = (Element) modsXPath.selectSingleNode(doc);
                    // modsElement is 'null' if last structural element pointed
                    // to a "virtueller Bestand" as parent element;
                    // this is not allowed in Kitodo, therefore throw an
                    // exception here!
                    if (Objects.equals(modsElement, null)) {
                        throw new RuntimeException("Requested document with ID '" + documentID
                                + "' is not associated with a valid inventory.");
                    }

                    // determine structType from untransformed xml document
                    lastStructureType = getStructureType(modsElement);

                    doc = transformXML(doc, transformationScript, sb);

                    // 'doc' can become "null", when the last doc had a
                    // 'parentID', but trying to retrieve the element with this
                    // parentID yields an empty SRW container (e.g. not
                    // containing any MODS documents)
                    // if 'doc' is null after the XSL transformation (e.g. just
                    // an empty XML header), 'selectSingleNode' can't be called
                    // on it anymore! Therefore the loop has to be terminated
                    // before reaching this point!
                    modsElement = (Element) modsXPath.selectSingleNode(doc);
                    metadataSection = createMETSDescriptiveMetadata((Element) modsElement.clone());

                    // Check whether the current DocStructType has to be saved to separate anchor file or not
                    DocStructType docStructType = preferences.getDocStrctTypeByName(lastStructureType);
                    String anchorClass = docStructType.getAnchorClass();

                    // Case 1: current docstruct has anchor class
                    if (!Objects.equals(anchorClass, null) && !anchorClass.isEmpty()) {
                        System.out.println("Create file for anchor class '" + anchorClass + "'");
                        String anchorFilenameSuffix = anchorClass.equals("true") ? "anchor" : anchorClass;
                        String anchorFilename = FilenameUtils.removeExtension(tempFile.getName()) + "_" + anchorFilenameSuffix;
                        String anchorFileFullPath = tempFile.getParent() + File.separator + anchorFilename + ".xml";
                        File anchorFile = new File(anchorFileFullPath);
                        if (!anchorFile.exists()) {
                            System.out.println("Create new anchor file!");
                            anchorFile.createNewFile();
                            anchorFiles.add(anchorFile);

                            anchorMetsDocument = createMetsDocument();
                            anchorRootElement = anchorMetsDocument.getRootElement();

                            // create new structure map
                            anchorStructureMap = createMETSStructureMap(METS_LOGICAL);
                            // add structure map to anchor root element
                            anchorRootElement.addContent(anchorStructureMap);
                        }
                        else {
                            System.out.println("Re-use existing anchor file!");
                            // read mets document from existing anchor file
                            anchorMetsDocument = sb.build(anchorFile);
                            anchorRootElement = anchorMetsDocument.getRootElement();

                            // retrieve existing logical structure map from anchor mets document
                            ElementFilter structMapFilter = new ElementFilter("structMap", METS_NAMESPACE);
                            for (Iterator<Element> structMapElementIter = anchorRootElement.getDescendants(structMapFilter); structMapElementIter.hasNext();) {
                                Element structMapElement = structMapElementIter.next();
                                if (Objects.equals(structMapElement.getAttributeValue("TYPE"), "LOGICAL")) {
                                    System.err.println("******");
                                    System.out.println("Found logical struct map in anchor mets document!");
                                    System.err.println("******");
                                    anchorStructureMap = structMapElement;
                                    break;
                                }
                            }
                            if (Objects.equals(anchorStructureMap, null)) {
                                System.err.println("******");
                                System.err.println("ERROR: no logical structmap found in existing anchor mets document!");
                                System.err.println("******");
                            }
                        }
                        System.out.println("=> ensured file exists at " + anchorFileFullPath);

                        // now save the mets metadata section to the anchor file
                        anchorRootElement.addContent(metadataSection);
                        anchorMetsDocument.setRootElement(anchorRootElement);

                        // TODO: create new structure div and add it to anchor structmap
                        //  - create structure div and add it to the anchor structmap
                        anchorStructureMapDiv = createMETSStructureMapDiv(metadataSection.getAttributeValue(METS_ID), lastStructureType);
                        anchorStructureMap.addContent(anchorStructureMapDiv);

                        // save new mets document to current anchor file
                        xmlOutputter.output(anchorMetsDocument, new FileWriter(anchorFile.getAbsoluteFile()));
                    }
                    // Case 2: current docstruct has NO anchor class
                    else {
                        rootElement.addContent(metadataSection);

                        // create structure map div for current ancestor
                        structureMapDivTemp = createMETSStructureMapDiv(metadataSection.getAttributeValue(METS_ID), lastStructureType);
                        structureMapDivTemp.addContent(structureMapDiv);
                        structureMapDiv = structureMapDivTemp;
                    }
                }

                // only add ancestors topmost structureMapDiv to structMap if
                // the div is not null (otherwise add documents own div as
                // topmost div to structmap)
                if (!Objects.equals(structureMapDivTemp, null)) {
                    structureMap.addContent(structureMapDivTemp);
                } else {
                    structureMap.addContent(structureMapDiv);
                }

                rootElement.addContent(structureMap);
                metsDocument.setRootElement(rootElement);
                // reviewing the constructed XML mets document can be done via
                // "xmlOutputter.output(metsDocument, System.out);"

                MetsMods mm = new MetsMods(preferences);

                xmlOutputter.output(metsDocument, new FileWriter(tempFile.getAbsoluteFile()));
                System.out.println("Read temp file " + tempFile.getAbsolutePath());
                mm.read(tempFile.getAbsolutePath());
                // reviewing the constructed DigitalDocument can be done via
                // "System.out.println(mm.getDigitalDocument());"

                //deleteFile(tempFile.getAbsolutePath());
                for(File f : anchorFiles) {
                    //deleteFile(f.getAbsolutePath());
                }
                DigitalDocument dd = mm.getDigitalDocument();
                ff = new XStream(preferences);
                ff.setDigitalDocument(dd);

                DocStructType dst = preferences.getDocStrctTypeByName("BoundBook");
                DocStruct dsBoundBook = dd.createDocStruct(dst);
                dd.setPhysicalDocStruct(dsBoundBook);

                if (!Objects.equals(result.get("shelfmarksource"), null)) {
                    org.kitodo.production.plugin.CataloguePlugin.ModsPlugin.UGHUtils
                            .replaceMetadatum(dd.getPhysicalDocStruct(), preferences, "shelfmarksource", (String) result.get("shelfmarksource"));
                }

                result.put("fileformat", ff);
                result.put("type", structureTypeToDocTypeMapping.get(lastStructureType));

            } catch (JDOMException | TypeNotAllowedForParentException | PreferencesException | ReadException
                    | IOException e) {
                logger.error("Error while retrieving document: " + e.getMessage());
                e.printStackTrace();
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
     * @throws JDOMException
     */
    private void initializeStructureToDocTypeMapping() throws JDOMException {

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
     * @param modsElement
     * @return the name of the determined structureType
     * @throws JDOMException
     */
    private String getStructureType(Element modsElement) throws JDOMException {

        initializeStructureElementTypeConditions();
        String structureType = "";

        boolean structureTypeFound = false;
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
     * 'stylesheetfile' and return the transformed Document.
     *
     * @param inputXML
     *            The Document that will be transformed
     * @param stylesheetfile
     *            The XSLT file containing the transformation rules
     * @param builder
     *            The SAXBuilder used to create the Document element from the
     *            transformed input document
     * @return the transformed JDOM document
     */
    private Document transformXML(Document inputXML, File stylesheetfile, SAXBuilder builder) {

        XMLOutputter xmlOutputter = new XMLOutputter();

        String xmlString = xmlOutputter.outputString(inputXML);

        String outputXMLFilename = dmdSecCounter + "_xslTransformedSRU";
        dmdSecCounter++;

        StreamSource transformSource = new StreamSource(stylesheetfile);

        TransformerFactoryImpl impl = new TransformerFactoryImpl();

        try {
            File outputFile = File.createTempFile(outputXMLFilename, "xml");

            FileOutputStream outputStream = new FileOutputStream(outputFile);

            Transformer xslfoTransformer = impl.newTransformer(transformSource);

            TransformerHandler transformHandler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();

            transformHandler.setResult(new StreamResult(outputStream));

            Result saxResult = new SAXResult(transformHandler);

            SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));

            xslfoTransformer.transform(saxSource, saxResult);

            Document resultDoc = builder.build(outputFile);
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
        List<Element> childDocuments = new LinkedList<Element>();
        Query childrenQuery = new Query("context.ead.id:" + documentId);
        String allChildren = client.retrieveModsRecord(childrenQuery.getQueryUrl(), timeout);

        SAXBuilder sb = new SAXBuilder();
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
}

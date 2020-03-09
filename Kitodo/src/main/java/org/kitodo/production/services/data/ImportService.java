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

package org.kitodo.production.services.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.SchemaConverterInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.ConfigProject;
import org.kitodo.config.OPACConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ParameterNotFoundException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.forms.createprocess.ProcessBooleanMetadata;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.forms.createprocess.ProcessSelectMetadata;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.KitodoNamespaceContext;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ImportService {

    private static final Logger logger = LogManager.getLogger(ImportService.class);

    private static volatile ImportService instance = null;
    private static ExternalDataImportInterface importModule;
    private static final String KITODO_NAMESPACE = "http://meta.kitodo.org/v1/";
    private static final String KITODO_STRING = "kitodo";

    private ProcessGenerator processGenerator;
    private static final String REPLACE_ME = "REPLACE_ME";
    // default value for identifierMetadata if no OPAC specific metadata has been configured in kitodo_opac.xml
    private static String identifierMetadata = "CatalogIDDigital";
    private static String parentXpath = "//kitodo:metadata[@name='" + REPLACE_ME + "']";
    private static final String PARENTHESIS_TRIM_MODE = "parenthesis";
    private String trimMode = "";
    private LinkedList<ExemplarRecord> exemplarRecords;

    private static final String PERSON = "Person";
    private static final String ROLE = "Role";
    private static final String AUTHOR = "Author";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";

    private static final String MONOGRAPH = "Monograph";
    private static final String VOLUME = "Volume";
    private static final String MULTI_VOLUME_WORK = "MultiVolumeWork";

    private String tiffDefinition;
    private boolean usingTemplates;

    private TempProcess parentTempProcess;

    private static final String CATALOG_IDENTIFIER = "CatalogIDDigital";

    /**
     * Return singleton variable of type ImportService.
     *
     * @return unique instance of ImportService
     */
    public static ImportService getInstance() {
        ImportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ImportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ImportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    private void loadOpacConfiguration(String catalogName) {
        try {
            OPACConfig.getOPACConfiguration(catalogName);
            try {
                trimMode = OPACConfig.getParentIDTrimMode(catalogName);
            } catch (NoSuchElementException e) {
                logger.debug(e.getLocalizedMessage());
            }
            try {
                String idMetadata = OPACConfig.getIdentifierMetadata(catalogName);
                if (StringUtils.isNotBlank(idMetadata)) {
                    identifierMetadata = idMetadata;
                }
            } catch (NoSuchElementException e) {
                logger.debug(e.getLocalizedMessage());
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + catalogName + "' is not supported!");
        }
    }

    /**
     * Load ExternalDataImportInterface implementation with KitodoServiceLoader and perform given query string
     * with loaded module.
     *
     * @param searchField field to query
     * @param searchTerm  given search term
     * @param catalogName catalog to search
     * @param start index of first record returned
     * @param rows number of records returned
     * @return search result
     */
    public SearchResult performSearch(String searchField, String searchTerm, String catalogName, int start, int rows) {
        importModule = initializeImportModule();
        loadOpacConfiguration(catalogName);
        return importModule.search(catalogName, searchField, searchTerm, start, rows);
    }

    private ExternalDataImportInterface initializeImportModule() {
        KitodoServiceLoader<ExternalDataImportInterface> loader =
                new KitodoServiceLoader<>(ExternalDataImportInterface.class);
        return loader.loadModule();
    }

    /**
     * Load search fields of catalog with given name 'opac' from library catalog configuration file and return them as a list
     * of Strings.
     *
     * @param opac name of catalog whose search fields are loaded
     * @return list containing search fields
     */
    public List<String> getAvailableSearchFields(String opac) {
        try {
            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opac);
            List<String> fields = new ArrayList<>();
            for (HierarchicalConfiguration searchField : searchFields.configurationsAt("searchField")) {
                if ("true".equals(searchField.getString("[@hide]"))) {
                    continue;
                }
                fields.add(searchField.getString("[@label]"));
            }
            return fields;
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + opac + "' is not supported!");
        }
    }

    /**
     * Load catalog names from library catalog configuration file and return them as a list of Strings.
     *
     * @return list of catalog names
     */
    public List<String> getAvailableCatalogs() {
        try {
            return OPACConfig.getCatalogs();
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: no supported OPACs found in configuration file!");
        }
    }

    private LinkedList<ExemplarRecord> extractExemplarRecords(DataRecord record, String opac) throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {
        LinkedList<ExemplarRecord> exemplarRecords = new LinkedList<>();
        String exemplarXPath = OPACConfig.getExemplarFieldXPath(opac);
        String ownerXPath = OPACConfig.getExemplarFieldOwnerXPath(opac);
        String signatureXPath = OPACConfig.getExemplarFieldSignatureXPath(opac);

        if (!StringUtils.isBlank(exemplarXPath) && !StringUtils.isBlank(ownerXPath)
                && !StringUtils.isBlank(signatureXPath) && record.getOriginalData() instanceof String) {
            String xmlString = (String) record.getOriginalData();
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new KitodoNamespaceContext());
            Document doc = XMLUtils.parseXMLString(xmlString);
            NodeList exemplars = (NodeList) xPath.compile(exemplarXPath).evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < exemplars.getLength(); i++) {
                Node exemplar = exemplars.item(i);
                Node ownerNode = (Node) xPath.compile(ownerXPath).evaluate(exemplar, XPathConstants.NODE);
                Node signatureNode = (Node) xPath.compile(signatureXPath).evaluate(exemplar, XPathConstants.NODE);

                if (Objects.nonNull(ownerNode) && Objects.nonNull(signatureNode)) {
                    String owner = ownerNode.getTextContent();
                    String signature = signatureNode.getTextContent();
                    if (!StringUtils.isBlank(owner) && !StringUtils.isBlank(signature)) {
                        exemplarRecords.add(new ExemplarRecord(owner, signature));
                    }
                }
            }
        }
        return exemplarRecords;
    }

    /**
     * Iterate over "SchemaConverterInterface" implementations using KitodoServiceLoader and return
     * first implementation that supports the Metadata and File formats of the given DataRecord object
     * as source formats and the Kitodo internal format and XML as target formats, respectively.
     *
     * @param record
     *      Record whose metadata and return formats are used to filter the SchemaConverterInterface implementations
     *
     * @return List of SchemaConverterInterface implementations that support the metadata and return formats of the
     *      given Record.
     *
     * @throws UnsupportedFormatException when no SchemaConverter module with matching formats could be found
     */
    private SchemaConverterInterface getSchemaConverter(DataRecord record) throws UnsupportedFormatException {
        KitodoServiceLoader<SchemaConverterInterface> loader =
                new KitodoServiceLoader<>(SchemaConverterInterface.class);
        List<SchemaConverterInterface> converterModules = loader.loadModules().stream()
                .filter(c -> c.supportsSourceMetadataFormat(record.getMetadataFormat())
                        && c.supportsSourceFileFormat(record.getFileFormat())
                        && c.supportsTargetMetadataFormat(MetadataFormat.KITODO)
                        && c.supportsTargetFileFormat(FileFormat.XML))
                .collect(Collectors.toList());
        if (converterModules.isEmpty()) {
            throw new UnsupportedFormatException("No SchemaConverter found that supports '"
                    + record.getMetadataFormat() + "' and '" + record.getFileFormat() + "'!");
        }
        return converterModules.get(0);
    }

    /**
     * Get docType form imported record.
     * @param record imported record
     *       as Document
     * @return docType as String
     */
    private String getRecordDocType(Document record) {
        Element root = record.getDocumentElement();
        NodeList kitodoNodes = root.getElementsByTagNameNS(KITODO_NAMESPACE, KITODO_STRING);
        if (kitodoNodes.getLength() > 0) {
            NodeList importedMetadata = kitodoNodes.item(0).getChildNodes();
            for (int i = 0; i < importedMetadata.getLength(); i++) {
                Node metadataNode = importedMetadata.item(i);
                Element metadataElement = (Element) metadataNode;
                if ("docType".equals(metadataElement.getAttribute("name"))) {
                    return metadataElement.getTextContent();
                }
            }
        }
        return "";
    }

    private String getParentID(Document document) throws XPathExpressionException {
        XPath parentIDXpath = XPathFactory.newInstance().newXPath();
        parentIDXpath.setNamespaceContext(new KitodoNamespaceContext());
        NodeList nodeList = (NodeList) parentIDXpath.compile(parentXpath)
                .evaluate(document, XPathConstants.NODESET);
        if (nodeList.getLength() == 1) {
            Node parentIDNode = nodeList.item(0);
            if (PARENTHESIS_TRIM_MODE.equals(trimMode)) {
                return parentIDNode.getTextContent().replaceAll("\\([^)]+\\)", "");
            } else {
                return parentIDNode.getTextContent();
            }
        } else {
            return null;
        }
    }

    public TempProcess createTempProcessFromDocument(Document document, int templateID, int projectID)
            throws ProcessGenerationException {
        String docType = getRecordDocType(document);
        NodeList metadataNodes = extractMetadataNodeList(document);

        Process process = null;
        // "processGenerator" needs to be initialized when function is called for the first time
        if (Objects.isNull(processGenerator)) {
            processGenerator = new ProcessGenerator();
        }
        if (processGenerator.generateProcess(templateID, projectID)) {
            process = processGenerator.getGeneratedProcess();
        }

        return new TempProcess(process, metadataNodes, docType);
    }

    public String importProcessAndReturnParentID(String recordId, LinkedList<TempProcess> allProcesses, String opac,
                                                 int projectID, int templateID)
            throws IOException, ProcessGenerationException, XPathExpressionException, ParserConfigurationException,
            NoRecordFoundException, UnsupportedFormatException, URISyntaxException, SAXException {

        Document internalDocument = importDocument(opac, recordId, allProcesses.isEmpty());
        TempProcess tempProcess = createTempProcessFromDocument(internalDocument, templateID, projectID);

        // Workaround for classifying MultiVolumeWorks with insufficient information
        if (!allProcesses.isEmpty()) {
            String childDocType = allProcesses.getLast().getWorkpiece().getRootElement().getType();
            Workpiece workpiece = tempProcess.getWorkpiece();
            if (Objects.nonNull(workpiece) && Objects.nonNull(workpiece.getRootElement())) {
                String docType = workpiece.getRootElement().getType();
                if ((MONOGRAPH.equals(childDocType) || VOLUME.equals(childDocType)) && MONOGRAPH.equals(docType)) {
                    tempProcess.getWorkpiece().getRootElement().setType(MULTI_VOLUME_WORK);
                    allProcesses.getFirst().getWorkpiece().getRootElement().setType(VOLUME);
                }
            }
        }

        allProcesses.add(tempProcess);
        return getParentID(internalDocument);
    }

    /**
     * Import a record identified by the given ID 'recordId'.
     * Additionally, import all ancestors of the given process referenced in the original data of the process imported
     * from the OPAC selected in the given CreateProcessForm instance.
     * Return the list of processes as a LinkedList of TempProcess.
     *
     * @param recordId identifier of the process to import
     * @param opac the name of the catalog from which the record is imported
     * @param projectId the ID of the project for which a process is created
     * @param templateId the ID of the template from which a process is created
     * @param importDepth the number of hierarchical processes that will be imported from the catalog
     * @param parentIdMetadata names of Metadata types holding parent IDs of structure elements in internal format
     * @return List of TempProcess
     */
    public LinkedList<TempProcess> importProcessHierarchy(String recordId, String opac, int projectId, int templateId,
                                                          int importDepth, Collection<String> parentIdMetadata)
            throws IOException, ProcessGenerationException, XPathExpressionException, ParserConfigurationException,
            NoRecordFoundException, UnsupportedFormatException, URISyntaxException, SAXException, DAOException {
        importModule = initializeImportModule();
        processGenerator = new ProcessGenerator();
        LinkedList<TempProcess> processes = new LinkedList<>();
        if (parentIdMetadata.isEmpty()) {
            if (importDepth > 1) {
                Helper.setErrorMessage("newProcess.catalogueSearch.parentIDMetadataMissing");
                importDepth = 1;
            }
        } else {
            parentXpath = parentXpath.replace(REPLACE_ME, parentIdMetadata.toArray()[0].toString());
        }

        String parentID = importProcessAndReturnParentID(recordId, processes, opac, projectId, templateId);
        Template template = ServiceManager.getTemplateService().getById(templateId);
        if (Objects.isNull(template.getRuleset())) {
            throw new ProcessGenerationException("Ruleset of template " + template.getId() + " is null!");
        }
        int level = 1;
        this.parentTempProcess = null;
        while (Objects.nonNull(parentID) && level < importDepth) {
            HashMap<String, String> parentIDMetadata = new HashMap<>();
            parentIDMetadata.put(identifierMetadata, parentID);
            try {
                Process parentProcess = loadParentProcess(parentIDMetadata, template.getRuleset().getId(), projectId);
                if (Objects.isNull(parentProcess)) {
                    parentID = importProcessAndReturnParentID(parentID, processes, opac, projectId, templateId);
                    level++;
                } else {
                    logger.info("Process with ID '" + parentID + "' already in database. Stop hierarchical import.");
                    URI workpieceUri = ServiceManager.getProcessService().getMetadataFileUri(parentProcess);
                    Workpiece parentWorkpiece = ServiceManager.getMetsService().loadWorkpiece(workpieceUri);
                    this.parentTempProcess = new TempProcess(parentProcess, parentWorkpiece);
                    break;
                }
            } catch (SAXParseException | DAOException e) {
                // this happens for example if a document is part of a "Virtueller Bestand" in Kalliope for which a
                // proper "record" is not returned from its SRU interface
                logger.error(e.getLocalizedMessage());
                break;
            }
        }
        // always try to find a parent for last imported process (e.g. level == importDepth) in the database!
        if (Objects.nonNull(parentID) && level == importDepth) {
            this.parentTempProcess = checkForParent(parentID, templateId, projectId);
        }
        return processes;
    }

    private TempProcess checkForParent(String parentID, int templateID, int projectID) throws DAOException, IOException,
            ProcessGenerationException {
        HashMap<String, String> parentIDMetadata = new HashMap<>();
        parentIDMetadata.put(identifierMetadata, parentID);
        Process parentProcess = loadParentProcess(parentIDMetadata, templateID, projectID);
        if (Objects.nonNull(parentProcess)) {
            logger.info("Linking last imported process to parent process with ID " + parentID + " in database!");
            URI workpieceUri = ServiceManager.getProcessService().getMetadataFileUri(parentProcess);
            Workpiece parentWorkpiece = ServiceManager.getMetsService().loadWorkpiece(workpieceUri);
            return new TempProcess(parentProcess, parentWorkpiece);
        }
        return null;
    }

    private List<DataRecord> searchChildRecords(String opac, String parentId, int numberOfRows) {
        String parenIDSearchField = OPACConfig.getParentIDElement(opac);
        if (Objects.isNull(parenIDSearchField)) {
            throw new ConfigException("Unable to find parent ID search field for catalog '" + opac + "'!");
        }
        return importModule.getMultipleFullRecordsFromQuery(opac, parenIDSearchField, parentId, numberOfRows);
    }

    /**
     * Get number of child records of record with ID 'parentId' from catalog 'opac'.
     *
     * @param opac name of the catalog
     * @param parentId ID of the parent record
     * @return number of child records
     */
    public int getNumberOfChildren(String opac, String parentId) {
        loadOpacConfiguration(opac);
        String parenIDSearchField = OPACConfig.getParentIDElement(opac);
        if (Objects.isNull(parenIDSearchField)) {
            throw new ConfigException("Unable to find parent ID search field for catalog '" + opac + "'!");
        }
        SearchResult searchResult = performSearch(parenIDSearchField, parentId, opac, 0, 0);
        if (Objects.nonNull(searchResult)) {
            return searchResult.getNumberOfHits();
        } else {
            Helper.setErrorMessage("Error retrieving number of children for record with ID " + parentId + " from OPAC "
                    + opac + "!");
            return 0;
        }
    }

    /**
     * Search child records of record with ID 'elementID' from catalog 'opac', transform them into a list of
     * 'TempProcess' and return the list.
     *
     * @param opac name of catalog
     * @param elementID ID of record for which child records are retrieved
     * @param projectId ID of project for which processes are created
     * @param templateId ID of template with which processes are created
     * @param rows number of child records to retrieve from catalog
     * @return list of TempProcesses containing the retrieved child records.
     */
    public LinkedList<TempProcess> getChildProcesses(String opac, String elementID, int projectId, int templateId,
                                                     int rows)
            throws SAXException, UnsupportedFormatException, URISyntaxException, ParserConfigurationException,
            NoRecordFoundException, IOException, ProcessGenerationException {
        loadOpacConfiguration(opac);
        importModule = initializeImportModule();
        List<DataRecord> childRecords = searchChildRecords(opac, elementID, rows);
        LinkedList<TempProcess> childProcesses = new LinkedList<>();
        if (!childRecords.isEmpty()) {
            SchemaConverterInterface converter = getSchemaConverter(childRecords.get(0));
            File mappingFile = getMappingFile(opac);
            for (DataRecord childRecord : childRecords) {
                DataRecord internalRecord = converter.convert(childRecord, MetadataFormat.KITODO, FileFormat.XML, mappingFile);
                Document childDocument = XMLUtils.parseXMLString((String)internalRecord.getOriginalData());
                childProcesses.add(createTempProcessFromDocument(childDocument, templateId, projectId));
            }
            // TODO: sort child processes (by what? catalog ID? Signature?)
            return childProcesses;
        } else {
            throw new NoRecordFoundException("No child records found for data record with ID '" + elementID
                    + "' in OPAC '" + opac + "'!");
        }
    }

    public Document importDocument(String opac, String identifier, boolean extractExemplars) throws NoRecordFoundException,
            UnsupportedFormatException, URISyntaxException, IOException, XPathExpressionException,
            ParserConfigurationException, SAXException {
        // ################ IMPORT #################
        importModule = initializeImportModule();
        DataRecord dataRecord = importModule.getFullRecordById(opac, identifier);

        if (extractExemplars) {
            exemplarRecords = extractExemplarRecords(dataRecord, opac);
        }

        // ################# CONVERT ################
        // depending on metadata and return form, call corresponding schema converter module!
        SchemaConverterInterface converter = getSchemaConverter(dataRecord);

        File mappingFile = getMappingFile(opac);

        // transform dataRecord to Kitodo internal format using appropriate SchemaConverter!
        DataRecord internalRecord = converter.convert(dataRecord, MetadataFormat.KITODO, FileFormat.XML, mappingFile);

        if (!(internalRecord.getOriginalData() instanceof String)) {
            throw new UnsupportedFormatException("Original metadata of internal record has to be an XML String, '"
                    + internalRecord.getOriginalData().getClass().getName() + "' found!");
        }

        return XMLUtils.parseXMLString((String)internalRecord.getOriginalData());
    }

    private NodeList extractMetadataNodeList(Document document) throws ProcessGenerationException {
        NodeList kitodoNodes = document.getElementsByTagNameNS(KITODO_NAMESPACE, KITODO_STRING);
        if (kitodoNodes.getLength() != 1) {
            throw new ProcessGenerationException("Number of 'kitodo' nodes unequal to '1' => unable to generate process!");
        }
        Node kitodoNode = kitodoNodes.item(0);
        return kitodoNode.getChildNodes();
    }

    private File getMappingFile(String opac) throws URISyntaxException {
        File mappingFile = null;

        String mappingFileName = OPACConfig.getXsltMappingFile(opac);
        if (!StringUtils.isBlank(mappingFileName)) {
            URI xsltFile = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)).toUri()
                    .resolve(new URI(mappingFileName));
            mappingFile = ServiceManager.getFileService().getFile(xsltFile);
        }
        return mappingFile;
    }

    /**
     * Converts DOM node list of Kitodo metadata elements to metadata objects.
     *
     * @param nodes
     *            node list to convert to metadata
     * @param domain
     *            domain of metadata
     * @return metadata from node list
     */
    public static List<Metadata> importMetadata(NodeList nodes, MdSec domain) {
        List<Metadata> allMetadata = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (!(node instanceof Element)) {
                continue;
            }
            Element element = (Element) node;
            Metadata metadata;
            switch (element.getLocalName()) {
                case "metadata":
                    MetadataEntry entry = new MetadataEntry();
                    entry.setValue(element.getTextContent());
                    metadata = entry;
                    break;
                case "metadataGroup": {
                    MetadataGroup group = new MetadataGroup();
                    group.setGroup(importMetadata(element.getChildNodes(), null));
                    metadata = group;
                    break;
                }
                default:
                    continue;
            }
            metadata.setKey(element.getAttribute("name"));
            metadata.setDomain(domain);
            allMetadata.add(metadata);
        }
        return allMetadata;
    }

    /**
     * Get the value of a specific processDetail in the processDetails.
     *
     * @param processDetail
     *            as ProcessDetail
     * @return the value as a java.lang.String
     */
    public static String getProcessDetailValue(ProcessDetail processDetail) {
        String value = "";
        if (processDetail instanceof ProcessTextMetadata) {
            return ((ProcessTextMetadata) processDetail).getValue();
        } else if (processDetail instanceof ProcessBooleanMetadata) {
            return String.valueOf(((ProcessBooleanMetadata) processDetail).isActive());
        } else if (processDetail instanceof ProcessSelectMetadata) {
            return String.join(", ", ((ProcessSelectMetadata) processDetail).getSelectedItems());
        } else if (processDetail instanceof ProcessFieldedMetadata && processDetail.getMetadataID().equals(PERSON)) {
            value = getCreator(((ProcessFieldedMetadata) processDetail).getRows());
        }
        return value;
    }

    /**
     * Set the value of a specific process detail in processDetails.
     * @param processDetail the specific process detail whose value should be set to the param value
     *      as ProcessDetail
     * @param value
     *       as a java.lang.String
     */
    public static void setProcessDetailValue(ProcessDetail processDetail, String value) {
        if (processDetail instanceof ProcessTextMetadata) {
            // TODO: incorporate "initstart" and "initend" values from kitodo_projects.xml like AddtionalField!
            ((ProcessTextMetadata) processDetail).setValue(value);
        } else if (processDetail instanceof ProcessBooleanMetadata) {
            ((ProcessBooleanMetadata) processDetail).setActive(Boolean.parseBoolean(value));
        } else if (processDetail instanceof ProcessSelectMetadata) {
            ((ProcessSelectMetadata) processDetail).setSelectedItem(value);
        }
    }

    /**
     * Get all creators names.
     * @param processDetailsList the list of elements in processDetails
     *      as a list of processDetail
     * @return all creators names as a String
     */
    public static String getListOfCreators(List<ProcessDetail> processDetailsList) {
        String listofAuthors = "";
        for (ProcessDetail detail : processDetailsList) {
            if (detail instanceof ProcessFieldedMetadata
                    && PERSON.equals(detail.getMetadataID())) {
                ProcessFieldedMetadata tableRow = (ProcessFieldedMetadata) detail;
                for (ProcessDetail detailsTableRow : tableRow.getRows()) {
                    if (ROLE.equals(detailsTableRow.getMetadataID())
                            && AUTHOR.equals(getProcessDetailValue(detailsTableRow))) {
                        listofAuthors = listofAuthors.concat(getCreator(tableRow.getRows()));
                        break;
                    }
                }
            }
        }
        return listofAuthors;
    }

    private static String getCreator(List<ProcessDetail> processDetailList) {
        String author = "";
        for (ProcessDetail detail : processDetailList) {
            String detailMetadataID = detail.getMetadataID();
            String detailValue = getProcessDetailValue(detail);
            if ((FIRST_NAME.equals(detailMetadataID)
                    || LAST_NAME.equals(detailMetadataID))
                    && !StringUtils.isBlank(detailValue)) {
                author = author.concat(detailValue);
            }
        }
        return author;
    }

    /**
     * Prepare.
     * @param projectTitle
     *      title of the project
     * @throws IOException when trying to create a 'ConfigProject' instance.
     * @throws DoctypeMissingException when trying to load TifDefinition fails
     */
    public void prepare(String projectTitle) throws IOException, DoctypeMissingException {
        ConfigProject configProject = new ConfigProject(projectTitle);
        usingTemplates = configProject.isUseTemplates();
        tiffDefinition = configProject.getTifDefinition();
    }

    /**
     * Get useTemplate.
     *
     * @return value of useTemplate
     */
    public boolean isUsingTemplates() {
        return usingTemplates;
    }

    /**
     * Set useTemplate.
     *
     * @param usingTemplates as boolean
     */
    public void setUsingTemplates(boolean usingTemplates) {
        this.usingTemplates = usingTemplates;
    }

    /**
     * Get tiffDefinition.
     *
     * @return value of tifDefinition
     */
    public String getTiffDefinition() {
        return tiffDefinition;
    }

    /**
     * Get exemplarRecords.
     *
     * @return value of exemplarRecords
     */
    public LinkedList<ExemplarRecord> getExemplarRecords() {
        return exemplarRecords;
    }

    /**
     * Set selected exemplar record data.
     * @param exemplarRecord
     *          selected exemplar record
     * @param opac
     *          selected catalog
     * @param metadata
     *          list of metadata fields
     * @throws ParameterNotFoundException if a parameter required for exemplar record extraction is missing
     */
    public static void setSelectedExemplarRecord(ExemplarRecord exemplarRecord, String opac,
                                                 List<ProcessDetail> metadata)  throws ParameterNotFoundException {
        String ownerMetadataName = OPACConfig.getExemplarFieldOwnerMetadata(opac);
        String signatureMetadataName = OPACConfig.getExemplarFieldSignatureMetadata(opac);
        if (StringUtils.isBlank(ownerMetadataName)) {
            throw new ParameterNotFoundException("ownerMetadata");
        } else if (StringUtils.isBlank(signatureMetadataName)) {
            throw new ParameterNotFoundException("signatureMetadata");
        }
        for (ProcessDetail processDetail : metadata) {
            if (ownerMetadataName.equals(processDetail.getMetadataID())) {
                ImportService.setProcessDetailValue(processDetail, exemplarRecord.getOwner());
            } else if (signatureMetadataName.equals(processDetail.getMetadataID())) {
                ImportService.setProcessDetailValue(processDetail, exemplarRecord.getSignature());
            }
        }
    }

    /**
     * Get parentTempProcess.
     *
     * @return value of parentTempProcess
     */
    public TempProcess getParentTempProcess() {
        return parentTempProcess;
    }

    private Process loadParentProcess(HashMap<String, String> parentIDMetadata, int rulesetId, int projectId)
            throws ProcessGenerationException, DAOException {
        Process parentProcess = null;
        try {
            for (ProcessDTO processDTO : ServiceManager.getProcessService().findByMetadata(parentIDMetadata, true)) {
                Process process = ServiceManager.getProcessService().getById(processDTO.getId());
                if (Objects.isNull(process.getRuleset()) || Objects.isNull(process.getRuleset().getId())) {
                    throw new ProcessGenerationException("Ruleset or ruleset ID of potential parent process "
                            + process.getId() + " is null!");
                }
                if (process.getProject().getId() == projectId
                        && process.getRuleset().getId().equals(rulesetId)) {
                    parentProcess = process;
                    break;
                }
            }
        } catch (DataException e) {
            logger.error(e.getLocalizedMessage());
        }
        return parentProcess;
    }

    /**
     * Check and return whether 'parentElement' has been configured for OPAC with name 'catalogName'.
     *
     * @param catalogName name of the OPAC to check
     * @return whether 'parentElement has been configured or not
     * @throws ConfigException thrown if configuration for OPAC 'catalogName' could not be found
     */
    public boolean isParentElementConfigured(String catalogName) throws ConfigException {
        loadOpacConfiguration(catalogName);
        return Objects.nonNull(OPACConfig.getParentIDElement(catalogName));
    }

    /**
     * Create and return a List of ProcessDetail objects for the given TempProcess 'tempProcess'.
     *
     * @param tempProcess the TempProcess for which the List of ProcessDetail objects is created
     * @param managementInterface RulesetManagementInterface used to create the metadata of the process
     * @param acquisitionStage String containing the acquisitionStage
     * @param priorityList List of LanguageRange objects used as priority list
     * @return List of ProcessDetail objects
     * @throws InvalidMetadataValueException thrown if TempProcess contains invalid metadata
     * @throws NoSuchMetadataFieldException thrown if TempProcess contains undefined metadata
     */
    public static List<ProcessDetail> transformToProcessDetails(TempProcess tempProcess,
                                                         RulesetManagementInterface managementInterface,
                                                         String acquisitionStage,
                                                         List<Locale.LanguageRange> priorityList)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        ProcessFieldedMetadata metadata = initializeProcessDetails(tempProcess.getWorkpiece().getRootElement(),
                managementInterface, acquisitionStage, priorityList);
        metadata.setMetadata(ImportService.importMetadata(tempProcess.getMetadataNodes(), MdSec.DMD_SEC));
        metadata.preserve();
        return metadata.getRows();
    }

    /**
     * Create a process title for the given TempProcess using the provided parameters.
     *
     * @param tempProcess the TempProcess for which the TifHeader is created
     * @param rulesetManagementInterface RulesetManagementInterface used to create TifHeader
     * @param acquisitionStage String containing name of acquisitionStage
     * @param priorityList List of LanguageRange objects used as priority list
     * @param processDetails List of ProcessDetail objects containing the metadata of the process
     * @throws ProcessGenerationException thrown if generating the Process title or the TifHeader fails
     */
    public static void createProcessTitle(TempProcess tempProcess,
                                            RulesetManagementInterface rulesetManagementInterface,
                                            String acquisitionStage, List<Locale.LanguageRange> priorityList,
                                            List<ProcessDetail> processDetails)
            throws ProcessGenerationException {
        String docType = tempProcess.getWorkpiece().getRootElement().getType();
        StructuralElementViewInterface docTypeView = rulesetManagementInterface
                .getStructuralElementView(docType, acquisitionStage, priorityList);
        String processTitle = docTypeView.getProcessTitle().orElse("");
        ProcessService.generateProcessTitle("", processDetails,
                processTitle, tempProcess.getProcess());
    }

    /**
     * Create and return an instance of 'ProcessFieldedMetadata' for the given IncludedStructuralElement 'structure',
     * RulesetManagementInterface 'managementInterface', acquisition stage String 'stage' and List of LanguageRange
     * 'priorityList'.
     *
     * @param structure IncludedStructuralElement for which to create a ProcessFieldedMetadata
     * @param managementInterface RulesetManagementInterface used to create ProcessFieldedMetadata
     * @param stage String containing acquisition stage used to create ProcessFieldedMetadata
     * @param priorityList List of LanguageRange objects used to create ProcessFieldedMetadata
     * @return the created ProcessFieldedMetadata
     */
    public static ProcessFieldedMetadata initializeProcessDetails(IncludedStructuralElement structure,
                                                                  RulesetManagementInterface managementInterface,
                                                                  String stage,
                                                                  List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface divisionView = managementInterface.getStructuralElementView(structure.getType(),
                stage, priorityList);
        return new ProcessFieldedMetadata(structure, divisionView);
    }

    /**
     * Ensure all processes in given list 'tempProcesses' have a non empty title.
     *
     * @param tempProcesses list of TempProcesses to be checked
     * @return whether a title was changed or not
     * @throws IOException if the meta.xml file of a process could not be loaded
     */
    public static boolean ensureNonEmptyTitles(LinkedList<TempProcess> tempProcesses) throws IOException {
        boolean changedTitle = false;
        for (TempProcess tempProcess : tempProcesses) {
            Process process = tempProcess.getProcess();
            if (Objects.nonNull(process) && StringUtils.isEmpty(process.getTitle())) {
                // FIXME:
                //  if metadataFileUri is null or no meta.xml can be found, the tempProcess has not
                //  yet been saved to disk and contains the workpiece directly, instead!
                URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(process);
                Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);
                Collection<Metadata> metadata = workpiece.getRootElement().getMetadata();
                String processTitle = "[" + Helper.getTranslation("process") + " " + process.getId() + "]";
                for (Metadata metadatum : metadata) {
                    if (CATALOG_IDENTIFIER.equals(metadatum.getKey())) {
                        processTitle = ((MetadataEntry) metadatum).getValue();
                    }
                }
                process.setTitle(processTitle);
                changedTitle = true;
            }
        }
        return changedTitle;
    }

    /**
     * Process list of child processes.
     *
     * @param mainProcess main process to which list of child processes are attached
     * @param childProcesses list of child processes that are attached to the main process
     * @throws DataException thrown if saving a process fails
     * @throws InvalidMetadataValueException thrown if process workpiece contains invalid metadata
     * @throws NoSuchMetadataFieldException thrown if process workpiece contains undefined metadata
     * @throws ProcessGenerationException thrown if process title cannot be created
     */
    public static void processProcessChildren(Process mainProcess, LinkedList<TempProcess> childProcesses,
                                              Template template, RulesetManagementInterface managementInterface,
                                              String acquisitionStage, List<Locale.LanguageRange> priorityList)
            throws DataException, InvalidMetadataValueException, NoSuchMetadataFieldException,
            ProcessGenerationException {
        for (TempProcess tempProcess : childProcesses) {
            if (Objects.isNull(tempProcess) || Objects.isNull(tempProcess.getProcess())) {
                logger.error("Child process " + (childProcesses.indexOf(tempProcess) + 1) + " is null => Skip!");
                continue;
            }
            processTempProcess(tempProcess, template, managementInterface, acquisitionStage, priorityList);
            Process childProcess = tempProcess.getProcess();
            ServiceManager.getProcessService().save(childProcess);
            ProcessService.setParentRelations(mainProcess, childProcess);
        }
    }

    /**
     * Add workpiece and template properties to given Process 'process'.
     *
     * @param process Process to which properties are added
     * @param template Template of process
     * @param processDetails metadata of process
     * @param docType String containing document type
     * @param imageDescription String containing image description
     */
    public static void addProperties(Process process, Template template, List<ProcessDetail> processDetails,
                                     String docType, String imageDescription) {
        addMetadataProperties(processDetails, process);
        ProcessGenerator.addPropertyForWorkpiece(process, "DocType", docType);
        ProcessGenerator.addPropertyForWorkpiece(process, "TifHeaderImagedescription", imageDescription);
        ProcessGenerator.addPropertyForWorkpiece(process, "TifHeaderDocumentname", process.getTitle());
        if (Objects.nonNull(template)) {
            ProcessGenerator.addPropertyForProcess(process, "Template", template.getTitle());
            ProcessGenerator.addPropertyForProcess(process, "TemplateID", String.valueOf(template.getId()));
        }
    }

    private static void addMetadataProperties(List<ProcessDetail> processDetailList, Process process) {
        try {
            for (ProcessDetail processDetail : processDetailList) {
                Collection<Metadata> processMetadata = processDetail.getMetadata();
                if (!processMetadata.isEmpty() && processMetadata.toArray()[0] instanceof Metadata) {
                    String metadataValue = ImportService.getProcessDetailValue(processDetail);
                    Metadata metadata = (Metadata) processMetadata.toArray()[0];
                    if (Objects.nonNull(metadata.getDomain())) {
                        switch (metadata.getDomain()) {
                            case DMD_SEC:
                                ProcessGenerator.addPropertyForWorkpiece(process, processDetail.getLabel(), metadataValue);
                                break;
                            case SOURCE_MD:
                                ProcessGenerator.addPropertyForTemplate(process, processDetail.getLabel(), metadataValue);
                                break;
                            case TECH_MD:
                                ProcessGenerator.addPropertyForProcess(process, processDetail.getLabel(), metadataValue);
                                break;
                            default:
                                logger.info("Don't save metadata '" + processDetail.getMetadataID() + "' with domain '"
                                        + metadata.getDomain() + "' to property.");
                                break;
                        }
                    } else {
                        ProcessGenerator.addPropertyForWorkpiece(process, processDetail.getLabel(), metadataValue);
                    }
                }
            }
        } catch (InvalidMetadataValueException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Update tasks of given Process 'process'.
     *
     * @param process Process whose tasks are updated
     */
    public static void updateTasks(Process process) {
        for (Task task : process.getTasks()) {
            task.setProcessingTime(process.getCreationDate());
            task.setEditType(TaskEditType.AUTOMATIC);
            if (task.getProcessingStatus() == TaskStatus.DONE) {
                task.setProcessingBegin(process.getCreationDate());
                Date date = new Date();
                task.setProcessingTime(date);
                task.setProcessingEnd(date);
            }
        }
    }

    /**
     * Process given TempProcess 'tempProcess' by creating the metadata, doc type and properties for the process and
     * updating the process' tasks.
     *
     * @param tempProcess TempProcess that will be processed
     * @param template Template of the process
     * @param managementInterface RulesetManagementInterface to create metadata and tiff header
     * @param acquisitionStage String containing the acquisition stage
     * @param priorityList List of LanguageRange objects
     * @throws InvalidMetadataValueException thrown if the process contains invalid metadata
     * @throws NoSuchMetadataFieldException thrown if the process contains undefined metadata
     * @throws ProcessGenerationException thrown if process title could not be generated
     */
    public static void processTempProcess(TempProcess tempProcess, Template template,
                                          RulesetManagementInterface managementInterface, String acquisitionStage,
                                          List<Locale.LanguageRange> priorityList)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException, ProcessGenerationException {
        List<ProcessDetail> processDetails = transformToProcessDetails(tempProcess, managementInterface,
                acquisitionStage, priorityList);
        String docType = tempProcess.getWorkpiece().getRootElement().getType();
        createProcessTitle(tempProcess, managementInterface, acquisitionStage, priorityList, processDetails);
        Process process = tempProcess.getProcess();
        addProperties(tempProcess.getProcess(), template, processDetails, docType, tempProcess.getProcess().getTitle());
        updateTasks(process);
    }

    /**
     * Opens the ruleset with the given fileName
     * @param fileName the filname of the rulesetfile.
     * @return an open ruleset
     */
    public RulesetManagementInterface openRulesetFile(String fileName) throws IOException, RulesetNotFoundException {
        final long begin = System.nanoTime();
        String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
        Locale.LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        try {
            ruleset.load(new File(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_RULESETS), fileName).toString()));
        } catch (FileNotFoundException e) {
            List<String> param = new ArrayList<>();
            param.add(fileName);
            throw new RulesetNotFoundException(Helper.getTranslation("rulesetNotFound", param));
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Reading ruleset took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return ruleset;
    }

    /**
     * Imports a process and saves it to database.
     * @param ppn the ppn to import
     * @param projectId the projectId
     * @param templateId the templateId
     * @param selectedCatalog the selected catalog to import from
     * @return the importedProcess
     */
    public Process importProcess(String ppn, int projectId, int templateId, String selectedCatalog) throws SAXException, NoRecordFoundException, UnsupportedFormatException, IOException, XPathExpressionException, URISyntaxException, ParserConfigurationException, ProcessGenerationException, DataException, DAOException, RulesetNotFoundException, InvalidMetadataValueException, NoSuchMetadataFieldException {
        LinkedList<TempProcess> processList = new LinkedList<>();
        Template template = ServiceManager.getTemplateService().getById(templateId);
        String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
        List<Locale.LanguageRange> priorityList = Locale.LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
        importProcessAndReturnParentID(ppn,processList,selectedCatalog,projectId,templateId);
        processTempProcess(processList.get(0), template, openRulesetFile(template.getRuleset().getFile()), "create", priorityList);
        ServiceManager.getProcessService().save(processList.get(0).getProcess());
        return processList.get(0).getProcess();
    }
}

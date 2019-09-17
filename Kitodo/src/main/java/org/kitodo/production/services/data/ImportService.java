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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.SchemaConverterInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.OPACConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.forms.createprocess.ProcessBooleanMetadata;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.forms.createprocess.ProcessSelectMetadata;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
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
    private static final String PARENT_XPATH = "//kitodo:metadata[@name='CatalogIDPredecessorPeriodical']";
    private static final String PARENTHESIS_TRIM_MODE = "parenthesis";
    private String trimMode = "";

    private static final String PERSON = "Person";
    private static final String ROLE = "Role";
    private static final String AUTHOR = "Author";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";

    private static final String MONOGRAPH = "Monograph";
    private static final String VOLUME = "Volume";
    private static final String MULTI_VOLUME_WORK = "MultiVolumeWork";

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
        NodeList nodeList = (NodeList) parentIDXpath.compile(PARENT_XPATH)
                .evaluate(document, XPathConstants.NODESET);
        if (nodeList.getLength() != 1) {
            return null;
        } else {
            Node parentIDNode = nodeList.item(0);
            if (PARENTHESIS_TRIM_MODE.equals(trimMode)) {
                return parentIDNode.getTextContent().replaceAll("\\([^)]+\\)", "");
            } else {
                return parentIDNode.getTextContent();
            }
        }
    }

    private void setParentRelations(Process parentProcess, Process childProcess) {
        childProcess.setParent(parentProcess);
        parentProcess.getChildren().add(childProcess);
    }

    private String importProcessAndReturnParentID(String id, LinkedList<TempProcess> allProcesses,
                                                  CreateProcessForm createProcessForm)
            throws IOException, ProcessGenerationException, XPathExpressionException, ParserConfigurationException,
            NoRecordFoundException, UnsupportedFormatException, URISyntaxException, SAXException {

        String opac = createProcessForm.getImportTab().getHitModel().getSelectedCatalog();

        DataRecord internalRecord = importRecord(opac, id);
        if (!(internalRecord.getOriginalData() instanceof String)) {
            throw new UnsupportedFormatException("Original metadata of internal record has to be an XML String, '"
                    + internalRecord.getOriginalData().getClass().getName() + "' found!");
        }

        Document internalDocument = XMLUtils.parseXMLString((String)internalRecord.getOriginalData());
        String docType = getRecordDocType(internalDocument);

        // Workaround for classifying MultiVolumeWorks with insufficient information
        if (!allProcesses.isEmpty()) {
            String childDocType = allProcesses.getLast().getWorkpiece().getRootElement().getType();
            if ((MONOGRAPH.equals(childDocType) || VOLUME.equals(childDocType)) && MONOGRAPH.equals(docType)) {
                docType = MULTI_VOLUME_WORK;
                allProcesses.getFirst().getWorkpiece().getRootElement().setType(VOLUME);
            }
        }

        NodeList metadataNodes = extractMetadataNodeList(internalDocument);

        int projectID = createProcessForm.getProject().getId();
        int templateID = createProcessForm.getTemplate().getId();

        Process process = null;
        if (processGenerator.generateProcess(templateID, projectID)) {
            process = processGenerator.getGeneratedProcess();
        }

        TempProcess tempProcess = new TempProcess(process, metadataNodes, docType);

        // skip setting relations to parent process if this is the first imported process of the hierarchy!
        if (!allProcesses.isEmpty()) {
            setParentRelations(tempProcess.getProcess(), allProcesses.getLast().getProcess());
        }
        allProcesses.add(tempProcess);
        return getParentID(internalDocument);
    }

    /**
     * Import a process identified by the given ID 'id' using the given CreateProcessForm 'createProcessForm'.
     * Additionally, import all ancestors of the given process referenced in the original data of the process imported
     * from the OPAC selected in the given CreateProcessForm instance.
     * Return the list of processes as a LinkedList of TempProcess.
     *
     * @param id identifier of the process to import
     * @param createProcessForm CreateProcessForm instance containing import configuration
     * @return List of TempProcess
     */
    public LinkedList<TempProcess> importProcessHierarchy(String id, CreateProcessForm createProcessForm,
                                                          int importDepth)
            throws IOException, ProcessGenerationException, XPathExpressionException, ParserConfigurationException,
            NoRecordFoundException, UnsupportedFormatException, URISyntaxException, SAXException {

        importModule = initializeImportModule();
        processGenerator = new ProcessGenerator();
        LinkedList<TempProcess> processes = new LinkedList<>();
        String parentID = importProcessAndReturnParentID(id, processes, createProcessForm);
        int level = 1;
        while (Objects.nonNull(parentID) && level < importDepth) {
            try {
                parentID = importProcessAndReturnParentID(parentID, processes, createProcessForm);
                level++;
            } catch (SAXParseException e) {
                // this happens for example if a document is part of a "Virtueller Bestand" in Kalliope for which a
                // proper "record" is not returned from its SRU interface
                logger.error(e.getLocalizedMessage());
                break;
            }
        }
        return processes;
    }

    private DataRecord importRecord(String opac, String identifier) throws NoRecordFoundException,
            UnsupportedFormatException, URISyntaxException, IOException {
        // ################ IMPORT #################
        importModule = initializeImportModule();
        DataRecord dataRecord = importModule.getFullRecordById(opac, identifier);

        // ################# CONVERT ################
        // depending on metadata and return form, call corresponding schema converter module!
        SchemaConverterInterface converter = getSchemaConverter(dataRecord);

        // transform dataRecord to Kitodo internal format using appropriate SchemaConverter!
        URI xsltFile = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)).toUri()
                .resolve(new URI(OPACConfig.getXsltMappingFile(opac)));
        return converter.convert(dataRecord, MetadataFormat.KITODO, FileFormat.XML,
                ServiceManager.getFileService().getFile(xsltFile));
    }

    private NodeList extractMetadataNodeList(Document document) throws ProcessGenerationException {
        NodeList kitodoNodes = document.getElementsByTagNameNS(KITODO_NAMESPACE, KITODO_STRING);
        if (kitodoNodes.getLength() != 1) {
            throw new ProcessGenerationException("Number of 'kitodo' nodes unequal to '1' => unable to generate process!");
        }
        Node kitodoNode = kitodoNodes.item(0);
        return kitodoNode.getChildNodes();
    }

    /**
     * Fill elements of given List of ProcessDetails 'processDetails' with values of given NodeList 'nodes'.
     *
     * @param processDetails List of ProcessDetail instances whose values are set
     * @param nodes NodeList used to fill set values of given ProcessDetail instances
     */
    public static void fillProcessDetails(ProcessFieldedMetadata processDetails, NodeList nodes,
                                   RulesetManagementInterface ruleset, String docType, String stage,
                                   List<Locale.LanguageRange> languages) {
        fillProcessDetailsElements(processDetails.getRows(), processDetails, nodes, ruleset, docType, stage, languages,
                false);
    }

    private static void fillProcessDetailsElements(List<ProcessDetail> processDetailList,
                                                   ProcessFieldedMetadata topLevelMetadata, NodeList nodes,
                                                   RulesetManagementInterface ruleset, String docType, String stage,
                                                   List<Locale.LanguageRange> languages, boolean isChild) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            boolean filled = false;
            for (ProcessDetail detail : processDetailList) {
                if (Objects.nonNull(detail.getMetadataID()) && detail.getMetadataID().equals(nodeName)) {
                    if (isChild || StringUtils.isBlank(getProcessDetailValue(detail))) {
                        filled = true;
                        if (node.getLocalName().equals("metadataGroup")
                                && detail instanceof ProcessFieldedMetadata) {
                            fillProcessDetailsElements(((ProcessFieldedMetadata) detail).getRows(), topLevelMetadata,
                                    element.getChildNodes(), ruleset, docType, stage, languages, true);
                        } else if (node.getLocalName().equals("metadata")) {
                            setProcessDetailValue(detail, element.getTextContent());
                        }
                    }
                    break;
                }
            }
            if (!filled) {

                try {
                    ProcessDetail newDetail = addProcessDetail(nodeName, docType, stage, languages, ruleset, topLevelMetadata);
                    topLevelMetadata.getRows().add(newDetail);
                    if (newDetail instanceof ProcessFieldedMetadata) {
                        fillProcessDetailsElements(((ProcessFieldedMetadata) newDetail).getRows(), topLevelMetadata,
                                element.getChildNodes(), ruleset, docType, stage, languages,true);
                    } else {
                        setProcessDetailValue(newDetail, element.getTextContent());
                    }
                } catch (NoSuchMetadataFieldException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    /**
     *  Get the value of a specific processDetail in the processDetails.
     * @param processDetail
     *      as ProcessDetail
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
     * @param processDetail the specific process detail that its value want to be modified
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
     * Add a new process detail to processDetails.
     * @param metadataId
     *              the key of the metadata want to be added
     * @return the added process detail as a ProcessDetail.
     *
     * @throws NoSuchMetadataFieldException
     *          if the metadataId is not allowed as a Metadata for the doctype.
     */
    private static ProcessDetail addProcessDetail(String metadataId, String docType, String stage,
                                                  List<Locale.LanguageRange> languageRanges,
                                                  RulesetManagementInterface ruleset, ProcessFieldedMetadata metadata)
            throws NoSuchMetadataFieldException {
        Collection<MetadataViewInterface> docTypeAddableDivisions = ruleset.getStructuralElementView(docType, stage,
                languageRanges).getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

        List<MetadataViewInterface> filteredViews = docTypeAddableDivisions
                .stream()
                .filter(metadataView -> metadataView.getId().equals(metadataId))
                .collect(Collectors.toList());

        if (!filteredViews.isEmpty()) {
            if (filteredViews.get(0).isComplex()) {
                return metadata.createMetadataGroupPanel((ComplexMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());
            } else {
                return metadata.createMetadataEntryEdit((SimpleMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());
            }
        }
        throw new NoSuchMetadataFieldException(metadataId, "");
    }

    /**
     * get all creators names .
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
}

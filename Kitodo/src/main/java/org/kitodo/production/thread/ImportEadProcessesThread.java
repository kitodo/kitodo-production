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

package org.kitodo.production.thread;

import static org.kitodo.constants.StringConstants.CREATE;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.constants.StringConstants;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.createprocess.CreateProcessForm;
import org.kitodo.production.helper.ProcessHelper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class ImportEadProcessesThread extends EmptyTask {

    private static final Logger logger = LogManager.getLogger(ImportEadProcessesThread.class);
    private final ImportService importService = ServiceManager.getImportService();
    private final String xmlString;
    private final String eadLevel;
    private final String eadParentLevel;
    private final int projectId;
    private final int templateId;
    private final ImportConfiguration importConfiguration;
    private final RulesetManagementInterface rulesetManagementInterface;
    private final List<Locale.LanguageRange> priorityList;
    private final List<Namespace> namespaces;
    private final User user;
    private final Client client;
    private TempProcess parentProcess = null;
    private int count;

    /**
     * Standard constructor, creating instance of ImportEadProcessesThread with settings from given
     * CreateProcessForm 'createProcessForm' and for given User 'user' and Client 'client'.
     *
     * @param createProcessForm CreateProcessForm instance encapsulating various settings required for this thread
     * @param user User instance to which this thread is assigned
     * @param client Client instance of current user
     */
    public ImportEadProcessesThread(CreateProcessForm createProcessForm, User user, Client client) {
        super(createProcessForm.getFilename());
        this.xmlString = createProcessForm.getXmlString();
        this.eadLevel = createProcessForm.getSelectedEadLevel();
        this.eadParentLevel = createProcessForm.getSelectedParentEadLevel();
        this.projectId = createProcessForm.getProject().getId();
        this.templateId = createProcessForm.getTemplate().getId();
        this.importConfiguration = createProcessForm.getCurrentImportConfiguration();
        this.rulesetManagementInterface = createProcessForm.getRulesetManagement();
        this.priorityList = ServiceManager.getUserService().getCurrentMetadataLanguage();
        this.namespaces = new ArrayList<>();
        this.user = user;
        this.client = client;
    }

    @Override
    protected void setNameDetail(String detail) {
        super.setNameDetail(detail);
    }

    @Override
    public void run() {
        setAuthenticatedUser();
        List<Integer> newProcessIds = new ArrayList<>();
        int newParentId = 0;
        boolean stopOnError = ConfigCore.getBooleanParameter(ParameterCore.STOP_EAD_COLLECTION_IMPORT_ON_EXCEPTION);
        try {
            int numberOfElements = XMLUtils.getNumberOfEADElements(xmlString, eadLevel);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(xmlString));
            boolean inProcessElement = false;
            boolean inParentProcessElement = false;
            String currentTagName;
            StringBuilder stringBuilder = new StringBuilder();
            while (eventReader.hasNext()) {

                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLEvent.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        currentTagName = startElement.getName().getLocalPart();
                        if (StringConstants.EAD.equals(currentTagName)) {
                            Iterator<Namespace> namespaceIterator = startElement.getNamespaces();
                            while (namespaceIterator.hasNext()) {
                                namespaces.add(namespaceIterator.next());
                            }
                        }
                        if (StringConstants.C_TAG_NAME.equals(currentTagName)) {
                            // all metadata until first nested "<c>" element is considered when creating parent process
                            // for EAD "collection"
                            if (inParentProcessElement) {
                                inParentProcessElement = false;
                                stringBuilder.append("</c>");
                                TempProcess tempProcess = parseXmlStringToTempProcess(stringBuilder.toString(), true);
                                TempProcess existingParent = getParentCandidate(tempProcess);
                                // distinguish between existing "collection" parent and newly created one
                                if (Objects.isNull(existingParent)) {
                                    parentProcess = processTempProcess(tempProcess);
                                    newParentId = parentProcess.getProcess().getId();
                                } else {
                                    parentProcess = existingParent;
                                }
                                stringBuilder = new StringBuilder();
                            }
                            Attribute levelAttribute = startElement.getAttributeByName(QName.valueOf(StringConstants
                                    .LEVEL));
                            Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                            if (Objects.nonNull(levelAttribute) && Objects.nonNull(idAttribute)) {
                                if (eadLevel.equals(levelAttribute.getValue())) {
                                    inProcessElement = true;
                                    count++;
                                    stringBuilder.append(processStartElement(event));
                                } else {
                                    if (eadParentLevel.equals(levelAttribute.getValue())) {
                                        inParentProcessElement = true;
                                        stringBuilder.append(processStartElement(event));
                                    }
                                }
                            }
                        } else {
                            if (inParentProcessElement || inProcessElement) {
                                String content = event.toString();
                                if (StringUtils.isNotBlank(content)) {
                                    stringBuilder.append(removeDefaultNamespaceUri(content));
                                }
                            }
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        EndElement endElement = event.asEndElement();
                        String endElementName = endElement.getName().getLocalPart();
                        if (inProcessElement && StringConstants.C_TAG_NAME.equals(endElementName)) {
                            int progress = (count * 100) / numberOfElements;
                            setProgress(progress);
                            inProcessElement = false;
                            String content = event.toString();
                            stringBuilder.append(removeDefaultNamespaceUri(content));
                            try {
                                newProcessIds.add(parseXmlStringToProcessedTempProcess(stringBuilder.toString()).getProcess().getId());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                if (stopOnError) {
                                    throw new ProcessGenerationException("Unable to create process. Cause: " + e.getMessage());
                                }
                            }
                            stringBuilder = new StringBuilder();
                        } else {
                            if (inParentProcessElement || inProcessElement) {
                                String content = event.toString();
                                if (StringUtils.isNotBlank(content)) {
                                    stringBuilder.append(removeDefaultNamespaceUri(content));
                                }
                            }
                        }
                        break;
                    default:
                        if (inParentProcessElement || inProcessElement) {
                            String content = event.toString();
                            if (StringUtils.isNotBlank(content)) {
                                stringBuilder.append(removeDefaultNamespaceUri(content));
                            }
                        }
                        break;
                }
            }

        } catch (XMLStreamException | IOException | ParserConfigurationException | SAXException
                 | UnsupportedFormatException | XPathExpressionException | ProcessGenerationException
                 | URISyntaxException | InvalidMetadataValueException | TransformerException
                 | NoSuchMetadataFieldException | DAOException | CommandException | FileStructureValidationException e) {
            logger.error(e.getMessage(), e);
            cleanUpProcesses(newProcessIds, newParentId);
            throw new RuntimeException(e);
        }
    }

    private void cleanUpProcesses(List<Integer> processIds, int newParentId) {
        logger.info("Deleting processes created until this point to resolve erroneous intermediate state.");
        // cleanup any processes that might have been created until this point
        for (int id : processIds) {
            try {
                ProcessService.deleteProcess(ServiceManager.getProcessService().getById(id));
            } catch (DAOException | IOException | SAXException | FileStructureValidationException ex) {
                logger.error(ex);
            }
        }
        if (newParentId > 0 && Objects.nonNull(parentProcess) && Objects.nonNull(parentProcess.getProcess())) {
            try {
                ProcessService.deleteProcess(parentProcess.getProcess());
            } catch (DAOException | IOException | SAXException | FileStructureValidationException ex) {
                logger.error(ex);
            }
        }
    }

    private String processStartElement(XMLEvent event) {
        String content = event.toString();
        for (Namespace namespace : namespaces) {
            String prefix = namespace.getPrefix();
            if (StringUtils.isNotBlank(prefix)) {
                content = content.replace(">", " xmlns:" + namespace.getPrefix() + "=\""
                        + namespace.getNamespaceURI() + "\">");
            } else {
                content = content.replace(">", " xmlns=\"" + namespace.getNamespaceURI() + "\">");
            }
        }
        return removeDefaultNamespaceUri(content);
    }

    // This method is a workaround for a problem encountered during development of the EAD import where XML files
    // defining a default names space without namespace prefix could be parsed incorrectly.
    private String removeDefaultNamespaceUri(String xmlEventString) {
        for (Namespace namespace : namespaces) {
            String prefix = namespace.getPrefix();
            if (StringUtils.isBlank(prefix)) {
                return xmlEventString.replace("['" + namespace.getNamespaceURI() + "']::", "");
            }
        }
        return xmlEventString;
    }

    private TempProcess getParentCandidate(TempProcess tempProcess) {
        String recordId = getRecordIdentifier(tempProcess);
        if (Objects.nonNull(recordId)) {
            TempProcess parent = ServiceManager.getImportService().retrieveParentTempProcess(recordId,
                    tempProcess.getProcess().getRuleset(), projectId);
            if (Objects.nonNull(parent)) {
                return parent;
            }
        }
        return null;
    }

    private TempProcess processTempProcess(TempProcess tempProcess) throws ProcessGenerationException, IOException,
            InvalidMetadataValueException, NoSuchMetadataFieldException, DAOException, CommandException, SAXException,
            FileStructureValidationException {
        ProcessHelper.generateAtstslFields(tempProcess, Collections.emptyList(), CREATE, priorityList, false);
        tempProcess.getProcessMetadata().preserve();
        ImportService.processTempProcess(tempProcess, rulesetManagementInterface, CREATE, priorityList, parentProcess);
        saveTempProcessMetadata(tempProcess);
        if (Objects.nonNull(parentProcess)) {
            ProcessService.setParentRelations(parentProcess.getProcess(), tempProcess.getProcess());
            MetadataEditor.addLink(parentProcess.getProcess(), String.valueOf(count - 1), tempProcess.getProcess()
                    .getId());
            ServiceManager.getProcessService().save(tempProcess.getProcess());
        }
        return tempProcess;
    }

    // used to parse parent (e.g. "collection")
    private TempProcess parseXmlStringToTempProcess(String xmlString, boolean isParent) throws IOException,
            ParserConfigurationException, SAXException, UnsupportedFormatException, XPathExpressionException,
            ProcessGenerationException, URISyntaxException, TransformerException, FileStructureValidationException {
        Document elementDocument = XMLUtils.parseXMLString(xmlString);
        Element element = elementDocument.getDocumentElement();
        return importService.createTempProcessFromElement(element, importConfiguration, projectId, templateId, isParent,
                ImportConfigurationType.OPAC_SEARCH.name().equals(importConfiguration.getConfigurationType()));
    }

    // used to parse children (e.g. "files")
    private TempProcess parseXmlStringToProcessedTempProcess(String xmlElementString) throws IOException,
            ParserConfigurationException, SAXException, UnsupportedFormatException, XPathExpressionException,
            ProcessGenerationException, URISyntaxException, InvalidMetadataValueException, TransformerException,
            NoSuchMetadataFieldException, DAOException, CommandException, FileStructureValidationException {
        TempProcess tempProcess = parseXmlStringToTempProcess(xmlElementString, false);
        return processTempProcess(tempProcess);
    }

    private String getRecordIdentifier(TempProcess tempProcess) {
        Collection<String> recordIdMetadata = rulesetManagementInterface
                .getFunctionalKeys(FunctionalMetadata.RECORD_IDENTIFIER);
        for (String recordId : recordIdMetadata) {
            for (Metadata metadata : tempProcess.getWorkpiece().getLogicalStructure().getMetadata()) {
                if (metadata instanceof MetadataEntry && recordId.equals(metadata.getKey())) {
                    return ((MetadataEntry) metadata).getValue();
                }
            }
        }
        return null;
    }

    private void setAuthenticatedUser() {
        SecurityUserDetails securityUserDetails = new SecurityUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(securityUserDetails, null,
                securityUserDetails.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityUserDetails.setSessionClient(client);
        securityContext.setAuthentication(authentication);
    }

    private void saveTempProcessMetadata(TempProcess tempProcess) throws DAOException, IOException, CommandException {
        ServiceManager.getProcessService().save(tempProcess.getProcess());
        URI processBaseUri = ServiceManager.getFileService().createProcessLocation(tempProcess.getProcess());
        tempProcess.getProcess().setProcessBaseUri(processBaseUri);
        ProcessHelper.saveTempProcessMetadata(tempProcess, rulesetManagementInterface, CREATE, priorityList);
    }
}

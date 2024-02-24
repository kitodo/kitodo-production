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

package org.kitodo.production.helper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.process.TiffHeaderGenerator;
import org.kitodo.production.process.TitleGenerator;
import org.kitodo.production.services.ServiceManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProcessHelper {

    private static final Logger logger = LogManager.getLogger(ProcessHelper.class);

    /**
     * Create and return a List of ProcessDetail objects for the given TempProcess
     * 'tempProcess'.
     *
     * @param tempProcess
     *            the TempProcess for which the List of ProcessDetail objects is
     *            created
     * @param rulesetManagement
     *            Ruleset management used to create the metadata of the process
     * @param acquisitionStage
     *            String containing the acquisitionStage
     * @param priorityList
     *            List of LanguageRange objects used as priority list
     * @return List of ProcessDetail objects
     * @throws InvalidMetadataValueException
     *             thrown if TempProcess contains invalid metadata
     * @throws NoSuchMetadataFieldException
     *             thrown if TempProcess contains undefined metadata
     */
    public static List<ProcessDetail> transformToProcessDetails(TempProcess tempProcess,
            RulesetManagementInterface rulesetManagement, String acquisitionStage,
            List<Locale.LanguageRange> priorityList)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        ProcessFieldedMetadata metadata = initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
            rulesetManagement, acquisitionStage, priorityList);
        metadata.preserve();
        return metadata.getRows();
    }

    /**
     * Create and return an instance of 'ProcessFieldedMetadata' for the given
     * LogicalDivision 'structure', RulesetManagementInterface
     * 'managementInterface', acquisition stage String 'stage' and List of
     * LanguageRange 'priorityList'.
     *
     * @param structure
     *            LogicalDivision for which to create a ProcessFieldedMetadata
     * @param rulesetManagement
     *            Ruleset management used to create ProcessFieldedMetadata
     * @param stage
     *            String containing acquisition stage used to create
     *            ProcessFieldedMetadata
     * @param priorityList
     *            List of LanguageRange objects used to create
     *            ProcessFieldedMetadata
     * @return the created ProcessFieldedMetadata
     */
    public static ProcessFieldedMetadata initializeProcessDetails(LogicalDivision structure,
            RulesetManagementInterface rulesetManagement, String stage, List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface divisionView = rulesetManagement.getStructuralElementView(structure.getType(),
                stage, priorityList);
        return new ProcessFieldedMetadata(structure, divisionView, rulesetManagement);
    }

    /**
     * Generates TSL/ATS dependent fields of temp process.
     *
     * @param tempProcess
     *         the temp process to generate TSL/ATS dependent fields
     * @param processDetails
     *         the process details of temp process
     * @param parentTempProcesses
     *         the parent temp processes
     * @param docType
     *         current division
     * @param rulesetManagement
     *         current ruleset management
     * @param acquisitionStage
     *         current acquisition level
     * @param priorityList
     *         weighted list of user-preferred display languages
     * @throws ProcessGenerationException
     *         thrown if process title cannot be created
     */
    public static void generateAtstslFields(TempProcess tempProcess, List<ProcessDetail> processDetails,
            List<TempProcess> parentTempProcesses, String docType,
            RulesetManagementInterface rulesetManagement, String acquisitionStage,
            List<Locale.LanguageRange> priorityList) throws ProcessGenerationException {
        generateAtstslFields(tempProcess, processDetails, parentTempProcesses, docType, rulesetManagement, acquisitionStage,
                priorityList, null, false);
    }

    /**
     * Generates TSL/ATS dependent fields of temp process.
     *
     * @param tempProcess
     *         the temp process to generate TSL/ATS dependent fields
     * @param parentTempProcesses
     *         the parent temp processes
     * @param acquisitionStage
     *         current acquisition level
     * @param force
     *         force regeneration atstsl fields if process title already exists
     * @throws ProcessGenerationException
     *         thrown if process title cannot be created
     * @throws InvalidMetadataValueException
     *         thrown if process workpiece contains invalid metadata
     * @throws NoSuchMetadataFieldException
     *         thrown if process workpiece contains undefined metadata
     * @throws IOException
     *         thrown if ruleset file cannot be loaded
     */
    public static void generateAtstslFields(TempProcess tempProcess, List<TempProcess> parentTempProcesses,
            String acquisitionStage, boolean force)
            throws ProcessGenerationException, InvalidMetadataValueException, NoSuchMetadataFieldException,
            IOException {
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetService()
                .openRuleset(tempProcess.getProcess().getRuleset());
        List<Locale.LanguageRange> priorityList = ServiceManager.getUserService().getCurrentMetadataLanguage();
        String docType = tempProcess.getWorkpiece().getLogicalStructure().getType();
        List<ProcessDetail> processDetails = transformToProcessDetails(tempProcess, rulesetManagement,
                acquisitionStage, priorityList);
        generateAtstslFields(tempProcess, processDetails, parentTempProcesses, docType, rulesetManagement,
                acquisitionStage, priorityList, null, force);
    }

    /**
     * Generates TSL/ATS dependent fields of temp process.
     *
     * @param tempProcess
     *            the temp process to generate TSL/ATS dependent fields
     * @param processDetails
     *            the process details of temp process
     * @param parentTempProcesses
     *            the parent temp processes of temp process
     * @param docType
     *            current division or docType to get the title definition from
     * @param rulesetManagement
     *            the ruleset management
     * @param acquisitionStage
     *            current acquisition level
     * @param priorityList
     *            weighted list of user-preferred display languages
     * @param parentProcess
     *            the process of the selected title record
     * @param force
     *            force regeneration atstsl fields if process title already exists
     * @throws ProcessGenerationException
     *             thrown if process title cannot be created
     */
    public static void generateAtstslFields(TempProcess tempProcess, List<ProcessDetail> processDetails,
            List<TempProcess> parentTempProcesses, String docType,
            RulesetManagementInterface rulesetManagement, String acquisitionStage,
            List<Locale.LanguageRange> priorityList, Process parentProcess, boolean force)
            throws ProcessGenerationException {
        if (!shouldGenerateAtstslFields(tempProcess) && !force) {
            return;
        }

        String titleDefinition = getTitleDefinition(rulesetManagement, docType, acquisitionStage,
                priorityList);
        String currentTitle = TitleGenerator.getValueOfMetadataID(TitleGenerator.TITLE_DOC_MAIN, processDetails);
        if (StringUtils.isBlank(currentTitle)) {
            if (Objects.nonNull(parentProcess)) {
                if (titleDefinition.startsWith("+")) {
                    titleDefinition = '\'' + parentProcess.getTitle() + '\'' + titleDefinition;
                }
                currentTitle = getTitleFromWorkpiece(parentProcess);
            } else if (Objects.nonNull(parentTempProcesses)) {
                currentTitle = getTitleFromParents(parentTempProcesses, rulesetManagement, acquisitionStage,
                        priorityList);
            }
        }

        tempProcess.setAtstsl(
                generateProcessTitleAndGetAtstsl(processDetails, titleDefinition, tempProcess.getProcess(),
                        currentTitle));

        tempProcess.setTiffHeaderDocumentName(tempProcess.getProcess().getTitle());
        String tiffDefinition = ServiceManager.getImportService().getTiffDefinition();
        if (Objects.nonNull(tiffDefinition)) {
            tempProcess.setTiffHeaderImageDescription(generateTiffHeader(processDetails, tempProcess.getAtstsl(),
                    ServiceManager.getImportService().getTiffDefinition(), docType));
        }
    }

    /**
     * Get the title definition of doc type view.
     *
     * @param rulesetManagement
     *         the ruleset management
     * @param docType
     *         current division
     * @param acquisitionStage
     *         current acquisition level
     * @param priorityList
     *         weighted list of user-preferred display languages
     * @return the process title of doc type view
     */
    public static String getTitleDefinition(RulesetManagementInterface rulesetManagement, String docType,
            String acquisitionStage, List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface docTypeView = rulesetManagement.getStructuralElementView(docType,
                acquisitionStage, priorityList);
        return docTypeView.getProcessTitle().orElse("");
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
    public static HashSet<Metadata> convertMetadata(NodeList nodes, MdSec domain) {
        HashSet<Metadata> allMetadata = new HashSet<>();
        if (Objects.nonNull(nodes)) {
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
                        group.setMetadata(convertMetadata(element.getChildNodes(), null));
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
        }
        return allMetadata;
    }

    /**
     * Generate and set the title to process using current title parameter and gets the atstsl.
     *
     * @param title
     *         of the work to generate atstsl
     * @return String atstsl
     */
    private static String generateProcessTitleAndGetAtstsl(List<ProcessDetail> processDetails, String titleDefinition,
            Process process, String title) throws ProcessGenerationException {
        TitleGenerator titleGenerator = new TitleGenerator(null, processDetails);
        String newTitle = titleGenerator.generateTitle(titleDefinition, null, title);
        process.setTitle(newTitle);
        // atstsl is created in title generator and next used in tiff header generator
        return titleGenerator.getAtstsl();
    }

    private static boolean shouldGenerateAtstslFields(TempProcess tempProcess) {
        return StringUtils.isBlank(tempProcess.getProcess().getTitle());
    }

    /**
     * Generate tiff header.
     */
    private static String generateTiffHeader(List<ProcessDetail> processDetails, String atstsl, String tiffDefinition,
            String docType) throws ProcessGenerationException {
        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(atstsl, processDetails);
        return tiffHeaderGenerator.generateTiffHeader(tiffDefinition, docType);
    }

    private static String getTitleFromWorkpiece(Process process) {
        try {
            LegacyMetsModsDigitalDocumentHelper metsModsDigitalDocumentHelper = ServiceManager.getProcessService()
                    .readMetadataFile(process);
            return getTitleFromMetadata(
                    metsModsDigitalDocumentHelper.getWorkpiece().getLogicalStructure().getMetadata());
        } catch (IOException | NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private static String getTitleFromParents(List<TempProcess> parentTempProcesses,
            RulesetManagementInterface rulesetManagement, String acquisitionStage,
            List<Locale.LanguageRange> priorityList) {
        if (parentTempProcesses.isEmpty()) {
            return StringUtils.EMPTY;
        }

        for (TempProcess tempProcess : parentTempProcesses) {
            String title;
            if (Objects.nonNull(tempProcess.getMetadataNodes())) {
                ProcessFieldedMetadata processFieldedMetadata = initializeTempProcessDetails(tempProcess,
                        rulesetManagement, acquisitionStage, priorityList);
                title = getTitleFromMetadata(processFieldedMetadata.getChildMetadata());
            } else {
                title = getTitleFromWorkpiece(tempProcess.getProcess());
            }

            if (StringUtils.isNotBlank(title)) {
                return title;
            }
        }
        return StringUtils.EMPTY;
    }

    private static ProcessFieldedMetadata initializeTempProcessDetails(TempProcess tempProcess,
            RulesetManagementInterface rulesetManagement, String acquisitionStage,
            List<Locale.LanguageRange> priorityList) {
        ProcessFieldedMetadata metadata = initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
                rulesetManagement, acquisitionStage, priorityList);
        metadata.setMetadata(convertMetadata(tempProcess.getMetadataNodes(), MdSec.DMD_SEC));
        return metadata;
    }

    private static String getTitleFromMetadata(Collection<Metadata> metadata) {
        Optional<Metadata> metadataOptional = metadata.parallelStream()
                .filter(metadataItem -> TitleGenerator.TITLE_DOC_MAIN.equals(metadataItem.getKey())).findFirst();
        if (metadataOptional.isPresent() && metadataOptional.get() instanceof MetadataEntry) {
            return ((MetadataEntry) metadataOptional.get()).getValue();
        }
        return StringUtils.EMPTY;
    }

}

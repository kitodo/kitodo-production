package org.kitodo.production.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
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
     * @param managementInterface
     *            RulesetManagementInterface used to create the metadata of the
     *            process
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
            RulesetManagementInterface managementInterface, String acquisitionStage,
            List<Locale.LanguageRange> priorityList)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        ProcessFieldedMetadata metadata = initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
            managementInterface, acquisitionStage, priorityList);
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
     * @param managementInterface
     *            RulesetManagementInterface used to create ProcessFieldedMetadata
     * @param stage
     *            String containing acquisition stage used to create
     *            ProcessFieldedMetadata
     * @param priorityList
     *            List of LanguageRange objects used to create
     *            ProcessFieldedMetadata
     * @return the created ProcessFieldedMetadata
     */
    public static ProcessFieldedMetadata initializeProcessDetails(LogicalDivision structure,
            RulesetManagementInterface managementInterface, String stage, List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface divisionView = managementInterface.getStructuralElementView(structure.getType(),
            stage, priorityList);
        return new ProcessFieldedMetadata(structure, divisionView);
    }

    public static void generateAtstslFields(TempProcess tempProcess, List<TempProcess> parents, String acquisitionStage)
            throws ProcessGenerationException, InvalidMetadataValueException, NoSuchMetadataFieldException,
            IOException {
        RulesetManagementInterface rulesetManagementInterface = ServiceManager.getRulesetService()
                .openRuleset(tempProcess.getProcess().getRuleset());
        List<Locale.LanguageRange> priorityList = ServiceManager.getUserService().getCurrentMetadataLanguage();
        String docType = tempProcess.getWorkpiece().getLogicalStructure().getType();
        List<ProcessDetail> processDetails = transformToProcessDetails(tempProcess, rulesetManagementInterface,
            acquisitionStage, priorityList);
        generateAtstslFields(tempProcess, processDetails, parents, docType, rulesetManagementInterface,
            acquisitionStage, priorityList, null);
    }

    public static void generateAtstslFields(TempProcess tempProcess, List<ProcessDetail> processDetails,
            List<TempProcess> parents, String docType, RulesetManagementInterface rulesetManagementInterface,
            String acquisitionStage, List<Locale.LanguageRange> priorityList, Process parentProcess)
            throws ProcessGenerationException {
        String processTitleOfDocTypeView = getProcessTitleOfDocTypeView(rulesetManagementInterface, docType,
            acquisitionStage, priorityList);

        String currentTitle = TitleGenerator.getValueOfMetadataID(TitleGenerator.TITLE_DOC_MAIN, processDetails);
        if (StringUtils.isBlank(currentTitle)) {
            if (Objects.nonNull(parentProcess)) {
                if (processTitleOfDocTypeView.startsWith("+")) {
                    processTitleOfDocTypeView = '\'' + parentProcess.getTitle() + '\'' + processTitleOfDocTypeView;
                }
                currentTitle = getTitleFromLogicalStructure(parentProcess);
            } else {
                currentTitle = getTitleFromParents(parents, rulesetManagementInterface, acquisitionStage, priorityList);
            }
        }

        tempProcess.setAtstsl(generateProcessTitleAndGetAtstsl(processDetails, processTitleOfDocTypeView,
            tempProcess.getProcess(), currentTitle));
        tempProcess.setTiffHeaderDocumentName(tempProcess.getProcess().getTitle());
        String tiffDefinition = ServiceManager.getImportService().getTiffDefinition();
        if (Objects.nonNull(tiffDefinition)) {
            tempProcess.setTiffHeaderImageDescription(generateTiffHeader(processDetails, tempProcess.getAtstsl(),
                ServiceManager.getImportService().getTiffDefinition(), docType));
        }
    }

    public static String getProcessTitleOfDocTypeView(RulesetManagementInterface rulesetManagementInterface,
            String docType, String acquisitionStage, List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface docTypeView = rulesetManagementInterface.getStructuralElementView(docType,
            acquisitionStage, priorityList);
        return docTypeView.getProcessTitle().orElse("");
    }

    /**
     * Generate and set the title to process using current title parameter and gets
     * the atstsl.
     *
     * @param title
     *            of the work to generate atstsl
     * @return String atstsl
     */
    public static String generateProcessTitleAndGetAtstsl(List<ProcessDetail> processDetails, String titleDefinition,
            Process process, String title) throws ProcessGenerationException {
        TitleGenerator titleGenerator = new TitleGenerator(null, processDetails);
        String newTitle = titleGenerator.generateTitle(titleDefinition, null, title);
        process.setTitle(newTitle);
        // atstsl is created in title generator and next used in tiff header generator
        return titleGenerator.getAtstsl();
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
    public static List<Metadata> convertMetadata(NodeList nodes, MdSec domain) {
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
                    group.setGroup(convertMetadata(element.getChildNodes(), null));
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
     * Calculate tiff header.
     */
    private static String generateTiffHeader(List<ProcessDetail> processDetails, String atstsl, String tiffDefinition,
            String docType) throws ProcessGenerationException {
        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(atstsl, processDetails);
        return tiffHeaderGenerator.generateTiffHeader(tiffDefinition, docType);
    }

    private static String getTitleFromLogicalStructure(Process process) {
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

    private static String getTitleFromParents(List<TempProcess> parents,
            RulesetManagementInterface rulesetManagementInterface, String acquisitionStage,
            List<Locale.LanguageRange> priorityList) {
        if (parents.size() == 0) {
            return StringUtils.EMPTY;
        }

        // get title of ancestors where TitleDocMain exists when several processes were
        // imported
        for (TempProcess tempProcess : parents) {
            ProcessFieldedMetadata processFieldedMetadata = initializeTempProcessDetails(tempProcess,
                rulesetManagementInterface, acquisitionStage, priorityList);
            String title = getTitleFromMetadata(processFieldedMetadata.getChildMetadata());
            if (StringUtils.isNotBlank(title)) {
                return title;
            }
        }
        return StringUtils.EMPTY;
    }

    private static ProcessFieldedMetadata initializeTempProcessDetails(TempProcess tempProcess,
            RulesetManagementInterface rulesetManagementInterface, String acquisitionStage,
            List<Locale.LanguageRange> priorityList) {
        ProcessFieldedMetadata metadata = initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
            rulesetManagementInterface, acquisitionStage, priorityList);
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

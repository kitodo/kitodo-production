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

import static org.kitodo.constants.StringConstants.CREATE;
import static org.kitodo.constants.StringConstants.EDIT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.NotImplementedException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.metadata.MetadataEditor;
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
        List<Locale.LanguageRange> priorityList = ServiceManager.getUserService().getCurrentMetadataLanguage();
        generateAtstslFields(tempProcess, parentTempProcesses, acquisitionStage, priorityList, force);
    }

    /**
     * Generates TSL/ATS dependent fields for given language list 'priorityList'.
     *
     * @param tempProcess
     *         the temp process to generate TSL/ATS dependent fields
     * @param parentTempProcesses
     *         the parent temp processes
     * @param acquisitionStage
     *         current acquisition stage
     * @param priorityList
     *         language list
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
                                            String acquisitionStage, List<Locale.LanguageRange> priorityList, boolean force)
            throws ProcessGenerationException, InvalidMetadataValueException, NoSuchMetadataFieldException,
            IOException {
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetService()
                .openRuleset(tempProcess.getProcess().getRuleset());
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

    private static HashSet<Metadata> createMetadataGroup(String[] keyParts, String[] contentParts, String separatorCharacter,
                                                         RulesetManagementInterface rulesetManagement,
                                                         List<Locale.LanguageRange> priorityList) {
        if (keyParts.length == 0 || contentParts.length == 0 || StringUtils.isBlank(separatorCharacter)) {
            return new HashSet<>();
        }
        HashSet<Metadata> metadataSet = new HashSet<>();
        for (int i = 0; i < keyParts.length; i++) {
            String subMetadataKey = keyParts[i];
            MetadataViewInterface metadataView = rulesetManagement.getMetadataView(subMetadataKey, CREATE, priorityList);
            if (Objects.nonNull(metadataView)) {
                if (metadataView.isComplex()) {
                    throw new NotImplementedException("Nested metadata groups are currently not supported in mass import from CSV files");
                    // TODO: implement!
                    //MetadataGroup metadataGroup = new MetadataGroup();
                    //metadataGroup.setKey(subMetadataKey);
                    //metadataGroup.setMetadata(createMetadataGroup());
                    //metadataSet.add(metadataGroup);
                } else {
                    MetadataEntry metadataEntry = new MetadataEntry();
                    metadataEntry.setKey(subMetadataKey);
                    metadataEntry.setValue(contentParts[i]);
                    metadataSet.add(metadataEntry);
                }
            }
        }
        return metadataSet;
    }

    /**
     * Converts DOM node list of Kitodo metadata elements to metadata objects.
     *
     * @param nodes
     *            node list to convert to metadata
     * @param ruleset
     *            ruleset of process
     * @return metadata from node list
     */
    public static HashSet<Metadata> convertMetadata(NodeList nodes, RulesetManagementInterface ruleset) {
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
                        group.setMetadata(convertMetadata(element.getChildNodes(), ruleset));
                        metadata = group;
                        break;
                    }
                    default:
                        continue;
                }
                String metadataKey = element.getAttribute("name");
                metadata.setKey(metadataKey);
                setMetadataDomain(metadata, ruleset);
                allMetadata.add(metadata);
            }
        }
        return allMetadata;
    }

    /**
     * Convert given map Strings and lists of Strings to a metadata set and return this set.
     * Map keys contain metadata IDs and map values contain lists of metadata values.
     *
     * @param metadataMap map containing metadata IDs as keys and metadata values as lists of Strings
     * @param rulesetManagementInterface used to determine whether keys are defined in corresponding ruleset
     * @param separator String used to parse metadata groups from Strings
     * @return metadata set
     */
    public static HashSet<Metadata> convertMetadata(Map<String, List<String>> metadataMap,
                                                    RulesetManagementInterface rulesetManagementInterface,
                                                    String separator) {
        final String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
        List<Locale.LanguageRange> priorityList = Locale.LanguageRange.parse(metadataLanguage);
        HashSet<Metadata> metadataSet = new HashSet<>();
        boolean separatorSet = StringUtils.isNotBlank(separator);
        for (Map.Entry<String, List<String>> entry : metadataMap.entrySet()) {
            for (String value : entry.getValue()) {
                // distinguish between metadata entries and groups (groups definitely required in the future!)
                if (separatorSet && entry.getKey().contains(separator)) {
                    String[] keyParts = entry.getKey().split(Pattern.quote(separator), -1);
                    String[] contentParts = value.split(Pattern.quote(separator), -1);
                    // "-1" because 'keyParts' also contains the name of the metadata group itself at the first index
                    if (keyParts.length < 1 || (keyParts.length - 1 != contentParts.length)) {
                        logger.error("Number of keys in CSV cell to be parsed into metadata group does not match number of metadata keys");
                        break;
                    } else {
                        // try to create MetadataGroup from 'entry.getValue'
                        String metadataGroupKey = keyParts[0];
                        // verify that "metadataGroupKey" is a valid metadata defined in the given "RulesetManagementInterface"
                        MetadataViewInterface metadataView = rulesetManagementInterface.getMetadataView(metadataGroupKey,
                                CREATE, priorityList);
                        if (metadataView.isComplex()) {
                            MetadataGroup metadataGroup = new MetadataGroup();
                            metadataGroup.setKey(metadataGroupKey);
                            metadataGroup.setMetadata(createMetadataGroup(Arrays.copyOfRange(keyParts, 1, keyParts.length),
                                    contentParts, separator, rulesetManagementInterface, priorityList));
                            metadataSet.add(metadataGroup);
                        } else {
                            logger.error("Metadata with key '{}' is configured as simple metadata entry in ruleset but passed as "
                                    + "metadata group in uploaded CSV file", metadataGroupKey);
                            break;
                        }
                    }
                } else {
                    // create MetadataEntry from 'entry.getValue'
                    MetadataViewInterface metadataView = rulesetManagementInterface.getMetadataView(entry.getKey(), CREATE, priorityList);
                    if (metadataView.isComplex()) {
                        logger.error("Metadata with key '{}' is configured as complex metadata group in ruleset but passed as simple "
                                + "metadata entry in uploaded CSV file", entry.getKey());
                        break;
                    }
                    MetadataEntry metadataEntry = new MetadataEntry();
                    metadataEntry.setKey(entry.getKey());
                    metadataEntry.setValue(value);
                    metadataSet.add(metadataEntry);
                }
            }
        }
        return metadataSet;
    }

    /**
     * Sets the domain of the given metadata. This method uses a side effect. It doesn't return a new instance of
     * metadata but changes the given metadata object.
     *
     * @param metadata Metadata instance for which the
     * @param ruleset RulesetManagementInterface from which domain information about given metadata is retrieved
     */
    public static void setMetadataDomain(Metadata metadata, RulesetManagementInterface ruleset) {
        MetadataViewInterface viewInterface = ruleset.getMetadataView(metadata.getKey(), EDIT, ServiceManager
                .getUserService().getCurrentMetadataLanguage());
        if (viewInterface.getDomain().isPresent()) {
            Domain domain = viewInterface.getDomain().get();
            // skip domain 'METS_DIV' because it has no equivalent 'MdSec'
            if (!Domain.METS_DIV.equals(domain)) {
                metadata.setDomain(MetadataEditor.domainToMdSec(domain));
            }
        }
    }

    /**
     * Add allowed metadata to given division.
     *
     * @param division
     *          division to which allowed metadata is added
     * @param keys
     *          metadata keys to consider
     * @param value
     *          String to be set as value of metadata
     * @param rulesetManagement
     *          RulesetManagementInterface containing metadata rules
     * @param acquisitionStage
     *          current acquisition stage
     * @param priorityList
     *          list of languages
     */
    public static void addAllowedMetadataRecursive(Division<?> division, Collection<String> keys, String value,
                                                   RulesetManagementInterface rulesetManagement, String acquisitionStage,
                                                   List<Locale.LanguageRange> priorityList) {
        StructuralElementViewInterface divisionView = rulesetManagement.getStructuralElementView(division.getType(),
                acquisitionStage, priorityList);
        for (MetadataViewInterface metadataView : divisionView.getAllowedMetadata()) {
            if (metadataView instanceof SimpleMetadataViewInterface && keys.contains(metadataView.getId())
                    && division.getMetadata().parallelStream()
                    .filter(metadata -> metadataView.getId().equals(metadata.getKey()))
                    .count() < metadataView.getMaxOccurs()) {
                MetadataEditor.writeMetadataEntry(division, (SimpleMetadataViewInterface) metadataView, value);
            }
        }
        for (Division<?> child : division.getChildren()) {
            addAllowedMetadataRecursive(child, keys, value, rulesetManagement, acquisitionStage, priorityList);
        }
    }

    /**
     * Save metadata of given tempProcesses process to meta.xml file.
     *
     * @param tempProcess
     *          TempProcess whose Process metadata is saved to a meta.xml file
     * @param rulesetManagement
     *          RulesetManagementInterface containing metadata rules
     * @param acquisitionStage
     *          current acquisition stage
     * @param priorityList
     *          list of languages
     */
    public static void saveTempProcessMetadata(TempProcess tempProcess, RulesetManagementInterface rulesetManagement,
                                        String acquisitionStage, List<Locale.LanguageRange> priorityList) {
        try (OutputStream out = ServiceManager.getFileService()
                .write(ServiceManager.getProcessService().getMetadataFileUri(tempProcess.getProcess()))) {
            Workpiece workpiece = tempProcess.getWorkpiece();
            workpiece.setId(tempProcess.getProcess().getId().toString());
            if (Objects.nonNull(rulesetManagement)) {
                setProcessTitleMetadata(workpiece, tempProcess.getProcess().getTitle(), rulesetManagement,
                        acquisitionStage, priorityList);
            }
            ServiceManager.getMetsService().save(workpiece, out);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private static void setProcessTitleMetadata(Workpiece workpiece, String processTitle,
                                         RulesetManagementInterface rulesetManagement, String acquisitionStage,
                                         List<Locale.LanguageRange> priorityList) {
        Collection<String> processTitleKeys = rulesetManagement.getFunctionalKeys(FunctionalMetadata.PROCESS_TITLE);
        if (!processTitleKeys.isEmpty()) {
            ProcessHelper.addAllowedMetadataRecursive(workpiece.getLogicalStructure(), processTitleKeys, processTitle,
                    rulesetManagement, acquisitionStage, priorityList);
            ProcessHelper.addAllowedMetadataRecursive(workpiece.getPhysicalStructure(), processTitleKeys, processTitle,
                    rulesetManagement, acquisitionStage, priorityList);
        }
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
        metadata.setMetadata(convertMetadata(tempProcess.getMetadataNodes(), rulesetManagement));
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

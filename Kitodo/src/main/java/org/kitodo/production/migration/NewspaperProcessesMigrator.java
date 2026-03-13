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

package org.kitodo.production.migration;



import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.BatchType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.enums.ProcessState;
import org.kitodo.production.helper.tasks.NewspaperMigrationTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.process.NewspaperProcessesGenerator;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.BatchService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.xml.sax.SAXException;

/**
 * Tool for converting newspaper processes from Production v. 2 format to
 * Production v. 3 format.
 */
public class NewspaperProcessesMigrator {
    private static final Logger logger = LogManager.getLogger(NewspaperProcessesMigrator.class);

    /**
     * Metadata field in Production v. 2 where the displayed title is contained.
     */
    private static final String FIELD_TITLE = "TitleDocMain";

    /**
     * Metadata field in Production v. 2, in which the title is contained in
     * sorting form.
     */
    private static final String FIELD_TITLE_SORT = "TitleDocMainShort";

    /**
     * Regular expression to find (and remove) the individual part of the
     * process title, to get the base process title.
     */
    private static final String INDIVIDUAL_PART = "(?<=.)\\p{Punct}+(?:1[6-9]|20)\\d{2}\\p{Punct}?(?:0[1-9]|1[012]).*$";

    /**
     * A regular expression describing a four-digit year number or a double year
     * consisting of two four-digit year numbers, concatenated by a slash.
     */
    private static final String YEAR_OR_DOUBLE_YEAR = "\\d{4}(?:/\\d{4})?";

    /**
     * Acquisition stage of newspaper processes migrator.
     */
    private final String acquisitionStage = "";

    /**
     * The database index number of the newspaper batch.
     */
    private Integer batchNumber;

    /**
     * Service to read and write Batch objects in the database or search engine
     * index.
     */
    private static final BatchService batchService = ServiceManager.getBatchService();

    /**
     * Service that contains the meta-data editor.
     */
    private final DataEditorService dataEditorService = ServiceManager.getDataEditorService();

    /**
     * Ruleset setting where to store the day information.
     */
    private DatesSimpleMetadataViewInterface daySimpleMetadataView;

    /**
     * Service to access files on the storage.
     */
    private static final FileService fileService = ServiceManager.getFileService();

    /**
     * Service to read and write METS file format.
     */
    private final MetsService metsService = ServiceManager.getMetsService();

    /**
     * Ruleset setting where to store the month information.
     */
    private DatesSimpleMetadataViewInterface monthSimpleMetadataView;

    /**
     * Service to read and write Process objects in the database or search
     * engine index.
     */
    private static final ProcessService processService = ServiceManager.getProcessService();

    /**
     * List of processes.
     */
    private final List<Process> processes;

    /**
     * Record ID of the process template.
     */
    private int templateId;

    /**
     * The metadata of the newspaper as its whole.
     */
    private Collection<Metadata> overallMetadata = new ArrayList<>();

    /**
     * A process representing the newspaper as its whole.
     */
    private Process overallProcess;

    /**
     * The workpiece of the newspaper as its whole.
     */
    private Workpiece overallWorkpiece = new Workpiece();

    /**
     * Record ID of the project.
     */
    private int projectId;

    /**
     * The process title.
     */
    private String title;

    /**
     * Ruleset setting where to store the year information.
     */
    private DatesSimpleMetadataViewInterface yearSimpleMetadataView;

    /**
     * The years of the course of appearance of the newspaper with their
     * logical structures.
     */
    private Map<String, LogicalDivision> years = new TreeMap<>();

    /**
     * Process IDs of children (issue processes) to be added to the years in
     * question.
     */
    private Map<String, Collection<Integer>> yearsChildren = new HashMap<>();

    /**
     * Years iterator during creation of year processes.
     */
    private PeekingIterator<Entry<String, LogicalDivision>> yearsIterator;

    /**
     * The ruleset.
     */
    private RulesetManagementInterface rulesetManagement;

    /**
     * Creates a new process migrator.
     *
     * @param batch
     *            the batch to process
     */
    public NewspaperProcessesMigrator(Batch batch) {
        this.batchNumber = batch.getId();
        this.processes = batch.getProcesses();
    }

    /**
     * Returns all newspaper batches.
     *
     * @return all newspaper batches
     * @throws DAOException
     *             if a batch cannot be load from the database
     * @throws IOException
     *             if an I/O error occurs when accessing the file system
     */
    public static List<Batch> getNewspaperBatches() throws DAOException, IOException {
        List<Batch> newspaperBatches = new ArrayList<>();
        for (Batch batch : batchService.getAll()) {
            if (BatchType.NEWSPAPER.equals(batch.getType())) {
                newspaperBatches.add(batch);
            }
        }
        return newspaperBatches;
    }

    /**
     * Creates a newspaper migration task for the given batch ID in the task
     * manager.
     *
     * @param batchId
     *            number of batch to migrate
     * @throws DAOException
     *             if a db error occurs
     */
    public static void initializeMigration(Integer batchId) throws DAOException {
        Batch batch = ServiceManager.getBatchService().getById(batchId);
        TaskManager.addTask(new NewspaperMigrationTask(batch));
    }

    /**
     * Initializes the newspaper processes migrator.
     *
     * @param process
     *            a process, to get basic information from
     * @param newspaperIncludedStructalElementDivision
     *            the ID of the newspaper division in the ruleset
     */
    private void initializeMigrator(Process process, String newspaperIncludedStructalElementDivision)
            throws IOException, ConfigurationException {

        title = generateNewspaperShortTitle(process.getTitle());
        logger.trace("Newspaper is: {}", title);
        projectId = process.getProject().getId();
        logger.trace("Project is: {} (ID {})", process.getProject().getTitle(), projectId);
        templateId = process.getTemplate().getId();
        logger.trace("Template is: {} (ID {})", process.getTemplate().getTitle(), templateId);

        rulesetManagement = ServiceManager.getRulesetService()
                .openRuleset(process.getRuleset());
        StructuralElementViewInterface newspaperView = rulesetManagement.getStructuralElementView(
            newspaperIncludedStructalElementDivision, "", NewspaperProcessesGenerator.ENGLISH);
        StructuralElementViewInterface yearDivisionView = NewspaperProcessesGenerator.nextSubView(rulesetManagement,
            newspaperView, acquisitionStage);
        yearSimpleMetadataView = yearDivisionView.getDatesSimpleMetadata().orElseThrow(
            () -> new ConfigurationException(yearDivisionView.getId() + " has no dates metadata configuration!"));
        StructuralElementViewInterface monthDivisionView = NewspaperProcessesGenerator.nextSubView(rulesetManagement,
            yearDivisionView, acquisitionStage);
        monthSimpleMetadataView = monthDivisionView.getDatesSimpleMetadata().orElseThrow(
            () -> new ConfigurationException(monthDivisionView.getId() + " has no dates metadata configuration!"));
        StructuralElementViewInterface dayDivisionView = NewspaperProcessesGenerator.nextSubView(rulesetManagement,
            monthDivisionView, acquisitionStage);
        daySimpleMetadataView = dayDivisionView.getDatesSimpleMetadata().orElseThrow(
            () -> new ConfigurationException(dayDivisionView.getId() + " has no dates metadata configuration!"));
    }

    /**
     * Convert a newspaper like full title into its shorted version.
     *
     * @param newspaperFullTitle Newspaper like full title
     * @return Shorted newspaper like title
     */
    public String generateNewspaperShortTitle(String newspaperFullTitle) {
        return newspaperFullTitle.replaceFirst(INDIVIDUAL_PART, "");
    }

    /**
     * Converts one newspaper process.
     *
     * @param index
     *            index of process to convert in the processes object
     *            list passed to the constructor—<b>not</b> the process ID
     */
    public void convertProcess(int index) throws DAOException, IOException, ConfigurationException, SAXException,
            FileStructureValidationException {
        final long begin = System.nanoTime();
        Integer processId = processes.get(index).getId();
        Process process = processService.getById(processId);
        String processTitle = process.getTitle();
        logger.info("Starting to convert process {} (ID {})...", processTitle, processId);
        URI metadataFilePath = fileService.getMetadataFilePath(process);
        URI anchorFilePath = fileService.createAnchorFile(metadataFilePath);
        URI yearFilePath = fileService.createYearFile(metadataFilePath);
        overallWorkpiece = metsService.loadWorkpiece(anchorFilePath);

        dataEditorService.readData(anchorFilePath);
        dataEditorService.readData(yearFilePath);
        dataEditorService.readData(metadataFilePath);

        Workpiece workpiece = metsService.loadWorkpiece(metadataFilePath);
        workpiece.setId(process.getId().toString());
        LogicalDivision newspaperLogicalDivision = workpiece.getLogicalStructure();

        if (Objects.isNull(title)) {
            initializeMigrator(process, newspaperLogicalDivision.getType());
        }

        LogicalDivision yearLogicalDivision = cutOffTopLevel(newspaperLogicalDivision);
        final String year = createLinkStructureAndCopyDates(process, yearFilePath, yearLogicalDivision);

        workpiece.setLogicalStructure(cutOffTopLevel(yearLogicalDivision));
        moveMetadataFromYearToIssue(process, processTitle, yearFilePath, workpiece);
        metsService.saveWorkpiece(workpiece, metadataFilePath);

        for (Metadata metadata : overallWorkpiece.getLogicalStructure().getMetadata()) {
            if (!overallMetadata.contains(metadata)) {
                logger.debug("Adding metadata to newspaper {}: {}", title, metadata);
                overallMetadata.add(metadata);
            }
        }
        yearsChildren.computeIfAbsent(year, each -> new ArrayList<>()).add(processId);

        ServiceManager.getFileService().renameFile(anchorFilePath, "meta_anchor.migrated");
        ServiceManager.getFileService().renameFile(yearFilePath, "meta_year.migrated");

        logger.info("Process {} (ID {}) successfully converted.", processTitle, processId);
        if (logger.isTraceEnabled()) {
            logger.trace("Converting {} took {} ms.", processTitle,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private void moveMetadataFromYearToIssue(Process process, String processTitle, URI yearFilePath,
            Workpiece workpiece) throws IOException, SAXException, FileStructureValidationException {
        Workpiece yearWorkpiece = metsService.loadWorkpiece(yearFilePath);
        // Copy metadata from year to issue
        Collection<Metadata> processMetadataFromYear = new ArrayList<>(
                yearWorkpiece.getLogicalStructure().getChildren().getFirst().getMetadata());
        List<LogicalDivision> issuesIncludedStructuralElements = workpiece.getLogicalStructure().getChildren()
                .getFirst().getChildren();
        issuesIncludedStructuralElements.getFirst().getMetadata().addAll(processMetadataFromYear);

        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();

        // find and load the ruleset file
        String rulesetDir = ConfigCore.getParameter(ParameterCore.DIR_RULESETS);
        String rulesetFullPath = Paths.get(rulesetDir, process.getRuleset().getFile()).toString();
        rulesetManagement.load(new File(rulesetFullPath));
        Collection<String> functionalKeys = rulesetManagement.getFunctionalKeys(FunctionalMetadata.PROCESS_TITLE);
        String titleKey = functionalKeys.isEmpty() ? FIELD_TITLE : functionalKeys.stream().findFirst().get();

        MetadataEntry titelMetadata = new MetadataEntry();
        titelMetadata.setValue(processTitle);
        titelMetadata.setKey(titleKey);
        titelMetadata.setDomain(MdSec.DMD_SEC);
        issuesIncludedStructuralElements.getFirst().getMetadata().add(titelMetadata);
    }

    /**
     * Cuts the top level of a tree logical division.
     *
     * @param logicalDivision
     *            tree logical division to be cut
     * @return the new top level
     */
    private static LogicalDivision cutOffTopLevel(LogicalDivision logicalDivision) {
        List<LogicalDivision> children = logicalDivision.getChildren();
        int numberOfChildren = children.size();
        if (numberOfChildren == 0) {
            return null;
        }
        LogicalDivision firstChild = children.getFirst();
        if (numberOfChildren > 1) {
            children.subList(1, numberOfChildren).stream()
                    .flatMap(theLogicalDivision -> theLogicalDivision.getChildren().stream())
                    .forEachOrdered(firstChild.getChildren()::add);
            String firstOrderlabel = firstChild.getOrderlabel();
            String lastOrderlabel = children.getLast().getOrderlabel();
            if (Objects.nonNull(firstOrderlabel) && !firstOrderlabel.equals(lastOrderlabel)) {
                firstChild.setOrderlabel(firstOrderlabel + '/' + lastOrderlabel);
            }
        }
        return firstChild;
    }

    /**
     * Creates or complements the logical root levels of the annual level.
     *
     * @param process
     *            process ID of the current process (on issue level)
     * @param yearMetadata
     *            Production v. 2 year metadata file
     * @param metaFileYearLogicalDivision
     *            year logical division of the processes’ metadata file
     * @throws IOException
     *             if an error occurs in the disk drive
     */
    private String createLinkStructureAndCopyDates(Process process, URI yearMetadata,
            LogicalDivision metaFileYearLogicalDivision)
            throws IOException, ConfigurationException, SAXException, FileStructureValidationException {

        LogicalDivision yearFileYearLogicalDivision = metsService.loadWorkpiece(yearMetadata)
                .getLogicalStructure().getChildren().getFirst();
        String year = MetadataEditor.getMetadataValue(yearFileYearLogicalDivision, FIELD_TITLE_SORT);
        if (Objects.isNull(year) || !year.matches(YEAR_OR_DOUBLE_YEAR)) {
            logger.debug("\"{}\" is not a year number. Falling back to {}.", year, FIELD_TITLE);
            year = MetadataEditor.getMetadataValue(yearFileYearLogicalDivision, FIELD_TITLE);
        }
        LogicalDivision processYearLogicalDivision = years.computeIfAbsent(year, theYear -> {
            // remove existing layers in the year
            yearFileYearLogicalDivision.getChildren().getFirst().getChildren().getFirst().getChildren().clear();
            MetadataEditor.writeMetadataEntry(yearFileYearLogicalDivision, yearSimpleMetadataView, theYear);
            return yearFileYearLogicalDivision;
        });

        createLinkStructureAndCopyMonths(process, metaFileYearLogicalDivision, yearFileYearLogicalDivision, year,
            processYearLogicalDivision);
        return year;
    }

    private void createLinkStructureAndCopyMonths(Process process,
            LogicalDivision metaFileYearLogicalDivision, LogicalDivision yearFileYearLogicalDivision, String year,
            LogicalDivision processYearLogicalDivision) throws ConfigurationException {

        // Add types to month and day
        StructuralElementViewInterface newspaperView = rulesetManagement.getStructuralElementView(
            overallWorkpiece.getLogicalStructure().getType(), acquisitionStage, Locale.LanguageRange.parse("en"));
        StructuralElementViewInterface yearDivisionView = nextSubView(rulesetManagement, newspaperView,
            acquisitionStage);
        yearSimpleMetadataView = yearDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        StructuralElementViewInterface monthDivisionView = nextSubView(rulesetManagement, yearDivisionView,
            acquisitionStage);
        monthSimpleMetadataView = monthDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        String monthType = monthDivisionView.getId();
        StructuralElementViewInterface dayDivisionView = nextSubView(rulesetManagement, monthDivisionView,
            acquisitionStage);
        daySimpleMetadataView = dayDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        String dayType = dayDivisionView.getId();

        for (Iterator<LogicalDivision> yearFileMonthLogicalDivisionsIterator = yearFileYearLogicalDivision
                .getChildren()
                .iterator(), metaFileMonthLogicalDivisionsIterator = metaFileYearLogicalDivision
                        .getChildren().iterator(); yearFileMonthLogicalDivisionsIterator.hasNext()
                                && metaFileMonthLogicalDivisionsIterator.hasNext();) {
            LogicalDivision yearFileMonthLogicalDivision = yearFileMonthLogicalDivisionsIterator
                    .next();
            LogicalDivision metaFileMonthLogicalDivision = metaFileMonthLogicalDivisionsIterator
                    .next();
            String month = getCompletedDate(yearFileMonthLogicalDivision, year);
            LogicalDivision processMonthLogicalDivision = computeIfAbsent(
                processYearLogicalDivision, monthSimpleMetadataView, month, monthType);
            MetadataEditor.writeMetadataEntry(metaFileMonthLogicalDivision, monthSimpleMetadataView, month);

            createLinkStructureAndCopyDays(process, yearFileMonthLogicalDivision,
                metaFileMonthLogicalDivision, month, dayType, processMonthLogicalDivision);
        }
    }

    private static StructuralElementViewInterface nextSubView(RulesetManagementInterface ruleset,
                                                             StructuralElementViewInterface superiorView, String acquisitionStage) {

        Map<String, String> allowedSubstructuralElements = superiorView.getAllowedSubstructuralElements();
        String subType = allowedSubstructuralElements.entrySet().iterator().next().getKey();
        return ruleset.getStructuralElementView(subType, acquisitionStage, Locale.LanguageRange.parse("en"));
    }

    private void createLinkStructureAndCopyDays(Process process,
            LogicalDivision yearFileMonthLogicalDivision,
            LogicalDivision metaFileMonthLogicalDivision, String month, String dayType,
            LogicalDivision processMonthLogicalDivision) {

        for (Iterator<LogicalDivision> yearFileDayLogicalDivisionsIterator = yearFileMonthLogicalDivision
                .getChildren()
                .iterator(), metaFileDayLogicalDivisionsIterator = metaFileMonthLogicalDivision
                        .getChildren().iterator(); yearFileDayLogicalDivisionsIterator.hasNext()
                                && metaFileDayLogicalDivisionsIterator.hasNext();) {
            LogicalDivision yearFileDayLogicalDivision = yearFileDayLogicalDivisionsIterator
                    .next();
            LogicalDivision metaFileDayLogicalDivision = metaFileDayLogicalDivisionsIterator
                    .next();
            String day = getCompletedDate(yearFileDayLogicalDivision, month);
            LogicalDivision processDayLogicalDivision = computeIfAbsent(
                processMonthLogicalDivision, daySimpleMetadataView, day, dayType);
            MetadataEditor.writeMetadataEntry(metaFileDayLogicalDivision, daySimpleMetadataView, day);
            createLinkStructureOfIssues(process, processDayLogicalDivision);
        }
    }

    private void createLinkStructureOfIssues(Process process, LogicalDivision processDayLogicalDivision) {
        MetadataEditor.addLink(processDayLogicalDivision, process.getId());
    }

    /**
     * Finds the logical division with the specified label, if it
     * exists, otherwise it creates.
     *
     * @param logicalDivision
     *            parent logical division
     * @param simpleMetadataView
     *            indication which metadata value is used to store the value
     * @param value
     *            the value
     * @return child with value
     */
    private static LogicalDivision computeIfAbsent(LogicalDivision logicalDivision,
            SimpleMetadataViewInterface simpleMetadataView, String value, String type) {

        int index = 0;
        for (LogicalDivision child : logicalDivision.getChildren()) {
            String firstSimpleMetadataValue = MetadataEditor.readSimpleMetadataValues(child, simpleMetadataView).getFirst();
            int comparison = firstSimpleMetadataValue.compareTo(value);
            if (comparison <= -1) {
                index++;
            } else if (comparison == 0) {
                return child;
            } else {
                break;
            }
        }
        LogicalDivision computed = new LogicalDivision();
        computed.setType(type);
        MetadataEditor.writeMetadataEntry(computed, simpleMetadataView, value);
        logicalDivision.getChildren().add(index, computed);
        return computed;
    }

    /**
     * Adds a date to get a complete date. In Production versions before 2.2,
     * the date is stored incompletely (as an integer). This is supplemented to
     * ISO if found. Otherwise just returns the date.
     *
     * @param logicalDivision
     *            the logical division that contains the date
     * @param previousLevel
     *            previous part of date
     * @return ISO date
     */
    private static String getCompletedDate(LogicalDivision logicalDivision, String previousLevel) {
        String date = MetadataEditor.getMetadataValue(logicalDivision, FIELD_TITLE_SORT);
        if (!date.matches("\\d{1,2}")) {
            return date;
        }
        logger.debug("Found integer date value ({}), supplementing to ISO date", date);
        StringBuilder composedDate = new StringBuilder();
        composedDate.append(previousLevel);
        composedDate.append('-');
        if (date.length() < 2) {
            composedDate.append('0');
        }
        composedDate.append(date);
        return composedDate.toString();
    }

    /**
     * Creates an overall process as a representation of the newspaper as a
     * whole.
     *
     * @throws ProcessGenerationException
     *             An error occurred while creating the process.
     * @throws IOException
     *             An error has occurred in the disk drive.
     */
    public void createOverallProcess() throws ProcessGenerationException, IOException, DAOException,
            CommandException {
        final long begin = System.nanoTime();
        logger.info("Creating overall process {}...", title);
        overallWorkpiece.getLogicalStructure().getChildren().clear();

        ProcessGenerator processGenerator = new ProcessGenerator();
        processGenerator.generateProcess(templateId, projectId);
        overallProcess = processGenerator.getGeneratedProcess();
        overallProcess.setTitle(getTitle());
        ProcessService.checkTasks(overallProcess, overallWorkpiece.getLogicalStructure().getType());
        processService.save(overallProcess);
        ServiceManager.getFileService().createProcessLocation(overallProcess);
        overallWorkpiece.setId(overallProcess.getId().toString());
        overallWorkpiece.getLogicalStructure().getMetadata().addAll(overallMetadata);
        addToBatch(overallProcess);

        logger.info("Process {} (ID {}) successfully created.", overallProcess.getTitle(), overallProcess.getId());
        if (logger.isTraceEnabled()) {
            logger.trace("Creating {} took {} ms.", overallProcess.getTitle(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * Creates the next year process.
     *
     * @throws ProcessGenerationException
     *             if the process cannot be generated
     * @throws IOException
     *             if an I/O error occurs when accessing the file system
     * @throws DAOException
     *             if there is an error saving the process or if a process
     *             cannot be load from the database
     */
    public void createNextYearProcess() throws ProcessGenerationException, IOException, DAOException,
            CommandException {
        final long begin = System.nanoTime();
        Entry<String, LogicalDivision> yearToCreate = yearsIterator.next();
        String yearTitle = getYearTitle(yearToCreate.getKey());
        logger.info("Creating process for year {}, {}...", yearToCreate.getKey(), yearTitle);
        ProcessGenerator processGenerator = new ProcessGenerator();
        processGenerator.generateProcess(templateId, projectId);
        Process yearProcess = processGenerator.getGeneratedProcess();
        yearProcess.setTitle(yearTitle);
        ProcessService.checkTasks(yearProcess, yearToCreate.getValue().getType());
        // remove metadata from year (which originally relates to issue and was copied there)
        yearToCreate.getValue().getMetadata().clear();
        processService.save(yearProcess);

        MetadataEditor.addLink(overallWorkpiece.getLogicalStructure(), yearProcess.getId());
        if (!yearsIterator.hasNext()) {
            metsService.saveWorkpiece(overallWorkpiece, fileService.getMetadataFilePath(overallProcess, false, false));
        }

        yearProcess.setParent(overallProcess);
        overallProcess.getChildren().add(yearProcess);
        processService.save(yearProcess);

        ServiceManager.getFileService().createProcessLocation(yearProcess);

        createYearWorkpiece(yearToCreate, yearTitle, yearProcess);

        Collection<Integer> childIds = yearsChildren.get(yearToCreate.getKey());
        for (Integer childId : childIds) {
            Process child = processService.getById(childId);
            child.setParent(yearProcess);
            yearProcess.getChildren().add(child);
            processService.save(child);
        }
        if (WorkflowControllerService.allChildrenClosed(yearProcess)) {
            yearProcess.setSortHelperStatus(ProcessState.COMPLETED.getValue());
        }
        processService.save(yearProcess);
        addToBatch(yearProcess);

        logger.info("Process {} (ID {}) successfully created.", yearProcess.getTitle(), yearProcess.getId());
        if (logger.isTraceEnabled()) {
            logger.trace("Creating {} took {} ms.", yearProcess.getTitle(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private void createYearWorkpiece(Entry<String, LogicalDivision> yearToCreate, String yearTitle,
                                     Process yearProcess) throws IOException {
        Workpiece yearWorkpiece = new Workpiece();
        yearWorkpiece.setId(yearProcess.getId().toString());
        yearWorkpiece.setLogicalStructure(yearToCreate.getValue());
        StructuralElementViewInterface newspaperView = rulesetManagement.getStructuralElementView(
            yearWorkpiece.getLogicalStructure().getType(), acquisitionStage, Locale.LanguageRange.parse("de"));
        final Collection<String> processTitleKeys = rulesetManagement.getFunctionalKeys(FunctionalMetadata.PROCESS_TITLE);
        newspaperView.getAllowedMetadata().parallelStream().filter(SimpleMetadataViewInterface.class::isInstance)
                .map(SimpleMetadataViewInterface.class::cast)
                .filter(metadataView -> processTitleKeys.contains(metadataView.getId())).collect(Collectors.toList())
                .forEach(yearView -> MetadataEditor.writeMetadataEntry(yearWorkpiece.getLogicalStructure(), yearView, yearTitle));
        metsService.saveWorkpiece(yearWorkpiece, fileService.getMetadataFilePath(yearProcess, false, false));
    }

    /**
     * Add the process to the newspaper batch.
     *
     * @param process
     *            process to be added
     */
    private void addToBatch(Process process) throws DAOException {
        Batch batch = batchService.getById(batchNumber);
        process.getBatches().add(batch);
        batch.getProcesses().add(process);
        processService.save(process);
        batchService.save(batch);
    }

    /**
     * Returns the number of years that have issues referenced.
     *
     * @return the number of years
     */
    public int getNumberOfYears() {
        return years.size();
    }

    /**
     * Returns the title of the year process to create next.
     *
     * @return the title of the year process to create next
     */
    public String getPendingYearTitle() {
        return getYearTitle(yearsIterator.peek().getKey());
    }

    /**
     * Returns the process title of the process with the given transfer index.
     *
     * @param transferIndex
     *            index of process in transfer object list
     * @return the process title
     */
    public String getProcessTitle(int transferIndex) {
        return processes.get(transferIndex).getTitle();
    }

    /**
     * Returns the title of the overall process.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the title of a year process.
     *
     * @param year
     *            year to return title
     * @return the title
     */
    private String getYearTitle(String year) {
        return title + '-' + year.split("/")[0];
    }

    /**
     * Returns whether there are more years to create.
     *
     * @return whether there are more years
     */
    public boolean hasNextYear() {
        if (Objects.isNull(yearsIterator)) {
            yearsIterator = new PeekingIterator(years.entrySet().iterator());
        }
        return yearsIterator.hasNext();
    }
}

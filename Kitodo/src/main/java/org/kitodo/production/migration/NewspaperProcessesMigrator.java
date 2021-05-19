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

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.io.IOException;
import java.net.URI;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.ProcessGenerationException;
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
    private static final String INDIVIDUAL_PART = "(?<=.)\\p{Punct}*(?:1[6-9]|20)\\d{2}\\p{Punct}?(?:0[1-9]|1[012]).*$";

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
     * The years of the course of appearance of the newspaper with their logical
     * root elements.
     */
    private Map<String, IncludedStructuralElement> years = new TreeMap<>();

    /**
     * Process IDs of children (issue processes) to be added to the years in
     * question.
     */
    private Map<String, Collection<Integer>> yearsChildren = new HashMap<>();

    /**
     * Years iterator during creation of year processes.
     */
    private PeekingIterator<Entry<String, IncludedStructuralElement>> yearsIterator;

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
            if (isNewspaperBatch(batch)) {
                newspaperBatches.add(batch);
            }
        }
        return newspaperBatches;
    }

    /**
     * Returns whether the batch is a newspaper batch. A
     * batch is a newspaper batch, if all of its processes are newspaper
     * processes. A process is a newspaper process if it has a
     * {@code meta_year.xml} file.
     *
     * @param batch
     *            the batch to check
     * @return whether the batch is a newspaper batch
     * @throws IOException
     *             if an I/O error occurs when accessing the file system
     */
    private static boolean isNewspaperBatch(Batch batch) throws IOException {

        logger.trace("Examining batch {}...", batch.getTitle());
        boolean newspaperBatch = true;
        for (Process process : batch.getProcesses()) {
            if (!fileService.processOwnsYearXML(process)) {
                newspaperBatch = false;
                break;
            }
        }
        logger.trace("{} {} newspaper batch.", batch.getTitle(), newspaperBatch ? "is a" : "is not a");
        return newspaperBatch;
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

        title = process.getTitle().replaceFirst(INDIVIDUAL_PART, "");
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
     * Converts one newspaper process.
     *
     * @param index
     *            index of process to convert in the processes object
     *            list passed to the constructor—<b>not</b> the process ID
     */
    public void convertProcess(int index) throws DAOException, IOException, ConfigurationException {
        final long begin = System.nanoTime();
        Integer processId = processes.get(index).getId();
        Process process = processService.getById(processId);
        String processTitle = process.getTitle();
        logger.info("Starting to convert process {} (ID {})...", processTitle, processId);
        URI metadataFilePath = fileService.getMetadataFilePath(process);
        URI anchorFilePath = fileService.createAnchorFile(metadataFilePath);
        URI yearFilePath = fileService.createYearFile(metadataFilePath);

        dataEditorService.readData(anchorFilePath);
        dataEditorService.readData(yearFilePath);
        dataEditorService.readData(metadataFilePath);

        Workpiece workpiece = metsService.loadWorkpiece(metadataFilePath);
        workpiece.setId(process.getId().toString());
        IncludedStructuralElement newspaperIncludedStructuralElement = workpiece.getRootElement();

        if (Objects.isNull(title)) {
            initializeMigrator(process, newspaperIncludedStructuralElement.getType());
        }

        IncludedStructuralElement yearIncludedStructuralElement = cutOffTopLevel(newspaperIncludedStructuralElement);
        final String year = createLinkStructureAndCopyDates(process, yearFilePath, yearIncludedStructuralElement);

        workpiece.setRootElement(cutOffTopLevel(yearIncludedStructuralElement));
        metsService.saveWorkpiece(workpiece, metadataFilePath);

        for (Metadata metadata : metsService.loadWorkpiece(anchorFilePath).getRootElement().getMetadata()) {
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

    /**
     * Cuts the top level of a tree included structural element.
     *
     * @param includedStructuralElement
     *            tree included structural element to be cut
     * @return the new top level
     */
    private static IncludedStructuralElement cutOffTopLevel(IncludedStructuralElement includedStructuralElement) {
        List<IncludedStructuralElement> children = includedStructuralElement.getChildren();
        int numberOfChildren = children.size();
        if (numberOfChildren == 0) {
            return null;
        }
        IncludedStructuralElement firstChild = children.get(0);
        if (numberOfChildren > 1) {
            children.subList(1, numberOfChildren).stream()
                    .flatMap(theIncludedStructuralElement -> theIncludedStructuralElement.getChildren().stream())
                    .forEachOrdered(firstChild.getChildren()::add);
            String firstOrderlabel = firstChild.getOrderlabel();
            String lastOrderlabel = children.get(children.size() - 1).getOrderlabel();
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
     * @param metaFileYearIncludedStructuralElement
     *            year included structural element of the processes’ metadata
     *            file
     * @throws IOException
     *             if an error occurs in the disk drive
     */
    private String createLinkStructureAndCopyDates(Process process, URI yearMetadata,
            IncludedStructuralElement metaFileYearIncludedStructuralElement)
            throws IOException {

        IncludedStructuralElement yearFileYearIncludedStructuralElement = metsService.loadWorkpiece(yearMetadata)
                .getRootElement().getChildren().get(0);
        String year = MetadataEditor.getMetadataValue(yearFileYearIncludedStructuralElement, FIELD_TITLE_SORT);
        if (Objects.isNull(year) || !year.matches(YEAR_OR_DOUBLE_YEAR)) {
            logger.debug("\"{}\" is not a year number. Falling back to {}.", year, FIELD_TITLE);
            year = MetadataEditor.getMetadataValue(yearFileYearIncludedStructuralElement, FIELD_TITLE);
        }
        IncludedStructuralElement processYearIncludedStructuralElement = years.computeIfAbsent(year, theYear -> {
            IncludedStructuralElement yearIncludedStructuralElement = new IncludedStructuralElement();
            MetadataEditor.writeMetadataEntry(yearIncludedStructuralElement, yearSimpleMetadataView, theYear);
            return yearIncludedStructuralElement;
        });

        createLinkStructureAndCopyMonths(process, metaFileYearIncludedStructuralElement,
            yearFileYearIncludedStructuralElement, year, processYearIncludedStructuralElement);
        return year;
    }

    private void createLinkStructureAndCopyMonths(Process process,
            IncludedStructuralElement metaFileYearIncludedStructuralElement,
            IncludedStructuralElement yearFileYearIncludedStructuralElement, String year,
            IncludedStructuralElement processYearIncludedStructuralElement) {

        for (Iterator<IncludedStructuralElement> yearFileMonthIncludedStructuralElementsIterator = yearFileYearIncludedStructuralElement
                .getChildren()
                .iterator(), metaFileMonthIncludedStructuralElementsIterator = metaFileYearIncludedStructuralElement
                        .getChildren().iterator(); yearFileMonthIncludedStructuralElementsIterator.hasNext()
                                && metaFileMonthIncludedStructuralElementsIterator.hasNext();) {
            IncludedStructuralElement yearFileMonthIncludedStructuralElement = yearFileMonthIncludedStructuralElementsIterator
                    .next();
            IncludedStructuralElement metaFileMonthIncludedStructuralElement = metaFileMonthIncludedStructuralElementsIterator
                    .next();
            String month = getCompletedDate(yearFileMonthIncludedStructuralElement, year);
            IncludedStructuralElement processMonthIncludedStructuralElement = computeIfAbsent(
                processYearIncludedStructuralElement, monthSimpleMetadataView, month);
            MetadataEditor.writeMetadataEntry(metaFileMonthIncludedStructuralElement, monthSimpleMetadataView, month);

            createLinkStructureAndCopyDays(process, yearFileMonthIncludedStructuralElement,
                metaFileMonthIncludedStructuralElement, month, processMonthIncludedStructuralElement);
        }
    }

    private void createLinkStructureAndCopyDays(Process process,
            IncludedStructuralElement yearFileMonthIncludedStructuralElement,
            IncludedStructuralElement metaFileMonthIncludedStructuralElement, String month,
            IncludedStructuralElement processMonthIncludedStructuralElement) {

        for (Iterator<IncludedStructuralElement> yearFileDayIncludedStructuralElementsIterator = yearFileMonthIncludedStructuralElement
                .getChildren()
                .iterator(), metaFileDayIncludedStructuralElementsIterator = metaFileMonthIncludedStructuralElement
                        .getChildren().iterator(); yearFileDayIncludedStructuralElementsIterator.hasNext()
                                && metaFileDayIncludedStructuralElementsIterator.hasNext();) {
            IncludedStructuralElement yearFileDayIncludedStructuralElement = yearFileDayIncludedStructuralElementsIterator
                    .next();
            IncludedStructuralElement metaFileDayIncludedStructuralElement = metaFileDayIncludedStructuralElementsIterator
                    .next();
            String day = getCompletedDate(yearFileDayIncludedStructuralElement, month);
            IncludedStructuralElement processDayIncludedStructuralElement = computeIfAbsent(
                processMonthIncludedStructuralElement, daySimpleMetadataView, day);
            MetadataEditor.writeMetadataEntry(metaFileDayIncludedStructuralElement, daySimpleMetadataView, day);

            createLinkStructureOfIssues(process, yearFileDayIncludedStructuralElement,
                processDayIncludedStructuralElement);
        }
    }

    private void createLinkStructureOfIssues(Process process,
            IncludedStructuralElement yearFileDayIncludedStructuralElement,
            IncludedStructuralElement processDayIncludedStructuralElement) {

        int numberOfIssues = yearFileDayIncludedStructuralElement.getChildren().size();
        for (int index = 0; index < numberOfIssues; index++) {
            MetadataEditor.addLink(processDayIncludedStructuralElement, process.getId());
        }
    }

    /**
     * Finds the included structural element with the specified label, if it
     * exists, otherwise it creates.
     *
     * @param includedStructuralElement
     *            parent included structural element
     * @param simpleMetadataView
     *            indication which metadata value is used to store the value
     * @param value
     *            the value
     * @return child with value
     */
    private static IncludedStructuralElement computeIfAbsent(IncludedStructuralElement includedStructuralElement,
            SimpleMetadataViewInterface simpleMetadataView, String value) {

        int index = 0;
        for (IncludedStructuralElement child : includedStructuralElement.getChildren()) {
            String firstSimpleMetadataValue = MetadataEditor.readSimpleMetadataValues(child, simpleMetadataView).get(0);
            int comparison = firstSimpleMetadataValue.compareTo(value);
            if (comparison <= -1) {
                index++;
            } else if (comparison == 0) {
                return child;
            } else {
                break;
            }
        }
        IncludedStructuralElement computed = new IncludedStructuralElement();
        MetadataEditor.writeMetadataEntry(computed, simpleMetadataView, value);
        includedStructuralElement.getChildren().add(index, computed);
        return computed;
    }

    /**
     * Adds a date to get a complete date. In Production versions before 2.2,
     * the date is stored incompletely (as an integer). This is supplemented to
     * ISO if found. Otherwise just returns the date.
     *
     * @param includedStructuralElement
     *            the included structural element that contains the date
     * @param previousLevel
     *            previous part of date
     * @return ISO date
     */
    private static String getCompletedDate(IncludedStructuralElement includedStructuralElement, String previousLevel) {
        String date = MetadataEditor.getMetadataValue(includedStructuralElement, FIELD_TITLE_SORT);
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

        ProcessGenerator processGenerator = new ProcessGenerator();
        processGenerator.generateProcess(templateId, projectId);
        overallProcess = processGenerator.getGeneratedProcess();
        overallProcess.setTitle(getTitle());
        ProcessService.checkTasks(overallProcess, overallWorkpiece.getRootElement().getType());
        processService.saveToDatabase(overallProcess);
        ServiceManager.getFileService().createProcessLocation(overallProcess);
        overallWorkpiece.setId(overallProcess.getId().toString());
        overallWorkpiece.getRootElement().getMetadata().addAll(overallMetadata);
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
     * @throws DataException
     *             if there is an error saving the process
     * @throws DAOException
     *             if a process cannot be load from the database
     */
    public void createNextYearProcess() throws ProcessGenerationException, IOException, DAOException,
            CommandException {
        final long begin = System.nanoTime();
        Entry<String, IncludedStructuralElement> yearToCreate = yearsIterator.next();
        String yearTitle = getYearTitle(yearToCreate.getKey());
        logger.info("Creating process for year {}, {}...", yearToCreate.getKey(), yearTitle);
        ProcessGenerator processGenerator = new ProcessGenerator();
        processGenerator.generateProcess(templateId, projectId);
        Process yearProcess = processGenerator.getGeneratedProcess();
        yearProcess.setTitle(yearTitle);
        ProcessService.checkTasks(yearProcess, yearToCreate.getValue().getType());
        processService.saveToDatabase(yearProcess);

        MetadataEditor.addLink(overallWorkpiece.getRootElement(), yearProcess.getId());
        if (!yearsIterator.hasNext()) {
            metsService.saveWorkpiece(overallWorkpiece, fileService.getMetadataFilePath(overallProcess, false, false));
        }

        yearProcess.setParent(overallProcess);
        overallProcess.getChildren().add(yearProcess);
        processService.saveToDatabase(yearProcess);

        ServiceManager.getFileService().createProcessLocation(yearProcess);

        createYearWorkpiece(yearToCreate, yearTitle, yearProcess);

        Collection<Integer> childIds = yearsChildren.get(yearToCreate.getKey());
        for (Integer childId : childIds) {
            Process child = processService.getById(childId);
            child.setParent(yearProcess);
            yearProcess.getChildren().add(child);
            processService.saveToDatabase(child);
        }
        processService.saveToDatabase(yearProcess);
        addToBatch(yearProcess);

        logger.info("Process {} (ID {}) successfully created.", yearProcess.getTitle(), yearProcess.getId());
        if (logger.isTraceEnabled()) {
            logger.trace("Creating {} took {} ms.", yearProcess.getTitle(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private void createYearWorkpiece(Entry<String, IncludedStructuralElement> yearToCreate, String yearTitle,
                                     Process yearProcess) throws IOException {
        Workpiece yearWorkpiece = new Workpiece();
        yearWorkpiece.setId(yearProcess.getId().toString());
        yearWorkpiece.setRootElement(yearToCreate.getValue());
        StructuralElementViewInterface newspaperView = rulesetManagement.getStructuralElementView(
            yearWorkpiece.getRootElement().getType(), acquisitionStage, Locale.LanguageRange.parse("de"));
        final Collection<String> processTitleKeys = rulesetManagement.getFunctionalKeys(FunctionalMetadata.PROCESS_TITLE);
        newspaperView.getAllowedMetadata().parallelStream().filter(SimpleMetadataViewInterface.class::isInstance)
                .map(SimpleMetadataViewInterface.class::cast)
                .filter(metadataView -> processTitleKeys.contains(metadataView.getId())).collect(Collectors.toList())
                .forEach(yearView -> MetadataEditor.writeMetadataEntry(yearWorkpiece.getRootElement(), yearView, yearTitle));
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
        processService.saveToDatabase(process);
        batchService.saveToDatabase(batch);
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
        return title + '_' + year.replace("/", "--");
    }

    /**
     * Returns whether there are more years to create.
     *
     * @return whether there are more years
     */
    public boolean hasNextYear() {
        if (Objects.isNull(yearsIterator)) {
            yearsIterator = Iterators.peekingIterator(years.entrySet().iterator());
        }
        return yearsIterator.hasNext();
    }
}

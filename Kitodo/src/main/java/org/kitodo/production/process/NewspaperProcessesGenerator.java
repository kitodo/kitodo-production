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

package org.kitodo.production.process;

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.config.ConfigProject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.IndividualIssue;
import org.kitodo.production.process.field.AdditionalField;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.RulesetService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;

/**
 * A generator for newspaper processes.
 */
public class NewspaperProcessesGenerator extends ProcessGenerator {
    private static final Logger logger = LogManager.getLogger(NewspaperProcessesGenerator.class);

    /**
     * Language for the ruleset. Since we run headless here, English only.
     */
    public static final List<LanguageRange> ENGLISH = LanguageRange.parse("en");

    /**
     * Number of steps the long-running task has to go through for
     * initialization.
     */
    private static final int NUMBER_OF_INIT_STEPS = 1;

    /**
     * Number of steps the long-running task must complete to complete.
     */
    private static final int NUMBER_OF_COMPLETION_STEPS = 1;

    /**
     * Date format pattern indicating a double year. A double year is a time
     * span with the length of one year, starting on a day different from
     * January 1ˢᵗ and spanning two complementary parts of two subsequent
     * calendar years.
     */
    private static final String PATTERN_DOUBLE_YEAR = "yyyy/yyyy";

    /**
     * This class requires service for files.
     */
    private final FileService fileService = ServiceManager.getFileService();

    /**
     * This class requires service for METS.
     */
    private final MetsService metsService = ServiceManager.getMetsService();

    /**
     * This class requires service for process.
     */
    private final ProcessService processService = ServiceManager.getProcessService();

    /**
     * This class requires service for rule definitions.
     */
    private final RulesetService rulesetService = ServiceManager.getRulesetService();

    /**
     * This is the supreme process of the newspaper.
     */
    private final Process overallProcess;

    /**
     * The appearance history for which operations are to be created.
     */
    private final Course course;

    /**
     * The current step. This class operates step by step and the long running
     * task can always be paused between two steps in Task Manager.
     */
    private int currentStep = 0;

    /**
     * Specifies which year is currently being processed.
     */
    private String currentYear;

    /**
     * An interface view for simple metadata that maps the date of the day.
     */
    private DatesSimpleMetadataViewInterface daySimpleMetadataView;

    /**
     * Which included structural element type are the days.
     */
    private String dayType;

    /**
     * Which included structural element type are the issues.
     */
    private String issueType;

    /**
     * An interface view for simple metadata that maps the date of the month.
     */
    private DatesSimpleMetadataViewInterface monthSimpleMetadataView;

    /**
     * Which included structural element type are the months.
     */
    private String monthType;

    /**
     * Uniform resource identifier of the location of the serialization of the
     * overall media presentation description.
     */
    private URI overallMetadataFileUri;

    /**
     * Object model of the overall media presentation description.
     */
    private Workpiece overallWorkpiece;

    /**
     * List of processes to be created. A process is characterized here only by
     * the issues contained therein.
     */
    private List<List<IndividualIssue>> processesToCreate;

    /**
     * A build statement for the process title, which can be interpreted by the
     * title generator.
     */
    private String titleDefinition;

    /**
     * The title generator is used to create the process titles.
     */
    private TitleGenerator titleGenerator;

    /**
     * Uniform resource identifier of the location of the serialization of the
     * annual media presentation description.
     */
    private URI yearMetadataFileUri;

    /**
     * This is the annual process which is currently being processed.
     */
    private Process yearProcess;

    /**
     * An interface view for simple metadata that maps the date of the year.
     */
    private DatesSimpleMetadataViewInterface yearSimpleMetadataView;

    /**
     * Which included structural element type are the years.
     */
    private String yearType;

    /**
     * Object model of the year media presentation description.
     */
    private Workpiece yearWorkpiece;

    /**
     * Creates a new newspaper process generator.
     *
     * @param overallProcess
     *            Process that represents the entirety of the newspaper
     * @param course
     *            object model of the course of the issue
     */
    public NewspaperProcessesGenerator(Process overallProcess, Course course) {
        this.overallProcess = overallProcess;
        this.course = course;
    }

    /**
     * Returns the progress of the newspaper processes generator.
     *
     * @return the progress
     */
    public int getProgress() {
        return currentStep;
    }

    /**
     * Returns the number of steps of the newspaper processes generator. Note
     * that the number of steps may change in the future and needs to be
     * updated.
     *
     * @return the number of steps
     */
    public int getNumberOfSteps() {
        return NUMBER_OF_INIT_STEPS + (Objects.isNull(processesToCreate) ? 0 : processesToCreate.size())
                + NUMBER_OF_COMPLETION_STEPS;
    }

    /**
     * Works the next step of the long-running task.
     *
     * @throws ConfigurationException
     *             if the configuration is wrong
     * @throws DAOException
     *             if an error occurs while saving in the database
     * @throws DataException
     *             if an error occurs while saving in the database
     * @throws IOException
     *             if something goes wrong when reading or writing one of the
     *             affected files
     * @throws ProcessGenerationException
     *             if there is a "CurrentNo" item in the projects configuration,
     *             but its value cannot be evaluated to an integer
     */
    public void nextStep()
            throws ConfigurationException, DAOException, DataException, IOException, ProcessGenerationException, DoctypeMissingException {

        if (currentStep == 0) {
            initialize();
        } else if (currentStep - NUMBER_OF_INIT_STEPS < processesToCreate.size()) {
            createProcess(currentStep - NUMBER_OF_INIT_STEPS);
        } else {
            finish();
        }
        currentStep++;
    }

    /**
     * Initializes the newspaper process generator.
     *
     * @throws ConfigurationException
     *             if the configuration is wrong
     * @throws IOException
     *             if something goes wrong when reading or writing one of the
     *             affected files
     */
    private void initialize() throws ConfigurationException, IOException, DoctypeMissingException {
        final long begin = System.nanoTime();

        overallMetadataFileUri = processService.getMetadataFileUri(overallProcess);
        overallWorkpiece = metsService.loadWorkpiece(overallMetadataFileUri);

        initializeRulesetFields(overallWorkpiece.getRootElement().getType());

        ConfigProject configProject = new ConfigProject(overallProcess.getProject().getTitle());

        Collection<MetadataViewInterface> addableDivisions = rulesetService.openRuleset(overallProcess.getRuleset())
                .getStructuralElementView(overallWorkpiece.getRootElement().getType(), "", ENGLISH)
                .getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

        titleGenerator = initializeTitleGenerator(configProject, overallWorkpiece , addableDivisions);
        titleDefinition = configProject.getTitleDefinition();

        processesToCreate = course.getProcesses();

        if (logger.isTraceEnabled()) {
            logger.trace("Initialization took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * Initializes the class fields related that hold information to be obtained
     * from the ruleset.
     *
     * @param newspaperType
     *            ruleset type of the overall newspaper process
     * @throws ConfigurationException
     *             if the configuration is wrong
     * @throws IOException
     *             if something goes wrong when reading or writing one of the
     *             affected files
     */
    private void initializeRulesetFields(String newspaperType) throws ConfigurationException, IOException {
        RulesetManagementInterface ruleset = rulesetService.openRuleset(overallProcess.getRuleset());
        StructuralElementViewInterface newspaperView = ruleset.getStructuralElementView(newspaperType, "", ENGLISH);
        StructuralElementViewInterface yearDivisionView = nextSubView(ruleset, newspaperView);
        yearSimpleMetadataView = yearDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        yearType = yearDivisionView.getId();
        StructuralElementViewInterface monthDivisionView = nextSubView(ruleset, yearDivisionView);
        monthSimpleMetadataView = monthDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        monthType = monthDivisionView.getId();
        StructuralElementViewInterface dayDivisionView = nextSubView(ruleset, monthDivisionView);
        daySimpleMetadataView = dayDivisionView.getDatesSimpleMetadata().orElseThrow(ConfigurationException::new);
        dayType = dayDivisionView.getId();
        issueType = nextSubView(ruleset, dayDivisionView).getId();
    }

    /**
     * Returns the next sub-view relative to given view from the ruleset.
     *
     * @param ruleset
     *            ruleset to return the sub-view from
     * @param superiorView
     *            relative superior view
     * @return the sub-view
     */
    public static StructuralElementViewInterface nextSubView(RulesetManagementInterface ruleset,
            StructuralElementViewInterface superiorView) {

        Map<String, String> allowedSubstructuralElements = superiorView.getAllowedSubstructuralElements();
        String subType = allowedSubstructuralElements.entrySet().iterator().next().getKey();
        return ruleset.getStructuralElementView(subType, "", ENGLISH);
    }

    /**
     * Initializes the title generator.
     *
     * @param configProject
     *            the config project
     * @param addableDivisions
     *            addable Metadata views
     * @return the initialized title generator
     */
    private static TitleGenerator initializeTitleGenerator(ConfigProject configProject, Workpiece workpiece,
                                                           Collection<MetadataViewInterface> addableDivisions) throws DoctypeMissingException {

        IncludedStructuralElement rootElement = workpiece.getRootElement();
        Map<String, Map<String, String>> metadata = new HashMap<>(4);
        Map<String, String> topstruct = getMetadataEntries(rootElement.getMetadata());
        metadata.put("topstruct", topstruct);
        List<IncludedStructuralElement> children = rootElement.getChildren();
        metadata.put("firstchild",
            children.isEmpty() ? Collections.emptyMap() : getMetadataEntries(children.get(0).getMetadata()));
        metadata.put("physSequence", getMetadataEntries(workpiece.getMediaUnit().getMetadata()));

        String docType = null;
        for (ConfigOpacDoctype configOpacDoctype : ConfigOpac.getAllDoctypes()) {
            if (configOpacDoctype.getRulesetType().equals(rootElement.getType())) {
                docType = configOpacDoctype.getTitle();
                break;
            }
        }

        List<AdditionalField> projectAdditionalFields = configProject.getAdditionalFields();
        ProcessFieldedMetadata table = new ProcessFieldedMetadata();
        for (AdditionalField additionalField : projectAdditionalFields) {
            if (isDocTypeAndNotIsNotDoctype(additionalField, docType)) {
                String value = metadata.getOrDefault(additionalField.getDocStruct(), Collections.emptyMap())
                        .get(additionalField.getMetadata());
                List<MetadataViewInterface> filteredViews = addableDivisions
                        .stream()
                        .filter(v -> v.getId().equals(additionalField.getMetadata()))
                        .collect(Collectors.toList());
                if (!filteredViews.isEmpty()) {
                    MetadataEntry metadataEntry = new MetadataEntry();
                    metadataEntry.setValue(value);
                    if (filteredViews.get(0).isComplex()) {
                        table.createMetadataGroupPanel((ComplexMetadataViewInterface) filteredViews.get(0),
                            Collections.singletonList(metadataEntry));
                    } else {
                        table.createMetadataEntryEdit((SimpleMetadataViewInterface) filteredViews.get(0),
                            Collections.singletonList(metadataEntry));
                    }
                }
            }
        }
        return new TitleGenerator(topstruct.getOrDefault("TSL_ATS", ""), table.getRows());
    }

    /**
     * Returns whether an additional field is assigned to the doc type and not
     * excluded from it. The {@code isDocType}s and {@code isNotDoctype}s are a
     * list as a string, separated by a horizontal line ({@code |}, U+007C).
     *
     * @param additionalField
     *            the field in question
     * @param docType
     *            the doc type used
     * @return whether the field is assigned and not excluded
     */
    private static boolean isDocTypeAndNotIsNotDoctype(AdditionalField additionalField, String docType) {
        boolean isDocType = false;
        boolean isNotDoctype = false;
        String isDocTypes = additionalField.getIsDocType();
        if (Objects.nonNull(isDocTypes)) {
            for (String isDocTypeOption : isDocTypes.split("\\|")) {
                if (isDocTypeOption.equals(docType)) {
                    isDocType = true;
                    break;
                }
            }
        }
        String isNotDoctypes = additionalField.getIsNotDoctype();
        if (Objects.nonNull(isNotDoctypes)) {
            for (String isNotDoctypeOption : isNotDoctypes.split("\\|")) {
                if (isNotDoctypeOption.equals(docType)) {
                    isNotDoctype = true;
                    break;
                }
            }
        }
        return isDocType ^ !isNotDoctype;
    }

    /**
     * Reduces the metadata to metadata entries and returns them as a map.
     *
     * @param metadata
     *            all the metadata
     * @return the metadata entries as map
     */
    private static Map<String, String> getMetadataEntries(Collection<Metadata> metadata) {
        Map<String, String> metadataEntries = metadata.parallelStream().filter(MetadataEntry.class::isInstance)
                .map(MetadataEntry.class::cast).collect(Collectors.toMap(Metadata::getKey, MetadataEntry::getValue));
        return metadataEntries;
    }

    private void createProcess(int index) throws DAOException, DataException, IOException, ProcessGenerationException {
        final long begin = System.nanoTime();

        List<IndividualIssue> individualIssuesForProcess = processesToCreate.get(index);
        if (individualIssuesForProcess.isEmpty()) {
            return;
        }

        IndividualIssue firstIssue = individualIssuesForProcess.get(0);
        prepareTheAppropriateYearProcess(dateMark(yearSimpleMetadataView.getScheme(), firstIssue.getDate()));

        generateProcess(overallProcess.getTemplate().getId(), overallProcess.getProject().getId());
        String title = titleGenerator.generateTitle(titleDefinition, firstIssue.getGenericFields());
        getGeneratedProcess().setTitle(title);
        processService.save(getGeneratedProcess());
        processService.refresh(getGeneratedProcess());
        getGeneratedProcess().setParent(yearProcess);
        yearProcess.getChildren().add(getGeneratedProcess());
        createMetadataFileForProcess(individualIssuesForProcess);
        processService.save(getGeneratedProcess());

        if (logger.isTraceEnabled()) {
            logger.trace("Creating newspaper process {} took {} ms", title,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private void createMetadataFileForProcess(List<IndividualIssue> individualIssues) throws IOException {

        IncludedStructuralElement rootElement = new IncludedStructuralElement();
        MetadataEntry dateMetadataEntry = new MetadataEntry();
        dateMetadataEntry.setKey(monthSimpleMetadataView.getId());
        dateMetadataEntry.setValue(dateMark(monthSimpleMetadataView.getScheme(), individualIssues.get(0).getDate()));
        rootElement.getMetadata().add(dateMetadataEntry);

        for (IndividualIssue individualIssue : individualIssues) {

            String monthMark = dateMark(monthSimpleMetadataView.getScheme(), individualIssue.getDate());
            IncludedStructuralElement yearMonth = getOrCreateIncludedStructuralElement(yearWorkpiece.getRootElement(),
                monthType, monthSimpleMetadataView, monthMark);
            String dayMark = dateMark(daySimpleMetadataView.getScheme(), individualIssue.getDate());
            IncludedStructuralElement processDay = getOrCreateIncludedStructuralElement(rootElement, null,
                daySimpleMetadataView, dayMark);
            final IncludedStructuralElement yearDay = getOrCreateIncludedStructuralElement(yearMonth, dayType,
                daySimpleMetadataView, dayMark);

            IncludedStructuralElement processIssue = new IncludedStructuralElement();
            processIssue.setType(issueType);
            for (Pair<String, String> metadata : individualIssue.getMetadata(
                yearSimpleMetadataView.getYearBegin().getMonthValue(),
                yearSimpleMetadataView.getYearBegin().getDayOfMonth())) {
                MetadataEntry metadataEntry = new MetadataEntry();
                metadataEntry.setKey(metadata.getKey());
                metadataEntry.setValue(metadata.getValue());
                processIssue.getMetadata().add(metadataEntry);
            }
            processDay.getChildren().add(processIssue);

            IncludedStructuralElement yearIssue = new IncludedStructuralElement();
            LinkedMetsResource linkToProcess = new LinkedMetsResource();
            linkToProcess.setLoctype("Kitodo.Production");
            linkToProcess.setUri(processService.getProcessURI(getGeneratedProcess()));
            yearIssue.setLink(linkToProcess);
            yearDay.getChildren().add(yearIssue);
        }

        Workpiece workpiece = new Workpiece();
        workpiece.setRootElement(rootElement);
        fileService.createProcessLocation(getGeneratedProcess());
        final URI metadataFileUri = processService.getMetadataFileUri(getGeneratedProcess());
        metsService.saveWorkpiece(workpiece, metadataFileUri);
    }

    /**
     * Creates a date indicator according to the specified schema for the given
     * date. Normally, the date indicator is generated with the date formatter.
     * Exception is the indication of a double year. In this case, the
     * information depends on the beginning of the year and must first be
     * determined.
     *
     * @param scheme
     *            scheme for the date indicator
     * @param date
     *            date for the date mark
     * @return the formatted date indicator
     */
    private String dateMark(String scheme, LocalDate date) {
        if (PATTERN_DOUBLE_YEAR.equals(scheme)) {
            int firstYear = date.getYear();
            MonthDay yearBegin = yearSimpleMetadataView.getYearBegin();
            LocalDate yearStartThisYear = LocalDate.of(firstYear, yearBegin.getMonth(), yearBegin.getDayOfMonth());
            if (date.isBefore(yearStartThisYear)) {
                firstYear--;
            }
            return String.format("%04d/%04d", firstYear, firstYear + 1);
        } else {
            DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern(scheme);
            return yearFormatter.format(date);
        }
    }

    private void prepareTheAppropriateYearProcess(String yearMark)
            throws DAOException, DataException, ProcessGenerationException, IOException {

        if (yearMark.equals(currentYear)) {
            return;
        } else if (Objects.nonNull(currentYear)) {
            saveAndCloseCurrentYearProcess();
        }
        if (!openExistingYearProcess(yearMark)) {
            createNewYearProcess(yearMark);
        }
    }

    private void saveAndCloseCurrentYearProcess() throws DataException, IOException {
        final long begin = System.nanoTime();

        metsService.saveWorkpiece(yearWorkpiece, yearMetadataFileUri);
        processService.save(yearProcess);

        this.yearProcess = null;
        this.yearWorkpiece = null;
        this.yearMetadataFileUri = null;
        String year = currentYear;
        this.currentYear = null;

        if (logger.isTraceEnabled()) {
            logger.trace("Saving year process for {} took {} ms", year,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private boolean openExistingYearProcess(String yearMark)
            throws DAOException, DataException, ProcessGenerationException, IOException {
        final long begin = System.nanoTime();

        boolean couldOpenExistingProcess = false;
        for (IncludedStructuralElement firstLevelChild : overallWorkpiece.getRootElement().getChildren()) {
            LinkedMetsResource firstLevelChildLink = firstLevelChild.getLink();
            if (Objects.isNull(firstLevelChildLink)) {
                continue;
            }
            Process linkedProcess = processService
                    .getById(processService.processIdFromUri(firstLevelChildLink.getUri()));
            URI metadataFileUri = processService.getMetadataFileUri(linkedProcess);
            Workpiece workpiece = metsService.loadWorkpiece(metadataFileUri);
            MetadataEntry yearMetadataEntry = null;
            for (Metadata metadata : workpiece.getRootElement().getMetadata()) {
                if (metadata.getKey().equals(yearSimpleMetadataView.getId()) && metadata instanceof MetadataEntry) {
                    yearMetadataEntry = (MetadataEntry) metadata;
                    break;
                }
            }
            if (Objects.isNull(yearMetadataEntry)) {
                continue;
            }
            couldOpenExistingProcess = yearMetadataEntry.getValue().equals(yearMark);
            if (couldOpenExistingProcess) {
                this.yearProcess = linkedProcess;
                this.yearWorkpiece = workpiece;
                this.yearMetadataFileUri = metadataFileUri;
                this.currentYear = yearMark;
                break;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Searching year process for {} took {} ms", yearMark,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return couldOpenExistingProcess;
    }

    private void createNewYearProcess(String yearMark) throws ProcessGenerationException, DataException, IOException {
        final long begin = System.nanoTime();

        generateProcess(overallProcess.getTemplate().getId(), overallProcess.getProject().getId());
        getGeneratedProcess().setTitle(overallProcess.getTitle() + '_' + yearMark.replace('/', '-'));
        processService.save(getGeneratedProcess());
        processService.refresh(getGeneratedProcess());

        getGeneratedProcess().setParent(overallProcess);
        overallProcess.getChildren().add(getGeneratedProcess());
        processService.save(getGeneratedProcess());

        fileService.createProcessLocation(getGeneratedProcess());
        final URI metadataFileUri = processService.getMetadataFileUri(getGeneratedProcess());

        IncludedStructuralElement newYearChild = new IncludedStructuralElement();
        LinkedMetsResource link = new LinkedMetsResource();
        link.setLoctype("Kitodo.Production");
        link.setUri(processService.getProcessURI(getGeneratedProcess()));
        newYearChild.setLink(link);
        overallWorkpiece.getRootElement().getChildren().add(newYearChild);

        IncludedStructuralElement rootElement = new IncludedStructuralElement();
        rootElement.setType(yearType);
        MetadataEntry dateMetadataEntry = new MetadataEntry();
        dateMetadataEntry.setKey(yearSimpleMetadataView.getId());
        dateMetadataEntry.setValue(yearMark);
        rootElement.getMetadata().add(dateMetadataEntry);
        Workpiece workpiece = new Workpiece();
        workpiece.setRootElement(rootElement);

        this.yearProcess = getGeneratedProcess();
        this.yearWorkpiece = workpiece;
        this.yearMetadataFileUri = metadataFileUri;
        this.currentYear = yearMark;

        if (logger.isTraceEnabled()) {
            logger.trace("Creating year process for {} took {} ms", yearMark,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    private IncludedStructuralElement getOrCreateIncludedStructuralElement(
            IncludedStructuralElement includedStructuralElement, String childType,
            SimpleMetadataViewInterface identifierMetadata,
            String identifierMetadataValue) {

        for (IncludedStructuralElement child : includedStructuralElement.getChildren()) {
            if (MetadataEditor.readSimpleMetadataValues(child, identifierMetadata).contains(identifierMetadataValue)) {
                return child;
            }
        }

        IncludedStructuralElement createdChild = new IncludedStructuralElement();
        createdChild.setType(childType);
        MetadataEditor.writeMetadataEntry(createdChild, identifierMetadata, identifierMetadataValue);
        includedStructuralElement.getChildren().add(createdChild);
        return createdChild;
    }

    private void finish() throws DataException, IOException {
        final long begin = System.nanoTime();

        saveAndCloseCurrentYearProcess();
        metsService.saveWorkpiece(overallWorkpiece, overallMetadataFileUri);
        processService.save(overallProcess);

        if (logger.isTraceEnabled()) {
            logger.trace("Finish took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }
}

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

package org.kitodo.production.helper.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.goobi.mq.processors.CreateNewProcessProcessor;
import org.joda.time.LocalDate;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetsModsImportExportInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessCreationException;
import org.kitodo.production.forms.ProzesskopieForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.CourseToGerman;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.model.bibliography.course.IndividualIssue;
import org.kitodo.production.services.ServiceManager;

/**
 * The class CreateNewspaperProcessesTask is a LongRunningTask to create
 * processes from a course of appearance.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CreateNewspaperProcessesTask extends EmptyTask {

    /**
     * The field batchLabel is set in addToBatches() on the first function call
     * which finds it to be null, and is used and set back to null in
     * flushLogisticsBatch() to create the batches’ specific part of the
     * identifier (put in parentheses behind the shared part).
     */
    private String batchLabel;

    /**
     * The field createBatches holds a granularity level that is used to create
     * batches out of the given processes. The field may be null which disables
     * the feature.
     */
    private final Granularity createBatches;

    /**
     * The field currentBreakMark holds an integer hash value which, for a given
     * Granularity, shall indicate for two neighboring processes whether they
     * form the same logistics batch (break mark is equal) or to different
     * processes (break mark differs).
     */
    private Integer currentBreakMark;

    /**
     * The field fullBatch holds a batch that all issues will be assigned to.
     */
    private Batch fullBatch = new Batch(Type.NEWSPAPER);

    /**
     * The field logisticsBatch holds a batch that all issues of the same
     * logistics unit will be assigned to.
     */
    private Batch logisticsBatch = new Batch(Type.LOGISTIC);

    /**
     * The field nextProcessToCreate holds the index of the next process to
     * create. Because long running tasks are interruptible is a field so the
     * thread will continue to work with the next process after being continued.
     */
    private int nextProcessToCreate;

    /**
     * The field numberOfProcesses holds the processes’ size to prevent calling
     * size() over and over again.
     */
    private final int numberOfProcesses;

    /**
     * The field pattern holds a ProzesskopieForm instance that will be used as
     * pattern for the creation of processes.
     */
    private final ProzesskopieForm pattern;

    /**
     * The field processes holds a List of List of IndividualIssue objects that
     * processes will be created from. Each list object, which is a list itself,
     * represents a process to create. Each process can consist of many issues
     * which will be part of that process.
     */
    private final List<List<IndividualIssue>> processes;

    /**
     * The field description holds a verbal description of the course of
     * appearance.
     */
    private final List<String> description;

    /**
     * The class CreateNewspaperProcessesTask is a LongRunningTask to create
     * processes from a course of appearance.
     *
     * @param pattern
     *            a ProzesskopieForm to use for creating processes
     * @param course
     *            course of appearance to create processes for
     * @param batchGranularity
     *            a granularity level at which batches shall be created
     */
    public CreateNewspaperProcessesTask(ProzesskopieForm pattern, Course course, Granularity batchGranularity) {
        super(pattern.getProzessVorlageTitel());
        this.pattern = pattern;
        this.processes = new ArrayList<>(course.getNumberOfProcesses());
        this.description = CourseToGerman.asReadableText(course);
        this.createBatches = batchGranularity;
        for (List<IndividualIssue> issues : course.getProcesses()) {
            List<IndividualIssue> process = new ArrayList<>(issues.size());
            process.addAll(issues);
            processes.add(process);
        }
        nextProcessToCreate = 0;
        numberOfProcesses = processes.size();
    }

    /**
     * The copy constructor creates a new thread from a given one. This is
     * required to call the copy constructor of the parent.
     *
     * @param master
     *            copy master
     */
    public CreateNewspaperProcessesTask(CreateNewspaperProcessesTask master) {
        super(master);
        this.pattern = master.pattern;
        this.processes = master.processes;
        this.description = master.description;
        this.createBatches = master.createBatches;
        this.logisticsBatch = master.logisticsBatch;
        this.currentBreakMark = master.currentBreakMark;
        this.batchLabel = master.batchLabel;
        this.fullBatch = master.fullBatch;
        this.nextProcessToCreate = master.nextProcessToCreate;
        this.numberOfProcesses = master.numberOfProcesses;
    }

    /**
     * The function run() is the main function of this task (which is a thread).
     *
     * <p>
     * It will create a new process for each entry from the field “processes”.
     * </p>
     *
     * <p>
     * Therefore it makes use of
     * CreateNewProcessProcessor.newProcessFromTemplate() to once again load a
     * ProzesskopieForm from Hibernate for each process to create, sets the
     * required fields accordingly, then triggers the calculation of the process
     * title and finally initiates the process creation one by one. The
     * statusProgress variable is being updated to show the operator how far the
     * task has proceeded.
     * </p>
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        String currentTitle = null;
        try {
            while (nextProcessToCreate < numberOfProcesses) {
                List<IndividualIssue> issues = processes.get(nextProcessToCreate);
                if (!issues.isEmpty()) {
                    ProzesskopieForm newProcess = CreateNewProcessProcessor
                            .newProcessFromTemplate(pattern.getTemplate().getTitle());
                    newProcess.setDigitalCollections(pattern.getDigitalCollections());
                    newProcess.setDocType(pattern.getDocType());
                    newProcess.setAdditionalFields(pattern.getAdditionalFields());
                    currentTitle = newProcess.generateTitle(issues.get(0).getGenericFields());
                    if (currentTitle.equals("")) {
                        setException(new ProcessCreationException(
                                "Couldn’t create process title for issue " + issues.get(0).toString()));
                        return;
                    }
                    setWorkDetail(currentTitle);

                    if (newProcess.getFileformat() == null) {
                        newProcess.createNewFileformat();
                    }
                    createLogicalStructure(newProcess, issues, StringUtils.join(description, "\n\n"));

                    if (isInterrupted()) {
                        return;
                    }
                    String state = newProcess.createNewProcess();
                    if (Objects.isNull(state) || !state.equals("NewProcess/Page3")) {
                        throw new ProcessCreationException(Helper.getLastMessage().replaceFirst(":\\?*$", ""));
                    }
                    addToBatches(newProcess.getProzessKopie(), issues, currentTitle);
                }
                nextProcessToCreate++;
                setProgress((100 * nextProcessToCreate) / (numberOfProcesses + 2));
                if (isInterrupted()) {
                    return;
                }
            }
            flushLogisticsBatch(currentTitle);
            setProgress(((100 * nextProcessToCreate) + 1) / (numberOfProcesses + 2));
            saveFullBatch(currentTitle);
            setProgress(100);
        } catch (DataException | RuntimeException e) {
            String message = currentTitle != null
                    ? Helper.getTranslation("createNewspaperProcessesTask.MetadataNotAllowedException",
                        currentTitle)
                    : e.getClass().getSimpleName();
            setException(new ProcessCreationException(message + ": " + e.getMessage(), e));
        }
    }

    /**
     * The function createFirstChild() creates the first level of the logical
     * document structure available at the given parent.
     *
     * @param docStruct
     *            level of the logical document structure to create a child in
     * @param document
     *            document to create the child in
     * @param ruleset
     *            rule set the document is based on
     * @return the created child
     */
    private DocStructInterface createFirstChild(DocStructInterface docStruct, DigitalDocumentInterface document,
            PrefsInterface ruleset) {

        String firstAddable = null;
        try {
            firstAddable = docStruct.getDocStructType().getAllAllowedDocStructTypes().get(0);
            return docStruct.createChild(firstAddable, document, ruleset);
        } catch (TypeNotAllowedAsChildException | TypeNotAllowedForParentException | RuntimeException e) {
            StringBuilder message = new StringBuilder();
            message.append("Could not add child ");
            if (firstAddable != null) {
                message.append(firstAddable);
                message.append(' ');
            }
            message.append("to DocStrct");
            if (docStruct.getDocStructType() == null) {
                message.append(" without type");
            } else {
                message.append("Type ");
                message.append(docStruct.getDocStructType().getName());
            }
            message.append(": ");
            if (e instanceof NullPointerException) {
                message.append("No child type available.");
            } else {
                message.append(e.getClass().getSimpleName());
            }
            throw new ProcessCreationException(message.toString(), e);
        }
    }

    /**
     * Creates a logical structure tree in the process under creation. In the
     * tree, all issues will have been created. Presumption is that never issues
     * for more than one year will be added to the same process.
     *
     * @param newProcess
     *            process under creation
     * @param issues
     *            issues to add
     * @param publicationRun
     *            verbal description of the course of appearance
     */
    private void createLogicalStructure(ProzesskopieForm newProcess, List<IndividualIssue> issues,
            String publicationRun) {

        // initialise
        PrefsInterface ruleset = ServiceManager.getRulesetService()
                .getPreferences(newProcess.getProzessKopie().getRuleset());
        DigitalDocumentInterface document;
        try {
            document = newProcess.getFileformat().getDigitalDocument();
        } catch (PreferencesException e) {
            throw new ProcessCreationException(e.getMessage(), e);
        }
        DocStructInterface newspaper = document.getLogicalDocStruct();

        // try to add the publication run
        addMetadatum(newspaper, "PublicationRun", publicationRun, false);

        // create the year level
        DocStructInterface year = createFirstChild(newspaper, document, ruleset);
        String theYear = Integer.toString(issues.get(0).getDate().getYear());
        addMetadatum(year, MetsModsImportExportInterface.CREATE_LABEL_ATTRIBUTE_TYPE, theYear, true);

        // create the month level
        Map<Integer, DocStructInterface> months = new HashMap<>();
        Map<LocalDate, DocStructInterface> days = new HashMap<>(488);
        for (IndividualIssue individualIssue : issues) {
            LocalDate date = individualIssue.getDate();
            Integer monthNo = date.getMonthOfYear();
            if (!months.containsKey(monthNo)) {
                DocStructInterface newMonth = createFirstChild(year, document, ruleset);
                addMetadatum(newMonth, MetsModsImportExportInterface.CREATE_ORDERLABEL_ATTRIBUTE_TYPE,
                    monthNo.toString(), true);
                addMetadatum(newMonth, year.getDocStructType().getName(), theYear, false);
                addMetadatum(newMonth, MetsModsImportExportInterface.CREATE_LABEL_ATTRIBUTE_TYPE, monthNo.toString(),
                    false);
                months.put(monthNo, newMonth);
            }
            DocStructInterface month = months.get(monthNo);

            // create the day level
            if (!days.containsKey(date)) {
                DocStructInterface newDay = createFirstChild(month, document, ruleset);
                addMetadatum(newDay, MetsModsImportExportInterface.CREATE_ORDERLABEL_ATTRIBUTE_TYPE,
                    Integer.toString(date.getDayOfMonth()), true);
                addMetadatum(newDay, year.getDocStructType().getName(), theYear, false);
                addMetadatum(newDay, month.getDocStructType().getName(), Integer.toString(date.getMonthOfYear()),
                    false);
                addMetadatum(newDay, MetsModsImportExportInterface.CREATE_LABEL_ATTRIBUTE_TYPE,
                    Integer.toString(date.getDayOfMonth()), false);
                days.put(date, newDay);
            }
            DocStructInterface day = days.get(date);

            // create the issue
            DocStructInterface issue = createFirstChild(day, document, ruleset);
            String heading = individualIssue.getHeading();
            if ((heading != null) && (heading.trim().length() > 0)) {
                addMetadatum(issue, issue.getDocStructType().getName(), heading, true);
            }
            addMetadatum(issue, year.getDocStructType().getName(), theYear, false);
            addMetadatum(issue, month.getDocStructType().getName(), Integer.toString(date.getMonthOfYear()), false);
            addMetadatum(issue, day.getDocStructType().getName(), Integer.toString(date.getDayOfMonth()), false);
            addMetadatum(issue, MetsModsImportExportInterface.CREATE_LABEL_ATTRIBUTE_TYPE, heading, false);
        }
    }

    /**
     * The function addMetadatum() adds a metadata to the given level of the
     * logical document structure hierarchy.
     *
     * @param level
     *            level of the logical document structure to create a child in
     * @param key
     *            name of the metadata to create
     * @param value
     *            value to set the metadata to
     * @param fail
     *            if true, throws an error on fail, otherwise returns silently
     */
    private void addMetadatum(DocStructInterface level, String key, String value, boolean fail) {
        try {
            level.addMetadata(key, value);
        } catch (MetadataTypeNotAllowedException | RuntimeException e) {
            if (fail) {
                throw new ProcessCreationException("Could not create metadatum " + key + " in "
                        + (level.getDocStructType() != null ? "DocStrctType " + level.getDocStructType().getName()
                                : "anonymous DocStrctType")
                        + ": " + e.getClass().getSimpleName().replace("NullPointerException",
                            "No metadata types are associated with that DocStructType."),
                        e);
            }
        }
    }

    /**
     * The method addToBatches() adds a given process to the allover and the
     * annual batch. If the break mark changes, the logistics batch will be
     * flushed and the process will be added to a new logistics batch.
     *
     * @param process
     *            process to add
     * @param issues
     *            list of individual issues in the process
     * @param processTitle
     *            the title of the process
     */
    private void addToBatches(Process process, List<IndividualIssue> issues, String processTitle) throws DataException {
        if (createBatches != null) {
            int lastIndex = issues.size() - 1;
            int breakMark = issues.get(lastIndex).getBreakMark(createBatches);
            if ((currentBreakMark != null) && (breakMark != currentBreakMark)) {
                flushLogisticsBatch(processTitle);
            }
            if (batchLabel == null) {
                batchLabel = createBatches.format(issues.get(lastIndex).getDate());
            }
            logisticsBatch.getProcesses().add(process);
            currentBreakMark = breakMark;
        }
        fullBatch.getProcesses().add(process);
    }

    /**
     * The method flushLogisticsBatch() sets the title for the logistics batch,
     * saves it to hibernate and then populates the global variable with a new,
     * empty batch.
     *
     * @param processTitle
     *            the title of the process
     */
    private void flushLogisticsBatch(String processTitle) throws DataException {
        if (ServiceManager.getBatchService().size(logisticsBatch) > 0) {
            logisticsBatch.setTitle(firstGroupFrom(processTitle) + " (" + batchLabel + ')');
            ServiceManager.getBatchService().save(logisticsBatch);
            logisticsBatch = new Batch(Type.LOGISTIC);
        }
        currentBreakMark = null;
        batchLabel = null;
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see org.kitodo.production.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("createNewspaperProcessesTask");
    }

    /**
     * The method saveFullBatch() sets the title for the allover batch and saves
     * it to hibernate.
     *
     * @param theProcessTitle
     *            the title of the process
     */
    private void saveFullBatch(String theProcessTitle) throws DataException {
        fullBatch.setTitle(firstGroupFrom(theProcessTitle));
        ServiceManager.getBatchService().save(fullBatch);
    }

    /**
     * The function firstGroupFrom() extracts the first sequence of characters
     * that are no punctuation characters
     * (<kbd>!&quot;#$%&amp;'()*+,-./:;&lt;=&gt;?@[\]^_`{|}~</kbd>) from the
     * given string.
     *
     * @param s
     *            string to parse
     * @return the first sequence of characters that are no punctuation
     *         characters
     */
    private String firstGroupFrom(String s) {
        final Pattern p = Pattern.compile("^[\\p{Punct}\\p{Space}]*([^\\p{Punct}]+)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.group(1).trim();
        } else {
            return s.trim();
        }
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     *
     * @return a not-yet-executed replacement of this thread
     * @see org.kitodo.production.helper.tasks.EmptyTask#replace()
     */
    @Override
    public CreateNewspaperProcessesTask replace() {
        return new CreateNewspaperProcessesTask(this);
    }

}

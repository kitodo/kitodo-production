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

package org.kitodo.production.services.command;

// base Java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// open source code
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProjectService;
import org.kitodo.production.services.data.TemplateService;


/**
 * Long-running task importing processes into Production.
 */
public final class ImportProcesses extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(ImportProcesses.class);

    // number of actions required for initialization
    private static final int INIT_ACTIONS_COUNT = 1;
    // number of actions required for validation
    private static final int VALIDATION_ACTIONS_COUNT = 1;

    // services required
    private final ProjectService projectService = ServiceManager.getProjectService();
    private final TemplateService templateService = ServiceManager.getTemplateService();

    // global system configuration
    private final boolean strictValidation = ConfigCore.getBooleanParameter(ParameterCore.VALIDATION_FAIL_ON_WARNING);

    // data
    private final Path importRootPath;
    private final Project project;
    private final Template templateForProcesses;
    private RulesetManagementInterface ruleset;
    private final Path errorPath;
    private final TreeMap<String, ImportingProcess> importingProcesses;
    private final int numberOfImportingProcesses;

    // thread execution
    private int step = 0;
    int totalActions = 1;
    private Iterator<ImportingProcess> importingProcessesIterator;
    ImportingProcess validatingImportingProcess;
    private ImportingProcess currentlyImporting;
    private int nextAction = 0;
    private int numberOfRemainingActions = 0;


    /**
     * <b>Constructor.</b><!-- --> Creates a {@code ProcessesImport}
     * long-running task.
     * 
     * @param indir
     *            input by the user for the source root directory. Can
     *            [invalidly] be {@code null} if the user has not specified it.
     * @param project
     *            input by the user for the project ID. Can [invalidly] be
     *            {@code null} if the user has not specified it.
     * @param template
     *            input by the for the process template ID. Can [invalidly] be
     *            {@code null} if the user has not specified it.
     * @param errors
     *            input by the user for the source root directory. Can [validly]
     *            be {@code null} if the user has not specified it.
     * @throws IllegalArgumentException
     *             if the user hasn't specified all the necessary parameters, or
     *             if the parameters have unusable values. Exception message is
     *             a message key that must be resolved via message properties.
     */
    public ImportProcesses(String indir, String project, String template, String errors) throws IOException {
        super(indir);
        this.importRootPath = checkIndir(indir);
        this.project = checkProject(project);
        this.templateForProcesses = checkTemplate(template);
        this.errorPath = checkErrors(errors);
        try (Stream<Path> pathStream = Files.walk(this.importRootPath, 1)) {
            this.importingProcesses = pathStream.filter(Files::isDirectory)
                    .filter(Predicate.not(this.importRootPath::equals))
                    .collect(Collectors.toMap(path -> path.getFileName().toString(), ImportingProcess::new,
                        (existing, replacing) -> replacing, TreeMap::new));
        }
        this.numberOfImportingProcesses = importingProcesses.size();
    }

    /**
     * <b>Clone constructor.</b><!-- --> Creates a copy of the object. But
     * because the object is a terminated thread, it makes a new thread that can
     * be started again.
     */
    private ImportProcesses(ImportProcesses source) {
        super(source);
        this.importRootPath = source.importRootPath;
        this.project = source.project;
        this.templateForProcesses = source.templateForProcesses;
        this.ruleset = source.ruleset;
        this.errorPath = source.errorPath;
        this.importingProcesses = source.importingProcesses;
        this.numberOfImportingProcesses = source.numberOfImportingProcesses;

        this.step = source.step;
        this.totalActions = source.totalActions;
        this.importingProcessesIterator = source.importingProcessesIterator;
        this.validatingImportingProcess = source.validatingImportingProcess;
        this.currentlyImporting = source.currentlyImporting;
        this.nextAction = source.nextAction;
        this.numberOfRemainingActions = source.numberOfRemainingActions;
    }

    /**
     * Checks whether the {@code indir} parameter is specified and valid. If
     * not, a corresponding error message is thrown as an exception. (This is
     * picked up above and will be translated and displayed to the user.) The
     * {@code indir} directory must be specified, it must exist, and the Tomcat
     * must have permission to get the directory listing (execution permission).
     * 
     * @param indir
     *            directory entered by the user. Can be {@code null} if the
     *            parameter is not specified
     * @return a Path object to the root directory for bulk import
     */
    private Path checkIndir(String indir) {
        if (Objects.isNull(indir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.isNull");
        }
        Path importRoot = Paths.get(indir);
        if (!Files.isDirectory(importRoot)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.isNoDirectory");
        }
        if (!Files.isExecutable(importRoot)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.cannotExecute");
        }
        return importRoot;
    }

    /**
     * Checks whether the parameter {@code project} is specified and valid. If
     * not, a corresponding error message is thrown as an exception. (This is
     * picked up above and will be translated and displayed to the user.) The
     * project must be specified, it must be syntactically valid (a positive
     * integer), and a project with that ID must exist.
     * 
     * @param project
     *            user-entered project number. Can be {@code null} if the
     *            parameter is not specified
     * @return the project object from the database
     */
    private Project checkProject(String project) {
        if (Objects.isNull(project)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.project.isNull");
        }
        if (!project.matches("[\\d]+")) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.project.isNoProjectID");
        }
        Integer projectInteger = Integer.valueOf(project);
        try {
            return projectService.getById(projectInteger);
        } catch (DAOException e) {
            logger.catching(e);
            throw new IllegalArgumentException("kitodoScript.importProcesses.project.noProjectWithID");
        }
    }

    /**
     * Checks whether the parameter {@code template} is specified and valid. If
     * not, a corresponding error message is thrown as an exception. (This is
     * picked up above and will be translated and displayed to the user.) The
     * production template must be specified, its ID must be syntactically valid
     * (a positive integer), and a production template with that ID must exist.
     * 
     * @param template
     *            user-entered production template number. Can be {@code null}
     *            if the parameter is not specified
     * @return the production template object from the database
     */
    private Template checkTemplate(String template) {
        if (Objects.isNull(template)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.isNull");
        }
        if (!template.matches("[\\d]+")) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.isNoTemplateID");
        }
        Integer templateInteger = Integer.valueOf(template);
        try {
            return templateService.getById(templateInteger);
        } catch (DAOException e) {
            logger.catching(e);
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.noTemplateWithID");
        }
    }

    /**
     * Checks whether the parameter {@code errors} is specified and—if so—is
     * valid. If not, a corresponding error message is thrown as an exception.
     * (This is picked up above and will be translated and displayed to the
     * user.) The errors directory is optional, but if provided, it must exist
     * and be writable by Tomcat.
     * 
     * @param errors
     *            Path to directory for errors. May be {@code null} if not
     *            specified
     * @return a Path object to the errors directory, or {@code null} if not
     *         specified
     */
    private Path checkErrors(String errors) {
        if (Objects.isNull(errors)) {
            return null;
        }
        Path errorDir = Paths.get(errors);
        if (!Files.isDirectory(errorDir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.errors.isNoDirectory");
        }
        if (!Files.isWritable(errorDir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.errors.cannotWrite");
        }
        return errorDir;
    }

    /*
     * The method is used by the Task Manager to spawn a new thread for this
     * task, if the previous thread was prematurely stopped. (A thread cannot
     * be continued under Java, a new thread object must be required.)
     */
    @Override
    public ImportProcesses replace() {
        return new ImportProcesses(this);
    }

    /*
     * The thread's runner method. This does the real work. Each importing
     * processes are always driven to individual work steps again and again,
     * after which the method returns to increase the progress and allow the
     * task to be stopped. This is complicated, but gives the task additional
     * flexibility to control.
     */
    @Override
    public void run() {
        try {
            while (step < totalActions) {
                run(step);
                super.setProgress(100d * ++step / totalActions);
                if (super.isInterrupted()) {
                    return;
                }
            }
            // error barrier
        } catch (IOException | DAOException | DataException | ProcessGenerationException | MediaNotFoundException
                | InvalidImagesException | RuntimeException exception) {
            Helper.setErrorMessage(exception.getLocalizedMessage(), logger, exception);
            super.setException(exception);
        }
    }

    void run(int setStep) throws IOException, DAOException, DataException, ProcessGenerationException,
            MediaNotFoundException, InvalidImagesException {

        step = setStep;
        Path processesPath = Paths.get(KitodoConfig.getKitodoDataDirectory());
        if (step == 0) {
            initialize();
        } else if (step <= numberOfImportingProcesses) {
            validate();
        } else {
            copyFilesAndCreateDatabaseEntry(step, processesPath);
        }
    }

    private void initialize() throws IOException {
        super.setWorkDetail(importRootPath.toString());
        ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_RULESETS),
            templateForProcesses.getRuleset().getFile()).toString()));
        totalActions = importingProcesses.entrySet().parallelStream().map(Entry::getValue)
                .mapToInt(ImportingProcess::numberOfActions).sum() + INIT_ACTIONS_COUNT;
        importingProcessesIterator = importingProcesses.values().iterator();
    }

    private void validate() throws IOException, DAOException {
        validatingImportingProcess = importingProcessesIterator.next();
        super.setWorkDetail(validatingImportingProcess.directoryName);
        validatingImportingProcess.validate(ruleset, strictValidation, importingProcesses);
        // in last iteration, re-initialize iterator
        if (step == numberOfImportingProcesses) {
            /* Children must be imported before their parents, so
             * that when the parents are imported, the process ID of
             * the children is already known and can be written down
             * in the METS file. */
            TreeSet<ImportingProcess> childrenFirst = new TreeSet<ImportingProcess>(
                    Comparator.comparingInt(ImportingProcess::childDepth).thenComparing(ImportingProcess::toString));
            childrenFirst.addAll(importingProcesses.values());
            importingProcessesIterator = childrenFirst.iterator();
        }
    }

    private void copyFilesAndCreateDatabaseEntry(int step, Path processesPath) throws IOException, DAOException, DataException,
            ProcessGenerationException, MediaNotFoundException, InvalidImagesException {
        if (nextAction == numberOfRemainingActions && step < totalActions - 1) {
            currentlyImporting = importingProcessesIterator.next();
            currentlyImporting.setProject(project);
            currentlyImporting.setTemplate(templateForProcesses);
            nextAction = 0;
            currentlyImporting.setOutputRoot(currentlyImporting.isCorrect() ? processesPath : errorPath);
            numberOfRemainingActions = currentlyImporting.numberOfActions() - VALIDATION_ACTIONS_COUNT;
        }
        super.setWorkDetail(currentlyImporting.directoryName);
        currentlyImporting.executeAction(nextAction);
        nextAction++;
    }
}

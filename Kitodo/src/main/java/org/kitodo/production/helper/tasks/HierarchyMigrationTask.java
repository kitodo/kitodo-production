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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;

public class HierarchyMigrationTask extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(HierarchyMigrationTask.class);

    /**
     * Service that contains the meta-data editor.
     */
    private static final DataEditorService dataEditorService = ServiceManager.getDataEditorService();

    /**
     * Service to access files on the storage.
     */
    private static final FileService fileService = ServiceManager.getFileService();

    /**
     * Service to read and write METS file format.
     */
    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * Service to generate processes.
     */
    private final ProcessGenerator processGenerator = new ProcessGenerator();

    /**
     * Service to read and write Process objects in the database or search
     * engine index.
     */
    private static final ProcessService processService = ServiceManager.getProcessService();

    /**
     * This map contains information about parent processes that have already
     * been created. Key is the identifier, the value is the process ID, then
     * the current numbers of child links already inserted. The current number
     * should not be confused with the process ID. It is not the process ID, but
     * a sort criterion that is read from the metadata. The background is that
     * during the migration the issues or volumes are found in any order, but
     * should be linked in ascending order according to their current number in
     * the parent process. Therefore, the sequential numbers of the already
     * linked children must be stored temporarily during the migration in order
     * to be able to determine the correct insertion position of another link.
     */
    private Map<String, List<Integer>> parentProcesses = new HashMap<>();

    /**
     * List of all processes to migrate.
     */
    private List<Integer> processesList;

    /**
     * All processes belong to a project.
     */
    private Collection<Project> projects;

    /**
     * The progress, for the progress bar.
     */
    private int progress = 0;

    public HierarchyMigrationTask(Collection<Project> projects) {
        super(projects.stream().map(Project::getTitle).collect(Collectors.joining(", ")));
        this.projects = projects;
    }

    /**
     * Clone constructor. Provides the ability to restart the task if it was
     * previously interrupted.
     *
     * @param source
     *            terminated thread
     */
    private HierarchyMigrationTask(HierarchyMigrationTask source) {
        super(source);
        this.processesList = source.processesList;
        this.projects = source.projects;
        this.progress = source.progress;
    }

    /**
     * Defines the display name of the task in the task manager. Usually this is
     * the class name, but here it is different from the class name in lower
     * case.
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation(getClass().getSimpleName().toLowerCase());
    }

    /**
     * The {@code run()} method is called when the thread starts. It initializes
     * the process list (if this has not already been done), processes it and
     * updates the progress display in the screen output.
     */
    @Override
    public void run() {
        try {
            if (Objects.isNull(processesList)) {
                processesList = projects.parallelStream().flatMap(project -> project.getProcesses().parallelStream())
                        .map(Process::getId).collect(Collectors.toList());
            }
            while (progress < processesList.size()) {
                Process process = processService.getById(processesList.get(progress));
                if (fileService.processOwnsAnchorXML(process, true) && !fileService.processOwnsYearXML(process, true)) {
                    setWorkDetail(process.getTitle());
                    migrate(process);
                }
                super.setProgress(100 * ++progress / processesList.size());
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        } catch (IOException | DAOException | ProcessGenerationException | DataException | CommandException e) {
            setException(e);
        }
    }

    /**
     * This function does the actual work and migrates exactly one process.
     *
     * @param process
     *            process to migrate
     */
   void migrate(Process process) throws IOException, ProcessGenerationException, DataException, DAOException, CommandException {
        logger.info("Starting to convert process {} (ID {})...", process.getTitle(), process.getId());
        long begin = System.nanoTime();
        migrateMetadataFiles(process);
        Optional<String> parentId = getParentRecordId(process);
        if (parentId.isPresent()) {
            if (parentProcesses.containsKey(parentId.get())) {
                linkProcessInParent(process, parentProcesses.get(parentId.get()));
            } else {
                parentProcesses.put(parentId.get(), createParentProcess(process));
            }
            renameAnchorFile(process);
        } else {
            logger.warn("Process {} (ID {}): Parent has no identifier! Cannot create parent process.",
                process.getTitle(), process.getId());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Converting {} took {} ms.", process.getTitle(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * The metadata file and the anchor file are converted to the new internal
     * format using XSLT.
     *
     * @param process
     *            process to migrate
     */
    private static void migrateMetadataFiles(Process process) throws IOException {
        URI metadataFilePath = fileService.getMetadataFilePath(process, true, true);
        dataEditorService.readData(metadataFilePath);
        URI anchorFilePath = fileService.createAnchorFile(metadataFilePath);
        dataEditorService.readData(anchorFilePath);
    }

    /**
     * Reads the parent record identifier from the anchor file.
     */
    private static Optional<String> getParentRecordId(Process process) throws IOException {
        URI metadataFilePath = fileService.getMetadataFilePath(process);
        URI anchorFilePath = fileService.createAnchorFile(metadataFilePath);
        Workpiece anchorWorkpiece = metsService.loadWorkpiece(anchorFilePath);
        Optional<String> parentRecordId = anchorWorkpiece.getRootElement().getMetadata().parallelStream()
                .filter(metadata -> metadata.getKey().equals("CatalogIDDigital"))
                .filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast).map(MetadataEntry::getValue)
                .findFirst();
        return parentRecordId;
    }

    /**
     * Creates a new parent process. The process is created in the database, the
     * process folder is created on the file system and the METS file is
     * written. The METS file of the child process is migrated and a link
     * between the processes is written in the database.
     *
     * @param childProcess
     *            process to migrate
     * @return a data object with the ID of the created parent process and the
     *         current number of the child process
     */
    private List<Integer> createParentProcess(Process childProcess)
            throws ProcessGenerationException, IOException, DataException, CommandException, DAOException {

        processGenerator.generateProcess(childProcess.getTemplate().getId(), childProcess.getProject().getId());
        Process parentProcess = processGenerator.getGeneratedProcess();
        processService.save(parentProcess);
        fileService.createProcessLocation(parentProcess);
        createParentMetsFile(childProcess);
        checkTaskAndId(parentProcess);
        processService.save(parentProcess);
        parentProcess = ServiceManager.getProcessService().getById(parentProcess.getId());
        ArrayList<Integer> parentData = new ArrayList<>();
        parentData.add(parentProcess.getId());
        URI metadataFilePath = fileService.getMetadataFilePath(childProcess);
        parentData.add(convertChildMetsFile(metadataFilePath));
        linkParentProcessWithChildProcess(parentProcess, childProcess);
        return parentData;
    }

    private void checkTaskAndId(Process parentProcess) throws IOException {
        URI parentMetadataFilePath = fileService.getMetadataFilePath(parentProcess, true, true);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(parentMetadataFilePath);
        ImportService.checkTasks(parentProcess, workpiece.getRootElement().getType());
        Collection<Metadata> metadata = workpiece.getRootElement().getMetadata();
        String title = "";
        for (Metadata metadatum : metadata) {
            if (metadatum.getKey().equals("TSL_ATS")) {
                title += ((MetadataEntry) metadatum).getValue() + "_";
            }
        }
        for (Metadata metadatum : metadata) {
            if (metadatum.getKey().equals("CatalogIDDigital")) {
                title += ((MetadataEntry) metadatum).getValue();
            }
        }
        parentProcess.setTitle(title);
        workpiece.setId(parentProcess.getId().toString());
    }

    /**
     * Links parent process and child process in the database. The processes are
     * saved.
     *
     * @param parentProcess
     *            parent process to link
     * @param childProcess
     *            child process to link
     */
    private static void linkParentProcessWithChildProcess(Process parentProcess, Process childProcess)
            throws DataException {

        parentProcess.getChildren().add(childProcess);
        childProcess.setParent(parentProcess);
        processService.save(childProcess);
    }

    /**
     * Generates the METS file for the parent process from the process anchor
     * file.
     *
     * @param process
     *            process to migrate
     */
    private void createParentMetsFile(Process process) throws IOException {
        URI metadataFileUri = fileService.getMetadataFilePath(process);
        URI anchorFileUri = fileService.createAnchorFile(metadataFileUri);
        Workpiece workpiece = metsService.loadWorkpiece(anchorFileUri);
        LinkedMetsResource link = workpiece.getRootElement().getChildren().get(0).getLink();
        link.setLoctype("Kitodo.Production");
        link.setUri(processService.getProcessURI(process));
        URI parentMetadataFileUri = fileService.getMetadataFilePath(processGenerator.getGeneratedProcess(), false,
            false);
        metsService.saveWorkpiece(workpiece, parentMetadataFileUri);
    }

    /**
     * Changes the METS file of the child process.
     *
     * @param metadataFilePath
     *            URI of the metadata file
     * @return the current number, may be {@code null}
     */
    private static Integer convertChildMetsFile(URI metadataFilePath) throws IOException {
        Workpiece workpiece = metsService.loadWorkpiece(metadataFilePath);
        IncludedStructuralElement childStructureRoot = workpiece.getRootElement().getChildren().get(0);
        workpiece.setRootElement(childStructureRoot);
        metsService.saveWorkpiece(workpiece, metadataFilePath);
        return getCurrentNo(childStructureRoot);
    }

    /**
     * Extracts the CurrentNo from the metadata. The current number is an
     * integer sorting criterion, which specifies the order of the subordinate
     * units within their superordinate entirety. In case of journal issues,
     * this can be the same as the issue number if the issue number continues to
     * be counted at the turn of the year. In the case of multi-volume works,
     * this number can correspond to the part number, or (for lexica, for
     * example) it is counted up according to the alphabetical order of the
     * volumes, supplementary volumes are counted on afterwards (thus, in the
     * order in which the books are usually placed on a shelf).
     *
     * @param includedStructualElement
     *            outline element with metadata
     * @return the CurrentNo, or {@code null}
     */
    private static Integer getCurrentNo(IncludedStructuralElement includedStructualElement) {
        Integer currentNo = includedStructualElement.getMetadata().parallelStream()
                .filter(metadata -> metadata.getKey().equals("CurrentNo")).filter(MetadataEntry.class::isInstance)
                .map(MetadataEntry.class::cast).map(MetadataEntry::getValue).filter(value -> value.matches("\\d+"))
                .map(Integer::valueOf).findFirst().orElse(null);
        return currentNo;
    }

    /**
     * Links a child process in an existing parent process.
     *
     * @param childProcess
     *            child process to link
     * @param parentData
     *            a data object with the ID of the parent process and the
     *            current numbers of the child processes already linked with the
     *            parent
     */
    private static void linkProcessInParent(Process childProcess, List<Integer> parentData)
            throws IOException, DAOException, DataException {

        URI metadataFilePath = fileService.getMetadataFilePath(childProcess);
        Integer currentNo = convertChildMetsFile(metadataFilePath);
        Process parentProcess = processService.getById(parentData.get(0));
        int insertionPosition = calculateInsertionPosition(parentData, currentNo);
        MetadataEditor.addLink(parentProcess, Integer.toString(insertionPosition), childProcess.getId());
        parentData.add(insertionPosition + 1, currentNo);
        linkParentProcessWithChildProcess(parentProcess, childProcess);
    }

    /**
     * Calculates the point at which the child process must be inserted in the
     * parent hierarchy.
     *
     * @param parentData
     *            a data object with the ID of the parent process (here unused)
     *            and the current numbers of the child processes already linked
     *            with the parent, which may be {@code null}
     * @param currentNo
     *            the current number of the child process to link, may be
     *            {@code null}
     * @return the insertion position
     */
    private static int calculateInsertionPosition(List<Integer> parentData, Integer currentNo) {
        int currentNumber = Objects.isNull(currentNo) ? Integer.MIN_VALUE : currentNo;
        int insertionPosition = 0;
        for (int index = 1; index < parentData.size(); index++) {
            int comparee = Objects.isNull(parentData.get(index)) ? Integer.MIN_VALUE : parentData.get(index);
            if (currentNumber >= comparee) {
                insertionPosition++;
            } else {
                break;
            }
        }
        return insertionPosition;
    }

    /**
     * Renames the anchor file. It is clear that this has been migrated.
     *
     * @param process
     *            process to migrate
     */
    private static void renameAnchorFile(Process process) throws IOException {
        URI anchorFile = fileService.createAnchorFile(fileService.getMetadataFilePath(process));
        fileService.renameFile(anchorFile, "meta_anchor.migrated");
    }
}

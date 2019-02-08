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
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.export.ExportDms;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;

/**
 * Thread implementation to export a batch holding a serial publication as set,
 * cross-over inserting METS pointer references to the respective other volumes
 * in the anchor file.
 *
 * <p>
 * Requires the {@code MetsModsImportExport.CREATE_MPTR_ELEMENT_TYPE} metadata
 * type ("MetsPointerURL") to be available for adding to the first level child
 * of the logical document structure hierarchy (typically "Volume").
 * </p>
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class ExportSerialBatchTask extends EmptyTask {

    private static final Logger logger = LogManager.getLogger(ExportSerialBatchTask.class);

    /**
     * The batch to export.
     */
    private final Batch batch;

    /**
     * The METS pointers of all volumes belonging to this serial publication.
     */
    private final ArrayList<String> pointers;

    /**
     * Counter used for incrementing the progress bar, starts from 0 and ends
     * with “maxsize”.
     */
    private int stepcounter;

    /**
     * Iterator along the processes of the batch during export.
     */
    private Iterator<Process> processesIterator;

    /**
     * Value indicating 100% on the progress bar.
     */
    private final int maxsize;

    /**
     * Creates a new ExportSerialBatchTask from a batch of processes belonging
     * to a serial publication.
     *
     * @param batch
     *            batch holding a serial publication
     */
    public ExportSerialBatchTask(Batch batch) {
        super(batch.getLabel());
        this.batch = batch;
        int batchSize = batch.getProcesses().size();
        pointers = new ArrayList<>(batchSize);
        stepcounter = 0;
        processesIterator = null;
        maxsize = batchSize + 1;
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see org.kitodo.production.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("exportSerialBatchTask");
    }

    /**
     * Clone constructor. Creates a new ExportSerialBatchTask from another one.
     * This is used for restarting the thread as a Java thread cannot be run
     * twice.
     *
     * @param master
     *            copy master
     */
    public ExportSerialBatchTask(ExportSerialBatchTask master) {
        super(master);
        batch = master.batch;
        pointers = master.pointers;
        stepcounter = master.stepcounter;
        processesIterator = master.processesIterator;
        maxsize = master.maxsize;
    }

    /**
     * The function run() is the main function of this task (which is a thread).
     * It will aggregate the data from all processes and then export all
     * processes with the recombined data. The statusProgress variable is being
     * updated to show the operator how far the task has proceeded.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        Process process = null;
        try {
            if (stepcounter == 0) {
                pointers.clear();
                for (Process processIterator : batch.getProcesses()) {
                    process = processIterator;
                    pointers.add(ExportNewspaperBatchTask.getMetsPointerURL(process));
                }
                processesIterator = batch.getProcesses().iterator();
                stepcounter++;
                setProgress(100 * stepcounter / maxsize);
            }
            if (stepcounter > 0) {
                while (processesIterator.hasNext()) {
                    if (isInterrupted()) {
                        return;
                    }
                    process = processesIterator.next();
                    LegacyMetsModsDigitalDocumentHelper out = buildExportDocument(process, pointers);
                    ExportDms exporter = new ExportDms(
                            ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITH_IMAGES));
                    exporter.setExportDmsTask(this);
                    exporter.startExport(process, ServiceManager.getUserService()
                            .getHomeDirectory(ServiceManager.getUserService().getAuthenticatedUser()),
                        out);
                    stepcounter++;
                    setProgress(100 * stepcounter / maxsize);
                }
            }
        } catch (IOException | RuntimeException e) {
            String message = e.getClass().getSimpleName() + " while " + (stepcounter == 0 ? "examining " : "exporting ")
                    + (process != null ? process.getTitle() : "") + ": " + e.getMessage();
            setException(new RuntimeException(message, e));
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * The function buildExportableMetsMods() returns a DigitalDocument object
     * whose logical document structure tree has been enriched with all nodes
     * that have to be exported along with the data to make cross-volume
     * referencing work.
     *
     * @param process
     *            process to get the METS/MODS data from
     * @param allPointers
     *            all the METS pointers from all volumes
     * @return an enriched DigitalDocument
     * @throws IOException
     *             if creating the process directory or reading the meta data
     *             file fails
     */
    private static LegacyMetsModsDigitalDocumentHelper buildExportDocument(Process process, Iterable<String> allPointers)
            throws IOException {
        LegacyMetsModsDigitalDocumentHelper result = ServiceManager.getProcessService().readMetadataFile(process)
                .getDigitalDocument();
        LegacyDocStructHelperInterface root = result.getLogicalDocStruct();
        String type = "Volume";
        try {
            type = root.getAllChildren().get(0).getDocStructType().getName();
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
        String ownPointer = ExportNewspaperBatchTask.getMetsPointerURL(process);
        LegacyPrefsHelper ruleset = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        for (String pointer : allPointers) {
            if (!pointer.equals(ownPointer)) {
                throw new UnsupportedOperationException("Dead code pending removal");
            }
        }
        return result;
    }
}

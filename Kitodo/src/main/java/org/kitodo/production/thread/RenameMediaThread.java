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

package org.kitodo.production.thread;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.LocaleHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.services.ServiceManager;

/**
 * This class is used to rename media files of multiple processes in a separate thread whose progress can be monitored
 * in the task manager.
 */
public class RenameMediaThread extends EmptyTask {

    private static final Logger logger = LogManager.getLogger(RenameMediaThread.class);
    private final List<Process> processes;
    private static final String THREAD_NAME = "renameMediaThread";
    private static final String ERRORS_OCCURRED_KEY = "errorsOccurredCheckLog";
    private static final String PROCESSES = "processes";

    public RenameMediaThread(List<Process> processes) {
        super(processes.size() + " " + Helper.getString(LocaleHelper.getCurrentLocale(), PROCESSES));
        this.processes = processes;
    }

    @Override
    protected void setNameDetail(String detail) {
        String localThreadName = Helper.getString(LocaleHelper.getCurrentLocale(), THREAD_NAME);
        super.setName(localThreadName + ": " + detail);
    }

    /**
     * Run method of this thread. Iterates over processes and renames all media files in each process using the regular
     * "media renaming" functionality of the FileService.
     */
    @Override
    public void run() {
        for (Process process : processes) {
            int processId = process.getId();
            URI metaXmlUri = ServiceManager.getProcessService().getMetadataFileUri(process);
            DualHashBidiMap<URI, URI> renamingMap = new DualHashBidiMap<>();
            Workpiece workpiece = null;
            try {
                workpiece = ServiceManager.getMetsService().loadWorkpiece(metaXmlUri);
                int numberOfRenamedFiles = ServiceManager.getFileService().renameMediaFiles(process, workpiece,
                        renamingMap);
                try (OutputStream out = ServiceManager.getFileService().write(metaXmlUri)) {
                    ServiceManager.getMetsService().save(workpiece, out);
                    logger.info("Renamed " + numberOfRenamedFiles + " media files for process " + process.getId());
                }
            } catch (IOException | URISyntaxException e) {
                logger.error(e.getMessage());
                String nameDetailMessage = processes.size()
                        + " " + Helper.getString(LocaleHelper.getCurrentLocale(), PROCESSES)
                        + " (" + Helper.getString(LocaleHelper.getCurrentLocale(), ERRORS_OCCURRED_KEY) + ")";
                this.setNameDetail(nameDetailMessage);
                if (Objects.nonNull(workpiece)) {
                    ServiceManager.getFileService().revertRenaming(renamingMap.inverseBidiMap(), workpiece);
                }
            }
            MetadataLock.setFree(processId);
            setProgress((100 / processes.size()) * (processes.indexOf(process) + 1));
        }
        setProgress(100);
    }
}

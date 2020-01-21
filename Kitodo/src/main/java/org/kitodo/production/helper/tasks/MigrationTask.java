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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.migration.MigrationService;

/**
 * Migrates metadata in a separate thread.
 */
public class MigrationTask extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(MigrationTask.class);

    /**
     * Service who has to migrate the processes.
     */
    private final MigrationService migrationService;

    /**
     * List of processes to be migrated.
     */
    private final List<Process> processes;

    /**
     * Migration progress. Points to the index of the next process in case the
     * thread is interrupted and restarted. Also used for the progress bar.
     */
    private int progress = 0;

    /**
     * Creates a new migration task.
     *
     * @param project
     *            project whose processes are to be migrated
     */
    public MigrationTask(Project project) {
        super(project.getTitle());
        this.migrationService = ServiceManager.getMigrationService();
        this.processes = project.getProcesses();
    }

    /**
     * Clone constructor. Provides the ability to restart an export that was
     * previously interrupted by the user.
     *
     * @param sourceMigrationTask
     *            terminated thread
     */
    private MigrationTask(MigrationTask sourceMigrationTask) {
        super(sourceMigrationTask);
        this.progress = sourceMigrationTask.progress;
        this.migrationService = sourceMigrationTask.migrationService;
        this.processes = sourceMigrationTask.processes;
    }

    /**
     * The method to work the thread.
     */
    @Override
    public void run() {
        String processTitle = null;
        try {
            for (int index = progress; index < processes.size(); index++) {
                final long begin = System.nanoTime();
                Process process = processes.get(index);
                processTitle = process.getTitle();
                setWorkDetail(processTitle);
                migrationService.migrateMetadata(process);
                if (logger.isTraceEnabled()) {
                    logger.trace("Migrating {} took {} ms", processTitle,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
                }
                progress = index + 1;
                setProgress(100 * progress / processes.size());
                if (isInterrupted()) {
                    return;
                }
            }
            setProgress(100);
        } catch (DAOException exception) {
            Helper.setErrorMessage(exception.getLocalizedMessage(), processTitle, logger, exception);
            super.setException(exception);
        }
    }
}

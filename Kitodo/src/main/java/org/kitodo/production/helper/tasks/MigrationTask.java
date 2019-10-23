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

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.migration.MigrationService;

/**
 * Migrates metadata in a separate thread.
 */
public class MigrationTask extends EmptyTask {
    /**
     * Key figure of the process to be migrated. At the same time progress.
     */
    private int index = 0;

    /**
     * Service who has to migrate the processes.
     */
    final private MigrationService migrationService;

    /**
     * List of processes to be migrated.
     */
    final private List<Process> processes;

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
     * @param source
     *            terminated thread
     */
    private MigrationTask(MigrationTask source) {
        super(source);
        this.index = source.index;
        this.migrationService = source.migrationService;
        this.processes = source.processes;
    }

    /**
     * This function displays the name in the task manager.
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("MigrationTask");
    }

    /**
     * The method to work the thread.
     */
    @Override
    public void run() {
        try {
            while (index < processes.size()) {
                Process process = processes.get(index++);
                setWorkDetail(process.getTitle());
                migrationService.migrateMetadata(process);
                setProgress(100 * index / processes.size());
                if (isInterrupted()) {
                    return;
                }
            }
            setProgress(100);
        } catch (Exception exception) {
            super.setException(exception);
        }
    }
}

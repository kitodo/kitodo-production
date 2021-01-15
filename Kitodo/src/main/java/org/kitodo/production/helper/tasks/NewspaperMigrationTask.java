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

import javax.naming.ConfigurationException;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.migration.NewspaperProcessesMigrator;

/**
 * Runs the migration of a newspaper in the task manager.
 */
public class NewspaperMigrationTask extends EmptyTask {

    /**
     * The migration is performed in three parts that build upon each other.
     */
    private static enum Part {
        /**
         * First, the individual processes are converted.
         */
        CONVERT_PROCESSES,
        /**
         * Then, the overall newspaper process will be created.
         */
        CREATE_NEWSPAPER,
        /**
         * Afterwards, for each year, a year process will be created, and linked
         * to the overall process.
         */
        CREATE_YEARS
    }

    /**
     * Migrator which is run by this thread.
     */
    private final NewspaperProcessesMigrator migrator;

    /**
     * Number of processes, used to calculate progress.
     */
    private final int numberOfProcesses;

    /**
     * Current part of migration.
     */
    private Part part;

    /**
     * Step counter (for progress).
     */
    private int step;

    /**
     * Creates a new newspaper migration task.
     *
     * @param batch
     *            the batch object which holds together the newspaper processes
     */
    public NewspaperMigrationTask(Batch batch) {
        super(batch.getTitle());
        this.numberOfProcesses = batch.getProcesses().size();
        this.part = Part.CONVERT_PROCESSES;
        this.migrator = new NewspaperProcessesMigrator(batch);
    }

    /**
     * Clone constructor. Provides the ability to restart an export that was
     * previously interrupted by the user.
     *
     * @param source
     *            terminated thread
     */
    private NewspaperMigrationTask(NewspaperMigrationTask source) {
        super(source);
        this.migrator = source.migrator;
        this.numberOfProcesses = source.numberOfProcesses;
        this.part = source.part;
        this.step = source.step;
    }

    /**
     * Returns the total number of steps. There is one (conversion) step per
     * process, one overall process generation step, and for each year one year
     * process generation step. The number of steps is growing during the first
     * part as long as more years are encountered, so this value must be updated
     * every time.
     *
     * @return the number of steps
     */
    private int getNumberOfSteps() {
        return numberOfProcesses + 1 + migrator.getNumberOfYears();
    }

    /**
     * Performs the next migration step.
     *
     * @throws DAOException
     *             if a process cannot be load from the database
     * @throws IOException
     *             if file syestem I/O fails
     * @throws ProcessGenerationException
     *             if a process cannot be generated
     * @throws DataException
     *             if a process cannot be saved to the database
     * @throws ConfigurationException
     *             if the newspaper division is not well configured in the
     *             ruleset
     */
    private void next() throws DAOException, IOException, ProcessGenerationException, DataException,
            ConfigurationException, CommandException {
        switch (part) {
            case CONVERT_PROCESSES: {
                super.setWorkDetail(migrator.getProcessTitle(step));
                migrator.convertProcess(step);
                if (step == numberOfProcesses - 1) {
                    part = Part.CREATE_NEWSPAPER;
                }
                break;
            }
            case CREATE_NEWSPAPER: {
                super.setWorkDetail(migrator.getTitle());
                migrator.createOverallProcess();
                part = Part.CREATE_YEARS;
                break;
            }
            case CREATE_YEARS: {
                super.setWorkDetail(migrator.getPendingYearTitle());
                migrator.createNextYearProcess();
                break;
            }
            default:
                throw new IllegalStateException("For step: " + part);
        }
        setProgress(100 * ++step / getNumberOfSteps());
    }

    /**
     * Runs the current thread.
     */
    @Override
    public void run() {
        try {
            while (!part.equals(Part.CREATE_YEARS) || migrator.hasNextYear()) {
                next();
                if (isInterrupted()) {
                    return;
                }
            }
            setProgress(100);
        } catch (ConfigurationException | DAOException | IOException | ProcessGenerationException | DataException
                | CommandException e) {
            setException(e);
        }
    }
}

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

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.process.NewspaperProcessesGenerator;

/**
 * A thread to create newspaper processes in the background.
 */
public class GeneratesNewspaperProcessesThread extends EmptyTask {
    /**
     * The generator object.
     */
    private NewspaperProcessesGenerator generator;

    /**
     * Creates a new thread that generates newspaper processes.
     *
     * @param process
     *            Process of the whole newspaper
     * @param course
     *            Course of publication of the newspaper
     */
    public GeneratesNewspaperProcessesThread(Process process, Course course) {
        super(process.getTitle());
        this.generator = new NewspaperProcessesGenerator(process, course);
    }

    /**
     * <b>Clone constructor.</b><!-- --> Provides the ability to restart the
     * task if it was previously interrupted.
     *
     * @param source
     *            terminated thread
     */
    private GeneratesNewspaperProcessesThread(GeneratesNewspaperProcessesThread source) {
        super(source);
        this.generator = source.generator;
    }

    /**
     * Creates a new thread based on this thread to be able to restart the
     * interrupted process.
     *
     * @return a new thread based on this thread
     */
    @Override
    public GeneratesNewspaperProcessesThread replace() {
        return new GeneratesNewspaperProcessesThread(this);
    }

    /**
     * Generates the processes.
     */
    @Override
    public void run() {
        try {
            while (generator.getProgress() < generator.getNumberOfSteps()) {
                if (!generator.nextStep()) {
                    return;
                }
                super.setProgress(100 * generator.getProgress() / generator.getNumberOfSteps());
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            super.setProgress(100);
        } catch (ConfigurationException | DAOException | DataException | DoctypeMissingException | IOException
                | ProcessGenerationException | CommandException e) {
            setException(e);
        }
    }
}

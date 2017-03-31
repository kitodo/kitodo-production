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

package org.kitodo.production.flow.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface IGoobiJob {

    /**
     * execute this {@link Job} for all database and metadata content don't
     * overwrite this method.
     *
     * @throws JobExecutionException
     */
    public abstract void execute(JobExecutionContext context) throws JobExecutionException;

    /**
     * override this method to let the job be called automatically.
     */
    public abstract void execute();

    public abstract void setIsRunning(Boolean inisRunning);

    public abstract Boolean getIsRunning();

    /**
     * getter for JobName.
     */
    public abstract String getJobName();

}

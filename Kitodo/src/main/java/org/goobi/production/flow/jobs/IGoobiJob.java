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

package org.goobi.production.flow.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public interface IGoobiJob {

    /**
     * execute this {@link Job} for all database and metadata content don't
     * overwrite this method.
     *
     */
    void execute(JobExecutionContext context);

    /**
     * override this method to let the job be called automatically.
     */
    void execute();

    void setIsRunning(Boolean inisRunning);

    Boolean getIsRunning();

    /**
     * getter for JobName.
     */
    String getJobName();

}

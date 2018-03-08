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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * SimpleGoobiJob as basis class for all big jobs
 * 
 * @author Steffen Hankiewicz
 * @version 21.10.2009
 */
public abstract class AbstractGoobiJob implements Job, IGoobiJob {
    private static final Logger logger = LogManager.getLogger(AbstractGoobiJob.class);
    private static Boolean isRunning = false;

    protected AbstractGoobiJob() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#execute(org.quartz.
     * JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (!getIsRunning()) {
            logger.trace("start scheduled Job: {}", getJobName());
            if (!isRunning) {
                logger.trace("start history updating for all processes");
                setIsRunning(true);
                execute();
                setIsRunning(false);
            }
            logger.trace("End scheduled Job: {}", getJobName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#execute()
     */
    @Override
    public void execute() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.jobs.IGoobiJob#setIsRunning(java.lang.Boolean)
     */
    @Override
    public void setIsRunning(Boolean inisRunning) {
        isRunning = inisRunning;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#getIsRunning()
     */
    @Override
    public Boolean getIsRunning() {
        return isRunning;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#getJobName()
     */
    @Override
    public String getJobName() {
        return "";
    }
}

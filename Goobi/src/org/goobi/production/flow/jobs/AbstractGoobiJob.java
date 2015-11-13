package org.goobi.production.flow.jobs;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * SimpleGoobiJob as basis class for all big jobs
 * 
 * @author Steffen Hankiewicz
 * @version 21.10.2009
 */
public abstract class AbstractGoobiJob implements Job, IGoobiJob {
	private static final Logger logger = Logger.getLogger(AbstractGoobiJob.class);
	private static Boolean isRunning = false;

	protected AbstractGoobiJob() {
	}

	/* (non-Javadoc)
	 * @see org.goobi.production.flow.jobs.IGoobiJob#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (getIsRunning() == false) {
			logger.trace("Start scheduled Job: " + getJobName());
			if (isRunning == false) {
				logger.trace("start history updating for all processes");
				setIsRunning(true);
				execute();
				setIsRunning(false);
			}
			logger.trace("End scheduled Job: " + getJobName());
		}
	}

	/* (non-Javadoc)
	 * @see org.goobi.production.flow.jobs.IGoobiJob#setIsRunning(java.lang.Boolean)
	 */
	@Override
	public void setIsRunning(Boolean inisRunning) {
		isRunning = inisRunning;
	}

	/* (non-Javadoc)
	 * @see org.goobi.production.flow.jobs.IGoobiJob#getIsRunning()
	 */
	@Override
	public Boolean getIsRunning() {
		return isRunning;
	}

	/* (non-Javadoc)
	 * @see org.goobi.production.flow.jobs.IGoobiJob#getJobName()
	 */
	@Override
	public String getJobName() {
		return "";
	}

	/* (non-Javadoc)
	 * @see org.goobi.production.flow.jobs.IGoobiJob#execute()
	 */
	@Override
	public void execute() {
	}

}

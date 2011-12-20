/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.flow.jobs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface IGoobiJob {

	/***************************************************************************
	 * execute this {@link Job} for all database and metadata content
	 * don't overwrite this method
	 * 
	 * @throws JobExecutionException
	 **************************************************************************/
	public abstract void execute(JobExecutionContext context)
			throws JobExecutionException;

	public abstract void setIsRunning(Boolean inisRunning);

	public abstract Boolean getIsRunning();

	/***************************************************************************
	 * getter for JobName
	 **************************************************************************/
	public abstract String getJobName();

	/***************************************************************************
	 * override this method to let the job be called automatically
	 **************************************************************************/
	public abstract void execute();

}
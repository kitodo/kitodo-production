package org.goobi.production.flow.jobs;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import de.sub.goobi.config.ConfigMain;

/**
 * JobManager organizes all scheduled jobs
 * 
 * @author Steffen Hankiewicz
 * @author Igor Toker
 * @version 21.10.2009
 */
public class JobManager implements ServletContextListener {
	private static final Logger logger = Logger.getLogger(JobManager.class);

	/***********************************************************************
	 * Restarts timed Jobs
	 * 
	 * @throws SchedulerException
	 **********************************************************************/
	public static void restartTimedJobs() throws SchedulerException {
		stopTimedJobs();
		startTimedJobs();
	}
	
	/***************************************************************************
	 * Stops timed updates of HistoryManager
	 * 
	 * @throws SchedulerException
	 **************************************************************************/
	private static void stopTimedJobs() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		schedFact.getScheduler().shutdown(false);
	}

	/***************************************************************************
	 * Starts timed updates of {@link HistoryAnalyserJob}
	 * 
	 * @throws SchedulerException
	 **************************************************************************/
	@SuppressWarnings("deprecation")
	private static void startTimedJobs() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		Scheduler sched = schedFact.getScheduler();
		sched.start();
		
		initializeJob(new HistoryAnalyserJob(), "dailyHistoryAnalyser", sched);
//		initializeJob(new LuceneIndexJob(), "dailyLuceneIndex", sched);
		initializeJobNonConfigured(new HotfolderJob(), 5, sched);
	}

	/***************************************************************************
	 * initializes given SimpleGoobiJob at given time
	 * 
	 * @throws SchedulerException
	 **************************************************************************/
	private static void initializeJob(IGoobiJob goobiJob, String configuredStartTimeProperty,Scheduler sched) throws SchedulerException{
		logger.debug(goobiJob.getJobName());
		JobDetail jobDetail = new JobDetail(goobiJob.getJobName(), null, goobiJob.getClass());
		
		
		if (ConfigMain.getLongParameter(configuredStartTimeProperty, -1) != -1) {
			long msOfToday = ConfigMain.getLongParameter(configuredStartTimeProperty, -1);
			Calendar cal = Calendar.getInstance();
			cal.set(1984, 8, 11, 0, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.setTime(new Date(cal.getTimeInMillis() + msOfToday));
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);

			Trigger trigger = TriggerUtils.makeDailyTrigger(hour, min);
			trigger.setStartTime(new Date());
			trigger.setName(goobiJob.getJobName() + "_trigger");

			logger.info("daily Job " + goobiJob.getJobName() + " start time: " + hour + ":" + min);
			sched.scheduleJob(jobDetail, trigger);
		}
	}
	
	/***************************************************************************
	 * initializes given SimpleGoobiJob at given time
	 * 
	 * @throws SchedulerException
	 **************************************************************************/
	private static void initializeJobNonConfigured(IGoobiJob goobiJob, int myTime,Scheduler sched) throws SchedulerException{
		logger.debug(goobiJob.getJobName());
		JobDetail jobDetail = new JobDetail(goobiJob.getJobName(), null, goobiJob.getClass());

			// hier alle 60 sek. oder so
			Trigger trigger = TriggerUtils.makeMinutelyTrigger(myTime);
			trigger.setStartTime(new Date());
			trigger.setName(goobiJob.getJobName() + "_trigger");
			sched.scheduleJob(jobDetail, trigger);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Stop daily JobManager scheduler");
		try {
			stopTimedJobs();
		} catch (SchedulerException e) {
			logger.error("daily JobManager could not be stopped", e);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("Start daily JobManager scheduler");
		try {
			startTimedJobs();
		} catch (SchedulerException e) {
			logger.error("daily JobManager could not be started", e);
		}
	}

	/***************************************************************************
	 * get current time plus 60 seconds as milliseconds from midnight to debug jobmanager
	 **************************************************************************/
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Calendar calNow = Calendar.getInstance();
		
		logger.debug(calNow.getTime() + " --- "+ cal.getTime());
		logger.debug("60 seconds from now in milliseconds from 0:00 are " + (calNow.getTimeInMillis()-cal.getTimeInMillis() + 60000));
		
	}
	
}

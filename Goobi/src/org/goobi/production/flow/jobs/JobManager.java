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

			if(logger.isInfoEnabled()){
				logger.info("daily Job " + goobiJob.getJobName() + " start time: " + hour + ":" + min);
			}
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
		
		if(logger.isDebugEnabled()){
			logger.debug(calNow.getTime() + " --- "+ cal.getTime());
			logger.debug("60 seconds from now in milliseconds from 0:00 are " + (calNow.getTimeInMillis()-cal.getTimeInMillis() + 60000));
		}
		
	}
	
}

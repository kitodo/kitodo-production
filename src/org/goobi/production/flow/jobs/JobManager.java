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

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.config.ConfigMain;

/**
 * HistoryManager organizes the history items of given {@link Prozess}
 * 
 * @author Steffen Hankiewicz
 * @author Igor Toker
 * @version 24.05.2009
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
	 * Starts timed updates of {@link HistoryJob}
	 * 
	 * @throws SchedulerException
	 **************************************************************************/
	private static void startTimedJobs() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		Scheduler sched = schedFact.getScheduler();
		sched.start();
		JobDetail jobDetail = new JobDetail(HistoryJob.JOBNAME, null, HistoryJob.class);
		// get time from config
		if (ConfigMain.getLongParameter("storageCalculationSchedule", -1) != -1) {
			long msOfToday = ConfigMain.getLongParameter("storageCalculationSchedule", -1);
			Calendar cal = Calendar.getInstance();
			cal.set(1984, 8, 11, 0, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.setTime(new Date(cal.getTimeInMillis() + msOfToday));
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);

			Trigger trigger = TriggerUtils.makeDailyTrigger(hour, min);
			trigger.setStartTime(new Date());
			trigger.setName(HistoryJob.JOBNAME + "_trigger");

			logger.info("-= History Manager start time: " + hour + ":" + min + " =-");
			sched.scheduleJob(jobDetail, trigger);
		}
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Start History Manager scheduler");
		try {
			stopTimedJobs();
		} catch (SchedulerException e) {
			logger.error("History Manager could not be stopped", e);
		}
	}

	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("Start StorageHistoryManager scheduler");
		try {
			startTimedJobs();
		} catch (SchedulerException e) {
			logger.error("StorageHistoryManager could not be started", e);
		}
	}

}

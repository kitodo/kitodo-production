package de.sub.goobi.Persistence.apache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.HistoryEvent;

public class StepManager {

	private static final Logger logger = Logger.getLogger(DbHelper.class);

	public static StepObject getStepById(int stepId) {
		StepObject so = null;
		try {
			so = DbHelper.getStepByStepId(stepId);
		} catch (SQLException e) {
			logger.error("Cannot not load step with id " + stepId, e);
		}

		return so;
	}

	public static List<StepObject> getStepsForProcess(int processId) {
		List<StepObject> answer = new ArrayList<StepObject>();

		try {
			answer = DbHelper.getStepsForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load process with id " + processId, e);
		}

		return answer;
	}

	public static void updateStep(StepObject step) {
		
		try {
			DbHelper.getInstance().updateStep(step);
		} catch (SQLException e) {
			logger.error("Cannot not save step with id " + step.getId(), e);
		}
		
	}

	public static void addHistory(HistoryEvent he) {
		try {
			DbHelper.getInstance().addHistory(he);
		} catch (SQLException e) {
			logger.error("Cannot not save history event", e);
		}
	}

	public static void updateProcessStatus(String value, int processId) {
		try {
			DbHelper.getInstance().updateProcess(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}
}

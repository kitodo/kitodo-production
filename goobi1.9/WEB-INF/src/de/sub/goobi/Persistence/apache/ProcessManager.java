package de.sub.goobi.Persistence.apache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Regelsatz;

public class ProcessManager {

	
	private static final Logger logger = Logger.getLogger(MySQLHelper.class);

	public static ProcessObject getProcessObjectForId(int processId) {
		try {
			return MySQLHelper.getProcessObjectForId(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load process with id " + processId, e);
		}
		return null;
	}
	
	
	public static void updateProcessStatus(String value, int processId) {
		try {
			MySQLHelper.getInstance().updateProcessStatus(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}
	
	public static void updateImages(Integer numberOfFiles, int processId) {
		try {
			MySQLHelper.getInstance().updateImages(numberOfFiles, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
		
	}
	
	
	public static void addLogfile(String value, int processId) {
		try {
			MySQLHelper.getInstance().updateProcessLog(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}
	
	
	public static Regelsatz getRuleset(int rulesetId) {
		try {
			return MySQLHelper.getRulesetForId(rulesetId);
		} catch (SQLException e) {
			logger.error("Cannot not load ruleset with id " + rulesetId, e);
		}
		return null;
	}
	
	public static List<Property> getProcessProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getProcessPropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}
	
	public static List<Property> getTemplateProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getTemplatePropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}
	
	public static List<Property> getProductProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getProductPropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}


	
	
	
}

package de.sub.goobi.Persistence.apache;

import java.sql.SQLException;

import org.apache.log4j.Logger;

public class ProcessManager {

	
	private static final Logger logger = Logger.getLogger(DbHelper.class);

	
	public static void updateProcessStatus(String value, int processId) {
		try {
			DbHelper.getInstance().updateProcessStatus(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}
	
	
	public static void addLogfile(String value, int processId) {
		try {
			DbHelper.getInstance().updateProcessLog(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
		
	}
	
}

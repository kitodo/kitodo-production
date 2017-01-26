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

package org.kitodo.data.database.persistence.apache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.kitodo.data.database.beans.Ruleset;

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

	public static Ruleset getRuleset(int rulesetId) {
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

	public static int getNumberOfProcessesWithRuleset(int rulesetId) {
		Integer answer = null;
		try {
			answer = MySQLHelper.getCountOfProcessesWithRuleset(rulesetId);
		} catch (SQLException e) {
			logger.error("Cannot not load information about ruleset with id " + rulesetId, e);
		}
		return answer;
	}
	
	public static int getNumberOfProcessesWithDocket(int docketId) {
		Integer answer = null;
		try {
			answer = MySQLHelper.getCountOfProcessesWithDocket(docketId);
		} catch (SQLException e) {
			logger.error("Cannot not load information about docket with id " + docketId, e);
		}
		return answer;
	}
	
	public static int getNumberOfProcessesWithTitle(String title) {
		int answer = 0;	
		try {
			answer = MySQLHelper.getCountOfProcessesWithTitle(title);
		} catch (SQLException e) {
			logger.error("Cannot not load information about processes with title " + title, e);
		}
		return answer;
	}
	
}

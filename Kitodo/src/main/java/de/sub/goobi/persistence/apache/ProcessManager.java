package de.sub.goobi.persistence.apache;

//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
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
//CHECKSTYLE:ON

import de.sub.goobi.beans.Regelsatz;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ProcessManager {

	private static final Logger logger = Logger.getLogger(MySQLHelper.class);

	/**
	 * @param processId add description
	 * @return add description
	 */
	public static ProcessObject getProcessObjectForId(int processId) {
		try {
			return MySQLHelper.getProcessObjectForId(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load process with id " + processId, e);
		}
		return null;
	}

	/**
	 * @param value add description
	 * @param processId add description
	 */
	public static void updateProcessStatus(String value, int processId) {
		try {
			MySQLHelper.getInstance().updateProcessStatus(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}

	/**
	 * @param numberOfFiles add description
	 * @param processId add description
	 */
	public static void updateImages(Integer numberOfFiles, int processId) {
		try {
			MySQLHelper.getInstance().updateImages(numberOfFiles, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}

	}

	/**
	 * @param value add description
	 * @param processId add description
	 */
	public static void addLogfile(String value, int processId) {
		try {
			MySQLHelper.getInstance().updateProcessLog(value, processId);
		} catch (SQLException e) {
			logger.error("Cannot not update status for process with id " + processId, e);
		}
	}

	/**
	 * @param rulesetId add description
	 * @return add description
	 */
	public static Regelsatz getRuleset(int rulesetId) {
		try {
			return MySQLHelper.getRulesetForId(rulesetId);
		} catch (SQLException e) {
			logger.error("Cannot not load ruleset with id " + rulesetId, e);
		}
		return null;
	}

	/**
	 * @param processId add description
	 * @return add description
	 */
	public static List<Property> getProcessProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getProcessPropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}

	/**
	 * @param processId add description
	 * @return add description
	 */
	public static List<Property> getTemplateProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getTemplatePropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}

	/**
	 * @param processId add description
	 * @return add description
	 */
	public static List<Property> getProductProperties(int processId) {
		List<Property> answer = new ArrayList<Property>();
		try {
			answer = MySQLHelper.getProductPropertiesForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load properties for process with id " + processId, e);
		}
		return answer;
	}

	/**
	 * @param rulesetId add description
	 * @return add description
	 */
	public static int getNumberOfProcessesWithRuleset(int rulesetId) {
		Integer answer = null;
		try {
			answer = MySQLHelper.getCountOfProcessesWithRuleset(rulesetId);
		} catch (SQLException e) {
			logger.error("Cannot not load information about ruleset with id " + rulesetId, e);
		}
		return answer;
	}

	/**
	 * @param docketId add description
	 * @return add description
	 */
	public static int getNumberOfProcessesWithDocket(int docketId) {
		Integer answer = null;
		try {
			answer = MySQLHelper.getCountOfProcessesWithDocket(docketId);
		} catch (SQLException e) {
			logger.error("Cannot not load information about docket with id " + docketId, e);
		}
		return answer;
	}

	/**
	 * @param title add description
	 * @return add description
	 */
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

package de.sub.goobi.persistence.apache;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class StepManager {

	private static final Logger logger = Logger.getLogger(MySQLHelper.class);

	public static StepObject getStepById(int stepId) {
		StepObject so = null;
		try {
			so = MySQLHelper.getStepByStepId(stepId);
		} catch (SQLException e) {
			logger.error("Cannot not load step with id " + stepId, e);
		}

		return so;
	}

	public static List<StepObject> getStepsForProcess(int processId) {
		List<StepObject> answer = new ArrayList<StepObject>();

		try {
			answer = MySQLHelper.getStepsForProcess(processId);
		} catch (SQLException e) {
			logger.error("Cannot not load process with id " + processId, e);
		}

		return answer;
	}

	public static void updateStep(StepObject step) {
		
		try {
			MySQLHelper.getInstance().updateStep(step);
		} catch (SQLException e) {
			logger.error("Cannot not save step with id " + step.getId(), e);
		}
		
	}

	public static void addHistory(Date myDate, double order, String value, int type, int processId) {
		try {
			MySQLHelper.getInstance().addHistory( myDate,  order,  value,  type,  processId);
		} catch (SQLException e) {
			logger.error("Cannot not save history event", e);
		}
	}

	public static List<String> loadScripts(int id) {
		try {
			return MySQLHelper.getScriptsForStep(id);
		} catch (SQLException e) {
			logger.error("Cannot not load scripts for step with id " + id, e);
		}
		return new ArrayList<String>();
	}
	public static Map<String,String> loadScriptMap(int id) {
		try {
			return MySQLHelper.getScriptMapForStep(id);
		} catch (SQLException e) {
			logger.error("Cannot not load scripts for step with id " + id, e);
		}
		return new HashMap<String, String>();
	}
	
	public static List<Integer> getStepIds(String query) {
		try {
			return MySQLHelper.getStepIds(query);
		} catch (SQLException e) {
			logger.error("Cannot not load steps", e);
		}
		return new ArrayList<Integer>();
	}
	
}

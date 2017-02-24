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

package de.sub.goobi.persistence.apache;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.beans.Regelsatz;

public class MySQLHelper {

	private static final int MAX_TRIES_NEW_CONNECTION = 5;
	private static final int TIME_FOR_CONNECTION_VALID_CHECK = 5;

	private static final Logger logger = Logger.getLogger(MySQLHelper.class);

	private static MySQLHelper helper = new MySQLHelper();
	private ConnectionManager cm = null;

	private MySQLHelper() {
		SqlConfiguration config = SqlConfiguration.getInstance();
		this.cm = new ConnectionManager(config);
	}

	public Connection getConnection() throws SQLException {

		Connection connection = this.cm.getDataSource().getConnection();

		if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
			return connection;
		}

		connection.close();

		for (int i = 0; i < MAX_TRIES_NEW_CONNECTION; i++) {

			if (logger.isEnabledFor(Level.WARN)) {
				logger.warn("Connection failed: Trying to get new connection. Attempt:" + i);
			}

			connection = this.cm.getDataSource().getConnection();

			if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
				return connection;
			}

			connection.close();
		}

		logger.warn("Connection failed: Trying to get a connection from a new ConnectionManager");
		SqlConfiguration config = SqlConfiguration.getInstance();
		this.cm = new ConnectionManager(config);
		connection = this.cm.getDataSource().getConnection();

		if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
			return connection;
		}

		logger.error("Connection failed!");

		return connection;
	}

	public static void closeConnection(Connection connection) throws SQLException {
		connection.close();
	}

	public static MySQLHelper getInstance() {
		return helper;
	}

	public static List<StepObject> getStepsForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM step WHERE process_id = ? ");
		sql.append("ORDER BY ordering ASC");
		try {
			Object[] params = { processId };
			logger.debug(sql.toString() + ", " + processId);
			List<StepObject> ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectListHandler, params);
			// (connection, stmt, MySQLUtils.resultSetToStepObjectListHandler);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getProcessPropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM processProperty WHERE process_id = ?");

		try {
			Object[] params = { processId };
			logger.debug(sql.toString() + ", " + processId);
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessPropertyListHandler, params);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getTemplatePropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM templateProperty WHERE templateProperty.template_id = ");
		sql.append("(SELECT id FROM template WHERE process_id = ?)");
		try {
			Object[] params = { processId };
			logger.debug(sql.toString() + ", " + processId);
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToTemplatePropertyListHandler, params);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getProductPropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM workpieceProperty WHERE workpieceProperty.workpiece_id = ");
		sql.append("(SELECT id FROM workpiece WHERE process_id = ? )");
		try {
			Object[] params = { processId };
			logger.debug(sql.toString() + ", " + processId);
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProductPropertyListHandler, params);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	
	public static ProcessObject getProcessObjectForId(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM process WHERE id = ? ");
		try {
			Object[] params = { processId };
			logger.debug(sql.toString() + ", " + processId);
			ProcessObject answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessHandler, params);
			return answer;
		} finally {
			closeConnection(connection);
		}

	}
	
	public static Regelsatz getRulesetForId(int rulesetId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ruleset WHERE id = ? ");
		try {
			Object[] params = { rulesetId };
			logger.debug(sql.toString() + ", " + rulesetId);
			Regelsatz ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToRulesetHandler, params);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	
	public static StepObject getStepByStepId(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM step WHERE id = ?");
		// sql.append(" ORDER BY Reihenfolge ASC");

		try {
			Object[] params = { stepId };
			logger.debug(sql.toString() + ", " + stepId);
			StepObject ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectHandler, params);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	
	public static List<String> getScriptsForStep(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM step WHERE id = ? ");
		try {
			Object[] params = { stepId };
			logger.debug(sql.toString() + ", " + stepId);
			List<String> ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToScriptsHandler, params);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}


	public static Map<String, String> getScriptMapForStep(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM step WHERE id = ? ");
		try {
			Object[] params = { stepId };
			logger.debug(sql.toString() + ", " + stepId);
			Map<String, String> ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToScriptMapHandler, params);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}


	
	public int updateStep(StepObject step) throws SQLException {
		int ret = -1;
		Connection connection = helper.getConnection();
		try {

			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE step SET title = ? , ");
			sql.append("ordering = ? , ");
			sql.append("processingStatus = ? , ");
			Timestamp time = null;
			sql.append("processingTime = ? , ");
			if (step.getBearbeitungszeitpunkt() != null) {
				time = new Timestamp(step.getBearbeitungszeitpunkt().getTime());

			}
			Timestamp start = null;
			sql.append("processingBegin = ? , ");
			if (step.getBearbeitungsbeginn() != null) {
				start = new Timestamp(step.getBearbeitungsbeginn().getTime());
			}
			Timestamp end = null;
			sql.append("processingEnd = ? , ");
			if (step.getBearbeitungsende() != null) {
				end = new Timestamp(step.getBearbeitungsende().getTime());
			}

			sql.append("processingUser_id = ? , ");
			sql.append("editType = ?, ");
			sql.append("typeAutomatic = ? ");
			sql.append(" WHERE id = ? ");
			Object[] param = { step.getTitle(), step.getReihenfolge(), step.getBearbeitungsstatus(), time, start, end,
					step.getBearbeitungsbenutzer(), step.getEditType(), step.isTypAutomatisch(), step.getId() };
			if(logger.isDebugEnabled()){
				logger.debug("saving step: " + sql.toString() + ", " + Arrays.toString(param));
			}

			run.update(connection, sql.toString(), param);
			// logger.debug(sql);
			ret = step.getId();
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public void addHistory(Date date, double order, String value, int type, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		Timestamp datetime = new Timestamp(date.getTime());

		try {
			QueryRunner run = new QueryRunner();
			// String propNames = "numericValue, stringvalue, type, date, processId";
			Object[] param = { order, value, type, datetime, processId };
			String sql = "INSERT INTO " + "history" + " (numericValue, stringValue, type, date, process_id) VALUES "
					+ "( ?, ?, ?, ? ,?)";
			if(logger.isTraceEnabled()){
				logger.trace("added history event " + sql + ", " + Arrays.toString(param));
			}
			run.update(connection, sql, param);
		} finally {
			closeConnection(connection);
		}
	}

	

	
	public void updateProcessStatus(String value, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			Object[] param = { value, processId };
			sql.append("UPDATE process SET sortHelperStatus = ? WHERE id = ?");
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			run.update(connection, sql.toString(), param);
		} finally {
			closeConnection(connection);
		}
	}

	public void updateImages(Integer numberOfFiles, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			Object[] param = { numberOfFiles, processId };
			sql.append("UPDATE process SET sortHelperImages = ? WHERE id = ?");
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			run.update(connection, sql.toString(), param);
		} finally {
			closeConnection(connection);
		}
	}
	
	public void updateProcessLog(String logValue, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			Object[] param = { logValue, processId };
			sql.append("UPDATE process SET wikiField = ? WHERE id = ?");
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			run.update(connection, sql.toString(), param);
		} finally {
			closeConnection(connection);
		}
	}

	public static ProjectObject getProjectObjectById(int projectId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM project WHERE id = ?");
		try {
			Object[] param = { projectId };
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			ProjectObject answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectHandler, param);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM projectFileGroup WHERE project_id = ? ");
		try {
			Object[] param = { projectId };
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			List<ProjectFileGroup> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectFilegroupListHandler,
					param);
			return answer;

		} finally {
			closeConnection(connection);
		}
	}
	
	public static List<String> getFilterForUser(int userId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM userProperty WHERE title = '_filter' AND user_id = ?");
		try {
			Object[] param = { userId };
			logger.debug(sql.toString() + ", " + Arrays.toString(param));
			List<String> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToFilterListtHandler, param);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}	
	
	public static void addFilterToUser(int userId, String filterstring) throws SQLException {
		Connection connection = helper.getConnection();
		Timestamp datetime = new Timestamp(new Date().getTime());
		try {
			QueryRunner run = new QueryRunner();
			String propNames = "title, value, isObligatory, dataType, choice, creationDate, user_id";
			Object[] param = { "_filter", filterstring, false, 5, null, datetime, userId };
			String sql = "INSERT INTO " + "userProperty" + " (" + propNames + ") VALUES ( ?, ?,? ,? ,? ,?,? )";
			if(logger.isDebugEnabled()){
				logger.debug(sql + ", " + Arrays.toString(param));
			}
			run.update(connection, sql, param);
		} finally {
			closeConnection(connection);
		}
	}

	public static void removeFilterFromUser(int userId, String filterstring) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			Object[] param = { userId, filterstring };
			String sql = "DELETE FROM userProperty WHERE title = '_filter' AND user_id = ? AND value = ?";
			if(logger.isDebugEnabled()){
				logger.debug(sql + ", " + Arrays.toString(param));
			}
			run.update(connection, sql, param);
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Integer> getStepIds(String query) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerListHandler);
		} finally {
			closeConnection(connection);
		}
	}
	
	public static int getCountOfProcessesWithRuleset(int rulesetId) throws SQLException {
		Connection connection = helper.getConnection();

		String query = "SELECT count(id) FROM process WHERE ruleset_id = ?";
		try {
			Object[] param = { rulesetId };
			return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
		} finally {
			closeConnection(connection);
		}
	}

	public static int getCountOfProcessesWithDocket(int docketId) throws SQLException {
		Connection connection = helper.getConnection();
		String query = "SELECT count(id) FROM process WHERE docket_id = ?";
		try {
			Object[] param = { docketId };
			return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
		} finally {
			closeConnection(connection);
		}
	}

	
	public static int getCountOfProcessesWithTitle(String title) throws SQLException {
		Connection connection = helper.getConnection();
		String query = "SELECT count(id) FROM process WHERE title = ?";
		try {
			Object[] param = { title };
			return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
		} finally {
			closeConnection(connection);
		}
	}

}

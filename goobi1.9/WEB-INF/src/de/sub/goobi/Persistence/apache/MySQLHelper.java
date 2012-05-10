package de.sub.goobi.Persistence.apache;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import de.sub.goobi.Beans.ProjectFileGroup;
import de.sub.goobi.Beans.Regelsatz;

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

		for (int i = 0; i < MAX_TRIES_NEW_CONNECTION; i++) {

			logger.warn("Connection failed: Trying to get new connection. Attempt:" + i);

			connection = this.cm.getDataSource().getConnection();

			if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
				return connection;
			}
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
		sql.append("SELECT * FROM schritte WHERE ProzesseID = " + processId);
		sql.append(" ORDER BY Reihenfolge ASC");
		try {
			logger.debug(sql.toString());
			List<StepObject> ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectListHandler);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getProcessPropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM prozesseeigenschaften WHERE prozesseID = " + processId);
		try {
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessPropertyListHandler);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getTemplatePropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM vorlageneigenschaften WHERE vorlageneigenschaften.vorlagenID = (SELECT VorlagenID FROM vorlagen WHERE ProzesseID = "
				+ processId);
		try {
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToTemplatePropertyListHandler);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<Property> getProductPropertiesForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM werkstueckeeigenschaften WHERE werkstueckeeigenschaften.werkstueckeID = (SELECT werkstueckeID FROM werkstuecke WHERE ProzesseID = "
				+ processId);
		try {
			List<Property> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProductPropertyListHandler);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static ProcessObject getProcessObjectForId(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM prozesse WHERE ProzesseID = " + processId);
		try {
			ProcessObject answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessHandler);
			return answer;
		} finally {
			closeConnection(connection);
		}

	}

	public static Regelsatz getRulesetForId(int rulesetId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = " + rulesetId);
		try {
			Regelsatz ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToRulesetHandler);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public static StepObject getStepByStepId(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM schritte WHERE SchritteID = " + stepId);
		// sql.append(" ORDER BY Reihenfolge ASC");
		try {
			logger.debug(sql.toString());
			StepObject ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectHandler);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<String> getScriptsForStep(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM schritte WHERE SchritteID = " + stepId);
		try {
			logger.debug(sql.toString());
			List<String> ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToScriptsHandler);
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
			sql.append("UPDATE schritte SET Titel = '" + step.getTitle() + "', ");
			sql.append("Reihenfolge = '" + step.getReihenfolge() + "', ");
			sql.append("Bearbeitungsstatus = '" + step.getBearbeitungsstatus() + "', ");
			if (step.getBearbeitungszeitpunkt() != null) {
				sql.append("BearbeitungsZeitpunkt = '" + new Timestamp(step.getBearbeitungszeitpunkt().getTime()) + "', ");
			}
			if (step.getBearbeitungsbeginn() != null) {
				sql.append("BearbeitungsBeginn = '" + new Timestamp(step.getBearbeitungsbeginn().getTime()) + "', ");
			}
			if (step.getBearbeitungsende() != null) {
				sql.append("BearbeitungsEnde = '" + new Timestamp(step.getBearbeitungsende().getTime()) + "', ");
			}
			sql.append("BearbeitungsBenutzerID = '" + step.getBearbeitungsbenutzer() + "', ");
			sql.append("edittype = '" + step.getEditType() + "', ");
			sql.append("typAutomatisch = " + step.isTypAutomatisch());

			sql.append(" WHERE SchritteID = " + step.getId() + ";");

			run.update(connection, sql.toString());
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
			String propNames = "numericValue, stringvalue, type, date, processId";
			String propValues = "'" + order + "','" + value + "','" + type + "','" + datetime + "','" + processId + "'";
			String sql = "INSERT INTO " + "history" + " (" + propNames + ") VALUES (" + propValues + ")";
			run.update(connection, sql);
		} finally {
			closeConnection(connection);
		}
	}

	public void updateProcessStatus(String value, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE prozesse SET sortHelperStatus = '" + value + "' WHERE ProzesseID = " + processId + ";");

			run.update(connection, sql.toString());
		} finally {
			closeConnection(connection);
		}
	}

	public void updateProcessLog(String logValue, int processId) throws SQLException {
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE prozesse SET wikifield = '" + logValue + "' WHERE ProzesseID = " + processId + ";");

			run.update(connection, sql.toString());
		} finally {
			closeConnection(connection);
		}
	}

	public static ProjectObject getProjectObjectById(int projectId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM projekte WHERE ProjekteID = " + projectId);
		try {
			ProjectObject answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectHandler);
			return answer;
		} finally {
			closeConnection(connection);
		}
	}

	public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM projectfilegroups WHERE ProjekteID = " + projectId);
		try {
			List<ProjectFileGroup> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectFilegroupListHandler);
			return answer;
			
		} finally {
			closeConnection(connection);
		}
	}

}

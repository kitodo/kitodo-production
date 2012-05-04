package de.sub.goobi.Persistence.apache;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import de.sub.goobi.Beans.HistoryEvent;

public class DbHelper {

	private static final int MAX_TRIES_NEW_CONNECTION = 5;
	private static final int TIME_FOR_CONNECTION_VALID_CHECK = 5;

	private static final Logger logger = Logger.getLogger(DbHelper.class);

	private static DbHelper helper = new DbHelper();
	private ConnectionManager cm = null;

	private DbHelper() {
		SqlConfiguration config = new SqlConfiguration();
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

		SqlConfiguration config = new SqlConfiguration();
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

	public static DbHelper getInstance() {
		return helper;
	}

	public static List<StepObject> getStepsForProcess(int processId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM schritte WHERE ProzesseID = " + processId);
		sql.append(" ORDER BY Reihenfolge ASC");
		try {
			logger.debug(sql.toString());
			List<StepObject> ret = new QueryRunner().query(connection, sql.toString(), DbUtils.resultSetToStepObjectListHandler);
			return ret;
		} finally {
			closeConnection(connection);
		}
	}

	public static StepObject getStepByStepId(int stepId) throws SQLException {
		Connection connection = helper.getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM schritte WHERE SchritteID = " + stepId);
		sql.append(" ORDER BY Reihenfolge ASC");
		try {
			logger.debug(sql.toString());
			StepObject ret = new QueryRunner().query(connection, sql.toString(), DbUtils.resultSetToStepObjectHandler);
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

	public void addHistory(HistoryEvent he) throws SQLException {
		double numericValue = he.getNumericValue().doubleValue();
		String stringvalue = he.getStringValue();
		int type = he.getHistoryType().getValue();
		Timestamp date = new Timestamp(he.getDate().getTime());
		int processId = he.getProcess().getId();
		Connection connection = helper.getConnection();
		try {
			QueryRunner run = new QueryRunner();
			String propNames = "numericValue, stringvalue, type, date, processId";
			String propValues = "'" + numericValue + "','" + stringvalue + "','" + type + "','" + date + "','" + processId + "'";
			String sql = "INSERT INTO " + "history" + " (" + propNames + ") VALUES (" + propValues + ")";
			run.update(connection, sql);
		} finally {
			closeConnection(connection);
		}
	}

	public void updateProcess(String value, int processId) throws SQLException {
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

}

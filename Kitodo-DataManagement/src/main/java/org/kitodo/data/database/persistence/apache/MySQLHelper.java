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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.Ruleset;

public class MySQLHelper {

    private static final int MAX_TRIES_NEW_CONNECTION = 5;
    private static final int TIME_FOR_CONNECTION_VALID_CHECK = 5;

    private static final Logger logger = LogManager.getLogger(MySQLHelper.class);

    private static MySQLHelper helper = new MySQLHelper();
    private ConnectionManager cm = null;

    private MySQLHelper() {
        SqlConfiguration config = SqlConfiguration.getInstance();
        this.cm = new ConnectionManager(config);
    }

    /**
     * Get Connection.
     *
     * @return connection
     */
    public Connection getConnection() throws SQLException {

        Connection connection = this.cm.getDataSource().getConnection();

        if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
            return connection;
        }

        connection.close();

        for (int i = 0; i < MAX_TRIES_NEW_CONNECTION; i++) {

            if (logger.isWarnEnabled()) {
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

    /**
     * Get tasks for process.
     *
     * @param processId
     *            id of process
     * @return tasks for process
     */
    public static List<StepObject> getStepsForProcess(int processId) throws SQLException {
        Connection connection = helper.getConnection();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM task WHERE process_id = ? ");
        sql.append("ORDER BY ordering ASC");
        try {
            Object[] params = {processId };
            logger.debug(sql.toString() + ", " + processId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectListHandler,
                    params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get process' properties for given id of process.
     *
     * @param processId
     *            id of process
     * @return process' properties
     */
    public static List<Property> getProcessPropertiesForProcess(int processId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM property AS p ");
        sql.append("INNER JOIN process_x_property AS pxp ON pxp.property_id = p.id ");
        sql.append("WHERE pxp.process_id = ?");
        try {
            Object[] params = {processId };
            logger.debug(sql.toString() + ", " + processId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessPropertyListHandler,
                    params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get template's properties for given id of process.
     *
     * @param processId
     *            id of process
     * @return template's properties for process
     */
    public static List<Property> getTemplatePropertiesForProcess(int processId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM property AS p ");
        sql.append("INNER JOIN template_x_property AS txp ON uxp.property_id = p.id ");
        sql.append("WHERE txp.template_id = ");
        sql.append("(SELECT id FROM template WHERE process_id = ?)");
        try {
            Object[] params = {processId };
            logger.debug(sql.toString() + ", " + processId);
            return new QueryRunner().query(connection, sql.toString(),
                    MySQLUtils.resultSetToTemplatePropertyListHandler, params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get product's properties for process.
     *
     * @param processId
     *            id of process
     * @return product's properties for process
     */
    public static List<Property> getProductPropertiesForProcess(int processId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM property AS p ");
        sql.append("INNER JOIN workpiece_x_property AS wxp ON uxp.property_id = p.id ");
        sql.append("WHERE wxp.workpiece_id = ");
        sql.append("(SELECT id FROM workpiece WHERE process_id = ? )");
        try {
            Object[] params = {processId };
            logger.debug(sql.toString() + ", " + processId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProductPropertyListHandler,
                    params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get process' object for id.
     *
     * @param processId
     *            id of process
     * @return process' object
     */
    public static ProcessObject getProcessObjectForId(int processId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM process WHERE id = ? ");
        try {
            Object[] params = {processId };
            logger.debug(sql.toString() + ", " + processId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProcessHandler, params);
        } finally {
            closeConnection(connection);
        }

    }

    /**
     * Get ruleset for id.
     *
     * @param rulesetId
     *            id of ruleset
     * @return ruleset
     */
    public static Ruleset getRulesetForId(int rulesetId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ruleset WHERE id = ? ");
        try {
            Object[] params = {rulesetId };
            logger.debug(sql.toString() + ", " + rulesetId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToRulesetHandler, params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get task by task id.
     *
     * @param stepId
     *            id of task
     * @return task
     */
    public static StepObject getStepByStepId(int stepId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM task WHERE id = ?");
        try {
            Object[] params = {stepId };
            logger.debug(sql.toString() + ", " + stepId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStepObjectHandler, params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get scripts for task.
     *
     * @param stepId
     *            id of task
     * @return list of scripts
     */
    public static List<String> getScriptsForStep(int stepId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM task WHERE id = ? ");
        try {
            Object[] params = {stepId };
            logger.debug(sql.toString() + ", " + stepId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToScriptsHandler, params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get script's map for task.
     *
     * @param stepId
     *            id of task
     * @return map of script
     */
    public static Map<String, String> getScriptMapForStep(int stepId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM task WHERE id = ? ");
        try {
            Object[] params = {stepId };
            logger.debug(sql.toString() + ", " + stepId);
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToScriptMapHandler, params);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Update task.
     *
     * @param step
     *            task object
     * @return int
     */
    public int updateStep(StepObject step) throws SQLException {
        int ret = -1;
        Connection connection = helper.getConnection();
        try {
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE task SET title = ? , ");
            sql.append("ordering = ? , ");
            sql.append("processingStatus = ? , ");
            Timestamp time = null;
            sql.append("processingTime = ? , ");
            if (step.getProcessingTime() != null) {
                time = new Timestamp(step.getProcessingTime().getTime());

            }
            Timestamp start = null;
            sql.append("processingBegin = ? , ");
            if (step.getProcessingBegin() != null) {
                start = new Timestamp(step.getProcessingBegin().getTime());
            }
            Timestamp end = null;
            sql.append("processingEnd = ? , ");
            if (step.getProcessingEnd() != null) {
                end = new Timestamp(step.getProcessingEnd().getTime());
            }

            sql.append("processingUser_id = ? , ");
            sql.append("editType = ?, ");
            sql.append("typeAutomatic = ? ");
            sql.append(" WHERE id = ? ");
            Object[] param = {step.getTitle(), step.getOrdering(), step.getProcessingStatus(), time, start, end,
                    step.getProcessingUser(), step.getEditType(), step.isTypeAutomatic(), step.getId() };
            if (logger.isDebugEnabled()) {
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

    /**
     * Add history.
     *
     * @param date
     *            date
     * @param order
     *            order
     * @param value
     *            String
     * @param type
     *            type
     * @param processId
     *            id of process
     */
    public void addHistory(Date date, double order, String value, int type, int processId) throws SQLException {
        Connection connection = helper.getConnection();
        Timestamp datetime = new Timestamp(date.getTime());

        try {
            QueryRunner run = new QueryRunner();
            // String propNames = "numericValue, stringvalue, type, date,
            // processId";
            Object[] param = {order, value, type, datetime, processId };
            String sql = "INSERT INTO " + "history" + " (numericValue, stringValue, type, date, process_id) VALUES "
                    + "( ?, ?, ?, ? ,?)";
            if (logger.isTraceEnabled()) {
                logger.trace("added history event " + sql + ", " + Arrays.toString(param));
            }
            run.update(connection, sql, param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Update process' status.
     *
     * @param value
     *            String
     * @param processId
     *            id of process
     */
    public void updateProcessStatus(String value, int processId) throws SQLException {
        Connection connection = helper.getConnection();
        try {
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            Object[] param = {value, processId };
            sql.append("UPDATE process SET sortHelperStatus = ? WHERE id = ?");
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            run.update(connection, sql.toString(), param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Update Images.
     *
     * @param numberOfFiles
     *            amount of image files to update
     * @param processId
     *            id of process
     */
    public void updateImages(Integer numberOfFiles, int processId) throws SQLException {
        Connection connection = helper.getConnection();
        try {
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            Object[] param = {numberOfFiles, processId };
            sql.append("UPDATE process SET sortHelperImages = ? WHERE id = ?");
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            run.update(connection, sql.toString(), param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Update process log.
     *
     * @param logValue
     *            String
     * @param processId
     *            id of process
     */
    public void updateProcessLog(String logValue, int processId) throws SQLException {
        Connection connection = helper.getConnection();
        try {
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            Object[] param = {logValue, processId };
            sql.append("UPDATE process SET wikiField = ? WHERE id = ?");
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            run.update(connection, sql.toString(), param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get project's object by id.
     *
     * @param projectId
     *            id of project
     * @return project object
     */
    public static ProjectObject getProjectObjectById(int projectId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM project WHERE id = ?");
        try {
            Object[] param = {projectId };
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get file groups' for project's id.
     *
     * @param projectId
     *            id of project
     * @return list of project's file groups
     */
    public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM projectFileGroup WHERE project_id = ? ");
        try {
            Object[] param = {projectId };
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            return new QueryRunner().query(connection, sql.toString(),
                    MySQLUtils.resultSetToProjectFilegroupListHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get step ids.
     *
     * @param query
     *            String
     * @return list of ids
     */
    public static List<Integer> getStepIds(String query) throws SQLException {
        Connection connection = helper.getConnection();
        try {
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerListHandler);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get count of processes with ruleset.
     *
     * @param rulesetId
     *            id of ruleset
     * @return amount of processes with ruleset
     */
    public static int getCountOfProcessesWithRuleset(int rulesetId) throws SQLException {
        Connection connection = helper.getConnection();

        String query = "SELECT count(id) FROM process WHERE ruleset_id = ?";
        try {
            Object[] param = {rulesetId };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get count of processes with docket.
     *
     * @param docketId
     *            id of docket
     * @return amount of processes with docket
     */
    public static int getCountOfProcessesWithDocket(int docketId) throws SQLException {
        Connection connection = helper.getConnection();
        String query = "SELECT count(id) FROM process WHERE docket_id = ?";
        try {
            Object[] param = {docketId };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get count of processes with title.
     *
     * @param title
     *            String
     * @return amount of processes with title
     */
    public static int getCountOfProcessesWithTitle(String title) throws SQLException {
        Connection connection = helper.getConnection();
        String query = "SELECT count(id) FROM process WHERE title = ?";
        try {
            Object[] param = {title };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

}

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

        if (!connection.isClosed()) {
            connection.close();
        }

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
        sql.append("SELECT * FROM schritte WHERE ProzesseID = ?");
        sql.append(" ORDER BY Reihenfolge ASC");
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
        sql.append("SELECT * FROM prozesseeigenschaften WHERE prozesseID = ?");

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
        sql.append("SELECT * FROM vorlageneigenschaften WHERE vorlageneigenschaften.vorlagenID = (SELECT VorlagenID FROM vorlagen WHERE ProzesseID = ?)");
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
        sql.append("SELECT * FROM werkstueckeeigenschaften WHERE werkstueckeeigenschaften.werkstueckeID = (SELECT werkstueckeID FROM werkstuecke WHERE ProzesseID = ? )");
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
        sql.append("SELECT * FROM prozesse WHERE ProzesseID = ? ");
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
        sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = ? ");
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

        sql.append("SELECT * FROM schritte WHERE SchritteID = ?");
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
        sql.append("SELECT * FROM schritte WHERE SchritteID = ? ");
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
        sql.append("SELECT * FROM schritte WHERE SchritteID = ? ");
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
            sql.append("UPDATE schritte SET Titel = ? , ");
            sql.append("Reihenfolge = ? , ");
            sql.append("Bearbeitungsstatus = ? , ");
            Timestamp time = null;
            sql.append("BearbeitungsZeitpunkt = ? , ");
            if (step.getBearbeitungszeitpunkt() != null) {
                time = new Timestamp(step.getBearbeitungszeitpunkt().getTime());

            }
            Timestamp start = null;
            sql.append("BearbeitungsBeginn = ? , ");
            if (step.getBearbeitungsbeginn() != null) {
                start = new Timestamp(step.getBearbeitungsbeginn().getTime());
            }
            Timestamp end = null;
            sql.append("BearbeitungsEnde = ? , ");
            if (step.getBearbeitungsende() != null) {
                end = new Timestamp(step.getBearbeitungsende().getTime());
            }

            sql.append("BearbeitungsBenutzerID = ? , ");
            sql.append("edittype = ?, ");
            sql.append("typAutomatisch = ? ");
            sql.append(" WHERE SchritteID = ? ");
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
            String sql = "INSERT INTO " + "history" + " (numericValue, stringvalue, type, date, processId) VALUES ( ?, ?, ?, ? ,?)";
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
            sql.append("UPDATE prozesse SET sortHelperStatus = ? WHERE ProzesseID = ?");
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
            sql.append("UPDATE prozesse SET sortHelperImages = ? WHERE ProzesseID = ?");
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
            sql.append("UPDATE prozesse SET wikifield = ? WHERE ProzesseID = ?");
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            run.update(connection, sql.toString(), param);
        } finally {
            closeConnection(connection);
        }
    }

    public static ProjectObject getProjectObjectById(int projectId) throws SQLException {
        Connection connection = helper.getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte WHERE ProjekteID = ?");
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

        sql.append("SELECT * FROM projectfilegroups WHERE ProjekteID = ? ");
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
        sql.append("SELECT * FROM benutzereigenschaften WHERE Titel = '_filter' AND BenutzerID = ?");
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
            String propNames = "Titel, Wert, IstObligatorisch, DatentypenID, Auswahl, creationDate, BenutzerID";
            Object[] param = { "_filter", filterstring, false, 5, null, datetime, userId };
            String sql = "INSERT INTO " + "benutzereigenschaften" + " (" + propNames + ") VALUES ( ?, ?,? ,? ,? ,?,? )";
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
            String sql = "DELETE FROM benutzereigenschaften WHERE Titel = '_filter' AND BenutzerID = ? AND Wert = ?";
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

        String query = "select count(ProzesseID) from prozesse where MetadatenKonfigurationID = ?";
        try {
            Object[] param = { rulesetId };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    public static int getCountOfProcessesWithDocket(int docketId) throws SQLException {
        Connection connection = helper.getConnection();
        String query = "select count(ProzesseID) from prozesse where  docketID= ?";
        try {
            Object[] param = { docketId };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }


    public static int getCountOfProcessesWithTitle(String title) throws SQLException {
        Connection connection = helper.getConnection();
        String query = "select count(ProzesseID) from prozesse where  titel = ?";
        try {
            Object[] param = { title };
            return new QueryRunner().query(connection, query, MySQLUtils.resultSetToIntegerHandler, param);
        } finally {
            closeConnection(connection);
        }
    }

    public static void main(String[] args) throws SQLException {
        MySQLHelper helper = MySQLHelper.getInstance();
        int start = 10000;
        int end = 1000000;
        long starttime = System.currentTimeMillis();
        generateProcesses(helper, start, end);
        generateProcessProperties(helper, start, end);
        generateSteps(helper, start, end);
        long endtime = System.currentTimeMillis();
        System.out.println("duration: " + (endtime - starttime));
    }

    private static void generateProcesses(MySQLHelper helper, int start, int end) throws SQLException {
        String propNames = "ProzesseID, Titel, IstTemplate, erstellungsdatum, ProjekteID, MetadatenKonfigurationID,inAuswahllisteAnzeigen, sortHelperStatus";
        StringBuilder propValues = new StringBuilder();
        Timestamp datetime = new Timestamp(new Date().getTime());
        try (Connection connection = helper.getConnection()) {
            QueryRunner run = new QueryRunner();
            for (int processId = start; processId <= end; processId++) {
                propValues.append("(" + processId + ", 'title" + processId + "', false, '" + datetime + "', 3, 1, false, '020000080' ),");

                if (processId % 5000 == 0) {
                    String values = propValues.toString();
                    values = values.substring(0, values.length() - 1);
                    String sql = "INSERT INTO " + "prozesse" + " (" + propNames + ") VALUES " + values + ";";
                    run.update(connection, sql);
                    propValues = new StringBuilder();
                }

            }
        }
    }

    private static void generateProcessProperties(MySQLHelper helper2, int start, int end) throws SQLException {
        String propNames = "Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, prozesseID, creationDate,container";
        StringBuilder propValues = new StringBuilder();
        Timestamp datetime = new Timestamp(new Date().getTime());
        try (Connection connection = helper.getConnection()) {
            QueryRunner run = new QueryRunner();
            for (int processId = start; processId <= end; processId++) {
                propValues.append("('title', '" + processId + "', false, 5, NULL, " + processId + ",'" + datetime + "',0" + "),");
                if (processId % 5000 == 0) {
                    String values = propValues.toString();
                    values = values.substring(0, values.length() - 1);
                    String sql = "INSERT INTO " + "prozesseeigenschaften" + " (" + propNames + ") VALUES " + values + ";";
                    run.update(connection, sql);
                    propValues = new StringBuilder();
                }
            }
        }
    }

    private static void generateSteps(MySQLHelper helper, int start, int end) throws SQLException {
        String propNames = "SchritteID, Titel, Prioritaet, Reihenfolge, Bearbeitungsstatus, typAutomatisch, typAutomatischScriptpfad, ProzesseID, typScriptStep, scriptName1, homeverzeichnisNutzen, typMetadaten,"
                + " typBeimAnnehmenModul, typImagesLesen, typBeimAnnehmenModulUndAbschliessen, typImagesSchreiben, typBeimAbschliessenVerifizieren, typExportDMS, typImportFileUpload, typBeimAnnehmenAbschliessen, typExportRus";

        String userNames = "schritteID, BenutzerID";
        StringBuilder userProps = new StringBuilder();
        int stepId = 100000;
        StringBuilder propValues = new StringBuilder();
        try (Connection connection = helper.getConnection()) {
            QueryRunner run = new QueryRunner();
            for (int processId = start; processId <= end; processId++) {
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'start',1,1,1,false, ''," + processId
                        + ", false, '', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch01',1,2,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch02',1,3,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch03',1,4,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch04',1,5,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch05',1,6,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch06',1,7,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch07',1,8,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch08',1,9,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch09',1,10,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                userProps.append("(" + stepId + ", 1 ),");
                propValues.append("(" + stepId++ + ",'automatisch10',1,11,0,true, '/bin/bash /usr/local/goobi/scripts/dummy2.sh (stepid)',"
                        + processId + ", true, 'dummy', 0, false, false, false, false, false, false, false, false, false, false" + "),");
                if (processId % 5000 == 0) {
                    String values = propValues.toString();
                    values = values.substring(0, values.length() - 1);
                    String sql = "INSERT INTO " + "schritte" + " (" + propNames + ") VALUES " + values + ";";
                    run.update(connection, sql);
                    propValues = new StringBuilder();

                    String userValues = userProps.toString();
                    userValues = userValues.substring(0, userValues.length() - 1);
                    String userSql = "INSERT INTO " + "schritteberechtigtebenutzer" + " (" + userNames + ") VALUES " + userValues + ";";
                    run.update(connection, userSql);
                    userProps = new StringBuilder();
                }
            }
        }
    }
}

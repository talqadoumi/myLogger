/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jo.aspire.automation.logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author tariqalqadoumi
 */
public class DBLogger 
{

    private static DBLogger dbDataSourceLogger;
    private final BasicDataSource basicDataSource;

    /**
     *
     * Set Database pool using BasicDataSource pool
     *
     *
     * https://commons.apache.org/proper/commons-dbcp/configuration.html
     *
     *
     *
     *
     */
    private DBLogger() {

        EnvirommentManager propsUtil = EnvirommentManager.getInstance();
        String username = propsUtil.getProperty("db.username");
        String password = propsUtil.getProperty("db.password");
        String connString = propsUtil.getProperty("db.connectionString");
        String driver = propsUtil.getProperty("db.driver");

        basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driver);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setUrl(connString);
        basicDataSource.setMaxActive(8);
        basicDataSource.setMinIdle(5);
        basicDataSource.setMaxIdle(10);
        basicDataSource.setMaxOpenPreparedStatements(20);

    }

    /**
     *
     * @return database connection instance if it not exist
     */
    public static DBLogger getInstance() {
        if (dbDataSourceLogger == null) {
            dbDataSourceLogger = new DBLogger();
            return dbDataSourceLogger;
        } else {
            return dbDataSourceLogger;
        }
    }

    /**
     *
     * @return
     *
     * Return DB Connection object
     */
    private Connection getConnection() {
        try {
            return this.basicDataSource.getConnection();
        } catch (SQLException ex) {
          
            AspireLog4j.setLoggerMessageLevel("Error in get Database connection ", Log4jLevels.ERROR, ex);

            return null;
        }
    }

    /**
     * Method used to insert run id into run_log log by jdbc insert
     *
     * @param projectId
     *
     * @param clientId
     * @return inserted id
     */
    public int insertRunID(String projectId, String clientId) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        int runId = 0;
        String insertTableSQL = "INSERT INTO RUN_LOG" + "( PROJECT_ID,CLIENT_ID,CREATION_DATE) VALUES" + "(?,?,NOW() )";

        try {
            dbConnection = DBLogger.getInstance().getConnection();
            preparedStatement = dbConnection.prepareStatement(insertTableSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, Integer.parseInt(projectId));
            preparedStatement.setInt(2, Integer.parseInt(clientId));
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                runId = generatedKeys.getInt(1);
            }

        } catch (SQLException e) {

            AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, e);

        } finally {

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, ex);
                }
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException ex) {
                    AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, ex);
                }
            }

        }
       
        return runId;
    }

   
    /**
     * Method used to insert log with information into logs table (JDBC insert)
     *
     * @param logger logger class
     * @param level log4j level info , warn , error ....
     * @param message log4j message
     * @param stacktrace
     *
     * exception stack trace
     * @param runId
     *
     * run_id
     */
    public static int insertLogIntoDatabase(String logger, String message, String stacktrace, String runId, String level) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String insertSql = "INSERT INTO LOGS ( DATED,LOGGER ,MESSAGE,STACKTRACE,RUN_ID,LEVEL) VALUES" + "(NOW(),?,?,?,?,? )";
        int result = 0;
        try {
            dbConnection = DBLogger.getInstance().getConnection();
            preparedStatement = dbConnection.prepareStatement(insertSql);
            preparedStatement.setString(1, logger);
            preparedStatement.setString(2, message);
            preparedStatement.setString(3, stacktrace);
            preparedStatement.setString(4, runId);
            preparedStatement.setString(5, level);

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {

            AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, e);

        } finally {

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, ex);
                }
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException ex) {
                    AspireLog4j.setLoggerMessageLevel("DB EXCEPTION ! ", Log4jLevels.ERROR, ex);
                }
            }

        }

        return result;

    }

}

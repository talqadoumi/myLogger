/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jo.aspire.automation.logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class AspireLog4j 


{

    /**
     * Static Block used to set the Current Date as part of log file name
     * current.date Property used inside log4j.properties
     */
    static {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        System.setProperty("current.date", dateFormat.format(new Date()));

        String log4jFilePath = System.getProperty("user.dir") + File.separator + "logs" + File.separator;
        System.setProperty("log4jFilePath", log4jFilePath);

    }

    private static Logger logger;
    private static int runDBId;
    private static AspireLog4j instance;
    private static String loggerName;

    private static AspireLog4j getInstance() {

        if (instance == null) {

            instance = new AspireLog4j();

        }

        return instance;

    }

    public static int getRunDBId() {
        return runDBId;
    }

    public void setRunDBId(int runDBId) {
        this.runDBId = runDBId;
    }

    public AspireLog4j() {

        EnvirommentManager propsUtil = EnvirommentManager.getInstance();
        Boolean apiCall = Boolean.valueOf(propsUtil.getProperty("logger.api.enable"));
        Boolean databaseCall = Boolean.valueOf(propsUtil.getProperty("logger.db.enable"));

        loggerName = this.getClass().getName();
        logger = LogManager.getLogger(AspireLog4j.class.getName());
        logger.trace("Logger instance Created");

        if (apiCall) {

            String runIdDBResult = AspireApiLog4j.InsertIntoRunLogAPI();
            this.setRunDBId(Integer.parseInt(runIdDBResult));

        }

        if (databaseCall) {
            int runIdDBResult = DBLogger.getInstance().insertRunID("0", "0");
            setRunDBId(runIdDBResult);
        }
    }

    public static void main(String args[]) throws IOException {

        setLoggerMessageLevel("INFO _ LOG _ API ", Log4jLevels.INFO);
        setLoggerMessageLevel("Logger New Test API ", Log4jLevels.WARN);

    }

    /**
     *
     * @param message Message to be set inside the logger
     * @param level
     *
     * enum value from Log4jLevels eg : Log4jLevels.INFO the enum contain log4j
     * levels ( DEBUG, INFO, WARN, ERROR, FATAL;)
     */
    public static void setLoggerMessageLevel(String message, Log4jLevels level) {

        getInstance();
        EnvirommentManager propsUtil = EnvirommentManager.getInstance();
        //Invoke //

        Boolean apiCall = Boolean.valueOf(propsUtil.getProperty("logger.api.enable"));
        Boolean databaseCall = Boolean.valueOf(propsUtil.getProperty("logger.db.enable"));

        switch (level) {

            case DEBUG:
                logger.debug(message);
                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, "", String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, "", String.valueOf(getRunDBId()), level.name());

                }

                break;
            case INFO:
                logger.info(message);

                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, "", String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, "", String.valueOf(getRunDBId()), level.name());

                }
                break;

            case WARN:
                logger.warn(message);
                if (apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, "", String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, "", String.valueOf(getRunDBId()), level.name());

                }
                break;

            case ERROR:
                logger.error(message);

                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, "", String.valueOf(getRunDBId()));

                } else if (databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, "", String.valueOf(getRunDBId()), level.name());

                }

                break;

            case FATAL:
                logger.fatal(message);
                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, "", String.valueOf(getRunDBId()));

                } else if (databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, "", String.valueOf(getRunDBId()), level.name());

                }

                break;
        }

    }

    /**
     *
     * @param message Message to be set inside the logger
     * @param level enum value from Log4jLevels eg : Log4jLevels.INFO the enum
     * contain log4j levels ( DEBUG, INFO, WARN, ERROR, FATAL;)
     * @param exception exception object to log stack strace
     *
     *
     * pass exception to be logged in case of ERROR , FATAL LEVEL
     */
    public static void setLoggerMessageLevel(String message, Log4jLevels level, Exception exception) {
        getInstance();
        EnvirommentManager propsUtil = EnvirommentManager.getInstance();
        Boolean apiCall = Boolean.valueOf(propsUtil.getProperty("logger.api.enable"));
        Boolean databaseCall = Boolean.valueOf(propsUtil.getProperty("logger.db.enable"));

        switch (level) {

            case DEBUG:
                logger.debug(message, exception);

                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, getStackTrace(exception), String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, getStackTrace(exception), String.valueOf(getRunDBId()), level.name());

                }

                break;
            case INFO:
                logger.info(message, exception);

                if (logger.isInfoEnabled() && apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, getStackTrace(exception), String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, getStackTrace(exception), String.valueOf(getRunDBId()), level.name());

                }
                break;

            case WARN:
                logger.warn(message, exception);
                if (apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, getStackTrace(exception), String.valueOf(getRunDBId()));

                } else if (logger.isInfoEnabled() && databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, getStackTrace(exception), String.valueOf(getRunDBId()), level.name());

                }
                break;

            case ERROR:
                logger.error(message, exception);
                if (apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, getStackTrace(exception), String.valueOf(getRunDBId()));

                } else if (databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, getStackTrace(exception), String.valueOf(getRunDBId()), level.name());

                }
                break;

            case FATAL:
                logger.fatal(message, exception);
                if (apiCall) {

                    AspireApiLog4j.InsertIntoLogAPI(loggerName, level.name(), message, getStackTrace(exception), String.valueOf(getRunDBId()));

                } else if (databaseCall) {

                    DBLogger.insertLogIntoDatabase(loggerName, message, getStackTrace(exception), String.valueOf(getRunDBId()), level.name());

                }

                break;
        }

    }

    /**
     * Create Log4j file programmatically
     *
     * @param fileName
     *
     * log4j file name to be created the file name should be sent without
     * extension
     *
     */
    public static void createNewLogFile(String fileName) {

        getInstance();
        String newLogFile = System.getProperty("user.dir") + File.separator + "logs" + File.separator + fileName + "_"
                + System.getProperty("current.date") + ".log";

        FileAppender fileAppender = null;
        try {
            fileAppender = new FileAppender(new PatternLayout("%-5p [%d{MMM dd HH:mm:ss}]  (%F:%L) - %m%n"), newLogFile,
                    true);

        } catch (IOException ex) {

            logger.error("ERROR in create log4j file", ex);

        }
        logger.addAppender(fileAppender);
        logger.setLevel((Level) Level.ALL);
    }

    private static String getStackTrace(Exception ex) {

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        return exceptionAsString;

    }

}

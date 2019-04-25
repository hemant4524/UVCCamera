package com.serenegiant.utils;



import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/* 
 * Class is for SLF4J using LOGBACK
 * it provide current selected value of combo-box used for log level according this selected value entry is made in log file 
 * @author Dharmesh R. Patel <dharmesh@unoindia.co> 
 * */
public class SysLogger {
    static void SysLogger(){
        configureLogbackDirectly();
    }
    static {
        configureLogbackDirectly();
    }

    public static Logger logger = (Logger) LoggerFactory.getLogger(SysLogger.class);

    public static void configureLogbackDirectly() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        // setup FileAppender
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{HH:mm:ss.SSS} %-5level [%thread] %msg%n");
        encoder1.start();

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(lc);
        fileAppender.setFile(Settings.LOG_DIRECTORY + "log.txt");

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(lc);
        // rolling policies need to know their parent
        // it's one of the rare cases, where a sub-component knows about its
        // parent
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(Settings.LOG_DIRECTORY + "log.%d.%i.txt.zip");

        SizeAndTimeBasedFNATP<ILoggingEvent> tbfatp = new SizeAndTimeBasedFNATP<ILoggingEvent>();
        tbfatp.setMaxFileSize("2MB");
        tbfatp.setContext(lc);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(tbfatp);
        rollingPolicy.setMaxHistory(15);
        rollingPolicy.start();

        fileAppender.setEncoder(encoder1);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);
        root.setLevel(Level.INFO);
    }

    public static Logger getGlobalLogger() {
        setLogLevel();
        return logger;
    }

    // Fetch the current selected combo-box value for Log Level
    public static Level getLogLevel(int logLevel) {
        Level level = Level.INFO;
        switch (logLevel) {
            case 1: {
                level = Level.TRACE;
                break;
            }
            case 2: {
                level = Level.DEBUG;
                break;
            }
            case 3: {
                level = Level.INFO;
                break;
            }
            case 4: {
                level = Level.WARN;
                break;
            }
            case 5: {
                level = Level.ERROR;
                break;
            }
            case 6: {
                level = Level.OFF;
                break;
            }
            default:
                level = Level.INFO;
                break;
        }
        logger.info("Log Level: " + level);
        return level;
    }

    public static void setLogLevel() {
        logger.setLevel(getLogLevel(Preference.getInstance().getLogLevel()));
    }

    public static void setLogLevel(Level lvl) {
        logger.setLevel(lvl);
    }

    public static void setLogLevel(int level) {
        logger.setLevel(getLogLevel(level));
    }

    public static void err(String message) {
        logger.error(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void trace(String message) {
        logger.trace(message);
    }

    public static void err(String message, Throwable e) {
        logger.error(message, e);
    }

    public static void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    public static void test() {
        logger.error("error");
        logger.warn("warn");
        logger.info("info");
        logger.debug("debug");
        logger.trace("trace");
    }
}
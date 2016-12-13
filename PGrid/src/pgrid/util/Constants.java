package pgrid.util;

import pgrid.util.logging.FlushedStreamHandler;
import pgrid.util.logging.LogFilter;
import pgrid.util.logging.LogFormatter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * This class represents global constants.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Constants {

	/**
	 * The system specific file separator.
	 */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * The system line seperator.
	 */
	public static final String LINE_SEPERATOR = System.getProperty("line.separator");

	/**
	 * The Log Formatter.
	 */
	protected static LogFormatter mLogFormatter = new LogFormatter();

	/**
	 * Initializes a child logger.
	 *
	 * @param parent    the parent logger.
	 * @param childLog  a child logger.
	 * @param formatter a log formatter for a logging file.
	 * @param logFile   the used loggin file.
	 */
	protected static void initChildLogger(Logger parent, Logger childLog, LogFormatter formatter, String logFile) {
		// set and use a parent logger
		childLog.setParent(parent);
		childLog.setUseParentHandlers(true);

		if (logFile != null) {
			try {
				FileHandler fHandler = new FileHandler(pgrid.Constants.LOG_DIR + logFile);
				fHandler.setFormatter(formatter);
				childLog.addHandler(fHandler);
			} catch (IOException e) {
				parent.log(Level.WARNING, "Could not use logging file '" + pgrid.Constants.LOG_DIR + logFile + "'!", e);
			} catch (SecurityException e) {
				parent.log(Level.WARNING, "Could not use logging file '" + pgrid.Constants.LOG_DIR + logFile + "'!", e);
			}
		}
	}

	/**
	 * Initializes the logger.
	 *
	 * @param logger     the logger to initialize.
	 * @param parent     a parent logger.
	 * @param debugLevel the used debug level (0-3).
	 * @param verbose    if also the System.out should be used for logging.
	 * @param logFile    the used loggin file.
	 */
	public static void initLogger(Logger logger, Logger parent, int debugLevel, boolean verbose, String logFile) {
		// set and use a given parent logger
		if (parent != null) {
			logger.setParent(parent);
			logger.setUseParentHandlers(true);
		}

		// set the logging level
		if (debugLevel >= 0) {
			switch (debugLevel) {
				case (0):
					logger.setLevel(Level.CONFIG);
					break;
				case (1):
					logger.setLevel(Level.FINE);
					break;
				case (2):
					logger.setLevel(Level.FINER);
					break;
				case (3):
					logger.setLevel(Level.FINEST);
					break;
			}
			mLogFormatter.setFormatPattern(LogFormatter.DEBUG_PATTERN);
		}

		// add a handler for System.out to the logger
		StreamHandler sHandler = new FlushedStreamHandler(System.out, mLogFormatter);
		String[] loggers = null;
		if (verbose) {
			sHandler.setLevel(Level.ALL);
			Level[] sLevels = {Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO};
			sHandler.setFilter(new LogFilter(sLevels, loggers, true));
			logger.addHandler(sHandler);
		}

		// add a handler for the logging file
		if (logFile != null) {
			if (pgrid.Constants.LOG_DIR.length() > 0)
				new File(pgrid.Constants.LOG_DIR).mkdirs();
			try {
				FileHandler fHandler = new FileHandler(pgrid.Constants.LOG_DIR + logFile);
				fHandler.setFormatter(mLogFormatter);
				logger.addHandler(fHandler);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Could not use logging file '" + pgrid.Constants.LOG_DIR + logFile + "'!", e);
			} catch (SecurityException e) {
				logger.log(Level.WARNING, "Could not use logging file '" + pgrid.Constants.LOG_DIR + logFile + "'!", e);
			}
		}
	}

}
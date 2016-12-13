/**
 * $Id: Constants.java,v 1.8 2006/01/16 17:32:23 rschmidt Exp $
 *
 * Copyright (c) 2002 The P-Grid Team,
 *                    All Rights Reserved.
 *
 * This file is part of the P-Grid package.
 * P-Grid homepage: http://www.p-grid.org/
 *
 * The P-Grid package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file LICENSE.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid;

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
 * This class represents all global constants.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Constants {


	/**
	 * The delay before peers start constructing the network.
	 */
	public static final int BOOTSTRAP_CONSTRUCTION_DELAY = 1000 * 60 * 3;

	/**
	 * The delay before peers replicate their data.
	 */
	public static final int BOOTSTRAP_REPLICATION_DELAY = 1000 * 60 * 1;

	/**
	 * The build version of this application.
	 */
	public static final String BUILD = "$Revision: 1.8 $".replace('$', ' ').trim() + " (" + "$Date: 2006/01/16 17:32:23 $".replace('$', ' ').trim() + ")";

	/**
	 * The debug flag.
	 */
	public static final boolean DEBUG = true;

	/**
	 * The default listening port for incoming P-Grid connections.
	 */
	public static final int DEFAULT_PORT = 1805;

	/**
	 * The delay before a distribution attempt is considered as over.
	 * If this delay is reached, the distribution has most probably failed.
	 */
	public static final int DISTRIBUTION_PROCESSING_TIMEOUT = 1000 * 60 * 1;

	/**
	 * The system line seperator.
	 */
	public static final String LINE_SEPERATOR = System.getProperty("line.separator");

	/**
	 * The Gridella logger.
	 */
	public static final Logger LOGGER = Logger.getLogger("PGrid");

	/**
	 * The default logging directory.
	 */
	public static String LOG_DIR = "log" + System.getProperty("file.separator");

	/**
	 * The delay before a query is considered as over
	 */
	public static final int QUERY_PROCESSING_TIMEOUT = 1000 * 60 * 3;

	/**
	 * The default data directory.
	 */
	public static String DATA_DIR = "data" + System.getProperty("file.separator");

	/**
	 * The default property file.
	 */
	public static String PROPERTY_FILE = System.getProperty("user.home") + System.getProperty("file.separator") + "PGrid.ini";

	/**
	 * The default log file.
	 */
	public static final String LOG_FILE = "PGrid.log";

	/**
	 * The version of the used P-Grid protocol.
	 */
	public static final String PGRID_PROTOCOL_VERSION = "2.0";

	/**
	 * The desired replication factor.
	 */
	public static final int REPLICATION_FACTOR = 2;

	/**
	 * The default resources used by P-Grid.
	 */
	public static final String RESOURCE_BASE = "pgrid.locale.Resources";

	/**
	 * The flag if test code should be executed.
	 */
	public static final boolean TESTS = true;

	/**
	 * Log files are used or not.
	 */
	public static final boolean USE_LOG_FILES = true;

	/**
	 * The version of the used P-Grid protocol.
	 */
	public static final String VERSION = "2.0a";

	/**
	 * The Log Formatter.
	 */
	private static LogFormatter mLogFormatter = new LogFormatter();

	static {
		LOGGER.setUseParentHandlers(false);
		mLogFormatter = new LogFormatter();
		StreamHandler eHandler = new FlushedStreamHandler(System.err, mLogFormatter);
		eHandler.setLevel(Level.WARNING);
		LOGGER.addHandler(eHandler);
	}

	/**
	 * Initializes a child logger.
	 *
	 * @param logger    a child logger.
	 * @param formatter a log formatter for a logging file.
	 * @param logFile   the used loggin file.
	 */
	public static void initChildLogger(Logger logger, LogFormatter formatter, String logFile) {
		// set and use a parent logger
		logger.setParent(LOGGER);
		logger.setUseParentHandlers(true);

		if ((USE_LOG_FILES) && (logFile != null)) {
			try {
				FileHandler fHandler = new FileHandler(LOG_DIR + logFile);
				fHandler.setFormatter(formatter);
				logger.addHandler(fHandler);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not use logging file '" + LOG_DIR + logFile + "'!", e);
			} catch (SecurityException e) {
				LOGGER.log(Level.WARNING, "Could not use logging file '" + LOG_DIR + logFile + "'!", e);
			}
		}
	}

	/**
	 * Initializes the logger.
	 *
	 * @param parent     a parent logger.
	 * @param debugLevel the used debug level (0-3).
	 * @param verbose    if also the System.out should be used for logging.
	 * @param logFile    the used loggin file.
	 */
	public static void initLogger(Logger parent, int debugLevel, boolean verbose, String logFile) {
		// set and use a given parent logger
		if (parent != null) {
			LOGGER.setParent(parent);
			LOGGER.setUseParentHandlers(true);
		}

		// set the logging level
		if (debugLevel >= 0) {
			switch (debugLevel) {
				case (0):
					LOGGER.setLevel(Level.CONFIG);
					break;
				case (1):
					LOGGER.setLevel(Level.FINE);
					break;
				case (2):
					LOGGER.setLevel(Level.FINER);
					break;
				case (3):
					LOGGER.setLevel(Level.FINEST);
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
			LOGGER.addHandler(sHandler);
		}

		// add a handler for the logging file
		if (USE_LOG_FILES) {
			if (logFile == null)
				logFile = LOG_FILE;
			if (LOG_DIR.length() > 0)
				new File(LOG_DIR).mkdirs();
			try {
				FileHandler fHandler = new FileHandler(LOG_DIR + logFile);
				fHandler.setFormatter(mLogFormatter);
				LOGGER.addHandler(fHandler);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not use logging file '" + LOG_DIR + logFile + "'!", e);
			} catch (SecurityException e) {
				LOGGER.log(Level.WARNING, "Could not use logging file '" + LOG_DIR + logFile + "'!", e);
			}
		}
	}

}

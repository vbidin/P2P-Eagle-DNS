/**
 * $Id: PGridServer.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.util.logging.FlushedStreamHandler;
import pgrid.util.logging.LogFormatter;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.logging.Level;
import java.util.logging.StreamHandler;

/**
 * Starts the P-Grid in server mode.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridServer {

	/**
	 * Checks the delivered arguments, and starts all required facilities of the
	 * Gridella program.
	 *
	 * @param argv delivered arguments by the user.
	 */
	public static void main(String[] argv) {
		System.out.println("starting P-Grid server [" + Constants.BUILD + "] ...");
		Constants.LOGGER.setUseParentHandlers(false);
		// System.err handler
		LogFormatter logFormatter = new LogFormatter();
		StreamHandler eHandler = new FlushedStreamHandler(System.err, logFormatter);
		eHandler.setLevel(Level.WARNING);
		Constants.LOGGER.addHandler(eHandler);

		int port = -1;
		String cfgFile = null;
		String routeFile = null;
		String logFile = null;
		boolean verboseMode = false;
		int debugMode = -1;

		// process delivered arguments
		LongOpt[] longOpts = new LongOpt[7];
		longOpts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longOpts[1] = new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'p');
		longOpts[2] = new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c');
		longOpts[3] = new LongOpt("route", LongOpt.REQUIRED_ARGUMENT, null, 'r');
		longOpts[4] = new LongOpt("log", LongOpt.REQUIRED_ARGUMENT, null, 'l');
		longOpts[5] = new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v');
		longOpts[6] = new LongOpt("debug", LongOpt.REQUIRED_ARGUMENT, null, 'd');
		Getopt args = new Getopt("PGridServer", argv, "", longOpts, true);
		args.setOpterr(true);

		int opt;
		while ((opt = args.getopt()) != -1) {
			switch (opt) {
				case 'h': // help
					usage();
					break;
				case 'p': // local port
					String portStr = args.getOptarg();
					if (portStr == null) {
						System.err.println("option '-p|-port' requires an argument");
						usage();
					}
					try {
						port = new Integer(args.getOptarg()).intValue();
					} catch (NumberFormatException e) {
						System.err.println("invalid value for option '-p|-port' (number required)");
						usage();
					}
					break;
				case 'c': // P-Grid property file
					cfgFile = args.getOptarg();
					if ((cfgFile == null) || (cfgFile.length() == 0)) {
						System.err.println("option '-c|-config' requires an argument");
						usage();
					}
					break;
				case 'r': // P-Grid routing file
					routeFile = args.getOptarg();
					if ((routeFile == null) || (routeFile.length() == 0)) {
						System.err.println("option '-r|-route' requires an argument");
						usage();
					}
					break;
				case 'l': // logging file
					logFile = args.getOptarg();
					if ((logFile == null) || (logFile.length() == 0)) {
						System.err.println("option '-l|-log' requires an argument");
						usage();
					}
					break;
				case 'v': // verbose mode
					verboseMode = true;
					break;
				case 'd': // debug mode
					//@todo debug level is only optional, not required
					debugMode = 0;
					if (args.getOptarg() == null) {
						debugMode = 0;
						break;
					}
					try {
						debugMode = new Integer(args.getOptarg()).intValue();
					} catch (NumberFormatException e) {
						System.err.println("invalid value for option '-d|-debug' (number required)");
						usage();
					}
					break;
				case '?': // unknown option
					usage();
					break;
				default:
					usage();
					break;
			}
		}

		// logging facility
		Constants.initLogger(null, debugMode, verboseMode, logFile);

		PGridP2P pgrid = PGridP2P.sharedInstance();
		// create the properties, if neccessery
		java.util.Properties props = new java.util.Properties();
		if ((port != -1) || (cfgFile != null) || (routeFile != null)) {
			if (port != -1)
				props.setProperty(PGridP2P.PROP_LOCAL_PORT, String.valueOf(port));
			if (cfgFile != null)
				props.setProperty(PGridP2P.PROP_PROPERTY_FILE, cfgFile);
		}

		// Start the PGridP2P listener
		props.setProperty(PGridP2P.PROP_START_LISTENER, "true");
		pgrid.init(props);

		// join the network
		pgrid.join();

		// this main makes something and catch Ctrl-C in order to clean its work env before to leave.
		final Thread mainThread = Thread.currentThread();
		// makes a signal holder which will interrupt the main thread if Ctrl-C is typed
		try {
			Signal.handle(new Signal("INT"), new SignalHandler() {
				public void handle(Signal sig) {
					mainThread.interrupt();
				}
			});
		} catch (IllegalArgumentException exc) {
			// do nothing
		}

		System.out.println("... P-Grid server started at " + PGridP2P.sharedInstance().getLocalHost().toHostString() + "!");
		// makes a unused object in the main thread, // sleep on it until wait() raise InterruptedException (raised if ctrl-C is typed)
		Object sync = new Object();
		synchronized (sync) {
			try {
				sync.wait();
			} catch (InterruptedException exc) {
				System.out.println("Shutdown the P-Grid server ...");
				PGridP2P.sharedInstance().shutdown();
				System.exit(0);
			}
		}
	}

	/**
	 * Prints the usage message, when the wrong number of arguments was deliverd,
	 * or the arguments had a wrong format.
	 */
	private static void usage() {
		System.out.println("Usage: PGridServer <options>");
		System.out.println("where possible options include:");
		System.out.println("  -p|-port <port>        Specify listening port");
		System.out.println("  -c|-config <file>      Specify the P-Grid configuration file");
		System.out.println("  -r|-route <file>       Specify the P-Grid routing file");
		System.out.println("  -l|-log                Specify the logging file");
		System.out.println("  -v|-verbose            Enables verbose output");
		System.out.println("  -d|-debug <level>      Enables debug output (level 0-3)");
		System.out.println("  -h|-help               Shows this information");
		System.exit(-1);
	}

}
/**
 * $Id: Properties.java,v 1.4 2006/01/16 23:57:14 rschmidt Exp $
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.zip.Deflater;

/**
 * This class represents the application properties.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Properties {

	/**
	 * Property "RoutingTable", the used Routing Table file.
	 */
	public static final String ROUTING_TABLE = "RoutingTable";

	/**
	 * Property "SharedFiles", the used Shared Files file.
	 */
	public static final String DATA_TABLE = "DataTable";

	/**
	 * Property "TreeIniFile", the used PGridP2P Tree initialization file.
	 */
	public static final String TREE_INI_FILE = "TreeIniFile";

	/**
	 * Property "TreeDbFile", the used PGridP2P Tree database file, if no initialization file is available.
	 */
	public static final String TREE_DB_FILE = "TreeDbFile";

	/**
	 * Property "MaxFidgets", the maximum amount of fidgets.
	 */
	public static final String MAX_FIDGETS = "MaxFidgets";

	/**
	 * Property "MaxRecursion", the maximum amount of recursive exchanges.
	 */
	public static final String MAX_RECURSIONS = "MaxRecursions";

	/**
	 * Property "MaxReferences", the maximum amount of references per level or replicas.
	 */
	public static final String MAX_REFERENCES = "MaxReferences";

	/**
	 * Property "RandomSearches", the amount of performed random searches.
	 */
	public static final String RANDOM_SEARCHES = "RandomSearches";

	/**
	 * Property "RangeQueryAlgorithm", the algorithm used for range query.
	 */
	public static final String RANGE_QUERY_ALGORITHM = "RangeQueryAlgorithm";

	/**
	 * Property "ExchangeRate", the time between two initiated exchanges in msec.
	 */
	public static final String EXCHANGE_RATE = "ExchangeRate";

	/**
	 * Property "ReplicationBalance", if replication balancing is active or not.
	 */
	public static final String REPLICATION_BALANCE = "ReplicationBalance";

	/**
	 * Property "CompressionLevel", the used compression level.
	 */
	public static final String COMPRESSION_LEVEL = "CompressionLevel";

	/**
	 * Property "BootstrapHosts", the used bootstrap hosts.
	 */
	public static final String BOOTSTRAP_HOSTS = "BootstrapHosts";

	/**
	 * Property "ConnectionSpeed", the connection speed of this host.
	 */
	public static final String CONNECTION_SPEED = "ConnectionSpeed";

	/**
	 * Property "BehindFirewall", if this host is behind a firewall.
	 */
	public static final String BEHIND_FIREWALL = "BehindFirewall";

	/**
	 * Property "ResolveIPs", if to lookup the hostname of remote host.
	 */
	public static final String RESOLVE_IP = "ResolveIPs";

	/**
	 * Property "AutoExchange", if to initiate Exchanges automatically after startup.
	 */
	public static final String INIT_EXCHANGES = "InitiateExchanges";

	/**
	 * Property "StartListener", if to start the own listener for incoming connections.
	 */
	public static final String START_LISTENER = "StartListener";

	/**
	 * Property "FileDistribution", for the auto-generated filenames.
	 */
	public static final String PLANETLAB_DISTRIBUTION = "FileDistribution";

	/**
	 * Property "StartReplication", the time peers start to replicate their data.
	 */
	public static final String REPLICATION_START_TIME = "ReplicationStartTime";

	/**
	 * Property "StartConstruction", the time peers start to construct P-Grid.
	 */
	public static final String CONSTRUCTION_START_TIME = "ConstructionStartTime";

	/**
	 * Property "MinimumStorage", The minimum number of data items before triggering a split
	 */
	public static final String EXCHANGE_MIN_STORAGE = "MinimumStorage";

	/**
	 * Property "IdentityMinCorrum", the minimum number of replicas reply to get a
	 * corrum
	 */
	public static final String IDENTITY_MIN_QUORUM = "IdentityMinQuorum";

	/**
	 * Proterty "ConnectionAttempts", the maximum number of times the ID-IP mMapping phase will be
	 * intended to get an answer.
	 */
	public static final String IDENTITY_CONNECTION_ATTEMPS = "ConnectionAttempts";

	/**
	 * Which maintenance politic is used (CoF, CoU)
	 */
	public static final String IDENTITY_MAINTENANCE_POLITIC = "MaintenancePolitic";

	/**
	 * The maximum of failure before repairing the routing table.
	 */
	public static final String IDENTITY_COF_MAX_STALE = "CoDMaxStale";

	/**
	 * The maximum of failure before repairing the routing table.
	 */
	public static final String IDENTITY_CHALLENGE = "Challenge";
	/**
	 * The default property values.
	 */
	private static final String[] DEFAULTS = {"#", "P-Grid properties file",
			"#", "",
			"#", "automatically generated",
			"#", "",
			"", "",
			/* General */ "#", "General",
			ROUTING_TABLE, "RoutingTable.xml",
			DATA_TABLE, "DataTable.xml",
			TREE_INI_FILE, "PGridTree.ini",
			TREE_DB_FILE, "PGridTree.dat",
			"", "",
			/* Setup */ "#", "Setup",
			REPLICATION_START_TIME, "0",
			CONSTRUCTION_START_TIME, "0",
			EXCHANGE_MIN_STORAGE, "0",
			MAX_FIDGETS, "10",
			MAX_RECURSIONS, "3",
			MAX_REFERENCES, "10",
			RANDOM_SEARCHES, "0",
			EXCHANGE_RATE, "60000",
			INIT_EXCHANGES, "true",
			REPLICATION_BALANCE, "false",
			"", "",
			/* Range query */ "#", "Range Query algorithm (" + RangeQuery.MINMAX_ALGORITHM + " or " + RangeQuery.SHOWER_ALGORITHM + ")",
			RANGE_QUERY_ALGORITHM, RangeQuery.SHOWER_ALGORITHM,
			"", "",
			/* Network */ "#", "Network",
			BOOTSTRAP_HOSTS, "www.p-grid.org:1805",
			COMPRESSION_LEVEL, String.valueOf(Deflater.BEST_COMPRESSION),
			CONNECTION_SPEED, "56",
			BEHIND_FIREWALL, "false",
			RESOLVE_IP, "true",
			START_LISTENER, "true",
			"", "",
			/* Maintenance */ "#", "P-Grid Identitiy & Maintenance",
			IDENTITY_MIN_QUORUM, "2",
			IDENTITY_CONNECTION_ATTEMPS, "3",
			"#", "Politic used: CoU, CoF, None",
			IDENTITY_MAINTENANCE_POLITIC, "None",
			"#", "Percent of stale host before correction (integer value)",
			IDENTITY_COF_MAX_STALE, "70",
			IDENTITY_CHALLENGE, "false",
			"", "",
			/* Planetlab
			(Constants"#", "Planetlab distribution (" + test.planetlab.Distributions.DIST_NORMAL + ", " + test.planetlab.Distributions.DIST_PARETO + ", " + test.planetlab.Distributions.DIST_UNIFORM + ")",
			PLANETLAB_DISTRIBUTION, test.planetlab.Distributions.DIST_UNIFORM,*/
			"", "" };

	/**
	 * The property file.
	 */
	private File mFile = null;

	/**
	 * The properties.
	 */
	private Hashtable mProperties = new Hashtable();

	/**
	 * Constructs the application properties.
	 */
	public Properties() {
		// create defaults
		for (int i = 0; i < DEFAULTS.length; i = i + 2)
			if ((!DEFAULTS[i].equals("")) && (!DEFAULTS[i].equals("#")))
				mProperties.put(DEFAULTS[i], DEFAULTS[i + 1]);
	}

	/**
	 * Initializes the properties with default values.
	 */
	synchronized public void init() {
		_init(Constants.PROPERTY_FILE);
	}

	/**
	 * Initializes the properties with the given property file and properties.
	 *
	 * @param file       the property file.
	 * @param properties further initialization properties.
	 */
	synchronized public void init(String file, java.util.Properties properties) {
		_init(file);
		if (properties != null) {
			for (Enumeration en = properties.propertyNames(); en.hasMoreElements();) {
				String key = (String)en.nextElement();
				if (mProperties.containsKey(key)) {
					mProperties.put(key, properties.getProperty(key));
				}
			}
		}
		store();
	}

	/**
	 * This really initializes the properties.
	 *
	 * @param file the property file.
	 */
	synchronized private void _init(String file) {
		mFile = new File(file);
		try {
			if (!mFile.exists()) {
				mFile.createNewFile();
				store();
			} else {
				load();
			}
		} catch (FileNotFoundException e) {
			Constants.LOGGER.log(Level.SEVERE, "Property file '" + file + "' not found or could not be created!", e);
			System.exit(-1);
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, "Could not read/write property file '" + file + "'!", e);
			System.exit(-1);
		}
	}

	/**
	 * Returns the property value as boolean.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public boolean getBoolean(String key) {
		if (getString(key).equals("true"))
			return true;
		else
			return false;
	}

	/**
	 * Returns the default value as boolean.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public boolean getDefaultBoolean(String key) {
		for (int i = 0; i < DEFAULTS.length; i = i + 2) {
			if (DEFAULTS[i].equals(key)) {
				if (DEFAULTS[i + 1].equals("true"))
					return true;
				else
					return false;
			}
		}
		throw new IllegalArgumentException("'" + key + "' not found!");
	}

	/**
	 * Returns the default value as integer.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public int getDefaultInteger(String key) {
		for (int i = 0; i < DEFAULTS.length; i = i + 2) {
			if (DEFAULTS[i].equals(key)) {
				int val;
				val = Integer.parseInt(DEFAULTS[i + 1]);
				return val;
			}
		}
		throw new IllegalArgumentException("'" + key + "' not found!");
	}

	/**
	 * Returns the default value as string.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public String getDefaultString(String key) {
		for (int i = 0; i < DEFAULTS.length; i = i + 2) {
			if (DEFAULTS[i].equals(key)) {
				return DEFAULTS[i + 1];
			}
		}
		throw new IllegalArgumentException("'" + key + "' not found!");
	}

	/**
	 * Returns the property value as integer.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public int getInteger(String key) {
		int val;
		val = Integer.parseInt(getString(key));
		return val;
	}

	/**
	 * Returns the property value as long.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public long getLong(String key) {
		return Long.parseLong(getString(key));
	}

	/**
	 * Returns the property value as string.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public String getString(String key) {
		return (String)mProperties.get(key);
	}

	/**
	 * Sets the property value by the delivered string.
	 *
	 * @param key   the key of the property.
	 * @param value the value of the property.
	 */
	synchronized public void setString(String key, String value) {
		if (mProperties.containsKey(key)) {
			mProperties.put(key, value);
			store();
		}
	}

	/**
	 * Loads the properties from the defined file.
	 */
	synchronized private void load() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(mFile));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.trim().length() == 0)
					continue;
				if (inputLine.trim().startsWith("#"))
					continue;
				String[] tokens = pgrid.util.Tokenizer.tokenize(inputLine, "=");
				mProperties.put(tokens[0], (tokens.length == 2 ? tokens[1] : ""));
			}
			in.close();
		} catch (IOException e) {
			pgrid.Constants.LOGGER.log(Level.WARNING, "Could not read/write property file '" + mFile.getName() + "'!", e);
		}
	}

	/**
	 * Stores the properties to the defined file.
	 */
	synchronized private void store() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(mFile));
			for (int i = 0; i < DEFAULTS.length; i = i + 2)
				if (DEFAULTS[i].equals(""))
					out.write(Constants.LINE_SEPERATOR);
				else if (DEFAULTS[i].equals("#"))
					out.write("# " + DEFAULTS[i + 1] + Constants.LINE_SEPERATOR);
				else
					out.write(DEFAULTS[i] + "=" + getString(DEFAULTS[i]) + Constants.LINE_SEPERATOR);
			out.flush();
			out.close();
		} catch (IOException e) {
			pgrid.Constants.LOGGER.log(Level.WARNING, "Could not read/write property file '" + mFile.getName() + "'!", e);
		}
	}

}

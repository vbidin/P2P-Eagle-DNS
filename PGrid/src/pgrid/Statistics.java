/**
 * $Id: Statistics.java,v 1.5 2005/12/07 20:26:26 john Exp $
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

import pgrid.network.protocol.PGridMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents the statistics of P-Grid.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Statistics {

	/**
	 * The statistic file for the bandwidth.
	 */
	private static final String BANDWIDTH_STAT_FILE = Constants.LOG_DIR + "bandwidth.dat";

	/**
	 * The statistic file for the uncompressed bandwidth.
	 */
	private static final String BANDWIDTH_UNCOMPR_STAT_FILE = Constants.LOG_DIR + "bandwidthUncompr.dat";

	/**
	 * The statistic file for data items.
	 */
	private static final String DATA_ITEMS_STAT_FILE = Constants.LOG_DIR + "dataItems.dat";

	/**
	 * The statistic file for the exchange cases.
	 */
	private static final String EXCHANGE_CASES_STAT_FILE = Constants.LOG_DIR + "exchangeCases.dat";

	/**
	 * The statistic file for the exchanges.
	 */
	private static final String EXCHANGE_STAT_FILE = Constants.LOG_DIR + "exchanges.dat";

	/**
	 * The statistic file for the messages.
	 */
	private static final String MESSAGES_STAT_FILE = Constants.LOG_DIR + "messages.dat";

	/**
	 * The statistic file for queries.
	 */
	private static final String QUERIES_STAT_FILE = Constants.LOG_DIR + "queries.dat";

	/**
	 * The statistic file for the amount of replicas.
	 */
	private static final String SYSTEM_STAT_FILE = Constants.LOG_DIR + "system.dat";

	/**
	 * The statistic file for the updates.
	 */
	private static final String UPDATES_STAT_FILE = Constants.LOG_DIR + "updates.dat";

	/**
	 * The amount of exchange cases.
	 */
	private static final int EXCHANGE_CASES = 10;

	/**
	 * The amount of message types.
	 */
	private static final int MESSAGE_TYPES = PGridMessage.MESSAGE_TYPES;

	/**
	 * The time between two saves.
	 */
	private static final int STAT_SAVE_RATE = 60000; // 60 sec.

	/**
	 * Statistics about "ExchangeCase1", the amount of how often the Exchange case 1 was taken.
	 */
	public static final short EXCHANGE_CASE_1 = 0;

	/**
	 * Statistics about "ExchangeCase2", the amount of how often the Exchange case 2 was taken.
	 */
	public static final short EXCHANGE_CASE_2 = 1;

	/**
	 * Statistics about "ExchangeCase2.1", the amount of how often the Exchange case 2.1 was taken.
	 */
	public static final short EXCHANGE_CASE_2_1 = 2;

	/**
	 * Statistics about "ExchangeCase2.2", the amount of how often the Exchange case 2.2 was taken.
	 */
	public static final short EXCHANGE_CASE_2_2 = 3;

	/**
	 * Statistics about "ExchangeCase3a", the amount of how often the Exchange case 3a was taken.
	 */
	public static final short EXCHANGE_CASE_3a = 4;

	/**
	 * Statistics about "ExchangeCase3a.1", the amount of how often the Exchange case 3a.1 was taken.
	 */
	public static final short EXCHANGE_CASE_3a_1 = 5;

	/**
	 * Statistics about "ExchangeCase3a.2", the amount of how often the Exchange case 3a.2 was taken.
	 */
	public static final short EXCHANGE_CASE_3a_2 = 6;

	/**
	 * Statistics about "ExchangeCase3b", the amount of how often the Exchange case 3b was taken.
	 */
	public static final short EXCHANGE_CASE_3b = 7;

	/**
	 * Statistics about "ExchangeCase3b.1", the amount of how often the Exchange case 3b.1 was taken.
	 */
	public static final short EXCHANGE_CASE_3b_1 = 8;

	/**
	 * Statistics about "ExchangeCase3b.2", the amount of how often the Exchange case 3b.2 was taken.
	 */
	public static final short EXCHANGE_CASE_3b_2 = 9;

	/**
	 * The bandwidth.
	 */
	public int[] Bandwidth = new int[MESSAGE_TYPES];

	/**
	 * The uncompressed bandwidth.
	 */
	public int[] BandwidthUncompr = new int[MESSAGE_TYPES];

	/**
	 * The amount of exchange per case.
	 */
	public int[] ExchangeCases = new int[EXCHANGE_CASES];

	/**
	 * The amount of exchanges.
	 */
	public int Exchanges = 0;

	/**
	 * The amount of failed exchanges.
	 */
	public int ExchangesFailed = 0;

	/**
	 * The amount of ignored exchange invitations.
	 */
	public int ExchangesIgnored = 0;

	/**
	 * The amount of initiated exchanges.
	 */
	public int ExchangesInitiated = 0;

	/**
	 * The amount of exchanges with replicas having equal data sets.
	 */
	public int ExchangesRealReplicas = 0;

	/**
	 * The amount of exchanges with replicas.
	 */
	public int ExchangesReplicas = 0;

	/**
	 * The amount of managed data items.
	 */
	public int DataItemsManaged = 0;

	/**
	 * The amount of managed data items belonging to the local path.
	 */
	public int DataItemsPath = 0;

	/**
	 * The amount of sent data items.
	 */
	public int DataItemsSent = 0;

	/**
	 * Indicates if exchanges are currently initiated by the local peer or not.
	 */
	public int InitExchanges = 0;

	/**
	 * The amount of messages.
	 */
	public int[] Messages = new int[MESSAGE_TYPES];

	/**
	 * The current path length.
	 */
	public int PathLength = 0;

	/**
	 * In which phase the local peer is.
	 */
	public int Phase = 0;

	/**
	 * The queries already seen.
	 */
	public int QueryAlreadySeen = 0;

	/**
	 * The bad query requests.
	 */
	public int QueryBadRequest = 0;

	/**
	 * The processed local queries.
	 */
	public int QueryLocalProcessed = 0;

	/**
	 * The queries not found.
	 */
	public int QueryNotFound = 0;

	/**
	 * The processed local queries.
	 */
	public int QueryRemoteProcessed = 0;

	/**
	 * The query timeouts.
	 */
	public int QueryTimeout = 0;

	/**
	 * The lookup not found.
	 */
	public int LookupNotFound = 0;

	/**
	 * The amount of replicas.
	 */
	public int Replicas = 0;

	/**
	 * The amount of bad requests for updates.
	 */
	public int UpdatesBadRequests = 0;

	/**
	 * The amount of local initiated updates.
	 */
	public int UpdatesLocalProcessed = 0;

	/**
	 * The amount of remote initiated updates.
	 */
	public int UpdatesRemoteProcessed = 0;

	/**
	 * Minimum storage
	 */
	public int MinStorage = 0;

	/**
	 * The shutdown flag.
	 */
	private boolean shutdown = false;

	/**
	 * Constructs the application statistics.
	 */
	public Statistics() {
	}

	private Date getGlobalTime() {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("rdate swisstime.ethz.ch");
		} catch (IOException e) {
			return new Date(System.currentTimeMillis());
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String time = null;
		try {
			time = in.readLine();
			if (time != null) time = time.substring(30).trim();
			else return new Date(System.currentTimeMillis());
		} catch (Exception e) {
			return new Date(System.currentTimeMillis());
		}
		SimpleDateFormat date = new SimpleDateFormat("MMM dd hh:mm:ss yyyy");
		try {
			return date.parse(time);
		} catch (ParseException e) {
			return new Date(System.currentTimeMillis());
		}
	}

	/**
	 * Initializes the properties with the given property file and properties.
	 */
	synchronized public void init() {
		final long globalStartTime = getGlobalTime().getTime();
		final long timeDiff = globalStartTime - System.currentTimeMillis();
		// start thread to store statistic series
		Thread t = new Thread("Statistics") {
			File bandwidthFile = new File(BANDWIDTH_STAT_FILE);
			File bandwidthUncomprFile = new File(BANDWIDTH_UNCOMPR_STAT_FILE);
			File dataItemsFile = new File(DATA_ITEMS_STAT_FILE);
			File exchangeCasesFile = new File(EXCHANGE_CASES_STAT_FILE);
			File exchangesFile = new File(EXCHANGE_STAT_FILE);
			File messagesFile = new File(MESSAGES_STAT_FILE);
			File queriesFile = new File(QUERIES_STAT_FILE);
			File systemFile = new File(SYSTEM_STAT_FILE);
			File updatesFile = new File(UPDATES_STAT_FILE);

			public void run() {
				bandwidthFile.delete();
				bandwidthUncomprFile.delete();
				dataItemsFile.delete();
				exchangeCasesFile.delete();
				exchangesFile.delete();
				messagesFile.delete();
				queriesFile.delete();
				systemFile.delete();
				updatesFile.delete();

				while (!shutdown) {
					try {
						Thread.sleep(STAT_SAVE_RATE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String time = String.valueOf(System.currentTimeMillis() + timeDiff);
					try {
						// bandwidth
						FileWriter writer = new FileWriter(bandwidthFile, true);
						String bdwdth = time + " " + String.valueOf(Phase);
						for (int i = 0; i < Bandwidth.length; i++) {
							bdwdth += " " + String.valueOf(Bandwidth[i] / (STAT_SAVE_RATE / 1000));
							Bandwidth[i] = 0;
						}
						writer.write(bdwdth + "\n");
						writer.close();

						// bandwidth uncompressed
						writer = new FileWriter(bandwidthUncomprFile, true);
						String bdwdthUnc = time + " " + String.valueOf(Phase);
						for (int i = 0; i < BandwidthUncompr.length; i++) {
							bdwdthUnc += " " + String.valueOf(BandwidthUncompr[i] / (STAT_SAVE_RATE / 1000));
							BandwidthUncompr[i] = 0;
						}
						writer.write(bdwdthUnc + "\n");
						writer.close();

						// data items
						writer = new FileWriter(dataItemsFile, true);
						writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(DataItemsManaged) + " " + String.valueOf(DataItemsPath) + " " + String.valueOf(DataItemsSent) + "\n");
						writer.close();
						DataItemsSent = 0;

						// exchange cases
						writer = new FileWriter(exchangeCasesFile, true);
						String exchCases = time + " " + String.valueOf(Phase);
						for (int i = 0; i < ExchangeCases.length; i++) {
							exchCases += " " + String.valueOf(ExchangeCases[i]);
							ExchangeCases[i] = 0;
						}
						writer.write(exchCases + "\n");
						writer.close();

						// exchanges
						writer = new FileWriter(exchangesFile, true);
						writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(Exchanges) + " " + String.valueOf(ExchangesInitiated) + " " + String.valueOf(ExchangesFailed) + " " + String.valueOf(ExchangesIgnored) + " " + String.valueOf(ExchangesReplicas) + " " + String.valueOf(ExchangesRealReplicas) + "\n");
						writer.close();
						Exchanges = 0;
						ExchangesInitiated = 0;
						ExchangesFailed = 0;
						ExchangesIgnored = 0;
						ExchangesReplicas = 0;
						ExchangesRealReplicas = 0;

						// messages
						writer = new FileWriter(messagesFile, true);
						String msg = time + " " + String.valueOf(Phase);
						for (int i = 0; i < Messages.length; i++) {
							msg += " " + String.valueOf(Messages[i]);
							Messages[i] = 0;
						}
						writer.write(msg + "\n");
						writer.close();

						// queries
						writer = new FileWriter(queriesFile, true);
						writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(QueryLocalProcessed) + " " + String.valueOf(QueryRemoteProcessed) + " " + String.valueOf(QueryAlreadySeen) + " " + String.valueOf(QueryBadRequest) + " " + String.valueOf(QueryNotFound) + " " + String.valueOf(QueryTimeout) + "\n");
						writer.close();
						QueryLocalProcessed = 0;
						QueryRemoteProcessed = 0;
						QueryAlreadySeen = 0;
						QueryBadRequest = 0;
						QueryNotFound = 0;
						QueryTimeout = 0;

						// system values
						writer = new FileWriter(systemFile, true);
						writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(InitExchanges) + " " + String.valueOf(PathLength) + " " + String.valueOf(Replicas) + " " + String.valueOf(Runtime.getRuntime().totalMemory()) + " " + String.valueOf(MinStorage) + "\n");
						// writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(InitExchanges) + " " + String.valueOf(PathLength) + " " + String.valueOf(Replicas) + " " + String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) + "\n");
						writer.close();

						// updates
						writer = new FileWriter(updatesFile, true);
						writer.write(time + " " + String.valueOf(Phase) + " " + String.valueOf(UpdatesLocalProcessed) + " " + String.valueOf(UpdatesRemoteProcessed) + " " + String.valueOf(UpdatesBadRequests) + "\n");
						writer.close();
						UpdatesLocalProcessed = 0;
						UpdatesRemoteProcessed = 0;
						UpdatesBadRequests = 0;
					} catch (IOException e) {
						e.printStackTrace();  //To change body of catch statement use Options | File Templates.
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Shutdowns the Statistics facility.
	 */
	synchronized public void shutdown() {
		shutdown = true;
	}

}
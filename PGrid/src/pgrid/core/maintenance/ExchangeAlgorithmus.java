/**
 * $Id: ExchangeAlgorithmus.java,v 1.4 2006/01/16 17:07:02 john Exp $
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

package pgrid.core.maintenance;

import pgrid.Constants;
import pgrid.Exchange;
import pgrid.Properties;
import pgrid.Statistics;
import pgrid.PGridHost;
import pgrid.util.Utils;
import pgrid.core.*;
import pgrid.core.storage.StorageManager;
import pgrid.core.storage.DBDataTable;
import pgrid.core.storage.DBView;
import pgrid.core.storage.Signature;
import pgrid.interfaces.basic.PGridP2P;

/**
 * This class represents the PGrid exchange algorithmus.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ExchangeAlgorithmus extends Exchanger {

	/**
	 * The amount of useless exchanges allowed before stopping initiating new exchanges.
	 */
	protected static final int MAX_USELESS_EXCH_COUNT = 2;

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The Exchange counter.
	 */
	private int mExchangeCount = 1;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Counts the amount of useless exchanges (no split, no new data items, etc.)
	 */
	private short mUselessExchCount = 0;

	/**
	 * Creates a new class to process PGrid exchanges.
	 *
	 */
	ExchangeAlgorithmus(MaintenanceManager manager) {
		super();
		mMaintencanceMgr = manager;
		mStorageManager = mPGridP2P.getStorageManager();
	}

	/**
	 * Executes the exchange.
	 *
	 * @param exchange   the exchange to process.
	 * @param invited    indicates the initiator of this exchange.
	 * @param recursion  the recursion.
	 * @param currentLen the current len position.
	 */
	void process(PGridHost host, Exchange exchange, boolean invited, int recursion, int currentLen, int minStorage) {
		LOGGER.fine("start " + mExchangeCount + ". Exchange " + exchange.getGUID() + " (Invite=" + String.valueOf(invited) + ", Recursion=" + exchange.getRecursion() + ") with Host " + exchange.getHost().toString());
		LOGGER.fine("Local Host (Path: '" + mPGridP2P.getLocalPath() + "', Data Items: " + mStorageManager.getDataTable().count() + ") - Remote Host (Path: '" + exchange.getHost().getPath() + "', Data Items: " + (exchange.getDataTable() != null ? exchange.getDataTable().count() : 0) + ")");
		mPGridP2P.getStatistics().Exchanges++;

		// save some values to compare output of exchange
		String initPath = mPGridP2P.getLocalPath();
		Signature initDataSign = mStorageManager.getDataTable().getSignature();

		XMLRoutingTable routingTable = exchange.getRoutingTable();
		String path = exchange.getHost().getPath();
		host.setPath(path);
		routingTable.setLocalHost(host);
		DBDataTable dataTable = exchange.getDataTable();
		if (dataTable == null)
			dataTable = new DBDataTable(exchange.getHost());

		// construct common path and its length
		String commonPath = Utils.commonPrefix(mPGridP2P.getLocalPath(), path);
		int len = commonPath.length();

		// update statistics
		mPGridP2P.getBalancer().updateStatistics(len, currentLen, mPGridP2P.getLocalPath().length(), path.length());

		// compute path lengths, union table, and table selections
		int lLen = mPGridP2P.getLocalPath().length() - len;
		int rLen = path.length() - len;

		// P-Grid maintainance activities: Refreshes the routing tables and fidget lists at each peer
		mPGridP2P.getRoutingTable().refresh(routingTable, len, mPGridP2P.getLocalPath().length(), path.length(),
				mPGridP2P.propertyInteger(Properties.MAX_FIDGETS), mPGridP2P.propertyInteger(Properties.MAX_REFERENCES));

		if ((lLen > 0) && (rLen > 0)) {
			// Peer's paths are incomplete
			LOGGER.finer("case 1: Peer's paths are incomplete");
			if (Constants.TESTS)
				mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_1]++;
			mPGridP2P.getRoutingTable().addLevel(len, host);

			DBView lDataOfRemotePath = DBView.selection(mStorageManager.getDataTable(), path);
			DBView rDataOfLocalPath = DBView.selection(dataTable, mPGridP2P.getLocalPath());

			//routingTable.addLevel(len, mPGridP2P.getLocalHost()); // INFO: save CPU
			mStorageManager.setDataTable(DBView.union(DBView.setDifference(mStorageManager.getDataTable(), lDataOfRemotePath),rDataOfLocalPath));

			if (invited) {
				// try to initiate a recursive exchange if local peer initiated this exchange
				//Vector hostList = (Vector)routingTable.getLevelVector(len);
				//if ((hostList.size() == 0) || ((hostList.size() == 1) && (((XMLPGridHost)hostList.get(0)).equals(mPGridP2P.getLocalHost()))))
				if (recursion < mPGridP2P.propertyInteger(Properties.MAX_RECURSIONS)) {
					LOGGER.finer("initialize a random exchange with one peer of the remote routing table");
					mMaintencanceMgr.randomExchange(routingTable.getLevelVector(len), recursion + 1, len + 1);
				}
			}
		} else if ((lLen == 0) && (rLen == 0)) {
			// Peers have the same paths, path extension possible
			LOGGER.finer("case 2: Peers have the same paths, path extension possible");
			if (Constants.TESTS)
				mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_2]++;

			DBView uData = DBView.union(mStorageManager.getDataTable(), dataTable);
			DBView lData = DBView.selection(uData, commonPath + ExchangeAlgUtils.pathExtension(invited, exchange.getRandomNumber()));
			DBView rData = DBView.selection(uData, commonPath + ExchangeAlgUtils.pathExtension(!invited, exchange.getRandomNumber()));
			float lDataCount = lData.count();
			float rDataCount = rData.count();

			float lr1 = DBView.selection(mStorageManager.getDataTable(), mPGridP2P.getLocalPath()).count();
			float lr2 = DBView.selection(dataTable, path).count();
			float lrt = DBView.selection(uData, mPGridP2P.getLocalPath()).count();
			float est = ExchangeAlgUtils.estimateN(lr1 + lr2 - lrt, lr1, lr2) / lrt * Constants.REPLICATION_FACTOR;
			mReplicaEstimate = est;

			LOGGER.finer("check: lr1: " + lr1 + ", lr2: " + lr2 + ", lrt: " + lrt + ", est: " + est);
			LOGGER.finer("case 2.1 or case 2.2: ld1: " + lDataCount + ", ld2: " + rDataCount + ", est: " + est + " randomNr: " + exchange.getRandomNumber() +
					", M1: " + ExchangeAlgUtils.computeM1(lDataCount, rDataCount, mReplicaEstimate));
			if ((lrt >= 2 * minStorage) && (exchange.getRandomNumber() < ExchangeAlgUtils.computeM1(lDataCount, rDataCount, mReplicaEstimate))) {
				// case 2.1: Data is exchanged, new level of routing table is added and statistics is reset.
				LOGGER.finer("case 2.1: Data is exchanged, new level of routing table is added and statistics is reset.");
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_2_1]++;

				mPGridP2P.setLocalPath(commonPath + ExchangeAlgUtils.pathExtension(invited, exchange.getRandomNumber()));
				path = commonPath + ExchangeAlgUtils.pathExtension(!invited, exchange.getRandomNumber());
				if (lDataCount < rDataCount) {

					mStorageManager.setDataTable(DBView.setDifference(uData, rData));

				} else {
					mStorageManager.setDataTable(lData);
				}
				mPGridP2P.getRoutingTable().addLevel(len, host);
				routingTable.getLocalHost().setPath(path);
				routingTable.addLevel(len, mPGridP2P.getLocalHost());
				mPGridP2P.getRoutingTable().clearReplicas();
				mPGridP2P.getBalancer().resetStatistics();
			} else {
				// case 2.2: Replicate data.
				LOGGER.finer("case 2.2: Replicate data if not too many data items");

				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_2_2]++;
				mPGridP2P.getRoutingTable().addReplica(host);
				mPGridP2P.getRoutingTable().addReplicas(routingTable.getReplicaVector());
				if ((lDataCount + rDataCount) <= (2 * minStorage)) {

					DBView sd1 = DBView.setDifference(DBView.selection(mStorageManager.getDataTable(), path), dataTable);
					DBView sd2 = DBView.setDifference(DBView.selection(dataTable, mPGridP2P.getLocalPath()), mStorageManager.getDataTable());
					if ((sd1.count() > 0) || (sd2.count() > 0)) {
						mStorageManager.setDataTable(DBView.union(mStorageManager.getDataTable(), sd2));
					}
				}
			}
		} else if ((lLen == 0) && (rLen > 0)) {
			// case 3a: Paths are in prefix relationship, exchange or retraction is possible (remote is longer)");
			LOGGER.finer("case 3a: Paths are in prefix relationship, exchange or retraction is possible (remote is longer)");
			if (Constants.TESTS)
				mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3a]++;
			String lPath = mPGridP2P.getLocalPath().concat((path.charAt(len) == '0' ? "1" : "0"));
			String lPath2 = mPGridP2P.getLocalPath().concat((path.charAt(len) == '0' ? "0" : "1"));

			DBView uData = DBView.union(mStorageManager.getDataTable(), dataTable);
			DBView lData = DBView.selection(mStorageManager.getDataTable(), lPath);
			DBView lData2 = DBView.selection(mStorageManager.getDataTable(), lPath2);


			float lDataCount = lData.count();
			float lDataCount2 = lData2.count();
			LOGGER.finer("case 3a.1 or case 3a.2? - ld1: " + lDataCount + ", ld2: " + lDataCount2 + ", est: " + exchange.getReplicaEstimate() +
					", randomNr: " + exchange.getRandomNumber() + ", M2: " + ExchangeAlgUtils.computeM2(lDataCount, lDataCount2,
					exchange.getReplicaEstimate() * Math.pow(2, rLen - 1)));
			if ((lDataCount > lDataCount2) ||
					((lDataCount <= lDataCount2) &&
							(exchange.getRandomNumber() <= ExchangeAlgUtils.computeM2(lDataCount, lDataCount2, exchange.getReplicaEstimate() * Math.pow(2, rLen - 1))))) {
				// case 3a.1: case where longer path in overpopulated region, then adopt opposite path only with reduced ...
				LOGGER.finer("case 3a.1: Longer path in overpopulated region");
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3a_1]++;
				mPGridP2P.setLocalPath(lPath);
				mPGridP2P.getRoutingTable().addLevel(len, host);
				for (int i = len + 1; i < routingTable.getLevelCount(); i++) {
					mPGridP2P.getRoutingTable().addLevel(len, routingTable.getLevelVector(i));
				}
				mStorageManager.setDataTable(DBView.selection(uData, lPath));
				mPGridP2P.getBalancer().resetStatistics();
				mPGridP2P.getRoutingTable().clearReplicas();
				mReplicaEstimate = exchange.getReplicaEstimate() * Math.pow(2, rLen - 1);
			} else {
				// case 3a.2: adopt longer remote path
				LOGGER.finer("case 3a.2: adopt longer remote path.");
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3a_2]++;
				mPGridP2P.setLocalPath(lPath2);
				mPGridP2P.getRoutingTable().setLevel(len, routingTable.getLevel(len));

				// register data items of other path
				mStorageManager.insertDataItems(mStorageManager.getDataTable().getDataItems(lPath));
				//@todo remove sent data items from the local data table

				if (rLen == 1) {
					mPGridP2P.getRoutingTable().addReplica(host);
					mPGridP2P.getRoutingTable().addReplicas(routingTable.getReplicaVector());
					mStorageManager.setDataTable(DBView.union(mStorageManager.getDataTable(), DBView.selection(dataTable, mPGridP2P.getLocalPath())));
				}
				mReplicaEstimate = exchange.getReplicaEstimate() * Math.pow(2, rLen - 1);
			}
		} else if ((rLen == 0) && (lLen > 0)) {
			// case 3b: case where longer path in overpopulated region, then adopt opposite path only with reduced ...
			LOGGER.finer("case 3b: Paths are in prefix relationship, exchange or retraction is possible (local is longer)");
			if (Constants.TESTS)
				mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3b]++;
			String rPath = path.concat((mPGridP2P.getLocalPath().charAt(len) == '0' ? "1" : "0"));
			String rPath2 = path.concat((mPGridP2P.getLocalPath().charAt(len) == '0' ? "0" : "1"));

			DBView uData = DBView.union(mStorageManager.getDataTable(), dataTable);
			DBView rData = DBView.selection(dataTable, rPath);
			DBView rData2 = DBView.selection(dataTable, rPath2);

			float rDataCount = rData.count();
			float rDataCount2 = rData2.count();
			LOGGER.finer("case 3b.1 or case 3b.2? - ld1: " + rDataCount + ", ld2: " + rDataCount2 + ", est: " + mReplicaEstimate +
					", randomNr: " + exchange.getRandomNumber() + ", M2: " + ExchangeAlgUtils.computeM2(rDataCount, rDataCount2,
					mReplicaEstimate * Math.pow(2, lLen - 1)));
			if ((rDataCount > rDataCount2) ||
					((rDataCount <= rDataCount2) &&
							(exchange.getRandomNumber() <= ExchangeAlgUtils.computeM2(rDataCount, rDataCount2, mReplicaEstimate * Math.pow(2, lLen - 1))))) {
				// case 3b.1: Path extension to complimentary bit at level len+1 if too much data");
				LOGGER.finer("case 3b.1: Path extension to complimentary bit at level len+1 if too much data.");
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3b_1]++;
				path = rPath;
				mPGridP2P.getRoutingTable().addLevel(len, host);
				mStorageManager.setDataTable(DBView.setDifference(uData, DBView.selection(uData, rPath)));

				mPGridP2P.getBalancer().resetStatistics();
			} else {
				// case 3b.2: adopt longer remote path
				LOGGER.finer("case 3b.2: adopt longer remote path.");
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangeCases[Statistics.EXCHANGE_CASE_3b_2]++;
				path = rPath2;

				if (lLen == 1) {
					mPGridP2P.getRoutingTable().addReplica(host);
					mPGridP2P.getRoutingTable().addReplicas(routingTable.getReplicaVector());
				}
				mStorageManager.setDataTable(DBView.union(mStorageManager.getDataTable(), DBView.selection(dataTable, mPGridP2P.getLocalPath())));
			}
		}
		mExchangeCount++;
		host.setPath(path);
		LOGGER.fine("Local Host (Path: '" + mPGridP2P.getLocalPath() + "', Data Items: " + mStorageManager.getDataTable().count() + ") - " +
				"Remote Host (Path: '" + path + "', Data Items: " + dataTable.count() + ")");
		routingTable.getLocalHost().setPath(path);
		routingTable.setLevels(path.length() - 1);

		// stop initiating exchanges if no usefull exchanges were performed for a while
		if ((mPGridP2P.getLocalPath().equals(initPath)) && (mStorageManager.getDataTable().getSignature().equals(initDataSign))) {
			if ((!invited) && (recursion == 0))
				mUselessExchCount++;
		} else {
			mUselessExchCount = 0;
		}
		if (mUselessExchCount < MAX_USELESS_EXCH_COUNT) {
			if (mPGridP2P.propertyBoolean(Properties.INIT_EXCHANGES) == false) {
				LOGGER.fine("Restart initiating exchanges.");
				// TODO fix me
				mPGridP2P.setInitExchanges(true);
			}
		} else {
			if (mPGridP2P.propertyBoolean(Properties.INIT_EXCHANGES) == true) {
				LOGGER.fine("Stop initiating exchanges.");
				// TODO fix me
				mPGridP2P.setInitExchanges(false);
			}
		}

		// run the Replication Balancer if activated
		if (mPGridP2P.propertyBoolean(Properties.REPLICATION_BALANCE))
			mPGridP2P.replicationBalance();

		// save the routing table and the data table
		mPGridP2P.getRoutingTable().save();
		if (Constants.TESTS) {
			mPGridP2P.getStatistics().PathLength = mPGridP2P.getRoutingTable().getLevelCount();
			mPGridP2P.getStatistics().Replicas = mPGridP2P.getRoutingTable().getReplicaVector().size();
			mPGridP2P.getStorageManager().writeDataTable();
			mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
			mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
		}

	}

}
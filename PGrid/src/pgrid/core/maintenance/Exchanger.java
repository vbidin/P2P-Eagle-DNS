/**
 * $Id: Exchanger.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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
import pgrid.core.storage.DBDataTable;
import pgrid.core.storage.DBView;
import pgrid.core.storage.Signature;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.ExchangeInvitationMessage;
import pgrid.network.protocol.ExchangeMessage;
import pgrid.network.protocol.ExchangeReplyMessage;
import pgrid.util.logging.LogFormatter;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class processes the PGridP2P Exchange between two hosts.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class Exchanger extends pgrid.util.WorkerThread {

	/**
	 * The time to wait for no further exchange.
	 */
	private static final int ATTEMPT_TIMEOUT = 30000; // 30 sec.

	/**
	 * The PGrid.Exchanger logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger("PGrid.Exchanger");

	/**
	 * Min storage before spliting
	 */
	protected int mMinStorageEstimate = 0;

	/**
	 * The estimation of replicas.
	 */
	protected double mReplicaEstimate = 0;

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The exchange algorithmus processing the exchanges.
	 */
	private ExchangeAlgorithmus mExchangeAlg = null;

	/**
	 * The exchange requests.
	 */
	private final Vector mExchangeRequests = new Vector();

	/**
	 * The exchange invitation requests.
	 */
	private final Vector mExchangeInvitations = new Vector();

	/**
	 * The exchange replies.
	 */
	private final Vector mExchangeReplies = new Vector();

	/**
	 * If the exchanger thread is idle or not.
	 */
	private boolean mIdleFlag = true;

	/**
	 * The Maintencance Manager.
	 */
	protected MaintenanceManager mMaintencanceMgr = null;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * Shutdown variable
	 */
	private boolean mShutdown = false;

	/**
	 * Reference to the worker thread
	 */
	private Thread mThread;

	/**
	 * Lock
	 */
	private final Object mVectorLock = new Object();

	/**
	 * The secure random number generator.
	 */
	private SecureRandom mRandomizer = new SecureRandom();

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(
				LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null);
	}

	/**
	 * Creates a new router.
	 */
	protected Exchanger() {
	}

	/**
	 * Creates a new router.
	 *
	 * @param p2p the P2P facility.
	 * @param maintenanceMgr the Maintenance Manager.
	 */
	Exchanger(PGridP2P p2p, MaintenanceManager maintenanceMgr) {
		super();
		mPGridP2P = p2p;
		mStorageManager = mPGridP2P.getStorageManager();
		mMaintencanceMgr = maintenanceMgr;
		mExchangeAlg = new ExchangeAlgorithmus(maintenanceMgr);
	}

	private Collection compileDataTable(String path, Signature sign) {
		DBDataTable localDataTable = mStorageManager.getDataTable();
		Signature localSign = localDataTable.getSignature();
		Collection col = null;
		// is the other host a replica?
		if (path.equals(mPGridP2P.getLocalPath())) {
			if (Constants.TESTS)
				mPGridP2P.getStatistics().ExchangesReplicas++;
			// are the signatures not equal => send also data items
			if (!localSign.equals(sign)) {
				col = DBView.selection(localDataTable, path).getDataItems();
			} else {
				if (Constants.TESTS)
					mPGridP2P.getStatistics().ExchangesRealReplicas++;
				col = new Vector();
			}
		} else {
			col = DBView.selection(localDataTable, path).getDataItems();
		}
		if (Constants.TESTS)
			mPGridP2P.getStatistics().DataItemsSent += col.size();
		return col;
	}

	protected void handleError(Throwable t) {
		if (t instanceof InterruptedException) {
			LOGGER.finer("Exchanger interupted.");
		} else {
			LOGGER.log(Level.WARNING, "Error in Exchanger thread", t);
		}
	}

	private void handleExchangeInvitation(ExchangeInvitationRequest request) {
		// check if the invitation is recent enough
		if ((request.getStartTime() + ATTEMPT_TIMEOUT) < System.currentTimeMillis()) {
			Constants.LOGGER.finer("Exchange invitation from " + request.getExchangeInvitation().getHeader().getHost().toString() + " ignored (invite timeout)!");
			mPGridP2P.getStatistics().ExchangesIgnored++;
			return;
		}
		// check if the invitation is allowed
		if (!request.getExchangeInvitation().getHeader().getHost().isExchangeTime()) {
			Constants.LOGGER.finer("Exchange invitation from " + request.getExchangeInvitation().getHeader().getHost().toString() + " ignored (too early)!");
			mPGridP2P.getStatistics().ExchangesIgnored++;
			return;
		}
		// invitation accepted => set the next possible exchange time
		request.getExchangeInvitation().getHeader().getHost().invited();

		// create the data table according to the path of the remote host
		Collection dataItems = compileDataTable(request.getExchangeInvitation().getPath(), request.getExchangeInvitation().getSignature());

		// create the message sent to the remote host
		mPGridP2P.getLocalHost().refreshPathTimestamp();
		ExchangeMessage msg = new ExchangeMessage(request.getExchangeInvitation().getGUID(), mPGridP2P.getLocalHost(),
				request.getExchangeInvitation().getRecursion(),	request.getExchangeInvitation().getCurrentLen(), mMinStorageEstimate,
				mReplicaEstimate,	mPGridP2P.getRoutingTable(), dataItems, mPGridP2P.getStorageManager().getDataSignature());

		msg.setRandomNumber(mRandomizer.nextDouble());
		mMsgMgr.sendMessage(request.getExchangeInvitation().getHeader().getHost(), msg);

		// block until the return message was received or timeout is raised
		long timeout = System.currentTimeMillis() + ATTEMPT_TIMEOUT;
		while (true) {
			long sleepTime = timeout - System.currentTimeMillis();
			if (sleepTime <= 0)
				break;

			// new exchange request
			try {
				synchronized (mVectorLock) {
					boolean found = false;
					ExchangeReplyMessage exchReply = null;
					for (Iterator it = mExchangeReplies.iterator(); it.hasNext();) {
						exchReply = (ExchangeReplyMessage) it.next();
						if (exchReply.getGUID().equals(request.getExchangeInvitation().getGUID())) {
							found = true;
							break;
						}
					}
					if (found) {
						mExchangeReplies.remove(exchReply);

						// duplicate local data table if the signatures are equal
						if (mStorageManager.getDataTable().getSignature().equals(exchReply.getDataTable().getSignature())) {
							mStorageManager.getDataTable().duplicate(exchReply.getDataTable());
						}
						// set the random number
						exchReply.setRandomNumber(msg.getRandomNumber());
						// execute the exchange algorithmus

						// recompute our min storage
						int oldStorage = mMinStorageEstimate;
						mMinStorageEstimate = (mMinStorageEstimate+exchReply.getMinStorage())/2;
						mPGridP2P.setProperty(Properties.EXCHANGE_MIN_STORAGE, ""+mMinStorageEstimate);

						mExchangeAlg.process(exchReply.getHost(), exchReply, true, exchReply.getRecursion(), exchReply.getLenCurrent(), oldStorage);
						exchReply.getDataTable().delete();
						mStorageManager.compactDB();

						exchReply.getHost().exchanged();


						if(Constants.TESTS){
							mPGridP2P.getStatistics().MinStorage = mMinStorageEstimate;
						}

						return;
					} else {

					mVectorLock.wait(sleepTime);

					}
				}
			} catch (InterruptedException e) {
				LOGGER.fine("Exchange with host " + request.getExchangeInvitation().getHeader().getHost() + " interupted.");
			}

		}
		LOGGER.fine("Exchange with host " + request.getExchangeInvitation().getHeader().getHost() + " failed (timeout).");
		if (Constants.TESTS)
			mPGridP2P.getStatistics().ExchangesFailed++;
	}

	private void handleExchangeRequest(ExchangeRequest request) {
		// create the data table according to the path of the remote host
		Collection dataItems = compileDataTable(request.getExchange().getHost().getPath(),
				request.getExchange().getDataTable().getSignature());

		// create the message sent to the remote host
		mPGridP2P.getLocalHost().refreshPathTimestamp();
		ExchangeReplyMessage msg = new ExchangeReplyMessage(request.getExchange().getGUID(), mPGridP2P.getLocalHost(),
				request.getExchange().getRecursion(), request.getExchange().getLenCurrent(), mMinStorageEstimate, mReplicaEstimate,
				mPGridP2P.getRoutingTable(), dataItems, mPGridP2P.getStorageManager().getDataSignature());

		mMsgMgr.sendMessage(request.getExchange().getHost(), msg);

		// duplicate local data table if the signatures are equal
		if (mStorageManager.getDataTable().getSignature().equals(request.getExchange().getDataTable().getSignature())) {
			mStorageManager.getDataTable().duplicate(request.getExchange().getDataTable());
		}

		// execute the exchange algorithmus
		// recompute our min storage
		mMinStorageEstimate = (mMinStorageEstimate+request.getExchange().getMinStorage())/2;
		mPGridP2P.setProperty(Properties.EXCHANGE_MIN_STORAGE, ""+mMinStorageEstimate);
		mExchangeAlg.process(request.getExchange().getHost(), request.getExchange(), false, request.getExchange().getRecursion(),
													request.getExchange().getLenCurrent(), mMinStorageEstimate);
		request.getExchange().getDataTable().delete();
		mStorageManager.compactDB();

		request.getExchange().getHost().exchanged();

		if(Constants.TESTS){
			mPGridP2P.getStatistics().MinStorage = mMinStorageEstimate;
		}
	}

	/**
	 * Processes a new exchange request.
	 *
	 * @param exchange the exchange request.
	 */
	public void newExchangeRequest(Exchange exchange) {
		synchronized(mVectorLock){
			mExchangeRequests.add(new ExchangeRequest(exchange));
		}
		broadcast();
	}

	/**
	 * Processes a new exchange invitation request.
	 *
	 * @param exchangeInvitation the exchange invitation request.
	 */
	public void newExchangeInvitation(ExchangeInvitationMessage exchangeInvitation) {
		synchronized(mVectorLock){
			mExchangeInvitations.add(new ExchangeInvitationRequest(exchangeInvitation));
		}
		broadcast();
	}

	/**
	 * A new exchange response was received.
	 *
	 * @param message the response message.
	 */
	public void newExchangeReply(ExchangeReplyMessage message) {
		synchronized (mVectorLock) {
			mExchangeReplies.add(message);
			mVectorLock.notifyAll();
		}
	}

	protected void prepareWorker() throws Exception {
		mThread = Thread.currentThread();
		LOGGER.config("Exchanger thread prepared.");
	}

	protected void releaseWorker() throws Exception {
		LOGGER.config("Exchanger thread released.");
	}

	protected void work() throws Exception {
		// iterate exchange requests first
		ExchangeRequest[] request = null;
		synchronized (mVectorLock) {
			if (mExchangeRequests.size() > 0) {
				if (mPGridP2P.getMaintenanceManager().isExchangeTime()) {
					request = (ExchangeRequest[])mExchangeRequests.toArray(new ExchangeRequest[mExchangeRequests.size()]);
				} else {
					// currently no exchange allowed => ignore them
					Constants.LOGGER.finer("Exchanges ignored (no exchange time)!");
					if (Constants.TESTS)
						mPGridP2P.getStatistics().ExchangesIgnored++;
				}
			}
			mExchangeRequests.clear();
		}
		// process exchanges sequentially
		if (request != null) {
			mIdleFlag = false;
			for (int i = 0; i < request.length; i++) {
				Constants.LOGGER.finer("Exchange request received from " + request[i].getExchange().getHost().toString());
				handleExchangeRequest(request[i]);
			}
		}
		// iterate exchange invitation requests
		ExchangeInvitationRequest[] invitation = null;
		synchronized (mVectorLock) {
			if (mExchangeInvitations.size() > 0) {
				if (mPGridP2P.getMaintenanceManager().isExchangeTime()) {
					invitation = (ExchangeInvitationRequest[])mExchangeInvitations.toArray(new ExchangeInvitationRequest[mExchangeInvitations.size()]);
				} else {
					// currently no exchange allowed => ignore them
					Constants.LOGGER.finer("Exchanges ignored (no exchange time)!");
					if (Constants.TESTS)
						mPGridP2P.getStatistics().ExchangesIgnored++;
				}
			}
			mExchangeInvitations.clear();
		}
		// process exchanges sequentially
		if (invitation != null) {
			mIdleFlag = false;
			for (int i = 0; i < invitation.length; i++) {
				Constants.LOGGER.finer("Exchange invitation request received from " + invitation[i].getExchangeInvitation().getHeader().getHost().toString());
				handleExchangeInvitation(invitation[i]);
			}
		}

		mIdleFlag = true;
	}

	protected boolean isCondition() {
		boolean isCondition = false;
		synchronized(mVectorLock) {
			isCondition = (!mShutdown && (!mExchangeRequests.isEmpty() || !mExchangeInvitations.isEmpty()));
		}

		return isCondition;
	}

	/**
	 * Checks if the exchanger thread is currently idle.
	 * @return <tt>true</tt> if idle, <tt>false</tt> otherwise.
	 */
	boolean isIdle() {
		return mIdleFlag;
	}

	/**
	 * Set the minimum storage before a split occures
	 * @param min
	 */
	public void setMinStorage(int min) {
		mMinStorageEstimate = min;
		mPGridP2P.setProperty(Properties.EXCHANGE_MIN_STORAGE, ""+mMinStorageEstimate);
		if(Constants.TESTS){
			mPGridP2P.getStatistics().MinStorage = mMinStorageEstimate;
		}
	}

	/**
	 * Returns the minimum storage before a split occures
	 */
	public int getMinStorage() {
		return mMinStorageEstimate;
	}

	/**
	 * Shutdown
	 */
	public void shutdown() {
		mShutdown=true;
		mThread.interrupt();
	}

}
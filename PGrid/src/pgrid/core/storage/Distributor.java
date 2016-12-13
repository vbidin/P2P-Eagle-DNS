/**
 * $Id: Distributor.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.storage;

import p2p.basic.Key;
import pgrid.Constants;
import pgrid.DataItem;
import pgrid.util.Utils;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.PGridMessage;
import pgrid.network.protocol.DataModifierMessage;
import pgrid.network.router.Router;
import pgrid.network.router.RoutingRequestFactory;
import pgrid.util.logging.LogFormatter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class distributes inserted, updated, and deleted data items in the network.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class Distributor extends pgrid.util.WorkerThread {

	/**
	 * The PGrid.Distributor logger.
	 */
	static final Logger LOGGER = Logger.getLogger("PGrid.Distributor");

	/**
	 * The insert handler.
	 */
	private DataModifier mDataModifier = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The remote requests.
	 */
	private final Vector mRemoteRequests = new Vector();

	/**
	 * The requests.
	 */
	private final Vector mRequests = new Vector();

	/**
	 * The PGrid router.
	 */
	private Router mRouter = null;

	/**
	 * The working thread
	 */
	private Thread mThread;

	/**
	 * Lock
	 */
	private final Object mVectorLock = new Object();

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null);
	}

	/**
	 * Constructs the Distributor.
	 *
	 */
	Distributor() {
		super();
		mPGridP2P = PGridP2P.sharedInstance();
		mRouter = mPGridP2P.getRouter();
		mDataModifier = new DataModifier(this);
	}

	/**
	 * Returns the common key for data items of a given level.
	 * @param level the level.
	 * @return the common key.
	 */
	String commonKeyForLevel(int level) {
		String localPath = mPGridP2P.getLocalPath();
		String key;
		if (level == 0)
			key = "";
		else
			key = localPath.substring(0, level);
		if (localPath.charAt(level) == '0')
			key += "1";
		else if (localPath.charAt(level) == '1')
			key += "0";
		return key;
	}

	protected void handleError(Throwable t) {
		if (t instanceof InterruptedException) {
			Constants.LOGGER.finer("Distributor interupted.");
		} else {
			Constants.LOGGER.log(Level.WARNING, "Error in Distributor thread", t);
		}
	}



	protected boolean isCondition() {
		boolean isCondition = false;
		synchronized(mVectorLock) {
			isCondition = (!mRequests.isEmpty() || !mRemoteRequests.isEmpty());
		}
		return isCondition;
	}

	protected void prepareWorker() throws Exception {
		mThread = Thread.currentThread();
		Constants.LOGGER.config("Distributor thread prepared.");
	}

	protected void releaseWorker() throws Exception {
		Constants.LOGGER.config("Distributor thread released.");
	}



	/**
	 * Inserts the given items in the network.
	 *
	 * @param items the items to insert.
	 */
	void insert(Collection items) {
		synchronized(mVectorLock) {
			mRequests.add(new DistributionRequest(DataModifier.INSERT, items));
		}
		broadcast();
	}

	/**
	 * Inserts the given items in the network.
	 *
	 * @param items the items to insert.
	 */
	void update(Collection items) {
		synchronized(mVectorLock) {
			mRequests.add(new DistributionRequest(DataModifier.UPDATE, items));
		}
		broadcast();
	}

	/**
	 * Inserts the given items in the network.
	 *
	 * @param items the items to insert.
	 */
	void delete(Collection items) {
		synchronized(mVectorLock) {
			mRequests.add(new DistributionRequest(DataModifier.DELETE, items));
		}
		broadcast();
	}

	/**
	 * Invoked when a new insert request was received by another host.
	 * @param message the insert request.
	 */
	public void remoteDistribution(DataModifierMessage message) {
		synchronized(mVectorLock) {
			mRemoteRequests.add(new RemoteDistributionRequest(message.getMode(), message));
		}
		broadcast();
	}

	/**
	 * Creates and sends a message to one or all of the given hosts for the provided data items.
	 *
	 * @param key the destination key.
	 * @param msg   the update message.
	 * @param listener the distribution result listener.
	 */
	void route(Key key, PGridMessage msg, DistributionListener listener) {
		mRouter.route(RoutingRequestFactory.createDistributionRoutingRequest(key, msg, listener));
	}

	/**
	 * Routes the message to all replicas excluding the ones which got the message already.
	 *
	 * @param msg   the message.
	 * @param exclReplicas the exclusion list.
	 */
	void routeToReplicas(PGridMessage msg, Vector exclReplicas) {
		mRouter.route(RoutingRequestFactory.createReplicasRoutingRequest(msg, exclReplicas));
	}

	/**
	 * Sorts the given date items according to their corresponding level.
	 * @param items the items to sort.
	 * @return an array of data item lists.
	 */
	Vector[] sortByLevel(Collection items) {
		String localPath = mPGridP2P.getLocalPath();
		Vector[] level = new Vector[localPath.length() + 1];
		for (Iterator it = items.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			String comPath = Utils.commonPrefix(localPath, item.getKey().toString());
			if ((comPath.length() == localPath.length()) || (comPath.length() == item.getKey().size())) {
				// data item belongs to local host => inform replicas
				if (level[level.length-1] == null)
					level[level.length-1] = new Vector();
				level[level.length-1].add(item);
			} else {
				// data item belongs to other host => inform host of responsible level
				if (level[comPath.length()] == null)
					level[comPath.length()] = new Vector();
				level[comPath.length()].add(item);
			}
		}
		return level;
	}

	protected void work() throws Exception {
		// Queries
		Vector request = null;
		synchronized (mVectorLock) {
			request = (Vector)mRequests.clone();
			mRequests.clear();
		}

		for (Iterator it = request.iterator();it.hasNext();) {
			mDataModifier.process((DistributionRequest)it.next());
		}

		Vector remoteRequest = null;
		synchronized (mVectorLock) {
			remoteRequest = (Vector)mRemoteRequests.clone();
			mRemoteRequests.clear();
		}
		for (Iterator it = remoteRequest.iterator();it.hasNext();)
				mDataModifier.remoteProcess((RemoteDistributionRequest)it.next());
	}

	/**
	 * Shutdown the system
	 */
	public void shutdown() {
		mThread.interrupt();
	}

}
/**
 * $Id: ReplicationBalancer.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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
import pgrid.PGridHost;
import pgrid.Properties;
import pgrid.util.Utils;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.SearchPathMessage;
import pgrid.network.protocol.SearchPathReplyMessage;
import pgrid.util.logging.LogFormatter;

import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * This class represents the replication balancer for load balancing in P-Grid.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ReplicationBalancer implements Runnable {

	/**
	 * The constant to reduce oscillatory behavior.
	 */
	private static final double BL = 0.1;

	/**
	 * The PGridP2P.Exchanger logger.
	 */
	public static final Logger LOGGER = Logger.getLogger("PGridP2P.Balancer");

	/**
	 * The logging file.
	 */
	public static final String LOG_FILE = "Balancer.log";

	/**
	 * The minimum of amount of accumulated samples.
	 */
	private static final int MIN_CHANGE = 10; // 10 is a good value

	/**
	 * The interval between two path change checks.
	 */
	private static final int PATH_CHANGE_INTERVAL = 600000; // 10 min.

	/**
	 * Further probability attenuete factor.
	 */
	private static final double PROB_C = 0.25;

	/**
	 * The time to wait for responses.
	 */
	private static final int REPLY_TIMEOUT = 120000; // 2 min.

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The message manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The transition probabilities.
	 */
	private TransitionProbability mProbTrans = new TransitionProbability();

	/**
	 * The randomizer delivers random numbers.
	 */
	private SecureRandom mRandomizer = new SecureRandom();

	/**
	 * The statistics collected by exchanges.
	 */
	private ExchangeStatistics mStats = new ExchangeStatistics();

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null); //LOG_FILE);
	}

	/**
	 * Creates and starts the replication balancer.
	 */
	public ReplicationBalancer() {
		mStorageManager = mPGridP2P.getStorageManager();
	}

	/**
	 * Resets the statistics.
	 */
	public void resetStatistics() {
		mStats.reset();
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p/>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	public void run() {
		while (true) {
			synchronized (this) {
				try {
					this.wait(PATH_CHANGE_INTERVAL);
				} catch (InterruptedException e) {
					// do balancing
				}
			}
			// if replication balancing is deactivated => wait
			if (!mPGridP2P.propertyBoolean(Properties.REPLICATION_BALANCE))
				continue;

			// ChangePath Alg. (Algorithm 6)
			int len = mPGridP2P.getLocalPath().length();
			boolean stop = false;
			mProbTrans = new TransitionProbability();
			while ((len > 0) && (!stop)) {
				if (mStats.samePath(len) >= mStats.compPath(len)) {
					if (mStats.count(len) > MIN_CHANGE) {
						mProbTrans.union(len, PROB_C * Math.max(mStats.samePath(len) - mStats.compPath(len) - BL, 0) / (2 * mStats.samePath(len)));
					}
				} else {
					stop = true;
				}
				len--;
			}
			double[][] sortedProbTrans = mProbTrans.sort();
			int k = 1;
			boolean change = false;
			while ((!change) && (k < sortedProbTrans[0].length)) {
				double prob = mRandomizer.nextDouble();
				if (prob < sortedProbTrans[1][k]) {
					//@todo fix me!
					//synchronized (Exchanger.LOCK) {
						searchPathAndClone((int)sortedProbTrans[0][k]);
						resetStatistics();
					// }
					change = true;
				}
				k++;
			}
			if (!change) {
				boolean reset = true;
				for (int i = 1; i <= mPGridP2P.getLocalPath().length(); i++) {
					if (mStats.count(i) <= (2 * MIN_CHANGE)) {
						reset = false;
						break;
					}
				}
				if (reset)
					resetStatistics();
			}
		}
	}

	/**
	 * Invoked when a remote search path message was received.
	 *
	 * @param host the sending host.
	 * @param msg  the received message.
	 */
	public void remoteSearchPath(PGridHost host, SearchPathMessage msg) {
		LOGGER.fine("received remote search path messeage from host " + host.toHostString() + " with path " + msg.getPath() + ", and common len " + msg.getCommonLen() + ".");
		String rPath = msg.getPath();
		int commonLen = msg.getCommonLen();
		int len = Utils.commonPrefix(mPGridP2P.getLocalPath(), rPath).length();
		// the common length of the paths are not as required => reply with 'Path Changed' code
		if ((len != commonLen) || (mPGridP2P.getLocalPath().length() == 0)) {
			LOGGER.fine("common length unequal do local common length => reply 'Path Changed'.");
			SearchPathReplyMessage replyMsg = new SearchPathReplyMessage(msg.getGUID());
			mMsgMgr.sendMessage(host, replyMsg, null);
			return;
		}
		int l0 = commonLen;
		while ((mRandomizer.nextDouble() > 0.5) && (l0 < mPGridP2P.getLocalPath().length())) {
			l0++;
		}
		if (l0 >= mPGridP2P.getLocalPath().length()) {
			// local peer is to be cloned
			LOGGER.fine("local peer will be cloned.");
			SearchPathReplyMessage replyMsg = new SearchPathReplyMessage(msg.getGUID(), mPGridP2P.getLocalPath(), mPGridP2P.getRoutingTable(), mStorageManager.getDataTable());
			mMsgMgr.sendMessage(host, replyMsg, null);
		} else {
			// try to find another peer
			LOGGER.fine("try to find another peer.");
			searchPathAndForward(host, msg, l0);
		}
	}

	/**
	 * Searches for a suitable peer to clone if the local peer decided to balance load.
	 *
	 * @param level the level to search for a peer to clone.
	 */
	private void searchPathAndClone(final int level) {
		//TODO: this part should be port to the new architecture of P-Grid 2
		/*LOGGER.fine("search path and clone at level " + (level - 1) + ".");
		List refs = new Vector(mPGridP2P.getRoutingTable().getLevelVector(level - 1));
		Collections.shuffle(refs);
		for (Iterator it = refs.iterator(); it.hasNext();) {
			PGridHost host = (PGridHost)it.next();
			SearchPathMessage msg = new SearchPathMessage(mPGridP2P.getLocalPath(), level - 1);
			LOGGER.fine("send local search path messeage to host " + host.toHostString() + ".");
			boolean sent = mMsgMgr.sendMessage(host, msg, true);
			if (!sent)
				continue;
			// wait and process incoming query reply messages
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait(REPLY_TIMEOUT);
				}
			} catch (InterruptedException e) {
				// do nothing
			}
			SearchPathReplyMessage searchPathReply = mMsgMgr.getSearchPathResponse(msg.getGUID(), true);
			if (searchPathReply == null) {
				continue;
			}
			if (searchPathReply.getCode() == SearchPathReplyMessage.CODE_PATH_CHANGED) {
				// the path of the reference has changed => remove reference and try another host
				LOGGER.fine("received lcoal search path reply messeage from host " + host.toHostString() + " with code 'Path Changed'.");
				mPGridP2P.getRoutingTable().removeLevel(host);
				continue;
			} else if (searchPathReply.getCode() == SearchPathReplyMessage.CODE_OK) {
				// a host was found => clone it
				LOGGER.fine("received local search path reply messeage from host " + host.toHostString() + " with code 'OK'.");
				mPGridP2P.setLocalPath(searchPathReply.getPath());
				mPGridP2P.getRoutingTable().clear();
				mPGridP2P.getRoutingTable().setFidgets(searchPathReply.getRoutingTable().getFidgetVector());
				for (int i = 0; i < searchPathReply.getPath().length(); i++) {
					mPGridP2P.getRoutingTable().setLevel(i, searchPathReply.getRoutingTable().getLevelVector(i));
				}
				mPGridP2P.getRoutingTable().setReplicas(searchPathReply.getRoutingTable().getReplicaVector());
				mPGridP2P.getRoutingTable().save();
				mStorageManager.getDataTable().clear();
				if (searchPathReply.getDataTable() != null)
					mStorageManager.getDataTable().addAll(searchPathReply.getDataTable().getDataItems());
				mStorageManager.saveDataTable();
				LOGGER.config("cloned host " + host.toHostString() + " with path " + searchPathReply.getPath() + ".");
				break;
			}
		}*/
	}

	/**
	 * Searches for a peer to clone and forwards the request to a possible suitable peer.
	 *
	 * @param host  the host willing to clone another host.
	 * @param msg   the search path message.
	 * @param level the requested level.
	 */
	private void searchPathAndForward(final PGridHost host, final SearchPathMessage msg, final int level) {
		//TODO: this part should be port to the new architecture of P-Grid 2
		/*Thread t = new Thread() {
			public void run() {
				SearchPathMessage newMsg = new SearchPathMessage(msg.getGUID(), mPGridP2P.getLocalPath(), level);
				List refs = new Vector(mPGridP2P.getRoutingTable().getLevelVector(level));
				Collections.shuffle(refs);
				for (Iterator it = refs.iterator(); it.hasNext();) {
					PGridHost ref = (PGridHost)it.next();
					LOGGER.fine("send remote search path message to host " + ref.toHostString() + ".");
					boolean sent = mMsgMgr.sendMessage(ref, newMsg, true);
					//@todo test if the amount of cloned and clones is more equal than before
					//if (!sent)
					//	continue;
					// wait and process incoming query reply messages
					try {
						synchronized (Thread.currentThread()) {
							Thread.currentThread().wait(REPLY_TIMEOUT);
						}
					} catch (InterruptedException e) {
						// do nothing
					}
					SearchPathReplyMessage searchPathReply = mMsgMgr.getSearchPathResponse(newMsg.getGUID(), true);
					if (searchPathReply == null) {
						continue;
					}
					if (searchPathReply.getCode() == SearchPathReplyMessage.CODE_PATH_CHANGED) {
						// the path of the reference has changed => remove reference and try another ref
						LOGGER.fine("received remote search path reply messeage from host " + ref.toHostString() + " with code 'Path Changed'.");
						mPGridP2P.getRoutingTable().removeLevel(ref);
						continue;
					} else if (searchPathReply.getCode() == SearchPathReplyMessage.CODE_OK) {
						// a ref was found => forward it to the search path initiator
						LOGGER.fine("received remote search path reply messeage from host " + ref.toHostString() + " with code 'OK'.");
						mMsgMgr.sendMessage(host, searchPathReply, false);
						break;
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();  */
	}

	/**
	 * Updates the statistics during an exchange.
	 *
	 * @param commonLen  the common length of exchanging peers.
	 * @param currentLen the current length of a previous peer.
	 * @param lLen       the remaining length of the local path (path.len-commonLen).
	 * @param rLen       the remaining length of the remote path (path.len-commonLen).
	 */
	public void updateStatistics(int commonLen, int currentLen, int lLen, int rLen) {
		int l = currentLen;
		while ((l <= commonLen) && (lLen > currentLen) && (rLen > currentLen)) {
			mStats.incCount(l + 1);
			if (commonLen > l) {
				mStats.incSamePath(l + 1, rLen);
			} else {
				mStats.incCompPath(l + 1, rLen);
			}
			l++;
		}
	}

}

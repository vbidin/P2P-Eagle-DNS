/**
 * $Id: IdentityMappingUpdater.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.maintenance.identity;

import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.PGridHost;
import pgrid.network.MessageManager;
import pgrid.util.logging.LogFormatter;

import java.util.Vector;
import java.util.logging.Logger;


/**
 * This class is in charge of doing the identity mapping
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public class IdentityMappingUpdater {
	/**
	 * The time to wait for lookup messages.
	 */
	private static final int REPLY_TIMEOUT = 1000 * 60; // 60s.

	/**
	 * The PGridP2P.Searcher logger.
	 */
	public static final Logger LOGGER = Logger.getLogger("PGridP2P.Id-ip_updater");

	/**
	 * The logging file.
	 */
	public static final String LOG_FILE = "Id-ip_updater.log";

	/**
	 * Host to where the lookup peer message and update message have been send
	 */
	protected Vector mLookupReply = new Vector();

	/**
	 * The requesting host.
	 */
	private PGridHost mHost = null;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Minimum quorum
	 */
	protected int mQuorum;

	/**
	 * The updated itentity item
	 */
	protected XMLIdentityDataItem mIdentity;

	/**
	 * The amount of started remote update.
	 */
	private int mRemoteUpdates = 0;

	/**
	 * The maximum number of tries
	 */
	private int mMaxAttemptPerThread = 3;

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, LOG_FILE);
	}

	/**
	 * Creates a new updater for a locally update.
	 *
	 * @param dataItem the mMapping.
	 */
	public IdentityMappingUpdater(XMLIdentityDataItem dataItem) {
		mIdentity = dataItem;

	}

	/**
	 * Perform a remote update
	 *
	 * @return true if the update met the quorum minumun, false otherwise
	 */
	public boolean remoteUpdate() {
		// todo: port to the new architecture
		/*
		mQuorum = mPGridP2P.propertyInteger(Properties.IDENTITY_MIN_QUORUM);
		int connectionAttempts = mPGridP2P.propertyInteger(Properties.IDENTITY_CONNECTION_ATTEMPS);
		int toContact = 3;//Constants.REPLICATION_FACTOR;
		final Key key = mIdentity.getKey();
		final int mode = PeerLookupMessage.ANY;
		long startTime;
		long endTime;
		Vector dataItems = new Vector();
		dataItems.add(mIdentity);
		final UpdateMessage msg = new UpdateMessage(new PGridKey(""), dataItems, UpdateMessage.UPDATE_OPERATION);

		LOGGER.finer("Starting a new identity process.");

		for (int c = 0; c < connectionAttempts; c++) {
			for (int i = 0; i < toContact && mRemoteUpdates < mQuorum; i++) {
				Thread s = new Thread("Lookup peer for ID-IP update - " + this.hashCode()) {
					PGridHost remoteHost;

					public void run() {
						boolean sent = false;

						for (int attempt = 0; attempt < mMaxAttemptPerThread && mRemoteUpdates < mQuorum; attempt++) {
							remoteHost = (PGridHost)mPGridP2P.lookup(PGridP2PFactory.sharedInstance().generateKey(key.toString()), REPLY_TIMEOUT);

							if (remoteHost != null) {
								// A host responsible for the ID-IP mMapping has been reached
								boolean failed = false;

								synchronized (mLookupReply) {
									failed = mLookupReply.contains(remoteHost);
									mLookupReply.add(remoteHost);
								}

								if (failed) {
									//This host has already be asked, probably by the random part, skip it
									LOGGER.fine("Attempt '"
											+ attempt + "' has faild. Host '" + remoteHost.toHostString() + "' has already been contacted");
									continue;
								} else {
									LOGGER.finer(remoteHost.toHostString() + " is responsible for the mapping (key: '" + key + "').");

									// send the update message
									if (remoteHost.equals(mPGridP2P.getLocalHost())) {
										// send a local msg
										mMsgMgr.newMessage(msg);
									} else {
										// send an update message
										sent = mMsgMgr.sendMessage(remoteHost, msg, null);
										if (sent == false) continue;
									}

									synchronized (mLookupReply) {
										// send the update
										++mRemoteUpdates;
										// wait up the main thread.
										mLookupReply.notifyAll();
									}


									break;
								}
							}
						}

					}
				};

				s.setDaemon(true);
				s.start();
			}

			while (true) {
				synchronized (mLookupReply) {
					if (mRemoteUpdates >= mQuorum) break;

					startTime = System.currentTimeMillis();
					try {
						mLookupReply.wait(REPLY_TIMEOUT);
					} catch (InterruptedException e) {
					}
					endTime = System.currentTimeMillis();

					if ((endTime - startTime) >= REPLY_TIMEOUT) break;
				}
			}

			synchronized (mLookupReply) {
				if (mRemoteUpdates >= mQuorum) {
					LOGGER.fine("Identification has succeeded. New idendity is " + ((PGridHost)mIdentity.getPeer()).toHostString() + ".");
					return true;
				}
			}
		}

		LOGGER.fine("Identification has faild.");

		// return true if we have contacted more host then the minimum quorum
		*/
		return false;

	}

}
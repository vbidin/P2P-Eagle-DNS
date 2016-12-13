/**
 * $Id: SynchronousLookupHandler.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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

package pgrid.network.lookup;

import p2p.basic.GUID;
import p2p.basic.Peer;
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.router.RoutingRequestFactory;
import pgrid.network.protocol.PeerLookupMessage;
import pgrid.Constants;

import java.util.Collection;
import java.util.Hashtable;

/**
 * This class processes remote lookup requests.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class SynchronousLookupHandler implements SearchListener {

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The treated queries.
	 */
	private Hashtable mLookups = new Hashtable();

	/**
	 * Wait object
	 */
	private final Hashtable mWaiter = new Hashtable();

	/**
	 * Creates a new RemoteSearchHandler.
	 *
	 * @param pgridP2P the PGridP2P facility.
	 */
	SynchronousLookupHandler(PGridP2P pgridP2P) {
		mPGridP2P = pgridP2P;
		mMsgMgr = MessageManager.sharedInstance();
	}

	/**
	 * Handles the query received by the sender.
	 *
	 * @param msg the lookup message.
	 * @param timeout maximum time to wait
	 */
	public Peer lookup(PeerLookupMessage msg, long timeout) {
		// try to route the message further
		mPGridP2P.getRouter().route(RoutingRequestFactory.createLookupRoutingRequest(msg, this));
		mWaiter.put(msg.getGUID(), Thread.currentThread());

		synchronized(Thread.currentThread()) {
			try {
				Thread.currentThread().wait(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

		mWaiter.remove(msg.getGUID());
		return (Peer)mLookups.remove(msg.getGUID());
	}

	/**
	 * Invoked when a new search result is available
	 *
	 * @param guid    the GUID of the original query
	 * @param results a Collection of hosts responsible for the path (only one host is returned
	 * in this version)
	 */
	public void newSearchResult(GUID guid, Collection results) {
		// get the requesting host
		Thread thread = (Thread)mWaiter.get(guid);
		if (thread == null) {
			Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (newSearchResult)!");
		} else {
			// reply the results to the requesting host
			Constants.LOGGER.fine("["+guid.toString()+"]: Return results for lookup request.");

			mLookups.put(guid, results.iterator().next());

			synchronized(thread) {
				thread.notifyAll();
			}
		}
	}

	/**
	 * Invoked when a search resulted in no results.
	 *
	 * @param guid the GUID of the original lookup
	 */
	public void noResultsFound(GUID guid) {
		// get the requesting host
		Thread thread = (Thread)mWaiter.get(guid);
		if (thread == null) {
			Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (noResultsFound)!");
		} else {
			// reply with not found message
			Constants.LOGGER.fine("["+guid.toString()+"]: Return no results for lookup request.");

			synchronized(thread) {
				thread.notifyAll();
			}
		}
	}

	/**
	 * Invoked when a search failed.
	 *
	 * @param guid the GUID of the original lookup
	 */
	public void searchFailed(GUID guid) {
		// get the requesting host
		Thread thread = (Thread)mWaiter.get(guid);
		if (thread == null) {
			Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (searchFailed)!");
		} else {
			// reply with bad request message
			Constants.LOGGER.fine("["+guid.toString()+"]: Return search failed.");

			synchronized(thread) {
				thread.notifyAll();
			}
		}
	}

	/**
	 * Invoked when a search finished.
	 *
	 * @param guid the GUID of the original lookup
	 */
	public void searchFinished(GUID guid) {
		// do nothing
	}

	/**
	 * Invoked when a search started (reached a responsible peer).
	 *
	 * @param guid the GUID of the original lookup
	 * @param message the explanation message.
	 */
	public void searchStarted(GUID guid, String message) {
		// do nothing
	}

}
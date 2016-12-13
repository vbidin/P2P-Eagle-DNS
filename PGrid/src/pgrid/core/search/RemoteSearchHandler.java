/**
 * $Id: RemoteSearchHandler.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.search;

import p2p.basic.GUID;
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.QueryReplyMessage;
import pgrid.AbstractQuery;
import pgrid.PGridHost;
import pgrid.Constants;
import pgrid.QueryReply;

import java.util.Collection;
import java.util.Hashtable;

/**
 * This class processes remote search requests.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class RemoteSearchHandler implements SearchListener {

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
	private Hashtable mQueries = new Hashtable();

	/**
	 * Creates a new RemoteSearchHandler.
	 *
	 * @param pgridP2P the PGridP2P facility.
	 */
	RemoteSearchHandler(PGridP2P pgridP2P) {
		mPGridP2P = pgridP2P;
		mMsgMgr = MessageManager.sharedInstance();
	}

	/**
	 * Handles the query received by the sender.
	 *
	 * @param query the query.
	 * @param sender the sending host.
	 */
	void register(AbstractQuery query, PGridHost sender) {
		// reply with an OK ACK message
		mMsgMgr.sendMessage(sender, new ACKMessage(query.getGUID(), ACKMessage.CODE_OK, "Query will be forwared to " + mPGridP2P.getRoutingTable().getReplicaVector().size() + " replicas."));

		// add query to list of treated queries
		mQueries.put(query.getGUID(), query);
	}

	/**
	 * Invoked when a new search result is available
	 *
	 * @param guid    the GUID of the original query
	 * @param results a Collection of DataItems matching the original query
	 */
	public void newSearchResult(GUID guid, Collection results) {
		// get the requesting host
		AbstractQuery query = (AbstractQuery)mQueries.get(guid);
		if (query == null) {
			Constants.LOGGER.config("no query '" + guid.toString() + "' found for search results!");
		} else {
			// reply the results to the requesting host
			Constants.LOGGER.fine("return results for query '" + guid.toString() + "'.");
			mMsgMgr.sendMessage(query.getInitialHost(), new QueryReplyMessage(guid, QueryReply.TYPE_OK, results));
		}
	}

	/**
	 * Invoked when a search resulted in no results.
	 *
	 * @param guid the GUID of the original query
	 */
	public void noResultsFound(GUID guid) {
		// get the requesting host
		AbstractQuery query = (AbstractQuery)mQueries.get(guid);
		if (query == null) {
			Constants.LOGGER.config("no query '" + guid.toString() + "' found for search results!");
		} else {
			// reply with not found message
			Constants.LOGGER.fine("return no results for query '" + guid.toString() + "'.");
			mMsgMgr.sendMessage(query.getInitialHost(), new QueryReplyMessage(guid, QueryReply.TYPE_NOT_FOUND, null));
		}
	}

	/**
	 * Invoked when a search failed.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFailed(GUID guid) {
		// get the requesting host
		AbstractQuery query = (AbstractQuery)mQueries.get(guid);
		if (query == null) {
			Constants.LOGGER.config("no query '" + guid.toString() + "' found for search results!");
		} else {
			// reply with bad request message
			Constants.LOGGER.fine("return search failed for query '" + guid.toString() + "'.");
			mMsgMgr.sendMessage(query.getInitialHost(), new QueryReplyMessage(guid, QueryReply.TYPE_BAD_REQUEST, null));
			mQueries.remove(guid);
		}
	}

	/**
	 * Invoked when a search finished.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFinished(GUID guid) {
		// remove the query from the list of treated queries
		mQueries.remove(guid);
	}

	/**
	 * Invoked when a search started (reached a responsible peer).
	 *
	 * @param guid the GUID of the original query
	 * @param message the explanation message.
	 */
	public void searchStarted(GUID guid, String message) {
		// do nothing
	}

}
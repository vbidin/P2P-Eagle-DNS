/**
 * $Id: RemoteRequestHandler.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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
import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.AbstractQuery;
import pgrid.network.MessageManager;
import pgrid.network.protocol.ACKMessage;

import java.util.Collection;
import java.util.Hashtable;
import java.util.WeakHashMap;

/**
 * This class handles remote search requests the local peer is not responsible for.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class RemoteRequestHandler implements SearchListener {

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = null;

  /**
   * The treated queries.
   */
  private Hashtable mQueries = new Hashtable();

	/**
	 * The senders of treated queries.
	 */
	private WeakHashMap mSenders = new WeakHashMap();

	/**
	 * Creates a new RemoteSearchHandler.
	 *
	 */
	RemoteRequestHandler() {
		mMsgMgr = MessageManager.sharedInstance();
	}

  /**
   * Invoked when a new search result is available
   *
   * @param guid    the GUID of the original query
   * @param results a Collection of DataItems matching the original query
   */
  public void newSearchResult(GUID guid, Collection results) {
		Constants.LOGGER.warning("Remote request handler has to treat search results for search " + guid.toString() + ".");
  }

  /**
   * Invoked when a search resulted in no results.
   *
   * @param guid the GUID of the original query
   */
  public void noResultsFound(GUID guid) {
		Constants.LOGGER.warning("Remote request handler has to treat no results found for search " + guid.toString() + ".");
  }

	/**
	 * Handles the query received by the sender.
	 *
	 * @param query the query.
	 * @param sender the sending host.
	 */
	void register(AbstractQuery query, PGridHost sender) {
		// add query to list of treated queries
		mQueries.put(query.getGUID(), query);
		mSenders.put(query.getGUID(), sender);
	}

  /**
   * Invoked when a search failed.
   *
   * @param guid the GUID of the original query
   */
  public void searchFailed(GUID guid) {
		sendACKMessage(guid, ACKMessage.CODE_CANNOT_ROUTE, null);
	  // remove the query from the list of treated queries
		mQueries.remove(guid);
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
		sendACKMessage(guid, ACKMessage.CODE_OK, message);
	}

	/**
	 * Sends an ACK message for the query.
	 * @param guid the id of the query.
	 * @param ackCode the ACK code (OK or CANNOT_ROUTE).
	 * @param message the explanation message.
	 */
	private void sendACKMessage(GUID guid, int ackCode, String message) {
		// get the requesting host
		AbstractQuery query = (AbstractQuery)mQueries.get(guid);
		if (query == null) {
			Constants.LOGGER.info("no query '" + guid.toString() + "' found for search!");
		} else {
			PGridHost host = (PGridHost)mSenders.get(guid);
			if (host == null) {
				Constants.LOGGER.info("no sending host found for query " + guid.toString() + "!");
			} else {
				// reply with cannot route ACK message
				Constants.LOGGER.fine("return ACK message for query " + guid.toString() + ".");
				mMsgMgr.sendMessage(host, new ACKMessage(guid, ackCode, message));
			}
		}
	}

}
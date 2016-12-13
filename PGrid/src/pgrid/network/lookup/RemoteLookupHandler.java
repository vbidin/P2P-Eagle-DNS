/**
 * $Id: RemoteLookupHandler.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.router.RoutingRequestFactory;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.PeerLookupMessage;
import pgrid.network.protocol.PeerLookupReplyMessage;
import pgrid.Constants;

import java.util.Collection;
import java.util.Hashtable;

/**
 * This class processes remote lookup requests.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class RemoteLookupHandler implements SearchListener {

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
	 * Creates a new RemoteSearchHandler.
	 *
	 * @param pgridP2P the PGridP2P facility.
	 */
	RemoteLookupHandler(PGridP2P pgridP2P) {
		mPGridP2P = pgridP2P;
		mMsgMgr = MessageManager.sharedInstance();
	}

  /**
   * Handles the query received by the sender.
   *
   * @param msg the lookup message.
   */
  void register(PeerLookupMessage msg) {
	  	int index = msg.getIndex();
		String path = msg.getPath();

		int compath = pgrid.util.Utils.commonPrefix(path, mPGridP2P.getLocalPath()).length();

		if (compath != path.length() && compath < index) {
			// reply with an bad routed ACK message
			mMsgMgr.sendMessage(msg.getHeader().getHost(), new ACKMessage(msg.getGUID(), ACKMessage.CODE_WRONG_ROUTE, ""));

		}
		else {
			Constants.LOGGER.fine("["+msg.getGUID().toString()+"]: Register remote peer lookup request.");

			// add query to list of treated queries
   		 	mLookups.put(msg.getGUID(), msg);

			// reply with an OK ACK message
			mMsgMgr.sendMessage(msg.getHeader().getHost(), new ACKMessage(msg.getGUID(), ACKMessage.CODE_OK, ""));


			// try to route the message further
			mPGridP2P.getRouter().route(RoutingRequestFactory.createLookupRoutingRequest(msg, this));
		}
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
    PeerLookupMessage msg = (PeerLookupMessage)mLookups.get(guid);
    if (msg == null) {
    	Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (newSearchResult)!");
    } else {
      	// reply the results to the requesting host
      	Constants.LOGGER.fine("["+guid.toString()+"]: Return results for lookup request.");

		PeerLookupReplyMessage reply = new PeerLookupReplyMessage(msg
					.getGUID(), mPGridP2P.getLocalHost(),
					PeerLookupReplyMessage.TYPE_OK, msg.getPeerLookupHops());

		mMsgMgr.sendMessage(msg.getInitialHost(), reply);
    }
  }

  /**
   * Invoked when a search resulted in no results.
   *
   * @param guid the GUID of the original lookup
   */
  public void noResultsFound(GUID guid) {
	  // get the requesting host
	  PeerLookupMessage msg = (PeerLookupMessage)mLookups.get(guid);
	  if (msg == null) {
		  Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (noResultsFound)!");
	  } else {
		  // reply with not found message
		  Constants.LOGGER.fine("["+guid.toString()+"]: Return no results for lookup request.");
		  PeerLookupReplyMessage reply = new PeerLookupReplyMessage(msg
				  .getGUID(), mPGridP2P.getLocalHost(),
				  PeerLookupReplyMessage.TYPE_NO_PEER_FOUNDS, msg.getPeerLookupHops());

		  mMsgMgr.sendMessage(msg.getInitialHost(), reply);
	  }
  }

  /**
   * Invoked when a search failed.
   *
   * @param guid the GUID of the original lookup
   */
  public void searchFailed(GUID guid) {
	  // get the requesting host
	  PeerLookupMessage msg = (PeerLookupMessage)mLookups.get(guid);
	  if (msg == null) {
		  Constants.LOGGER.config("["+guid.toString()+"]: Unsolicited lookup message found for search results (searchFailed)!");
	  } else {
		  // reply with bad request message
		  Constants.LOGGER.fine("["+guid.toString()+"]: Return search failed.");

		  PeerLookupReplyMessage reply = new PeerLookupReplyMessage(msg
				  .getGUID(), mPGridP2P.getLocalHost(),
				  PeerLookupReplyMessage.TYPE_BAD_REQUEST, msg.getPeerLookupHops());

		  Constants.LOGGER.fine("["+guid.toString()+"]: Release remote peer lookup request (searchFailed).");
		  mLookups.remove(guid);
		  mMsgMgr.sendMessage(msg.getInitialHost(), reply);
	  }
  }

	/**
	 * Invoked when a search finished.
	 *
	 * @param guid the GUID of the original lookup
	 */
	public void searchFinished(GUID guid) {
		// remove the query from the list of treated queries
		Constants.LOGGER.fine("["+guid.toString()+"]: Release remote peer lookup request (searchFinished).");

		mLookups.remove(guid);
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
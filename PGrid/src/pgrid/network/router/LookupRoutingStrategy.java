/**
 * $Id: LookupRoutingStrategy.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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

package pgrid.network.router;

import pgrid.PGridHost;
import pgrid.PGridKey;
import pgrid.util.PathComparator;
import pgrid.core.RoutingTable;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.router.PeerLookupReplyWaiter;
import pgrid.network.router.Router;
import pgrid.network.router.Request;
import pgrid.network.router.LookupRoutingRequest;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.PeerLookupMessage;
import pgrid.network.protocol.PeerLookupReplyMessage;
import pgrid.util.Utils;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import p2p.storage.events.SearchListener;

/**
 * The Query Router routes query messages in the network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class LookupRoutingStrategy extends pgrid.network.router.RoutingStrategy implements PeerLookupReplyWaiter {

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME ="Lookup routing";

	  /**
	 * The list of all already seen Querys.
		*/
	  private Hashtable mQueries = new Hashtable();

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Message manager
	 */
	protected MessageManager mMsgMng = MessageManager.sharedInstance();

	/**
	 * default constructor
	 *
	 * @param router
	 */
	public LookupRoutingStrategy(Router router) {
		super(router);
	}

	/**
	 * Routes a query to the responsible peer.
	 *
	 * @param routingRequest the query route request.
	 */
	public void route(Request routingRequest) {
			if (!(routingRequest instanceof LookupRoutingRequest)) {
			  // TODO throw an exception if needed
				return;
		  }
		LookupRoutingRequest request = (LookupRoutingRequest)routingRequest;

		String path = request.getLookupMessage().getPath();
		String localPath = mPGridP2P.getLocalPath();
		PeerLookupMessage msg = request.getLookupMessage();
		boolean sent = false;

		// remember the request
		mQueries.put(request.getLookupMessage().getGUID(), request);
		Router.LOGGER.finest("["+request.getLookupMessage().getGUID()+"]: Inserting a lookup request reference.");

		int compath = Utils.commonPrefix(localPath, path).length();

		//First part of the algorithm. Route to a peer responsible for path
		if (path.length() > compath && localPath.length() > compath) {
			Router.LOGGER.fine("Searching for a subtree compatible with path: '" + path + "'.");

			// forward the message to an other peer
			sent = getRouter().route(new PGridKey(path), request.getLookupMessage(), this);
			if (!sent) {
				//TODO do something like sending an ACK
			}

		} else if ((localPath.length() > compath) &&
				(msg.getMode() != PeerLookupMessage.ANY)) {
			//second part of the algorithm: find the left or right most peer
			Router.LOGGER
					.fine("Searching for the " + ((msg.getMode() == PeerLookupMessage.RIGHT_MOST) ? "right" : "left") + " most peer of key space: '" + msg.getPath()
					+ "'.");
			PGridHost hosts[];
			Vector list;
			int cmp;
			PathComparator pathComparator = new PathComparator();
			RoutingTable routingTable = mPGridP2P.getRoutingTable();
			int routingLevelCount = routingTable.getLevelCount();


			for (int index = msg.getPath().length(); index < routingLevelCount && !sent; index++) {
				hosts = routingTable.getLevel(index);
				if (hosts.length == 0)
					continue;
				cmp = pathComparator.compare(hosts[0].getPath(), localPath);
				if ((cmp > 0 && msg.getMode() == PeerLookupMessage.RIGHT_MOST)
						|| (cmp < 0 && msg.getMode() == PeerLookupMessage.LEFT_MOST)) {
					//Try to forward the lookup message
					msg.setIndex(index);
					sent = getRouter().routeAtLevel(msg, index, this);

					if (sent) {
						Router.LOGGER
								.fine("Lookup message has been forwarded.");
						//we should not send a lookup reply
						break;
					}
				}
			}
		}
		if (!sent) {
			Router.LOGGER.fine("Sending a lookup peer reply to host: " + msg.getInitialHost().toHostString() + ".");

			//if this lookup message has been started remotly, we don't care anymore
			if (!request.getLookupMessage().getInitialHost().equals(mPGridP2P.getLocalHost())) {
				Router.LOGGER.finest("["+request.getLookupMessage().getGUID()+"]: Removing lookup request reference for replied lookup (Route).");
				mQueries.remove(request.getLookupMessage().getGUID());
			}

			//msg hasn't been forwarded. This peer is the right one.
			Vector hosts = new Vector();
			hosts.add(mPGridP2P.getLocalHost());

			request.getListener().newSearchResult(msg.getGUID(), hosts);
			request.getListener().searchFinished(request.getLookupMessage().getGUID());
		}

	}

	/**
	 * A new peer lookup reply was received.
	 *
	 * @param message the response message.
	 */
	public void newPeerLookupReply(PeerLookupReplyMessage message) {
		LookupRoutingRequest request = (LookupRoutingRequest)mQueries.get(message.getGUID());
		PeerLookupMessage msg = request.getLookupMessage();
		SearchListener listener = request.getListener();

		Router.LOGGER.fine("Response for remote lookup (" + message.getGUID().toString() + ") with path '" + msg.getPath() + "' from " + message.getHeader().getHost().toHostString() + " received.");
		// something message
		if (message.getType() == PeerLookupReplyMessage.TYPE_OK) {
			// local request => add results
			Router.LOGGER.fine("Responsible host found.");
			Vector hosts = new Vector();
			hosts.add(message.getHost());
			listener.newSearchResult(msg.getGUID(), hosts);
		} else if (message.getType() == PeerLookupReplyMessage.TYPE_NO_PEER_FOUNDS) {
			PGridP2P.sharedInstance().getStatistics().LookupNotFound++;
			Router.LOGGER.fine("Return NO_PEER_FOUNDS for the lookup (" + msg.getGUID().toString() + ") with path '" + msg.getPath() + "' from host " + message.getHeader().getHost().toHostString() + ".");
			listener.noResultsFound(msg.getGUID());
		}

		//We have found the host (or not, who cares?) we can remove the reference to the request
		mQueries.remove(message.getGUID());
		listener.searchFinished(message.getGUID());
		Router.LOGGER.finest("["+message.getGUID()+"]: Removing lookup request reference for local lookup (newPeerLookupReply).");
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		LookupRoutingRequest request = (LookupRoutingRequest)mQueries.get(message.getGUID());
		if (request == null) {
			Router.LOGGER.log(Level.WARNING, "["+message.getGUID()+"]: Received an unsolicited acknowlegment message.");
			return;
		}
		SearchListener listener = request.getListener();
		short status = getRouter().checkAcknowledgment(message);
		if (status == Router.ROUTE_OK) {
			listener.searchStarted(message.getGUID(), message.getMessage());
		} else if (status == Router.ROUTE_FAILED) {
			listener.searchFailed(message.getGUID());
		}
		//if this lookup message has been started remotly, we don't care anymore
		if (!request.getLookupMessage().getInitialHost().equals(mPGridP2P.getLocalHost())) {
			Router.LOGGER.finest("["+message.getGUID()+"]: Removing lookup request reference for remote lookup (newAcknowledgment).");
			listener.searchFinished(message.getGUID());
			mQueries.remove(message.getGUID());
		}
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

}
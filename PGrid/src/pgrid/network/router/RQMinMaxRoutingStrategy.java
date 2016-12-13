/**
 * $Id: RQMinMaxRoutingStrategy.java,v 1.3 2005/12/02 18:15:31 john Exp $
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

import p2p.storage.events.SearchListener;
import p2p.basic.GUID;
import pgrid.*;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.*;
import pgrid.network.MessageManager;

import java.util.*;

/**
 * The Query Router routes query messages in the network.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
class RQMinMaxRoutingStrategy extends RoutingStrategy implements QueryReplyWaiter, SearchListener {

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME = RangeQuery.MINMAX_ALGORITHM;

	  /**
	 * The list of all already seen Querys.
		*/
	  private Hashtable mQueries = new Hashtable();

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * default constructor
	 *
	 * @param router
	 */
	public RQMinMaxRoutingStrategy(Router router) {
		super(router);
	}

	/**
	 * This method try to guess the next path in P-Grid. If there is no higher path
	 * it will returns null.
	 *
	 * @return the next path or null if there is no next peer
	 */
	protected String computeNextPath(String path) {

		if (path.equals("")) return "";

		String nextPath = Integer.toBinaryString(Integer.parseInt(path, 2) + 1);
		int toAdd = path.length() - nextPath.length();

		if (toAdd > 0) {
			char[] zeros = new char[toAdd];
			Arrays.fill(zeros, '0');
			nextPath = String.valueOf(zeros) + nextPath;
		}

		if (nextPath.length() <= path.length())
			return nextPath;

		return null;
	}

	/**
	 * Routes a query to the responsible peer.
	 *
	 * @param routingRequest the query route request.
	 */
	public void route(Request routingRequest) {
		// check the correctness of the routingRequest
		if (!(routingRequest instanceof RangeQueryRoutingRequest)) {
			// TODO throw an exception if needed
			return;
		}

		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)routingRequest;

		if (mPGridP2P.getLocalPath().equals("")) {
			Router.LOGGER.fine("P-Grid is currently not structured. Range query ("+request.getQuery().getGUID().toString()+") won't continue.");
			request.getSearchListener().searchFinished(request.getQuery().getGUID());
			return;
		}

		// set the path if needed
		if (request.getPath() == null) {
			if (request.getQuery() instanceof RangeQueryMessage) {
				request.setPath(computeNextPath(mPGridP2P.getLocalHost().getPath()));
			} else request.setPath(request.getQuery().getKeyRange().getMin().toString());

			mQueries.put(request.getQuery().getGUID(), request);
			Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Route range query ("+request.getQuery().getKeyRange()+")"+
					(request.getPath() != null?" to path '"+request.getPath()+"'.":"."));

		} else {
			request.setPath(computeNextPath(request.getPath()));
			Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Continue routing of range query ("+request.getQuery().getKeyRange()+")"+
					(request.getPath() != null?" to path '"+request.getPath()+"'.":"."));
		}


		// find next peer address or send the query message to the next peer
		boolean sent = false;
		PGridHost neighbor = request.popNextHost();
		if (neighbor != null) {
			 sent=queryNeighbor(request, neighbor);
		}
	   	if (!sent) {
			findNeighbor(request);
		}

	}

	private void findNeighbor(RangeQueryRoutingRequest request) {
		RangeQuery query = request.getQuery();

		String path = request.getPath();

		// check if the current peer is responsible for the next path, if yes, go to next neighbor
		if (path != null && mPGridP2P.isLocalPeerResponsible(new PGridKey(request.getPath()))) {
			request.setPath(computeNextPath(mPGridP2P.getLocalHost().getPath()));
			Router.LOGGER.finer("["+request.getQuery().getGUID()+"]: The local peer is responsible for '"+path+"' skip "+
					(request.getPath() != null?"to path '"+request.getPath()+"'.":"to next path"));
		}
		// check if a next path wasn't available, stop the algo
		if (path == null) {
			Router.LOGGER.finer("["+query.getGUID()+"]: Range query cannot be further forwarded.");
			cleanRemote(request, "Route");
			return;
		}

		if (query.getKeyRange().withinRange(new PGridKey(path))) {
			Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Trying to contact host reponsible for key '"
					+ path + "'.");
			GUID guid = pgrid.GUID.getGUID();
			PeerLookupMessage msg = new PeerLookupMessage(guid, path, mPGridP2P.getLocalHost(), PeerLookupMessage.LEFT_MOST);
			mQueries.put(guid, request);
			mPGridP2P.getLookupManager().peerLookup(msg, this);
		} else {
			Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Next path out of range '"
					+ path + " > " + query.getKeyRange().getMax().toString() + "'.");
			cleanRemote(request, "findNeighbor");
		}

	}

	private boolean queryNeighbor(RangeQueryRoutingRequest request, PGridHost remoteHost) {
		RangeQuery query = request.getQuery();
		RangeQueryMessage msg;
		if (query instanceof RangeQueryMessage) {
			// the query is already a message => simply forward it
			//String compath = Utils.commonPrefix(query.getKey().toString(), mPGridP2P.getLocalPath());
			msg = (RangeQueryMessage)query;
			//msg.setIndex(compath.length());
			msg.incHops();
		} else {
			// create message
			msg = new RangeQueryMessage(query.getGUID(), query.getType(),
					query.getHops(), query.getAlgorithm(), query.getLowerBound(), query.getHigherBound(),
					query.getKeyRange(), query.getIndex(), query.getPrefix(),
					query.getMinSpeed(), query.getInitialHost());
		}

		return mMsgMgr.sendMessage(remoteHost, msg, this);
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newQueryReply(QueryReplyMessage message) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(message.getGUID());
		if (request == null) return;
		
		RangeQuery query = request.getQuery();
		SearchListener listener = request.getSearchListener();

		Router.LOGGER.fine("Response for remote search (" + query.getGUID().toString() + ") with key range '" + query.getKeyRange() + "' for '" + query.getLowerBound() + " - " + query.getHigherBound() + "' from " + message.getHeader().getHost().toHostString() + " received with " + message.getHits() + " hit(s).");
		// something message
		if (message.getType() == QueryReply.TYPE_OK) {
			// local request => add results
			Router.LOGGER.fine("Return " + message.getHits() + " file(s) for the search (" + query.getGUID().toString() + ") with key range '" + query.getKeyRange() + "' for '" + query.getLowerBound() + " - " + query.getHigherBound() + "' returned from host " + message.getHeader().getHost().toHostString() + ".");
			listener.newSearchResult(query.getGUID(), message.getResultSet());
		} else if (message.getType() == QueryReply.TYPE_BAD_REQUEST) {
			PGridP2P.sharedInstance().getStatistics().QueryBadRequest++;
			listener.searchFailed(query.getGUID());
		} else if (message.getType() == QueryReply.TYPE_NOT_FOUND) {
			PGridP2P.sharedInstance().getStatistics().QueryNotFound++;
			Router.LOGGER.fine("Return NOT_FOUND for the search (" + query.getGUID().toString() + ") with key range '" + query.getKeyRange() + "' for '" + query.getLowerBound() + " - " + query.getHigherBound() + "' returned from host " + message.getHeader().getHost().toHostString() + ".");
			listener.noResultsFound(query.getGUID());
		}
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(message.getGUID());
		if (request == null) return;
		
		SearchListener listener = request.getSearchListener();
		short status = getRouter().checkAcknowledgment(message);
		if (status == Router.ROUTE_OK) {
			listener.searchStarted(message.getGUID(), message.getMessage());
		} else if (status == Router.ROUTE_FAILED) {
			listener.searchFailed(message.getGUID());
		}

		//if this range query message has been started remotly, we don't care anymore
		cleanRemote(request, "newAcknowledgment");
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

	/**
	 * For the lookup algorithm
	 * @see p2p.storage.events.SearchListener#newSearchResult(p2p.basic.GUID, java.util.Collection)
	 */
	public void newSearchResult(GUID guid, Collection hosts) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(guid);
		if (request == null) return;

		if (hosts.size() == 0)
			Router.LOGGER.fine("Response for remote lookup invalid. No host found for (" + guid.toString() + "). This looks like a bug, please report.");
		else {
			PGridHost host = (PGridHost)hosts.iterator().next();

			// in this implementation, the result set of the initiator peer is already counted
			// if the peer is the initiator, skip it
			if (host.getPath().equals(request.getQuery().getInitialHost().getPath())) {
				Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Neighbor is the initiator peer, skip path"+(request.getPath()!=null?" (" + request.getPath() + ").":"."));
			} else {
				Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Response for remote lookup (" + host.getGUID().toString() + ") from " + host.toHostString()+".");
				request.setNextHost(host);
			}
			request.setPath(host.getPath());
		}
		getRouter().route(request);
	}


	/**
	 * Invoked when a search failed.
	 *
	 * @param guid the GUID of the original query
	 */
	public void noResultsFound(p2p.basic.GUID guid) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(guid);
		if (request == null) return;

		getRouter().route(request);
	}

	/**
	 * Invoked when a search failed.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFailed(GUID guid) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(guid);
		if (request == null) return;

		getRouter().route(request);
	}

	/**
	 * Invoked when a search finished.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFinished(GUID guid) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Invoked when a search started (reached a responsible peer).
	 *
	 * @param guid	the GUID of the original query
	 * @param message the explanation message.
	 */
	public void searchStarted(GUID guid, String message) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Remove unnecessary reference to search request
	 * @param request
	 * @param location
	 */
	private void cleanRemote(RangeQueryRoutingRequest request, String location) {
		if (!request.getQuery().getInitialHost().equals(mPGridP2P.getLocalHost())) {
			Router.LOGGER.finest("["+request.getQuery().getGUID()+"]: Removing range query request reference for remote search ("+location+").");
			request.getSearchListener().searchFinished(request.getQuery().getGUID());
			mQueries.remove(request.getQuery().getGUID());
		}

	}
}
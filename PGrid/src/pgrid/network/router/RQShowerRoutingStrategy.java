/**
 * $Id: RQShowerRoutingStrategy.java,v 1.6 2006/01/16 11:30:53 john Exp $
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
import pgrid.*;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.QueryReplyMessage;
import pgrid.network.protocol.RangeQueryMessage;
import pgrid.network.router.QueryReplyWaiter;
import pgrid.network.router.Router;
import pgrid.network.router.Request;
import pgrid.network.router.RangeQueryRoutingRequest;

import java.util.*;

/**
 * The Query Router routes query messages in the network.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
class RQShowerRoutingStrategy extends pgrid.network.router.RoutingStrategy implements QueryReplyWaiter, pgrid.util.TimerListener {

	/**
	 * The timer manager
	 */
	protected pgrid.util.TimerManager timerManager = pgrid.util.TimerManager.sharedInstance();

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME = RangeQuery.SHOWER_ALGORITHM;

	  /**
	 * The list of all already seen Querys.
		*/
	  private Hashtable mQueries = new Hashtable();

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * default constructor
	 *
	 * @param router
	 */
	public RQShowerRoutingStrategy(Router router) {
		super(router);
		timerManager.register(1000*60*1, null, this, true);
	}

	/**
	 * Routes a range query to the responsible peer.
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

		RangeQuery query = request.getQuery();
		boolean localSearch = false; // true if the search is local


		// add query to the already seen queries list
		request.startProcessing();
		mQueries.put(request.getQuery().getGUID(), request);

		RangeQueryMessage msg;

		if (query instanceof RangeQueryMessage) {
			// the query is already a message => simply forward it
			msg = (RangeQueryMessage)query;
			msg.incHops();
		} else {
			// create message
			msg = new RangeQueryMessage(query.getGUID(), query.getType(),
					query.getHops()+1, query.getAlgorithm(), query.getLowerBound(), query.getHigherBound(),
					query.getKeyRange(), 0, query.getPrefix(),
					query.getMinSpeed(), query.getInitialHost());

			localSearch = true;
		}


		//forward the range query to all subtrees
		getRouter().route(msg.getKeyRange(), msg, localSearch, this);
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newQueryReply(QueryReplyMessage message) {
		RangeQueryRoutingRequest request = (RangeQueryRoutingRequest)mQueries.get(message.getGUID());
		if (request == null) return;
		
		RangeQuery query = (RangeQuery)request.getQuery();
		SearchListener listener = request.getSearchListener();

		Router.LOGGER.fine("Response for remote search (" + query.getGUID().toString() + ") with key range '" + query.getKeyRange() + "' for '" + query.getLowerBound() + " - " + query.getHigherBound() + "' from " + message.getHeader().getHost().toHostString() + " received with " + message.getHits() + " hit(s).");
		// something message
		if (message.getType() == QueryReply.TYPE_OK) {
			// local request => add results
			Router.LOGGER.fine("Return " + message.getHits() + " file(s) for the search (" + query.getGUID().toString() + ") with key range '" + query.getKeyRange() + "' for '" + query.getLowerBound() + " - " + query.getHigherBound() + "' returned from host " + message.getHeader().getHost().toHostString() + ".");
			listener.newSearchResult(query.getGUID(), message.getResultSet());
		} else if (message.getType() == QueryReply.TYPE_BAD_REQUEST) {
			if (Constants.TESTS)
				PGridP2P.sharedInstance().getStatistics().QueryBadRequest++;
			listener.searchFailed(query.getGUID());
		} else if (message.getType() == QueryReply.TYPE_NOT_FOUND) {
			if (Constants.TESTS)
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
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

	public void timerTriggered(Object id) {
		Iterator it = mQueries.values().iterator();
		Vector guids = new Vector();
		long currentTime = System.currentTimeMillis();
		RangeQueryRoutingRequest request;

		while(it.hasNext()) {
			request = (RangeQueryRoutingRequest)it.next();
			if (request.getStartTime()+Constants.QUERY_PROCESSING_TIMEOUT < currentTime)
				guids.add(request.getQuery().getGUID());
		}

		if (!guids.isEmpty()) {
			it = guids.iterator();
			while(it.hasNext()) {
				request = (RangeQueryRoutingRequest)mQueries.remove(it.next());
				if (request != null) {
					Router.LOGGER.fine("["+request.getQuery().getGUID()+"]: Removing range query request reference.");
					request.getSearchListener().searchFinished(request.getQuery().getGUID());
				}
			}
		}
	}
}
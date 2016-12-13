/**
 * $Id: ExactQueryRoutingStrategy.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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

import p2p.storage.Query;
import p2p.storage.events.SearchListener;
import pgrid.PGridHost;
import pgrid.QueryReply;
import pgrid.Constants;
import pgrid.util.TimerManager;
import pgrid.util.Utils;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.QueryMessage;
import pgrid.network.protocol.QueryReplyMessage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * The Query Router routes query messages in the network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ExactQueryRoutingStrategy extends pgrid.network.router.RoutingStrategy implements QueryReplyWaiter, pgrid.util.TimerListener {

	/**
	 * The timer manager
	 */
	protected pgrid.util.TimerManager timerManager = TimerManager.sharedInstance();

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME ="Simple query routing";

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
	public ExactQueryRoutingStrategy(Router router) {
		super(router);
		timerManager.register(1000*60*1, null, this, true);
	}

	/**
	 * Routes a query to the responsible peer.
	 *
	 * @param routingRequest the query route request.
	 */
	public void route(Request routingRequest) {
		if (!(routingRequest instanceof QueryRoutingRequest)) {
			// TODO throw an exception if needed
			return;
		}

		QueryRoutingRequest request = (QueryRoutingRequest)routingRequest;

		// add query to the already seen queries list
		request.startProcessing();
		Query query = request.getQuery();
		mQueries.put(request.getQuery().getGUID(), request);

		QueryMessage msg = null;
		if (query instanceof QueryMessage) {
			// the query is already a message => simply forward it
			String compath = Utils.commonPrefix(query.getKeyRange().toString(), mPGridP2P.getLocalPath());
			msg = (QueryMessage)query;
			msg.setIndex(compath.length());
			msg.incHops();
		} else {
			// create message
			String compath = Utils.commonPrefix(query.getKeyRange().toString(), mPGridP2P.getLocalPath());
			msg = new QueryMessage(query.getGUID(), query.getType(), query.getLowerBound(), query.getKeyRange().getMin(), compath.length(), 0, (PGridHost)mPGridP2P.getLocalPeer(), 0, null);
		}


		// send query message
		boolean sent = getRouter().route(query.getKeyRange().getMin(), msg, this);
		if (!sent) {
			request.getSearchListener().searchFailed(query.getGUID());
		}
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newQueryReply(QueryReplyMessage message) {
		QueryRoutingRequest request = (QueryRoutingRequest)mQueries.remove(message.getGUID());
		if (request == null) {
			Router.LOGGER.fine("Unknown query reply message (" + message.getGUID() + ").");
			return;
		}

		Query query = request.getQuery();
		SearchListener listener = request.getSearchListener();

		Router.LOGGER.fine("Response for remote search (" + query.getGUID().toString() + ") with key '" + query.getKeyRange() + "' for '" + query.getLowerBound() + "' from " + message.getHeader().getHost().toHostString() + " received with " + message.getHits() + " hit(s).");
		// something message
		if (message.getType() == QueryReply.TYPE_OK) {
			// local request => add results
			Router.LOGGER.fine("Return " + message.getHits() + " file(s) for the search (" + query.getGUID().toString() + ") with key '" + query.getKeyRange() + "' for '" + query.getLowerBound() + "' returned from host " + message.getHeader().getHost().toHostString() + ".");
			listener.newSearchResult(query.getGUID(), message.getResultSet());
			listener.searchFinished(query.getGUID());
		} else if (message.getType() == QueryReply.TYPE_BAD_REQUEST) {
			PGridP2P.sharedInstance().getStatistics().QueryBadRequest++;
			listener.searchFailed(query.getGUID());
		} else if (message.getType() == QueryReply.TYPE_NOT_FOUND) {
			PGridP2P.sharedInstance().getStatistics().QueryNotFound++;
			Router.LOGGER.fine("Return NOT_FOUND for the search (" + query.getGUID().toString() + ") with key '" + query.getKeyRange() + "' for '" + query.getLowerBound() + "' returned from host " + message.getHeader().getHost().toHostString() + ".");
			listener.noResultsFound(query.getGUID());
			listener.searchFinished(query.getGUID());
		}

	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		QueryRoutingRequest request = (QueryRoutingRequest)mQueries.get(message.getGUID());
		if (request == null) return;
		
		SearchListener listener = request.getSearchListener();
		short status = getRouter().checkAcknowledgment(message);
		if (status == Router.ROUTE_OK) {
			listener.searchStarted(message.getGUID(), message.getMessage());
		} else if (status == Router.ROUTE_FAILED) {
			listener.searchFailed(message.getGUID());
		}
	}

	/**
	 * Timer triggered callback method
	 *
	 * @param id
	 */
	public void timerTriggered(Object id) {
		Iterator it = mQueries.values().iterator();
		Vector guids = new Vector();
		long currentTime = System.currentTimeMillis();
		QueryRoutingRequest request;

		while(it.hasNext()) {
			request = (QueryRoutingRequest)it.next();
			if (request.getStartTime()+Constants.QUERY_PROCESSING_TIMEOUT < currentTime)
				guids.add(request.getQuery().getGUID());
		}

		if (!guids.isEmpty()) {
			it = guids.iterator();
			while(it.hasNext()) {
				request = (QueryRoutingRequest)mQueries.remove(it.next());
				if (request != null)
					Router.LOGGER.finest("["+request.getQuery().getGUID()+"]: Removing  query request reference.");
			}
		}
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

}
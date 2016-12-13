/**
 * $Id: Router.java,v 1.3 2005/11/15 09:57:52 john Exp $
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

import p2p.basic.Key;
import p2p.basic.Message;
import p2p.basic.P2P;
import p2p.basic.KeyRange;
import p2p.basic.events.NoRouteToKeyException;
import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.PGridKey;
import pgrid.core.RoutingTable;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.PGridMessage;
import pgrid.network.protocol.RoutableMessage;
import pgrid.util.Utils;
import pgrid.util.logging.LogFormatter;
import pgrid.util.TimerManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Router routes messages in the network.
 *
 * To route a message, it should first be encapsulated into a request which contains
 * the routing strategy.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Router extends pgrid.util.WorkerThread implements AcknowledgmentWaiter, pgrid.util.TimerListener {

	/**
	 * The routing failed.
	 */
	public static final short ROUTE_FAILED = 2;

	/**
	 * The routing was successful.
	 */
	public static final short ROUTE_OK = 0;

	/**
	 * The routing has to be redone.
	 */
	public static final short ROUTE_PENDING = 1;

	/**
	 * The PGrid.Router logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger("PGrid.Router");

	/**
	 * The time to wait for reply messages.
	 */
	public static final int REPLY_TIMEOUT = 60000 * 1; // 1 min.

	/**
	 * The P2P facility.
	 */
	protected P2P mP2P = null;

	/**
	 * The message manager.
	 */
	protected MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The messages requests to route.
	 */
	private final Vector mMessageToRoute = new Vector();

	/**
	 * The timer manager
	 */
	protected pgrid.util.TimerManager timerManager = TimerManager.sharedInstance();

	/**
	 * Route Handler
	 */
	private Hashtable mRoutingStrategies = new Hashtable();

	private Hashtable mRouteAttempts = new Hashtable();

	/**
	 * The worker thread
	 */
	private Thread mThread = null;

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null);
	}


	/**
	 * Creates a new router.
	 *
	 */
	public Router() {
		super();

		// Register strategies
		registerStrategy(new DistributionRoutingStrategy(this));
		registerStrategy(new pgrid.network.router.ReplicasRoutingStrategy(this));

		// simple queries
		registerStrategy(new pgrid.network.router.ExactQueryRoutingStrategy(this));

		// range queries
		registerStrategy(new RQShowerRoutingStrategy(this));
		registerStrategy(new LookupRoutingStrategy(this));
		registerStrategy(new pgrid.network.router.RQMinMaxRoutingStrategy(this));
		
		// Generic messages
		registerStrategy(new GenericRoutingStrategy(this));

		timerManager.register(1000*60*1, null, this, true);
	}

	/**
	 * Creates a new router.
	 */
	protected Router(boolean init) {
	}

	/**
	 * Register a new routing strategy.
	 * @param strategy to be registreted
	 */
	public void registerStrategy(pgrid.network.router.RoutingStrategy strategy) {
		mRoutingStrategies.put(strategy.getStrategyName(), strategy);
	}

	/**
	 * Returns a reference to the Router logger. This logger can be used in
	 * strategies or requests object
	 * @return a reference to the router logger.
	 */
	public static Logger getLogger() {
		return LOGGER;
	}


	/**
	 * Check new response was received.
	 *
	 * @param message the response message.
	 */
	public short checkAcknowledgment(ACKMessage message) {
		RouteAttempt attempt = (RouteAttempt)mRouteAttempts.remove(message.getGUID());
		if (message.getCode() == ACKMessage.CODE_OK) {
			// the message was routed correctly
			return ROUTE_OK;
		} else if (message.getCode() == ACKMessage.CODE_MSG_ALREADY_SEEN) {
			// message has been seen by host already => try another host if available, otherwise routing failed
			if (attempt.getIterator().hasNext()) {
				// try another host
				route(attempt);
			} else {
				return ROUTE_FAILED;
			}
		} else if (message.getCode() == ACKMessage.CODE_WRONG_ROUTE) {
			// the message was forwarded to a wrong host => repair the routing table and try another host if available, otherwise routing failed
			//@todo repair routing table
			if (attempt.getIterator().hasNext()) {
				// try another host
				route(attempt);
			} else {
				return ROUTE_FAILED;
			}
		} else if (message.getCode() == ACKMessage.CODE_CANNOT_ROUTE) {
			// the message cannot be routed => return the ACK for further details
			if (attempt.getIterator().hasNext()) {
				// try another host
				route(attempt);
			} else {
				return ROUTE_FAILED;
			}
		}
		return ROUTE_PENDING;
	}

	/**
	 * A new response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		checkAcknowledgment(message);
	}

	/**
	 * Routes a message to a peer responsible for the given key.
	 *
	 * @param key the key.
	 * @param msg the message.
	 */
	public boolean route(Key key, Message msg, AcknowledgmentWaiter waiter) {
		// determine the responsible routing table level
		String compath = pgrid.util.Utils.commonPrefix(key.toString(), mPGridP2P.getLocalPath());
		int level = compath.length();

		return routeAtLevel(msg, level, waiter);
	}

	/**
	 * Route a routable message to a range of host
	 * @param keyRange the range
	 * @param msg the message
	 * @param treeFlip true iff the message could be sent to the other side of the tree
	 * @param waiter
	 */
	protected void route(KeyRange keyRange, RoutableMessage msg, boolean treeFlip, AcknowledgmentWaiter waiter) {
		String localpath = mPGridP2P.getLocalPath();
		int maxLevel;
		int size = mPGridP2P.getRoutingTable().getLevelCount();
		String tmpPath = "";
		char[] charLocalPath = localpath.toCharArray();
		char[] charLower = keyRange.getMin().toString().toCharArray();
		char[] charHigher = keyRange.getMax().toString().toCharArray();
		int qIndex = msg.getIndex();
		String info ="";

		int min = Utils.commonPrefix(keyRange.getMin().toString(), localpath).length();
		int max = Utils.commonPrefix(keyRange.getMax().toString(), localpath).length();

		maxLevel = Math.min(Math.max(min, max), localpath.length()-1);

		Router.LOGGER.fine("Try to route message ("+msg.getGUID().toString()+").");
		for (int level = maxLevel; level >= qIndex; level--) {
			// don't send a range query when not necessary
			if ((!treeFlip && level == 0) || level >= size) continue;

			Router.LOGGER.fine("Try to contact a host at level " + level + ".");

			// Standard case
			if (level != 0) {
				tmpPath = localpath.substring(0, level) + ((charLocalPath[level] == '1') ? '0' : '1');
				info = "Starting parallel remote search to handle subtree '"+tmpPath+"'.";
			}
			// side change
			else if (charLocalPath[0] != charLower[0] || charLocalPath[0] != charHigher[0]) {
				info = "Sending range message to the other side of the trie.";
				tmpPath = (charLocalPath[0] == '1') ? "0" : "1";
			} else {
				continue;
			}

			// send the message
			if (keyRange.withinRange(new PGridKey(tmpPath))) {
				msg.setIndex(level+1);
				Router.LOGGER.fine(info);
				boolean sent = routeAtLevel((PGridMessage)msg, level, waiter);
				if (!sent) {
					//request.getSearchListener().searchFailed(query.getGUID());
				}
			}
		}
	}
	/**
	 * Route a message to a random host at the given level
	 * @param msg   message to route
	 * @param level to choose the host from
	 * @param waiter
	 * @return true if the attemp has succeeded
	 */
	protected boolean routeAtLevel(Message msg, int level, AcknowledgmentWaiter waiter) {
		RoutingTable rTable = mPGridP2P.getRoutingTable();

		// determine the responsible routing table level
		PGridHost[] hosts = rTable.getLevel(level);

		// if no hosts are available in this level => throw exception
		if ((hosts == null) || (hosts.length == 0))
			throw new NoRouteToKeyException();

		// create and shuffle hosts list and iterate throw it
		List list = Arrays.asList(hosts);
		Collections.shuffle(list);
		Iterator it = list.iterator();

		RouteAttempt attempt = new RouteAttempt(msg, list, it, waiter);

		// send query message
		return route(attempt);
	}


	protected boolean route(RouteAttempt attempt) {
		Message msg = attempt.getMessage();
		MessageWaiter waiter = attempt.getWaiter();
		Iterator it = attempt.getIterator();
		mRouteAttempts.put(msg.getGUID(), attempt);

		while (it.hasNext()) {
			PGridHost host;
			try {
				host = (PGridHost)it.next();
			} catch (NoSuchElementException e) {
				return false;
			}
			LOGGER.fine("Send message (" + msg.getGUID().toString() + ") to " + host.toHostString() + ".");
			boolean sent = mMsgMgr.sendMessage(host, (PGridMessage)msg, waiter);
			if (!sent) {
				if (it.hasNext()) {
					continue;
				} else {
					LOGGER.fine("not sent and no more hosts for message (" + msg.getGUID().toString() + ").");
					return false;
				}
			}
			break;
		}
		return true;
	}

	/**
	 * Routes a message encapsulated into a request to the responsible peer.
	 * @param request
	 */
	public void route(Request request) {

		mMessageToRoute.add(request);
		broadcast();
	}

	protected void work() throws Exception {
		// Queries
		Iterator requests;
		synchronized (mMessageToRoute) {
			requests = ((Vector)mMessageToRoute.clone()).iterator();
			mMessageToRoute.clear();
		}
		if (requests != null) {
			while (requests.hasNext()) {
				Request request =  (Request)requests.next();
				RoutingStrategy strategy =
					(RoutingStrategy)mRoutingStrategies.get(request.getRoutingStrategyName());


				if (strategy != null) {
					strategy.route(request);
				}

			}
		}

	}

	protected void handleError(Throwable t) {
		if (t instanceof InterruptedException) {
			LOGGER.finer("Router interupted.");
		} else {
			LOGGER.log(Level.WARNING, "Error in Router thread", t);
		}

	}

	protected void prepareWorker() throws Exception {
		mThread = Thread.currentThread();
		LOGGER.config("Router thread prepared.");
	}

	protected void releaseWorker() throws Exception {
		LOGGER.config("Router thread released.");
	}

	protected boolean isCondition() {
		return !mMessageToRoute.isEmpty();
	}

	/**
	 * Shutdown
	 */
	public void shutdown() {
		mThread.interrupt();
	}

	/**
	 * Timer triggered callback method. This method will remove all routing attempts older then a
	 * certain amount of time.
	 *
	 * @param id
	 */
	public void timerTriggered(Object id) {
		Iterator it = mRouteAttempts.values().iterator();
		Vector guids = new Vector();
		long currentTime = System.currentTimeMillis();
		RouteAttempt request;

		while(it.hasNext()) {
			request = (RouteAttempt)it.next();
			if (request.getStartTime()+Constants.QUERY_PROCESSING_TIMEOUT < currentTime)
				guids.add(request.getMessage().getGUID());
		}

		if (!guids.isEmpty()) {
			it = guids.iterator();
			while(it.hasNext()) {
				request = (RouteAttempt)mRouteAttempts.remove(it.next());
				if (request != null) {
					Router.LOGGER.finest("["+request.getMessage().getGUID()+"]: Removing routing attemps request reference.");
				}
			}
		}
	}

}
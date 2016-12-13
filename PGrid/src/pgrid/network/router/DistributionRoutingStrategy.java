/**
 * $Id: DistributionRoutingStrategy.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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

import pgrid.core.storage.DistributionListener;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.router.AcknowledgmentWaiter;
import pgrid.network.router.Router;
import pgrid.network.router.Request;
import pgrid.network.router.DistributionRequest;
import pgrid.Constants;
import pgrid.util.TimerManager;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

/**
 * The Distribution Router routes insert/update/delete messages in the network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class DistributionRoutingStrategy extends pgrid.network.router.RoutingStrategy implements AcknowledgmentWaiter, pgrid.util.TimerListener {

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME = "Distribution routing";

	/**
	 * The list of request being processed.
	 */
	private Hashtable mRequests = new Hashtable();

	/**
	 * The timer manager
	 */
	protected TimerManager timerManager = TimerManager.sharedInstance();


	/**
	 * default constructor
	 *
	 * @param router
	 */
	public DistributionRoutingStrategy(Router router) {
		super(router);
		timerManager.register(Constants.DISTRIBUTION_PROCESSING_TIMEOUT, null, this, true);
	}


	/**
	 * Routes an insert/update/delete request to the responsible peers.
	 *
	 * @param routingRequest the request.
	 */
	public void route(Request routingRequest) {
		if (!(routingRequest instanceof DistributionRequest)) {
			// TODO throw an exception if needed
			return;
		}

		DistributionRequest request = (DistributionRequest)routingRequest;

		request.startProcessing();

		boolean sent = getRouter().route(request.getKey(), request.getMessage(), this);
		mRequests.put(request.getMessage().getGUID(), request);
		if (sent) {
			// INFO: requests should be added here only but sent is sometimes false though a message was sent successfully
			// mRequests.put(request.getMessage().getGUID(), request);
		} else {
			request.getDistributionListener().distributionFailed(request.getMessage().getGUID());
			mRequests.remove(request.getMessage().getGUID());
		}
	}

	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		DistributionRequest request = (DistributionRequest)mRequests.remove(message.getGUID());
		if (request == null) {
			Router.getLogger().info("No Distribution request found for ACK message.");
			return;
		}
		DistributionListener listener = request.getDistributionListener();
		short status = getRouter().checkAcknowledgment(message);
		if (status == Router.ROUTE_OK) {
			listener.distributionSuccess(message.getGUID());
		} else if (status == Router.ROUTE_FAILED) {
			listener.distributionFailed(message.getGUID());
		}
		mRequests.remove(message.getGUID());
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

	/**
	 * Timer triggered callback method. This method will remove all routing attempts older then a
	 * certain amount of time.
	 *
	 * @param id
	 */
	public void timerTriggered(Object id) {
		Iterator it = mRequests.values().iterator();
		Vector guids = new Vector();
		long currentTime = System.currentTimeMillis();
		DistributionRequest request;

		while(it.hasNext()) {
			request = (DistributionRequest)it.next();
			if (request.getStartTime()+Constants.DISTRIBUTION_PROCESSING_TIMEOUT < currentTime)
				guids.add(request.getMessage().getGUID());
		}

		if (!guids.isEmpty()) {
			it = guids.iterator();
			while(it.hasNext()) {
				request = (DistributionRequest)mRequests.remove(it.next());
				if (request != null) {
					Router.getLogger().fine("["+request.getMessage().getGUID()+"]: Removing distribution request reference.");
					request.getDistributionListener().distributionFailed(request.getMessage().getGUID());
				}
			}
		}
	}
}
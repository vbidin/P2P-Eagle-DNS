/**
 * $Id: GenericRoutingStrategy.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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

import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.GenericMessage;
import pgrid.network.router.AcknowledgmentWaiter;
import pgrid.network.router.Router;
import pgrid.network.router.Request;
import pgrid.network.router.GenericRoutingRequest;

/**
 * The Query Router routes query messages in the network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class GenericRoutingStrategy extends pgrid.network.router.RoutingStrategy implements AcknowledgmentWaiter {

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME ="Generic routing";

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * default constructor
	 *
	 * @param router
	 */
	public GenericRoutingStrategy(Router router) {
		super(router);
	}

	/**
	 * Routes a query to the responsible peer.
	 *
	 * @param routingRequest the query route request.
	 */
	public void route(Request routingRequest) {
		if (!(routingRequest instanceof GenericRoutingRequest)) {
			// TODO throw an exception if needed
			return;
		}

		GenericRoutingRequest request = (GenericRoutingRequest)routingRequest;

		// add query to the already seen queries list
		request.startProcessing();
		GenericMessage msg = request.getMessage();

		// send message
		getRouter().route(msg.getKeyRange(), msg, request.isLocallyStarted(), this);
	}


	/**
	 * A new ACK response was received.
	 *
	 * @param message the response message.
	 */
	public void newAcknowledgment(ACKMessage message) {
		getRouter().checkAcknowledgment(message);
	}

	public String getStrategyName() {
		return STRATEGY_NAME;
	}

}
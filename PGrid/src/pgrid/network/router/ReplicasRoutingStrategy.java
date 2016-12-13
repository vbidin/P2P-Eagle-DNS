/**
 * $Id: ReplicasRoutingStrategy.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.PGridMessage;
import pgrid.network.MessageManager;
import pgrid.network.router.Router;
import pgrid.network.router.Request;
import pgrid.network.router.ReplicasRequest;

import java.util.Iterator;
import java.util.Vector;

/**
 * The Replicas Router routes messages to all replicas.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ReplicasRoutingStrategy extends pgrid.network.router.RoutingStrategy {

	/**
	 * Stategy name
	 */
	public static String STRATEGY_NAME = "Replicas routing";

	/**
	 * The P2P facility.
	 */
	protected PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The message manager.
	 */
	protected MessageManager mMsgMgr = MessageManager.sharedInstance();


  /**
   * Creates a new router.
   *
   */
	public ReplicasRoutingStrategy(Router router) {
		super(router);
  }

  /**
   * Routes a message to all replicas.
   *
   * @param routingRequest the request.
   */
  public void route(Request routingRequest) {
	  	if (!(routingRequest instanceof ReplicasRequest)) {
				// TODO throw an exception if needed
					return;
				}

		ReplicasRequest request = (ReplicasRequest)routingRequest;


		// compose list of replicas the message will be sent to
		Vector replicasToSend = new Vector();
		replicasToSend.addAll(mPGridP2P.getRoutingTable().getReplicaVector());
		if (request.getExclReplicas() != null) {
			replicasToSend.removeAll(request.getExclReplicas());
		}

		for (Iterator it = replicasToSend.iterator(); it.hasNext();) {
			PGridHost host = (PGridHost)it.next();
			PGridMessage msg = request.getMessage();
			getRouter().LOGGER.fine("Send message (" + msg.getGUID().toString() + ") to replica " + host.toHostString() + ".");
			mMsgMgr.sendMessage(host, msg);
		}
  }

	public String getStrategyName() {
		return STRATEGY_NAME;
	}


}
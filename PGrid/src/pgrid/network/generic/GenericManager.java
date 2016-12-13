/**
 * $Id: GenericManager.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

package pgrid.network.generic;

import p2p.basic.events.NoRouteToKeyException;
import p2p.storage.events.NoSuchTypeException;
import pgrid.*;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.GenericMessage;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.router.RoutingRequestFactory;

/**
 * This class is the manager for all generic message.
 *
 * @author <a href="mailto:Renault John <Renault.John@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public class GenericManager {
	/**
	 * The P-Grid P2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = null;

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate the search manager directly will get an error at
	 * compile-time.
	 */
	public GenericManager() {
	}

	/**
	 * Initializes the search manager.
	 */
	public void init() {
		mPGridP2P = PGridP2P.sharedInstance();
		mMsgMgr = MessageManager.sharedInstance();
	}

	/**
	 * Invoked when a new query message was received.
	 *
	 * @param msg the generic message.
	 * @param remoteHost origin of the message
	 */
	public void remoteGenericMessage(GenericMessage msg, PGridHost remoteHost) {
		mMsgMgr.sendMessage(msg.getHeader().getHost(), new ACKMessage(msg.getGUID(), ACKMessage.CODE_OK, ""));
		if (mPGridP2P.isLocalPeerResponsible(msg.getKeyRange())) {
			mPGridP2P.newGenericMessage(msg, remoteHost);
		}
		mPGridP2P.getRouter().route(RoutingRequestFactory.createGenericRoutingRequest(msg, false));
	}

	/**
	 * Search the network for matching items. Implemented as
	 * an asynchronous operation, because search might take
	 * some time. Callback is notified for each new result.
	 *
	 * @param msg    the message to route
	 * @throws p2p.storage.events.NoSuchTypeException if the provided Type is unknown.
	 * @throws p2p.basic.events.NoRouteToKeyException if the query cannot be routed to a responsible peer.
	 */
	public void newGenericMessage(GenericMessage msg) throws NoSuchTypeException, NoRouteToKeyException {
		if (mPGridP2P.isLocalPeerResponsible(msg.getKeyRange())) {
			mPGridP2P.newGenericMessage(msg, mPGridP2P.getLocalPeer());
		}
		mPGridP2P.getRouter().route(RoutingRequestFactory.createGenericRoutingRequest(msg, true));
	}



}
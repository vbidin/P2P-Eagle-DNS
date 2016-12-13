/**
 * $Id: LookupManager.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import p2p.basic.P2P;
import p2p.basic.Peer;
import p2p.storage.events.SearchListener;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.PeerLookupMessage;
import pgrid.network.router.RoutingRequestFactory;
import pgrid.network.router.Request;
import pgrid.util.logging.LogFormatter;

import java.util.logging.Logger;

/**
 * This class will look for a particular host in the network. This is a pgrid.helper class
 * that can given a path and some criteria retrieve a host.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public class LookupManager {

	/**
	 * The PGrid.Router logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger("PGrid.Lookup");

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
	 * Remote request handler
	 */
	protected RemoteLookupHandler mRemoteLookupHandler = new RemoteLookupHandler(mPGridP2P);

	/**
	 * Synchronous request handler
	 */
	protected SynchronousLookupHandler mSynchronousLookupHandler = new SynchronousLookupHandler(mPGridP2P);


	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null);
	}

	/**
	 * Continue a lookup started remotly
	 * @param msg the lookup message
	 */
	public void remotePeerLookup(PeerLookupMessage msg) {
		mRemoteLookupHandler.register(msg);
	}

	/**
	 * Start a peer lookup mechanism to retrieve a peer responsible for the
	 * given path
	 * @param msg the lookup message
	 * @param listener an object to notify when results arrive
	 */
	public void peerLookup(PeerLookupMessage msg, SearchListener listener) {

		// try to route the message further
		Request request = RoutingRequestFactory.createLookupRoutingRequest(msg, listener);
		mPGridP2P.getRouter().route(request);
	}

	/**
	 * Start a peer lookup mechanism to retrieve a peer responsible for the
	 * given path. This method is synchronous.
	 * @param msg the lookup message
	 */
	public Peer synchronousPeerLookup(PeerLookupMessage msg, long timeout) {
		return mSynchronousLookupHandler.lookup(msg, timeout);
	}

}

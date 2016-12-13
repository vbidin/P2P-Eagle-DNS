/**
 * $Id: MessageManager.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

package pgrid.network;

import p2p.basic.GUID;
import pgrid.*;
import pgrid.core.maintenance.identity.IdentityManager;
import pgrid.core.search.SearchManager;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.*;
import pgrid.network.router.AcknowledgmentWaiter;
import pgrid.network.router.MessageWaiter;
import pgrid.network.router.QueryReplyWaiter;
import pgrid.network.router.PeerLookupReplyWaiter;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents the message manager for all sent and received
 * messages.
 * This class implements the <code>Singleton</code> pattern as defined by
 * Gamma et.al. As there could only exist one instance of this class, other
 * clients must use the <code>sharedInstance</code> function to use this class.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */

//FIXME: There was a synchro bug... Implement a more clever wating list
public class MessageManager implements MessageListener {

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final MessageManager SHARED_INSTANCE = new MessageManager();

	/**
	 * The Connection Manager.
	 */
	private ConnectionManager mConnMgr = null;

	/**
	 * The Identity Manager.
	 */
	private IdentityManager mIdentMgr = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The received challenge response messages.
	 */
	private Hashtable mChallengeResponses = new Hashtable();

	/**
	 * The received response messages.
	 */
	private Hashtable mResponses = new Hashtable();

	/**
	 * The Search Manager.
	 */
	private SearchManager mSearchManager = null;

	/**
	 * The threads waiting for a response.
	 */
	private Hashtable mWaiters = new Hashtable();

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridP2P directly will get an error at compile-time.
	 */
	protected MessageManager() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static MessageManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Initializes the Message Manager.
	 */
	public void init() {
		mPGridP2P = PGridP2P.sharedInstance();
		mConnMgr = ConnectionManager.sharedInstance();
	mSearchManager = SearchManager.sharedInstance();
		mIdentMgr = IdentityManager.sharedInstance();
	}

	/**
	 * Invoked when a new bootstrap message was received.
	 *
	 * @param bootstrap the bootstrap message.
	 */
	public void newMessage(BootstrapMessage bootstrap) {
		mPGridP2P.getMaintenanceManager().newBootstrapRequest(bootstrap);
	}

	/**
	 * Invoked when a new bootstrap reply message was received.
	 *
	 * @param bootstrapReply the bootstrap reply message.
	 */
	public void newMessage(BootstrapReplyMessage bootstrapReply) {
		mPGridP2P.getMaintenanceManager().newBootstrapReply(bootstrapReply);
	}

	/**
	 * Invoked when a new exchange invitation message was received.
	 *
	 * @param exchangeInvitation the exchange message.
	 */
	public void newMessage(ExchangeInvitationMessage exchangeInvitation) {
		mPGridP2P.getMaintenanceManager().newExchangeInvitation(exchangeInvitation);
	}

	/**
	 * Invoked when a new exchange message was received.
	 *
	 * @param exchange the exchange message.
	 */
	public void newMessage(ExchangeMessage exchange) {
		mPGridP2P.getMaintenanceManager().newExchangeRequest(exchange);
	}

	/**
	 * Invoked when a new exchange reply message was received.
	 *
	 * @param exchangeReply the exchange reply message.
	 */
	public void newMessage(ExchangeReplyMessage exchangeReply) {
		mPGridP2P.getMaintenanceManager().newExchangeReply(exchangeReply);
	}

	/**
	 * Invoked when a new range query message was received.
	 *
	 * @param query the range query message.
	 */
	public void newMessage(RangeQueryMessage query) {
		mSearchManager.remoteSearch(query, query.getHeader().getHost());
	}

	/**
	 * Invoked when a new query message was received.
	 *
	 * @param query the query message.
	 */
	public void newMessage(QueryMessage query) {
		mSearchManager.remoteSearch(query, query.getHeader().getHost());
	}

	/**
	 * Invoked when a new query reply message was received.
	 *
	 * @param queryReply the query reply message.
	 */
	synchronized public void newMessage(QueryReplyMessage queryReply) {
		QueryReplyWaiter waiter = (QueryReplyWaiter)mWaiters.get(queryReply.getGUID());
		waiter.newQueryReply(queryReply);
	}

	/**
	 * @see pgrid.network.MessageListener
	 */
	public void newMessage(DataModifierMessage insert) {
		mPGridP2P.getStorageManager().remoteDataModification(insert);
	}

	/**
	 * Invoked when an info message was received.
	 *
	 * @param generic the generic message.
	 */
	public void newMessage(GenericMessage generic) {
		mPGridP2P.getGenericManager().remoteGenericMessage(generic, generic.getHeader().getHost());
	}

	/**
	 * Invoked when a acknowledgement message was received.
	 *
	 * @param acknowledgement the query reply message.
	 */
	public void newMessage(ACKMessage acknowledgement) {
		AcknowledgmentWaiter waiter = (AcknowledgmentWaiter)mWaiters.get(acknowledgement.getGUID());
		if (waiter == null)
			Constants.LOGGER.info("No waiter found for acknowledgment!");
		else
			waiter.newAcknowledgment(acknowledgement);
	}

	/**
	 * Invoked when a new challenge message was received.
	 *
	 * @param challenge the challenge message.
	 */
	public void newMessage(ChallengeMessage challenge) {
		mPGridP2P.newChallenge(challenge.getHeader().getHost(), challenge);
	}

	/**
	 * Invoked when a challenge response message was received.
	 *
	 * @param challengeReply the challenge reply message.
	 */
	synchronized public void newMessage(ChallengeReplyMessage challengeReply) {
		// todo: port the challenge mechanism to the new architecture
		Thread searcher = null;
		if (challengeReply.getGUID() != null) {
			searcher = getWaiter(challengeReply.getGUID());
		}
		Collection col = (Collection)mChallengeResponses.remove(challengeReply.getGUID());
		if (col == null)
			col = new Vector();
		col.add(challengeReply);
		mChallengeResponses.put(challengeReply.getGUID(), col);
		//if (searcher != null) {
		notifyAll();
		//}
	}

	/**
	 * Invoked when a new search path message was received.
	 *
	 * @param searchPath the query message.
	 */
	public void newMessage(SearchPathMessage searchPath) {
		mPGridP2P.newSearchPath(searchPath.getHeader().getHost(), searchPath);
	}

	/**
	 * Invoked when a new search path message was received.
	 *
	 * @param peerLookup the query message.
	 */
	public void newMessage(PeerLookupMessage peerLookup) {
		//mPGridP2P.newPeerLookup(peerLookup.getHeader().getPeer(), peerLookup);
		mPGridP2P.getLookupManager().remotePeerLookup(peerLookup);
	}

	/**
	 * Invoked when a new lookup peer reply message was received.
	 *
	 * @param peerLookup the query message.
	 */
	synchronized public void newMessage(PeerLookupReplyMessage peerLookup) {
		PeerLookupReplyWaiter waiter = (PeerLookupReplyWaiter)mWaiters.get(peerLookup.getGUID());
		if (waiter != null)
			waiter.newPeerLookupReply(peerLookup);
	}

	/**
	 * Invoked when a new search path reply message was received.
	 *
	 * @param searchPathReply the query reply message.
	 */
	public void newMessage(SearchPathReplyMessage searchPathReply) {
		Thread pathSearcher = null;
		if (searchPathReply.getGUID() != null) {
			pathSearcher = getWaiter(searchPathReply.getGUID());
		}
		if (pathSearcher != null) {
			mResponses.put(searchPathReply.getGUID(), searchPathReply);
			synchronized (pathSearcher) {
				pathSearcher.notify();
			}
		}
	}

	/**
	 * Invoked when a new replicate message was received.
	 *
	 * @param replicate message.
	 */
	public void newMessage(ReplicateMessage replicate) {
		mPGridP2P.getMaintenanceManager().newReplicateRequest(replicate.getHeader().getHost(), replicate.getDataItems());
	}

	/**
	 * Sends the delivered message to the delivered host.
	 *
	 * @param host          the receiving host.
	 * @param msg           the message to send.
	 * @return <code>true</code> if the message was sent sucessfull, <code>false</code> otherwise.
	 */
	public boolean sendMessage(PGridHost host, PGridMessage msg) {
		return sendMessage(host, msg, null);
	}

	/**
	 * Sends the delivered message to the delivered host.
	 *
	 * @param host          the receiving host.
	 * @param msg           the message to send.
	 * @param waiter if the calling thread should be notified, if a response was received.
	 * @return <code>true</code> if the message was sent sucessfull, <code>false</code> otherwise.
	 */
	public boolean sendMessage(PGridHost host, PGridMessage msg, MessageWaiter waiter) {
		if ((host == null) || (msg == null))
			throw new NullPointerException();
		// add waiter
		if (waiter != null) {
			mWaiters.put(msg.getGUID(), waiter);
		}
		return mConnMgr.sendPGridMessage(host, msg);
	}

	/**
	 * Add the current thread to the waiting list for a specific message
	 * represented by its GUID
	 *
	 * @param guid Unique identifier representing the message
	 */
	synchronized public void addWaiter(GUID guid) {
		// each message type get a different waiting list
		Collection col = (Collection)mWaiters.get(guid);
		if (col == null) {
			col = new Vector();
			mWaiters.put(guid, col);
		}
		col.add(Thread.currentThread());
	}

	/**
	 * Return a waiting thread
	 *
	 * @param guid Unique identifier representing the message
	 * @return a waiting thread
	 */
	synchronized protected Thread getWaiter(GUID guid) {
		Thread searcher = null;
		Collection searchers = (Collection)mWaiters.get(guid);
		if (searchers != null)
			searcher = (Thread)searchers.iterator().next();

		return searcher;
	}

}
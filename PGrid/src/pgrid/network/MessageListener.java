/**
 * $Id: MessageListener.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import pgrid.network.protocol.*;

/**
 * The listener interface for receiving notification about new incoming
 * messages. Everytime a new message is received the
 * <code>statusChanged()</code> method would be used to inform all listeners.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface MessageListener {

	/**
	 * Invoked when a new bootstrap message was received.
	 *
	 * @param bootstrap the bootstrap message.
	 */
	public void newMessage(BootstrapMessage bootstrap);

	/**
	 * Invoked when a new bootstrap reply message was received.
	 *
	 * @param bootstrapReply the bootstrap reply message.
	 */
	public void newMessage(BootstrapReplyMessage bootstrapReply);

	/**
	 * Invoked when a new exchange invitation message was received.
	 *
	 * @param exchangeInvitation the exchange invitation message.
	 */
	public void newMessage(ExchangeInvitationMessage exchangeInvitation);

	/**
	 * Invoked when a new exchange message was received.
	 *
	 * @param exchange the exchange message.
	 */
	public void newMessage(ExchangeMessage exchange);

	/**
	 * Invoked when a new exchange reply message was received.
	 *
	 * @param exchange the exchange reply message.
	 */
	public void newMessage(ExchangeReplyMessage exchange);

	/**
	 * Invoked when a new range query message was received.
	 *
	 * @param query the query message.
	 */
	public void newMessage(RangeQueryMessage query);

	/**
	 * Invoked when a new query message was received.
	 *
	 * @param query the query message.
	 */
	public void newMessage(QueryMessage query);

	/**
	 * Invoked when a new query reply message was received.
	 *
	 * @param queryReply the query reply message.
	 */
	public void newMessage(QueryReplyMessage queryReply);

	/**
	 * Invoked when a new search path message was received.
	 *
	 * @param searchPath the search path message.
	 */
	public void newMessage(SearchPathMessage searchPath);

	/**
	 * Invoked when a new search path reply message was received.
	 *
	 * @param searchPathReply the search path reply message.
	 */
	public void newMessage(SearchPathReplyMessage searchPathReply);

	/**
	 * Invoked when an acknowledgement message was received.
	 *
	 * @param acknowledgement the acknowlegdement message.
	 */
	public void newMessage(ACKMessage acknowledgement);

	/**
	 * Invoked when a peer lookup message was received.
	 *
	 * @param peerLookup the acknowlegdement message.
	 */
	public void newMessage(PeerLookupMessage peerLookup);

	/**
	 * Invoked when a peer lookup reply message was received.
	 *
	 * @param peerLookupReply the acknowlegdement message.
	 */
	public void newMessage(PeerLookupReplyMessage peerLookupReply);

	/**
	 * Invoked when a new insert message was received.
	 *
	 * @param insert insert message.
	 */
	public void newMessage(DataModifierMessage insert);

	/**
	 * Invoked when a new generic message was received.
	 *
	 * @param msg message.
	 */
	public void newMessage(GenericMessage msg);

	/**
	 * Invoked when a new replicate message was received.
	 *
	 * @param replicate message.
	 */
	public void newMessage(ReplicateMessage replicate);

	/**
	 * Invoked when a new challenge message was received.
	 *
	 * @param challenge message.
	 */
	public void newMessage(ChallengeMessage challenge);

	/**
	 * Invoked when a new challenge reply message was received.
	 *
	 * @param replicate message.
	 */
	public void newMessage(ChallengeReplyMessage replicate);

}
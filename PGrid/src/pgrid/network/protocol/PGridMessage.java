/**
 * $Id: PGridMessage.java,v 1.3 2005/12/21 13:07:05 rschmidt Exp $
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

package pgrid.network.protocol;

import p2p.basic.GUID;
import p2p.basic.Message;

/**
 * This class represents a interface for all Gridella messages.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface PGridMessage extends Message {

	/**
	 * The Query acknowledgement descriptor.
	 */
	public static final int DESC_ACK = 11;

	/**
	 * The P-Grid Bootstrap message descriptor.
	 */
	public static final int DESC_BOOTSTRAP = 3;

	/**
	 * The P-Grid Bootstrap message descriptor.
	 */
	public static final int DESC_BOOTSTRAP_REPLY = 4;

	/**
	 * The string representation of the Challenge descriptor.
	 */
	public static final int DESC_CHALLENGE = 19;

	/**
	 * The string representation of the Challenge response descriptor.
	 */
	public static final int DESC_CHALLENGE_REPLY = 20;

	/**
	 * The Exchange descriptor.
	 */
	public static final int DESC_EXCHANGE = 7;

	/**
	 * The Exchange invitation descriptor.
	 */
	public static final int DESC_EXCHANGE_INVITATION = 6;

	/**
	 * The Exchange reply descriptor.
	 */
	public static final int DESC_EXCHANGE_REPLY = 8;

	/**
	 * The header descriptor.
	 */
	public static final int DESC_HEADER = 0;

	/**
	 * The Gridella Init message descriptor.
	 */
	public static final int DESC_INIT = 1;

	/**
	 * The Gridella Init response message descriptor.
	 */
	public static final int DESC_INIT_RESP = 2;

	/**
	 * The data modifier descriptor.
	 */
	public static final int DESC_MODIFIER = 9;

	/**
	 * The range query descriptor.
	 */
	public static final int DESC_PEERLOOKUP = 17;

	/**
	 * The range query descriptor.
	 */
	public static final int DESC_PEERLOOKUP_REPLY = 18;

	/**
	 * The Query descriptor.
	 */
	public static final int DESC_QUERY = 12;

	/**
	 * The Query Hit descriptor.
	 */
	public static final int DESC_QUERY_REPLY = 13;

	/**
	 * The range query descriptor.
	 */
	public static final int DESC_RANGE_QUERY = 14;

	/**
	 * The Replicate descriptor.
	 */
	public static final int DESC_REPLICATE = 5;

	/**
	 * The Search Path descriptor.
	 */
	public static final int DESC_SEARCH_PATH = 15;

	/**
	 * The Search Path Reply descriptor.
	 */
	public static final int DESC_SEARCH_PATH_REPLY = 16;

	/**
	 * The Update descriptor.
	 */
	public static final int DESC_GENERIC = 10;

	/**
	 * The number of different message types
	 */
	public static final int MESSAGE_TYPES = 23;

	/**
	 * The string representation of the acknowledgement descriptor.
	 */
	public static final String DESC_ACK_STRING = "Acknowledgement";

	/**
	 * The string representation of the P-Grid Bootstrap message descriptor.
	 */
	public static final String DESC_BOOTSTRAP_STRING = "Bootstrap";

	/**
	 * The string representation of the P-Grid Bootstrap message descriptor.
	 */
	public static final String DESC_BOOTSTRAP_REPLY_STRING = "Bootstrap Reply";

	/**
	 * The string representation of the Challenge descriptor.
	 */
	public static final String DESC_CHALLENGE_STRING = "Challenge";

	/**
	 * The string representation of the Challenge response descriptor.
	 */
	public static final String DESC_CHALLENGE_REPLY_STRING = "ChallengeReply";

	/**
	 * The string representation of the Delete descriptor.
	 */
	public static final String DESC_DELETE_STRING = "Delete";

	/**
	 * The string representation of the Exchange descriptor.
	 */
	public static final String DESC_EXCHANGE_STRING = "Exchange";

	/**
	 * The string representation of the Exchange invitation descriptor.
	 */
	public static final String DESC_EXCHANGE_INVITATION_STRING = "Exchange Invitation";

	/**
	 * The string representation of the Exchange descriptor.
	 */
	public static final String DESC_EXCHANGE_REPLY_STRING = "Exchange Reply";

	/**
	 * The string representation of the header descriptor.
	 */
	public static final String DESC_HEADER_STRING = "Header";

	/**
	 * The string representation of the Gridella Init message descriptor.
	 */
	public static final String DESC_INIT_STRING = "Init";

	/**
	 * The string representation of the Gridella Init response message descriptor.
	 */
	public static final String DESC_INIT_RESP_STRING = "Init Response";

	/**
	 * The string representation of the Insert descriptor.
	 */
	public static final String DESC_MODIFIER_STRING = "Modifier";

	/**
	 * The string representation of the Query descriptor.
	 */
	public static final String DESC_QUERY_STRING = "Query";

	/**
	 * The string representation of the Peer Query descriptor.
	 */
	public static final String DESC_PEERLOOKUP_STRING = "Peer Lookup";

	/**
	 * The string representation of the Peer Query Hit descriptor.
	 */
	public static final String DESC_PEERLOOKUP_REPLY_STRING = "Peer Lookup Reply";

	/**
	 * The string representation of the Query Hit descriptor.
	 */
	public static final String DESC_QUERY_REPLY_STRING = "Query Reply";

	/**
	 * The string representation of the Query descriptor.
	 */
	public static final String DESC_RANGE_QUERY_STRING = "Range Query";

	/**
	 * The string representation of the Replicate descriptor.
	 */
	public static final String DESC_REPLICATE_STRING = "Replicate";

	/**
	 * The string representation of the Search Path descriptor.
	 */
	public static final String DESC_SEARCH_PATH_STRING = "Search Path";

	/**
	 * The string representation of the Search Path Reply descriptor.
	 */
	public static final String DESC_SEARCH_PATH_REPLY_STRING = "Search Path Reply";

	/**
	 * The string representation of the Generic descriptor.
	 */
	public static final String DESC_GENERIC_STRING = "Generic";

	/**
	 * Returns the message as array of bytes.
	 *
	 * @return the message bytes.
	 */
	public byte[] getBytes();

	/**
	 * Returns a descriptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc();

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString();

	/**
	 * Returns the message GUID.
	 *
	 * @return the message GUID.
	 */
	public GUID getGUID();

	/**
	 * Returns the message header.
	 *
	 * @return the header.
	 */
	public MessageHeader getHeader();

	/**
	 * Returns the message length.
	 *
	 * @return the message length.
	 */
	public int getSize();

	/**
	 * Tests if the message is valid.
	 *
	 * @return <code>true</code> if valid, else <code>false</code>.
	 */
	public boolean isValid();

	/**
	 * Returns a string represantation of the message.
	 *
	 * @return a string represantation of the message.
	 */
	public String toXMLString();

}
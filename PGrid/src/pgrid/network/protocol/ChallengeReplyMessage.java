/**
 * $Id $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import p2p.basic.GUID;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.XMLizable;
import pgrid.util.LexicalDefaultHandler;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid challenge response message.
 *
 * @author Renault John
 * @version 1.0.0
 */
public class ChallengeReplyMessage extends LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_CHALLENGE_REPLY = "ChallengeReply";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_CHALLENGE_REPLY_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_CHALLENGE_REPLY_RESPONSE = "Response";

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The message id.
	 */
	protected GUID mGUID = null;

	/**
	 * Challenge string
	 */
	protected String mResponse = null;

	/**
	 * Constructor of a challenge - response scheme message
	 *
	 * @param msgHeader
	 */
	public ChallengeReplyMessage(MessageHeader msgHeader) {
		mHeader = msgHeader;
	}

	/**
	 * Constructor of an challenge - response scheme message
	 *
	 * @param guid the challenge identifier.
	 * @param challenge the challenge code.
	 */
	public ChallengeReplyMessage(GUID guid, String challenge) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mResponse = challenge;
	}

	/**
	 * Returns the exchange message as array of bytes.
	 *
	 * @return the message bytes.
	 */
	public byte[] getBytes() {
		byte[] bytes=null;

		try {
			bytes = toXMLString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return bytes;
	}

	/**
	 * Returns the response string
	 *
	 * @return the response string
	 */
	public String getResponse() {
		return mResponse;
	}

	/**
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_CHALLENGE_REPLY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_CHALLENGE_REPLY_STRING;
	}

	/**
	 * Returns the message GUID.
	 *
	 * @return the message GUID.
	 */
	public GUID getGUID() {
		return mGUID;
	}

	/**
	 * Get the message content.
	 *
	 * @return a binary representation of the message
	 */
	public byte[] getData() {
		return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Returns the message header.
	 *
	 * @return the header.
	 */
	public MessageHeader getHeader() {
		return mHeader;
	}

	/**
	 * Returns the message length.
	 *
	 * @return the message length.
	 */
	public int getSize() {
		return toXMLString().length();
	}

	/**
	 * Tests if this init response message is valid.
	 *
	 * @return <code>true</code> if valid.
	 */
	public boolean isValid() {
		if (mHeader == null) {
			return false;
		} else {
			if (!mHeader.isValid()) {
				return false;
			}
		}
		if (mResponse == null)
			return false;
		return true;
	}

	/**
	 * The Parser will invoke this method at the beginning of every element in the XML document; there will be a
	 * corresponding endElement event for every startElement event (even when the element is empty). All of the element's
	 * content will be reported, in order, before the corresponding endElement event.
	 *
	 * @param uri   the Namespace URI.
	 * @param lName the local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName the qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attrs the attributes attached to the element. If there are no attributes, it shall be an empty Attributes
	 *              object.
	 * @throws SAXException any SAX exception, possibly wrapping another exception.
	 */
	public synchronized void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
		if (qName.equals(XML_CHALLENGE_REPLY)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_CHALLENGE_REPLY_GUID));
			mResponse = attrs.getValue(XML_CHALLENGE_REPLY_RESPONSE);
		}
	}

	/**
	 * Returns a string represantation of this message.
	 *
	 * @return a string represantation of this message.
	 */
	public synchronized String toXMLString() {
		return toXMLString(XML_TAB, XML_NEW_LINE);
	}

	/**
	 * Returns the XML representation of this object.
	 *
	 * @param prefix  the XML prefix before each element in a new line.
	 * @param newLine the new line string.
	 * @return the XML string.
	 */
	public String toXMLString(String prefix, String newLine) {
		return prefix + XML_ELEMENT_OPEN + XML_CHALLENGE_REPLY + // {prefix}<ACK
				XML_SPACE + XML_CHALLENGE_REPLY_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_CHALLENGE_REPLY_RESPONSE + XML_ATTR_OPEN + mResponse + XML_ATTR_CLOSE + // _Challenge="Challenge"
				XML_ELEMENT_END_CLOSE + newLine; // />{newLine}
	}

}

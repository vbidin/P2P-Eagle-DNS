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
import pgrid.XMLizable;
import pgrid.interfaces.basic.PGridP2P;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid acknolegment message.
 *
 * @author Renault John
 * @version 1.0.0
 */
public class ACKMessage extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * The acknowledgment code "message already seen".
	 */
	public static final int CODE_MSG_ALREADY_SEEN = 400;

	/**
	 * The acknowledgment code "OK".
	 */
	public static final int CODE_OK = 200;

	/**
	 * The acknowledgment code "wrong route".
	 */
	public static final int CODE_WRONG_ROUTE = 401;

	/**
	 * The acknowledgment code "cannot route".
	 */
	public static final int CODE_CANNOT_ROUTE = 402;

	/**
	 * A part of the XML string.
	 */
	public static final String XML_ACK = "ACK";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_ACK_MESSAGE = "Message";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_ACK_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_ACK_CODE = "Code";

	/**
	 * Acknowledgment type
	 */
	protected int mCode = -1;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The message id.
	 */
	protected GUID mGUID = null;

	/**
	 * Additional information (optional).
	 */
	protected String mMsg = null;

	/**
	 * Constructor of an acknowlegdment
	 *
	 * @param msgHeader
	 */
	public ACKMessage(MessageHeader msgHeader) {
		mHeader = msgHeader;
	}

	/**
	 * Constructor of an acknowlegdment
	 *
	 * @param guid the ack identifier.
	 * @param code the ack code.
	 */
	public ACKMessage(GUID guid, int code) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mCode = code;
	}

	/**
	 * Constructor of an acknowlegdment
	 *
	 * @param guid the ack identifier.
	 * @param code the ack code.
	 * @param msg  additional information.
	 */
	public ACKMessage(GUID guid, int code, String msg) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mCode = code;
		mMsg = msg;
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
	 * Returns the query acknowlegment type.
	 * <br>
	 * <code>200</code>: receiver was responsible for
	 * the path<br>
	 * <code>400</code>: receiver was not responsible for
	 * the path
	 *
	 * @return the query acknowlegment code.
	 */
	public int getCode() {
		return mCode;
	}

	/**
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_ACK;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_ACK_STRING;
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
	 * Returns additional information if provided.
	 *
	 * @return the message.
	 */
	public String getMessage() {
		return mMsg;
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
		if (mCode == -1)
			return false;
		return true;
	}

	/**
	 * The Parser will call this method to report each chunk of character data. SAX parsers may return all contiguous
	 * character data in a single chunk, or they may split it into several chunks; however, all of the characters in any
	 * single event must come from the same external entity so that the Locator provides useful information.
	 *
	 * @param ch     the characters from the XML document.
	 * @param start  the start position in the array.
	 * @param length the number of characters to read from the array.
	 * @throws SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (mMsg == null) {
			mMsg = String.valueOf(ch, start, length).trim();
		} else {
			String append = String.valueOf(ch, start, length).trim();
			if (append.trim().length() > 0)
				mMsg = mMsg.concat(append);
		}
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
		if (qName.equals(XML_ACK)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_ACK_GUID));
			mCode = Integer.parseInt(attrs.getValue(XML_ACK_CODE));
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
		String msg = prefix + XML_ELEMENT_OPEN + XML_ACK + // {prefix}<ACK
				XML_SPACE + XML_ACK_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_ACK_CODE + XML_ATTR_OPEN + mCode + XML_ATTR_CLOSE; // _Code="CODE"
		if ((mMsg == null) || (mMsg.length() == 0)) {
			msg += XML_ELEMENT_END_CLOSE + newLine; // />{newLine}
		} else {
			msg += XML_ELEMENT_CLOSE + newLine + // >{newLine}
					prefix + XML_TAB + XML_ELEMENT_OPEN + XML_ACK_MESSAGE + XML_ELEMENT_CLOSE + mMsg + XML_ELEMENT_OPEN_END + XML_ACK_MESSAGE + XML_ELEMENT_CLOSE + newLine + //{prefix}\t<Message>MESSAGE</Message>{newLine}
					prefix + XML_ELEMENT_OPEN_END + XML_ACK + XML_ELEMENT_CLOSE + newLine; // {prefix}</ACK>{newLine}
		}
		return msg;
	}

}

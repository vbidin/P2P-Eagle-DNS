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
import pgrid.PGridHost;
import pgrid.util.LexicalDefaultHandler;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid peer query message.
 *
 * @author Renault John
 * @version 1.0.0
 */
public class PeerLookupReplyMessage extends LexicalDefaultHandler implements PGridMessage, XMLizable {
	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP = "LookupPeerReply";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_CODE = "Code";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_HOPS = "Hops";

	/**
	 * The acknowledgment code Bad Request.
	 */
	private static final int CODE_BAD_REQUEST = 404;

	/**
	 * The acknowledgment code OK.
	 */
	private static final int CODE_OK = 200;

	/**
	 * The Query reply type Bad Request.
	 */
	public static final int TYPE_BAD_REQUEST = 1;

	/**
	 * The Query reply type Bad Request.
	 */
	public static final int TYPE_NO_PEER_FOUNDS = 2;

	/**
	 * The Query reply type OK.
	 */
	public static final int TYPE_OK = 0;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The searched host
	 */
	protected XMLPGridHost mHost = null;

	/**
	 * The message id.
	 */
	protected GUID mGUID = null;

	/**
	 * HTTP code
	 */
	protected int mCode;

	/**
	 * Hops
	 */
	protected int mHops;

	/**
	 * Constructor of an acknowlegdment
	 *
	 * @param msgHeader
	 */
	public PeerLookupReplyMessage(MessageHeader msgHeader) {
		mHeader = msgHeader;
	}

	/**
	 * Constructor of an acknowlegdment
	 */
	public PeerLookupReplyMessage(GUID guid, PGridHost host, int type, int hops) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mHost = new XMLPGridHost(host);
		if (type == TYPE_OK)
			mCode = CODE_OK;
		else
			mCode = CODE_BAD_REQUEST;
		mHops = hops;
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
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_PEERLOOKUP_REPLY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_PEERLOOKUP_REPLY_STRING;
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
	 * Returns the host
	 *
	 * @return the host
	 */
	public PGridHost getHost() {
		return mHost.getHost();
	}

	/**
	 * Return the code
	 *
	 * @return the code
	 */
	public int getType() {
		if (mCode == CODE_OK) return TYPE_OK;
		return TYPE_NO_PEER_FOUNDS;
	}

	/**
	 * return the number of hops
	 *
	 * @return number of hops
	 */
	public int getPeerLookupHops() {
		return mHops;
	}

	/**
	 * Tests if this init response message is valid.
	 *
	 * @return <code>true</code> if valid.
	 */
	public boolean isValid() {
		if (mHeader == null || mHost == null) {
			return false;
		} else {
			if (!mHeader.isValid()) {
				return false;
			}
		}
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
		if (qName.equals(XML_PEERLOOKUP)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_PEERLOOKUP_GUID));
			mCode = Integer.parseInt(attrs.getValue(XML_PEERLOOKUP_CODE));
			mHops = Integer.parseInt(attrs.getValue(XML_PEERLOOKUP_HOPS));
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// Host
			mHost = XMLPGridHost.getXMLHost(qName, attrs, false);
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
		String xmlMessage = prefix + XML_ELEMENT_OPEN + XML_PEERLOOKUP + // {prefix}<PeerQuery
				XML_SPACE + XML_PEERLOOKUP_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_PEERLOOKUP_HOPS + XML_ATTR_OPEN + mHops + XML_ATTR_CLOSE + // _Code="CODE"
				XML_SPACE + XML_PEERLOOKUP_CODE + XML_ATTR_OPEN + mCode + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _Code="CODE"
				mHost.toXMLString(prefix + XML_TAB, newLine, false) + // <Host .../>
				prefix + XML_ELEMENT_OPEN_END + XML_PEERLOOKUP + XML_ELEMENT_CLOSE + newLine; // </PeerQuery>

		return xmlMessage;
	}

}

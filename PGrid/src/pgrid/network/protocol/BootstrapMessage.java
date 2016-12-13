/**
 * $Id: BootstrapMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.PGridHost;
import pgrid.XMLizable;
import pgrid.core.XMLRoutingTable;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid bootstrap message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class BootstrapMessage extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_BOOTSTRAP = "Bootstrap";

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * The routing table.
	 */
	private XMLRoutingTable mRoutingTable = null;

	/**
	 * Creates a new PGridP2P Exchange message with the given header.
	 *
	 * @param header the message header.
	 */
	public BootstrapMessage(MessageHeader header) {
		mHeader = header;
	}

	/**
	 * Creates a new exchange with given values for bootstrapping.
	 *
	 * @param host the creating host.
	 */
	public BootstrapMessage(PGridHost host) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, host);
	}

	/**
	 * Creates a new exchange with given values for bootstrapping.
	 *
	 * @param host the creating host.
	 * @param routingTable the routing table.
	 */
	public BootstrapMessage(PGridHost host, XMLRoutingTable routingTable) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, host);
		mRoutingTable = routingTable;
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
		return PGridMessage.DESC_BOOTSTRAP;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_BOOTSTRAP_STRING;
	}

	/**
	 * Returns the message GUID.
	 *
	 * @return the message GUID.
	 */
	public GUID getGUID() {
		return null;
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
	 * Returns the routing table.
	 *
	 * @return the routing table.
	 */
	public XMLRoutingTable getRoutingTable() {
		return mRoutingTable;
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
	public synchronized void characters(char[] ch, int start, int length) throws SAXException {
		if (mParsedObject != null)
			mParsedObject.characters(ch, start, length);
	}

	/**
	 * The SAX parser will invoke this method at the end of every element in the XML document; there will be a
	 * corresponding startElement event for every endElement event (even when the element is empty).
	 *
	 * @param uri   the Namespace URI.
	 * @param lName the local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName the qualified name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException any SAX exception, possibly wrapping another exception.
	 */
	public synchronized void endElement(String uri, String lName, String qName) throws SAXException {
		if (qName.equals(XMLRoutingTable.XML_ROUTING_TABLE)) {
			mParsedObject.endElement(uri, lName, qName);
			mParsedObject = null;
		} else if (mParsedObject != null) {
			mParsedObject.endElement(uri, lName, qName);
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
		if (qName.equals(XMLRoutingTable.XML_ROUTING_TABLE)) {
			mRoutingTable = new XMLRoutingTable();
			mRoutingTable.startElement(uri, lName, qName, attrs);
			mParsedObject = mRoutingTable;
		} else if (mParsedObject != null) {
			mParsedObject.startElement(uri, lName, qName, attrs);
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
	public synchronized String toXMLString(String prefix, String newLine) {
		if (mRoutingTable == null) {
			return prefix + XML_ELEMENT_OPEN + XML_BOOTSTRAP + XML_ELEMENT_END_CLOSE + newLine; // {prefix}<Bootstrap/>{newLine}
		} else {
			StringBuffer strBuff = new StringBuffer(100);
			strBuff.append(prefix + XML_ELEMENT_OPEN + XML_BOOTSTRAP + XML_ELEMENT_CLOSE + newLine); // {prefix}<Bootstrap>{newLine}
			strBuff.append(mRoutingTable.toXMLString(prefix + XML_TAB, newLine, true, false, false));
			strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_BOOTSTRAP + XML_ELEMENT_CLOSE + newLine); // {prefix}</Bootstrap>{newLine}
			return strBuff.toString();
		}
	}

}
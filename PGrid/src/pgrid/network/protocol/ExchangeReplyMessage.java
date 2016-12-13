/**
 * $Id: ExchangeReplyMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.Exchange;
import pgrid.PGridHost;
import pgrid.XMLizable;
import pgrid.core.XMLRoutingTable;
import pgrid.core.storage.DBDataTable;
import pgrid.core.storage.Signature;

import java.util.Collection;
import java.io.UnsupportedEncodingException;

/**
 * This class represents an exchange reply message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ExchangeReplyMessage extends Exchange implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_EXCHANGE_REPLY = "ExchangeReply";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_LEN_CURRENT = "CurrentLength";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_RANDOM_NUMBER = "RandomNumber";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_RECURSION = "Recursion";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_MINSTORAGE = "MinStorage";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_REPLY_REPLICA_ESTIMATE = "ReplicaEstimate";

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * The data table as XML.
	 */
	private XMLDataTable mXMLDataTable = null;

	/**
	 * Creates a new PGridP2P Exchange message with the given header.
	 *
	 * @param header the message header.
	 */
	public ExchangeReplyMessage(MessageHeader header) {
		super();
		mHeader = header;
		mHost = mHeader.getHost();
	}

	/**
	 * Creates a new exchange message with given values.
	 *
	 * @param guid         the message guid.
	 * @param host         the message creating host.
	 * @param recursion    the recursion.
	 * @param lCurrent     the current common length.
	 * @param replicaEst   the replication estimate.
	 * @param routingTable the Routing Table for this host.
	 * @param dataItems    the list of data items.
	 * @param sign         the data table signature.
	 */
	public ExchangeReplyMessage(GUID guid, PGridHost host, int recursion, int lCurrent, int minStorage, double replicaEst,
								XMLRoutingTable routingTable, Collection dataItems, Signature sign) {
		super(guid, host, recursion, lCurrent, minStorage, replicaEst, routingTable, null);
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, host);
		mXMLDataTable = new XMLDataTable(dataItems, sign);
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
		return PGridMessage.DESC_EXCHANGE_REPLY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_EXCHANGE_REPLY_STRING;
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
		} else if (qName.equals(XMLDataTable.XML_DATA_TABLE)) {
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
		if (qName.equals(XML_EXCHANGE_REPLY)) {
			// Exchange
			String guidStr = attrs.getValue(XML_EXCHANGE_REPLY_GUID);
			if (guidStr != null)
				mGUID = new pgrid.GUID(attrs.getValue(XML_EXCHANGE_REPLY_GUID));
			String recStr = attrs.getValue(XML_EXCHANGE_REPLY_RECURSION);
			if (recStr == null)
				mRecursion = 0;
			else
				mRecursion = Integer.parseInt(recStr);
			String lCurrStr = attrs.getValue(XML_EXCHANGE_REPLY_LEN_CURRENT);
			if (lCurrStr == null)
				mLenCurrent = 0;
			else
				mLenCurrent = Integer.parseInt(lCurrStr);
			String minStorage = attrs.getValue(XML_EXCHANGE_REPLY_MINSTORAGE);
			if (minStorage == null)
				mMinStorage = 0;
			else
				mMinStorage = Integer.parseInt(minStorage);
			String rndNmbrStr = attrs.getValue(XML_EXCHANGE_REPLY_RANDOM_NUMBER);
			if (rndNmbrStr == null)
				mRandomNumber = Double.MIN_VALUE;
			else
				mRandomNumber = Double.parseDouble(rndNmbrStr);
			String replicaEstStr = attrs.getValue(XML_EXCHANGE_REPLY_REPLICA_ESTIMATE);
			if (replicaEstStr == null)
				mReplicateEstimate = 0;
			else
				mReplicateEstimate = Double.parseDouble(replicaEstStr);
		} else if (qName.equals(XMLRoutingTable.XML_ROUTING_TABLE)) {
			mRoutingTable = new XMLRoutingTable();
			mRoutingTable.startElement(uri, lName, qName, attrs);
			mParsedObject = mRoutingTable;
		} else if (qName.equals(XMLDataTable.XML_DATA_TABLE)) {
			mDataTable = new DBDataTable(mHost);
			mXMLDataTable = new XMLDataTable(mDataTable);
			mXMLDataTable.startElement(uri, lName, qName, attrs);
			mParsedObject = mXMLDataTable;
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
		StringBuffer strBuff;
		if (mDataTable == null)
			strBuff = new StringBuffer(100);
		else
			strBuff = new StringBuffer(mDataTable.count() * 100);
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_EXCHANGE_REPLY); // {prefix}<Exchange
	strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE); // _GUID="GUID"
	if (mRandomNumber != Double.MIN_VALUE)
	  strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_RANDOM_NUMBER + XML_ATTR_OPEN + String.valueOf(mRandomNumber) + XML_ATTR_CLOSE); // _RandomNumber="RANDOM NUMBER"
		strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_RECURSION + XML_ATTR_OPEN + mRecursion + XML_ATTR_CLOSE); // _Recursion="RECURSION"
		strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_LEN_CURRENT + XML_ATTR_OPEN + mLenCurrent + XML_ATTR_CLOSE); // _CurrentLength="LEN_CURRENT"
		strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_MINSTORAGE + XML_ATTR_OPEN + mMinStorage + XML_ATTR_CLOSE); // _MinStorage="MinStorage"
		strBuff.append(XML_SPACE + XML_EXCHANGE_REPLY_REPLICA_ESTIMATE + XML_ATTR_OPEN + mReplicateEstimate + XML_ATTR_CLOSE); // _ReplicaEstimate="REPLICA_ESTIMATE"
		strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}

		// routing table
		strBuff.append(mRoutingTable.toXMLString(prefix + XML_TAB, newLine, true, true, true));

		// data table
		strBuff.append(mXMLDataTable.toXMLString(prefix + XML_TAB, newLine));

		strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_EXCHANGE_REPLY + XML_ELEMENT_CLOSE + newLine); // {prefix}</Exchange>{newLine}
		return strBuff.toString();
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
	 * Report the start of a CDATA section.
	 * <p/>
	 * <p>The contents of the CDATA section will be reported through
	 * the regular {@link org.xml.sax.ContentHandler#characters
	 * characters} event; this event is intended only to report
	 * the boundary.</p>
	 *
	 * @throws org.xml.sax.SAXException The application may raise an exception.
	 * @see #endCDATA
	 */
	public void startCDATA() throws SAXException {
		mCDataSection = true;
		if (mParsedObject != null) mParsedObject.startCDATA();
	}

	/**
	 * Report the end of a CDATA section.
	 *
	 * @throws org.xml.sax.SAXException The application may raise an exception.
	 * @see #startCDATA
	 */
	public void endCDATA() throws SAXException {
		mCDataSection = false;
		if (mParsedObject != null) mParsedObject.endCDATA();
	}

}
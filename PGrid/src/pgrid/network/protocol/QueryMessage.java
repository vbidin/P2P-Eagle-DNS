/**
 * $Id: QueryMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import p2p.basic.Key;
import pgrid.*;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.Iterator;
import java.util.Vector;
import java.io.UnsupportedEncodingException;

/**
 * This class represents a Gridella query message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class QueryMessage extends Query implements PGridMessage, RoutableMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_QUERY = "Query";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_HOPS = "Hops";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_INDEX = "Index";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_LOWER_BOUND_KEY = "LowerBoundKey";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HIGHER_BOUND_KEY = "HigherBoundKey";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_MINSPEED = "MinSpeed";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_REPLICAS = "Replicas";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_TYPE = "Type";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_QUERY_KEYWORD = "Keyword";

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * Flag to indicate if a host is the requesting host or a replica during parsing a message.
	 */
	private boolean mReplicaFlag = false;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * Creates an empty query hit message.
	 *
	 * @param header the message header.
	 */
	public QueryMessage(MessageHeader header) {
		super();
		mHeader = header;
	}

	/**
	 * Creates a new query message with given values.
	 *
	 * @param guid        the guid of the query.
	 * @param type        the type of the query.
	 * @param query       the search string.
	 * @param key         the key (binary represantation of the search query).
	 * @param index       the search progress.
	 * @param minSpeed    the minimal speed for responding hosts.
	 * @param initialHost the requesting and initiating host.
	 */
	public QueryMessage(GUID guid, p2p.storage.Type type, String query, Key key, int index, int minSpeed, PGridHost initialHost, int hops, Vector replicas) {
		super(initialHost, guid, type, query, key, index, minSpeed, hops);
		super.setReplicas(replicas);
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
	}

	/**
	 * Returns the query message as array of bytes.
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
		return PGridMessage.DESC_QUERY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_QUERY_STRING;
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
		if (mGUID == null)
			return false;
		if (mType == null)
			return false;
		if (mQueryString == null)
			return false;
		if (mKey == null)
			return false;
		if (mIndex == -1)
			return false;
		if (mMinSpeed == -1)
			return false;
		return true;
	}

	/**
	 * Sets the message header.
	 *
	 * @param header the header.
	 */
	public void setHeader(MessageHeader header) {
		mHeader = header;
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
		if (parsingCDATA()) {
			if (mQueryString == null) {
				mQueryString = String.valueOf(ch, start, length);
			} else {
				String append = String.valueOf(ch, start, length);
				if (append.length() > 0)
					mQueryString = mQueryString.concat(append);
			}
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
	 * @throws org.xml.sax.SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
		if (qName.equals(XML_QUERY)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_QUERY_GUID));
			// Query
			mType = StorageManager.getInstance().getTypeByString(attrs.getValue(XML_QUERY_TYPE));
			mIndex = Integer.parseInt(attrs.getValue(XML_QUERY_INDEX));
			mKey = new PGridKeyRange(new PGridKey(attrs.getValue(XML_LOWER_BOUND_KEY)), new PGridKey(attrs.getValue(XML_HIGHER_BOUND_KEY)));
			String minSpeed = attrs.getValue(XML_QUERY_MINSPEED);
			if (minSpeed == null)
				mMinSpeed = 0;
			else
				mMinSpeed = Integer.parseInt(minSpeed);
			String hopStr = attrs.getValue(XML_QUERY_HOPS);
			if (hopStr != null)
				mHops = Integer.parseInt(hopStr);
		} else if (qName.equals(XML_QUERY_REPLICAS)) {
			mReplicas = new Vector();
			mReplicaFlag = true;
		} else if (qName.equals(XML_QUERY_KEYWORD)) {

		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// Host
			if (mReplicaFlag) {
				XMLPGridHost host = new XMLPGridHost();
				host.startElement(uri, lName, qName, attrs);
				if (host.getHost().isValid())
					mReplicas.add(host);
			} else {
				mRequestingHost = XMLPGridHost.getHost(qName, attrs, false);
			}
		}
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
	public void endElement(String uri, String lName, String qName) throws SAXException {
		if (qName.equals(XML_QUERY_REPLICAS)) {
			mReplicaFlag = false;
		}
	}

	/**
	 * Returns a string represantation of this message.
	 *
	 * @return a string represantation of this message.
	 */
	public String toXMLString() {
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
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_QUERY + // {prefix}<Query
				XML_SPACE + XML_QUERY_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_QUERY_TYPE + XML_ATTR_OPEN + getTypeString() + XML_ATTR_CLOSE + // _Type="TYPE"
				XML_SPACE + XML_QUERY_INDEX + XML_ATTR_OPEN + mIndex + XML_ATTR_CLOSE + // _Index="INDEX"
				XML_SPACE + XML_LOWER_BOUND_KEY + XML_ATTR_OPEN + mKey.getMin() + XML_ATTR_CLOSE + // _LowerBound="KEY"
				XML_SPACE + XML_HIGHER_BOUND_KEY + XML_ATTR_OPEN + mKey.getMax() + XML_ATTR_CLOSE + // _HigherBound="KEY"
				XML_SPACE + XML_QUERY_MINSPEED + XML_ATTR_OPEN + mMinSpeed + XML_ATTR_CLOSE + // _MinSpeed="MINSPEED"
				XML_SPACE + XML_QUERY_HOPS + XML_ATTR_OPEN + mHops + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _Hops="HOPS">{newLine}
				new XMLPGridHost(mRequestingHost).toXMLString(prefix + XML_TAB, newLine, false)); // <Host .../>{newLine}
		if (mReplicas != null) {
			if (mReplicas.size() == 0) {
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_QUERY_REPLICAS + XML_ELEMENT_END_CLOSE + newLine); // {prefix}\t<Replicas/>{newLine}
			} else {
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_QUERY_REPLICAS + XML_ELEMENT_CLOSE + newLine); // {prefix}\t<Replicas>{newLine}
				for (Iterator it = mReplicas.iterator(); it.hasNext();) {
					Object next = it.next();
					strBuff.append(((XMLPGridHost)next).toXMLString(prefix + XML_TAB + XML_TAB, newLine, false));
				}
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN_END + XML_QUERY_REPLICAS + XML_ELEMENT_CLOSE + newLine); // {prefix}\t</Replicas>{newLine}
			}
		}
		strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_QUERY_KEYWORD + XML_ELEMENT_CLOSE + // <Keyword>
				XML_CDATA_OPEN + mQueryString + XML_CDATA_CLOSE + // <![CDATA[QUERY-STRING]]>
				XML_ELEMENT_OPEN_END + XML_QUERY_KEYWORD + XML_ELEMENT_CLOSE + newLine + // </ Keyword>{newLine}
				prefix + XML_ELEMENT_OPEN_END + XML_QUERY + XML_ELEMENT_CLOSE + newLine); // </Query>{newLine}
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
}
/**
 * $Id: RangeQueryMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import p2p.basic.KeyRange;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.PGridKey;
import pgrid.PGridKeyRange;
import pgrid.RangeQuery;
import pgrid.XMLizable;
import pgrid.PGridHost;
import pgrid.core.storage.StorageManager;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a Gridella range query message.
 *
 * @author <a href="mailto:Renault JOHN <renault.john@epfl.ch>">Renault JOHN</a>
 */
public class RangeQueryMessage extends RangeQuery implements PGridMessage, RoutableMessage,
		XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_QUERY = "RangeQuery";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_ALGORITHM = "Algorithm";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_INDEX = "Index";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_PREFIX = "Prefix";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_POINTER_KEY = "Key";

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
	private static final String XML_QUERY_RECURSION = "Recursion";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_TYPE = "Type";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_HOPS = "Hops";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_KEYWORD = "Keyword";

	/**
	 * Temporary int use for parsing
	 */
	private int mFirstParsed = 0;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * Creates an empty query hit message.
	 *
	 * @param header the message header.
	 */
	public RangeQueryMessage(MessageHeader header) {
		super();
		mHeader = header;
		mPrefix = "";
	}

	/**
	 * Creates a new query message with given values.
	 *
	 * @param guid      the guid of the query.
	 * @param type     	the type of the query.
	 * @param minQuery  the lower bound search string.
	 * @param maxQuery  the higher bound search string.
	 * @param rq      the key (binary represantation of the search query).
	 * @param index    the search progress.
	 * @param minSpeed the minimal speed for responding hosts.
	 * @param hops     the hop count.
	 */
	public RangeQueryMessage(GUID guid, p2p.storage.Type type, int hops, String algorithm, String minQuery, String maxQuery, KeyRange rq, int index, String prefix, int minSpeed, PGridHost initialHost) {
		super(guid, type, hops, algorithm, minQuery, maxQuery, rq, index, prefix, minSpeed, initialHost);
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mPrefix = prefix;
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
		return PGridMessage.DESC_RANGE_QUERY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_RANGE_QUERY_STRING;
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
		if (mFirstQueryString == null)
			return false;
		if (mSecondQueryString == null)
			return false;
		if (mBoundsKeys == null)
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
		String parsed;

		if (parsingCDATA()) {
			parsed = String.valueOf(ch, start, length);
			if (mFirstParsed == 1) {
				if (mFirstQueryString == null) {
					mFirstQueryString = parsed;
				} else {
					if (parsed.length() > 0)
						mFirstQueryString = mFirstQueryString.concat(parsed);
				}
			} else {
				if (mSecondQueryString == null) {
					mSecondQueryString = parsed;
				} else {
					if (parsed.length() > 0)
						mSecondQueryString = mSecondQueryString.concat(parsed);
				}
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
			mAlgorithm = attrs.getValue(XML_QUERY_ALGORITHM);
			mBoundsKeys = new PGridKeyRange(new PGridKey(attrs.getValue(XML_LOWER_BOUND_KEY)),
					new PGridKey(attrs.getValue(XML_HIGHER_BOUND_KEY)));
			String minSpeed = attrs.getValue(XML_QUERY_MINSPEED);

			if (minSpeed == null)
				mMinSpeed = 0;
			else
				mMinSpeed = Integer.parseInt(minSpeed);

			mIndex = Integer.parseInt(attrs.getValue(XML_QUERY_INDEX));

			mHops = Integer.parseInt(attrs.getValue(XML_QUERY_HOPS));

			mPrefix = attrs.getValue(XML_QUERY_PREFIX);

		} else if (qName.equals(XML_KEYWORD)) {
			mFirstParsed++;
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// Host
			setInitialHost(XMLPGridHost.getHost(qName, attrs, false));
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
		String xmlMessage = prefix + XML_ELEMENT_OPEN + XML_QUERY + // {prefix}<RangeQuery
				XML_SPACE + XML_QUERY_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_QUERY_TYPE + XML_ATTR_OPEN + getTypeString() + XML_ATTR_CLOSE + // _Type="TYPE"
				XML_SPACE + XML_QUERY_HOPS + XML_ATTR_OPEN + getHops() + XML_ATTR_CLOSE + // _Hops="HOPS"
				XML_SPACE + XML_QUERY_ALGORITHM + XML_ATTR_OPEN + mAlgorithm + XML_ATTR_CLOSE + // _Algorithm="ALGORITHM"
				XML_SPACE + XML_QUERY_INDEX + XML_ATTR_OPEN + mIndex + XML_ATTR_CLOSE + // _Index="INDEX"
				XML_SPACE + XML_QUERY_PREFIX + XML_ATTR_OPEN + mPrefix + XML_ATTR_CLOSE + // _Prefix="PREFIX"
				XML_SPACE + XML_LOWER_BOUND_KEY + XML_ATTR_OPEN + mBoundsKeys.getMin() + XML_ATTR_CLOSE + // _FirstKey="KEY"
				XML_SPACE + XML_HIGHER_BOUND_KEY + XML_ATTR_OPEN + mBoundsKeys.getMax() + XML_ATTR_CLOSE + // _SecondKey="SECOND_KEY"
				XML_SPACE + XML_QUERY_MINSPEED + XML_ATTR_OPEN + mMinSpeed + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _MinSpeed="MINSPEED"
				new XMLPGridHost(getInitialHost()).toXMLString(prefix + XML_TAB, newLine, false) + // <Host .../>
				prefix + XML_TAB + XML_ELEMENT_OPEN + XML_KEYWORD + XML_ELEMENT_CLOSE + // <Keyword>
				XML_CDATA_OPEN + mFirstQueryString + XML_CDATA_CLOSE + // <![CDATA[QUERY-STRING]]>
				XML_ELEMENT_OPEN_END + XML_KEYWORD + XML_ELEMENT_CLOSE + newLine + // </ Keyword>
				prefix + XML_TAB + XML_ELEMENT_OPEN + XML_KEYWORD + XML_ELEMENT_CLOSE + // <Keyword>
				XML_CDATA_OPEN + mSecondQueryString + XML_CDATA_CLOSE + // <![CDATA[QUERY-STRING]]>
				XML_ELEMENT_OPEN_END + XML_KEYWORD + XML_ELEMENT_CLOSE + newLine + // </ Keyword>
				prefix + XML_ELEMENT_OPEN_END + XML_QUERY + XML_ELEMENT_CLOSE + newLine; // </RangeQuery>
		return xmlMessage;
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

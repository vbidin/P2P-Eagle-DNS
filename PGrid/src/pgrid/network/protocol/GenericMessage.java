/**
 * $Id: GenericMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import p2p.basic.Key;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.*;
import pgrid.util.LexicalDefaultHandler;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid generic message to be used with the P2P interface.
 *
 * @author @author <a href="mailto:Roman Schmidt <Renault.John@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public class GenericMessage extends LexicalDefaultHandler implements PGridMessage, RoutableMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_GENERIC = "Generic";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_GENERIC_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_MIN_KEY = "Min";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_MAX_KEY = "Max";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_GENERIC_INDEX = "Index";

	/**
	 * The message id.
	 */
	protected GUID mGUID = null;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The originating host.
	 */
	protected XMLPGridHost mHost = null;

	/**
	 * The data.
	 */
	protected byte[] mData = null;

	/**
	 * The data string.
	 */
	protected String mDataString = null;

	/**
	 * The key.
	 */
	protected KeyRange mKeyRange = null;

	/**
	 * Number of resolved bit of the key
	 */
	private int mIndex;

	/**
	 * Creates an empty info message.
	 *
	 * @param header the message header.
	 */
	public GenericMessage(MessageHeader header) {
		super();
		mHeader = header;
	}

	/**
	 * Creates a new generic message with given values.
	 *
	 * @param keyRange a range of key to where this message should be routed
	 * @param data the generic message content.
	 */
	public GenericMessage(KeyRange keyRange, byte[] data) {
		this(pgrid.GUID.getGUID(),PGridP2P.sharedInstance().getLocalHost(), keyRange, data);
	}

	/**
	 * Creates a new generic message with given values.
	 *
	 * @param key a key to where this message should be routed
	 * @param data the generic message content.
	 */
	public GenericMessage(Key key, byte[] data) {
		this(new PGridKeyRange(key, key), data);
	}

	/**
	 * Creates a new generic message with given values.
	 *
	 * @param host the message originator.
	 * @param keyRange a range of key to where this message should be routed
	 * @param data the generic message content.
	 */
	public GenericMessage(GUID guid, PGridHost host, KeyRange keyRange, byte[] data) {
		super();
		mGUID = guid;
		mHost = new XMLPGridHost(host);
		mKeyRange = keyRange;
		mData = data;
		mDataString = new String(mData);

		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, host);
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
		return PGridMessage.DESC_GENERIC;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_GENERIC_STRING;
	}

	/**
	 * Set the number of resolved bit
	 * @param index
	 */
	public void setIndex(int index) {
		mIndex = index;
	}

	/**
	 * Get the number of resolved bit
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * Returns the message id.
	 *
	 * @return the message id.
	 */
	public GUID getGUID() {
		return mGUID;
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
	 * Returns the originating host.
	 *
	 * @return the host.
	 */
	public PGridHost getHost() {
		return mHost.getHost();
	}

	/**
	 * Returns the data.
	 *
	 * @return the data.
	 */
	public byte[] getData() {
		if (mData == null) {
			byte[] bytes=null;

			try {
				bytes = mDataString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			mData = bytes;
		}
		return mData;
	}

	/**
	 * Returns the destination key.
	 *
	 * @return the key.
	 */
	public KeyRange getKeyRange() {
		return mKeyRange;
	}

	/**
	 * Set the destination key.
	 */
	public void setKeyRange(KeyRange key) {
		mKeyRange = key;
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
		if (mHost == null)
			return false;
		if (mKeyRange == null)
			return false;
		if (mData == null)
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
	 * @throws org.xml.sax.SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (parsingCDATA()) {
			if (mDataString == null) {
				mDataString = String.valueOf(ch, start, length);
			} else {
				String append = String.valueOf(ch, start, length);
				if (append.length() > 0)
					mDataString = mDataString.concat(append);
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
	 * @throws org.xml.sax.SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void endElement(String uri, String lName, String qName) throws SAXException {

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
		if (qName.equals(XML_GENERIC)) {
			// Info
			mGUID = new pgrid.GUID(attrs.getValue(XML_GENERIC_GUID));
			mKeyRange = new PGridKeyRange(new PGridKey(attrs.getValue(XML_MIN_KEY)), new PGridKey(attrs.getValue(XML_MAX_KEY)));
			mIndex = Integer.parseInt(attrs.getValue(XML_GENERIC_INDEX));
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// the originating host
			mHost = XMLPGridHost.getXMLHost(qName, attrs, false);
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
		return prefix + XML_ELEMENT_OPEN + XML_GENERIC + // {prefix}<GenericMessage
				XML_SPACE + XML_GENERIC_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_MIN_KEY + XML_ATTR_OPEN + mKeyRange.getMin() + XML_ATTR_CLOSE + // _MIN="MIN"
				XML_SPACE + XML_MAX_KEY + XML_ATTR_OPEN + mKeyRange.getMax() + XML_ATTR_CLOSE + // _MIN="MIN"
				XML_SPACE + XML_GENERIC_INDEX + XML_ATTR_OPEN + mIndex + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _INDEX="INDEX">{newLine}
				mHost.toXMLString(prefix + XML_TAB, newLine, false) + // <Host .../>{newLine}
				prefix + XML_TAB + XML_CDATA_OPEN + mDataString + XML_CDATA_CLOSE + newLine + // <![CDATA[data]]>
				prefix + XML_ELEMENT_OPEN_END + XML_GENERIC + XML_ELEMENT_CLOSE + newLine; // </GenericMessage>{newLine}
	}

}
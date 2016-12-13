/**
 * $Id: QueryReplyMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.Constants;
import pgrid.QueryReply;
import pgrid.XMLDataItem;
import pgrid.XMLizable;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.io.UnsupportedEncodingException;

/**
 * This class represents a query reply.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class QueryReplyMessage extends QueryReply implements PGridMessage, XMLizable {

	/**
	 * The Query Hit reply code Bad Request.
	 */
	private static final int CODE_BAD_REQUEST = 400;

	/**
	 * The Query Hit reply code File Not Found.
	 */
	private static final int CODE_NOT_FOUND = 404;

	/**
	 * The Query Hit reply code OK.
	 */
	private static final int CODE_OK = 200;

	/**
	 * A part of the XML string.
	 */
	public static final String XML_QUERY_REPLY = "QueryReply";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_REPLY_CODE = "Code";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_QUERY_REPLY_GUID = "GUID";

	/**
	 * The Query Hit reply code.
	 */
	private int mCode = -1;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * Data items manager
	 */
	private StorageManager mStorageManager = null;

	/**
	 * Creates a query reply message with the given header.
	 *
	 * @param header the message header.
	 */
	public QueryReplyMessage(MessageHeader header) {
		super();
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mHeader = header;
	}

	/**
	 * Creates a new query reply message with given values.
	 *
	 * @param guid      the GUID of the Query Reply.
	 * @param type      the query reply type.
	 * @param resultSet the result set of found files.
	 */
	public QueryReplyMessage(p2p.basic.GUID guid, int type, Collection resultSet) {
		super(guid, type, resultSet);
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		if (type == TYPE_OK)
			mCode = CODE_OK;
		else if (type == TYPE_NOT_FOUND)
			mCode = CODE_NOT_FOUND;
	}

	/**
	 * Returns the pong message as array of bytes.
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
		return PGridMessage.DESC_QUERY_REPLY;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_QUERY_REPLY_STRING;
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
	 * Returns the message size.
	 *
	 * @return the message size.
	 */
	public int getSize() {
		return toXMLString().length();
	}

	/**
	 * Tests if this query hit message is valid.
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
		if (mCode == -1)
			return false;
		if ((mCode == CODE_OK) && (mResultSet == null))
			return false;
		if ((mCode >= 0) && (mCode != CODE_OK) && (mResultSet != null))
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
	public void endElement(String uri, String lName, String qName) throws SAXException {
			if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
				// Query Reply Result
				mResultSet.add(mParsedObject);
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
	 * @throws org.xml.sax.SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
		if (qName.equals(XML_QUERY_REPLY)) {
			// Query Reply
			mGUID = new pgrid.GUID(attrs.getValue(XML_QUERY_REPLY_GUID));
			mCode = Integer.parseInt(attrs.getValue(XML_QUERY_REPLY_CODE));
			if (mCode == CODE_OK)
				mType = TYPE_OK;
			else if (mCode == CODE_NOT_FOUND)
				mType = TYPE_NOT_FOUND;
			else if (mCode == CODE_BAD_REQUEST)
				mType = TYPE_BAD_REQUEST;
			if (mCode == CODE_OK) {
				mResultSet = new Vector();
			}
		} else if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
			// Query Reply Result
			mParsedObject = (XMLDataItem)mStorageManager.createDataItem(attrs.getValue(XMLDataItem.XML_DATA_ITEM_TYPE));
			mParsedObject.startElement(uri, lName, qName, attrs);
		} else if (mParsedObject != null) {
			mParsedObject.startElement(uri, lName, qName, attrs);
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
		StringBuffer strBuff;
		if (mResultSet == null)
			strBuff = new StringBuffer(100);
		else
			strBuff = new StringBuffer(mResultSet.size() * 100);
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_QUERY_REPLY); // {prefix}<QueryReply
		strBuff.append(XML_SPACE + XML_QUERY_REPLY_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE); // _GUID="GUID"
		strBuff.append(XML_SPACE + XML_QUERY_REPLY_CODE + XML_ATTR_OPEN + mCode + XML_ATTR_CLOSE); // _Code="CODE"
		if (mResultSet != null) {
			strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}
			for (Iterator it = mResultSet.iterator(); it.hasNext();) {
				strBuff.append(((XMLDataItem)it.next()).toXMLString(prefix + XML_TAB, newLine));
			}
			strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_QUERY_REPLY + XML_ELEMENT_CLOSE + newLine); // {prefix}</QueryReply>{newLine}
		} else {
			strBuff.append(XML_ELEMENT_END_CLOSE + newLine); // />{newLine}
		}
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
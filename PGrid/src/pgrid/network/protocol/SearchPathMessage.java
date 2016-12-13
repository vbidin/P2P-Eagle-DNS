/**
 * $Id: SearchPathMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class SearchPathMessage extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_SEARCH_PATH = "SearchPath";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_SEARCH_PATH_COMMON_LEN = "CommonLen";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_SEARCH_PATH_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_SEARCH_PATH_PATH = "Path";

	/**
	 * The message GUID.
	 */
	private GUID mGUID = null;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The common path length.
	 */
	private int mCommonLen = -1;

	/**
	 * The path of the creating host.
	 */
	private String mPath = null;

	/**
	 * Creates an empty query hit message.
	 *
	 * @param header the message header.
	 */
	public SearchPathMessage(MessageHeader header) {
		mHeader = header;
	}

	/**
	 * Creates a new path search message with given values.
	 *
	 * @param path      the path of the creating host.
	 * @param commonLen the common path length.
	 */
	public SearchPathMessage(String path, int commonLen) {
		this(pgrid.GUID.getGUID(), path, commonLen);
	}

	/**
	 * Creates a new path search message with given values.
	 *
	 * @param guid      the messsage id.
	 * @param path      the path of the creating host.
	 * @param commonLen the common path length.
	 */
	public SearchPathMessage(GUID guid, String path, int commonLen) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mPath = path;
		mCommonLen = commonLen;
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
	 * Returns the required common path length.
	 *
	 * @return the common path length.
	 */
	public int getCommonLen() {
		return mCommonLen;
	}

	/**
	 * Sets the required common path length.
	 *
	 * @param commonLen the length.
	 */
	public void setCommonLen(int commonLen) {
		mCommonLen = commonLen;
	}

	/**
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_SEARCH_PATH;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_SEARCH_PATH_STRING;
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
	 * The path of the creating host.
	 *
	 * @return the path.
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 * Sets the hosts path.
	 *
	 * @param path the path.
	 */
	public void setPath(String path) {
		mPath = path;
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
		if (mPath == null)
			return false;
		if (mCommonLen == -1)
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
		if (qName.equals(XML_SEARCH_PATH)) {
			// Search path
			mGUID = pgrid.GUID.getGUID(attrs.getValue(XML_SEARCH_PATH_GUID));
			mPath = attrs.getValue(XML_SEARCH_PATH_PATH);
			mCommonLen = Integer.parseInt(attrs.getValue(XML_SEARCH_PATH_COMMON_LEN));
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
		return prefix + XML_ELEMENT_OPEN + XML_SEARCH_PATH + // {prefix}<SearchPath
				XML_SPACE + XML_SEARCH_PATH_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_SEARCH_PATH_PATH + XML_ATTR_OPEN + mPath + XML_ATTR_CLOSE + // _Path="PATH"
				XML_SPACE + XML_SEARCH_PATH_COMMON_LEN + XML_ATTR_OPEN + mCommonLen + XML_ATTR_CLOSE + XML_ELEMENT_END_CLOSE + newLine; // _CommonLen="COMMON_LEN"/>{newLine}
	}

}
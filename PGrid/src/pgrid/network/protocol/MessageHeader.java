/**
 * $Id: MessageHeader.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.XMLizable;
import pgrid.PGridHost;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a Gridella query message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class MessageHeader extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * Returns only the leading part of the header.
	 */
	public static final short LEADING_PART = 1;

	/**
	 * Returns only the ending part of the header.
	 */
	public static final short ENDING_PART = 2;

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PGRID = "P-Grid";

	/**
	 * The XML closing tag for a PGridP2P message header.
	 */
	public static final String CLOSING_TAG = XML_ELEMENT_OPEN_END + XML_PGRID + XML_ELEMENT_CLOSE; // </P-Grid>

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HEADER_CONTENT_LENGTH = "Content-Length";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HEADER_VERSION = "Version";

	/**
	 * The content length of the message.
	 */
	private int mContentLen = -1;

	/**
	 * The host.
	 */
	private XMLPGridHost mHost = null;

	/**
	 * The protocol version.
	 */
	private String mVersion = null;

	/**
	 * Creates an empty message header.
	 */
	public MessageHeader() {
	}

	/**
	 * Creates a new message header with given values.
	 *
	 * @param version    the protocol version.
	 * @param contentLen the length of the content.
	 * @param host       the requesting host.
	 */
	public MessageHeader(String version, int contentLen, PGridHost host) {
		mVersion = version;
		mContentLen = contentLen;
		mHost = new XMLPGridHost(host);
	}

	/**
	 * Returns the message header as array of bytes.
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
	 * Returns a part of the message header as array of bytes.
	 *
	 * @param part the part to return.
	 * @return a part of the message bytes.
	 */
	public byte[] getBytes(short part) {
		byte[] bytes=null;

		try {
			bytes = toXMLString(part).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return bytes;
	}

	/**
	 * Returns the content length of the message.
	 *
	 * @return the content length.
	 */
	public int getContentLen() {
		return mContentLen;
	}

	/**
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_HEADER;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_HEADER_STRING;
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
	 * Returns the creating host.
	 *
	 * @return the creating host.
	 */
	public PGridHost getHost() {
		return mHost.getHost();
	}

	/**
	 * Set the host
	 *
	 * @param host is the new host
	 */
	public void setHost(PGridHost host) {
		mHost = new XMLPGridHost(host);
	}

	/**
	 * Returns the message header.
	 *
	 * @return the header.
	 */
	public MessageHeader getHeader() {
		return null;
	}

	/**
	 * Returns the message length.
	 *
	 * @return the message length.
	 */
	public int getSize() {
		return getBytes().length;
	}

	/**
	 * Returns the protocol version.
	 *
	 * @return the protocol version.
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * Tests if this message header is valid.
	 *
	 * @return <code>true</code> if valid.
	 */
	public boolean isValid() {
		if (mVersion == null)
			return false;
		if (mContentLen == -1)
			return false;
		if (mHost == null)
			return false;
		if (!mHost.getHost().isValid())
			return false;
		return true;
	}

	/**
	 * Sets the message header.
	 *
	 * @param header the header.
	 */
	public void setHeader(MessageHeader header) {
		mVersion = header.getVersion();
		mContentLen = header.getContentLen();
		mHost = new XMLPGridHost(header.getHost());
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
	public void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
		if (qName.equals(XML_PGRID)) {
			// Header
			mVersion = attrs.getValue(XML_HEADER_VERSION);
			mContentLen = Integer.parseInt(attrs.getValue(XML_HEADER_CONTENT_LENGTH));
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// Host
			mHost = XMLPGridHost.getXMLHost(qName, attrs);
		}
	}

	/**
	 * Sets the content length for the message.
	 *
	 * @param contentLen the content length.
	 */
	public void setContentLen(int contentLen) {
		mContentLen = contentLen;
	}

	/**
	 * Returns a string represantation of this message.
	 *
	 * @return a string represantation of this message.
	 */
	public String toXMLString() {
		return toXMLString(LEADING_PART) + toXMLString(ENDING_PART);
	}

	/**
	 * Returns a string represantation of a part of this message.
	 *
	 * @param part the part to return.
	 * @return a string represantation of a part of this message.
	 */
	public String toXMLString(short part) {
		return toXMLString("", XML_NEW_LINE, part);
	}

	/**
	 * Returns the XML representation of this object.
	 *
	 * @param prefix  the XML prefix before each element in a new line.
	 * @param newLine the new line string.
	 * @return the XML string.
	 */
	public String toXMLString(String prefix, String newLine) {
		return toXMLString(prefix, newLine, LEADING_PART) + toXMLString(prefix, newLine, ENDING_PART);
	}

	/**
	 * Returns the XML representation of this object.
	 *
	 * @param prefix  the XML prefix before each element in a new line.
	 * @param newLine the new line string, e.g., \n.
	 * @param part    the leading or ending part.
	 * @return the XML string.
	 */
	public String toXMLString(String prefix, String newLine, short part) {
		if (part == LEADING_PART)
			return prefix + XML_ELEMENT_OPEN + XML_PGRID + // {prefix}<P-Grid
					XML_SPACE + XML_HEADER_VERSION + XML_ATTR_OPEN + mVersion + XML_ATTR_CLOSE + // _Version="VERSION"
					XML_SPACE + XML_HEADER_CONTENT_LENGTH + XML_ATTR_OPEN + mContentLen + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _Content-Length="CONTENT-LENGTH">{newLine}
					mHost.toXMLString(prefix + XML_TAB, newLine, false);
		else if (part == ENDING_PART)
			return prefix + CLOSING_TAG + newLine; // {prefix}</P-Grid>{newLine}
		return null;
	}

}
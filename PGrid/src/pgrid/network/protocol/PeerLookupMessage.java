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

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid peer query message.
 *
 * @author Renault John
 * @version 1.0.0
 */
public class PeerLookupMessage extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {
	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP = "LookupPeer";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_PATH = "Path";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_HOPS = "Hops";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_PEERLOOKUP_INDEX = "Index";

	/**
	 * look for the smallest peer greater or equal to the given path
	 */
	public static final int RIGHT_MOST = 0;

	/**
	 * look for the greatest peer smaller or equal to the given path
	 */
	public static final int LEFT_MOST = 1;

	/**
	 * look for a peer responsible for the path
	 */
	public static final int ANY = 2;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The searcher host
	 */
	protected XMLPGridHost mInitialHost = null;

	/**
	 * The message id.
	 */
	protected GUID mGUID = null;

	/**
	 * Path of the searched peer
	 */
	protected String mPath = "";

	/**
	 * Numbers of bit resolved
	 */
	protected int mIndex = 0;

	/**
	 * Mode
	 */
	protected int mMode = 0;

	/**
	 * Hops
	 */
	protected int mHops = 0;

	/**
	 * Constructor of a PeerLookupMessage
	 *
	 * @param msgHeader
	 */
	public PeerLookupMessage(MessageHeader msgHeader) {
		mHeader = msgHeader;
	}

	/**
	 * Constructor of a PeerLookupMessage
	 */
	public PeerLookupMessage(GUID guid, String path, PGridHost host, int mode) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mInitialHost = new XMLPGridHost(host);
		mPath = path;
		mMode = mode;
		mHops = 0;
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
		return PGridMessage.DESC_PEERLOOKUP;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_PEERLOOKUP_STRING;
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
	 * Returns the search progress.
	 *
	 * @return the search progress.
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * Set the search progress.
	 */
	public void setIndex(int index) {
		mIndex = index;
	}

	/**
	 * Returns the mode.
	 *
	 * @return the mode.
	 */
	public int getMode() {
		return mMode;
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
	public PGridHost getInitialHost() {
		return mInitialHost.getHost();
	}

	/**
	 * Returns the path
	 *
	 * @return the path
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 * set the path
	 */
	public void setPath(String path) {
		mPath = path;
	}

	/**
	 * Tests if this init response message is valid.
	 *
	 * @return <code>true</code> if valid.
	 */
	public boolean isValid() {
		if (mHeader == null || mInitialHost == null) {
			return false;
		} else {
			if (!mHeader.isValid()) {
				return false;
			}
		}
		return true;
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
	 * Increments the number of Hops
	 */
	public void incHops() {
		++mHops;
	}

	/**
	 * Increments the number of Hops
	 */
	public void setHops(int hops) {
		mHops = hops;
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
			mPath = attrs.getValue(XML_PEERLOOKUP_PATH);
			if (mPath.endsWith("r"))
				mMode = RIGHT_MOST;
			else if (mPath.endsWith("l"))
				mMode = LEFT_MOST;
			else
				mMode = ANY;
			mPath = mPath.substring(0, mPath.length() - 1);
			mIndex = Integer.parseInt(attrs.getValue(XML_PEERLOOKUP_INDEX));
			mHops = Integer.parseInt(attrs.getValue(XML_PEERLOOKUP_HOPS));
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			// Host
			mInitialHost = XMLPGridHost.getXMLHost(qName, attrs, false);
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
		String modeEnding;
		switch (mMode) {
			case RIGHT_MOST:
				modeEnding = "r";
				break;
			case LEFT_MOST:
				modeEnding = "l";
				break;
			case ANY:
				modeEnding = "a";
				break;
			default:
				modeEnding = "a";
		}
		String xmlMessage = prefix + XML_ELEMENT_OPEN + XML_PEERLOOKUP + // {prefix}<PeerQuery
				XML_SPACE + XML_PEERLOOKUP_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_PEERLOOKUP_PATH + XML_ATTR_OPEN + mPath + modeEnding + XML_ATTR_CLOSE +
				XML_SPACE + XML_PEERLOOKUP_HOPS + XML_ATTR_OPEN + mHops + XML_ATTR_CLOSE +
				XML_SPACE + XML_PEERLOOKUP_INDEX + XML_ATTR_OPEN + mIndex + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _Index="Index"
				mInitialHost.toXMLString(prefix + XML_TAB, newLine, false) + // <Host .../>
				prefix + XML_ELEMENT_OPEN_END + XML_PEERLOOKUP + XML_ELEMENT_CLOSE + newLine; // </PeerQuery>

		return xmlMessage;
	}

}

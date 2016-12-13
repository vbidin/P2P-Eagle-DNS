/**
 * $Id: ReplicateMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.XMLDataItem;
import pgrid.XMLizable;
import pgrid.util.LexicalDefaultHandler;
import pgrid.core.storage.StorageManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.io.UnsupportedEncodingException;

/**
 * This class represents a Gridella replica message.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ReplicateMessage extends LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_REPLICATE = "Replicate";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_REPLICATE_GUID = "GUID";

	/**
	 * Data items vector.
	 */
	private Collection mDataItems = new Vector();

	/**
	 * The message id.
	 */
	private pgrid.GUID mGUID = null;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * The storage manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * Creates an empty replicate message.
	 *
	 * @param header the message header.
	 */
	public ReplicateMessage(MessageHeader header) {
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mHeader = header;
	}

	/**
	 * Creates a new replicate message with given values.
	 *
	 * @param dataItems the data items.
	 */
	public ReplicateMessage(Collection dataItems) {
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = pgrid.GUID.getGUID();
		mDataItems = dataItems;
	}

	/**
	 * Returns the update message as array of bytes.
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
	 * Returns the dataitems collection.
	 *
	 * @return the dataitems.
	 */
	public Collection getDataItems() {
		return mDataItems;
	}

	/**
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_REPLICATE;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_REPLICATE_STRING;
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
		if (mDataItems == null)
			return false;
		if (mDataItems.size() == 0)
			return false;
		if (mGUID == null)
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
			mDataItems.add(mParsedObject);
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
		if (qName.equals(XML_REPLICATE)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_REPLICATE_GUID));
		} else if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
			// TODO get rid of the createDataItem()
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
		if (mDataItems == null)
			strBuff = new StringBuffer(100);
		else
			strBuff = new StringBuffer(mDataItems.size() * 100);
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_REPLICATE); // {prefix}<Replicate
		strBuff.append(XML_SPACE + XML_REPLICATE_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE); // _GUID="GUID"
		strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}
		for (Iterator it = mDataItems.iterator(); it.hasNext();) {
			strBuff.append(((XMLDataItem)it.next()).toXMLString(prefix + XML_TAB, newLine)); // {prefix}\t<DataItem ...>{newLine}
		}
		strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_REPLICATE + XML_ELEMENT_CLOSE + newLine); // {prefix}</Replicate>{newLine}
		return strBuff.toString();
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
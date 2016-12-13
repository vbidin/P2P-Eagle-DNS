/**
 * $Id: DataModifierMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.PGridKey;
import pgrid.XMLDataItem;
import pgrid.XMLizable;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.io.UnsupportedEncodingException;

/**
 * This class represents a data modifier message.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class DataModifierMessage extends pgrid.util.LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATAMODIFIER = "DataModifier";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATAMODIFIER_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATAMODIFIER_KEY = "Key";

		/**
	 * A part of the XML string.
	 */
	private static final String XML_DATAMODIFIER_MODE = "Mode";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATAMODIFIER_REPLICAS = "Replicas";

	/**
	 * Data items to insert vector.
	 */
	private Collection mDataItems = new Vector();

	/**
	 * The Storage Manager.
	 */
	private StorageManager mStorageManager = PGridP2P.sharedInstance().getStorageManager();

	/**
	 * The message id.
	 */
	private pgrid.GUID mGUID = null;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The common key of all data items.
	 */
	private Key mKey = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * Inser, update or delete
	 */
	private short mMode = -1;

	/**
	 * The list of replicas the message was also sent to.
	 */
	private Vector mReplicas = null;

	/**
	 * Creates an empty update message.
	 *
	 * @param header the message header.
	 */
	public DataModifierMessage(MessageHeader header) {
		mHeader = header;
	}

	/**
	 * Creates a new data modifier message with given values.
	 *
	 * @param guid      the guid of the query.
	 * @param key       the common prefix of all data items.
	 * @param mode		the mode of the modification: insert, update or delete
	 * @param dataItems the data items.
	 */
	public DataModifierMessage(pgrid.GUID guid, Key key, short mode, Collection dataItems) {
		this(guid, key, mode, dataItems, null);
	}

	/**
	 * Creates a new data modifier message with given values.
	 *
	 * @param guid      the guid of the query.
	 * @param key       the common prefix of all data items.
	 * @param mode		the mode of the modification: insert, update or delete
	 * @param dataItems the data items to insert
	 * @param replicas
	 */
	public DataModifierMessage(pgrid.GUID guid, Key key, short mode, Collection dataItems, Vector replicas) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = guid;
		mKey = key;
		mDataItems = dataItems;
		mReplicas = replicas;
		mMode = mode;
	}

	/**
	 * Returns the data modifier message as array of bytes.
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
	 * Returns the dataitems as a collection.
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
		return PGridMessage.DESC_MODIFIER;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_MODIFIER_STRING;
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
	 * Returns the key representing the common prefix of all data items.
	 *
	 * @return the key.
	 */
	public Key getKey() {
		return mKey;
	}

	/**
	 * Returns the replicas this message was already sent too.
	 *
	 * @return the list of replicas.
	 */
	public Vector getReplicas() {
		return mReplicas;
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
	 * return the distribution mode
	 * @return the distribution mode
	 */
	public short getMode() {
		return mMode;
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
		if (mKey == null)
			return false;
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
		if (qName.equals(XML_DATAMODIFIER)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_DATAMODIFIER_GUID));
			mKey = new PGridKey(attrs.getValue(XML_DATAMODIFIER_KEY));
			mMode = Short.parseShort(attrs.getValue(XML_DATAMODIFIER_MODE));
		} else if (qName.equals(XML_DATAMODIFIER_REPLICAS)) {
			mReplicas = new Vector();
		} else if (mParsedObject != null) {
			mParsedObject.startElement(uri, lName, qName, attrs);
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			PGridHost host = XMLPGridHost.getHost(qName, attrs, false);
			if (host.isValid())
				mReplicas.add(host);
		} else if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
			mParsedObject = (XMLDataItem)mStorageManager.createDataItem(attrs.getValue(XMLDataItem.XML_DATA_ITEM_TYPE));
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
		int size = 0;
		if (mDataItems == null)
			size = 100;
		else
			size = mDataItems.size() * 100;

		strBuff = new StringBuffer(size);

		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_DATAMODIFIER); // {prefix}<DataModifier
		strBuff.append(XML_SPACE + XML_DATAMODIFIER_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE); // _GUID="GUID"
		strBuff.append(XML_SPACE + XML_DATAMODIFIER_KEY + XML_ATTR_OPEN + mKey + XML_ATTR_CLOSE); // _Key="KEY"
		strBuff.append(XML_SPACE + XML_DATAMODIFIER_MODE + XML_ATTR_OPEN + mMode + XML_ATTR_CLOSE); // _Mode="MODE"
		strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}
		if (mReplicas != null) {
			if (mReplicas.size() > 0) {
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_DATAMODIFIER_REPLICAS + XML_ELEMENT_CLOSE + newLine); // {prefix}\t<Replicas>{newLine}
				for (Iterator it = mReplicas.iterator(); it.hasNext();) {
					Object next = it.next();
					strBuff.append(new XMLPGridHost(((PGridHost)next)).toXMLString(prefix + XML_TAB + XML_TAB, newLine, false));
				}
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN_END + XML_DATAMODIFIER_REPLICAS + XML_ELEMENT_CLOSE + newLine); // {prefix}\t</Replicas>{newLine}
			} else {
				strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_DATAMODIFIER_REPLICAS + XML_ELEMENT_END_CLOSE + newLine); // {prefix}\t<Replicas/>{newLine}
			}
		}
		for (Iterator it = mDataItems.iterator(); it.hasNext();) {
			// add a signature to all data items
			strBuff.append(((XMLDataItem)it.next()).toXMLString(prefix + XML_TAB, newLine, true)); // {prefix}\t<DataItem ...>{newLine}
		}
		strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_DATAMODIFIER + XML_ELEMENT_CLOSE + newLine); // {prefix}</Insert>{newLine}
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
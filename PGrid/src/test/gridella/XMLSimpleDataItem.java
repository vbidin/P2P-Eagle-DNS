/**
 * $Id: XMLSimpleDataItem.java,v 1.1 2006/01/09 03:06:19 rschmidt Exp $
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

package test.gridella;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import p2p.basic.GUID;
import p2p.basic.Key;
import p2p.basic.Peer;
import p2p.storage.Type;
import pgrid.PGridKey;
import pgrid.XMLDataItem;
import pgrid.core.storage.StorageManager;
import pgrid.network.protocol.XMLPGridHost;

/**
 * This class represents a shared Gridella file.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class XMLSimpleDataItem extends SimpleDataItem implements XMLDataItem {

	/**
	 * A temp string used during parsing a data item.
	 */
	private String mTmpString = null;

	/**
	 * Creates a new empty PGridP2P data item.
	 */
	public XMLSimpleDataItem() {
		super();
	}

	/**
	 * Creates a new PGridP2P data item with all parameters.
	 *
	 * @param guid      the unique id.
	 * @param type      the data type.
	 * @param key       the key for this host.
	 * @param peer      the inserting peer.
	 * @param data      the data.
	 */
	public XMLSimpleDataItem(GUID guid, Type type, Key key, Peer peer, Object data) {
		super(guid, type, key, peer, data);
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
			if (mTmpString == null) {
				mTmpString = String.valueOf(ch, start, length);
			} else {
				String append = String.valueOf(ch, start, length);
				if (append.length() > 0)
					mTmpString = mTmpString.concat(append);
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
		if (qName.equals(XML_DATA_ITEM)) {
			// Data Item
			mGUID = pgrid.GUID.getGUID(attrs.getValue(XML_DATA_ITEM_GUID));
			mType = StorageManager.getInstance().getTypeByString(attrs.getValue(XML_DATA_ITEM_TYPE));
			mKey = new PGridKey(attrs.getValue(XML_DATA_ITEM_KEY));
		} else if (qName.equals(XMLPGridHost.XML_HOST)) {
			mHost = XMLPGridHost.getHost(qName, attrs, false);
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
		if (qName.equals(XML_DATA_ITEM_DATA)) {
			mData = mTmpString;
			mTmpString = null;
			decode();
		}
	}

	/**
	 * Returns the XML representation of this object.
	 *
	 * @return the XML string.
	 */
	public String toString() {
		return toXMLString("", XML_NEW_LINE);
	}

	/**
	 * Returns the XML representation of this object.
	 *
	 * @return the XML string.
	 */
	public String toXMLString() {
		return toXMLString("", XML_NEW_LINE);
	}

	/**
	 * Returns a string represantation of this result set.
	 *
	 * @param prefix  a string prefix for each line.
	 * @param newLine the string for a new line, e.g. \n.
	 * @return a string represantation of this result set.
	 */
	public String toXMLString(String prefix, String newLine) {
		return toXMLString(prefix, newLine, false);
	}

	/**
	 * Returns a string represantation of this result set.
	 *
	 * @param prefix        a string prefix for each line.
	 * @param newLine       the string for a new line, e.g. \n.
	 * @param withSignature whether or not to include the signature.
	 * @return a string represantation of this result set.
	 */
	public String toXMLString(String prefix, String newLine, boolean withSignature) {
		StringBuffer strBuff = new StringBuffer(200);
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_DATA_ITEM + // {prefix}<DataItem
				XML_SPACE + XML_DATA_ITEM_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_DATA_ITEM_TYPE + XML_ATTR_OPEN + getTypeString() + XML_ATTR_CLOSE + // _Type="TYPE"
				XML_SPACE + XML_DATA_ITEM_KEY + XML_ATTR_OPEN + mKey + XML_ATTR_CLOSE + XML_ELEMENT_CLOSE + newLine + // _Key="KEY">{newLine}
				XMLPGridHost.toXMLHost(mHost).toXMLString(prefix + XML_TAB, newLine, false)); // {prefix}\t<Host ...>{newLine}
		if ((mData != null) && (mData.trim().length() > 0))
			strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_DATA_ITEM_DATA + XML_ELEMENT_CLOSE + // {prefix}\t<Data>
					XML_CDATA_OPEN + mData + XML_CDATA_CLOSE + // <![CDATA[DESC]]>
					XML_ELEMENT_OPEN_END + XML_DATA_ITEM_DATA + XML_ELEMENT_CLOSE + newLine); // </Data>{newLine}
		strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_DATA_ITEM + XML_ELEMENT_CLOSE + newLine);// {prefix}</DataItem>
		return strBuff.toString();
	}

	public void decode() {
	}

}
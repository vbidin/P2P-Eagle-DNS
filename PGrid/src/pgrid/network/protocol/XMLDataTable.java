/**
 * $Id: XMLDataTable.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.XMLDataItem;
import pgrid.XMLizable;
import pgrid.core.storage.DBDataTable;
import pgrid.core.storage.Signature;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class extends the {@link pgrid.core.storage.DataTable} with XML functionality.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
//@todo restrict access level	
public class XMLDataTable implements XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_TABLE = "DataTable";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATA_TABLE_SIGNATURE = "Signature";

	/**
	 * The represented data table.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The data items.
	 */
	private Collection mDataItems = null;

	/**
	 * The data table.
	 */
	private DBDataTable mDataTable = null;

	/**
	 * The temporary variable during parsing.
	 */
	private XMLizable mParsedObject = null;

	/**
	 * The signature of the data table.
	 */
	private Signature mSignature = null;

	/**
	 * The temporary variable during parsing.
	 */
	private Vector mTmpDataItems = null;

	/**
	 * Construct a new Data Table.
	 * @param dataTable the data table.
	 */
	public XMLDataTable(DBDataTable dataTable) {
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mDataTable = dataTable;
		mDataItems = mDataTable.getDataItems();
		mSignature = mDataTable.getSignature();
	}

	/**
	 * Construct a new Data Table.
	 * @param dataItems the data items.
	 * @param sign the data table signature.
	 */
	public XMLDataTable(Collection dataItems, Signature sign) {
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mDataItems = dataItems;
		mSignature = sign;
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
		if (qName.equals(XML_DATA_TABLE)) {
			mParsedObject = null;
			mDataTable.addAll(mTmpDataItems);
			mDataItems = mTmpDataItems;
			mDataTable.setSignature(mSignature);
		} else if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
			mTmpDataItems.add(mParsedObject);
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
	 * @throws SAXException any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
		if (qName.equals(XML_DATA_TABLE)) {
			// Data Table
			String sign = attrs.getValue(XML_DATA_TABLE_SIGNATURE);
			if (sign != null)
				mSignature = new Signature(sign);
			mTmpDataItems = new Vector();
		} else if (qName.equals(XMLDataItem.XML_DATA_ITEM)) {
			// a Data Item
			// mParsedObject = (XMLDataItem)mStorageManagerdataItemFactory(attrs.getValue(XMLDataItem.XML_DATA_ITEM_TYPE));
			// TODO get rid of the createDataItem()
			mParsedObject = (XMLDataItem)mStorageManager.createDataItem(attrs.getValue(XMLDataItem.XML_DATA_ITEM_TYPE));
			mParsedObject.startElement(uri, lName, qName, attrs);
		} else if (mParsedObject != null) {
			mParsedObject.startElement(uri, lName, qName, attrs);
		}
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
	 * Returns the XML representation of this object.
	 *
	 * @param prefix    the prefix string for all new lines.
	 * @param newLine   the new line string, e.g., \n.
	 * @return the XML string.
	 */
	public String toXMLString(String prefix, String newLine) {
		if ((prefix == null) || (newLine == null))
			throw new NullPointerException();

		StringBuffer strBuff;
		if (mDataItems == null)
			strBuff = new StringBuffer(100);
		else
			strBuff = new StringBuffer(mDataItems.size() * 100);

		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_DATA_TABLE); // {prefix}<DataTable
		strBuff.append(XML_SPACE + XML_DATA_TABLE_SIGNATURE + XML_ATTR_OPEN + mSignature.toString() + XML_ATTR_CLOSE); // _Signature="SIGNATURE"

		if ((mDataItems != null) && (mDataItems.size() > 0)) {
			strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}
			for (Iterator it = mDataItems.iterator(); it.hasNext();) {
				strBuff.append(((XMLDataItem)it.next()).toXMLString(prefix + XML_TAB, newLine));
			}
			strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_DATA_TABLE + XML_ELEMENT_CLOSE + newLine); // {prefix}</DataTable>{newLine}
		} else {
			strBuff.append(XML_ELEMENT_END_CLOSE + newLine); // />{newLine}
		}
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
		if (mParsedObject != null) mParsedObject.startCDATA();
	}

	/**
	 * Report the end of a CDATA section.
	 *
	 * @throws org.xml.sax.SAXException The application may raise an exception.
	 * @see #startCDATA
	 */
	public void endCDATA() throws SAXException {
		if (mParsedObject != null) mParsedObject.endCDATA();
	}

}

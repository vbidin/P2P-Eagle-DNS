/**
 * $Id: XMLIdentityDataItem.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.maintenance.identity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import p2p.basic.Key;
import pgrid.core.storage.StorageManager;
import pgrid.core.maintenance.identity.IdentityDataItem;
import pgrid.XMLDataItem;
import pgrid.PGridHost;
import pgrid.PGridKey;

/**
 * This class represents a shared Gridella file.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class XMLIdentityDataItem extends IdentityDataItem implements XMLDataItem {

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HOST_ADDRESS = "Address";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HOST_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_HOST_PORT = "Port";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATA_ITEM_KEY = "Key";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATA_ITEM_TS = "TimeStamp";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_DATA_ITEM_E = "PublicKey";

	/**
	 * 
	 */
	private static final String XML_ID_MAPPING = "Signature";

	/**
	 * Creates a new empty PGridP2P data item.
	 */
	public XMLIdentityDataItem() {
		super();
	}

	/**
	 * Creates a new PGridP2P data item with all parameters.
	 *
	 * @param type      the type of entry.
	 * @param host		the host
	 * @param key       the key for this host.
	 * @param publicKey public key of this host.
	 * @param timestamp at which this item has been created.
	 * @param desc      the file description.
	 */
	public XMLIdentityDataItem(p2p.storage.Type type, PGridHost host, Key key, String publicKey, long timestamp, String desc) {
		super(type, host, key, publicKey, timestamp, desc);
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
			mType = StorageManager.getInstance().getTypeByString(attrs.getValue(XML_DATA_ITEM_TYPE));
			mSignature = attrs.getValue(XML_ID_MAPPING);
			mHost = PGridHost.getHost(attrs.getValue(XML_HOST_GUID), attrs.getValue(XML_HOST_ADDRESS), attrs.getValue(XML_HOST_PORT));

			String timestamp = attrs.getValue(XML_DATA_ITEM_TS);
			if (timestamp != null) {
				mTimeStamp = Long.parseLong(timestamp);
			}
			mPublicKey = attrs.getValue(XML_DATA_ITEM_E);
			mKey = new PGridKey(attrs.getValue(XML_DATA_ITEM_KEY));
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
				XML_SPACE + XML_DATA_ITEM_TYPE + XML_ATTR_OPEN + getTypeString() + XML_ATTR_CLOSE + // _Type="TYPE"
				XML_SPACE + XML_HOST_GUID + XML_ATTR_OPEN + mHost.getGUID().toString() + XML_ATTR_CLOSE + // _GUID="GUID"
				XML_SPACE + XML_HOST_ADDRESS + XML_ATTR_OPEN + mHost.getAddressString() + XML_ATTR_CLOSE + // _ADDRESS="ADDRESS"
				XML_SPACE + XML_HOST_PORT + XML_ATTR_OPEN + mHost.getPort() + XML_ATTR_CLOSE + // _Port="PORT"
				XML_SPACE + XML_DATA_ITEM_KEY + XML_ATTR_OPEN + mKey + XML_ATTR_CLOSE); // _Key="KEY"

		if (withSignature) {
			strBuff.append(XML_SPACE + XML_DATA_ITEM_TS + XML_ATTR_OPEN + mTimeStamp + XML_ATTR_CLOSE); // _PublicKey="PublicKey"
			strBuff.append(XML_SPACE + XML_DATA_ITEM_E + XML_ATTR_OPEN + mPublicKey + XML_ATTR_CLOSE); // _PublicKey="PublicKey"
			strBuff.append(XML_SPACE + XML_ID_MAPPING + XML_ATTR_OPEN + mSignature + XML_ATTR_CLOSE); // _Signature="Signature"
		}

		strBuff.append(XML_ELEMENT_END_CLOSE + newLine); // />{newLine}
		return strBuff.toString();
	}

}
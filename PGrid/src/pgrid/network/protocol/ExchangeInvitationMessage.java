/**
 * $Id: ExchangeInvitationMessage.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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
import pgrid.GUID;
import pgrid.XMLizable;
import pgrid.util.LexicalDefaultHandler;
import pgrid.core.storage.Signature;
import pgrid.interfaces.basic.PGridP2P;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a P-Grid exchange invitation.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ExchangeInvitationMessage extends LexicalDefaultHandler implements PGridMessage, XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_EXCHANGE_INVITATION = "ExchangeInvitation";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_INVITATION_CURRENT_LEN = "CurrentLength";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_INVITATION_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_INVITATION_PATH = "Path";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_INVITATION_RECURSION = "Recursion";

	/**
	 * A part of the XML string.
	 */
	private static final String XML_EXCHANGE_INVITATION_SIGNATURE = "Signature";

	/**
	 * The common length of pathes for recusive exchanges.
	 */
	private int mCurrentLen = -1;

	/**
	 * The message id.
	 */
	private GUID mGUID = null;

	/**
	 * The message header.
	 */
	private MessageHeader mHeader = null;

	/**
	 * The path of the requesting host.
	 */
	private String mPath = null;

	/**
	 * The recusrion of this invitation.
	 */
	private int mRecursion = -1;

	/**
	 * The signature of the data table of the requesting host.
	 */
	private Signature mSignature = null;

	/**
	 * The temp signature string.
	 */
	private String mTmpSign = null;

	/**
	 * Creates an empty replicate message.
	 *
	 * @param header the message header.
	 */
	public ExchangeInvitationMessage(MessageHeader header) {
		mHeader = header;
	}

	/**
	 * Creates a new exchange invitation message with given values.
	 *
	 * @param path the path of the requesting host.
	 * @param sign the data table signature of the requesting host.
	 */
	public ExchangeInvitationMessage(String path, Signature sign, int recursion, int currentLen) {
		mHeader = new MessageHeader(Constants.PGRID_PROTOCOL_VERSION, -1, PGridP2P.sharedInstance().getLocalHost());
		mGUID = GUID.getGUID();
		mPath = path;
		mSignature = sign;
		mRecursion = recursion;
		mCurrentLen = currentLen;
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
	 * Returns a desricptor for the type of message.
	 *
	 * @return the message descriptor.
	 */
	public int getDesc() {
		return PGridMessage.DESC_EXCHANGE_INVITATION;
	}

	/**
	 * Returns the representation string for a descriptor of a message.
	 *
	 * @return the message descriptor string.
	 */
	public String getDescString() {
		return PGridMessage.DESC_EXCHANGE_INVITATION_STRING;
	}

	/**
	 * Returns the common length for recursive exchange requests.
	 *
	 * @return the common length.
	 */
	public int getCurrentLen() {
		return mCurrentLen;
	}

	/**
	 * Returns the message id.
	 *
	 * @return the message id.
	 */
	public p2p.basic.GUID getGUID() {
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
	 * Returns the path of the requesting host.
	 *
	 * @return the path.
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 * Returns the recursion level.
	 *
	 * @return the recursion.
	 */
	public int getRecursion() {
		return mRecursion;
	}

	/**
	 * Returns the signature of the data table of the requesting host.
	 *
	 * @return the signature.
	 */
	public Signature getSignature() {
		return mSignature;
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
		if (mSignature == null)
			return false;
		if (mRecursion == -1)
			return false;
		if (mCurrentLen == -1)
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
		if (mTmpSign == null) {
			mTmpSign = String.valueOf(ch, start, length).trim();
		} else {
			String append = String.valueOf(ch, start, length).trim();
			if (append.trim().length() > 0)
				mTmpSign = mTmpSign.concat(append);
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
		if (qName.equals(XML_EXCHANGE_INVITATION_SIGNATURE)) {
			mSignature = new Signature(mTmpSign);
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
		if (qName.equals(XML_EXCHANGE_INVITATION)) {
			mGUID = new pgrid.GUID(attrs.getValue(XML_EXCHANGE_INVITATION_GUID));
			mPath = attrs.getValue(XML_EXCHANGE_INVITATION_PATH);
			String recStr = attrs.getValue(XML_EXCHANGE_INVITATION_RECURSION);
			if (recStr == null)
				mRecursion = 0;
			else
				mRecursion = Integer.parseInt(recStr);
			String currLenStr = attrs.getValue(XML_EXCHANGE_INVITATION_CURRENT_LEN);
			if (currLenStr == null)
				mCurrentLen = 0;
			else
				mCurrentLen = Integer.parseInt(currLenStr);
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
		StringBuffer strBuff = new StringBuffer(100);
		strBuff.append(prefix + XML_ELEMENT_OPEN + XML_EXCHANGE_INVITATION); // {prefix}<ExchangeInvitation
		strBuff.append(XML_SPACE + XML_EXCHANGE_INVITATION_GUID + XML_ATTR_OPEN + mGUID.toString() + XML_ATTR_CLOSE); // _GUID="GUID"
		strBuff.append(XML_SPACE + XML_EXCHANGE_INVITATION_PATH + XML_ATTR_OPEN + mPath + XML_ATTR_CLOSE); // _Path="PATH"
		strBuff.append(XML_SPACE + XML_EXCHANGE_INVITATION_RECURSION + XML_ATTR_OPEN + mRecursion + XML_ATTR_CLOSE); // _Recursion="RECURSION"
		strBuff.append(XML_SPACE + XML_EXCHANGE_INVITATION_CURRENT_LEN + XML_ATTR_OPEN + mCurrentLen + XML_ATTR_CLOSE); // _CurrentLength="CURRENT_LEN"
		strBuff.append(XML_ELEMENT_CLOSE + newLine); // >{newLine}

		strBuff.append(prefix + XML_TAB + XML_ELEMENT_OPEN + XML_EXCHANGE_INVITATION_SIGNATURE + XML_ELEMENT_CLOSE); // {prefix}\t<Signature>
		strBuff.append(mSignature.toString()); // SIGNATURE
		strBuff.append(XML_ELEMENT_OPEN_END + XML_EXCHANGE_INVITATION_SIGNATURE + XML_ELEMENT_CLOSE + newLine); // </Signature>{newLine}

		strBuff.append(prefix + XML_ELEMENT_OPEN_END + XML_EXCHANGE_INVITATION + XML_ELEMENT_CLOSE + newLine); // {prefix}</ExchangeInvitation>{newLine}
		return strBuff.toString();
	}

}
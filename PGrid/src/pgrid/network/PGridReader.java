/**
 * $Id: PGridReader.java,v 1.4 2005/11/23 14:19:59 john Exp $
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

package pgrid.network;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.XMLizable;
import pgrid.network.protocol.*;
import pgrid.util.Compression;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.Vector;
import java.util.Iterator;

/**
 * This class reads a P-Grid messages from the Input Stream.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridReader implements Runnable {

	/**
	 *  A vector of PGridWriterListener. Use for testing and debuging purpose
	 */
	static private Vector mListener = new Vector();

	/**
	 * The Communication Manager.
	 */
	private ConnectionManager mConnMgr = ConnectionManager.sharedInstance();

	/**
	 * The connection.
	 */
	private Connection mConn = null;

	/**
	 * The message listener handling the received messages.
	 */
	private MessageListener mMsgListener = null;

	/**
	 * The SAX Parser.
	 */
	private static XMLReader mParser = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The Communication reader.
	 */
	private ConnectionReader mReader = null;

	static {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			mParser = spf.newSAXParser().getXMLReader();//XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * Register a P-Grid Reader listener. This listener will be called just after
	 * the processing of the message.
	 *
	 * @param listener
	 */
	static public void registerListener(PGridReaderListener listener) {
		mListener.add(listener);
	}


	/**
	 * Creates a new Gridella reader.
	 *
	 * @param conn        the Connection.
	 * @param msgListener the listener for incoming messages.
	 */
	PGridReader(Connection conn, MessageListener msgListener) {
		mConn = conn;
		mMsgListener = msgListener;
	}

	/**
	 * Reads a PGridP2P message from the Input Stream and calls the Message Handler.
	 *
	 * @throws ConnectionClosedException  the connection was closed by the remote host.
	 * @throws ConnectionTimeoutException the connection has timed out.
	 */
	private void readMsg() throws ConnectionClosedException, ConnectionTimeoutException {
		// read message header (leading part)
		StringBuffer buffer = new StringBuffer();
		buffer.append(mReader.readLine());
		buffer.append(mReader.readLine());
		buffer.append(MessageHeader.CLOSING_TAG);

		MessageHeader msgHeader = new MessageHeader();
		try {
			synchronized (mParser) {
				mParser.setContentHandler(msgHeader);
				mParser.parse(new InputSource(new StringReader(buffer.toString())));
			}
		} catch (SAXException e) {
			mConn.incDroppedCount();
			return;
		} catch (IOException e) {
			mConn.incDroppedCount();
			return;
		}
		mConn.incReceivedBytes(msgHeader.getSize());

		if (!msgHeader.isValid()) {
			mConn.incDroppedCount();
			return;
		}

		// read message content
		byte[] msgContent = mReader.readBytes(msgHeader.getContentLen());
		StringBuffer msg = new StringBuffer(msgContent.length);
		// decompress the bytes if necessary
		if (mConn.isCompressed()) {
			byte[] byteArray;
			try {
				byteArray = Compression.decompress(msgContent, 0, msgContent.length);
			} catch (DataFormatException e) {
				e.printStackTrace();
				return;
			}
			if (byteArray != null)
				try {
					msg.append(new String(byteArray, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
		} else {
			try {
				msg.append(new String(msgContent, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
		mConn.incReceivedBytes(msgContent.length);

		// read message header (ending part)
		String endingHeader = mReader.readLine();
		if (!endingHeader.equals(MessageHeader.CLOSING_TAG)) {
			mConn.incDroppedCount();
			return;
		}

		String msgString = msg.toString().trim();
		if (msgString.length() == 0) {
			return;
		}

		PGridMessage recvMsg = null;
		try {
			synchronized (mParser) {
				// run the garbage collector if not enough free memory
				//if (Runtime.getRuntime().freeMemory() < msgString.length())
				// System.gc();
				if ((msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + BootstrapMessage.XML_BOOTSTRAP + XMLizable.XML_ELEMENT_END_CLOSE)) ||
						(msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + BootstrapMessage.XML_BOOTSTRAP + XMLizable.XML_ELEMENT_CLOSE))) {
					recvMsg = new BootstrapMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((BootstrapMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((BootstrapMessage)recvMsg);
				} else if ((msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + BootstrapReplyMessage.XML_BOOTSTRAP_REPLY + XMLizable.XML_ELEMENT_CLOSE)) ||
					(msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + BootstrapReplyMessage.XML_BOOTSTRAP_REPLY + XMLizable.XML_SPACE))) {
					recvMsg = new BootstrapReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((BootstrapReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((BootstrapReplyMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ExchangeInvitationMessage.XML_EXCHANGE_INVITATION + XMLizable.XML_SPACE)) {
					recvMsg = new ExchangeInvitationMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ExchangeInvitationMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mConn.getHost().setPort(recvMsg.getHeader().getHost().getPort());
					mMsgListener.newMessage((ExchangeInvitationMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ExchangeMessage.XML_EXCHANGE + XMLizable.XML_SPACE)) {
					recvMsg = new ExchangeMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ExchangeMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mConn.getHost().setPort(recvMsg.getHeader().getHost().getPort());
					mMsgListener.newMessage((ExchangeMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ExchangeReplyMessage.XML_EXCHANGE_REPLY + XMLizable.XML_SPACE)) {
					recvMsg = new ExchangeReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ExchangeReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((ExchangeReplyMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + QueryMessage.XML_QUERY + XMLizable.XML_SPACE)) {
					recvMsg = new QueryMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((QueryMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((QueryMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + RangeQueryMessage.XML_QUERY + XMLizable.XML_SPACE)) {
					recvMsg = new RangeQueryMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((RangeQueryMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					// todo Move the hops count. It has nothing to do here!!!
					//add 1 to the number of Hops
					((RangeQueryMessage)recvMsg).incHops();
					mMsgListener.newMessage((RangeQueryMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + QueryReplyMessage.XML_QUERY_REPLY + XMLizable.XML_SPACE)) {
					recvMsg = new QueryReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((QueryReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((QueryReplyMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + SearchPathMessage.XML_SEARCH_PATH + XMLizable.XML_SPACE)) {
					recvMsg = new SearchPathMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((SearchPathMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((SearchPathMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + SearchPathReplyMessage.XML_SEARCH_PATH_REPLY + XMLizable.XML_SPACE)) {
					recvMsg = new SearchPathReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((SearchPathReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((SearchPathReplyMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ACKMessage.XML_ACK + XMLizable.XML_SPACE)) {
					recvMsg = new ACKMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ACKMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((ACKMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + PeerLookupMessage.XML_PEERLOOKUP + XMLizable.XML_SPACE)) {
					recvMsg = new PeerLookupMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((PeerLookupMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					//add 1 to the number of Hops
					((PeerLookupMessage)recvMsg).incHops();
					mMsgListener.newMessage((PeerLookupMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + PeerLookupReplyMessage.XML_PEERLOOKUP + XMLizable.XML_SPACE)) {
					recvMsg = new PeerLookupReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((PeerLookupReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((PeerLookupReplyMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + DataModifierMessage.XML_DATAMODIFIER + XMLizable.XML_SPACE)) {
					recvMsg = new DataModifierMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((DataModifierMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((DataModifierMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + GenericMessage.XML_GENERIC + XMLizable.XML_SPACE)) {
					recvMsg = new GenericMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((GenericMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((GenericMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ReplicateMessage.XML_REPLICATE + XMLizable.XML_SPACE)) {
					recvMsg = new ReplicateMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ReplicateMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((ReplicateMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ChallengeMessage.XML_CHALLENGE + XMLizable.XML_SPACE)) {
					recvMsg = new ChallengeMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ChallengeMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((ChallengeMessage)recvMsg);
				} else if (msgString.startsWith(XMLizable.XML_ELEMENT_OPEN + ChallengeReplyMessage.XML_CHALLENGE_REPLY + XMLizable.XML_SPACE)) {
					recvMsg = new ChallengeReplyMessage(msgHeader);
					mParser.setProperty("http://xml.org/sax/properties/lexical-handler", recvMsg);
					mParser.setContentHandler((ChallengeReplyMessage)recvMsg);
					mParser.parse(new InputSource(new StringReader(msgString)));
					mMsgListener.newMessage((ChallengeReplyMessage)recvMsg);
				}
			}
			Constants.LOGGER.finer("PGrid " + recvMsg.getDescString() + " Message received from " + mConn.getHost().toHostString());
			if (Constants.DEBUG)
				Constants.LOGGER.finest("Message Content:\n" + msgHeader.toXMLString(MessageHeader.LEADING_PART) + recvMsg.toXMLString() + msgHeader.toXMLString(MessageHeader.ENDING_PART));
			mConn.incReceivedCount();

			if (Constants.TESTS) {
				// statistics
				mPGridP2P.getStatistics().Messages[recvMsg.getDesc()]++;
				mPGridP2P.getStatistics().Bandwidth[recvMsg.getDesc()] += msgHeader.getSize() + msgContent.length;
				mPGridP2P.getStatistics().BandwidthUncompr[recvMsg.getDesc()] += msgHeader.getSize() + msg.length();

				Iterator it = mListener.iterator();

				for(;it.hasNext();) {
					((PGridWriterListener)it.next()).messageWritten(recvMsg);
				}
			}

		} catch (SAXParseException e) {
			Constants.LOGGER.warning("Could not parse message in line '" + e.getLineNumber() + "', column '" + e.getColumnNumber() + "'! (" + e.getMessage() + ")");
		} catch (SAXException e) {
			e.printStackTrace();
			mConn.incDroppedCount();
			return;
		} catch (IOException e) {
			mConn.incDroppedCount();
			return;
		}
	}

	/**
	 * Starts the P-Grid reader.
	 */
	public void run() {
		if (mConn.getSocket() == null) {
			mConnMgr.socketClosed(mConn);
			return;
		}
		try {
			mReader = new ConnectionReader(mConn.getSocket().getInputStream());
		} catch (NullPointerException e) {
			mConnMgr.socketClosed(mConn);
			return;
		} catch (IOException e) {
			mConnMgr.socketClosed(mConn);
			return;
		}
		if (mParser == null)
			return;

		while (mConn.isConnected()) {
			// receive a new message
			try {
				readMsg();
			} catch (ConnectionClosedException e) {
				mConn.setStatus(Connection.STATUS_ERROR, "Closed");
				break;
			} catch (ConnectionTimeoutException e) {
				if (mConn.isConnected()) {
					mConn.setStatus(Connection.STATUS_ERROR, "Timeout");
					break;
				}
				continue;
			}
		}
		mConnMgr.socketClosed(mConn);
	}

}
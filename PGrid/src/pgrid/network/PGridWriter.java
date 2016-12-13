/**
 * $Id: PGridWriter.java,v 1.3 2005/11/23 09:34:48 john Exp $
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

//import test.planetlab.RangeQueryTester;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.MessageHeader;
import pgrid.network.protocol.PGridMessage;
import pgrid.util.Compression;

import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;

/**
 * This class writes Gridella messages at the Output Stream.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridWriter {

	/**
	 * The Communication Manager.
	 */
	private ConnectionManager mConnMgr = ConnectionManager.sharedInstance();

	/**
	 * The connection.
	 */
	private Connection mConn = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 *  A vector of PGridWriterListener. Use for testing and debuging purpose
	 */
	static private Vector mListener = new Vector();

	/**
	 * The Communication writer.
	 */
	private ConnectionWriter mWriter = null;

	/**
	 * Register a P-Grid Writer listener. This listener will be called just before
	 * the processing of the message to be sent
	 * @param listener
	 */
	static public void registerListener(PGridWriterListener listener) {
		mListener.add(listener);
	}

	/**
	 * Creates a new Gridella writer.
	 *
	 * @param conn the connection.
	 */
	PGridWriter(Connection conn) {
		mConn = conn;
		try {
			mWriter = new ConnectionWriter(conn.getSocket().getOutputStream());
		} catch (IOException e) {
			// do nothing
		}
	}

	/**
	 * Writes a Gridella message to the Output Stream.
	 *
	 * @param msg the msg to write.
	 */
	void sendMsg(PGridMessage msg) {
		MessageHeader header = msg.getHeader();
		header.setHost(mPGridP2P.getLocalHost());
		byte[] content = msg.getBytes();
		byte[] msgContent;
		// compress the bytes if necessary
		if (mConn.isCompressed())
			msgContent = Compression.compress(content, 0, content.length);
		else
			msgContent = content;
		header.setContentLen(msgContent.length);

		//Constants.LOGGER.finer("PGrid " + msg.getDescString() + " Message sent to " + mConn.getPeer());
		Constants.LOGGER.finer("PGrid " + msg.getDescString() + " Message sent to " + mConn.getHost().toHostString());
		if (Constants.DEBUG)
			Constants.LOGGER.finest("Message Content:\n" + msg.getHeader().toXMLString(MessageHeader.LEADING_PART) + msg.toXMLString() + msg.getHeader().toXMLString(MessageHeader.ENDING_PART));

		if (Constants.TESTS) {
			// statistics
			mPGridP2P.getStatistics().Messages[msg.getDesc()]++;
			mPGridP2P.getStatistics().Bandwidth[msg.getDesc()] += msgContent.length + header.getSize();
			mPGridP2P.getStatistics().BandwidthUncompr[msg.getDesc()] += content.length + header.getSize();

			Iterator it = mListener.iterator();

			for(;it.hasNext();) {
				((PGridWriterListener)it.next()).messageWritten(msg);
			}
		}
		try {
			mWriter.write(msg.getHeader().getBytes(MessageHeader.LEADING_PART));
			mWriter.write(msgContent);
			mWriter.write(msg.getHeader().getBytes(MessageHeader.ENDING_PART));
		} catch (IOException e) {
			mConn.setStatus(Connection.STATUS_ERROR);
			mConnMgr.socketClosed(mConn);
			return;
		}
		mConn.incSentCount();
		mConn.incSentBytes(msgContent.length + header.getSize());
	}

}
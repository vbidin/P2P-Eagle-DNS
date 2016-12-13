/**
 * $Id: Acceptor.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import pgrid.PGridHost;
import pgrid.Properties;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.protocol.InitMessage;
import pgrid.network.protocol.InitResponseMessage;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * The Communication Acceptor handles incomming connections from remote host.
 * It tries to identitify the used protocol of the remote host, and starts then
 * the corresponding worker.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class Acceptor implements Runnable {

	/**
	 * The start of a P-Grid greeting.
	 */
	private static final String PGRID_GREETING = "P-GRID";

	/**
	 * The Communication Manager.
	 */
	private ConnectionManager mConnMgr = ConnectionManager.sharedInstance();

	/**
	 * The connection.
	 */
	private Connection mConn = null;

	/**
	 * The already reveived greeting.
	 */
	private String mGreeting = null;

	/**
	 * The connection.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Creates a new worker for a delivered socket.
	 *
	 * @param conn the connection to handle.
	 */
	Acceptor(Connection conn) {
		mConn = conn;
	}

	/**
	 * Creates a new worker for a delivered socket.
	 *
	 * @param conn     the connection to handle.
	 * @param greeting the received greeting.
	 */
	Acceptor(Connection conn, String greeting) {
		mConn = conn;
		mGreeting = greeting;
	}

	/**
	 * Starts to decide the used protocol and start the corresponding worker.
	 */
	public void run() {
		try {
			ConnectionReader reader = new ConnectionReader(mConn.getSocket().getInputStream());
			ConnectionWriter writer = new ConnectionWriter(mConn.getSocket().getOutputStream());

			// read and check greeting
			if (mGreeting == null)
				mGreeting = reader.readGreeting();
			if (mGreeting == null) {
				mConn.setStatus(Connection.STATUS_ERROR, "Timeout");
				mConnMgr.acceptanceFinished(mConn);
				return;
			}
			if (mGreeting.startsWith(PGRID_GREETING)) {
				// P-Grid
				InitMessage msgInit = new InitMessage(mGreeting);
				if (!msgInit.isValid()) {
					mConn.setStatus(Connection.STATUS_ERROR, "Invalid");
					mConnMgr.acceptanceFinished(mConn);
					return;
				}
				// write response
				InitResponseMessage msgInitResp = new InitResponseMessage(mPGridP2P.getLocalHost().getGUID());
				boolean compression = (msgInit.getHeaderField(InitMessage.HEADER_COMPRESSION).toLowerCase().equals("yes") ? true : false);
				if ((mPGridP2P.propertyInteger(Properties.COMPRESSION_LEVEL) != Deflater.NO_COMPRESSION) && (compression)) {
					msgInitResp.setHeaderField(InitResponseMessage.HEADER_COMPRESSION, "yes");
					mConn.setCompression(true);
				} else {
					msgInitResp.setHeaderField(InitResponseMessage.HEADER_COMPRESSION, "no");
					mConn.setCompression(false);
				}
				msgInitResp.setHeaderField(InitResponseMessage.HEADER_COMPRESSION, "yes");
				// Constants.LOGGER.finest("Init response message:\n" + msgInitResp.toXMLString());
				writer.write(msgInitResp.getBytes());
				mConn.setStatus(Connection.STATUS_CONNECTED);
				mConn.setProtocolString(msgInit.getVersion());
				PGridHost host = PGridHost.getHost(pgrid.GUID.getGUID(msgInit.getHeaderField(InitMessage.HEADER_GUID)), mConn.getSocket().getInetAddress(), Integer.parseInt(msgInit.getHeaderField(InitMessage.HEADER_PORT)));
				mConn.setHost(host);
				//mConn.setPeer(XMLPGridHost.getPeer(pgrid.GUID.getGUID(msgInit.getHeaderField(InitMessage.HEADER_GUID)), mConn.getSocket().getIP(), Integer.parseInt(msgInit.getHeaderField(InitMessage.HEADER_PORT))));
				mConnMgr.acceptanceFinished(mConn);
			} else {
				mConn.setStatus(Connection.STATUS_ERROR, "Invalid");
				mConnMgr.acceptanceFinished(mConn);
				return;
			}
		} catch (ConnectionClosedException e) {
			mConn.setStatus(Connection.STATUS_ERROR, "Invalid");
			mConnMgr.acceptanceFinished(mConn);
			return;
		} catch (ConnectionTimeoutException e) {
			mConn.setStatus(Connection.STATUS_ERROR, "Timeout");
			mConnMgr.acceptanceFinished(mConn);
			return;
		} catch (IOException e) {
			mConn.setStatus(Connection.STATUS_ERROR);
			mConnMgr.acceptanceFinished(mConn);
			return;
		}
	}

}
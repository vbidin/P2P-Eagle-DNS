/**
 * $Id: Connection.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import pgrid.interfaces.basic.PGridP2P;
import pgrid.PGridHost;

import java.io.IOException;
import java.net.Socket;

/**
 * This class represents a connection.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Connection {

	/**
	 * The connection is accepting.
	 */
	public static final short STATUS_ACCEPTING = 3;

	/**
	 * The connection is active.
	 */
	public static final short STATUS_CONNECTED = 4;

	/**
	 * A connection is established.
	 */
	public static final short STATUS_CONNECTING = 2;

	/**
	 * The connection has caused an error.
	 */
	public static final short STATUS_ERROR = 1;

	/**
	 * The connection is not used.
	 */
	public static final short STATUS_NOT_CONNECTED = 0;

	/**
	 * If compression should be used for this connection.
	 */
	private boolean mCompressionFlag = false;

	/**
	 * The start time of the connection.
	 */
	private long mConnectionStartTime = 0;

	/**
	 * The start time of establishing the connection.
	 */
	private long mConnectingStartTime = 0;

	/**
	 * The number of dropped messages.
	 */
	private int mDroppedCount = 0;

	/**
	 * The connected host.
	 */
	private PGridHost mHost = null;

	/**
	 * The connection id.
	 */
	private pgrid.GUID mGUID = null;

	/**
	 * The last status message.
	 */
	private String mLastStatusMsg = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The used protocol as string.
	 */
	private String mProtocolString = null;

	/**
	 * The number of received bytes.
	 */
	private int mReceivedBytes = 0;

	/**
	 * The number of received messages.
	 */
	private int mReceivedCount = 0;

	/**
	 * The number of sent bytes.
	 */
	private int mSentBytes = 0;

	/**
	 * The number of sent messages.
	 */
	private int mSentCount = 0;

	/**
	 * The socket.
	 */
	private Socket mSocket = null;

	/**
	 * The status of the connection.
	 */
	private short mStatus = -1;

	/**
	 * Creates a new Connection.
	 *
	 * @param host the host.
	 */
	public Connection(PGridHost host) {
		mGUID = new pgrid.GUID();
		mHost = host;
	}

	/**
	 * Creates a new Connection.
	 *
	 * @param socket the socket.
	 */
	public Connection(Socket socket) {
		mGUID = new pgrid.GUID();
		mSocket = socket;
	}

	/**
	 * Closes the connection and all streams.
	 */
	public void close() {
		if (mSocket != null) {
			try {
				mSocket.shutdownInput();
				mSocket.shutdownOutput();
				mSocket.close();
				mSocket = null;
			} catch (NullPointerException e) {
				// do nothing
			} catch (IOException e) {
				// do nothing
			}
		}
		setStatus(Connection.STATUS_NOT_CONNECTED, "Closed");
	}

	/**
	 * Tests if the connection uses the compressed protocol.
	 *
	 * @return <code>true</code> if compression is used, <code>false</code> otherwise.
	 */
	public boolean isCompressed() {
		return mCompressionFlag;
	}

	/**
	 * Sets if the connection uses the compressed protocol.
	 *
	 * @param flag <code>true</code> if compression is used, <code>false</code> otherwise.
	 */
	public void setCompression(boolean flag) {
		mCompressionFlag = flag;
	}

	/**
	 * Tests if connected or not.
	 *
	 * @return <code>true</code> if connected, <code>false</code> otherwise.
	 */
	public boolean isConnected() {
		if ((mSocket != null) && (mSocket.isConnected()))
			return true;
		else
			return false;
	}

	/**
	 * Returns the start time of the connection to the host.
	 *
	 * @return the start time of the connection to the host.
	 */
	public long getConnectionTime() {
		if ((mStatus == STATUS_NOT_CONNECTED) || (mStatus == STATUS_ERROR))
			return 0;
		if ((mStatus == STATUS_ACCEPTING) || (mStatus == STATUS_CONNECTING))
			return System.currentTimeMillis() - mConnectingStartTime;
		return System.currentTimeMillis() - mConnectionStartTime;
	}

	/**
	 * Returns the number of dropped messages.
	 *
	 * @return the number of dropped messages.
	 */
	public int getDroppedCount() {
		return mDroppedCount;
	}

	/**
	 * Increases the number of dropped messages from the host.
	 */
	public void incDroppedCount() {
		mDroppedCount++;
	}

	/**
	 * Returns the host.
	 *
	 * @return the host.
	 */
	public PGridHost getHost() {
		return mHost;
	}

	/**
	 * Sets the host.
	 *
	 * @param host the host.
	 */
	public void setHost(PGridHost host) {
		mHost = host;
	}

	/**
	 * Returns the connection id.
	 *
	 * @return the connection id.
	 */
	public pgrid.GUID getGUID() {
		return mGUID;
	}

	/**
	 * Returns the used protocol string.
	 *
	 * @return the protocol string.
	 */
	public String getProtocolString() {
		return mProtocolString;
	}

	/**
	 * Sets the used protocol string.
	 *
	 * @param protocolStr the protocol string.
	 */
	public void setProtocolString(String protocolStr) {
		mProtocolString = protocolStr;
	}

	/**
	 * Returns the number of received bytes.
	 *
	 * @return the number of received bytes.
	 */
	public long getReceivedBytes() {
		return mReceivedBytes;
	}

	/**
	 * Returns the number of received messages.
	 *
	 * @return the number of received messages.
	 */
	public int getReceivedCount() {
		return mReceivedCount;
	}

	/**
	 * Increases the number of received bytes from the host.
	 *
	 * @param bytes the number of received bytes.
	 */
	public void incReceivedBytes(long bytes) {
		mReceivedBytes += bytes;
	}

	/**
	 * Increases the number of received messages from the host.
	 */
	public void incReceivedCount() {
		mReceivedCount++;
	}

	/**
	 * Returns the number of sent bytes.
	 *
	 * @return the number of sent bytes.
	 */
	public long getSentBytes() {
		return mSentBytes;
	}

	/**
	 * Returns the number of sent messages.
	 *
	 * @return the number of sent messages.
	 */
	public int getSentCount() {
		return mSentCount;
	}

	/**
	 * Increases the number of sent bytes from the host.
	 *
	 * @param bytes the number of sent bytes.
	 */
	public void incSentBytes(long bytes) {
		mSentBytes += bytes;
	}

	/**
	 * Increases the number of sent messages to the host.
	 */
	public void incSentCount() {
		mSentCount++;
	}

	/**
	 * Returns the socket.
	 *
	 * @return the socket.
	 */
	public Socket getSocket() {
		return mSocket;
	}

	/**
	 * Sets the socket.
	 *
	 * @param socket the socket.
	 */
	public void setSocket(Socket socket) {
		mSocket = socket;
	}

	/**
	 * Returns the status of the connection.
	 *
	 * @return the status.
	 */
	public short getStatus() {
		return mStatus;
	}

	/**
	 * Returns a string representation of the status.
	 *
	 * @return a string representation of the status.
	 */
	public String getStatusString() {
		if (mLastStatusMsg != null)
			return mLastStatusMsg;
		switch (mStatus) {
			case STATUS_NOT_CONNECTED:
				return "Not connected";
			case STATUS_ERROR:
				return "Error";
			case STATUS_CONNECTING:
				return "Connecting";
			case STATUS_ACCEPTING:
				return "Accepting";
			case STATUS_CONNECTED:
				return "Connected";
		}
		return "Unknown";
	}

	/**
	 * Sets the status of the connection.
	 *
	 * @param status the status.
	 */
	public void setStatus(short status) {
		setStatus(status, null);
	}

	/**
	 * Sets the status of the connection with a given status message.
	 *
	 * @param status the status.
	 * @param msg    the status message.
	 */
	public void setStatus(short status, String msg) {
		if (mStatus == status)
			return;
		mStatus = status;
		mLastStatusMsg = msg;
		switch (status) {
			case STATUS_NOT_CONNECTED:
			case STATUS_ERROR:
				mConnectionStartTime = 0;
				mReceivedBytes = 0;
				mSentBytes = 0;
				break;
			case STATUS_CONNECTING:
			case STATUS_ACCEPTING:
				mConnectingStartTime = System.currentTimeMillis();
				break;
			case STATUS_CONNECTED:
				mConnectionStartTime = System.currentTimeMillis();
				break;
		}
	}

}
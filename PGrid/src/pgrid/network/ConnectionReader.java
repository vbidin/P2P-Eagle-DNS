/**
 * $Id: ConnectionReader.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

/**
 * The Communication Reader provides basic functions to read messages from an
 * Input Stream.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ConnectionReader {

	/**
	 * The buffered Input Stream reader.
	 */
	private DataInputStream mDataReader = null;

	/**
	 * The Input Stream.
	 */
	private InputStream mIn = null;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Creates a reader.
	 *
	 * @param in the Input Stream.
	 */
	ConnectionReader(InputStream in) {
		mIn = in;
		mDataReader = new DataInputStream(new BufferedInputStream(mIn));
	}

	/**
	 * Reads the delivered amount of bytes.
	 *
	 * @param len the length to read.
	 * @return the read bytes.
	 * @throws ConnectionClosedException  the connection was closed by the remote host.
	 * @throws ConnectionTimeoutException the connection has timed out.
	 * @throws IllegalArgumentException   the length to read was illegal.
	 */
	byte[] readBytes(int len) throws ConnectionClosedException, ConnectionTimeoutException, IllegalArgumentException {
		if (len < 1)
			throw new IllegalArgumentException("len " + String.valueOf(len) + " is illegal!");

		byte[] data = new byte[len];
		try {
			mDataReader.readFully(data, 0, len);
		} catch (EOFException e) {
			throw new ConnectionClosedException();
		} catch (SocketTimeoutException e) {
			throw new ConnectionTimeoutException();
		} catch (InterruptedIOException e) {
			throw new ConnectionTimeoutException();
		} catch (IOException e) {
			throw new ConnectionClosedException();
		}
		return data;
	}

	/**
	 * Returns a line from the Input Stream.
	 *
	 * @return the read line.
	 * @throws ConnectionClosedException  the connection was closed by the remote host.
	 * @throws ConnectionTimeoutException the connection has timed out.
	 */
	String readLine() throws ConnectionClosedException, ConnectionTimeoutException {
		StringBuffer line = new StringBuffer(256);
		try {
			byte[] ch = new byte[1];
			while (true) {
				mDataReader.readFully(ch, 0, ch.length);
				if (ch[0] == (byte)'\n')
					break;
				line = line.append(new String(ch, "UTF-8"));
			}
		} catch (EOFException e) {
			throw new ConnectionClosedException();
		} catch (SocketTimeoutException e) {
			throw new ConnectionTimeoutException();
		} catch (InterruptedIOException e) {
			throw new ConnectionTimeoutException();
		} catch (IOException e) {
			throw new ConnectionClosedException();
		}
		return line.toString();
	}

	/**
	 * Reads the greeting message of an incoming connection request.
	 *
	 * @return the read message.
	 * @throws ConnectionClosedException  the connection was closed by the remote host.
	 * @throws ConnectionTimeoutException the connection has timed out.
	 */
	String readGreeting() throws ConnectionClosedException, ConnectionTimeoutException {
		String greeting = new String();
		String line;
		while ((line = readLine()) != null) {
			greeting = greeting.concat(line + "\n");
			if (line.trim().length() == 0)
				break;
		}
		return greeting;
	}

	/**
	 * Skips the delivered amount of bytes.
	 *
	 * @param len the length to skip.
	 * @throws ConnectionClosedException  the connection was closed by the remote host.
	 * @throws ConnectionTimeoutException the connection has timed out.
	 * @throws IllegalArgumentException   the delivered length to skip was illegal.
	 */
	void skipBytes(long len) throws ConnectionClosedException, ConnectionTimeoutException, IllegalArgumentException {
		long skipLen = 0;
		try {
			if ((len < 1) || (len > 65536)) {
				throw new IllegalArgumentException("len " + String.valueOf(len) + " is illegal!");
			}
			while (skipLen != len) {
				skipLen += mDataReader.skipBytes((int)(len - skipLen));
			}
		} catch (SocketTimeoutException e) {
			throw new ConnectionTimeoutException();
		} catch (InterruptedIOException e) {
			throw new ConnectionTimeoutException();
		} catch (IOException e) {
			throw new ConnectionClosedException();
		}
	}

}
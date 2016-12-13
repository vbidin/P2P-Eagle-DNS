/**
 * $Id: ConnectionWriter.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Communication Writer provides basic functions to write messages to an
 * Output Stream.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ConnectionWriter {

	/**
	 * The data output stream writer.
	 */
	private DataOutputStream mDataWriter = null;

	/**
	 * The output stream.
	 */
	private OutputStream mOut = null;

	/**
	 * Creates a writer.
	 *
	 * @param out the Output Stream.
	 */
	ConnectionWriter(OutputStream out) {
		mOut = out;
		mDataWriter = new DataOutputStream(new BufferedOutputStream(mOut));
	}

	/**
	 * Writes an array of bytes to the Output Stream.
	 *
	 * @param data the array of bytes.
	 * @throws IOException
	 */
	void write(byte[] data) throws IOException {
		mDataWriter.write(data, 0, data.length);
		mDataWriter.flush();
	}

	/**
	 * Writes an array of bytes to the Output Stream.
	 *
	 * @param data  the array of bytes.
	 * @param start the first byte to write.
	 * @param len   the length to write.
	 * @throws IOException
	 */
	void write(byte[] data, int start, int len) throws IOException {
		mDataWriter.write(data, start, len);
		mDataWriter.flush();
	}

}
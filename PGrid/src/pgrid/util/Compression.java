/**
 * Copyright (c) 2003 Roman Schmidt,
 *                    All Rights Reserved.
 *
 * This file is part of the pgrid.utils package.
 * pgrid.utils homepage: http://lsirpeople.epfl.ch/pgrid.helper/pgrid.utils
 *
 * The pgrid.utils package is free software; you can redistribute it and/or
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
 * along with this package; see the file gpl.txt.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid.util;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class compresses and decompresses data using the Java {@link java.util.zip.Deflater} and
 * {@link java.util.zip.Inflater}.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0 2003/04/15
 * @see java.util.zip.Deflater
 * @see java.util.zip.Inflater
 */
public class Compression {

	/**
	 * Creates a new <code>Compression</code>.
	 */
	protected Compression() {
		// do nothing
	}

	/**
	 * Compresses the delivered data with the default level, and returns it.
	 *
	 * @param data   the byte array to compress.
	 * @param offset the first byte to compress.
	 * @param len    the amount of bytes to compress.
	 * @return the compressed bytes.
	 */
	public static byte[] compress(byte[] data, int offset, int len) {
		return compress(data, offset, len, Deflater.DEFAULT_COMPRESSION);
	}

	/**
	 * Compresses the delivered data, and returns it.
	 *
	 * @param data        the byte array to compress.
	 * @param offset      the first byte to compress.
	 * @param len         the amount of bytes to compress.
	 * @param compression the compression level (levels of {@link java.util.zip.Deflater}).
	 * @return the compressed bytes.
	 */
	public static byte[] compress(byte[] data, int offset, int len, int compression) {
		if (data == null)
			return null;
		if ((offset + len) > data.length)
			return null;

		Deflater compresser = new Deflater(compression);
		compresser.setInput(data, offset, len);
		compresser.finish();
		byte[] buffer = new byte[1024];
		int bufferPos = 0;
		while (!compresser.finished()) {
			if (bufferPos >= buffer.length) {
				final byte[] tmp = buffer;
				buffer = new byte[buffer.length + len];
				System.arraycopy(tmp, 0, buffer, 0, tmp.length);
			}
			final int tmpLen = compresser.deflate(buffer, bufferPos, buffer.length - bufferPos);
			bufferPos += tmpLen;
		}
		byte[] ret = new byte[bufferPos];
		System.arraycopy(buffer, 0, ret, 0, bufferPos);
		return ret;
	}

	/**
	 * Decompresses the delivered data, and returns it.
	 *
	 * @param data   the byte array to decompress.
	 * @param offset the first byte to decompress.
	 * @param len    the amount of bytes to decompress.
	 * @return the decompressed bytes.
	 * @throws DataFormatException
	 */
	public static byte[] decompress(byte[] data, int offset, int len) throws DataFormatException {
		if (data == null)
			return null;
		if ((offset + len) > data.length)
			return null;
		Inflater decompresser = new Inflater();
		decompresser.setInput(data, offset, len);
		byte[] buffer = new byte[1024];
		int bufferPos = 0;
		while (decompresser.getRemaining() > 0) {
			if (bufferPos >= buffer.length) {
				byte[] tmp = buffer;
				buffer = new byte[buffer.length + len];
				System.arraycopy(tmp, 0, buffer, 0, tmp.length);
			}
			int tmpLen = decompresser.inflate(buffer, bufferPos, buffer.length - bufferPos);
			bufferPos += tmpLen;
		}

		decompresser.end();
		byte[] ret = new byte[bufferPos];
		System.arraycopy(buffer, 0, ret, 0, bufferPos);
		return ret;
	}

}
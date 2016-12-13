/**
 * $Id $
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
package pgrid.util;

/**
 * This pgrid.helper class take care of encryption.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 */
public class SecurityHelper {

	/**
	 * Encode the given string with the given key
	 *
	 * @param toEncode string to encode
	 * @param key      key to use
	 * @return			encoded string
	 */
	public static String encode(String toEncode, String key) {
		return toEncode;
	}

	/**
	 * Decode the given string with the given key
	 *
	 * @param toEncode string to decode
	 * @param key      key to use
	 * @return			decoded string
	 */
	public static String decode(String toDecode, String key) {
		return toDecode;
	}

	/**
	 * Generate a public and a private key. {public, private}
	 *
	 * @return {public, private}
	 */
	public static String[] generateKeys() {
		String keyPair[] = {"", ""};
		return keyPair;
	}

}

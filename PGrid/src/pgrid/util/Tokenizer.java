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

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This <code>Tokenizer</code> uses the Java {@link java.util.StringTokenizer} to tokenize given strings by a given
 * separator.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0 2003/03/14
 * @see java.util.StringTokenizer
 */
public class Tokenizer {

	/**
	 * Create a new <code>Tokenizer</code>.
	 */
	protected Tokenizer() {
		// do nothing
	}

	/**
	 * Splits the given string by whitespaces in an array of strings.
	 *
	 * @param input the string to split.
	 * @return the delivered string splitted in an array of strings.
	 */
	public static String[] tokenize(String input) {
		return tokenize(input, " ");
	}

	/**
	 * Splits the given string by a given separator in an array of strings.
	 *
	 * @param input     the string to split.
	 * @param separator the splitting string.
	 * @return the delivered string splitted in an array of strings.
	 */
	public static String[] tokenize(String input, String separator) {
		Vector vector = new Vector();
		StringTokenizer strTokens = new StringTokenizer(input, separator);
		String[] strings;

		while (strTokens.hasMoreTokens())
			vector.addElement(strTokens.nextToken());
		strings = new String[vector.size()];
		for (int i = 0; i < strings.length; i++)
			strings[i] = (String)vector.get(i);
		return strings;
	}

}
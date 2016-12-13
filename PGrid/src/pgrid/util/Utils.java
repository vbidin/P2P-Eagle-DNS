/**
 * $Id: Utils.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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

import pgrid.Constants;
import pgrid.core.storage.Signature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class causes periodic Exchanges.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Utils {

	/**
	 * The antilog table file.
	 */
	private static final String ANTILOG_FILE = "antilogTable.dat";

	/**
	 * The log table file.
	 */
	private static final String LOG_FILE = "logTable.dat";

	/**
	 * The antilog table.
	 */
	private static int[] mAntilogTable = null;

	/**
	 * The log table.
	 */
	private static int[] mLogTable = null;

	/**
	 * Constructs the Utils facility.
	 */
	public Utils() {
		if ((mAntilogTable == null) || (mLogTable == null)) {
			mAntilogTable = new int[131071];
			mLogTable = new int[65536];
			try {
				// read antilog table
				InputStream inStream = getClass().getResourceAsStream("/" + ANTILOG_FILE);
				if (inStream == null) {
					Constants.LOGGER.severe("Antilog table '" + ANTILOG_FILE + "' not found!");
					System.exit(-1);
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
				String inputLine;
				int idx = 1;
				while ((inputLine = in.readLine()) != null) {
					mAntilogTable[idx++] = Integer.parseInt(inputLine);
				}
				in.close();

				// read log table
				inStream = getClass().getResourceAsStream("/" + LOG_FILE);
				if (inStream == null) {
					Constants.LOGGER.severe("Log table '" + LOG_FILE + "' not found!");
					System.exit(-1);
				}
				in = new BufferedReader(new InputStreamReader(inStream));
				idx = 1;
				while ((inputLine = in.readLine()) != null) {
					mLogTable[idx++] = Integer.parseInt(inputLine);
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * Returns the common prefix of two deliverd strings.
	 *
	 * @param str1 the first string.
	 * @param str2 the second string.
	 * @return the common prefix string.
	 */
	public static String commonPrefix(String str1, String str2) {
		if ((str1.length() == 0) || (str2.length() == 0))
			return "";
		String prefix = "";
		int length;
		if (str1.length() < str2.length()) {
			length = str1.length();
		} else {
			length = str2.length();
		}
		for (int i = 1; i <= length; i++) {
			if (str1.substring(0, i).equals(str2.substring(0, i))) {
				prefix = str1.substring(0, i);
			} else {
				break;
			}
		}
		return prefix;
	}

	/**
	 * Creates and returns the signature for the given string.
	 *
	 * @param signString the string to sign.
	 * @param pageSize   the maximum page size.
	 * @param signLength the length of each page signature.
	 * @return the created signature.
	 */
	public Signature signature(String signString, int pageSize, int signLength) {
		// transfrom the string
		char[] signChars = signString.toCharArray();
		// calculate the number of required pages
		int pages = signChars.length / pageSize;
		if (signChars.length > (pages * pageSize))
			pages++;
		// create signature object
		Signature signature = new Signature(pages, signLength);
		// process all pages
		for (int page = 0; page < pages; page++) {
			// the first char of this page
			int firstIdx = page * pageSize;
			int lastIdx = Math.min(firstIdx + pageSize, signChars.length);
			// create the page signature with the given signature length
			for (int len = 0; len < signLength; len++) {
				long sign = 0;
				int k = 0;
				for (int i = firstIdx; i < lastIdx; i = i + 2) {
					try {
						sign ^= mAntilogTable[(((len + 1) * k++) + mLogTable[(signChars[i] * 256 + ((i + 1) < lastIdx ? signChars[i + 1] : 0)) % mLogTable.length]) % mAntilogTable.length];
					} catch (Exception e) {
						System.err.println("len = " + len + ", signChars.length = " + signChars.length + ", i = " + i);
						System.err.println("signChars[i] = " + (int)signChars[i] + " signChars[i+1] = " + signChars[i + 1]);
						System.err.println("mLogTable.length = " + mLogTable.length + " index = " + (signChars[i] * 256 + ((i + 1) < lastIdx ? signChars[i + 1] : 0)));
						e.printStackTrace();  //To change body of catch statement use Options | File Templates.
						System.exit(-1);
					}
				}
				signature.setSignature(page, len, sign);
			}
		}
		return signature;
	}

}
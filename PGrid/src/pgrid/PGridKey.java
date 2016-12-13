/**
 * $Id: PGridKey.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
 *
 * Copyright (c) 2005 The P-Grid Team,
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
package pgrid;

import p2p.basic.Key;

/**
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.2.0
 */
public class PGridKey implements Key {

	/**
	 * String representation of the key
	 */
	protected String mKey;

	/**
	 * Constructor
	 *
	 * @param key
	 */
	public PGridKey(String key) {
		mKey = key;
	}

	/**
	 * @see p2p.basic.Key#getBytes()
	 */
	public byte[] getBytes() {
		return mKey.getBytes();
	}

	/**
	 * Append two keys together
	 *
	 * @param toAppend the key to append
	 * @return the new key
	 */
	public Key append(Key toAppend) {
		mKey = mKey + toAppend.toString();
		return this;
	}

	/**
	 * Append two keys together
	 *
	 * @param toAppend the key to append
	 * @return the new key
	 */
	public Key append(String toAppend) {
		mKey = mKey + toAppend;
		return this;
	}

	/**
	 * @see p2p.basic.Key#size()
	 */
	public int size() {
		return mKey.length();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return mKey;
	}

	public boolean equals(Object o) {
		if (!(o instanceof PGridKey)) 
			return false;
		return ((PGridKey)o).mKey.equals(mKey);
	}

}

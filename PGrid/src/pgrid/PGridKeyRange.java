/**
 * $Id: PGridKeyRange.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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
import p2p.basic.KeyRange;
import pgrid.util.PathComparator;

/**
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.2.0
 */
public class PGridKeyRange implements KeyRange {

	/**
	 * Path comparator
	 */
	protected static PathComparator pathComparator = new PathComparator();

	/**
	 * Lower bound of the range
	 */
	protected Key mLowerKey;

	/**
	 * Higher bound of the range
	 */
	protected Key mHigherKey;

	/**
	 * Constructor for a key range
	 *
	 * @param lower  bound of the range
	 * @param higher bound of the range
	 */
	public PGridKeyRange(Key lower, Key higher) {
		mLowerKey = lower;
		mHigherKey = higher;
	}

	/**
	 * @see p2p.basic.KeyRange#getMin()
	 */
	public Key getMin() {
		return mLowerKey;
	}

	/**
	 * @see p2p.basic.KeyRange#getMax()
	 */
	public Key getMax() {
		return mHigherKey;
	}

	public String toString() {
		return mLowerKey.toString()+"-"+mHigherKey.toString();
	}

	/**
	 * @see p2p.basic.KeyRange#withinRange(p2p.basic.Key)
	 */
	public boolean withinRange(Key key) {
		String strKey = key.toString();

		return ((pathComparator.compare(strKey, mLowerKey.toString()) >= 0 &&
				pathComparator.compare(strKey, mHigherKey.toString()) <= 0));
	}

}

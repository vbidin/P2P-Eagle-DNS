/**
 * $Id: ExchangeStatistics.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

package pgrid.core.maintenance;

/**
 * This class stores statistical information about performed exchanges used for load balancing.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ExchangeStatistics {

	/**
	 * The comp. path count.
	 */
	private double mCompPath[] = new double[0];

	/**
	 * The level count.
	 */
	private int mCount[] = new int[0];

	/**
	 * The same path count.
	 */
	private double mSamePath[] = new double[0];

	/**
	 * Creates a new statistics object.
	 */
	ExchangeStatistics() {
	}

	/**
	 * Returns the count for a level.
	 *
	 * @param level the level.
	 * @return the count.
	 */
	double compPath(int level) {
		ensureLength(level - 1);
		return mCompPath[level - 1];
	}

	/**
	 * Returns the count for a level.
	 *
	 * @param level the level.
	 * @return the count.
	 */
	int count(int level) {
		ensureLength(level - 1);
		return mCount[level - 1];
	}

	/**
	 * Ensures, that the arrays are long enough.
	 *
	 * @param level the minimal length of the arrays.
	 */
	private void ensureLength(int level) {
		if (mCompPath.length <= level) {
			double[] tmp = new double[level + 1];
			System.arraycopy(mCompPath, 0, tmp, 0, mCompPath.length);
			mCompPath = tmp;
		}
		if (mCount.length <= level) {
			int[] tmp = new int[level + 1];
			System.arraycopy(mCount, 0, tmp, 0, mCount.length);
			mCount = tmp;
		}
		if (mSamePath.length <= level) {
			double[] tmp = new double[level + 1];
			System.arraycopy(mSamePath, 0, tmp, 0, mSamePath.length);
			mSamePath = tmp;
		}
	}

	/**
	 * Increases the count for comp. path at a level.
	 *
	 * @param level   the level.
	 * @param pathLen the path length.
	 */
	void incCompPath(int level, int pathLen) {
		ensureLength(level - 1);
		mCompPath[level - 1] += (1.0 / Math.pow(2.0, pathLen - (level - 1) - 1));
	}

	/**
	 * Increases the count for a level.
	 *
	 * @param level the level.
	 */
	void incCount(int level) {
		ensureLength(level - 1);
		mCount[level - 1]++;
	}

	/**
	 * Increases the count for same path at a level.
	 *
	 * @param level   the level.
	 * @param pathLen the path length.
	 */
	void incSamePath(int level, int pathLen) {
		ensureLength(level - 1);
		mSamePath[level - 1] += 1.0 / Math.pow(2, pathLen - (level - 1) - 1);
	}

	/**
	 * Resets the statistics.
	 */
	void reset() {
		mCompPath = new double[0];
		mCount = new int[0];
		mSamePath = new double[0];
	}

	/**
	 * Returns the count for a level.
	 *
	 * @param level the level.
	 * @return the count.
	 */
	double samePath(int level) {
		ensureLength(level - 1);
		return mSamePath[level - 1];
	}

}

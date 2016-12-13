/**
 * $Id: TransitionProbability.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

import java.util.Arrays;

/**
 * This class provides transition probabilities for load balancing.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class TransitionProbability {

	/**
	 * The transition probabilities.
	 */
	private double[] mProps = new double[0];

	/**
	 * Creates the transition probabilities.
	 */
	TransitionProbability() {
	}

	/**
	 * Ensures, that the arrays are long enough.
	 *
	 * @param level the minimal length of the arrays.
	 */
	private void ensureLength(int level) {
		if (mProps.length <= level) {
			double[] tmp = new double[level + 1];
			System.arraycopy(mProps, 0, tmp, 0, mProps.length);
			mProps = tmp;
			Arrays.fill(mProps, level, mProps.length, 0);
		}
	}

	/**
	 * Returns the probability for the given level.
	 *
	 * @param level the level.
	 * @return the probability.
	 */
	public double get(int level) {
		ensureLength(level - 1);
		return mProps[level - 1];
	}

	/**
	 * Returns the transition probabilities in descening order by their probability.
	 *
	 * @return the sorted list.
	 */
	public double[][] sort() {
		double[] unsorted = new double[mProps.length];
		System.arraycopy(mProps, 0, unsorted, 0, mProps.length);
		double[][] sorted = new double[2][mProps.length + 1];
		int sortIndex = 1;
		while (true) {
			int index = -1;
			double max = Double.MIN_VALUE;
			for (int j = 0; j < unsorted.length; j++) {
				if (unsorted[j] > max) {
					max = unsorted[j];
					index = j;
				}
			}
			if (max == Double.MIN_VALUE)
				break;
			sorted[0][sortIndex] = index + 1;
			sorted[1][sortIndex++] = max;
			unsorted[index] = Double.MIN_VALUE;
		}
		return sorted;
	}

	/**
	 * Adds the given transition probability to the given level.
	 *
	 * @param level the level.
	 * @param prop  the probability.
	 */
	public void union(int level, double prop) {
		ensureLength(level - 1);
		mProps[level - 1] += prop;
	}

}

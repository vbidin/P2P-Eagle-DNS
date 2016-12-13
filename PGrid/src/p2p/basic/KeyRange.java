/*
$BRICKS_LICENSE$
*/


package p2p.basic;

/**
 * Represents a part of the key space, for example, the keys a
 * node is responsible for. It may be used both for range or
 * prefix-based addressing.
 */

public interface KeyRange {
	/**
	 * Get the lower bound of the key range.
	 *
	 * @return the first key in the range
	 */
	Key getMin();

	/**
	 * Get the upper bound of the key range.
	 *
	 * @return the last key in the range
	 */
	Key getMax();

	/**
	 * Check whether key is inside the range.
	 *
	 * @param key the key to test
	 * @return true if key is inside the key range, false otherwise
	 */
	boolean withinRange(Key key);
}

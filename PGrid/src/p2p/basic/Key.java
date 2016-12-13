/*
$BRICKS_LICENSE$
*/


package p2p.basic;



/**
 * Is used to address items and peers in the
 * distributed indexing structure used by
 * the peer-to-peer network.
 */
public interface Key {

	/**
	 * Returns the value of the key.
	 *
	 * @return returns a byte array the represents the key.
	 */
	public byte[] getBytes();

	/**
	 * Returns the size of the key.
	 * @return the size.
	 */ 
	public int size();

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString();

}


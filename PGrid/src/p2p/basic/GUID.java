/*
$BRICKS_LICENSE$
*/

package p2p.basic;

/**
 * Global Unique Identifier is used to distinguish objects
 * in a distributed environment. Implementations must
 * define the hashCode and equals methods to that effect.
 */
public interface GUID {

	/**
	 * Returns the value of the unique ID.
	 *
	 * @return returns a byte array the represents the unique ID.
	 */
	public byte[] getBytes();

  /**
   * Returns the string representation of the unique ID.
   *
   * @return returns a string that represents the unique ID.
   */
  public String toString();

}

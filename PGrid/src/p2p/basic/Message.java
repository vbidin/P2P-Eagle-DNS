/*
$BRICKS_LICENSE$
*/


package p2p.basic;

/**
 * Defines a message that peers exchange. Most systems will
 * use multiple types of messages in their protocols, and
 * thus several implementations of this interface will be
 * required.
 */
public interface Message {

	/**
	 * Get the message's guid. Useful to determine if a
	 * message has already been observed previously.
	 *
	 * @return the global unique identifier
	 */
	GUID getGUID();

	/**
	 * Get the message content.
	 *
	 * @return a binary representation of the message
	 */
	byte[] getData();
}

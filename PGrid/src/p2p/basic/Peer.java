/*
$BRICKS_LICENSE$
*/

package p2p.basic;

import java.net.InetAddress;

/**
 * Defines the operation that peers support. Each
 * instance provides addressing information on the
 * represented peer.
 */
public interface Peer {

	/**
	 * Get the guid the distiguishes this peer from others.
	 *
	 * @return the global unique identifer
	 */
	public GUID getGUID();

	/**
	 * Get the range for which the peer is responsible.
	 *
	 * @return the KeyRange
	 */
  public KeyRange getKeyRange();

	/**
	 * Get the peer's address
	 *
	 * @return the Internet address
	 */
	public InetAddress getIP();

	/**
	 * Get the peer's service port
	 *
	 * @return the service port number
	 */
	public int getPort();
}
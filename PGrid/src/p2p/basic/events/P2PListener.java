/*
$BRICKS_LICENSE$
*/


package p2p.basic.events;

import p2p.basic.Message;
import p2p.basic.Peer;

/**
 * Defines callback interface to inform listeners of
 * events on the network.
 */
public interface P2PListener {

	/**
	 * Invoked when a new message needs to be delivered to the application.
	 *
	 * @param message the message received
	 * @param origin  the peer from which the message was sent
	 */
	void newMessage(Message message, Peer origin);
}

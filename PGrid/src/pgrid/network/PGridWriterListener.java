package pgrid.network;

import pgrid.network.protocol.PGridMessage;

/**
 * This interface represent a PGrid writer listner. Its use is mainly for
 * debugging and testing.
 * The listener will be triggered iff the TESTS parameter is set to true.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public interface PGridWriterListener {
	/**
	 * This method is call just before a message is written
	 * to the socket
	 * @param msg being written.
	 */
	public void messageWritten(PGridMessage msg);
}

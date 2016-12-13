package pgrid.network;

import pgrid.network.protocol.PGridMessage;

/**
 * This interface represent a PGrid reader listner. Its use is mainly for
 * debugging and testing.
 * The listener will be triggered iff the TESTS parameter is set to true.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public interface PGridReaderListener {
	/**
	 * This method is call just after a message has been read and
	 * decompressed.
	 * @param msg being written.
	 */
	public void messageRead(PGridMessage msg);
}

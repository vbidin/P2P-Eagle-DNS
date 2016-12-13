package pgrid.network.protocol;

import p2p.basic.GUID;

/**
 * Class description goes here
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public interface RoutableMessage {

	/**
	 * Set the number of resolved bit
	 * @param index
	 */
	public void setIndex(int index);

	/**
	 * Get the number of resolved bit
	 */
	public int getIndex();

	/**
	 * Unique identifier for this message
	 * @return The GUID
	 */
	public GUID getGUID();
}

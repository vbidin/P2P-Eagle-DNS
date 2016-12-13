/*
 $BRICKS_LICENSE$
 */

package p2p.basic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Abstract Factory (GoF) that defines the operations that create various
 * objects of the P2P layer. It is recommended to instantiate
 * such types only through a concrete implementation of this factory
 * to avoid hard-coding direct references to them.
 * This class provides static methods to find concrete factories
 * using the reflection API to further decouple the subsystem from its
 * client.
 */
public abstract class P2PFactory {

	/**
	 * Create the concrete P2P implementation.
	 *
	 * @return P2P implementation implementation
	 */
	public abstract P2P createP2P(Properties properties);

	/**
	 * Generate a Key instance compatible with the P2P implementation.
	 * Acceptable source object types depend on implementation.
	 *
	 * @param obj the source object from which to generate the key
	 * @return the generated Key implementation
	 */
	public abstract Key generateKey(Object obj);

	/**
	 * Generate a KeyRange instance compatible with the P2P implementation.
	 * Acceptable source object types depend on implementation.
	 *
	 * @param lowerBound the source object from which to generate the lower key
	 * @param lowerBound the source object from which to generate the higher key
	 * @return the generated Key implementation
	 */
	public abstract KeyRange generateKeyRange(Object lowerBound, Object higherBound);

	/**
	 * Generate a GUID instance compatible with the P2P implementation.
	 *
	 * @return the generated global unique identifier
	 */
	public abstract GUID generateGUID();

	/**
	 * Create a Message instance compatible with the P2P implementation.
	 *
	 * @param contents the binary content to include in the message
	 * @return the Message implementation
	 */
	public abstract Message createMessage(byte[] contents);

	/**
	 * Create a Peer instance compatible with the P2P implementation
	 * based on the provided information.
	 *
	 * @param netAddr the Internet address of the peer
	 * @param port    the service port number of the peer
	 * @return the Peer implementation
	 * @throws UnknownHostException if the given address is invalid.
	 */
	public abstract Peer createPeer(InetAddress netAddr, int port) throws UnknownHostException;

}
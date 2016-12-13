package pgrid.network.router;

import pgrid.core.storage.DistributionListener;
import pgrid.network.protocol.PGridMessage;
import pgrid.network.protocol.PeerLookupMessage;
import pgrid.network.protocol.GenericMessage;
import pgrid.RangeQuery;
import p2p.storage.Query;
import p2p.storage.events.SearchListener;
import p2p.basic.Key;

import java.util.Vector;

/**
 * Implementation of the abstract factory from Gamma et al.
 *
 * This factory provides a way to create routing request easely.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public class RoutingRequestFactory {

	/**
	 * Create a new query routing request
	 * @param query the query
	 * @param listener the listner
	 * @return a QueryRoutingRequest to be used by the router
	 */
	public static QueryRoutingRequest createQueryRoutingRequest(Query query, SearchListener listener) {
		return new QueryRoutingRequest(query, listener);
	}

	/**
	 * Create a new query routing request
	 * @param query the query
	 * @param listener the listner
	 * @return a QueryRoutingRequest to be used by the router
	 */
	public static RangeQueryRoutingRequest createRangeQueryRoutingRequest(RangeQuery query, SearchListener listener) {
		return new RangeQueryRoutingRequest(query, listener, query.getAlgorithm());
	}

	/**
	 * Create a new lookup routing request
	 * @param msg the mesage
	 * @param listener the listner
	 * @return a LookupRoutingRequest to be used by the router
	 */
	public static LookupRoutingRequest createLookupRoutingRequest(PeerLookupMessage msg, SearchListener listener) {
		return new LookupRoutingRequest(msg, listener);
	}

	/**
	 * Create a new Distribution Request
	 * @param key
	 * @param msg
	 * @param listener
	 * @return the new request object
	 */
	public static DistributionRequest createDistributionRoutingRequest(Key key, PGridMessage msg, DistributionListener listener){
		 return new DistributionRequest(key, msg, listener);
	}

	/**
	 * Create a new replicas request. The given message will be sent to all replicas
	 * exept to the one in exclReplicas.
	 * @param message to be sent to replicas
	 * @param exclReplicas list of excluded replicas
	 * @return the new request object
	 */
	public static ReplicasRequest createReplicasRoutingRequest(PGridMessage message, Vector exclReplicas){
		return new ReplicasRequest(message, exclReplicas);
	}

	/**
	 * Create a new generic request.
	 * @param message to be sent
	 * @param locallyStarted true iff the routing process is locally started
	 * @return the new request object
	 */
	public static GenericRoutingRequest createGenericRoutingRequest(GenericMessage message, boolean locallyStarted){
		return new GenericRoutingRequest(message, locallyStarted);
	}
}

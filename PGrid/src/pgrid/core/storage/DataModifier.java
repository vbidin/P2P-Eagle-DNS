package pgrid.core.storage;

import pgrid.*;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.network.MessageManager;
import pgrid.network.protocol.ACKMessage;
import pgrid.network.protocol.DataModifierMessage;
import pgrid.util.Utils;

import java.util.*;
import java.util.logging.Logger;

/**
 * This class processes data modifier requests.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class DataModifier implements DistributionListener {

	/**
	 * Represente a delete operation
	 */
	public static final short DELETE = 0;

	/**
	 * Represente an insert operation
	 */
	public static final short INSERT = 1;

	/**
	 * Represente an update operation
	 */
	public static final short UPDATE = 2;


	/**
	 * The PGrid.Distributor logger.
	 */
	private static final Logger LOGGER = Distributor.LOGGER;

	/**
	 * The already seen messages.
	 */
	private Vector mAlreadySeen = new Vector(100);

	/**
	 * The list of attempts.
	 */
	private Hashtable mAttempts = new Hashtable();

	/**
	 * The list of request
	 */
	private Hashtable mRequests = new Hashtable();

	/**
	 * The distributor.
	 */
	private Distributor mDistributor = null;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The P-Grid instance.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * Creates the insert handler.
	 *
	 * @param distributor the distributor.
	 */
	DataModifier(Distributor distributor) {
		mDistributor = distributor;
	}

	/**
	 * Invoked when data items were not distributed successfully.
	 *
	 * @param guid  the GUID of the distribution request.
	 */
	public void distributionFailed(p2p.basic.GUID guid) {
		LOGGER.config("Data operation request failed.");
		DistributionAttempt attempt = (DistributionAttempt)mAttempts.remove(guid);
		DistributionRequestInt request = (DistributionRequestInt)mRequests.remove(guid);

		if (!attempt.isLocal()) {
			// store data items because they could not be transmitted to a responsible host
			switch (request.getRequest()) {
				case INSERT:
					mPGridP2P.getStorageManager().getDataTable().addAll(attempt.getItems());
					break;
				case UPDATE:
					break;
				case DELETE:
					break;
				default:
					LOGGER.fine("Unknown request type '"+request.getRequest()+".");
					break;
			}

			if (Constants.TESTS) {
				mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
				mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
			}
			// TODO retry to insert
		}
	}

	/**
	 * Invoked when data items were distributed successfully.
	 *
	 * @param guid  the GUID of the distribution request.
	 */
	public void distributionSuccess(p2p.basic.GUID guid) {
		LOGGER.fine("Data operation request succeeded.");


		DistributionAttempt attempt = (DistributionAttempt)mAttempts.remove(guid);
		DistributionRequestInt request = (DistributionRequestInt)mRequests.remove(guid);

		if (attempt.isLocal()) {
			// remove data items because they are now stored at a remote host
			switch (request.getRequest()) {
				case INSERT:
					mPGridP2P.getStorageManager().getDataTable().removeAll(attempt.getItems());
					break;
				case UPDATE:
					break;
				case DELETE:
					break;
				default:
					LOGGER.fine("Unknown request type '"+request.getRequest()+".");
					break;
			}



			if (Constants.TESTS) {
				int count = mPGridP2P.getStorageManager().getDataTable().count();
				int countPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalPath()).size();
				mPGridP2P.getStatistics().DataItemsManaged = count;
				mPGridP2P.getStatistics().DataItemsPath = countPath;
			}
		}
	}

	/**
	 * Perform the processing of the given items in the network.
	 * @param request the request.
	 */
	void process(DistributionRequest request) {
		LOGGER.finest("processing " + request.getItems().size() + " items.");
		if (Constants.TESTS)
			mPGridP2P.getStatistics().UpdatesLocalProcessed++;

		// sort the data items according to their corresponding routing table level
		Vector[] levels = mDistributor.sortByLevel(request.getItems());

		// send insert message for each level
		processPerLevel(request, levels, true);

		// send insert message to all replicas
		processAtReplicas(request, levels[levels.length-1], new Vector(), null);
	}

	/**
	 * Perform the processing of the items at all replicas excluding the already informed replicas.
	 * @param items the items to be processed.
	 * @param exclReplicas the excluded replicas.
	 * @param guid the used message id.
	 */
	private void processAtReplicas(DistributionRequestInt request, Vector items, Vector exclReplicas, GUID guid) {
		if ((items == null) || (items.size() == 0))
			return;

		// create list of replicas the message is sent to excluding all replicas the message was already sent to
		Collection myReplicas = mPGridP2P.getRoutingTable().getReplicaVector();
		Vector replicasToUse = new Vector();
		LOGGER.finest("Perform the processing of " + items.size() + " data items in the replicat subnetwork.");

		for (Iterator it = myReplicas.iterator(); it.hasNext();) {
			PGridHost host = (PGridHost)it.next();
			boolean found = false;
			for (Iterator it2 = exclReplicas.iterator(); it2.hasNext();) {
				PGridHost exclHost = (PGridHost)it2.next();
				if (host.equals(exclHost)) {
					found = true;
					break;
				}
			}
			if (!found)
				replicasToUse.add(host);
		}

		replicasToUse.addAll(exclReplicas);

		// send data modifier message to all replicas
		if (guid == null)
			guid = GUID.getGUID();
		mAlreadySeen.add(guid);
		DataModifierMessage msg = new DataModifierMessage(guid, new PGridKey(mPGridP2P.getLocalPath()), request.getRequest(), items, replicasToUse);
		// save a reference
		//mRequests.put(guid, request);

		mDistributor.routeToReplicas(msg, exclReplicas);
	}

	/**
	 * Perform the processing of the data items ordered by level at a responsible host.
	 * @param levels the data items ordered by level.
	 */
	private void processPerLevel(DistributionRequestInt request, Vector[] levels, boolean local) {
		for (int i = 0; i < levels.length-1; i++) {
			Vector level = levels[i];
			// if no data items for this level exist => next level
			if ((level == null) || (level.size() == 0))
				continue;
			// create common key
			PGridKey key = new PGridKey(mDistributor.commonKeyForLevel(i));

			LOGGER.finest("Perform the processing of " + level.size() + " data items with prefix key " + key + " at host at level " + i);
			GUID guid = GUID.getGUID();
			DataModifierMessage msg = new DataModifierMessage(guid, key, request.getRequest(), level);
			mAttempts.put(guid, new DistributionAttempt(level, local));
			// save a reference
			mRequests.put(guid, request);

			mDistributor.route(key, msg, this);
		}
	}

	/**
	 * Invoked when a new insert request was received by another host.
	 * @param request the insert request.
	 */
	void remoteProcess(RemoteDistributionRequest request) {
		DataModifierMessage msg = (DataModifierMessage)request.getMessage();
		LOGGER.fine("processing remote " + msg.getDataItems().size() + " items.");
		if (Constants.TESTS)
			mPGridP2P.getStatistics().UpdatesRemoteProcessed++;

		boolean replicaBroadcast = false;
		if (msg.getReplicas() != null)
			replicaBroadcast = true;

		// check if the message was already received
		if ((mAlreadySeen.contains(request.getMessage().getGUID()) && (!replicaBroadcast))) {
			ACKMessage ack = new ACKMessage(msg.getGUID(), ACKMessage.CODE_MSG_ALREADY_SEEN);
			mMsgMgr.sendMessage(msg.getHeader().getHost(), ack);
			LOGGER.fine("remote process request already seen.");
			return;
		}

		if (Utils.commonPrefix(mPGridP2P.getLocalPath(), msg.getKey().toString()).length() < msg.getKey().size()) {
			// message not routed correctly => send bad request ACK
			if (Constants.TESTS)
				mPGridP2P.getStatistics().UpdatesBadRequests++;
			if (!replicaBroadcast) {
				ACKMessage ack = new ACKMessage(msg.getGUID(), ACKMessage.CODE_WRONG_ROUTE);
				mMsgMgr.sendMessage(msg.getHeader().getHost(), ack);
				LOGGER.fine("received bad remote process request.");
			}
			return;
		} else {
			// message routed correctly => send ACK if not replica broadcast
			if (!replicaBroadcast) {
				ACKMessage ack = new ACKMessage(msg.getGUID(), ACKMessage.CODE_OK);
				mMsgMgr.sendMessage(msg.getHeader().getHost(), ack);
			}
			LOGGER.fine("process new received request.");

			// sort the data items according to their corresponding routing table level
			Vector[] levels = mDistributor.sortByLevel(msg.getDataItems());

			// send insert message for each level
			processPerLevel(request, levels, false);

			// add items for the local peer to the data table
			Vector localItems = levels[levels.length-1];
			if ((localItems == null) || (localItems.size() == 0))
				return;

			switch (request.getRequest()) {
				case INSERT:
					mPGridP2P.getStorageManager().getDataTable().addAll(localItems);
					break;
				case UPDATE:
					Vector remote = new Vector();
					boolean notResponsable;
					pgrid.DataItem itemTemp;                                                          
					DataItem item;

					for (Iterator it = localItems.iterator(); it.hasNext();) {
						item = (DataItem)it.next();

						// Check if the local host is still responsible for the new key
						itemTemp = (pgrid.DataItem)(item).clone();
						itemTemp.setKey(PGridP2PFactory.sharedInstance().generateKey((String)itemTemp.getData()));
						notResponsable = !mPGridP2P.isLocalPeerResponsible(itemTemp.getKey());


						if (notResponsable) {
							// Those data items will be remove from the local host and send
							// to their responsable host
							remote.add(itemTemp);
							mPGridP2P.getStorageManager().getDataTable().removeDataItem(item);
							LOGGER.finest("Local peer isn't responsible anymore for the updated data item. Old key:"+
							item.getKey()+" new key:"+itemTemp.getKey()+(replicaBroadcast?"(Replicas subnetwork)":"")+".");
						}
						else {
							mPGridP2P.getStorageManager().getDataTable().updateDataItem(item);
							LOGGER.finest("Local peer still responsible of the updated data item. key: "+itemTemp.getKey()+(replicaBroadcast?"(Replicas subnetwork)":"")+".");
						}
					}

					// If some data item have change there responsible host, insert them
					if ((!remote.isEmpty()) && (!replicaBroadcast)) {
						LOGGER.finest("Re-insert the updated data items.");
						mPGridP2P.getStorageManager().insertDataItems(remote);
					}
					break;
				case DELETE:
					mPGridP2P.getStorageManager().getDataTable().removeAll(localItems, msg.getHeader().getHost());
					break;
				default:
					LOGGER.fine("Unknown request type '"+request.getRequest()+".");
					break;
			}

			if (Constants.TESTS) {
				mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
				mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
			}

			// prepare to send insert request to all replicas
			Vector replicas = msg.getReplicas();
			if (replicas == null)
				replicas = new Vector();
			if (msg.getReplicas() != null)
				replicas.add(msg.getHeader().getHost());

			processAtReplicas(request, localItems, replicas, (GUID)msg.getGUID());
		}
	}

}

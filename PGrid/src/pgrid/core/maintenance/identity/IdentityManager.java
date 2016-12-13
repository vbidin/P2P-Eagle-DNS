/**
 * $Id: IdentityManager.java,v 1.3 2005/11/15 09:57:51 john Exp $
 *
 * Copyright (c) 2002 The P-Grid Team,
 *                    All Rights Reserved.
 *
 * This file is part of the P-Grid package.
 * P-Grid homepage: http://www.p-grid.org/
 *
 * The P-Grid package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file LICENSE.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid.core.maintenance.identity;

import p2p.basic.GUID;
import p2p.basic.Key;
import p2p.basic.Peer;
import p2p.storage.*;
import p2p.storage.Query;
import p2p.storage.events.SearchListener;
import pgrid.*;
import pgrid.DataItem;
import pgrid.core.storage.StorageManager;
import pgrid.core.search.SearchManager;
import pgrid.core.maintenance.identity.CoUPolicy;
import pgrid.core.maintenance.identity.CoFPolicy;
import pgrid.core.maintenance.identity.MaintenancePolicy;
import pgrid.core.maintenance.identity.NoMaintenancePolicy;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.storage.PGridStorageFactory;
import pgrid.network.Challenger;
import pgrid.network.Connection;
import pgrid.util.logging.LogFormatter;
import pgrid.util.SecurityHelper;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * This class represents the GUID manager to map GUIDs of peers to current IP addresses and ports.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 * todo: port this manager to the new architecture
 */
public class IdentityManager implements TypeHandler {

	/**
	 * The PGridP2P.Updater logger.
	 */
	public static final Logger LOGGER = Logger.getLogger("PGrid.IdentityManager");

	/**
	 * The logging file.
	 */
	public static final String LOG_FILE = "IdentityManager.log";

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final IdentityManager SHARED_INSTANCE = new IdentityManager();

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The data item type for GUIDs to IP address mappings.
	 */
	private p2p.storage.Type mGUIDType = null;

	/**
	 * The files as hashtable indexed by the file index.
	 */
	private Hashtable mItemsByGUID = new Hashtable();

	/**
	 * The P-Grid facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The search manager.
	 */
	private SearchManager mSearchManager;

	/**
	 * The shutdown flag.
	 */
	private boolean mShutdownFlag = false;

	/**
	 * Leave join management policy
	 */
	private JoinLeaveProtocol mLeaveJoinPolicy = null;

	/**
	 * Mapping of join leave protocol implementation
	 */
	private Hashtable mJoinLeaveProtocols = new Hashtable();

	/**
	 * Maintenance management policy
	 */
	private MaintenancePolicy mMaintenancePolicy = null;

	/**
	 * Mapping of maintenance protocol implementation
	 */
	private Hashtable mMaintenanceProtocols = new Hashtable();

	/**
	 * Public key of this peer
	 */
	private String mPublicKey = "";

	/**
	 * Private key of this peer
	 */
	private String mPrivateKey = "";

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null); //LOG_FILE);
	}

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridP2P directly will get an error at compile-time.
	 */
	protected IdentityManager() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static IdentityManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Invoked when a data item was added to the data table.
	 *
	 * @param item the added data item.
	 */
	public void dataItemAdded(DataItem item) {
		mItemsByGUID.put(item.getPeer().getGUID().toString(), item);
	}

	/**
	 * Invoked when a data item was removed from the data table.
	 *
	 * @param item the removed data item.
	 */
	public void dataItemRemoved(DataItem item) {
		mItemsByGUID.remove(item.getPeer().getGUID().toString());
	}

	/**
	 * Invoked when the data table was cleared.
	 */
	public void dataTableCleared() {
		mItemsByGUID.clear();
	}

	/**
	 * Handles a local search request.
	 *
	 * @param query the query.
	 * @return the list of initialized queries.
	 */
	public AbstractQuery[] search(QueryInterface query) {
		//todo: port
		/*AbstractQuery[] queries = null;
				if (query.getType().getClass() != RQType.class) {
					queries = new Query[1];
					queries[0] = new Query(mPGridP2P.getLocalHost(), ((DataType)query.getType()), query.getLowerBound(), query.getMinSpeed());
					((Query)queries[0]).setKey(PGridP2PFactory.sharedInstance().generateKey(queries[0].getQueryString()));
				} else {
					queries = new RangeQuery[1];
					//todo fix this if it is useful, anyway.
					//TODO: to be removed
					//queries[0] = new RangeQuery(((DataType)query.getType()), query.getLowerBound(), query.getHigherBound(), query.getMinSpeed());

				}
				mSearchManager.search(queries[0]);
				return queries;  */
		return null;
	}

	/**
	 * Returns the data item for the given GUID.
	 *
	 * @param guid the GUID.
	 * @return the data item.
	 */
	public DataItem getItem(GUID guid) {
		return (DataItem)mItemsByGUID.get(guid.toString());
	}

	/**
	 * Load the private key
	 */
	public void loadPrivateKey() {
		//FIXME: add crypto...
		mPrivateKey = "";
	}

	/**
	 * Load the private key
	 */
	public void loadPublicKey() {
		mPublicKey = PGridP2P.sharedInstance().getLocalHost().getPublicKey();
	}

	/**
	 * Return the data type of an identity data item
	 * @return the data type of an identity data item
	 */
	public p2p.storage.Type getType() {
		return mGUIDType;
	}

	/**
	 * Returns the public key
	 *
	 * @return the public key
	 */
	public String getPublicKey() {
		return mPublicKey;
	}

	/**
	 * Returns the private key
	 *
	 * @return the private key
	 */
	public String getPrivateKey() {
		return mPrivateKey;
	}

	/**
	 * Register a new join leave protocol
	 *
	 * @param protocol the new protocol
	 */
	public void registerJoinLeaveProtocol(JoinLeaveProtocol protocol) {
		mJoinLeaveProtocols.put(protocol.getProtocolName(), protocol);
	}

	/**
	 * Register a new maintenance protocol
	 *
	 * @param protocol the new protocol
	 */
	public void registerMaintenanceProtocol(MaintenancePolicy protocol) {
		mMaintenanceProtocols.put(protocol.getProtocolName(), protocol);
	}

	/**
	 * Initializes the identity manager.
	 */
	public void init() {
		/* TODO This implementation wont work behind a router or a firewall
		 * since nothing is done to get the External IP.
		 */

		mPGridP2P = PGridP2P.sharedInstance();
		mSearchManager = SearchManager.sharedInstance();
		mStorageManager = mPGridP2P.getStorageManager();
		// registers as data item type handler
		mGUIDType = PGridStorageFactory.sharedInstance().createType("GUID/Addr");
		mStorageManager.registerTypeHandler(mGUIDType, this);

		// add all already managed data items of GUID type
		// todo put back
		/*if (items != null) {
			for (Iterator it = items.iterator(); it.hasNext();) {
				DataItem item = (DataItem)it.next();
				mItemsByGUID.put(item.getPeer().getGUID().toString(), item);
			}
		}*/

		CoUPolicy codPolicy = new CoUPolicy();
		CoFPolicy cofPolicy = new CoFPolicy();
		NoMaintenancePolicy nonePolicy = new NoMaintenancePolicy();

		// register join-leave protocol implementation
		registerJoinLeaveProtocol(cofPolicy);
		registerJoinLeaveProtocol(codPolicy);
		registerJoinLeaveProtocol(nonePolicy);

		// register maintenance protocol implementation
		registerMaintenanceProtocol(cofPolicy);
		registerMaintenanceProtocol(codPolicy);
		registerMaintenanceProtocol(nonePolicy);

		// select the identity protocol based on property
		Constants.LOGGER.config("Loading identity policy...");
		String protocolName = mPGridP2P.propertyString(Properties.IDENTITY_MAINTENANCE_POLITIC);

		if (protocolName != null) {
			mLeaveJoinPolicy = (JoinLeaveProtocol)(mJoinLeaveProtocols.get(protocolName));
			mMaintenancePolicy = (MaintenancePolicy)(mMaintenanceProtocols.get(protocolName));
		}

		if (mLeaveJoinPolicy == null) {
			Constants.LOGGER.config("Unknown leave - join policy. Using '" + CoUPolicy.PROTOCOL_NAME + "' instead of '" + protocolName + "'.");
			mLeaveJoinPolicy = (JoinLeaveProtocol)(mJoinLeaveProtocols.get(CoUPolicy.PROTOCOL_NAME));
		}

		if (mMaintenancePolicy == null) {
			Constants.LOGGER.config("Unknown maintenance policy. Using '" + CoUPolicy.PROTOCOL_NAME + "' instead of '" + protocolName + "'.");
			mMaintenancePolicy = (MaintenancePolicy)(mMaintenanceProtocols.get(CoUPolicy.PROTOCOL_NAME));
		}
	}

	/**
	 * Perform the Bootstrap phase or the startup phase of the maintenance
	 * algorithm.
	 */
	public void startIdentification() {
		if (PGridP2P.sharedInstance().getRoutingTable().isNewIdentity()) {
			// 3.1	Bootstrap phase
			String keys[] = SecurityHelper.generateKeys();
			mPublicKey = keys[0];
			mPrivateKey = keys[1];

			mPGridP2P.getLocalHost().setPublicKey(mPublicKey);

			mLeaveJoinPolicy.newlyJoined();

		} else {
			loadPrivateKey();
			loadPublicKey();

			mLeaveJoinPolicy.join();
		}

		mStorageManager.saveDataTable();
	}

	/**
	 * Returns a list of all files matching the search string.
	 *
	 * @param query the search query.
	 * @return the list of files.
	 */
	public synchronized Collection handleSearch(ExactQueryInterface query) {
		if (query == null)
			throw new NullPointerException();

		String queryStr = query.getLowerBound();
		if (mShutdownFlag)
			return null;
		Vector result = new Vector();
		DataItem item = (DataItem)mItemsByGUID.get(queryStr);
		if (item != null) {
			result.add(item);
		}

		return result;
	}

	/**
	 * Returns a list of all files in the range of the range query.
	 *
	 * @param query the search query.
	 * @return the list of files.
	 */
	public synchronized Collection handleSearch(RangeQueryInterface query) {

		//TODO Is a range query useful to discover new IP?
	    
		return null;
	}

	/**
	 * Shutdowns the file manager.
	 */
	synchronized public void shutdown() {
		mShutdownFlag = true;
	}

	/**
	 * This method should be called when a host is stale. Some decision may be taken
	 * depending on the maintenance policy.
	 *
	 * @param host stale host
	 * @return		true if the routing table has been updated
	 */
	public boolean stale(PGridHost host) {
		return mMaintenancePolicy.stale(host);
	}

	/**
	 * This method handle the update of an ID - IP mMapping. It check if it knows
	 * the item to update and if the signature is correct. If everything is correct
	 * it perform the update.
	 *
	 * @see pgrid.DataTypeHandler#handleUpdate(pgrid.DataItem)
	 */
	public boolean handleUpdate(DataItem item) {
		return mMaintenancePolicy.handleUpdate(item);
	}

	/**
	 * Start a challenge-response with the given host.
	 *
	 * @param host to challenge
	 * @return true is host has succeeded.
	 */
	public boolean challengeHost(PGridHost host, Connection conn) {
		Challenger challenger = new Challenger(host);

		return challenger.challengeHost(conn);
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @return a DataItem instance
	 */
	public p2p.storage.DataItem createDataItem() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 */
	public p2p.storage.DataItem createDataItem(Object data) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param guid the guid of the data.
	 * @param key  the key generated of the data.
	 * @param host the host.
	 * @param data the encapsulated data.
	 * @return a DataItem instance
	 */
	public p2p.storage.DataItem createDataItem(GUID guid, Key key, Peer host, Object data) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Searches localy for all dataitems matching the query query
	 *
	 * @param query	the query.
	 * @param listener the search listener.
	 */
	public void handleLocalSearch(Query query, SearchListener listener) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Construct the string out of the lowerbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a lowerbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The lower bound will be inlcuded in
	 * the query, but the lower bound used for the searching will be the return value
	 * of this method.
	 * <br/>
	 * <br/>
	 * If you want to search with the lower bound, either return null or the lower bound
	 *
	 * @param query the query being processed.
	 * @return a string use to perform the search
	 */
	public String submitSearchLowerBoundValue(Query query) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Construct the string out of the higherbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a higherbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The higher bound will be inlcuded in
	 * the query, but the higher bound used for the searching will be the return value
	 * of this method.
	 * <br/>
	 * <br/>
	 * If you want to search with the higher bound, either return null or the lower bound
	 *
	 * @param query the query being processed.
	 * @return a string use to perform the search
	 */
	public String submitSearchHigherBoundValue(Query query) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
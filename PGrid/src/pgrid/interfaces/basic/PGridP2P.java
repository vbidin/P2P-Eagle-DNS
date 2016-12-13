/**
 * $Id: PGridP2P.java,v 1.6 2005/12/19 18:12:40 john Exp $
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

package pgrid.interfaces.basic;

import p2p.basic.Key;
import p2p.basic.Message;
import p2p.basic.Peer;
import p2p.basic.KeyRange;
import p2p.basic.P2P;
import p2p.basic.events.P2PListener;
import pgrid.core.search.SearchManager;
import pgrid.core.storage.StorageManager;
import pgrid.core.maintenance.identity.IdentityManager;
import pgrid.core.*;
import pgrid.core.maintenance.*;
import pgrid.network.Challenger;
import pgrid.network.ConnectionManager;
import pgrid.network.MessageManager;
import pgrid.network.lookup.LookupManager;
import pgrid.network.generic.GenericManager;
import pgrid.network.router.Router;
import pgrid.network.router.RoutingRequestFactory;
import pgrid.network.protocol.*;
import pgrid.*;
import pgrid.util.PathComparator;
import pgrid.util.Tokenizer;
import pgrid.util.Utils;

import java.util.*;
import java.net.UnknownHostException;
import java.net.InetAddress;

/**
 * This class represents the PGridP2P facility.
 * It is responsible for all activities in the Gridella network (search,
 * exchange).
 * This class implements the <code>Singleton</code> pattern as defined by
 * Gamma et.al. As there could only exist one instance of this class, other
 * clients must use the <code>sharedInstance</code> function to use this class.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridP2P implements P2P {

	/**
	 * The key for the debug level used to initialize the P-Grid facility.
	 */
	public static final String PROP_DEBUG_LEVEL = "DebugLevel";

	/**
	 * The key for the local port used to initialize the P-Grid facility.
	 */
	public static final String PROP_LOCAL_PORT = "LocalPort";

	/**
	 * The key for the log file used to initialize the P-Grid facility.
	 */
	public static final String PROP_LOG_FILE = "LogFile";

	/**
	 * The key for the property file used to initialize the P-Grid facility.
	 */
	public static final String PROP_PROPERTY_FILE = "PropertyFile";

	/**
	 * The key for the start listener flag used to initialize the P-Grid facility.
	 */
	public static final String PROP_START_LISTENER = pgrid.Properties.START_LISTENER;

	/**
	 * The key for the verbose mode used to initialize the P-Grid facility.
	 */
	public static final String PROP_VERBOSE_MODE = "VerboseMode";

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final PGridP2P SHARED_INSTANCE = new PGridP2P();

	/**
	 * The load balancer.
	 */
	private pgrid.core.maintenance.ReplicationBalancer mBalancer = null;

	/**
	 * The load balancer thread.
	 */
	private Thread mBalancerThread = null;

	/**
	 * The list of bootstrap hosts used to join the network at the first time.
	 */
	private Vector mBootstrapHosts = new Vector();

	/**
	 * The Data Manager.
	 */
	private ConnectionManager mConnManager = ConnectionManager.sharedInstance();

	/**
	 * The Identity manager.
	 */
	private IdentityManager mIdentMgr = IdentityManager.sharedInstance();

	/**
	 * The Maintencance Manager.
	 */
	private MaintenanceManager mMaintencanceMgr = null;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgManager = MessageManager.sharedInstance();

	/**
	 * Lookup manager
	 */
	private LookupManager mLookupManager;

	/**
	 * The generic manager
	 */
	private GenericManager mGenericManager;

	/**
	 * The PGridP2P indexing tree.
	 */
	private PGridTree mPGridTree = new PGridTree();

	/**
	 * The application properties.
	 */
	private pgrid.Properties mProperties = new pgrid.Properties();

	/**
	 * The PGrid router.
	 */
	private Router mRouter = null;

	/**
	 * The routing table.
	 */
	private LocalRoutingTable mRoutingTable = null;

	/**
	 * The search manager.
	 */
	private SearchManager mSearchManager = SearchManager.sharedInstance();

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The P-Grid statistics.
	 */
	private Statistics mStatistics = null;

	//@todo: planetlab
	public Hashtable mRangeQueries = new Hashtable();

	/**
	 * List of listeners to messages
	 */
	private Vector listeners = new Vector();


	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridP2P directly will get an error at compile-time.
	 */
	protected PGridP2P() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static PGridP2P sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Initializes the P-Grid facility with the given properties.
	 *
	 * @param properties further initialization properties.
	 */
	synchronized public void init(java.util.Properties properties) {
		// use constants for properties
		int debugLevel = -1;
		int localPort = Constants.DEFAULT_PORT;
		String logFile = Constants.LOG_FILE;
		String propFile = Constants.PROPERTY_FILE;
		boolean startListener = true;
		boolean verboseMode = false;

		// get user properties for the logger
		if (properties != null) {
			if (properties.containsKey(PROP_DEBUG_LEVEL))
				debugLevel = Integer.parseInt(properties.getProperty(PROP_DEBUG_LEVEL));
			if (properties.containsKey(PROP_LOCAL_PORT))
				localPort = Integer.parseInt(properties.getProperty(PROP_LOCAL_PORT));
			if (properties.containsKey(PROP_LOG_FILE))
				logFile = properties.getProperty(PROP_LOG_FILE);
			if (properties.containsKey(PROP_PROPERTY_FILE))
				propFile = properties.getProperty(PROP_PROPERTY_FILE);
			if (properties.containsKey(PROP_START_LISTENER))
				startListener = Boolean.valueOf(properties.getProperty(PROP_START_LISTENER)).booleanValue();
			if (properties.containsKey(PROP_VERBOSE_MODE))
				verboseMode = Boolean.valueOf(properties.getProperty(PROP_VERBOSE_MODE)).booleanValue();
		}

		// init logging facility
		Constants.initLogger(null, debugLevel, verboseMode, logFile);
		Constants.LOGGER.info("starting P-Grid " + Constants.BUILD + " ...");

		// load the properties from the property file and override them by the given properties
		mProperties.init(propFile, properties);

		// create the statistic facility
		mStatistics = new Statistics();
		// init the statistics
		if (Constants.TESTS)
			mStatistics.init();

		// PGrid Routing Table
		Constants.LOGGER.config("initializing P-Grid Routing Table ...");
		mRoutingTable = new LocalRoutingTable(Constants.DATA_DIR+propertyString(pgrid.Properties.ROUTING_TABLE), localPort);

		// set local host properties
		getLocalHost().setSpeed(propertyInteger(pgrid.Properties.CONNECTION_SPEED));

		// Router
		mRouter = new Router();
		Thread router = new Thread(mRouter, "Router");
		router.setDaemon(true);
		router.start();

		// Storage Manager
		Constants.LOGGER.config("starting Storage Manager ...");
		mStorageManager = StorageManager.getInstance();
		mStorageManager.init(propertyString(pgrid.Properties.DATA_TABLE), getLocalHost());
		//mStorageManager.registerDataItemClass(new GUIDType(), XMLIdentityDataItem.class);

		// Connection Manager
		Constants.LOGGER.config("starting Connection Manager ...");
		mConnManager.init(startListener);

		// Message Manager
		Constants.LOGGER.config("starting Message Manager ...");
		mMsgManager.init();

		// PGrid Identity Manager
		// todo: rework the identity part to be complient with the new architecture
		/*
		Constants.LOGGER.config("starting P-Grid Identity Manager ...");
		mIdentMgr.init();

		// PGrid Identity Manager
		Constants.LOGGER.config("start identification ...");
		mIdentMgr.startIdentification();
		*/

		// PGrid tree
		Constants.LOGGER.config("starting P-Grid Mapping Tree ...");
		mPGridTree.init();

		// Maintenance Manager
		Constants.LOGGER.config("starting Maintenance Manager ...");
		mMaintencanceMgr = new MaintenanceManager(this);
		Thread maintenanceThread = new Thread(mMaintencanceMgr, "Maintenance Manager");
		maintenanceThread.setDaemon(true);
		maintenanceThread.start();

		// Lookup Manager
		Constants.LOGGER.config("starting Lookup Manager ...");
		mLookupManager = new LookupManager();

		// Search Manager
		Constants.LOGGER.config("starting Search Manager ...");
		mSearchManager.init();
		Thread searchThread = new Thread(mSearchManager, "Search Manager");
		searchThread.setDaemon(true);
		searchThread.start();

		// Load ReplicationBalancer
		mBalancer = new pgrid.core.maintenance.ReplicationBalancer();
		mBalancerThread = new Thread(mBalancer, "Replication Balancer");
		mBalancerThread.setDaemon(true);
		// mBalancerThread.start();


		mGenericManager = new GenericManager();
		mGenericManager.init();
	}

  /**
   * Checks if the local peer is responsible for the given key.
   *
   * @param key the key to check.
   * @return <code>true</code> if the local peer is responsible, <code>false</code> otherwise.
   */
  public boolean isLocalPeerResponsible(Key key) {
	if (key == null)
	  throw new NullPointerException();

	String compath = Utils.commonPrefix(key.toString(), getLocalPath());
	if ((compath.length() == key.size()) || (compath.length() == getLocalPath().length()) || getLocalPath().length() == 0)
	  return true;
	else
	  return false;
  }

  /**
   * Checks if the local peer is responsible for the given key range.
   *
   * @param key the key range to check.
   * @return <code>true</code> if the local peer is responsible for at least a part of the range, <code>false</code> otherwise.
   */
  public boolean isLocalPeerResponsible(KeyRange key) {
	  if (key == null)
		  throw new NullPointerException();

	  PathComparator pathComparator = new PathComparator();

	  return getLocalPath().length() == 0 ||
			  ((pathComparator.compare(getLocalPath(), key.getMin().toString()) >= 0) &&
					  (pathComparator.compare(getLocalPath(), key.getMax().toString()) <= 0));
  }

	/**
	 * Joins the network.
	 */
	public void join() {
		mMaintencanceMgr.join();
	}

	/**
	 * @see p2p.basic.P2P#join(p2p.basic.Peer)
	 */
	public void join(Peer peer) {
		if (peer == null)
			throw new NullPointerException();

		// check if the peer is equal to the local peer
		PGridHost host = PGridHost.toPGridHost(peer);

		// bootstrap with the given host
		mMaintencanceMgr.bootstrap(host);
	}

	/**
	 * @see p2p.basic.P2P#leave()
	 */
	public void leave() {
		// do nothing
	}

	/**
	 * @see p2p.basic.P2P#lookup(p2p.basic.Key, long timeout)
	 */
	public Peer lookup(Key key, long timeout) {
		PeerLookupMessage msg = new PeerLookupMessage(new pgrid.GUID(),
				key.toString(),
				PGridP2P.sharedInstance().getLocalHost(), PeerLookupMessage.ANY);

		return mLookupManager.synchronousPeerLookup(msg, timeout);
	}

	/**
	 * @see p2p.basic.P2P#route(p2p.basic.Key, p2p.basic.Message)
	 */
	public void route(Key key, Message message) {
		// set message destination key and let PGridP2P route it

		((GenericMessage)message).setKeyRange(new PGridKeyRange(key, key));
		mGenericManager.newGenericMessage((GenericMessage)message);
	}

	/**
	 * @see p2p.basic.P2P#route(p2p.basic.Key[], p2p.basic.Message[])
	 */
	public void route(Key[] keys, Message[] message) {
		// TODO: to be tested.
		// THIS IS WRONG!!! the GUID change. A new type of message have to be introduice
		// that can encapsulate multiple messages.
		for (int i = 0; i < keys.length; i++) {
			GenericMessage msg = new GenericMessage(keys[i], message[i].getData());
			mGenericManager.newGenericMessage(msg);

		}

	}

	/**
	 * @see p2p.basic.P2P#route(p2p.basic.KeyRange, p2p.basic.Message)
	 */
	public void route(KeyRange range, Message message) {
		((GenericMessage)message).setKeyRange(range);
		mGenericManager.newGenericMessage((GenericMessage)message);
	}

	/**
	 * @see p2p.basic.P2P#send(p2p.basic.Peer, p2p.basic.Message)
	 */
	public void send(Peer peer, Message message) {
		mMsgManager.sendMessage((PGridHost)peer, (PGridMessage)message, null);
	}

	/**
	 * @see p2p.basic.P2P#routeToReplicas(p2p.basic.Message)
	 */
	public void routeToReplicas(Message message) {
		mRouter.route(RoutingRequestFactory.createReplicasRoutingRequest((PGridMessage)message, null));
	}

	/**
	 * @see p2p.basic.P2P#getLocalPeer()
	 */
	public Peer getLocalPeer() {
		return getLocalHost();
	}

	/**
	 * @see p2p.basic.P2P#getNeighbors()
	 */
	public Peer[] getNeighbors() {
		RoutingTable rt = getRoutingTable();
		List neighbors = rt.getAllReferences();

		return (Peer[])neighbors.toArray(new Peer[neighbors.size()]);
	}

	/**
	 * Register an object to be notified of new messages.
	 *
	 * @param listener the P2PListener implementation to register
	 */
	public void addP2PListener(P2PListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove registration of a current listener of new messages.
	 *
	 * @param listener the P2PListener implementation to unregister
	 */
	public void removeP2PListener(P2PListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Handles a new received generic message.
	 *
	 * @param generic the generic message.
	 * @param origin the originating host.
	 */
	public void newGenericMessage(GenericMessage generic, Peer origin) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			P2PListener listener = (P2PListener)it.next();
			listener.newMessage(generic, origin);
		}
	}

	/**
	 * Returns the P-Grid lookup Manager.
	 * @return the lookup Manager.
	 */
	public LookupManager getLookupManager() {
		return mLookupManager;
	}

	/**
	 * Returns the P-Grid Maintenance Manager.
	 * @return the Maintenance Manager.
	 */
	public MaintenanceManager getMaintenanceManager() {
		return mMaintencanceMgr;
	}

	/**
	 * Returns the P-Grid Search Manager.
	 * @return the Search Manager.
	 */
	public SearchManager getSearchManager() {
		return mSearchManager;
	}

	/**
	 * Returns the P-Grid Storage Manager.
	 * @return the Storage Manager.
	 */
	public StorageManager getStorageManager() {
		return mStorageManager;
	}

	/**
	 * Returns the P-Grid Generic Manager.
	 * @return the Generic Manager.
	 */
	public GenericManager getGenericManager() {
		return mGenericManager;
	}

	/**
	 * Returns the local P-Grid router.
	 * @return the router.
	 */
	public Router getRouter() {
		return mRouter;
	}

	/**
	 * Checks if the given host is the local host.
	 * @param host the host to check.
	 * @return <tt>true</tt> if the host is the local host, <tt>false</tt> otherwise.
	 */
	public boolean isLocalHost(PGridHost host) {
		if (host.equals(getLocalHost())) {
			return true;
		}
		if (((host.getIP().getCanonicalHostName().equals(getLocalPeer().getIP().getCanonicalHostName()))) &&
			  (host.getPort() == getLocalHost().getPort())) {
			return true;
		}
		return false;
	}

	/**
	 * Invoked when a new challenge was received.
	 *
	 * @param host  the sending host.
	 * @param challenge the received challenge.
	 */
	public void newChallenge(PGridHost host, ChallengeMessage challenge) {
		Thread t = new Thread(new Challenger(host, challenge));
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Invoked when a new search path was received.
	 *
	 * @param host       the sending host.
	 * @param searchPath the search path message.
	 */
	public void newSearchPath(PGridHost host, SearchPathMessage searchPath) {
		mBalancer.remoteSearchPath(host, searchPath);
	}

	/**
	 * Resumes the replication balancer.
	 * This method is invoked after each exchange.
	 */
	public void replicationBalance() {
		synchronized (mBalancerThread) {
			mBalancerThread.interrupt();
		}
	}

	/**
	 * Resets P-Grid by removing all hosts from the local routing table and all items from the local data table.
	 */
	public void reset() {
		Constants.LOGGER.info("reset P-Grid by clearing data table and routing table.");
		mMaintencanceMgr.reset();
		setLocalPath("");
		mStorageManager.getDataTable().clear();
		mRoutingTable.clear();
		mStorageManager.getDataTable().init((PGridHost)getLocalPeer());
		mRoutingTable.addFidget(getLocalHost());
		mRoutingTable.save();
	}

	/**
	 * Tests if PGridP2P automatically tries to initiate Exchanges.
	 *
	 * @return <code>true</code>, or <code>false</code>.
	 */
	public boolean getInitExchanges() {
		return propertyBoolean(pgrid.Properties.INIT_EXCHANGES);
	}

	/**
	 * Sets if PGridP2P should automatically initiate Exchanges.
	 *
	 * @param flag automatically initiate, or not.
	 */
	public void setInitExchanges(boolean flag) {
		if (flag == propertyBoolean(pgrid.Properties.INIT_EXCHANGES))
			return;
		if (flag) {
			setProperty(pgrid.Properties.INIT_EXCHANGES, Boolean.toString(true));
			mStatistics.InitExchanges = 1;
		} else {
			setProperty(pgrid.Properties.INIT_EXCHANGES, Boolean.toString(false));
			mStatistics.InitExchanges = 0;
		}
	}

	/**
	 * Returns the load balancer.
	 *
	 * @return the load balancer.
	 */
	public pgrid.core.maintenance.ReplicationBalancer getBalancer() {
		return mBalancer;
	}

	/**
	 * Returns the list of bootstrap hosts.
	 *
	 * @return the list of bootstrap hosts.
	 */
	public Vector getBootstrapHosts() {
		return mBootstrapHosts;
	}

	/**
	 * Updates the list of bootstrap hosts.
	 */
	public void updateBootstrapHosts() {

		mBootstrapHosts.clear();
		String[] hostStr = pgrid.util.Tokenizer.tokenize(propertyString(pgrid.Properties.BOOTSTRAP_HOSTS), ";");

		for (int i = 0; i < hostStr.length; i++) {
			String[] parts = Tokenizer.tokenize(hostStr[i], ":");
			PGridHost host = null;
			int port = Constants.DEFAULT_PORT;
			if (parts.length > 1) {
				try {
					port = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					port = Constants.DEFAULT_PORT;
				}
			}
			try {
				host = (PGridHost)PGridP2PFactory.sharedInstance().createPeer(InetAddress.getByName(parts[0]), port);
			} catch (UnknownHostException e) {
			}
			try {
				host.resolve();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				continue;
			}

			if (((host.getAddressString() != null) && (host.getPort() > 0)) && (!((Host)host).equals(getLocalHost()))) {
				mMaintencanceMgr.bootstrap(host);
			}
		}

	}

	/**
	 * Returns the local host.
	 *
	 * @return the local host.
	 */
	public PGridHost getLocalHost() {
		return mRoutingTable.getLocalHost();
	}

	/**
	 * Returns the local path.
	 *
	 * @return the local path.
	 */
	public String getLocalPath() {
		return mRoutingTable.getLocalHost().getPath();
	}

	/**
	 * Sets the local path.
	 *
	 * @param path the local path.
	 */
	public synchronized void setLocalPath(String path) {
		if (mRoutingTable != null) {
			mRoutingTable.getLocalHost().setPath(path, Long.MAX_VALUE);
			for (int i = mRoutingTable.getLevelCount() - 1; i >= path.length(); i--) {
				mRoutingTable.removeLevel(i);
			}
		}
		mStatistics.PathLength = path.length();
	}

	/**
	 * Returns the local PGridP2P Routing Table.
	 *
	 * @return the Routing Table.
	 */
	public LocalRoutingTable getRoutingTable() {
		return mRoutingTable;
	}

	/**
	 * Returns the P-Grid statistics.
	 *
	 * @return the P-Grid statistics.
	 */
	public Statistics getStatistics() {
		return mStatistics;
	}

	/**
	 * Returns the default property value as boolean.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public boolean defaultPropertyBoolean(String key) {
		return mProperties.getDefaultBoolean(key);
	}

	/**
	 * Returns the default property value as integer.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public int defaultPropertyInteger(String key) {
		return mProperties.getDefaultInteger(key);
	}

	/**
	 * Returns the default property value as string.
	 *
	 * @param key the key of the property.
	 * @return the default value of the property.
	 */
	public String defaultPropertyString(String key) {
		return mProperties.getDefaultString(key);
	}

	/**
	 * Returns the property value as boolean.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public boolean propertyBoolean(String key) {
		return mProperties.getBoolean(key);
	}

	/**
	 * Returns the property value as integer.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public int propertyInteger(String key) {
		return mProperties.getInteger(key);
	}

	/**
	 * Returns the property value as long.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public long propertyLong(String key) {
		return mProperties.getLong(key);
	}

	/**
	 * Returns the property value as string.
	 *
	 * @param key the key of the property.
	 * @return the value of the property.
	 */
	public String propertyString(String key) {
		return mProperties.getString(key);
	}

	/**
	 * Sets the property value by the delivered string.
	 *
	 * @param key   the key of the property.
	 * @param value the value of the property.
	 */
	public void setProperty(String key, String value) {
		mProperties.setString(key, value);
	}

	/**
	 * Shutdowns the P-Grid facility.
	 */
	synchronized public void shutdown() {
		Constants.LOGGER.info("Shutdown P-Grid ...");
		// pgrid.core.Exchanger.shutdown();
		//mStorageManager.shutdown();
		mSearchManager.shutdown();
		mMaintencanceMgr.shutdown();
		mRouter.shutdown();
		mRoutingTable.shutdown();
		mStatistics.shutdown();

	}

}
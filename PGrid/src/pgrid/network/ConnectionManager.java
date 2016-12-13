/**
 * $Id: ConnectionManager.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

package pgrid.network;

import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.PGridHost;
import pgrid.Properties;
import pgrid.core.maintenance.identity.IdentityManager;
import pgrid.network.protocol.PGridMessage;

import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The Communication Manager adminstrates all connection to other hosts.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ConnectionManager {

	/**
	 * Offline period of a host
	 */
	//private static final int OFFLINE_PERIOD = 1000*20; // 20s for testes
	private static final int OFFLINE_PERIOD = 1000 * 60 * 2; // 5m.

	/**
	 * Timout to wait for a connection.
	 */
	private static final int CONNECT_TIMEOUT = 1000 * 60 * 4; // ~ 2m.

	/**
	 * Timout to wait for a connection.
	 */
	private static final int CONNECTING_TIMEOUT = 1000 * 20; // ~ 20.

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final ConnectionManager SHARED_INSTANCE = new ConnectionManager();

	/**
	 * Timout to wait for a message to receive.
	 */
	private static final int SO_TIMEOUT = 30000; // ~ 10 sec.

	/**
	 * List of accepting connections.
	 */
	private Vector mAcceptances = new Vector();

	/**
	 * List of connecting connections.
	 */
	private Hashtable mConnectings = new Hashtable();

	/**
	 * List of waiters on connections.
	 */
	private Hashtable mConnectingWaiter = new Hashtable();

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = null;

	/**
	 * Hashtable of all PGridP2P connections, indexed by the GUID.
	 */
	private Hashtable mConnections = new Hashtable();

	/**
	 * Hashtable of all Writers, by Host GUID.
	 */
	private Hashtable mWriters = new Hashtable();

	/**
	 * Hashtable of all failed connections, by Host GUID.
	 */
	private Hashtable mFailedConnections = new Hashtable();
	
	/**
	 * Hashtable of all timestamp of offline host, by Host GUID.
	 */
	//private Hashtable mOfflineHostTimestamps = new Hashtable();
	
	/**
	 * The Identity Manager.
	 */
	private IdentityManager mIdentMgr = null;

	/**
	 * True if connections must be challenged before acceptance
	 */
	private boolean mSecuredConnection = false;

	/**
	 * Number of connection attemps.
	 */
	protected int mAttemps;

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridP2P directly will get an error at compile-time.
	 */
	protected ConnectionManager() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static ConnectionManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Processes an incoming connection accepted by the CommListener.
	 *
	 * @param socket the socket.
	 */
	void accept(Socket socket) {
		try {
			socket.setSoTimeout(SO_TIMEOUT);
			socket.setTcpNoDelay(true);
		} catch (SocketException e) {
			// do nothing
		}                                                 
		Connection conn = new Connection(socket);
		conn.setStatus(Connection.STATUS_ACCEPTING);
		mAcceptances.add(conn);
		Thread t = new Thread(new Acceptor(conn), "Acceptor");
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Processes an incoming connection.
	 *
	 * @param socket   the socket.
	 * @param greeting the already received greeting.
	 */
	public void accept(Socket socket, String greeting) {
		try {
			socket.setSoTimeout(SO_TIMEOUT);
			socket.setTcpNoDelay(true);
		} catch (SocketException e) {
			// do nothing
		}
		Connection conn = new Connection(socket);
		conn.setStatus(Connection.STATUS_ACCEPTING);
		mAcceptances.add(conn);
		Thread t = new Thread(new Acceptor(conn, greeting), "Acceptor");
		t.setDaemon(true);
		t.start();
	}

	/**
	 * The acceptance of an incoming connection is finished.
	 *
	 * @param conn connection.
	 */
	public void acceptanceFinished(Connection conn) {
		mAcceptances.remove(conn);
		if (conn.getStatus() == Connection.STATUS_CONNECTED) {
			// check if a connection already exists
			Connection oldConn = (Connection)mConnections.get(conn.getHost().getGUID());
			if (oldConn != null) {
				// a connection already exists => disconnect new one and return
				Constants.LOGGER.info("Additional connection (" + conn.getStatusString() + ") to host " + conn.getHost().toHostString() + " closed.");
				conn.close();
				return;
			}
			mConnections.put(conn.getHost().getGUID(), conn);
			mWriters.put(conn.getHost().getGUID(), new PGridWriter(conn));
			Thread t = new Thread(new PGridReader(conn, mMsgMgr), "Reader for '" + conn.getHost().toHostString() + "' - " + conn.hashCode());
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * Connects the host with the given protocol.
	 *
	 * @param host the host.
	 * @return the connection.
	 */
	public Connection connect(PGridHost host) {
		Connection conn;
		Object waiter = null;
		Object connectingWaiter = null;
		boolean stop = false;

		// try to find existing connection, if a PGrid connection is requested and the host is no bootstrap host
		// create a queue if needed
		synchronized (host) {
			connectingWaiter = (Object)mConnectingWaiter.get(host);
			// give the coin to only one thread
			if (connectingWaiter == null) {
				connectingWaiter = new Object();
				mConnectingWaiter.put(host, connectingWaiter);
			}
		}
		synchronized (connectingWaiter) {
			// if the thread does not have the coin, wait
			while (!stop) {
				if ((host != null) && (host.getGUID() != null)) {
					conn = (Connection)mConnections.get(host.getGUID());

					if (conn != null) {
						return conn;
					}
				}

				waiter = mConnectings.get(host);
				if (waiter != null) {
					try {
						connectingWaiter.wait(CONNECTING_TIMEOUT);
					} catch (InterruptedException e) {
						// do nothing here
					}
					if (host.getState() != PGridHost.HOST_OK) {
						return null;
					}
				} else {
					//give the coin to only one thread
					waiter = new Object();
					mConnectings.put(host, waiter);
					stop = true;
				}

			}

		}

		// establish new connection
		conn = new Connection(host);
		conn.setStatus(Connection.STATUS_CONNECTING);
		Thread t = new Thread(new Connector(conn), "Connect to '" + host.toHostString() + "' - " + conn.hashCode());
		t.setDaemon(true);
		t.start();
		// wait for established connection

		synchronized (waiter) {
			while(conn.getStatus() == Connection.STATUS_CONNECTING) {
				try {
					waiter.wait(CONNECT_TIMEOUT);
				} catch (InterruptedException e) {
				}
			}
		}
		Connection newConn = null;

		if (host.getGUID() != null)
			newConn = (Connection)mConnections.get(host.getGUID());

		synchronized (connectingWaiter) {
			mConnectings.remove(host);
			connectingWaiter.notifyAll();
		}
		return (newConn == null ? conn : newConn);
	}

	/**
	 * Establishing a connection has finished.
	 *
	 * @param conn the connection.
	 * @param guid an GUID containing further informations.
	 */
	void connectingFinished(Connection conn, pgrid.GUID guid) {
		boolean challengeSucceeded = true;
		boolean bootstrap = false;
		PGridHost host = conn.getHost();
		Thread readerThread = null;

		// if the host uses a temp. GUID (because it was not know before) => set the correct guid
		// INFO (Roman): I changed it to guid only because the GUID is temp. for bootstrap requests
		if (host.isGUIDTmp())
			host.setGUID(guid);

		// connection has been established
		if (conn.getStatus() == Connection.STATUS_CONNECTED) {
			if (host.getGUID() == null) {
				bootstrap = true;
				host.setGUID(guid);
			}
			mWriters.put(host.getGUID(), new PGridWriter(conn));
			readerThread = new Thread(new PGridReader(conn, mMsgMgr), "Reader for '" + host.toHostString() + "' - " + conn.hashCode());
			readerThread.setDaemon(true);
			readerThread.start();
			if (mSecuredConnection && !bootstrap) {
				Constants.LOGGER.fine("Challenging host " + host.toHostString() + "...");
				if (!mIdentMgr.challengeHost(host, conn)) {
					host.setState(PGridHost.HOST_STALE);
					Constants.LOGGER.fine("Challenge failed for host '" + host.toHostString() + "'!");
					conn.close();
					conn.setStatus(Connection.STATUS_ERROR);
					challengeSucceeded = false;
					mWriters.remove(host.getGUID());
				} else {
					host.resetOfflineTime();
					Constants.LOGGER.fine("Challenge succeeded for host '" + host.toHostString() + "'!");
				}
			}
		} // connection has not been established
		if (conn.getGUID() != null &&
				conn.getStatus() != Connection.STATUS_CONNECTED &&
				host.getState() != PGridHost.HOST_OFFLINE) {
			boolean updated = false;

			if (mSecuredConnection && !bootstrap) {
			host.setState(PGridHost.HOST_STALE);
			Constants.LOGGER.finer("Connection to host '" + host.toHostString() + "' failed, inform maintenance handler.");
		   
			// connection has failed. Notify the identity manager in order to perform
			// hypotetical correction of the routing table
			updated = mIdentMgr.stale(host);
			}

			//FIXME: planetlab
			/* if (!challengeSucceeded && host.getState() != Host.HOST_OFFLINE) {
					 //host.setState(Host.HOST_OK);
					 updated = true;
					 host.setState(Host.HOST_OK);
			 }*/
			//FIXME: planetlab

			// save the time stamp for a timeout.
			// TODO: add a single working thread to do the work.
			if (host.getState() == PGridHost.HOST_OFFLINE) {
				host.incOfflineTime();
				final long time = OFFLINE_PERIOD * host.getOfflineTime();
				Constants.LOGGER.finer("Host '" + host.toHostString() + "' is considered offline during the next " + time + "ms.");

				final PGridHost curHost = host;
				// start a timer

				Thread sleeper = new Thread("Offline timer for '" + host.toHostString() + "' - " + host.hashCode()) {
					public void run() {
						try {
							Thread.sleep(time);
						} catch (Exception e) {
							curHost.setState(PGridHost.HOST_OK);
						}
						curHost.setState(PGridHost.HOST_OK);
						curHost.resetMappingAttemps();
					}
				};

				sleeper.setDaemon(true);
				sleeper.start();
			}


			// if the host was not available try to reconnect
			if (updated && host.getState() == PGridHost.HOST_OK) {
				Constants.LOGGER.finer("Mapping found for host '" + host.toHostString() + "', try to connect to this host.");
				conn.close();
				conn.setStatus(Connection.STATUS_ERROR);

				Connection newConn = reconnect(host);
				if (newConn != null) {
					conn = newConn;
					return;
				} else if (updated && host.getState() == PGridHost.HOST_OK)
					Constants.LOGGER.finer("No new address for host '" + host.toHostString() + "'.");
				else if (host.getState() == PGridHost.HOST_STALE) Constants.LOGGER.finer("Host '" + host.toHostString() + "' is stale.");
			} else {
				Constants.LOGGER.finer("No new address found for host '" + host.toHostString() + "'.");
			}
		}

		if (conn.getStatus() == Connection.STATUS_CONNECTED) {
			mConnections.put(host.getGUID(), conn);
			host.resetMappingAttemps();
		} 
        
        
		// inform waiting thread that the connection is established
		synchronized (mConnectingWaiter.get(host)) {
			Object t = mConnectings.get(host);

			if (t != null) {
				synchronized (t) {
					t.notify();
				}
			}
		}
	}

	/**
	 * Initializes the Connection Manager.
	 *
	 * @param startListener <tt>true</tt> if the connection listener should be started, <tt>false</tt> otherwise.
	 */
	synchronized public void init(boolean startListener) {
		if (startListener) {
			Thread t = new Thread(new Listener(), "P-Grid Listener");
			t.setDaemon(true);
			t.start();
		}
		mMsgMgr = MessageManager.sharedInstance();
		mSecuredConnection = PGridP2P.sharedInstance().propertyBoolean(Properties.IDENTITY_CHALLENGE);

		if (mSecuredConnection)
			mIdentMgr = IdentityManager.sharedInstance();

		mAttemps = PGridP2P.sharedInstance().propertyInteger(Properties.IDENTITY_CONNECTION_ATTEMPS);
	}

	/**
	 * Try to reconnect a failed connection by updating the host IPort.
	 *
	 * @param host the host to connect.
	 * @return the established connection or null.
	 */
	private Connection reconnect(PGridHost host) {
		Constants.LOGGER.fine("try to reconnect '" + host.toHostString() + "' ...");
		
		// establish new connection
		Connection conn = new Connection(host);
		conn.setStatus(Connection.STATUS_CONNECTING);
		Connector myConnector = new Connector(conn);
		//myConnector.run();
		Thread t = new Thread(new Connector(conn), "Reconnect to '" + host.toHostString() + "' - " + conn.hashCode());
		t.setDaemon(true);
		t.start();

		synchronized (t) {
			try {
				t.join();
			} catch (InterruptedException e) {
			}
		}

		return conn;
	}

	/**
	 * The socket of the delivered connection was closed by the remote host.
	 *
	 * @param conn the connection.
	 */
	//FIXME: add a bool to notify connection
	public void socketClosed(Connection conn) {
		conn.close();

		mAcceptances.remove(conn);
		mConnections.remove(conn.getHost().getGUID());

	}

	/**
	 * Sends the delivered message to the delivered host.
	 *
	 * @param host the receiving host.
	 * @param msg  the message to send.
	 * @return <code>true</code> if the message was sent sucessfull, <code>false</code> otherwise.
	 */
	boolean sendPGridMessage(PGridHost host, PGridMessage msg) {
		// if the host is offline, don't try to send this message
		synchronized (host) {
			if (host.getState() == PGridHost.HOST_OFFLINE) {
				return false;
			}
		}
		//@todo has to run in a separate thread to not block the application
		Connection conn = connect(host);
		if (conn != null && conn.getStatus() == Connection.STATUS_CONNECTED) {
			PGridWriter writer = (PGridWriter)mWriters.get(host.getGUID());
			if (writer != null) {
				writer.sendMsg(msg);
				return true;
			}
		}
		return false;
	}

	/**
	 * Sends the delivered message to the delivered host through a specific connection.
	 *
	 * @param host the receiving host.
	 * @param conn the connection to the host.
	 * @param msg  the message to send.
	 * @return <code>true</code> if the message was sent sucessfull, <code>false</code> otherwise.
	 */
	boolean sendPGridMessage(PGridHost host, Connection conn, PGridMessage msg) {
		if (conn.getStatus() == Connection.STATUS_CONNECTED) {
			PGridWriter writer = (PGridWriter)mWriters.get(host.getGUID());
			if (writer != null) {
				writer.sendMsg(msg);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the connection for the given host and protocol.
	 *
	 * @param host the host.
	 * @return the connection.
	 */
	public Connection getConnection(PGridHost host) {
		Connection conn = null;
		// try to find existing connection
		if (host.getGUID() != null) {
			conn = (Connection)mConnections.get(host.getGUID());
		}
		return conn;
	}

	/**
	 * Returns all connections
	 *
	 * @return all connections.
	 */
	public Collection getConnections() {
		return mConnections.values();
	}

}
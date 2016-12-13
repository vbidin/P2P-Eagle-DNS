/**
 * $Id: MaintenanceManager.java,v 1.8 2005/12/22 07:44:56 john Exp $
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

package pgrid.core.maintenance;

import p2p.storage.events.StorageListener;
import pgrid.*;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.BootstrapMessage;
import pgrid.network.protocol.BootstrapReplyMessage;
import pgrid.network.protocol.ExchangeInvitationMessage;
import pgrid.network.protocol.ExchangeReplyMessage;

import java.util.Collection;
import java.util.logging.Level;

/**
 * This class manages all maintenance tasks of P-Grid.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class MaintenanceManager extends pgrid.util.WorkerThread implements StorageListener {

	/**
	 * Local peer sleeps.
	 */
	static final short PHASE_SLEEPS = 0;

	/**
	 * Local peer bootstraps.
	 */
	static final short PHASE_BOOTSTRAP = 1;

	/**
	 * Local peer replicates its data.
	 */
	static final short PHASE_REPLICATE = 3;

	/**
	 * Local peer runs.
	 */
	static final short PHASE_RUN = 4;

	/**
	 * Peer build an unstructured network by exchanging their fidget lists.
	 */
	static final short PHASE_FIDGET_EXCHANGE = 2;

	/**
	 * The PGrid bootstrapper.
	 */
	private Bootstrapper mBootstrapper = null;

	/**
	 * The time the construction starts.
	 */
	private long mConsructionStartTime = Long.MAX_VALUE;

	/**
	 * The initiator for Exchanges.
	 */
	private ExchangeInitiator mExchangeInitiator = null;

	/**
	 * The PGrid exchanger.
	 */
	private Exchanger mExchanger = null;

	/**
	 * The exchanger thread
	 */
	private Thread mExchangerThread;

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The current phase.
	 */
	private short mPhase = PHASE_SLEEPS;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The time the replication starts.
	 */
	private long mReplicationStartTime = Long.MAX_VALUE;

	/**
	 * The replicator.
	 */
	private Replicator mReplicator = null;

	private boolean isCondition = true;

	/**
	 * Data lock
	 */
	private final Object dataLock = new Object();

	/**
	 * Reference to the worker thread
	 */
	private Thread mThread;

	/**
	 * Constructs the Exchanger.
	 *
	 * @param p2p the P-Grid P2P facility.
	 */
	public MaintenanceManager(PGridP2P p2p) {
		super();
		mPGridP2P = p2p;
		Constants.LOGGER.config("starting P-Grid Maintenance manager ...");

		// Bootstrapper
		mBootstrapper = new Bootstrapper(mPGridP2P);

		// set the phase to sleep until we join.
		mPhase = PHASE_SLEEPS;

		// Replicator
		mReplicator = new Replicator(mPGridP2P);

		// Exchange Initiator
		mExchangeInitiator = new ExchangeInitiator(mPGridP2P);

		// Exchanger
		mExchanger = new Exchanger(mPGridP2P, this);
		mExchanger.setMinStorage(0);
		mExchangerThread = new Thread(mExchanger, "Exchanger");
		mExchangerThread.setDaemon(true);
		mExchangerThread.start();

		// register the maintenance manager as a storage listener
		// in order to compute the min storage
		StorageManager.getInstance().addStorageListener(this);


		// set worker thread timeout
		setTimeout(1000*10);
	}

	/**
	 * Tries to re-bootstrap in case exchanges were unsuccessful.
	 */
	void bootstrap() throws Exception {
		mBootstrapper.bootstrap();
	}

	/**
	 * Adds the given host to the list of bootstrap hosts.
	 *
	 * @param host the new bootstrap host.
	 */
	public void bootstrap(PGridHost host) {
		// if the current state is sleeping then the bootstrapping phase starts
		synchronized(dataLock) {
			if (mPhase == PHASE_SLEEPS)
				mPhase = PHASE_BOOTSTRAP;
		}
		mBootstrapper.addBootstrapHost(host);

		broadcast();
	}

	/**
	 * Returns the used bootstrap hosts.
	 * @return the bootstrap hosts.
	 */
	public Collection getBootstrapHosts() {
		return mBootstrapper.getHosts();
	}

	/**
	 * Returns true iff the local host is a bootstrap host
	 * @return true if the localhost is a bootstrap host
	 */
	public boolean isBootstrapHost() {
		return mBootstrapper.isBootstrapHost();
	}


	/**
	 * Returns the construction phase start time.
	 * @return the start time.
	 */
	long getConstractionStartTime() {
		long time;

		synchronized(dataLock) {
			time = mConsructionStartTime;
		}
		return time;
	}

	/**
	 * Returns the current phase of P-Grid.
	 * @return the current phase.
	 */
	short getPhase() {
		short phase;

		synchronized(dataLock) {
			phase = mPhase;
		}

		return phase;
	}

	/**
	 * Returns the replication phase start time.
	 * @return the start time.
	 */
	long getReplicationStartTime() {
		long time;

		synchronized(dataLock) {
			time = mReplicationStartTime;
		}
		return time;
	}

	protected void handleError(Throwable t) {
		if (t instanceof InterruptedException) {
			Constants.LOGGER.finer("Maintenance manager interupted.");
		} else {
			Constants.LOGGER.log(Level.WARNING, "Error in Maintenance thread", t);
		}
	}

	protected boolean isCondition() {
		return isCondition;
	}

	/**
	 * Joins the network.
	 */
	public void join() {
		synchronized(dataLock) {
			if (Constants.TESTS) {
				//mReplicationStartTime = mPGridP2P.propertyLong(Properties.REPLICATION_START_TIME);
				//mConsructionStartTime = mPGridP2P.propertyLong(Properties.CONSTRUCTION_START_TIME);
			}

			setInitExchanges(true);

			if (mPhase == PHASE_SLEEPS)
				mPhase = PHASE_BOOTSTRAP;
		}
		broadcast();
	}

	/**
	 * Processes a new addBootstrapHost request.
	 *
	 * @param bootstrap the addBootstrapHost request.
	 */
	public void newBootstrapRequest(BootstrapMessage bootstrap) {
		// respond with the local routing table, more precisely with the local fidget list
		BootstrapReplyMessage msg = null;
		if (bootstrap.getRoutingTable() == null) {
			long currentTime = System.currentTimeMillis();
			long replicationDelay;
			long consructionDelay;
			synchronized(dataLock) {
				replicationDelay = Math.max(0, mReplicationStartTime - currentTime);
				consructionDelay = Math.max(0, mConsructionStartTime - currentTime);
			}
			msg = new BootstrapReplyMessage(mPGridP2P.getLocalHost(), mPGridP2P.getRoutingTable(), replicationDelay, consructionDelay);
		} else
			msg = new BootstrapReplyMessage(mPGridP2P.getLocalHost(), mPGridP2P.getRoutingTable());
		mMsgMgr.sendMessage(bootstrap.getHeader().getHost(), msg, null);
		if (bootstrap.getRoutingTable() != null) {
			// merge the fidget lists if available
			mPGridP2P.getRoutingTable().unionFidgets(bootstrap.getRoutingTable());
			mPGridP2P.getRoutingTable().save();
		}
	}

	/**
	 * Processes a new addBootstrapHost response.
	 *
	 * @param bootstrapReply the addBootstrapHost response.
	 */
	public void newBootstrapReply(BootstrapReplyMessage bootstrapReply) {
		// copy received fidget hosts to the list of fidget hosts
		// one of this hosts will be used by the Exchanger to initiate the first Exchange
		mPGridP2P.getRoutingTable().unionFidgets(bootstrapReply.getRoutingTable());
		mPGridP2P.getRoutingTable().save();
		// if the local peer is in bootstrap phase => use the replication and construction delays
		synchronized(dataLock) {
			if (mPhase == PHASE_BOOTSTRAP) {
				long currentTime = System.currentTimeMillis();
				mConsructionStartTime = currentTime + bootstrapReply.getConstructionDelay();
				mReplicationStartTime = currentTime + bootstrapReply.getReplicationDelay();
			}

			// if more than one peer (the local peer) is in the fidget list => stop bootstrapping
			if (mPGridP2P.getRoutingTable().getFidgetVector().size() > 1) {
				mBootstrapper.setReady(false);

				mPhase = PHASE_FIDGET_EXCHANGE;

			}
		}
	}

	/**
	 * Processes a new exchange invitation request.
	 *
	 * @param exchangeInvitation the exchange invitation request.
	 */
	public void newExchangeInvitation(ExchangeInvitationMessage exchangeInvitation) {
		mExchanger.newExchangeInvitation(exchangeInvitation);
	}

	/**
	 * Processes a new exchange request.
	 *
	 * @param exchange the exchange request.
	 */
	public void newExchangeRequest(Exchange exchange) {
		mExchanger.newExchangeRequest(exchange);
	}

	/**
	 * A new exchange response was received.
	 *
	 * @param message the response message.
	 */
	public void newExchangeReply(ExchangeReplyMessage message) {
		mExchanger.newExchangeReply(message);
	}

	/**
	 * Invoked when a new replicate request was received.
	 *
	 * @param host      the requesting host.
	 * @param dataItems the data items to replicate.
	 */
	public void newReplicateRequest(PGridHost host, Collection dataItems) {
		mReplicator.replicateRequest(host, dataItems);
	}

	protected void prepareWorker() throws Exception {
		mThread = Thread.currentThread();
		Constants.LOGGER.config("Maintenance thread prepared.");
	}

	/**
	 * Invites one of the delivered hosts for an exchange.
	 *
	 * @param hosts     the hosts.
	 * @param recursion the recursion value.
	 * @param lCurrent  the current common length.
	 */
	public void randomExchange(Collection hosts, int recursion, int lCurrent) {
		mExchangeInitiator.randomExchange(hosts, recursion, lCurrent);
	}

	protected void releaseWorker() throws Exception {
		Constants.LOGGER.config("Maintenance thread released.");
	}

	/**
	 * Sets if PGridP2P should automatically initiate Exchanges.
	 *
	 * @param flag automatically initiate, or not.
	 */
	public void setInitExchanges(boolean flag) {
		if (Constants.TESTS) {
			if (flag) {
				mPGridP2P.getStatistics().InitExchanges = 1;
			} else {
				mPGridP2P.getStatistics().InitExchanges = 0;
			}
		}

		if (flag == mPGridP2P.propertyBoolean(pgrid.Properties.INIT_EXCHANGES))
			return;

		mPGridP2P.setProperty(pgrid.Properties.INIT_EXCHANGES, Boolean.toString(flag));
	}

	public void setReady(boolean cond) {
		isCondition = cond;
	}

	protected void work() throws Exception {

		// if we are a bootstrap host, set replication and construction value
		if (mBootstrapper.isBootstrapHost()) {
			// set the start times to default values if they are unknown
			mReplicationStartTime = mPGridP2P.propertyLong(Properties.REPLICATION_START_TIME);
			mConsructionStartTime = mPGridP2P.propertyLong(Properties.CONSTRUCTION_START_TIME);

			// if no properties are available, take default
			if ((mReplicationStartTime == 0)) {
				long currentTime = System.currentTimeMillis();
				mReplicationStartTime = currentTime + Constants.BOOTSTRAP_REPLICATION_DELAY;
				mPGridP2P.setProperty(Properties.REPLICATION_START_TIME, Long.toString(mReplicationStartTime));
			}
			if ((mConsructionStartTime == 0)) {
				long currentTime = System.currentTimeMillis();
				mConsructionStartTime = currentTime + Constants.BOOTSTRAP_CONSTRUCTION_DELAY;
				mPGridP2P.setProperty(Properties.CONSTRUCTION_START_TIME, Long.toString(mConsructionStartTime));
			}
		}

		// determine the current phase
		short phase;

		long currentTime = System.currentTimeMillis();
		synchronized(dataLock) {
			if (currentTime >= mConsructionStartTime) {
				mPhase = PHASE_RUN;
			} else if (currentTime >= mReplicationStartTime) {
				mPhase = PHASE_REPLICATE;
			}
			phase = mPhase;
		}

		if (Constants.TESTS)
			mPGridP2P.getStatistics().Phase = phase;

		if (mPGridP2P.propertyBoolean(Properties.INIT_EXCHANGES)) {

			if (phase == PHASE_SLEEPS) {
				// do nothing except sleeping
			} else if (phase == PHASE_BOOTSTRAP) {
				mBootstrapper.bootstrap();
			} else if (phase == PHASE_FIDGET_EXCHANGE) {
				mBootstrapper.fidgetExchange();
			} else if (phase == PHASE_REPLICATE) {
				mReplicator.replicate();
			} else if (phase == PHASE_RUN) {
				if (mExchanger.getMinStorage() == 0) {
					int minStorage;

					minStorage = StorageManager.getInstance().getDataTable().getOwnedDataItems().size()*Constants.REPLICATION_FACTOR;

					mExchanger.setMinStorage(minStorage);
				}
				mExchangeInitiator.inviteHost(mExchanger.getMinStorage());
			}
		}

		// wait a bit
		try{
			synchronized (getLock()) {
				getLock().wait(getTimeout());
			}
		} catch(InterruptedException e) {
			handleError(e);
		}

	}

	/**
	 * Shutdown
	 */
	public void shutdown() {
		setInitExchanges(false);
		mExchanger.shutdown();
		mThread.interrupt();
	}

	// reset the maintenance manager
	public void reset() {
		long currentTime = System.currentTimeMillis();
		synchronized(dataLock) {
			mPhase = PHASE_BOOTSTRAP;
			mExchanger.shutdown();
			mReplicationStartTime = currentTime + Constants.BOOTSTRAP_REPLICATION_DELAY;
			mConsructionStartTime = currentTime + Constants.BOOTSTRAP_CONSTRUCTION_DELAY;
			mPGridP2P.setProperty(Properties.REPLICATION_START_TIME, Long.toString(mReplicationStartTime));
			mPGridP2P.setProperty(Properties.CONSTRUCTION_START_TIME, Long.toString(mConsructionStartTime));
		}

		// Exchanger
		mExchanger = new Exchanger(mPGridP2P, this);
		mExchanger.setMinStorage(mPGridP2P.propertyInteger(Properties.EXCHANGE_MIN_STORAGE));
		mExchangerThread = new Thread(mExchanger, "Exchanger");
		mExchangerThread.setDaemon(true);
		mExchangerThread.start();
	}

	/**
	 * Returns true if it is time for exchanges
	 */
	public boolean isExchangeTime() {
		synchronized(dataLock) {
			return (mPhase == PHASE_RUN);
		}
	}

	/**
	 * Invoked when data items were added to the data table.
	 *
	 * @param items the added data item.
	 */
	public void dataItemsAdded(Collection items) {

	}

	/**
	 * Invoked when data items were removed from the data table.
	 *
	 * @param items the removed data item.
	 */
	public void dataItemsRemoved(Collection items) {
		/*int minStorage;

		if (mPGridP2P.getLocalPath().length() == 0)
			minStorage = mExchanger.getMinStorage()-items.size()*Constants.REPLICATION_FACTOR;
		else
			minStorage = mExchanger.getMinStorage()-items.size();

		mExchanger.setMinStorage(minStorage); */
	}

	/**
	 * Invoked when data items were updated from the data table.
	 *
	 * @param items the removed data item.
	 */
	public void dataItemsUpdated(Collection items) {
		//Nothing to do here
	}

	/**
	 * Invoked when the data table is cleared.
	 */
	public void dataTableCleared() {
		mExchanger.setMinStorage(0);
	}
}